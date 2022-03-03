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

import org.wso2.micro.integrator.core.services.CarbonServerConfigurationService;

/**
 * TODO: class description
 */
public final class Util {

    public static void checkAccess(String ownerTenantDomain, int ownerTenantId) {
        // super tenant only
        return;
    }


    /**
     * Get map name of a cache in the distributed map provider
     * @param cacheName name of the cache
     * @param ownerTenantDomain owner tenant domain of the cache manager
     * @param cacheManagerName name of the cache manager
     * @return the distributed map name
     */
    public static String getDistributedMapNameOfCache(String cacheName, String ownerTenantDomain,
                                                      String cacheManagerName) {
        return "$cache.$domain[" + ownerTenantDomain + "]" +
                cacheManagerName + "#" + cacheName;
    }


    /**
     * Return the default cache timeout value (Mins) specified in Carbon.xml
     *
     * @return long
     */
    public static long getDefaultCacheTimeout() {
        CarbonServerConfigurationService serverConfigService = CarbonServerConfigurationService.getInstance();
        if (serverConfigService != null) {
            String defaultCacheTimeoutValue = serverConfigService.getFirstProperty("Cache.DefaultCacheTimeout");
            return defaultCacheTimeoutValue == null ? CachingConstants.DEFAULT_CACHE_EXPIRY_MINS :
                    Long.parseLong(defaultCacheTimeoutValue);
        }
        return CachingConstants.DEFAULT_CACHE_EXPIRY_MINS;
    }

    private Util() {
    }
}
