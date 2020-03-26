/*
 * Copyright 2020 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wso2.carbon.inbound.endpoint.protocol.nats;

/**
 * Constant parameters and default values for NATS and NATS Streaming protocol.
 */
public class NatsConstants {

    private NatsConstants(){}

    // NATS Streaming Constants
    public static final String NATS_STREAMING = "nats.streaming";
    public static final String NATS_STREAMING_URL = "nats.streaming.url";
    public static final String NATS_STREAMING_CLIENT_ID = "nats.streaming.client.id";
    public static final String NATS_STREAMING_CLUSTER_ID = "nats.streaming.cluster.id";
    public static final String NATS_STREAMING_QUEUE_GROUP = "nats.streaming.queue.group";
    public static final String NATS_STREAMING_DURABLE_NAME = "nats.streaming.durable.name";
    public static final String NATS_STREAMING_MANUAL_ACK = "nats.streaming.manual.ack";
    public static final String NATS_STREAMING_ACK_WAIT = "nats.streaming.ack.wait";
    public static final String NATS_STREAMING_MAX_IN_FLIGHT = "nats.streaming.max.in.flight";
    public static final String NATS_STREAMING_SUBSCRIPTION_TIMEOUT = "nats.streaming.subscription.timeout";
    public static final String NATS_STREAMING_DISPATCHER = "nats.streaming.dispatcher";
    public static final String NATS_STREAMING_CONNECT_WAIT = "nats.streaming.connect.wait";
    public static final String NATS_STREAMING_DISCOVER_PREFIX = "nats.streaming.discover.prefix";
    public static final String NATS_STREAMING_MAX_PINGS_OUT = "nats.streaming.max.pings.out";
    public static final String NATS_STREAMING_PING_INTERVAL = "nats.streaming.ping.interval";
    public static final String NATS_STREAMING_TRACE_CONNECTION = "nats.streaming.trace.connection";
    public static final String USE_CORE_NATS_CONNECTION = "use.core.nats.connection";

    // NATS Streaming default values
    public static final String DEFAULT_NATS_STREAMING_URL = "nats://localhost:4222";
    public static final String DEFAULT_NATS_STREAMING_CLUSTER_ID = "test-cluster";

    // Core NATS Constants
    public static final String SUBJECT = "subject";
    public static final String QUEUE_GROUP = "queue.group";
    public static final String BUFFER_SIZE = "buffer.size";
    public static final String TURN_ON_ADVANCED_STATS = "turn.on.advanced.stats";
    public static final String TRACE_CONNECTION = "trace.connection";
    public static final String TLS_PROTOCOL = "tls.protocol";
    public static final String TLS_KEYSTORE_TYPE = "tls.keystore.type";
    public static final String TLS_KEYSTORE_LOCATION = "tls.keystore.location";
    public static final String TLS_KEYSTORE_PASSWORD = "tls.keystore.password";
    public static final String TLS_TRUSTSTORE_TYPE = "tls.truststore.type";
    public static final String TLS_TRUSTSTORE_LOCATION = "tls.truststore.location";
    public static final String TLS_TRUSTSTORE_PASSWORD = "tls.truststore.password";
    public static final String TLS_KEY_MANAGER_ALGORITHM = "tls.key.manager.algorithm";
    public static final String TLS_TRUST_MANAGER_ALGORITHM = "tls.trust.manager.algorithm";
    public static final String CONTENT_TYPE = "content.type";

    // Core NATS default values
    static final String DEFAULT_TLS_ALGORITHM = "SunX509";
    static final String DEFAULT_STORE_TYPE = "JKS";
}
