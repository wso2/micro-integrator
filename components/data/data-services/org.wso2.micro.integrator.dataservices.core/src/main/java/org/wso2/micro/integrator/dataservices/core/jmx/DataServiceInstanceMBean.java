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
package org.wso2.micro.integrator.dataservices.core.jmx;

/**
 * JMX MBean interface to represent a data service.
 */
public interface DataServiceInstanceMBean {
	
	String getServiceName();
	
	String getDataServiceDescriptorPath();
	
	String[] getConfigIds();
	
	String[] getQueryIds();
	
	String[] getOperationNames();
	
	String[] getResourcePaths();
	
	String getQueryIdFromOperationName(String operationName);
	
	String getConfigIdFromQueryId(String queryId);
	
	String[] getHTTPMethodsForResourcePath(String resPath);
	
	boolean isConfigActive(String configId);
	
	String getConfigTypeFromId(String configId);
	
	boolean isDatabaseConnectionStatsAvailable(String configId);
	
	int getOpenDatabaseConnectionsCount(String configId);

}
