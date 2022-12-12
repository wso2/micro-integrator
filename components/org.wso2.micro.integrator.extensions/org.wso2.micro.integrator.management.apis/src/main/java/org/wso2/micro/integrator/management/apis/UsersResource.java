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
package org.wso2.micro.integrator.management.apis;

import com.google.gson.JsonObject;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.MessageContext;
import org.apache.synapse.commons.json.JsonUtil;
import org.apache.synapse.config.SynapseConfiguration;
import org.json.JSONObject;
import org.wso2.micro.core.util.AuditLogger;
import org.wso2.micro.integrator.management.apis.security.handler.SecurityUtils;
import org.wso2.micro.integrator.security.user.api.UserStoreException;
import org.wso2.micro.integrator.security.user.api.UserStoreManager;
import org.wso2.micro.integrator.security.user.core.multiplecredentials.UserAlreadyExistsException;

import java.io.IOException;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static org.wso2.micro.integrator.management.apis.Constants.BAD_REQUEST;
import static org.wso2.micro.integrator.management.apis.Constants.DOMAIN;
import static org.wso2.micro.integrator.management.apis.Constants.INTERNAL_SERVER_ERROR;
import static org.wso2.micro.integrator.management.apis.Constants.LIST;
import static org.wso2.micro.integrator.management.apis.Constants.NOT_FOUND;
import static org.wso2.micro.integrator.management.apis.Constants.PASSWORD;
import static org.wso2.micro.integrator.management.apis.Constants.PATTERN;
import static org.wso2.micro.integrator.management.apis.Constants.ROLE;
import static org.wso2.micro.integrator.management.apis.Constants.SEARCH_KEY;
import static org.wso2.micro.integrator.management.apis.Constants.STATUS;
/**
 * Resource for a retrieving and adding users.
 * <p>
 * Handles resources in the form "management/users"
 */
public class UsersResource extends UserResource {

    private static final Log LOG = LogFactory.getLog(UsersResource.class);

    private static final String USER_ID = "userId";
    private static final String IS_ADMIN = "isAdmin";

    public UsersResource() {

        methods = new HashSet<>();
        methods.add(Constants.HTTP_GET);
        methods.add(Constants.HTTP_POST);
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
                        response = populateSearchResults(messageContext, searchKey.toLowerCase());
                    } else {
                        response = handleGet(messageContext);
                    }
                    break;
                }
                case Constants.HTTP_POST: {
                    response = handlePost(messageContext, axis2MessageContext);
                    break;
                }
                default: {
                    response = Utils.createJsonError("Unsupported HTTP method, " + httpMethod + ". Only GET and "
                                                     + "POST methods are supported",
                                                     axis2MessageContext, BAD_REQUEST);
                    break;
                }
            }
        } catch (UserStoreException e) {
            response = Utils.createJsonError("Error initializing the user store. Please try again later", e,
                                             axis2MessageContext, INTERNAL_SERVER_ERROR);
        } catch (IOException e) {
            response = Utils.createJsonError("Error processing the request", e, axis2MessageContext, BAD_REQUEST);
        } catch (ResourceNotFoundException e) {
            response = Utils.createJsonError("Requested resource not found. ", e, axis2MessageContext, NOT_FOUND);
        }
        axis2MessageContext.removeProperty(Constants.NO_ENTITY_BODY);
        Utils.setJsonPayLoad(axis2MessageContext, response);
        return true;
    }

    private static List<String> getUserResults(MessageContext messageContext) throws UserStoreException {
        String searchPattern = Utils.getQueryParameter(messageContext, PATTERN);
        if (Objects.isNull(searchPattern)) {
            searchPattern = "*";
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug("Searching for users with the pattern: " + searchPattern);
        }
        List<String> patternUsersList = Arrays.asList(Utils.getUserStore(null).listUsers(searchPattern, -1));
        if (LOG.isDebugEnabled()) {
            LOG.debug("Retrieved list of users for the pattern: ");
            patternUsersList.forEach(LOG::debug);
        }
        String roleFilter = Utils.getQueryParameter(messageContext, ROLE);
        if (LOG.isDebugEnabled()) {
            LOG.debug("Searching for users with the role: " + roleFilter);
        }
        List<String> users;
        if (Objects.isNull(roleFilter)) {
            users = patternUsersList;
        } else {
            List<String> roleUserList = Arrays.asList(Utils.getUserStore(null).getUserListOfRole(roleFilter));
            if (LOG.isDebugEnabled()) {
                LOG.debug("Retrieved list of users for the role: ");
                roleUserList.forEach(LOG::debug);
            }
            users = patternUsersList.stream().filter(roleUserList::contains).collect(Collectors.toList());
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug("Filtered list of users: ");
            users.forEach(LOG::debug);
        }
        return users;
    }

    @Override
    protected JSONObject handleGet(MessageContext messageContext) throws UserStoreException {
        return setResponseBody(getUserResults(messageContext));
    }

    protected JSONObject populateSearchResults(MessageContext messageContext, String searchKey) throws UserStoreException {

        List<String> users = getUserResults(messageContext).stream()
                .filter(name -> name.toLowerCase().contains(searchKey))
                .collect(Collectors.toList());
        return setResponseBody(users);
    }

    private JSONObject setResponseBody(List<String> users) {
        JSONObject jsonBody = Utils.createJSONList(users.size());
        for (String user : users) {
            JSONObject userObject = new JSONObject();
            userObject.put(USER_ID, user);
            jsonBody.getJSONArray(LIST).put(userObject);
        }
        return jsonBody;
    }

    private JSONObject handlePost(MessageContext messageContext,
            org.apache.axis2.context.MessageContext axis2MessageContext)
            throws UserStoreException, IOException, ResourceNotFoundException {
        if (!Utils.isUserAuthenticated(messageContext)) {
            LOG.warn("Adding a user without authenticating/authorizing the request sender. Adding "
                     + "authetication and authorization handlers is recommended.");
        }
        if (!JsonUtil.hasAJsonPayload(axis2MessageContext)) {
            return Utils.createJsonErrorObject("JSON payload is missing");
        }
        JsonObject payload = Utils.getJsonPayload(axis2MessageContext);
        boolean isAdmin = false;
        if (payload.has(USER_ID) && payload.has(PASSWORD)) {
            String[] roleList = null;
            if (payload.has(IS_ADMIN) && payload.get(IS_ADMIN).getAsBoolean()) {
                String adminRole = Utils.getRealmConfiguration().getAdminRoleName();
                roleList = new String[]{adminRole};
                isAdmin = payload.get(IS_ADMIN).getAsBoolean();
            }
            String user = payload.get(USER_ID).getAsString();
            String domain = null;
            if (payload.has(DOMAIN) ) {
                domain = payload.get(DOMAIN).getAsString();
            }
            UserStoreManager userStoreManager = Utils.getUserStore(domain);
            if (LOG.isDebugEnabled()) {
                LOG.debug("Adding user, id: " + user + ", roleList: " + Arrays.toString(roleList));
            }
            try {
                synchronized (this) {
                    userStoreManager.addUser(user, payload.get(PASSWORD).getAsString(),
                            roleList, null, null);
                }
            } catch (UserAlreadyExistsException e) {
                throw new IOException("User: " + user + " already exists.", e);
            }
            JSONObject jsonBody = new JSONObject();
            jsonBody.put(USER_ID, user);
            jsonBody.put(STATUS, "Added");
            String performedBy = Constants.ANONYMOUS_USER;
            if (messageContext.getProperty(Constants.USERNAME_PROPERTY) !=  null) {
                performedBy = messageContext.getProperty(Constants.USERNAME_PROPERTY).toString();
            }
            JSONObject info = new JSONObject();
            info.put(USER_ID, user);
            info.put(IS_ADMIN, isAdmin);
            AuditLogger.logAuditMessage(performedBy, Constants.AUDIT_LOG_TYPE_USER, Constants.AUDIT_LOG_ACTION_CREATED, info);
            return jsonBody;
        } else {
            throw new IOException("Missing one or more of the fields, '" + USER_ID + "', '" + PASSWORD + "' in the "
                                  + "payload.");
        }
    }

}
