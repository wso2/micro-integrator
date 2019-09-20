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

import org.wso2.micro.integrator.dataservices.core.test.sql.AbstractStoredProcedureServiceTest;

public class H2StoredProcedureServiceTest extends AbstractStoredProcedureServiceTest {

	public H2StoredProcedureServiceTest(String testName) {
		super(testName, "H2NestedQueryStoredProcService");
	}

	public void testH2StoredProcNoParams() {
		this.storedProcNoParams();
	}
	
	public void testH2StoredProcWithParams() {
		this.storedProcWithParams();
	}
	
	public void testStoredProcNested1() {
		this.storedProcNested1();
	}
	
	public void testStoredProcNested2() {
		this.storedProcNested2();
	}
	
	public void testStoredProcNested3() {
		this.storedProcNested3();
	}

    public void testStoredProccWithWrongResult() {
        this.storedProcWithWrongResultSet();
    }
	
	public void testH2StoredFuncNoParams() {
		this.storedFuncNoParams();
	}
	
	public void testH2StoredFuncWithParams() {
		this.storedFuncWithParams();
	}

        public void testH2StoredProcWithFaulty() {
                this.storedProcWithFaulty();
        }

}
