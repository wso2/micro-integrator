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

    private static final Level[] logLevels = new Level[]{Level.OFF, Level.TRACE, Level.DEBUG, Level.INFO, Level.WARN, Level.ERROR, Level.FATAL};

    public LoggingResource(String urlTemplate) {
        super(urlTemplate);
    }

    @Override
    public Set<String> getMethods() {

        Set<String> methods = new HashSet<>();
        methods.add(Constants.HTTP_METHOD_GET);
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
        JSONObject jsonBody;

        String httpMethod = axis2MessageContext.getProperty("HTTP_METHOD").toString();

        if (httpMethod.equals(Constants.HTTP_METHOD_GET)) {
            String param = Utils.getQueryParameter(messageContext, Constants.LOGGER_NAME);
            if (Objects.nonNull(param)) {
                jsonBody = getLoggerData(axis2MessageContext, param);
            } else {
                // 400-Bad Request loggerName is missing
                jsonBody = Utils.createJsonErrorObject("Logger Name is missing");
                axis2MessageContext.setProperty(Constants.HTTP_STATUS_CODE, Constants.BAD_REQUEST);
            }
        } else {
            if (jsonPayload.has(Constants.LOGGING_LEVEL)) {
                logLevel = jsonPayload.getString(Constants.LOGGING_LEVEL);
                if (!isALogLevel(logLevel)) {
                    // 400-Bad Request Invalid loggingLevel
                    jsonBody = Utils.createJsonErrorObject("Invalid log level " + logLevel);
                    axis2MessageContext.setProperty(Constants.HTTP_STATUS_CODE, Constants.BAD_REQUEST);
                } else {
                    if (jsonPayload.has(Constants.LOGGER_NAME)) {
                        loggerName = jsonPayload.getString(Constants.LOGGER_NAME);
                        if(loggerName.equals(Constants.ROOT_LOGGER)){
                            // update root logger
                            jsonBody = updateSystemLog(logLevel);
                        } else {
                            // update the specific logger
                            jsonBody = updateLoggerData(axis2MessageContext, loggerName, logLevel);
                        }
                    } else {
                        // 400-Bad Request logger name is missing
                        jsonBody = Utils.createJsonErrorObject("Logger name is missing");
                        axis2MessageContext.setProperty(Constants.HTTP_STATUS_CODE, Constants.BAD_REQUEST);
                    }
                }
            } else {
                // 400-Bad Request logLevel is missing
                jsonBody = Utils.createJsonErrorObject("Log level is missing");
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
            jsonBody.put(Constants.MESSAGE, "Successfully updated log level of " + loggerName + " to " + logger.getLevel().toString());
        } else {
            jsonBody = Utils.createJsonErrorObject("Invalid logger " + loggerName);
            axis2MessageContext.setProperty(Constants.HTTP_STATUS_CODE, Constants.BAD_REQUEST);
        }
        return jsonBody;
    }

    private JSONObject getLoggerData(org.apache.axis2.context.MessageContext axis2MessageContext, String loggerName) {

        JSONObject jsonBody = new JSONObject();
        String logLevel;
        String parentName;

        if (loggerName.equals(Constants.ROOT_LOGGER)) {
            parentName = "empty";
            logLevel = LogManager.getRootLogger().getEffectiveLevel().toString();
        } else {
            Logger logger = LogManager.exists(loggerName);

            if (Objects.nonNull(logger)) {
                parentName = Objects.isNull(logger.getParent()) ? "empty" : logger.getParent().getName();
                logLevel = logger.getEffectiveLevel().toString();
            } else {
                jsonBody = Utils.createJsonErrorObject("Invalid logger " + loggerName);
                axis2MessageContext.setProperty(Constants.HTTP_STATUS_CODE, Constants.BAD_REQUEST);
                return jsonBody;
            }
        }
        jsonBody.put(Constants.NAME, loggerName);
        jsonBody.put(Constants.LEVEL, logLevel);
        jsonBody.put(Constants.PARENT, parentName);
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
}
