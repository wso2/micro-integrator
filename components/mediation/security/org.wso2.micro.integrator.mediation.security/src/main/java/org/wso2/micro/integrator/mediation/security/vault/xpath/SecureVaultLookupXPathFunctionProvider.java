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
package org.wso2.micro.integrator.mediation.security.vault.xpath;

import org.apache.synapse.MessageContext;
import org.apache.synapse.util.xpath.ext.SynapseXpathFunctionContextProvider;
import org.jaxen.Function;

public class SecureVaultLookupXPathFunctionProvider implements SynapseXpathFunctionContextProvider {

	private static final String NAME_SPACE_PREFIX = "wso2";
	private static final String VAULT_LOOKUP = "vault-lookup";

	public Function getInitializedExtFunction(MessageContext messageContext) {
		return new VaultLookupFunction(messageContext);
	}

	public String getResolvingQName() {
		return NAME_SPACE_PREFIX + VAULT_LOOKUP;
	}
}
