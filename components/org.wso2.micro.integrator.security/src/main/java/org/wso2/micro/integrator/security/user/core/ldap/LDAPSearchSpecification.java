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
package org.wso2.micro.integrator.security.user.core.ldap;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.wso2.micro.integrator.security.user.api.RealmConfiguration;
import org.wso2.micro.integrator.security.user.core.UserStoreException;
import org.wso2.micro.integrator.security.user.core.model.ExpressionAttribute;
import org.wso2.micro.integrator.security.user.core.model.ExpressionCondition;
import org.wso2.micro.integrator.security.user.core.model.ExpressionOperation;

import java.util.ArrayList;
import java.util.List;
import javax.naming.directory.SearchControls;

/**
 * In order to perform the search on LDAP, need to generate filter query, search bases and SearchControls depends
 * on user input. This class able to generate and define required elements for LDAP search.
 */
public class LDAPSearchSpecification {

    private static final String EQUALS_SIGN = "=";
    private static final String SERVICE_NAME_ATTRIBUTE = "sn";
    private static final String VALUE_SEPARATOR = ",";

    private RealmConfiguration realmConfig;
    private SearchControls searchControls = new SearchControls();
    private String searchBases = null;
    private LDAPFilterQueryBuilder ldapFilterQueryBuilder = null;

    private boolean isUsernameFiltering = false;
    private boolean isClaimFiltering = false;
    private boolean isGroupFiltering = false;
    private boolean isMultiGroupFiltering = false;
    private boolean isMemberOfPropertyFound = false;
    private boolean isMemberShipPropertyFound = false;

    public LDAPSearchSpecification(RealmConfiguration realmConfig, List<ExpressionCondition> expressionConditions)
            throws UserStoreException {

        this.realmConfig = realmConfig;
        this.searchControls.setSearchScope(SearchControls.SUBTREE_SCOPE);

        for (ExpressionCondition expressionCondition : expressionConditions) {
            if (ExpressionAttribute.ROLE.toString().equals(expressionCondition.getAttributeName())
                    && isGroupFiltering) {
                isMultiGroupFiltering = true;
            } else if (ExpressionAttribute.ROLE.toString().equals(expressionCondition.getAttributeName())) {
                isGroupFiltering = true;
            } else if (ExpressionAttribute.USERNAME.toString().equals(expressionCondition.getAttributeName())) {
                isUsernameFiltering = true;
            } else {
                isClaimFiltering = true;
            }
        }
        setLDAPSearchParamters(expressionConditions);
    }

    /**
     * Set LDAP search parameters, such as define searchBases, define searchControls and generate search filter query.
     *
     * @param expressionConditions
     * @throws UserStoreException
     */
    private void setLDAPSearchParamters(List<ExpressionCondition> expressionConditions)
            throws UserStoreException {

        List<String> returnedAttributes = new ArrayList<>();

        if (isGroupFiltering) {
            checkForMemberOfAttribute(expressionConditions, returnedAttributes);
            // If 'memberOf' attribute not found, go with membership attribute.
            if (!isMemberOfPropertyFound) {
                checkForMembershipAttribute(returnedAttributes);
            }
        } else {
            this.searchBases = realmConfig.getUserStoreProperty(LDAPConstants.USER_SEARCH_BASE);
            returnedAttributes.add(realmConfig.getUserStoreProperty(LDAPConstants.USER_NAME_ATTRIBUTE));
            /* To get the serviceNameAttributeValue while interpreting the search result
            for username or claim filtering. */
            returnedAttributes.add(SERVICE_NAME_ATTRIBUTE);
        }

        if (CollectionUtils.isNotEmpty(returnedAttributes)) {
            this.searchControls.setReturningAttributes(returnedAttributes.toArray(new String[0]));
        }

        searchFilterBuilder(isGroupFiltering, isMultiGroupFiltering, expressionConditions);
    }

    /**
     * Check for membership attribute exist or not.
     *
     * @param returnedAttributes
     * @throws UserStoreException
     */
    private void checkForMembershipAttribute(List<String> returnedAttributes) throws UserStoreException {

        String membershipProperty = realmConfig.getUserStoreProperty(LDAPConstants.MEMBERSHIP_ATTRIBUTE);
        if (StringUtils.isEmpty(membershipProperty)) {
            throw new UserStoreException("Please set member of attribute or membership attribute");
        }
        this.isMemberShipPropertyFound = true;
        this.searchBases = realmConfig.getUserStoreProperty(LDAPConstants.GROUP_SEARCH_BASE);
        returnedAttributes.add(membershipProperty);
    }

    /**
     * Check for memberOf attribute can be found or not.
     *
     * @param expressionConditions
     * @param returnedAttributes
     */
    private void checkForMemberOfAttribute(List<ExpressionCondition> expressionConditions,
                                           List<String> returnedAttributes) {

        boolean isEqOperationFound = false;
        boolean otherOperationsFound = false;

        for (ExpressionCondition expressionCondition : expressionConditions) {
            if (ExpressionAttribute.ROLE.toString().equals(expressionCondition.getAttributeName()) &&
                    ExpressionOperation.EQ.toString().equals(expressionCondition.getOperation())) {
                isEqOperationFound = true;
            } else if (ExpressionAttribute.ROLE.toString().equals(expressionCondition.getAttributeName()) &&
                    !ExpressionOperation.EQ.toString().equals(expressionCondition.getOperation())) {
                otherOperationsFound = true;
            }
        }

        // 'memberOf' attribute only support 'EQ' filter operation, can't apply 'EW','SW','CO' filter operations.
        if (isEqOperationFound && !otherOperationsFound) {
            String memberOfProperty = realmConfig.getUserStoreProperty(LDAPConstants.MEMBEROF_ATTRIBUTE);
            // Give priority to 'memberOf' attribute.
            if (StringUtils.isNotEmpty(memberOfProperty)) {
                this.isMemberOfPropertyFound = true;
                this.searchBases = realmConfig.getUserStoreProperty(LDAPConstants.USER_SEARCH_BASE);
                returnedAttributes.add(realmConfig.getUserStoreProperty(LDAPConstants.USER_NAME_ATTRIBUTE));
            }
        }
    }

    /**
     * Building search filter query.
     *
     * @param isGroupFiltering
     * @param isMultiGroupFiltering
     * @param expressionConditions
     * @throws UserStoreException
     */
    private void searchFilterBuilder(boolean isGroupFiltering, boolean isMultiGroupFiltering,
                                     List<ExpressionCondition> expressionConditions) throws UserStoreException {

        String userPropertyName = realmConfig.getUserStoreProperty(LDAPConstants.USER_NAME_ATTRIBUTE);
        String groupPropertyName = realmConfig.getUserStoreProperty(LDAPConstants.GROUP_NAME_ATTRIBUTE);
        String memberOfAttributeName = realmConfig.getUserStoreProperty(LDAPConstants.MEMBEROF_ATTRIBUTE);
        String memberAttributeName = realmConfig.getUserStoreProperty(LDAPConstants.MEMBERSHIP_ATTRIBUTE);

        initiateLDAPQueryBuilder(isGroupFiltering);

        for (ExpressionCondition expressionCondition : expressionConditions) {
            StringBuilder property;
            String attributeName = expressionCondition.getAttributeName();
            StringBuilder value = new StringBuilder(expressionCondition.getAttributeValue());
            String operation = expressionCondition.getOperation();
            boolean isMembershipMultiGroupFilters = false;

            if (ExpressionAttribute.ROLE.toString().equals(attributeName)) {
                if (isMemberOfPropertyFound) {
                    property = getMemberOfProperty(groupPropertyName, memberOfAttributeName, value, operation);
                } else if (isMultiGroupFiltering && !isMemberOfPropertyFound) {
                    property = new StringBuilder(groupPropertyName);
                    isMembershipMultiGroupFilters = true;
                } else {
                    property = new StringBuilder(groupPropertyName);
                }
            } else if (ExpressionAttribute.USERNAME.toString().equals(expressionCondition.getAttributeName())) {
                property = getUserNameProperty(userPropertyName, memberAttributeName, value, operation);
                if (property == null) continue;
            } else {
                property = getClaimProperty(expressionCondition);
                if (property == null) continue;
            }
            ExpressionCondition condition = new ExpressionCondition(operation, String.valueOf(property),
                    String.valueOf(value));
            ldapFilterQueryBuilder.addFilter(condition, isMembershipMultiGroupFilters);
        }
    }

    /**
     * Initialize LDAP query builder with search category.
     *
     * @param isGroupFiltering
     */
    private void initiateLDAPQueryBuilder(boolean isGroupFiltering) {

        if (isGroupFiltering && isMemberShipPropertyFound) {
            ldapFilterQueryBuilder = new LDAPFilterQueryBuilder(realmConfig.
                    getUserStoreProperty(LDAPConstants.GROUP_NAME_LIST_FILTER));
        } else {
            ldapFilterQueryBuilder = new LDAPFilterQueryBuilder(realmConfig.
                    getUserStoreProperty(LDAPConstants.USER_NAME_LIST_FILTER));
        }
    }

    /**
     * Get memberOf attribute full property name.
     *
     * @param groupPropertyName
     * @param memberOfAttributeName
     * @param value
     * @param operation
     * @return
     * @throws UserStoreException
     */
    private StringBuilder getMemberOfProperty(String groupPropertyName, String memberOfAttributeName,
                                              StringBuilder value, String operation) throws UserStoreException {

        StringBuilder property;
        if (ExpressionOperation.EQ.toString().equals(operation)) {
            property = new StringBuilder(memberOfAttributeName).append(EQUALS_SIGN).append(groupPropertyName);
            value.append(VALUE_SEPARATOR).append(realmConfig.getUserStoreProperty(LDAPConstants.GROUP_SEARCH_BASE));
        } else {
            throw new UserStoreException("MemberOf attribute only support 'EQ' filter operation.");
        }
        return property;
    }

    /**
     * Get full username property name depends on membership attribute.
     *
     * @param userPropertyName
     * @param memberAttributeName
     * @param value
     * @param operation
     * @return
     */
    private StringBuilder getUserNameProperty(String userPropertyName, String memberAttributeName,
                                              StringBuilder value, String operation) {

        StringBuilder property;
        if (isMemberShipPropertyFound) {
            property = getMembershipProperty(userPropertyName, memberAttributeName, value, operation);
            if (property == null) return null;
        } else {
            property = new StringBuilder(userPropertyName);
        }
        return property;
    }

    /**
     * Get membership attribute full property name.
     *
     * @param userPropertyName
     * @param memberAttributeName
     * @param value
     * @param operation
     * @return
     */
    private StringBuilder getMembershipProperty(String userPropertyName, String memberAttributeName,
                                                StringBuilder value, String operation) {

        StringBuilder property;
        property = new StringBuilder(memberAttributeName).append(EQUALS_SIGN).append(userPropertyName);
        if (ExpressionOperation.CO.toString().equals(operation) ||
                ExpressionOperation.EW.toString().equals(operation)) {
            return null;
        } else if (ExpressionOperation.EQ.toString().equals(operation)) {
            value.append(VALUE_SEPARATOR).append(realmConfig.getUserStoreProperty(LDAPConstants.USER_SEARCH_BASE));
        }
        return property;
    }

    /**
     * Get claim property name.
     *
     * @param expressionCondition
     * @return
     */
    private StringBuilder getClaimProperty(ExpressionCondition expressionCondition) {

        StringBuilder property;
        if (!isMemberShipPropertyFound) {
            property = new StringBuilder(expressionCondition.getAttributeName());
        } else {
            return null;
        }
        return property;
    }

    public SearchControls getSearchControls() {

        return searchControls;
    }

    public String getSearchBases() {

        return searchBases;
    }

    public boolean isGroupFiltering() {

        return isGroupFiltering;
    }

    public boolean isUsernameFiltering() {

        return isUsernameFiltering;
    }

    public boolean isClaimFiltering() {

        return isClaimFiltering;
    }

    public boolean isMemberOfPropertyFound() {

        return isMemberOfPropertyFound;
    }

    public boolean isMemberShipPropertyFound() {

        return isMemberShipPropertyFound;
    }

    public String getSearchFilterQuery() {

        return ldapFilterQueryBuilder.getSearchFilterQuery();
    }
}
