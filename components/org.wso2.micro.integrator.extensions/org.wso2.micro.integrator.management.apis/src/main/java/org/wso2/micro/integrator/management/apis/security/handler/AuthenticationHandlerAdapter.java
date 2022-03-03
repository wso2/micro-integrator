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

package org.wso2.micro.integrator.management.apis.security.handler;

import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.MessageContext;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.wso2.micro.core.util.CarbonException;
import org.wso2.micro.integrator.management.apis.ManagementApiUndefinedException;
import org.wso2.micro.integrator.security.MicroIntegratorSecurityUtils;
import org.wso2.micro.integrator.security.user.api.UserStoreException;

import java.io.IOException;
import java.util.Map;
import java.util.Objects;
import javax.xml.stream.XMLStreamException;

import static org.wso2.micro.integrator.management.apis.Constants.USERNAME_PROPERTY;

/**
 * This class provides an abstraction for all security handlers using authentication for management api.
 */
public abstract class AuthenticationHandlerAdapter extends SecurityHandlerAdapter {

    private static final Log LOG = LogFactory.getLog(AuthenticationHandlerAdapter.class);

    public AuthenticationHandlerAdapter(String context) throws CarbonException, XMLStreamException, IOException,
            ManagementApiUndefinedException {
        super(context);
    }

    @Override
    public Boolean handle(MessageContext messageContext) {
        org.apache.axis2.context.MessageContext axis2MessageContext
                = ((Axis2MessageContext) messageContext).getAxis2MessageContext();
        Map headers = SecurityUtils.getHeaders(axis2MessageContext);
        if (Objects.nonNull(headers)) {
            if (Objects.nonNull(headers.get(HTTPConstants.HEADER_AUTHORIZATION))) {
                String authHeader = (String) headers.get(HTTPConstants.HEADER_AUTHORIZATION);
                String authHeaderToken = authHeader;
                if ((authHeader.startsWith(AuthConstants.BASIC_AUTH_HEADER_TOKEN_TYPE)) &&
                    (authHeader.length() >= (AuthConstants.BASIC_AUTH_HEADER_TOKEN_TYPE.length() + 1))) {
                    authHeaderToken =
                            authHeader.substring(AuthConstants.BASIC_AUTH_HEADER_TOKEN_TYPE.length() + 1).trim();
                } else if ((authHeader.startsWith(AuthConstants.BEARER_AUTH_HEADER_TOKEN_TYPE)) &&
                           (authHeader.length() >= (AuthConstants.BEARER_AUTH_HEADER_TOKEN_TYPE.length() + 1))) {
                    authHeaderToken =
                            authHeader.substring(AuthConstants.BEARER_AUTH_HEADER_TOKEN_TYPE.length() + 1).trim();
                } else {
                    // Other auth header types are not supported atm
                    clearHeaders(headers);
                    SecurityUtils.setStatusCode(messageContext, AuthConstants.SC_UNAUTHORIZED);
                    return false;
                }
                if (authenticate(messageContext, authHeaderToken)) {
                    return true;
                } else {
                    clearHeaders(headers);
                    SecurityUtils.setStatusCode(messageContext, AuthConstants.SC_UNAUTHORIZED);
                    return false;
                }
            } else {
                clearHeaders(headers);
                headers.put(AuthConstants.WWW_AUTHENTICATE, AuthConstants.WWW_AUTH_METHOD);
                SecurityUtils.setStatusCode(messageContext, AuthConstants.SC_UNAUTHORIZED);
                return false;
            }
        } else {
            return false;
        }
    }

    /**
     * Executes the authentication logic relevant to the handler.
     *
     * @param messageContext synapse message context
     * @param authHeaderToken encoded authorization token
     * @return Boolean authenticated
     */
    protected abstract Boolean authenticate(MessageContext messageContext, String authHeaderToken);

    /**
     * Processes authentication request with basic auth and carbon user store.
     *
     * @param messageContext synapse message context
     * @param token extracted basic auth token
     * @return if successfully authenticated
     */
    boolean processAuthRequestWithCarbonUserStore(MessageContext messageContext, String token) throws UserStoreException {

        String[] userDetails = extractDetails(token);
        if (userDetails.length == 0) {
            return false;
        }
        boolean isAuthenticated = MicroIntegratorSecurityUtils.getUserStoreManager().authenticate(userDetails[0],
                                                                                                  userDetails[1]);
        if (isAuthenticated) {
            messageContext.setProperty(USERNAME_PROPERTY, userDetails[0]);
            LOG.info("User " + userDetails[0] + " logged in successfully");
            return true;
        }
        return false;
    }

    /**
     * Processes authentication request with basic auth and in memory user store
     *
     * @param messageContext synapse message context
     * @param token          extracted basic auth token
     * @return boolean if successfully authenticated
     */
    boolean processAuthRequestWithFileBasedUserStore(MessageContext messageContext, String token) {

        String[] userDetails = extractDetails(token);
        if (userDetails.length == 0) {
            return false;
        }
        boolean isAuthenticated = FileBasedUserStoreManager.getUserStoreManager().authenticate(userDetails[0],
                userDetails[1]);
        if (isAuthenticated) {
            messageContext.setProperty(USERNAME_PROPERTY, userDetails[0]);
            LOG.info("User " + userDetails[0] + " logged in successfully");
            return true;
        }
        return false;
    }

    private String[] extractDetails(String token) {

        String decodedCredentials = new String(new Base64().decode(token.getBytes()));
        String[] usernamePasswordArray = decodedCredentials.split(":");
        if (usernamePasswordArray.length != 2) {
            return new String[] {};
        }
        return new String[] { usernamePasswordArray[0], usernamePasswordArray[1] };
    }
}
