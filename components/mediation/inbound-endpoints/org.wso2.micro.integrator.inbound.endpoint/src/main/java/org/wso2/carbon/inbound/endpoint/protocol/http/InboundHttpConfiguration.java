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

public class InboundHttpConfiguration {

    private final int port;
    private final String name;
    private final String coreSize;
    private final String maxSize;
    private final String keepAlive;
    private final String queueLength;
    private final String threadGroup;
    private final String threadID;
    private final String dispatchPattern;

    private InboundHttpConfiguration(InboundHttpConfigurationBuilder builder) {
        this.port = builder.port;
        this.name = builder.name;
        this.coreSize = builder.coreSize;
        this.maxSize = builder.maxSize;
        this.keepAlive = builder.keepAlive;
        this.queueLength = builder.queueLength;
        this.threadGroup = builder.threadGroup;
        this.threadID = builder.threadID;
        this.dispatchPattern = builder.dispatchPattern;
    }

    public int getPort() {
        return port;
    }

    public String getName() {
        return getName();
    }

    public String getCoresize() {
        return coreSize;
    }

    public String getMaxSize() {
        return maxSize;
    }

    public String getKeepAlive() {
        return keepAlive;
    }

    public String getQueueLength() {
        return queueLength;
    }

    public String getThreadGroup() {
        return threadGroup;
    }

    public String getThreadID() {
        return threadID;
    }

    public String getDispatchPattern() {
        return dispatchPattern;
    }

    public static class InboundHttpConfigurationBuilder {
        private final int port;
        private final String name;
        private String coreSize;
        private String maxSize;
        private String keepAlive;
        private String queueLength;
        private String threadGroup;
        private String threadID;
        private String dispatchPattern;

        public InboundHttpConfigurationBuilder(int port, String name) {
            this.port = port;
            this.name = name;
        }

        public InboundHttpConfiguration build() {
            return new InboundHttpConfiguration(this);
        }

        public InboundHttpConfigurationBuilder workerPoolCoreSize(String coreSize) {
            this.coreSize = coreSize;
            return this;
        }

        public InboundHttpConfigurationBuilder workerPoolMaxSize(String maxSize) {
            this.maxSize = maxSize;
            return this;
        }

        public InboundHttpConfigurationBuilder workerPoolKeepAlive(String keepAlive) {
            this.keepAlive = keepAlive;
            return this;
        }

        public InboundHttpConfigurationBuilder workerPoolQueueLength(String queueLength) {
            this.queueLength = queueLength;
            return this;
        }

        public InboundHttpConfigurationBuilder workerPoolThreadGroup(String threadGroup) {
            this.threadGroup = threadGroup;
            return this;
        }

        public InboundHttpConfigurationBuilder workerPoolThreadId(String threadID) {
            this.threadID = threadID;
            return this;
        }

        public InboundHttpConfigurationBuilder dispatchPattern(String dispatchPattern) {
            this.dispatchPattern = dispatchPattern;
            return this;
        }
    }

}

