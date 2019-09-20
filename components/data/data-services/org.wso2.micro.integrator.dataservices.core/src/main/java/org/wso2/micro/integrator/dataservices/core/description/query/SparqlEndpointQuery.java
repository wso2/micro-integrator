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
package org.wso2.micro.integrator.dataservices.core.description.query;

import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.ResultSet;
import org.wso2.micro.integrator.dataservices.core.DataServiceFault;import org.wso2.micro.integrator.dataservices.core.description.config.SparqlEndpointConfig;import org.wso2.micro.integrator.dataservices.core.description.event.EventTrigger;import org.wso2.micro.integrator.dataservices.core.engine.DataEntry;import org.wso2.micro.integrator.dataservices.core.engine.DataService;import org.wso2.micro.integrator.dataservices.core.engine.InternalParamCollection;import org.wso2.micro.integrator.dataservices.core.engine.QueryParam;import org.wso2.micro.integrator.dataservices.core.engine.Result;

import javax.xml.stream.XMLStreamWriter;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * This class represents a SPARQL data services query made to an arbitrary SPARQL endpoint via HTTP
 */
public class SparqlEndpointQuery extends SparqlQueryBase {

	private SparqlEndpointConfig config;
	
	public SparqlEndpointQuery(DataService dataService, String queryId,
                               String configId, String query, List<QueryParam> queryParams,
                               Result result, EventTrigger inputEventTrigger,
                               EventTrigger outputEventTrigger,
                               Map<String, String> advancedProperties,
                               String inputNamespace) throws DataServiceFault {
		super(dataService, queryId, configId, query, queryParams, result,
				inputEventTrigger, outputEventTrigger, advancedProperties,
				inputNamespace);
		try {
			this.config = (SparqlEndpointConfig) this.getDataService().getConfig(
					this.getConfigId());
		} catch (ClassCastException e) {
			throw new DataServiceFault(e, "Configuration is not a SPARQL Endpoint config:"
					+ this.getConfigId());
		}

	}	

	public SparqlEndpointConfig getConfig() {
		return config;
	}

	@Override
	public QueryExecution getQueryExecution() throws IOException, DataServiceFault {
		return QueryExecutionFactory.sparqlService(this.getConfig().getSparqlEndpoint(), this.getQuery());
	}

	@Override
    public Object processPreQuery(InternalParamCollection params, int queryLevel) throws DataServiceFault {
		try {
		    ResultSet results = null;
    		    QueryExecution qe = this.getQueryExecution();
    		    /* execute query as a select query */
    		    results = qe.execSelect();
    		    return results;
		} catch (Exception e) {
			throw new DataServiceFault(e, "Error in 'SparqlQueryBase.processQuery'");
		}
	}

    @Override
    public void processPostQuery(Object result, XMLStreamWriter xmlWriter,
                                 InternalParamCollection params, int queryLevel) throws DataServiceFault {
        ResultSet results = (ResultSet) result;
        DataEntry dataEntry;
        while (results != null && results.hasNext()) {
            dataEntry = this.getDataEntryFromRS(results);
            this.writeResultEntry(xmlWriter, dataEntry, params, queryLevel);
        }
    }
}
