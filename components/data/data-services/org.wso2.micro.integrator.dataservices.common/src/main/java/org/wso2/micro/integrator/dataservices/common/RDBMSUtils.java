/*
 *  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */
package org.wso2.micro.integrator.dataservices.common;

import java.sql.Connection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.wso2.micro.integrator.dataservices.common.DBConstants.JDBCDriverPrefixes;
import org.wso2.micro.integrator.dataservices.common.DBConstants.XAJDBCDriverClasses;

/**
 * This class contain utilities related to RDBMS operations.
 */
public class RDBMSUtils {

	/**
	 * Returns the RDBMS engine name by analyzing the JDBC URL.
	 */
	public static String getRDBMSEngine(String jdbcUrl) {
        Pattern p = Pattern.compile("jdbc:[a-zA-Z0-9]+");
        Matcher m = p.matcher(jdbcUrl);
        while (m.find()) {
            if (JDBCDriverPrefixes.MYSQL.equals(m.group())) {
                return DBConstants.RDBMSEngines.MYSQL;
            } else if (JDBCDriverPrefixes.DERBY.equals(m.group())) {
                return DBConstants.RDBMSEngines.DERBY;
            } else if (JDBCDriverPrefixes.MSSQL.equals(m.group())) {
                return DBConstants.RDBMSEngines.MSSQL;
            } else if (JDBCDriverPrefixes.ORACLE.equals(m.group())) {
                return DBConstants.RDBMSEngines.ORACLE;
            } else if (JDBCDriverPrefixes.DB2.equals(m.group())) {
                return DBConstants.RDBMSEngines.DB2;
            } else if (JDBCDriverPrefixes.HSQLDB.equals(m.group())) {
                return DBConstants.RDBMSEngines.HSQLDB;
            } else if (JDBCDriverPrefixes.POSTGRESQL.equals(m.group())) {
                return DBConstants.RDBMSEngines.POSTGRESQL;
            } else if (JDBCDriverPrefixes.SYBASE.equals(m.group())) {
                return DBConstants.RDBMSEngines.SYBASE;
            } else if (JDBCDriverPrefixes.H2.equals(m.group())) {
                return DBConstants.RDBMSEngines.H2;
            } else if (JDBCDriverPrefixes.INFORMIX.equals(m.group())) {
                return DBConstants.RDBMSEngines.INFORMIX_SQLI;
            }
        }
        return DBConstants.RDBMSEngines.GENERIC;
	}

	public static int toIntTransactionIsolation(String isolation) {
		if (isolation != null && !"".equals(isolation)) {
			if ("TRANSACTION_NONE".equals(isolation)) {
				return Connection.TRANSACTION_NONE;
			} else if ("TRANSACTION_READ_COMMITTED".equals(isolation.trim())) {
				return Connection.TRANSACTION_READ_COMMITTED;
			} else if ("TRANSACTION_READ_UNCOMMITTED".equals(isolation.trim())) {
				return Connection.TRANSACTION_READ_UNCOMMITTED;
			} else if ("TRANSACTION_REPEATABLE_READ".equals(isolation.trim())) {
				return Connection.TRANSACTION_REPEATABLE_READ;
			} else if ("TRANSACTION_SERIALIZABLE".equals(isolation.trim())) {
				return Connection.TRANSACTION_SERIALIZABLE;
			} else {
				return -1;
			}
		} else {
			return -1;
		}
	}

	/**
	 * Returns the RDBMS engine name by analyzing the XA Datasource Class
	 */
	public static String getRDBMSEngine4XADataSource(String xaDatasourceClass) {
		if (XAJDBCDriverClasses.MYSQL.indexOf(xaDatasourceClass) > -1) {
			return DBConstants.RDBMSEngines.MYSQL;
		} else if (XAJDBCDriverClasses.DERBY.indexOf(xaDatasourceClass) > -1) {
			return DBConstants.RDBMSEngines.DERBY;
		} else if (XAJDBCDriverClasses.MSSQL.indexOf(xaDatasourceClass) > -1) {
			return DBConstants.RDBMSEngines.MSSQL;
		} else if (XAJDBCDriverClasses.ORACLE.indexOf(xaDatasourceClass) > -1) {
			return DBConstants.RDBMSEngines.ORACLE;
		} else if (XAJDBCDriverClasses.DB2.indexOf(xaDatasourceClass) > -1) {
			return DBConstants.RDBMSEngines.DB2;
		} else if (XAJDBCDriverClasses.HSQLDB.indexOf(xaDatasourceClass) > -1) {
			return DBConstants.RDBMSEngines.HSQLDB;
		} else if (XAJDBCDriverClasses.POSTGRESQL.indexOf(xaDatasourceClass) > -1) {
			return DBConstants.RDBMSEngines.POSTGRESQL;
		} else if (XAJDBCDriverClasses.SYBASE.indexOf(xaDatasourceClass) > -1) {
			return DBConstants.RDBMSEngines.SYBASE;
		} else if (XAJDBCDriverClasses.H2.indexOf(xaDatasourceClass) > -1) {
			return DBConstants.RDBMSEngines.H2;
		} else if (XAJDBCDriverClasses.INFORMIX.indexOf(xaDatasourceClass) > -1) {
			return DBConstants.RDBMSEngines.INFORMIX_SQLI;
		} else {
			return DBConstants.RDBMSEngines.GENERIC;
		}

	}

	public static boolean configPropContainsInV2(String propName) {
		return DBConstants.RDBMSv2ToV3Map.containsKey(propName);
	}
	
	public static String convertConfigPropFromV2toV3(String propName) {
		return DBConstants.RDBMSv2ToV3Map.get(propName);
	}

	public static Map<String, String> convertConfigPropsFromV2toV3(Map<String, String> props) {
		Entry<String, String> entry;
		String newPropName, oldPropName;
		Map<String, String> newValueMap = new HashMap<String, String>();
		for (Iterator<Entry<String, String>> itr = props.entrySet().iterator(); itr.hasNext();) {
			entry = itr.next();
			oldPropName = entry.getKey();
			if (configPropContainsInV2(oldPropName)) {
				newPropName = convertConfigPropFromV2toV3(oldPropName);
				if (newPropName != null) {
					newValueMap.put(newPropName, props.get(oldPropName));
				}
				itr.remove();
			}
		}
		props.putAll(newValueMap);
		return props;
	}
}
