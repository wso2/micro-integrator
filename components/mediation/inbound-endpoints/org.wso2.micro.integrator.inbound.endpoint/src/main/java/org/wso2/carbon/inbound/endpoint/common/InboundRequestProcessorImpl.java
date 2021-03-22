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
package org.wso2.carbon.inbound.endpoint.common;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.core.SynapseEnvironment;
import org.apache.synapse.inbound.InboundRequestProcessor;
import org.apache.synapse.startup.quartz.StartUpController;
import org.apache.synapse.task.TaskDescription;
import org.apache.synapse.task.TaskManager;
import org.wso2.carbon.inbound.endpoint.persistence.InboundEndpointsDataStore;
import org.wso2.carbon.inbound.endpoint.protocol.jms.JMSTask;
import org.wso2.micro.integrator.mediation.ntask.NTaskTaskManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * This class provides the common implementation for polling protocol processors
 */
public abstract class InboundRequestProcessorImpl implements InboundRequestProcessor {

    protected SynapseEnvironment synapseEnvironment;
    protected long interval;
    protected String name;
    protected boolean coordination;

    private List<StartUpController> startUpControllersList = new ArrayList<>();
    private HashMap<Thread, InboundRunner> inboundRunnersThreadsMap = new HashMap<>();
    private static final Log log = LogFactory.getLog(InboundRequestProcessorImpl.class);
    private InboundEndpointsDataStore dataStore;

    protected final static String COMMON_ENDPOINT_POSTFIX = "--SYNAPSE_INBOUND_ENDPOINT";

    public InboundRequestProcessorImpl() {
        dataStore = InboundEndpointsDataStore.getInstance();
    }

    /**
     * Based on the coordination option schedule the task with NTASK or run as a
     * background thread
     *
     * @param task
     * @param endpointPostfix
     */
    protected void start(InboundTask task, String endpointPostfix) {
        log.info("Starting the inbound endpoint " + name + ", with coordination " + coordination + ". Interval : "
                         + interval + ". Type : " + endpointPostfix);
        if (coordination) {
            try {
                TaskDescription taskDescription = new TaskDescription();
                taskDescription.setName(name + "-" + endpointPostfix);
                taskDescription.setTaskGroup(endpointPostfix);
                if (interval < InboundTask.TASK_THRESHOLD_INTERVAL) {
                    taskDescription.setInterval(InboundTask.TASK_THRESHOLD_INTERVAL);
                } else {
                    taskDescription.setInterval(interval);
                }
                taskDescription.setIntervalInMs(true);
                taskDescription.addResource(TaskDescription.INSTANCE, task);
                taskDescription.addResource(TaskDescription.CLASSNAME, task.getClass().getName());
                StartUpController startUpController = new StartUpController();
                startUpController.setTaskDescription(taskDescription);
                startUpController.init(synapseEnvironment);
                startUpControllersList.add(startUpController);
                //register a listener to be notified when the local jms task is deleted
                if (task instanceof JMSTask) {
                    TaskManager taskManagerImpl = synapseEnvironment.getTaskManager().getTaskManagerImpl();
                    if (taskManagerImpl instanceof NTaskTaskManager) {
                        ((NTaskTaskManager) taskManagerImpl)
                                .registerListener((JMSTask) task, taskDescription.getName());
                    }
                }
            } catch (Exception e) {
                log.error("Error starting the inbound endpoint " + name + ". Unable to schedule the task. " + e
                        .getLocalizedMessage(), e);
            }
        } else {

            startInboundRunnerThread(task, Constants.SUPER_TENANT_DOMAIN_NAME, false);
        }
    }

    private void startInboundRunnerThread(InboundTask task, String tenantDomain, boolean mgrOverride) {
        InboundRunner inboundRunner = new InboundRunner(task, interval, tenantDomain, mgrOverride);
        Thread runningThread = new Thread(inboundRunner);
        inboundRunnersThreadsMap.put(runningThread, inboundRunner);
        runningThread.start();
    }

    /**
     * Stop the inbound polling processor This will be called when inbound is
     * undeployed/redeployed or when server stop
     */
    public void destroy() {
        log.info("Inbound endpoint " + name + " stopping.");

        if (!startUpControllersList.isEmpty()) {
            for (StartUpController sc : startUpControllersList) {
                sc.destroy();
            }
            startUpControllersList.clear();
        } else if (!inboundRunnersThreadsMap.isEmpty()) {

            Iterator itr = inboundRunnersThreadsMap.entrySet().iterator();
            while (itr.hasNext()) {
                Map.Entry entry = (Map.Entry) itr.next();
                Thread thread = (Thread) entry.getKey();
                InboundRunner inboundRunner = (InboundRunner) entry.getValue();

                inboundRunner.terminate();
                thread.interrupt();
                try {
                    thread.join();
                } catch (InterruptedException e) {
                    thread.interrupt();
                    log.error("Error while stopping the inbound thread.");
                }
            }
            inboundRunnersThreadsMap.clear();
        }
    }

}
