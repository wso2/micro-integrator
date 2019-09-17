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
package org.wso2.micro.integrator.dataservices.core.datasource;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.micro.integrator.dataservices.core.DataServiceFault;
import org.wso2.micro.integrator.dataservices.core.engine.InternalParam;

/**
 * A sample query based data source implementation which simply echos the parameters sent to it.
 */
public class EchoDataSource implements CustomQueryBasedDS {

	private static final Log log = LogFactory.getLog(
            EchoDataSource.class);
	
	@Override
	public void init(Map<String, String> props) throws DataServiceFault {
		if (log.isDebugEnabled()) {
		    log.debug("Echo DataSource Props:" + props);
		}
	}

	@Override
	public void close() {
		if (log.isDebugEnabled()) {
		    log.debug("Closing Echo DataSource");
		}
	}

	@Override
	public QueryResult executeQuery(String query, List<InternalParam> params)
            throws DataServiceFault {
		return new EchoQueryResult(query, params);
	}
	
	/**
	 * Sample query:-
	 * "column1,column2,column3;
	 *  R1C1 $1,R1C2 $2;
	 *  R2C1 $3,R2C2 $4"
	 */
	public class EchoQueryResult implements QueryResult {

		private String[] data;
		
		private int current = 1;
		
		private List<InternalParam> params;
		
		public EchoQueryResult(String query, List<InternalParam> params) {
			this.data = query.split(";");
			this.params = params;
		}
		
		@Override
		public List<DataColumn> getDataColumns() throws DataServiceFault {
			List<DataColumn> result = new ArrayList<DataColumn>();
			String[] columns = this.data[0].split(",");
			for (String column : columns) {
				result.add(new DataColumn(column));
			}
			return result;
		}

		@Override
		public boolean hasNext() throws DataServiceFault {
			return this.current < data.length;
		}

		@Override
		public DataRow next() throws DataServiceFault {
			List<DataColumn> dataColumns = this.getDataColumns();
			String[] cols = this.data[this.current].split(",");
			Map<String, String> rowData = new HashMap<String, String>();
			for (int i = 0; i < cols.length; i++) {
				for (int j = 0; j < this.params.size(); j++) {
					cols[i] = cols[i].replaceAll(":" + this.params.get(j).getName(), 
							this.params.get(j).getValue().getValueAsString());
				}
				rowData.put(dataColumns.get(i).getName(), cols[i]);
			}
			this.current++;
			return new FixedDataRow(rowData);
		}
		
	}

}
