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

import javax.cache.Cache;
import java.io.Serializable;

/**
 * TODO: class description
 */
public class CacheEntry<K, V> implements Cache.Entry<K, V>, Serializable {

    private static final long serialVersionUID = 1996179870860085427L;

    private K key;
    private V value;
    private long lastAccessed;
    private long lastModified;

    public CacheEntry(K key, V value) {
        this.key = key;
        this.value = value;
        long now = System.currentTimeMillis();
        this.lastAccessed = now;
        this.lastModified = now;
    }

    public K getKey() {
        return key;
    }

    public void setValue(V value) {
        lastModified = System.currentTimeMillis();
        this.value = value;
    }

    public V getValue() {
        lastAccessed = System.currentTimeMillis();
        return value;
    }

    public long getLastAccessed() {
        return lastAccessed;
    }

    public long getLastModified() {
        return lastModified;
    }

    public void setLastAccessed(Long lastAccessed) {
        this.lastAccessed = lastAccessed;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CacheEntry that = (CacheEntry) o;
        return key.equals(that.key) && value.equals(that.value);
    }

    @Override
    public int hashCode() {
        int result = key.hashCode();
        result = 31 * result + value.hashCode();
        return result;
    }
}
