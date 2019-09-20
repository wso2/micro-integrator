/*
 *  Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.AutomationContext;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.esb.integration.common.utils.ESBIntegrationTest;
import org.wso2.esb.integration.common.utils.ESBTestCaseUtils;
import org.wso2.esb.integration.common.utils.common.ServerConfigurationManager;
import org.wso2.esb.integration.common.utils.servers.ThriftServer;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;

public class SequenceStatisticsTest extends ESBIntegrationTest {
    public static final int MEDIATOR_ID_INDEX = 4;
    ThriftServer thriftServer;

    @BeforeClass(alwaysRun = true)
    protected void initialize() throws Exception {
        //Starting the thrift port to listen to statistics events
        thriftServer = new ThriftServer("Wso2EventTestCase", 7612, true);
        thriftServer.start(7612);
        log.info("Thrift Server is Started on port 7612");

        //Changing synapse configuration to enable statistics and tracing
        ServerConfigurationManager serverConfigurationManager = new ServerConfigurationManager(
                new AutomationContext("ESB", TestUserMode.SUPER_TENANT_ADMIN));
        serverConfigurationManager.applyMIConfigurationWithRestart(new File(
                getESBResourceLocation() + File.separator + "StatisticTestResources" + File.separator
                        + "deployment.toml"));
        super.init();
        thriftServer.waitToReceiveEvents(20000); //waiting for esb to send artifact config data to the thriftserver
    }

    @Test(groups = {"wso2.esb"}, description = "Proxy statistics message count check.")
    public void statisticsCollectionCountTest() throws Exception {
        thriftServer.resetMsgCount();
        thriftServer.resetPreservedEventList();

        for (int i = 0; i < 100; i++) {
            axis2Client.sendSimpleStockQuoteRequest(getProxyServiceURLHttp("SequenceReferenceProxy"), null, "WSO2");
        }
        thriftServer.waitToReceiveEvents(20000, 100); //wait to esb for asynchronously send statistics events to the
        // backend
        Assert.assertEquals(thriftServer.getMsgCount(), 100, "Hundred statistics events are required, but different "
                                                             + "number is found");
    }

    @Test(groups = {"wso2.esb"}, description = "Sequence statistics message count check.")
    public void statisticsCollectionCountTestForNestedSequence() throws Exception {
        thriftServer.resetMsgCount();
        thriftServer.resetPreservedEventList();
        for (int i = 0; i < 100; i++) {
            axis2Client.sendSimpleStockQuoteRequest(getProxyServiceURLHttp("ReferencingProxyStatisticDisableProxy"), null,
                    "WSO2");
        }
        thriftServer.waitToReceiveEvents(20000, 100); //wait to esb for asynchronously send statistics events to the
        // backend
        Assert.assertEquals(thriftServer.getMsgCount(), 100, "Hundred statistics events are required, but different "
                                                            + "number is found");
    }

    @Test(groups = {"wso2.esb"}, description = "Nested Sequence statistics statistics event data check")
    public void statisticsEventDataTestForNestedSequence() throws Exception {
        thriftServer.resetMsgCount();
        thriftServer.resetPreservedEventList();

        axis2Client
                .sendSimpleStockQuoteRequest(getProxyServiceURLHttp("ReferencingProxyStatisticDisableProxy"), null, "WSO2");
        thriftServer.waitToReceiveEvents(20000, 1);//wait to esb for asynchronously send statistics events
        Assert.assertEquals(thriftServer.getMsgCount(), 1, "Statistics event is received");
        Map<String, Object> aggregatedEvent = ESBTestCaseUtils
                .decompress((String) thriftServer.getPreservedEventList().get(0).getPayloadData()[1]);
        ArrayList eventList = (ArrayList) aggregatedEvent.get("events");
        HashSet<String> allMediatorEventIds = new HashSet<>();
        for (Object list : eventList) {
            allMediatorEventIds.add((String) ((ArrayList) list).get(MEDIATOR_ID_INDEX));
        }

		/* Mediator list in the StockQuoteProxy
        BackendSequence@0:BackendSequence
		BackendSequence@1:CallMediator
		StockQuoteService@0:StockQuoteService
		BackendSequence@3:HeaderMediator:Action
		BackendSequence@4:PayloadFactoryMediator
		BackendSequence@5:CallMediator
		StockQuoteService@0:StockQuoteService
		BackendSequence@7:RespondMediator
		 */
        ArrayList<String> mediatorList = new ArrayList<>();
        mediatorList.add("StatisticBackendSequence@0:StatisticBackendSequence");
        mediatorList.add("StatisticBackendSequence@1:CallMediator");
        mediatorList.add("StockQuoteServiceEndPoint@0:StockQuoteServiceEndPoint");
        mediatorList.add("StatisticBackendSequence@3:HeaderMediator:Action");
        mediatorList.add("StatisticBackendSequence@4:PayloadFactoryMediator");
        mediatorList.add("StatisticBackendSequence@5:CallMediator");
        mediatorList.add("StockQuoteServiceEndPoint@0:StockQuoteServiceEndPoint");
        mediatorList.add("StatisticBackendSequence@7:RespondMediator");

        //Checking whether all the mediators are present in the event
        Assert.assertEquals(eventList.size(), 8, "Eight configuration events are required");

        for (String mediatorId : mediatorList) {
            Assert.assertTrue(allMediatorEventIds.contains(mediatorId), "Mediator not found");
        }
    }

    @Test(groups = {"wso2.esb"}, description = "Sequence statistics statistics event data check")
    public void statisticsEventDataTest() throws Exception {
        thriftServer.resetMsgCount();
        thriftServer.resetPreservedEventList();

        axis2Client.sendSimpleStockQuoteRequest(getProxyServiceURLHttp("SequenceReferenceProxy"), null, "WSO2");
        thriftServer.waitToReceiveEvents(20000, 1);//wait to esb for asynchronously send statistics events
        Assert.assertEquals(thriftServer.getMsgCount(), 1, "Statistics event is received");
        Map<String, Object> aggregatedEvent = ESBTestCaseUtils
                .decompress((String) thriftServer.getPreservedEventList().get(0).getPayloadData()[1]);
        ArrayList eventList = (ArrayList) aggregatedEvent.get("events");
        HashSet<String> allMediatorEventIds = new HashSet<>();
        for (Object list : eventList) {
            allMediatorEventIds.add((String) ((ArrayList) list).get(MEDIATOR_ID_INDEX));
        }

		/* Mediator list in the StockQuoteProxy
        ReferencingProxy@0:ReferencingProxy
		BackendSequence@0:BackendSequence
		BackendSequence@1:CallMediator
		StockQuoteService@0:StockQuoteService
		BackendSequence@3:HeaderMediator:Action
		BackendSequence@4:PayloadFactoryMediator
		BackendSequence@5:CallMediator
		StockQuoteService@0:StockQuoteService
		BackendSequence@7:RespondMediator
		 */

        ArrayList<String> mediatorList = new ArrayList<>();
        mediatorList.add("SequenceReferenceProxy@0:SequenceReferenceProxy");
        mediatorList.add("StatisticBackendSequence@0:StatisticBackendSequence");
        mediatorList.add("StatisticBackendSequence@1:CallMediator");
        mediatorList.add("StockQuoteServiceEndPoint@0:StockQuoteServiceEndPoint");
        mediatorList.add("StatisticBackendSequence@3:HeaderMediator:Action");
        mediatorList.add("StatisticBackendSequence@4:PayloadFactoryMediator");
        mediatorList.add("StatisticBackendSequence@5:CallMediator");
        mediatorList.add("StockQuoteServiceEndPoint@0:StockQuoteServiceEndPoint");
        mediatorList.add("StatisticBackendSequence@7:RespondMediator");

        //Checking whether all the mediators are present in the event
        Assert.assertEquals(eventList.size(), 9, "Nine configuration events are required");

        for (String mediatorId : mediatorList) {
            Assert.assertTrue(allMediatorEventIds.contains(mediatorId), "Mediator not found");
        }

    }

    @AfterClass(alwaysRun = true)
    public void cleanupArtifactsIfExist() {
        thriftServer.stop();
    }
}
