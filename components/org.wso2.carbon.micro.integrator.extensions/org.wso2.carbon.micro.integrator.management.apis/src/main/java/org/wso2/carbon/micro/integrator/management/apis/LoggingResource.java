package org.wso2.carbon.micro.integrator.management.apis;

import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.synapse.MessageContext;
import org.apache.synapse.commons.json.JsonUtil;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.json.JSONObject;

import java.util.Enumeration;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class LoggingResource extends ApiResource {

    private static String[] logLevels = new String[]{"OFF", "TRACE", "DEBUG", "INFO", "WARN", "ERROR", "FATAL"};

    public LoggingResource(String urlTemplate) {
        super(urlTemplate);
    }

    @Override
    public Set<String> getMethods() {

        Set<String> methods = new HashSet<>();
        methods.add("GET");
        methods.add("PATCH");
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
        JSONObject jsonBody = new JSONObject();

        String httpMethod = axis2MessageContext.getProperty("HTTP_METHOD").toString();

        if (httpMethod.equals("GET")) {
            String param = Utils.getQueryParameter(messageContext, Constants.LOGGER_NAME);
            if (Objects.nonNull(param)) {
                jsonBody = getLoggerData(axis2MessageContext, param);
            } else {
                // 400-Bad Request loggerName is missing
                jsonBody.put(Constants.MESSAGE, "Logger Name is missing");
                axis2MessageContext.setProperty(Constants.HTTP_STATUS_CODE, Constants.BAD_REQUEST);
            }
        } else {
            if (jsonPayload.has(Constants.LOGGING_LEVEL)) {
                logLevel = jsonPayload.getString(Constants.LOGGING_LEVEL);
                if (!isALogLevel(logLevel)) {
                    // 400-Bad Request Invalid loggingLevel
                    jsonBody.put(Constants.MESSAGE, "Invalid log level " + logLevel);
                    axis2MessageContext.setProperty(Constants.HTTP_STATUS_CODE, Constants.BAD_REQUEST);
                } else {
                    if (jsonPayload.has(Constants.LOGGER_NAME)) {
                        // update the specific logger
                        loggerName = jsonPayload.getString(Constants.LOGGER_NAME);
                        jsonBody = updateLoggerData(axis2MessageContext, loggerName, logLevel);
                    } else {
                        // update root logger
                        jsonBody = updateSystemLog(logLevel);
                    }
                }
            } else {
                // 400-Bad Request loggingLevel is missing
                jsonBody.put(Constants.MESSAGE, "Logging level is missing");
                axis2MessageContext.setProperty(Constants.HTTP_STATUS_CODE, Constants.BAD_REQUEST);
            }
        }
        Utils.setJsonPayLoad(axis2MessageContext, jsonBody);
        return true;
    }

    private JSONObject updateSystemLog(String logLevel) {

        JSONObject jsonBody = new JSONObject();

        // update root logger details
        Logger rootLogger = Logger.getRootLogger();
        rootLogger.setLevel(Level.toLevel(logLevel));

        // update logger and appender data, following are set
        Enumeration loggersEnum = LogManager.getCurrentLoggers();
        Level systemLevel = Level.toLevel(logLevel);
        while (loggersEnum.hasMoreElements()) {
            Logger logger = (Logger) loggersEnum.nextElement();
            logger.setLevel(systemLevel);
        }
        jsonBody.put(Constants.MESSAGE, "Successfully updated system log level to " + rootLogger.getLevel().toString());
        return jsonBody;
    }

    private JSONObject updateLoggerData(org.apache.axis2.context.MessageContext axis2MessageContext, String loggerName, String loggerLevel) {

        JSONObject jsonBody = new JSONObject();
        //update logger data in current system
        Logger logger = LogManager.exists(loggerName);

        if (Objects.nonNull(logger)) {
            logger.setLevel(Level.toLevel(loggerLevel));
            jsonBody.put(Constants.MESSAGE, "Successfully updated " + loggerName + " level to " + logger.getLevel().toString());
        } else {
            jsonBody.put(Constants.MESSAGE, "Invalid logger " + loggerName);
            axis2MessageContext.setProperty(Constants.HTTP_STATUS_CODE, Constants.BAD_REQUEST);
        }
        return jsonBody;
    }

    private JSONObject getLoggerData(org.apache.axis2.context.MessageContext axis2MessageContext, String loggerName) {

        JSONObject jsonBody = new JSONObject();
        Logger logger = LogManager.exists(loggerName);

        if (Objects.nonNull(logger)) {
            String parentName = Objects.isNull(logger.getParent()) ? "empty" : logger.getParent().getName();
            String logLevel = logger.getEffectiveLevel().toString();
            jsonBody.put(Constants.NAME, loggerName);
            jsonBody.put(Constants.LEVEL, logLevel);
            jsonBody.put(Constants.PARENT, parentName);
        } else {
            jsonBody.put(Constants.MESSAGE, "Invalid logger " + loggerName);
            axis2MessageContext.setProperty(Constants.HTTP_STATUS_CODE, Constants.BAD_REQUEST);
        }
        return jsonBody;
    }

    private boolean isALogLevel(String logLevelToTest) {
        boolean returnValue = false;
        for (String logLevel : logLevels) {
            if (logLevel.equalsIgnoreCase(logLevelToTest))
                returnValue = true;
        }
        return returnValue;
    }
}
