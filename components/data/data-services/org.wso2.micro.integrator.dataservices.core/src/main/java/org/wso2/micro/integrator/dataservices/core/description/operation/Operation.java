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
package org.wso2.micro.integrator.dataservices.core.description.operation;

import javax.xml.stream.XMLStreamWriter;

import org.wso2.micro.integrator.dataservices.core.DataServiceFault;import org.wso2.micro.integrator.dataservices.core.engine.CallQuery;import org.wso2.micro.integrator.dataservices.core.engine.CallableRequest;import org.wso2.micro.integrator.dataservices.core.engine.DataService;import org.wso2.micro.integrator.dataservices.core.engine.ExternalParamCollection;

/**
 * Represents an operation within a data service.
 */
public class Operation extends CallableRequest {

	private DataService dataService;
	
	private String name;

	public Operation(DataService dataService, String name, String description,
			CallQuery callQuery, boolean batchRequest, Operation parentOperation,
			boolean disableStreamingRequest, boolean disableStreamingEffective) {
		super(name, description, callQuery, batchRequest, parentOperation,
		        disableStreamingRequest, disableStreamingEffective);
		this.dataService = dataService;
		this.name = name;
	}

	public DataService getDataService() {
		return dataService;
	}

	public String getName() {
		return name;
	}
	
	/**
	 * This executes the operation, by retrieving the call query group associated with it,
	 * and executing the query group.
	 */
	public void execute(XMLStreamWriter xmlWriter, ExternalParamCollection params)
			throws DataServiceFault {
		this.getCallQuery().execute(xmlWriter, params, 0, false);
	}
	
}
