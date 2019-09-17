/**
 *  Copyright (c) 2012, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.wso2.micro.integrator.ndatasource.core;

/**
 * This class represents the full Carbon data source, including its meta-data,
 * status and the actual data source object.
 */
public class CarbonDataSource {

	private DataSourceMetaInfo dsmInfo;
	
	private DataSourceStatus dsStatus;
	
	private Object dsObject;
	
	public CarbonDataSource(DataSourceMetaInfo dsmInfo,
                            DataSourceStatus dsStatus, Object dsObject) {
		this.dsmInfo = dsmInfo;
		this.dsStatus = dsStatus;
		this.dsObject = dsObject;
	}

	public DataSourceMetaInfo getDSMInfo() {
		return dsmInfo;
	}

	public DataSourceStatus getDSStatus() {
		return dsStatus;
	}

	public Object getDSObject() {
		return dsObject;
	}

}
