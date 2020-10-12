/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.

 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at

 * http://www.apache.org/licenses/LICENSE-2.0

 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.micro.integrator.mediation.security.vault.external;

import org.apache.axiom.om.OMElement;
import org.apache.commons.lang3.StringUtils;
import org.wso2.carbon.securevault.SecretCallbackHandlerService;
import org.wso2.config.mapper.ConfigParser;
import org.wso2.securevault.SecretResolver;
import org.wso2.securevault.SecretResolverFactory;
import org.wso2.securevault.commons.MiscellaneousUtil;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * {@code ExternalVaultLoader} contains utilities to load configuration file content required for External vault loader
 * implementation.
 */
public class ExternalVaultConfigLoader {

    private static final String EXTERNAL_VAULT_CONFIG = "external_vault";
    private static final String EXTERNAL_VAULT_NAME = "name";

    private static SecretResolver secretResolver;

    private static Map<String, Map<String, String>> externalVaultMap = new HashMap<>();

    private ExternalVaultConfigLoader() {}

    /**
     * Reads the external-vaults.xml file located in conf/security dir and loads to the memory.
     *
     * @param secretCallbackHandlerService secret callback handler
     */
    public static void loadExternalVaultConfigs(SecretCallbackHandlerService secretCallbackHandlerService) {

        Map<String, Object> configs = ConfigParser.getParsedConfigs();
        List<Object> externalVaultProperties = (List<Object>) configs.get(EXTERNAL_VAULT_CONFIG);

        if (externalVaultProperties != null) {
            for (int x = 0; x < externalVaultProperties.size(); x++) {
                Map<String, String> vaultProperties = (Map<String, String>) externalVaultProperties.get(x);

                Map<String, String> childParameters = new HashMap<>();
                String currentVaultName = null;
                for (Map.Entry<String, String> entry : vaultProperties.entrySet()) {
                    if (entry.getKey().equals(EXTERNAL_VAULT_NAME)) {
                        currentVaultName = entry.getValue();
                    } else {
                        String resolvedValue = resolveSecret(entry.getValue(), secretCallbackHandlerService);
                        childParameters.put(entry.getKey(), resolvedValue);
                    }
                }
                externalVaultMap.put(currentVaultName, childParameters);
            }
        }
    }

    /**
     * Get the external vault configuration map based on the given vault name.
     *
     * @param name vault name
     */
    public static Map<String, String> getVaultParameters(String name) {
        return externalVaultMap.get(name);
    }

    /**
     * Checks if the text is protected and returns decrypted text if protected, else returns the plain text
     * @param text value to resolve
     * @param secretCallbackHandlerService secret callback handler
     * @return Decrypted text if protected else plain text
     */
    private static String resolveSecret(String text, SecretCallbackHandlerService secretCallbackHandlerService) {
        String alias = MiscellaneousUtil.getProtectedToken(text);
        if (!StringUtils.isEmpty(alias)) {
            if (secretResolver == null) {
                secretResolver = SecretResolverFactory.create((OMElement) null, false);
            }
            if (!secretResolver.isInitialized()) {
                secretResolver.init(secretCallbackHandlerService.getSecretCallbackHandler());
            }
            return secretResolver.resolve(alias);
        }
        return text;
    }
}
