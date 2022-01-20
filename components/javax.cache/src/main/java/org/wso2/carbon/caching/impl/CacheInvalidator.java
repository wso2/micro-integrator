/*
 * Copyright (c) 2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wso2.carbon.caching.impl;


import java.io.Serializable;

/**
 * This is used for global cluster cache invalidation
 */
public interface CacheInvalidator {

    /**
     * Publish global cache invalidate message to the topic
     *
     * @param tenantId The current tenant Id
     * @param cacheManagerName Cache manager name
     * @param cacheName Cache name
     * @param cacheKey Cache entry
     */
    public void invalidateCache(int tenantId, String cacheManagerName, String cacheName, Serializable cacheKey);

}
