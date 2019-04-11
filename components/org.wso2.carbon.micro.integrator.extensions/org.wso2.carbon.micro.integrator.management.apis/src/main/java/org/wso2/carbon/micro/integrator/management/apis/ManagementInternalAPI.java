package org.wso2.carbon.micro.integrator.management.apis;

import org.wso2.carbon.inbound.endpoint.internal.http.api.APIResource;
import org.wso2.carbon.inbound.endpoint.internal.http.api.InternalAPI;

import static org.wso2.carbon.micro.integrator.management.apis.CONSTANTS.PREFIX_APIS;
import static org.wso2.carbon.micro.integrator.management.apis.CONSTANTS.PREFIX_CARBON_APPS;
import static org.wso2.carbon.micro.integrator.management.apis.CONSTANTS.PREFIX_ENDPOINTS;
import static org.wso2.carbon.micro.integrator.management.apis.CONSTANTS.PREFIX_INBOUND_ENDPOINTS;
import static org.wso2.carbon.micro.integrator.management.apis.CONSTANTS.PREFIX_PROXY_SERVICES;
import static org.wso2.carbon.micro.integrator.management.apis.CONSTANTS.PREFIX_SEQUENCES;
import static org.wso2.carbon.micro.integrator.management.apis.CONSTANTS.PREFIX_TASKS;
import static org.wso2.carbon.micro.integrator.management.apis.CONSTANTS.REST_API_CONTEXT;

public class ManagementInternalAPI implements InternalAPI {

    private String name;

    public APIResource[] getResources() {

        APIResource[] resources = new APIResource[7];
        resources[0] = new ApiResource(PREFIX_APIS);
        resources[1] = new EndpointResource(PREFIX_ENDPOINTS);
        resources[2] = new InboundEndpointResource(PREFIX_INBOUND_ENDPOINTS);
        resources[3] = new ProxyServiceResource(PREFIX_PROXY_SERVICES);
        resources[4] = new CarbonAppResource(PREFIX_CARBON_APPS);
        resources[5] = new TaskResource(PREFIX_TASKS);
        resources[6] = new SequenceResource(PREFIX_SEQUENCES);
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
