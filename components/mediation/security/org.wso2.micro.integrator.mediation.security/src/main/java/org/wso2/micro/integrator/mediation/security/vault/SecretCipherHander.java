/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.mediation.security.vault;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Entry point for manage secrets
 */
public class SecretCipherHander {

	private static Log log = LogFactory.getLog(SecretCipherHander.class);

	/* Root Secret Repository */
	private RegistrySecretRepository parentRepository = new RegistrySecretRepository();

	private org.apache.synapse.MessageContext synCtx;

	CipherInitializer ciperInitializer = CipherInitializer.getInstance();

	public SecretCipherHander(org.apache.synapse.MessageContext synCtx) {
		super();
		this.synCtx = synCtx;
		parentRepository.setSynCtx(synCtx);
	}

	/**
	 * Returns the secret corresponding to the given alias name
	 * 
	 * @param alias
	 *            The logical or alias name
	 * @return If there is a secret , otherwise , alias itself
	 */
	public String getSecret(String alias) {
		return parentRepository.getSecret(alias);
	}

	/**
	 * Returns the encrypted value corresponding to the given alias name
	 * 
	 * @param alias
	 *            The logical or alias name
	 * @return If there is a encrypted value , otherwise , alias itself
	 */
	public String getEncryptedData(String alias) {
		return parentRepository.getEncryptedData(alias);

	}

	public void shoutDown() {
		this.parentRepository = null;

	}

	public org.apache.synapse.MessageContext getSynCtx() {
		return synCtx;
	}

	public void setSynCtx(org.apache.synapse.MessageContext synCtx) {
		this.synCtx = synCtx;
	}

}