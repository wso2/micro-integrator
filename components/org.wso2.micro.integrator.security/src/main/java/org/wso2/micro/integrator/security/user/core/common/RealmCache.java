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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.micro.integrator.security.user.api.UserRealm;
import org.wso2.micro.integrator.security.user.core.UserCoreConstants;

import javax.cache.Cache;
import javax.cache.CacheManager;
import javax.cache.Caching;

public class RealmCache {

    public static final String CUSTOM_TENANT_CACHE_MANAGER = "CUSTOM_TENANT_CACHE_MANAER";
    public static final String CUSTOM_TENANT_CACHE =
            UserCoreConstants.CachingConstants.LOCAL_CACHE_PREFIX + "CUSTOM_TENANT_CACHE";
    private static final RealmCache instance = new RealmCache();
    private static Log log = LogFactory.getLog(RealmCache.class);

    /**
     * Gets a new instance of TenantCache.
     *
     * @return A new instance of TenantCache.
     */
    public synchronized static RealmCache getInstance() {
        return instance;
    }

    private Cache<RealmCacheKey, RealmCacheEntry> getRealmCache() {

        Cache<RealmCacheKey, RealmCacheEntry> cache;
        CacheManager cacheManager = Caching.getCacheManagerFactory().getCacheManager(CUSTOM_TENANT_CACHE_MANAGER);
        if (cacheManager != null) {
            cache = cacheManager.getCache(CUSTOM_TENANT_CACHE);
        } else {
            cache = Caching.getCacheManager().getCache(CUSTOM_TENANT_CACHE);
        }
        if (log.isDebugEnabled()) {
            log.debug("created authorization cache : " + cache);
        }
        return cache;
    }

    public UserRealm getUserRealm(int tenantId, String realmName) {

        RealmCacheKey key = new RealmCacheKey(tenantId, realmName);
        RealmCacheEntry entry = instance.getValueFromCache(key);
        if (entry != null) {
            return entry.getUserRealm();
        } else {
            return null;
        }
    }

    public void addToCache(int tenantId, String realmName, UserRealm userRealm) {
        instance.addToCache(new RealmCacheKey(tenantId, realmName),
                new RealmCacheEntry(userRealm));
    }

    /**
     * Clear the cache entry
     *
     * @param tenantId
     * @param realmName
     */
    public void clearFromCache(int tenantId, String realmName) {
        RealmCacheKey key = new RealmCacheKey(tenantId, realmName);
        RealmCacheEntry entry = instance.getValueFromCache(key);
        if (entry != null) {
            instance.clearCacheEntry(key);
        }
    }

    /**
     * Add a cache entry.
     *
     * @param key   Key which cache entry is indexed.
     * @param entry Actual object where cache entry is placed.
     */
    public void addToCache(RealmCacheKey key, RealmCacheEntry entry) {
        Cache<RealmCacheKey, RealmCacheEntry> cache = getRealmCache();
        if (cache.containsKey(key)) {
            // Element already in the cache. Remove it first
            cache.remove(key);
        }
        cache.put(key, entry);
    }

    /**
     * Retrieves a cache entry.
     *
     * @param key CacheKey
     * @return Cached entry.
     */
    public RealmCacheEntry getValueFromCache(RealmCacheKey key) {

        Cache<RealmCacheKey, RealmCacheEntry> cache = getRealmCache();
        if (cache.containsKey(key)) {
            return cache.get(key);
        }
        return null;
    }

    /**
     * Clears a cache entry.
     *
     * @param key Key to clear cache.
     */
    public void clearCacheEntry(RealmCacheKey key) {

        Cache<RealmCacheKey, RealmCacheEntry> cache = getRealmCache();
        if (cache.containsKey(key)) {
            cache.remove(key);
        }
    }

    /**
     * Remove everything in the cache.
     */
    public void clear() {
        Cache<RealmCacheKey, RealmCacheEntry> cache = getRealmCache();
        cache.removeAll();
    }

}
