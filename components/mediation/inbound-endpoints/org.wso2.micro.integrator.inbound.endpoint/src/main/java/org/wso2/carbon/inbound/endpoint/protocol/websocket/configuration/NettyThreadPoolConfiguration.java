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

package org.wso2.carbon.inbound.endpoint.protocol.websocket.configuration;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class NettyThreadPoolConfiguration {

    private static final Log log = LogFactory.getLog(NettyThreadPoolConfiguration.class);

    private int bossThreadPoolSize;
    private int workerThreadPoolSize;

    public NettyThreadPoolConfiguration(String bossThreadPoolSize, String workerThreadPoolSize) {

        try {
            if (bossThreadPoolSize != null && !"".equals(bossThreadPoolSize.trim())) {
                this.bossThreadPoolSize = Integer.parseInt(bossThreadPoolSize);
            } else {
                this.bossThreadPoolSize = Runtime.getRuntime().availableProcessors();
            }
            if (workerThreadPoolSize != null && !"".equals(workerThreadPoolSize.trim())) {
                this.workerThreadPoolSize = Integer.parseInt(workerThreadPoolSize);
            } else {
                this.workerThreadPoolSize = Runtime.getRuntime().availableProcessors() * 2;
            }
        } catch (Exception e) {
            log.error("failed to validate the Netty thread pool configuration", e);
        }
    }

    public int getBossThreadPoolSize() {
        return bossThreadPoolSize;
    }

    public int getWorkerThreadPoolSize() {
        return workerThreadPoolSize;
    }

}
