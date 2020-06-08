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

import org.wso2.securevault.secret.SecretManager;
import org.wso2.securevault.secret.SecretRepository;


import java.util.Properties;

public class CiphertextRepository implements SecretRepository {

    /* Parent secret repository */
    private SecretRepository parentRepository;
    private static CiphertextRepository instance = null;

    private CiphertextRepository() {
    }

    public static CiphertextRepository getInstance() {
        if (instance == null) {
            instance = new CiphertextRepository();
        }
        return instance;
    }

    @Override
    public void init(Properties properties, String s) {
        // nothing to to here
    }

    @Override
    public String getSecret(String alias) {
        SecretManager secretManager = SecretManager.getInstance();
        return secretManager.getSecret(alias);
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
        return this.parentRepository;
    }
}
