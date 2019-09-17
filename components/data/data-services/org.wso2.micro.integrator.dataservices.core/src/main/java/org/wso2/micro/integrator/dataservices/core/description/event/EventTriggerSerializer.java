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

import javax.xml.namespace.QName;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.wso2.micro.integrator.dataservices.common.DBConstants.DBSFields;
import org.wso2.micro.integrator.dataservices.core.DBUtils;
import org.wso2.micro.integrator.dataservices.core.description.operation.Operation;

/**
 * This class represents the serializing functionality of an EventTrigger.
 * @see Operation
 */
public class EventTriggerSerializer {

	public static OMElement serializeEventTrigger(EventTrigger eventTrigger) {
		OMFactory fac = DBUtils.getOMFactory();
		if (eventTrigger instanceof XPathEventTrigger) {
			return serializeXPathEventTrigger((XPathEventTrigger) eventTrigger, fac);
		} else {
			return null;
		}
	}

	private static OMElement serializeXPathEventTrigger(XPathEventTrigger eventTrigger,
                                                        OMFactory fac) {
		OMElement eventTriggerEl = fac.createOMElement(new QName(DBSFields.EVENT_TRIGGER));
		/* set id */
		eventTriggerEl.addAttribute(DBSFields.ID, eventTrigger.getTriggerId(), null);
		String language = eventTrigger.getLanguage();
		/* set language */
		if (language != null) {
			eventTriggerEl.addAttribute(DBSFields.LANGUAGE, language, null);
		}
		/* set expression */
		OMElement exprEl = fac.createOMElement(new QName(DBSFields.EXPRESSION));
		exprEl.setText(eventTrigger.getExpression());
		eventTriggerEl.addChild(exprEl);
		/* set target topic */
		OMElement topicEl = fac.createOMElement(new QName(DBSFields.TARGET_TOPIC));
		topicEl.setText(eventTrigger.getTargetTopic());
		eventTriggerEl.addChild(topicEl);
		/* set subscriptions */
		OMElement subsEl = fac.createOMElement(new QName(DBSFields.SUBSCRIPTIONS));
		OMElement subEl;
		for (String epr : eventTrigger.getEndpointUrls()) {
			subEl = fac.createOMElement(new QName(DBSFields.SUBSCRIPTION));
			subEl.setText(epr);
			subsEl.addChild(subEl);
		}
		eventTriggerEl.addChild(subsEl);
		return eventTriggerEl;
	}
	
}
