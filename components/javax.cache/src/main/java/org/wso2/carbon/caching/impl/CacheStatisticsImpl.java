/*
*  Copyright (c) 2005-2011, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import javax.cache.CacheStatistics;
import javax.cache.Status;
import java.util.Date;

/**
 * TODO: class description
 */
public class CacheStatisticsImpl implements CacheStatistics {
    private Status status;

    private long cacheHits;
    private long cacheMisses;
    private long cacheGets;
    private long cachePuts;
    private long cacheRemovals;
    private long cacheEvictions;

    @Override
    public void clear() {
        cacheGets = 0;
        cacheMisses = 0;
        cachePuts = 0;
        cacheHits = 0;
    }

    @Override
    public Date getStartAccumulationDate() {
        return null;  //TODO
    }

    @Override
    public long getCacheHits() {
        return cacheHits;
    }

    @Override
    public float getCacheHitPercentage() {
        return 0;  //TODO
    }

    @Override
    public long getCacheMisses() {
        return cacheMisses;
    }

    @Override
    public float getCacheMissPercentage() {
        return 0;  //TODO
    }

    @Override
    public long getCacheGets() {
        return cacheGets;
    }

    @Override
    public long getCachePuts() {
        return cachePuts;
    }

    @Override
    public long getCacheRemovals() {
        return 0;  //TODO
    }

    @Override
    public long getCacheEvictions() {
        return cacheEvictions;
    }

    @Override
    public float getAverageGetMillis() {
        return 0;  //TODO
    }

    @Override
    public float getAveragePutMillis() {
        return 0;  //TODO
    }

    @Override
    public float getAverageRemoveMillis() {
        return 0;  //TODO
    }
}
