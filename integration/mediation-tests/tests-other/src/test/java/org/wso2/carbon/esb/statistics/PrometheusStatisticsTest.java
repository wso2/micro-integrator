/*
 *  Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied. See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.wso2.carbon.esb.statistics;

import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.awaitility.Awaitility;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.AutomationContext;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.automation.extensions.servers.httpserver.SimpleHttpClient;
import org.wso2.esb.integration.common.utils.ESBIntegrationTest;
import org.wso2.esb.integration.common.utils.common.ServerConfigurationManager;

import java.io.File;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import javax.ws.rs.core.MediaType;

public class PrometheusStatisticsTest extends ESBIntegrationTest {

    private ServerConfigurationManager serverConfigurationManager;
    private String serviceUrl;
    private String serviceName = "test";
    private String serviceType = "api";
    private String contextPath = "services";
    String metricsEndPoint = "http://localhost:9391/metric-service/metrics";
    private String initialRequestCount = "1.0.0";
    private String updatedRequestCount = "2.0.0";
    private String initialMetric;
    private String updatedMetric;

    @BeforeClass(alwaysRun = true)
    public void init() throws Exception {

        super.init();
        serverConfigurationManager = new ServerConfigurationManager(
                new AutomationContext("ESB", TestUserMode.SUPER_TENANT_ADMIN));
        serverConfigurationManager.applyMIConfigurationWithRestart(new File(
                getESBResourceLocation()
                        + File.separator + "StatisticTestResources"
                        + File.separator + "prometheus"
                        + File.separator + "deployment.toml"));
        super.init();

        serviceUrl = context.getContextUrls().getServiceUrl()
                .replace("services", serviceName + File.separator + contextPath)
                .replace(hostName, InetAddress.getByName(hostName).getHostAddress());
        initialMetric =
                "wso2_integration_api_request_count_total{service_name=\"" + serviceName + "\", " + "service_type=\""
                        + serviceType + "\", " + "invocation_url=\"" + serviceUrl + "\"} " + initialRequestCount;
        updatedMetric = initialMetric.replace(initialRequestCount, updatedRequestCount);
    }

    @Test(groups = { "wso2.esb" }, description = "Test if metric data are exposed, when it is enabled in config")
    public void testPrometheusPublisher() throws Exception {

        Awaitility.await().pollInterval(50, TimeUnit.MILLISECONDS).atMost(60, TimeUnit.SECONDS).
                until(isManagementApiAvailable());
        SimpleHttpClient client = new SimpleHttpClient();
        Map<String, String> headers = new HashMap<>();
        headers.put("Accept", "application/json");

        HttpResponse response = client.doGet(metricsEndPoint, headers);
        String responsePayload = client.getResponsePayload(response);

        Assert.assertEquals(response.getStatusLine().getStatusCode(), 200, "Metric retrieval failed");
        Assert.assertFalse(responsePayload.isEmpty(), "Metric response is empty");
    }

    @Test(groups = { "wso2.esb" }, description = "Verifies request counter increments for context \"/services\"")
    public void testRequestCounterIncrementsForServicesContext() throws Exception {

        SimpleHttpClient client = new SimpleHttpClient();
        Map<String, String> headers = new HashMap<>();
        headers.put(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON);
        client.doGet(serviceUrl, headers);

        HttpResponse response = client.doGet(metricsEndPoint, headers);
        String responsePayload = client.getResponsePayload(response);
        responsePayload.contains(initialMetric);

        client.doGet(serviceUrl, headers);
        response = client.doGet(metricsEndPoint, headers);
        responsePayload = client.getResponsePayload(response);
        responsePayload.contains(updatedMetric);
    }

    @AfterClass(alwaysRun = true)
    public void cleanup() throws Exception {

        super.cleanup();
    }

}
