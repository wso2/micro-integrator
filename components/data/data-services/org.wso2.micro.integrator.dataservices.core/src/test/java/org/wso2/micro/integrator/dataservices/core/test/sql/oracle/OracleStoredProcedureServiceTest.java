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

package org.wso2.micro.integrator.dataservices.core.test.sql.oracle;

import org.wso2.micro.integrator.dataservices.core.test.sql.AbstractStoredProcedureServiceTest;

public class OracleStoredProcedureServiceTest extends AbstractStoredProcedureServiceTest{

	public OracleStoredProcedureServiceTest(String testName) {
		super(testName, "OracleNestedQueryStoredProcService");
	}
	
	public void testOracleStoredProcNoParams() {
		this.storedProcNoParams();
	}
	
	public void testOracleStoredProcWithParams() {
		this.storedProcWithParams();
	}
	
	public void testStoredProcNested1ForDateTime() {
		this.storedProcNested1ForDateTime();
	}
	
	public void testStoredProcNested2ForDateTime() {
		this.storedProcNested2ForDateTime();
	}
	
	public void testStoredProcNested3ForDateTime() {
		this.storedProcNested3ForDateTime();
	}
	
	public void testOracleStoredFuncNoParams() {
		this.storedFuncNoParams();
	}
	
	public void testOracleStoredFuncWithParams() {
		this.storedFuncWithParams();
	}

}
