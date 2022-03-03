/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.micro.integrator.ws.policies;

import org.apache.http.HttpResponse;
import org.json.JSONArray;
import org.json.JSONObject;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.esb.integration.common.utils.ESBIntegrationTest;
import org.wso2.esb.integration.common.utils.clients.SimpleHttpClient;
import org.wso2.esb.integration.common.utils.servers.axis2.SampleAxis2Server;

import java.io.IOException;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

public class WSPoliciesTests extends ESBIntegrationTest {

    private final String USER_ID_PARAM = "userId";
    private final String ADMIN_USER_ID = "adminUser";
    private final String ADMIN_USER_PASSWORD = "adminpwd";
    private final String NON_ADMIN_USER_ID = "nonAdminUser";
    private final String NON_ADMIN_USER_PASSWORD = "pwd-nonadmin";
    private final String NON_EXISTENT_USER = "nonExistentUser";
    private final String ADMIN_PROXY_SERVICE_NAME = "StockQuoteProxy";
    private final String NON_ADMIN_PROXY_SERVICE_NAME = "StockQuoteProxyb";

    private String userResource;

    @BeforeClass()
    public void initialize() throws Exception {
        super.init();
        initLight();
        userResource = "https://" + hostName + ":" + (DEFAULT_INTERNAL_API_HTTPS_PORT + portOffset) + "/management"
                       + "/users";

        SampleAxis2Server axis2Server = new SampleAxis2Server();
        axis2Server.deployService(SampleAxis2Server.SIMPLE_STOCK_QUOTE_SERVICE);
        axis2Server.start();
    }

    @Test()
    public void testAddAdmin() throws Exception {
        String userId = ADMIN_USER_ID;
        String response = addValidUser(userId, ADMIN_USER_PASSWORD, true);
        JSONObject successResponse = new JSONObject(response);
        Assert.assertEquals(successResponse.getString(USER_ID_PARAM), userId,
                            "Invalid response received " + successResponse);
        Assert.assertEquals(successResponse.getString("status"), "Added",
                            "Invalid response received " + successResponse);
        validateUserDetails(userId, new String[]{"Internal/everyone", "admin"}, true);
    }

    @Test(dependsOnMethods = "testAddAdmin")
    public void testAddNonAdmin() throws Exception {
        String userId = NON_ADMIN_USER_ID;
        String response = addValidUser(userId, NON_ADMIN_USER_PASSWORD, false);
        JSONObject successResponse = new JSONObject(response);
        Assert.assertEquals(successResponse.getString(USER_ID_PARAM), userId,
                            "Invalid response received " + successResponse);
        Assert.assertEquals(successResponse.getString("status"), "Added",
                            "Invalid response received " + successResponse);
        validateUserDetails(userId, new String[]{"Internal/everyone"}, false);
    }

    @Test()
    public void testInvokeAdminProxyWithNonExistentUser() throws Exception {
        HttpResponse response2 = sendSimpleQuoteRequest(ADMIN_PROXY_SERVICE_NAME, NON_EXISTENT_USER, NON_EXISTENT_USER);
        Assert.assertNotNull(response2);
        Assert.assertNotNull(response2.getStatusLine());
        Assert.assertEquals(response2.getStatusLine().getStatusCode(), 401);
    }

    @Test(dependsOnMethods = "testAddAdmin")
    public void testInvokeAdminProxyWithAdminUser() throws Exception {
        HttpResponse response = sendSimpleQuoteRequest(ADMIN_PROXY_SERVICE_NAME, ADMIN_USER_ID, ADMIN_USER_PASSWORD);
        Assert.assertNotNull(response);
        Assert.assertNotNull(response.getStatusLine());
        Assert.assertEquals(response.getStatusLine().getStatusCode(), 200);
    }

    @Test(dependsOnMethods = "testAddNonAdmin")
    public void testInvokeAdminProxyWithNonAdminUser() throws Exception {
        HttpResponse response1 = sendSimpleQuoteRequest(ADMIN_PROXY_SERVICE_NAME, NON_ADMIN_USER_ID,
                NON_ADMIN_USER_PASSWORD);
        Assert.assertNotNull(response1);
        Assert.assertNotNull(response1.getStatusLine());
        Assert.assertEquals(response1.getStatusLine().getStatusCode(), 401);
    }

    @Test()
    public void testInvokeNonAdminProxyWithNonExistentUser() throws Exception {
        HttpResponse response = sendSimpleQuoteRequest(NON_ADMIN_PROXY_SERVICE_NAME, NON_EXISTENT_USER,
                NON_EXISTENT_USER);
        Assert.assertNotNull(response);
        Assert.assertNotNull(response.getStatusLine());
        Assert.assertEquals(response.getStatusLine().getStatusCode(), 401);
    }

    @Test(dependsOnMethods = "testAddNonAdmin")
    public void testInvokeNonAdminProxyWithNonAdminUser() throws Exception {
        HttpResponse response1 = sendSimpleQuoteRequest(NON_ADMIN_PROXY_SERVICE_NAME, NON_ADMIN_USER_ID,
                NON_ADMIN_USER_PASSWORD);
        Assert.assertNotNull(response1);
        Assert.assertNotNull(response1.getStatusLine());
        Assert.assertEquals(response1.getStatusLine().getStatusCode(), 200);
    }

    @Test(dependsOnMethods = "testAddAdmin")
    public void testInvokeNonAdminProxyWithAdminUser() throws Exception {
        HttpResponse response2 = sendSimpleQuoteRequest(NON_ADMIN_PROXY_SERVICE_NAME, ADMIN_USER_ID,
                ADMIN_USER_PASSWORD);
        Assert.assertNotNull(response2);
        Assert.assertNotNull(response2.getStatusLine());
        Assert.assertEquals(response2.getStatusLine().getStatusCode(), 200);
    }

    @AfterClass()
    public void deleteUsers() throws IOException {
        deleteUser(ADMIN_USER_ID);
        deleteUser(NON_ADMIN_USER_ID);
    }

    private HttpResponse sendSimpleQuoteRequest(String proxyServiceName, String username, String password)
            throws IOException {
        SimpleHttpClient simpleHttpClient = new SimpleHttpClient();
        Map<String, String> headers = new HashMap<>();
        headers.put("SOAPAction", "urn:getSimpleQuote");
        String basic = Base64.getEncoder().encodeToString((username + ":" + password).getBytes());
        headers.put("Authorization", "Basic " + basic);

        String payload =
                "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" " +
                        "xmlns:ser=\"http://services.samples\">\n" +
                        "   <soapenv:Header/>\n" +
                        "   <soapenv:Body>\n" +
                        "      <ser:getSimpleQuote>\n" +
                        "         <!--Optional:-->\n" +
                        "         <ser:symbol>WSO2</ser:symbol>\n" +
                        "      </ser:getSimpleQuote>\n" +
                        "   </soapenv:Body>\n" +
                        "</soapenv:Envelope>";
        return simpleHttpClient.doPost(getProxyServiceHttpUrl(proxyServiceName), headers, payload,
                "text/xml; charset=UTF-8");
    }

    private String getProxyServiceHttpUrl(String proxyServiceName) {
        int port = 8243 + portOffset;
        return "https://" + hostName + ":" + port + "/services/" + proxyServiceName;
    }

    private String getUser(String user) throws IOException {
        HttpResponse response = getUserPayload(user);
        Assert.assertEquals(response.getStatusLine().getStatusCode(), 200, "Unexpected status code");
        String responseString = (new SimpleHttpClient()).getResponsePayload(response);
        log.info("Received payload: " + responseString);
        return responseString;
    }

    private HttpResponse getUserPayload(String user) throws IOException {
        SimpleHttpClient client = new SimpleHttpClient();
        Map<String, String> headers = new HashMap<>();
        headers.put("Accept", "application/json");

        String endpoint = userResource + "/" + user;
        return client.doGet(endpoint, headers);
    }

    private String addValidUser(String userName, String password, boolean isAdmin) throws IOException {
        SimpleHttpClient client = new SimpleHttpClient();
        HttpResponse response = addUser(userName, password, isAdmin);
        Assert.assertEquals(response.getStatusLine().getStatusCode(), 200, "Unexpected status code");
        String responseString = client.getResponsePayload(response);
        log.info("Received payload: " + responseString);
        return responseString;
    }

    private HttpResponse addUser(String userName, String password, boolean isAdmin) throws IOException {
        SimpleHttpClient client = new SimpleHttpClient();
        Map<String, String> headers = new HashMap<>();
        headers.put("Accept", "application/json");

        String endpoint = userResource;
        String payload = "{"
                         + "\"" + USER_ID_PARAM + "\":\"" + userName + "\","
                         + "\"password\":\"" + password + "\","
                         + "\"isAdmin\":\"" + isAdmin + "\" "
                         + "}";

        return client.doPost(endpoint, headers, payload, "application/json");
    }

    private void validateUserDetails(String user, String[] expectedRoles, boolean isAdmin) throws IOException {
        String payload = getUser(user);
        String errorMessageOnAssertionFailure = "Received response" + payload;

        JSONObject payloadJson = new JSONObject(payload);

        //Assert user id
        Assert.assertEquals(user, payloadJson.get(USER_ID_PARAM), errorMessageOnAssertionFailure);
        Assert.assertEquals(isAdmin, payloadJson.get("isAdmin"), errorMessageOnAssertionFailure);

        //Assert roles
        JSONArray rolesArray = payloadJson.getJSONArray("roles");
        Assert.assertEquals(expectedRoles.length, rolesArray.length(), errorMessageOnAssertionFailure);

        String[] returnedRoleArray = new String[rolesArray.length()];
        for (int i = 0; i < rolesArray.length(); i++) {
            String role = rolesArray.getString(i);
            returnedRoleArray[i] = role;
        }

        Assert.assertTrue(Arrays.equals(expectedRoles, returnedRoleArray),
                          "There's mismatch between the expected roles " + Arrays.toString(expectedRoles) + "and "
                          + "returned roles: " + Arrays.toString(returnedRoleArray));

    }

    private void deleteUser(String userName) throws IOException {
        SimpleHttpClient client = new SimpleHttpClient();
        Map<String, String> headers = new HashMap<>();
        headers.put("Accept", "application/json");
        String endpoint = userResource + "/" + userName;
        HttpResponse response = client.doDelete(endpoint, headers);
        Assert.assertEquals(response.getStatusLine().getStatusCode(), 200);
        log.info("Deleted user: " + userName);
    }

}
