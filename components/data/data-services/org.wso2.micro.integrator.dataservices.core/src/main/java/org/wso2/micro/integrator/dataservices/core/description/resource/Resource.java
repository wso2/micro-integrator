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
package org.wso2.micro.integrator.dataservices.core.description.resource;

import javax.xml.stream.XMLStreamWriter;

import org.wso2.micro.integrator.dataservices.core.DataServiceFault;
import org.wso2.micro.integrator.dataservices.core.engine.CallQuery;
import org.wso2.micro.integrator.dataservices.core.engine.CallableRequest;
import org.wso2.micro.integrator.dataservices.core.engine.DataService;
import org.wso2.micro.integrator.dataservices.core.engine.ExternalParamCollection;

/**
 * Represents a resource within a data service.
 */
public class Resource extends CallableRequest {

	private DataService dataService;
	
	private ResourceID resourceId;
	
	public Resource(DataService dataService, ResourceID resourceId, String description,
                    CallQuery callQuery, boolean batchRequest, Resource parentResource, boolean disableStreamingRequest,
                    boolean disableStreamingEffective) {
		super(generateRequestName(resourceId.getPath(), resourceId.getMethod()), description,
				callQuery, batchRequest, parentResource, disableStreamingRequest,
				disableStreamingEffective);
		this.dataService = dataService;
		this.resourceId = resourceId;
	}

	public static String generateRequestName(String path, String method) {
		String pathWithoutSlashes = path.replaceAll("/", "_");
		String pathWithoutLeftBraces = pathWithoutSlashes.replaceAll("\\{", "");
		String pathWithoutRightBraces = pathWithoutLeftBraces.replaceAll("\\}", "");
		String requestName = "_" + method + pathWithoutRightBraces;
		return requestName.toLowerCase();
	}
	
	public DataService getDataService() {
		return dataService;
	}

	public ResourceID getResourceId() {
		return resourceId;
	}
	
	public void execute(XMLStreamWriter xmlWriter, 
			ExternalParamCollection params)
            throws DataServiceFault {
		this.getCallQuery().execute(xmlWriter, params, 0, false);
	}
	
	/**
	 * Represents the identifier used to uniquely identify a resource.
	 */
	public static class ResourceID implements Comparable {

        private String path;
		
		private String method;
		
		private int hashCode;
		
		public ResourceID(String path, String method) {
			this.path = path;
			this.method = method;
			this.hashCode = generateRequestName(this.path, this.method).hashCode();
		}
		
		public String getPath() {
			return path;
		}
		
		public String getMethod() {
			return method;
		}
		
		@Override
		public int hashCode() {
			return hashCode;
		}
		
		@Override
		public boolean equals(Object rhs) {
			if (!(rhs instanceof ResourceID)) {
				return false;
			}
			return this.hashCode() == rhs.hashCode();
		}

        @Override
        public int compareTo(Object obj) {
            ResourceID resource = (ResourceID) obj;
            return generateRequestName(this.path, this.method).
                    compareTo(generateRequestName(resource.getPath(), resource.getMethod()));
        }
    }
}
