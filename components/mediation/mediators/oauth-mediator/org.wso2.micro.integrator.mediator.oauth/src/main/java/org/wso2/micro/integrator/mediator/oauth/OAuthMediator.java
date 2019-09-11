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

import java.lang.String;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.axis2.AxisFault;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.MessageContext;
import org.apache.synapse.SynapseException;
import org.apache.synapse.core.SynapseEnvironment;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.apache.synapse.mediators.AbstractMediator;
import org.apache.synapse.transport.nhttp.NhttpConstants;
import org.wso2.carbon.identity.oauth.stub.dto.OAuthConsumerDTO;
import org.wso2.carbon.identity.oauth.stub.types.Parameters;
import org.wso2.carbon.identity.oauth2.stub.dto.OAuth2TokenValidationRequestDTO_TokenValidationContextParam;
import org.wso2.carbon.identity.oauth2.stub.dto.OAuth2TokenValidationResponseDTO;

public class OAuthMediator extends AbstractMediator {

    private static final Log log = LogFactory.getLog(OAuthMediator.class);

    // The server URL of the WSO2 Identity Server
    private String remoteServiceUrl;
    // The username and password used to log in to WSO2 Identity Server with admin privileges
    private String username;
    private String password;

    ConfigurationContext cfgCtx = null;
    private String clientRepository = null;
    private String axis2xml = null;
    public final static String DEFAULT_CLIENT_REPO = "./samples/axis2Client/client_repo";
    public final static String DEFAULT_AXIS2_XML = "./samples/axis2Client/client_repo/conf/axis2.xml";

    /**
     * {@inheritDoc}
     */
    public void init(SynapseEnvironment synEnv) {
        try {
            cfgCtx = ConfigurationContextFactory.createConfigurationContextFromFileSystem(clientRepository != null
                    ? clientRepository : DEFAULT_CLIENT_REPO, axis2xml != null ? axis2xml : DEFAULT_AXIS2_XML);
        } catch (AxisFault e) {
            String msg = "Error initializing OAuth mediator : " + e.getMessage();
            throw new SynapseException(msg, e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean mediate(MessageContext synCtx) {

        if (synCtx.getEnvironment().isDebuggerEnabled()) {
            if (super.divertMediationRoute(synCtx)) {
                return true;
            }
        }

        // checks if the message carries OAuth params
        boolean isOauth2 = validateRequest(synCtx);

        if (isOauth2) {
            return handleOAuth2(synCtx);
        } else {
            return handleOAuth1a(synCtx);
        }
    }

    /**
     * Checks if the message contains Authorization header or query strings
     *
     * @param synCtx
     * @return
     */
    private boolean validateRequest(MessageContext synCtx) {

        boolean isOauth2 = false;
        String accessToken = null;

        org.apache.axis2.context.MessageContext msgContext = ((Axis2MessageContext) synCtx).getAxis2MessageContext();
        Map headersMap =
                (Map) msgContext.getProperty(org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS);
        String authHeader = (String) headersMap.get("Authorization");

        // if we can't find the OAuth header, prompt error
        if (authHeader == null) {
            throw new SynapseException("Not a valid OAuth Request");
        }

        // checking for OAuth 2.0 params
        if (authHeader != null && authHeader.startsWith(OAuthConstants.BEARER)) {
            isOauth2 = true;
            // Do not need do validate an empty OAuth2 token
            if (authHeader.length() > OAuthConstants.BEARER.length()) {
                accessToken = authHeader.substring(OAuthConstants.BEARER.length()).trim();
            }
        }

        // not a valid OAuth 2.0 request
        if (isOauth2 == true && accessToken == null) {
            // Throw a correct descriptive message.
            throw new SynapseException("Invalid or empty OAuth 2.0 token");
        }

        return isOauth2;
    }

    /**
     * Try to authenticate using OAuth 2.0
     *
     * @param synCtx
     * @return true/false
     */
    private boolean handleOAuth2(MessageContext synCtx) {
        log.debug("Validating the OAuth 2.0 Request");
        OAuth2TokenValidationResponseDTO respDTO;
        Map headersMap;
        try {
            OAuth2TokenValidationServiceClient oauth2Client =
                    new OAuth2TokenValidationServiceClient(
                            getRemoteServiceUrl(),
                            getUsername(),
                            getPassword(),
                            cfgCtx);
            org.apache.axis2.context.MessageContext msgContext = ((Axis2MessageContext) synCtx).getAxis2MessageContext();
            headersMap =
                    (Map) msgContext.getProperty(org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS);
            String authHeader = (String) headersMap.get("Authorization");
            String accessToken = authHeader.substring(7).trim();
            List<OAuth2TokenValidationRequestDTO_TokenValidationContextParam> contextParams =
                    new ArrayList<OAuth2TokenValidationRequestDTO_TokenValidationContextParam>();
            for (int i = 0; ; i++) {
                if (synCtx.getProperty("oauth_context_param_key_" + i) != null &&
                        synCtx.getProperty("oauth_context_param_key_" + i) instanceof String &&
                        !synCtx.getProperty("oauth_context_param_key_" + i).equals("") &&
                        synCtx.getProperty("oauth_context_param_value_" + i) != null &&
                        synCtx.getProperty("oauth_context_param_value_" + i) instanceof String &&
                        !synCtx.getProperty("oauth_context_param_value_" + i).equals("")) {
                    String paramKey = (String) synCtx.getProperty("oauth_context_param_key_" + i);
                    String paramValue = (String) synCtx.getProperty("oauth_context_param_value_" + i);
                    OAuth2TokenValidationRequestDTO_TokenValidationContextParam param =
                            new OAuth2TokenValidationRequestDTO_TokenValidationContextParam();
                    param.setKey(paramKey);
                    param.setValue(paramValue);
                    contextParams.add(param);
                } else {
                    break;
                }
            }

            respDTO = oauth2Client.validateAuthenticationRequest(accessToken, contextParams);
        } catch (Exception e) {
            throw new SynapseException("Error occured while validating oauth 2.0 access token", e);
        }

        if (!respDTO.getValid()) {
            throw new SynapseException("OAuth 2.0 authentication failed");
        }
        if (respDTO.getAuthorizationContextToken() != null) {
            headersMap.put("X-JWT-Assertion", respDTO.getAuthorizationContextToken().getTokenString());
        }

        // Scope validation.
        if (synCtx.getProperty(OAuthConstants.OAUTH2_SCOPE_VALIDATION_ENABLED) != null &&
                Boolean.parseBoolean((String) synCtx.getProperty(OAuthConstants.OAUTH2_SCOPE_VALIDATION_ENABLED))) {
            String[] scopes = respDTO.getScope();
            if (scopes != null) {

                String apiScope = (String) synCtx.getProperty(OAuthConstants.SCOPE);

                // if API, default value
                if (apiScope == null) {
                    apiScope = (String) synCtx.getProperty("SYNAPSE_REST_API");
                }

                // if proxy service, default value.
                if (apiScope == null) {
                    apiScope = ((Axis2MessageContext) synCtx).getAxis2MessageContext().getAxisService().getName();
                }

                List<String> values = new ArrayList<String>(Arrays.asList(scopes));
                if (!values.contains(apiScope)) {
                    log.debug("Valid Scope is not match for given access token. OAuth2 scope validation is failed.");
                    throw new SynapseException("OAuth 2.0 authentication failed");
                }
            } else {
                log.debug("Scope is null for given access token.  OAuth2 scope validation is failed.");
                throw new SynapseException("OAuth 2.0 authentication failed");
            }
        }

        return true;
    }

    /**
     * Try to authenticate using OAuth 1.0a.
     *
     * @param synCtx
     * @return
     */
    private boolean handleOAuth1a(MessageContext synCtx) {

        log.debug("Validating the OAuth 1.0a Request");

        OAuthServiceClient client = null;
        ConfigurationContext configContext = null;
        OAuthConsumerDTO consumer = null;
        boolean isValidConsumer = false;

        try {

            Parameters params = populateOauthConsumerData(synCtx);
            client = new OAuthServiceClient(getRemoteServiceUrl(), configContext);

            if (params != null && params.getOauthToken() == null) {
                consumer = new OAuthConsumerDTO();
                consumer.setBaseString(params.getBaseString());
                consumer.setHttpMethod(params.getHttpMethod());
                consumer.setOauthConsumerKey(params.getOauthConsumerKey());
                consumer.setOauthNonce(params.getOauthNonce());
                consumer.setOauthSignature(params.getOauthSignature());
                consumer.setOauthSignatureMethod(params.getOauthSignatureMethod());
                consumer.setOauthTimeStamp(params.getOauthTimeStamp());
                isValidConsumer = client.isOAuthConsumerValid(consumer);
            } else {
                isValidConsumer = client.validateAuthenticationRequest(params);

            }

            if (!isValidConsumer) {
                throw new SynapseException("OAuth authentication failed");
            } else {
                return true;
            }

        } catch (Exception e) {
            throw new SynapseException("Error occured while validating oauth consumer", e);
        }

    }

    /**
     * Populates the Parameters object from the OAuth authorization header or
     * query string.
     *
     * @param synCtx
     * @return
     */
    private Parameters populateOauthConsumerData(MessageContext synCtx) {

        org.apache.axis2.context.MessageContext msgContext = ((Axis2MessageContext) synCtx).getAxis2MessageContext();
        Map headersMap =
                (Map) msgContext.getProperty(org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS);
        String authHeader = (String) headersMap.get("Authorization");
        String queryString = (String) msgContext.getProperty(NhttpConstants.REST_URL_POSTFIX);

        Parameters params = null;
        String splitChar = ",";
        boolean noAuthorizationHeader = false;

        params = new Parameters();
        String operation = null;

        if (queryString.indexOf("?") > -1) {
            String temp = queryString;
            queryString = queryString.substring(queryString.indexOf("?") + 1);
            operation = temp.substring(0, temp.indexOf("?") + 1);
        }

        if (authHeader == null) {
            noAuthorizationHeader = true;
            // No Authorization header available.
            authHeader = queryString;
            splitChar = "&";
        }

        StringBuffer nonAuthParams = new StringBuffer();

        if (authHeader != null) {
            if (authHeader.startsWith("OAuth ")) {
                authHeader = authHeader.substring(authHeader.indexOf("o"));
            }
            String[] headers = authHeader.split(splitChar);
            if (headers != null && headers.length > 0) {
                for (String header : headers) {
                    String[] elements = header.split("=");
                    if (elements != null && elements.length > 0) {
                        if (OAuthConstants.OAUTH_CONSUMER_KEY.equals(elements[0].trim())) {
                            params.setOauthConsumerKey(removeLeadingAndTrailingQuatation(elements[1].trim()));
                        } else if (OAuthConstants.OAUTH_NONCE.equals(elements[0].trim())) {
                            params.setOauthNonce(removeLeadingAndTrailingQuatation(elements[1].trim()));
                        } else if (OAuthConstants.OAUTH_SIGNATURE.equals(elements[0].trim())) {
                            params.setOauthSignature(removeLeadingAndTrailingQuatation(elements[1].trim()));
                        } else if (OAuthConstants.OAUTH_SIGNATURE_METHOD.equals(elements[0].trim())) {
                            params.setOauthSignatureMethod(removeLeadingAndTrailingQuatation(elements[1].trim()));
                        } else if (OAuthConstants.OAUTH_TIMESTAMP.equals(elements[0].trim())) {
                            params.setOauthTimeStamp(removeLeadingAndTrailingQuatation(elements[1].trim()));
                        } else if (OAuthConstants.OAUTH_CALLBACK.equals(elements[0].trim())) {
                            params.setOauthCallback(removeLeadingAndTrailingQuatation(elements[1].trim()));
                        } else if (OAuthConstants.SCOPE.equals(elements[0].trim())) {
                            params.setScope(removeLeadingAndTrailingQuatation(elements[1].trim()));
                        } else if (OAuthConstants.OAUTH_DISPLAY_NAME.equals(elements[0].trim())) {
                            params.setDisplayName(removeLeadingAndTrailingQuatation(elements[1].trim()));
                        } else if (OAuthConstants.OAUTH_TOKEN.equals(elements[0].trim())) {
                            params.setOauthToken(removeLeadingAndTrailingQuatation(elements[1].trim()));
                        } else if (OAuthConstants.OAUTH_VERIFIER.equals(elements[0].trim())) {
                            params.setOauthTokenVerifier(removeLeadingAndTrailingQuatation(elements[1].trim()));
                        } else if (OAuthConstants.OAUTH_TOKEN_SECRET.equals(elements[0].trim())) {
                            params.setOauthTokenSecret(removeLeadingAndTrailingQuatation(elements[1].trim()));
                        } else if (OAuthConstants.OAUTH_VERSION.equals(elements[0].trim())) {
                            params.setVersion(removeLeadingAndTrailingQuatation(elements[1].trim()));
                        } else {
                            nonAuthParams.append(elements[0].trim() +
                                    "=" +
                                    removeLeadingAndTrailingQuatation(elements[1].trim()) +
                                    "&");
                        }
                    }
                }
            }
        }

        String nonOauthParamStr = nonAuthParams.toString();

        if (!noAuthorizationHeader) {
            nonOauthParamStr = queryString + "&";
        }

        String scope = (String) synCtx.getProperty(OAuthConstants.SCOPE);

        if (scope == null) {
            throw new SynapseException("Unable to find SCOPE value in Synapse Message Context");
        }
        params.setScope(scope);

        params.setHttpMethod((String) msgContext.getProperty("HTTP_METHOD"));

        String prefix = (String) msgContext.getProperty(NhttpConstants.SERVICE_PREFIX);

        if (nonOauthParamStr.length() > 1) {
            params.setBaseString(prefix + operation +
                    nonOauthParamStr.substring(0, nonOauthParamStr.length() - 1));
        } else {
            params.setBaseString(prefix);
        }

        return params;
    }

    private String removeLeadingAndTrailingQuatation(String base) {
        String result = base;

        if (base.startsWith("\"") || base.endsWith("\"")) {
            result = base.replace("\"", "");
        }
        return result.trim();
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getRemoteServiceUrl() {
        if (remoteServiceUrl != null) {
            if (!remoteServiceUrl.endsWith("/")) {
                remoteServiceUrl += "/";
            }
        }
        return remoteServiceUrl;
    }

    public void setRemoteServiceUrl(String remoteServiceUrl) {
        this.remoteServiceUrl = remoteServiceUrl;
    }

    @Override
    public boolean isContentAware() {
        return false;
    }

}
