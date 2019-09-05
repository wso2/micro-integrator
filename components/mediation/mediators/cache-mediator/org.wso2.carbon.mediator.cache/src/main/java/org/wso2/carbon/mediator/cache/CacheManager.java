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
package org.wso2.carbon.mediator.cache;

import com.google.common.cache.LoadingCache;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * There would be two instances of the cache mediator in a single mediation flow. Hence it must be possible for the
 * cache created in one instance to be reused in the next. This CacheManager enables this feature with static methods.
 */
public class CacheManager {

    /**
     * Maps the id with the relevant LoadingCache
     */
    private Map<String, LoadingCache<String, CachableResponse>> cacheMap = new ConcurrentHashMap<>();

    /**
     * @param id the id of the mediator
     * @return the relevant cache of the mediator
     */
    LoadingCache<String, CachableResponse> get(String id) {
        return cacheMap.get(id);
    }

    /**
     * Insert id and the LoadingCache to the CaccheManager
     *
     * @param id    the id of the cache mediator
     * @param cache the Loading cache related to the id
     */
    void put(String id, LoadingCache<String, CachableResponse> cache) {
        cacheMap.put(id, cache);
    }

    /**
     * removes the LoadingCache associated with the id in the CacheManager
     *
     * @param id the id of the cache mediator
     */
    void remove(String id) {
        cacheMap.remove(id);
    }

    /**
     * Clears the CacheManager
     */
    void clean() {
        cacheMap.clear();
    }

}
