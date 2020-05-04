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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.SynapseException;

/**
 * Entry point for manage secrets
 */
class SecretCipherHander {

	private static Log log = LogFactory.getLog(SecretCipherHander.class);

	/* Root Secret Repository */
	private CiphertextRepository parentRepository = CiphertextRepository.getInstance();
	private FileSecretRepository fileSecretRepository = new FileSecretRepository();
	private EnvironmentSecretRepository environmentSecretRepository =  new EnvironmentSecretRepository();

	/**
	 * Returns the secret corresponding to the given alias name
	 * 
	 * @param alias
	 *            The logical or alias name
	 * @return If there is a secret , otherwise , alias itself
	 */
	String getSecret(String alias) {
		return parentRepository.getSecret(alias);
	}

	public String getSecret(String alias, SecretSrcData secretSrcData) {

		if (VaultType.DOCKER.equals(secretSrcData.getVaultType()) || VaultType.FILE.equals(secretSrcData.getVaultType())) {
			String resolvedAlias = secretSrcData.getSecretRoot() + alias;
			if (secretSrcData.isEncrypted()) {
				return fileSecretRepository.getSecret(resolvedAlias);
			}
			return fileSecretRepository.getPlainTextSecret(resolvedAlias);
		} else if (VaultType.ENV.equals(secretSrcData.getVaultType())) {
			if (secretSrcData.isEncrypted()) {
				return environmentSecretRepository.getSecret(alias);
			}
			return environmentSecretRepository.getPlainTextSecret(alias);
		} else if (VaultType.REG.equals(secretSrcData.getVaultType())) {
			// For registry type we only support plain text
			return parentRepository.getSecret(alias);
		} else {
			// Will never reach here unless customized
			throw new SynapseException("Unknown secret type : " + secretSrcData.getVaultType().toString());
		}
	}
}
