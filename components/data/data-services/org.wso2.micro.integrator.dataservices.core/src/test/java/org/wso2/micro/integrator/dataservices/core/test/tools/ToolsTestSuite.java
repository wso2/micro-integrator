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

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class ToolsTestSuite extends TestCase {

	public static Test suite() {
		TestSuite suite = new TestSuite(
				"Test for org.wso2.carbon.dataservices.core.core.test.generic");
		//$JUnit-BEGIN$
		suite.addTestSuite(ToolsInitTest.class);
		suite.addTestSuite(ToolsTest.class);
		suite.addTestSuite(ToolsFinalizeTest.class);
		//$JUnit-END$
		return suite;
	}

	
}
