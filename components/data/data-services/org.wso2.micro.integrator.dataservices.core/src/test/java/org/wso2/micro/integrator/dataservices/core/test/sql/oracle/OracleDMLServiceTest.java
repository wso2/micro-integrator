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

import org.wso2.micro.integrator.dataservices.core.test.sql.AbstractDMLServiceTest;

public class OracleDMLServiceTest extends AbstractDMLServiceTest {

	public OracleDMLServiceTest(String testName) {
		super(testName, "OracleDMLService");
	}
	
	public void testOracleDMLOperations() {
		this.doDMLOperations();
	}
	
	public void testOracleDMLOperationsWithNoResultNestedQuery() {
		this.doDMLOperationsWithNoResultNestedQuery();
	}
	
	public void testOracleDMLOperationsWithNoResultStoredProcNestedQuery() {
		this.doDMLOperationsWithNoResultStoredProcNestedQuery();
	}
	
	public void testOracleDMLOperationsVal1() {
		this.doDMLOperationsVal1();
	}
	
	public void testOracleDMLOperationsVal2() {
		this.doDMLOperationsVal2();
	}
	
	public void testOracleDMLOperationsVal3() {
		this.doDMLOperationsVal3();
	}
	
	public void testOracleDMLOperationsVal4() {
		this.doDMLOperationsVal4();
	}
	
	public void testOracleDMLOperationsVal5() {
		this.doDMLOperationsVal5();
	}
	
	public void testOracleDMLOperationsVal6() {
		this.doDMLOperationsVal6();
	}

}
