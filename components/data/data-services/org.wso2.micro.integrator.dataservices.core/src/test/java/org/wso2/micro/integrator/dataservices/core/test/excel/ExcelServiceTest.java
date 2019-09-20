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
package org.wso2.micro.integrator.dataservices.core.test.excel;

import org.apache.axiom.om.OMElement;
import org.wso2.micro.integrator.dataservices.core.test.DataServiceBaseTestCase;
import org.wso2.micro.integrator.dataservices.core.test.util.TestUtils;

public class ExcelServiceTest extends DataServiceBaseTestCase {

private String epr = null;
	
	public ExcelServiceTest(String testName) {
		super(testName);
		this.epr = this.baseEpr + "ExcelService";
	}
	
	/**
	 * Test Excel 2000/XP file datasource
	 */
	public void testExcelOldFormatNoHeaderWithInts() {
		TestUtils.showMessage(this.epr + " - testExcelOldFormatNoHeaderWithInts");
		try {
            TestUtils.checkForService(this.epr);
			OMElement result = TestUtils.callOperation(this.epr,
					"excel_old_noheader_with_ints_op", null);
			assertTrue(TestUtils.validateResultStructure(result,
					TestUtils.CUSTOMER_XSD_PATH));
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}
	
	/**
	 * Test Excel 2007 file datasource
	 */
	public void testExcelNewFormatNoHeader() {
		TestUtils.showMessage(this.epr + " - testExcelNewFormatNoHeader");
		try {
            TestUtils.checkForService(this.epr);
			OMElement result = TestUtils.callOperation(this.epr,
					"excel_new_op", null);
			assertTrue(TestUtils.validateResultStructure(result,
					TestUtils.CUSTOMER_XSD_PATH));
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}
	
	public void testExcelOldNoInts() {
		TestUtils.showMessage(this.epr + " - testExcelOldNoInts");
		try {
            TestUtils.checkForService(this.epr);
			OMElement result = TestUtils.callOperation(this.epr,
					"customers_old_no_ints_op", null);
			assertTrue(TestUtils.validateResultStructure(result,
					TestUtils.OFFICES_XSD_PATH));
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}
	
}
