/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package internal.http.api;

import org.apache.synapse.MessageContext;
import org.wso2.carbon.inbound.endpoint.internal.http.api.InternalAPIHandler;

import java.util.List;

public class SampleInternalApiHandler implements InternalAPIHandler {

    protected String name;
    public List<String> resources;
    protected String context;

    public SampleInternalApiHandler(String context) {
        this.context = context;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public Boolean invoke(MessageContext synCtx) {
        return true;
    }

    @Override
    public void setResources(List<String> resources) {
        this.resources = resources;
    }

    @Override
    public List<String> getResources() {
        return this.resources;
    }
}
