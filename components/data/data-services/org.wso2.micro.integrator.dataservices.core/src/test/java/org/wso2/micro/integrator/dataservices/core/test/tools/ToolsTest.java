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
package org.wso2.micro.integrator.dataservices.core.test.tools;

import java.io.File;
import java.io.FileInputStream;
import java.util.HashMap;
import java.util.Map;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.wso2.micro.integrator.dataservices.core.engine.DataService;
import org.wso2.micro.integrator.dataservices.core.engine.ParamValue;
import org.wso2.micro.integrator.dataservices.core.test.DataServiceBaseTestCase;
import org.wso2.micro.integrator.dataservices.core.test.util.TestUtils;
import org.wso2.micro.integrator.dataservices.core.tools.DSTools;

public class ToolsTest extends DataServiceBaseTestCase {

	public ToolsTest() {
		super("ToolsTest");
	}
	
	private OMElement loadXML(String filePath) throws Exception {
		FileInputStream in = new FileInputStream(filePath);
		OMElement dsElement = (new StAXOMBuilder(in)).getDocumentElement();
		dsElement.build();
		return dsElement;
	}
	
	private String getTestDBSDirectory() {
		return "." + File.separator + "src" + File.separator + "test"
				+ File.separator + "resources" + File.separator + "test-dbs"
				+ File.separator;
	}
	
	/**
	 * Tests local, RDBMS data source based, operation invocation without parameters.
	 */
	public void testLocalOperationInvocation1() {
		TestUtils.showMessage("testLocalOperationInvocation1");
		try {
			String filePath = this.getTestDBSDirectory() + "H2BasicService.dbs";
			DataService dataService = DSTools.createDataService(this.loadXML(filePath), filePath);
			OMElement result = DSTools.invokeOperation(dataService, "select_op_given_fields",
							new HashMap<String, ParamValue>());
			assertTrue(TestUtils.validateResultStructure(result, TestUtils.CUSTOMER_XSD_PATH));
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}
	
	/**
	 * Tests local, RDBMS data source based, operation invocation with parameters.
	 */
	public void testLocalOperationInvocation2() {
		TestUtils.showMessage("testLocalOperationInvocation2");
		try {
			String filePath = this.getTestDBSDirectory() + "H2InputMappingService.dbs";
			DataService dataService = DSTools.createDataService(this.loadXML(filePath), filePath);
			Map<String, ParamValue> params = new HashMap<String, ParamValue>();
			params.put("orderNumber", new ParamValue("10242"));
			params.put("orderDate", new ParamValue("2004-04-20"));
			OMElement result = DSTools.invokeOperation(dataService, "select_input_mappings_op1", params);
			assertTrue(TestUtils.validateResultStructure(result,
					TestUtils.ORDERS_XSD_PATH));
			String val = TestUtils.getFirstValue(result,
					"/Orders/Order/orderNumber",
					TestUtils.DEFAULT_DS_WS_NAMESPACE);
			assertTrue(params.get("orderNumber").getScalarValue().equals(val));
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}
	
	/**
	 * Tests local, CSV data source based, operation invocation.
	 */
	public void testLocalOperationInvocation3() {
		TestUtils.showMessage("testLocalOperationInvocation3");
		try {
			String filePath = this.getTestDBSDirectory() + "CSVService.dbs";
			DataService dataService = DSTools.createDataService(this.loadXML(filePath), filePath);
			OMElement result = DSTools.invokeOperation(dataService, "customer_list_op", 
					new HashMap<String, ParamValue>());
			assertTrue(TestUtils.validateResultStructure(result,
					TestUtils.CUSTOMER_XSD_PATH));
			String val = TestUtils.getFirstValue(result,
					"/Customers/Customer/customerNumber",
					TestUtils.DEFAULT_DS_WS_NAMESPACE);
			assertTrue(Integer.parseInt(val) == 103);
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}
	
	/**
	 * Tests local, Excel data source based, operation invocation.
	 */
	public void testLocalOperationInvocation4() {
		TestUtils.showMessage("testLocalOperationInvocation4");
		try {
			String filePath = this.getTestDBSDirectory() + "ExcelService.dbs";
			DataService dataService = DSTools.createDataService(this.loadXML(filePath), filePath);
			OMElement result = DSTools.invokeOperation(dataService, "excel_old_noheader_with_ints_op", 
					new HashMap<String, ParamValue>());
			assertTrue(TestUtils.validateResultStructure(result,
					TestUtils.CUSTOMER_XSD_PATH));
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}
	
	/**
	 * Tests local, RDBMS data source based, resource access without parameters.
	 */
	public void testLocalResourceAccess1() {
		TestUtils.showMessage("testLocalResourceAccess1");
		try {
			String filePath = this.getTestDBSDirectory() + "H2BasicService.dbs";
			DataService dataService = DSTools.createDataService(this.loadXML(filePath), filePath);
			OMElement result = DSTools.accessResource(dataService, "customers1",
							new HashMap<String, ParamValue>(), "GET");
			assertTrue(TestUtils.validateResultStructure(result, TestUtils.CUSTOMER_XSD_PATH));
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}
	
}
