/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package endpoint.protocol.jms.tests;

import endpoint.protocol.jms.JMSBrokerController;
import endpoint.protocol.jms.JMSTestsUtils;
import junit.framework.TestCase;
import org.apache.activemq.command.ActiveMQDestination;
import org.junit.Assert;
import org.junit.Test;
import org.wso2.carbon.inbound.endpoint.common.InboundTask;
import org.wso2.carbon.inbound.endpoint.protocol.jms.JMSConstants;
import org.wso2.carbon.inbound.endpoint.protocol.jms.JMSPollingConsumer;
import org.wso2.carbon.inbound.endpoint.protocol.jms.JMSTask;

import java.util.Properties;
import java.util.Set;

public class JMSPollingConsumerTopicTest extends TestCase {

    private final static String PROVIDER_URL = "tcp://127.0.0.1:61616";
    private final static long INTERVAL = 1000;
    private final static String INBOUND_EP_NAME = "testPolling";

    /**
     * Test polling on topic using JMS inbound endpoint task
     *
     * @throws Exception
     */
    @Test
    public void testPollingOnTopic() throws Exception {
        String topicName = "testTopic1";
        boolean isTopicExist = false;
        Properties jmsProperties = JMSTestsUtils.getJMSPropertiesForDestination(topicName, PROVIDER_URL, false);
        JMSBrokerController brokerController = new JMSBrokerController(PROVIDER_URL, jmsProperties);
        JMSPollingConsumer jmsPollingConsumer = new JMSPollingConsumer(jmsProperties, INTERVAL, INBOUND_EP_NAME);
        InboundTask task = new JMSTask(jmsPollingConsumer, INTERVAL);
        try {
            brokerController.startProcess();
            task.execute();
            ActiveMQDestination[] activeMQDestination = brokerController.getBrokerService().getRegionBroker().
                    getDestinations();
            for (ActiveMQDestination destination : activeMQDestination) {
                if (destination.isTopic() && topicName.equals(destination.getPhysicalName())) {
                    isTopicExist = true;
                }
            }
            Assert.assertTrue("Topic is not added as a subscription", isTopicExist);
        } finally {
            task.destroy();
            brokerController.stopProcess();
        }
    }

    /**
     * Test polling on durable topic using JMS inbound endpoint task
     *
     * @throws Exception
     */
    @Test
    public void testPollingOnDurableTopic() throws Exception {
        String topicName = "testTopic2";
        boolean isTopicExist = false;
        Properties jmsProperties = JMSTestsUtils.getJMSPropertiesForDestination(topicName, PROVIDER_URL, false);
        jmsProperties.put(JMSConstants.PARAM_SUB_DURABLE, "true");
        JMSBrokerController brokerController = new JMSBrokerController(PROVIDER_URL, jmsProperties);
        JMSPollingConsumer jmsPollingConsumer = new JMSPollingConsumer(jmsProperties, INTERVAL, INBOUND_EP_NAME);
        InboundTask task = new JMSTask(jmsPollingConsumer, INTERVAL);
        try {
            brokerController.startProcess();
            task.execute();
            Set<ActiveMQDestination> activeMQDestination = brokerController.getBrokerService().getRegionBroker().
                    getDurableDestinations();
            for (ActiveMQDestination destination : activeMQDestination) {
                if (destination.isTopic() && topicName.equals(destination.getPhysicalName())) {
                    isTopicExist = true;
                }
            }
            Assert.assertTrue("Topic is not added as a subscription", isTopicExist);
        } finally {
            task.destroy();
            brokerController.stopProcess();
        }
    }

    /**
     * Test polling on durable topic using JMS inbound endpoint task when the JMS Spec Version is 2.0
     *
     * @throws Exception
     */
    @Test
    public void testPollingOnDurableTopicV2() throws Exception {
        String topicName = "testTopic3";
        boolean isTopicExist = false;
        Properties jmsProperties = JMSTestsUtils.getJMSPropertiesForDestination(topicName, PROVIDER_URL, false);
        jmsProperties.put(JMSConstants.PARAM_SUB_DURABLE, "true");
        jmsProperties.put(JMSConstants.PARAM_JMS_SPEC_VER, JMSConstants.JMS_SPEC_VERSION_2_0);
        JMSBrokerController brokerController = new JMSBrokerController(PROVIDER_URL, jmsProperties);
        JMSPollingConsumer jmsPollingConsumer = new JMSPollingConsumer(jmsProperties, INTERVAL, INBOUND_EP_NAME);
        InboundTask task = new JMSTask(jmsPollingConsumer, INTERVAL);
        try {
            brokerController.startProcess();
            task.execute();
            Set<ActiveMQDestination> activeMQDestination = brokerController.getBrokerService().getRegionBroker().
                    getDurableDestinations();
            for (ActiveMQDestination destination : activeMQDestination) {
                if (destination.isTopic() && topicName.equals(destination.getPhysicalName())) {
                    isTopicExist = true;
                }
            }
            Assert.assertTrue("Topic is not added as a subscription", isTopicExist);
        } finally {
            task.destroy();
            brokerController.stopProcess();
        }
    }

    /**
     * Test polling on durable topic using JMS inbound endpoint task when the JMS Spec Version is 1.0
     *
     * @throws Exception
     */
    @Test
    public void testPollingOnDurableTopicV1() throws Exception {
        String topicName = "testTopic4";
        boolean isTopicExist = false;
        Properties jmsProperties = JMSTestsUtils.getJMSPropertiesForDestination(topicName, PROVIDER_URL, false);
        jmsProperties.put(JMSConstants.PARAM_SUB_DURABLE, "true");
        jmsProperties.put(JMSConstants.PARAM_JMS_SPEC_VER, JMSConstants.JMS_SPEC_VERSION_1_0);
        JMSBrokerController brokerController = new JMSBrokerController(PROVIDER_URL, jmsProperties);
        JMSPollingConsumer jmsPollingConsumer = new JMSPollingConsumer(jmsProperties, INTERVAL, INBOUND_EP_NAME);
        InboundTask task = new JMSTask(jmsPollingConsumer, INTERVAL);
        try {
            brokerController.startProcess();
            task.execute();
            Set<ActiveMQDestination> activeMQDestination = brokerController.getBrokerService().getRegionBroker().
                    getDurableDestinations();
            for (ActiveMQDestination destination : activeMQDestination) {
                if (destination.isTopic() && topicName.equals(destination.getPhysicalName())) {
                    isTopicExist = true;
                }
            }
            Assert.assertTrue("Topic is not added as a subscription", isTopicExist);
        } finally {
            task.destroy();
            brokerController.stopProcess();
        }
    }

}
