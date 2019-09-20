/**
 * Copyright (c) 2009, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.micro.integrator.initializer.services;

import org.apache.synapse.core.SynapseEnvironment;
import org.apache.axis2.context.ConfigurationContext;

public class SynapseEnvironmentServiceImpl implements SynapseEnvironmentService {

    private SynapseEnvironment synapseEnvironment;

    private ConfigurationContext configurationContext;

    private int tenantId;

    public SynapseEnvironmentServiceImpl(SynapseEnvironment synapseEnvironment, int tenantId,
                                         ConfigurationContext configCtx) {
        this.synapseEnvironment = synapseEnvironment;
        this.tenantId = tenantId;
        this.configurationContext = configCtx;
    }

    public int getTenantId() {
        return tenantId;
    }

    public SynapseEnvironment getSynapseEnvironment() {
        return synapseEnvironment;
    }

    public void setSynapseEnvironment(SynapseEnvironment synapseEnvironment) {
        this.synapseEnvironment = synapseEnvironment;
    }

    public ConfigurationContext getConfigurationContext() {
        return configurationContext;
    }

    public void setConfigurationContext(ConfigurationContext configurationContext) {
        this.configurationContext = configurationContext;
    }
}
