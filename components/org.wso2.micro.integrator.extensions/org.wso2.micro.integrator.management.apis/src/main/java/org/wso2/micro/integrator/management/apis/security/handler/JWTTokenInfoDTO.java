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

import com.nimbusds.jose.jwk.RSAKey;

import java.security.KeyPair;

/**
 * DTO for holding issued token information
 */
public class JWTTokenInfoDTO {

    private String token;
    private long expiry;
    private String scope;
    private String issuer;
    private String hash;
    private boolean revoked;
    private String username;
    private KeyPair generatedKeyPair;
    private RSAKey rsaKey;
    private long lastAccess;

    public JWTTokenInfoDTO(String userName) {
        this.username = userName;
        this.scope = AuthConstants.JWT_TOKEN_DEFAULT_SCOPE;
        this.expiry = AuthConstants.DEFAULT_EXPIRY_DURATION;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public long getExpiry() {
        return expiry;
    }

    public void setExpiry(long expiry) {
        this.expiry = expiry;
    }

    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public boolean isRevoked() {
        return revoked;
    }

    public void setRevoked(boolean revoked) {
        this.revoked = revoked;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getIssuer() {
        return issuer;
    }

    public void setIssuer(String issuer) {
        this.issuer = issuer;
    }


    public KeyPair getGeneratedKeyPair() {
        return generatedKeyPair;
    }

    public void setGeneratedKeyPair(KeyPair generatedKeyPair) {
        this.generatedKeyPair = generatedKeyPair;
    }


    public RSAKey getRsaKey() {
        return rsaKey;
    }

    public void setRsaKey(RSAKey rsaKey) {
        this.rsaKey = rsaKey;
    }


    public long getLastAccess() {
        return lastAccess;
    }

    public void setLastAccess(long lastAccess) {
        this.lastAccess = lastAccess;
    }
}
