/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.esb.datamapper;

import org.testng.Assert;
import org.testng.annotations.Test;
import org.wso2.carbon.esb.datamapper.common.DataMapperIntegrationTest;

/**
 * Test cases for github.com/wso2/product-ei/issues/2391
 */
public class NestedElementsWithSameNameTest extends DataMapperIntegrationTest {

    @Test(groups = {"wso2.esb"}, description = "Datamapper : test nested elements with same name")
    public void testNestedElementsWithSameName() throws Exception {

        String requestMsg =
                "{\n" + "    \"chartfield\": [ { \n" + "        \"chartfield\": true\n" + "    } ] \n" + "}";

        String response = sendRequest(getApiInvocationURL("sampleNestedElementAPI"), requestMsg, "application/json");
        Assert.assertEquals(response,
                "<jsonObject><chartfield><chartfield>true</chartfield></chartfield></jsonObject" + ">",
                "unexpected response for data-mapper nested element test");
    }
}
