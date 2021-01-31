/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
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
package org.wso2.micro.integrator.observability.metric.publisher;


import java.util.List;

import org.apache.synapse.api.cors.CORSConfiguration;
import org.wso2.carbon.inbound.endpoint.internal.http.api.APIResource;
import org.wso2.carbon.inbound.endpoint.internal.http.api.InternalAPI;
import org.wso2.carbon.inbound.endpoint.internal.http.api.InternalAPIHandler;

public class MetricAPI implements InternalAPI {

    private String name;
    private List<InternalAPIHandler> handlerList = null;
    private CORSConfiguration apiCORSConfiguration = null;

    @Override
    public APIResource[] getResources() {

        APIResource[] resources = new APIResource[1];
        resources[0] = new MetricResource("/metrics");
        return resources;
    }

    @Override
    public String getContext() {
        return "/metric-service";
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
    public void setHandlers(List<InternalAPIHandler> handlerList) {
        this.handlerList = handlerList;
    }

    @Override
    public List<InternalAPIHandler> getHandlers() {
        return this.handlerList;
    }

    @Override
    public void setCORSConfiguration(CORSConfiguration corsConfiguration) {
        apiCORSConfiguration = corsConfiguration;
    }

    @Override
    public CORSConfiguration getCORSConfiguration() {
        return  apiCORSConfiguration;
    }
}
