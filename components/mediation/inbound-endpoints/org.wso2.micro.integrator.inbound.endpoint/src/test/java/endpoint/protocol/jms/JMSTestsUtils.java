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
package endpoint.protocol.jms;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.soap.SOAPBody;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.util.UIDGenerator;
import org.apache.axis2.AxisFault;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.OperationContext;
import org.apache.axis2.context.ServiceContext;
import org.apache.axis2.description.InOutAxisOperation;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.synapse.config.SynapseConfiguration;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.apache.synapse.core.axis2.Axis2SynapseEnvironment;
import org.wso2.carbon.inbound.endpoint.protocol.jms.JMSConstants;
import org.wso2.carbon.inbound.endpoint.protocol.jms.JMSPollingConsumer;

import java.util.Properties;
import javax.jms.Message;

public class JMSTestsUtils {

    /**
     * Poll message from destination using JMSPollingConsumer
     *
     * @return Properties
     */
    public static Properties getJMSPropertiesForDestination(String destinationName, String providerURL,
                                                            boolean isQueue) {
        Properties jmsProperties = new Properties();
        jmsProperties.put(JMSConstants.PROVIDER_URL, providerURL);
        jmsProperties
                .put(JMSConstants.NAMING_FACTORY_INITIAL, "org.apache.activemq.jndi.ActiveMQInitialContextFactory");
        jmsProperties.put(JMSConstants.CONNECTION_FACTORY_JNDI_NAME, "QueueConnectionFactory");
        if (isQueue) {
            jmsProperties.put(JMSConstants.CONNECTION_FACTORY_TYPE, JMSConstants.DESTINATION_TYPE_QUEUE);
        } else {
            jmsProperties.put(JMSConstants.CONNECTION_FACTORY_TYPE, JMSConstants.DESTINATION_TYPE_TOPIC);
        }
        jmsProperties.put(JMSConstants.DESTINATION_NAME, destinationName);
        jmsProperties.put(JMSConstants.SESSION_TRANSACTED, "false");
        jmsProperties.put(JMSConstants.SESSION_ACK, "AUTO_ACKNOWLEDGE");
        jmsProperties.put(JMSConstants.PARAM_CACHE_LEVEL, 3);
        return jmsProperties;
    }

    /**
     * Poll message from destination using JMSPollingConsumer
     *
     * @return Message
     * @throws InterruptedException on an error polling message
     */
    public static Message pollMessagesFromDestination(JMSPollingConsumer jmsPollingConsumer)
            throws InterruptedException {
        Message receivedMsg = jmsPollingConsumer.poll();
        int count = 0;
        while (receivedMsg == null) {
            count++;
            if (count == 10) {
                // return null to avoid hanging
                return null;
            }
            Thread.sleep(10);
            receivedMsg = jmsPollingConsumer.poll();
        }
        return receivedMsg;
    }

    /**
     * Create a empty message context.
     *
     * @return A context with empty message
     * @throws AxisFault on an error creating a context
     */
    public static org.apache.synapse.MessageContext createMessageContext() throws AxisFault {

        Axis2SynapseEnvironment synapseEnvironment = new Axis2SynapseEnvironment(new SynapseConfiguration());
        org.apache.axis2.context.MessageContext axis2MC = new org.apache.axis2.context.MessageContext();
        axis2MC.setConfigurationContext(new ConfigurationContext(new AxisConfiguration()));

        ServiceContext svcCtx = new ServiceContext();
        OperationContext opCtx = new OperationContext(new InOutAxisOperation(), svcCtx);
        axis2MC.setServiceContext(svcCtx);
        axis2MC.setOperationContext(opCtx);
        org.apache.synapse.MessageContext mc = new Axis2MessageContext(axis2MC, new SynapseConfiguration(),
                                                                       synapseEnvironment);
        mc.setMessageID(UIDGenerator.generateURNString());
        SOAPEnvelope env = OMAbstractFactory.getSOAP11Factory().createSOAPEnvelope();
        OMNamespace namespace = OMAbstractFactory.getSOAP11Factory()
                .createOMNamespace("http://ws.apache.org/commons/ns/payload", "text");
        env.declareNamespace(namespace);
        mc.setEnvelope(env);
        SOAPBody body = OMAbstractFactory.getSOAP11Factory().createSOAPBody();
        OMElement element = OMAbstractFactory.getSOAP11Factory().createOMElement("TestElement", namespace);
        element.setText("This is a test!!");
        body.addChild(element);
        mc.getEnvelope().addChild(body);
        return mc;
    }

}
