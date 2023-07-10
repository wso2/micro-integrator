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
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.core.SynapseEnvironment;

import java.util.Date;
import java.util.Properties;
import java.util.concurrent.BlockingQueue;

import static org.wso2.carbon.inbound.endpoint.protocol.cdc.CDCProcessor.inboundEpEventQueueMap;

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
        try {
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
                        "Skip cycle since cuncurrent rate is higher than the scan interval : CDC Inbound EP : " + inboundEndpointName);
            }
            if (logger.isDebugEnabled()) {
                logger.debug("End : CDC Inbound EP : " + inboundEndpointName);
            }
        } catch (Exception e) {
            logger.error("Error while getting events. " + e.getMessage(), e);
        }
    }

    /**
     * Do the CDC processing operation for the given set of properties. Then inject
     * according to the registered handler
     */
    public ChangeEvent<String, String> poll() {

        if (logger.isDebugEnabled()) {
            logger.debug("Start : listening to DB events : ");
        }

        BlockingQueue<ChangeEvent<String, String>> eventQueue = inboundEpEventQueueMap.get(inboundEndpointName);
        while (!eventQueue.isEmpty()) {
            injectHandler.invoke(eventQueue.poll(), inboundEndpointName);
        }

        if (logger.isDebugEnabled()) {
            logger.debug("End : Listening to DB events : ");
        }
        return null;
    }

    protected Properties getInboundProperties() {
        return cdcProperties;
    }

}
