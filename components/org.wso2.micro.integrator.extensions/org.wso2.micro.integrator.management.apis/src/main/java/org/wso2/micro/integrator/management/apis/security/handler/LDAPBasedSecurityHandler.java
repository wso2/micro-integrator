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
import org.wso2.micro.core.util.CarbonException;
import org.wso2.micro.integrator.management.apis.ManagementApiUndefinedException;
import org.wso2.micro.integrator.security.MicroIntegratorSecurityUtils;
import org.wso2.micro.integrator.security.user.api.UserStoreException;
import org.wso2.micro.integrator.security.user.api.UserStoreManager;

import javax.xml.stream.XMLStreamException;
import java.io.IOException;

import static org.wso2.micro.integrator.management.apis.Constants.USERNAME_PROPERTY;

/**
 * This class extents AuthenticationHandlerAdapter to implement the authentication logic for a
 * LDAP user store for management api
 */
public class LDAPBasedSecurityHandler extends AuthenticationHandlerAdapter {

    private String name;
    private static final Log LOG = LogFactory.getLog(LDAPBasedSecurityHandler.class);

    public LDAPBasedSecurityHandler(String context) throws CarbonException, XMLStreamException, IOException,
            ManagementApiUndefinedException {
        super(context);
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
    protected Boolean authenticate(MessageContext messageContext, String authHeaderToken) {
        String decodedCredentials = new String(new Base64().decode(authHeaderToken.getBytes()));
        String[] usernamePasswordArray = decodedCredentials.split(":");
        // Avoid possible array index out of bound errors
        if (usernamePasswordArray.length != 2) {
            return false;
        }
        String username = usernamePasswordArray[0];
        String password = usernamePasswordArray[1];
        UserStoreManager userStoreManager;
        try {
            userStoreManager = MicroIntegratorSecurityUtils.getUserStoreManager();
        } catch (UserStoreException e) {
            LOG.error("Error occurred while retrieving User Store Manager", e);
            return false;
        }
        try {
            boolean isAuthenticated = userStoreManager.authenticate(username, password);
            if (isAuthenticated) {
                messageContext.setProperty(USERNAME_PROPERTY, username);
            }
            return isAuthenticated;
        } catch (UserStoreException e) {
            LOG.error("Error in authenticating user", e);
            return false;
        }
    }
}
