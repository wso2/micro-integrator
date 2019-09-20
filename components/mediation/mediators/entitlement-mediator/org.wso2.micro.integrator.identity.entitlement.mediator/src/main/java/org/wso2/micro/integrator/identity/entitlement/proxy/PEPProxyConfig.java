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

import java.util.Map;

public class PEPProxyConfig {

    private Map<String, Map<String, String>> appToPDPClientConfigMap;
    private String defaultAppId;
    private String cacheType;
    private int invalidationInterval;
    private int maxCacheEntries;

    public PEPProxyConfig(Map<String, Map<String, String>> appToPDPClientConfigMap, String defaultAppId,
                          String cacheType, int invalidationInterval, int maxCacheEntries) {
        this.defaultAppId = defaultAppId;
        this.appToPDPClientConfigMap = appToPDPClientConfigMap;
        this.cacheType = cacheType;
        this.invalidationInterval = invalidationInterval;
        this.maxCacheEntries = maxCacheEntries;
        if (this.maxCacheEntries > ProxyConstants.MAX_CACHE_SIZE || this.maxCacheEntries < 0) {
            this.maxCacheEntries = ProxyConstants.MAX_CACHE_SIZE;
        }
    }

    public String getDefaultAppId() {
        return defaultAppId;
    }

    public String getCacheType() {
        return cacheType;
    }

    public int getInvalidationInterval() {
        return invalidationInterval;
    }

    public Map<String, Map<String, String>> getAppToPDPClientConfigMap() {
        return appToPDPClientConfigMap;
    }

    public int getMaxCacheEntries() {
        return maxCacheEntries;
    }
}
