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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.SynapseException;
import org.apache.synapse.core.SynapseEnvironment;
import org.apache.synapse.inbound.InboundProcessorParams;
import org.apache.synapse.inbound.InboundTaskProcessor;
import org.apache.synapse.task.TaskStartupObserver;
import org.apache.synapse.util.resolver.SecureVaultResolver;
import org.wso2.carbon.inbound.endpoint.common.InboundRequestProcessorImpl;
import org.wso2.carbon.inbound.endpoint.common.InboundTask;
import org.wso2.carbon.inbound.endpoint.protocol.PollingConstants;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.wso2.carbon.inbound.endpoint.protocol.cdc.InboundCDCConstants.DEBEZIUM_ALLOWED_OPERATIONS;
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
import static org.wso2.carbon.inbound.endpoint.protocol.cdc.InboundCDCConstants.DEBEZIUM_SKIPPED_OPERATIONS;
import static org.wso2.carbon.inbound.endpoint.protocol.cdc.InboundCDCConstants.TRUE;

public class CDCProcessor extends InboundRequestProcessorImpl implements TaskStartupObserver, InboundTaskProcessor {

    private CDCPollingConsumer pollingConsumer;
    private Properties cdcProperties;
    private String injectingSeq;
    private String onErrorSeq;
    private boolean sequential;
    private static final String ENDPOINT_POSTFIX = "CDC" + COMMON_ENDPOINT_POSTFIX;
    private static final String FILE_OFFSET_STORAGE_CLASS = "org.apache.kafka.connect.storage.FileOffsetBackingStore";
    private static final String FILE_SCHEMA_HISTORY_STORAGE_CLASS = "io.debezium.storage.file.history.FileSchemaHistory";
    private static final Log logger = LogFactory.getLog(CDCProcessor.class);

    private enum operations {create, update, delete, truncate};
    private enum opCodes {c, u, d, t};

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
    }

    private void setProperties () {
        logger.info("Initializing the CDC properties");
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

            this.cdcProperties.setProperty(DEBEZIUM_DATABASE_PASSWORD, SecureVaultResolver.resolve(synapseEnvironment, passwordString));

            if (this.cdcProperties.getProperty(DEBEZIUM_DATABASE_ALLOW_PUBLIC_KEY_RETRIEVAL) == null) {
                this.cdcProperties.setProperty(DEBEZIUM_DATABASE_ALLOW_PUBLIC_KEY_RETRIEVAL, TRUE);
            }

            if (this.cdcProperties.getProperty(DEBEZIUM_ALLOWED_OPERATIONS) != null) {
                this.cdcProperties.setProperty(DEBEZIUM_SKIPPED_OPERATIONS,
                        getSkippedOperationsString(this.cdcProperties.getProperty(DEBEZIUM_ALLOWED_OPERATIONS)));
            }

            if (this.cdcProperties.getProperty(DEBEZIUM_TOPIC_PREFIX) == null) {
                this.cdcProperties.setProperty(DEBEZIUM_TOPIC_PREFIX, this.name +"_topic");
            }

            // set the output format as json in a way a user cannot override
            this.cdcProperties.setProperty(DEBEZIUM_VALUE_CONVERTER, "org.apache.kafka.connect.json.JsonConverter");
            this.cdcProperties.setProperty(DEBEZIUM_KEY_CONVERTER, "org.apache.kafka.connect.json.JsonConverter");
            this.cdcProperties.setProperty(DEBEZIUM_KEY_CONVERTER_SCHEMAS_ENABLE, TRUE);
            this.cdcProperties.setProperty(DEBEZIUM_VALUE_CONVERTER_SCHEMAS_ENABLE, TRUE);

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
            String msg = "Error while setting the CDC Properties";
            logger.error(msg);
            throw new RuntimeException(msg, e);
        }
    }

    private void createFile (String filePath) throws IOException {
        File file = new File(filePath);
        file.getParentFile().mkdirs();
        if(!file.exists()) {
            file.createNewFile();
        }
    }


    /**
     * This will be called at the time of synapse artifact deployment.
     */
    public void init() {
        logger.info("Inbound CDC listener " + name + " starting ...");
        pollingConsumer = new CDCPollingConsumer(cdcProperties, name, synapseEnvironment, interval);
        pollingConsumer.registerHandler(new CDCInjectHandler(injectingSeq, onErrorSeq, sequential,
                synapseEnvironment, cdcProperties));
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
            pollingConsumer.destroy();
        }
    }

    @Override
    public void update() {
        // This will not be called for inbound endpoints
    }

    private String getOpCode(String op) {
        if (op != null) {
            switch (operations.valueOf(op)) {
                case create:
                    return opCodes.c.toString();
                case update:
                    return opCodes.u.toString();
                case delete:
                    return opCodes.d.toString();
                case truncate:
                    return opCodes.t.toString();
            }
        }
        return "";
    }

    /**
     * Get the comma separated list containing allowed operations and returns the string of skipped operation codes
     * @param allowedOperationsString string
     * @return the coma separated string of skipped operation codes
     */
    private String getSkippedOperationsString(String allowedOperationsString) {
        List<String> allOperations = Stream.of(opCodes.values()).map(Enum :: toString).collect(Collectors.toList());
        Set<String> allowedOperationsSet = Stream.of(allowedOperationsString.split(",")).
                map(String :: trim).map(String :: toLowerCase).map(op -> getOpCode(op)).
                collect(Collectors.toSet());
        allOperations.removeAll(allowedOperationsSet);
        return String.join(",", allOperations);
    }
}
