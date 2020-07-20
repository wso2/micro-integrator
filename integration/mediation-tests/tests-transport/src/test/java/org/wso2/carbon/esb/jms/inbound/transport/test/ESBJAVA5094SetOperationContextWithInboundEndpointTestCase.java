/*
 * Copyright (c) 2017, WSO2 Inc. (http://wso2.com) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
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
package org.wso2.carbon.esb.jms.inbound.transport.test;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.util.AXIOMUtil;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.annotations.ExecutionEnvironment;
import org.wso2.carbon.automation.engine.annotations.SetEnvironment;
import org.wso2.carbon.automation.engine.frameworkutils.FrameworkPathUtil;
import org.wso2.carbon.automation.extensions.servers.jmsserver.client.JMSQueueMessageProducer;
import org.wso2.carbon.automation.extensions.servers.jmsserver.controller.config.JMSBrokerConfigurationProvider;
import org.wso2.esb.integration.common.utils.CarbonLogReader;
import org.wso2.esb.integration.common.utils.ESBIntegrationTest;
import org.wso2.esb.integration.common.utils.Utils;
import org.wso2.esb.integration.common.utils.common.SqlDataSourceUtil;

import java.io.File;
import java.util.ArrayList;
import javax.xml.stream.XMLStreamException;

/**
 * Related JIRA: https://wso2.org/jira/browse/ESBJAVA-5094
 * This testcase tests whether the operation context of axis2 context is set back when it is null with inbound endpoint
 */
public class ESBJAVA5094SetOperationContextWithInboundEndpointTestCase extends ESBIntegrationTest {

    private static final String QUEUE_NAME = "testInboundQueue";
    private SqlDataSourceUtil sqlDataSourceUtilLookup = null;
    private CarbonLogReader logReader;

    @BeforeClass(alwaysRun = true)
    public void initialize() throws Exception {

        super.init();
        sqlDataSourceUtilLookup = new SqlDataSourceUtil(getSessionCookie(), contextUrls.getBackEndUrl());
        addDataSource();

        OMElement messageStoreConfig = AXIOMUtil.stringToOM(
                "<messageStore name=\"JDBC\" class=\"org.apache.synapse.message.store.impl.jdbc.JDBCMessageStore\" "
                        + "xmlns=\"http://ws.apache.org/ns/synapse\">\n" + "   <parameter name=\"store.jdbc.driver\">"
                        + sqlDataSourceUtilLookup.getDriver() + "</parameter>\n"
                        + "   <parameter name=\"store.jdbc.connection.url\">" + sqlDataSourceUtilLookup.getJdbcUrl()
                        + "</parameter>\n" + "   <parameter name=\"store.jdbc.username\">" + sqlDataSourceUtilLookup
                        .getDatabaseUser() + "</parameter>\n" + "   <parameter name=\"store.jdbc.password\">"
                        + sqlDataSourceUtilLookup.getDatabasePassword() + "</parameter>\n"
                        + "   <parameter name=\"store.jdbc.table\">message_stored</parameter>\n"
                        + "   <parameter name=\"store.producer.guaranteed.delivery.enable\">false</parameter>\n"
                        + "   <parameter name=\"store.failover.message.store.name\">JDBC</parameter>\n"
                        + "</messageStore>");

        OMElement messageProcessorConfig = AXIOMUtil.stringToOM(
                "<messageProcessor\n class=\"org.apache.synapse.message.processor.impl.sampler.SamplingProcessor\"\n"
                        + "        messageStore=\"JDBC\" name=\"JDBCMP\" xmlns=\"http://ws.apache.org/ns/synapse\">\n"
                        + "    <parameter name=\"sequence\">secondSeq</parameter>\n"
                        + "    <parameter name=\"interval\">1000</parameter>\n"
                        + "    <parameter name=\"is.active\">true</parameter>\n"
                        + "    <parameter name=\"concurrency\">1</parameter>\n" + "</messageProcessor>");

        OMElement seq = AXIOMUtil.stringToOM(
                "<sequence xmlns=\"http://ws.apache.org/ns/synapse\"  name=\"firstSeq\" onError=\"fault\">\n" + "    "
                        + "<log level=\"custom\">\n"
                        + "        <property name=\"firstSeq\" value=\"In first sequence !!\"/>\n" + "    </log>\n"
                        + "    <store messageStore=\"JDBC\"/>\n" + "</sequence>");

        logReader = new CarbonLogReader();
        logReader.start();

        Utils.deploySynapseConfiguration(messageStoreConfig, "JDBC", Utils.ArtifactType.MESSAGE_STORES, false);
        Assert.assertTrue(
                Utils.checkForLog(logReader, "Successfully added Message Store configuration of : [JDBC]", 60));

        Utils.deploySynapseConfiguration(messageProcessorConfig, "JDBCMP", Utils.ArtifactType.MESSAGE_PROCESSOR, false);
        Utils.deploySynapseConfiguration(seq, "firstSeq", Utils.ArtifactType.SEQUENCE, false);
        Utils.deploySynapseConfiguration(addEndpoint(), "jmsInboundCtxEP", Utils.ArtifactType.INBOUND_ENDPOINT, false);
    }

    private OMElement addEndpoint() throws XMLStreamException {

        return AXIOMUtil.stringToOM("<inboundEndpoint xmlns=\"http://ws.apache.org/ns/synapse\"\n"
                                            + "                 name=\"jmsInboundCtxEP\"\n"
                                            + "                 sequence=\"firstSeq\"\n"
                                            + "                 onError=\"fault\"\n"
                                            + "                 protocol=\"jms\"\n"
                                            + "                 suspend=\"false\">\n" + "    <parameters>\n"
                                            + "        <parameter name=\"interval\">2000</parameter>\n"
                                            + "        <parameter name=\"transport.jms.Destination\">testInboundQueue</parameter>\n"
                                            + "        <parameter name=\"transport.jms.CacheLevel\">3</parameter>\n"
                                            + "        <parameter name=\"transport.jms.ConnectionFactoryJNDIName\">QueueConnectionFactory</parameter>\n"
                                            + "        <parameter name=\"java.naming.factory.initial\">org.apache.activemq.jndi.ActiveMQInitialContextFactory</parameter>\n"
                                            + "        <parameter name=\"java.naming.provider.url\">tcp://localhost:61616</parameter>\n"
                                            + "        <parameter name=\"transport.jms.SessionAcknowledgement\">AUTO_ACKNOWLEDGE</parameter>\n"
                                            + "        <parameter name=\"transport.jms.SessionTransacted\">false</parameter>\n"
                                            + "        <parameter name=\"transport.jms.ConnectionFactoryType\">queue</parameter>\n"
                                            + "        <parameter name=\"sequential\">true</parameter>\n"
                                            + "        <parameter name=\"coordination\">true</parameter>\n"
                                            + "        <parameter name=\"transport.jms.SubscriptionDurable\">false</parameter>\n"
                                            + "        <parameter name=\"transport.jms.SharedSubscription\">false</parameter>\n"
                                            + "    </parameters>\n" + "</inboundEndpoint>");
    }

    @SetEnvironment(executionEnvironments = { ExecutionEnvironment.STANDALONE })
    @Test(groups = { "wso2.esb" },
            description = "Test for the operation context of axis2 context with inbound endpoint")
    public void settingOperationContextWithInboundTest() throws Exception {

        Assert.assertTrue(isArtifactDeployed(() -> checkSequenceExistence("firstSeq"), DEFAULT_TIMEOUT),
                          "Seq deployment failed.");
        Assert.assertTrue(isArtifactDeployed(() -> checkSequenceExistence("secondSeq"), DEFAULT_TIMEOUT),
                          "Seq deployment failed.");
        Assert.assertTrue(isArtifactDeployed(() -> checkMessageProcessorExistence("JDBCMP"), DEFAULT_TIMEOUT),
                          "Msg processor deployment failed.");
        logReader.clearLogs();
        sendMessage();
        boolean assertValue = Utils.checkForLog(logReader, "In second sequence !!", 60);
        Assert.assertTrue(assertValue, "Operation context becomes null with the inbound endpoint.");
    }

    @AfterClass(alwaysRun = true)
    public void deleteService() {

        logReader.stop();
        Utils.undeploySynapseConfiguration("JDBC", Utils.ArtifactType.MESSAGE_STORES, false);
        Utils.undeploySynapseConfiguration("JDBCMP", Utils.ArtifactType.MESSAGE_PROCESSOR, false);
        Utils.undeploySynapseConfiguration("firstSeq", Utils.ArtifactType.SEQUENCE, false);
        Utils.undeploySynapseConfiguration("jmsInboundCtxEP", Utils.ArtifactType.INBOUND_ENDPOINT, false);
    }

    private void addDataSource() throws Exception {

        ArrayList<File> sqlFileList = new ArrayList<>();
        File stock = new File(
                FrameworkPathUtil.getSystemResourceLocation() + File.separator + "artifacts" + File.separator + "ESB"
                        + File.separator + "sql" + File.separator + "store_message.sql");
        sqlFileList.add(stock);
        sqlDataSourceUtilLookup.createDataSource("test", sqlFileList);
    }

    private void sendMessage() throws Exception {

        JMSQueueMessageProducer sender = new JMSQueueMessageProducer(
                JMSBrokerConfigurationProvider.getInstance().getBrokerConfiguration());
        String message = "<?xml version='1.0' encoding='UTF-8'?>"
                + "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\""
                + " xmlns:ser=\"http://services.samples\" xmlns:xsd=\"http://services.samples/xsd\">"
                + "  <soapenv:Header/>" + "  <soapenv:Body>" + "   <ser:placeOrder>" + "     <ser:order>"
                + "      <xsd:price>100</xsd:price>" + "      <xsd:quantity>2000</xsd:quantity>"
                + "      <xsd:symbol>JMSTransport</xsd:symbol>" + "     </ser:order>" + "   </ser:placeOrder>"
                + "  </soapenv:Body>" + "</soapenv:Envelope>";
        try {
            sender.connect(QUEUE_NAME);
            sender.pushMessage(message);
        } finally {
            sender.disconnect();
        }
    }

}
