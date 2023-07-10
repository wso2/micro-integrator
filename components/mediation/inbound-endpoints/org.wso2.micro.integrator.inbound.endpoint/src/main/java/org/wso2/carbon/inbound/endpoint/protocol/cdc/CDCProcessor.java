/*
 * Copyright (c) 2023, WSO2 LLC. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.inbound.endpoint.protocol.cdc;

import io.debezium.engine.ChangeEvent;
import io.debezium.engine.DebeziumEngine;
import io.debezium.engine.format.Json;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.MessageContext;
import org.apache.synapse.SynapseException;
import org.apache.synapse.core.SynapseEnvironment;
import org.apache.synapse.inbound.InboundProcessorParams;
import org.apache.synapse.inbound.InboundTaskProcessor;
import org.apache.synapse.mediators.Value;
import org.apache.synapse.task.TaskStartupObserver;
import org.apache.synapse.util.xpath.SynapseXPath;
import org.jaxen.JaxenException;
import org.wso2.carbon.inbound.endpoint.common.InboundRequestProcessorImpl;
import org.wso2.carbon.inbound.endpoint.common.InboundTask;
import org.wso2.carbon.inbound.endpoint.protocol.PollingConstants;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.wso2.carbon.inbound.endpoint.protocol.cdc.InboundCDCConstants.DEBEZIUM_DATABASE_ALLOW_PUBLIC_KEY_RETRIEVAL;
import static org.wso2.carbon.inbound.endpoint.protocol.cdc.InboundCDCConstants.DEBEZIUM_DATABASE_PASSWORD;
import static org.wso2.carbon.inbound.endpoint.protocol.cdc.InboundCDCConstants.DEBEZIUM_KEY_CONVERTER;
import static org.wso2.carbon.inbound.endpoint.protocol.cdc.InboundCDCConstants.DEBEZIUM_KEY_CONVERTER_SCHEMAS_ENABLE;
import static org.wso2.carbon.inbound.endpoint.protocol.cdc.InboundCDCConstants.DEBEZIUM_OFFSET_FLUSH_INTERVAL_MS;
import static org.wso2.carbon.inbound.endpoint.protocol.cdc.InboundCDCConstants.DEBEZIUM_OFFSET_STORAGE;
import static org.wso2.carbon.inbound.endpoint.protocol.cdc.InboundCDCConstants.DEBEZIUM_OFFSET_STORAGE_FILE_FILENAME;
import static org.wso2.carbon.inbound.endpoint.protocol.cdc.InboundCDCConstants.DEBEZIUM_SCHEMA_HISTORY_INTERNAL;
import static org.wso2.carbon.inbound.endpoint.protocol.cdc.InboundCDCConstants.DEBEZIUM_SCHEMA_HISTORY_INTERNAL_FILE_FILENAME;
import static org.wso2.carbon.inbound.endpoint.protocol.cdc.InboundCDCConstants.DEBEZIUM_TOPIC_PREFIX;
import static org.wso2.carbon.inbound.endpoint.protocol.cdc.InboundCDCConstants.DEBEZIUM_VALUE_CONVERTER;
import static org.wso2.carbon.inbound.endpoint.protocol.cdc.InboundCDCConstants.DEBEZIUM_VALUE_CONVERTER_SCHEMAS_ENABLE;
import static org.wso2.carbon.inbound.endpoint.protocol.cdc.InboundCDCConstants.TRUE;

public class CDCProcessor extends InboundRequestProcessorImpl implements TaskStartupObserver, InboundTaskProcessor {

    private CDCPollingConsumer pollingConsumer;
    private Properties cdcProperties;
    private String injectingSeq;
    private String onErrorSeq;
    private boolean sequential;

    private static final String SECURE_VAULT_REGEX = "(wso2:vault-lookup\\('(.*?)'\\))";
    private static Pattern vaultLookupPattern = Pattern.compile(SECURE_VAULT_REGEX);
    private static final String ENDPOINT_POSTFIX = "CDC" + COMMON_ENDPOINT_POSTFIX;
    private static final String FILE_OFFSET_STORAGE_CLASS = "org.apache.kafka.connect.storage.FileOffsetBackingStore";
    private static final String FILE_SCHEMA_HISTORY_STORAGE_CLASS = "io.debezium.storage.file.history.FileSchemaHistory";
    private static final Log LOGGER = LogFactory.getLog(CDCProcessor.class);
    protected static Map<String, BlockingQueue> inboundEpEventQueueMap = new HashMap();
    private ExecutorService executorService = null;

    public CDCProcessor(InboundProcessorParams params) {
        this.name = params.getName();
        this.injectingSeq = params.getInjectingSeq();
        this.onErrorSeq = params.getOnErrorSeq();
        this.synapseEnvironment = params.getSynapseEnvironment();
        this.cdcProperties = params.getProperties();
        setProperties();
        try {
            this.interval = Long.parseLong(cdcProperties.getProperty(PollingConstants.INBOUND_ENDPOINT_INTERVAL));
        } catch (NumberFormatException nfe) {
            throw new SynapseException("Invalid numeric value for interval.", nfe);
        } catch (Exception e) {
            throw new SynapseException("Invalid value for interval.", e);
        }
        this.sequential = true;
        if (cdcProperties.getProperty(PollingConstants.INBOUND_ENDPOINT_SEQUENTIAL) != null) {
            this.sequential = Boolean
                    .parseBoolean(cdcProperties.getProperty(PollingConstants.INBOUND_ENDPOINT_SEQUENTIAL));
        }
        this.coordination = true;
        if (cdcProperties.getProperty(PollingConstants.INBOUND_COORDINATION) != null) {
            this.coordination = Boolean.parseBoolean(cdcProperties.getProperty(PollingConstants.INBOUND_COORDINATION));
        }
        if (!inboundEpEventQueueMap.containsKey(this.name)) {
            BlockingQueue<ChangeEvent<String, String>> eventQueue = new LinkedBlockingQueue<>();
            inboundEpEventQueueMap.put(this.name, eventQueue);
        }
    }

    private void setProperties () {
        LOGGER.info("Initializing the properties");
        try {
            if (this.cdcProperties.getProperty(DEBEZIUM_OFFSET_STORAGE) == null) {
                this.cdcProperties.setProperty(DEBEZIUM_OFFSET_STORAGE, FILE_OFFSET_STORAGE_CLASS);
            }
            if (this.cdcProperties.getProperty(DEBEZIUM_OFFSET_STORAGE).equals(FILE_OFFSET_STORAGE_CLASS)) {
                String filePath;
                if (this.cdcProperties.getProperty(DEBEZIUM_OFFSET_STORAGE_FILE_FILENAME) == null) {
                    filePath = "cdc/offsetStorage/" + this.name + "_.dat";
                } else {
                    filePath = this.cdcProperties.getProperty(DEBEZIUM_OFFSET_STORAGE_FILE_FILENAME);
                }
                createFile(filePath);
                this.cdcProperties.setProperty(DEBEZIUM_OFFSET_STORAGE_FILE_FILENAME, filePath);
            }

            if (this.cdcProperties.getProperty(DEBEZIUM_OFFSET_FLUSH_INTERVAL_MS) == null) {
                this.cdcProperties.setProperty(DEBEZIUM_OFFSET_FLUSH_INTERVAL_MS, "1000");
            }

            String passwordString = this.cdcProperties.getProperty(DEBEZIUM_DATABASE_PASSWORD);
            SynapseEnvironment synapseEnvironment = this.synapseEnvironment;
            MessageContext messageContext = synapseEnvironment.createMessageContext();

            this.cdcProperties.setProperty(DEBEZIUM_DATABASE_PASSWORD, resolveSecureVault(messageContext, passwordString));

            if (this.cdcProperties.getProperty(DEBEZIUM_DATABASE_ALLOW_PUBLIC_KEY_RETRIEVAL) == null) {
                this.cdcProperties.setProperty(DEBEZIUM_DATABASE_ALLOW_PUBLIC_KEY_RETRIEVAL, TRUE);
            }

            if (this.cdcProperties.getProperty(DEBEZIUM_TOPIC_PREFIX) == null) {
                this.cdcProperties.setProperty(DEBEZIUM_TOPIC_PREFIX, this.name +"_topic");
            }

            if (this.cdcProperties.getProperty(DEBEZIUM_VALUE_CONVERTER) == null) {
                this.cdcProperties.setProperty(DEBEZIUM_VALUE_CONVERTER, "org.apache.kafka.connect.json.JsonConverter");
            }
            if (this.cdcProperties.getProperty(DEBEZIUM_KEY_CONVERTER) == null) {
                this.cdcProperties.setProperty(DEBEZIUM_KEY_CONVERTER, "org.apache.kafka.connect.json.JsonConverter");
            }
            if (this.cdcProperties.getProperty(DEBEZIUM_KEY_CONVERTER_SCHEMAS_ENABLE) == null) {
                this.cdcProperties.setProperty(DEBEZIUM_KEY_CONVERTER_SCHEMAS_ENABLE, TRUE);
            }
            if (this.cdcProperties.getProperty(DEBEZIUM_VALUE_CONVERTER_SCHEMAS_ENABLE) == null) {
                this.cdcProperties.setProperty(DEBEZIUM_VALUE_CONVERTER_SCHEMAS_ENABLE, TRUE);
            }

            if (this.cdcProperties.getProperty(DEBEZIUM_SCHEMA_HISTORY_INTERNAL) == null) {
                this.cdcProperties.setProperty(DEBEZIUM_SCHEMA_HISTORY_INTERNAL, FILE_SCHEMA_HISTORY_STORAGE_CLASS);
            }

            if (this.cdcProperties.getProperty(DEBEZIUM_SCHEMA_HISTORY_INTERNAL).equals(FILE_SCHEMA_HISTORY_STORAGE_CLASS)) {
                String filePath;
                if (this.cdcProperties.getProperty(DEBEZIUM_SCHEMA_HISTORY_INTERNAL_FILE_FILENAME) == null) {
                    filePath = "cdc/schemaHistory/" + this.name + "_.dat";
                } else {
                    filePath = this.cdcProperties.getProperty(DEBEZIUM_SCHEMA_HISTORY_INTERNAL_FILE_FILENAME);
                }
                createFile(filePath);
                this.cdcProperties.setProperty(DEBEZIUM_SCHEMA_HISTORY_INTERNAL_FILE_FILENAME, filePath);
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (NullPointerException e) {
            LOGGER.error("A required property value is not defined", e);
            throw new RuntimeException(e);
        }
    }

    private void createFile (String filePath) throws IOException {
        File file = new File(filePath);
        file.getParentFile().mkdirs();
        if(!file.exists()) {
            file.createNewFile();
        }
    }

    private static synchronized String resolveSecureVault(MessageContext messageContext, String passwordString) {
        if (passwordString == null) {
            return null;
        }
        Matcher lookupMatcher = vaultLookupPattern.matcher(passwordString);
        String resolvedValue = "";
        if (lookupMatcher.find()) {
            Value expression;
            String expressionStr = lookupMatcher.group(1);
            try {
                expression = new Value(new SynapseXPath(expressionStr));

            } catch (JaxenException e) {
                throw new SynapseException("Error while building the expression : " + expressionStr, e);
            }
            resolvedValue = expression.evaluateValue(messageContext);
            if (StringUtils.isEmpty(resolvedValue)) {
                LOGGER.warn("Found Empty value for expression : " + expression.getExpression());
                resolvedValue = "";
            }
        } else {
            resolvedValue = passwordString;
        }
        return resolvedValue;
    }


    /**
     * This will be called at the time of synapse artifact deployment.
     */
    public void init() {
        LOGGER.info("Inbound CDC listener " + name + " starting ...");
        pollingConsumer = new CDCPollingConsumer(cdcProperties, name, synapseEnvironment, interval);
        pollingConsumer.registerHandler(new CDCInjectHandler(injectingSeq, onErrorSeq, sequential,
                synapseEnvironment, cdcProperties));

        DebeziumEngine<ChangeEvent<String, String>> engine = DebeziumEngine.create(Json.class)
                .using(this.cdcProperties)
                .notifying(record -> {
                    try {
                        inboundEpEventQueueMap.get(this.name).offer(record, interval, TimeUnit.MILLISECONDS);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }).build();

        executorService = Executors.newSingleThreadExecutor();
        executorService.execute(engine);
        start();
    }


    /**
     * Register/start the schedule service
     */
    public void start() {
        InboundTask task = new CDCTask(pollingConsumer, interval);
        start(task, ENDPOINT_POSTFIX);
    }

    /**
     * Remove inbound endpoints.
     *
     * @param removeTask Whether to remove scheduled task from the registry or not.
     */
    @Override
    public void destroy(boolean removeTask) {
        if (removeTask) {
            destroy();
            executorService.shutdown();
        }
    }

    @Override
    public void update() {
        // This will not be called for inbound endpoints
    }
}
