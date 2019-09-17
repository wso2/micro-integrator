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
package org.wso2.micro.integrator.ndatasource.core.services;

import org.wso2.micro.integrator.ndatasource.core.DataSourceInfo;
import org.wso2.micro.integrator.ndatasource.core.DataSourceStatus;

/**
 * This is a bean class to contain the DataSourceInfo in a web services context.
 */
public class WSDataSourceInfo {

	private DataSourceStatus dsStatus;
	
	private WSDataSourceMetaInfo dsMetaInfo;
	
	public WSDataSourceInfo() {
	}
	
	public WSDataSourceInfo(DataSourceInfo dsInfo) {
		this.dsStatus = dsInfo.getDsStatus();
		this.dsMetaInfo = new WSDataSourceMetaInfo(dsInfo.getDsMetaInfo());
	}
	
	public DataSourceInfo extractDataSourceInfo() {
		DataSourceInfo dsInfo = new DataSourceInfo(
				this.getDsMetaInfo().extractDataSourceMetaInfo(), dsStatus);
		return dsInfo;
	}

	public DataSourceStatus getDsStatus() {
		return dsStatus;
	}

	public void setDsStatus(DataSourceStatus dsStatus) {
		this.dsStatus = dsStatus;
	}

	public WSDataSourceMetaInfo getDsMetaInfo() {
		return dsMetaInfo;
	}

	public void setDsMetaInfo(WSDataSourceMetaInfo dsMetaInfo) {
		this.dsMetaInfo = dsMetaInfo;
	}
	
}
