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
package org.wso2.micro.integrator.security.user.core.ldap;

import org.wso2.micro.integrator.security.user.core.common.RoleContext;

import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class LDAPRoleContext extends RoleContext {

    private String searchBase;

    private List<String> roleDNPatterns = new ArrayList<String>();

    private String searchFilter;

    private String listFilter;

    private String roleNameProperty;

    private String groupEntryObjectClass;

    // used for shared roles
    private String tenantDomain;

    public String getSearchBase() {
        return searchBase;
    }

    public void setSearchBase(String searchBase) {
        this.searchBase = searchBase;
    }

    public String getSearchFilter() {
        return searchFilter;
    }

    public void setSearchFilter(String searchFilter) {
        this.searchFilter = searchFilter;
    }

    public String getListFilter() {
        return listFilter;
    }

    public void setListFilter(String listFilter) {
        this.listFilter = listFilter;
    }

    public String getRoleNameProperty() {
        return roleNameProperty;
    }

    public void setRoleNameProperty(String roleNameProperty) {
        this.roleNameProperty = roleNameProperty;
    }

    public String getTenantDomain() {
        return tenantDomain;
    }

    public void setTenantDomain(String tenantDomain) {
        this.tenantDomain = tenantDomain;
    }

    public String getGroupEntryObjectClass() {
        return groupEntryObjectClass;
    }

    public void setGroupEntryObjectClass(String groupEntryObjectClass) {
        this.groupEntryObjectClass = groupEntryObjectClass;
    }

    public List<String> getRoleDNPatterns() {
        return roleDNPatterns;
    }

    public void addRoleDNPatterns(String roleDNPattern) {
        this.roleDNPatterns.add(roleDNPattern);
    }
}
