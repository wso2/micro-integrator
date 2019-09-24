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
package org.wso2.micro.integrator.management.apis;

import com.nimbusds.jose.JOSEException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.MessageContext;
import org.apache.synapse.SynapseConstants;
import org.apache.synapse.config.SynapseConfiguration;
import org.json.JSONObject;
import org.wso2.micro.integrator.management.apis.security.handler.*;

import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class LoginResource implements MiApiResource {

    private static final Log LOG = LogFactory.getLog(LoginResource.class);

    // HTTP method types supported by the resource
    Set<String> methods;

    public LoginResource() {
        methods = new HashSet<>();
        methods.add(Constants.HTTP_POST);
        methods.add(Constants.HTTP_GET);
    }

    @Override
    public Set<String> getMethods() {
        return methods;
    }

    @Override
    public boolean invoke(MessageContext messageContext, org.apache.axis2.context.MessageContext axis2MessageContext,
                          SynapseConfiguration synapseConfiguration) {
        //Init token store
        JWTTokenStore tokenStore = JWTInMemoryTokenStore.
                getInstance(JWTConfig.getInstance().getJwtConfigDto().getTokenStoreSize());

        //UUID used as unique token
        UUID uuid = UUID.randomUUID();
        String randomUUIDString = uuid.toString();

        JWTTokenInfoDTO newToken = new JWTTokenInfoDTO();
        newToken.setToken(randomUUIDString);
        newToken.setScope(AuthConstants.JWT_TOKEN_DEFAULT_SCOPE);
        newToken.setIssuer((String) axis2MessageContext.getProperty(SynapseConstants.SERVER_IP));
        Long time = new Date().getTime();
        String expiryConfig = JWTConfig.getInstance().getJwtConfigDto().getExpiry();
        Long expiryInMins = AuthConstants.DEFAULT_EXPIRY_DURATION;
        if(expiryConfig != null && !expiryConfig.isEmpty()) {
            expiryInMins = Long.parseLong(expiryConfig);
        }
        newToken.setExpiry(time + (expiryInMins * 60 * 1000));
        String jwtHash = null;
        try {
            jwtHash = populateJWTToken(newToken);
        } catch (JOSEException e) {
            LOG.error("Error occurred while generating JWT token", e);
            handleServerError(axis2MessageContext, "Error occurred while generating JWT token");
            return true;
        } catch (NoSuchAlgorithmException e) {
            LOG.error("Error occurred while generating key pairs", e);
            handleServerError(axis2MessageContext, "Error occurred while generating key pairs");
            return true;
        }
        newToken.setHash(jwtHash);
        if(!tokenStore.putToken(jwtHash, newToken)) {
            handleServerError(axis2MessageContext, "Error occurred while adding token to store");
            return true;
        }

        JSONObject jsonPayload = new JSONObject();
        jsonPayload.put(AuthConstants.RESPONSE_JSON_TOKEN_FIELD, jwtHash);
        Utils.setJsonPayLoad(axis2MessageContext, jsonPayload);
        axis2MessageContext.removeProperty(Constants.NO_ENTITY_BODY);
        return true;
    }

    /**
     * Create JWT Token using JWT Token info DTO
     * @param jwtToken JWT Token info object
     * @return Serialized JWT Token string
     * @throws JOSEException
     * @throws NoSuchAlgorithmException
     */
    public String populateJWTToken(JWTTokenInfoDTO jwtToken) throws JOSEException, NoSuchAlgorithmException {
        JWTTokenGenerator generator = new JWTTokenGenerator();
        return generator.generateJWTToken(jwtToken);
    }

    /**
     * Generate and sets error json response
     * @param axis2MessageContext msg ctx
     * @param errorDetail Error string
     */
    private void handleServerError(org.apache.axis2.context.MessageContext axis2MessageContext, String errorDetail) {
        Utils.setJsonPayLoad(axis2MessageContext, Utils.createJsonErrorObject(errorDetail));
        axis2MessageContext.setProperty(Constants.HTTP_STATUS_CODE, Constants.INTERNAL_SERVER_ERROR);
        axis2MessageContext.removeProperty(Constants.NO_ENTITY_BODY);
    }


}
