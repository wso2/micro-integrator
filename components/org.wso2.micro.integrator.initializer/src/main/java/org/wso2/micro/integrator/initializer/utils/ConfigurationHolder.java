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

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.wso2.micro.integrator.core.services.CarbonServerConfigurationService;

import java.util.Map;
import java.util.HashMap;

public class ConfigurationHolder {
    private static ConfigurationHolder ourInstance = new ConfigurationHolder();

    private BundleContext bundleContext;
    private CarbonServerConfigurationService carbonServerConfigurationService;

    private Map<Integer, ServiceRegistration> synapseRegistrations =
            new HashMap<Integer, ServiceRegistration>(); 

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
}
