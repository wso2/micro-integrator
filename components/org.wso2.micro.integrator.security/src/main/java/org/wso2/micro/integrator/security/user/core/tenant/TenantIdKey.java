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


/**
 * Date: Oct 1, 2010 Time: 3:22:01 PM
 */

import java.io.Serializable;

/**
 * Id class for tenant cache.
 */
public class TenantIdKey implements Serializable {

    private static final long serialVersionUID = 5955432981622647262L;
    private int tenantId;

    public TenantIdKey(int tenantId) {
        this.tenantId = tenantId;
    }

    public int getTenantId() {
        return tenantId;
    }

    @Override
    public boolean equals(Object otherObject) {

        if (!(otherObject instanceof TenantIdKey)) {
            return false;
        }

        TenantIdKey secondKey = (TenantIdKey) otherObject;

        return this.getTenantId() == secondKey.getTenantId();
    }

    @Override
    public int hashCode() {
        return this.getTenantId();
    }
}