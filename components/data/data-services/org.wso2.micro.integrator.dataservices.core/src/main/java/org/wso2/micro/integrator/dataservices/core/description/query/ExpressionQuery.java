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

package org.wso2.micro.integrator.dataservices.core.description.query;

import org.wso2.micro.integrator.dataservices.common.DBConstants;
import org.wso2.micro.integrator.dataservices.core.DataServiceFault;
import org.wso2.micro.integrator.dataservices.core.description.event.EventTrigger;
import org.wso2.micro.integrator.dataservices.core.engine.DataService;
import org.wso2.micro.integrator.dataservices.core.engine.InternalParam;
import org.wso2.micro.integrator.dataservices.core.engine.InternalParamCollection;
import org.wso2.micro.integrator.dataservices.core.engine.ParamValue;
import org.wso2.micro.integrator.dataservices.core.engine.QueryParam;
import org.wso2.micro.integrator.dataservices.core.engine.Result;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * This class represents Generalized Query Expressions which are similar to SQL and CQL.
 */
public abstract class ExpressionQuery extends Query {

    private String query;

    private List<String> namedParamNames;

    private int paramCount;

    private static final String QUESTION_MARK = "?";

    public ExpressionQuery(DataService dataService, String queryId, List<QueryParam> queryParams, String query,
                           Result result, String configId, EventTrigger inputEventTrigger,
                           EventTrigger outputEventTrigger, Map<String, String> advancedProperties,
                           String inputNamespace) {
        super(dataService, queryId, queryParams, result, configId, inputEventTrigger, outputEventTrigger,
              advancedProperties, inputNamespace);
        this.query = query;
    }

    public String getQuery() {
        return query;
    }

    public int getParamCount() {
        return paramCount;
    }

    /**
     * Pre-processing of the query
     *
     * @param query Query
     * @throws DataServiceFault
     */
    protected void init(String query) throws DataServiceFault {
        this.processNamedParams(query);
        this.query = createPreprocessedQueryFromQueryString(query);
        paramCount = calculateParamCount(this.query);
    }

    private void processNamedParams(String query) {
        Map<String, QueryParam> paramMap = new HashMap<>();
        for (QueryParam param : this.getQueryParams()) {
            paramMap.put(param.getName(), param);
        }
        List<String> paramNames = this.extractParamNames(query, paramMap.keySet());
        this.namedParamNames = new ArrayList<>();
        QueryParam tmpParam;
        String tmpParamName;
        int tmpOrdinal;
        Set<String> checkedQueryParams = new HashSet<>();
        Set<Integer> processedOrdinalsForNamedParams = new HashSet<>();
        for (int i = 0; i < paramNames.size(); i++) {
            String tmp = paramNames.get(i);
            if (!tmp.equals(QUESTION_MARK)) {
                tmpParamName = tmp;
                tmpParam = paramMap.get(tmpParamName);
                if (tmpParam != null) {
                    if (!checkedQueryParams.contains(tmpParamName)) {
                        tmpParam.clearOrdinals();
                        checkedQueryParams.add(tmpParamName);
                    }
                    this.namedParamNames.add(tmpParamName);
                    /* ordinals of named params */
                    tmpOrdinal = i + 1;
                    tmpParam.addOrdinal(tmpOrdinal);
                    processedOrdinalsForNamedParams.add(tmpOrdinal);
                }
            }
        }
        this.cleanupProcessedNamedParams(checkedQueryParams, processedOrdinalsForNamedParams, paramMap);
    }

    /**
     * This method is used to clean up the ordinal in the named parameter
     * scenario, where the Query may not have all the params as named parameters,
     * so other non-named parameters ordinals may clash with the processed one.
     */
    private void cleanupProcessedNamedParams(Set<String> checkedQueryParams,
                                             Set<Integer> processedOrdinalsForNamedParams,
                                             Map<String, QueryParam> paramMap) {
        QueryParam tmpQueryParam;
        for (String paramName : paramMap.keySet()) {
            if (!checkedQueryParams.contains(paramName)) {
                tmpQueryParam = paramMap.get(paramName);
                /* unchecked query param can only have one ordinal */
                if (processedOrdinalsForNamedParams.contains(tmpQueryParam.getOrdinal())) {
					/* set to a value that will not clash with valid ordinals */
                    tmpQueryParam.setOrdinal(0);
                }
            }
        }
    }

    private void sortStringsByLength(List<String> values) {
        Collections.sort(values, new Comparator<String>() {
            @Override
            public int compare(String lhs, String rhs) {
                return lhs.length() - rhs.length();
            }
        });
    }

    private String createPreprocessedQueryFromQueryString(String query) {
		/* get a copy of the param names */
        List<String> values = new ArrayList<>(namedParamNames);
		/* sort the strings */
        this.sortStringsByLength(values);
		/*
		 * make it from largest to smallest, this is done to make sure, if there
		 * are params like, :abcd,:abc, then the step of replacing :abc doesn't
		 * also initially replace :abcd's substring as well
		 */
        Collections.reverse(values);
        for (String val : values) {
			/* replace named params with ?'s */
            query = query.replaceAll(":" + val, QUESTION_MARK);
        }
        return query;
    }

    private List<String> extractParamNames(String query, Set<String> queryParams) {
        boolean doubleQuoteExists = false;
        boolean singleQuoteExists = false;
        List<String> paramNames = new ArrayList<>();
        String tmpParam;
        for (int i = 0; i < query.length(); i++) {
            if (query.charAt(i) == '\'') {
                singleQuoteExists = !singleQuoteExists;
            } else if (query.charAt(i) == '\"') {
                doubleQuoteExists = !doubleQuoteExists;
            } else if (query.charAt(i) == '?' && !(doubleQuoteExists || singleQuoteExists)) {
                paramNames.add(QUESTION_MARK);
            } else if (query.charAt(i) == ':' && !(doubleQuoteExists || singleQuoteExists)) {
				/* check if the string is at the end */
                if (i + 1 < query.length()) {
					/*
					 * split params in situations like ":a,:b", ":a :b", ":a:b",
					 * "(:a,:b)"
					 */
                    tmpParam = query.substring(i + 1, query.length()).split(" |,|\\)|\\(|:|\\r|\\n|\\.")[0];
                    if (queryParams.contains(tmpParam)) {
						/*
						 * only consider this as a parameter if it's in input
						 * mappings
						 */
                        paramNames.add(tmpParam);
                    }
                }
            }
        }
        return paramNames;
    }

    private int calculateParamCount(String query) {
        int n = 0;
        boolean doubleQuoteExists = false;
        boolean singleQuoteExists = false;
        for (char ch : query.toCharArray()) {
            if (ch == '\'') {
                singleQuoteExists = !singleQuoteExists;
            } else if (ch == '\"') {
                doubleQuoteExists = !doubleQuoteExists;
            } else if (ch == '?' && !(doubleQuoteExists || singleQuoteExists)) {
                n++;
            }
        }
        return n;
    }

    /**
     * This method checks whether DataTypes.QUERY_STRING type parameters are available in the query
     * input mappings and returns a boolean value.
     *
     * @param params The parameters in the input mappings
     * @return The boolean value of the isDynamicQuery variable
     */
    protected boolean isDynamicQuery(InternalParamCollection params) {
        boolean isDynamicQuery = false;
        InternalParam tmpParam;
        for (int i = 1; i <= params.getData().size(); i++) {
            tmpParam = params.getParam(i);
            if (DBConstants.DataTypes.QUERY_STRING.equals(tmpParam.getSqlType())) {
                isDynamicQuery = true;
                break;
            }
        }
        return isDynamicQuery;
    }

    /**
     * Returns the Query manipulated to suite the given parameters, e.g. adding
     * additional "?"'s for array types.
     */
    protected String createProcessedQuery(String query, InternalParamCollection params, int paramCount) {
        String currentQuery = query;
        int start = 0;
        Object[] vals;
        InternalParam param;
        ParamValue value;
        int count;
        for (int i = 1; i <= paramCount; i++) {
            param = params.getParam(i);
            if (param != null) {
                value = param.getValue();
                /*
                 * value can be null in stored proc OUT params, so it is simply
                 * treated as a single param, because the number of elements in an
                 * array cannot be calculated, since there's no actual value passed
                 * in
                 */
                if (value != null && (value.getValueType() == ParamValue.PARAM_VALUE_ARRAY)) {
                    count = (value.getArrayValue()).size();
                } else {
                    count = 1;
                }
                vals = this.expandQuery(start, count, currentQuery);
                start = (Integer) vals[0];
                currentQuery = (String) vals[1];
            }
        }
        return currentQuery;
    }

    /**
     * Given the starting position, this method searches for the first occurrence
     * of "?" and replace it with `count` "?"'s. Returns [0] - end position of
     * "?"'s, [1] - modified query.
     */
    private Object[] expandQuery(int start, int count, String query) {
        StringBuilder result = new StringBuilder();
        int n = query.length();
        boolean doubleQuoteExists = false;
        boolean singleQuoteExists = false;
        int end = n;
        for (int i = start; i < n; i++) {
            if (query.charAt(i) == '\'') {
                singleQuoteExists = !singleQuoteExists;
            } else if (query.charAt(i) == '\"') {
                doubleQuoteExists = !doubleQuoteExists;
            } else if (query.charAt(i) == '?' && !(doubleQuoteExists || singleQuoteExists)) {
                result.append(query.substring(0, i));
                result.append(this.generateQuestionMarks(count));
                end = result.length() + 1;
                if (i + 1 < n) {
                    result.append(query.substring(i + 1));
                }
                break;
            }
        }
        return new Object[] { end, result.toString() };
    }

    private String generateQuestionMarks(int n) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < n; i++) {
            builder.append(QUESTION_MARK);
            if (i + 1 < n) {
                builder.append(",");
            }
        }
        return builder.toString();
    }

    /**
     * Modifies the Query to include the direct value of the parameters of type
     * "QUERY_STRING"; The Query will be recreated and the other parameters will
     * be re-organized to point to correct ordinal values.
     *
     * @return [0] The updated Query, [1] The updated parameter count
     */
    protected Object[] processDynamicQuery(String query, InternalParamCollection params) {
        Integer[] paramIndices = this.extractQueryParamIndices(query);
        Map<String, QueryParam> tempParams = new HashMap<>();
        int currentOrdinalDiff = 0;
        int currentParamIndexDiff = 0;
        InternalParam tmpParam;
        int paramIndex;
        String tmpValue;
        int resultParamCount = paramCount;
        for (QueryParam queryParam : this.getQueryParams()) {
            tempParams.put(queryParam.getName(), queryParam);
        }
        for (int ordinal = 1; ordinal <= paramCount; ordinal++) {
            tmpParam = params.getParam(ordinal);
            if (tmpParam == null && !(((SQLQuery)this).getSqlQueryType() == SQLQuery.QueryType.UPDATE)) {
                throw new RuntimeException("Parameters are not Defined Correctly, missing parameter ordinal - "
                        + ordinal);
            }
            if (tmpParam == null && !(tempParams.get(namedParamNames.get(ordinal - 1)).isOptional())) {
                throw new RuntimeException("Parameters are not Defined Correctly, missing parameter ordinal - "
                        + ordinal);
            }
            if (tmpParam != null && !(tempParams.get(tmpParam.getName()).isOptional()) &&
                    DBConstants.DataTypes.QUERY_STRING.equals(tmpParam.getSqlType())) {
                paramIndex = paramIndices[ordinal - 1] + currentParamIndexDiff;
                tmpValue = params.getParam(ordinal).getValue().getScalarValue();
                currentParamIndexDiff += tmpValue.length() - 1;
                if (paramIndex + 1 < query.length()) {
                    query = query.substring(0, paramIndex) + tmpValue + query.substring(paramIndex + 1);
                } else {
                    query = query.substring(0, paramIndex) + tmpValue;
                }
                params.remove(ordinal);
                currentOrdinalDiff++;
                resultParamCount--;
            } else {
                if (params.getParam(ordinal) != null) {
                    params.remove(ordinal);
                    tmpParam.setOrdinal(ordinal - currentOrdinalDiff);
                    params.addParam(tmpParam);
                }
            }
        }
        return new Object[] { query, resultParamCount };
    }

    private Integer[] extractQueryParamIndices(String query) {
        List<Integer> result = new ArrayList<>();
        boolean doubleQuoteExists = false;
        boolean singleQuoteExists = false;
        char[] data = query.toCharArray();
        for (int i = 0; i < data.length; i++) {
            if (data[i] == '\'') {
                singleQuoteExists = !singleQuoteExists;
            } else if (data[i] == '\"') {
                doubleQuoteExists = !doubleQuoteExists;
            } else if (data[i] == '?' && !(doubleQuoteExists || singleQuoteExists)) {
                result.add(i);
            }
        }
        return result.toArray(new Integer[result.size()]);
    }

}
