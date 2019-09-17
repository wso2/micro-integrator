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

import org.wso2.micro.integrator.dataservices.core.DataServiceFault;

/**
 * This interface represents the functionality that should be implemented to provide 
 * custom dynamic user authentication logic.
 */
public interface DynamicUserAuthenticator {

	/**
	 * This method is used to lookup a username/password pair given a source username.
	 * @param user The source username
	 * @return A two element String array containing the username and password respectively
	 * @throws DataServiceFault
	 */
	String[] lookupCredentials(String user) throws DataServiceFault;
	
}
