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
package org.wso2.micro.integrator.ndatasource.rdbms;

/**
 * RDBMS data source constants.
 */
public class RDBMSDataSourceConstants {

	public static final String RDBMS_DATASOURCE_TYPE = "RDBMS";

	public static final String DATASOURCE_PROPS_NAME = "dataSourceProps";

	public static final String JDBC_INTERCEPTOR_SEPARATOR = ";";

	public static final String ROLLBACK_ON_RETURN = "rollbackOnReturn";

	public static final String CORRELATION_LOG_INTERCEPTOR = "org.wso2.micro.integrator.ndatasource.rdbms.CorrelationLogInterceptor";

	public static final String STANDARD_JDBC_INTERCEPTORS = "ConnectionState;StatementFinalizer;";

	public static final class TX_ISOLATION_LEVELS {

		public static final String NONE = "NONE";

		public static final String READ_COMMITTED = "READ_COMMITTED";

		public static final String READ_UNCOMMITTED = "READ_UNCOMMITTED";

		public static final String REPEATABLE_READ = "REPEATABLE_READ";

		public static final String SERIALIZABLE = "SERIALIZABLE";

	}

	public static final String[] CLASS_RETURN_TYPES = {"String", "Byte", "Character",
			"Short", "Integer", "Float", "Double", "Character", "Boolean"};

	public static final String INITIAL_SIZE = "0";

	public static final String MAX_IDLE = "8";

	public static final String MIN_IDLE = "0";

}
