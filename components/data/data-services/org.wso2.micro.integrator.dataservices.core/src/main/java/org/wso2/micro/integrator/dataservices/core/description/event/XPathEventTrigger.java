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

import java.util.List;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.xpath.AXIOMXPath;
import org.jaxen.JaxenException;
import org.wso2.micro.integrator.dataservices.common.DBConstants;
import org.wso2.micro.integrator.dataservices.core.DataServiceFault;import org.wso2.micro.integrator.dataservices.core.engine.DataService;

/**
 * This class represents an XPath language based event trigger.
 * @see EventTrigger
 */
public class XPathEventTrigger extends EventTrigger {
	
	private AXIOMXPath xPathExpr;
	
	public XPathEventTrigger(DataService dataService, String triggerId,
                             String expression, String targetTopic, List<String> endpointUrls)
            throws DataServiceFault {
		super(dataService, DBConstants.EventTriggerLanguages.XPATH,
				triggerId, expression, targetTopic, endpointUrls);
		try {
		    this.xPathExpr = new AXIOMXPath(this.getExpression());
		} catch (JaxenException e) {
			throw new DataServiceFault(e,
					"Error in building AXIOMPath instance with expression: " + 
					this.getExpression());
		}
	}
	
	private AXIOMXPath getXPathExpr() {
		return xPathExpr;
	}

	@Override
	public boolean evaluate(OMElement input) throws DataServiceFault {
		try {
			return this.getXPathExpr().booleanValueOf(input);
		} catch (JaxenException e) {
			throw new DataServiceFault(e, "Error in XPath evaluation with expression: "	+
					this.getExpression() + " input:" + input);
		}
	}
	
}
