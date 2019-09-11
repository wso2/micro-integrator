/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.micro.integrator.identity.entitlement.proxy.thrift;

import org.apache.thrift.protocol.TCompactProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.THttpClient;
import org.wso2.micro.integrator.identity.entitlement.proxy.exception.EntitlementProxyException;
import org.wso2.micro.integrator.identity.entitlement.proxy.generatedCode.AuthenticatorService;

public class Authenticator {

    private String userName;
    private String password;
    private String serverUrl;
    private String sessionId;

    public Authenticator(String userName, String password, String serverUrl) throws EntitlementProxyException {
        this.userName = userName;
        this.password = password;
        this.serverUrl = serverUrl;

        if (!authenticate()) {
            throw new EntitlementProxyException("Authentication Failed");
        }
    }

    private boolean authenticate() throws EntitlementProxyException {
        boolean isAuthenticated;
        try {
            THttpClient client = new THttpClient(serverUrl);
            TProtocol protocol = new TCompactProtocol(client);
            AuthenticatorService.Client authClient = new AuthenticatorService.Client(protocol);
            client.open();
            sessionId = authClient.authenticate(userName, password);
            client.close();
            isAuthenticated = true;
        } catch (Exception e) {
            throw new EntitlementProxyException("Error while authenticating with ThriftAuthenticator", e);
        }
        return isAuthenticated;

    }

    public String getSessionId(boolean isExpired) throws EntitlementProxyException {
        if (isExpired) {
            authenticate();
        }
        return sessionId;
    }

}