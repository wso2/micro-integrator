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

package org.wso2.micro.integrator.security.user.core.claim.inmemory;

import org.wso2.micro.integrator.security.user.core.claim.ClaimKey;
import org.wso2.micro.integrator.security.user.core.claim.ClaimMapping;

import java.util.HashMap;
import java.util.Map;

/**
 * ClaimConfig class used to create ClaimConfig object which is containing claims and properties of those claims. After
 * reading the claim-config.xml file, which is done inside FileBasedClaimBuilder class. ClaimConfig will be created.
 */

public class ClaimConfig {

    /**
     * contains claim uri as the key and claim mapping of each claim as the value.
     */
    private Map<ClaimKey, ClaimMapping> claims;

    /**
     * inside map contains meta data value as the key and value of that meta data as the value (including claim dialect
     * info). PropertyHolder map contains that meta data map and the related claim uri as key value.
     */
    private Map<ClaimKey, Map<String, String>> propertyHolder;

    public ClaimConfig() {

    }

    /**
     * Contains the claims and the related meta data info.
     *
     * @param claims         contains claim uri as the key and claim mapping of each claim as the value.
     * @param propertyHolder inside map contains meta data value as the key and value of that meta data as the value
     *                       (including claim
     *                       dialect info). PropertyHolder map contains that meta data map and the related claim uri
     *                       as key value.
     */
    public ClaimConfig(Map<String, ClaimMapping> claims, Map<String, Map<String, String>> propertyHolder) {
        setClaims(claims);
        setPropertyHolder(propertyHolder);
    }

    /**
     * Get the Claims.
     * This method is deprecated because of this will not support for multiple claim uri across the dialect.
     * To support that, we can use 'getClaims' instead of this method.
     *
     * @return
     */
    public Map<String, ClaimMapping> getClaims() {
        Map<String, ClaimMapping> convertedClaimMap = new HashMap<>();
        for (Map.Entry<ClaimKey, ClaimMapping> entry : claims.entrySet()) {
            convertedClaimMap.put(entry.getKey().getClaimUri(), entry.getValue());
        }
        return convertedClaimMap;
    }

    /**
     * Set the claim map.
     * <p>
     * This method is deprecated because of this will not support for multiple claim uri across the dialect.
     * To support that, we can use 'setClaimMap' instead of this method.
     *
     * @param claims
     */
    public void setClaims(Map<String, ClaimMapping> claims) {
        this.claims = new HashMap<>();
        for (Map.Entry<String, ClaimMapping> entry : claims.entrySet()) {
            ClaimKey claimKey = new ClaimKey(entry.getKey(), entry.getValue().getClaim().getDialectURI());
            this.claims.put(claimKey, entry.getValue());
        }
    }

    /**
     * Get PropertyHolder.
     * This method is deprecated because of this will not support for multiple claim uri across the dialect.
     * To support that, we can use 'getPropertyHolderMap' instead of this method.
     *
     * @return
     */
    public Map<String, Map<String, String>> getPropertyHolder() {
        Map<String, Map<String, String>> convertedPropertyHolder = new HashMap<>();
        for (Map.Entry<ClaimKey, Map<String, String>> entry : propertyHolder.entrySet()) {
            convertedPropertyHolder.put(entry.getKey().getClaimUri(), entry.getValue());
        }
        return convertedPropertyHolder;
    }

    /**
     * Set Property Holder.
     * This method is deprecated because of this will not support for multiple claim uri across the dialect.
     * To support that, we can use 'setPropertyHolderMap' instead of this method.
     *
     * @param propertyHolder
     */
    @Deprecated
    public void setPropertyHolder(Map<String, Map<String, String>> propertyHolder) {
        this.propertyHolder = new HashMap<>();
        for (Map.Entry<String, Map<String, String>> entry : propertyHolder.entrySet()) {
            ClaimKey claimKey = new ClaimKey();
            claimKey.setClaimUri(entry.getKey());
            claimKey.setDialectUri(claims.get(entry.getKey()).getClaim().getDialectURI());
            this.propertyHolder.put(claimKey, entry.getValue());
        }
    }


    /**
     * This is for get claim mappings for all the uri's.
     *
     * @return
     */
    public Map<ClaimKey, ClaimMapping> getClaimMap() {
        return claims;
    }

    /**
     * Set the claim uri's againse claim mapping.
     *
     * @param claims
     */
    public void setClaimMap(Map<ClaimKey, ClaimMapping> claims) {
        this.claims = claims;
    }

    /**
     * Get the claim properties.
     *
     * @return
     */
    public Map<ClaimKey, Map<String, String>> getPropertyHolderMap() {
        return propertyHolder;
    }

    /**
     * Set the claim properties.
     *
     * @param propertyHolder
     */
    public void setPropertyHolderMap(Map<ClaimKey, Map<String, String>> propertyHolder) {
        this.propertyHolder = propertyHolder;
    }
}
