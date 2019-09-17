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

import au.com.bytecode.opencsv.CSVReader;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.micro.integrator.dataservices.core.DataServiceFault;
import org.wso2.micro.integrator.dataservices.core.description.config.CSVConfig;
import org.wso2.micro.integrator.dataservices.core.description.event.EventTrigger;
import org.wso2.micro.integrator.dataservices.core.engine.DataEntry;
import org.wso2.micro.integrator.dataservices.core.engine.DataService;
import org.wso2.micro.integrator.dataservices.core.engine.InternalParamCollection;
import org.wso2.micro.integrator.dataservices.core.engine.ParamValue;
import org.wso2.micro.integrator.dataservices.core.engine.QueryParam;
import org.wso2.micro.integrator.dataservices.core.engine.Result;

import javax.xml.stream.XMLStreamWriter;
import java.util.List;
import java.util.Map;

/**
 * This class represents a CSV data services query.
 */
public class CSVQuery extends Query {

	private static final Log log = LogFactory.getLog(CSVQuery.class);
	
	private CSVConfig config;
	
	public CSVQuery(DataService dataService, String queryId,
                    List<QueryParam> queryParams, String configId, Result result,
                    EventTrigger inputEventTrigger, EventTrigger outputEventTrigger,
                    Map<String, String> advancedProperties, String inputNamespace)
            throws DataServiceFault {
		super(dataService, queryId, queryParams, result, configId, inputEventTrigger,
				outputEventTrigger, advancedProperties, inputNamespace);
		try {
		    this.config = (CSVConfig) this.getDataService().getConfig(this.getConfigId());
		} catch (ClassCastException e) {
			throw new DataServiceFault(e, "Configuration is not a CSV config:" +
					this.getConfigId());
		}
	}
	
	public CSVConfig getConfig() {
		return config;
	}
	
	public Object runPreQuery(InternalParamCollection params, int queryLevel)
            throws DataServiceFault {
		CSVReader reader = null;
		boolean isError = false;
		try {
            reader = this.getConfig().createCSVReader();
            return reader;
		} catch (Exception e) {
		    isError = true;
			throw new DataServiceFault(e, "Error in CSVQuery.runQuery.");
		} finally {
			if (reader != null && isError) {
				try {
				    reader.close();
				} catch (Exception e) {
					log.error("Error in closing CSV reader", e);
				}
			}
		}
	}

    @Override
    public void runPostQuery(Object result, XMLStreamWriter xmlWriter,
                             InternalParamCollection params, int queryLevel) throws DataServiceFault {
        CSVReader reader = null;
        boolean isError = false;
        try {
            reader = (CSVReader) result;
            String[] record;
            int maxCount = this.getConfig().getMaxRowCount();
            int i = 0;
            DataEntry dataEntry;
            Map<Integer, String> columnsMap = this.getConfig().getColumnMappings();
            boolean useColumnNumbers = this.isUsingColumnNumbers();
            while ((record = reader.readNext()) != null) {
                if (maxCount != -1 && i >= maxCount) {
                    break;
                }
                dataEntry = new DataEntry();
                for (int j = 0; j < record.length; j++) {
                    dataEntry.addValue(useColumnNumbers ? Integer.toString(j + 1) :
                            columnsMap.get(j + 1), new ParamValue(record[j]));
                }
                this.writeResultEntry(xmlWriter, dataEntry, params, queryLevel);
                i++;
            }
        } catch (Exception e) {
            isError = true;
            throw new DataServiceFault(e, "Error in CSVQuery.runQuery.");
        } finally {
            if (reader != null || isError) {
                try {
                    reader.close();
                } catch (Exception e) {
                    log.error("Error in closing CSV reader", e);
                }
            }
        }
    }

}
