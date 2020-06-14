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

import org.apache.synapse.SynapseConstants;
import org.wso2.micro.integrator.obsrvability.handler.metrics.publisher.prometheus.reporter.PrometheusReporter;
import org.wso2.micro.integrator.obsrvability.handler.util.MetricConstants;

import java.util.HashMap;
import java.util.Map;

public class PrometheusMetricCreator {

    private static final String SERVICE_NAME = "service_name";
    private static final String SERVICE_TYPE = "service_type";
    private static final String INVOCATION_URL = "invocation_url";
    private static final String HOST = "host";
    private static final String PORT = "port";
    private static final String JAVA_VERSION = "java_version";
    private static final String JAVA_HOME = "java_home";
    private PrometheusReporter prometheusReporter;

    public PrometheusMetricCreator () {

        prometheusReporter = new PrometheusReporter();
    }

    public void createProxyServiceMetric() {
        Map<String, String[]> map = new HashMap<>();
        String[] labels = {SERVICE_NAME, SERVICE_TYPE};
        map.put(MetricConstants.PROXY_REQUEST_COUNT_TOTAL, labels);
        map.put(MetricConstants.PROXY_LATENCY_SECONDS, labels);

        prometheusReporter.initMetric(SynapseConstants.PROXY_SERVICE_TYPE, MetricConstants.COUNTER,
                MetricConstants.PROXY_REQUEST_COUNT_TOTAL,
                "Total number of requests to a proxy service", map);
        prometheusReporter.initMetric(SynapseConstants.PROXY_SERVICE_TYPE, MetricConstants.HISTOGRAM,
                MetricConstants.PROXY_LATENCY_SECONDS,
                "Latency of requests to a proxy service", map);
    }

    public void createAPIServiceMetric() {
        HashMap<String, String[]> map = new HashMap();
        String[] labels = {SERVICE_NAME, SERVICE_TYPE, INVOCATION_URL};
        map.put(MetricConstants.API_REQUEST_COUNT_TOTAL, labels);
        map.put(MetricConstants.API_LATENCY_SECONDS, labels);

        prometheusReporter.initMetric(SynapseConstants.FAIL_SAFE_MODE_API, MetricConstants.COUNTER,
                MetricConstants.API_REQUEST_COUNT_TOTAL,
                "Total number of requests to an api", map);
        prometheusReporter.initMetric(SynapseConstants.FAIL_SAFE_MODE_API, MetricConstants.HISTOGRAM,
                MetricConstants.API_LATENCY_SECONDS,
                "Latency of requests to an api", map);
    }

    public void createInboundEndpointMetric() {
        HashMap<String, String[]> map = new HashMap<>();
        String[] labels = {SERVICE_NAME, SERVICE_TYPE};
        map.put(MetricConstants.INBOUND_ENDPOINT_REQUEST_COUNT_TOTAL, labels);
        map.put(MetricConstants.INBOUND_ENDPOINT_LATENCY_SECONDS, labels);

        prometheusReporter.initMetric("INBOUND_ENDPOINT", MetricConstants.COUNTER,
                MetricConstants.INBOUND_ENDPOINT_REQUEST_COUNT_TOTAL,
                "Total number of requests to an inbound endpoint.", map);

        prometheusReporter.initMetric("INBOUND_ENDPOINT", MetricConstants.HISTOGRAM,
                MetricConstants.INBOUND_ENDPOINT_LATENCY_SECONDS,
                "Latency of requests to an inbound endpoint.", map);
    }

    public void createProxyServiceErrorMetric() {
        HashMap<String, String[]> map = new HashMap();
        map.put(MetricConstants.PROXY_REQUEST_COUNT_ERROR_TOTAL, new String[]{SERVICE_NAME, SERVICE_TYPE});
        prometheusReporter.initErrorMetrics("PROXY", MetricConstants.COUNTER,
                MetricConstants.PROXY_REQUEST_COUNT_ERROR_TOTAL,
                "Total number of error requests to a proxy service", map);
    }

    public void createApiErrorMetric() {
        HashMap<String, String[]> map = new HashMap();

        map.put(MetricConstants.API_REQUEST_COUNT_ERROR_TOTAL, new String[]{SERVICE_NAME, SERVICE_TYPE,
                INVOCATION_URL});
        prometheusReporter.initErrorMetrics("API", MetricConstants.COUNTER,
                MetricConstants.API_REQUEST_COUNT_ERROR_TOTAL,
                "Total number of error requests to an api", map);
    }

    public void createInboundEndpointErrorMetric() {
        HashMap<String, String[]> map = new HashMap();

        map.put(MetricConstants.INBOUND_ENDPOINT_REQUEST_COUNT_ERROR_TOTAL, new String[]{SERVICE_NAME, SERVICE_TYPE});
        prometheusReporter.initErrorMetrics("INBOUND_ENDPOINT", MetricConstants.COUNTER,
                MetricConstants.INBOUND_ENDPOINT_REQUEST_COUNT_ERROR_TOTAL,
                "Total number of error requests to an inbound endpoint.", map);
    }

    public void createServerUpMetrics() {
        HashMap map = new HashMap();
        map.put(MetricConstants.SERVER_UP, new String[]{HOST, PORT, JAVA_HOME, JAVA_VERSION});

        prometheusReporter.initMetric(MetricConstants.SERVER, MetricConstants.GAUGE, MetricConstants.SERVER_UP,
                "Server Status", map);
    }

    public void createServiceUpMetrics() {
        HashMap map = new HashMap();
        map.put(MetricConstants.SERVICE_UP, new String[]{SERVICE_NAME, SERVICE_TYPE});

        prometheusReporter.initMetric(MetricConstants.SERVICE, MetricConstants.GAUGE, MetricConstants.SERVICE_UP,
                "Service Status", map);
    }
}
