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
package org.wso2.micro.integrator.ndatasource.common;

/**
 * This class represents the constants related to data sources.
 */
public class DataSourceConstants {

	public static final String DATASOURCES_REPOSITORY_BASE_PATH = "/repository/components/org.wso2.carbon.ndatasource";

	public static final String SECURE_VAULT_NS = "http://org.wso2.securevault/configuration";

	public static final String SECRET_ALIAS_ATTR_NAME = "secretAlias";

	public static final String ENCRYPTED_ATTR_NAME = "encrypted";

	public static final String DATASOURCES_DIRECTORY_NAME = "datasources";

	public static final String SYS_DS_FILE_NAME_SUFFIX = "-datasources.xml";

	public static final String MASTER_DS_FILE_NAME = "master-datasources.xml";

	public static final String DATASOURCES_SYNC_GROUP_NAME = "__CARBON_DATA_SOURCES";

	public static final class DataSourceStatusModes {

		public static final String ACTIVE = "ACTIVE";

		public static final String ERROR = "ERROR";

	}

}
