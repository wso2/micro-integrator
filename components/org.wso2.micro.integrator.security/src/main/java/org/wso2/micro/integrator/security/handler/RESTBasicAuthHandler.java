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

package org.wso2.micro.integrator.security.handler;

import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.MessageContext;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.apache.synapse.core.axis2.Axis2Sender;
import org.apache.synapse.rest.Handler;
import org.wso2.micro.integrator.security.MicroIntegratorSecurityUtils;
import org.wso2.micro.integrator.security.user.api.UserStoreException;
import org.wso2.micro.integrator.security.user.api.UserStoreManager;

import java.util.Map;

/**
 * This class can be added as a handler to enforce Basic Auth
 */
public class RESTBasicAuthHandler implements Handler {

    private static final Log log = LogFactory.getLog(RESTBasicAuthHandler.class);
    private static final String AUTH_FAILED_MESSAGE = "Authentication failed. ";

    @Override
    public boolean handleRequest(MessageContext messageContext) {
        org.apache.axis2.context.MessageContext axis2MessageContext
                = ((Axis2MessageContext) messageContext).getAxis2MessageContext();
        Object headers = axis2MessageContext.getProperty(
                org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS);

        if (headers != null && headers instanceof Map) {
            Map headersMap = (Map) headers;
            if (headersMap.get(HTTPConstants.HEADER_AUTHORIZATION) == null) {
                log.error(AUTH_FAILED_MESSAGE + HTTPConstants.HEADER_AUTHORIZATION + " header does not exist.");
                headersMap.clear();
                axis2MessageContext.setProperty(BasicAuthConstants.HTTP_STATUS_CODE, BasicAuthConstants.SC_UNAUTHORIZED);
                headersMap.put(BasicAuthConstants.WWW_AUTHENTICATE, BasicAuthConstants.WWW_AUTH_METHOD);
                axis2MessageContext.setProperty(BasicAuthConstants.NO_ENTITY_BODY, true);
                messageContext.setProperty(BasicAuthConstants.RESPONSE, BasicAuthConstants.TRUE);
                messageContext.setTo(null);
                Axis2Sender.sendBack(messageContext);
                return false;

            } else {
                String authHeader = (String) headersMap.get(HTTPConstants.HEADER_AUTHORIZATION);
                String credentials = authHeader.substring(6).trim();
                if (processSecurity(credentials)) {
                    return true;
                } else {
                    log.error(AUTH_FAILED_MESSAGE + "Invalid username or password has been provided.");
                    headersMap.clear();
                    axis2MessageContext.setProperty(BasicAuthConstants.HTTP_STATUS_CODE, BasicAuthConstants.SC_FORBIDDEN);
                    axis2MessageContext.setProperty(BasicAuthConstants.NO_ENTITY_BODY, true);
                    messageContext.setProperty(BasicAuthConstants.RESPONSE, BasicAuthConstants.TRUE);
                    messageContext.setTo(null);
                    Axis2Sender.sendBack(messageContext);
                    return false;
                }
            }
        }
        log.error(AUTH_FAILED_MESSAGE + "Could not authenticate due to missing headers in request.");
        return false;
    }

    @Override
    public boolean handleResponse(MessageContext messageContext) {
        return true;
    }

    @Override
    public void addProperty(String s, Object o) {
    }

    @Override
    public Map getProperties() {
        return null;
    }

    /**
     * This method authenticates credentials
     *
     * @param credentials credentials The Basic Auth credentials of the request
     * @return true if the credentials are authenticated successfully
     */
    public boolean processSecurity(String credentials) {
        String decodedCredentials = new String(new Base64().decode(credentials.getBytes()));
        String username = decodedCredentials.split(":")[0];
        String password = decodedCredentials.split(":")[1];
        UserStoreManager userStoreManager;
        try {
            userStoreManager = MicroIntegratorSecurityUtils.getUserStoreManager();
        } catch (UserStoreException e) {
            log.error("Error occurred while retrieving User Store Manager", e);
            return false;
        }
        try {
            return userStoreManager.authenticate(username, password);
        } catch (UserStoreException e) {
            log.error("Error in authenticating user", e);
            return false;
        }
    }
}
