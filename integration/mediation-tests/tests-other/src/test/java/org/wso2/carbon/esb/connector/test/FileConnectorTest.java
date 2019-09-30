/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.esb.connector.test;

import org.apache.http.HttpResponse;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.extensions.servers.httpserver.SimpleHttpClient;
import org.wso2.esb.integration.common.utils.ESBIntegrationTest;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * This test case deploys the carbon application FileConnectorProjectCompositeApplication_1.0.0.car with File
 * connector version 2.0.18 and then invoke a proxy to create a file using the file connector.
 */
public class FileConnectorTest extends ESBIntegrationTest {

    @BeforeClass(alwaysRun = true)
    public void setEnvironment() throws Exception {

        super.init();
    }

    @Test(groups = "wso2.esb",
            description = "Test whether the file is created using file connector")
    public void testFileCreation() throws Exception {

        String folderLocation = System.getProperty("user.dir") + File.separator + "FileConnectorTest";
        Assert.assertTrue(new File(folderLocation).mkdirs(), "Folder creation failed.");
        String fileLocation = folderLocation + File.separator + "fileConnectorTest.txt";
        SimpleHttpClient client = new SimpleHttpClient();
        Map<String, String> headers = new HashMap<>();
        headers.put("source", fileLocation);
        headers.put("input", "File Connector Test");
        String endpoint = getProxyServiceURLHttp("fileCreatProxy");
        HttpResponse response = client.doGet(endpoint, headers);
        Assert.assertEquals(response.getStatusLine().getStatusCode(), 200, "File creation failed");
        Assert.assertTrue(new File(fileLocation).exists(), "File does not exists in : " + fileLocation);
    }

}
