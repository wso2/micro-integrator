/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.micro.integrator.security.user.core.multiplecredentials;

/**
 * The credential type interface
 */
public interface CredentialType {

    public void add(String userId, Credential credential) throws MultipleCredentialsException;

    public void update(String userId, Credential newCredential) throws MultipleCredentialsException;

    public boolean authenticate(Credential credential) throws MultipleCredentialsException;

    public void delete(Credential credential) throws MultipleCredentialsException;

    public Credential get(String identifier) throws MultipleCredentialsException;

    public void activate(String identifier) throws MultipleCredentialsException;

    public void deactivate(String identifier) throws MultipleCredentialsException;

    public boolean isActive(String identifier) throws MultipleCredentialsException;

    public String getCredentialTypeName();

    void setCredentialTypeName(String credentialTypeName);

    /**
     * Returns true if this credential type contains null passwords.
     *
     * @return
     */
    public boolean isNullSecretAllowed();
}
