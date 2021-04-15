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
import org.wso2.micro.integrator.management.apis.ManagementApiUndefinedException;
import org.wso2.micro.integrator.security.user.api.UserStoreException;

import java.io.IOException;
import javax.xml.stream.XMLStreamException;

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
    public Boolean invoke(MessageContext messageContext) {
        return super.invoke(messageContext);
    }

    @Override
    protected Boolean authenticate(MessageContext messageContext, String authHeaderToken) {

        LOG.debug("Handling authentication with BasicSecurityHandler");
        if (useCarbonUserStore) {
            try {
                return processAuthRequestWithCarbonUserStore(messageContext, authHeaderToken);
            } catch (UserStoreException e) {
                LOG.error("Error while authenticating with carbon user store", e);
                return false;
            }
        } else {
            return processAuthRequestWithFileBasedUserStore(messageContext, authHeaderToken);
        }
    }

}
