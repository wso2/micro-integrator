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
package org.wso2.micro.integrator.management.apis.security.handler;

/**
 * This class is the DTO for JWT configs in internal-apis.xml
 */
public class JWTConfigDTO {

    private String expiry;
    private String tokenSize;
    private int tokenStoreSize;
    private boolean removeOldestElementOnOverflow = true;
    private int cleanupThreadInterval;
    private boolean jwtHandlerEngaged;

    public String getExpiry() {
        return expiry;
    }

    public void setExpiry(String expiry) {
        this.expiry = expiry;
    }

    public String getTokenSize() {
        return tokenSize;
    }

    public void setTokenSize(String tokenSize) {
        this.tokenSize = tokenSize;
    }

    public int getTokenStoreSize() {
        return tokenStoreSize;
    }

    public void setTokenStoreSize(int tokenStoreSize) {
        this.tokenStoreSize = tokenStoreSize;
    }


    public boolean isRemoveOldestElementOnOverflow() {
        return removeOldestElementOnOverflow;
    }

    public void setRemoveOldestElementOnOverflow(boolean removeOldestElementOnOverflow) {
        this.removeOldestElementOnOverflow = removeOldestElementOnOverflow;
    }

    public int getCleanupThreadInterval() {

        return cleanupThreadInterval;
    }

    public void setCleanupThreadInterval(int cleanupThreadInterval) {

        this.cleanupThreadInterval = cleanupThreadInterval;
    }

    public boolean isJwtHandlerEngaged() {

        return jwtHandlerEngaged;
    }

    public void setJwtHandlerEngaged(boolean jwtHandlerEngaged) {

        this.jwtHandlerEngaged = jwtHandlerEngaged;
    }

}
