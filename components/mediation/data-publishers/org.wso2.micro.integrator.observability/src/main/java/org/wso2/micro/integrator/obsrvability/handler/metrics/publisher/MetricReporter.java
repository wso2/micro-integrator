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
package org.wso2.micro.integrator.obsrvability.handler.metrics.publisher;

import java.util.Map;

/**
 * The Metric Reporter is allows one to access the relevant Metric Reporter implementation.
 */
public interface MetricReporter {
    /**
     * Creates a metric of the provided type with the given metric name,
     * the metric description and the set of properties that can be used
     * to identify a metric uniquely and for metric aggregation, metric filtering.
     *
     * @param serviceType The service type (Proxy Service, API, Inbound endpoint )
     *                    for which the metric is going to be instrumented.
     * @param type        The type of metric (e.g.Counter) that is used in intrumentation.
     * @param metricHelp  Metric Description
     * @param properties  Metric labels used to uniquely identify a metric.
     */
    public void createMetric(String serviceType, String type, String metricName, String metricHelp,
                           Map<String, String[]> properties);

    /**
     * This is called in the handleInit() method of the AbstractExtendedSynapseHandler.
     * There by the metrics created in the createMetric() method are invoked.
     */
    public void initMetrics();

    /**
     * Creates a metric of the provided type when a request invocation fails
     * with the given metric name, the metric description and the set of
     * properties that can be used to identify a metric uniquely and for
     * metric aggregation, metric filtering.
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
     * @param properties Metric labels where the Metric and the labels are defined as key-value pairs of the properties
     *                   Map
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
     * Return a timer object used to observe the round trip time from the moment a
     * request enters the Synapse Engine until the response goes out of the Synapse
     * Engine.
     * A Timer Object which contains the metric name, it's respective set of labels
     * and time the request reached the Synapse Engine is returned.
     *
     * @param metricName Metric Name
     * @param properties Metric and the labels as key-value pairs
     */
    public Object getTimer(String metricName, Map<String, String[]> properties);

    /**
     * Stops the timer once the response leaves the Synapse Engine, so that the
     * latency of a request entering the Synapse Engine is calculated by the difference
     * between the time the timer was started in the getTimer() method and the
     * observeTime() method.
     *
     * @param timer Timer Object returned from the getTimer() method
     */
    public void observeTime(Object timer);

    /**
     * Instrument metrics related to server deployment where a metric can be uniquely
     * identify by the IP Address of the host and port the MI server is running and the
     * java home and java version used for running the MI server.
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
     * Instrument metrics on service deployment where a metric can be uniquely
     * identify by service name and the service type.
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
