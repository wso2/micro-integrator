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
package org.wso2.micro.integrator.dataservices.core.test.generic;

import java.util.HashMap;
import java.util.Map;

import org.apache.axis2.AxisFault;
import org.wso2.micro.integrator.dataservices.core.test.DataServiceBaseTestCase;
import org.wso2.micro.integrator.dataservices.core.test.util.TestUtils;

public class GenericExceptionTest extends DataServiceBaseTestCase {

	private String epr = null;
	
	public GenericExceptionTest() {
		super("GenericExceptionTest");
		this.epr = this.baseEpr + "H2InputMappingService";
	}
	
	/**
	 * Test throwing of an exception when the EPR is invalid.
	 */
	public void testInvalidEprException() {
		TestUtils.showMessage(this.epr + " - testInvalidEprException");
		Map<String, String> params = new HashMap<String, String>();
		params.put("orderNumber", "10242");
		params.put("orderDate", "2004-04-20");
		try {
			TestUtils.callOperation(this.epr + "xxxxx",
					"select_input_mappings_op1", params);
			assertTrue(false);
		} catch (AxisFault e) {
			assertTrue(true);
		} catch (Exception e) {
			e.printStackTrace();
			assertTrue(false);
		}
	}

	/**
	 * Test throwing of an exception when parameters are expected but none are provided.
	 */
	public void testNoParamException() {
		TestUtils.showMessage(this.epr + " - testNoParamException");
		Map<String, String> params = new HashMap<String, String>();
		try {
			TestUtils.callOperation(this.epr,
					"select_input_mappings_op2", params);
			assertTrue(false);
		} catch (AxisFault e) {
			assertTrue(true);
		} catch (Exception e) {
			e.printStackTrace();
			assertTrue(false);
		}
	}
	
	/**
	 * Test throwing of an exception when the number of parameters are wrong.
	 */
    public void testInvalidParamCountException() {
    	TestUtils.showMessage(this.epr + " - testNoParamException");
		Map<String, String> params = new HashMap<String, String>();
		params.put("customerNumber", "141");
		params.put("checkNumber", "HJ32686");
		try {
			TestUtils.callOperation(this.epr,
					"select_input_mappings_op2", params);
			assertTrue(false);
		} catch (AxisFault e) {
			assertTrue(true);
		} catch (Exception e) {
			e.printStackTrace();
			assertTrue(false);
		}
	}
	
}
