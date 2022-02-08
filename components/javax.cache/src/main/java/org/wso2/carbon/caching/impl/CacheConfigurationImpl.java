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

import javax.cache.CacheConfiguration;
import javax.cache.CacheLoader;
import javax.cache.CacheWriter;
import javax.cache.transaction.IsolationLevel;
import javax.cache.transaction.Mode;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

/**
 * TODO: class description
 */
public final class CacheConfigurationImpl implements CacheConfiguration {
    private static final boolean DEFAULT_READ_THROUGH = false;
    private static final boolean DEFAULT_WRITE_THROUGH = false;
    private static final boolean DEFAULT_STATISTICS_ENABLED = false;
    private static final Duration DEFAULT_TIME_TO_LIVE = Duration.ETERNAL;
    private static final boolean DEFAULT_STORE_BY_VALUE = true;
    private static final IsolationLevel DEFAULT_TRANSACTION_ISOLATION_LEVEL = IsolationLevel.NONE;
    private static final Mode DEFAULT_TRANSACTION_MODE = Mode.NONE;

    /**
     * read through
     */
    protected boolean readThrough = DEFAULT_READ_THROUGH;
    /**
     * write through
     */
    protected boolean writeThrough = DEFAULT_WRITE_THROUGH;
    /**
     * statistics enabled
     */
    protected boolean statisticsEnabled = DEFAULT_STATISTICS_ENABLED;
    /**
     * duration
     */
    protected Duration[] timeToLive;

    /**
     * store by value
     */
    protected boolean storeByValue = DEFAULT_STORE_BY_VALUE;

    /**
     * isolation level
     */
    protected IsolationLevel isolationLevel = DEFAULT_TRANSACTION_ISOLATION_LEVEL;

    /**
     * transaction mode
     */
    protected Mode transactionMode = DEFAULT_TRANSACTION_MODE;

    private CacheLoader cacheLoader;
    private CacheWriter cacheWriter;

    public CacheConfigurationImpl() {
    }

    public CacheConfigurationImpl(boolean readThrough, boolean writeThrough,
                                  boolean storeByValue, boolean statisticsEnabled,
                                  IsolationLevel isolationLevel, Mode transactionMode,
                                  Duration[] timeToLive) {
        this.readThrough = readThrough;
        this.writeThrough = writeThrough;
        this.storeByValue = storeByValue;
        this.statisticsEnabled = statisticsEnabled;
        this.isolationLevel = isolationLevel;
        this.transactionMode = transactionMode;
        this.timeToLive = Arrays.copyOf(timeToLive, timeToLive.length);
    }

    void setReadThrough(boolean readThrough) {
        this.readThrough = readThrough;
    }

    void setWriteThrough(boolean writeThrough) {
        this.writeThrough = writeThrough;
    }

    void setStoreByValue(boolean storeByValue) {
        this.storeByValue = storeByValue;
    }

    void setIsolationLevel(IsolationLevel isolationLevel) {
        this.isolationLevel = isolationLevel;
    }

    void setTransactionMode(Mode transactionMode) {
        this.transactionMode = transactionMode;
    }

    void setTimeToLive(Duration[] timeToLive) {
        this.timeToLive = timeToLive;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isReadThrough() {
        return readThrough;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isWriteThrough() {
        return writeThrough;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isStoreByValue() {
        return storeByValue;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isStatisticsEnabled() {
        return statisticsEnabled;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setStatisticsEnabled(boolean enableStatistics) {
        this.statisticsEnabled = enableStatistics;
    }

    @Override
    public Duration getExpiry(ExpiryType type) {
        return timeToLive[type.ordinal()];
    }

    void setExpiry(long expiryTime, TimeUnit timeUnit, ExpiryType type) {
        if (timeToLive == null) {
            timeToLive = new Duration[2];
        }
        timeToLive[type.ordinal()] = new Duration(timeUnit, expiryTime);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isTransactionEnabled() {
        return isolationLevel != null && transactionMode != null;
    }

    @Override
    public IsolationLevel getTransactionIsolationLevel() {
        return isolationLevel;
    }

    @Override
    public Mode getTransactionMode() {
        return transactionMode;
    }

    @Override
    public CacheLoader getCacheLoader() {
        return this.cacheLoader;
    }

    @Override
    public CacheWriter getCacheWriter() {
        return this.cacheWriter;
    }

    void setCacheLoader(CacheLoader cacheLoader) {
        this.cacheLoader = cacheLoader;
    }

    void setCacheWriter(CacheWriter cacheWriter) {
        this.cacheWriter = cacheWriter;
    }

    @Override
    public int hashCode() {
        int result = (readThrough ? 1 : 0);
        result = 31 * result + (writeThrough ? 1 : 0);
        result = 31 * result + (isStatisticsEnabled() ? 1 : 0);
        result = 31 * result + Arrays.hashCode(timeToLive);
        result = 31 * result + (storeByValue ? 1 : 0);
        result = 31 * result + (isolationLevel != null ? isolationLevel.hashCode() : 0);
        result = 31 * result + (transactionMode != null ? transactionMode.hashCode() : 0);
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CacheConfiguration)) return false;

        CacheConfiguration that = (CacheConfiguration) o;

        if (readThrough != that.isReadThrough()) return false;
        if (writeThrough != that.isWriteThrough()) return false;
        if (isStatisticsEnabled() != that.isStatisticsEnabled()) return false;
        for (ExpiryType ttyType : ExpiryType.values()) {
            if (getExpiry(ttyType) != that.getExpiry(ttyType)) return false;
        }
        return storeByValue == isStoreByValue() &&
               isolationLevel == that.getTransactionIsolationLevel() &&
               transactionMode == that.getTransactionMode();

    }
}
