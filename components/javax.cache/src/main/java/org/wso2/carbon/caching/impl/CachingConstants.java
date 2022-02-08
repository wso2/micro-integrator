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

import org.wso2.carbon.caching.impl.eviction.EvictionAlgorithm;
import org.wso2.carbon.caching.impl.eviction.LeastRecentlyUsedEvictionAlgorithm;

/**
 * TODO: class description
 */
public final class CachingConstants {

    public static final int DEFAULT_CACHE_CAPACITY = 10000;
    public static final EvictionAlgorithm DEFAULT_EVICTION_ALGORITHM = new LeastRecentlyUsedEvictionAlgorithm();
    public static final double CACHE_EVICTION_FACTOR = 0.25;
    public static final long MAX_CACHE_IDLE_TIME_MILLIS = 15 * 60 * 1000; // 15mins

    public static final long DEFAULT_CACHE_EXPIRY_MINS = 15;

    // Cache name prefix of local cache
    public static final String LOCAL_CACHE_PREFIX = "$__local__$.";

    // Cache name prefix of clear all
    public static final String CLEAR_ALL_PREFIX = "$__clear__all__$.";

    // Cache name prefix of Time Stamp cache
    public static final String TIMESTAMP_CACHE_PREFIX = "$_timestamp_$";
    public static final String FORCE_LOCAL_CACHE = "Cache.ForceLocalCache";
    //Keep or discard empty cache objects
    public static final String DISCARD_EMPTY_CACHES = "Cache.DiscardEmptyCaches";

    public static final String ILLEGAL_STATE_EXCEPTION_MESSAGE = "The cache status is not STARTED";

    private CachingConstants() {
    }
}
