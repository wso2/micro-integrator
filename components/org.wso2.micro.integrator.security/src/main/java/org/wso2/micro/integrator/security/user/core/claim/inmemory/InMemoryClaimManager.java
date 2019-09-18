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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.micro.integrator.security.user.api.Claim;
import org.wso2.micro.integrator.security.user.core.UserStoreException;
import org.wso2.micro.integrator.security.user.core.claim.ClaimKey;
import org.wso2.micro.integrator.security.user.core.claim.ClaimManager;
import org.wso2.micro.integrator.security.user.core.claim.ClaimMapping;
import org.wso2.micro.integrator.security.user.core.claim.DefaultClaimManager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.xml.stream.XMLStreamException;

public class InMemoryClaimManager implements ClaimManager {

    private static final Log log = LogFactory.getLog(DefaultClaimManager.class);

    public ClaimConfig getClaimConfig() {
        return claimConfig;
    }

    public void setClaimConfig(ClaimConfig claimConfig) {
        this.claimConfig = claimConfig;
    }

    protected static ClaimConfig claimConfig;
    protected Map<String, ClaimMapping> claimMapping = new HashMap<>();

    static {
        try {
            claimConfig = FileBasedClaimBuilder.buildClaimMappingsFromConfigFile();
        } catch (IOException e) {
            log.error("Could not find claim configuration file", e);
        } catch (XMLStreamException e) {
            log.error("Error while parsing claim configuration file", e);
        } catch (UserStoreException e) {
            log.error("Error while initializing claim manager");
        }
    }


    public InMemoryClaimManager() throws UserStoreException {
        //Convert the new data structure to the existing model to make this backward compatible.
        Map<ClaimKey, ClaimMapping> tmpClaimMap = claimConfig.getClaimMap();
        for (Map.Entry<ClaimKey, ClaimMapping> entry : tmpClaimMap.entrySet()) {
            claimMapping.put(entry.getKey().getClaimUri(), entry.getValue());
        }
    }

    /**
     * @param domainName
     * @param claimURI
     * @return
     * @throws UserStoreException
     */
    @Override
    public String getAttributeName(String domainName, String claimURI) throws UserStoreException {
        ClaimMapping mapping = claimMapping.get(claimURI);

        if (mapping != null) {
            if (domainName != null) {
                String mappedAttrib = mapping.getMappedAttribute(domainName.toUpperCase());
                if (mappedAttrib != null) {
                    return mappedAttrib;
                }
                return mapping.getMappedAttribute();
            } else {
                return mapping.getMappedAttribute();
            }
        }
        return null;
    }

    @Override
    public String getAttributeName(String claimURI) throws UserStoreException {
        ClaimMapping mapping = claimMapping.get(claimURI);
        if (mapping != null) {
            return mapping.getMappedAttribute();
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String[] getAllClaimUris() throws UserStoreException {
        return claimMapping.keySet().toArray(new String[claimMapping.size()]);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Claim getClaim(String claimURI) throws UserStoreException {
        ClaimMapping mapping = claimMapping.get(claimURI);
        if (mapping != null) {
            return mapping.getClaim();
        }
        return null;
    }

    /**
     * @param claimURI
     */
    @Override
    public ClaimMapping getClaimMapping(String claimURI) throws UserStoreException {
        return claimMapping.get(claimURI);
    }

    @Override
    public ClaimMapping[] getAllSupportClaimMappingsByDefault() throws org.wso2.micro.integrator.security.user.api.UserStoreException {
        List<ClaimMapping> claimList = new ArrayList<>();

        for (Map.Entry<String, ClaimMapping> entry : claimMapping.entrySet()) {
            ClaimMapping claimMapping = entry.getValue();
            org.wso2.micro.integrator.security.user.core.claim.Claim claim = claimMapping.getClaim();
            if (claim.isSupportedByDefault()) {
                claimList.add(claimMapping);
            }
        }
        return claimList.toArray(new ClaimMapping[claimList.size()]);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ClaimMapping[] getAllClaimMappings() throws UserStoreException {
        List<ClaimMapping> claimList = new ArrayList<>();

        for (Map.Entry<String, ClaimMapping> entry : claimMapping.entrySet()) {
            ClaimMapping claimMapping = entry.getValue();
            claimList.add(claimMapping);
        }
        return claimList.toArray(new ClaimMapping[claimList.size()]);
    }

    @Override
    public ClaimMapping[] getAllClaimMappings(String dialectUri) throws org.wso2.micro.integrator.security.user.api.UserStoreException {
        List<ClaimMapping> claimList = new ArrayList<>();

        for (Map.Entry<String, ClaimMapping> entry : claimMapping.entrySet()) {
            ClaimMapping claimMapping = entry.getValue();
            if (claimMapping.getClaim().getDialectURI().equals(dialectUri)) {
                claimList.add(claimMapping);
            }
        }
        return claimList.toArray(new ClaimMapping[claimList.size()]);
    }

    @Override
    public ClaimMapping[] getAllRequiredClaimMappings() throws UserStoreException {
        List<ClaimMapping> claimList = new ArrayList<>();

        for (Map.Entry<String, ClaimMapping> entry : claimMapping.entrySet()) {
            ClaimMapping claimMapping = entry.getValue();
            if (claimMapping.getClaim().isRequired()) {
                claimList.add(claimMapping);
            }
        }
        return claimList.toArray(new ClaimMapping[claimList.size()]);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addNewClaimMapping(org.wso2.micro.integrator.security.user.api.ClaimMapping mapping)
            throws UserStoreException{
        claimMapping.put(mapping.getClaim().getClaimUri(), (ClaimMapping) mapping);
    }

    /**
     * {@inheritDoc}
     */
    public void updateClaimMapping(org.wso2.micro.integrator.security.user.api.ClaimMapping mapping)
            throws UserStoreException{
        claimMapping.remove(mapping.getClaim().getClaimUri());
        claimMapping.put(mapping.getClaim().getClaimUri(), (ClaimMapping) mapping);
    }
    /**
     * {@inheritDoc}
     */
    public void deleteClaimMapping(org.wso2.micro.integrator.security.user.api.ClaimMapping mapping) throws UserStoreException {
        claimMapping.remove(mapping.getClaim().getClaimUri());
    }
}
