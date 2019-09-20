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
package org.wso2.micro.integrator.security.user.core.common;

import org.wso2.micro.integrator.security.user.core.UserStoreException;
import org.wso2.micro.integrator.security.user.core.listener.ClaimManagerListener;

public class AbstractClaimManagerListener implements ClaimManagerListener {

    /**
     * Get the execution order identifier for this listener.
     *
     * @return The execution order identifier integer value.
     */
    @Override
    public int getExecutionOrderId() {
        return 5;
    }

    /**
     * Retrieves the attribute name of the claim URI.
     *
     * @param claimURI The claim URI
     * @return - Whether execution of this method of the underlying
     * ClaimManager must happen.
     * @throws org.wso2.micro.integrator.security.user.core.UserStoreException
     */
    @Override
    public boolean getAttributeName(String claimURI) throws UserStoreException {
        return true;
    }

    /**
     * The Claim object of the claim URI
     *
     * @param claimURI The claim URI
     * @return - Whether execution of this method of the underlying
     * ClaimManager must happen.
     * @throws org.wso2.micro.integrator.security.user.core.UserStoreException
     */
    @Override
    public boolean getClaim(String claimURI) throws UserStoreException {
        return true;
    }

    /**
     * Gets the claim mapping.
     *
     * @param claimURI The claim URI
     * @return - Whether execution of this method of the underlying
     * ClaimManager must happen.
     * @throws org.wso2.micro.integrator.security.user.core.UserStoreException
     */
    @Override
    public boolean getClaimMapping(String claimURI) throws UserStoreException {
        return true;
    }

    /**
     * Gets all supported claims by default in the system.
     *
     * @return - Whether execution of this method of the underlying
     * ClaimManager must happen.
     * @throws org.wso2.micro.integrator.security.user.core.UserStoreException
     */
    @Override
    public boolean getAllSupportClaimMappingsByDefault() throws UserStoreException {
        return true;
    }

    /**
     * Gets all claim objects
     *
     * @return - Whether execution of this method of the underlying
     * ClaimManager must happen.
     * @throws org.wso2.micro.integrator.security.user.core.UserStoreException
     */
    @Override
    public boolean getAllClaimMappings() throws UserStoreException {
        return true;
    }

    /**
     * Gets all claims in the dialect
     *
     * @param dialectUri The dialect URI
     * @return - Whether execution of this method of the underlying
     * ClaimManager must happen.
     * @throws org.wso2.micro.integrator.security.user.core.UserStoreException
     */
    @Override
    public boolean getAllClaimMappings(String dialectUri) throws UserStoreException {
        return true;
    }

    /**
     * Gets all mandatory claims
     *
     * @return - Whether execution of this method of the underlying
     * ClaimManager must happen.
     * @throws org.wso2.micro.integrator.security.user.core.UserStoreException
     */
    @Override
    public boolean getAllRequiredClaimMappings() throws UserStoreException {
        return true;
    }

    /**
     * Gets all claim URIs
     *
     * @return - Whether execution of this method of the underlying
     * ClaimManager must happen.
     * @throws org.wso2.micro.integrator.security.user.core.UserStoreException
     */
    @Override
    public boolean getAllClaimUris() throws UserStoreException {
        return true;
    }

    /**
     * Adds a new claim mapping
     *
     * @param mapping The claim mapping to be added
     * @return - Whether execution of this method of the underlying
     * ClaimManager must happen.
     * @throws org.wso2.micro.integrator.security.user.core.UserStoreException
     */
    @Override
    public boolean addNewClaimMapping(org.wso2.micro.integrator.security.user.api.ClaimMapping mapping) throws UserStoreException {
        return true;
    }

    /**
     * Deletes a claim mapping
     *
     * @param mapping The claim mapping to be deleted
     * @return - Whether execution of this method of the underlying
     * ClaimManager must happen.
     * @throws org.wso2.micro.integrator.security.user.core.UserStoreException
     */
    @Override
    public boolean deleteClaimMapping(org.wso2.micro.integrator.security.user.api.ClaimMapping mapping) throws UserStoreException {
        return true;
    }

    /**
     * Updates a claim mapping
     *
     * @param mapping The claim mapping to be updated
     * @return - Whether execution of this method of the underlying
     * ClaimManager must happen.
     * @throws org.wso2.micro.integrator.security.user.core.UserStoreException
     */
    @Override
    public boolean updateClaimMapping(org.wso2.micro.integrator.security.user.api.ClaimMapping mapping) throws UserStoreException {
        return true;
    }

    /**
     * Retrieves the attribute name of the claim URI
     *
     * @param domainName
     * @param claimURI
     * @return - Whether execution of this method of the underlying
     * ClaimManager must happen.
     * @throws org.wso2.micro.integrator.security.user.core.UserStoreException
     */
    @Override
    public boolean getAttributeName(String domainName, String claimURI) throws UserStoreException {
        return true;
    }
}
