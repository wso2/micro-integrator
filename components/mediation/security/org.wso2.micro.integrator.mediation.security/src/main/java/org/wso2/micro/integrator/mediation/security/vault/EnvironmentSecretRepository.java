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

import org.wso2.securevault.secret.SecretRepository;
import java.util.Properties;

public class EnvironmentSecretRepository implements SecretRepository {

    /* Parent secret repository */
    private SecretRepository parentRepository;

    @Override
    public void init(Properties properties, String s) {
        // Nothing to do here
    }

    @Override
    public String getSecret(String alias) {
        String rawValue = getPlainTextSecret(alias);
        return SecureVaultUtils.decryptSecret(rawValue);
    }

    /**
     * Returns the plaintext value from the environment.
     *
     * @param alias Environment variable name.
     */
    public String getPlainTextSecret(String alias) {
        String planTextSecret = System.getenv(alias);
        if (planTextSecret == null) {
            return null;
        }
        return planTextSecret.trim();
    }

    @Override
    public String getEncryptedData(String s) {
        return null;
    }

    @Override
    public void setParent(SecretRepository secretRepository) {
        parentRepository = secretRepository;
    }

    @Override
    public SecretRepository getParent() {
        return parentRepository;
    }
}
