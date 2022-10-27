/*
 * Copyright (c) 2022, WSO2 LLC (http://www.wso2.com).
 *
 * WSO2 LLC licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.esb.mediator.test.enrich;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.esb.integration.common.utils.ESBIntegrationTest;
import org.wso2.esb.integration.common.utils.clients.CloneClient;

public class EnrichJSONPayloadWithSpaceTest extends ESBIntegrationTest {

    private Client client = Client.create();
    private String JSON_Payload = "{ \"exchange\" : { \"sponsor\" : { \"spo\" : [ { \"spo_emad_spo_srs\" : " +
            "\"shirley.kirkaldy@ed.ac.uk; ppls.finance@ed.ac.uk\" } ] } } }";
    private String Expected_Response = "{\"exchange\":{\"sponsor\":{\"spo\":[{\"spo_emad_spo_srs\":" +
            "\"shirley.kirkaldy@ed.ac.uk; ppls.finance@ed.ac.uk\"}]}}}";

    //Creates the API
    @BeforeClass(alwaysRun = true)
    public void setEnvironment() throws Exception {
        super.init();
    }

    @Test(groups = "wso2.esb", description = "Testing json requests with enrich mediator in API")
    public void testJSONWithEnrichMediator() throws Exception {
        WebResource webResource = client.resource(getApiInvocationURL("enrich"));
        ClientResponse getResponse = webResource.type("application/json").post(ClientResponse.class, JSON_Payload);
        String response = getResponse.getEntity(String.class);
        Assert.assertEquals(response, Expected_Response);
    }

}
