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
package org.wso2.micro.integrator.dataservices.core.test.csv;

import org.apache.axiom.om.OMElement;
import org.wso2.micro.integrator.dataservices.core.test.DataServiceBaseTestCase;
import org.wso2.micro.integrator.dataservices.core.test.util.TestUtils;

public class CSVServiceTest extends DataServiceBaseTestCase {

	private String epr = null;

	public CSVServiceTest(String testName) {
		super(testName);
		this.epr = this.baseEpr + "CSVService";
	}

	/**
	 * Test CSV data source by retrieving all the records in the file - with header
	 */
	public void testBasicCSVWithHeader() {
		TestUtils.showMessage(this.epr + " - testBasicCSVWithHeader");
		try {
            TestUtils.checkForService(this.epr);
			OMElement result = TestUtils.callOperation(this.epr,
					"customer_list_op", null);
			assertTrue(TestUtils.validateResultStructure(result,
					TestUtils.CUSTOMER_XSD_PATH));
			String val = TestUtils.getFirstValue(result,
					"/Customers/Customer/customerNumber",
					TestUtils.DEFAULT_DS_WS_NAMESPACE);
			assertTrue(Integer.parseInt(val) == 103);
		} catch (Exception e) {
			fail(e.getMessage());
		}
	}
	
	public void testBasicCSVNoHeader() {
		TestUtils.showMessage(this.epr + " - testBasicCSVNoHeader");
		try {
            TestUtils.checkForService(this.epr);
			OMElement result = TestUtils.callOperation(this.epr,
					"office_list_op", null);
			assertTrue(TestUtils.validateResultStructure(result,
					TestUtils.OFFICES_XSD_PATH));
			String val = TestUtils.getFirstValue(result,
					"/Offices/Office/city",
					TestUtils.DEFAULT_DS_WS_NAMESPACE);
			assertTrue(val.equals("San Francisco"));
		} catch (Exception e) {
			fail(e.getMessage());
		}
	}

}
