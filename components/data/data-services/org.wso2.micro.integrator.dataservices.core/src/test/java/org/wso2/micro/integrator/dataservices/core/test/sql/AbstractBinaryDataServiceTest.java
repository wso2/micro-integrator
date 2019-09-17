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
import org.apache.commons.codec.binary.Base64;
import org.wso2.micro.integrator.dataservices.common.DBConstants;
import org.wso2.micro.integrator.dataservices.core.test.DataServiceBaseTestCase;
import org.wso2.micro.integrator.dataservices.core.test.sql.mysql.MySQLDMLServiceTest;
import org.wso2.micro.integrator.dataservices.core.test.util.TestUtils;

/**
 * Class to represent binary data storing/accessing functionality.
 */
public class AbstractBinaryDataServiceTest extends DataServiceBaseTestCase {

	private String epr;
	
	private static final String TMP_BINARY_DATA_ID = "50001";
	
	public AbstractBinaryDataServiceTest(String testName, String serviceName) {
		super(testName);
		this.epr = this.baseEpr + serviceName;		
	}
	
	private byte[] retrieveBinaryData(String id) throws Exception {
		Map<String, String> params = new HashMap<String, String>();
		params.put("id", id);
        TestUtils.checkForService(this.epr);
		OMElement result = TestUtils.callOperation(this.epr, "retrieve_binary_data_op",	params);
		String value = TestUtils.getFirstValue(result, "/DataList/DataEntry/data", 
				TestUtils.DEFAULT_DS_WS_NAMESPACE);
		return Base64.decodeBase64(value.getBytes(DBConstants.DEFAULT_CHAR_SET_TYPE));
	}

	private void storeBinaryData(String id, byte[] data) throws Exception {
		Map<String, String> params = new HashMap<String, String>();
		params.put("id", id);
		params.put("data", new String(Base64.encodeBase64(data), DBConstants.DEFAULT_CHAR_SET_TYPE));
		try {
            TestUtils.checkForService(this.epr);
			assertNotNull(TestUtils.callOperation(this.epr, "store_binary_data_op",
					params));
		} catch (AxisFault e) {
			// ignore for now
			if (!MySQLDMLServiceTest.INCOMING_MESSAGE_IS_NULL_ERROR.equals(e.getReason())) {
				e.printStackTrace();
				throw e;
			}
		}
	}
	
	private void deleteBinaryData(String id) throws Exception {
		Map<String, String> params = new HashMap<String, String>();
		params.put("id", id);
		try {
            TestUtils.checkForService(this.epr);
			assertNotNull(TestUtils.callOperation(this.epr, "delete_binary_data_op",
					params));
		} catch (AxisFault e) {
			// ignore for now
			if (!MySQLDMLServiceTest.INCOMING_MESSAGE_IS_NULL_ERROR.equals(e.getReason())) {
				throw e;
			}
		}
	}
	
	/**
	 * Test with a stored procedure call with OUT parameters.
	 */
	protected void binaryDataStoreRetrieve() {
		TestUtils.showMessage(this.epr + " - binaryDataStoreRetrieve");
		try {
			String data = "OIJOXIJWFIUWHOIJEGIWEOI CJWXMWOIRGOIJOIJGOEIJGE@!#$#$245145!QXQ$%^!$";
			this.deleteBinaryData(TMP_BINARY_DATA_ID);
			this.storeBinaryData(TMP_BINARY_DATA_ID, 
					data.getBytes(DBConstants.DEFAULT_CHAR_SET_TYPE));
			byte[] binRetData = this.retrieveBinaryData(TMP_BINARY_DATA_ID);
			String retDataStr = new String(binRetData, DBConstants.DEFAULT_CHAR_SET_TYPE);
			assertEquals(data, retDataStr);
			this.deleteBinaryData(TMP_BINARY_DATA_ID);
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}
	
}
