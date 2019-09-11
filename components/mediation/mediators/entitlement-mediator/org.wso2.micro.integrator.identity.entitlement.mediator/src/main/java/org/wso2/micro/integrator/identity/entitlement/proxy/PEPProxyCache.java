/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.micro.integrator.identity.entitlement.proxy;

import java.util.Calendar;
import java.util.LinkedHashMap;
import java.util.Map.Entry;
import javax.cache.Cache;
import javax.cache.CacheManager;
import javax.cache.Caching;

class PEPProxyCache {

    private SimpleCache<String, EntitlementDecision> simpleCache;
    private boolean isCarbonCache = false;
    private int invalidationInterval = 0;

    PEPProxyCache(String enableCaching, int invalidationInterval, int maxEntries) {
        if ("simple".equalsIgnoreCase(enableCaching)) {
            simpleCache = new SimpleCache<String, EntitlementDecision>(maxEntries);
            this.invalidationInterval = invalidationInterval;
        } else if ("carbon".equalsIgnoreCase(enableCaching)) {
            isCarbonCache = true;
        }
    }

    /**
     * Return an instance of a named cache that is common to all tenants.
     *
     * @param name the name of the cache.
     * @return the named cache instance.
     */
    private Cache<IdentityCacheKey, IdentityCacheEntry> getCommonCache() {
        // TODO Should verify the cache creation done per tenant or as below

        // We create a single cache for all tenants. It is not a good choice to create per-tenant
        // caches in this case. We qualify tenants by adding the tenant identifier in the cache key.

        CacheManager manager = Caching.getCacheManagerFactory().getCacheManager(ProxyConstants.DECISION_CACHE);
        return manager.getCache(ProxyConstants.DECISION_CACHE);
    }

    void put(String key, String entry) {
        if (simpleCache != null) {
            EntitlementDecision entitlementDecision = new EntitlementDecision(entry,
                                                                              Calendar.getInstance().getTimeInMillis());
            simpleCache.put(key, entitlementDecision);
        } else if (isCarbonCache) {
            Cache<IdentityCacheKey, IdentityCacheEntry> carbonCache = getCommonCache();
            if (carbonCache != null) {
                IdentityCacheKey identityKey = new IdentityCacheKey(key);
                IdentityCacheEntry identityEntry = new IdentityCacheEntry(entry);
                carbonCache.put(identityKey, identityEntry);
            }
        }
    }

    String get(String key) {
        if (simpleCache != null) {
            EntitlementDecision entitlementDecision = simpleCache.get(key);
            if (entitlementDecision != null && (entitlementDecision.getCachedTime() + (long) invalidationInterval
                    > Calendar.getInstance().getTimeInMillis())) {
                return entitlementDecision.getResponse();
            }
        } else if (isCarbonCache) {
            Cache<IdentityCacheKey, IdentityCacheEntry> carbonCache = getCommonCache();
            if (carbonCache != null) {
                IdentityCacheKey identityKey = new IdentityCacheKey(key);
                IdentityCacheEntry identityCacheEntry = carbonCache.get(identityKey);
                if (identityCacheEntry != null) {
                    return identityCacheEntry.getCacheEntry();
                }
            }
        }
        return null;
    }

    void clear() {
        if (simpleCache != null) {
            simpleCache = new SimpleCache<>(simpleCache.maxEntries);
        } else if (isCarbonCache) {
            Cache<IdentityCacheKey, IdentityCacheEntry> carbonCache = getCommonCache();
            if (carbonCache != null) {
                carbonCache.removeAll();
            }
        }
    }

    private class SimpleCache<K, V> extends LinkedHashMap<K, V> {

        private static final long serialVersionUID = -6958380913702000534L;

        private int maxEntries;

        public SimpleCache(int maxEntries) {
            // removeEldestEntry() is called after a put(). To allow maxEntries in
            // cache, capacity should be maxEntries + 1 (for the entry which will be
            // removed). Load factor is taken as 1 because size is fixed. This is
            // less space efficient when very less entries are present, but there
            // will be no effect on time complexity for get(). The third parameter
            // in the base class constructor says that this map is
            // insertion-order oriented.
            super(maxEntries + 1, 1, false);
            this.maxEntries = maxEntries;
        }

        @Override
        protected boolean removeEldestEntry(Entry<K, V> eldest) {
            // After size exceeds max entries, this statement returns true and the
            // oldest value will be removed. Behaves like a queue, the first
            // inserted value will go away.
            return size() > maxEntries;
        }

    }

    /**
     * Encapsulate the XACML Decision with XACML response and time stamp
     */
    private class EntitlementDecision {

        /**
         * XACML response
         */
        private String response;

        /**
         * time stamp
         */
        private long cachedTime;

        EntitlementDecision(String response, long cachedTime) {
            this.response = response;
            this.cachedTime = cachedTime;
        }

        public String getResponse() {
            return response;
        }

        public long getCachedTime() {
            return cachedTime;
        }
    }

}
