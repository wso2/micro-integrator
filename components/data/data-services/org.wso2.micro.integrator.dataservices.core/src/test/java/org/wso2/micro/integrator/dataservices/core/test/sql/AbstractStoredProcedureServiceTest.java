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

import java.util.HashMap;
import java.util.Map;

/**
 * Class to represent stored procedure test cases.
 */
public abstract class AbstractStoredProcedureServiceTest extends
		DataServiceBaseTestCase {

	private String epr = null;

	public AbstractStoredProcedureServiceTest(String testName,
			String serviceName) {
		super(testName);
		this.epr = this.baseEpr + serviceName;
	}

	/**
	 * Test with a stored procedure call with no params.
	 */
	protected void storedProcNoParams() {
		TestUtils.showMessage(this.epr + " - storedProcNoParams");
		try {
            TestUtils.checkForService(this.epr);
			OMElement result = TestUtils.callOperation(this.epr,
					"stored_procedure_noparam_op", null);
			assertTrue(TestUtils.validateResultStructure(result,
					TestUtils.CUSTOMER_XSD_PATH));
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}

	/**
	 * Test with a stored procedure call with params.
	 */
	protected void storedProcWithParams() {
		TestUtils.showMessage(this.epr + " - storedProcWithParams");
		Map<String, String> params = new HashMap<String, String>();
		params.put("customerNumber", "103");
		params.put("contactLastName", "Schmitt");
		try {
            TestUtils.checkForService(this.epr);
			OMElement result = TestUtils.callOperation(this.epr,
					"stored_procedure_withparam_op", params);
			assertTrue(TestUtils.validateResultStructure(result,
					TestUtils.CUSTOMER_XSD_PATH));
			String val = TestUtils.getFirstValue(result,
					"/Customers/Customer/customerNumber",
					TestUtils.DEFAULT_DS_WS_NAMESPACE);
			assertTrue(params.get("customerNumber").equals(val));
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}

	/**
	 * Test with a nested stored procedure call - 1.
	 */
	protected void storedProcNested1() {
		TestUtils.showMessage(this.epr + " - storedProcNested1");
		try {
            TestUtils.checkForService(this.epr);
			OMElement result = TestUtils.callOperation(this.epr,
					"stored_procedure_nested_op1", null);
			assertTrue(TestUtils.validateResultStructure(result,
					TestUtils.PAYMENT_INFO_NESTED_XSD_PATH));
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}
	
	/**
	 * Test with a nested stored procedure call - 1 - For DateTime
	 */
	protected void storedProcNested1ForDateTime(){
		TestUtils.showMessage(this.epr + " - storedProcNested1ForDateTime");
		try {
            TestUtils.checkForService(this.epr);
			OMElement result = TestUtils.callOperation(this.epr,
					"stored_procedure_nested_for_date_time_op1", null);
			assertTrue(TestUtils.validateResultStructure(result,
					TestUtils.PAYMENT_INFO_NESTED_WITH_DATE_TIME_XSD_PATH));
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}
	
	/**
	 * Test with a nested stored procedure call - 2.
	 */
	protected void storedProcNested2() {
		TestUtils.showMessage(this.epr + " - storedProcNested2");
		try {
            TestUtils.checkForService(this.epr);
			OMElement result = TestUtils.callOperation(this.epr,
					"stored_procedure_nested_op2", null);
			assertTrue(TestUtils.validateResultStructure(result,
					TestUtils.PAYMENT_INFO_NESTED_XSD_PATH));
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}
	
	/**
	 * Test with a nested stored procedure call -2 - For DateTime
	 */
	protected void storedProcNested2ForDateTime(){
		TestUtils.showMessage(this.epr + " - storedProcNested2ForDateTime");
		try {
            TestUtils.checkForService(this.epr);
			OMElement result = TestUtils.callOperation(this.epr,
					"stored_procedure_nested_for_date_time_op2", null);
			assertTrue(TestUtils.validateResultStructure(result,
					TestUtils.PAYMENT_INFO_NESTED_WITH_DATE_TIME_XSD_PATH));
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}
	
	/**
	 * Test with a nested stored procedure call - 3.
	 */
	protected void storedProcNested3() {
		TestUtils.showMessage(this.epr + " - storedProcNested3");
		try {
            TestUtils.checkForService(this.epr);
			OMElement result = TestUtils.callOperation(this.epr,
					"stored_procedure_nested_op3", null);
			assertTrue(TestUtils.validateResultStructure(result,
					TestUtils.PAYMENT_INFO_NESTED_XSD_PATH));
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}
	
	/**
	 * Test with a nested stored procedure call -3 - For DateTime
	 */
	protected void storedProcNested3ForDateTime(){
		TestUtils.showMessage(this.epr + " - storedProcNested3ForDateTime");
		try {
            TestUtils.checkForService(this.epr);
			OMElement result = TestUtils.callOperation(this.epr,
					"stored_procedure_nested_for_date_time_op3", null);
			assertTrue(TestUtils.validateResultStructure(result,
					TestUtils.PAYMENT_INFO_NESTED_WITH_DATE_TIME_XSD_PATH));
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}

    /**
     * Test with a result set in config of the stored procedure
     * but does not return anything. i.e Insert query
     */
    protected void storedProcWithWrongResultSet() {
        TestUtils.showMessage(this.epr + " - storedProcWithWrongResultSet");
        Map<String, String> params = new HashMap<String, String>();
        params.put("customerNumber", "5005");
        params.put("customerName", "Will<&''\"\"&>Smith");
        params.put("contactLastName", "&Silva");
        params.put("contactFirstName", "Kelvin");
        try {
            TestUtils.checkForService(this.epr);
            TestUtils.callOperation(this.epr,
                    "stored_procedure_with_wrong_result_set", params);
            Map<String, String> params1 = new HashMap<String, String>();
            params1.put("customerNumber", "5005");
            OMElement result = TestUtils.callOperation(this.epr,
                    "select_op_count", params1);
            String val = TestUtils.getFirstValue(result,
                    "/Customers/CustomerCount/customerCount",
                    TestUtils.DEFAULT_DS_WS_NAMESPACE);
            assertTrue(Integer.parseInt(val) == 1);
        } catch (Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }
	
	/**
	 * Test with a stored function call with no params.
	 */
	protected void storedFuncNoParams() {
		TestUtils.showMessage(this.epr + " - storedFuncNoParams");
		try {
            TestUtils.checkForService(this.epr);
			OMElement result = TestUtils.callOperation(this.epr,
					"stored_func_noparam_op", null);
			String val = TestUtils.getFirstValue(result,
					"/CreditLimit/AverageCreditLimit/value",
					TestUtils.DEFAULT_DS_WS_NAMESPACE);
			assertTrue(Double.parseDouble(val) > 0.0);
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}

	/**
	 * Test with a stored function call with params.
	 */
	protected void storedFuncWithParams() {
		TestUtils.showMessage(this.epr + " - storedFuncWithParams");
		Map<String, String> params = new HashMap<String, String>();
		params.put("customerNumber", "128");
		try {
            TestUtils.checkForService(this.epr);
			OMElement result = TestUtils.callOperation(this.epr,
					"stored_func_withparam_op", params);
			String val = TestUtils.getFirstValue(result,
					"/Customer/Phone/value", TestUtils.DEFAULT_DS_WS_NAMESPACE);
			assertTrue(val.equals("+49 69 66 90 2555"));
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}

        /**
         * Test with a stored function call with params.
         */
        protected void storedProcWithFaulty() {
                TestUtils.showMessage(this.epr + " - storedProcWithFaulty");
                Map<String, String> params = new HashMap<String, String>();
                params.put("customerNumber", "103");
                params.put("contactLastName", "Scott");
                try {
                    TestUtils.checkForService(this.epr);
                        TestUtils.callUpdateOperation(this.epr,
                                "stored_procedure_withfaulty_op", params);
                        OMElement result = TestUtils.callOperation(this.epr,
                                        "stored_procedure_withparam_op", params);
                        OMElement ss = result.getFirstElement();
                        assertTrue(ss==null);

                } catch (Exception e) {
                        e.printStackTrace();
                }
        }

}
