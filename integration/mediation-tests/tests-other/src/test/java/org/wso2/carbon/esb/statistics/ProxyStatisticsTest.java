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

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;

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

public class ProxyStatisticsTest extends ESBIntegrationTest {
    private static final int MEDIATOR_ID_INDEX = 4;
    private ThriftServer thriftServer;

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
            axis2Client.sendSimpleStockQuoteRequest(getProxyServiceURLHttp("StockQuoteProxy"), null, "WSO2");
        }
        thriftServer.waitToReceiveEvents(20000, 100); //wait to esb for asynchronously send statistics events to the
        // backend
        Assert.assertEquals(thriftServer.getMsgCount(), 100,
                            "Hundred statistics events are required, but different number is found");

    }

    @Test(groups = {"wso2.esb"}, description = "Dynamic endpoint stat check")
    public void dynamicEndpointStatisticsCollectionTest() throws Exception {

        thriftServer.resetMsgCount();
        thriftServer.resetPreservedEventList();
        axis2Client.sendSimpleStockQuoteRequest(getProxyServiceURLHttp("TestEndpointStatProxy"), null, "WSO2");
        thriftServer.waitToReceiveEvents(2000, 1);
        Assert.assertEquals(thriftServer.getMsgCount(), 1,
                "Hundred statistics events are required, but different number " +
                        "is found");
        Map<String, Object> aggregatedEvent =
                ESBTestCaseUtils.decompress((String) thriftServer.getPreservedEventList().get(0).getPayloadData()[1]);
        ArrayList eventList = (ArrayList) aggregatedEvent.get("events");
        HashSet<String> allMediatorEventIds = new HashSet<>();
        for (Object list : eventList) {
            allMediatorEventIds.add((String) ((ArrayList) list).get(MEDIATOR_ID_INDEX));
        }

        ArrayList<String> mediatorList = new ArrayList<>();
        mediatorList.add("TestEndpointStatProxy@0:TestEndpointStatProxy");
        mediatorList.add("TestEndpointStatProxy@1:PROXY_INSEQ");
        mediatorList.add("TestEndpointStatProxy@2:CallMediator");
        mediatorList.add("TestEndpointStatProxy@3:StatEndpoint1");
        mediatorList.add("TestEndpointStatProxy@4:RespondMediator");

        //Checking whether all the mediators are present in the event
        Assert.assertEquals(eventList.size(), 5, "Five configuration events are required");

        for (String mediatorId : mediatorList) {
            Assert.assertTrue(allMediatorEventIds.contains(mediatorId), "Mediator not found");
        }
    }

    @Test(groups = {"wso2.esb"}, description = "Proxy statistics statistics event data check")
    public void statisticsEventDataTest() throws Exception {
        thriftServer.resetMsgCount();
        thriftServer.resetPreservedEventList();

        axis2Client.sendSimpleStockQuoteRequest(getProxyServiceURLHttp("StockQuoteProxy"), null, "WSO2");
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
        StockQuoteProxy@0:StockQuoteProxy
		StockQuoteProxy@1:AnonymousEndpoint
		StockQuoteProxy@2:PROXY_OUTSEQ
		StockQuoteProxy@3:SendMediator
		 */
        ArrayList<String> mediatorList = new ArrayList<>();
        mediatorList.add("StockQuoteProxy@0:StockQuoteProxy");
        mediatorList.add("StockQuoteProxy@1:AnonymousEndpoint");
        mediatorList.add("StockQuoteProxy@2:PROXY_OUTSEQ");
        mediatorList.add("StockQuoteProxy@3:SendMediator");

        //Checking whether all the mediators are present in the event
        Assert.assertEquals(eventList.size(), 4, "Four configuration events are required");

        for (String mediatorId : mediatorList) {
            Assert.assertTrue(allMediatorEventIds.contains(mediatorId), "Mediator not found");
        }
    }

    @Test(groups = {"wso2.esb"}, description = "Proxy Spilt Aggregate scenario statistics message count check.")
    public void statisticsSpiltAggregateProxyCollectionCountTest() throws Exception {
        thriftServer.resetMsgCount();
        thriftServer.resetPreservedEventList();

        for (int i = 0; i < 100; i++) {
            axis2Client.sendSimpleStockQuoteRequest(getProxyServiceURLHttp("StockQuoteProxy"), null, "WSO2");
        }
        thriftServer.waitToReceiveEvents(20000, 100); //wait to esb for asynchronously send statistics events
        Assert.assertEquals(thriftServer.getMsgCount(), 100,
                            "Hundred statistics events are required, but different number is found");
    }

    @Test(groups = {"wso2.esb"}, description = "Proxy SpiltAggregate statistics event data check")
    public void spiltAggregatesStatisticsEventDataTest() throws Exception {
        thriftServer.resetMsgCount();
        thriftServer.resetPreservedEventList();

        axis2Client.sendMultipleQuoteRequest(getProxyServiceURLHttp("SplitAggregateProxy"), null, "WSO2", 4);
        thriftServer.waitToReceiveEvents(20000, 1);//wait to esb for asynchronously send statistics events to the
        Assert.assertEquals(thriftServer.getMsgCount(), 1, "Statistics event is received");
        Map<String, Object> aggregatedEvent = ESBTestCaseUtils
                .decompress((String) thriftServer.getPreservedEventList().get(0).getPayloadData()[1]);
        ArrayList eventList = (ArrayList) aggregatedEvent.get("events");

        HashSet<String> allMediatorEventIds = new HashSet<>();
        for (Object list : eventList) {
            allMediatorEventIds.add((String) ((ArrayList) list).get(MEDIATOR_ID_INDEX));
        }

		/* Mediator list in the proxy
        SplitAggregateProxy@0:SplitAggregateProxy
		SplitAggregateProxy@1:PROXY_INSEQ
		SplitAggregateProxy@2:IterateMediator
		SplitAggregateProxy@3:SendMediator
		SplitAggregateProxy@4:AnonymousEndpoint
		SplitAggregateProxy@5:PROXY_OUTSEQ
		SplitAggregateProxy@6:AggregateMediator
		SplitAggregateProxy@7:SendMediator
		 */
        ArrayList<String> mediatorList = new ArrayList<>();
        mediatorList.add("SplitAggregateProxy@0:SplitAggregateProxy");
        mediatorList.add("SplitAggregateProxy@1:PROXY_INSEQ");
        mediatorList.add("SplitAggregateProxy@2:IterateMediator");
        mediatorList.add("SplitAggregateProxy@3:SendMediator");
        mediatorList.add("SplitAggregateProxy@4:AnonymousEndpoint");
        mediatorList.add("SplitAggregateProxy@5:PROXY_OUTSEQ");
        mediatorList.add("SplitAggregateProxy@6:AggregateMediator");
        mediatorList.add("SplitAggregateProxy@7:SendMediator");

        //Checking whether all the mediators are present in the event
        Assert.assertEquals(eventList.size(), 25, "Twenty Five configuration events are required");

        for (String mediatorId : mediatorList) {
            Assert.assertTrue(allMediatorEventIds.contains(mediatorId), "Mediator not found");
        }
    }

    @AfterClass(alwaysRun = true)
    public void cleanupArtifactsIfExist() {
        thriftServer.stop();
    }
}
