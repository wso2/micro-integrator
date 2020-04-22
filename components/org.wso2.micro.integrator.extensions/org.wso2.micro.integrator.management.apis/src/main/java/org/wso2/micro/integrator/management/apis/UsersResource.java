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
package org.wso2.micro.integrator.management.apis;

import com.google.gson.JsonObject;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.MessageContext;
import org.apache.synapse.commons.json.JsonUtil;
import org.apache.synapse.config.SynapseConfiguration;
import org.json.JSONObject;
import org.wso2.micro.integrator.security.user.api.UserStoreException;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static org.wso2.micro.integrator.management.apis.Constants.BAD_REQUEST;
import static org.wso2.micro.integrator.management.apis.Constants.INTERNAL_SERVER_ERROR;
import static org.wso2.micro.integrator.management.apis.Constants.IS_ADMIN;
import static org.wso2.micro.integrator.management.apis.Constants.PASSWORD;
import static org.wso2.micro.integrator.management.apis.Constants.PATTERN;
import static org.wso2.micro.integrator.management.apis.Constants.ROLE;
import static org.wso2.micro.integrator.management.apis.Constants.STATUS;
import static org.wso2.micro.integrator.management.apis.Constants.USER_ID;

/**
 * Resource for login. This obtains JWT token. This is basic auth protected.
 */
public class UsersResource extends UserResource {

    private static final Log LOG = LogFactory.getLog(UsersResource.class);

    // HTTP method types supported by the resource
    Set<String> methods;

    public UsersResource() {

        methods = new HashSet<>();
        methods.add(Constants.HTTP_GET);
        methods.add(Constants.HTTP_POST);
    }

    @Override
    public Set<String> getMethods() {
        return methods;
    }

    @Override
    public boolean invoke(MessageContext messageContext, org.apache.axis2.context.MessageContext axis2MessageContext,
                          SynapseConfiguration synapseConfiguration) {
        LOG.info("Handling request by users resource");
        String httpMethod = axis2MessageContext.getProperty(Constants.HTTP_METHOD_PROPERTY).toString();
        JSONObject response;
        try {
            switch (httpMethod) {
                case Constants.HTTP_GET: {
                    response = handleGet(messageContext);
                    break;
                }
                case Constants.HTTP_POST: {
                    response = handlePost(axis2MessageContext);
                    break;
                }
                default: {
                    response = Utils.createJsonError("Unsupported HTTP method, " + httpMethod + ". Only GET, POST, DELETE "
                            + "methods are supported", axis2MessageContext, BAD_REQUEST);
                }
            }
        } catch (UserStoreException e) {
            response = Utils.createJsonError("Error initializing the user store. Please try again later", e,
                            axis2MessageContext, INTERNAL_SERVER_ERROR);
        } catch (IOException e) {
            response = Utils.createJsonError("Error processing the request", e, axis2MessageContext, BAD_REQUEST);
        }
        axis2MessageContext.removeProperty(Constants.NO_ENTITY_BODY);
        Utils.setJsonPayLoad(axis2MessageContext, response);
        return true;
    }

    JSONObject handleGet(MessageContext messageContext) throws UserStoreException {
        LOG.debug("Handling GET");

        String searchPattern = Utils.getQueryParameter(messageContext, PATTERN);
        if (Objects.isNull(searchPattern)) {
            searchPattern = "*";
        }

        List<String> patternUsersList = Arrays.asList(getUserStore().listUsers(searchPattern, -1));

        String roleFilter = Utils.getQueryParameter(messageContext, ROLE);
        JSONObject jsonBody;
        List<String> users;
        if (Objects.isNull(roleFilter)) {
            users = patternUsersList;
        } else {
            List<String> roleUserList = Arrays.asList(getUserStore().getUserListOfRole(roleFilter));
            users = patternUsersList.stream().filter(roleUserList::contains).collect(Collectors.toList());
        }
        jsonBody = Utils.createJSONList(users.size());
        for (String user : users) {
            JSONObject apiObject = new JSONObject();
            apiObject.put(Constants.USER_ID, user);
            jsonBody.getJSONArray(Constants.LIST).put(apiObject);

        }
        return jsonBody;
    }

    private JSONObject handlePost(
            org.apache.axis2.context.MessageContext axis2MessageContext) throws UserStoreException, IOException {
        LOG.info("Handling POST");
        if (!JsonUtil.hasAJsonPayload(axis2MessageContext)) {
            return Utils.createJsonErrorObject("JSON payload is missing");
        }
        JsonObject payload = Utils.getJsonPayload(axis2MessageContext);
        if (payload.has(USER_ID) && payload.has(PASSWORD)) {
            String[] roleList = null;
            if (payload.has(IS_ADMIN)) {
                String adminRole = getRealmConfiguration().getAdminRoleName();
                roleList = new String[]{adminRole};
            }
            String user = payload.get(USER_ID).getAsString();
            getUserStore().addUser(user, payload.get(PASSWORD).getAsString(),
                                   roleList, null, null);

            JSONObject jsonBody = new JSONObject();
            jsonBody.put(USER_ID, user);
            jsonBody.put(STATUS, "Added");
            return jsonBody;
        } else {
            throw new IOException("Missing one or more of the fields, '" + USER_ID + "', '" + PASSWORD + "' in the "
                                          + "payload");
        }
    }

}
