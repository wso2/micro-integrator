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

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.xml.namespace.QName;

import org.apache.axiom.om.OMElement;
import org.wso2.micro.integrator.dataservices.core.test.DataServiceBaseTestCase;
import org.wso2.micro.integrator.dataservices.core.test.util.TestUtils;

/**
 * Class to represent input mappings test cases.
 */
public abstract class AbstractInputMappingServiceTest extends DataServiceBaseTestCase {

	private String epr = null;

	public AbstractInputMappingServiceTest(String testName, String serviceName) {
		super(testName);
		this.epr = this.baseEpr + serviceName;
	}
	
	/**
	 * Test with simple input mappings - 1.
	 */
	protected void inputMappings1() {
		TestUtils.showMessage(this.epr + " - inputMappings1");
		Map<String, String> params = new HashMap<String, String>();
		params.put("orderNumber", "10242");
		params.put("orderDate", "2004-04-20");
		try {
            TestUtils.checkForService(this.epr);
			OMElement result = TestUtils.callOperation(this.epr,
					"select_input_mappings_op1", params);
			assertTrue(TestUtils.validateResultStructure(result,
					TestUtils.ORDERS_XSD_PATH));
			String val = TestUtils.getFirstValue(result,
					"/Orders/Order/orderNumber",
					TestUtils.DEFAULT_DS_WS_NAMESPACE);
			assertTrue(params.get("orderNumber").equals(val));
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}
	
	/**
	 * Test with simple input mapping - 1 - For DateTime
	 */
	protected void inputMappings1ForDateTime() {
		TestUtils.showMessage(this.epr + " - inputMappings1ForDateTime");
		Map<String, String> params = new HashMap<String, String>();
		params.put("orderNumber", "10242");
		params.put("orderDate", "2004-04-20");
		try {
            TestUtils.checkForService(this.epr);
			OMElement result = TestUtils.callOperation(this.epr,
					"select_input_mappings_for_date_time_op1", params);
			assertTrue(TestUtils.validateResultStructure(result,
					TestUtils.ORDERS_WITH_DATETIME_PATH));
			String val = TestUtils.getFirstValue(result,
					"/Orders/Order/orderNumber",
					TestUtils.DEFAULT_DS_WS_NAMESPACE);
			assertTrue(params.get("orderNumber").equals(val));

		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}

	/**
	 * Test with simple input mappings - 2.
	 */
	protected void inputMappings2() {
		TestUtils.showMessage(this.epr + " - inputMappings2");
		Map<String, String> params = new HashMap<String, String>();
		params.put("customerNumber", "141");
		params.put("checkNumber", "HJ32686");
		params.put("amount", "59830.55");
		try {
            TestUtils.checkForService(this.epr);
			OMElement result = TestUtils.callOperation(this.epr,
					"select_input_mappings_op2", params);
			assertTrue(TestUtils.validateResultStructure(result,
					TestUtils.PAYMENTS_XSD_PATH));
			//TODO: check attribute value
//			String val1 = TestUtils.getFirstValue(result,
//					"/Payments/Payment@customerNumber",
//					TestUtils.DEFAULT_DS_WS_NAMESPACE);
			String val2 = TestUtils.getFirstValue(result,
					"/Payments/Payment/checkNumber",
					TestUtils.DEFAULT_DS_WS_NAMESPACE);
			assertTrue(params.get("checkNumber").equals(val2));
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}
	
	/**
	 * Test with simple input mappings - 2 - For DateTime
	 */
	protected void inputMappings2ForDateTime(){
		TestUtils.showMessage(this.epr + " - inputMappings2ForDateTime");
		Map<String, String> params = new HashMap<String, String>();
		params.put("customerNumber", "141");
		params.put("checkNumber", "HJ32686");
		params.put("amount", "59830.55");
		try {
            TestUtils.checkForService(this.epr);
			OMElement result = TestUtils.callOperation(this.epr,
					"select_input_mappings_for_date_time_op2", params);
			assertTrue(TestUtils.validateResultStructure(result,
					TestUtils.PAYMENTS_WITH_DATETIME_XSD_PATH));
			// TODO: check attribute value
			// String val1 = TestUtils.getFirstValue(result,
			// "/Payments/Payment@customerNumber",
			// TestUtils.DEFAULT_DS_WS_NAMESPACE);
			String val2 = TestUtils.getFirstValue(result,
					"/Payments/Payment/checkNumber",
					TestUtils.DEFAULT_DS_WS_NAMESPACE);
			assertTrue(params.get("checkNumber").equals(val2));
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}

	/**
	 * Test call-query merge with input mappings.
	 */
	@SuppressWarnings("unchecked")
	protected void inputMappingsCallQueryMerge() {
		TestUtils.showMessage(this.epr + " - inputMappingsCallQueryMerge");
		Map<String, String> params = new HashMap<String, String>();
		params.put("customerNumber", "103");
		params.put("paymentDate", "2004-12-18");
		try {
            TestUtils.checkForService(this.epr);
			OMElement result = TestUtils.callOperation(this.epr,
					"select_input_mappings_merge_op", params);
			assertTrue(TestUtils.validateResultStructure(result,
					TestUtils.PAYMENTS_XSD_PATH));
			Iterator<OMElement> elItr = result.getChildrenWithName(new QName("Payment"));
			int c = 0;
			while (elItr.hasNext()) {
				assertNotNull(elItr.next());
				c++;
			}
			assertTrue(c == 3);
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}
	
	/**
	 * Test call-query merge with input mappings - For DateTime
	 */
	@SuppressWarnings("unchecked")
	protected void inputMappingsCallQueryMergeForDateTime(){
		TestUtils.showMessage(this.epr + " - inputMappingsCallQueryMergeForDateTime");
		Map<String, String> params = new HashMap<String, String>();
		params.put("customerNumber", "103");
		params.put("paymentDate", "2004-12-18");
		try {
            TestUtils.checkForService(this.epr);
			OMElement result = TestUtils.callOperation(this.epr,
					"select_input_mappings_merge_for_date_time_op", params);
			assertTrue(TestUtils.validateResultStructure(result,
					TestUtils.PAYMENTS_WITH_DATETIME_XSD_PATH));
			Iterator<OMElement> elItr = result.getChildrenWithName(new QName("Payment"));
			int c = 0;
			while (elItr.hasNext()) {
				assertNotNull(elItr.next());
				c++;
			}
			assertTrue(c == 3);
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}
	
    /**
     * Test with simple input mappings with default values
     */

    protected void inputMappingsWithDefValue() {
        TestUtils.showMessage(this.epr + " - inputMappingsWithDefValue");
        Map<String, String> params = new  HashMap<String, String>();
        try {
            TestUtils.checkForService(this.epr);
            OMElement result = TestUtils.callOperation(this.epr,
					"results_with_default_values_op", params);
			assertTrue(TestUtils.validateResultStructure(result,
					TestUtils.PAYMENTS_XSD_PATH));
        } catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
    }
    
    /**
	 * Test with simple input mappings with default values - For DateTime
	 */
	protected void inputMappingsWithDefValueForDateTime(){
		TestUtils.showMessage(this.epr + " - inputMappingsWithDefValueForDateTime");
		Map<String, String> params = new HashMap<String, String>();
		try {
            TestUtils.checkForService(this.epr);
			OMElement result = TestUtils.callOperation(this.epr,
					"results_with_default_values_for_date_time_op", params);
			assertTrue(TestUtils.validateResultStructure(result,
					TestUtils.PAYMENTS_WITH_DATETIME_XSD_PATH));
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}

}
