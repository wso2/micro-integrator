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

import org.wso2.micro.integrator.security.user.core.UserStoreException;
import org.wso2.micro.integrator.security.user.core.UserStoreManager;
import org.wso2.micro.integrator.security.user.core.claim.Claim;

import java.util.Map;

public interface MultipleCredentialUserStoreManager extends UserStoreManager {

    /**
     * @param credential
     * @param roleList
     * @param claims
     * @param profileName
     */
    public void addUser(Credential credential, String[] roleList, Map<String, String> claims,
                        String profileName) throws UserStoreException;

    /**
     * @param credential
     * @param roleList
     * @param claims
     * @param profileName
     * @throws UserStoreException
     */
    public void addUsers(Credential[] credential, String[] roleList, Map<String, String> claims,
                         String profileName) throws UserStoreException;

    public void deleteUser(String identifier, String credentialType) throws UserStoreException;

    public void deleteUser(Credential credential) throws UserStoreException;

    public void addCredential(String anIdentifier, String credentialType, Credential credential)
            throws UserStoreException;

    public void updateCredential(String identifier, String credentialType, Credential credential)
            throws UserStoreException;

    public void deleteCredential(String identifier, String credentialType)
            throws UserStoreException;

    public Credential[] getCredentials(String anIdentifier, String credentialType)
            throws UserStoreException;

    public Credential[] getCredentials(Credential credential) throws UserStoreException;

    public boolean authenticate(Credential credential) throws UserStoreException;

    public String[] getRoleListOfUser(String identifer, String credentialType) throws UserStoreException;

    public void addUserWithUserId(String userId, Credential credential, String[] roleList,
                                  Map<String, String> claimMap, String profileName) throws UserStoreException;

    public String getUserId(Credential credential) throws UserStoreException;

    public void setUserClaimValues(String identifer, String credentialType, Map<String, String> claims,
                                   String profileName) throws UserStoreException;

    public void setUserClaimValue(String identifer, String credentialType, String claimURI,
                                  String claimValue, String profileName) throws UserStoreException;

    public void deleteUserClaimValue(String identifer, String credentialType, String claimURI,
                                     String profileName) throws UserStoreException;

    public void deleteUserClaimValues(String identifer, String credentialType, String[] claims,
                                      String profileName) throws UserStoreException;

    public String getUserClaimValue(String identifer, String credentialType, String claimUri,
                                    String profileName) throws UserStoreException;

    public Claim[] getUserClaimValues(String identifer, String credentialType, String[] claims,
                                      String profileName) throws UserStoreException;

    public Claim[] getUserClaimValues(String identifer, String credentialType, String profileName)
            throws UserStoreException;
}
