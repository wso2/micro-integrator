/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.mediation.security.vault;

import java.util.Calendar;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.MessageContext;
import org.apache.synapse.config.SynapseConfiguration;
import org.apache.synapse.registry.Registry;
import org.wso2.carbon.base.api.ServerConfigurationService;
import org.wso2.carbon.registry.core.exceptions.RegistryException;

public class SecureVaultLookupHandlerImpl implements SecureVaultLookupHandler {

	private static Log log = LogFactory.getLog(SecureVaultLookupHandlerImpl.class);

	private static SecureVaultLookupHandlerImpl instance = null;

	private ServerConfigurationService serverConfigService;

	private Registry registry;
	
	Object decryptlockObj = new Object();

	private SecureVaultLookupHandlerImpl(ServerConfigurationService serverConfigurationService, Registry registry) {
		this.serverConfigService = serverConfigurationService;
		this.registry = registry;
		init();
	}

	public static SecureVaultLookupHandlerImpl getDefaultSecurityService() {
		return getDefaultSecurityService(SecurityServiceHolder.getInstance().getServerConfigurationService(),
		                                 SecurityServiceHolder.getInstance().getRegistry());
	}

	private static SecureVaultLookupHandlerImpl getDefaultSecurityService(
			ServerConfigurationService serverConfigurationService, Registry registry) {
		if (instance == null) {
			instance = new SecureVaultLookupHandlerImpl(serverConfigurationService, registry);
		}
		return instance;
	}

	private void init() {
		// creating vault-specific storage repository (this happens only if resource not existing)
		initRegistryRepo();
	}

	/**
	 * Initializing the repository which requires to store the secure vault
	 * cipher text
	 */
	private void initRegistryRepo() {
		 //	Here, the secure vault resource is created, if it does not exist in the registry.
		if (!registry.isResourceExists(SecureVaultConstants.CONNECTOR_SECURE_VAULT_CONFIG_REPOSITORY)) {
			registry.newResource(SecureVaultConstants.CONNECTOR_SECURE_VAULT_CONFIG_REPOSITORY, true);
		}
	}

	public String getProviderClass() {
		return this.getClass().getName();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.wso2.carbon.mediation.secure.vault.MediationSecurity#evaluate(java
	 * .lang.String,
	 * org.wso2.carbon.mediation.secure.vault.MediationSrecurtyClient
	 * .LookupType)
	 */
	@Override
	public String evaluate(String aliasPasword, MessageContext synCtx) throws RegistryException {
		SynapseConfiguration synapseConfiguration = synCtx.getConfiguration();
		Map<String, Object> decryptedCacheMap = synapseConfiguration.getDecryptedCacheMap();
		if (decryptedCacheMap.containsKey(aliasPasword)) {
			SecureVaultCacheContext cacheContext =
			                                       (SecureVaultCacheContext) decryptedCacheMap.get(aliasPasword);
			if (cacheContext != null) {
				String cacheDurable = synCtx.getConfiguration().getRegistry().getConfigurationProperties().getProperty
						("cachableDuration");
				long cacheTime = (cacheDurable != null && !cacheDurable.isEmpty()) ? Long.parseLong(cacheDurable) :
						10000;
				if ((cacheContext.getDateTime().getTime() + cacheTime) >= System.currentTimeMillis()) {
					// which means the given value between the cachable limit
					return cacheContext.getDecryptedValue();
				} else {
					decryptedCacheMap.remove(aliasPasword);
					return vaultLookup(aliasPasword, synCtx, decryptedCacheMap);
				}
			} else {
				return vaultLookup(aliasPasword, synCtx, decryptedCacheMap);
			}
		} else {
			String decryptedValue = vaultLookup(aliasPasword, synCtx, decryptedCacheMap);
			return decryptedValue;
		}
	}

	private String vaultLookup(String aliasPasword, MessageContext synCtx,
							   Map<String, Object> decryptedCacheMap) {
		synchronized (decryptlockObj) {
			SecretCipherHander secretManager = new SecretCipherHander(synCtx);
			String decryptedValue = secretManager.getSecret(aliasPasword);
			if (decryptedCacheMap == null) {
				return null;
			}

			if (decryptedValue.isEmpty()) {
				SecureVaultCacheContext cacheContext =
						(SecureVaultCacheContext) decryptedCacheMap.get(aliasPasword);
				if (cacheContext != null) {
					return cacheContext.getDecryptedValue();
				}
			}

			decryptedCacheMap.put(aliasPasword, new SecureVaultCacheContext(Calendar.getInstance()
					.getTime(),
					decryptedValue));
			return decryptedValue;
		}
	}

}
