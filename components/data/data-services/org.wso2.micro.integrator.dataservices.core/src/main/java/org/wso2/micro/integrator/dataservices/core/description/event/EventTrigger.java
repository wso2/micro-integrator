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
package org.wso2.micro.integrator.dataservices.core.description.event;

import org.apache.axiom.om.OMDocument;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.micro.integrator.dataservices.common.DBConstants;
import org.wso2.micro.integrator.dataservices.core.DBUtils;
import org.wso2.micro.integrator.dataservices.core.DataServiceFault;
import org.wso2.micro.integrator.dataservices.core.engine.DataService;
import org.wso2.micro.integrator.dataservices.core.internal.DataServicesDSComponent;

import javax.xml.namespace.QName;
import java.util.*;

/**
 * This class represents an <event-trigger> .. </event-trigger> element in a dbs.
 * Which is used to define a event trigger that can be specified in queries (input and output sections).
 */
public abstract class EventTrigger {

    public static final String EVENT_DISPATCHER_NAME = "wsEventDispatcher";

    private static Log log = LogFactory.getLog(EventTrigger.class);

    private DataService dataService;

    private String language;

    private String triggerId;

    private String expression;

    private String targetTopic;

    private List<String> endpointUrls;

    public EventTrigger(DataService dataService, String language,
                        String triggerId, String expression, String targetTopic,
                        List<String> endpointUrls) throws DataServiceFault {
        this.dataService = dataService;
        this.language = language;
        this.triggerId = triggerId;
        this.expression = expression;
        this.targetTopic = targetTopic;
        this.endpointUrls = endpointUrls;
        if (!dataService.isServiceInactive()) {
            DataServicesDSComponent.registerSubscriptions(this);
        }
    }

    public void processEventTriggerSubscriptions() {
//        try {
//            this.registerSubscribers(DataServicesDSComponent.getEventBroker(), this.getTargetTopic(), this.getEndpointUrls());
//        } catch (Exception e) {
//            log.error("Error in processing event trigger subscriptions: " + e.getMessage());
//        }
    }

    public DataService getDataService() {
        return dataService;
    }

    public String getLanguage() {
        return language;
    }

    public String getTriggerId() {
        return triggerId;
    }

    public String getExpression() {
        return expression;
    }

    public String getTargetTopic() {
        return targetTopic;
    }

    public List<String> getEndpointUrls() {
        return endpointUrls;
    }

    /**
     * Registers a given subscription to a given topic.
     *
     * @param eventBroker  EventBroker
     * @param topic        Topic
     * @param endpointUrls Endpoint URLs
     * @throws DataServiceFault DataServiceFault
     */
//    private void registerSubscribers(EventBroker eventBroker, String topic,
//                                     List<String> endpointUrls) throws DataServiceFault {
//        if (eventBroker == null) {
//			String msg = "Unable To Register Event Subscribers for topic: '"
//					+ topic + "' , Event Broker Not Available.";
//            log.warn(msg);
//            return;
//        }
//
//        /*
//         * Adding new subscriptions.
//         */
//        Subscription subscription;
//        for (String epr : endpointUrls) {
//            try {
//                int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
//                PrivilegedCarbonContext.startTenantFlow();
//                PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantId(tenantId, true);
//                PrivilegedCarbonContext.getThreadLocalCarbonContext().setUsername(
//                        CarbonConstants.REGISTRY_SYSTEM_USERNAME);
//                subscription = new Subscription();
//                subscription.setEventSinkURL(epr);
//                subscription.setId(UUID.randomUUID().toString());
//                subscription.setTopicName(topic);
//                subscription.setEventDispatcherName(EVENT_DISPATCHER_NAME);
//                //Setting subscription owner as wso2.system.user as there's no logged-in user
//                //at service deployment time.
//                subscription.setOwner(CarbonContext.getThreadLocalCarbonContext().getUsername());
//                subscription.setProperties(this.getSubscriptionProperties());
//                eventBroker.subscribe(subscription);
//            } catch (Exception e) {
//                throw new DataServiceFault(e, "Error in event subscription for EPR: " + epr +
//                        " Topic:" + topic);
//            } finally {
//            	PrivilegedCarbonContext.endTenantFlow();
//            }
//        }
//    }

    /**
     * Retrieves the property map corresponding to a particular subscription.
     *
     * @return map of additional subscription properties
     */
    private Map<String, String> getSubscriptionProperties() {
        Map<String, String> properties = new HashMap<String, String>();
        properties.put(DBConstants.DATA_SERVICE_NAME, this.getDataService().getName());
        return properties;
    }

    /**
     * This method sends a particular message for a give topic.
     *
     * @param omMessage Converted message as an OMElement
     * @param topic     Topic
     * @throws DataServiceFault DataServiceFault
     */
//    private void sendMessageToTopic(OMElement omMessage, String topic) throws DataServiceFault {
//        try {
//            EventBroker eventBroker = DataServicesDSComponent.getEventBroker();
//            if (eventBroker == null) {
//                throw new DataServiceFault("Event broker instance is not available");
//            }
//            Message message = new Message();
//            message.setMessage(omMessage);
//            eventBroker.publish(message, topic);
//        } catch (Exception e) {
//            throw new DataServiceFault(e, "Error in publishing event for topic: " +
//                    topic + " message:-\n" + omMessage);
//        }
//    }

    /**
     * This class must be implemented by a concrete implementation of the EventTrigger class,
     * to provide the logic in evaluating the XML input.
     *
     * @param input input as an OMElement
     * @return a boolean
     * @throws DataServiceFault DataServiceFault
     */
    protected abstract boolean evaluate(OMElement input) throws DataServiceFault;

    private OMElement createEventMessage(DataService dataService, String queryId, OMElement data) {
        OMFactory fac = DBUtils.getOMFactory();
        OMElement result = fac.createOMElement(
                new QName(DBConstants.EventNotification.MESSAGE_WRAPPER));
        OMElement serviceNameEl = fac.createOMElement(
                new QName(DBConstants.EventNotification.SERVICE_NAME));
        serviceNameEl.setText(dataService.getName());
        result.addChild(serviceNameEl);
        OMElement queryIdEl = fac.createOMElement(
                new QName(DBConstants.EventNotification.QUERY_ID));
        queryIdEl.setText(queryId);
        result.addChild(queryIdEl);
        OMElement timeEl = fac.createOMElement(
                new QName(DBConstants.EventNotification.TIME));
        timeEl.setText(Calendar.getInstance().getTime().toString());
        result.addChild(timeEl);
        OMElement contentEl = fac.createOMElement(
                new QName(DBConstants.EventNotification.CONTENT));
        contentEl.addChild(data);
        result.addChild(contentEl);
        /* clone required, or else the content in 'content' element is missing in result */
        result = result.cloneOMElement();
        OMDocument doc = fac.createOMDocument();
        doc.addChild(result);
        return doc.getOMDocumentElement();
    }

    /**
     * Executes the event trigger, this uses the "evaluate" method to check
     * if an event should be fired, if so, it sends out the event notification.
     */
    public void execute(OMElement input, String queryId) throws DataServiceFault {
        /* if evaluation succeeds, fire event .. */
        if (this.evaluate(input)) {
//            this.sendMessageToTopic(this.createEventMessage(this.getDataService(),
//            		queryId, input), this.getTargetTopic());
        }
    }

}
