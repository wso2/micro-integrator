/*
 * Copyright (c) 2022, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
import org.wso2.micro.core.util.StringUtils;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * User and role management test for primary and secondary user stores.
 */
public class SecondaryUserManagementTests extends ESBIntegrationTest {

    private String userResource;
    private String roleResource;
    private final String USER_ID_PARAM = "userId";
    private final String ADMIN_USER_ID = "adminUser";
    private final String SECONDARY_USER_ID = "wso2User";
    private final String NON_ADMIN_USER_ID = "nonAdminUser";
    private final String ROLE = "role";
    private final String NEW_ROLE = "newRole";
    private final String SECONDARY_ROLE = "wso2Role";
    private final String HYBRID_ROLE = "Internal/internalRole";
    private final String PRIMARY_DOMAIN = "primary";
    private final String SECONDARY_DOMAIN = "wso2.com";

    @BeforeClass
    public void initialize() {
        initLight();
        userResource = "https://" + hostName + ":" + (DEFAULT_INTERNAL_API_HTTPS_PORT + portOffset) + "/management"
                       + "/users";
        roleResource = "https://" + hostName + ":" + (DEFAULT_INTERNAL_API_HTTPS_PORT + portOffset) + "/management"
                + "/roles";
    }

    @Test
    public void testGetInitialUsers() throws Exception {

        String users = getUsers();
        JSONObject usersJson = new JSONObject(users);

        String errorMessageOnAssertionFailure = "Received response " + users;
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
        String response = addValidUser(userId, "adminpwd", true, PRIMARY_DOMAIN);
        JSONObject successResponse = new JSONObject(response);
        Assert.assertEquals(successResponse.getString(USER_ID_PARAM), userId,
                            "Invalid response received " + successResponse);
        Assert.assertEquals(successResponse.getString("status"), "Added",
                            "Invalid response received " + successResponse);
        validateUserDetails(userId, new String[]{"Internal/everyone", "admin"}, true, PRIMARY_DOMAIN);
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
        assertNonExistenceOfUser(userId,PRIMARY_DOMAIN);
    }

    @Test(dependsOnMethods = "testGetInitialUsers")
    public void testAddNonAdmin() throws Exception {
        String userId = NON_ADMIN_USER_ID;
        String response = addValidUser(userId, "pwd-nonadmin", false, PRIMARY_DOMAIN);
        JSONObject successResponse = new JSONObject(response);
        Assert.assertEquals(successResponse.getString(USER_ID_PARAM), userId,
                            "Invalid response received " + successResponse);
        Assert.assertEquals(successResponse.getString("status"), "Added",
                            "Invalid response received " + successResponse);
        validateUserDetails(userId, new String[]{"Internal/everyone"}, false, PRIMARY_DOMAIN);
    }

    @Test(dependsOnMethods = "testAddNonAdmin")
    public void testAddExistingUser() throws Exception {
        HttpResponse response = addUser(NON_ADMIN_USER_ID, "pwd-nonadmin2", false, PRIMARY_DOMAIN);
        Assert.assertEquals(response.getStatusLine().getStatusCode(), 400);
    }

    @Test(dependsOnMethods = "testAddExistingUser")
    public void testDeleteNonExistentUser() throws Exception {
        String userId = "nonExistentUser";
        HttpResponse response = deleteUser(userId);
        Assert.assertEquals(response.getStatusLine().getStatusCode(), 404);
    }

    @Test(dependsOnMethods = "testDeleteNonExistentUser")
    public void testGetInitialRoles() throws Exception {
        String roles = getRoles();
        JSONObject rolesJson = new JSONObject(roles);
        String errorMessageOnAssertionFailure = "Received response" + roles;
        //Assert role count
        Assert.assertEquals(rolesJson.get("count"), 2, errorMessageOnAssertionFailure);
        //Assert role details
        Assert.assertEquals((rolesJson.getJSONArray("list")).length(), 2, errorMessageOnAssertionFailure);
        Assert.assertTrue(roles.contains("{\"role\":\"admin\"}"), "Could not find the admin role");
        Assert.assertTrue(roles.contains("{\"role\":\"Internal/everyone\"}"),
                "Could not find the internal/everyone role");
    }

    @Test(dependsOnMethods = "testGetInitialRoles")
    public void testAddRoleToPrimary() throws Exception {
        String response = addValidRole(NEW_ROLE, PRIMARY_DOMAIN);
        JSONObject successResponse = new JSONObject(response);
        Assert.assertEquals(successResponse.getString(ROLE), NEW_ROLE,
                "Invalid response received " + successResponse);
        Assert.assertEquals(successResponse.getString("status"), "Added",
                "Invalid response received " + successResponse);
    }

    @Test(dependsOnMethods = "testAddRoleToPrimary")
    public void testAddHybridRole() throws Exception {
        String response = addValidRole(HYBRID_ROLE, PRIMARY_DOMAIN);
        JSONObject successResponse = new JSONObject(response);
        Assert.assertEquals(successResponse.getString(ROLE), HYBRID_ROLE,
                "Invalid response received " + successResponse);
        Assert.assertEquals(successResponse.getString("status"), "Added",
                "Invalid response received " + successResponse);
    }

    @Test(dependsOnMethods = "testAddHybridRole")
    public void testDeleteRole() throws Exception {
        String response = deleteRole(NEW_ROLE);
        JSONObject successResponse = new JSONObject(response);
        Assert.assertEquals(successResponse.getString(ROLE), NEW_ROLE,
                "Invalid response received " + successResponse);
        Assert.assertEquals(successResponse.getString("status"), "Deleted",
                "Invalid response received " + successResponse);
    }

    @Test(dependsOnMethods = "testDeleteRole")
    public void testVerifyDeletedRole() throws Exception {
        String roles = getRoles();
        JSONObject rolesJson = new JSONObject(roles);
        String errorMessageOnAssertionFailure = "Received response" + roles;
        //Assert role count
        Assert.assertEquals(rolesJson.get("count"), 3, errorMessageOnAssertionFailure);
    }

    @Test(dependsOnMethods = "testVerifyDeletedRole")
    public void testAddRoleToSecondary() throws Exception {
        String response = addValidRole(SECONDARY_ROLE, SECONDARY_DOMAIN);
        JSONObject successResponse = new JSONObject(response);
        Assert.assertEquals(successResponse.getString(ROLE), SECONDARY_ROLE,
                "Invalid response received " + successResponse);
        Assert.assertEquals(successResponse.getString("status"), "Added",
                "Invalid response received " + successResponse);
    }
    @Test (dependsOnMethods = "testVerifyDeletedRole")
    public void testGetSearchedRoles() throws Exception {
        String roles = getSearchedRoles("admin");
        JSONObject rolesJson = new JSONObject(roles);
        String errorMessageOnAssertionFailure = "Received response" + roles;
        //Assert role count
        Assert.assertEquals(rolesJson.get("count"), 1, errorMessageOnAssertionFailure);
        //Assert role details
        Assert.assertEquals((rolesJson.getJSONArray("list")).length(), 1, errorMessageOnAssertionFailure);
        Assert.assertEquals((rolesJson.getJSONArray("list")).getJSONObject(0).get(ROLE), "admin",
                errorMessageOnAssertionFailure);
    }

    @Test(dependsOnMethods = "testGetSearchedRoles")
    public void testAddUserToSecondary() throws Exception {
        String response = addValidUser(SECONDARY_USER_ID, "adminpwd", false, SECONDARY_DOMAIN);
        JSONObject successResponse = new JSONObject(response);
        Assert.assertEquals(successResponse.getString(USER_ID_PARAM), SECONDARY_USER_ID,
                "Invalid response received " + successResponse);
        Assert.assertEquals(successResponse.getString("status"), "Added",
                "Invalid response received " + successResponse);
        validateUserDetails(SECONDARY_USER_ID, new String[]{"Internal/everyone"}, false, SECONDARY_DOMAIN);
    }

    @Test(dependsOnMethods = "testAddUserToSecondary")
    public void testAssignRoleToUser() throws Exception {
        String[] addedRoles = {"\"Internal/internalRole\"", "\"" + SECONDARY_ROLE + "\""};
        String response = assignRolesToUser(SECONDARY_USER_ID, SECONDARY_DOMAIN, addedRoles, null);
        JSONObject successResponse = new JSONObject(response);
        Assert.assertEquals(successResponse.getString(USER_ID_PARAM), SECONDARY_USER_ID,
                "Invalid response received " + successResponse);
        Assert.assertEquals(successResponse.getString("status"), "Added/removed the roles",
                "Invalid response received " + successResponse);
        validateUserDetails(SECONDARY_USER_ID,
                new String[]{"Internal/everyone", "Internal/internalRole", "WSO2.COM/wso2Role"}, false,
                SECONDARY_DOMAIN);
    }

    @Test(dependsOnMethods = "testAssignRoleToUser")
    public void testRevokeRolesFromUser() throws Exception {
        String[] removedRoles = {"\"Internal/internalRole\"", "\"" + SECONDARY_ROLE + "\""};
        String response = assignRolesToUser(SECONDARY_USER_ID, SECONDARY_DOMAIN, null, removedRoles);
        JSONObject successResponse = new JSONObject(response);
        Assert.assertEquals(successResponse.getString(USER_ID_PARAM), SECONDARY_USER_ID,
                "Invalid response received " + successResponse);
        Assert.assertEquals(successResponse.getString("status"), "Added/removed the roles",
                "Invalid response received " + successResponse);
        validateUserDetails(SECONDARY_USER_ID,
                new String[]{"Internal/everyone"}, false, SECONDARY_DOMAIN);
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

    private String getUser(String user, String domain) throws IOException {

        HttpResponse response = getUserPayload(user, domain);
        Assert.assertEquals(response.getStatusLine().getStatusCode(), 200, "Unexpected status code");
        String responseString = (new SimpleHttpClient()).getResponsePayload(response);
        log.info("Received payload: " + responseString);
        return responseString;
    }

    private HttpResponse getUserPayload(String user, String domain) throws IOException {

        SimpleHttpClient client = new SimpleHttpClient();
        Map<String, String> headers = new HashMap<>();
        headers.put("Accept", "application/json");

        String endpoint = userResource + "/" + user;
        if (!StringUtils.isEmpty(domain)) {
            endpoint = endpoint.concat("?domain=" + domain);
        }
        return client.doGet(endpoint, headers);
    }

    private String addValidUser(String userName, String password, boolean isAdmin, String domain) throws IOException {

        SimpleHttpClient client = new SimpleHttpClient();
        HttpResponse response = addUser(userName, password, isAdmin, domain);
        Assert.assertEquals(response.getStatusLine().getStatusCode(), 200, "Unexpected status code");
        String responseString = client.getResponsePayload(response);
        log.info("Received payload: " + responseString);
        return responseString;
    }

    private HttpResponse addUser(String userName, String password, boolean isAdmin,String domain) throws IOException {

        SimpleHttpClient client = new SimpleHttpClient();
        Map<String, String> headers = new HashMap<>();
        headers.put("Accept", "application/json");

        String endpoint = userResource;
        String payload = "{"
                         + "\"" + USER_ID_PARAM + "\":\"" + userName + "\","
                         + "\"password\":\"" + password + "\","
                         + "\"isAdmin\":\"" + isAdmin + "\" ,"
                         + "\"domain\":\"" + domain + "\""
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

    private void validateUserDetails(String user, String[] expectedRoles, boolean isAdmin, String domain) throws IOException {
        String payload = getUser(user, domain);
        String errorMessageOnAssertionFailure = "Received response " + payload;

        JSONObject payloadJson = new JSONObject(payload);

        if (!StringUtils.isEmpty(domain) && !domain.equalsIgnoreCase(PRIMARY_DOMAIN)) {
            user = domain.concat("/" + user);
        }

        //Assert user id
        Assert.assertEquals(user.toLowerCase(), payloadJson.get(USER_ID_PARAM).toString().toLowerCase(),
                errorMessageOnAssertionFailure);
        Assert.assertEquals(isAdmin, payloadJson.get("isAdmin"), errorMessageOnAssertionFailure);

        //Assert roles
        JSONArray rolesArray = payloadJson.getJSONArray("roles");
        Assert.assertEquals(expectedRoles.length, rolesArray.length(), errorMessageOnAssertionFailure);

        String[] returnedRoleArray = new String[rolesArray.length()];
        for (int i = 0; i < rolesArray.length(); i++) {
            String role = rolesArray.getString(i);
            returnedRoleArray[i] = role;
        }
        // Sort before check equality
        Arrays.sort(expectedRoles);
        Arrays.sort(returnedRoleArray);
        Assert.assertTrue(Arrays.equals(expectedRoles, returnedRoleArray),
                          "There's mismatch between the expected roles " + Arrays.toString(expectedRoles) + "and "
                          + "returned roles: " + Arrays.toString(returnedRoleArray));

    }

    private void assertNonExistenceOfUser(String userId, String domain) throws IOException {
        HttpResponse response = getUserPayload(userId, domain);
        Assert.assertEquals(response.getStatusLine().getStatusCode(), 404);
    }

    private String getRoles() throws IOException {

        SimpleHttpClient client = new SimpleHttpClient();
        Map<String, String> headers = new HashMap<>();
        headers.put("Accept", "application/json");
        String endpoint = roleResource;
        HttpResponse response = client.doGet(endpoint, headers);
        Assert.assertEquals(response.getStatusLine().getStatusCode(), 200, "Unexpected status code");
        String responseString = client.getResponsePayload(response);
        log.info("Received payload: " + responseString);
        return responseString;
    }

    private String getSearchedRoles(String searchKey) throws IOException {

        SimpleHttpClient client = new SimpleHttpClient();
        Map<String, String> headers = new HashMap<>();
        headers.put("Accept", "application/json");
        String endpoint = roleResource.concat("?searchKey=").concat(searchKey);
        HttpResponse response = client.doGet(endpoint, headers);
        Assert.assertEquals(response.getStatusLine().getStatusCode(), 200, "Unexpected status code");
        String responseString = client.getResponsePayload(response);
        log.info("Received payload: " + responseString);
        return responseString;
    }

    private String addValidRole(String roleName, String domain) throws IOException {

        SimpleHttpClient client = new SimpleHttpClient();
        HttpResponse response = addRole(roleName, domain);
        Assert.assertEquals(response.getStatusLine().getStatusCode(), 200, "Unexpected status code");
        String responseString = client.getResponsePayload(response);
        log.info("Received payload: " + responseString);
        return responseString;
    }

    private HttpResponse addRole(String roleName, String domain) throws IOException {

        SimpleHttpClient client = new SimpleHttpClient();
        Map<String, String> headers = new HashMap<>();
        headers.put("Accept", "application/json");

        String endpoint = roleResource;
        String payload = "{\n" +
                "   \"role\" : \"" + roleName + "\",\n" +
                "   \"domain\" : \"" + domain + "\"\n" +
                "}\n";

        return client.doPost(endpoint, headers, payload, "application/json");
    }

    private String deleteRole(String roleName) throws IOException {

        SimpleHttpClient client = new SimpleHttpClient();
        Map<String, String> headers = new HashMap<>();
        headers.put("Accept", "application/json");
        String endpoint = roleResource + "/" + roleName;
        HttpResponse response = client.doDelete(endpoint, headers);
        Assert.assertEquals(response.getStatusLine().getStatusCode(), 200, "Unexpected status code");
        String responseString = client.getResponsePayload(response);
        log.info("Received payload: " + responseString);
        return responseString;
    }

    private String assignRolesToUser(String userName, String domain, String[] addedRoles, String[] removedRoles) throws IOException {

        SimpleHttpClient client = new SimpleHttpClient();
        Map<String, String> headers = new HashMap<>();
        headers.put("Accept", "application/json");
        String addedRolesString, removedRolesString;
        addedRolesString = removedRolesString = "[]";

        if (addedRoles != null) {
            addedRolesString = "[" + String.join(",", addedRoles) + "]";
        }
        if (removedRoles != null) {
            removedRolesString = "[" + String.join(",", removedRoles) + "]";
        }

        String endpoint = roleResource;
        String payload = "{\n" +
                "   \"userId\" : \"" + userName + "\",\n" +
                "   \"domain\" : \"" + domain + "\",\n" +
                "   \"removedRoles\" : " + removedRolesString + ",\n" +
                "   \"addedRoles\" : " + addedRolesString + "\n" +
                "}\n";

        HttpResponse response = client.doPut(endpoint, headers, payload, "application/json");
        String responseString = client.getResponsePayload(response);
        log.info("Received payload: " + responseString);
        return responseString;
    }
}
