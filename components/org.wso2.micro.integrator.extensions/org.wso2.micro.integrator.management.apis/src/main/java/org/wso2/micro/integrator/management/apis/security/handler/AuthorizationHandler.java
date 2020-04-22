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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.MessageContext;
import org.wso2.micro.core.util.CarbonException;
import org.wso2.micro.integrator.management.apis.Constants;
import org.wso2.micro.integrator.management.apis.ManagementApiUndefinedException;
import org.wso2.micro.integrator.security.MicroIntegratorSecurityUtils;
import org.wso2.micro.integrator.security.user.api.UserStoreException;

import javax.xml.stream.XMLStreamException;
import java.io.IOException;
import java.util.Arrays;

import static org.wso2.micro.integrator.management.apis.Constants.USERNAME_PROPERTY;

public class AuthorizationHandler extends SecurityHandlerAdapter {

    private static final Log LOG = LogFactory.getLog(AuthorizationHandler.class);
    private String name;
    private MessageContext messageContext;

    public AuthorizationHandler() throws CarbonException, XMLStreamException, IOException, ManagementApiUndefinedException {
        super();
    }

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

        if ((messageContext.getTo().getAddress()).startsWith((Constants.REST_API_CONTEXT + Constants.PREFIX_USERS))) {
            if (useCarbonUserStore) {
                //Uses carbon user store
                try {
                    return processAuthorizationWithCarbonUserStore(authHeaderToken);
                } catch (UserStoreException e) {
                    LOG.error("Error while authenticating with carbon user store", e);
                }
            } else {
                //Uses in memory user store
                LOG.error("User management is not supported with the in memory user store. Users need to be modified "
                          + "in the internal-apis.xml file");
                return false;
            }
        }
        return true;
    }

    /**
     * Processes /users request if the user is an admin
     *
     * @param token extracted basic auth token
     * @return if successfully authorized
     */
    private boolean processAuthorizationWithCarbonUserStore(String token) throws UserStoreException {

        String username = messageContext.getProperty(USERNAME_PROPERTY).toString();
        boolean isAuthorized = authorize(username,
                                         MicroIntegratorSecurityUtils.getRealmConfiguration().getAdminRoleName());
        if (!isAuthorized) {
            LOG.error("User " + username + " cannot be authorized");
        }
        return isAuthorized;
    }

    private boolean authorize(String username, String desiredRole){
        try {
            String[] listOfRoles = MicroIntegratorSecurityUtils.getUserStoreManager().getRoleListOfUser(username);
            return Arrays.asList(listOfRoles).contains(desiredRole);
        } catch (UserStoreException e) {
            LOG.error("Error initializing the user store", e);
        }
        return false;
    }
}
