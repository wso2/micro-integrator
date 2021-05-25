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

import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.MessageContext;
import org.apache.synapse.config.Entry;
import org.apache.synapse.registry.Registry;
import org.wso2.securevault.DecryptionProvider;
import org.wso2.securevault.secret.SecretRepository;

/**
 * Holds all secrets in a file
 */
public class RegistrySecretRepository implements SecretRepository {

	private static Log log = LogFactory.getLog(RegistrySecretRepository.class);

	/* Parent secret repository */
	private SecretRepository parentRepository;

	private MessageContext synCtx;

	public RegistrySecretRepository() {
		super();

	}

	/**
	 * @param alias
	 *            Alias name for look up a secret
	 * @return Secret if there is any , otherwise ,alias itself
	 * @see org.wso2.securevault.secret.SecretRepository
	 */
	public String getSecret(String alias) {

		Entry propEntry =
		                  synCtx.getConfiguration()
		                        .getEntryDefinition(SecureVaultConstants.CONF_CONNECTOR_SECURE_VAULT_CONFIG_PROP_LOOK);

		Registry registry = synCtx.getConfiguration().getRegistry();

		String propertyValue = "";

		if (registry != null) {
			registry.getResource(propEntry, new Properties());
			if (alias != null) {
				Properties reqProperties = propEntry.getEntryProperties();
				if (reqProperties != null && reqProperties.get(alias) != null) {
						propertyValue = reqProperties.getProperty(alias);
				}
			}
		}
		DecryptionProvider decyptProvider = CipherInitializer.getInstance().getDecryptionProvider();

		if (decyptProvider == null) {
			log.error("Can not proceed decyption due to the secret repository intialization error");
			return null;
		}

		String decryptedText = new String(decyptProvider.decrypt(propertyValue.trim().getBytes()));

		if (log.isDebugEnabled()) {
			log.info("evaluation completed succesfully " + decryptedText);
		}
		return decryptedText;

	}

	/**
	 * @param alias
	 *            Alias name for look up a encrypted Value
	 * @return encrypted Value if there is any , otherwise ,alias itself
	 * @see org.wso2.securevault.secret.SecretRepository
	 */
	public String getEncryptedData(String alias) {

		return null;
	}

	public void setParent(SecretRepository parent) {
		this.parentRepository = parent;
	}

	public SecretRepository getParent() {
		return this.parentRepository;
	}

	public void setSynCtx(MessageContext synCtx) {
		this.synCtx = synCtx;
	}

	@Override
	public void init(Properties arg0, String arg1) {
		// TODO Auto-generated method stub

	}

}
