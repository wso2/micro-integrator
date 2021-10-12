/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
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

package org.wso2.micro.integrator.management.apis;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.configuration.PropertiesConfigurationLayout;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.Level;
import org.apache.synapse.MessageContext;
import org.apache.synapse.commons.json.JsonUtil;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.json.JSONArray;
import org.json.JSONObject;
import org.wso2.carbon.utils.ServerConstants;
import org.wso2.micro.core.util.AuditLogger;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class LoggingResource extends ApiResource {

    private static final Log log = LogFactory.getLog(LoggingResource.class);

    private static final Level[] logLevels =
            new Level[] { Level.OFF, Level.TRACE, Level.DEBUG, Level.INFO, Level.WARN, Level.ERROR, Level.FATAL };

    private JSONObject jsonBody;
    private String filePath = System.getProperty(ServerConstants.CARBON_CONFIG_DIR_PATH) + File.separator
            + "log4j2.properties";
    private File logPropFile = new File(filePath);

    private PropertiesConfiguration config;
    private PropertiesConfigurationLayout layout;

    private static final String EXCEPTION_MSG = "Exception while getting logger data ";
    private static final String LOGGER_PREFIX = "logger.";
    private static final String LOGGER_LEVEL_SUFFIX = ".level";
    private static final String LOGGER_NAME_SUFFIX = ".name";
    private static final String LOGGER_CLASS = "loggerClass";
    private static final String LOGGERS_PROPERTY = "loggers";

    public LoggingResource(String urlTemplate) {
        super(urlTemplate);
    }

    @Override
    public Set<String> getMethods() {

        Set<String> methods = new HashSet<>();
        methods.add(Constants.HTTP_GET);
        methods.add(Constants.HTTP_METHOD_PATCH);
        return methods;
    }

    @Override
    public boolean invoke(MessageContext messageContext) {

        buildMessage(messageContext);

        org.apache.axis2.context.MessageContext axis2MessageContext =
                ((Axis2MessageContext) messageContext).getAxis2MessageContext();

        axis2MessageContext.removeProperty(Constants.NO_ENTITY_BODY);

        JSONObject jsonPayload = new JSONObject(JsonUtil.jsonPayloadToString(axis2MessageContext));
        String httpMethod = axis2MessageContext.getProperty("HTTP_METHOD").toString();

        if (httpMethod.equals(Constants.HTTP_GET)) {
            String param = Utils.getQueryParameter(messageContext, Constants.LOGGER_NAME);
            if (Objects.nonNull(param)) {
                try {
                    if (isLoggerExist(param)) {
                        jsonBody = getLoggerData(axis2MessageContext, param);
                    } else {
                        jsonBody = createJsonError("Logger name ('" + param + "') not found", "", axis2MessageContext);
                    }
                } catch (IOException exception) {
                    jsonBody = createJsonError(EXCEPTION_MSG, exception, axis2MessageContext);
                }
            } else {
                Object payload;
                try {
                    payload = getAllLoggerDetails(axis2MessageContext);
                } catch (IOException e) {
                    payload = createJsonError(EXCEPTION_MSG, e, axis2MessageContext);
                }
                Utils.setJsonPayLoad(axis2MessageContext, payload);
                return true;
            }
        } else {
            if (jsonPayload.has(Constants.LOGGING_LEVEL)) {
                String logLevel = jsonPayload.getString(Constants.LOGGING_LEVEL);
                if (!isValidLogLevel(logLevel)) {
                    // 400-Bad Request Invalid loggingLevel
                    jsonBody = createJsonError("Invalid log level " + logLevel, "", axis2MessageContext);
                } else {
                    if (jsonPayload.has(Constants.LOGGER_NAME)) {
                        String loggerName = jsonPayload.getString(Constants.LOGGER_NAME);
                        boolean isRootLogger = Constants.ROOT_LOGGER.equals(loggerName);
                        boolean hasLoggerClass = jsonPayload.has(LOGGER_CLASS);
                        String performedBy = messageContext.getProperty(Constants.USERNAME_PROPERTY).toString();
                        JSONObject info = new JSONObject();
                        info.put(Constants.LOGGER_NAME, loggerName);
                        info.put(Constants.LOGGING_LEVEL, logLevel);
                        if (isRootLogger || !hasLoggerClass) {
                            // update existing logger
                            jsonBody = updateLoggerData(performedBy, info, axis2MessageContext, loggerName, logLevel);
                        } else {
                            try {
                                if (isLoggerExist(loggerName)) {
                                    String errorMsg = "Specified logger name ('" + loggerName
                                            + "') already exists, try updating the level instead";
                                    jsonBody = createJsonError(errorMsg, "", axis2MessageContext);
                                } else {
                                    String loggerClass = jsonPayload.getString(LOGGER_CLASS);
                                    jsonBody = updateLoggerData(performedBy, info,
                                                                axis2MessageContext, loggerName, loggerClass, logLevel);
                                }
                            } catch (IOException exception) {
                                jsonBody = createJsonError(EXCEPTION_MSG, exception, axis2MessageContext);
                            }
                        }
                    } else {
                        // 400-Bad Request logger name is missing
                        jsonBody = createJsonError("Logger name is missing", "", axis2MessageContext);
                    }
                }
            } else {
                // 400-Bad Request logLevel is missing
                jsonBody = createJsonError("Log level is missing", "", axis2MessageContext);
            }
        }
        Utils.setJsonPayLoad(axis2MessageContext, jsonBody);
        return true;
    }

    private JSONObject updateLoggerData(String performedBy, JSONObject info,
                                        org.apache.axis2.context.MessageContext axis2MessageContext, String loggerName,
                                        String loggerClass, String logLevel) {

        try {
            loadConfigs();
            String modifiedLogger = getLoggers().concat(", ").concat(loggerName);
            config.setProperty(LOGGERS_PROPERTY, modifiedLogger);
            config.setProperty(LOGGER_PREFIX + loggerName + LOGGER_NAME_SUFFIX, loggerClass);
            config.setProperty(LOGGER_PREFIX + loggerName + LOGGER_LEVEL_SUFFIX, logLevel);
            applyConfigs();
            jsonBody.put(Constants.MESSAGE, getSuccessMsg(loggerClass, loggerName, logLevel));
            AuditLogger.logAuditMessage(performedBy, Constants.AUDIT_LOG_TYPE_LOG_LEVEL,
                                        Constants.AUDIT_LOG_ACTION_CREATED, info);

        } catch (ConfigurationException | IOException exception) {
            jsonBody = createJsonError("Exception while updating logger data ", exception, axis2MessageContext);
        }
        return jsonBody;
    }

    private JSONObject updateLoggerData(String performedBy, JSONObject info,
                                        org.apache.axis2.context.MessageContext axis2MessageContext, String loggerName,
                                        String logLevel) {

        try {
            loadConfigs();
            if (loggerName.equals(Constants.ROOT_LOGGER)) {
                config.setProperty(loggerName + LOGGER_LEVEL_SUFFIX, logLevel);
                applyConfigs();
                jsonBody.put(Constants.MESSAGE, getSuccessMsg("", loggerName, logLevel));
                AuditLogger.logAuditMessage(performedBy, Constants.AUDIT_LOG_TYPE_ROOT_LOG_LEVEL,
                                            Constants.AUDIT_LOG_ACTION_UPDATED, info);

            } else {
                if (isLoggerExist(loggerName)) {
                    config.setProperty(LOGGER_PREFIX + loggerName + LOGGER_LEVEL_SUFFIX, logLevel);
                    AuditLogger.logAuditMessage(performedBy, Constants.AUDIT_LOG_TYPE_LOG_LEVEL,
                                                Constants.AUDIT_LOG_ACTION_UPDATED, info);
                    applyConfigs();
                    jsonBody.put(Constants.MESSAGE, getSuccessMsg("", loggerName, logLevel));
                } else {
                    jsonBody = createJsonError("Specified logger ('" + loggerName + "') not found", "",
                                               axis2MessageContext);
                }
            }
        } catch (ConfigurationException | IOException exception) {
            jsonBody = createJsonError("Exception while updating logger data ", exception, axis2MessageContext);
        }
        return jsonBody;
    }

    private String getSuccessMsg(String loggerClass, String loggerName, String logLevel) {

        return "Successfully added logger for ('" + loggerName + "') with level " + logLevel + (loggerClass.isEmpty() ?
                "" :
                " for class " + loggerClass);
    }

    private void loadConfigs() throws FileNotFoundException, ConfigurationException {

        jsonBody = new JSONObject();
        config = new PropertiesConfiguration();
        layout = new PropertiesConfigurationLayout(config);
        layout.load(new InputStreamReader(new FileInputStream(logPropFile)));
    }

    private boolean isLoggerExist(String loggerName) throws IOException {

        String logger = getLoggers();
        String[] loggers = logger.split(",");
        return Arrays.stream(loggers).anyMatch(loggerValue -> loggerValue.trim().equals(loggerName));
    }

    private String getLoggers() throws IOException {
        return Utils.getProperty(logPropFile, LOGGERS_PROPERTY);
    }

    private JSONObject getLoggerData(org.apache.axis2.context.MessageContext axis2MessageContext, String loggerName) {

        String logLevel;
        String componentName = "";
        jsonBody = new JSONObject();
        try {
            if (loggerName.equals(Constants.ROOT_LOGGER)) {
                logLevel = Utils.getProperty(logPropFile, loggerName + LOGGER_LEVEL_SUFFIX);
            } else {
                componentName = Utils.getProperty(logPropFile, LOGGER_PREFIX + loggerName + LOGGER_NAME_SUFFIX);
                logLevel = Utils.getProperty(logPropFile, LOGGER_PREFIX + loggerName + LOGGER_LEVEL_SUFFIX);
            }
        } catch (IOException exception) {
            jsonBody = createJsonError("Error while obtaining logger data ", exception, axis2MessageContext);
            return jsonBody;
        }
        jsonBody.put(Constants.LOGGER_NAME, loggerName);
        jsonBody.put(Constants.COMPONENT_NAME, componentName);
        jsonBody.put(Constants.LEVEL, logLevel);
        return jsonBody;
    }

    private JSONArray getAllLoggerDetails(org.apache.axis2.context.MessageContext axisMsgCtx) throws IOException {

        JSONArray payload = new JSONArray();
        // add root logger
        JSONObject data = getLoggerData(axisMsgCtx, Constants.ROOT_LOGGER);
        payload.put(data);

        String[] loggers = getLoggers().split(",");
        for (String logger : loggers) {
            data = getLoggerData(axisMsgCtx, logger.trim());
            payload.put(data);
        }
        return payload;
    }

    private boolean isValidLogLevel(String logLevelToTest) {

        return Arrays.stream(logLevels).anyMatch(logLevel -> logLevel.toString().equalsIgnoreCase(logLevelToTest));
    }

    private JSONObject createJsonError(String message, Object exception,
                                       org.apache.axis2.context.MessageContext axis2MessageContext) {
        log.error(message + exception);
        JSONObject jsonBody = Utils.createJsonErrorObject(message);
        axis2MessageContext.setProperty(Constants.HTTP_STATUS_CODE, Constants.BAD_REQUEST);
        return jsonBody;
    }

    private void applyConfigs() throws IOException, ConfigurationException {
        layout.save(new FileWriter(filePath, false));
        Utils.updateLoggingConfiguration();
    }
}
