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
import java.util.List;

import org.apache.axis2.AxisFault;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.oauth2.stub.OAuth2TokenValidationServiceStub;
import org.wso2.carbon.identity.oauth2.stub.dto.OAuth2TokenValidationRequestDTO;
import org.wso2.carbon.identity.oauth2.stub.dto.OAuth2TokenValidationRequestDTO_OAuth2AccessToken;
import org.wso2.carbon.identity.oauth2.stub.dto.OAuth2TokenValidationRequestDTO_TokenValidationContextParam;
import org.wso2.carbon.identity.oauth2.stub.dto.OAuth2TokenValidationResponseDTO;
import org.wso2.micro.integrator.core.util.MicroIntegratorBaseUtils;

public class OAuth2TokenValidationServiceClient {

    private OAuth2TokenValidationServiceStub stub = null;
    private static final Log log = LogFactory.getLog(OAuth2TokenValidationServiceClient.class);

    /**
     * OAuth2TokenValidationService Admin Service Client
     *
     * @param backendServerURL The server URL of the WSO2 Identity Server
     * @param username The user name to be used to log into the WSO2 Identity Server with admin privileges
     * @param password The password used to log into the WSO2 Identity Server with admin privileges
     * @param configCtx The configuration context instance
     * @throws Exception
     */
    public OAuth2TokenValidationServiceClient(String backendServerURL, String username, String password,
                                              ConfigurationContext configCtx) throws Exception {
        String serviceURL = backendServerURL + "OAuth2TokenValidationService";
        try {
            stub = new OAuth2TokenValidationServiceStub(configCtx, serviceURL);
            MicroIntegratorBaseUtils.setBasicAccessSecurityHeaders(username, password, true, stub._getServiceClient());
        } catch (AxisFault e) {
            throw new Exception("Error initializing OAuth Client", e);
        }
    }

    /**
     * Validates the OAuth 2.0 request
     *
     * @param accessTokenIdentifier The accessToken from the authorization header
     * @return OAuth2TokenValidationResponseDTO
     * @throws Exception
     */
    public OAuth2TokenValidationResponseDTO validateAuthenticationRequest(String accessTokenIdentifier,
                                                                          List<OAuth2TokenValidationRequestDTO_TokenValidationContextParam> params) throws Exception {

        OAuth2TokenValidationRequestDTO oauthReq = new OAuth2TokenValidationRequestDTO();
        OAuth2TokenValidationRequestDTO_OAuth2AccessToken accessToken =
                new org.wso2.carbon.identity.oauth2.stub.dto.OAuth2TokenValidationRequestDTO_OAuth2AccessToken();
        accessToken.setTokenType(OAuthConstants.BEARER_TOKEN_TYPE);
        accessToken.setIdentifier(accessTokenIdentifier);
        oauthReq.setAccessToken(accessToken);
        oauthReq.setContext(params.toArray(new OAuth2TokenValidationRequestDTO_TokenValidationContextParam[params.size()]));
        try {
            return stub.validate(oauthReq);
        } catch (RemoteException e) {
            throw new Exception("Error while validating OAuth2 request", e);
        }
    }

}
