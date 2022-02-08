/*
 *  Copyright (c) 2022, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.caching.impl;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import javax.cache.CacheManager;
import javax.cache.CacheManagerFactory;
import javax.cache.CachingShutdownException;

import static org.wso2.carbon.caching.impl.Constants.SUPER_TENANT_DOMAIN_NAME;

/**
 * Carbon implementation of java cache.
 *
 */
public class CacheManagerFactoryImpl implements CacheManagerFactory, TenantCacheManager {

    private static CacheCleanupTask cacheCleanupTask = new CacheCleanupTask();
    private ScheduledExecutorService cacheEvictionScheduler;
    private static ThreadFactory threadFactory;
    private static int threadCount = 0;

    static {
        threadFactory = new ThreadFactory() {
            @Override
            public Thread newThread(Runnable runnable) {
                Thread th = new Thread(runnable);
                th.setName("CacheExpirySchedulerThread-" + threadCount++);
                return th;
            }
        };
    }

    static void addCacheForMonitoring(CacheImpl cache) {
        cacheCleanupTask.addCacheForMonitoring(cache);
    }

    void removeCacheFromMonitoring(CacheImpl cache) {
        cacheCleanupTask.removeCacheFromMonitoring(cache);
    }

    /**
     * Map<tenantDomain, Map<cacheManagerName,CacheManager> >
     */
    private Map<String, Map<String, CacheManager>> globalCacheManagerMap =
            new ConcurrentHashMap<String, Map<String, CacheManager>>();

    void switchToDistributedMode(){
        for (Map<String, CacheManager> cacheManagerMap : globalCacheManagerMap.values()) {
            for (CacheManager cacheManager : cacheManagerMap.values()) {
                ((CarbonCacheManager) cacheManager).switchToDistributedMode();
            }
        }
    }

    @Override
    public CacheManager getCacheManager(String cacheManagerName) {
        String tenantDomain = SUPER_TENANT_DOMAIN_NAME;
        if(tenantDomain == null){
            throw new NullPointerException("Tenant domain has not been set in CarbonContext");
        }
        Map<String, CacheManager> cacheManagers = globalCacheManagerMap.get(tenantDomain);
        if (cacheManagers == null) {
            synchronized (tenantDomain.intern()) {
                if ((cacheManagers = globalCacheManagerMap.get(tenantDomain)) == null) {
                    cacheManagers = new ConcurrentHashMap<String, CacheManager>();
                    globalCacheManagerMap.put(tenantDomain, cacheManagers);
                }
            }
            ensureExpirySchedulerRunning();
        }
        CacheManager cacheManager = cacheManagers.get(cacheManagerName);
        if (cacheManager == null) {
            synchronized ((tenantDomain + "*.*" + cacheManagerName).intern()) {
                if ((cacheManager = cacheManagers.get(cacheManagerName)) == null) {
                    cacheManager = new CarbonCacheManager(cacheManagerName, this);
                    cacheManagers.put(cacheManagerName, cacheManager);
                }
            }
        }
        return cacheManager;
    }

    @Override
    public CacheManager getCacheManager(ClassLoader classLoader, String name) {
        // Since we have a single CacheManager, we don't have to take the ClassLoader into consideration
        return getCacheManager(name);
    }

    @Override
    public void close() throws CachingShutdownException {
        String tenantDomain = SUPER_TENANT_DOMAIN_NAME;
        synchronized (tenantDomain.intern()) {
            Map<String, CacheManager> cacheManagers = globalCacheManagerMap.get(tenantDomain);
            if (cacheManagers != null) {
                for (CacheManager cacheManager : cacheManagers.values()) {
                    cacheManager.shutdown();
                }
                cacheManagers.clear();
            }
        }
        if (cacheEvictionScheduler != null) {
            cacheEvictionScheduler.shutdown();
        }
    }

    @Override
    public boolean close(ClassLoader classLoader) throws CachingShutdownException {
        close();
        return true;
    }

    @Override
    public boolean close(ClassLoader classLoader, String name) throws CachingShutdownException {
        String tenantDomain = SUPER_TENANT_DOMAIN_NAME;
        Map<String, CacheManager> cacheManagers = globalCacheManagerMap.get(tenantDomain);
        CacheManager cacheManager;
        if (cacheManagers != null) {
            cacheManager = cacheManagers.get(name);
            cacheManager.shutdown();
            return true;
        }
        return false;
    }

    public void removeCacheManager(CarbonCacheManager cacheManager, String tenantDomain) {
        Map<String, CacheManager> cacheManagers = globalCacheManagerMap.get(tenantDomain);
        if (cacheManagers != null) {
            cacheManagers.remove(cacheManager.getName());
        }
    }

    /**
     * Remove all the Caches and CacheManagers of the specified tenant
     *
     * @param tenantDomain The domain of the tenant whose caches need to be removed
     */
    public void removeAllCacheManagers(String tenantDomain) {
        Map<String, CacheManager> cacheManagers = globalCacheManagerMap.get(tenantDomain);
        if (cacheManagers != null) {
            for (CacheManager cacheManager : cacheManagers.values()) {
                if (((CarbonCacheManager) cacheManager).removeLocalCaches()) {
                    cacheManagers.remove(cacheManager.getName());
                }
            }
        }
    }

    public void removeCacheManagerMap(String tenantDomain) {

        globalCacheManagerMap.remove(tenantDomain);
    }

    private void ensureExpirySchedulerRunning() {

        if (cacheEvictionScheduler == null || cacheEvictionScheduler.isShutdown() || cacheEvictionScheduler
                .isTerminated()) {
            int threadCount = calculateExpiryThreadCount();
            cacheEvictionScheduler = Executors.newScheduledThreadPool(threadCount, threadFactory);
            cacheEvictionScheduler.scheduleWithFixedDelay(cacheCleanupTask, 30, 30, TimeUnit.SECONDS);
        }
    }

    /**
     * Determines the optimal thread count for the current system.
     * Cache expiry is a CPU bound task. The maximum theoretical performance can be obtained utilizing all available
     * cores fully. More threads than available core is counter-productive.
     *
     * @return
     */
    private int calculateExpiryThreadCount() {

        int threadCount = Runtime.getRuntime().availableProcessors();
        if (threadCount < 1) {
            threadCount = 2;
        }
        return threadCount;
    }
}
