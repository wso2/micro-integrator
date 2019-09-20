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

import org.apache.commons.lang.StringUtils;
import org.wso2.micro.integrator.security.user.core.model.ExpressionCondition;
import org.wso2.micro.integrator.security.user.core.model.ExpressionOperation;

/**
 * This class is to generate LDAP multi attribute search filter query.
 * Currently only support for 'AND' operation.
 */
public class LDAPFilterQueryBuilder {

    private static final String OPEN_PARENTHESIS = "(";
    private static final String CLOSE_PARENTHESIS = ")";
    private static final String AND_OPERATION = "&";
    private static final String OR_OPERATION = "|";
    private static final String EQUALS_SIGN = "=";
    private static final String ANY_STRING = "*";

    private StringBuilder searchFilter;
    private StringBuilder membershipMultiGroupFilters;

    public LDAPFilterQueryBuilder(String searchFilter) {

        this.searchFilter = new StringBuilder(OPEN_PARENTHESIS).append(AND_OPERATION).append(searchFilter);
        this.membershipMultiGroupFilters = new StringBuilder();
    }

    /**
     * Add a new filter to the query.
     *
     * @param condition                     Validated expression condition.
     * @param isMembershipMultiGroupFilters boolean value indicate is a membership multi group filters or not.
     */
    public void addFilter(ExpressionCondition condition, boolean isMembershipMultiGroupFilters) {

        String property = condition.getAttributeName();
        String operation = condition.getOperation();
        String value = condition.getAttributeValue();

        if (isMembershipMultiGroupFilters) {
            buildFilter(membershipMultiGroupFilters, property, operation, value);
        } else {
            buildFilter(searchFilter, property, operation, value);
        }
    }

    /**
     * Generate filter depends on given filter operation.
     *
     * @param queryBuilder Query builder.
     * @param property     Attribute name.
     * @param operation    Attribute value.
     * @param value        Filter query with provided filter.
     */
    private void buildFilter(StringBuilder queryBuilder, String property, String operation, String value) {

        if (ExpressionOperation.EQ.toString().equals(operation)) {
            queryBuilder.append(equalFilterBuilder(property, value));
        } else if (ExpressionOperation.CO.toString().equals(operation)) {
            queryBuilder.append(containsFilterBuilder(property, value));
        } else if (ExpressionOperation.EW.toString().equals(operation)) {
            queryBuilder.append(endWithFilterBuilder(property, value));
        } else if (ExpressionOperation.SW.toString().equals(operation)) {
            queryBuilder.append(startWithFilterBuilder(property, value));
        }
    }

    /**
     * Generate "EQ" filter.
     *
     * @param property Attribute name.
     * @param value    Attribute value.
     * @return Search Filter query with eq filter.
     */
    private String equalFilterBuilder(String property, String value) {

        StringBuilder filter = new StringBuilder();
        filter.append(OPEN_PARENTHESIS).append(property).append(EQUALS_SIGN).append(value).append(CLOSE_PARENTHESIS);
        return filter.toString();
    }

    /**
     * Generate "CO" filter.
     *
     * @param property Attribute name.
     * @param value    Attribute value.
     * @return Filter query with contains filter.
     */
    private String containsFilterBuilder(String property, String value) {

        StringBuilder filter = new StringBuilder();
        filter.append(OPEN_PARENTHESIS).append(property).append(EQUALS_SIGN).append(ANY_STRING).append(value).
                append(ANY_STRING).append(CLOSE_PARENTHESIS);
        return filter.toString();
    }

    /**
     * Generate "EW" filter.
     *
     * @param property Attribute name.
     * @param value    Attribute value.
     * @return Filter query with end-with filter.
     */
    private String endWithFilterBuilder(String property, String value) {

        StringBuilder filter = new StringBuilder();
        filter.append(OPEN_PARENTHESIS).append(property).append(EQUALS_SIGN).append(ANY_STRING).append(value).
                append(CLOSE_PARENTHESIS);
        return filter.toString();
    }

    /**
     * Generate "SW" filter.
     *
     * @param property Attribute name.
     * @param value    Attribute value.
     * @return Filter query with start-with filter.
     */
    private String startWithFilterBuilder(String property, String value) {

        StringBuilder filter = new StringBuilder();
        filter.append(OPEN_PARENTHESIS).append(property).append(EQUALS_SIGN).append(value).append(ANY_STRING).
                append(CLOSE_PARENTHESIS);
        return filter.toString();
    }

    /**
     * Get final search filter query.
     *
     * @return filter query string.
     */
    public String getSearchFilterQuery() {

        if (searchFilter != null) {
            if (StringUtils.isNotEmpty(membershipMultiGroupFilters.toString())) {
                searchFilter.append(OPEN_PARENTHESIS).append(OR_OPERATION).append(membershipMultiGroupFilters).
                        append(CLOSE_PARENTHESIS);
            }
            searchFilter.append(CLOSE_PARENTHESIS);
            return String.valueOf(searchFilter);
        } else {
            return "";
        }
    }
}
