/*
 *Copyright (c) 2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.esb.mediator.test.property;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.extensions.servers.jmsserver.controller.config.JMSBrokerConfigurationProvider;
import org.wso2.esb.integration.common.utils.ESBIntegrationTest;
import org.wso2.esb.integration.common.utils.Utils;
import org.wso2.esb.integration.common.utils.clients.axis2client.AxisServiceClient;

import java.util.Properties;
import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.Session;
import javax.naming.Context;
import javax.naming.InitialContext;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

/**
 * This class tests the functionality of the JMS_COORELATION_ID property
 */

public class PropertyIntegrationJmsCoorelationIDPropertyTestCase extends ESBIntegrationTest {
    private MessageConsumer consumer;
    private Connection connection;

    @BeforeClass(alwaysRun = true)
    public void setEnvironment() throws Exception {
        super.init();
    }

    @AfterClass(alwaysRun = true)
    public void close() throws Exception {
        try {
            if (consumer != null) {
                consumer.close();
            }

            if (connection != null) {
                connection.close();
            }

        } finally {
            super.cleanup();
        }
    }

    @Test(groups = { "wso2.esb" }, description = "Test adding of JMS_COORELATION_ID - " + "hard coded method")
    public void testAddingJMSCoorelationID() throws Exception {

        super.init();

        AxisServiceClient client = new AxisServiceClient();
        client.sendRobust(Utils.getStockQuoteRequest("JMS"),
                getProxyServiceURLHttp("propertyJmsCorrelationIdTestProxy"), "getQuote");

        Thread.sleep(5000);

        //Adding JNDI properties
        Properties props = new Properties();
        props.setProperty(Context.INITIAL_CONTEXT_FACTORY, "org.apache.activemq.jndi.ActiveMQInitialContextFactory");
        props.setProperty(Context.PROVIDER_URL,
                JMSBrokerConfigurationProvider.getInstance().getBrokerConfiguration().getProviderURL());

        //Specify queue propertyname as queue.jndiname
        String queueName = "testAddingJMSCoorelationID";
        props.setProperty("queue.testAddingJMSCoorelationID", queueName);

        Context ctx = new InitialContext(props);
        ConnectionFactory connectionFactory = (ConnectionFactory) ctx.lookup("ConnectionFactory");
        connection = connectionFactory.createConnection("guest", "guest");
        connection.start();

        Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

        Destination destination = (Destination) ctx.lookup(queueName);

        consumer = session.createConsumer(destination);
        Message message = consumer.receive(5000);

        assertNotNull(message, "Consumed message is Null");
        assertEquals(message.getJMSCorrelationID(), ("1234"), "Correlation ID mismatch");
    }
}
