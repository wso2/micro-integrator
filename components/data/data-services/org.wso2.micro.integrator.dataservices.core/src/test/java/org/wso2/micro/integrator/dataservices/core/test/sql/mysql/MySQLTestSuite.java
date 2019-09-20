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
package org.wso2.micro.integrator.dataservices.core.test.sql.mysql;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class MySQLTestSuite extends TestCase {

	public static Test suite() {
		TestSuite suite = new TestSuite(
				"Test for org.wso2.carbon.dataservices.core.test.sql.mysql");
		//$JUnit-BEGIN$
		suite.addTestSuite(MySQLInitTest.class);
		suite.addTestSuite(MySQLBasicTest.class);
		suite.addTestSuite(MySQLDMLServiceTest.class);
		suite.addTestSuite(MySQLInputMappingServiceTest.class);
		suite.addTestSuite(MySQLNestedQueryTest.class);
		suite.addTestSuite(MySQLStoredProcedureServiceTest.class);
		//suite.addTestSuite(MySQLResourceServiceTest.class);
		suite.addTestSuite(MySQLAdvancedStoredProcServiceTest.class);
		suite.addTestSuite(MySQLBinaryDataServiceTest.class);
		suite.addTestSuite(MySQLFinalizeTest.class);
		//$JUnit-END$
		return suite;
	}

}
