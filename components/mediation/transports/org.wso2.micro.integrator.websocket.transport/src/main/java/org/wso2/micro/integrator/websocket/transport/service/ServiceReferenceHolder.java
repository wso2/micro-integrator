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

package org.wso2.micro.integrator.websocket.transport.service;

import org.wso2.micro.integrator.core.services.Axis2ConfigurationContextService;

public class ServiceReferenceHolder {

    private Axis2ConfigurationContextService axis2ConfigurationContextService;

    private static final ServiceReferenceHolder INSTANCE = new ServiceReferenceHolder();

    private ServiceReferenceHolder() {
    }

    public static ServiceReferenceHolder getInstance() {
        return INSTANCE;
    }

    public Axis2ConfigurationContextService getAxis2ConfigurationContextService() {
        if (axis2ConfigurationContextService != null) {
            return axis2ConfigurationContextService;
        }
        return null;
    }

    public void setAxis2ConfigurationContextService(Axis2ConfigurationContextService axis2ConfigurationContextService) {
        this.axis2ConfigurationContextService = axis2ConfigurationContextService;
    }

}
