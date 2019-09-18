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
package org.wso2.micro.integrator.security.user.core.common;

import org.wso2.micro.core.Constants;

import java.io.Serializable;

public class UserRolesCacheKey implements Serializable {

    private static final long serialVersionUID = 987045632165409867L;

    private String userName;
    private String serverId;
    private int tenantId;

    public UserRolesCacheKey(String serverId, int tenantId, String userName) {
        this.tenantId = tenantId;
        this.userName = userName;
        this.serverId = serverId;
    }

    public boolean equals(Object otherObject) {
        if (!(otherObject instanceof UserRolesCacheKey)) {
            return false;
        }
        UserRolesCacheKey userRolesCacheKey = (UserRolesCacheKey) otherObject;
        return checkKeyAttributesEqual(userRolesCacheKey.getServerId(),
                userRolesCacheKey.getTenantId(), userRolesCacheKey.getUserName());
    }

    public int hashCode() {
        return getAttributeHashCode();
    }

    public boolean checkKeyAttributesEqual(String serverId, int tenantId, String userName) {

        if (this.serverId != null) {
            return ((this.tenantId == tenantId) && (this.userName.equalsIgnoreCase(userName)) &&
                    (this.serverId.equalsIgnoreCase(serverId)));
        } else {
            return ((this.tenantId == tenantId) && (this.userName.equalsIgnoreCase(userName)));
        }
    }

    public int getAttributeHashCode() {

        if (this.serverId != null) {
            return ((this.tenantId == Constants.SUPER_TENANT_ID ? 0 : tenantId)
                    + this.userName.toLowerCase().hashCode() * 7) + this.serverId.hashCode() * 11;
        } else {
            return ((this.tenantId == Constants.SUPER_TENANT_ID ? 0 : tenantId)
                    + this.userName.toLowerCase().hashCode() * 7);
        }
    }

    public int getTenantId() {
        return tenantId;
    }

    public String getUserName() {
        return userName;
    }

    public String getServerId() {
        return serverId;
    }
}
