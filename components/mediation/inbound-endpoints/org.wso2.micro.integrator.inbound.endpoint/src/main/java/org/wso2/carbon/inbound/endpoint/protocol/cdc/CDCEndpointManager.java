package org.wso2.carbon.inbound.endpoint.protocol.cdc;

import io.debezium.engine.ChangeEvent;
import io.debezium.engine.DebeziumEngine;
import io.debezium.engine.format.Json;
import org.apache.synapse.mediators.Value;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.MessageContext;
import org.apache.synapse.SynapseException;
import org.apache.synapse.core.SynapseEnvironment;
import org.apache.synapse.inbound.InboundProcessorParams;
import org.apache.synapse.util.xpath.SynapseXPath;
import org.jaxen.JaxenException;
import org.wso2.carbon.inbound.endpoint.common.AbstractInboundEndpointManager;

import java.io.File;
import java.io.IOException;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.wso2.carbon.inbound.endpoint.common.Constants.SUPER_TENANT_DOMAIN_NAME;
import static org.wso2.carbon.inbound.endpoint.protocol.cdc.InboundCDCConstants.DEBEZIUM_CONNECTOR_CLASS;
import static org.wso2.carbon.inbound.endpoint.protocol.cdc.InboundCDCConstants.DEBEZIUM_DATABASE_ALLOW_PUBLIC_KEY_RETRIEVAL;
import static org.wso2.carbon.inbound.endpoint.protocol.cdc.InboundCDCConstants.DEBEZIUM_DATABASE_DBNAME;
import static org.wso2.carbon.inbound.endpoint.protocol.cdc.InboundCDCConstants.DEBEZIUM_DATABASE_HOSTNAME;
import static org.wso2.carbon.inbound.endpoint.protocol.cdc.InboundCDCConstants.DEBEZIUM_DATABASE_PASSWORD;
import static org.wso2.carbon.inbound.endpoint.protocol.cdc.InboundCDCConstants.DEBEZIUM_DATABASE_PORT;
import static org.wso2.carbon.inbound.endpoint.protocol.cdc.InboundCDCConstants.DEBEZIUM_DATABASE_SERVER_ID;
import static org.wso2.carbon.inbound.endpoint.protocol.cdc.InboundCDCConstants.DEBEZIUM_DATABASE_USER;
import static org.wso2.carbon.inbound.endpoint.protocol.cdc.InboundCDCConstants.DEBEZIUM_KEY_CONVERTER;
import static org.wso2.carbon.inbound.endpoint.protocol.cdc.InboundCDCConstants.DEBEZIUM_KEY_CONVERTER_SCHEMAS_ENABLE;
import static org.wso2.carbon.inbound.endpoint.protocol.cdc.InboundCDCConstants.DEBEZIUM_OFFSET_FLUSH_INTERVAL_MS;
import static org.wso2.carbon.inbound.endpoint.protocol.cdc.InboundCDCConstants.DEBEZIUM_OFFSET_STORAGE;
import static org.wso2.carbon.inbound.endpoint.protocol.cdc.InboundCDCConstants.DEBEZIUM_OFFSET_STORAGE_FILE_FILENAME;
import static org.wso2.carbon.inbound.endpoint.protocol.cdc.InboundCDCConstants.DEBEZIUM_OPERATIONS_EXCLUDE_LIST;
import static org.wso2.carbon.inbound.endpoint.protocol.cdc.InboundCDCConstants.DEBEZIUM_MAX_THREADS;
import static org.wso2.carbon.inbound.endpoint.protocol.cdc.InboundCDCConstants.DEBEZIUM_NAME;
import static org.wso2.carbon.inbound.endpoint.protocol.cdc.InboundCDCConstants.DEBEZIUM_SCHEMA_HISTORY_INTERNAL;
import static org.wso2.carbon.inbound.endpoint.protocol.cdc.InboundCDCConstants.DEBEZIUM_SCHEMA_HISTORY_INTERNAL_FILE_FILENAME;
import static org.wso2.carbon.inbound.endpoint.protocol.cdc.InboundCDCConstants.DEBEZIUM_SNAPSHOT_MODE;
import static org.wso2.carbon.inbound.endpoint.protocol.cdc.InboundCDCConstants.DEBEZIUM_TABLES_INCLUDE_LIST;
import static org.wso2.carbon.inbound.endpoint.protocol.cdc.InboundCDCConstants.DEBEZIUM_TOPIC_PREFIX;
import static org.wso2.carbon.inbound.endpoint.protocol.cdc.InboundCDCConstants.DEBEZIUM_VALUE_CONVERTER;
import static org.wso2.carbon.inbound.endpoint.protocol.cdc.InboundCDCConstants.DEBEZIUM_VALUE_CONVERTER_SCHEMAS_ENABLE;
import static org.wso2.carbon.inbound.endpoint.protocol.cdc.InboundCDCConstants.TRUE;

public class CDCEndpointManager extends AbstractInboundEndpointManager {

    private static final Log log = LogFactory.getLog(CDCEndpointManager.class);

    private static CDCEndpointManager instance = null;
    private InboundCDCEventExecutor eventExecutor;

    private static final String SECURE_VAULT_REGEX = "(wso2:vault-lookup\\('(.*?)'\\))";
    private static Pattern vaultLookupPattern = Pattern.compile(SECURE_VAULT_REGEX);

    private CDCEndpointManager() {
        super();
    }

    public static CDCEndpointManager getInstance() {
        if (instance == null) {
            instance = new CDCEndpointManager();
        }
        return instance;
    }

    @Override
    public boolean startListener(int port, String name, InboundProcessorParams inboundParameters) {

        if (CDCEventExecutorManager.getInstance().isRegisteredExecutor(port)) {
            log.info("CDC Listener already started on port " + port);
            return true;
        }

        log.info("Starting CDC Listener on port " + port);

        eventExecutor = new InboundCDCEventExecutor();
        CDCEventExecutorManager.getInstance().registerEventExecutor(port, eventExecutor);
        Properties props = setProperties(inboundParameters);

        CDCSourceHandler sourceHandler = new CDCSourceHandler(port, inboundParameters);
        DebeziumEngine<ChangeEvent<String, String>> engine = DebeziumEngine.create(Json.class)
                .using(props)
                .notifying(record -> {
                    sourceHandler.requestReceived(record);
                    System.out.println(record);
                }).build();

        eventExecutor.getExecutorService().execute(engine);
        log.info("Debezium engine started");

        return true;
    }

    private Properties setProperties (InboundProcessorParams params) {
        Properties inboundProperties = params.getProperties();
        log.info("Initializing the properties");
        final Properties props = new Properties();
        try {
            props.setProperty(DEBEZIUM_NAME, inboundProperties.getProperty(DEBEZIUM_NAME));
            if (inboundProperties.getProperty(DEBEZIUM_SNAPSHOT_MODE) != null) {
                props.setProperty(DEBEZIUM_SNAPSHOT_MODE, inboundProperties.getProperty(DEBEZIUM_SNAPSHOT_MODE));
            }

            if (inboundProperties.getProperty(DEBEZIUM_MAX_THREADS) != null) {
                props.setProperty(DEBEZIUM_MAX_THREADS, inboundProperties.getProperty(DEBEZIUM_MAX_THREADS));
            }

            if (inboundProperties.getProperty(DEBEZIUM_OFFSET_STORAGE) != null) {
                props.setProperty(DEBEZIUM_OFFSET_STORAGE, inboundProperties.getProperty(DEBEZIUM_OFFSET_STORAGE));
            } else {
                props.setProperty(DEBEZIUM_OFFSET_STORAGE, "org.apache.kafka.connect.storage.FileOffsetBackingStore");
            }

            if (inboundProperties.getProperty(DEBEZIUM_OFFSET_STORAGE_FILE_FILENAME) != null) {
                props.setProperty(DEBEZIUM_OFFSET_STORAGE_FILE_FILENAME, inboundProperties.getProperty(DEBEZIUM_OFFSET_STORAGE_FILE_FILENAME));
            } else {
                String filePath = "cdc/offsetStorage/" + params.getName() + "_.dat";
                createFile(filePath);
                props.setProperty(DEBEZIUM_OFFSET_STORAGE_FILE_FILENAME, filePath);
            }

            if (inboundProperties.getProperty(DEBEZIUM_OFFSET_FLUSH_INTERVAL_MS) != null) {
                props.setProperty(DEBEZIUM_OFFSET_FLUSH_INTERVAL_MS, inboundProperties.getProperty(DEBEZIUM_OFFSET_FLUSH_INTERVAL_MS));
            } else {
                props.setProperty(DEBEZIUM_OFFSET_FLUSH_INTERVAL_MS, "1000");
            }

            /* begin connector properties */
            props.setProperty(DEBEZIUM_CONNECTOR_CLASS, inboundProperties.getProperty(DEBEZIUM_CONNECTOR_CLASS));
            props.setProperty(DEBEZIUM_DATABASE_HOSTNAME, inboundProperties.getProperty(DEBEZIUM_DATABASE_HOSTNAME));
            props.setProperty(DEBEZIUM_DATABASE_PORT, inboundProperties.getProperty(DEBEZIUM_DATABASE_PORT));
            props.setProperty(DEBEZIUM_DATABASE_USER, inboundProperties.getProperty(DEBEZIUM_DATABASE_USER));

            String passwordString = inboundProperties.getProperty(DEBEZIUM_DATABASE_PASSWORD);
            SynapseEnvironment synapseEnvironment = params.getSynapseEnvironment();
            MessageContext messageContext = synapseEnvironment.createMessageContext();

            props.setProperty(DEBEZIUM_DATABASE_PASSWORD, resolveSecureVault(messageContext, passwordString));

            props.setProperty(DEBEZIUM_DATABASE_DBNAME, inboundProperties.getProperty(DEBEZIUM_DATABASE_DBNAME));
            props.setProperty(DEBEZIUM_DATABASE_SERVER_ID, inboundProperties.getProperty(DEBEZIUM_DATABASE_SERVER_ID));

            if (inboundProperties.getProperty(DEBEZIUM_DATABASE_ALLOW_PUBLIC_KEY_RETRIEVAL) != null) {
                props.setProperty(DEBEZIUM_DATABASE_ALLOW_PUBLIC_KEY_RETRIEVAL, inboundProperties.getProperty(DEBEZIUM_DATABASE_ALLOW_PUBLIC_KEY_RETRIEVAL));
            } else {
                props.setProperty(DEBEZIUM_DATABASE_ALLOW_PUBLIC_KEY_RETRIEVAL, TRUE);
            }

            if (inboundProperties.getProperty(DEBEZIUM_TOPIC_PREFIX) != null) {
                props.setProperty(DEBEZIUM_TOPIC_PREFIX, inboundProperties.getProperty(DEBEZIUM_TOPIC_PREFIX));
            } else {
                props.setProperty(DEBEZIUM_TOPIC_PREFIX, params.getName() +"_topic");
            }

            props.setProperty(DEBEZIUM_VALUE_CONVERTER, "org.apache.kafka.connect.json.JsonConverter");
            props.setProperty(DEBEZIUM_KEY_CONVERTER, "org.apache.kafka.connect.json.JsonConverter");
            props.setProperty(DEBEZIUM_KEY_CONVERTER_SCHEMAS_ENABLE, TRUE);
            props.setProperty(DEBEZIUM_VALUE_CONVERTER_SCHEMAS_ENABLE, TRUE);

            if (inboundProperties.getProperty(DEBEZIUM_SCHEMA_HISTORY_INTERNAL) != null) {
                props.setProperty(DEBEZIUM_SCHEMA_HISTORY_INTERNAL, inboundProperties.getProperty(DEBEZIUM_SCHEMA_HISTORY_INTERNAL));
            } else {
                props.setProperty(DEBEZIUM_SCHEMA_HISTORY_INTERNAL, "io.debezium.storage.file.history.FileSchemaHistory");
            }

            if (inboundProperties.getProperty(DEBEZIUM_SCHEMA_HISTORY_INTERNAL_FILE_FILENAME) != null) {
                props.setProperty(DEBEZIUM_SCHEMA_HISTORY_INTERNAL_FILE_FILENAME, inboundProperties.getProperty(DEBEZIUM_SCHEMA_HISTORY_INTERNAL_FILE_FILENAME));
            } else {
                String filePath = "cdc/schemaHistory/" + params.getName() + "_.dat";
                createFile(filePath);
                props.setProperty(DEBEZIUM_SCHEMA_HISTORY_INTERNAL_FILE_FILENAME, filePath);
            }

            if (inboundProperties.getProperty(DEBEZIUM_TABLES_INCLUDE_LIST) != null) {
                props.setProperty(DEBEZIUM_TABLES_INCLUDE_LIST, inboundProperties.getProperty(DEBEZIUM_TABLES_INCLUDE_LIST));
            }

            if (inboundProperties.getProperty(DEBEZIUM_OPERATIONS_EXCLUDE_LIST) != null) {
                props.setProperty(DEBEZIUM_OPERATIONS_EXCLUDE_LIST, inboundProperties.getProperty(DEBEZIUM_OPERATIONS_EXCLUDE_LIST));
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (NullPointerException e) {
            log.error("A required property value is not defined", e);
            throw new RuntimeException(e);
        }
        return props;
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
                log.warn("Found Empty value for expression : " + expression.getExpression());
                resolvedValue = "";
            }
        } else {
            resolvedValue = passwordString;
        }
        return resolvedValue;
    }

    @Override
    public boolean startEndpoint(int port, String name, InboundProcessorParams inboundParameters) {
        log.info("Starting CDC Endpoint on port " + port);
        dataStore.registerListeningEndpoint(port, SUPER_TENANT_DOMAIN_NAME, InboundCDCConstants.CDC, name,
                inboundParameters);

        boolean start = startListener(port, name, inboundParameters);

        if (start) {
            //do nothing
        } else {
            dataStore.unregisterListeningEndpoint(port, SUPER_TENANT_DOMAIN_NAME);
            return false;
        }
        return true;
    }

    private void createFile (String filePath) throws IOException {
        File file = new File(filePath);
        file.getParentFile().mkdirs();
        if(!file.exists()) {
            file.createNewFile();
        }
    }

    @Override
    public void closeEndpoint(int port) {
        log.info("Closing CDC Endpoint on port " + port);
        eventExecutor.getExecutorService().shutdown();
        dataStore.unregisterListeningEndpoint(port, SUPER_TENANT_DOMAIN_NAME);

        if (!CDCEventExecutorManager.getInstance().isRegisteredExecutor(port)) {
            log.info("Listener Endpoint is not started");
            return;
        } else if (dataStore.isEndpointRegistryEmpty(port)) {
            CDCEventExecutorManager.getInstance().shutdownExecutor(port);
        }

    }

}
