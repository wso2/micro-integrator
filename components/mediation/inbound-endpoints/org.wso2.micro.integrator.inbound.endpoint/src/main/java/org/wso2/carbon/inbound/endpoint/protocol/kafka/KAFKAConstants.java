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
package org.wso2.carbon.inbound.endpoint.protocol.kafka;

/**
 * The constant parameters for kafka protocol
 */
public class KAFKAConstants {

    public static final String ZOOKEEPER_CONNECT = "zookeeper.connect";

    public static final String GROUP_ID = "group.id";

    public static final String ZOOKEEPER_SESSION_TIMEOUT_MS = "zookeeper.session.timeout.ms";

    public static final String ZOOKEEPER_SYNC_TIME_MS = "zookeeper.sync.time.ms";

    public static final String ZOOKEEPER_COMMIT_INTERVAL_MS = "auto.commit.interval.ms";

    public static final String THREAD_COUNT = "thread.count";

    public static final String TOPICS = "topics";

    public static final String CONTENT_TYPE = "content.type";

    public static final String TOPIC_FILTER = "topic.filter";

    public static final String FILTER_FROM_WHITE_LIST = "filter.from.whitelist";

    public static final String SIMPLE_TOPIC = "simple.topic";

    public static final String SIMPLE_BROKERS = "simple.brokers";

    public static final String SIMPLE_PORT = "simple.port";

    public static final String SIMPLE_PARTITION = "simple.partition";

    public static final String SIMPLE_MAX_MSG_TO_READ = "simple.max.messages.to.read";

    public static final String CONSUMER_TYPE = "consumer.type";

    public static final String CONSUMER_TIMEOUT = "consumer.timeout.ms";

    public static final int SO_TIMEOUT = 100000;

    public static final int BUFFER_SIZE = 64 * 1024;

}
