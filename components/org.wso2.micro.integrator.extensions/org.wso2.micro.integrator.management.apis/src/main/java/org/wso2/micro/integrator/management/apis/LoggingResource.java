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
import org.json.JSONObject;
import org.wso2.carbon.utils.ServerConstants;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class LoggingResource extends ApiResource {

    private static final Log log = LogFactory.getLog(LoggingResource.class);

    private static final Level[] logLevels = new Level[]{Level.OFF, Level.TRACE, Level.DEBUG, Level.INFO, Level.WARN, Level.ERROR, Level.FATAL};

    private JSONObject jsonBody;
    private String filePath = System.getProperty(ServerConstants.CARBON_CONFIG_DIR_PATH) + File.separator + "log4j2.properties";

    private PropertiesConfiguration config = new PropertiesConfiguration();
    private PropertiesConfigurationLayout layout = new PropertiesConfigurationLayout(config);

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

        String logLevel;
        String loggerName;

        String httpMethod = axis2MessageContext.getProperty("HTTP_METHOD").toString();

        if (httpMethod.equals(Constants.HTTP_GET)) {
            String param = Utils.getQueryParameter(messageContext, Constants.LOGGER_NAME);
            if (Objects.nonNull(param)) {
                jsonBody = getLoggerData(axis2MessageContext, param);
            } else {
                // 400-Bad Request loggerName is missing
                jsonBody = createJsonError("Logger Name is missing", "", axis2MessageContext);
            }
        } else {
            if (jsonPayload.has(Constants.LOGGING_LEVEL)) {
                logLevel = jsonPayload.getString(Constants.LOGGING_LEVEL);
                if (!isALogLevel(logLevel)) {
                    // 400-Bad Request Invalid loggingLevel
                    jsonBody = createJsonError("Invalid log level " + logLevel, "", axis2MessageContext);
                } else {
                    if (jsonPayload.has(Constants.LOGGER_NAME)) {
                        loggerName = jsonPayload.getString(Constants.LOGGER_NAME);
                        // update root and specific logger
                        jsonBody = updateLoggerData(axis2MessageContext, loggerName, logLevel);
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

    private JSONObject updateLoggerData(org.apache.axis2.context.MessageContext axis2MessageContext, String loggerName,
                                        String logLevel) {
        jsonBody = new JSONObject();
        File log4j2PropertiesFile = new File(filePath);

        try {
            String loggers = Utils.getProperty(log4j2PropertiesFile, "loggers");
            layout.load(new InputStreamReader(new FileInputStream(log4j2PropertiesFile)));
            if (loggerName.equals(Constants.ROOT_LOGGER)) {
                setLoggerLevel(loggerName + ".level", logLevel);
            } else {
                if (loggers.contains(loggerName)) {
                    setLoggerLevel("logger." + loggerName + ".level", logLevel);
                } else {
                    jsonBody = createJsonError("Specified logger " + loggerName + " is not found",
                            "", axis2MessageContext);
                }
            }
        } catch (ConfigurationException | IOException exception) {
            jsonBody = createJsonError("Exception while updating logger data ", exception, axis2MessageContext);
        }
        return jsonBody;
    }

    private JSONObject getLoggerData(org.apache.axis2.context.MessageContext axis2MessageContext, String loggerName) {

        String logLevel = null;
        String componentName = null;
        jsonBody = new JSONObject();
        File log4j2PropertiesFile = new File(filePath);

        try {
            String logger = Utils.getProperty(log4j2PropertiesFile, "loggers");

            if (loggerName.equals(Constants.ROOT_LOGGER)) {
                componentName = "Not available for rootLogger";
                logLevel = Utils.getProperty(log4j2PropertiesFile, loggerName + ".level");
            } else if (logger.contains(loggerName)) {
                componentName = Utils.getProperty(log4j2PropertiesFile, "logger." + loggerName + ".name");
                logLevel = Utils.getProperty(log4j2PropertiesFile, "logger." + loggerName + ".level");
            } else {
                jsonBody = createJsonError("Specified logger " + loggerName + " is not found",
                        "", axis2MessageContext);
                return jsonBody;
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

    private boolean isALogLevel(String logLevelToTest) {
        boolean returnValue = false;
        for (Level logLevel : logLevels) {
            if (logLevel.toString().equalsIgnoreCase(logLevelToTest))
                returnValue = true;
        }
        return returnValue;
    }

    private JSONObject createJsonError(String message, Object exception,
                                       org.apache.axis2.context.MessageContext axis2MessageContext) {
        log.error(message + exception);
        JSONObject jsonBody = Utils.createJsonErrorObject(message);
        axis2MessageContext.setProperty(Constants.HTTP_STATUS_CODE, Constants.BAD_REQUEST);
        return jsonBody;
    }

    private void setLoggerLevel(String loggerLevelKey , String logLevel) throws IOException, ConfigurationException {
        config.setProperty(loggerLevelKey , logLevel);
        layout.save(new FileWriter(filePath, false));
        Utils.updateLoggingConfiguration();
        jsonBody.put(Constants.MESSAGE, "Successfully updated " + loggerLevelKey
                + " to " + logLevel);
    }
}
