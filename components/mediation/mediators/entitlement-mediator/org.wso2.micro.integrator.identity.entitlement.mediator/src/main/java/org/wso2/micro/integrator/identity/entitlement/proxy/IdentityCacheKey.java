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

/**
 * Identity Cache key which wraps the identity related cache key values
 */
public class IdentityCacheKey implements Serializable {

    private static final long serialVersionUID = 1806955328379952612L;

    private String key;

    public IdentityCacheKey(String key) {
        this.key = key;
    }

    public String getKey() {
        return key;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof IdentityCacheKey)) {
            return false;
        }
        IdentityCacheKey cacheKey = (IdentityCacheKey) obj;

        return cacheKey.getKey() != null && cacheKey.getKey().equals(key);

    }

    @Override
    public int hashCode() {

        return key.hashCode();
    }
}
