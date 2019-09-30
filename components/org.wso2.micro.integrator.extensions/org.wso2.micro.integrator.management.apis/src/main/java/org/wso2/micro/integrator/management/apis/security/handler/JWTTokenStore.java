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

public abstract class JWTTokenStore {

    /**
     * Get token from the token store
     * @param token JWT serialized token String
     * @return JWTTokenInfoDTO Corresponding token info object
     */
    public abstract JWTTokenInfoDTO getToken(String token);

    /**
     * Adds new token to the token store
     * @param token JWT serialized token String
     * @param jwtTokenInfoDTO token info object
     * @return true if success
     */
    public abstract boolean putToken(String token, JWTTokenInfoDTO jwtTokenInfoDTO);

    /**
     * Removes token entry from token store
     * @param token JWT serialized token String
     */
    public abstract void removeToken(String token);

    /**
     * Revokes corresponding token
     * @param token JWT serialized token String
     * @return true on successful revoke
     */
    public abstract boolean revokeToken(String token);

    /**
     * Removes expired tokens based on expiry info on token dto
     */
    public abstract void removeExpired();

    /**
     * Cleanups the store if the store is exhausted.
     */
    public abstract void cleanupStore();

    /**
     * Returns the current size of the token store
     * @return int size
     */
    public abstract int getCurrentSize();

}
