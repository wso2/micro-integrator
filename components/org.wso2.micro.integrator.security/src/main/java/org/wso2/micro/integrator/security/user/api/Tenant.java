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

import org.wso2.micro.integrator.security.user.api.RealmConfiguration;

import java.util.Date;

/**
 * Represents a Tenant
 */
@SuppressWarnings("unused")
public class Tenant {

    /**
     * The tenant Id
     */
    private int id;

    /**
     * The domain name of the tenant
     */
    private String domain;

    /**
     * The admin user name of the tenant
     */
    private String adminName;

    /**
     * Full name of admin user
     */
    private String adminFullName;

    /**
     * First name of admin user
     */
    private String adminFirstName;

    /**
     * Last name of admin user
     */
    private String adminLastName;

    /**
     * The email address of the tenant
     */
    private String email;

    /**
     * Indicates whether this tenant is active or not
     */
    private boolean active;

    /**
     * Holds the created date
     */
    private Date createdDate;

    /**
     * Realm configuration of the tenant
     */
    private RealmConfiguration realmConfig;

    /**
     * The admin password of the tenant.
     * Used only when using as DTO strictly on Tenant additions
     */
    private String adminPassword;

    public String getAdminFullName() {
        return adminFullName;
    }

    public void setAdminFullName(String adminFullName) {
        this.adminFullName = adminFullName;
    }

    public String getAdminFirstName() {
        return adminFirstName;
    }

    public void setAdminFirstName(String adminFirstName) {
        this.adminFirstName = adminFirstName;
    }

    public String getAdminLastName() {
        return adminLastName;
    }

    public void setAdminLastName(String adminLastName) {
        this.adminLastName = adminLastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getAdminName() {
        return adminName;
    }

    public void setAdminName(String adminName) {
        this.adminName = adminName;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public String getAdminPassword() {
        return adminPassword;
    }

    public void setAdminPassword(String adminPassword) {
        this.adminPassword = adminPassword;
    }

    public Date getCreatedDate() {
        if (null != createdDate) {
            return (Date) createdDate.clone();
        } else {
            return null;
        }
    }

    public void setCreatedDate(Date createdDate) {
        if (null != createdDate) {
            this.createdDate = (Date) createdDate.clone();
        } else {
            this.createdDate = null;
        }

    }

    public RealmConfiguration getRealmConfig() {
        return realmConfig;
    }

    public void setRealmConfig(RealmConfiguration realmConfig) {
        this.realmConfig = realmConfig;
    }


}