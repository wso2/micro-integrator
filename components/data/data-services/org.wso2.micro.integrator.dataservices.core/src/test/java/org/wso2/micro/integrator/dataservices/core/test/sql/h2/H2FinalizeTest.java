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
package org.wso2.micro.integrator.dataservices.core.test.sql.h2;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

import org.wso2.micro.integrator.dataservices.core.test.DataServiceBaseTestCase;
import org.wso2.micro.integrator.dataservices.core.test.util.UtilServer;

public class H2FinalizeTest extends DataServiceBaseTestCase {

	public H2FinalizeTest() {
		super("H2FinalizeTest");
	}
	
	public void testH2Stop() throws Exception {
		UtilServer.stop();
		Connection conn = DriverManager.getConnection("jdbc:h2:mem:ds-test-db");
		Statement stmt = conn.createStatement();
		stmt.executeUpdate("DROP ALL OBJECTS");
		stmt.close();
		conn.close();
               endTenantFlow();
	}
	
}
