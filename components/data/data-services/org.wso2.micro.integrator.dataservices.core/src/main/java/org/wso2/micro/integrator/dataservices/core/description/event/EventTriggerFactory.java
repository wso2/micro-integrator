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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.xml.namespace.QName;

import org.apache.axiom.om.OMElement;
import org.wso2.micro.integrator.dataservices.common.DBConstants.DBSFields;
import org.wso2.micro.integrator.dataservices.common.DBConstants.EventTriggerLanguages;
import org.wso2.micro.integrator.dataservices.core.DataServiceFault;
import org.wso2.micro.integrator.dataservices.core.engine.DataService;

/**
 * This class creates the EventTrigger objects by passing the 
 * relevant event-trigger sections in the dbs file.
 */
public class EventTriggerFactory {

	private EventTriggerFactory() { }
	
	public static EventTrigger createEventTrigger(DataService dataService,
                                                  OMElement eventEl) throws DataServiceFault {
		String language = eventEl.getAttributeValue(new QName(DBSFields.LANGUAGE));
		/* default language is 'XPath' */
		if (language == null || language.equals(EventTriggerLanguages.XPATH)) {
			return createXPathEventTrigger(dataService, eventEl);
		} else {
			return null;
		}
	}
	
	@SuppressWarnings("unchecked")
	private static XPathEventTrigger createXPathEventTrigger(DataService dataService,
                                                             OMElement xpathEventEl) throws DataServiceFault {
		try {
		    String id = xpathEventEl.getAttributeValue(new QName(DBSFields.ID));
		    String expression = xpathEventEl.getFirstChildWithName(
		    		new QName(DBSFields.EXPRESSION)).getText();
		    String targetTopic = xpathEventEl.getFirstChildWithName(
		    		new QName(DBSFields.TARGET_TOPIC)).getText();
		    OMElement subsEl = xpathEventEl.getFirstChildWithName(
		    		new QName(DBSFields.SUBSCRIPTIONS));
		    OMElement subEl;
		    Iterator<OMElement> subElItr = subsEl.getChildrenWithName(
		    		new QName(DBSFields.SUBSCRIPTION));
		    List<String> endpointUrls = new ArrayList<String>();
		    while (subElItr.hasNext()) {
		    	subEl = subElItr.next();
		    	endpointUrls.add(subEl.getText());
		    }
		    return new XPathEventTrigger(dataService, id, expression, targetTopic, endpointUrls);
		} catch (Exception e) {
			throw new DataServiceFault(e, "Error in create XPathEventTrigger");
		}
	}
	
}
