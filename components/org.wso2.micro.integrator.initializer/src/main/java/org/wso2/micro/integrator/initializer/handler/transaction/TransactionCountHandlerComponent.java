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

import org.apache.axis2.transport.base.BaseConstants;
import org.apache.axis2.util.JavaUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.config.mapper.ConfigParser;
import org.wso2.micro.core.util.StringUtils;
import org.wso2.micro.integrator.initializer.handler.transaction.security.CryptoUtil;
import org.wso2.micro.integrator.initializer.handler.transaction.store.TransactionStore;
import org.wso2.micro.integrator.ndatasource.common.DataSourceException;
import org.wso2.micro.integrator.ndatasource.core.CarbonDataSource;
import org.wso2.micro.integrator.ndatasource.core.DataSourceService;

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
    private static final int DEFAULT_SCHEDULED_PERIOD_INTERVAL = 60;

    private static final String TRANSACTION_CONFIG_SECTION = "transaction_tracker";
    private static final String TRANSACTION_CONFIG_DATA_SOURCE = "datasource";
    private static final String TRANSACTION_CONFIG_SCHEDULED_PERIOD = "update_interval";

    private ScheduledExecutorService txCountWriterTaskScheduler;

    public void start(DataSourceService dataSourceService) throws TransactionCounterException, DataSourceException {
        DataSource dataSource = getTransactionDataSource(dataSourceService);

        // initialize cipher for encryption needs.
        Cipher cipher = CryptoUtil.initializeCipher();

        String nodeId = generateRandomId();

        // initialize Transaction store.
        TransactionStore transactionStore = new TransactionStore(dataSource, nodeId, cipher);

        // start and schedule transaction writer task.
        scheduleTransactionWriterTask(transactionStore);
    }

    /**
     * Get the transaction count update period for running the transaction writer scheduled task.
     *
     * @return - transaction count update period.
     */
    private int getTransactionCountScheduledPeriod() {
        Object scheduledPeriodObject = ConfigParser.getParsedConfigs().get(
                TRANSACTION_CONFIG_SECTION + "." + TRANSACTION_CONFIG_SCHEDULED_PERIOD);
        if (null != scheduledPeriodObject) {
            String scheduledPeriodStr = scheduledPeriodObject.toString();
            try {
                return Integer.parseInt(scheduledPeriodStr);
            } catch (NumberFormatException e) {
                LOG.warn("Cannot parse the provided transaction count update period " + scheduledPeriodStr
                                 + " as an Integer. Hence, proceeding with default value ("
                                 + DEFAULT_SCHEDULED_PERIOD_INTERVAL + "s)");
                return DEFAULT_SCHEDULED_PERIOD_INTERVAL;
            }
        } else {
            LOG.debug("Transaction count update period is set to default value (" + DEFAULT_SCHEDULED_PERIOD_INTERVAL
                              + "s).");
            return DEFAULT_SCHEDULED_PERIOD_INTERVAL;
        }
    }

    /**
     * Schedule the transaction writer task to write the current transaction count to the database at a fixed rate.
     *
     * @param transactionStore - transactionStore instance.
     */
    private void scheduleTransactionWriterTask(TransactionStore transactionStore) {
        txCountWriterTaskScheduler = Executors.newSingleThreadScheduledExecutor();
        int scheduledPeriod = getTransactionCountScheduledPeriod();

        txCountWriterTaskScheduler.scheduleAtFixedRate(() -> {
            try {
                transactionStore.addTransaction();
            } catch (TransactionCounterException e) {
                LOG.debug("Could not persist the transaction count: ", e);
            }
        }, scheduledPeriod, scheduledPeriod, TimeUnit.SECONDS);
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
     * @throws DataSourceException         - when DataSource is not an RDBMS data source.
     * @throws TransactionCounterException - when no DataSource is available for transaction counting utility.
     */
    private DataSource getTransactionDataSource(DataSourceService dataSourceService)
            throws DataSourceException, TransactionCounterException {

        String dataSourceId = getTransactionDatasourceId();
        CarbonDataSource dataSource = dataSourceService.getDataSource(dataSourceId);
        if (dataSource == null) {
            throw new DataSourceException(
                    "DataSource " + dataSourceId + " is not configured properly.");
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
     * @throws TransactionCounterException - when a dataSource is not configured for transaction counting utility.
     */
    private String getTransactionDatasourceId() throws TransactionCounterException {
        Object dataSourceIdObject = ConfigParser.getParsedConfigs().get(
                TRANSACTION_CONFIG_SECTION + "." + TRANSACTION_CONFIG_DATA_SOURCE);
        if (dataSourceIdObject != null) {
            return dataSourceIdObject.toString();
        } else {
            LOG.error("DataSource is not configured for transaction component.");
            throw new TransactionCounterException("DataSource is not configured for transaction component.");
        }
    }

    /**
     * Check whether the transaction property is enabled or not. Default value is true.
     *
     * @return - true if transaction property is enabled, otherwise false.
     */
    public static boolean isTransactionPropertyEnabled() {
        String scheduledPeriodStr = System.getProperty(BaseConstants.INTERNAL_TRANSACTION_COUNTED);
        if (StringUtils.isEmpty(scheduledPeriodStr)) {
            scheduledPeriodStr = System.getenv(BaseConstants.INTERNAL_TRANSACTION_COUNTED);
        }
        return JavaUtils.isTrue(scheduledPeriodStr, true);
    }

    public void cleanup() {
        stopTransactionWriterTask();
    }
}
