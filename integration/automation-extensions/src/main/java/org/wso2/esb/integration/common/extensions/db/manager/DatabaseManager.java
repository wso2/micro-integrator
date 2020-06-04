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

package org.wso2.esb.integration.common.extensions.db.manager;

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
import java.util.stream.Stream;

/**
 * Manages the MySQL , MSSQL , Postgres , DB2 and Oracle dbs for integration tests.
 */
public class DatabaseManager extends ExecutionListenerExtension {

    private static final Log logger = LogFactory.getLog(DatabaseManager.class);

    private String dbName;
    private String scriptPath;
    private static String connectionUrl;
    private static String pwd;
    private static String userName;
    private String dbType;
    private String dataSource;
    private String scriptBaseDir;
    private String scriptSuffix;
    private String delimiter;

    private static final String ALLOW_MULTIPLE_QUERIES = "allowMultiQueries=true";
    private static final String MY_SQL = "mysql";
    private static final String MS_SQL = "sqlserver";
    private static final String POSTGRE_SQL = "postgresql";
    private static final String DB2 = "db2";
    private static final String ORACLE = "oracle";

    @Override
    public void initiate() throws AutomationFrameworkException {
        logger.info("Initializing coordination database.");
        populateParameters();
    }

    @Override
    public void onExecutionStart() throws AutomationFrameworkException {

        logger.info("Database type : " + dbType);
        logger.info("Database name : " + dbName);

        if (MY_SQL.equals(dbType)) {
            setUpMysql();
        } else if (MS_SQL.equals(dbType)) {
            setUpMssql();
        } else if (POSTGRE_SQL.equals(dbType)) {
            setUpPostgres();
        } else if (DB2.equals(dbType) || ORACLE.equals(dbType)) {
            executeScript(scriptBaseDir + "/" + dbType + "/" + dbType + "_" + scriptSuffix);
        } else {
            logger.error("Not supported db type : " + dbType);
        }

        logger.info("Coordination database configured successfully.");
    }

    /**
     * Removes all tables from the database and creates them again.
     *
     * @throws AutomationFrameworkException Exception
     */
    private void setUpPostgres() throws AutomationFrameworkException {

        String dbUrl = connectionUrl.concat("?" + ALLOW_MULTIPLE_QUERIES);
        scriptPath = scriptBaseDir + "/postgres/postgresql_" + scriptSuffix;
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
        } catch (Exception ex) {
            throw new AutomationFrameworkException(ex);
        }
    }

    /**
     * Create the database with given name first and then source the db script in it. The db will be removed upon
     * execution end.
     *
     * @throws AutomationFrameworkException Exception
     */
    private void setUpMssql() throws AutomationFrameworkException {

        // create db
        String dbUrl = connectionUrl.concat(";" + ALLOW_MULTIPLE_QUERIES).replace(";databaseName=" + dbName, "");
        try (Connection conn = DriverManager.getConnection(dbUrl, userName, pwd);
                PreparedStatement preparedStatement = conn.prepareStatement("CREATE DATABASE " + dbName + ";")) {
            preparedStatement.executeUpdate();
        } catch (Exception ex) {
            throw new AutomationFrameworkException(ex);
        }
        // create tables
        dbUrl = connectionUrl.concat(";" + ALLOW_MULTIPLE_QUERIES);
        scriptPath = scriptBaseDir + "/mssql/mssql_" + scriptSuffix;
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

    /**
     * Drop the data base if it exits and creates a new one and source db the script.
     *
     * @throws AutomationFrameworkException Exception
     */
    private void setUpMysql() throws AutomationFrameworkException {

        scriptPath = scriptBaseDir + "/mysql/mysql_" + scriptSuffix;
        String dbUrl = connectionUrl.replace("/" + dbName, "").concat("&" + ALLOW_MULTIPLE_QUERIES);
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
        } catch (Exception ex) {
            throw new AutomationFrameworkException(ex);
        }
    }

    @Override
    public void onExecutionFinish() throws AutomationFrameworkException {

        // remove ms sql db upon suite execution end.
        if (MS_SQL.equals(dbType)) {
            // drop db
            String dbUrl = connectionUrl.concat(";" + ALLOW_MULTIPLE_QUERIES).replace(";databaseName=" + dbName, "");
            try (Connection conn = DriverManager.getConnection(dbUrl, userName, pwd);
                    PreparedStatement preparedStatement = conn.prepareStatement("DROP DATABASE " + dbName + ";")) {
                preparedStatement.executeUpdate();
            } catch (Exception ex) {
                throw new AutomationFrameworkException(ex);
            }
        } else if (DB2.equals(dbType) || ORACLE.equals(dbType)) {
            // remove all tables from db2 and oracle upon end. The scripts need to be in /unset dir.
            executeScript(scriptBaseDir + "/unset/" + dbType + "/" + dbType + "_" + scriptSuffix);
        }
    }

    /**
     * Delimits the script with "script-delimiter" and executes all query in db.
     *
     * @param scriptFilePath path of script
     * @throws AutomationFrameworkException Exception
     */
    private void executeScript(String scriptFilePath) throws AutomationFrameworkException {

        File file = new File(getSystemDependentPath(scriptFilePath));
        try {
            String[] queries = FileUtils.readFileToString(file, StandardCharsets.UTF_8).split(delimiter);
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
                scriptBaseDir = value;
                break;
            case "script-suffix":
                scriptSuffix = value;
                break;
            case "data-source":
                dataSource = value;
                break;
            case "script-delimiter":
                delimiter = value;
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
                if (Stream.of(MY_SQL, DB2, POSTGRE_SQL).anyMatch(s -> s.equals(dbType))) {
                    dbName = path.replace("/", "");
                } else if (MS_SQL.equals(dbType)) {
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
