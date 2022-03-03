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

package org.wso2.ei.dataservice.integration.test.odata;

import org.apache.commons.io.FileUtils;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.frameworkutils.FrameworkPathUtil;
import org.wso2.ei.dataservice.integration.test.DSSIntegrationTest;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import static org.wso2.ei.dataservice.integration.test.odata.ODataTestUtils.*;

/**
 * This class contains OData Testcases to verify whether the metadata details are returned properly.
 * Having additional attributes in the Metadata might throw errors when fed into applications like PowerBI
 */
public class ODataMetaDataTestCase extends DSSIntegrationTest {
    private final String serviceName = "ODataMetadataSampleService";
    private final String configId = "default";
    private String webAppUrl;

    @BeforeClass(alwaysRun = true)
    public void serviceDeployment() throws Exception {
        super.init();
        webAppUrl = dssContext.getContextUrls().getWebAppURL();
    }

    @AfterClass(alwaysRun = true)
    public void destroy() throws Exception {
        deleteService(serviceName);
        cleanup();
    }

    @Test(groups = "wso2.dss", description = "MetaData retrieval test")
    public void MetaDataRetrievalTestCase() throws Exception {
        String expectedMetadataResp = FileUtils.readFileToString(new File(getResourceLocation() +
                File.separator + "resources" + File.separator + "OdataMetadata.xml"));
        String endpoint = webAppUrl + "/odata/" + serviceName + "/" + configId + "/$metadata";
        Map<String, String> headers = new HashMap<>();
        headers.put("Accept", "application/xml");
        Object[] response = sendGET(endpoint, headers);
        Assert.assertEquals(response[0], 200);
        Assert.assertEquals(response[1], expectedMetadataResp);
    }
}
