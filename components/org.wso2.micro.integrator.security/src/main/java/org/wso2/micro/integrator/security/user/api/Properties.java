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

import org.wso2.micro.integrator.security.user.api.Property;

public class Properties {
    Property[] mandatoryProperties;
    Property[] optionalProperties;
    Property[] advancedProperties;

    public Property[] getMandatoryProperties() {
        return mandatoryProperties;
    }

    public void setMandatoryProperties(Property[] mandatoryProperties) {
        this.mandatoryProperties = mandatoryProperties;
    }

    public Property[] getOptionalProperties() {
        return optionalProperties;
    }

    public void setOptionalProperties(Property[] optionalProperties) {
        this.optionalProperties = optionalProperties;
    }

    public Property[] getAdvancedProperties() {
        return advancedProperties;
    }

    public void setAdvancedProperties(Property[] advancedProperties) {
        this.advancedProperties = advancedProperties;
    }
}
