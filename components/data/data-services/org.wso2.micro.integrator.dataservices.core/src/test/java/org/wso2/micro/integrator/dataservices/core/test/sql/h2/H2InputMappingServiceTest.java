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

import org.wso2.micro.integrator.dataservices.core.test.sql.AbstractInputMappingServiceTest;

public class H2InputMappingServiceTest extends AbstractInputMappingServiceTest {

	public H2InputMappingServiceTest(String testName) {
		super(testName, "H2InputMappingService");
	}
	
	public void testH2InputMappings1() {
		this.inputMappings1();
	}
	
    public void testH2InputMappings2() {
    	this.inputMappings2();
	}
    
    public void testH2InputMappingsWithDefValue() {
        this.inputMappingsWithDefValue();
    }
	
}
