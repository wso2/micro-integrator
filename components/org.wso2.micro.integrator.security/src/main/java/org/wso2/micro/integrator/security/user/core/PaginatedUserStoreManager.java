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
package org.wso2.micro.integrator.security.user.core;

import org.wso2.micro.integrator.security.user.core.model.Condition;
import org.wso2.micro.integrator.security.user.core.model.UserClaimSearchEntry;

import java.util.List;
import java.util.Map;

/**
 * This interface provides the pagination support of user operations.
 */
public interface PaginatedUserStoreManager {

    /**
     * Retrieves a list of paginated user names.
     *
     * @param filter The string to filter out user.
     * @param limit  No of search results. If the given value is greater than the system configured max limit
     *               it will be reset to the system configured max limit.
     * @param offset Start index of the user search.
     * @return An array of user names.
     * @throws UserStoreException User Store Exception.
     */
    String[] listUsers(String filter, int limit, int offset) throws UserStoreException;

    /**
     * Retrieves a list of paginated user names from user claims.
     *
     * @param claim       Claim URI. If the claim uri is domain qualified, search the users respective user store. Else
     *                    search recursively.
     * @param claimValue  Claim value.
     * @param profileName User profile name.
     * @param limit       No of search results. If the given value is greater than the system configured max limit
     *                    it will be reset to the system configured max limit.
     * @param offset      Start index of the user search.
     * @return An array of user names.
     * @throws UserStoreException User Store Exception.
     */
    String[] getUserList(String claim, String claimValue, String profileName, int limit, int offset) throws
            UserStoreException;

    /**
     * Retrieves a list of paginated user names conditionally.
     *
     * @param condition   Conditional filter.
     * @param profileName User profile name.
     * @param domain      User Store Domain.
     * @param limit       No of search results. If the given value is greater than the system configured max limit
     *                    it will be reset to the system configured max limit.
     * @param offset      Start index of the user search.
     * @return An array of user names.
     * @throws UserStoreException User Store Exception.
     */
    String[] getUserList(Condition condition, String domain, String profileName, int limit, int offset, String sortBy,
                         String sortOrder) throws UserStoreException;

    /**
     * Get claim values of users.
     *
     * @param userNames User names
     * @param claims    Required claims
     * @return User claim search entry set
     * @throws UserStoreException
     */
    UserClaimSearchEntry[] getUsersClaimValues(String[] userNames, String[] claims, String profileName)
            throws UserStoreException;


    /**
     * Get roles of a users.
     *
     * @param userNames user names
     * @return A map contains a list of role names each user belongs.
     * @throws UserStoreException
     */
    Map<String, List<String>> getRoleListOfUsers(String[] userNames) throws UserStoreException;
}
