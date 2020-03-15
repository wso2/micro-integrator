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

package org.wso2.micro.integrator.ntask.core.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.micro.integrator.coordination.ClusterCoordinator;
import org.wso2.micro.integrator.ntask.coordination.task.ClusterNodeDetails;
import org.wso2.micro.integrator.ntask.coordination.task.TaskDataBase;
import org.wso2.micro.integrator.ntask.coordination.task.db.cleaner.TaskDBCleaner;
import org.wso2.micro.integrator.ntask.coordination.task.resolver.TaskLocationResolver;
import org.wso2.micro.integrator.ntask.coordination.task.scehduler.CoordinatedTaskScheduler;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * The class which manages CoordinatedTaskScheduler.
 *
 * @see CoordinatedTaskScheduler
 */
public class CoordinatedTaskScheduleManager {

    private static final Log LOG = LogFactory.getLog(CoordinatedTaskScheduleManager.class);

    private TaskDataBase taskDataBase;
    private ClusterCoordinator clusterCoordinator;
    private TaskLocationResolver resolver;

    public CoordinatedTaskScheduleManager(TaskDataBase taskDataBase, ClusterCoordinator clusterCoordinator,
                                          TaskLocationResolver resolver) {
        this.taskDataBase = taskDataBase;
        this.clusterCoordinator = clusterCoordinator;
        this.resolver = resolver;
    }

    /**
     * Spawns new scheduled executor service which is responsible for handling coordinated tasks.
     */
    public void startTaskScheduler(String msg) {

        ScheduledExecutorService taskSchedulerExecutor = Executors.newSingleThreadScheduledExecutor();
        TaskDBCleaner taskDBCleaner = new TaskDBCleaner(taskDataBase);
        // the frequency at which task resolving need to be done per cleaning.
        int resolvingFrequency = 5;
        ClusterNodeDetails connector = new ClusterNodeDetails(clusterCoordinator);
        CoordinatedTaskScheduler taskScheduler = new CoordinatedTaskScheduler(taskDataBase, resolver, connector,
                                                                              taskDBCleaner, resolvingFrequency);
        int initialDelay = 0; // can start immediately as the task service is already registered.
        //todo read from toml
        int period = 2;
        LOG.info("Triggering coordinated task scheduler with an initial delay of " + initialDelay + " second(s) and a "
                         + "period of " + period + " second(s) upon " + msg);
        taskSchedulerExecutor.scheduleWithFixedDelay(taskScheduler, initialDelay, period, TimeUnit.SECONDS);
        DataHolder.getInstance().setTaskScheduler(taskSchedulerExecutor);
    }

}
