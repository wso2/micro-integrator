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
package org.wso2.micro.integrator.mediator.oauth;

import java.rmi.RemoteException;

import org.apache.axis2.AxisFault;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.oauth.stub.dto.OAuthConsumerDTO;
import org.wso2.carbon.identity.oauth.stub.types.Parameters;
import org.wso2.carbon.identity.oauth.stub.OAuthServiceStub;

public class OAuthServiceClient {

    private OAuthServiceStub stub = null;
    private static final Log log = LogFactory.getLog(OAuthServiceClient.class);

    /**
     * @param backendServerURL The server URL of the WSO2 Identity Server
     * @param configCtx The configuration context instance
     * @throws Exception
     */
    public OAuthServiceClient(String backendServerURL, ConfigurationContext configCtx)
            throws Exception {
        String serviceURL = backendServerURL + "OAuthService";
        try {
            stub = new OAuthServiceStub(configCtx, serviceURL);
        } catch (AxisFault e) {
            throw new Exception("Error initializing OAuth Client", e);
        }
    }

    /**
     * @param oauthConsumer The consumer of OAuth protocol
     * @return Returns oauthConsumer is true or false
     * @throws Exception
     */
    public boolean isOAuthConsumerValid(OAuthConsumerDTO oauthConsumer) throws Exception {
        try {
            return stub.isOAuthConsumerValid(oauthConsumer);
        } catch (RemoteException e) {
            throw new Exception(
                    "Error while validating OAuth consumer credentials with 2-legged OAuth", e);
        }
    }

    /**
     * @param params Populated parameter object from the OAuth authorization header or query string
     * @return Returns authentication request is true or false
     * @throws Exception
     */
    public boolean validateAuthenticationRequest(Parameters params) throws Exception {
        try {
            stub.validateAuthenticationRequest(params);
            return true;
        } catch (RemoteException e) {
            throw new Exception(
                    "Error while validating OAuth consumer credentials with 3-legged OAuth", e);
        }
    }

}
