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

import java.util.Calendar;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.MessageContext;
import org.apache.synapse.config.SynapseConfiguration;

public class SecureVaultLookupHandlerImpl implements SecureVaultLookupHandler {

	private static Log log = LogFactory.getLog(SecureVaultLookupHandlerImpl.class);

	private static SecureVaultLookupHandlerImpl instance = null;

	private final Object decryptlockObj = new Object();

	private SecureVaultLookupHandlerImpl() {
	}

	public static SecureVaultLookupHandlerImpl getDefaultSecurityService() {
		if (instance == null) {
			instance = new SecureVaultLookupHandlerImpl();
		}
		return instance;
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
	public String evaluate(String aliasPasword, SecretSrcData secretSrcData, MessageContext synCtx) {
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
					return vaultLookup(aliasPasword, secretSrcData, decryptedCacheMap);
				}
			} else {
				return vaultLookup(aliasPasword, secretSrcData, decryptedCacheMap);
			}
		} else {
			return vaultLookup(aliasPasword, secretSrcData, decryptedCacheMap);
		}
	}

	@Override
	public String evaluate(String aliasPasword, MessageContext synCtx) {
		return evaluate(aliasPasword, new SecretSrcData(), synCtx);
	}

	/**
	 * Return the decrypted secret value
	 * @param aliasPasword Password alias
	 * @param secretSrcData SecretSrcData object
	 * @param synCtx synapse message context
	 * @param decryptedCacheMap decrypted cache map from the message context
	 * */
	private String vaultLookup(String aliasPasword, SecretSrcData secretSrcData, Map<String, Object> decryptedCacheMap) {
		synchronized (decryptlockObj) {
			SecretCipherHander secretManager = new SecretCipherHander();
			String decryptedValue = secretManager.getSecret(aliasPasword, secretSrcData);
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
