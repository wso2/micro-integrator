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
package org.wso2.micro.integrator.dataservices.core.script;

import java.util.ArrayList;
import java.util.List;

import org.wso2.micro.integrator.dataservices.core.DBUtils;

/**
 * This class create SQL Statement in Dynamically When Consider One Statement it
 * build by part by part in the Runtime after complete the Statement it executed
 * and finally return the Result as a single query so it can use for SQL query
 * operations
 */

public class DynamicSqlUtils {

	/**
	 * select All SQL Statement
	 * <p/>
	 * SELECT * FROM [table name];
	 * 
	 * @param tableName
	 *            -table Name of the given table
	 * @param schema the database schema
	 * @return SQL statement as a String
	 */
	public String getSelectAll(String tableName, String schema, String columnNames) {
		StringBuffer statement = new StringBuffer();
		statement.append("SELECT " + columnNames.trim() + " FROM ");
		statement.append((DBUtils.isEmptyString(schema) ? "" : (schema + ".")) + tableName.trim());
		return new String(statement);
	}

	/**
	 * This method creates select by key, SQL Statement
	 * <p/>
	 * SELECT * FROM [table name] WHERE [field name] = "whatever";
	 * 
	 * @param tableName
	 *            -table Name of the given table
	 * @param schema the database schema
	 * @param pKey
	 *            -primary key of table
	 * @return SQL statement as a String
	 */
	public String getSelectByKey(String tableName, String schema, 
			String pKey, String columnNames) {
		StringBuffer statement = new StringBuffer();
		statement.append("SELECT " + columnNames.trim() + " FROM ");
		statement.append((DBUtils.isEmptyString(schema) ? "" : (schema + ".")) + tableName.trim());
		statement.append(" WHERE ");
		statement.append(pKey).append("=?");
		return new String(statement);
	}

	/**
	 * This method creates Data Insertion SQL Statement
	 * <p/>
	 * INSERT INTO tableName(c1,c2,c3) VALUES(p1,p2,p3)
	 * 
	 * @param param
	 *            -List of parameters values
	 * @param tableName
	 *            -table Name of the given table
	 * @param schema the database schema
	 * @return SQL statement as a String
	 */
	public String getInsertStatement(String tableName, String schema, List<String> param) {
		StringBuffer statement = new StringBuffer();
		statement.append("INSERT INTO ");
		statement.append((DBUtils.isEmptyString(schema) ? "" : (schema + ".")) + tableName.trim());
		statement.append("(");
		int last = param.size();
		int index = 1;
		for (String par : param) {
			statement.append(par);
			if (index != last)
				statement.append(",");
			index++;
		}
		statement.append(")");
		statement.append(" VALUES");
		statement.append("(");
		index = 1;
		/* use this fore each loop just to travel inside list */
		// noinspection UnusedDeclaration
		for (@SuppressWarnings("unused")
		String par : param) {
			statement.append('?');
			if (index != last)
				statement.append(",");
			index++;
		}
		statement.append(")");
		return new String(statement);
	}

	/**
	 * This method creates Data Update SQL Statement
	 * <p/>
	 * UPDATE [table name] SET Select_prev = 'Y',Update_prev = 'Y' where [field
	 * name] = 'user';
	 * 
	 * @param param2
	 *            -List of parameters values
	 * @param tableName
	 *            -table Name of the given table
	 * @param schema the database schema
	 * @param pKey
	 *            -primary key of table
	 * @return SQL statement as a String
	 */
	public String getUpdateStatement(String tableName, String schema, List<String> param2,
			String pKey) {
		List<String> param = new ArrayList<String>();
		for (String par : param2) {
			if (!par.equals(pKey))
				param.add(par);
		}

		StringBuffer statement = new StringBuffer();
		statement.append("UPDATE ");
		statement.append((DBUtils.isEmptyString(schema) ? "" : (schema + ".")) + tableName.trim());
		statement.append(" SET ");
		int last = param.size();
		int index = 1;
		for (String par : param) {

			statement.append(par).append("=?");
			if (index != last)
				statement.append(",");

			index++;
		}
		statement.append(" WHERE ");
		statement.append(pKey).append("=?");
		return new String(statement);
	}

	/**
	 * This method creates Data Delete SQL Statement
	 * <p/>
	 * DELETE from [table name] where [field name] = 'whatever';
	 * 
	 * @param tableName
	 *            -table Name of the given table
	 * @param schema the database schema
	 * @param pKey
	 *            -primary key of table
	 * @return SQL statement as a String
	 */
	public String getDeleteStatement(String tableName, String schema, String pKey) {
		StringBuffer statement = new StringBuffer();
		statement.append("DELETE FROM ");
		statement.append((DBUtils.isEmptyString(schema) ? "" : (schema + ".")) + tableName.trim());
		statement.append(" WHERE ");
		statement.append(pKey).append("=?");
		return new String(statement);
	}

	public String getProcedureInvokeStatement(String proceName) {
		return "call " + proceName + "(?)";
	}

}
