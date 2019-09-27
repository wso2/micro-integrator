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
package org.wso2.micro.integrator.management.apis.security.handler;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This singleton class holds the in memory token store
 */
public class JWTInMemoryTokenStore extends JWTTokenStore {

    private static Log LOG = LogFactory.getLog(JWTInMemoryTokenStore.class);

    private static JWTInMemoryTokenStore JWT_IN_MEMORY_TOKEN_STORE_INSTANCE = null;

    private static Map<String, JWTTokenInfoDTO> tokenStore;

    private int storeSize = AuthConstants.JWT_TOKEN_STORE_DEFAULT_SIZE;

    private JWTInMemoryTokenStore() {
    }

    /**
     * get store instance with defined store size
     * @param storeSize size limit of the token store
     * @return JWTInMemoryTokenStore singleton instance
     */
    public static JWTInMemoryTokenStore getInstance(int storeSize) {
        if (JWT_IN_MEMORY_TOKEN_STORE_INSTANCE == null ) {
            JWT_IN_MEMORY_TOKEN_STORE_INSTANCE = new JWTInMemoryTokenStore();
            JWT_IN_MEMORY_TOKEN_STORE_INSTANCE.setStoreSize(storeSize);
            setTokenStore(new ConcurrentHashMap<String, JWTTokenInfoDTO>());
        }
        return JWT_IN_MEMORY_TOKEN_STORE_INSTANCE;
    }

    /**
     * get store instance with default store size
     * @return JWTInMemoryTokenStore singleton instance
     */
    public static JWTInMemoryTokenStore getInstance() {
        if (JWT_IN_MEMORY_TOKEN_STORE_INSTANCE == null ) {
            //Create store with default size
            return getInstance(AuthConstants.JWT_TOKEN_STORE_DEFAULT_SIZE);
        } else {
            //Already initialized so store size won't be considered
            return getInstance(0);
        }
    }

    private Map<String, JWTTokenInfoDTO> getTokenStore() {
        return tokenStore;
    }

    private static void setTokenStore(Map<String, JWTTokenInfoDTO> tokenStore) {
        JWTInMemoryTokenStore.tokenStore = tokenStore;
    }

    @Override
    public JWTTokenInfoDTO getToken(String token) {
        return getTokenStore().get(token);
    }

    @Override
    public boolean putToken(String token, JWTTokenInfoDTO jwtTokenInfoDTO) {
        if (getTokenStore().size() < storeSize) { //Limit store size to avoid memory growth
            LOG.debug("New token added to token store");
            getTokenStore().put(token, jwtTokenInfoDTO);
            return true;
        } else {
            if (JWTConfig.getInstance().getJwtConfigDto().isRemoveOldestElementOnOverflow()) {
                LOG.info("Token store exhausted. Retrying after cleaning up the store");
                cleanupStore();
                if(putToken(token, jwtTokenInfoDTO)) {
                    return true;
                }
            }
            LOG.warn("Token store exhausted. Please increase the token store size");
            return false;
        }
    }


    @Override
    public boolean revokeToken(String token) {
        JWTTokenInfoDTO  jwtToken = getToken(token);
        if (jwtToken != null) {
            removeToken(token);
            return true;
        } else {
            LOG.debug("Token is expired or not available in the token store");
            return false;
        }
    }

    @Override
    public void removeToken(String token) {
        getTokenStore().remove(token);
    }

    public int getStoreSize() {
        return storeSize;
    }

    private void setStoreSize(int storeSize) {
        this.storeSize = storeSize;
    }

    @Override
    public void removeExpired() {
        LOG.debug("Removing expired tokens from token store");
        Iterator<String> tokenIterator = getTokenStore().keySet().iterator();
        while (tokenIterator.hasNext()) {
            String key = tokenIterator.next();
            if ((getTokenStore().get(key).getExpiry() < System.currentTimeMillis()) || getTokenStore().get(key).isRevoked()) {
                tokenIterator.remove(); //Remove if token is expired or revoked
            }
        }
    }

    @Override
    public void cleanupStore() {
        // Current cleanup logic is to remove the token with oldest access time
        LOG.debug("Removing oldest accessed token from store");
        Iterator<String> tokenIterator = getTokenStore().keySet().iterator();
        long leastAccessTimeStamp = System.currentTimeMillis();
        String leastAccessedToken = null;
        while (tokenIterator.hasNext()) {
            String key = tokenIterator.next();
            long tokenTimeStamp = getTokenStore().get(key).getLastAccess();
            if (tokenTimeStamp < leastAccessTimeStamp) {
                leastAccessTimeStamp = tokenTimeStamp;
                leastAccessedToken = key;
            }
        }
        getTokenStore().remove(leastAccessedToken);
    }

    @Override
    public int getCurrentSize() {
        return getTokenStore().size();
    }
}
