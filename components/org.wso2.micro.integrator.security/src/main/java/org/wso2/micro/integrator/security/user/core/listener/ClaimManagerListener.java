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
package org.wso2.micro.integrator.security.user.core.listener;


import org.wso2.micro.integrator.security.user.api.ClaimMapping;
import org.wso2.micro.integrator.security.user.core.UserStoreException;

public interface ClaimManagerListener {

    /**
     * Get the execution order identifier for this listener.
     *
     * @return The execution order identifier integer value.
     */
    int getExecutionOrderId();

    /**
     * Retrieves the attribute name of the claim URI.
     *
     * @param claimURI The claim URI
     * @return - Whether execution of this method of the underlying
     * ClaimManager must happen.
     * @throws UserStoreException
     */
    boolean getAttributeName(String claimURI) throws UserStoreException;

    /**
     * The Claim object of the claim URI
     *
     * @param claimURI The claim URI
     * @return - Whether execution of this method of the underlying
     * ClaimManager must happen.
     * @throws UserStoreException
     */
    boolean getClaim(String claimURI) throws UserStoreException;

    /**
     * Gets the claim mapping.
     *
     * @param claimURI The claim URI
     * @return - Whether execution of this method of the underlying
     * ClaimManager must happen.
     * @throws UserStoreException
     */
    boolean getClaimMapping(String claimURI) throws UserStoreException;

    /**
     * Gets all supported claims by default in the system.
     *
     * @return - Whether execution of this method of the underlying
     * ClaimManager must happen.
     * @throws UserStoreException
     */
    boolean getAllSupportClaimMappingsByDefault() throws UserStoreException;

    /**
     * Gets all claim objects
     *
     * @return - Whether execution of this method of the underlying
     * ClaimManager must happen.
     * @throws UserStoreException
     */
    boolean getAllClaimMappings() throws UserStoreException;

    /**
     * Gets all claims in the dialect
     *
     * @param dialectUri The dialect URI
     * @return - Whether execution of this method of the underlying
     * ClaimManager must happen.
     * @throws UserStoreException
     */
    boolean getAllClaimMappings(String dialectUri) throws UserStoreException;

    /**
     * Gets all mandatory claims
     *
     * @return - Whether execution of this method of the underlying
     * ClaimManager must happen.
     * @throws UserStoreException
     */
    boolean getAllRequiredClaimMappings() throws UserStoreException;

    /**
     * Gets all claim URIs
     *
     * @return - Whether execution of this method of the underlying
     * ClaimManager must happen.
     * @throws UserStoreException
     */
    boolean getAllClaimUris() throws UserStoreException;

    /**
     * Adds a new claim mapping
     *
     * @param mapping The claim mapping to be added
     * @return - Whether execution of this method of the underlying
     * ClaimManager must happen.
     * @throws UserStoreException
     */
    boolean addNewClaimMapping(ClaimMapping mapping) throws UserStoreException;

    /**
     * Deletes a claim mapping
     *
     * @param mapping The claim mapping to be deleted
     * @return - Whether execution of this method of the underlying
     * ClaimManager must happen.
     * @throws UserStoreException
     */
    boolean deleteClaimMapping(ClaimMapping mapping) throws UserStoreException;

    /**
     * Updates a claim mapping
     *
     * @param mapping The claim mapping to be updated
     * @return - Whether execution of this method of the underlying
     * ClaimManager must happen.
     * @throws UserStoreException
     */
    boolean updateClaimMapping(ClaimMapping mapping) throws UserStoreException;

    /**
     * Retrieves the attribute name of the claim URI
     *
     * @param domainName
     * @param claimURI
     * @return - Whether execution of this method of the underlying
     * ClaimManager must happen.
     * @throws UserStoreException
     */
    boolean getAttributeName(String domainName, String claimURI) throws UserStoreException;
}
