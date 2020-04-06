/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.micro.integrator.mediation.security.vault;

import org.apache.synapse.SynapseException;
import org.wso2.securevault.DecryptionProvider;

public class SecureVaultUtils {

    /**
     * Returns the decrypted value for a secret.
     *
     * @param encryptedValue encrypted password text
     */
    public static String decryptSecret(String encryptedValue) {
        DecryptionProvider decyptProvider = CipherInitializer.getInstance().getDecryptionProvider();
        if (encryptedValue == null || encryptedValue.isEmpty()) {
          return encryptedValue;
        }
        if (decyptProvider == null) {
            // This cannot happen unless someone mess with OSGI references
            throw new SynapseException("Secret repository has not been initialized.");
        }
        return new String(decyptProvider.decrypt(encryptedValue.trim().getBytes()));
    }
}
