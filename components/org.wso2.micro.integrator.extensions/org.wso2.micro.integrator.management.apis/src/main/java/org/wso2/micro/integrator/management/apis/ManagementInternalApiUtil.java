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

package org.wso2.micro.integrator.management.apis;

import org.wso2.carbon.inbound.endpoint.internal.http.api.APIResource;
import org.wso2.micro.core.util.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static org.wso2.micro.integrator.management.apis.Constants.PATH_PARAM_CARBON_APP_NAME;
import static org.wso2.micro.integrator.management.apis.Constants.PATH_PARAM_EXTERNAL_VAULT_NAME;
import static org.wso2.micro.integrator.management.apis.Constants.PATH_PARAM_ROLE;
import static org.wso2.micro.integrator.management.apis.Constants.PATH_PARAM_TRANSACTION;
import static org.wso2.micro.integrator.management.apis.Constants.PATH_PARAM_USER;
import static org.wso2.micro.integrator.management.apis.Constants.PREFIX_APIS;
import static org.wso2.micro.integrator.management.apis.Constants.PREFIX_CARBON_APPS;
import static org.wso2.micro.integrator.management.apis.Constants.PREFIX_CONNECTORS;
import static org.wso2.micro.integrator.management.apis.Constants.PREFIX_DATA_SERVICES;
import static org.wso2.micro.integrator.management.apis.Constants.PREFIX_DATA_SOURCES;
import static org.wso2.micro.integrator.management.apis.Constants.PREFIX_ENDPOINTS;
import static org.wso2.micro.integrator.management.apis.Constants.PREFIX_EXTERNAL_VAULTS;
import static org.wso2.micro.integrator.management.apis.Constants.PREFIX_INBOUND_ENDPOINTS;
import static org.wso2.micro.integrator.management.apis.Constants.PREFIX_LOCAL_ENTRIES;
import static org.wso2.micro.integrator.management.apis.Constants.PREFIX_LOGGING;
import static org.wso2.micro.integrator.management.apis.Constants.PREFIX_LOGIN;
import static org.wso2.micro.integrator.management.apis.Constants.PREFIX_LOGOUT;
import static org.wso2.micro.integrator.management.apis.Constants.PREFIX_LOG_FILES;
import static org.wso2.micro.integrator.management.apis.Constants.PREFIX_MESSAGE_PROCESSORS;
import static org.wso2.micro.integrator.management.apis.Constants.PREFIX_MESSAGE_STORE;
import static org.wso2.micro.integrator.management.apis.Constants.PREFIX_PROXY_SERVICES;
import static org.wso2.micro.integrator.management.apis.Constants.PREFIX_ROLES;
import static org.wso2.micro.integrator.management.apis.Constants.PREFIX_SEQUENCES;
import static org.wso2.micro.integrator.management.apis.Constants.PREFIX_SERVER_DATA;
import static org.wso2.micro.integrator.management.apis.Constants.PREFIX_TASKS;
import static org.wso2.micro.integrator.management.apis.Constants.PREFIX_TEMPLATES;
import static org.wso2.micro.integrator.management.apis.Constants.PREFIX_TRANSACTION;
import static org.wso2.micro.integrator.management.apis.Constants.PREFIX_USERS;
import static org.wso2.micro.integrator.management.apis.Constants.REST_API_CONTEXT;
import static org.wso2.micro.integrator.management.apis.Constants.ROOT_CONTEXT;

/**
 * Util class for Management Internal API.
 */
public class ManagementInternalApiUtil {

    private static Map<String,APIResource[]> resourcesMap;

    public static void populateResourceMap() {
        ArrayList<APIResource> resourcesList = new ArrayList<>();
        resourcesList.add(new ApiResourceAdapter(ROOT_CONTEXT, new RootResource()));
        resourcesList.add(new ApiResource(PREFIX_APIS));
        resourcesList.add(new ApiResourceAdapter(PREFIX_ENDPOINTS, new EndpointResource()));
        resourcesList.add(new InboundEndpointResource(PREFIX_INBOUND_ENDPOINTS));
        resourcesList.add(new ProxyServiceResource(PREFIX_PROXY_SERVICES));
        resourcesList.add(new CarbonAppResource(PREFIX_CARBON_APPS));
        resourcesList.add(new CarbonAppResource(PREFIX_CARBON_APPS + PATH_PARAM_CARBON_APP_NAME));
        resourcesList.add(new TaskResource(PREFIX_TASKS));
        resourcesList.add(new SequenceResource(PREFIX_SEQUENCES));
        resourcesList.add(new DataServiceResource(PREFIX_DATA_SERVICES));
        resourcesList.add(new TemplateResource(PREFIX_TEMPLATES));
        resourcesList.add(new LoggingResource(PREFIX_LOGGING));
        resourcesList.add(new ApiResourceAdapter(PREFIX_MESSAGE_STORE, new MessageStoreResource()));
        resourcesList.add(new MessageProcessorResource(PREFIX_MESSAGE_PROCESSORS));
        resourcesList.add(new ApiResourceAdapter(PREFIX_LOCAL_ENTRIES, new LocalEntryResource()));
        resourcesList.add(new ApiResourceAdapter(PREFIX_CONNECTORS, new ConnectorResource()));
        resourcesList.add(new ApiResourceAdapter(PREFIX_LOGIN, new LoginResource()));
        resourcesList.add(new ApiResourceAdapter(PREFIX_USERS, new UsersResource()));
        resourcesList.add(new ApiResourceAdapter(PREFIX_LOGOUT, new LogoutResource()));
        resourcesList.add(new ApiResourceAdapter(PREFIX_SERVER_DATA, new MetaDataResource()));
        resourcesList.add(new LogFilesResource(PREFIX_LOG_FILES));
        resourcesList.add(new ApiResourceAdapter(PREFIX_TRANSACTION + PATH_PARAM_TRANSACTION,
                new RequestCountResource()));
        resourcesList.add(new ExternalVaultResource(PREFIX_EXTERNAL_VAULTS
                + PATH_PARAM_EXTERNAL_VAULT_NAME));
        resourcesList.add(new ApiResourceAdapter(PREFIX_DATA_SOURCES, new DataSourceResource()));
        resourcesList.add(new ApiResourceAdapter(PREFIX_ROLES, new RolesResource()));

        // take a copy before adding resources for new versions
        ArrayList<APIResource> resourcesListV1_1 = new ArrayList<>(resourcesList);
        resourcesListV1_1.add(new ApiResourceAdapter(PREFIX_USERS + PATH_PARAM_USER, new UserResourceV1_1()));
        resourcesList.add(new ApiResourceAdapter(PREFIX_USERS + PATH_PARAM_USER, new UserResource()));

        resourcesListV1_1.add(new ApiResourceAdapter(PREFIX_ROLES + PATH_PARAM_ROLE, new RoleResourceV1_1()));
        resourcesList.add(new ApiResourceAdapter(PREFIX_ROLES + PATH_PARAM_ROLE, new RoleResource()));

        APIResource[] resources = new APIResource[resourcesList.size()];
        resources = resourcesList.toArray(resources);
        APIResource[] resourcesV1_1 = new APIResource[resourcesListV1_1.size()];
        resourcesV1_1 = resourcesListV1_1.toArray(resourcesV1_1);

        resourcesMap.put("1.0", resources);
        resourcesMap.put("1.1", resourcesV1_1);
    }

    private static APIResource[] populateResources(String version) {
        if (resourcesMap == null) {
            resourcesMap = new HashMap<>();
            populateResourceMap();
        }
        if (StringUtils.isEmpty(version)) {
            return resourcesMap.get("1.0");
        } else {
            return resourcesMap.get(version);
        }
    }

    public static APIResource[] getResources(String version) {
        return populateResources(version);
    }

    public static String getContext(String version) {
        if (!StringUtils.isEmpty(version)) {
            return REST_API_CONTEXT + "/" + version;
        }
        return REST_API_CONTEXT;
    }

}
