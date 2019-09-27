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

package org.wso2.micro.integrator.management.apis.security.handler;

import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.synapse.MessageContext;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.apache.synapse.rest.RESTConstants;
import org.wso2.micro.integrator.inbound.endpoint.internal.http.api.InternalAPIHandler;

import java.util.Map;
import java.util.Objects;

/**
 * This class provides an abstraction for all security handlers using basic authentication for management api.
 */
public abstract class SecurityHandlerAdapter implements InternalAPIHandler {

    @Override
    public Boolean invoke(MessageContext messageContext) {

        org.apache.axis2.context.MessageContext axis2MessageContext
                = ((Axis2MessageContext) messageContext).getAxis2MessageContext();
        Map headers = SecurityUtils.getHeaders(axis2MessageContext);
        if (Objects.nonNull(headers)) {
            if (Objects.nonNull(headers.get(HTTPConstants.HEADER_AUTHORIZATION))) {
                String authHeader = (String) headers.get(HTTPConstants.HEADER_AUTHORIZATION);
                String authHeaderToken = authHeader;
                if (authHeader.startsWith(AuthConstants.BASIC_AUTH_HEADER_TOKEN_TYPE)) {
                    authHeaderToken = authHeader.substring(AuthConstants.BASIC_AUTH_HEADER_TOKEN_TYPE.length() + 1).trim();
                } else if (authHeader.startsWith(AuthConstants.BEARER_AUTH_HEADER_TOKEN_TYPE)) {
                    authHeaderToken = authHeader.substring(AuthConstants.BEARER_AUTH_HEADER_TOKEN_TYPE.length() + 1).trim();
                } else {
                    // Other auth header types are not supported atm
                    return false;
                }
                if (authenticate(authHeaderToken)) {
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
     * @param authHeaderToken encoded authorization token
     * @return Boolean authenticated
     */
    protected abstract Boolean authenticate(String authHeaderToken);

    /**
     * Clear headers map preserving cors headers
     * @param headers msg ctx headers map
     * @return cors headers preserved header map
     */
    public Map clearHeaders(Map headers) {

        Object allowOriginCorsHeader = headers.get(RESTConstants.CORS_HEADER_ACCESS_CTL_ALLOW_ORIGIN);
        Object allowMethodsCorsHeader = headers.get(RESTConstants.CORS_HEADER_ACCESS_CTL_ALLOW_METHODS);
        Object allowHeadersCorsHeader = headers.get(RESTConstants.CORS_HEADER_ACCESS_CTL_ALLOW_HEADERS);
        headers.clear();
        if (allowOriginCorsHeader != null) {
            headers.put(RESTConstants.CORS_HEADER_ACCESS_CTL_ALLOW_ORIGIN, allowOriginCorsHeader);
        }
        if (allowMethodsCorsHeader != null) {
            headers.put(RESTConstants.CORS_HEADER_ACCESS_CTL_ALLOW_METHODS, allowMethodsCorsHeader);
        }
        if (allowHeadersCorsHeader != null) {
            headers.put(RESTConstants.CORS_HEADER_ACCESS_CTL_ALLOW_HEADERS, allowHeadersCorsHeader);
        }
        return headers;
    }

}
