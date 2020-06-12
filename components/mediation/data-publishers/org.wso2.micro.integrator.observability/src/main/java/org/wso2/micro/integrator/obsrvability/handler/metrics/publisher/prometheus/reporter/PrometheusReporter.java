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
package org.wso2.micro.integrator.obsrvability.handler.metrics.publisher.prometheus.reporter;

import io.prometheus.client.Counter;
import io.prometheus.client.Gauge;
import io.prometheus.client.Histogram;
import io.prometheus.client.hotspot.DefaultExports;
import org.wso2.micro.integrator.initializer.ServiceBusConstants;
import org.wso2.config.mapper.ConfigParser;
import org.wso2.micro.integrator.obsrvability.handler.metrics.publisher.MetricReporter;
import org.wso2.micro.integrator.obsrvability.handler.util.MetricConstants;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PrometheusReporter implements MetricReporter {
    static Counter TOTAL_REQUESTS_RECEIVED_PROXY_SERVICE;
    static Counter TOTAL_REQUESTS_RECEIVED_API;
    static Counter TOTAL_REQUESTS_RECEIVED_INBOUND_ENDPOINT;
    static Counter ERROR_REQUESTS_RECEIVED_PROXY_SERVICE;
    static Counter ERROR_REQUESTS_RECEIVED_API;
    static Counter ERROR_REQUESTS_RECEIVED_INBOUND_ENDPOINT;

    static Histogram PROXY_LATENCY_HISTOGRAM;
    static Histogram API_LATENCY_HISTOGRAM;
    static Histogram INBOUND_ENDPOINT_LATENCY_HISTOGRAM;

    static Gauge SERVER_UP;
    static Gauge SERVICE_UP;

    double[] proxyLatencyBuckets;
    double[] apiLatencyBuckets;
    double[] inboundEndpointLatencyBuckets;

    static Map<String, Object> metricMap = new HashMap();

    @Override
    public void initMetric(String serviceType, String type, String metricName, String metricHelp, Map<String,
            String[]> properties) {
        proxyLatencyBuckets = new double[]{0.19, 0.20, 0.25, 0.30, 0.35, 0.40, 0.50, 0.60, 1, 5};
        apiLatencyBuckets = new double[]{0.19, 0.20, 0.25, 0.30, 0.35, 0.40, 0.50, 0.60, 1, 5};
        inboundEndpointLatencyBuckets = new double[]{0.19, 0.20, 0.25, 0.30, 0.35, 0.40, 0.50, 0.60, 1, 5};

        Map<String, Object> configs = ConfigParser.getParsedConfigs();
        readConfigs(configs);
        DefaultExports.initialize();

        //Read the label names from the map
        String[] labels = properties.get(metricName);

        if (serviceType.equalsIgnoreCase(service.PROXY.name())) {
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
        } else if (serviceType.equalsIgnoreCase(service.API.name())) {
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
        } else if (serviceType.equalsIgnoreCase(service.INBOUND_ENDPOINT.name())) {
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

        if (serviceType.equals(service.PROXY.name())) {
            ERROR_REQUESTS_RECEIVED_PROXY_SERVICE = Counter.build(MetricConstants.PROXY_REQUEST_COUNT_ERROR_TOTAL,
                    metricHelp).
                    labelNames(labels).register();
            metricMap.put(metricName, ERROR_REQUESTS_RECEIVED_PROXY_SERVICE);
        } else if (serviceType.equals(service.API.name())) {
            ERROR_REQUESTS_RECEIVED_API = Counter.build(MetricConstants.API_REQUEST_COUNT_ERROR_TOTAL, metricHelp).
                    labelNames(labels).register();
            metricMap.put(metricName, ERROR_REQUESTS_RECEIVED_API);
        } else if (serviceType.equals(service.INBOUND_ENDPOINT.name())) {
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

    }

    @Override
    public Object getTimer(String metricName, Map<String, String[]> properties) {
        String[] value = properties.get(metricName);
        Histogram timer = (Histogram) metricMap.get(metricName);

        return timer.labels(value).startTimer();
    }

    @Override
    public void observeTime(Object timer) {
        Histogram.Timer timer2 = (Histogram.Timer) timer;
        timer2.observeDuration();
    }

    @Override
    public void serverUp(String host, String port, String javaVersion, String javaHome) {
        Gauge gauge = (Gauge) metricMap.get("wso2_integration_server_up");
        gauge.labels(host, port, javaHome, javaVersion).setToCurrentTime();
    }

    @Override
    public void serverDown(String host, String port, String javaVersion, String javaHome) {
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

    enum service {
        PROXY,
        API,
        INBOUND_ENDPOINT
    }

    private void readConfigs(Map<String, Object> configs) {
        Object proxyConfigBuckets = configs.get(ServiceBusConstants.PROMETHEUS_HANDLER + "." +
                MetricConstants.PROXY_LATENCY_BUCKETS);
        Object apiConfigBuckets = configs.get(ServiceBusConstants.PROMETHEUS_HANDLER + "." +
                MetricConstants.API_LATENCY_BUCKETS);
        Object inboundEndpointConfigBuckets = configs.get(ServiceBusConstants.PROMETHEUS_HANDLER + "." +
                MetricConstants.INBOUND_ENDPOINT_LATENCY_BUCKETS);

        if (null != proxyConfigBuckets) {
            List<Object> list = Arrays.asList(proxyConfigBuckets);

            int size = ((ArrayList) proxyConfigBuckets).size();
            for (int i = 0; i < size; i++) {
                proxyLatencyBuckets[i] = (double) ((ArrayList) list.get(0)).get(i);
            }
        }
        if (null != apiConfigBuckets) {
            List<Object> list = Arrays.asList(apiConfigBuckets);

            int size = ((ArrayList) apiConfigBuckets).size();
            for (int i = 0; i < size; i++) {
                apiLatencyBuckets[i] = (double) ((ArrayList) list.get(0)).get(i);
            }
        }
        if (null != inboundEndpointConfigBuckets) {
            List<Object> list = Arrays.asList(inboundEndpointConfigBuckets);

            int size = ((ArrayList) inboundEndpointConfigBuckets).size();
            for (int i = 0; i < size; i++) {
                inboundEndpointLatencyBuckets[i] = (double) ((ArrayList) list.get(0)).get(i);
            }
        }
    }
}