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
import java.util.Objects;

public class CoordinationDatabase extends ExecutionListenerExtension {

    private static final Log logger = LogFactory.getLog(CoordinationDatabase.class);

    private String dbName;
    private String scriptPath;
    private static String connectionUrl;
    private static String pwd;
    private static String userName;
    private String dbType;
    private String dataSource;
    private String scriptbaseDir;
    private String scriptSuffix;

    @Override
    public void initiate() throws AutomationFrameworkException {
        logger.info("Initializing coordination database.");
        populateParameters();
    }

    @Override
    public void onExecutionStart() throws AutomationFrameworkException {

        if ("mysql".equals(dbType)) {
            setUpMysql();
        }
        if ("sqlserver".equals(dbType)) {
            setUpMssql();
        }
        if ("postgresql".equals(dbType)) {
            setUpPostgres();
        }
        if ("oracle".equals(dbType)) {
            setUpOracle();
        }
    }

    private void setUpOracle() throws AutomationFrameworkException {

        executeOracleUpdate(scriptbaseDir + "/oracle/oracle_" + scriptSuffix);
    }

    private void setUpPostgres() throws AutomationFrameworkException {

        String dbUrl = connectionUrl.concat("?allowMultiQueries=true");
        scriptPath = scriptbaseDir + "/postgres/postgresql_" + scriptSuffix;
        scriptPath = getSystemDependentPath(scriptPath);
        File file = new File(scriptPath);
        try {
            List<String> schema = new ArrayList<>();
            schema.add("DROP SCHEMA public CASCADE;");
            schema.add("CREATE SCHEMA public;");
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

    private void setUpMssql() throws AutomationFrameworkException {

        scriptPath = scriptbaseDir + "/mssql/mssql_" + scriptSuffix;
        // create db
        String dbUrl = connectionUrl.concat(";allowMultiQueries=true").replace(";databaseName=" + dbName, "");
        try (Connection conn = DriverManager.getConnection(dbUrl, userName, pwd);
                PreparedStatement preparedStatement = conn.prepareStatement("CREATE DATABASE " + dbName + ";")) {
            preparedStatement.executeUpdate();
        } catch (Exception ex) {
            throw new AutomationFrameworkException(ex);
        }

        // create tables
        dbUrl = connectionUrl.concat(";allowMultiQueries=true");
        scriptPath = getSystemDependentPath(scriptPath);
        File file = new File(scriptPath);
        try (Connection conn = DriverManager.getConnection(dbUrl, userName, pwd);
                PreparedStatement preparedStatement = conn.prepareStatement(
                        FileUtils.readFileToString(file, StandardCharsets.UTF_8))) {
            preparedStatement.executeUpdate();
        } catch (Exception ex) {
            throw new AutomationFrameworkException(ex);
        }

    }

    private void setUpMysql() throws AutomationFrameworkException {

        scriptPath = scriptbaseDir + "/mysql/mysql_" + scriptSuffix;
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
    public void onExecutionFinish() throws AutomationFrameworkException {
        if ("sqlserver".equals(dbType)) {
            // drop db
            String dbUrl = connectionUrl.concat(";allowMultiQueries=true").replace(";databaseName=" + dbName, "");
            try (Connection conn = DriverManager.getConnection(dbUrl, userName, pwd);
                    PreparedStatement preparedStatement = conn.prepareStatement("DROP DATABASE " + dbName + ";")) {
                preparedStatement.executeUpdate();
            } catch (Exception ex) {
                throw new AutomationFrameworkException(ex);
            }
        }
        if ("oracle".equals(dbType)) {
            executeOracleUpdate(scriptbaseDir + "/unset/oracle/oracle_" + scriptSuffix);
        }
    }

    private void executeOracleUpdate(String scriptFilePath) throws AutomationFrameworkException {
        File file = new File(getSystemDependentPath(scriptFilePath));
        try {
            String[] queries = FileUtils.readFileToString(file, StandardCharsets.UTF_8).split(";");
            for (String query : queries) {
                query = query.trim();
                if (!query.isEmpty()) {
                    logger.info("Executing query : " + query);
                    try (Connection conn = DriverManager.getConnection(connectionUrl, userName, pwd);
                            PreparedStatement preparedStatement = conn.prepareStatement(query)) {
                        preparedStatement.executeUpdate();
                    }
                }
            }
        } catch (Exception ex) {
            throw new AutomationFrameworkException(ex);
        }
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
            case "script-basedir":
                scriptbaseDir = value;
                break;
            case "script-suffix":
                scriptSuffix = value;
                break;
            case "data-source":
                dataSource = value;
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
            if (Objects.nonNull(dataSource) && !dataSource.equals(datasourceId)) {
                throw new AutomationFrameworkException(
                        "Data source " + dataSource + " is not defined in toml or not added as first datasource.");
            }
            connectionUrl = parseToml.getString("datasource[0].url").replaceAll("amp;", "");
            userName = parseToml.getString("datasource[0].username");
            pwd = parseToml.getString("datasource[0].password");
            URI uri = URI.create(connectionUrl.substring(5));
            dbType = uri.getScheme();
            String path = uri.getPath();
            if (path != null) {
                if ("mysql".equals(dbType) || "db2".equals(dbType) || "postgresql".equals(dbType)) {
                    dbName = path.replace("/", "");
                } else if ("sqlserver".equals(dbType)) {
                    String[] splits = connectionUrl.split("databaseName=");
                    dbName = splits[1].substring(0, splits[1].indexOf(';'));
                }
            }
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
