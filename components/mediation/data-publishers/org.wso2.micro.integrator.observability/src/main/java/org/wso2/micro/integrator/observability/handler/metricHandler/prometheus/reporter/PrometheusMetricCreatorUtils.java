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
package org.wso2.micro.integrator.observability.handler.metricHandler.prometheus.reporter;

import org.apache.synapse.SynapseConstants;
import org.wso2.micro.integrator.observability.handler.util.MetricConstants;

import java.util.HashMap;
import java.util.Map;

/**
 * Class for creating Prometheus Metrics from the data scraped in the Metric Handler.
 */
public class PrometheusMetricCreatorUtils {
    private static PrometheusReporter prometheusReporter = new PrometheusReporter();

    /**
     * Create the proxy services related metrics.
     */
    public static void createProxyServiceMetric() {
        Map<String, String[]> map = new HashMap<>();
        String[] labels = {MetricConstants.SERVICE_NAME, MetricConstants.SERVICE_TYPE};
        map.put(MetricConstants.PROXY_REQUEST_COUNT_TOTAL, labels);
        map.put(MetricConstants.PROXY_LATENCY_SECONDS, labels);

         prometheusReporter.createMetric(SynapseConstants.PROXY_SERVICE_TYPE, MetricConstants.COUNTER,
                MetricConstants.PROXY_REQUEST_COUNT_TOTAL,
                "Total number of requests to a proxy service", map);
        prometheusReporter.createMetric(SynapseConstants.PROXY_SERVICE_TYPE, MetricConstants.HISTOGRAM,
                MetricConstants.PROXY_LATENCY_SECONDS,
                "Latency of requests to a proxy service", map);
    }

    /**
     * Create the api related metrics.
     */
    public static void createAPIServiceMetric() {
        HashMap<String, String[]> map = new HashMap();
        String[] labels = {MetricConstants.SERVICE_NAME, MetricConstants.SERVICE_TYPE, MetricConstants.INVOCATION_URL};
        map.put(MetricConstants.API_REQUEST_COUNT_TOTAL, labels);
        map.put(MetricConstants.API_LATENCY_SECONDS, labels);

        prometheusReporter.createMetric(SynapseConstants.FAIL_SAFE_MODE_API, MetricConstants.COUNTER,
                MetricConstants.API_REQUEST_COUNT_TOTAL,
                "Total number of requests to an api", map);
        prometheusReporter.createMetric(SynapseConstants.FAIL_SAFE_MODE_API, MetricConstants.HISTOGRAM,
                MetricConstants.API_LATENCY_SECONDS,
                "Latency of requests to an api", map);
    }

    /**
     * Create the inbound endpoint related metrics.
     */
    public static void createInboundEndpointMetric() {
        HashMap<String, String[]> map = new HashMap<>();
        String[] labels = {MetricConstants.SERVICE_NAME, MetricConstants.SERVICE_TYPE};
        map.put(MetricConstants.INBOUND_ENDPOINT_REQUEST_COUNT_TOTAL, labels);
        map.put(MetricConstants.INBOUND_ENDPOINT_LATENCY_SECONDS, labels);

        prometheusReporter.createMetric("INBOUND_ENDPOINT", MetricConstants.COUNTER,
                MetricConstants.INBOUND_ENDPOINT_REQUEST_COUNT_TOTAL,
                "Total number of requests to an inbound endpoint.", map);
        prometheusReporter.createMetric("INBOUND_ENDPOINT", MetricConstants.HISTOGRAM,
                MetricConstants.INBOUND_ENDPOINT_LATENCY_SECONDS,
                "Latency of requests to an inbound endpoint.", map);
    }

    /**
     * Create the metrics related to failed proxy services.
     */
    public static void createProxyServiceErrorMetric() {
        HashMap<String, String[]> map = new HashMap();
        map.put(MetricConstants.PROXY_REQUEST_COUNT_ERROR_TOTAL, new String[]{MetricConstants.SERVICE_NAME,
                MetricConstants.SERVICE_TYPE});
        new PrometheusReporter().initErrorMetrics("PROXY", MetricConstants.COUNTER,
                MetricConstants.PROXY_REQUEST_COUNT_ERROR_TOTAL,
                "Total number of error requests to a proxy service", map);
    }

    /**
     * Create the metrics related to failed apis.
     */
    public static void createApiErrorMetric() {
        HashMap<String, String[]> map = new HashMap();

        map.put(MetricConstants.API_REQUEST_COUNT_ERROR_TOTAL, new String[]{MetricConstants.SERVICE_NAME,
                MetricConstants.SERVICE_TYPE, MetricConstants.INVOCATION_URL});
        prometheusReporter.initErrorMetrics("API", MetricConstants.COUNTER,
                MetricConstants.API_REQUEST_COUNT_ERROR_TOTAL,
                "Total number of error requests to an api", map);
    }

    /**
     * Create the metrics related to failed inbound endpoints.
     */
    public static void createInboundEndpointErrorMetric() {
        HashMap<String, String[]> map = new HashMap();

        map.put(MetricConstants.INBOUND_ENDPOINT_REQUEST_COUNT_ERROR_TOTAL, new String[]{MetricConstants.SERVICE_NAME,
                MetricConstants.SERVICE_TYPE});
        prometheusReporter.initErrorMetrics("INBOUND_ENDPOINT", MetricConstants.COUNTER,
                MetricConstants.INBOUND_ENDPOINT_REQUEST_COUNT_ERROR_TOTAL,
                "Total number of error requests to an inbound endpoint.", map);
    }

    /**
     * Create the metrics related to server startup.
     */
    public static void createServerUpMetrics() {
        HashMap map = new HashMap();
        map.put(MetricConstants.SERVER_UP, new String[]{MetricConstants.HOST, MetricConstants.PORT,
                MetricConstants.JAVA_HOME_LABEL, MetricConstants.JAVA_VERSION_LABEL});

        prometheusReporter.createMetric(MetricConstants.SERVER, MetricConstants.GAUGE, MetricConstants.SERVER_UP,
                "Server Status", map);
    }

    /**
     * Create the metrics related to service deployment.
     */
    public static void createServiceUpMetrics() {
        HashMap map = new HashMap();
        map.put(MetricConstants.SERVICE_UP, new String[]{MetricConstants.SERVICE_NAME, MetricConstants.SERVICE_TYPE});

        prometheusReporter.createMetric(MetricConstants.SERVICE, MetricConstants.GAUGE, MetricConstants.SERVICE_UP,
                "Service Status", map);
    }
}
