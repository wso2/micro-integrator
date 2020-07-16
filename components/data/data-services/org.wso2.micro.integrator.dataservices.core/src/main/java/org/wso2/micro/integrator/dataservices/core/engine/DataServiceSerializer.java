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
package org.wso2.micro.integrator.dataservices.core.engine;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.wso2.micro.integrator.dataservices.common.DBConstants.DBSFields;
import org.wso2.micro.integrator.dataservices.core.DBUtils;
import org.wso2.micro.integrator.dataservices.core.description.config.Config;
import org.wso2.micro.integrator.dataservices.core.description.config.ConfigSerializer;
import org.wso2.micro.integrator.dataservices.core.description.event.EventTrigger;
import org.wso2.micro.integrator.dataservices.core.description.event.EventTriggerSerializer;
import org.wso2.micro.integrator.dataservices.core.description.operation.OperationSerializer;
import org.wso2.micro.integrator.dataservices.core.description.query.Query;
import org.wso2.micro.integrator.dataservices.core.description.query.QuerySerializer;
import org.wso2.micro.integrator.dataservices.core.description.resource.Resource.ResourceID;
import org.wso2.micro.integrator.dataservices.core.description.resource.ResourceSerializer;

import java.util.Set;
import javax.xml.namespace.QName;

/**
 * This class represents the serializing functionality of a DataService.
 * @see DataService
 */
public class DataServiceSerializer {

	public static OMElement serializeDataService(DataService dataService) {
		return serializeDataService(dataService, false);
	}

	public static OMElement serializeDataService(DataService dataService, boolean isUiSerialization) {
		OMFactory fac = DBUtils.getOMFactory();
		OMElement dataEl = fac.createOMElement(new QName("data"));
		/* set 'name' attribute */
		if (dataService.getName() != null) {
		    dataEl.addAttribute(DBSFields.NAME, dataService.getName(), null);
		}
		/* set 'namespace' attribute */
		if (dataService.getServiceNamespace() != null) {
		    dataEl.addAttribute(DBSFields.SERVICE_NAMESPACE, dataService.getServiceNamespace(), null);
		}
		/* set 'description' attribute */
		if (dataService.getDescription() != null) {
		    dataEl.addAttribute(DBSFields.DESCRIPTION, dataService.getDescription(), null);
		}
		/* set 'serviceStatus' attribute */
        if (dataService.getServiceStatus() != null) {
            dataEl.addAttribute(DBSFields.SERVICE_STATUS, dataService.getServiceStatus(), null);
        }
        
        /* set 'enableBatchRequests' attribute */
        dataEl.addAttribute(DBSFields.ENABLE_BATCH_REQUESTS,
        		Boolean.toString(dataService.isBatchRequestsEnabled()), null);
        /* set 'enableBoxcarring' attribute */
        dataEl.addAttribute(DBSFields.ENABLE_BOXCARRING,
        		Boolean.toString(dataService.isBoxcarringEnabled()), null);
        /* set 'disableLegacyBoxcarringMode' attribute */
        dataEl.addAttribute(DBSFields.DISABLE_LEGACY_BOXCARRING_MODE,
                            Boolean.toString(dataService.isDisableLegacyBoxcarringMode()), null);
        /* set 'transports' attribute */
        StringBuilder stringBuilder = new StringBuilder("");
		for (String transport : dataService.getTransports()) {
			stringBuilder.append(" ").append(transport);
		}
		dataEl.addAttribute(DBSFields.TRANSPORTS, stringBuilder.toString().trim(), null);

        /* add configs */
        for (Config config : dataService.getConfigs().values()) {
	        dataEl.addChild(ConfigSerializer.serializeConfig(config, isUiSerialization));
        }
        /* add event triggers */
        for (EventTrigger eventTrigger : dataService.getEventTriggers().values()) {
        	dataEl.addChild(EventTriggerSerializer.serializeEventTrigger(eventTrigger));
        }
        /* add queries */
        for (Query query : dataService.getQueries().values()) {
        	dataEl.addChild(QuerySerializer.serializeQuery(query));
        }
        /* add operations */
        for (String opName : dataService.getOperationNames()) {
        	dataEl.addChild(OperationSerializer.serializeOperation(dataService.getOperation(opName)));
        }
        /* add resources */
        Set<ResourceID> resourceIds = dataService.getResourceIds();
        for (ResourceID resourceId : resourceIds) {
        	dataEl.addChild(ResourceSerializer.serializeResource(
        	   		dataService.getResource(resourceId)));
        }
        fac.createOMDocument().addChild(dataEl);
		return dataEl;
	}

}
