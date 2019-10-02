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

package org.wso2.carbon.inbound.endpoint.osgi.service;

import org.wso2.micro.integrator.core.services.Axis2ConfigurationContextService;

public class ServiceReferenceHolder {

    private Axis2ConfigurationContextService configurationContextService;

    private static final ServiceReferenceHolder INSTANCE = new ServiceReferenceHolder();

    private ServiceReferenceHolder() {
    }

    public static ServiceReferenceHolder getInstance() {
        return INSTANCE;
    }

    public Axis2ConfigurationContextService getConfigurationContextService() {
        if (configurationContextService != null) {
            return configurationContextService;
        }
        return null;
    }

    public void setConfigurationContextService(Axis2ConfigurationContextService configurationContextService) {
        this.configurationContextService = configurationContextService;
    }

}
