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
package org.wso2.micro.integrator.dataservices.core.test.tools;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

import org.wso2.micro.integrator.dataservices.core.test.DataServiceBaseTestCase;

public class ToolsInitTest extends DataServiceBaseTestCase {
	
	public ToolsInitTest() {
		super("ToolsInitTest");
	}
	
	public void testGenericStartup() throws Exception {
               startTenantFlow();
		Class.forName("org.h2.Driver");
		Connection conn = DriverManager.getConnection("jdbc:h2:mem:ds-test-db;DB_CLOSE_DELAY=-1");
		Statement stmt = conn.createStatement();
		stmt.executeUpdate("RUNSCRIPT FROM './src/test/resources/sql/CreateH2TestDB.sql'");
		stmt.close();
		conn.close();
	}
	

}
