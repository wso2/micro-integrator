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

package org.wso2.micro.integrator.security.user.core.tenant;

import org.wso2.micro.core.Constants;

import java.io.Serializable;

/**
 * Model class of Tenant Domain cache key.
 */
public class TenantDomainKey implements Serializable {

    private static final long serialVersionUID = 7375056603998213673L;
    private String tenantDomain;

    public TenantDomainKey(String tenantDomain) {

        this.tenantDomain = tenantDomain;
    }

    public String getTenantDomain() {

        return tenantDomain;
    }

    @Override
    public boolean equals(Object otherObject) {

        if (!(otherObject instanceof TenantDomainKey)) {
            return false;
        }

        TenantDomainKey secondKey = (TenantDomainKey) otherObject;

        if (tenantDomain != null && !tenantDomain.equals(secondKey.getTenantDomain())) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {

        if (tenantDomain != null) {
            return tenantDomain.hashCode();
        } else {
            return Constants.SUPER_TENANT_DOMAIN_NAME.hashCode();
        }
    }
}