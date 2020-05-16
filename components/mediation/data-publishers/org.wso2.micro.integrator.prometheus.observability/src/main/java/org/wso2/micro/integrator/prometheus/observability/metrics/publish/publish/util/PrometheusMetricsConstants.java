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

package org.wso2.micro.integrator.prometheus.observability.metrics.publish.publish.util;

/**
 * Constants required in instrumenting Prometheus metrics.
 */
public class PrometheusMetricsConstants {

    private PrometheusMetricsConstants() {

    }

    public static final String PROXY_SERVICE = "proxy_service";
    public static final String PROXY_NAME = "proxy.name";
    public static final String API = "api";
    public static final String SYNAPSE_REST_API = "SYNAPSE_REST_API";
    public static final String REMOTE_HOST = "REMOTE_HOST";
    public static final String INBOUND_ENDPOINT = "inbound_endpoint";
    public static final String INBOUND_ENDPOINT_NAME = "inbound.endpoint.name";

    public static final String PROXY_LATENCY_TIMER = "PROXY_LATENCY_TIMER";
    public static final String API_LATENCY_TIMER = "API_LATENCY_TIMER";
    public static final String INBOUND_ENDPOINT_LATENCY_TIMER = "INBOUND_ENDPOINT_LATENCY_TIMER";

    public static final String CARBON_LOCAL_IP = "carbon.local.ip";
    public static final String HTTP_PORT = "http.nio.port";
    public static final String JAVA_VERSION = "java.vm.specification.version";
    public static final String JAVA_HOME = "java.home";
    public static final String SERVER_START_TIME = "wso2carbon.start.time";

    public static final String METRICS_ENDPOINT = "/metric-service/metrics";
    public static final String REST_FULL_REQUEST_PATH = "REST_FULL_REQUEST_PATH";

    //Constants for Synapse properties
    public static final String HAS_EXECUTED_ERROR_FLOW = "HAS_EXECUTED_ERROR_FLOW";
    public static final String TRANSPORT_IN_URL = "TransportInURL";
    public static final String SERVICE_PREFIX = "SERVICE_PREFIX";

}
