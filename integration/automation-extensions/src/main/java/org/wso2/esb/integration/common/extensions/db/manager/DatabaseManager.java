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
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.automation.engine.exceptions.AutomationFrameworkException;
import org.wso2.carbon.automation.engine.extensions.ExecutionListenerExtension;

import java.io.File;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
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
    private String dbClearScripts;
    private boolean createSecondary = false;

    private static final String ALLOW_MULTIPLE_QUERIES = "allowMultiQueries=true";
    private static final String MY_SQL = "mysql";
    private static final String MS_SQL = "sqlserver";
    private static final String POSTGRES = "postgresql";
    private static final String DB2 = "db2";
    private static final String ORACLE = "oracle";

    private static final String DEFAULT_DB_URL =
            "jdbc:mysql://localhost:3306/testDb?useSSL=false&allowPublicKeyRetrieval=true";
    private static final String DEFAULT_DB_USER = "root";
    private static final String DEFAULT_DB_PWD = "root";
    private static final String DEFAULT_DRIVER = "com.mysql.jdbc.Driver";
    private static final String SECONDARY_DB = "secondaryDb";

    @Override
    public void initiate() throws AutomationFrameworkException {

        logger.info("Initializing database.");
        populateParameters();
    }

    @Override
    public void onExecutionStart() throws AutomationFrameworkException {

        if ("True".equalsIgnoreCase(System.getProperty("dbProvided"))) {
            logger.info("Skipping database creation ....");
            return;
        }
        logger.info("Database type : " + dbType);
        logger.info("Database name : " + dbName);
        try {
            switch (dbType) {
            case MY_SQL:
                setUpMysql();
                break;
            case MS_SQL:
                setUpMssql();
                break;
            case POSTGRES:
                setUpPostgres();
                break;
            case DB2:
            case ORACLE:
                // clear db
                executeScript(dbClearScripts + dbType + "/" + dbType + "_" + scriptSuffix, true);
                // source script
                executeScript(scriptBaseDir + "/" + dbType + "/" + dbType + "_" + scriptSuffix, false);
                break;
            default:
                logger.info("Db type '" + dbType + "' is not supported by the framework, if you have configured the "
                                    + "database with respective scripts, the test cases will run smoothly.");
            }
        } catch (Exception ex) {
            throw new AutomationFrameworkException(ex);
        }
        logger.info("Database configured successfully.");
    }

    /**
     * Drop db if exists and create again to source the script.
     *
     * @throws Exception Exception
     */
    private void setUpPostgres() throws Exception {

        // create db
        String dbUrl = connectionUrl.replace("/" + dbName, "/");
        executeUpdate(dbUrl, "drop database if exists " + dbName + ";");
        executeUpdate(dbUrl, "create database " + dbName + ";");

        // create tables
        scriptPath = scriptBaseDir + "/postgres/postgresql_" + scriptSuffix;
        scriptPath = getSystemDependentPath(scriptPath);
        File file = new File(scriptPath);
        dbUrl = connectionUrl.concat("?" + ALLOW_MULTIPLE_QUERIES);
        executeUpdate(dbUrl, FileUtils.readFileToString(file, StandardCharsets.UTF_8));
    }

    /**
     * Drop db if exists and create again to source the script.
     *
     * @throws Exception Exception
     */
    private void setUpMssql() throws Exception {

        // create db
        String dbUrl = connectionUrl.replace(";databaseName=" + dbName, "").concat(";" + ALLOW_MULTIPLE_QUERIES);
        List<String> schema = new ArrayList<>();
        schema.add("USE master;");
        schema.add("IF EXISTS(select * from sys.databases where name='" + dbName + "') DROP DATABASE " + dbName + ";");
        schema.add("CREATE DATABASE " + dbName + ";");
        executeUpdate(dbUrl, String.join("", schema));

        // create tables
        dbUrl = connectionUrl.concat(";" + ALLOW_MULTIPLE_QUERIES);
        scriptPath = scriptBaseDir + "/mssql/mssql_" + scriptSuffix;
        scriptPath = getSystemDependentPath(scriptPath);
        File file = new File(scriptPath);
        executeUpdate(dbUrl, FileUtils.readFileToString(file, StandardCharsets.UTF_8));
    }

    /**
     * Drop db if exists and create again to source the script.
     *
     * @throws Exception Exception
     */
    private void setUpMysql() throws Exception {

        scriptPath = scriptBaseDir + "/mysql/mysql_" + scriptSuffix;
        String dbUrl = connectionUrl.replace("/" + dbName, "").concat("&" + ALLOW_MULTIPLE_QUERIES);
        scriptPath = getSystemDependentPath(scriptPath);
        File file = new File(scriptPath);
        List<String> schema = new ArrayList<>();
        schema.add("drop database if exists " + dbName + ";");
        schema.add("create database " + dbName + " character set latin1;");
        schema.add("use " + dbName + ";");
        schema.add(FileUtils.readFileToString(file, StandardCharsets.UTF_8));
        executeUpdate(dbUrl, String.join("", schema));
        if (createSecondary) {
            schema = new ArrayList<>();
            schema.add("drop database if exists " + SECONDARY_DB + ";");
            schema.add("create database " + SECONDARY_DB + " character set latin1;");
            schema.add("use " + SECONDARY_DB + ";");
            schema.add(FileUtils.readFileToString(file, StandardCharsets.UTF_8));
            executeUpdate(dbUrl, String.join("", schema));
        }
    }

    @Override
    public void onExecutionFinish() {
        // do nothing
    }

    /**
     * Delimits the script with "script-delimiter" and executes all query in db.
     *
     * @param scriptFilePath   Delimits the script with "script-delimiter" and executes all query in db
     * @param ignoreExceptions whether to ignore if any exception occurred while executing the script
     * @throws Exception Exception
     */
    private void executeScript(String scriptFilePath, boolean ignoreExceptions) throws Exception {

        File file = new File(getSystemDependentPath(scriptFilePath));
        String[] queries = FileUtils.readFileToString(file, StandardCharsets.UTF_8).split(delimiter);
        for (String query : queries) {
            query = query.trim();
            if (!query.isEmpty()) {
                try {
                    executeUpdate(connectionUrl, query);
                } catch (Exception ex) {
                    if (!ignoreExceptions) {
                        throw new AutomationFrameworkException(ex);
                    }
                }
            }
        }
    }

    private void executeUpdate(String dbUrl, String sql) throws Exception {

        logger.info("Executing sql : " + sql);
        try (Connection conn = DriverManager.getConnection(dbUrl, userName, pwd);
                Statement statement = conn.createStatement()) {
            statement.executeUpdate(sql);
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
            case "db-clear-scripts-base-dir":
                dbClearScripts = value;
                break;
            case "create-secondary":
                createSecondary = Boolean.parseBoolean(value);
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
            connectionUrl = resolveValue(parseToml.getString("datasource[0].url").replaceAll("amp;", ""),
                                         DEFAULT_DB_URL);
            userName = resolveValue(parseToml.getString("datasource[0].username"), DEFAULT_DB_USER);
            pwd = resolveValue(parseToml.getString("datasource[0].password"), DEFAULT_DB_PWD);
            // set driver
            resolveValue(parseToml.getString("datasource[0].driver"), DEFAULT_DRIVER);
            URI uri = URI.create(connectionUrl.substring(5));
            dbType = uri.getScheme();
            String path = uri.getPath();

            if (path != null) {
                if (Stream.of(MY_SQL, DB2, POSTGRES).anyMatch(s -> s.equals(dbType))) {
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

    private String resolveValue(String tomlValue, String defValue) {

        if (tomlValue.startsWith("$sys{")) {
            String value = defValue;
            String envVariableName = StringUtils.substringBetween(tomlValue, "$sys{", "}");
            String resolvedEnvValue = System.getProperty(envVariableName);
            if (!StringUtils.isEmpty(resolvedEnvValue)) {
                value = resolvedEnvValue;
            }
            System.setProperty(envVariableName, value);
            return value;
        } else {
            return tomlValue;
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
