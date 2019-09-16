/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.micro.integrator.analytics.messageflow.data.publisher.publish;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.databridge.agent.AgentHolder;
import org.wso2.carbon.databridge.agent.DataPublisher;
import org.wso2.carbon.databridge.agent.exception.DataEndpointAgentConfigurationException;
import org.wso2.carbon.databridge.agent.exception.DataEndpointAuthenticationException;
import org.wso2.carbon.databridge.agent.exception.DataEndpointConfigurationException;
import org.wso2.carbon.databridge.agent.exception.DataEndpointException;
import org.wso2.carbon.databridge.commons.exception.TransportException;
import org.wso2.micro.integrator.analytics.data.publisher.util.AnalyticsDataPublisherConstants;
import org.wso2.micro.integrator.core.services.CarbonServerConfigurationService;
import org.wso2.micro.integrator.core.util.MicroIntegratorBaseUtils;

public class DataBridgePublisher {

    private static DataPublisher publisher;
    private static Log log = LogFactory.getLog(DataBridgePublisher.class);
    private static String receiverUrl;
    private static String authUrl;
    private static String username;
    private static String password;

    private synchronized static void initDataPublisher() {
        if (publisher == null) {
            try {
                loadConfigs();

                publisher = new DataPublisher(null, receiverUrl, authUrl, username, password);
                if (log.isDebugEnabled()) {
                    log.debug(
                            "Connected to analytics sever with the following details, " + " ReceiverURL:" + receiverUrl
                                    + ", AuthURL:" + authUrl + ", Username:" + username);
                }
            } catch (DataEndpointAgentConfigurationException | DataEndpointConfigurationException | DataEndpointException | DataEndpointAuthenticationException | TransportException e) {
                log.error("Error while creating databridge publisher", e);
            }
        }
    }

    public static DataPublisher getDataPublisher() {
        if (publisher == null) {
            initDataPublisher();
        }
        return publisher;
    }

    private static void loadConfigs() {

        String agentConfPath = MicroIntegratorBaseUtils.getCarbonConfigDirPath()
                + AnalyticsDataPublisherConstants.DATA_AGENT_CONFIG_PATH;

        AgentHolder.setConfigPath(agentConfPath);

        receiverUrl = CarbonServerConfigurationService.getInstance()
                .getFirstProperty(AnalyticsDataPublisherConstants.ANALYTICS_RECEIVER_URL);
        authUrl = CarbonServerConfigurationService.getInstance()
                .getFirstProperty(AnalyticsDataPublisherConstants.ANALYTICS_AUTH_URL);
        username = CarbonServerConfigurationService.getInstance()
                .getFirstProperty(AnalyticsDataPublisherConstants.ANALYTICS_USERNAME);
        password = CarbonServerConfigurationService.getInstance()
                .getFirstProperty(AnalyticsDataPublisherConstants.ANALYTICS_PASSWORD);

    }
}
