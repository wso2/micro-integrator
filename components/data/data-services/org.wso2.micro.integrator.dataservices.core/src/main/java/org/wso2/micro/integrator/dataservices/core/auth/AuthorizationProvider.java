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
import org.wso2.micro.integrator.dataservices.core.DataServiceFault;

import java.util.Map;

/**
 * This is the interface which we can use to connect to third party authorisation provider in order to do role based
 * filtering in DSS.
 */
public interface AuthorizationProvider {

    /**
     * Method used to get the roles of the user.
     *
     * @param msgContext to be used in retrieving roles.
     * @return String array of user roles assigned to that particular user.
     * @throws DataServiceFault
     */
    public String[] getUserRoles(MessageContext msgContext) throws DataServiceFault;

    /**
     * Method used to get all the user roles in order to display in data service design phase.
     *
     * @return String array of all user roles.
     * @throws DataServiceFault
     */
    public String[] getAllRoles() throws DataServiceFault;

    /**
     * Method to get username from the message context.
     *
     * @param msgContext
     * @return username.
     *
     * @throws DataServiceFault
     */
    public String getUsername(MessageContext msgContext) throws DataServiceFault;

    /**
     * To set the properties specific to AuthorizationProvider, and instantiate the object.
     * if no properties specified, empty map will be passed, format to specify
     * property "<Property name="userName">admin</Property>"
     *
     * @throws DataServiceFault
     */
    public abstract void init(Map<String, String> authorizationProps) throws DataServiceFault;

}
