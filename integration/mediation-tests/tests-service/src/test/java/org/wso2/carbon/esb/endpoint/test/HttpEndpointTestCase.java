/*
 *Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.esb.endpoint.test;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URL;
import javax.xml.xpath.XPathExpressionException;

import org.apache.axiom.om.OMElement;
import org.apache.axis2.AxisFault;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.exceptions.AutomationFrameworkException;
import org.wso2.carbon.automation.test.utils.http.client.HttpResponse;
import org.wso2.carbon.automation.test.utils.http.client.HttpURLConnectionClient;
import org.wso2.carbon.integration.common.utils.exceptions.AutomationUtilException;
import org.wso2.esb.integration.common.utils.ESBIntegrationTest;
import org.wso2.esb.integration.common.utils.ESBTestConstant;
import org.wso2.esb.integration.common.utils.common.ServerConfigurationManager;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

public class HttpEndpointTestCase extends ESBIntegrationTest {

    private static final String CUSTOMER_API_CONTEXT = "customerService";
    private static final String RESOURCE_CONTEXT = "/customer";
    private static final String RESOURCE_ENCODED_CONTEXT = "/customer/encoded/";
    private static final String AUTHENTICATION_RESOURCE_CONTEXT = "authentication";
    private static final String customerId = "8fa3fc1b-f63c-4b21-8aff-3ac684c74d97";
    private static final String customerName = "John";
    private static final String updateCustomerName = "Emma";
    private static final String getCustomerResponse =
            "<getCustomerResponse xmlns=\"http://ws.apache" +
                    ".org/ns/synapse\"><id>8fa3fc1b-f63c-4b21-8aff-3ac684c74d97</id><name>John</name" +
                    "></getCustomerResponse>";

    @Override
    @BeforeClass(alwaysRun = true)
    public void init() throws Exception {
        super.init();
    }

    @Test(groups = {"wso2.esb"}, description = "Test HTTP Endpoint addition, deletion & stats", priority = 1)
    public void testHttpEndpoint() throws IOException {
        checkEndpointExistence("HTTPEndpointTestEP");
    }

    @Test(groups = {"wso2.esb"}, description = "HTTP Endpoint POST Test: RESTful", priority = 5)
    public void testToPost() throws MalformedURLException, AutomationFrameworkException {

        String createCustomerData =
                "<createCustomer>\n" + "<id>" + customerId + "</id>\n" + "<name>" + customerName + "</name>\n"
                        + "</createCustomer>";
        StringReader customerData = new StringReader(createCustomerData);
        StringWriter postResponseData = new StringWriter();
        URL postRestURL = new URL((getApiInvocationURL(CUSTOMER_API_CONTEXT)) + RESOURCE_CONTEXT);
        HttpURLConnectionClient.sendPostRequest(customerData, postRestURL, postResponseData, "application/xml");
        assertTrue(postResponseData.toString().contains(customerName),
                   "response doesn't contain the expected output but contains: " + postResponseData.toString());

    }

    @Test(groups = {"wso2.esb"}, description = "HTTP Endpoint GET test: RESTful", priority = 6)
    public void testToGet() throws IOException {

        String getRestURI = getApiInvocationURL(CUSTOMER_API_CONTEXT) + RESOURCE_CONTEXT + "/" + customerId;
        HttpResponse getResponseData = HttpURLConnectionClient.sendGetRequest(getRestURI, null);
        assertTrue(getResponseData.getData().contains(getCustomerResponse),
                   "Unexpected output received:" + getResponseData.toString());

    }

    @Test(groups = {"wso2.esb"}, description = "HTTP Endpoint PUT Test: RESTful", priority = 7)
    public void testToPut() throws MalformedURLException, AutomationFrameworkException {

        String updateCustomerData =
                "<createCustomer>\n" + "<id>" + customerId + "</id>\n" + "<name>" + updateCustomerName + "</name>\n"
                        + "</createCustomer>";
        StringReader sendUpdateData = new StringReader(updateCustomerData);
        StringWriter updateResponseData = new StringWriter();
        URL updateRestURL = new URL((getApiInvocationURL(CUSTOMER_API_CONTEXT)) + RESOURCE_CONTEXT);
        HttpURLConnectionClient.sendPutRequest(sendUpdateData, updateRestURL, updateResponseData, "application/xml");
        assertTrue(updateResponseData.toString().contains(updateCustomerName),
                   "response contains unexpected output: " + updateResponseData.toString());

    }

    @Test(groups = {"wso2.esb"}, description = "HTTP Endpoint DELETE Test: RESTful", priority = 9)
    public void testToDelete() throws IOException {

        URL deleteRestURL = new URL((getApiInvocationURL(CUSTOMER_API_CONTEXT)) + RESOURCE_CONTEXT + "/" + customerId);

        HttpResponse response = HttpURLConnectionClient.sendDeleteRequest(deleteRestURL, null);
        assertEquals(response.getResponseCode(), 200, "Delete request was not successful.");
        assertTrue(response.getData().contains(
                "<deleteCustomerResponse xmlns=\"http://ws.apache.org/ns/synapse\"><return/></deleteCustomerResponse>"),
                   "No wrapped response received");
    }

    @Test(groups = {
            "wso2.esb"}, description = "Test usage of legacy-encoding property for encoded URL: RESTful", priority = 10)
    public void testLegacyEncodingProperty() throws IOException {

        String getEncodedRestURI = getApiInvocationURL(CUSTOMER_API_CONTEXT) + RESOURCE_ENCODED_CONTEXT + customerId;
        HttpResponse getResponseData = HttpURLConnectionClient.sendGetRequest(getEncodedRestURI, null);
        assertTrue(getResponseData.getData().contains(getCustomerResponse),
                   "Unexpected output received for encoded URL:" + getResponseData.toString());
    }

    @Test(groups = {"wso2.esb"}, description = "HTTP endpoint with basic auth", priority = 11)
    public void testBasicAuthHTTPEndpoint() throws IOException, XPathExpressionException, AutomationUtilException {
        ServerConfigurationManager serverConfigurationManager = new ServerConfigurationManager(context);
        serverConfigurationManager.applyMIConfigurationWithRestart(new File(
                getESBResourceLocation() + File.separator + "config" + File.separator + "deployment.toml"));
        String getAuthRestURI = getApiInvocationURL(AUTHENTICATION_RESOURCE_CONTEXT) + "/with-basic-authentication";
        HttpResponse getResponseData = HttpURLConnectionClient.sendGetRequest(getAuthRestURI, null);
        assertEquals(getResponseData.getResponseCode(), 200, "Basic authentication failed");
        serverConfigurationManager.applyMIConfigurationWithRestart(new File(
                getESBResourceLocation() + File.separator + "server" + File.separator + "conf"
                        + File.separator + "deployment.toml"));
    }

    @Test(groups = {"wso2.esb"}, description = "HTTP endpoint POST test: SOAP", priority = 2)
    public void testSendingToHttpEndpoint() throws XPathExpressionException, AxisFault {
        OMElement response = axis2Client.sendSimpleStockQuoteRequest(getProxyServiceURLHttp("HttpEndPointProxy"),
                                                                     getBackEndServiceUrl(
                                                                             ESBTestConstant.SIMPLE_STOCK_QUOTE_SERVICE),
                                                                     "WSO2");
        Assert.assertNotNull(response);
        Assert.assertTrue(response.toString().contains("WSO2 Company"),
                          "Contains unexpected output: " + response.toString());
    }

    @Test(groups = {"wso2.esb"}, description = "Sending a Message to HTTP Endpoint with invalid URI", priority = 3)
    public void testSendingToInvalidHttpEndpoint() throws XPathExpressionException {
        try {
            axis2Client.sendSimpleStockQuoteRequest(getProxyServiceURLHttp("InvalidHttpEndPointProxy"),
                                                    getBackEndServiceUrl(ESBTestConstant.SIMPLE_STOCK_QUOTE_SERVICE),
                                                    "WSO2");
            Assert.fail("Expected exception was not thrown");
        } catch (AxisFault e) {
            Assert.assertTrue(e.getMessage().contains("Error connecting to the back end"),
                              "Did not throw expected error condition for invalid endpoint.");
        }
    }

    @Test(groups = {
            "wso2.esb"}, description = "Sending a Message to an HTTP endpoint with missing uri.var variable",
          priority = 4)
    public void testSendingToNoVarHttpEndpoint() {
        try {
            axis2Client.sendSimpleStockQuoteRequest(getProxyServiceURLHttp("MissingVariableEndPointProxy"),
                                                    getBackEndServiceUrl(ESBTestConstant.SIMPLE_STOCK_QUOTE_SERVICE),
                                                    "WSO2");
        } catch (Exception e) {
            Assert.assertTrue(e instanceof AxisFault, "Did not throw expected error condition for invalid endpoint.");
        }
    }
}
