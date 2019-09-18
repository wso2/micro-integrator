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

package org.wso2.micro.integrator.analytics.messageflow.data.publisher.observer.jmx.data;

/**
 * MBean interface to expose collected statistic data using JMX.
 */
public interface StatisticCollectionViewMXBean {

    /**
     * Reset JMX API statistic collection.
     */
    void resetAPIStatistics();

    /**
     * Reset JMX Proxy statistic collection.
     */
    void resetProxyStatistics();

    /**
     * Reset JMX Sequence statistic collection.
     */
    void resetSequenceStatistics();

    /**
     * Reset JMX Inbound Endpoint statistic collection.
     */
    void resetInboundEndpointStatistics();

    /**
     * Reset JMX Endpoint statistic collection.
     */
    void resetEndpointStatistics();

    /**
     * Reset all JMX statistics.
     */
    void resetAllStatistics();

    /**
     * Returns statistics related to a Proxy Service.
     *
     * @param proxyName Name of the proxy service.
     * @return Composite Data Object that contains Proxy Statistics.
     */
    StatisticsCompositeObject getProxyServiceJmxStatistics(String proxyName);

    /**
     * Returns statistics related to a Sequence.
     *
     * @param sequenceName Name of the Sequence.
     * @return Composite Data Object that contains Sequence Statistics.
     */
    StatisticsCompositeObject getSequenceJmxStatistics(String sequenceName);

    /**
     * Returns statistics related to a API.
     *
     * @param apiName Name of the API.
     * @return Composite Data Object that contains API Statistics.
     */
    StatisticsCompositeObject getApiJmxStatistics(String apiName);

    /**
     * Returns statistics related to a Inbound Endpoint.
     *
     * @param inboundEndpointName Name of the Inbound Endpoint.
     * @return Composite Data Object that contains Inbound Endpoint Statistics.
     */
    StatisticsCompositeObject getInboundEndpointJmxStatistics(String inboundEndpointName);

    /**
     * Returns statistics related to a Endpoint.
     *
     * @param endpointName Name of the Endpoint.
     * @return Composite Data Object that contains Endpoint Statistics.
     */
    StatisticsCompositeObject getEndpointJmxStatistics(String endpointName);
}
