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
package org.wso2.micro.integrator.dataservices.core.auth;

import org.apache.axis2.context.MessageContext;
import org.wso2.micro.integrator.dataservices.common.DBConstants;

/**
 * This is the abstract class which implements some of the methods in Authorization provider interface.
 * This will have default getUserName method implemented which gets username from messageContext,
 * if needs that can be overridden too.
 */
public abstract class AbstractAuthorizationProvider implements AuthorizationProvider {

    /**
     * Default implementation to get user name from message context.
     *
     * @param msgContext
     * @return username.
     */
    public String getUsername(MessageContext msgContext) {
        String userName = (String) msgContext.getProperty(
                DBConstants.MSG_CONTEXT_USERNAME_PROPERTY);
        return userName;
    }

}
