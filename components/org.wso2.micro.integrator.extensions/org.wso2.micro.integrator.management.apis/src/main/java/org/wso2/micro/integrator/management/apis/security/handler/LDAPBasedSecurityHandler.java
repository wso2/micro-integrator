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
import org.wso2.micro.integrator.security.MicroIntegratorSecurityUtils;
import org.wso2.micro.integrator.security.user.api.UserStoreException;
import org.wso2.micro.integrator.security.user.api.UserStoreManager;

/**
 * This class extents SecurityHandlerAdapter to implement the authentication logic for a
 * LDAP user store for management api
 */
public class LDAPBasedSecurityHandler extends SecurityHandlerAdapter {

    private String name;
    private static final Log LOG = LogFactory.getLog(LDAPBasedSecurityHandler.class);

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
            return userStoreManager.authenticate(username, password);
        } catch (UserStoreException e) {
            LOG.error("Error in authenticating user", e);
            return false;
        }
    }
}
