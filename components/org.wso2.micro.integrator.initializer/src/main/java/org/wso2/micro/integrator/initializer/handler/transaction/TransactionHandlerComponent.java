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
public class TransactionHandlerComponent {

    private static final Log LOG = LogFactory.getLog(TransactionHandlerComponent.class);
    private static final int DEFAULT_SCHEDULED_PERIOD_INTERVAL = 60;
    private static final String SCHEDULED_PERIOD = "transactionCountScheduledPeriod";

    private static final String TRANSACTION_DATA_SOURCE = "transactionDataSource";
    private static final String TRANSACTION_CONFIG_SECTION = "transaction";
    private static final String TRANSACTION_CONFIG_NAME = "data_source";

    private ScheduledExecutorService trnCountWriterTaskScheduler;

    public void start(DataSourceService dataSourceService) throws TransactionException, DataSourceException {
        String nodeId = generateRandomId();

        // initialize cipher for encryption needs.
        Cipher cipher = CryptoUtil.initializeCipher();

        DataSource dataSource = getTransactionDataSource(dataSourceService);

        // initialize Transaction store.
        TransactionStore transactionStore = new TransactionStore(dataSource, nodeId, cipher);

        // start and schedule transaction writer task.
        scheduleTransactionWriterTask(transactionStore);
    }

    /**
     * Get the time rate for running the transaction writer scheduled task.
     *
     * @return - scheduled period.
     */
    private int getTransactionCountScheduledPeriod() {
        String scheduledPeriodStr = System.getProperty(SCHEDULED_PERIOD);
        if (StringUtils.isEmpty(scheduledPeriodStr)) {
            scheduledPeriodStr = System.getenv(SCHEDULED_PERIOD);
            if (StringUtils.isEmpty(scheduledPeriodStr)) {
                Object scheduledPeriodObject = ConfigParser.getParsedConfigs().get("" + "." + "");
                if (null != scheduledPeriodObject) {
                    scheduledPeriodStr = scheduledPeriodObject.toString();
                }
            }
        }
        try {
            return Integer.parseInt(scheduledPeriodStr);
        } catch (NumberFormatException e) {
            return DEFAULT_SCHEDULED_PERIOD_INTERVAL;
        }
    }

    /**
     * Schedule the transaction writer task to write the current transaction count to the database at a fixed rate.
     *
     * @param transactionStore - transactionStore instance.
     */
    private void scheduleTransactionWriterTask(TransactionStore transactionStore) {
        trnCountWriterTaskScheduler = Executors.newSingleThreadScheduledExecutor();
        int scheduledPeriod = getTransactionCountScheduledPeriod();

        trnCountWriterTaskScheduler.scheduleAtFixedRate(() -> {
            try {
                transactionStore.addTransaction();
            } catch (TransactionException e) {
                LOG.warn("Could not persist the transaction count: ", e);
            }
        }, scheduledPeriod, scheduledPeriod, TimeUnit.SECONDS);
    }

    /**
     * Stop the transactionWriterTask immediately.
     */
    private void stopTransactionWriterTask() {
        if (trnCountWriterTaskScheduler != null) {
            trnCountWriterTaskScheduler.shutdownNow();
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
     * @throws DataSourceException  - when DataSource is not an RDBMS data source.
     * @throws TransactionException - when no DataSource is available for transaction counting utility.
     */
    private DataSource getTransactionDataSource(DataSourceService dataSourceService)
            throws DataSourceException, TransactionException {

        String dataSourceId = getTransactionDatasourceId();
        CarbonDataSource dataSource = dataSourceService.getDataSource(dataSourceId);
        if (dataSource == null) {
            throw new TransactionException("DataSource " + dataSourceId + " is not available for transaction.");
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
     * @throws TransactionException - when a dataSource is not configured for transaction counting utility.
     */
    private String getTransactionDatasourceId() throws TransactionException {
        String dataSourceId = System.getProperty(TRANSACTION_DATA_SOURCE);
        if (StringUtils.isEmpty(dataSourceId)) {
            dataSourceId = System.getenv(TRANSACTION_DATA_SOURCE);
            if (StringUtils.isEmpty(dataSourceId)) {
                Object dataSourceIdObject = ConfigParser.getParsedConfigs().get(
                        TRANSACTION_CONFIG_SECTION + "." + TRANSACTION_CONFIG_NAME);
                if (dataSourceIdObject != null) {
                    dataSourceId = dataSourceIdObject.toString();
                } else {
                    LOG.warn("DataSource is not configured.");
                    throw new TransactionException(
                            "DataSource " + dataSourceId + " is not configured for transaction.");
                }
            }
        }
        return dataSourceId;
    }

    /**
     * Check whether the transaction property is enabled or not.
     * Default value is true.
     *
     * @return - true if transaction property is enabled, otherwise false.
     */
    public static boolean isTransactionPropertyEnabled() {
        String scheduledPeriodStr = System.getProperty(BaseConstants.TRANSACTION);
        if (StringUtils.isEmpty(scheduledPeriodStr)) {
            scheduledPeriodStr = System.getenv(BaseConstants.TRANSACTION);
        }
        return JavaUtils.isTrue(scheduledPeriodStr, true);
    }

    public void cleanup() {
        stopTransactionWriterTask();
    }
}
