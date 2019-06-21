/*
 *Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *WSO2 Inc. licenses this file to you under the Apache License,
 *Version 2.0 (the "License"); you may not use this file except
 *in compliance with the License.
 *You may obtain a copy of the License at
 *
 *http://www.apache.org/licenses/LICENSE-2.0
 *
 *Unless required by applicable law or agreed to in writing,
 *software distributed under the License is distributed on an
 *"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *KIND, either express or implied.  See the License for the
 *specific language governing permissions and limitations
 *under the License.
 */

package org.wso2.ei.dataservice.integration.test.syntax;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.axis2.AxisFault;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.test.utils.axis2client.AxisServiceClient;
import org.wso2.ei.dataservice.integration.test.DSSIntegrationTest;

//JIRA  https://wso2.org/jira/browse/CARBON-11321
public class WhiteSpaceWithQueryParamsTest extends DSSIntegrationTest {

    private static final Log log = LogFactory.getLog(WhiteSpaceWithQueryParamsTest.class);

    private final OMFactory fac = OMAbstractFactory.getOMFactory();
    private final OMNamespace omNs = fac
            .createOMNamespace("http://ws.wso2.org/dataservice/samples/rdbms_sample", "ns1");
    private static String serviceEndPoint;

    @BeforeClass(alwaysRun = true)
    public void initialize() throws Exception {
        super.init();
        String serviceName = "WhiteSpacesInQueryParamsTest";
        serviceEndPoint = getServiceUrlHttp(serviceName);
    }

    @Test(groups = "wso2.dss", description = "add employee records")
    public void testInsertOperation() throws AxisFault {
        for (int i = 0; i < 5; i++) {
            addEmployee(serviceEndPoint, String.valueOf(i));
        }
        log.info("Insert Operation Success");
    }

    @Test(groups = "wso2.dss", dependsOnMethods = { "testInsertOperation" }, description = "get employee by Id")
    public void testSelectByNumber() throws AxisFault {
        for (int i = 0; i < 5; i++) {
            getEmployeeById(String.valueOf(i));
        }
        log.info("Select operation with parameter success");
    }

    @Test(groups = "wso2.dss", dependsOnMethods = { "testSelectByNumber" }, description = "delete employees by id")
    public void deleteOperation() throws AxisFault {
        for (int i = 0; i < 5; i++) {
            deleteEmployeeById(String.valueOf(i));
            verifyDeletion(String.valueOf(i));
        }
        log.info("Delete operation success");
    }

    private void addEmployee(String serviceEndPoint, String employeeNumber) throws AxisFault {
        OMElement payload = fac.createOMElement("addEmployee", omNs);

        OMElement empNo = fac.createOMElement("employeeNumber", omNs);
        empNo.setText(employeeNumber);
        payload.addChild(empNo);

        OMElement lastName = fac.createOMElement("lastName", omNs);
        lastName.setText("BBB");
        payload.addChild(lastName);

        OMElement fName = fac.createOMElement("firstName", omNs);
        fName.setText("AAA");
        payload.addChild(fName);

        OMElement email = fac.createOMElement("email", omNs);
        email.setText("aaa@ccc.com");
        payload.addChild(email);

        OMElement salary = fac.createOMElement("salary", omNs);
        salary.setText("50000");
        payload.addChild(salary);

        new AxisServiceClient().sendRobust(payload, serviceEndPoint, "addEmployee");

    }

    private void getEmployeeById(String employeeNumber) throws AxisFault {
        OMElement payload = fac.createOMElement("employeesByNumber", omNs);

        OMElement empNo = fac.createOMElement("employeeNumber", omNs);
        empNo.setText(employeeNumber);
        payload.addChild(empNo);

        OMElement result = new AxisServiceClient().sendReceive(payload, serviceEndPoint, "employeesByNumber");
        Assert.assertTrue(result.toString().contains("<first-name>AAA</first-name>"), "Expected Result Mismatched");
    }

    private void deleteEmployeeById(String employeeNumber) throws AxisFault {
        OMElement payload = fac.createOMElement("deleteEmployeeById", omNs);

        OMElement empNo = fac.createOMElement("employeeNumber", omNs);
        empNo.setText(employeeNumber);
        payload.addChild(empNo);

        new AxisServiceClient().sendRobust(payload, serviceEndPoint, "deleteEmployeeById");

    }

    private void verifyDeletion(String employeeNumber) throws AxisFault {
        OMElement payload = fac.createOMElement("employeesByNumber", omNs);

        OMElement empNo = fac.createOMElement("employeeNumber", omNs);
        empNo.setText(employeeNumber);
        payload.addChild(empNo);

        OMElement result = new AxisServiceClient().sendReceive(payload, serviceEndPoint, "employeesByNumber");
        Assert.assertFalse(result.toString().contains("<employee>"),
                "Employee record found. deletion is now working fine");
    }
}
