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

package org.wso2.mi.migration.migrate.ei;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.mediation.security.vault.CipherInitializer;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.mi.migration.internal.EIRegistryDataHolder;
import org.wso2.mi.migration.migrate.MigrationClientConfig;
import org.wso2.mi.migration.migrate.MigrationClientException;
import org.wso2.mi.migration.migrate.MigrationConstants;
import org.wso2.mi.migration.utils.MigrationIOUtils;
import org.wso2.securevault.DecryptionProvider;
import org.wso2.securevault.secret.SecretManager;

import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

/**
 * Decrypts secure vault entries in the registry and entries in the cipher-text.properties
 */
public class EIPasswordMigrationClient {

    private static final String SECURE_VAULT_PATH = MigrationConstants.SECURE_VAULT_PATH;
    private UserRegistry userRegistry;
    private Resource userRegistryResource;
    private String carbonHome = System.getProperty(MigrationConstants.CARBON_HOME);
    private String ESB_CIPHER_TEXT_PATH = Paths.get(carbonHome, "repository","conf", "security",
            "cipher-text.properties").toString();
    private String EI_CIPHER_TEXT_PATH = Paths.get(carbonHome, "conf", "security",
            "cipher-text.properties").toString();

    private static final Log log = LogFactory.getLog(EIPasswordMigrationClient.class);

    public EIPasswordMigrationClient() throws RegistryException {
        String adminUserName = MigrationClientConfig.getInstance()
                .getMigrationConfiguration()
                .getProperty(MigrationConstants.ADMIN_USERNAME);
        if (adminUserName.isEmpty()) {
            throw new MigrationClientException("Invalid admin username");
        }

        this.userRegistry = EIRegistryDataHolder.getRegistryService().getRegistry(adminUserName);
        this.userRegistryResource = userRegistry.get(SECURE_VAULT_PATH);
    }


    /**
     * filter registry properties and extract only system properties
     *
     * @param userRegistryResourceProperties resource properties
     * @return secure vault properties
     */
    private Map<String, String> getSecureVaultProperties(Properties userRegistryResourceProperties) {
        Map<String, String> systemProperties = new HashMap<>();

        Set<Object> keys = userRegistryResourceProperties.keySet();
        for (Object key: keys){
            if (!key.toString().startsWith("registry")) {
                systemProperties.put(key.toString(), userRegistryResource.getProperty(key.toString()));
            }
        }
        return systemProperties;
    }

    /**
     * decrypt  passwords using RSA algorithm
     *
     * @param secureVaultProperties secure vault property-value map
     * @return decrypted passwords
     */
    private Map<String, String> getDecryptedPasswords(Map<String, String> secureVaultProperties) {
        Map<String, String> decryptedPasswords = new HashMap<>();
        DecryptionProvider decryptionProvider = CipherInitializer.getInstance().getDecryptionProvider();
        if (decryptionProvider == null) {
            // This cannot happen unless someone mess with OSGI references
            throw new MigrationClientException("Secret repository has not been initialized.");
        }

        secureVaultProperties.forEach((alias, password) -> {
            String decryptedPassword = new String(decryptionProvider.decrypt(password.getBytes()));
            decryptedPasswords.put(alias, decryptedPassword);
        });
        return decryptedPasswords;
    }

    /**
     * Decrypt passwords using RSA and encrypt passwords and migrate passwords
     */
    public void migratePasswords(Boolean isESB) {
        MigrationIOUtils.createMigrationDirectoryIfNotExists();
        migrateSecureVaultPasswords();
        migrateCipherTextProperties(isESB);
    }

    /**
     * Writes decrypted secure-vault registry entries to a file.
     */
    private void migrateSecureVaultPasswords() {
        Properties userRegistryResourceProperties = userRegistryResource.getProperties();
        Map<String, String> secureVaultProperties = getSecureVaultProperties(userRegistryResourceProperties);
        Map<String, String> decryptedPasswords = getDecryptedPasswords(secureVaultProperties);
        MigrationIOUtils.writePropertiesFile("secure-vault-decrypted.properties", decryptedPasswords);
    }


    /**
     * Writes decrypted cipher-text.properties entries to a file.
     */
    private void migrateCipherTextProperties(Boolean isESB) {

        String cipherTextPath = ESB_CIPHER_TEXT_PATH;
        if (!isESB){
            cipherTextPath = EI_CIPHER_TEXT_PATH;
        }
        Map<String, String> cipherTextProperties =  MigrationIOUtils.getProperties(cipherTextPath);
        Map<String, String> decryptedPasswords = new HashMap<>();
        SecretManager manager = SecretManager.getInstance();
        cipherTextProperties.forEach((alias, encryptedValue) -> {
            String decyptedValue = manager.getSecret(alias);
            decryptedPasswords.put(alias, decyptedValue);
        });
        MigrationIOUtils.writePropertiesFile("cipher-text-decrypted.properties", decryptedPasswords);
    }
}
