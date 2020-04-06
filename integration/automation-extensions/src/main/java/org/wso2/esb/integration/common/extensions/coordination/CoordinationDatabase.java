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

import com.moandjiezana.toml.Toml;
import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.automation.engine.exceptions.AutomationFrameworkException;
import org.wso2.carbon.automation.engine.extensions.ExecutionListenerExtension;

import java.io.File;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CoordinationDatabase extends ExecutionListenerExtension {

    private static final Log logger = LogFactory.getLog(CoordinationDatabase.class);

    private String dbName;
    private String scriptPath;
    private static String connectionUrl;
    private static String pwd;
    private static String userName;

    @Override
    public void initiate() throws AutomationFrameworkException {
        logger.info("Initializing coordination database.");
        populateParameters();
    }

    @Override
    public void onExecutionStart() throws AutomationFrameworkException {

        String dbUrl = connectionUrl.replace("/" + dbName, "").concat("&allowMultiQueries=true");
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
    }

    @Override
    public void onExecutionFinish() {
        // do nothing.
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
            case "toml-path":
                parseToml(value);
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

    private void parseToml(String filePath) throws AutomationFrameworkException {

        File toml = new File(filePath);
        if (!toml.exists()) {
            throw new AutomationFrameworkException("File not found in : " + filePath);
        }
        try {
            Toml parseToml = new Toml().read(toml);
            String datasourceId = parseToml.getString("datasource[0].id");
            if (!"WSO2_COORDINATION_DB".equals(datasourceId)) {
                throw new AutomationFrameworkException(
                        "Coordination db is not defined in toml or not added as first datasource.");
            }
            connectionUrl = parseToml.getString("datasource[0].url").replaceAll("amp;", "");
            userName = parseToml.getString("datasource[0].username");
            pwd = parseToml.getString("datasource[0].password");
            URI uri = URI.create(connectionUrl.substring(5));
            dbName = uri.getPath().replace("/", "");
        } catch (Exception ex) {
            throw new AutomationFrameworkException(ex);
        }
    }

    public static String getConnectionUrl() {
        return connectionUrl;
    }

    public static String getUserName() {
        return userName;
    }

    public static String getPwd() {
        return pwd;
    }

}
