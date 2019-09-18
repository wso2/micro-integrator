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

package org.wso2.micro.integrator.security.user.core.claim;

import java.io.Serializable;

/**
 * Unique key to represent a claim across the dialect.
 */
public class ClaimKey implements Serializable {
    private static final long serialVersionUID = -2002899750350065724L;
    private String claimUri;
    private String dialectUri;

    public ClaimKey() {
    }

    public ClaimKey(String claimUri, String dialectUri) {
        this.claimUri = claimUri;
        this.dialectUri = dialectUri;
    }

    public String getClaimUri() {
        return claimUri;
    }

    public void setClaimUri(String claimUri) {
        this.claimUri = claimUri;
    }

    public String getDialectUri() {
        return dialectUri;
    }

    public void setDialectUri(String dialectUri) {
        this.dialectUri = dialectUri;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ClaimKey that = (ClaimKey) o;

        if (!claimUri.equals(that.claimUri)) {
            return false;
        }
        return dialectUri.equals(that.dialectUri);
    }

    @Override
    public int hashCode() {
        int result = claimUri.hashCode();
        result = 31 * result + dialectUri.hashCode();
        return result;
    }
}
