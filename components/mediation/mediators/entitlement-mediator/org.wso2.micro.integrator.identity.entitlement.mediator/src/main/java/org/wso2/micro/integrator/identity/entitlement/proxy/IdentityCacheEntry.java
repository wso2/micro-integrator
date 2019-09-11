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

import java.io.Serializable;
import java.security.Key;
import java.util.Arrays;
import java.util.Date;

/**
 * Identity Cache entry which wraps the identity related cache entry values
 */
public class IdentityCacheEntry implements Serializable {

    private static final long serialVersionUID = 7239122598310553180L;

    private String cacheEntry;
    private String[] cacheEntryArray;
    private int hashEntry;
    private long cacheInterval;
    private boolean cacheClearing;
    private Key secretKey;
    private Date date;

    public IdentityCacheEntry(String cacheEntry) {
        this.cacheEntry = cacheEntry;
    }

    public IdentityCacheEntry(int hashEntry) {
        this.hashEntry = hashEntry;
    }

    public IdentityCacheEntry(boolean cacheClearing) {
        this.cacheClearing = cacheClearing;
    }

    public IdentityCacheEntry(String cacheEntry, long cacheInterval) {
        this.cacheEntry = cacheEntry;
        this.cacheInterval = cacheInterval;
    }

    public IdentityCacheEntry(String[] cacheEntryArray) {
        this.cacheEntryArray = Arrays.copyOf(cacheEntryArray, cacheEntryArray.length);
    }

    public IdentityCacheEntry(String cacheEntry, Key secretKey, Date date) {
        this.cacheEntry = cacheEntry;
        this.secretKey = secretKey;

        if (date != null) {
            this.date = new Date(date.getTime());
        }
    }

    public String getCacheEntry() {
        return cacheEntry;
    }

    public int getHashEntry() {
        return hashEntry;
    }

    public long getCacheInterval() {
        return cacheInterval;
    }

    public boolean isCacheClearing() {
        return cacheClearing;
    }

    public String[] getCacheEntryArray() {

        if (cacheEntryArray != null) {
            return cacheEntryArray.clone();
        }
        return new String[0];
    }

    public Key getSecretKey() {
        return secretKey;
    }

    public Date getDate() {
        if (date != null) {
            return new Date(date.getTime());
        }
        return null;
    }
}
