/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.micro.integrator.dashboard.state.monitor;

import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SimpleScheduleBuilder;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.impl.StdSchedulerFactory;

/**
 * Constants used for utils.
 */
public final class StateMonitor {
    private static final String NAME_OF_JOB = "StateMonitorJob";
    private static final String NAME_OF_GROUP = "StateMonitorGroup";
    private static final String NAME_OF_TRIGGER = "triggerStart";

    private static final int TIME_INTERVAL = 30;
    public void startStateMonitor() throws StateMonitorException {
        Trigger triggerNew =  createTrigger();
        Scheduler scheduler;
        try {
            scheduler = new StdSchedulerFactory().getScheduler();
            scheduler.start();
        } catch (SchedulerException e) {
            throw new StateMonitorException("Error while starting the scheduler", e);
        }
        scheduleJob(triggerNew, scheduler);
    }
    private static void scheduleJob(Trigger triggerNew, Scheduler scheduler) throws StateMonitorException {

        //create an instance of the JoDetails to connect Quartz job to the CreateQuartzJob
        JobDetail jobInstance = JobBuilder.newJob(StateMonitorJob.class)
                .withIdentity(NAME_OF_JOB, NAME_OF_GROUP).build();

        //invoke scheduleJob method to connect the Quartz scheduler to the jobInstance and the triggerNew
        try {
            scheduler.scheduleJob(jobInstance, triggerNew);
        } catch (SchedulerException e) {
            throw new StateMonitorException("Error while starting the scheduler", e);
        }

    }
    private Trigger createTrigger() {

        //create a trigger to be returned from the method
        Trigger triggerNew = TriggerBuilder.newTrigger().withIdentity(NAME_OF_TRIGGER, NAME_OF_GROUP)
                .withSchedule(
                        SimpleScheduleBuilder.simpleSchedule().withIntervalInSeconds(TIME_INTERVAL).repeatForever())
                .build();

        return triggerNew;
    }
    public static final String HEARTBEAT_POOL_SIZE = "heartbeat_pool_size";
}
