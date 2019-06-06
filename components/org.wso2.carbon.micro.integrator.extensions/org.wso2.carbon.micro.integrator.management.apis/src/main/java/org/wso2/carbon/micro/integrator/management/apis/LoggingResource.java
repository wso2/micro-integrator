package org.wso2.carbon.micro.integrator.management.apis;

import org.apache.log4j.Appender;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.synapse.MessageContext;
import org.apache.synapse.commons.json.JsonUtil;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.json.JSONObject;

import java.util.Enumeration;
import java.util.HashSet;
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

        JSONObject jsonPayload = new JSONObject(JsonUtil.jsonPayloadToString(axis2MessageContext));

        String logLevel = jsonPayload.getString("loggingLevel");
        boolean persist = jsonPayload.getBoolean("persist");

        updateSystemLog(messageContext, logLevel, persist);

        axis2MessageContext.removeProperty(Constants.NO_ENTITY_BODY);
        return true;
    }

    private void updateSystemLog(MessageContext messageContext, String logLevel, boolean persist) {

        org.apache.axis2.context.MessageContext axis2MessageContext =
                ((Axis2MessageContext) messageContext).getAxis2MessageContext();

        JSONObject jsonBody = new JSONObject();

        if (!isALogLevel(logLevel)) {
            jsonBody.put("message", "Invalid log level");
            axis2MessageContext.setProperty(Constants.HTTP_STATUS_CODE, Constants.BAD_REQUEST);
        }

        Set<Appender> appenderSet = new HashSet<>();

        if (persist) {
            //
        }

        // update root logger details
        Logger rootLogger = Logger.getRootLogger();
        rootLogger.setLevel(Level.toLevel(logLevel));
        addAppendersToSet(rootLogger.getAllAppenders(), appenderSet);

        // update logger and appender data, following are set
        // 1. log level of all the loggers to logLevel
        // 2. pattern of all the appenders to logpattern
        Enumeration loggersEnum = LogManager.getCurrentLoggers();
        Level systemLevel = Level.toLevel(logLevel);
        while (loggersEnum.hasMoreElements()) {
            Logger logger = (Logger) loggersEnum.nextElement();
            // we ignore all class level defined loggers
            addAppendersToSet(logger.getAllAppenders(), appenderSet);
            logger.setLevel(systemLevel);
        }
        jsonBody.put("message", "Successfully updated log level to " + rootLogger.getLevel().toString());
        Utils.setJsonPayLoad(axis2MessageContext, jsonBody);
    }

    private void addAppendersToSet(Enumeration appenders, Set<Appender> appenderSet) {
        Appender appender;
        while (appenders.hasMoreElements()) {
            appender = (Appender) appenders.nextElement();
            if (appender.getName() != null) {
                appenderSet.add(appender);
            }
        }
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
