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

package org.wso2.micro.integrator.security.user.core.authorization;

import org.wso2.micro.core.Constants;

import java.io.Serializable;

/**
 * Date: Oct 7, 2010 Time: 11:13:54 AM
 */

/**
 * A key class which wraps a cache key used by Authorization manager.
 */
public class AuthorizationKey implements Serializable {

    private static final long serialVersionUID = 926710669453381695L;

    private String userName;

    private String resourceId;

    private String action;

    private int tenantId;

    private String serverId;

    public AuthorizationKey(String serverId, int tenantId, String userName, String resourceId, String action) {
        this.userName = userName;
        this.resourceId = resourceId;
        this.action = action;
        this.tenantId = tenantId;
        this.serverId = serverId;
    }

    @Override
    public boolean equals(Object otherObject) {

        if ((otherObject==null) || !(otherObject instanceof AuthorizationKey)) {
            return false;
        }

        AuthorizationKey secondObject = (AuthorizationKey) otherObject;

        // serverId can be null. We assume other parameters are not null.
        return checkAttributesAreEqual(this.serverId, this.tenantId, this.userName, this.resourceId,
                this.action, secondObject);
    }

    @Override
    public int hashCode() {

        return getHashCodeForAttributes(this.serverId, this.tenantId, this.userName,
                this.resourceId, this.action);
    }

    public String getUserName() {
        return userName;
    }

    public String getResourceId() {
        return resourceId;
    }

    public String getAction() {
        return action;
    }

    public int getTenantId() {
        return tenantId;
    }

    public void setTenantId(int tenantId) {
        this.tenantId = tenantId;
    }

    public String getServerId() {
        return serverId;
    }

    private int getHashCodeForAttributes(String severId, int tenantId, String userName,
                                         String resourceId, String action) {

        if ((tenantId != Constants.INVALID_TENANT_ID) && userName != null &&
                severId != null) {
            if (tenantId == Constants.SUPER_TENANT_ID) {
                tenantId = 0;
            }
            return tenantId + userName.toLowerCase().hashCode() * 5 + severId.hashCode() * 7 +
                    resourceId.hashCode() * 11 + action.hashCode() * 13;
        } else if (severId != null) {
            return severId.hashCode() * 7 + resourceId.hashCode() * 11 + action.hashCode() * 11;
        } else {
            return resourceId.hashCode() * 11 + action.hashCode() * 13;
        }
    }

    private boolean checkAttributesAreEqual(String serverId, int tenantId, String userName,
                                            String resourceIdentifier, String actionName,
                                            AuthorizationKey authorizationKey) {
        boolean equality = tenantId == authorizationKey.getTenantId() &&
                userName.equalsIgnoreCase(authorizationKey.getUserName()) &&
                resourceIdentifier.equals(authorizationKey.getResourceId()) &&
                actionName.equals(authorizationKey.getAction());
        //as server id can be null, then we skip the equality of it.
        if (serverId != null) {
            equality = equality && serverId.equals(authorizationKey.getServerId());
        }
        return equality;
    }
}
