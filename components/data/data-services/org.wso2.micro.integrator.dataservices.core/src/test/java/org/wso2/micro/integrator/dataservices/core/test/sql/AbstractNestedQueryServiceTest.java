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
package org.wso2.micro.integrator.dataservices.core.test.sql;

import org.apache.axiom.om.OMElement;
import org.wso2.micro.integrator.dataservices.core.test.DataServiceBaseTestCase;
import org.wso2.micro.integrator.dataservices.core.test.util.TestUtils;

/**
 * Class to represent nested sql query test cases.
 */
public abstract class AbstractNestedQueryServiceTest extends
		DataServiceBaseTestCase {

	private String epr = null;

	public AbstractNestedQueryServiceTest(String testName, String serviceName) {
		super(testName);
		this.epr = this.baseEpr + serviceName;
	}

	protected void nestedQuery1() {
		TestUtils.showMessage(this.epr + " - nestedQuery1");
		try {
            TestUtils.checkForService(this.epr);
			OMElement result = TestUtils.callOperation(this.epr,
					"payment_info_op", null);
			assertTrue(TestUtils.validateResultStructure(result,
					TestUtils.PAYMENT_INFO_NESTED_XSD_PATH));
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}

	protected void nestedQuery2() {
		TestUtils.showMessage(this.epr + " - nestedQuery2");
		try {
            TestUtils.checkForService(this.epr);
			OMElement result = TestUtils.callOperation(this.epr,
					"order_details_op", null);
			assertTrue(TestUtils.validateResultStructure(result,
					TestUtils.ORDER_DETAILS_NESTED_XSD_PATH));
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}
	
	/*
	 * Test nested query with DateTime 
	 */
	protected void nestedQuery2ForDateTime(){
		TestUtils.showMessage(this.epr + " - nestedQuery2ForDateTime");
		try {
            TestUtils.checkForService(this.epr);
			OMElement result = TestUtils.callOperation(this.epr,
					"order_details_for_date_time_op", null);
			assertTrue(TestUtils.validateResultStructure(result,
					TestUtils.ORDER_DETAILS_NESTED_WITH_DATETIME_XSD_PATH));
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}

}
