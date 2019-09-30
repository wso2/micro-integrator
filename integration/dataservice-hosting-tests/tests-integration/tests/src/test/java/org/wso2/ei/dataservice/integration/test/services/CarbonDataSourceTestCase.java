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
package org.wso2.ei.dataservice.integration.test.services;

import javax.xml.xpath.XPathExpressionException;

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
import org.testng.annotations.Test;
import org.wso2.carbon.automation.test.utils.concurrency.test.ConcurrencyTest;
import org.wso2.carbon.automation.test.utils.concurrency.test.exception.ConcurrencyTestFailedError;
import org.wso2.ei.dataservice.integration.common.utils.SampleDataServiceClient;
import org.wso2.ei.dataservice.integration.test.DSSIntegrationTest;

public class CarbonDataSourceTestCase extends DSSIntegrationTest {
    private static final Log log = LogFactory.getLog(CarbonDataSourceTestCase.class);

    private final String serviceName = "CarbonDSDataServiceTest";

    private SampleDataServiceClient client;

    @BeforeClass(alwaysRun = true)
    public void serviceDeployment() throws Exception {
        super.init();
        client = new SampleDataServiceClient(getServiceUrlHttp(serviceName));
    }

    @AfterClass(alwaysRun = true)
    public void destroy() throws Exception {
        deleteService(serviceName);
        cleanup();
    }

    @Test(groups = {"wso2.dss"})
    public void selectOperation() throws Exception {
        for (int i = 0; i < 5; i++) {
            client.getCustomerInBoston();
        }
        log.info("Select Operation Success");
    }

    @Test(groups = {"wso2.dss"})
    public void insertOperation() throws AxisFault {
        for (int i = 0; i < 5; i++) {
            client.addEmployee(String.valueOf(i));
        }
        log.info("Insert Operation Success");
    }

    @Test(groups = {"wso2.dss"}, dependsOnMethods = {"insertOperation"})
    public void selectByNumber() throws AxisFault {
        for (int i = 0; i < 5; i++) {
            OMElement result = client.getEmployeeById(String.valueOf(i));
            Assert.assertTrue(result.toString().contains("<first-name>AAA</first-name>"), "Expected Result Mismatched");
        }
        log.info("Select Operation with parameter Success");
    }

    @Test(groups = {"wso2.dss"}, dependsOnMethods = {"insertOperation"})
    public void updateOperation() throws AxisFault {
        for (int i = 0; i < 5; i++) {
            client.increaseEmployeeSalary(String.valueOf(i), "10000");

            OMElement result = client.getEmployeeById(String.valueOf(i));
            Assert.assertTrue(result.toString().contains("<salary>60000.0</salary>"),
                              "Expected Result Mismatched. update operation is not working fine");

        }
        log.info("Update Operation success");
    }

    @Test(groups = {"wso2.dss"}, dependsOnMethods = {"updateOperation"})
    public void deleteOperation() throws AxisFault {
        for (int i = 0; i < 5; i++) {
            client.deleteEmployeeById(String.valueOf(i));
            verifyDeletion(String.valueOf(i));
        }
        log.info("Delete operation success");
    }

    @Test(groups = {"wso2.dss"}, dependsOnMethods = {"selectOperation"})
    public void concurrencyTest() throws ConcurrencyTestFailedError, InterruptedException, XPathExpressionException {
        final OMFactory fac = OMAbstractFactory.getOMFactory();
        final OMNamespace omNs = fac.createOMNamespace("http://ws.wso2.org/dataservice/samples/rdbms_sample", "ns1");
        ConcurrencyTest concurrencyTest = new ConcurrencyTest(5, 5);
        OMElement payload = fac.createOMElement("customersInBoston", omNs);
        concurrencyTest.run(getServiceUrlHttp(serviceName), payload, "customersInBoston");
    }

    private void verifyDeletion(String employeeNumber) throws AxisFault {
        OMElement result = client.getEmployeeById(employeeNumber);
        Assert.assertFalse(result.toString().contains("<employee>"),
                           "Employee record found. deletion is now working fine");
    }
}
