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
package org.wso2.micro.integrator.dataservices.core.tools;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.axiom.om.OMDocument;
import org.apache.axiom.om.OMElement;
import org.wso2.micro.integrator.dataservices.common.DBConstants;
import org.wso2.micro.integrator.dataservices.common.DBConstants.BoxcarringOps;
import org.wso2.micro.integrator.dataservices.core.DBUtils;
import org.wso2.micro.integrator.dataservices.core.DataServiceFactory;
import org.wso2.micro.integrator.dataservices.core.DataServiceFault;
import org.wso2.micro.integrator.dataservices.core.DataServiceUser;
import org.wso2.micro.integrator.dataservices.core.description.resource.Resource;
import org.wso2.micro.integrator.dataservices.core.dispatch.BatchDataServiceRequest;
import org.wso2.micro.integrator.dataservices.core.dispatch.BoxcarringDataServiceRequest;
import org.wso2.micro.integrator.dataservices.core.dispatch.DataServiceRequest;
import org.wso2.micro.integrator.dataservices.core.dispatch.SingleDataServiceRequest;
import org.wso2.micro.integrator.dataservices.core.engine.DataService;
import org.wso2.micro.integrator.dataservices.core.engine.DataServiceSerializer;
import org.wso2.micro.integrator.dataservices.core.engine.ParamValue;

/**
 * This class is used in creating and invoking data services locally, without running it as a server.
 */
public class DSTools {
	
	/**
	 * This method creates a DataService object, given the data services descriptor information.
	 * @param dbsElement The OMElement of the DBS file root element.
	 * @param dsLocation The location of the DBS file, this is purely used for informational 
	 * purposes and can be null.
	 * @return The initialized DataService object.
	 * @throws DataServiceFault The exception is thrown if the DataService cannot be initialized,
	 * i.e. authentication problems, cannot create database connections etc.
	 */
	public static DataService createDataService(OMElement dbsElement, String dsLocation)
            throws DataServiceFault {
		return DataServiceFactory.createDataService(dbsElement, dsLocation);
	}
	
	/**
	 * This method serializes the given DataService object to a data services configuration document 
	 * (.dbs XML file format).
	 * @param dataService The DataService object to be serialized.
	 * @return return an OMElement representing the root element of a data services configuration document.
	 */
	public static OMElement serializeDataService(DataService dataService) {
		return DataServiceSerializer.serializeDataService(dataService);
	}
	
	/**
	 * This method sets the current active data services user.
	 * @param user This represents the data service objects which contains the username and user roles.
	 */
	public static void setDataServicesUser(DataServiceUser user) {
		DataService.setCurrentUser(user);
	}
	
	/**
	 * This method begins a boxcarring session.
	 * @param dataService The DataService object which represents the data service
	 * @throws DataServiceFault thrown if an error condition occurs in executing the operation
	 */
	public static void beginBoxcar(DataService dataService) throws DataServiceFault {
		callBoxcarringOp(dataService, BoxcarringOps.BEGIN_BOXCAR,
				new HashMap<String, ParamValue>());
	}
	
	/**
	 * This method ends and commits a boxcarring session.
	 * @param dataService The DataService object which represents the data service
	 * @return returns the XML payload if there is any from the boxcarring session
	 * @throws DataServiceFault thrown if an error condition occurs in executing the operation
	 */
	public static OMElement endBoxcar(DataService dataService) throws DataServiceFault {
		return callBoxcarringOp(dataService, BoxcarringOps.END_BOXCAR,
				new HashMap<String, ParamValue>());
	}
	
	/**
	 * This method aborts the current boxcarring session
	 * @param dataService The DataService object which represents the data service
	 * @throws DataServiceFault thrown if an error condition occurs in executing the operation
	 */
	public static void abortBoxcar(DataService dataService) throws DataServiceFault {
		callBoxcarringOp(dataService, BoxcarringOps.ABORT_BOXCAR, 
				new HashMap<String, ParamValue>());
	}
	
	private static OMElement callBoxcarringOp(DataService dataService, String opName,
                                              Map<String, ParamValue> params) throws DataServiceFault {
		return (new BoxcarringDataServiceRequest(
				new SingleDataServiceRequest(dataService, opName,
                                             params))).dispatch();
	}
		
	/**
	 * This method invokes a single operation defined in the given data service.
	 * @param dataService The DataService object which represents the data service.
	 * @param operationName The name of the operation.
	 * @param params The parameters destined for the operation.
	 * @return returns the XML result if it exists.
	 * @throws DataServiceFault thrown if an error condition occurs in executing the operation.
	 * @see DSTools#invokeOperation(DataService, String, List)
	 */
	public static OMElement invokeOperation(DataService dataService,
                                            String operationName, Map<String, ParamValue> params)
            throws DataServiceFault {
		if (DataServiceRequest.isBoxcarringRequest(operationName)) {
			return callBoxcarringOp(dataService, operationName, params);
		}
		OMElement result = (new SingleDataServiceRequest(dataService, operationName,
                                                         params)).dispatch();
		if (result == null) {
			return null;
		}
		/* result must have a parent, or there are problems when it comes to XPath expressions etc.. */
		OMDocument doc = DBUtils.getOMFactory().createOMDocument();
		doc.addChild(result);
		return doc.getOMDocumentElement();
	}
	
	/**
	 * This method invokes the batch version of the operation defined in the given data service.
	 * @param dataService The DataService object which represents the data service.
	 * @param operationName The name of the operation.
	 * @param params The lists of parameters destined for the operation.
	 * @throws DataServiceFault thrown if an error condition occurs in executing the batch operation.
	 * @see DSTools#invokeOperation(DataService, String, Map)
	 */
	public static void invokeOperation(DataService dataService,
                                       String operationName, List<Map<String, ParamValue>> batchParams)
            throws DataServiceFault {
		(new BatchDataServiceRequest(dataService, operationName + DBConstants.BATCH_OPERATON_NAME_SUFFIX,
                                     batchParams)).dispatch();
	}

	/**
	 * This method accesses a resource defined in the data service.
	 * @param dataService The DataService object which represents the data service.
	 * @param resourcePath The resource path of the data service resource.
	 * @param params The parameters destined for the resource.
	 * @param accessMethod The HTTP access method defined in the data service resource.
	 * @return returns the XML result if it exists.
	 * @throws DataServiceFault thrown if an error condition occurs in accessing the resource.
	 */
	public static OMElement accessResource(DataService dataService,
                                           String resourcePath, Map<String, ParamValue> params,
                                           String accessMethod) throws DataServiceFault {
		OMElement result = (new SingleDataServiceRequest(dataService, Resource
				.generateRequestName(resourcePath, accessMethod),
                                                         params)).dispatch();
		OMDocument doc = DBUtils.getOMFactory().createOMDocument();
		doc.addChild(result);
		return doc.getOMDocumentElement();
	}

}
