/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.esb.registry.task;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.llom.util.AXIOMUtil;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.esb.integration.common.utils.CarbonLogReader;
import org.wso2.esb.integration.common.utils.ESBIntegrationTest;
import org.wso2.esb.integration.common.utils.Utils;

/**
 * ESBJAVA-4565
 * NullPointerException when registry resource is accessed inside a scheduled task.
 */

public class ESBJAVA4565TestCase extends ESBIntegrationTest {
    CarbonLogReader carbonLogReader = new CarbonLogReader();

    @BeforeClass(alwaysRun = true)
    protected void init() throws Exception {
        super.init();

        OMElement task = AXIOMUtil.stringToOM(
                "<task \n"
                        + "           name=\"TestTask\"\n"
                        + "           class=\"org.apache.synapse.startup.tasks.MessageInjector\" group=\"synapse.simple.quartz\" "
                        + "xmlns=\"http://ws.apache.org/ns/synapse\">\n"
                        + "    <trigger interval=\"10\"/>\n"
                        + "    <property name=\"format\" "
                        + "value=\"get\" xmlns:task=\"http://www.wso2.org/products/wso2commons/tasks\"/>\n"
                        + "    <property name=\"sequenceName\" value=\"ESBJAVA4565TestSequence\" "
                        + "xmlns:task=\"http://www.wso2.org/products/wso2commons/tasks\"/>\n"
                        + "    <property name=\"injectTo\" value=\"sequence\" xmlns:task=\"http://www.wso2.org/products/wso2commons/tasks\"/>\n"
                        + "    <property name=\"message\" xmlns:task=\"http://www.wso2.org/products/wso2commons/tasks\"><empty/></property>\n" + "</task>");
        Utils.deploySynapseConfiguration(task, "TestTask", "tasks", true);
        carbonLogReader.start();
    }

    @Test(groups = "wso2.esb", description = "Analyze carbon logs to find NPE due to unresolved tenant domain.")
    public void checkErrorLog() throws Exception {
        boolean hasErrorLog = carbonLogReader.checkForLog(
                "java.lang.NullPointerException: Tenant domain has not been set in CarbonContext", DEFAULT_TIMEOUT);
        Assert.assertFalse(hasErrorLog,
                "Tenant domain not resolved when registry resource is accessed inside " + "a scheduled task");
        carbonLogReader.stop();
    }
}
