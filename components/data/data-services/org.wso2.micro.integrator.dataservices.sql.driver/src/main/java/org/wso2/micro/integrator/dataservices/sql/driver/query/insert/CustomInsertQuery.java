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
package org.wso2.micro.integrator.dataservices.sql.driver.query.insert;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

import org.wso2.micro.integrator.dataservices.sql.driver.TCustomConnection;
import org.wso2.micro.integrator.dataservices.sql.driver.processor.reader.DataCell;
import org.wso2.micro.integrator.dataservices.sql.driver.processor.reader.DataRow;
import org.wso2.micro.integrator.dataservices.sql.driver.processor.reader.DataTable;
import org.wso2.micro.integrator.dataservices.sql.driver.query.ParamInfo;

/**
 * This class represents a select query for custom data sources.
 */
public class CustomInsertQuery extends InsertQuery {

	public CustomInsertQuery(Statement stmt) throws SQLException {
		super(stmt);
	}

	@Override
	public ResultSet executeQuery() throws SQLException {
		this.executeUpdate();
		return null;
	}

	@Override
	public int executeUpdate() throws SQLException {
		if (!(this.getConnection() instanceof TCustomConnection)) {
            throw new SQLException("Connection does not refer to a Custom connection");
        }
		DataTable table = ((TCustomConnection) this.getConnection()).getDataSource().getDataTable(
				this.getTargetTableName());
		if (table == null) {
			throw new SQLException("The custom data table '" + 
					this.getTargetTableName() + "' does not exist");
		}
		DataRow row = new DataRow(0);
		Map<Integer, DataCell> cells = new HashMap<Integer, DataCell>();
		for (ParamInfo param : getParameters()) {
			cells.put(param.getOrdinal(), new DataCell(param.getOrdinal(), param.getSqlType(),
					param.getValue()));	
		}
		row.setCells(cells);
		table.addRow(row);
		return 1;
	}

	@Override
	public boolean execute() throws SQLException {
		this.executeUpdate();
		return true;
	}

}
