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

package org.wso2.micro.integrator.prometheus.observability.metrics.publish.publish.publish;

import io.prometheus.client.Counter;
import io.prometheus.client.Gauge;
import io.prometheus.client.Histogram;

/**
 * Class for registering Prometheus Metrics.
 */
public class PrometheusMetrics {
    public static final String SERVICE_NAME = "service_name";
    public static final String SERVICE_TYPE = "service_type";
    public static final String REMOTE_ADDRESS = "remote_address";
    public static final String INVOCATION_URL = "invocation_url";
    public static final String PROXY_NAME = "proxy_name";
    public static final String API_NAME = "api_name";
    public static final String INBOUND_ENDPOINT_NAME = "inobund_endpoint_name";
    public static final String HOST = "host";
    public static final String PORT = "port";
    public static final String JAVA_VERSION = "java_version";
    public static final String JAVA_HOME = "java_home";
    public static final String START_TIME = "start_time";

    protected PrometheusMetrics() {

    }

    /**
     * Counters for instrumenting metrics for Proxy Services.
     **/
    public static final Counter TOTAL_REQUESTS_RECEIVED_PROXY_SERVICE = Counter.build
            ("wso2_integration_proxy_request_count_total", "Total number of requests. to a proxy service").
            labelNames(SERVICE_NAME,SERVICE_TYPE,REMOTE_ADDRESS).register();

    public static final Counter ERROR_REQUESTS_RECEIVED_PROXY_SERVICE = Counter.build
            ("wso2_integration_proxy_request_count_error_total", "Total number of error requests " +
                    "to a proxy service").labelNames(SERVICE_NAME,SERVICE_TYPE,REMOTE_ADDRESS).register();

    /**
     * Counters for instrumenting metrics for APIS.
     **/
    public static final Counter TOTAL_REQUESTS_RECEIVED_API =
            Counter.build("wso2_integration_api_request_count_total",
                    "Total number of requests to an API.").
                    labelNames(SERVICE_NAME, SERVICE_TYPE,INVOCATION_URL,REMOTE_ADDRESS).register();

    public static final Counter ERROR_REQUESTS_RECEIVED_API =
            Counter.build("wso2_integration_api_request_error_count_total",
                    "Total number of error requests to an api").
                    labelNames(SERVICE_NAME, SERVICE_TYPE,INVOCATION_URL,REMOTE_ADDRESS).register();

    /**
     * Counters for instrumenting metrics for Inbound Endpoints.
     **/
    public static final Counter TOTAL_REQUESTS_RECEIVED_INBOUND_ENDPOINT = Counter.
                  build(("wso2_integration_inbound_endpoint_request_count_total"),
                   "Total number of requests to an Inbound Endpoint.").
                                        labelNames(SERVICE_NAME,SERVICE_TYPE).register();

    public static final Counter ERROR_REQUESTS_RECEIVED_INBOUND_ENDPOINT = Counter.build
            ("wso2_integration_inbound_endpoint_error_request_count_total",
                      "Total number of error requests to an inbound endpoint").
                                       labelNames(SERVICE_NAME,SERVICE_TYPE).register();

    /**
     * Gauges for instrumenting metrics during Server startup and artifact deployment.
     **/
    public static final Gauge SERVER_UP = Gauge.build("wso2_up", "Server status").
              labelNames(HOST, PORT, JAVA_HOME, JAVA_VERSION, START_TIME).register();

    public static final Gauge SERVICE_UP = Gauge.build("wso2_service_up", "Service status").
            labelNames(SERVICE_NAME, SERVICE_TYPE, START_TIME).register();

    /**
     * Histograms for instrumenting metrics for Proxy Services.
     **/

    public static final Histogram PROXY_LATENCY_DURATION_HISTOGRAM = Histogram.build()
            .name("proxy_latency_seconds")
            .help("Proxy service latency in seconds")
            .labelNames(PROXY_NAME)
            .buckets(0.19, 0.20, 0.25, 0.30, 0.35, 0.40, 0.50, 0.60, 1, 5)
            .register();

    /**
     * Histograms for instrumenting metrics for APIs.
     **/

    public static final Histogram API_REQUEST_LATENCY_HISTOGRAM = Histogram.build()
            .name("api_latency_time_seconds")
            .help("API latency time in seconds")
            .labelNames(API_NAME, INVOCATION_URL)
            .buckets(0.0005, 0.0007, 0.001, 0.005, 0.05, 0.1, 0.2, 0.3, 0.4, 0.5, 1)
            .register();

    /**
     * Histograms for instrumenting metrics for Inbound Endpoints.
     **/
    public static final Histogram INBOUND_ENDPOINT_LATENCY_HISTOGRAM = Histogram.build()
            .name("inbound_endpoint_latency_time_seconds")
            .help("Inbound Endpoint latency time in seconds")
            .labelNames(INBOUND_ENDPOINT_NAME)
            .buckets(0.00001,0.0001,0.0005, 0.0007, 0.001, 0.005, 0.05, 0.1, 0.2, 0.3, 0.4, 0.5, 1, 5, 10)
            .register();

}
