package org.wso2.micro.integrator.prometheus.handler;

import java.util.Map;

/**
 * The Metric Reporter is allows one to access the relevant Metric Reporter implementation.
 */
public interface MetricReporter {


    public void initMetric(String serviceType, String type, String metricName, String metricHelp,  Map<String, String[]> properties);

    public void initErrorMetrics(String serviceType, String type, String metricName, String metricHelp,  Map<String, String[]> properties);

    /**
     * Formats the metric data into required format.
     *
     * @param type       Metric type
     * @param metricName Metric Name
     * @return Formatted metric string
     */
    public void createMetric(String type, String metricName);

    /**
     * Increment the metric value when a request is received/server/service is deployed.
     *
     * @param  metricName Metric Name
     * @param properties Metric and the labels as key-value pairs of the properties Map.
     */
    public void incrementCount(String metricName, Map<String, String[]> properties);

    /**
     * Decrement the metric value when a request is received/server/service is un-deployed.
     *
     * @param  metricName Metric Name
     * @param properties Metric and the labels as key-value pairs of the properties Map.
     */
    public void decrementCount(String metricName, Map<String, String> properties);

//    /**
//     * Increment the metric value when an error request is received/server/service is deployed.
//     *
//     * @param metricName Metric Name
//     * @param properties Metric and the labels as key-value pairs of the properties Map.
//     */
//    public void incrementErrorCount(String metricName, Map<String, String[]> properties);

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

    public void serverUp(String host, String port, String javaVersion, String javaHome);

    public void serviceUp(String serviceName, String serviceType, String startTime);

}
