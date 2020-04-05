/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.esb.integration.common.extensions.coordination;

import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.automation.engine.exceptions.AutomationFrameworkException;
import org.wso2.carbon.automation.engine.extensions.ExecutionListenerExtension;
import org.wso2.carbon.automation.engine.frameworkutils.FrameworkPathUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public class CoordinationDatabase extends ExecutionListenerExtension {

    private static final Log logger = LogFactory.getLog(CoordinationDatabase.class);
    private int port;
    private String userName;
    private String pwd;
    private String dbName;
    private String dbType;
    private String driver;
    private String scriptPath;
    private String datasourceConfig;
    private String host;
    private String tomlPath =
            FrameworkPathUtil.getSystemResourceLocation() + "artifacts" + File.separator + "ESB" + File.separator
                    + "server" + File.separator + "conf" + File.separator + "deployment.toml";

    @Override
    public void initiate() throws AutomationFrameworkException {
        logger.info("Initializing coordination database.");
        populateParameters();
    }

    @Override
    public void onExecutionStart() throws AutomationFrameworkException {

        String dbUrl = "jdbc:" + dbType + "://" + host + ":" + port
                + "?allowMultiQueries=true&useSSL=false&allowPublicKeyRetrieval=true";
        scriptPath = getSystemDependentPath(scriptPath);
        File file = new File(scriptPath);
        try {
            List<String> schema = new ArrayList<>();
            schema.add("drop database if exists " + dbName + ";");
            schema.add("create database " + dbName + " character set latin1;");
            schema.add("use " + dbName + ";");
            schema.add(FileUtils.readFileToString(file, StandardCharsets.UTF_8));
            try (Connection conn = DriverManager.getConnection(dbUrl, userName, pwd);
                    PreparedStatement preparedStatement = conn.prepareStatement(String.join("", schema))) {
                preparedStatement.executeUpdate();
            }
            logger.info("Coordination database configured successfully.");
        } catch (Exception ex) {
            throw new AutomationFrameworkException(ex);
        }
        updateToml();
    }

    @Override
    public void onExecutionFinish() throws AutomationFrameworkException {
        cleanToml();
    }

    private static String getSystemDependentPath(String path) {
        return path.replace('/', File.separatorChar);
    }

    private void populateParameters() throws AutomationFrameworkException {

        Map<String, String> parameters = getParameters();
        for (Map.Entry<String, String> entry : parameters.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            switch (key) {
            case "database-properties":
                extractProperties(value);
                break;
            case "script-path":
                scriptPath = value;
                break;
            default:
                logger.error("Unknown property : " + key);
                break;
            }
        }
    }

    private void extractProperties(String filePath) throws AutomationFrameworkException {

        File propertiesFile = new File(filePath);
        if (!propertiesFile.exists()) {
            throw new AutomationFrameworkException("File not found in : " + filePath);
        }
        try {
            Properties properties = new Properties();
            properties.load(new FileInputStream(propertiesFile));
            properties.keySet().forEach(key -> {
                String keyString = key.toString();
                switch (keyString) {
                case "port":
                    port = Integer.parseInt(properties.getProperty(keyString));
                    break;
                case "user-name":
                    userName = properties.getProperty(keyString);
                    break;
                case "password":
                    pwd = properties.getProperty(keyString);
                    break;
                case "database-name":
                    dbName = properties.getProperty(keyString);
                    break;
                case "db-type":
                    dbType = properties.getProperty(keyString);
                    break;
                case "driver":
                    driver = properties.getProperty(keyString);
                    break;
                case "host-name":
                    host = properties.getProperty(keyString);
                    break;
                default:
                    break;
                }
            });
        } catch (Exception ex) {
            throw new AutomationFrameworkException(ex);
        }
    }

    private void updateToml() throws AutomationFrameworkException {

        if (!new File(tomlPath).exists()) {
            throw new AutomationFrameworkException("Initial toml is not found in " + tomlPath);
        }
        try {
            String tomlContent = FileUtils.readFileToString(new File(tomlPath), StandardCharsets.UTF_8);
            if (tomlContent.contains("WSO2_COORDINATION_DB")) {
                logger.warn("Coordination db is already defined, hence skipping updating it.");
                return;
            }
            datasourceConfig =
                    "\n[[datasource]]\n" + "id = \"WSO2_COORDINATION_DB\"\n" + "url = \"jdbc:" + dbType + "://" + host
                            + ":" + port + "/" + dbName + "?useSSL=false&amp;allowPublicKeyRetrieval=true\"\n"
                            + "username = \"" + userName + "\"\n" + "password" + " = \"" + pwd + "\"\n" + "driver = \""
                            + driver + "\"";
            try (FileWriter fileWriter = new FileWriter(tomlPath, true); PrintWriter printWriter = new PrintWriter(
                    fileWriter)) {
                printWriter.append(datasourceConfig);
            }
        } catch (Exception ex) {
            throw new AutomationFrameworkException(ex);
        }
    }

    private void cleanToml() throws AutomationFrameworkException {
        try {
            String tomlContent = FileUtils.readFileToString(new File(tomlPath), StandardCharsets.UTF_8);
            tomlContent = tomlContent.replace(datasourceConfig, "");
            try (FileWriter fileWriter = new FileWriter(tomlPath, false); PrintWriter printWriter = new PrintWriter(
                    fileWriter)) {
                printWriter.append(tomlContent);
            }
        } catch (Exception ex) {
            throw new AutomationFrameworkException(ex);
        }
    }

}
