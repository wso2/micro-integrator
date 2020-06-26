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

package org.wso2.micro.integrator.mediation.security.vault;

import org.apache.synapse.MessageContext;

public interface SecureVaultLookupHandler {

	/**
	 * Method to evaluate the parameters passed to extract values from
	 * secure-vault lookup
	 * 
	 * @param aliasPasword alias for the password
	 * @return the string value of the password
	 */
	@Deprecated
	public String evaluate(String aliasPasword, MessageContext synCtx);

	/**
	 * Method to evaluate the parameters passed to extract values from
	 * secure-vault lookup
	 *
	 * @param aliasPasword
	 * @param secretSrcData Information about the secret
	 * @return
	 * @throws org.wso2.carbon.registry.core.exceptions.RegistryException
	 */
	public String evaluate(String aliasPasword, SecretSrcData secretSrcData, MessageContext synCtx);

}
