/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
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
import org.wso2.micro.integrator.management.apis.ManagementApiUndefinedException;
import org.wso2.micro.integrator.management.apis.Utils;

import javax.xml.stream.XMLStreamException;
import java.io.IOException;
import java.util.Objects;

import static org.wso2.micro.integrator.management.apis.Constants.USERNAME_PROPERTY;

/**
 * This class provides an abstraction for all security handlers using authorization for management api.
 */
public abstract class AuthorizationHandlerAdapter extends SecurityHandlerAdapter {

    private static final Log LOG = LogFactory.getLog(AuthorizationHandlerAdapter.class);

    public AuthorizationHandlerAdapter(String context) throws CarbonException, XMLStreamException, IOException,
            ManagementApiUndefinedException {
        super(context);
    }

    @Override
    public Boolean handle(MessageContext messageContext) {
        String userName = Utils.getStringPropertyFromMessageContext(messageContext, USERNAME_PROPERTY);
        if (Objects.nonNull(userName)) {
            if (authorize(userName)) {
                return true;
            } else {
                SecurityUtils.setStatusCode(messageContext, AuthConstants.SC_FORBIDDEN);
                return false;
            }
        } else {
            LOG.error("The user has not been authenticated. Consider adding an AuthenticationHandler prior to the "
                      + "AuthorizationHandler");
        }
        return false;
    }

    /**
     * Executes the authentication logic relevant to the handler.
     *
     * @param userName user name of the logged in user
     * @return Boolean authenticated
     */
    protected abstract Boolean authorize(String userName);

}
