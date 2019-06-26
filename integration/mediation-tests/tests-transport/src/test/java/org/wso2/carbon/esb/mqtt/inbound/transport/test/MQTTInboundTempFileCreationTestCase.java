/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.esb.mqtt.inbound.transport.test;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.util.AXIOMUtil;
import org.apache.commons.io.FileUtils;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.esb.integration.common.utils.ESBIntegrationTest;
import org.wso2.esb.integration.common.utils.Utils;

import java.io.File;

/**
 * This test case is to check if Inbound MQTT Transport receives MQTT messages once deployed
 * 1. Start ActiveMQ server (MQTT Provider)
 * 2. Deploy MQTT transport configuration
 * 3. Send a sample MQTT message
 * 4. Inspect logs and check if message is consumed
 */
public class MQTTInboundTempFileCreationTestCase extends ESBIntegrationTest {

    private static final String CARBON_HOME = "carbon.home";
    private static final String PAHO_TEMP_DIR = "paho";

    @BeforeClass(alwaysRun = true)
    protected void init() throws Exception {
        super.init();
        OMElement inboundOMElement = AXIOMUtil.stringToOM(FileUtils.readFileToString(new File(getESBResourceLocation()
                + File.separator + "mqtt" + File.separator + "inbound" + File.separator + "transport" + File.separator
                + "PublishToMqttTestEndpoint.xml")));
        Utils.deploySynapseConfiguration(inboundOMElement, "PublishToMqttTestEndpoint", "inbound-endpoints", true);
    }

    @Test(groups = { "wso2.esb" }, description = "Temp folder creation test case")
    public void connectToMQTTBroker() throws Exception {

        File file = new File(System.getProperty(CARBON_HOME));
        String[] listOfNames = file.list();
        Boolean tempFileFound = false;

        for (int i = 0; i < listOfNames.length; i++) {
            if (listOfNames[i].contains(PAHO_TEMP_DIR)) {
                tempFileFound = true;
                break;
            }
        }
        Assert.assertFalse(tempFileFound, "Temp files created");
    }

    @AfterClass(alwaysRun = true)
    public void destroy() throws Exception {
        Utils.undeploySynapseConfiguration("PublishToMqttTestEndpoint", "inbound-endpoints");
        super.cleanup();
    }

}
