/*
 * Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.esb.endpoint.test;

import java.io.IOException;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.esb.integration.common.utils.ESBIntegrationTest;

public class InlinedWSDLEndpointTestCase extends ESBIntegrationTest {

    @Override
    @BeforeClass(alwaysRun = true)
    public void init() throws Exception {
        super.init();
    }

    @Test(groups = {"wso2.esb"})
    public void testInlineWSDLEndpoint() throws IOException {
        endpointAdditionScenario();
        endpointStatisticsScenario();
    }

    private void endpointAdditionScenario() throws IOException {
        Assert.assertTrue(checkEndpointExistence("wsdlEpTest"), "Endpoint has not been added to the system properly");
    }

    private void endpointStatisticsScenario() throws IOException {
        Assert.assertTrue(checkEndpointExistence("wsdlEpWithStatisticsEnabledTest"),
                          "Endpoint has not been added to the system properly");
    }
}
