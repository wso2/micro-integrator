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
package org.wso2.micro.integrator.prometheus.handler;

import java.util.Map;

/**
 * The Metric Reporter is allows one to access the relevant Metric Reporter implementation.
 */
public interface MetricReporter {

    /**
     * Create the metrics used in instrumentation.
     *
     * @param serviceType Service Type
     * @param type        Metric type
     * @param metricHelp  Metric Description
     * @param properties  Metric labels
     */
    public void initMetric(String serviceType, String type, String metricName, String metricHelp,
                           Map<String, String[]> properties);

    /**
     * Create the metrics used in instrumentation of error requests.
     *
     * @param serviceType Service Type
     * @param type        Metric type
     * @param metricHelp  Metric Description
     * @param properties  Metric labels
     */
    public void initErrorMetrics(String serviceType, String type, String metricName, String metricHelp,
                                 Map<String, String[]> properties);

    /**
     * Increment the metric value when a request is received/server/service is deployed.
     *
     * @param metricName Metric Name
     * @param properties Metric and the labels as key-value pairs of the properties Map
     */
    public void incrementCount(String metricName, Map<String, String[]> properties);

    /**
     * Decrement the metric value when a request is received/server/service is un-deployed.
     *
     * @param metricName Metric Name
     * @param properties Metric and the labels as key-value pairs of the properties Map
     */
    public void decrementCount(String metricName, Map<String, String> properties);

    /**
     * Return a timer object to observe the request latency.
     *
     * @param metricName Metric Name
     * @param properties Metric and the labels as key-value pairs
     */
    public Object getTimer(String metricName, Map<String, String[]> properties);

    /**
     * Observes the latency of a request.
     *
     * @param timer Timer Object observing the latency of a request.
     */
    public void observeTime(Object timer);

    /**
     * Instrument metrics on server deployment.
     *
     * @param host        The IP Address of the host the MI server is running
     * @param port        The port the MI server is running
     * @param javaHome    Java Home
     * @param javaVersion Java Version
     */
    public void serverUp(String host, String port, String javaHome, String javaVersion);

    /**
     * Instrument metrics on server undeployment.
     *
     * @param host        The IP Address of the host the MI server is running
     * @param port        The port the MI server is running
     * @param javaHome    Java Home
     * @param javaVersion Java Version
     */
    public void serverDown(String host, String port, String javaVersion, String javaHome);

    /**
     * Instrument metrics on service deployment.
     *
     * @param serviceName Service Name
     * @param serviceType Service Type
     */
    public void serviceUp(String serviceName, String serviceType);

    /**
     * Instrument metrics on service undeployment.
     *
     * @param serviceName Service Name
     * @param serviceType Service Type
     */
    public void serviceDown(String serviceName, String serviceType);
}
