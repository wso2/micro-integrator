/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.micro.integrator.security.user.api;

import java.util.HashMap;
import java.util.Map;

/**
 * Class that represents the tenant-mgt.xml
 */
public class TenantMgtConfiguration {

    protected String tenantManagerClass = null;
    protected Map<String, String> tenantStoreProperties = new HashMap<String, String>();

    public String getTenantManagerClass() {
        return tenantManagerClass;
    }

    public void setTenantManagerClass(String tenantManagerClass) {
        this.tenantManagerClass = tenantManagerClass;
    }

    public Map<String, String> getTenantStoreProperties() {
        return tenantStoreProperties;
    }

    public void setTenantStoreProperties(Map<String, String> tenantStoreProperties) {
        this.tenantStoreProperties = tenantStoreProperties;
    }

    public String getTenantStoreProperty(String propertyName) {
        return tenantStoreProperties.get(propertyName);
    }

}
