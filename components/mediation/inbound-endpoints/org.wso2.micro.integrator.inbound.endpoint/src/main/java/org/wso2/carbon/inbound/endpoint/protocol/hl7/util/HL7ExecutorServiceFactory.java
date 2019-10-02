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

package org.wso2.carbon.inbound.endpoint.protocol.hl7.util;

import org.wso2.carbon.inbound.endpoint.protocol.hl7.core.MLLPConstants;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

public class HL7ExecutorServiceFactory {

    private static ScheduledExecutorService executorService = Executors.newScheduledThreadPool(
            HL7Configuration.getInstance().getIntProperty(MLLPConstants.TCPConstants.WORKER_THREADS_CORE,
                                                          MLLPConstants.TCPConstants.WORKER_THREADS_CORE_DEFAULT),
            HL7WorkerThreadFactory.getInstance());

    public static ScheduledExecutorService getExecutorService() {
        return executorService;
    }

    private static class HL7WorkerThreadFactory implements ThreadFactory {
        final ThreadGroup group;
        final AtomicInteger threadNumber = new AtomicInteger(1);
        final String namePrefix;

        private static HL7WorkerThreadFactory instance = new HL7WorkerThreadFactory();

        private HL7WorkerThreadFactory() {
            group = new ThreadGroup("HL7-inbound-thread-group");
            namePrefix = "HL7-inbound-worker-";
        }

        public static HL7WorkerThreadFactory getInstance() {
            return instance;
        }

        public Thread newThread(Runnable r) {
            Thread t = new Thread(group, r, namePrefix + threadNumber.getAndIncrement(), 0);
            if (t.isDaemon())
                t.setDaemon(false);
            if (t.getPriority() != Thread.NORM_PRIORITY)
                t.setPriority(Thread.NORM_PRIORITY);
            return t;
        }
    }

}

