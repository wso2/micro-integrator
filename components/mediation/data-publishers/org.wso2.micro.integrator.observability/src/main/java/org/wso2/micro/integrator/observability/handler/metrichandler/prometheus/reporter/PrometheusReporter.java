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
package org.wso2.micro.integrator.observability.handler.metrichandler.prometheus.reporter;

import io.prometheus.client.Counter;
import io.prometheus.client.Gauge;
import io.prometheus.client.Histogram;
import io.prometheus.client.hotspot.DefaultExports;
import org.wso2.config.mapper.ConfigParser;
import org.wso2.micro.integrator.observability.handler.metrichandler.MetricReporter;
import org.wso2.micro.integrator.observability.handler.util.MetricConstants;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Class for instrumenting Prometheus Metrics.
 */
public class PrometheusReporter implements MetricReporter {
    private static Counter TOTAL_REQUESTS_RECEIVED_PROXY_SERVICE;
    private static Counter TOTAL_REQUESTS_RECEIVED_API;
    private static Counter TOTAL_REQUESTS_RECEIVED_INBOUND_ENDPOINT;
    private static Counter ERROR_REQUESTS_RECEIVED_PROXY_SERVICE;
    private static Counter ERROR_REQUESTS_RECEIVED_API;
    private static Counter ERROR_REQUESTS_RECEIVED_INBOUND_ENDPOINT;

    private static Histogram PROXY_LATENCY_HISTOGRAM;
    private static Histogram API_LATENCY_HISTOGRAM;
    private static Histogram INBOUND_ENDPOINT_LATENCY_HISTOGRAM;

    private static Gauge SERVER_UP;
    private static Gauge SERVICE_UP;

    private double[] proxyLatencyBuckets;
    private double[] apiLatencyBuckets;
    private double[] inboundEndpointLatencyBuckets;

    private static Map<String, Object> metricMap = new HashMap();

    @Override
    public void createMetric(String serviceType, String type, String metricName, String metricHelp, Map<String,
            String[]> properties) {

        Map<String, Object> configs = ConfigParser.getParsedConfigs();
        createBuckets(configs);
        DefaultExports.initialize();

        //Read the label names from the map
        String[] labels = properties.get(metricName);

        if (serviceType.equalsIgnoreCase(SERVICE.PROXY.name())) {
            if (type.equals(MetricConstants.COUNTER)) {
                TOTAL_REQUESTS_RECEIVED_PROXY_SERVICE = Counter.build(MetricConstants.PROXY_REQUEST_COUNT_TOTAL,
                        metricHelp).
                        labelNames(labels).register();
                metricMap.put(metricName, TOTAL_REQUESTS_RECEIVED_PROXY_SERVICE);

            } else if (type.equals(MetricConstants.HISTOGRAM)) {
                PROXY_LATENCY_HISTOGRAM = Histogram.build()
                        .name(MetricConstants.PROXY_LATENCY_SECONDS)
                        .help(metricHelp)
                        .labelNames(labels)
                        .buckets(proxyLatencyBuckets)
                        .register();
                metricMap.put(metricName, PROXY_LATENCY_HISTOGRAM);
            }
        } else if (serviceType.equalsIgnoreCase(SERVICE.API.name())) {
            if (type.equals(MetricConstants.COUNTER)) {
                TOTAL_REQUESTS_RECEIVED_API = Counter.build(MetricConstants.API_REQUEST_COUNT_TOTAL, metricHelp).
                        labelNames(labels).register();
                metricMap.put(metricName, TOTAL_REQUESTS_RECEIVED_API);
            } else if (type.equals(MetricConstants.HISTOGRAM)) {
                API_LATENCY_HISTOGRAM = Histogram.build()
                        .name(MetricConstants.API_LATENCY_SECONDS)
                        .help(metricHelp)
                        .labelNames(labels)
                        .buckets(apiLatencyBuckets)
                        .register();
                metricMap.put(metricName, API_LATENCY_HISTOGRAM);
            }
        } else if (serviceType.equalsIgnoreCase(SERVICE.INBOUND_ENDPOINT.name())) {
            if (type.equals(MetricConstants.COUNTER)) {
                TOTAL_REQUESTS_RECEIVED_INBOUND_ENDPOINT = Counter.build
                        (MetricConstants.INBOUND_ENDPOINT_REQUEST_COUNT_TOTAL, metricHelp).
                        labelNames(labels).register();
                metricMap.put(metricName, TOTAL_REQUESTS_RECEIVED_INBOUND_ENDPOINT);

            } else if (type.equals(MetricConstants.HISTOGRAM)) {
                INBOUND_ENDPOINT_LATENCY_HISTOGRAM = Histogram.build()
                        .name(MetricConstants.INBOUND_ENDPOINT_LATENCY_SECONDS)
                        .help(metricHelp)
                        .labelNames(labels)
                        .buckets(inboundEndpointLatencyBuckets)
                        .register();
                metricMap.put(metricName, INBOUND_ENDPOINT_LATENCY_HISTOGRAM);
            }
        } else if (serviceType.equals(MetricConstants.SERVER)) {
            SERVER_UP = Gauge.build(MetricConstants.SERVER_UP, "Server status").
                    labelNames(labels).register();
            metricMap.put(MetricConstants.SERVER_UP, SERVER_UP);

        } else {
            SERVICE_UP = Gauge.build(MetricConstants.SERVICE_UP, "Service status").
                    labelNames(labels).register();
            metricMap.put(MetricConstants.SERVICE_UP, SERVICE_UP);
        }
    }

    @Override
    public void initErrorMetrics(String serviceType, String type, String metricName, String metricHelp, Map<String,
            String[]> properties) {

        String[] labels = properties.get(metricName);

        if (serviceType.equals(SERVICE.PROXY.name())) {
            ERROR_REQUESTS_RECEIVED_PROXY_SERVICE = Counter.build(MetricConstants.PROXY_REQUEST_COUNT_ERROR_TOTAL,
                    metricHelp).
                    labelNames(labels).register();
            metricMap.put(metricName, ERROR_REQUESTS_RECEIVED_PROXY_SERVICE);
        } else if (serviceType.equals(SERVICE.API.name())) {
            ERROR_REQUESTS_RECEIVED_API = Counter.build(MetricConstants.API_REQUEST_COUNT_ERROR_TOTAL, metricHelp).
                    labelNames(labels).register();
            metricMap.put(metricName, ERROR_REQUESTS_RECEIVED_API);
        } else if (serviceType.equals(SERVICE.INBOUND_ENDPOINT.name())) {
            ERROR_REQUESTS_RECEIVED_INBOUND_ENDPOINT = Counter.
                    build(MetricConstants.INBOUND_ENDPOINT_REQUEST_COUNT_ERROR_TOTAL, metricHelp).labelNames(labels).
                    register();
            metricMap.put(metricName, ERROR_REQUESTS_RECEIVED_INBOUND_ENDPOINT);

        }
    }

    @Override
    public void incrementCount(String metricName, Map<String, String[]> properties) {
        Counter counter = (Counter) metricMap.get(metricName);
        String[] value = properties.get(metricName);

        counter.labels(value).inc();
    }

    @Override
    public void decrementCount(String metricName, Map<String, String> properties) {
        // decrementCount() is not necessary to be implemented for the Prometheus Reporter
        // as Gauge is used in Prometheus for the metrics that can both increment and decrement value.
    }

    @Override
    public Object getTimer(String metricName, Map<String, String[]> properties) {
        String[] value = properties.get(metricName);
        Histogram timer = (Histogram) metricMap.get(metricName);

        return timer.labels(value).startTimer();
    }

    @Override
    public void observeTime(Object timer) {
        Histogram.Timer histogramTimer = (Histogram.Timer) timer;
        histogramTimer.observeDuration();
    }

    @Override
    public void serverUp(String host, String port, String javaHome, String javaVersion) {
        Gauge gauge = (Gauge) metricMap.get(MetricConstants.SERVER_UP);
        gauge.labels(host, port, javaHome, javaVersion).setToCurrentTime();
    }

    @Override
    public void serverDown(String host, String port, String javaHome, String javaVersion) {
        Gauge gauge = (Gauge) metricMap.get(MetricConstants.SERVER_UP);
        gauge.labels(host, port, javaHome, javaVersion).set(0);
    }

    @Override
    public void serviceUp(String serviceName, String serviceType) {
        Gauge gauge = (Gauge) metricMap.get(MetricConstants.SERVICE_UP);
        gauge.labels(serviceName, serviceType).setToCurrentTime();
    }

    @Override
    public void serviceDown(String serviceName, String serviceType) {
        Gauge gauge = (Gauge) metricMap.get(MetricConstants.SERVICE_UP);
        gauge.labels(serviceName, serviceType).set(0);
    }

    @Override
    public void initMetrics() {
        PrometheusMetricCreatorUtils.createProxyServiceMetric();
        PrometheusMetricCreatorUtils.createAPIServiceMetric();
        PrometheusMetricCreatorUtils.createInboundEndpointMetric();
        PrometheusMetricCreatorUtils.createProxyServiceErrorMetric();
        PrometheusMetricCreatorUtils.createApiErrorMetric();
        PrometheusMetricCreatorUtils.createInboundEndpointErrorMetric();

        PrometheusMetricCreatorUtils.createServerUpMetrics();
        PrometheusMetricCreatorUtils.createServiceUpMetrics();
    }

    enum SERVICE {
        PROXY,
        API,
        INBOUND_ENDPOINT
    }

    /**
     * Load the user defined Histogram bucket upper limits configurations from the
     * deployment.toml file else assign the default bucket configuration values.  .
     *
     * @param configs The map of configs defined for Histogram bucket upper limits
     *                in the deployment.toml file
     */
    private void createBuckets(Map<String, Object> configs) {
        proxyLatencyBuckets = new double[]{0.19, 0.20, 0.25, 0.30, 0.35, 0.40, 0.50, 0.60, 1, 5};
        apiLatencyBuckets = new double[]{0.19, 0.20, 0.25, 0.30, 0.35, 0.40, 0.50, 0.60, 1, 5};
        inboundEndpointLatencyBuckets = new double[]{0.19, 0.20, 0.25, 0.30, 0.35, 0.40, 0.50, 0.60, 1, 5};

        Object proxyConfigBuckets = configs.get(MetricConstants.METRIC_HANDLER+ "." +
                MetricConstants.PROXY_LATENCY_BUCKETS);
        Object apiConfigBuckets = configs.get(MetricConstants.METRIC_HANDLER + "." +
                MetricConstants.API_LATENCY_BUCKETS);
        Object inboundEndpointConfigBuckets = configs.get(MetricConstants.METRIC_HANDLER + "." +
                MetricConstants.INBOUND_ENDPOINT_LATENCY_BUCKETS);

        if (null != proxyConfigBuckets) {
            List<Object> list = Arrays.asList(proxyConfigBuckets);
            int size = ((ArrayList) proxyConfigBuckets).size();
            ArrayList bucketList =  (ArrayList) list.get(0);
            for (int i = 0; i < size; i++) {
                proxyLatencyBuckets[i] = (double) bucketList.get(i);
            }
        }
        if (null != apiConfigBuckets) {
            List<Object> list = Arrays.asList(apiConfigBuckets);
            int size = ((ArrayList) apiConfigBuckets).size();
            ArrayList bucketList =  (ArrayList) list.get(0);
            for (int i = 0; i < size; i++) {
                apiLatencyBuckets[i] = (double) bucketList.get(i);
            }
        }
        if (null != inboundEndpointConfigBuckets) {
            List<Object> list = Arrays.asList(inboundEndpointConfigBuckets);
            int size = ((ArrayList) inboundEndpointConfigBuckets).size();
            ArrayList bucketList =  (ArrayList) list.get(0);
            for (int i = 0; i < size; i++) {
                inboundEndpointLatencyBuckets[i] = (double) bucketList.get(i);
            }
        }
    }
}
