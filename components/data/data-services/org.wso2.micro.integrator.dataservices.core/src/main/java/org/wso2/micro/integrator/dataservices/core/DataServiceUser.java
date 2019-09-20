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
package org.wso2.micro.integrator.dataservices.core;

import java.util.Set;

/**
 * Represents a data services user who is sending requests.
 */
public class DataServiceUser {

	/**
	 * Username of the data services user.
	 */
	private String username;
	
	/**
	 * User roles assigned to this user.
	 */
	private Set<String> userRoles;
	
	public DataServiceUser(String username, Set<String> userRoles) {
		this.username = username;
		this.userRoles = userRoles;
	}
	
	public String getUsername() {
		return username;
	}
	
	public Set<String> getUserRoles() {
		return userRoles;
	}
	
}
