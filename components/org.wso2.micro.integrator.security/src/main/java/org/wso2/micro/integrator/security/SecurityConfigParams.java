/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.micro.integrator.security;

import org.wso2.micro.integrator.core.services.CarbonServerConfigurationService;

/**
 * Holds the information available in Carbon Security configuration element in the Security Policy.
 */
public class SecurityConfigParams {

    private String privateStore;
    private String trustStores;
    private String allowedRoles;
    private String keyAlias;
    private String serverPrincipalPassword;
    private boolean isServerPrincipalPasswordEncrypted = true;

    /**
     * Get the private key store
     *
     * If the key store is defined in the Security configuration take it from there otherwise
     * key store is taken from the Server Configuration
     *
     * @return private key store
     */
    public String getPrivateStore() {

        if (privateStore == null) {
            CarbonServerConfigurationService serverConfig = CarbonServerConfigurationService.getInstance();
            String pvtStore = serverConfig.getFirstProperty("Security.KeyStore.Location");
            return pvtStore.substring(pvtStore.lastIndexOf("/") + 1);
        }
        return privateStore;
    }

    /**
     * Set the private store
     *
     * @param privateStore private store
     */
    public void setPrivateStore(String privateStore) {
        this.privateStore = privateStore;
    }

    /**
     * Get trusted stores
     *
     * @return trusted stores as comma separated value
     */
    public String getTrustStores() {
        return trustStores;
    }

    /**
     * Set the trusted stores
     *
     * @param trustStores trusted stores as comma separated value
     */
    public void setTrustStores(String trustStores) {
        this.trustStores = trustStores;
    }

    /**
     * Get authorized roles
     *
     * @return authorized roles as a comma separated value
     */
    public String getAllowedRoles() {
        return allowedRoles;
    }

    /**
     * Set authorized roles
     *
     * @param allowedRoles authorized roles as a comma separated value
     */
    public void setAllowedRoles(String allowedRoles) {
        this.allowedRoles = allowedRoles;
    }

    /**
     * Get the private key alias
     *
     * If the key alias is defined in the Security configuration take it from there otherwise
     * key alias is taken from the Server Configuration
     *
     * @return private key key alias
     */
    public String getKeyAlias() {
        if (keyAlias == null) {
            CarbonServerConfigurationService serverConfig = CarbonServerConfigurationService.getInstance();
            return serverConfig.getFirstProperty("Security.KeyStore.KeyAlias");
        }
        return keyAlias;
    }

    /**
     * Set the private key alias
     *
     * @param keyAlias key alias
     */
    public void setKeyAlias(String keyAlias) {
        this.keyAlias = keyAlias;
    }

    /**
     * Get the server principal password for kerberos authentication
     *
     * @return server principal password
    */
    public String getServerPrincipalPassword() {
        return serverPrincipalPassword;
    }

    /**
     * Set the server principal password for kerberos authentication
     *
     * @param serverPrincipalPassword server principal password
     */
    public void setServerPrincipalPassword(String serverPrincipalPassword) {
        this.serverPrincipalPassword = serverPrincipalPassword;
    }

    /**
     * Whether server principal password for kerberos authentication is encrypted
     * If the parameter is not set, default value is true
     *
     * @return whether server principal password is encrypted
     */
    public boolean isServerPrincipalPasswordEncrypted() {
        return isServerPrincipalPasswordEncrypted;
    }

    /**
     * Set server principal password for kerberos authentication is encrypted or not
     *
     * @param isServerPrincipalPasswordEncrypted whether server principal password is encrypted
     */
    public void setServerPrincipalPasswordEncrypted(boolean isServerPrincipalPasswordEncrypted) {
        this.isServerPrincipalPasswordEncrypted = isServerPrincipalPasswordEncrypted;
    }
}
