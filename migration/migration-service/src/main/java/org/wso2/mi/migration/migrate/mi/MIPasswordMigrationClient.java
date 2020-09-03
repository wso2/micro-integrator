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

package org.wso2.mi.migration.migrate.mi;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.SynapseException;
import org.wso2.mi.migration.migrate.MigrationConstants;
import org.wso2.mi.migration.utils.MigrationIOUtils;
import org.wso2.micro.integrator.mediation.security.vault.CipherInitializer;
import org.wso2.securevault.DecryptionProvider;
import org.wso2.securevault.secret.SecretManager;

import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

/**
 * Decrypt passwords in RSA algorithm and decrypts passwords according to provided algorithm and migrate the passwords.
 */
public class MIPasswordMigrationClient {

    private String carbonHome = System.getProperty(MigrationConstants.CARBON_HOME);
    private String SECURE_VAULT_PATH = Paths.get(carbonHome, "registry", "config", "repository",
            "components", "secure-vault", "secure-vault.properties").toString();
    private String CIPHER_TEXT_PATH = Paths.get(carbonHome, "conf", "security",
            "cipher-text.properties").toString();
    // Map for secrets available in the securevault.properties file in MI-1.1.0
    private Map<String, String> secureVaultProperties;
    // Map for secrets available in the cipher-text.properties file in MI-1.1.0
    private Map<String, String> cipherTextProperties;

    private static final Log log = LogFactory.getLog(MIPasswordMigrationClient.class);

    public MIPasswordMigrationClient() {
        this.secureVaultProperties = MigrationIOUtils.getProperties(SECURE_VAULT_PATH);
        this.cipherTextProperties = MigrationIOUtils.getProperties(CIPHER_TEXT_PATH);
    }

    /**
     * Decrypt passwords using RSA and encrypt passwords and migrate passwords
     */
    public void migratePasswords() {

        MigrationIOUtils.createMigrationDirectoryIfNotExists();
        // migrate secrets in the secure-vault.properties
        if (secureVaultProperties != null && !secureVaultProperties.isEmpty()) {
            Map<String, String> decryptedSecureVaultPasswords = getSecureVaultDecrypted();
            MigrationIOUtils.writePropertiesFile("secure-vault-decrypted.properties", decryptedSecureVaultPasswords);
        }

        // migrate passwords in the cipher-text.properties
        if (cipherTextProperties != null && !cipherTextProperties.isEmpty()) {
            Map<String, String> decryptedCipherTextPasswords = getCipherTextDecrypted();
            MigrationIOUtils.writePropertiesFile("cipher-text-decrypted.properties", decryptedCipherTextPasswords);
        }
    }

    /**
     * Decrypts passwords in secure-vault.properties file.
     *
     * @return Map of decrypted secrets
     */
    private Map<String, String> getSecureVaultDecrypted() {
        DecryptionProvider decyptProvider = CipherInitializer.getInstance().getDecryptionProvider();
        if (decyptProvider == null) {
            // This cannot happen unless someone mess with OSGI references
            throw new SynapseException("Secret repository has not been initialized.");
        }
        Map<String, String> decryptedPasswords = new HashMap<>();
        this.secureVaultProperties.forEach((alias, encryptedValue) -> {
            decryptedPasswords.put(alias, new String(decyptProvider.decrypt(encryptedValue.trim().getBytes())));
        });

        return decryptedPasswords;
    }

    /**
     * Decrypts passwords in cipher-text.properties file.
     *
     * @return Map of decrypted secrets
     */
    private Map<String, String> getCipherTextDecrypted() {
        Map<String, String> decryptedPasswords = new HashMap<>();
        SecretManager manager = SecretManager.getInstance();
        this.cipherTextProperties.forEach((alias, encryptedValue) -> {
            String decyptedValue = manager.getSecret(alias);
            decryptedPasswords.put(alias, decyptedValue);
        });
        return decryptedPasswords;
    }
}
