/*
*  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.wso2.micro.integrator.initializer.services;

import org.osgi.framework.ServiceRegistration;
import org.apache.axis2.context.ConfigurationContext;

/**
 * 
 */
public class SynapseRegistrationsServiceImpl implements SynapseRegistrationsService {

    private ServiceRegistration synapseConfigurationServiceRegistration;
    private ServiceRegistration synapseEnvironmentServiceRegistration;

    private int tenantId;

    private ConfigurationContext configCtx;

    public SynapseRegistrationsServiceImpl(ServiceRegistration synCfgSvcReg,
                                           ServiceRegistration synEnvSvcReg,
                                           int tenantId,
                                           ConfigurationContext configCtx) {
        this.synapseConfigurationServiceRegistration = synCfgSvcReg;
        this.synapseEnvironmentServiceRegistration = synEnvSvcReg;
        this.tenantId = tenantId;
        this.configCtx = configCtx;
    }

    public ServiceRegistration getSynapseConfigurationServiceRegistration() {
        return synapseConfigurationServiceRegistration;
    }

    public ServiceRegistration getSynapseEnvironmentServiceRegistration() {
        return synapseEnvironmentServiceRegistration;
    }

    public int getTenantId() {
        return tenantId;
    }

    public ConfigurationContext getConfigurationContext() {
        return configCtx;
    }
}
