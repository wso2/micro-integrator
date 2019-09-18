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
package org.wso2.micro.integrator.analytics.data.publisher.util;

public class AnalyticsDataPublisherConstants {

    // Carbon XML properties
    public static final String STAT_CONFIG_ELEMENT = "MediationFlowStatisticConfig";
    public static final String FLOW_STATISTIC_WORKER_IDLE_INTERVAL = STAT_CONFIG_ELEMENT + ".StatWorkerIdleInterval";
    public static final long FLOW_STATISTIC_WORKER_IDLE_INTERVAL_DEFAULT = 5000;//in milliseconds
    public static final String FLOW_STATISTIC_JMX_PUBLISHING = STAT_CONFIG_ELEMENT + ".JmxPublishingDisable";
    public static final String FLOW_STATISTIC_WORKER_COUNT = STAT_CONFIG_ELEMENT + ".StatWorkerCount";
    public static final int FLOW_STATISTIC_WORKER_COUNT_DEFAULT = 2;
    public static final String FLOW_STATISTIC_ANALYTICS_PUBLISHING = STAT_CONFIG_ELEMENT + ".AnalyticPublishingDisable";
    public static final String STAT_OBSERVERS = STAT_CONFIG_ELEMENT + ".Observers";
    public static final String FLOW_STATISTIC_NODE_HOST_NAME = STAT_CONFIG_ELEMENT + ".NodeHostName";

    // Carbon xml analytics server configs.
    public static final String ANALYTICS_ELEMENT = "Analytics";
    public static final String ANALYTICS_RECEIVER_URL = ANALYTICS_ELEMENT + ".ServerURL";
    public static final String ANALYTICS_AUTH_URL = ANALYTICS_ELEMENT + ".AuthServerURL";
    public static final String ANALYTICS_USERNAME = ANALYTICS_ELEMENT + ".Username";
    public static final String ANALYTICS_PASSWORD = ANALYTICS_ELEMENT + ".Password";

    public static final String DATA_AGENT_CONFIG_PATH = "/data-bridge/data-agent-config.xml";

}
