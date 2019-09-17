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

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.micro.integrator.dataservices.common.DBConstants;
import org.wso2.micro.integrator.dataservices.common.DBConstants.DataSourceTypes;
import org.wso2.micro.integrator.dataservices.core.DBUtils;import org.wso2.micro.integrator.dataservices.core.DataServiceFault;import org.wso2.micro.integrator.dataservices.core.engine.DataService;import org.wso2.micro.integrator.dataservices.core.odata.ODataDataHandler;import org.wso2.micro.integrator.dataservices.core.odata.ODataServiceFault;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

/**
 * This class represents a RDF based data source configuration.
 */
public class RDFConfig extends Config {

	private static final Log log = LogFactory.getLog(ExcelConfig.class);
	
	private String rdfDataSourcePath;

	public RDFConfig(DataService dataService, String configId, Map<String, String> properties, boolean odataEnable) {
		super(dataService, configId, DataSourceTypes.RDF, properties, odataEnable);
		this.rdfDataSourcePath = this.getProperty(DBConstants.RDF.DATASOURCE).trim();
	}

	public Model createRDFModel() throws IOException, DataServiceFault {
		InputStream in = DBUtils.getInputStreamFromPath(this.getRDFDataSourcePath());
		Model model = ModelFactory.createMemModelMaker().createDefaultModel();
		model.read(in,null);		
		return model;
	}
	
	public String getRDFDataSourcePath() {
		return rdfDataSourcePath;
	}
	
	@Override
	public boolean isActive() {
		try {
			Model model = this. createRDFModel();
			return model != null;
		} catch (Exception e) {
			log.error("Error in checking RDF config availability", e);
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
