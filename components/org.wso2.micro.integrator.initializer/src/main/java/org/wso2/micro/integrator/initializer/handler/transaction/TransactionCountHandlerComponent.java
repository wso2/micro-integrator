/*
Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.micro.integrator.initializer.handler.transaction;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.config.mapper.ConfigParser;
import org.wso2.micro.core.util.StringUtils;
import org.wso2.micro.integrator.initializer.handler.DataHolder;
import org.wso2.micro.integrator.initializer.handler.transaction.exception.TransactionCounterInitializationException;
import org.wso2.micro.integrator.initializer.handler.transaction.security.CryptoUtil;
import org.wso2.micro.integrator.initializer.handler.transaction.store.TransactionStore;
import org.wso2.micro.integrator.ndatasource.common.DataSourceException;
import org.wso2.micro.integrator.ndatasource.core.CarbonDataSource;
import org.wso2.micro.integrator.ndatasource.core.DataSourceService;

import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import javax.crypto.Cipher;
import javax.sql.DataSource;

/**
 * This class represents the internal transaction count handler component.
 */
public class TransactionCountHandlerComponent {

    private static final Log LOG = LogFactory.getLog(TransactionCountHandlerComponent.class);
    private static final int DEFAULT_UPDATE_INTERVAL = 1;

    private static final String TRANSACTION_COUNTER_SYS_PROPERTY_NAME = "enableTransactionCounter";
    private static final String TRANSACTION_CONFIG_SECTION = "transaction_counter";
    private static final String TRANSACTION_CONFIG_ENABLE = "enable";
    private static final String TRANSACTION_CONFIG_DATA_SOURCE = "data_source";
    private static final String TRANSACTION_CONFIG_UPDATE_INTERVAL = "update_interval";

    private ScheduledExecutorService txCountWriterTaskScheduler;

    public void start(DataSourceService dataSourceService)
            throws DataSourceException, TransactionCounterInitializationException {
        DataSource dataSource = getTransactionDataSource(dataSourceService);

        // initialize cipher for encryption needs.
        Cipher cipher = CryptoUtil.initializeCipher();

        String nodeId = generateRandomId();

        // initialize Transaction store and adding to the data-holder.
        TransactionStore transactionStore = new TransactionStore(dataSource, nodeId, cipher);
        DataHolder.getInstance().setTransactionStore(transactionStore);

        // start and schedule transaction writer task.
        scheduleTransactionWriterTask(transactionStore);
    }

    /**
     * Get the transaction count update interval for running the transaction writer scheduled task.
     *
     * @return - transaction count update interval.
     */
    private int getTransactionCountUpdateInterval() {
        Object updateIntervalObject = ConfigParser.getParsedConfigs().get(
                TRANSACTION_CONFIG_SECTION + "." + TRANSACTION_CONFIG_UPDATE_INTERVAL);
        if (null != updateIntervalObject) {
            String updateIntervalStr = updateIntervalObject.toString();
            try {
                return Integer.parseInt(updateIntervalStr);
            } catch (NumberFormatException e) {
                LOG.warn("Cannot parse the provided transaction count update period " + updateIntervalStr
                                 + " as an Integer. Hence, proceeding with default value ("
                                 + DEFAULT_UPDATE_INTERVAL + "min)");
                return DEFAULT_UPDATE_INTERVAL;
            }
        } else {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Transaction count update period is set to default value (" + DEFAULT_UPDATE_INTERVAL
                                  + "min).");
            }
            return DEFAULT_UPDATE_INTERVAL;
        }
    }

    /**
     * Schedule the transaction writer task to write the current transaction count to the database at a fixed rate.
     *
     * @param transactionStore - transactionStore instance.
     */
    private void scheduleTransactionWriterTask(TransactionStore transactionStore) {
        txCountWriterTaskScheduler = Executors.newSingleThreadScheduledExecutor();
        int updateInterval = getTransactionCountUpdateInterval();
        DataHolder.getInstance().setTransactionUpdateInterval(updateInterval);

        txCountWriterTaskScheduler.scheduleAtFixedRate(() -> {
            try {
                transactionStore.addTransaction();
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Added the current transaction count: " + TransactionCountHandler.getTransactionCount()
                                      + " to the database.");
                }
            } catch (Throwable e) {
                LOG.error("Could not persist the transaction count: ", e);
            }
        }, 0, updateInterval, TimeUnit.MINUTES);
    }

    /**
     * Stop the transactionWriterTask immediately.
     */
    private void stopTransactionWriterTask() {
        if (txCountWriterTaskScheduler != null) {
            txCountWriterTaskScheduler.shutdownNow();
        }
    }

    private String generateRandomId() {
        return UUID.randomUUID().toString();
    }

    /**
     * Check whether datasource is configured for transaction counting utility.
     *
     * @param dataSourceService dataSourceService instance.
     * @return - true if datasource defined.
     * @throws DataSourceException                       - when DataSource is not an RDBMS data source.
     * @throws TransactionCounterInitializationException - when no DataSource is available for transaction counting
     *                                                   utility.
     */
    private DataSource getTransactionDataSource(DataSourceService dataSourceService)
            throws DataSourceException, TransactionCounterInitializationException {

        String dataSourceId = getTransactionDatasourceId();
        CarbonDataSource dataSource = dataSourceService.getDataSource(dataSourceId);
        if (Objects.isNull(dataSource)) {
            throw new DataSourceException("DataSource " + dataSourceId + " is not configured properly.");
        }
        Object transactionDataSourceObject = dataSource.getDSObject();
        if (!(transactionDataSourceObject instanceof DataSource)) {
            throw new DataSourceException("DataSource is not an RDBMS data source.");
        }
        return (DataSource) transactionDataSourceObject;
    }

    /**
     * Get the dataSource configured for transaction counting utility.
     *
     * @return - datasource id.
     * @throws TransactionCounterInitializationException - when a dataSource is not configured for transaction counting
     *                                                   utility.
     */
    private String getTransactionDatasourceId() throws TransactionCounterInitializationException {
        Object dataSourceIdObject = ConfigParser.getParsedConfigs().get(
                TRANSACTION_CONFIG_SECTION + "." + TRANSACTION_CONFIG_DATA_SOURCE);
        if (dataSourceIdObject != null) {
            return dataSourceIdObject.toString();
        } else {
            LOG.error("DataSource is not configured for transaction component.");
            throw new TransactionCounterInitializationException(
                    "DataSource is not configured for transaction component.");
        }
    }

    /**
     * Check whether the transaction property is enabled or not. Default value is false.
     *
     * @return - true if transaction property is enabled, otherwise false.
     */
    public static boolean isTransactionPropertyEnabled() {
        String transactionCounterPropertyStr = System.getProperty(TRANSACTION_COUNTER_SYS_PROPERTY_NAME);
        if (StringUtils.isEmpty(transactionCounterPropertyStr)) {
            transactionCounterPropertyStr = System.getenv(TRANSACTION_COUNTER_SYS_PROPERTY_NAME);
            if (StringUtils.isEmpty(transactionCounterPropertyStr)) {
                Object updatePeriodObject = ConfigParser.getParsedConfigs().get(
                        TRANSACTION_CONFIG_SECTION + "." + TRANSACTION_CONFIG_ENABLE);
                if (Objects.nonNull(updatePeriodObject)) {
                    transactionCounterPropertyStr = updatePeriodObject.toString();
                }
            }
        }
        return "true".equalsIgnoreCase(transactionCounterPropertyStr);
    }

    public void cleanup() {
        stopTransactionWriterTask();
    }
}
