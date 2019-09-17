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
import java.util.Map;

import org.apache.axiom.om.OMElement;
import org.apache.axis2.AxisFault;
import org.wso2.micro.integrator.dataservices.core.test.DataServiceBaseTestCase;
import org.wso2.micro.integrator.dataservices.core.test.util.TestUtils;

/**
 * Class to represent DML sql query test cases.
 */
public abstract class AbstractDMLServiceTest extends DataServiceBaseTestCase {

	public static final String INCOMING_MESSAGE_IS_NULL_ERROR = "The input stream for an incoming message is null.";
	
	private static final String TMP_CUSTOMER_NUMBER = "450001";
	
	private String epr = null;

	public AbstractDMLServiceTest(String testName, String serviceName) {
		super(testName);
		this.epr = this.baseEpr + serviceName;
	}

	private int selectDataCount(String id) throws Exception {
		Map<String, String> params = new HashMap<String, String>();
		params.put("customerNumber", id);
        TestUtils.checkForService(this.epr);
		OMElement result = TestUtils.callOperation(this.epr,
				"select_count_id_op", params);
		String val = TestUtils.getFirstValue(result,
				"/Customers/CustomerDetails/customerCount",
				TestUtils.DEFAULT_DS_WS_NAMESPACE);
		return Integer.parseInt(val);
	}

	private Map<String, String> selectCustomerData(String id) throws Exception {
		Map<String, String> params = new HashMap<String, String>();
		params.put("customerNumber", id);
		OMElement result = TestUtils.callOperation(this.epr,
				"select_data_op", params);
		Map<String, String> data = new HashMap<String, String>();
		data.put("customerNumber", TestUtils.getFirstValue(result,
				"/Customers/Customer/customerNumber",
				TestUtils.DEFAULT_DS_WS_NAMESPACE));
		data.put("customerName", TestUtils.getFirstValue(result,
				"/Customers/Customer/customerName",
				TestUtils.DEFAULT_DS_WS_NAMESPACE));
		data.put("contactLastName", TestUtils.getFirstValue(result,
				"/Customers/Customer/contactLastName",
				TestUtils.DEFAULT_DS_WS_NAMESPACE));
		data.put("phone", TestUtils.getFirstValue(result,
				"/Customers/Customer/phone",
				TestUtils.DEFAULT_DS_WS_NAMESPACE));
		data.put("city", TestUtils.getFirstValue(result,
				"/Customers/Customer/city", 
				TestUtils.DEFAULT_DS_WS_NAMESPACE));
		return data;
	}

	private void deleteData(String id) throws Exception {
		Map<String, String> params = new HashMap<String, String>();
		params.put("customerNumber", id);
		// TODO: Fix exception occuring "org.apache.axis2.AxisFault: The input
		// stream for an incoming message is null"
		try {
            TestUtils.checkForService(this.epr);
			assertNotNull(TestUtils.callOperation(this.epr, "delete_data_op",
					params));
		} catch (AxisFault e) {
			// ignore for now
			if (!INCOMING_MESSAGE_IS_NULL_ERROR.equals(e.getReason())) {
				throw e;
			}
		}
	}

	private void insertData(Map<String, String> params) throws Exception {
		// TODO: Fix exception occuring "org.apache.axis2.AxisFault: The input
		// stream for an incoming message is null"
		try {
            TestUtils.checkForService(this.epr);
			assertNotNull(TestUtils.callOperation(this.epr, "insert_data_op",
					params));
		} catch (AxisFault e) {
			// ignore for now
			if (!INCOMING_MESSAGE_IS_NULL_ERROR.equals(e.getReason())) {
				throw e;
			}
		}
	}
	
	private void insertDataVal(Map<String, String> params) throws Exception {
		// TODO: Fix exception occuring "org.apache.axis2.AxisFault: The input
		// stream for an incoming message is null"
		try {
            TestUtils.checkForService(this.epr);
			assertNotNull(TestUtils.callOperation(this.epr, "insert_data_val_op",
					params));
		} catch (AxisFault e) {
			// ignore for now
			if (!INCOMING_MESSAGE_IS_NULL_ERROR.equals(e.getReason())) {
				throw e;
			}
		}
	}

	private void updateData(String id) throws Exception {
		Map<String, String> params = new HashMap<String, String>();
		params.put("customerNumber", id);
		// TODO: Fix exception occuring "org.apache.axis2.AxisFault: The input
		// stream for an incoming message is null"
		try {
            TestUtils.checkForService(this.epr);
			assertNotNull(TestUtils.callOperation(this.epr, "update_data_op",
					params));
		} catch (AxisFault e) {
			// ignore for now
			if (!INCOMING_MESSAGE_IS_NULL_ERROR.equals(e.getReason())) {
				throw e;
			}
		}
	}

	private void updateDataWithStoredProc(String id) throws Exception {
		Map<String, String> params = new HashMap<String, String>();
		params.put("customerNumber", id);
		// TODO: Fix exception occuring "org.apache.axis2.AxisFault: The input
		// stream for an incoming message is null"
		try {
            TestUtils.checkForService(this.epr);
			assertNotNull(TestUtils.callOperation(this.epr,
					"update_stored_proc_data_op", params));
		} catch (AxisFault e) {
			// ignore for now
			if (!INCOMING_MESSAGE_IS_NULL_ERROR.equals(e.getReason())) {
				throw e;
			}
		}
	}

	/**
     * Data Manipulation Test. Steps:-
     * > Delete record with the given id - remove any previous left over data, from a failed test perhaps.
     * > Insert record - insert a new record.
     * > Select record - select the just inserted record and checks if the data has been inserted properly.
     * > Delete record - delete the earlier added record.
     * > Select record count - select the number of records with the earlier deleted id, to check to see if the delete
     * operation was successful. */
	protected void doDMLOperations() {
		TestUtils.showMessage(this.epr + " - doDMLOperations");
		Map<String, String> params = new HashMap<String, String>();
		params.put("customerNumber", TMP_CUSTOMER_NUMBER);
		params.put("customerName", "Will<&''\"\"&>Smith");
		params.put("contactLastName", "&Silva");
		params.put("phone", "'+9400123456'");
		params.put("city", "@!~#$%^&*()-+`={}|<Colombo>");
		try {
			deleteData(params.get("customerNumber"));
			insertData(params);
			Map<String, String> insertedValues = selectCustomerData(params.get("customerNumber"));
			for (String key : params.keySet()) {
				assertTrue(params.get(key).equals(insertedValues.get(key)));
			}
			deleteData(params.get("customerNumber"));
			int count = selectDataCount(params.get("customerNumber"));
			assertTrue(count == 0);
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
		assertTrue(true);
	}
	
	/**
	 * Data Manipulation Test, with a no result nested query. 
	 * Steps:- 
	 * > Delete record with the given id - remove any previous left over data, from a failed test perhaps. 
	 * > Insert record - insert a new record. 
	 * > Update/Select record - update and select just inserted records and checks if the data has been updated properly. 
	 * > Delete record - delete the earlier added record.
	 * > Select record count - select the number of records with the earlier deleted id, to check to see if the delete
	 * operation was successful.
	 */
	protected void doDMLOperationsWithNoResultNestedQuery() {
		TestUtils.showMessage(this.epr + " - doDMLOperationsWithNoResultNestedQuery");
		Map<String, String> params1 = new HashMap<String, String>();
		Map<String, String> params2 = new HashMap<String, String>();
		Map<String, String> params3 = new HashMap<String, String>();
		
		params1.put("customerNumber", TMP_CUSTOMER_NUMBER);
		params1.put("customerName", "Will Smith");
		params1.put("contactLastName", "Silva");
		params1.put("phone", "+9400123456");
		params1.put("city", "Colombo");
		
		params2.put("customerNumber", "112");
		params2.put("customerName", "Signal Gift Stores");
		params2.put("contactLastName", "King");
		params2.put("phone", "7025551838");
		params2.put("city", "Las Vegas");
		
		params3.put("customerNumber", "114");
		params3.put("customerName", "Australian Collectors, Co.");
		params3.put("contactLastName", "Ferguson");
		params3.put("phone", "03 9520 4555");
		params3.put("city", "Melbourne");
		
		try {
			deleteData(params1.get("customerNumber"));
			insertData(params1);
			
			updateData(params2.get("customerNumber"));
			Map<String, String> updatedValues = selectCustomerData(params1.get("customerNumber"));
			for (String key : params1.keySet()) {
				if (key.equals("customerNumber")) {
					continue;
				}
				assertTrue(params2.get(key).equals(updatedValues.get(key)));
			}
			
			updateData(params3.get("customerNumber"));
			updatedValues = selectCustomerData(params1.get("customerNumber"));
			for (String key : params1.keySet()) {
				if (key.equals("customerNumber")) {
					continue;
				}
				assertTrue(params3.get(key).equals(updatedValues.get(key)));
			}
			
			deleteData(params1.get("customerNumber"));
			int count = selectDataCount(params1.get("customerNumber"));
			assertTrue(count == 0);
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
		assertTrue(true);
	}
	
	/**
	 * @see doDMLOperationsWithNoResultNestedQuery
	 * Here the insert is done with a stored procedure.
	 */
	protected void doDMLOperationsWithNoResultStoredProcNestedQuery() {
		TestUtils.showMessage(this.epr + " - doDMLOperationsWithNoResultNestedQuery");
		Map<String, String> params1 = new HashMap<String, String>();
		Map<String, String> params2 = new HashMap<String, String>();
		Map<String, String> params3 = new HashMap<String, String>();
		
		params1.put("customerNumber", TMP_CUSTOMER_NUMBER);
		params1.put("customerName", "Will Smith");
		params1.put("contactLastName", "Silva");
		params1.put("phone", "+9400123456");
		params1.put("city", "Colombo");
		
		params2.put("customerNumber", "112");
		params2.put("customerName", "Signal Gift Stores");
		params2.put("contactLastName", "King");
		params2.put("phone", "7025551838");
		params2.put("city", "Las Vegas");
		
		params3.put("customerNumber", "114");
		params3.put("customerName", "Australian Collectors, Co.");
		params3.put("contactLastName", "Ferguson");
		params3.put("phone", "03 9520 4555");
		params3.put("city", "Melbourne");
		
		try {
			deleteData(params1.get("customerNumber"));
			insertData(params1);
			
			updateDataWithStoredProc(params2.get("customerNumber"));
			Map<String, String> updatedValues = selectCustomerData(params1.get("customerNumber"));
			for (String key : params1.keySet()) {
				if (key.equals("customerNumber")) {
					continue;
				}
				assertTrue(params2.get(key).equals(updatedValues.get(key)));
			}
			
			updateDataWithStoredProc(params3.get("customerNumber"));
			updatedValues = selectCustomerData(params1.get("customerNumber"));
			for (String key : params1.keySet()) {
				if (key.equals("customerNumber")) {
					continue;
				}
				assertTrue(params3.get(key).equals(updatedValues.get(key)));
			}
			
			deleteData(params1.get("customerNumber"));
			int count = selectDataCount(params1.get("customerNumber"));
			assertTrue(count == 0);
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
		assertTrue(true);
	}
	
	/**
	 * DML Operations test with validation - 1.
	 */
	protected void doDMLOperationsVal1() {
		TestUtils.showMessage(this.epr + " - doDMLOperationsVal1");
		Map<String, String> params = new HashMap<String, String>();
		params.put("customerNumber", TMP_CUSTOMER_NUMBER);
		params.put("customerName", "Will Smith");
		params.put("contactLastName", "Silva");
		params.put("phone", "(515) 555-7212");
		params.put("city", "Colombo");
		try {
			deleteData(params.get("customerNumber"));
			insertDataVal(params);
			Map<String, String> insertedValues = selectCustomerData(params.get("customerNumber"));
			for (String key : params.keySet()) {
				assertTrue(params.get(key).equals(insertedValues.get(key)));
			}
			deleteData(params.get("customerNumber"));
			int count = selectDataCount(params.get("customerNumber"));
			assertTrue(count == 0);
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
		assertTrue(true);
	}
	
	/**
	 * DML Operations test with validation - 2.
	 */
	protected void doDMLOperationsVal2() {
		TestUtils.showMessage(this.epr + " - doDMLOperationsVal2");
		Map<String, String> params = new HashMap<String, String>();
		params.put("customerNumber", "500");
		params.put("customerName", "Will Smith");
		params.put("contactLastName", "Silva");
		params.put("phone", "(515) 555-7212");
		params.put("city", "Colombo");
		try {
			deleteData(params.get("customerNumber"));
			insertDataVal(params);
			Map<String, String> insertedValues = selectCustomerData(params.get("customerNumber"));
			for (String key : params.keySet()) {
				assertTrue(params.get(key).equals(insertedValues.get(key)));
			}
			deleteData(params.get("customerNumber"));
			int count = selectDataCount(params.get("customerNumber"));
			assertTrue(count == 0);
			assertTrue(false);
		} catch (Exception e) {
			assertTrue(true);
		}
	}
	
	/**
	 * DML Operations test with validation - 3.
	 */
	protected void doDMLOperationsVal3() {
		TestUtils.showMessage(this.epr + " - doDMLOperationsVal3");
		Map<String, String> params = new HashMap<String, String>();
		params.put("customerNumber", TMP_CUSTOMER_NUMBER);
		params.put("customerName", "Will");
		params.put("contactLastName", "Silva");
		params.put("phone", "(515) 555-7212");
		params.put("city", "Colombo");
		try {
			deleteData(params.get("customerNumber"));
			insertDataVal(params);
			Map<String, String> insertedValues = selectCustomerData(params.get("customerNumber"));
			for (String key : params.keySet()) {
				assertTrue(params.get(key).equals(insertedValues.get(key)));
			}
			deleteData(params.get("customerNumber"));
			int count = selectDataCount(params.get("customerNumber"));
			assertTrue(count == 0);
			assertTrue(false);
		} catch (Exception e) {
			assertTrue(true);
		}
	}
	
	/**
	 * DML Operations test with validation - 4.
	 */
	protected void doDMLOperationsVal4() {
		TestUtils.showMessage(this.epr + " - doDMLOperationsVal4");
		Map<String, String> params = new HashMap<String, String>();
		params.put("customerNumber", TMP_CUSTOMER_NUMBER);
		params.put("customerName", "Will Smith");
		params.put("contactLastName", "Fernandezzzzzzzzzzzzzzz");
		params.put("phone", "(515) 555-7212");
		params.put("city", "Colombo");
		try {
			deleteData(params.get("customerNumber"));
			insertDataVal(params);
			Map<String, String> insertedValues = selectCustomerData(params.get("customerNumber"));
			for (String key : params.keySet()) {
				assertTrue(params.get(key).equals(insertedValues.get(key)));
			}
			deleteData(params.get("customerNumber"));
			int count = selectDataCount(params.get("customerNumber"));
			assertTrue(count == 0);
			assertTrue(false);
		} catch (Exception e) {
			assertTrue(true);
		}
	}

	/**
	 * DML Operations test with validation - 5.
	 */
	protected void doDMLOperationsVal5() {
		TestUtils.showMessage(this.epr + " - doDMLOperationsVal5");
		Map<String, String> params = new HashMap<String, String>();
		params.put("customerNumber", TMP_CUSTOMER_NUMBER);
		params.put("customerName", "Will Smith");
		params.put("contactLastName", "Silva");
		params.put("phone", "+9411434");
		params.put("city", "Colombo");
		try {
			deleteData(params.get("customerNumber"));
			insertDataVal(params);
			Map<String, String> insertedValues = selectCustomerData(params.get("customerNumber"));
			for (String key : params.keySet()) {
				assertTrue(params.get(key).equals(insertedValues.get(key)));
			}
			deleteData(params.get("customerNumber"));
			int count = selectDataCount(params.get("customerNumber"));
			assertTrue(count == 0);
			assertTrue(false);
		} catch (Exception e) {
			assertTrue(true);
		}
	}
	
	/**
	 * DML Operations test with validation - 6.
	 */
	protected void doDMLOperationsVal6() {
		TestUtils.showMessage(this.epr + " - doDMLOperationsVal6");
		Map<String, String> params = new HashMap<String, String>();
		params.put("customerNumber", TMP_CUSTOMER_NUMBER);
		params.put("customerName", "Will Smith");
		params.put("contactLastName", "Travolta"); /* odd length string - custom validator should fail */
		params.put("phone", "(515) 555-7212");
		params.put("city", "Colombo");
		try {
			deleteData(params.get("customerNumber"));
			insertDataVal(params);
			Map<String, String> insertedValues = selectCustomerData(params.get("customerNumber"));
			for (String key : params.keySet()) {
				assertTrue(params.get(key).equals(insertedValues.get(key)));
			}
			deleteData(params.get("customerNumber"));
			int count = selectDataCount(params.get("customerNumber"));
			assertTrue(count == 0);
			assertTrue(false);
		} catch (Exception e) {
			assertTrue(true);
		}
	}


}
