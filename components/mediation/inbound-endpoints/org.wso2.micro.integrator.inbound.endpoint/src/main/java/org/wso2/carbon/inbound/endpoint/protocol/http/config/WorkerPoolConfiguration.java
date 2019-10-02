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

package org.wso2.carbon.inbound.endpoint.protocol.http.config;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Wrapping class for Inbound Worker Pool Configs
 */
public class WorkerPoolConfiguration {

    private static final Log log = LogFactory.getLog(WorkerPoolConfiguration.class);

    private int workerPoolCoreSize;
    private int workerPoolSizeMax;
    private int workerPoolThreadKeepAliveSec;

    private int workerPoolQueuLength;

    private String threadGroupID;
    private String threadID;

    public WorkerPoolConfiguration(String workerPoolCoreSize, String workerPoolSizeMax,
                                   String workerPoolThreadKeepAliveSec, String workerPoolQueuLength,
                                   String threadGroupID, String threadID) {

        try {
            if (workerPoolCoreSize != null && !"".equals(workerPoolCoreSize.trim())) {
                this.workerPoolCoreSize = Integer.parseInt(workerPoolCoreSize);
            }
            if (workerPoolSizeMax != null && !"".equals(workerPoolSizeMax.trim())) {
                this.workerPoolSizeMax = Integer.parseInt(workerPoolSizeMax);
            }
            if (workerPoolThreadKeepAliveSec != null && !"".equals(workerPoolThreadKeepAliveSec.trim())) {
                this.workerPoolThreadKeepAliveSec = Integer.parseInt(workerPoolThreadKeepAliveSec);
            }
            if (workerPoolQueuLength != null && !"".equals(workerPoolQueuLength.trim())) {
                this.workerPoolQueuLength = Integer.parseInt(workerPoolQueuLength);
            }
            if (threadGroupID != null && !"".equals(threadGroupID.trim())) {
                this.threadGroupID = threadGroupID;
            } else {
                this.threadGroupID = "Pass-Through Inbound WorkerThread Group";
            }
            if (threadID != null && !"".equals(threadID.trim())) {
                this.threadID = threadID;
            } else {
                this.threadID = "PassThroughInboundWorkerThread";
            }
        } catch (Exception e) {
            log.error("Please Provide int value for worker pool configuration", e);
        }
    }

    public int getWorkerPoolCoreSize() {
        return workerPoolCoreSize;
    }

    public int getWorkerPoolSizeMax() {
        return workerPoolSizeMax;
    }

    public int getWorkerPoolThreadKeepAliveSec() {
        return workerPoolThreadKeepAliveSec;
    }

    public String getThreadGroupID() {
        return threadGroupID;
    }

    public String getThreadID() {
        return threadID;
    }

    public int getWorkerPoolQueuLength() {
        return workerPoolQueuLength;
    }
}
