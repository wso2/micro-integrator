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
                String credentials = authHeader.substring(6).trim();
                if (authenticate(credentials)) {
                    return true;
                } else {
                    headers.clear();
                    SecurityUtils.setStatusCode(messageContext, AuthConstants.SC_FORBIDDEN);
                    return false;
                }
            } else {
                headers.clear();
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
     * @param credentials encoded authorization token
     * @return Boolean authenticated
     */
    protected abstract Boolean authenticate(String credentials);
}
