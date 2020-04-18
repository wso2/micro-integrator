/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.axis2.AxisFault;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.MessageContext;
import org.apache.synapse.commons.json.JsonUtil;
import org.apache.synapse.rest.RESTConstants;
import org.json.JSONArray;
import org.json.JSONObject;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.wso2.micro.integrator.initializer.utils.ConfigurationHolder;
import org.wso2.micro.service.mgt.ServiceAdmin;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Utils {

    private static final Log LOG = LogFactory.getLog(Utils.class);

    public static String getQueryParameter(MessageContext messageContext, String key){
        if (Objects.nonNull(messageContext.getProperty(RESTConstants.REST_QUERY_PARAM_PREFIX + key))){
            return messageContext.getProperty(RESTConstants.REST_QUERY_PARAM_PREFIX + key).toString();
        }
        return null;
    }

    public static void setJsonPayLoad(org.apache.axis2.context.MessageContext axis2MessageContext, JSONObject payload) {

        try {
            JsonUtil.getNewJsonPayload(axis2MessageContext, payload.toString(), true, true);
        } catch (AxisFault axisFault) {
            axis2MessageContext.setProperty(Constants.HTTP_STATUS_CODE, Constants.INTERNAL_SERVER_ERROR);
            LOG.error("Error occurred while setting json payload", axisFault);
        }
        axis2MessageContext.setProperty("messageType", Constants.HEADER_VALUE_APPLICATION_JSON);
        axis2MessageContext.setProperty("ContentType", Constants.HEADER_VALUE_APPLICATION_JSON);
    }

    public static JSONObject createJSONList(int count) {
        JSONObject jsonBody = new JSONObject();
        JSONArray list = new JSONArray();
        jsonBody.put(Constants.COUNT, count);
        jsonBody.put(Constants.LIST, list);
        return jsonBody;
    }

    public static JSONObject createJsonErrorObject(String error) {
        JSONObject errorObject =  new JSONObject();
        errorObject.put("Error", error);
        return errorObject;
    }

    public static boolean isDoingPOST(org.apache.axis2.context.MessageContext axis2MessageContext) {
        if (Constants.HTTP_POST.equals(axis2MessageContext.getProperty(Constants.HTTP_METHOD_PROPERTY))) {
            return true;
        }
        return false;
    }

    /**
     * Returns the JSON payload of a given message
     *
     * @param axis2MessageContext axis2MessageContext
     * @return JsonObject payload
     */
    public static JsonObject getJsonPayload(org.apache.axis2.context.MessageContext axis2MessageContext)
            throws IOException {

        InputStream jsonStream = JsonUtil.getJsonPayload(axis2MessageContext);
        String jsonString = IOUtils.toString(jsonStream);
        return new JsonParser().parse(jsonString).getAsJsonObject();
    }

    /**
     * Returns the ServiceAdmin based on a given message context
     *
     * @param messageContext Synapse message context
     */
    public static ServiceAdmin getServiceAdmin(MessageContext messageContext) {

        ServiceAdmin serviceAdmin = ServiceAdmin.getInstance();
        if (!serviceAdmin.isInitailized()) {
            serviceAdmin.init(messageContext.getConfiguration().getAxisConfiguration());
        }
        return serviceAdmin;
    }

    /**
     * Method to update pax-logging configuration.
     */
    public static void updateLoggingConfiguration() throws IOException {

        ConfigurationAdmin configurationAdmin = ConfigurationHolder.getInstance().getConfigAdminService();
        Configuration configuration =
                configurationAdmin.getConfiguration(Constants.PAX_LOGGING_CONFIGURATION_PID, "?");
        configuration.update();
    }

    /**
     * Util method to return the specified  property from a properties file.
     *
     * @param srcFile - The source file which needs to be looked up.
     * @param key     - Key of the property.
     * @return - Value of the property.
     */
    public static String getProperty(File srcFile, String key) throws IOException {

        String value = null;
        try (FileInputStream fis = new FileInputStream(srcFile)) {
            Properties properties = new Properties();
            properties.load(fis);
            value = properties.getProperty(key);
        } catch (IOException e) {
            throw new IOException("Error occurred while reading the input stream");
        }
        return value;
    }

    /**
     * This method will return the path to logs directory.
     * @return  path as string.
     */
    public static String getCarbonLogsPath() {

        String carbonLogsPath = System.getProperty("carbon.logs.path");
        if (carbonLogsPath == null) {
            carbonLogsPath = System.getenv("CARBON_LOGS");
            if (carbonLogsPath == null) {
                return getCarbonHome() + File.separator + "repository" + File.separator + "logs";
            }
        }
        return carbonLogsPath;
    }

    /**
     * This method will return the path to CARBON_HOME.
     * @return  path as String.
     */
    public static String getCarbonHome() {

        String carbonHome = System.getProperty("carbon.home");
        if (carbonHome == null) {
            carbonHome = System.getenv("CARBON_HOME");
            System.setProperty("carbon.home", carbonHome);
        }
        return carbonHome;
    }

    /**
     * This method will provide information on all the files in the logs directory.
     * @return list of file info objects.
     */
    public static List<LogFileInfo> getLogFileInfoList() {

        String folderPath = Utils.getCarbonLogsPath();
        List<LogFileInfo> logFilesList = new ArrayList<>();
        LogFileInfo logFileInfo;

        File folder = new File(folderPath);
        File[] listOfFiles = folder.listFiles();

        if (listOfFiles == null || listOfFiles.length == 0) {
            // folder.listFiles can return a null, in that case return a default log info
            if (LOG.isDebugEnabled()) {
                LOG.debug("List of log files of the given pattern is null.");
            }
            return getDefaultLogInfoList();
        }
        for (File file : listOfFiles) {
            String filename = file.getName();
            if (!filename.endsWith(".lck")) {
                // regex to match YYYY-MM-DD and DD-MM-YYYY
                String[] dateRegex = {"\\d{4}-\\d{2}-\\d{2}", "\\d{2}-\\d{2}-\\d{4}"};
                String date = matchDatePatters(dateRegex, filename);
                String filePath = Utils.getCarbonLogsPath() + File.separator + filename;
                File logfile = new File(filePath);
                logFileInfo = new LogFileInfo(filename, date, getFileSize(logfile));
                logFilesList.add(logFileInfo);
            }
        }
        return logFilesList;
    }

    private static String matchDatePatters(String[] patterns, String filename) {

        for (String datePattern : patterns) {
            Pattern pattern = Pattern.compile(datePattern);
            Matcher matcher = pattern.matcher(filename);
            if (matcher.find()) {
                return matcher.group(0);
            }
        }
        // empty string if filename does not contains a date
        return "";
    }

    private static String getFileSize(File file) {

        long bytes = file.length();
        int unit = 1024;
        if (bytes < unit) {
            return bytes + " B";
        }
        int exp = (int) (Math.log(bytes) / Math.log(unit));
        char pre = "KMGTPE".charAt(exp - 1);
        return String.format("%.1f %sB", bytes / Math.pow(unit, exp), pre);
    }

    private static List<LogFileInfo> getDefaultLogInfoList() {

        List<LogFileInfo> defaultLogFileInfoList = new ArrayList<>();
        defaultLogFileInfoList.add(new LogFileInfo("NO_LOG_FILES", "---", "---"));
        return defaultLogFileInfoList;
    }

}
