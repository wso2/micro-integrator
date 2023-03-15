/*
 * Copyright (c) 2023, WSO2 LLC. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.MessageContext;
import org.apache.synapse.config.SynapseConfiguration;
import org.json.JSONArray;
import org.json.JSONObject;
import org.wso2.micro.core.util.StringUtils;
import org.wso2.micro.integrator.management.apis.security.handler.SecurityUtils;
import org.wso2.micro.integrator.security.user.api.UserStoreException;
import org.wso2.micro.integrator.security.user.api.UserStoreManager;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.HashSet;
import java.util.Set;

import static org.wso2.micro.integrator.management.apis.Constants.*;
import static org.wso2.micro.integrator.management.apis.Constants.USER_ID;

/**
 * Base class for role resources.
 */
public class RoleResourceBase {

    private static final Log LOG = LogFactory.getLog(RoleResource.class);
    private static final String INTERNAL_ROLE = "internal";
    private static final String APPLICATION_ROLE = "application";
    // HTTP method types supported by the resource
    protected Set<String> methods;

    public RoleResourceBase() {
        methods = new HashSet<>();
        methods.add(Constants.HTTP_GET);
        methods.add(Constants.HTTP_DELETE);
    }

    public Set<String> getMethods() {
        return methods;
    }

    public boolean invoke(MessageContext messageContext, org.apache.axis2.context.MessageContext axis2MessageContext,
                          SynapseConfiguration synapseConfiguration, boolean isEncoded) {

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
                    response = handleGet(messageContext, isEncoded);
                    break;
                }
                case Constants.HTTP_DELETE: {
                    response = handleDelete(messageContext, isEncoded);
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
            response = Utils.createJsonError("Error initializing the user store. Please try again later", e,
                    axis2MessageContext, INTERNAL_SERVER_ERROR);
        } catch (ResourceNotFoundException e) {
            response = Utils.createJsonError("Requested resource not found. ", e, axis2MessageContext, NOT_FOUND);
        }
        axis2MessageContext.removeProperty(Constants.NO_ENTITY_BODY);
        Utils.setJsonPayLoad(axis2MessageContext, response);
        return true;
    }

    protected JSONObject handleGet(MessageContext messageContext, boolean isEncoded)
            throws UserStoreException, ResourceNotFoundException {
        String domain = Utils.getQueryParameter(messageContext, DOMAIN);
        if (isEncoded && !StringUtils.isEmpty(domain)) {
            domain = urlDecode(domain);
        }
        String roleName = Utils.getPathParameter(messageContext, ROLE);
        if (!StringUtils.isEmpty(domain)) {
            roleName = Utils.addDomainToName(roleName, domain);
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug("Requested details for the role: " + roleName);
        }
        if (!Utils.getUserStore(null).isExistingRole(roleName)) {
            throw new ResourceNotFoundException("Role: " + roleName + " cannot be found.");
        }
        JSONObject roleObject = new JSONObject();
        roleObject.put(ROLE, roleName);
        String[] users = Utils.getUserStore(null).getUserListOfRole(roleName);
        JSONArray list = new JSONArray(users);
        roleObject.put(USERS, list);
        return roleObject;
    }

    protected JSONObject handleDelete(MessageContext messageContext, boolean isEncoded)
            throws UserStoreException, ResourceNotFoundException {
        String domain = Utils.getQueryParameter(messageContext, DOMAIN);
        if (isEncoded && !StringUtils.isEmpty(domain)) {
            domain = urlDecode(domain);
        }
        String roleName = Utils.getPathParameter(messageContext, ROLE);
        String domainAwareRoleName = roleName;
        String adminRole = Utils.getRealmConfiguration().getAdminRoleName();
        if (roleName.equals(adminRole)) {
            throw new UserStoreException("Cannot remove the admin role");
        }
        if (!StringUtils.isEmpty(domain)) {
            domainAwareRoleName = Utils.addDomainToName(roleName, domain);
            // for internal or application roles fetch the primary user store
            if (INTERNAL_ROLE.equalsIgnoreCase(domain) || APPLICATION_ROLE.equalsIgnoreCase(domain)) {
                domain = null;
            }
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug("Requested details for the role: " + roleName);
        }
        UserStoreManager userStoreManager = Utils.getUserStore(domain);
        if (userStoreManager.isExistingRole(roleName)) {
            userStoreManager.deleteRole(roleName);
        } else if (userStoreManager.isExistingRole(domainAwareRoleName)) {
            userStoreManager.deleteRole(domainAwareRoleName);
        } else {
            throw new ResourceNotFoundException("Role: " + roleName + " cannot be found.");
        }
        JSONObject jsonBody = new JSONObject();
        jsonBody.put(ROLE, roleName);
        jsonBody.put(STATUS, "Deleted");
        JSONObject info = new JSONObject();
        info.put(USER_ID, roleName);
        return jsonBody;
    }

    /**
     * Decodes the given value.
     *
     * @param value Value to be decoded.
     * @return Decoded value.
     */
    private static String urlDecode(String value) {
        try {
            return URLDecoder.decode(value, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("Error while decoding the value: " + value, e);
        }
    }
}
