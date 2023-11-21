/*
 * Copyright (c) 2023, WSO2 LLC. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
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
package org.wso2.carbon.inbound.endpoint.protocol.cdc;

import io.debezium.engine.ChangeEvent;
import io.debezium.engine.DebeziumEngine;
import io.debezium.engine.format.Json;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.core.SynapseEnvironment;

import java.io.IOException;
import java.util.Date;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * This class implement the processing logic related to inbound CDC protocol.
 * Common functionalities are include in synapse
 * util that is found in synapse commons
 */
public class CDCPollingConsumer {

    private static final Log logger = LogFactory.getLog(CDCPollingConsumer.class);
    private Properties cdcProperties;
    private String inboundEndpointName;
    private SynapseEnvironment synapseEnvironment;
    private long scanInterval;
    private Long lastRanTime;
    private CDCInjectHandler injectHandler;
    private ExecutorService executorService = null;
    private DebeziumEngine<ChangeEvent<String, String>> engine = null;

    public CDCPollingConsumer(Properties cdcProperties, String inboundEndpointName, SynapseEnvironment synapseEnvironment,
                              long scanInterval) {
        this.cdcProperties = cdcProperties;
        this.inboundEndpointName = inboundEndpointName;
        this.synapseEnvironment = synapseEnvironment;
        this.scanInterval = scanInterval;
        this.lastRanTime = null;
    }

    /**
     * Register a handler to process the file stream after reading from the
     * source
     *
     * @param injectHandler
     */
    public void registerHandler(CDCInjectHandler injectHandler) {
        this.injectHandler = injectHandler;
    }

    /**
     * This will be called by the task scheduler. If a cycle execution takes
     * more than the schedule interval, tasks will call this method ignoring the
     * interval. Timestamp based check is done to avoid that.
     */
    public void execute() {
        if (logger.isDebugEnabled()) {
            logger.debug("Start : CDC Inbound EP : " + inboundEndpointName);
        }
        // Check if the cycles are running in correct interval and start
        // scan
        long currentTime = (new Date()).getTime();
        if (lastRanTime == null || ((lastRanTime + (scanInterval)) <= currentTime)) {
            lastRanTime = currentTime;
            poll();
        } else if (logger.isDebugEnabled()) {
            logger.debug(
                    "Skip cycle since concurrent rate is higher than the scan interval : CDC Inbound EP : " + inboundEndpointName);
        }
        if (logger.isDebugEnabled()) {
            logger.debug("End : CDC Inbound EP : " + inboundEndpointName);
        }
    }

    /**
     * Do the CDC processing operation for the given set of properties. Then inject
     * according to the registered handler
     */
    public ChangeEvent<String, String> poll() {
        logger.debug("Start : listening to DB events : ");
        listenDataChanges();
        logger.debug("End : Listening to DB events : ");
        return null;
    }

    private void listenDataChanges () {
        executorService = Executors.newSingleThreadExecutor();

        if (engine == null || executorService.isShutdown()) {
            engine = DebeziumEngine.create(Json.class)
                    .using(this.cdcProperties)
                    .notifying(record -> {
                        injectHandler.invoke(record, this.inboundEndpointName);
                    }).build();

            executorService.execute(engine);
        }
    }

    protected Properties getInboundProperties() {
        return cdcProperties;
    }

    protected void destroy () {
        if (!executorService.isShutdown()) {
            executorService.shutdown();
        }
        try {
            if (engine != null) {
                engine.close();
            }
        } catch (IOException e) {
            throw new RuntimeException("Error while closing the Debezium Engine", e);
        }
    }

}
