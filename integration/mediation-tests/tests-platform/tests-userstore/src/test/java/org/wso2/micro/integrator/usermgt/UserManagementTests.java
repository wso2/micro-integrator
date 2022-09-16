/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.micro.integrator.usermgt;

import org.apache.http.HttpResponse;
import org.json.JSONArray;
import org.json.JSONObject;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.esb.integration.common.utils.ESBIntegrationTest;
import org.wso2.esb.integration.common.utils.clients.SimpleHttpClient;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class UserManagementTests extends ESBIntegrationTest {

    private String userResource;
    private final String USER_ID_PARAM = "userId";
    private final String ADMIN_USER_ID = "adminUser";
    private final String NON_ADMIN_USER1_ID = "nonAdminUser";
    private final String NON_ADMIN_USER2_ID = "developerUser";
    private final String NON_ADMIN_USER3_ID = "observerUser";


    @BeforeClass
    public void initialize() {
        initLight();
        userResource = "https://" + hostName + ":" + (DEFAULT_INTERNAL_API_HTTPS_PORT + portOffset) + "/management"
                + "/users";
    }

    @Test
    public void testGetInitialUsers() throws Exception {

        String users = getUsers();
        JSONObject usersJson = new JSONObject(users);

        String errorMessageOnAssertionFailure = "Received response" + users;
        //Assert user count
        Assert.assertEquals(usersJson.get("count"), 1, errorMessageOnAssertionFailure);

        //Assert user details
        Assert.assertEquals((usersJson.getJSONArray("list")).length(), 1, errorMessageOnAssertionFailure);
        Assert.assertEquals((usersJson.getJSONArray("list")).getJSONObject(0).get(USER_ID_PARAM), "admin",
                errorMessageOnAssertionFailure);
    }

    @Test(dependsOnMethods = "testGetInitialUsers")
    public void testAddAdmin() throws Exception {
        String userId = ADMIN_USER_ID;
        String response = addValidUser(userId, "adminpwd", true);
        JSONObject successResponse = new JSONObject(response);
        Assert.assertEquals(successResponse.getString(USER_ID_PARAM), userId,
                "Invalid response received " + successResponse);
        Assert.assertEquals(successResponse.getString("status"), "Added",
                "Invalid response received " + successResponse);
        validateUserDetails(userId, new String[]{"Internal/everyone", "admin"}, true);
    }

    @Test(dependsOnMethods = "testAddAdmin")
    public void testDeleteUser() throws Exception {
        String userId = ADMIN_USER_ID;
        String response = deleteUserSuccessfully(userId);
        JSONObject successResponse = new JSONObject(response);
        Assert.assertEquals(successResponse.getString(USER_ID_PARAM), userId,
                "Invalid response received " + successResponse);
        Assert.assertEquals(successResponse.getString("status"), "Deleted",
                "Invalid response received " + successResponse);
        assertNonExistenceOfUser(userId);
    }

    @Test(dependsOnMethods = "testGetInitialUsers")
    public void testAddNonAdmin() throws Exception {
        String userId = NON_ADMIN_USER1_ID;
        String response = addValidUser(userId, "pwd-nonadmin", false);
        JSONObject successResponse = new JSONObject(response);
        Assert.assertEquals(successResponse.getString(USER_ID_PARAM), userId,
                "Invalid response received " + successResponse);
        Assert.assertEquals(successResponse.getString("status"), "Added",
                "Invalid response received " + successResponse);
        validateUserDetails(userId, new String[]{"Internal/everyone"}, false);
    }

    @Test(dependsOnMethods = "testAddNonAdmin")
    public void testAddExistingUser() throws Exception {
        HttpResponse response = addUser(NON_ADMIN_USER1_ID, "pwd-nonadmin2", false);
        Assert.assertEquals(response.getStatusLine().getStatusCode(), 400);
    }

    @Test(dependsOnMethods = "testAddExistingUser")
    public void testDeleteNonExistentUser() throws Exception {
        String userId = "nonExistentUser";
        HttpResponse response = deleteUser(userId);
        Assert.assertEquals(response.getStatusLine().getStatusCode(), 404);
        // Delete extra users than admin
        deleteUser(NON_ADMIN_USER1_ID);
    }
    @Test(dependsOnMethods = "testDeleteNonExistentUser")
    public void testAddNonAdminUser2() throws Exception {
        String userId = NON_ADMIN_USER2_ID;
        String response = addValidUser(userId, "pwd-nonadmin", false);
        JSONObject successResponse = new JSONObject(response);
        Assert.assertEquals(successResponse.getString(USER_ID_PARAM), userId,
                "Invalid response received " + successResponse);
        Assert.assertEquals(successResponse.getString("status"), "Added",
                "Invalid response received " + successResponse);
        validateUserDetails(userId, new String[]{"Internal/everyone"}, false);
    }

    @Test(dependsOnMethods = "testAddNonAdminUser2")
    public void testAddNonAdminUser3() throws Exception {
        String userId = NON_ADMIN_USER3_ID;
        String response = addValidUser(userId, "pwd-nonadmin", false);
        JSONObject successResponse = new JSONObject(response);
        Assert.assertEquals(successResponse.getString(USER_ID_PARAM), userId,
                "Invalid response received " + successResponse);
        Assert.assertEquals(successResponse.getString("status"), "Added",
                "Invalid response received " + successResponse);
        validateUserDetails(userId, new String[]{"Internal/everyone"}, false);
    }

    @Test(dependsOnMethods = "testAddNonAdminUser3")
    public void testGetAllCurrentUsers() throws Exception {
        String users = getUsers();
        JSONObject usersJson = new JSONObject(users);
        String errorMessageOnAssertionFailure = "Received response" + users;
        //Assert user count
        Assert.assertEquals(usersJson.get("count"), 3, errorMessageOnAssertionFailure);
        //Assert user details
        Assert.assertEquals((usersJson.getJSONArray("list")).length(), 3, errorMessageOnAssertionFailure);
        Assert.assertEquals((usersJson.getJSONArray("list")).getJSONObject(0).get(USER_ID_PARAM), "admin",
                errorMessageOnAssertionFailure);
        Assert.assertEquals((usersJson.getJSONArray("list")).getJSONObject(1).get(USER_ID_PARAM), "developerUser",
                errorMessageOnAssertionFailure);
        Assert.assertEquals((usersJson.getJSONArray("list")).getJSONObject(2).get(USER_ID_PARAM), "observerUser",
                errorMessageOnAssertionFailure);
    }

    @Test(dependsOnMethods = "testGetAllCurrentUsers")
    public void testSearchCurrentUsers() throws Exception {
        String users = getSearchedUsers("developer");
        JSONObject usersJson = new JSONObject(users);
        String errorMessageOnAssertionFailure = "Received response" + users;
        //Assert user count
        Assert.assertEquals(usersJson.get("count"), 1, errorMessageOnAssertionFailure);
        //Assert user details
        Assert.assertEquals((usersJson.getJSONArray("list")).length(), 1, errorMessageOnAssertionFailure);
        Assert.assertEquals((usersJson.getJSONArray("list")).getJSONObject(0).get(USER_ID_PARAM), "developerUser",
                errorMessageOnAssertionFailure);
    }

    @Test(dependsOnMethods = "testSearchCurrentUsers")
    public void testDeleteNonAdminUser2() throws Exception {
        String userId = NON_ADMIN_USER2_ID;
        String response = deleteUserSuccessfully(userId);
        JSONObject successResponse = new JSONObject(response);
        Assert.assertEquals(successResponse.getString(USER_ID_PARAM), userId,
                "Invalid response received " + successResponse);
        Assert.assertEquals(successResponse.getString("status"), "Deleted",
                "Invalid response received " + successResponse);
        assertNonExistenceOfUser(userId);
    }

    @Test(dependsOnMethods = "testDeleteNonAdminUser2")
    public void testDeleteNonAdminUser3() throws Exception {
        String userId = NON_ADMIN_USER3_ID;
        String response = deleteUserSuccessfully(userId);
        JSONObject successResponse = new JSONObject(response);
        Assert.assertEquals(successResponse.getString(USER_ID_PARAM), userId,
                "Invalid response received " + successResponse);
        Assert.assertEquals(successResponse.getString("status"), "Deleted",
                "Invalid response received " + successResponse);
        assertNonExistenceOfUser(userId);
    }

    private String getUsers() throws IOException {

        SimpleHttpClient client = new SimpleHttpClient();
        Map<String, String> headers = new HashMap<>();
        headers.put("Accept", "application/json");

        String endpoint = userResource;

        HttpResponse response = client.doGet(endpoint, headers);
        Assert.assertEquals(response.getStatusLine().getStatusCode(), 200, "Unexpected status code");
        String responseString = client.getResponsePayload(response);
        log.info("Received payload: " + responseString);
        return responseString;
    }

    private String getSearchedUsers(String searchKey) throws IOException {
        SimpleHttpClient client = new SimpleHttpClient();
        Map<String, String> headers = new HashMap<>();
        headers.put("Accept", "application/json");
        String endpoint = userResource.concat("?searchKey=").concat(searchKey);
        HttpResponse response = client.doGet(endpoint, headers);
        Assert.assertEquals(response.getStatusLine().getStatusCode(), 200, "Unexpected status code");
        String responseString = client.getResponsePayload(response);
        log.info("Received payload: " + responseString);
        return responseString;
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

    private String deleteUserSuccessfully(String userName) throws IOException {
        SimpleHttpClient client = new SimpleHttpClient();
        HttpResponse response = deleteUser(userName);
        Assert.assertEquals(response.getStatusLine().getStatusCode(), 200, "Unexpected status code");
        String responseString = client.getResponsePayload(response);
        log.info("Received payload: " + responseString);
        return responseString;
    }

    private HttpResponse deleteUser(String userName) throws IOException {

        SimpleHttpClient client = new SimpleHttpClient();
        Map<String, String> headers = new HashMap<>();
        headers.put("Accept", "application/json");
        String endpoint = userResource + "/" + userName;
        return client.doDelete(endpoint, headers);
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

    private void assertNonExistenceOfUser(String userId) throws IOException {
        HttpResponse response = getUserPayload(userId);
        Assert.assertEquals(response.getStatusLine().getStatusCode(), 404);
    }

}
