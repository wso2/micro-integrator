/*
 *  Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.ei.dataservice.integration.test.jira.issues;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.test.utils.http.client.HttpResponse;
import org.wso2.ei.dataservice.integration.test.DSSIntegrationTest;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import static org.testng.Assert.assertTrue;

/**
 * This test is to verify the fix for https://wso2.org/jira/browse/DS-1111
 * test the roles retriever extension point.
 */
public class DS1111UserRoleExtensionTestCase extends DSSIntegrationTest {

    private static final Log log = LogFactory.getLog(DS1111UserRoleExtensionTestCase.class);
    private String serviceEndPoint;

    @BeforeClass(alwaysRun = true)
    public void serviceDeployment() throws Exception {
        super.init();
        String serviceName = "RoleExtension";
        serviceEndPoint = getServiceUrlHttp(serviceName);
    }

    @Test(groups = {
            "wso2.dss" }, description = "Check whether role extension returns roles correctly", alwaysRun = true)
    public void invokeRoleExtensionTest() throws Exception {
        /*
        When invoking the service endpoint, extension should provide required roles.
         */
        HttpResponse response1 = this.getHttpResponse(serviceEndPoint + "/_getemployees");
        assertTrue(response1.getData().contains("</salary>"));
        log.info("role retriever extension worked and roles were provided correctly");
    }

    /**
     * This method will return the http response for the request
     *
     * @param endpoint service endpoint
     * @return HttpResponse
     * @throws Exception
     */
    private HttpResponse getHttpResponse(String endpoint) throws Exception {

        if (endpoint.startsWith("http://")) {
            URL url = new URL(endpoint);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setDoOutput(true);
            conn.setRequestProperty("charset", "UTF-8");
            conn.setReadTimeout(10000);
            conn.connect();
            // Get the response
            StringBuilder sb = new StringBuilder();
            try (BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
                String line;
                while ((line = rd.readLine()) != null) {
                    sb.append(line);
                }
            } catch (FileNotFoundException ignored) {
            }
            return new HttpResponse(sb.toString(), conn.getResponseCode());
        }
        return null;
    }
}
