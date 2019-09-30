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

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.MessageContext;
import org.wso2.micro.integrator.management.apis.Constants;
import org.wso2.micro.integrator.security.MicroIntegratorSecurityUtils;
import org.wso2.micro.integrator.security.user.api.UserStoreException;

import java.util.Map;
import java.util.Objects;

public class JWTTokenSecurityHandler extends SecurityHandlerAdapter {

    private static final Log LOG = LogFactory.getLog(JWTTokenSecurityHandler.class);
    private String name;
    private Map<String, char[]> userList = null;
    private MessageContext messageContext;

    @Override
    public Boolean invoke(MessageContext messageContext) {

        this.messageContext = messageContext;
        return super.invoke(messageContext);
    }

    @Override
    public String getName() {

        return this.name;
    }

    @Override
    public void setName(String name) {

        this.name = name;
    }

    @Override
    protected Boolean authenticate(String authHeaderToken) {

        if ((Constants.REST_API_CONTEXT + Constants.PREFIX_LOGIN).contentEquals(messageContext.getTo().getAddress())) {
            //Login request is basic auth
            if (JWTConfig.getInstance().getJwtConfigDto().isUseCarbonUserStore()) {
                //Uses carbon user store
                try {
                    return processLoginRequestWithCarbonUserStore(authHeaderToken);
                } catch (UserStoreException e) {
                    LOG.error("Error while authenticating with carbon user store", e);
                }
            } else {
                //Uses in memory user store
                return processLoginRequestInMemoryUserStore(authHeaderToken);
            }
        } else { //Other resources apart from /login should be authenticated from JWT based auth
            JWTTokenStore tokenStore = JWTInMemoryTokenStore.getInstance();
            JWTTokenInfoDTO jwtTokenInfoDTO = tokenStore.getToken(authHeaderToken);
            if (jwtTokenInfoDTO != null && !jwtTokenInfoDTO.isRevoked()) {
                jwtTokenInfoDTO.setLastAccess(System.currentTimeMillis()); //Record last successful access
                return true;
            }
        }
        return false;
    }

    /**
     * Checks if a given value is not null and not empty.
     *
     * @param value String value
     */
    private Boolean isValid(String value) {

        return (Objects.nonNull(value) && !value.isEmpty());
    }

    /**
     * Processes /login request if the JWTToken Security Handler is engaged. Since /login is
     * basic auth
     *
     * @param token extracted basic auth token
     * @return boolean if successfully authenticated
     */
    private boolean processLoginRequestInMemoryUserStore(String token) {

        String decodedCredentials = new String(new Base64().decode(token.getBytes()));
        String[] usernamePasswordArray = decodedCredentials.split(":");
        if (userList == null || userList.isEmpty()) {
            populateUserList();
        }
        if (usernamePasswordArray.length != 2) {
            return false;
        }
        String username = usernamePasswordArray[0];
        String password = usernamePasswordArray[1];
        if (!userList.isEmpty()) {
            for (String userNameFromStore : userList.keySet()) {
                if (userNameFromStore.equals(username)) {
                    String passwordFromStore = String.valueOf(userList.get(userNameFromStore));
                    if (isValid(passwordFromStore) && passwordFromStore.equals(password)) {
                        LOG.info("User " + username + " logged in successfully");
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * Processes /login request if the JWTToken Security Handler is engaged. Since /login is
     * basic auth
     *
     * @param token extracted basic auth token
     * @return if successfully authenticated
     */
    private boolean processLoginRequestWithCarbonUserStore(String token) throws UserStoreException {

        String decodedCredentials = new String(new Base64().decode(token.getBytes()));
        String[] usernamePasswordArray = decodedCredentials.split(":");
        if (usernamePasswordArray.length != 2) {
            return false;
        }
        String username = usernamePasswordArray[0];
        String password = usernamePasswordArray[1];
        boolean isAuthenticated = MicroIntegratorSecurityUtils.getUserStoreManager().authenticate(username,
                password);
        if (isAuthenticated) {
            LOG.info("User " + username + " logged in successfully");
            return true;
        }
        return false;
    }

    /**
     * Populates the userList hashMap with user list obtain through user store
     */
    private void populateUserList() {

        JWTConfigDTO jwtConfig = JWTConfig.getInstance().getJwtConfigDto();
        if (jwtConfig != null) {
            userList = jwtConfig.getUsers();
        }
    }
}
