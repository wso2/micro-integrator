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

import java.util.ArrayList;
import java.util.List;

import org.apache.axiom.om.OMElement;
import org.wso2.micro.integrator.dataservices.core.test.DataServiceBaseTestCase;
import org.wso2.micro.integrator.dataservices.core.test.util.TestUtils;

/**
 * Class to represent basic sql query test cases.
 */
public abstract class AbstractBasicServiceTest extends DataServiceBaseTestCase {

	private String epr = null;
	
	public AbstractBasicServiceTest(String testName, String serviceName) {
		super(testName);
		this.epr = this.baseEpr + serviceName;
	}

	/**
	 * Test with a simple select statement with the given fields.
	 */
	protected void basicSelectWithFields() {
		TestUtils.showMessage(this.epr + " - basicSelectWithFields");
		try {
            TestUtils.checkForService(this.epr);
			OMElement result = TestUtils.callOperation(this.epr,
					"select_op_given_fields", null);
			assertTrue(TestUtils.validateResultStructure(result, TestUtils.CUSTOMER_XSD_PATH));
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}

	/**
	 * Test with a simple select statement with all fields.
	 */
	protected void basicSelectAll() {
		TestUtils.showMessage(this.epr + " - basicSelectAll");
		try {
            TestUtils.checkForService(this.epr);
			OMElement result = TestUtils.callOperation(this.epr,
					"select_op_all_fields", null);
			assertTrue(TestUtils.validateResultStructure(result, TestUtils.PRODUCTS_XSD_PATH));
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
		assertTrue(true);
	}

	/**
	 * Test with a simple select statement to get a row count.
	 */
	protected void basicSelectCount() {
		TestUtils.showMessage(this.epr + " - basicSelectCount");
		try {
            TestUtils.checkForService(this.epr);
			OMElement result = TestUtils.callOperation(this.epr,
					"select_op_count", null);
			assertTrue(TestUtils.validateResultStructure(result, TestUtils.ORDER_COUNT_XSD_PATH));
			String val = TestUtils.getFirstValue(result,
					"/Orders/OrderDetails/orderDetailsCount",
					TestUtils.DEFAULT_DS_WS_NAMESPACE);
			assertTrue(Integer.parseInt(val) > 0);	
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}
	
	/**
	 * Test with a simple select statement with attributes.
	 */
	protected void basicSelectWithAttributes() {
		TestUtils.showMessage(this.epr + " - basicSelectWithAttributes");
		try {
            TestUtils.checkForService(this.epr);
			OMElement result = TestUtils.callOperation(this.epr,
					"select_attributes_op", null);
			assertTrue(TestUtils.validateResultStructure(result, TestUtils.PAYMENTS_XSD_PATH));
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}
	
	/**
	 * Test with a simple select statement with attributes and DateTime field
	 */
	protected void basicSelectWithAttributesForDateTime(){
		TestUtils.showMessage(this.epr + " -basicSelectWithAttributesForDateTime");
		try{
            TestUtils.checkForService(this.epr);
			OMElement result = TestUtils.callOperation(this.epr, "select_attributes_for_date_time_op", null);
			assertTrue(TestUtils.validateResultStructure(result, TestUtils.PAYMENTS_WITH_DATETIME_XSD_PATH));
			
		}catch(Exception e){
			e.printStackTrace();
			fail(e.getMessage());
		}
	}

	private List<String> getList(String name, String val) {
		List<String> list = new ArrayList<String>();
		list.add(name);
		list.add(val);
		return list;
	}
	
    /**
     * Test for array types
     */
    protected void basicArrayInputTypes(){
        TestUtils.showMessage(this.epr + " - arrayInputTypes");
        List<List<String>> params = new ArrayList<List<String>>();
        params.add(this.getList("country", "USA"));
		params.add(this.getList("state", "CA"));
		params.add(this.getList("state", "MA"));
		params.add(this.getList("creditLimit", "50000"));
		params.add(this.getList("city", "San Rafael"));
		params.add(this.getList("city", "San Francisco"));
		params.add(this.getList("city", "San Diego"));
		params.add(this.getList("city", "Los Angeles"));
		params.add(this.getList("city", "Brisbane"));
		params.add(this.getList("contactFirstName", "Julie"));
		try {
            TestUtils.checkForService(this.epr);
			OMElement result = TestUtils.callOperationWithArray(this.epr, "basic_array_type_op", params);
			assertTrue(TestUtils.validateResultStructure(result, TestUtils.CUSTOMER_XSD_PATH));
			String val = TestUtils.getFirstValue(result, "/Customers/Customer/customerNumber",
					TestUtils.DEFAULT_DS_WS_NAMESPACE);
			assertTrue(Integer.parseInt(val) > 0);
		} catch (Exception e) {
			
			e.printStackTrace();
			fail(e.getMessage());
		}
    }
}
