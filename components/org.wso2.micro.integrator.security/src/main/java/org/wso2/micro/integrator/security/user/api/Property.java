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

import java.util.Arrays;

/**
 * Represents a property in user store manager
 */
public class Property {
    private String name;
    private String value;
    private String description;
    private org.wso2.micro.integrator.security.user.api.Property[] childProperties;

    public Property(String name, String value, String description, org.wso2.micro.integrator.security.user.api.Property[] childProperties) {
        this.name = name;
        this.value = value;
        this.description = description;
        if (childProperties == null) {
            this.childProperties = new org.wso2.micro.integrator.security.user.api.Property[0];
        } else {
            this.childProperties = Arrays.copyOf(childProperties, childProperties.length);
        }
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public org.wso2.micro.integrator.security.user.api.Property[] getChildProperties() {
        return childProperties;
    }

    public void setChildProperties(org.wso2.micro.integrator.security.user.api.Property[] childProperties) {
        if (childProperties == null) {
            this.childProperties = new org.wso2.micro.integrator.security.user.api.Property[0];
        } else {
            this.childProperties = Arrays.copyOf(childProperties, childProperties.length);
        }
    }
}
