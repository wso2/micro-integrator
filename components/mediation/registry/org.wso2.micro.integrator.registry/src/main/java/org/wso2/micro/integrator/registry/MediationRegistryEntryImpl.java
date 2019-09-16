/**
 * Copyright (c) 2009, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.micro.integrator.registry;

import org.apache.synapse.registry.RegistryEntryImpl;

import java.util.Date;

/**
 * The Synapse {@link org.apache.synapse.registry.RegistryEntryImpl} conflicts the
 * {@link MicroIntegratorRegistryConstants#FILE} and the
 * {@link MicroIntegratorRegistryConstants#FOLDER} types because it
 * is doing a validation, so this class is to get rid of that validation;
 */
public class MediationRegistryEntryImpl extends RegistryEntryImpl {

    private String type;

    @Override
    public void setType(String type) {
        this.type = type;
    }

    @Override
    public String getType() {
        return this.type;
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("RegistryEntry {")
                .append(" Key : ").append(getKey())
                .append(" Name : ").append(getName())
                .append(" Version : ").append(getVersion())
                .append(" Type : ").append(type)
                .append(" Description : ").append(getDescription())
                .append(" Created : ").append(new Date(getCreated()))
                .append(" Modified : ").append(new Date(getLastModified()))
                .append(" Cacheable for : ").append(getCachableDuration() / 1000)
                .append("sec").append("}");
        return sb.toString();
    }
}
