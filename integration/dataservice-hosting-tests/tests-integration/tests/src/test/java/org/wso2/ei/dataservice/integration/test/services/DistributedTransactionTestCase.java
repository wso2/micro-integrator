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

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.axis2.AxisFault;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.ei.dataservice.integration.test.DSSIntegrationTest;

import javax.xml.xpath.XPathExpressionException;

public class DistributedTransactionTestCase extends DSSIntegrationTest {
    private static final Log log = LogFactory.getLog(DistributedTransactionTestCase.class);

    private final OMFactory fac = OMAbstractFactory.getOMFactory();
    private final OMNamespace omNs = fac.createOMNamespace("http://ws.wso2.org/dataservice/samples/dtp_sample", "ns1");

    private final String serviceName = "DTPServiceTest";

    @BeforeClass(alwaysRun = true)
    public void serviceDeployment() throws Exception {
        super.init();
    }

    @Test(groups = { "wso2.dss" })
    public void addBankAccountToADatabase() throws AxisFault, XPathExpressionException {
        addBankAccount1test();
        log.info("Added bank account on database1");
    }

    @Test(groups = { "wso2.dss" })
    public void addBankAccountOnOtherDatabase() throws AxisFault, XPathExpressionException {
        addBankAccount2test();
        log.info("Added bank account on database2");
    }

    @Test(groups = { "wso2.dss" }, dependsOnMethods = { "addBankAccountToADatabase", "addBankAccountOnOtherDatabase" })
    public void distributedTransactionTest() throws AxisFault, XPathExpressionException {
        distributedTransactionSuccess();
        log.info("Added transaction on both databases");
    }

    @Test(groups = { "wso2.dss" }, dependsOnMethods = { "addBankAccountToADatabase", "addBankAccountOnOtherDatabase" })
    public void distributedTransactionFailTest() throws AxisFault, XPathExpressionException {
        distributedTransactionFail();
        log.info("Distributed transaction verified");
    }

    private OMElement addAccountToBank1(double accountBalance) {
        OMElement payload = fac.createOMElement("addAccountToBank1", omNs);

        OMElement balance = fac.createOMElement("balance", omNs);
        balance.setText(accountBalance + "");
        payload.addChild(balance);
        return payload;

    }

    private OMElement addAccountToBank2(double accountBalance) {
        OMElement payload = fac.createOMElement("addAccountToBank2", omNs);

        OMElement balance = fac.createOMElement("balance", omNs);
        balance.setText(accountBalance + "");
        payload.addChild(balance);
        return payload;

    }

    private OMElement getAccountBalanceFromBank1(int accId) {
        OMElement payload = fac.createOMElement("getAccountBalanceFromBank1", omNs);

        OMElement accountId = fac.createOMElement("accountId", omNs);
        accountId.setText(accId + "");
        payload.addChild(accountId);

        return payload;

    }

    private OMElement getAccountBalanceFromBank2(int accId) {
        OMElement payload = fac.createOMElement("getAccountBalanceFromBank2", omNs);

        OMElement accountId = fac.createOMElement("accountId", omNs);
        accountId.setText(accId + "");
        payload.addChild(accountId);

        return payload;

    }

    private OMElement addToAccountBalanceInBank1(int accId, double value) {
        OMElement payload = fac.createOMElement("addToAccountBalanceInBank1", omNs);

        OMElement accountId = fac.createOMElement("accountId", omNs);
        accountId.setText(accId + "");
        payload.addChild(accountId);

        OMElement valueEl = fac.createOMElement("value", omNs);
        valueEl.setText(value + "");
        payload.addChild(valueEl);

        return payload;

    }

    private OMElement addToAccountBalanceInBank2(int accId, double value) {
        OMElement payload = fac.createOMElement("addToAccountBalanceInBank2", omNs);

        OMElement accountId = fac.createOMElement("accountId", omNs);
        accountId.setText(accId + "");
        payload.addChild(accountId);

        OMElement valueEl = fac.createOMElement("value", omNs);
        valueEl.setText(value + "");
        payload.addChild(valueEl);

        return payload;

    }

    private OMElement begin_boxcar() {
        return fac.createOMElement("begin_boxcar", omNs);

    }

    private OMElement end_boxcar() {
        return fac.createOMElement("end_boxcar", omNs);

    }

    private int getAccountIdFromResponse(OMElement response) {
        Assert.assertNotNull(response, "Response Message is null");
        if (log.isDebugEnabled()) {
            log.debug(response);
        }

        try {
            return Integer.parseInt(((OMElement) ((OMElement) response.getChildrenWithLocalName("Entry").next())
                    .getChildrenWithLocalName("ID").next()).getText());
        } catch (Exception e) {
            Assert.fail("Id Not Found in response : " + e);
        }
        return -1;
    }

    private double getBalanceFromResponse(OMElement response) {
        Assert.assertNotNull(response, "Response Message is null");
        log.debug(response);
        try {
            return Double.parseDouble(((OMElement) response.getChildrenWithLocalName("Value").next()).getText());
        } catch (Exception e) {
            Assert.fail("Balance Not Found in response : " + e);
        }
        return -1;
    }

    private void addBankAccount1test() throws AxisFault, XPathExpressionException {
        ServiceClient sender;
        Options options;
        OMElement response;

        sender = new ServiceClient();
        options = new Options();
        options.setTo(new EndpointReference(getServiceUrlHttp(serviceName)));
        options.setProperty(org.apache.axis2.transport.http.HTTPConstants.CHUNKED, Boolean.FALSE);
        options.setManageSession(true);
        sender.setOptions(options);

        options.setAction("urn:" + "addAccountToBank1");
        sender.setOptions(options);
        response = sender.sendReceive(addAccountToBank1(100));
        log.debug(response);
        int id1 = getAccountIdFromResponse(response);

        options.setAction("urn:" + "getAccountBalanceFromBank1");
        Assert.assertEquals(100.0, getBalanceFromResponse(sender.sendReceive(getAccountBalanceFromBank1(id1))),
                "invalid balance ");

    }

    private void addBankAccount2test() throws AxisFault, XPathExpressionException {
        ServiceClient sender;
        Options options;
        OMElement response;

        sender = new ServiceClient();
        options = new Options();
        options.setTo(new EndpointReference(getServiceUrlHttp(serviceName)));
        options.setProperty(org.apache.axis2.transport.http.HTTPConstants.CHUNKED, Boolean.FALSE);
        options.setManageSession(true);
        sender.setOptions(options);

        options.setAction("urn:" + "addAccountToBank2");
        sender.setOptions(options);
        response = sender.sendReceive(addAccountToBank2(200));
        log.debug(response);

        int id1 = getAccountIdFromResponse(response);

        options.setAction("urn:" + "getAccountBalanceFromBank2");
        Assert.assertEquals(200.0, getBalanceFromResponse(sender.sendReceive(getAccountBalanceFromBank2(id1))),
                "invalid balance ");

    }

    private void distributedTransactionSuccess() throws AxisFault, XPathExpressionException {
        ServiceClient sender;
        Options options;

        sender = new ServiceClient();
        options = new Options();
        options.setTo(new EndpointReference(getServiceUrlHttp(serviceName)));
        options.setProperty(org.apache.axis2.transport.http.HTTPConstants.CHUNKED, Boolean.FALSE);
        options.setManageSession(true);
        sender.setOptions(options);

        options.setAction("urn:" + "addAccountToBank1");
        sender.setOptions(options);
        int id1 = getAccountIdFromResponse(sender.sendReceive(addAccountToBank1(100)));

        options.setAction("urn:" + "addAccountToBank2");
        sender.setOptions(options);
        int id2 = getAccountIdFromResponse(sender.sendReceive(addAccountToBank2(200)));

        options.setAction("urn:" + "begin_boxcar");
        sender.setOptions(options);
        sender.sendRobust(begin_boxcar());

        options.setAction("urn:" + "addToAccountBalanceInBank1");
        sender.setOptions(options);
        sender.sendRobust(addToAccountBalanceInBank1(id1, 50));

        options.setAction("urn:" + "addToAccountBalanceInBank2");
        sender.setOptions(options);
        sender.sendRobust(addToAccountBalanceInBank2(id2, -25));

        options.setAction("urn:" + "end_boxcar");
        sender.sendRobust(end_boxcar());

        options.setAction("urn:" + "getAccountBalanceFromBank1");
        sender.setOptions(options);
        Assert.assertEquals(getBalanceFromResponse(sender.sendReceive(getAccountBalanceFromBank1(id1))), 150.0,
                "Expected not same");

        options.setAction("urn:" + "getAccountBalanceFromBank2");
        sender.setOptions(options);
        Assert.assertEquals(getBalanceFromResponse(sender.sendReceive(getAccountBalanceFromBank2(id2))), 175.0,
                "Expected not same");

    }

    private void distributedTransactionFail() throws AxisFault, XPathExpressionException {
        ServiceClient sender;
        Options options;

        sender = new ServiceClient();
        options = new Options();
        options.setTo(new EndpointReference(getServiceUrlHttp(serviceName)));
        options.setProperty(org.apache.axis2.transport.http.HTTPConstants.CHUNKED, Boolean.FALSE);
        options.setManageSession(true);
        sender.setOptions(options);

        options.setAction("urn:" + "addAccountToBank1");
        sender.setOptions(options);
        int id1 = getAccountIdFromResponse(sender.sendReceive(addAccountToBank1(11500)));

        options.setAction("urn:" + "addAccountToBank2");
        sender.setOptions(options);
        int id2 = getAccountIdFromResponse(sender.sendReceive(addAccountToBank2(2000)));
        int id3 = getAccountIdFromResponse(sender.sendReceive(addAccountToBank2(1500)));
        int id4 = getAccountIdFromResponse(sender.sendReceive(addAccountToBank2(3000)));

        options.setAction("urn:" + "begin_boxcar");
        sender.setOptions(options);
        sender.sendRobust(begin_boxcar());

        options.setAction("urn:" + "addToAccountBalanceInBank2");
        sender.setOptions(options);
        sender.sendRobust(addToAccountBalanceInBank2(id2, 1500));
        sender.sendRobust(addToAccountBalanceInBank2(id3, 350));
        sender.sendRobust(addToAccountBalanceInBank2(id4, 700));

        // this should fail, validation error, value < -2000
        options.setAction("urn:" + "addToAccountBalanceInBank1");
        sender.setOptions(options);
        sender.sendRobust(addToAccountBalanceInBank1(id1, -2700));
        try {
            options.setAction("urn:" + "end_boxcar");
            sender.sendRobust(end_boxcar());
            Assert.fail("Service validation failed. end_boxcar Not Working");
        } catch (AxisFault e) {
            log.info("Operation failed");

        }

        options.setAction("urn:" + "getAccountBalanceFromBank1");
        sender.setOptions(options);
        Assert.assertEquals(getBalanceFromResponse(sender.sendReceive(getAccountBalanceFromBank1(id1))), 11500.0,
                "Expected not same");

        options.setAction("urn:" + "getAccountBalanceFromBank2");
        sender.setOptions(options);
        Assert.assertEquals(getBalanceFromResponse(sender.sendReceive(getAccountBalanceFromBank2(id2))), 2000.0,
                "Expected not same");
        Assert.assertEquals(getBalanceFromResponse(sender.sendReceive(getAccountBalanceFromBank2(id3))), 1500.0,
                "Expected not same");
        Assert.assertEquals(getBalanceFromResponse(sender.sendReceive(getAccountBalanceFromBank2(id4))), 3000.0,
                "Expected not same");

    }

}
