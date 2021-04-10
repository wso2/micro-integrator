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
package org.wso2.carbon.inbound.endpoint.protocol.http;

public class InboundHttpConstants {

    /**
     * String representing SOAPAction which is used to set WSAction in synapse message properties.
     */
    public final static String SOAP_ACTION = "SOAPAction";

    /**
     * String representing port of the InboundEndpoint
     */
    public static final String INBOUND_ENDPOINT_PARAMETER_HTTP_PORT = "inbound.http.port";
    public static final String INBOUND_ENDPOINT_PARAMETER_API_DISPATCHING_ENABLED = "api.dispatching.enabled";
    public static final String INBOUND_ENDPOINT_PARAMETER_DISPATCH_FILTER_PATTERN = "dispatch.filter.pattern";
    public static final String KEY_STORE = "keystore";
    public static final String TRUST_STORE = "truststore";
    public static final String SSL_VERIFY_CLIENT = "SSLVerifyClient";
    public static final String SSL_PROTOCOL = "SSLProtocol";
    public static final String HTTPS_PROTOCOL = "HttpsProtocols";
    public static final String CLIENT_REVOCATION = "CertificateRevocationVerifier";
    public static final String HTTP = "http";
    public static final String HTTPS = "https";
    public static final String ENABLE_PORT_OFFSET_FOR_INBOUND_ENDPOINT = "inbound.port.offset.enable";
    /**
     * Defines the core size (number of threads) of the worker thread pool.
     */
    public static final String INBOUND_WORKER_POOL_SIZE_CORE = "inbound.worker.pool.size.core";

    /**
     * Defines the maximum size (number of threads) of the worker thread pool.
     */
    public static final String INBOUND_WORKER_POOL_SIZE_MAX = "inbound.worker.pool.size.max";

    /**
     * Defines the keep-alive time for extra threads in the worker pool.
     */
    public static final String INBOUND_WORKER_THREAD_KEEP_ALIVE_SEC = "inbound.worker.thread.keep.alive.sec";

    /**
     * Defines the length of the queue that is used to hold Runnable tasks to be executed by the
     * worker pool.
     */
    public static final String INBOUND_WORKER_POOL_QUEUE_LENGTH = "inbound.worker.pool.queue.length";

    public static final String INBOUND_THREAD_GROUP_ID = "inbound.thread.group.id";

    public static final String INBOUND_THREAD_ID = "inbound.thread.id";

    /**
     * Parameter name for preferred ciphers in inbound endpoint configs
     **/
    public static final String PREFERRED_CIPHERS = "PreferredCiphers";

    public static final String INTERNAL_HTTP_INBOUND_ENDPOINT_NAME = "EI_INTERNAL_HTTP_INBOUND_ENDPOINT";
    public static final String INTERNAL_HTTPS_INBOUND_ENDPOINT_NAME = "EI_INTERNAL_HTTPS_INBOUND_ENDPOINT";

}