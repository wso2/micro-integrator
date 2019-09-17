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

import java.util.HashMap;
import java.util.Map;

/**
 * Feed a array using ,wso2 define sql types for Data-service return the static
 * array which contain the data types
 */
public class DSSqlTypes {

	private static Map<Integer, String> definedTypeMap = new HashMap<Integer, String>();

	private static Map<Integer, String> qnameTypeMap = new HashMap<Integer, String>();

	static {
		definedTypeMap.put(java.sql.Types.CHAR, "STRING");
		definedTypeMap.put(java.sql.Types.NUMERIC, "NUMERIC");
		definedTypeMap.put(java.sql.Types.DECIMAL, "DOUBLE");
		definedTypeMap.put(java.sql.Types.INTEGER, "INTEGER");
		definedTypeMap.put(java.sql.Types.SMALLINT, "SMALLINT");
		definedTypeMap.put(java.sql.Types.FLOAT, "DOUBLE");
		definedTypeMap.put(java.sql.Types.REAL, "REAL");
		definedTypeMap.put(java.sql.Types.DOUBLE, "DOUBLE");
		definedTypeMap.put(java.sql.Types.VARCHAR, "STRING");
                definedTypeMap.put(java.sql.Types.NVARCHAR, "STRING");
		// typeMap.put(java.sql.Types.LONGNVARCHAR, "STRING");
		// typeMap.put(java.sql.Types.NCHAR, "STRING");
		// typeMap.put(java.sql.Types.LONGNVARCHAR, "STRING");
		// typeMap.put(java.sql.Types.NVARCHAR, "STRING");
		definedTypeMap.put(java.sql.Types.CLOB, "STRING");
		// typeMap.put(java.sql.Types.SQLXML, "STRING");
		definedTypeMap.put(java.sql.Types.BOOLEAN, "BOOLEAN");
		definedTypeMap.put(java.sql.Types.TIMESTAMP, "TIMESTAMP");
		definedTypeMap.put(java.sql.Types.BIT, "BIT");
		definedTypeMap.put(java.sql.Types.TIME, "TIME");
		definedTypeMap.put(java.sql.Types.TINYINT, "TINYINT");
		definedTypeMap.put(java.sql.Types.BIGINT, "BIGINT");
		definedTypeMap.put(java.sql.Types.LONGVARBINARY, "BINARY");
		definedTypeMap.put(java.sql.Types.VARBINARY, "BINARY");
		definedTypeMap.put(java.sql.Types.BINARY, "BINARY");
		definedTypeMap.put(java.sql.Types.BLOB, "BINARY");
		definedTypeMap.put(java.sql.Types.DATE, "DATE");
		definedTypeMap.put(java.sql.Types.TIMESTAMP, "TIMESTAMP");

		qnameTypeMap.put(java.sql.Types.CHAR, "string");
		qnameTypeMap.put(java.sql.Types.NUMERIC, "integer");
		qnameTypeMap.put(java.sql.Types.DECIMAL, "decimal");
		qnameTypeMap.put(java.sql.Types.INTEGER, "integer");
		qnameTypeMap.put(java.sql.Types.SMALLINT, "integer");
		qnameTypeMap.put(java.sql.Types.FLOAT, "float");
		qnameTypeMap.put(java.sql.Types.REAL, "double");
		qnameTypeMap.put(java.sql.Types.DOUBLE, "double");
		qnameTypeMap.put(java.sql.Types.VARCHAR, "string");
                qnameTypeMap.put(java.sql.Types.NVARCHAR, "string");
		// typeMap.put(java.sql.Types.LONGNVARCHAR, "string");
		// typeMap.put(java.sql.Types.NCHAR, "string");
		// typeMap.put(java.sql.Types.NVARCHAR, "string");
		// typeMap.put(java.sql.Types.LONGNVARCHAR, "string");
		qnameTypeMap.put(java.sql.Types.CLOB, "string");
		// typeMap.put(java.sql.Types.SQLXML, "string");
		qnameTypeMap.put(java.sql.Types.BOOLEAN, "boolean");
		qnameTypeMap.put(java.sql.Types.TIMESTAMP, "dateTime");
		qnameTypeMap.put(java.sql.Types.BIT, "integer");
		qnameTypeMap.put(java.sql.Types.TIME, "time");
		qnameTypeMap.put(java.sql.Types.TINYINT, "integer");
		qnameTypeMap.put(java.sql.Types.BIGINT, "long");
		qnameTypeMap.put(java.sql.Types.LONGVARBINARY, "base64Binary");
		qnameTypeMap.put(java.sql.Types.VARBINARY, "base64Binary");
		qnameTypeMap.put(java.sql.Types.BINARY, "base64Binary");
		qnameTypeMap.put(java.sql.Types.BLOB, "base64Binary");
		qnameTypeMap.put(java.sql.Types.DATE, "date");
	}

	public static Map<Integer, String> getDefinedTypes() {
		return definedTypeMap;
	}

	public static String getQNameType(int jdbcType) {
		return qnameTypeMap.get(jdbcType);
	}
}
