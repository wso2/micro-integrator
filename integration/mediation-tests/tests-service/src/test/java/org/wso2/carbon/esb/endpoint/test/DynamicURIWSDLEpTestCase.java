/**
 * Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 * <p>
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.esb.endpoint.test;

import java.io.IOException;
import javax.xml.stream.XMLStreamException;

import org.apache.axiom.om.util.AXIOMUtil;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.endpoint.stub.types.EndpointAdminEndpointAdminException;
import org.wso2.carbon.esb.endpoint.test.util.EndpointTestUtils;
import org.wso2.esb.integration.common.clients.endpoint.EndPointAdminClient;
import org.wso2.esb.integration.common.utils.ESBIntegrationTest;

public class DynamicURIWSDLEpTestCase extends ESBIntegrationTest {

    private String ENDPOINT_PATH_1 = "conf:/DynamicAddressEndpointConf";
    private String ENDPOINT_PATH_2 = "gov:/DynamicAddressEndpointGov";
    private String ENDPOINT_XML = "<endpoint xmlns=\"http://ws.apache.org/ns/synapse\">\n"
            +
            "   <wsdl uri=\"http://webservices.amazon.com/AWSECommerceService/JP/AWSECommerceService1.wsdl\" " +
            "service=\"AWSECommerceService1\" port=\"AWSECommerceServicePort\" >\n"
            + "      <suspendOnFailure>\n" + "         <progressionFactor>1.0</progressionFactor>\n"
            + "      </suspendOnFailure>\n" + "      <markForSuspension>\n"
            + "         <retriesBeforeSuspension>0</retriesBeforeSuspension>\n"
            + "         <retryDelay>0</retryDelay>\n" + "      </markForSuspension>\n" + "   </wsdl>\n" + "</endpoint>";
    private EndPointAdminClient endPointAdminClient;

    @BeforeClass(alwaysRun = true)
    public void init() throws Exception {
        super.init();
    }

    @Test(groups = {"wso2.esb"})
    public void testDynamicURIESDLEndpoint() throws Exception {
        dynamicEndpointAdditionScenario(ENDPOINT_PATH_1);
        dynamicEndpointAdditionScenario(ENDPOINT_PATH_2);
    }

    private void dynamicEndpointAdditionScenario(String path)
            throws IOException, EndpointAdminEndpointAdminException, XMLStreamException {
        int beforeCount = endPointAdminClient.getDynamicEndpointCount();
        endPointAdminClient.addDynamicEndPoint(path, AXIOMUtil.stringToOM(ENDPOINT_XML));
        EndpointTestUtils.assertDynamicEndpointAddition(path, beforeCount, endPointAdminClient);
    }
}

