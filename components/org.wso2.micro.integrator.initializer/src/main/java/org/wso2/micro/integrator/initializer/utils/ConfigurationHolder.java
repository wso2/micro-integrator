/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.micro.integrator.initializer.utils;

import org.apache.synapse.config.SynapseConfiguration;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.wso2.micro.integrator.core.services.Axis2ConfigurationContextService;
import org.wso2.micro.integrator.core.services.CarbonServerConfigurationService;
import org.wso2.micro.integrator.initializer.services.SynapseConfigurationService;
import org.wso2.micro.integrator.initializer.services.SynapseEnvironmentService;
import org.wso2.micro.integrator.initializer.services.SynapseRegistrationsService;

import java.util.Map;
import java.util.HashMap;

public class ConfigurationHolder {
    private static ConfigurationHolder ourInstance = new ConfigurationHolder();

    private BundleContext bundleContext;
    private CarbonServerConfigurationService carbonServerConfigurationService;
    private Axis2ConfigurationContextService axis2ConfigurationContextService;
    private SynapseConfigurationService synapseConfigurationService;
    private SynapseEnvironmentService synapseEnvironmentService;
    private SynapseRegistrationsService synapseRegistrationsService;
    private ConfigurationAdmin configAdminService;

    private Map<Integer, ServiceRegistration> synapseRegistrations =
            new HashMap<Integer, ServiceRegistration>();

    private Map<Integer, SynapseConfiguration> synapseConfigurationHashMap = new HashMap<>();

    public static ConfigurationHolder getInstance() {
        return ourInstance;
    }

    private ConfigurationHolder() {
    }

    public BundleContext getBundleContext() {
        return bundleContext;
    }

    public void setBundleContext(BundleContext bundleContext) {
        this.bundleContext = bundleContext;
    }

    public void addSynapseRegistration(int tenanId, ServiceRegistration serviceRegistration) {
        synapseRegistrations.put(tenanId, serviceRegistration);
    }

    public ServiceRegistration getSynapseRegistration(int tenantId) {
        return synapseRegistrations.get(tenantId);
    }

    public CarbonServerConfigurationService getCarbonServerConfigurationService() {
        return carbonServerConfigurationService;
    }

    public void setCarbonServerConfigurationService(CarbonServerConfigurationService carbonServerConfigurationService) {
        this.carbonServerConfigurationService = carbonServerConfigurationService;
    }

    public Axis2ConfigurationContextService getAxis2ConfigurationContextService() {
        return this.axis2ConfigurationContextService;
    }

    public void setAxis2ConfigurationContextService(Axis2ConfigurationContextService axis2ConfigurationContextService) {
        this.axis2ConfigurationContextService = axis2ConfigurationContextService;
    }

    public SynapseConfigurationService getSynapseConfigurationService() {
        return synapseConfigurationService;
    }

    public void setSynapseConfigurationService(SynapseConfigurationService synapseConfigurationService) {
        this.synapseConfigurationService = synapseConfigurationService;
    }

    public SynapseEnvironmentService getSynapseEnvironmentService() {
        return synapseEnvironmentService;
    }

    public void setSynapseEnvironmentService(SynapseEnvironmentService synapseEnvironmentService) {
        this.synapseEnvironmentService = synapseEnvironmentService;
    }

    public SynapseRegistrationsService getSynapseRegistrationsService() {
        return synapseRegistrationsService;
    }

    public void setSynapseRegistrationsService(SynapseRegistrationsService synapseRegistrationsService) {
        this.synapseRegistrationsService = synapseRegistrationsService;
    }

    public SynapseConfiguration getSynapseConfiguration(int tenantId) {
        return synapseConfigurationHashMap.get(tenantId);
    }

    public void addSynapseConfiguration(int tenanId, SynapseConfiguration synapseConfiguration) {
        synapseConfigurationHashMap.put(tenanId, synapseConfiguration);
    }

    public void setConfigAdminService(ConfigurationAdmin configAdminService) {
        this.configAdminService = configAdminService;
    }

    public ConfigurationAdmin getConfigAdminService() {
        return configAdminService;
    }
}
