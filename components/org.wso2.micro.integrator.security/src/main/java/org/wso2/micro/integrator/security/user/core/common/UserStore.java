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

import org.wso2.micro.integrator.security.user.core.UserStoreManager;

public class UserStore {

    private UserStoreManager userStoreManager;

    private String domainAwareName;

    private String domainFreeName;

    private String domainName;

    private boolean recurssive;

    private boolean hybridRole;

    private boolean systemStore;


    public boolean isHybridRole() {
        return hybridRole;
    }

    public void setHybridRole(boolean hybridRole) {
        this.hybridRole = hybridRole;
    }

    public boolean isRecurssive() {
        return recurssive;
    }

    public void setRecurssive(boolean recurssive) {
        this.recurssive = recurssive;
    }

    public UserStoreManager getUserStoreManager() {
        return userStoreManager;
    }

    public void setUserStoreManager(UserStoreManager userStoreManager) {
        this.userStoreManager = userStoreManager;
    }

    public String getDomainAwareName() {
        return domainAwareName;
    }

    public void setDomainAwareName(String domainAwareName) {
        this.domainAwareName = domainAwareName;
    }

    public String getDomainFreeName() {
        return domainFreeName;
    }

    public void setDomainFreeName(String domainFreeName) {
        this.domainFreeName = domainFreeName;
    }

    public String getDomainName() {
        return domainName;
    }

    public void setDomainName(String domainName) {
        this.domainName = domainName;
    }

    public boolean isSystemStore() {
        return systemStore;
    }

    public void setSystemStore(boolean systemStore) {
        this.systemStore = systemStore;
    }
}
