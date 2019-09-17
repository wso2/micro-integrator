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
package org.wso2.micro.integrator.dataservices.core.engine;

import java.util.Set;

/**
 * Represents SQL Dialects SQL Query mapping
 */
public class SQLDialect {

	Set<String> sqlDialects;
	String sqlQuery;

	public SQLDialect() {

	}

	public SQLDialect(Set<String> sqlDialects, String sqlQuery) {
		super();
		this.sqlDialects = sqlDialects;
		this.sqlQuery = sqlQuery;
	}

	public Set<String> getSqlDialects() {
		return sqlDialects;
	}

	public void setSqlDialects(Set<String> sqlDialects) {
		this.sqlDialects = sqlDialects;
	}

	public String getSqlQuery() {
		return sqlQuery;
	}

	public void setSqlQuery(String sqlQuery) {
		this.sqlQuery = sqlQuery;
	}

}
