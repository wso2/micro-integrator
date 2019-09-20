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

import java.util.ArrayList;
import java.util.List;

import org.wso2.micro.core.AbstractAdmin;
import org.wso2.micro.integrator.ndatasource.common.DataSourceException;
import org.wso2.micro.integrator.ndatasource.core.CarbonDataSource;
import org.wso2.micro.integrator.ndatasource.core.DataSourceInfo;
import org.wso2.micro.integrator.ndatasource.core.DataSourceManager;

/**
 * This class represents the admin service class for data sources.
 */
public class NDataSourceAdminService extends AbstractAdmin {

	public WSDataSourceInfo[] getAllDataSources() throws DataSourceException {
		List<WSDataSourceInfo> result = new ArrayList<WSDataSourceInfo>();
		for (CarbonDataSource cds : DataSourceManager.getInstance().
				getDataSourceRepository().getAllDataSources()) {
			result.add(new WSDataSourceInfo(new DataSourceInfo(
					cds.getDSMInfo(), cds.getDSStatus())));
		}
		return result.toArray(new WSDataSourceInfo[result.size()]);
	}
	
	public WSDataSourceInfo getDataSource(String dsName) throws DataSourceException {
		CarbonDataSource cds = DataSourceManager.getInstance().
				getDataSourceRepository().getDataSource(dsName);
		if (cds == null) {
			return null;
		}
		return new WSDataSourceInfo(new DataSourceInfo(cds.getDSMInfo(), cds.getDSStatus()));
	}
	
	public WSDataSourceInfo[] getAllDataSourcesForType(String dsType) throws DataSourceException {
		List<WSDataSourceInfo> result = new ArrayList<WSDataSourceInfo>();
		for (CarbonDataSource cds : DataSourceManager.getInstance().
				getDataSourceRepository().getAllDataSources()) {
			if (dsType.equals(cds.getDSMInfo().getDefinition().getType())) {
			    result.add(new WSDataSourceInfo(
			    		new DataSourceInfo(cds.getDSMInfo(), cds.getDSStatus())));
			}
		}
		return result.toArray(new WSDataSourceInfo[0]);
	}
	
	public String[] getDataSourceTypes() throws DataSourceException {
		return DataSourceManager.getInstance().getDataSourceTypes().toArray(new String[0]);
	}
	
	public boolean reloadAllDataSources() throws DataSourceException {
		DataSourceManager.getInstance().getDataSourceRepository().refreshAllUserDataSources();
		return true;
	}
	
	public boolean reloadDataSource(String dsName) throws DataSourceException {
		DataSourceManager.getInstance().getDataSourceRepository().refreshUserDataSource(dsName);
		return true;
	}
	
	public boolean addDataSource(WSDataSourceMetaInfo dsmInfo) throws DataSourceException {
		DataSourceManager.getInstance().getDataSourceRepository().addDataSource(
				dsmInfo.extractDataSourceMetaInfo());
		return true;
	}
	
	public boolean deleteDataSource(String dsName) throws DataSourceException {
		DataSourceManager.getInstance().getDataSourceRepository().deleteDataSource(dsName);
		return true;
	}
	
	public boolean testDataSourceConnection(WSDataSourceMetaInfo dsmInfo) throws DataSourceException {
		return DataSourceManager.getInstance().getDataSourceRepository().
				testDataSourceConnection(dsmInfo.extractDataSourceMetaInfo());
	}
	
}
