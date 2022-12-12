/*
 * Copyright (c) 2022, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.micro.integrator.management.apis;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.MessageContext;
import org.apache.synapse.commons.json.JsonUtil;
import org.apache.synapse.config.SynapseConfiguration;
import org.json.JSONObject;
import org.wso2.micro.integrator.management.apis.security.handler.SecurityUtils;
import org.wso2.micro.integrator.security.user.api.UserStoreException;
import org.wso2.micro.integrator.security.user.api.UserStoreManager;

import java.io.IOException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import static org.wso2.micro.integrator.management.apis.Constants.BAD_REQUEST;
import static org.wso2.micro.integrator.management.apis.Constants.DOMAIN;
import static org.wso2.micro.integrator.management.apis.Constants.INTERNAL_SERVER_ERROR;
import static org.wso2.micro.integrator.management.apis.Constants.LIST;
import static org.wso2.micro.integrator.management.apis.Constants.NOT_FOUND;
import static org.wso2.micro.integrator.management.apis.Constants.ROLE;
import static org.wso2.micro.integrator.management.apis.Constants.SEARCH_KEY;
import static org.wso2.micro.integrator.management.apis.Constants.STATUS;
import static org.wso2.micro.integrator.management.apis.Constants.USER_ID;

/**
 * This resource will handle requests coming to roles/.
 * Handle fetching get all roles, add new role and assigning roles to a user.
 */
public class RolesResource implements MiApiResource {

    private static final Log LOG = LogFactory.getLog(RolesResource.class);
    private static final String ROLE_LIST_ADDED = "addedRoles";
    private static final String ROLE_LIST_REMOVED = "removedRoles";

    // HTTP method types supported by the resource
    protected Set<String> methods;

    public RolesResource() {
        methods = new HashSet<>();
        methods.add(Constants.HTTP_GET);
        methods.add(Constants.HTTP_POST);
        methods.add(Constants.HTTP_PUT);
    }

    @Override
    public Set<String> getMethods() {
        return methods;
    }

    @Override
    public boolean invoke(MessageContext messageContext, org.apache.axis2.context.MessageContext axis2MessageContext,
                          SynapseConfiguration synapseConfiguration) {
        String httpMethod = axis2MessageContext.getProperty(Constants.HTTP_METHOD_PROPERTY).toString();
        if (LOG.isDebugEnabled()) {
            LOG.debug("Handling " + httpMethod + "request.");
        }

        if (Boolean.TRUE.equals(SecurityUtils.isFileBasedUserStoreEnabled())) {
            Utils.setInvalidUserStoreResponse(axis2MessageContext);
            return true;
        }

        JSONObject response;
        try {
            switch (httpMethod) {
                case Constants.HTTP_GET: {
                    String searchKey = Utils.getQueryParameter(messageContext, SEARCH_KEY);
                    if (Objects.nonNull(searchKey) && !searchKey.trim().isEmpty()) {
                        response = populateSearchResults(searchKey.toLowerCase());
                    } else {
                        response = handleGet();
                    }
                    break;
                }
                case Constants.HTTP_POST: {
                    response = handlePost(messageContext, axis2MessageContext);
                    break;
                }
                case Constants.HTTP_PUT: {
                    response = handlePut(messageContext, axis2MessageContext);
                    break;
                }
                default: {
                    response = Utils.createJsonError("Unsupported HTTP method, " + httpMethod + ". Only GET and "
                                    + "DELETE methods are supported",
                            axis2MessageContext, BAD_REQUEST);
                    break;
                }
            }
        } catch (UserStoreException e) {
            response = Utils.createJsonError("Error initializing the user store. Please try again later ", e,
                    axis2MessageContext, INTERNAL_SERVER_ERROR);
        } catch (ResourceNotFoundException e) {
            response = Utils.createJsonError("Requested resource not found. ", e, axis2MessageContext, NOT_FOUND);
        } catch (IOException e) {
            response = Utils.createJsonError("Error processing the request. ", e, axis2MessageContext, BAD_REQUEST);
        }
        axis2MessageContext.removeProperty(Constants.NO_ENTITY_BODY);
        Utils.setJsonPayLoad(axis2MessageContext, response);
        return true;
    }

    protected JSONObject handleGet() throws UserStoreException {
        String[] roles = Utils.getUserStore(null).getRoleNames();
        return setResponseBody(Arrays.asList(roles));
    }

    private JSONObject setResponseBody(List<String> roles) {

        JSONObject jsonBody = Utils.createJSONList(roles.size());
        for (String role : roles) {
            JSONObject userObject = new JSONObject();
            userObject.put(ROLE, role);
            jsonBody.getJSONArray(LIST).put(userObject);
        }
        return jsonBody;
    }

    private List<String> getSearchResults(String searchKey) throws UserStoreException {
        String[] roles = Utils.getUserStore(null).getRoleNames();
        List<String> searchResults = new ArrayList<>();

        for (String role : roles) {
            if (role.toLowerCase().contains(searchKey)) {
                searchResults.add(role);
            }
        }
        return searchResults;
    }

    protected JSONObject populateSearchResults(String searchKey) throws UserStoreException {

        List<String> roles = getSearchResults(searchKey);
        return setResponseBody(roles);
    }

    protected JSONObject handlePost(MessageContext messageContext,
                                    org.apache.axis2.context.MessageContext axis2MessageContext)
            throws UserStoreException, ResourceNotFoundException, IOException {
        if (!Utils.isUserAuthenticated(messageContext)) {
            LOG.warn("Adding a user without authenticating/authorizing the request sender. Adding "
                    + "authetication and authorization handlers is recommended.");
        }
        if (!JsonUtil.hasAJsonPayload(axis2MessageContext)) {
            return Utils.createJsonErrorObject("JSON payload is missing");
        }
        JsonObject payload = Utils.getJsonPayload(axis2MessageContext);
        String domain = null;
        if (payload.has(DOMAIN) ) {
            domain = payload.get(DOMAIN).getAsString();
        }
        UserStoreManager userStoreManager = Utils.getUserStore(domain);
        if (payload.has(ROLE)) {
            String role = payload.get(ROLE).getAsString();
            if (userStoreManager.isExistingRole(role)) {
                throw new UserStoreException("The role : " + role + " already exists");
            }
            userStoreManager.addRole(role, null, null, false);
            JSONObject roleResponse = new JSONObject();
            roleResponse.put(ROLE, role);
            roleResponse.put(STATUS, "Added");
            return roleResponse;
        } else {
            throw new IOException("Missing role name in the payload");
        }
    }

    protected JSONObject handlePut(MessageContext messageContext,
                                   org.apache.axis2.context.MessageContext axis2MessageContext)
            throws UserStoreException, ResourceNotFoundException, IOException {

        if (!Utils.isUserAuthenticated(messageContext)) {
            LOG.warn("Adding a user without authenticating/authorizing the request sender. Adding "
                    + "authetication and authorization handlers is recommended.");
        }
        if (!JsonUtil.hasAJsonPayload(axis2MessageContext)) {
            return Utils.createJsonErrorObject("JSON payload is missing");
        }
        JsonObject payload = Utils.getJsonPayload(axis2MessageContext);
        String domain = null;
        if (payload.has(DOMAIN) ) {
            domain = payload.get(DOMAIN).getAsString();
        }
        UserStoreManager userStoreManager = Utils.getUserStore(domain);
        ArrayList<String> addedRoleList = new ArrayList<>();
        ArrayList<String> removedRoleList = new ArrayList<>();
        if ((payload.has(ROLE_LIST_ADDED) || payload.has(ROLE_LIST_REMOVED)) && payload.has(USER_ID)) {
            String userId = payload.get(USER_ID).getAsString();
            if (!userStoreManager.isExistingUser(userId)) {
                throw new UserStoreException("The user : " + userId + " does not exists");
            }
            if (payload.has(ROLE_LIST_ADDED)) {
                JsonArray addedRoles = payload.getAsJsonArray(ROLE_LIST_ADDED);
                for (JsonElement roleElement : addedRoles) {
                    addedRoleList.add(roleElement.getAsString());
                }
            }
            if (payload.has(ROLE_LIST_REMOVED)) {
                JsonArray removedRoles = payload.getAsJsonArray(ROLE_LIST_REMOVED);
                for (JsonElement roleElement : removedRoles) {
                    removedRoleList.add(roleElement.getAsString());
                }
            }
            userStoreManager.updateRoleListOfUser(userId,removedRoleList.toArray(new String[0]),addedRoleList.toArray(new String[0]));
            JSONObject roleResponse = new JSONObject();
            roleResponse.put(USER_ID, userId);
            roleResponse.put(STATUS, "Added/removed the roles");
            return roleResponse;
        } else {
            throw new IOException("Missing one or more of the fields, '" + USER_ID + "', '" + ROLE_LIST_ADDED + "', '" +
                    ROLE_LIST_REMOVED + "' in the "
                    + "payload.");
        }
    }
}
