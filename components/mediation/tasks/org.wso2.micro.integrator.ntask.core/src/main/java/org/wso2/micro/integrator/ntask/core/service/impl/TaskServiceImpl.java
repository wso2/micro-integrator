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
package org.wso2.micro.integrator.ntask.core.service.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.wso2.carbon.ntask.common.TaskException;
import org.wso2.carbon.ntask.common.TaskException.Code;
import org.wso2.carbon.ntask.core.impl.clustered.ClusterGroupCommunicator;
import org.wso2.carbon.ntask.core.impl.remote.RemoteTaskManager;
import org.wso2.micro.integrator.core.util.MicroIntegratorBaseUtils;
import org.wso2.micro.integrator.ntask.core.TaskManager;
import org.wso2.micro.integrator.ntask.core.TaskManagerFactory;
import org.wso2.micro.integrator.ntask.core.TaskManagerId;
import org.wso2.micro.integrator.ntask.core.TaskUtils;
import org.wso2.micro.integrator.ntask.core.impl.standalone.StandaloneTaskManagerFactory;
import org.wso2.micro.integrator.ntask.core.service.TaskService;
import org.wso2.micro.integrator.ntask.core.service.impl.TaskServiceXMLConfiguration.DefaultLocationResolver;
import org.wso2.micro.integrator.ntask.core.service.impl.TaskServiceXMLConfiguration.DefaultLocationResolver.Property;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;

/**
 * This class represents the TaskService implementation.
 * @see TaskService
 */
public class TaskServiceImpl implements TaskService {

    private static final Log log = LogFactory.getLog(TaskServiceImpl.class);

    private Set<String> registeredTaskTypes;

    private boolean serverInit;

    private org.wso2.micro.integrator.ntask.core.TaskManagerFactory taskManagerFactory;

    private TaskServiceConfiguration taskServerConfiguration;

    private TaskServerMode effectiveTaskServerMode;

    static final int SUPER_TENANT_ID = -1234;

    public TaskServiceImpl() throws TaskException {
        this.registeredTaskTypes = new HashSet<String>();
        this.taskServerConfiguration = new TaskServiceConfigurationImpl(
                this.loadTaskServiceXMLConfig());
        switch (this.getServerConfiguration().getTaskServerMode()) {
            case STANDALONE:
                this.taskManagerFactory = new StandaloneTaskManagerFactory();
                this.effectiveTaskServerMode = TaskServerMode.STANDALONE;
                break;
            case AUTO:
                    this.taskManagerFactory = new StandaloneTaskManagerFactory();
                    ((StandaloneTaskManagerFactory) this.taskManagerFactory).setTaskRepositoryClass(this.taskServerConfiguration.getTaskRepositoryClass());
                    this.effectiveTaskServerMode = TaskServerMode.STANDALONE;
        }
        log.info("Task service starting in " + this.getEffectiveTaskServerMode() + " mode...");
    }

    private org.wso2.micro.integrator.ntask.core.service.impl.TaskServiceXMLConfiguration loadTaskServiceXMLConfig() throws TaskException {
        String path = MicroIntegratorBaseUtils.getCarbonConfigDirPath() + File.separator + "etc"
                + File.separator + "tasks-config.xml";
        File file = new File(path);
        if (!file.exists()) {
            return null;
        }
        Document doc = org.wso2.micro.integrator.ntask.core.TaskUtils.convertToDocument(file);
        TaskUtils.secureResolveDocument(doc);
        JAXBContext ctx;
        try {
            ctx = JAXBContext.newInstance(org.wso2.micro.integrator.ntask.core.service.impl.TaskServiceXMLConfiguration.class);
            org.wso2.micro.integrator.ntask.core.service.impl.TaskServiceXMLConfiguration taskConfig = (org.wso2.micro.integrator.ntask.core.service.impl.TaskServiceXMLConfiguration) ctx
                    .createUnmarshaller().unmarshal(doc);
            return taskConfig;
        } catch (JAXBException e) {
            throw new TaskException(e.getMessage(), Code.CONFIG_ERROR, e);
        }

    }

    @Override
    public boolean isServerInit() {
        return serverInit;
    }

    public TaskManagerFactory getTaskManagerFactory() {
        return taskManagerFactory;
    }

    @Override
    public Set<String> getRegisteredTaskTypes() {
        return registeredTaskTypes;
    }

    private void initTaskManagersForType(String taskType) throws TaskException {
        if (log.isDebugEnabled()) {
            log.debug("Initializing task managers [" + taskType + "]");
        }
        List<org.wso2.micro.integrator.ntask.core.TaskManager> startupTms = this.getTaskManagerFactory()
                .getStartupSchedulingTaskManagersForType(taskType);
        for (org.wso2.micro.integrator.ntask.core.TaskManager tm : startupTms) {
            tm.initStartupTasks();
        }
    }

    @Override
    public org.wso2.micro.integrator.ntask.core.TaskManager getTaskManager(String taskType) throws TaskException {
        return this.getTaskManagerFactory().getTaskManager(
                new TaskManagerId(SUPER_TENANT_ID, taskType));
    }

    @Override
    public List<TaskManager> getAllTenantTaskManagersForType(String taskType) throws TaskException {
        return this.getTaskManagerFactory().getAllTenantTaskManagersForType(taskType);
    }

    @Override
    public synchronized void registerTaskType(String taskType) throws TaskException {
        this.registeredTaskTypes.add(taskType);
        this.processClusteredTaskTypeRegistration(taskType);
        /* if server has finished initializing, lets initialize the
         * task managers for this type */
        if (this.isServerInit()) {
            this.initTaskManagersForType(taskType);
        }
    }

    private void processClusteredTaskTypeRegistration(String taskType) throws TaskException {
        if (this.getEffectiveTaskServerMode() == TaskServerMode.CLUSTERED) {
            ClusterGroupCommunicator.getInstance(taskType).addMyselfToGroup();
        }
    }

    @Override
    public synchronized void serverInitialized() {
        try {
            this.serverInit = true;
            for (String taskType : this.getRegisteredTaskTypes()) {
                this.initTaskManagersForType(taskType);
            }
        } catch (TaskException e) {
            String msg = "Error initializing task managers: " + e.getMessage();
            log.error(msg, e);
            throw new RuntimeException(msg, e);
        }
    }

    @Override
    public TaskServiceConfiguration getServerConfiguration() {
        return taskServerConfiguration;
    }

    private class TaskServiceConfigurationImpl implements TaskServiceConfiguration {

        private TaskServerMode taskServerMode;

        private int taskServerCount = -1;

        private String taskClientDispatchAddress;

        private String remoteServerAddress;

        private String remoteServerUsername;

        private String remoteServerPassword;

        private String taskRepositoryClass;

        private String locationResolverClass;

        private Map<String, String> locationResolverProperties;

        public TaskServiceConfigurationImpl(
                org.wso2.micro.integrator.ntask.core.service.impl.TaskServiceXMLConfiguration taskXMLConfig) {
            this.processXMLConfig(taskXMLConfig);
            this.processSystemProps();
        }

        private void processXMLConfig(org.wso2.micro.integrator.ntask.core.service.impl.TaskServiceXMLConfiguration taskXMLConfig) {
            if (taskXMLConfig == null) {
                return;
            }
            this.taskClientDispatchAddress = taskXMLConfig.getTaskClientDispatchAddress();
            this.remoteServerAddress = taskXMLConfig.getRemoteServerAddress();
            this.remoteServerUsername = taskXMLConfig.getRemoteServerUsername();
            this.remoteServerPassword = taskXMLConfig.getRemoteServerPassword();
            this.taskServerMode = taskXMLConfig.getTaskServerMode();
            this.taskRepositoryClass = taskXMLConfig.getTaskRepositoryClass();
            this.taskServerCount = taskXMLConfig.getTaskServerCount();
            DefaultLocationResolver locationResolver = taskXMLConfig.getDefaultLocationResolver();
            this.locationResolverClass = locationResolver.getLocationResolverClass();
            this.locationResolverProperties = this.extractLocationResolverProperties(locationResolver);
        }

        private Map<String, String> extractLocationResolverProperties(DefaultLocationResolver locationResolver) {
            Map<String, String> result = new HashMap<String, String>();
            Property[] props = locationResolver.getProperties();
            if (props != null) {
                for (Property prop : props) {
                    result.put(prop.getName(), prop.getValue());
                }
            }
            return result;
        }

        private void processSystemProps() {
            this.taskClientDispatchAddress = returnSystemPropValueIfValid(
                    this.taskClientDispatchAddress, RemoteTaskManager.TASK_CLIENT_DISPATCH_ADDRESS);
            this.remoteServerAddress = returnSystemPropValueIfValid(this.remoteServerAddress,
                    RemoteTaskManager.REMOTE_TASK_SERVER_ADDRESS);
            this.remoteServerUsername = returnSystemPropValueIfValid(this.remoteServerUsername,
                    RemoteTaskManager.REMOTE_TASK_SERVER_USERNAME);
            this.remoteServerPassword = returnSystemPropValueIfValid(this.remoteServerPassword,
                    RemoteTaskManager.REMOTE_TASK_SERVER_PASSWORD);
            if (this.taskServerMode == null) {
                this.taskServerMode = TaskServerMode.AUTO;

            }
            if (this.taskServerCount == -1) {
                String taskServerCountStr = System.getProperty(
                        ClusterGroupCommunicator.TASK_SERVER_COUNT_SYS_PROP);
                if (taskServerCountStr != null) {
                    this.taskServerCount = Integer.parseInt(taskServerCountStr);
                } else {
                    this.taskServerCount = -1;
                }
            }
        }

        private String returnSystemPropValueIfValid(String originalValue, String sysPropName) {
            String sysPropValue = System.getProperty(sysPropName);
            if (sysPropValue != null) {
                return sysPropValue;
            } else {
                return originalValue;
            }
        }

        @Override
        public String getTaskClientDispatchAddress() {
            return taskClientDispatchAddress;
        }

        @Override
        public TaskServerMode getTaskServerMode() {
            return taskServerMode;
        }

        @Override
        public String getRemoteServerAddress() {
            return remoteServerAddress;
        }

        @Override
        public String getRemoteServerUsername() {
            return remoteServerUsername;
        }

        @Override
        public String getRemoteServerPassword() {
            return remoteServerPassword;
        }

        @Override
        public int getTaskServerCount() {
            return taskServerCount;
        }

        @Override
        public String getLocationResolverClass() {
            if (locationResolverClass == null) {
                return TaskServiceXMLConfiguration.DEFAULT_LOCATION_RESOLVER_CLASS;
            }
            return locationResolverClass;
        }

        @Override
        public String getTaskRepositoryClass() {
            return taskRepositoryClass;
        }

        @Override
        public Map<String, String> getLocationResolverProperties() {
            return locationResolverProperties;
        }

    }

    public TaskServerMode getEffectiveTaskServerMode() {
        return effectiveTaskServerMode;
    }

    @Override
    public void runAfterRegistrationActions() throws TaskException {
        if (this.getEffectiveTaskServerMode() == TaskServerMode.CLUSTERED) {
            for (String taskType : this.getRegisteredTaskTypes()) {
                ClusterGroupCommunicator.getInstance(taskType).checkServers();
            }
        }
    }

}
