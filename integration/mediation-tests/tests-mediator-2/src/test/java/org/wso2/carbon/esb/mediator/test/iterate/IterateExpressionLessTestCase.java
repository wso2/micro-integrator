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

package org.wso2.carbon.esb.mediator.test.iterate;

import java.util.HashMap;
import java.util.Map;

import org.apache.http.HttpResponse;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.esb.integration.common.utils.ESBIntegrationTest;
import org.wso2.esb.integration.common.utils.clients.SimpleHttpClient;

/**
 * This class will test Iterator Mediator, when there is no iterator expression specified.
 */
public class IterateExpressionLessTestCase extends ESBIntegrationTest {

    @BeforeClass(alwaysRun = true)
    public void uploadSynapseConfig() throws Exception {
        super.init();
    }

    /**
     * This is a negative test under iterator mediator.It will not specify an iterator expression and will try to create
     * a sequence.A sequence cannot be created because  SequenceMediator Sequence named Value cannot be found.
     */

    @Test(groups = {"wso2.esb"}, description = "Testing when there is no iterate expression is specified")
    public void testIterateExpressionLessSequenceAdding() throws Exception {

        SimpleHttpClient client = new SimpleHttpClient();
        Map<String, String> headers = new HashMap<>();
        headers.put("Accept", "application/json");
        String endpoint = "https://localhost:9354/management/sequences?sequenceName=invalidIterateMessages";

        HttpResponse response = client.doGet(endpoint, headers);
        Assert.assertEquals(response.getStatusLine().getStatusCode(), 404,
                            "This Configuration can not be saved successfully");

    }
}
