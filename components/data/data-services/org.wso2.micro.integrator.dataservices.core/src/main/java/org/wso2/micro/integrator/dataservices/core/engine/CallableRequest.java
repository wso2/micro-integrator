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

import javax.xml.stream.XMLStreamWriter;

import org.wso2.micro.integrator.dataservices.core.DataServiceFault;

/**
 * This class represents an entity which can be called to carry out a specific execution 
 * of a request, which can be invoking a data service operation or accessing a resource. 
 */
public abstract class CallableRequest {

	private String requestName;
	
	private String description;
	
	private CallQuery callQuery;

	private boolean batchRequest;

	private boolean disableStreamingRequest;

	private boolean disableStreamingEffective;

	/**
	 * A flag to check whether the operation should return a message saying
	 * if the request was a success or not, this can be used for in-only operations
	 * which does not have a result defined
	 */
	private boolean returnRequestStatus;

	private CallableRequest parentRequest;

	/**
	 * Creates a callable request with the given request name and a call query.
	 */
	public CallableRequest(String requestName, String description, CallQuery callQuery,
                           boolean batchRequest, CallableRequest parentRequest, boolean disableStreamingRequest,
                           boolean disableStreamingEffective) {
		this.requestName = requestName;
		this.description = description;
		this.callQuery = callQuery;
		this.batchRequest = batchRequest;
		this.parentRequest = parentRequest;
		this.disableStreamingRequest = disableStreamingRequest;
		this.disableStreamingEffective = disableStreamingEffective;
	}
	
	/**
     * This method returns the parent request, provided that this is a batch request.
     */
    public CallableRequest getParentRequest() {
        return parentRequest;
    }
	
	public boolean isReturnRequestStatus() {
		return returnRequestStatus;
	}

	public void setReturnRequestStatus(boolean returnRequestStatus) {
		this.returnRequestStatus = returnRequestStatus;
	}
	
	public boolean isDisableStreamingRequest() {
		return disableStreamingRequest;
	}

	public boolean isDisableStreamingEffective() {
		return disableStreamingEffective;
	}

	public String getRequestName() {
		return requestName;
	}
	
	public String getDescription() {
		return description;
	}
	
	public CallQuery getCallQuery() {
		return callQuery;
	}
	
	public boolean isBatchRequest() {
		return batchRequest;
	}
		
	/**
	 * This method must be implemented in concrete classes to define the semantics of the request.
	 */
	public abstract void execute(XMLStreamWriter xmlWriter, 
			ExternalParamCollection params)
            throws DataServiceFault;
	
}
