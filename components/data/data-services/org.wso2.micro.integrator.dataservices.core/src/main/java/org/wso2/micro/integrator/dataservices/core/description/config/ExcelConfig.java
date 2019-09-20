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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.wso2.micro.integrator.dataservices.common.DBConstants;
import org.wso2.micro.integrator.dataservices.common.DBConstants.DataSourceTypes;
import org.wso2.micro.integrator.dataservices.core.DBUtils;
import org.wso2.micro.integrator.dataservices.core.DataServiceFault;
import org.wso2.micro.integrator.dataservices.core.engine.DataService;
import org.wso2.micro.integrator.dataservices.core.odata.ODataDataHandler;
import org.wso2.micro.integrator.dataservices.core.odata.ODataServiceFault;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

/**
 * This class represents a Excel based data source configuration.
 */
public class ExcelConfig extends Config {

	private static final Log log = LogFactory.getLog(
            ExcelConfig.class);
	
	private String excelDataSourcePath;

	public ExcelConfig(DataService dataService, String configId, Map<String, String> properties, boolean odataEnable) {
		super(dataService, configId, DataSourceTypes.EXCEL, properties, odataEnable);
		this.excelDataSourcePath = this.getProperty(DBConstants.Excel.DATASOURCE).trim();
	}

	public String getExcelDataSourcePath() {
		return excelDataSourcePath;
	}
	
	public Workbook createWorkbook() throws IOException, InvalidFormatException, DataServiceFault {
		InputStream ins = DBUtils.getInputStreamFromPath(this.getExcelDataSourcePath());
		Workbook wb = WorkbookFactory.create(ins);
		return wb;
	}
	
	@Override
	public boolean isActive() {
		try {
			Workbook wb = this.createWorkbook();
			return wb != null;
		} catch (Exception e) {
			log.error("Error in checking Excel config availability", e);
			return false;
		}
	}
	
	public void close() {
		/* nothing to close */
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
