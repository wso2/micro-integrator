/*
 * Copyright (c) 2023, WSO2 LLC. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.micro.integrator.transport.handlers.requestprocessors.swagger.format;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.models.parameters.Parameter;

import java.util.Set;

public abstract class MediaTypeMixin {
    public MediaTypeMixin() {

    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public abstract Object getExample();

    public void setExample(Object example) {
        if (example != null) {
            setExampleSetFlag(true);
        }
    }

    @JsonIgnore
    abstract boolean getExampleSetFlag();

    @JsonIgnore
    public abstract void setExampleSetFlag(boolean exampleSetFlag);

    public abstract String getType();

    @JsonIgnore
    public abstract Set<String> getTypes();

    @JsonIgnore
    public boolean shouldIgnoreTypes() {
        return getType() != null;
    }

    @JsonIgnore
    public boolean shouldSerializeTypes() {
        return getTypes() != null && !shouldIgnoreTypes();
    }

    @JsonIgnore
    public abstract Parameter.StyleEnum getStyle();

    @JsonIgnore
    public abstract void setStyle(Parameter.StyleEnum style);
}
