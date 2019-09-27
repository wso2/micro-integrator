/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.micro.integrator.management.apis.security.handler;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

public class JWTTokenCleanupTask {

    private static final Log log = LogFactory.getLog(JWTTokenCleanupTask.class);

    private static JWTTokenCleanupTask JWT_CLEANUP_TASK_INSTANCE = null;

    private static ScheduledExecutorService executor;

    private JWTTokenCleanupTask() {

        startAndScheduleTask();
    }

    public static JWTTokenCleanupTask startCleanupTask() {

        if (JWT_CLEANUP_TASK_INSTANCE == null) {
            JWT_CLEANUP_TASK_INSTANCE = new JWTTokenCleanupTask();
        } else if (JWT_CLEANUP_TASK_INSTANCE != null && executor.isShutdown()) {
            JWT_CLEANUP_TASK_INSTANCE.startAndScheduleTask();
        }
        return JWT_CLEANUP_TASK_INSTANCE;
    }

    private void startAndScheduleTask() {

        executor = Executors.newScheduledThreadPool(1,
                new ThreadFactory() {

                    public Thread newThread(Runnable runnableInstance) {

                        Thread cleanupThread = new Thread(runnableInstance);
                        cleanupThread.setName("JWT Token Cleanup Task");
                        return cleanupThread;
                    }
                });

        int cleanupInterval = JWTConfig.getInstance().getJwtConfigDto().getCleanupThreadInterval();

        if (log.isDebugEnabled()) {
            log.debug("JWT Token Cleanup Task Frequency set to " + cleanupInterval);
        }

        executor.scheduleAtFixedRate(new TokenCleanupTask(), cleanupInterval,
                cleanupInterval, TimeUnit.SECONDS);
    }

    /**
     * Shutdown the executor thread.
     */
    public void destroy() {

        if (executor != null && !executor.isShutdown()) {
            executor.shutdown();
        }
    }

    /**
     * Clean up task thread
     */
    private class TokenCleanupTask implements Runnable {

        public void run() {

            if (log.isDebugEnabled()) {
                log.debug("Running the token cleanup task");
            }
            JWTInMemoryTokenStore tokenStore = JWTInMemoryTokenStore.getInstance();
            if (tokenStore.getCurrentSize() != 0) { //Check whether the token store is empty
                tokenStore.removeExpired(); //If not remove all expired and continue next run
            } else {
                if (log.isDebugEnabled()) {
                    log.debug("Destroying the token cleanup task");
                }
                destroy(); //Destroy the thread if the store is empty since its cleanup task is done. It will retrigger
                // once a new token is created.
            }
        }
    }
}
