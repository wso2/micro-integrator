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
import org.wso2.micro.core.util.CarbonException;
import org.wso2.micro.integrator.management.apis.ManagementApiUndefinedException;

import javax.xml.stream.XMLStreamException;
import java.io.IOException;
import java.util.Objects;

import static org.wso2.micro.integrator.management.apis.Constants.USERNAME_PROPERTY;

/**
 * This class extends the AuthenticationHandlerAdapter to create a basic security handler with a user store defined in
 * internal-apis.xml.
 */
public class BasicSecurityHandler extends AuthenticationHandlerAdapter {

    private static final Log LOG = LogFactory.getLog(BasicSecurityHandler.class);

    private String name;

    public BasicSecurityHandler(String context) throws CarbonException, XMLStreamException, IOException,
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
    protected Boolean authenticate(String authHeaderToken) {
        LOG.debug("Handling authentication");
        String decodedCredentials = new String(new Base64().decode(authHeaderToken.getBytes()));
        String[] usernamePasswordArray = decodedCredentials.split(":");
        // Avoid possible array index out of bound errors
        if (usernamePasswordArray.length != 2) {
            return false;
        }
        String userNameFromHeader = usernamePasswordArray[0];
        String passwordFromHeader = usernamePasswordArray[1];
        if (!usersList.isEmpty()) {
            for (String userNameFromStore : usersList.keySet()) {
                if (userNameFromStore.equals(userNameFromHeader)) {
                    String passwordFromStore = String.valueOf(usersList.get(userNameFromStore));
                    if (isValid(passwordFromStore) && passwordFromStore.equals(passwordFromHeader)) {
                        messageContext.setProperty(USERNAME_PROPERTY, userNameFromHeader);
                        return true;
                    }
                }
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
}
