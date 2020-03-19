/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.micro.integrator.security.user.core.util;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.tomcat.jdbc.pool.PoolProperties;
import org.wso2.micro.integrator.security.user.api.RealmConfiguration;
import org.wso2.micro.integrator.security.user.core.UserStoreException;
import org.wso2.micro.integrator.security.user.core.jdbc.JDBCRealmConstants;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import javax.naming.InitialContext;
import javax.sql.DataSource;

import static org.wso2.micro.core.util.CarbonUtils.resolveSystemProperty;
import static org.wso2.micro.integrator.security.user.core.constants.UserCoreErrorConstants.ErrorMessages.ERROR_CODE_DUPLICATE_WHILE_WRITING_TO_DATABASE;

public class DatabaseUtil {

    private static final int DEFAULT_MAX_ACTIVE = 40;
    private static final int DEFAULT_MAX_WAIT = 1000 * 60;
    private static final int DEFAULT_MIN_IDLE = 5;
    private static final int DEFAULT_MAX_IDLE = 6;
    private static Log log = LogFactory.getLog(DatabaseUtil.class);
    private static DataSource dataSource = null;
    private static final String VALIDATION_INTERVAL = "validationInterval";
    private static final long DEFAULT_VALIDATION_INTERVAL = 30000;

    /**
     * Gets a database pooling connection. If a pool is not created this will create a connection pool.
     *
     * @param realmConfig The realm configuration. This includes necessary configuration parameters needed to
     *                    create a database pool.
     *                    <p/>
     *                    NOTE : If we use this there will be a single connection for all tenants. But there might be a requirement
     *                    where different tenants want to connect to multiple data sources. In that case we need to create
     *                    a dataSource for each tenant.
     * @return A database pool.
     */
    public static synchronized DataSource getRealmDataSource(RealmConfiguration realmConfig) {

        if (dataSource == null) {
            return createRealmDataSource(realmConfig);
        } else {
            return dataSource;
        }
    }

    /**
     * Close all database connections in the pool.
     */
    public static synchronized void closeDatabasePoolConnection() {
        if (dataSource != null && dataSource instanceof org.apache.tomcat.jdbc.pool.DataSource) {
            ((org.apache.tomcat.jdbc.pool.DataSource) dataSource).close();
            dataSource = null;
        }
    }

    private static DataSource lookupDataSource(String dataSourceName) {
        try {
            return (DataSource) InitialContext.doLookup(dataSourceName);
        } catch (Exception e) {
            throw new RuntimeException("Error in looking up data source: " + e.getMessage(), e);
        }
    }

    public static DataSource createUserStoreDataSource(RealmConfiguration realmConfig) {

        String dataSourceName = realmConfig.getUserStoreProperty(JDBCRealmConstants.DATASOURCE);
        if (dataSourceName != null) {
            dataSourceName = resolveSystemProperty(dataSourceName);
            return lookupDataSource(dataSourceName);
        }

        PoolProperties poolProperties = new PoolProperties();

        if (realmConfig.getUserStoreProperty(JDBCRealmConstants.DRIVER_NAME) == null) {
            return null;
        }
        poolProperties.setDriverClassName(realmConfig.getUserStoreProperty(JDBCRealmConstants.DRIVER_NAME));
        poolProperties.setUrl(realmConfig.getUserStoreProperty(JDBCRealmConstants.URL));
        poolProperties.setUsername(realmConfig.getUserStoreProperty(JDBCRealmConstants.USER_NAME));
        poolProperties.setPassword(realmConfig.getUserStoreProperty(JDBCRealmConstants.PASSWORD));

        if (realmConfig.getUserStoreProperty(JDBCRealmConstants.MAX_ACTIVE) != null &&
                !realmConfig.getUserStoreProperty(JDBCRealmConstants.MAX_ACTIVE).trim().equals("")) {
            poolProperties.setMaxActive(Integer.parseInt(realmConfig.getUserStoreProperty(
                    JDBCRealmConstants.MAX_ACTIVE)));
        } else {
            poolProperties.setMaxActive(DEFAULT_MAX_ACTIVE);
        }

        if (realmConfig.getUserStoreProperty(JDBCRealmConstants.MIN_IDLE) != null &&
                !realmConfig.getUserStoreProperty(JDBCRealmConstants.MIN_IDLE).trim().equals("")) {
            poolProperties.setMinIdle(Integer.parseInt(realmConfig.getUserStoreProperty(
                    JDBCRealmConstants.MIN_IDLE)));
        } else {
            poolProperties.setMinIdle(DEFAULT_MIN_IDLE);
        }

        if (realmConfig.getUserStoreProperty(JDBCRealmConstants.MAX_IDLE) != null &&
                !realmConfig.getUserStoreProperty(JDBCRealmConstants.MAX_IDLE).trim().equals("")) {
            poolProperties.setMaxIdle(Integer.parseInt(realmConfig.getUserStoreProperty(
                    JDBCRealmConstants.MAX_IDLE)));
        } else {
            poolProperties.setMaxIdle(DEFAULT_MAX_IDLE);
        }

        if (realmConfig.getUserStoreProperty(JDBCRealmConstants.MAX_WAIT) != null &&
                !realmConfig.getUserStoreProperty(JDBCRealmConstants.MAX_WAIT).trim().equals("")) {
            poolProperties.setMaxWait(Integer.parseInt(realmConfig.getUserStoreProperty(
                    JDBCRealmConstants.MAX_WAIT)));
        } else {
            poolProperties.setMaxWait(DEFAULT_MAX_WAIT);
        }

        if (realmConfig.getUserStoreProperty(JDBCRealmConstants.TEST_WHILE_IDLE) != null &&
                !realmConfig.getUserStoreProperty(JDBCRealmConstants.TEST_WHILE_IDLE).trim().equals("")) {
            poolProperties.setTestWhileIdle(Boolean.parseBoolean(realmConfig.getUserStoreProperty(
                    JDBCRealmConstants.TEST_WHILE_IDLE)));
        }

        if (realmConfig.getUserStoreProperty(JDBCRealmConstants.TIME_BETWEEN_EVICTION_RUNS_MILLIS) != null &&
                !realmConfig.getUserStoreProperty(
                        JDBCRealmConstants.TIME_BETWEEN_EVICTION_RUNS_MILLIS).trim().equals("")) {
            poolProperties.setTimeBetweenEvictionRunsMillis(Integer.parseInt(
                    realmConfig.getUserStoreProperty(
                            JDBCRealmConstants.TIME_BETWEEN_EVICTION_RUNS_MILLIS)));
        }

        if (realmConfig.getUserStoreProperty(JDBCRealmConstants.MIN_EVIC_TABLE_IDLE_TIME_MILLIS) != null &&
                !realmConfig.getUserStoreProperty(
                        JDBCRealmConstants.MIN_EVIC_TABLE_IDLE_TIME_MILLIS).trim().equals("")) {
            poolProperties.setMinEvictableIdleTimeMillis(Integer.parseInt(realmConfig.getUserStoreProperty(
                    JDBCRealmConstants.MIN_EVIC_TABLE_IDLE_TIME_MILLIS)));
        }

        if (StringUtils.isNotEmpty(realmConfig.getUserStoreProperty(JDBCRealmConstants.VALIDATION_QUERY)) &&
                !realmConfig.getUserStoreProperty(JDBCRealmConstants.VALIDATION_QUERY).trim().isEmpty()) {
            poolProperties.setValidationQuery(realmConfig.getUserStoreProperty(JDBCRealmConstants.VALIDATION_QUERY));
        }

        if (StringUtils.isNotEmpty(realmConfig.getUserStoreProperty(VALIDATION_INTERVAL)) &&
                StringUtils.isNumeric(realmConfig.getUserStoreProperty(VALIDATION_INTERVAL))) {
            poolProperties.setValidationInterval(Long.parseLong(realmConfig.getUserStoreProperty(
                    VALIDATION_INTERVAL)));
        } else {
            poolProperties.setValidationInterval(DEFAULT_VALIDATION_INTERVAL);
        }

        if (StringUtils.isNotEmpty(realmConfig.getUserStoreProperty(JDBCRealmConstants.DEFAULT_AUTO_COMMIT)) &&
                !realmConfig.getUserStoreProperty(JDBCRealmConstants.DEFAULT_AUTO_COMMIT).trim().isEmpty()) {
            poolProperties.setDefaultAutoCommit(Boolean.parseBoolean(realmConfig.getUserStoreProperty
                    (JDBCRealmConstants.DEFAULT_AUTO_COMMIT)));
        }

        if (StringUtils.isNotEmpty(realmConfig.getUserStoreProperty(JDBCRealmConstants.DEFAULT_READ_ONLY)) &&
                !realmConfig.getUserStoreProperty(JDBCRealmConstants.DEFAULT_READ_ONLY).trim().isEmpty()) {
            poolProperties.setDefaultReadOnly(Boolean.parseBoolean(realmConfig.getUserStoreProperty
                    (JDBCRealmConstants.DEFAULT_READ_ONLY)));
        }

        if (StringUtils.isNotEmpty(realmConfig.getUserStoreProperty(JDBCRealmConstants.DEFAULT_CATALOG)) &&
                !realmConfig.getUserStoreProperty(JDBCRealmConstants.DEFAULT_CATALOG).trim().isEmpty()) {
            poolProperties.setDefaultCatalog(realmConfig.getUserStoreProperty(JDBCRealmConstants.DEFAULT_CATALOG));
        }

        if (StringUtils.isNotEmpty(realmConfig.getUserStoreProperty(JDBCRealmConstants.INITIAL_SIZE)) &&
                StringUtils.isNumeric(JDBCRealmConstants.INITIAL_SIZE)) {
            poolProperties.setInitialSize(Integer.parseInt(realmConfig.getUserStoreProperty(JDBCRealmConstants
                    .INITIAL_SIZE)));
        }

        if (StringUtils.isNotEmpty(realmConfig.getUserStoreProperty(JDBCRealmConstants.TEST_ON_RETURN)) &&
                !realmConfig.getUserStoreProperty(JDBCRealmConstants.TEST_ON_RETURN).trim().isEmpty()) {
            poolProperties.setTestOnReturn(Boolean.parseBoolean(realmConfig.getUserStoreProperty
                    (JDBCRealmConstants.TEST_ON_RETURN)));
        }

        if (StringUtils.isNotEmpty(realmConfig.getUserStoreProperty(JDBCRealmConstants.TEST_ON_BORROW)) &&
                !realmConfig.getUserStoreProperty(JDBCRealmConstants.TEST_ON_BORROW).trim().isEmpty()) {
            poolProperties.setTestOnBorrow(Boolean.parseBoolean(realmConfig.getUserStoreProperty
                    (JDBCRealmConstants.TEST_ON_BORROW)));
        }

        if (StringUtils.isNotEmpty(realmConfig.getUserStoreProperty(JDBCRealmConstants.VALIDATOR_CLASS_NAME)) &&
                !realmConfig.getUserStoreProperty(JDBCRealmConstants.VALIDATOR_CLASS_NAME).trim().isEmpty()) {
            poolProperties.setValidatorClassName(realmConfig.getUserStoreProperty(JDBCRealmConstants
                    .VALIDATOR_CLASS_NAME));
        }

        if (StringUtils.isNotEmpty(realmConfig.getUserStoreProperty(JDBCRealmConstants.NUM_TESTS_PER_EVICTION_RUN)) &&
                StringUtils.isNumeric(JDBCRealmConstants.NUM_TESTS_PER_EVICTION_RUN)) {
            poolProperties.setNumTestsPerEvictionRun(Integer.parseInt(realmConfig.getUserStoreProperty(
                    JDBCRealmConstants.NUM_TESTS_PER_EVICTION_RUN)));
        }

        if (StringUtils.isNotEmpty(realmConfig.getUserStoreProperty(JDBCRealmConstants
                .ACCESS_TO_UNDERLYING_CONNECTION_ALLOWED)) && !realmConfig.getUserStoreProperty(JDBCRealmConstants
                .ACCESS_TO_UNDERLYING_CONNECTION_ALLOWED).trim().isEmpty()) {
            poolProperties.setAccessToUnderlyingConnectionAllowed(Boolean.parseBoolean(realmConfig.getUserStoreProperty
                    (JDBCRealmConstants.ACCESS_TO_UNDERLYING_CONNECTION_ALLOWED)));
        }

        if (StringUtils.isNotEmpty(realmConfig.getUserStoreProperty(JDBCRealmConstants.REMOVE_ABANDONED)) &&
                !realmConfig.getUserStoreProperty(JDBCRealmConstants.REMOVE_ABANDONED).trim().isEmpty()) {
            poolProperties.setRemoveAbandoned(Boolean.parseBoolean(realmConfig.getUserStoreProperty
                    (JDBCRealmConstants.REMOVE_ABANDONED)));
        }

        if (StringUtils.isNotEmpty(realmConfig.getUserStoreProperty(JDBCRealmConstants.REMOVE_ABANDONED_TIMEOUT)) &&
                StringUtils.isNumeric(JDBCRealmConstants.REMOVE_ABANDONED_TIMEOUT)) {
            poolProperties.setRemoveAbandonedTimeout(Integer.parseInt(realmConfig.getUserStoreProperty(
                    JDBCRealmConstants.REMOVE_ABANDONED_TIMEOUT)));
        }

        if (StringUtils.isNotEmpty(realmConfig.getUserStoreProperty(JDBCRealmConstants.LOG_ABANDONED)) &&
                !realmConfig.getUserStoreProperty(JDBCRealmConstants.LOG_ABANDONED).trim().isEmpty()) {
            poolProperties.setLogAbandoned(Boolean.parseBoolean(realmConfig.getUserStoreProperty
                    (JDBCRealmConstants.LOG_ABANDONED)));
        }

        if (StringUtils.isNotEmpty(realmConfig.getUserStoreProperty(JDBCRealmConstants.CONNECTION_PROPERTIES)) &&
                !realmConfig.getUserStoreProperty(JDBCRealmConstants.CONNECTION_PROPERTIES).trim().isEmpty()) {
            poolProperties.setConnectionProperties(realmConfig.getUserStoreProperty(JDBCRealmConstants
                    .CONNECTION_PROPERTIES));
        }

        if (StringUtils.isNotEmpty(realmConfig.getUserStoreProperty(JDBCRealmConstants.INIT_SQL)) &&
                !realmConfig.getUserStoreProperty(JDBCRealmConstants.INIT_SQL).trim().isEmpty()) {
            poolProperties.setInitSQL(realmConfig.getUserStoreProperty(JDBCRealmConstants.INIT_SQL));
        }

        if (StringUtils.isNotEmpty(realmConfig.getUserStoreProperty(JDBCRealmConstants.JDBC_INTERCEPTORS)) &&
                !realmConfig.getUserStoreProperty(JDBCRealmConstants.JDBC_INTERCEPTORS).trim().isEmpty()) {
            poolProperties.setJdbcInterceptors(realmConfig.getUserStoreProperty(JDBCRealmConstants.JDBC_INTERCEPTORS));
        }

        if (StringUtils.isNotEmpty(realmConfig.getUserStoreProperty(JDBCRealmConstants.JMX_ENABLED)) &&
                !realmConfig.getUserStoreProperty(JDBCRealmConstants.JMX_ENABLED).trim().isEmpty()) {
            poolProperties.setJmxEnabled(Boolean.parseBoolean(realmConfig.getUserStoreProperty
                    (JDBCRealmConstants.JMX_ENABLED)));
        }

        if (StringUtils.isNotEmpty(realmConfig.getUserStoreProperty(JDBCRealmConstants.FAIR_QUEUE)) &&
                !realmConfig.getUserStoreProperty(JDBCRealmConstants.FAIR_QUEUE).trim().isEmpty()) {
            poolProperties.setFairQueue(Boolean.parseBoolean(realmConfig.getUserStoreProperty
                    (JDBCRealmConstants.FAIR_QUEUE)));
        }

        if (StringUtils.isNumeric(JDBCRealmConstants.ABANDON_WHEN_PERCENTAGE_FULL)) {
            poolProperties.setAbandonWhenPercentageFull(Integer.parseInt(realmConfig.getUserStoreProperty(
                    JDBCRealmConstants.ABANDON_WHEN_PERCENTAGE_FULL)));
        }

        if (StringUtils.isNotEmpty(realmConfig.getUserStoreProperty(JDBCRealmConstants.MAX_AGE)) &&
                StringUtils.isNumeric(JDBCRealmConstants.MAX_AGE)) {
            poolProperties.setMaxAge(Integer.parseInt(realmConfig.getUserStoreProperty(JDBCRealmConstants.MAX_AGE)));
        }

        if (StringUtils.isNotEmpty(realmConfig.getUserStoreProperty(JDBCRealmConstants.USE_EQUALS)) &&
                !realmConfig.getUserStoreProperty(JDBCRealmConstants.USE_EQUALS).trim().isEmpty()) {
            poolProperties.setUseEquals(Boolean.parseBoolean(realmConfig.getUserStoreProperty(JDBCRealmConstants
                    .USE_EQUALS)));
        }

        if (StringUtils.isNotEmpty(realmConfig.getUserStoreProperty(JDBCRealmConstants.SUSPECT_TIMEOUT)) &&
                StringUtils.isNumeric(JDBCRealmConstants.SUSPECT_TIMEOUT)) {
            poolProperties.setSuspectTimeout(Integer.parseInt(realmConfig.getUserStoreProperty(JDBCRealmConstants
                    .SUSPECT_TIMEOUT)));
        }

        if (StringUtils.isNotEmpty(realmConfig.getUserStoreProperty(JDBCRealmConstants.VALIDATION_QUERY_TIMEOUT))
                && StringUtils.isNumeric(JDBCRealmConstants.VALIDATION_QUERY_TIMEOUT)) {
            poolProperties.setValidationQueryTimeout(Integer.parseInt(realmConfig.getUserStoreProperty(
                    JDBCRealmConstants.VALIDATION_QUERY_TIMEOUT)));
        }

        if (StringUtils.isNotEmpty(realmConfig.getUserStoreProperty(JDBCRealmConstants.ALTERNATE_USERNAME_ALLOWED)) &&
                !realmConfig.getUserStoreProperty(JDBCRealmConstants.ALTERNATE_USERNAME_ALLOWED).trim().isEmpty()) {
            poolProperties.setAlternateUsernameAllowed(Boolean.parseBoolean(realmConfig.getUserStoreProperty
                    (JDBCRealmConstants.ALTERNATE_USERNAME_ALLOWED)));
        }

        if (StringUtils.isNotEmpty(realmConfig.getUserStoreProperty(JDBCRealmConstants.COMMIT_ON_RETURN)) &&
                !realmConfig.getUserStoreProperty(JDBCRealmConstants.COMMIT_ON_RETURN).trim().isEmpty()) {
            poolProperties.setCommitOnReturn(Boolean.parseBoolean(realmConfig.getUserStoreProperty
                    (JDBCRealmConstants.COMMIT_ON_RETURN)));
        }

        if (StringUtils.isNotEmpty(realmConfig.getUserStoreProperty(JDBCRealmConstants.ROLLBACK_ON_RETURN)) &&
                !realmConfig.getUserStoreProperty(JDBCRealmConstants.ROLLBACK_ON_RETURN).trim().isEmpty()) {
            poolProperties.setRollbackOnReturn(Boolean.parseBoolean(realmConfig.getUserStoreProperty
                    (JDBCRealmConstants.ROLLBACK_ON_RETURN)));
        }

        setIsolationLevel(poolProperties, realmConfig.getUserStoreProperty(JDBCRealmConstants
                .DEFAULT_TRANSACTION_ISOLATION));

        return new org.apache.tomcat.jdbc.pool.DataSource(poolProperties);
    }

    private static DataSource createRealmDataSource(RealmConfiguration realmConfig) {

        String dataSourceName = realmConfig.getRealmProperty(JDBCRealmConstants.DATASOURCE);
        if (dataSourceName != null) {
            dataSourceName = resolveSystemProperty(dataSourceName);
            return lookupDataSource(dataSourceName);
        }

        PoolProperties poolProperties = new PoolProperties();

        poolProperties.setDriverClassName(realmConfig.getRealmProperty(JDBCRealmConstants.DRIVER_NAME));
        poolProperties.setUrl(realmConfig.getRealmProperty(JDBCRealmConstants.URL));
        poolProperties.setUsername(realmConfig.getRealmProperty(JDBCRealmConstants.USER_NAME));
        poolProperties.setPassword(realmConfig.getRealmProperty(JDBCRealmConstants.PASSWORD));

        if (realmConfig.getRealmProperty(JDBCRealmConstants.MAX_ACTIVE) != null &&
                !realmConfig.getRealmProperty(JDBCRealmConstants.MAX_ACTIVE).trim().equals("")) {
            poolProperties.setMaxActive(Integer.parseInt(realmConfig.getRealmProperty(
                    JDBCRealmConstants.MAX_ACTIVE)));
        } else {
            poolProperties.setMaxActive(DEFAULT_MAX_ACTIVE);
        }

        if (realmConfig.getRealmProperty(JDBCRealmConstants.MIN_IDLE) != null &&
                !realmConfig.getRealmProperty(JDBCRealmConstants.MIN_IDLE).trim().equals("")) {
            poolProperties.setMinIdle(Integer.parseInt(realmConfig.getRealmProperty(
                    JDBCRealmConstants.MIN_IDLE)));
        } else {
            poolProperties.setMinIdle(DEFAULT_MIN_IDLE);
        }

        if (realmConfig.getRealmProperty(JDBCRealmConstants.MAX_IDLE) != null &&
                !realmConfig.getRealmProperty(JDBCRealmConstants.MAX_IDLE).trim().equals("")) {
            poolProperties.setMaxIdle(Integer.parseInt(realmConfig.getRealmProperty(
                    JDBCRealmConstants.MAX_IDLE)));
        } else {
            poolProperties.setMaxIdle(DEFAULT_MAX_IDLE);
        }

        if (realmConfig.getRealmProperty(JDBCRealmConstants.MAX_WAIT) != null &&
                !realmConfig.getRealmProperty(JDBCRealmConstants.MAX_WAIT).trim().equals("")) {
            poolProperties.setMaxWait(Integer.parseInt(realmConfig.getRealmProperty(
                    JDBCRealmConstants.MAX_WAIT)));
        } else {
            poolProperties.setMaxWait(DEFAULT_MAX_WAIT);
        }

        if (realmConfig.getRealmProperty(JDBCRealmConstants.TEST_WHILE_IDLE) != null &&
                !realmConfig.getRealmProperty(JDBCRealmConstants.TEST_WHILE_IDLE).trim().equals("")) {
            poolProperties.setTestWhileIdle(Boolean.parseBoolean(realmConfig.getRealmProperty(
                    JDBCRealmConstants.TEST_WHILE_IDLE)));
        }

        if (realmConfig.getRealmProperty(JDBCRealmConstants.TIME_BETWEEN_EVICTION_RUNS_MILLIS) != null &&
                !realmConfig.getRealmProperty(
                        JDBCRealmConstants.TIME_BETWEEN_EVICTION_RUNS_MILLIS).trim().equals("")) {
            poolProperties.setTimeBetweenEvictionRunsMillis(Integer.parseInt(
                    realmConfig.getRealmProperty(
                            JDBCRealmConstants.TIME_BETWEEN_EVICTION_RUNS_MILLIS)));
        }

        if (realmConfig.getRealmProperty(JDBCRealmConstants.MIN_EVIC_TABLE_IDLE_TIME_MILLIS) != null &&
                !realmConfig.getRealmProperty(
                        JDBCRealmConstants.MIN_EVIC_TABLE_IDLE_TIME_MILLIS).trim().equals("")) {
            poolProperties.setMinEvictableIdleTimeMillis(Integer.parseInt(realmConfig.getRealmProperty(
                    JDBCRealmConstants.MIN_EVIC_TABLE_IDLE_TIME_MILLIS)));
        }

        if (StringUtils.isNotEmpty(realmConfig.getRealmProperty(JDBCRealmConstants.VALIDATION_QUERY)) &&
                !realmConfig.getRealmProperty(JDBCRealmConstants.VALIDATION_QUERY).trim().isEmpty()) {
            poolProperties.setValidationQuery(realmConfig.getRealmProperty(JDBCRealmConstants.VALIDATION_QUERY));
        }

        if (StringUtils.isNotEmpty(realmConfig.getRealmProperty(VALIDATION_INTERVAL)) &&
                StringUtils.isNumeric(realmConfig.getRealmProperty(VALIDATION_INTERVAL))) {
            poolProperties.setValidationInterval(Long.parseLong(realmConfig.getRealmProperty(
                    VALIDATION_INTERVAL)));
        } else {
            poolProperties.setValidationInterval(DEFAULT_VALIDATION_INTERVAL);
        }

        if (StringUtils.isNotEmpty(realmConfig.getRealmProperty(JDBCRealmConstants.DEFAULT_AUTO_COMMIT)) &&
                !realmConfig.getRealmProperty(JDBCRealmConstants.DEFAULT_AUTO_COMMIT).trim().isEmpty()) {
            poolProperties.setDefaultAutoCommit(Boolean.parseBoolean(realmConfig.getRealmProperty
                    (JDBCRealmConstants.DEFAULT_AUTO_COMMIT)));
        }

        if (StringUtils.isNotEmpty(realmConfig.getRealmProperty(JDBCRealmConstants.DEFAULT_READ_ONLY)) &&
                !realmConfig.getRealmProperty(JDBCRealmConstants.DEFAULT_READ_ONLY).trim().isEmpty()) {
            poolProperties.setDefaultReadOnly(Boolean.parseBoolean(realmConfig.getRealmProperty
                    (JDBCRealmConstants.DEFAULT_READ_ONLY)));
        }

        if (StringUtils.isNotEmpty(realmConfig.getRealmProperty(JDBCRealmConstants.DEFAULT_CATALOG)) &&
                !realmConfig.getRealmProperty(JDBCRealmConstants.DEFAULT_CATALOG).trim().isEmpty()) {
            poolProperties.setDefaultCatalog(realmConfig.getRealmProperty(JDBCRealmConstants.DEFAULT_CATALOG));
        }

        if (StringUtils.isNotEmpty(realmConfig.getRealmProperty(JDBCRealmConstants.INITIAL_SIZE)) &&
                StringUtils.isNumeric(JDBCRealmConstants.INITIAL_SIZE)) {
            poolProperties.setInitialSize(Integer.parseInt(realmConfig.getRealmProperty(JDBCRealmConstants
                    .INITIAL_SIZE)));
        }

        if (StringUtils.isNotEmpty(realmConfig.getRealmProperty(JDBCRealmConstants.TEST_ON_RETURN)) &&
                !realmConfig.getRealmProperty(JDBCRealmConstants.TEST_ON_RETURN).trim().isEmpty()) {
            poolProperties.setTestOnReturn(Boolean.parseBoolean(realmConfig.getRealmProperty
                    (JDBCRealmConstants.TEST_ON_RETURN)));
        }

        if (StringUtils.isNotEmpty(realmConfig.getRealmProperty(JDBCRealmConstants.TEST_ON_BORROW)) &&
                !realmConfig.getRealmProperty(JDBCRealmConstants.TEST_ON_BORROW).trim().isEmpty()) {
            poolProperties.setTestOnBorrow(Boolean.parseBoolean(realmConfig.getRealmProperty
                    (JDBCRealmConstants.TEST_ON_BORROW)));
        }

        if (StringUtils.isNotEmpty(realmConfig.getRealmProperty(JDBCRealmConstants.VALIDATOR_CLASS_NAME)) &&
                !realmConfig.getRealmProperty(JDBCRealmConstants.VALIDATOR_CLASS_NAME).trim().isEmpty()) {
            poolProperties.setValidatorClassName(realmConfig.getRealmProperty(JDBCRealmConstants
                    .VALIDATOR_CLASS_NAME));
        }

        if (StringUtils.isNotEmpty(realmConfig.getRealmProperty(JDBCRealmConstants.NUM_TESTS_PER_EVICTION_RUN)) &&
                StringUtils.isNumeric(JDBCRealmConstants.NUM_TESTS_PER_EVICTION_RUN)) {
            poolProperties.setNumTestsPerEvictionRun(Integer.parseInt(realmConfig.getRealmProperty(
                    JDBCRealmConstants.NUM_TESTS_PER_EVICTION_RUN)));
        }

        if (StringUtils.isNotEmpty(realmConfig.getRealmProperty(JDBCRealmConstants
                .ACCESS_TO_UNDERLYING_CONNECTION_ALLOWED)) && !realmConfig.getRealmProperty(JDBCRealmConstants
                .ACCESS_TO_UNDERLYING_CONNECTION_ALLOWED).trim().isEmpty()) {
            poolProperties.setAccessToUnderlyingConnectionAllowed(Boolean.parseBoolean(realmConfig.getRealmProperty
                    (JDBCRealmConstants.ACCESS_TO_UNDERLYING_CONNECTION_ALLOWED)));
        }

        if (StringUtils.isNotEmpty(realmConfig.getRealmProperty(JDBCRealmConstants.REMOVE_ABANDONED)) &&
                !realmConfig.getRealmProperty(JDBCRealmConstants.REMOVE_ABANDONED).trim().isEmpty()) {
            poolProperties.setRemoveAbandoned(Boolean.parseBoolean(realmConfig.getRealmProperty
                    (JDBCRealmConstants.REMOVE_ABANDONED)));
        }

        if (StringUtils.isNotEmpty(realmConfig.getRealmProperty(JDBCRealmConstants.REMOVE_ABANDONED_TIMEOUT)) &&
                StringUtils.isNumeric(JDBCRealmConstants.REMOVE_ABANDONED_TIMEOUT)) {
            poolProperties.setRemoveAbandonedTimeout(Integer.parseInt(realmConfig.getRealmProperty(
                    JDBCRealmConstants.REMOVE_ABANDONED_TIMEOUT)));
        }

        if (StringUtils.isNotEmpty(realmConfig.getRealmProperty(JDBCRealmConstants.LOG_ABANDONED)) &&
                !realmConfig.getRealmProperty(JDBCRealmConstants.LOG_ABANDONED).trim().isEmpty()) {
            poolProperties.setLogAbandoned(Boolean.parseBoolean(realmConfig.getRealmProperty
                    (JDBCRealmConstants.LOG_ABANDONED)));
        }

        if (StringUtils.isNotEmpty(realmConfig.getRealmProperty(JDBCRealmConstants.CONNECTION_PROPERTIES)) &&
                !realmConfig.getRealmProperty(JDBCRealmConstants.CONNECTION_PROPERTIES).trim().isEmpty()) {
            poolProperties.setConnectionProperties(realmConfig.getRealmProperty(JDBCRealmConstants
                    .CONNECTION_PROPERTIES));
        }

        if (StringUtils.isNotEmpty(realmConfig.getRealmProperty(JDBCRealmConstants.INIT_SQL)) &&
                !realmConfig.getRealmProperty(JDBCRealmConstants.INIT_SQL).trim().isEmpty()) {
            poolProperties.setInitSQL(realmConfig.getRealmProperty(JDBCRealmConstants.INIT_SQL));
        }

        if (StringUtils.isNotEmpty(realmConfig.getRealmProperty(JDBCRealmConstants.JDBC_INTERCEPTORS)) &&
                !realmConfig.getRealmProperty(JDBCRealmConstants.JDBC_INTERCEPTORS).trim().isEmpty()) {
            poolProperties.setJdbcInterceptors(realmConfig.getRealmProperty(JDBCRealmConstants.JDBC_INTERCEPTORS));
        }

        if (StringUtils.isNotEmpty(realmConfig.getRealmProperty(JDBCRealmConstants.JMX_ENABLED)) &&
                !realmConfig.getRealmProperty(JDBCRealmConstants.JMX_ENABLED).trim().isEmpty()) {
            poolProperties.setJmxEnabled(Boolean.parseBoolean(realmConfig.getRealmProperty
                    (JDBCRealmConstants.JMX_ENABLED)));
        }

        if (StringUtils.isNotEmpty(realmConfig.getRealmProperty(JDBCRealmConstants.FAIR_QUEUE)) &&
                !realmConfig.getRealmProperty(JDBCRealmConstants.FAIR_QUEUE).trim().isEmpty()) {
            poolProperties.setFairQueue(Boolean.parseBoolean(realmConfig.getRealmProperty
                    (JDBCRealmConstants.FAIR_QUEUE)));
        }

        if (StringUtils.isNumeric(JDBCRealmConstants.ABANDON_WHEN_PERCENTAGE_FULL)) {
            poolProperties.setAbandonWhenPercentageFull(Integer.parseInt(realmConfig.getRealmProperty(
                    JDBCRealmConstants.ABANDON_WHEN_PERCENTAGE_FULL)));
        }

        if (StringUtils.isNotEmpty(realmConfig.getRealmProperty(JDBCRealmConstants.MAX_AGE)) &&
                StringUtils.isNumeric(JDBCRealmConstants.MAX_AGE)) {
            poolProperties.setMaxAge(Integer.parseInt(realmConfig.getRealmProperty(JDBCRealmConstants.MAX_AGE)));
        }

        if (StringUtils.isNotEmpty(realmConfig.getRealmProperty(JDBCRealmConstants.USE_EQUALS)) &&
                !realmConfig.getRealmProperty(JDBCRealmConstants.USE_EQUALS).trim().isEmpty()) {
            poolProperties.setUseEquals(Boolean.parseBoolean(realmConfig.getRealmProperty(JDBCRealmConstants
                    .USE_EQUALS)));
        }

        if (StringUtils.isNotEmpty(realmConfig.getRealmProperty(JDBCRealmConstants.SUSPECT_TIMEOUT)) &&
                StringUtils.isNumeric(JDBCRealmConstants.SUSPECT_TIMEOUT)) {
            poolProperties.setSuspectTimeout(Integer.parseInt(realmConfig.getRealmProperty(JDBCRealmConstants
                    .SUSPECT_TIMEOUT)));
        }

        if (StringUtils.isNotEmpty(realmConfig.getRealmProperty(JDBCRealmConstants.VALIDATION_QUERY_TIMEOUT))
                && StringUtils.isNumeric(JDBCRealmConstants.VALIDATION_QUERY_TIMEOUT)) {
            poolProperties.setValidationQueryTimeout(Integer.parseInt(realmConfig.getRealmProperty(
                    JDBCRealmConstants.VALIDATION_QUERY_TIMEOUT)));
        }

        if (StringUtils.isNotEmpty(realmConfig.getRealmProperty(JDBCRealmConstants.ALTERNATE_USERNAME_ALLOWED)) &&
                !realmConfig.getRealmProperty(JDBCRealmConstants.ALTERNATE_USERNAME_ALLOWED).trim().isEmpty()) {
            poolProperties.setAlternateUsernameAllowed(Boolean.parseBoolean(realmConfig.getRealmProperty
                    (JDBCRealmConstants.ALTERNATE_USERNAME_ALLOWED)));
        }

        if (StringUtils.isNotEmpty(realmConfig.getUserStoreProperty(JDBCRealmConstants.COMMIT_ON_RETURN)) &&
                !realmConfig.getUserStoreProperty(JDBCRealmConstants.COMMIT_ON_RETURN).trim().isEmpty()) {
            poolProperties.setCommitOnReturn(Boolean.parseBoolean(realmConfig.getUserStoreProperty
                    (JDBCRealmConstants.COMMIT_ON_RETURN)));
        }

        if (StringUtils.isNotEmpty(realmConfig.getUserStoreProperty(JDBCRealmConstants.ROLLBACK_ON_RETURN)) &&
                !realmConfig.getUserStoreProperty(JDBCRealmConstants.ROLLBACK_ON_RETURN).trim().isEmpty()) {
            poolProperties.setRollbackOnReturn(Boolean.parseBoolean(realmConfig.getUserStoreProperty
                    (JDBCRealmConstants.ROLLBACK_ON_RETURN)));
        }

        setIsolationLevel(poolProperties, realmConfig.getRealmProperty(JDBCRealmConstants
                .DEFAULT_TRANSACTION_ISOLATION));

        return (dataSource = new org.apache.tomcat.jdbc.pool.DataSource(poolProperties));
    }

    private static void setIsolationLevel(PoolProperties poolProperties, String isolationLevelString) {

        if (StringUtils.isNotEmpty(isolationLevelString)) {
            if (JDBCRealmConstants.TX_ISOLATION_LEVELS.NONE.equals(isolationLevelString)) {
                poolProperties.setDefaultTransactionIsolation(Connection.TRANSACTION_NONE);
            } else if (JDBCRealmConstants.TX_ISOLATION_LEVELS.READ_UNCOMMITTED.equals(isolationLevelString)) {
                poolProperties.setDefaultTransactionIsolation(Connection.TRANSACTION_READ_UNCOMMITTED);
            } else if (JDBCRealmConstants.TX_ISOLATION_LEVELS.READ_COMMITTED.equals(isolationLevelString)) {
                poolProperties.setDefaultTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
            } else if (JDBCRealmConstants.TX_ISOLATION_LEVELS.REPEATABLE_READ.equals(isolationLevelString)) {
                poolProperties.setDefaultTransactionIsolation(Connection.TRANSACTION_REPEATABLE_READ);
            } else if (JDBCRealmConstants.TX_ISOLATION_LEVELS.SERIALIZABLE.equals(isolationLevelString)) {
                poolProperties.setDefaultTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
            }
        }
    }

    public static String[] getStringValuesFromDatabase(Connection dbConnection, String sqlStmt, Object... params)
            throws UserStoreException {
        String[] values = new String[0];
        PreparedStatement prepStmt = null;
        ResultSet rs = null;
        try {
            prepStmt = dbConnection.prepareStatement(sqlStmt);
            if (params != null && params.length > 0) {
                for (int i = 0; i < params.length; i++) {
                    Object param = params[i];
                    if (param == null) {
                        //allow to send null data since null allowed values can be in the table. eg: domain name
                        prepStmt.setString(i + 1, null);
                        //throw new UserStoreException("Null data provided.");
                    } else if (param instanceof String) {
                        prepStmt.setString(i + 1, (String) param);
                    } else if (param instanceof Integer) {
                        prepStmt.setInt(i + 1, (Integer) param);
                    }
                }
            }
            rs = prepStmt.executeQuery();
            List<String> lst = new ArrayList<String>();
            while (rs.next()) {
                String name = rs.getString(1);
                lst.add(name);
            }
            if (lst.size() > 0) {
                values = lst.toArray(new String[lst.size()]);
            }
            return values;
        } catch (SQLException e) {
            String errorMessage = "Using sql : " + sqlStmt + " " + e.getMessage();
            if (log.isDebugEnabled()) {
                log.debug(errorMessage, e);
            }
            throw new UserStoreException(errorMessage, e);
        } finally {
            DatabaseUtil.closeAllConnections(null, rs, prepStmt);
        }
    }

    /*This retrieves two parameters, combines them and send back*/
    public static String[] getStringValuesFromDatabaseForInternalRoles(Connection dbConnection, String sqlStmt, Object... params)
            throws UserStoreException {
        String[] values = new String[0];
        PreparedStatement prepStmt = null;
        ResultSet rs = null;
        try {
            prepStmt = dbConnection.prepareStatement(sqlStmt);
            if (params != null && params.length > 0) {
                for (int i = 0; i < params.length; i++) {
                    Object param = params[i];
                    if (param == null) {
                        throw new UserStoreException("Null data provided.");
                    } else if (param instanceof String) {
                        prepStmt.setString(i + 1, (String) param);
                    } else if (param instanceof Integer) {
                        prepStmt.setInt(i + 1, (Integer) param);
                    }
                }
            }
            rs = prepStmt.executeQuery();
            List<String> lst = new ArrayList<String>();
            while (rs.next()) {
                String name = rs.getString(1);
                String domain = rs.getString(2);
                if (domain != null) {
                    name = UserCoreUtil.addDomainToName(name, domain);
                }
                lst.add(name);
            }
            if (lst.size() > 0) {
                values = lst.toArray(new String[lst.size()]);
            }
            return values;
        } catch (SQLException e) {
            String errorMessage = "Using sql : " + sqlStmt + " " + e.getMessage();
            if (log.isDebugEnabled()) {
                log.debug(errorMessage, e);
            }
            throw new UserStoreException(errorMessage, e);
        } finally {
            DatabaseUtil.closeAllConnections(null, rs, prepStmt);
        }
    }

    public static int getIntegerValueFromDatabase(Connection dbConnection, String sqlStmt,
                                                  Object... params) throws UserStoreException {
        PreparedStatement prepStmt = null;
        ResultSet rs = null;
        int value = -1;
        try {
            prepStmt = dbConnection.prepareStatement(sqlStmt);
            if (params != null && params.length > 0) {
                for (int i = 0; i < params.length; i++) {
                    Object param = params[i];
                    if (param == null) {
                        throw new UserStoreException("Null data provided.");
                    } else if (param instanceof String) {
                        prepStmt.setString(i + 1, (String) param);
                    } else if (param instanceof Integer) {
                        prepStmt.setInt(i + 1, (Integer) param);
                    }
                }
            }
            rs = prepStmt.executeQuery();
            if (rs.next()) {
                value = rs.getInt(1);
            }
            return value;
        } catch (SQLException e) {
            String errorMessage = "Using sql : " + sqlStmt + " " + e.getMessage();
            if (log.isDebugEnabled()) {
                log.debug(errorMessage, e);
            }
            throw new UserStoreException(errorMessage, e);
        } finally {
            DatabaseUtil.closeAllConnections(null, rs, prepStmt);
        }
    }

    public static void udpateUserRoleMappingInBatchModeForInternalRoles(Connection dbConnection,
                                                                        String sqlStmt, String primaryDomain, Object... params) throws UserStoreException {
        PreparedStatement prepStmt = null;
        boolean localConnection = false;
        try {
            prepStmt = dbConnection.prepareStatement(sqlStmt);
            int batchParamIndex = -1;
            if (params != null && params.length > 0) {
                for (int i = 0; i < params.length; i++) {
                    Object param = params[i];
                    if (param == null) {
                        throw new UserStoreException("Null data provided.");
                    } else if (param instanceof String[]) {
                        batchParamIndex = i;
                    } else if (param instanceof String) {
                        prepStmt.setString(i + 1, (String) param);
                    } else if (param instanceof Integer) {
                        prepStmt.setInt(i + 1, (Integer) param);
                    }
                }
            }
            if (batchParamIndex != -1) {
                String[] values = (String[]) params[batchParamIndex];
                for (String value : values) {
                    String strParam = (String) value;
                    //add domain if not set
                    strParam = UserCoreUtil.addDomainToName(strParam, primaryDomain);
                    //get domain from name
                    String domainParam = UserCoreUtil.extractDomainFromName(strParam);
                    if (domainParam != null) {
                        domainParam = domainParam.toUpperCase();
                    }
                    //set domain to sql
                    prepStmt.setString(params.length + 1, domainParam);
                    //remove domain before persisting
                    String nameWithoutDomain = UserCoreUtil.removeDomainFromName(strParam);
                    //set name in sql
                    prepStmt.setString(batchParamIndex + 1, nameWithoutDomain);
                    prepStmt.addBatch();
                }
            }

            int[] count = prepStmt.executeBatch();
            if (log.isDebugEnabled()) {
                log.debug("Executed a batch update. Query is : " + sqlStmt + ": and result is"
                        + Arrays.toString(count));
            }
            if (localConnection) {
                dbConnection.commit();
            }
        } catch (SQLException e) {
            String errorMessage = "Using sql : " + sqlStmt + " " + e.getMessage();
            if (log.isDebugEnabled()) {
                log.debug(errorMessage, e);
            }
            throw new UserStoreException(errorMessage, e);
        } finally {
            if (localConnection) {
                DatabaseUtil.closeAllConnections(dbConnection);
            }
            DatabaseUtil.closeAllConnections(null, prepStmt);
        }
    }

    public static void udpateUserRoleMappingWithExactParams(Connection dbConnection, String sqlStmt,
                                                            String[] roles, String userName,
                                                            Integer[] tenantIds, int currentTenantId)
            throws UserStoreException {
        PreparedStatement ps = null;
        boolean localConnection = false;
        try {
            ps = dbConnection.prepareStatement(sqlStmt);
            byte count = 0;
            byte index = 0;

            for (String role : roles) {
                count = 0;
                ps.setString(++count, role);
                ps.setInt(++count, tenantIds[index]);
                ps.setString(++count, userName);
                ps.setInt(++count, currentTenantId);
                ps.setInt(++count, currentTenantId);
                ps.setInt(++count, tenantIds[index]);

                ps.addBatch();
                ++index;
            }

            int[] cnt = ps.executeBatch();
            if (log.isDebugEnabled()) {
                log.debug("Executed a batch update. Query is : " + sqlStmt + ": and result is" +
                        Arrays.toString(cnt));
            }
            if (localConnection) {
                dbConnection.commit();
            }
        } catch (SQLException e) {
            String errorMessage = "Using sql : " + sqlStmt + " " + e.getMessage();
            if (log.isDebugEnabled()) {
                log.debug(errorMessage, e);
            }
            throw new UserStoreException(errorMessage, e);
        } finally {
            if (localConnection) {
                DatabaseUtil.closeAllConnections(dbConnection);
            }
            DatabaseUtil.closeAllConnections(null, ps);
        }
    }

    public static void udpateUserRoleMappingInBatchMode(Connection dbConnection, String sqlStmt,
                                                        Object... params) throws UserStoreException {
        PreparedStatement prepStmt = null;
        boolean localConnection = false;
        try {
            prepStmt = dbConnection.prepareStatement(sqlStmt);
            int batchParamIndex = -1;
            if (params != null && params.length > 0) {
                for (int i = 0; i < params.length; i++) {
                    Object param = params[i];
                    if (param == null) {
                        throw new UserStoreException("Null data provided.");
                    } else if (param instanceof String[]) {
                        batchParamIndex = i;
                    } else if (param instanceof String) {
                        prepStmt.setString(i + 1, (String) param);
                    } else if (param instanceof Integer) {
                        prepStmt.setInt(i + 1, (Integer) param);
                    }
                }
            }
            if (batchParamIndex != -1) {
                String[] values = (String[]) params[batchParamIndex];
                for (String value : values) {
                    prepStmt.setString(batchParamIndex + 1, value);
                    prepStmt.addBatch();
                }
            }

            int[] count = prepStmt.executeBatch();
            if (log.isDebugEnabled()) {
                log.debug("Executed a batch update. Query is : " + sqlStmt + ": and result is"
                        + Arrays.toString(count));
            }
            dbConnection.commit();
        } catch (SQLException e) {
            String errorMessage = "Using sql : " + sqlStmt + " " + e.getMessage();
            if (log.isDebugEnabled()) {
                log.debug(errorMessage, e);
            }
            throw new UserStoreException(errorMessage, e);
        } finally {
            if (localConnection) {
                DatabaseUtil.closeAllConnections(dbConnection);
            }
            DatabaseUtil.closeAllConnections(null, prepStmt);
        }
    }

    public static void updateDatabase(Connection dbConnection, String sqlStmt, Object... params)
            throws UserStoreException {
        PreparedStatement prepStmt = null;
        try {
            prepStmt = dbConnection.prepareStatement(sqlStmt);
            if (params != null && params.length > 0) {
                for (int i = 0; i < params.length; i++) {
                    Object param = params[i];
                    if (param == null) {
                        //allow to send null data since null allowed values can be in the table. eg: domain name
                        prepStmt.setString(i + 1, null);
                        //throw new UserStoreException("Null data provided.");
                    } else if (param instanceof String) {
                        prepStmt.setString(i + 1, (String) param);
                    } else if (param instanceof Integer) {
                        prepStmt.setInt(i + 1, (Integer) param);
                    } else if (param instanceof Short) {
                        prepStmt.setShort(i + 1, (Short) param);
                    } else if (param instanceof Date) {
                        Date date = (Date) param;
                        Timestamp time = new Timestamp(date.getTime());
                        prepStmt.setTimestamp(i + 1, time);
                    }
                }
            }
            prepStmt.executeUpdate();
        } catch (SQLException e) {
            String errorMessage = "Using sql : " + sqlStmt + " " + e.getMessage();
            if (log.isDebugEnabled()) {
                log.debug(errorMessage, e);
            }
            if (e instanceof SQLIntegrityConstraintViolationException) {
                // Duplicate entry
                throw new UserStoreException(e.getMessage(), ERROR_CODE_DUPLICATE_WHILE_WRITING_TO_DATABASE.getCode(), e);
            } else {
                // Other SQL Exception
                throw new UserStoreException(e.getMessage(), e);
            }
        } finally {
            DatabaseUtil.closeAllConnections(null, prepStmt);
        }
    }

    public static Connection getDBConnection(DataSource dataSource) throws SQLException {
        Connection dbConnection = dataSource.getConnection();
        dbConnection.setAutoCommit(false);
        if (dbConnection.getTransactionIsolation() != Connection.TRANSACTION_READ_COMMITTED) {
            dbConnection.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
        }
        return dbConnection;
    }

    public static void closeConnection(Connection dbConnection) {

        if (dbConnection != null) {
            try {
                dbConnection.close();
            } catch (SQLException e) {
                log.error("Database error. Could not close statement. Continuing with others. - " + e.getMessage(), e);
            }
        }
    }

    private static void closeResultSet(ResultSet rs) {

        if (rs != null) {
            try {
                rs.close();
            } catch (SQLException e) {
                log.error("Database error. Could not close result set  - " + e.getMessage(), e);
            }
        }

    }

    private static void closeStatement(PreparedStatement preparedStatement) {

        if (preparedStatement != null) {
            try {
                preparedStatement.close();
            } catch (SQLException e) {
                log.error("Database error. Could not close statement. Continuing with others. - " + e.getMessage(), e);
            }
        }

    }

    private static void closeStatements(PreparedStatement... prepStmts) {

        if (prepStmts != null && prepStmts.length > 0) {
            for (PreparedStatement stmt : prepStmts) {
                closeStatement(stmt);
            }
        }

    }

    public static void closeAllConnections(Connection dbConnection, PreparedStatement... prepStmts) {

        closeStatements(prepStmts);
        closeConnection(dbConnection);
    }

    public static void closeAllConnections(Connection dbConnection, ResultSet rs, PreparedStatement... prepStmts) {

        closeResultSet(rs);
        closeStatements(prepStmts);
        closeConnection(dbConnection);
    }

    public static void closeAllConnections(Connection dbConnection, ResultSet rs1, ResultSet rs2,
                                           PreparedStatement... prepStmts) {
        closeResultSet(rs1);
        closeResultSet(rs2);
        closeStatements(prepStmts);
        closeConnection(dbConnection);
    }

    public static void rollBack(Connection dbConnection) {
        try {
            if (dbConnection != null) {
                dbConnection.rollback();
            }
        } catch (SQLException e1) {
            log.error("An error occurred while rolling back transactions. ", e1);
        }
    }
}
