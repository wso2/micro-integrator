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
package org.wso2.micro.integrator.dataservices.core.description.config;

import org.apache.axiom.om.util.AXIOMUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.micro.integrator.dataservices.common.DBConstants;
import org.wso2.micro.integrator.dataservices.common.DBConstants.DataSourceTypes;
import org.wso2.micro.integrator.dataservices.core.DBUtils;
import org.wso2.micro.integrator.dataservices.core.DataServiceFault;
import org.wso2.micro.integrator.dataservices.core.datasource.CustomQueryBasedDS;
import org.wso2.micro.integrator.dataservices.core.engine.DataService;
import org.wso2.micro.integrator.dataservices.core.odata.ODataDataHandler;
import org.wso2.micro.integrator.dataservices.core.odata.ODataServiceFault;

import java.util.HashMap;
import java.util.Map;

/**
 * This class represents a data services custom query based in-line data source configuration.
 */
public class InlineCustomQueryBasedDSConfig extends CustomQueryBasedDSConfig {

	private static final Log log = LogFactory.getLog(
            InlineCustomQueryBasedDSConfig.class);
	
	private CustomQueryBasedDS dataSource;

	public InlineCustomQueryBasedDSConfig(DataService dataService, String configId, Map<String, String> properties,
                                          boolean odataEnable) throws DataServiceFault {
		super(dataService, configId, DataSourceTypes.CUSTOM_QUERY, properties, odataEnable);
		String dsClass = properties.get(DBConstants.CustomDataSource.DATA_SOURCE_QUERY_CLASS);
		try {
			this.dataSource = (CustomQueryBasedDS) Class.forName(dsClass).newInstance();
			String dataSourcePropsString = properties.get(
					DBConstants.CustomDataSource.DATA_SOURCE_PROPS);
			Map<String, String> dsProps;
			if (dataSourcePropsString != null) {
				dsProps = DBUtils.extractProperties(AXIOMUtil.stringToOM(
						dataSourcePropsString));
			} else {
				dsProps = new HashMap<String, String>();
			}
			DBUtils.populateStandardCustomDSProps(dsProps, this.getDataService(), this);
			this.dataSource.init(dsProps);
			if (log.isDebugEnabled()) {
				log.debug("Creating custom data source with info: #" + 
						this.getDataService().getTenantId() + "#" + 
						this.getDataService() + "#" + this.getConfigId());
			}
		} catch (Exception e) {
			throw new DataServiceFault(e, "Error in creating custom data source config: " +
					e.getMessage());
		}
	}
	
	public CustomQueryBasedDS getDataSource() {
		return dataSource;
	}

	@Override
	public boolean isActive() {
		return true;
	}

	@Override
	public ODataDataHandler createODataHandler() throws ODataServiceFault {
		throw new ODataServiceFault("Expose as OData Service feature doesn't support for the " + getConfigId() +
		                           " Datasource.");
	}

	@Override
	public boolean isResultSetFieldsCaseSensitive() {
		return false;
	}
}
