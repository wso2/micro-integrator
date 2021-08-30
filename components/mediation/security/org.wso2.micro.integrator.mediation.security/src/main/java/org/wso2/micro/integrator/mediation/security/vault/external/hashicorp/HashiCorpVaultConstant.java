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

package org.wso2.micro.integrator.mediation.security.vault.external.hashicorp;

public class HashiCorpVaultConstant {

    private HashiCorpVaultConstant() {}

    static final String CARBON_HOME_VARIABLE = "${carbon.home}";
    static final String NUMBER_REGEX = "[0-9]+";

    static final String VAULT_NAMESPACE_PARAMETER = "vault-namespace";
    static final String PATH_PARAMETER = "path-parameter";
    static final String FIELD_PARAMETER = "field-parameter";

    static final String ADDRESS_PARAMETER = "address";
    static final String TOKEN_PARAMETER = "rootToken";
    static final String ROLE_ID_PARAMETER = "roleId";
    static final String SECRET_ID_PARAMETER = "secretId";
    static final String LDAP_USERNAME_PARAMETER = "ldapUsername";
    static final String LDAP_PASSWORD_PARAMETER = "ldapPassword";
    static final String ENGINE_TYPE_PARAMETER = "engineVersion";
    static final String CACHEABLE_DURATION_PARAMETER = "cacheableDuration";
    static final String NAMESPACE_PARAMETER = "namespace";
    static final String TRUST_STORE_PARAMETER = "trustStoreFile";
    static final String KEY_STORE_PARAMETER = "keyStoreFile";
    static final String KEY_STORE_PASSWORD_PARAMETER = "keyStorePassword";
    static final String HTTPS_PARAMETER = "https";
    static final long LEAST_TTL_VALUE = -2000L;
}
