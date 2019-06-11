/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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


package org.wso2.carbon.micro.integrator.management.apis;

import java.util.ArrayList;
import org.wso2.carbon.inbound.endpoint.internal.http.api.APIResource;
import org.wso2.carbon.inbound.endpoint.internal.http.api.InternalAPI;

import static org.wso2.carbon.micro.integrator.management.apis.Constants.PREFIX_APIS;
import static org.wso2.carbon.micro.integrator.management.apis.Constants.PREFIX_CARBON_APPS;
import static org.wso2.carbon.micro.integrator.management.apis.Constants.PREFIX_DATA_SERVICES;
import static org.wso2.carbon.micro.integrator.management.apis.Constants.PREFIX_ENDPOINTS;
import static org.wso2.carbon.micro.integrator.management.apis.Constants.PREFIX_INBOUND_ENDPOINTS;
import static org.wso2.carbon.micro.integrator.management.apis.Constants.PREFIX_PROXY_SERVICES;
import static org.wso2.carbon.micro.integrator.management.apis.Constants.PREFIX_SEQUENCES;
import static org.wso2.carbon.micro.integrator.management.apis.Constants.PREFIX_TASKS;
import static org.wso2.carbon.micro.integrator.management.apis.Constants.PREFIX_TEMPLATES;
import static org.wso2.carbon.micro.integrator.management.apis.Constants.REST_API_CONTEXT;

public class ManagementInternalAPI implements InternalAPI {

    private APIResource[] resources;

    public ManagementInternalAPI() {
        ArrayList<APIResource> resourcesList = new ArrayList<>();
        resourcesList.add(new ApiResource(PREFIX_APIS));
        resourcesList.add(new EndpointResource(PREFIX_ENDPOINTS));
        resourcesList.add(new InboundEndpointResource(PREFIX_INBOUND_ENDPOINTS));
        resourcesList.add(new ProxyServiceResource(PREFIX_PROXY_SERVICES));
        resourcesList.add(new CarbonAppResource(PREFIX_CARBON_APPS));
        resourcesList.add(new TaskResource(PREFIX_TASKS));
        resourcesList.add(new SequenceResource(PREFIX_SEQUENCES));
        resourcesList.add(new DataServiceResource(PREFIX_DATA_SERVICES));
        resourcesList.add(new TemplateResource(PREFIX_TEMPLATES));

        resources = new APIResource[resourcesList.size()];
        resources = resourcesList.toArray(resources);
    }

    private String name;

    public APIResource[] getResources() {
        return resources;
    }

    public String getContext() {
        return REST_API_CONTEXT;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
