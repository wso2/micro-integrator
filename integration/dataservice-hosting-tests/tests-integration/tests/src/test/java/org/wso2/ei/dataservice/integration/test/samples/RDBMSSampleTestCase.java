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
package org.wso2.ei.dataservice.integration.test.samples;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.axis2.AxisFault;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.automation.test.utils.axis2client.AxisServiceClient;
import org.wso2.ei.dataservice.integration.test.DSSIntegrationTest;

import javax.xml.namespace.QName;
import java.rmi.RemoteException;
import java.util.Iterator;
import java.util.Random;

public class RDBMSSampleTestCase extends DSSIntegrationTest {
    private static final Log log = LogFactory.getLog(RDBMSSampleTestCase.class);

    private final String serviceName = "RDBMSSample";
    private int randomNumber;
    private String serverEpr;
    private final OMFactory fac = OMAbstractFactory.getOMFactory();
    private final OMNamespace omNs = fac.createOMNamespace("http://ws.wso2.org/dataservice", "ns1");

    @Factory(dataProvider = "userModeDataProvider")
    public RDBMSSampleTestCase(TestUserMode userMode) {
        this.userMode = userMode;
    }

    @BeforeClass(alwaysRun = true)
    public void serviceDeployment() throws Exception {
        super.init();
        serverEpr = getServiceUrlHttp(serviceName);
        randomNumber = new Random().nextInt(2000) + 2000; //added 2000 because table already have ids up nearly to 2000
    }

    @Test(groups = { "wso2.dss" })
    public void selectOperation() throws RemoteException {
        OMElement payload = fac.createOMElement("customersInBoston", omNs);
        OMElement result = new AxisServiceClient().sendReceive(payload, serverEpr, "customersInBoston");
        Iterator customersEle = result.getChildElements();
        int count = 0;
        while (customersEle.hasNext() && count < 5) {
            OMElement customer = (OMElement) customersEle.next();
            String value = customer.getFirstChildWithName(new QName(
                    "http://ws.wso2.org/dataservice/samples/rdbms_sample", "city")).getText();
            count++;
            Assert.assertEquals(value, "Boston", "City mismatched");
        }
        log.info("Select Operation Success");
    }

    @Test(groups = { "wso2.dss" }, dependsOnMethods = "selectOperation")
    public void insertOperation() throws RemoteException {
        for (int i = 0; i < 5; i++) {
            int empNumber = randomNumber + i;
            OMElement addEmpEle = createAddEmployeePayload(empNumber, "LastName", "FirstName",
                    "testmail" + empNumber + "@test.com", 5000.00);
            new AxisServiceClient().sendRobust(addEmpEle, serverEpr, "addEmployee");
            OMElement getEmpEle = createGetEmployeeByNumberPayload(empNumber);
            OMElement result = new AxisServiceClient().sendReceive(getEmpEle, serverEpr, "employeesByNumber");
            Assert.assertTrue(result.toString().contains("<email>testmail" + empNumber + "@test.com</email>"));
        }
        log.info("Insert Operation Success");
    }

    @Test(groups = { "wso2.dss" }, dependsOnMethods = "selectOperation")
    public void testLengthValidator() {
        try {
            OMElement addEmpEle = createAddEmployeePayload(1, "FN", "LN",
                    "testmail@test.com", 50000.00);
            new AxisServiceClient().sendRobust(addEmpEle, serverEpr, "addEmployee");
        } catch (AxisFault e) {
            Assert.assertTrue(e.getMessage().contains("VALIDATION_ERROR"), "The error is not a validation error");
            Assert.assertTrue(e.getMessage().contains("addEmployee"), "The error does not have addEmployee");
        }
    }

    @Test(groups = { "wso2.dss" }, dependsOnMethods = "selectOperation")
    public void testPatternValidator() throws RemoteException {
        try {
            OMElement addEmpEle = createAddEmployeePayload(1, "FirstName", "LastName",
                    "wrong_email_pattern", 50000.00);
            new AxisServiceClient().sendRobust(addEmpEle, serverEpr, "addEmployee");
        } catch (AxisFault e) {
            Assert.assertTrue(e.getMessage().contains("VALIDATION_ERROR"), "The error is not a validation error");
            Assert.assertTrue(e.getMessage().contains("addEmployee"), "The error does not have addEmployee");
        }
    }

    @Test(groups = { "wso2.dss" }, dependsOnMethods = { "insertOperation" })
    public void selectByNumber() throws RemoteException {
        for (int i = 0; i < 5; i++) {
            OMElement getEmpEle = createGetEmployeeByNumberPayload(randomNumber + i);
            OMElement result = new AxisServiceClient().sendReceive(getEmpEle, serverEpr, "employeesByNumber");
            Assert.assertNotNull(result, "Employee not found");
            Assert.assertEquals(countChildren(result), 1, "Employee count mismatched for given emp number");
        }
        log.info("Select operation with parameter success");
    }

    @Test(groups = { "wso2.dss" }, dependsOnMethods = { "selectByNumber" })
    public void updateOperation() throws RemoteException {
        for (int i = 0; i < 5; i++) {
            OMElement incrementSalaryEle = createIncrementSalaryPayload(randomNumber + i, 20000.00);
            new AxisServiceClient().sendRobust(incrementSalaryEle, serverEpr, "incrementEmployeeSalary");
            OMElement getEmpEle = createGetEmployeeByNumberPayload(randomNumber + i);
            OMElement result = new AxisServiceClient().sendReceive(getEmpEle, serverEpr, "employeesByNumber");
            Assert.assertNotNull(result, "Employee not found");
            Assert.assertEquals(countChildren(result), 1, "Employee count mismatched for given emp number");
            Assert.assertTrue(result.toString().contains("25000"), "Salary Increment not set");
        }
        log.info("Update Operation success");
    }

    @Test(groups = { "wso2.dss" }, dependsOnMethods = { "updateOperation" }, enabled = false)
    public void deleteOperation() throws RemoteException {
        for (int i = 0; i < 5; i++) {
            OMElement beginBoxcarEle = fac.createOMElement("begin_boxcar", omNs);
            new AxisServiceClient().sendRobust(beginBoxcarEle, serverEpr, "begin_boxcar");
            OMElement thousandFiveEle = fac.createOMElement("thousandFive", omNs);
            new AxisServiceClient().sendRobust(thousandFiveEle, serverEpr, "thousandFive");
            OMElement incrementSalaryEle = createIncrementSalaryExPayload(randomNumber + i);
            new AxisServiceClient().sendRobust(incrementSalaryEle, serverEpr, "incrementEmployeeSalaryEx");
            OMElement endBoxcarEle = fac.createOMElement("end_boxcar", omNs);
            new AxisServiceClient().sendRobust(endBoxcarEle, serverEpr, "end_boxcar");

            OMElement getEmpEle = createGetEmployeeByNumberPayload(randomNumber + i);
            OMElement result = new AxisServiceClient().sendReceive(getEmpEle, serverEpr, "employeesByNumber");
            Assert.assertNotNull(result, "Employee not found");
            Assert.assertEquals(countChildren(result), 1, "Employee count mismatched for given emp number");
            Assert.assertTrue(result.toString().contains("71500.00"), "Salary Increment not setby boxcaring");
        }
        log.info("Delete operation success");
    }

    @AfterClass(alwaysRun = true)
    public void destroy() throws Exception {
        deleteService(serviceName);
        cleanup();
    }


    private OMElement createAddEmployeePayload(int empNo, String lastName, String firstName, String email, double salary) {
        OMElement addEmployeeSQL = fac.createOMElement("addEmployee", omNs);
        OMElement empNumber = fac.createOMElement("employeeNumber", omNs);
        OMElement empLastName = fac.createOMElement("lastName", omNs);
        OMElement empFirstName = fac.createOMElement("firstName", omNs);
        OMElement empEmail = fac.createOMElement("email", omNs);
        OMElement empSalary = fac.createOMElement("salary", omNs);

        empNumber.setText("" + empNo);
        empLastName.setText(lastName);
        empFirstName.setText(firstName);
        empEmail.setText(email);
        empSalary.setText("" + salary);
        addEmployeeSQL.addChild(empNumber);
        addEmployeeSQL.addChild(empLastName);
        addEmployeeSQL.addChild(empFirstName);
        addEmployeeSQL.addChild(empEmail);
        addEmployeeSQL.addChild(empSalary);

        return addEmployeeSQL;
    }

    private OMElement createGetEmployeeByNumberPayload(int empNo) {
        OMElement getEmployeeSQL = fac.createOMElement("employeesByNumber", omNs);
        OMElement empNumberEle = fac.createOMElement("employeeNumber", omNs);
        empNumberEle.setText("" + empNo);
        getEmployeeSQL.addChild(empNumberEle);
        return getEmployeeSQL;
    }

    private OMElement createIncrementSalaryPayload(int empNo, double salary) {
        OMElement incrementSalarySQL = fac.createOMElement("incrementEmployeeSalary", omNs);
        OMElement empNumber = fac.createOMElement("employeeNumber", omNs);
        OMElement empSalary = fac.createOMElement("increment", omNs);
        empNumber.setText("" + empNo);
        empSalary.setText("" + salary);
        incrementSalarySQL.addChild(empNumber);
        incrementSalarySQL.addChild(empSalary);
        return incrementSalarySQL;
    }

    private OMElement createIncrementSalaryExPayload(int empNo) {
        OMElement incrementSalarySQL = fac.createOMElement("incrementEmployeeSalaryEx", omNs);
        OMElement empNumber = fac.createOMElement("employeeNumber", omNs);
        empNumber.setText("" + empNo);
        incrementSalarySQL.addChild(empNumber);
        return incrementSalarySQL;
    }

    private int countChildren(OMElement element) {
        int count = 0;
        for (Iterator<?> it = element.getChildElements(); it.hasNext(); count++) {
            it.next();
        }
        return count;
    }
}
