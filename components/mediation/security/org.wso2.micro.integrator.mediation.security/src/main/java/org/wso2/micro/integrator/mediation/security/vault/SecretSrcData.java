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

import java.io.File;

/**
 * Data holder class to hold data related to the secret
 */
public class SecretSrcData {

    private VaultType vaultType;
    private boolean isEncrypted;
    private String secretRoot;
    private static String dockerSecretRoot;
    private static String fileSecretRoot;

    static {
        String dockerSecretProp = System.getProperty(SecureVaultConstants.PROP_DOCKER_SECRET_ROOT_DIRECTORY);
        if (dockerSecretProp != null && !dockerSecretProp.trim().isEmpty()) {
            dockerSecretRoot = dockerSecretProp.trim();
        } else {
            if (System.getProperty("os.name").toLowerCase().contains("windows")) {
                dockerSecretRoot = SecureVaultConstants.PROP_DOCKER_SECRET_ROOT_DIRECTORY_DEFAULT_WIN;
            } else {
                dockerSecretRoot = SecureVaultConstants.PROP_DOCKER_SECRET_ROOT_DIRECTORY_DEFAULT;
            }
        }
        if (!dockerSecretRoot.endsWith(File.separator)) {
            dockerSecretRoot = dockerSecretRoot + File.separator;
        }
        dockerSecretRoot = SecureVaultConstants.FILE_PROTOCOL_PREFIX + dockerSecretRoot;
        String fileSecretProp = System.getProperty(SecureVaultConstants.PROP_FILE_SECRET_ROOT_DIRECTORY);
        if (fileSecretProp != null && !fileSecretProp.trim().isEmpty()) {
            fileSecretRoot = fileSecretProp.trim();
        } else {
            fileSecretRoot = SecureVaultConstants.PROP_FILE_SECRET_ROOT_DIRECTORY_DEFAULT;
        }
        if (!fileSecretRoot.endsWith(File.separator)) {
            fileSecretRoot = fileSecretRoot + File.separator;
        }
        fileSecretRoot = SecureVaultConstants.FILE_PROTOCOL_PREFIX + fileSecretRoot;
    }

    public SecretSrcData (String vaultType, boolean isEncrypted) {
        if (VaultType.DOCKER.toString().equals(vaultType)) {
            this.vaultType = VaultType.DOCKER;
            this.secretRoot = dockerSecretRoot;
        } else if (VaultType.ENV.toString().equals(vaultType)) {
            this.vaultType = VaultType.ENV;
        } else if (VaultType.FILE.toString().equals(vaultType)) {
            this.vaultType = VaultType.FILE;
            this.secretRoot = fileSecretRoot;
        }
        this.isEncrypted = isEncrypted;
    }

    public SecretSrcData() {
        // Default is registry with encrypted mode (for backward compatibility)
        this.vaultType = VaultType.REG;
        this.isEncrypted = true;
    }

    public VaultType getVaultType() {
        return vaultType;
    }

    public boolean isEncrypted() {
        return isEncrypted;
    }

    public String getSecretRoot() {
        return this.secretRoot;
    }
}
