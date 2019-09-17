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
package org.wso2.micro.integrator.dataservices.sql.driver.processor.reader;

import java.sql.SQLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

import org.wso2.micro.integrator.dataservices.sql.driver.parser.Constants;
import org.wso2.micro.integrator.dataservices.sql.driver.query.ColumnInfo;

/**
 * Fixed data table implementation.
 */
public class FixedDataTable extends DataTable {

    private Map<Integer, DataRow> rows;

    public FixedDataTable(String tableName, ColumnInfo[] headers) {
        super(tableName, headers);
        this.rows = Collections.synchronizedMap(new TreeMap<Integer, DataRow>());
    }

    @Override
    public Map<Integer, DataRow> getRows() {
        return rows;
    }

    public void setData(Map<Integer, DataRow> rows) {
        this.rows = rows;
    }

    @Override
    public void addRow(DataRow dataRow) {
        getRows().put(dataRow.getRowId(), dataRow);
    }
    
    private void handleEqualCondition(Map<Integer, DataRow> dataRows, int cellId, String value, 
    		String operator) {
    	DataRow tmpRow;
    	double tmpNumberLhs, tmpNumberRhs = 0;
    	boolean isNumber = true;
    	try {
    		tmpNumberRhs = Double.parseDouble(value);
    	} catch (NumberFormatException e) {
    		isNumber = false;
		}
    	for (Iterator<Map.Entry<Integer, DataRow>> itr = dataRows.entrySet().iterator(); 
    	            itr.hasNext();) {
			tmpRow = itr.next().getValue();
			try {
				if (isNumber) {
				    tmpNumberLhs = Double.parseDouble(tmpRow.getCell(
						    cellId).getCellValue().toString());
				    if (!(tmpNumberLhs == tmpNumberRhs)) {
					    itr.remove();
				    }
				    continue;
				}
			} catch (NumberFormatException e) {
				isNumber = false;
			}	
			if (!value.equals(tmpRow.getCell(cellId).getCellValue())) {
				itr.remove();
			}
		}
    }
    
    private void handleLessThanCondition(Map<Integer, DataRow> dataRows, int cellId, String value, 
    		String operator) {
    	double tmpNumberLhs, tmpNumberRhs;
    	try {
    		tmpNumberRhs = Double.parseDouble(value);
    	} catch (NumberFormatException e) {
			dataRows.clear();
			return;
		}
    	DataRow tmpRow;
		for (Iterator<Map.Entry<Integer, DataRow>> itr = dataRows.entrySet().iterator(); 
		            itr.hasNext();) {
			tmpRow = itr.next().getValue();
			try {
				tmpNumberLhs = Double.parseDouble(tmpRow.getCell(
						cellId).getCellValue().toString());
				if (!(tmpNumberLhs < tmpNumberRhs)) {
					itr.remove();
				}
			} catch (NumberFormatException e) {
				itr.remove();
			}
		}
    }
    
    private void handleGreaterThanCondition(Map<Integer, DataRow> dataRows, int cellId, String value, 
    		String operator) {
    	double tmpNumberLhs, tmpNumberRhs;
    	try {
    		tmpNumberRhs = Double.parseDouble(value);
    	} catch (NumberFormatException e) {
			dataRows.clear();
			return;
		}
    	DataRow tmpRow;
		for (Iterator<Map.Entry<Integer, DataRow>> itr = dataRows.entrySet().iterator(); 
		            itr.hasNext();) {
			tmpRow = itr.next().getValue();
			try {
				tmpNumberLhs = Double.parseDouble(tmpRow.getCell(
						cellId).getCellValue().toString());
				if (!(tmpNumberLhs > tmpNumberRhs)) {
					itr.remove();
				}
			} catch (NumberFormatException e) {
				itr.remove();
			}
		}
    }
    
	@Override
	public Map<Integer, DataRow> applyCondition(String column,
			String value, String operator) throws SQLException {
		Map<Integer, DataRow> dataRows = new HashMap<Integer, DataRow>(this.getRows());
		int cellId = this.getHeader(column).getId();
		if (Constants.EQUAL.equals(operator)) {
			this.handleEqualCondition(dataRows, cellId, value, operator);
		} else if (Constants.GREATER_THAN.equals(operator)) {
			this.handleGreaterThanCondition(dataRows, cellId, value, operator);
		} else if (Constants.LESS_THAN.equals(operator)) {
			this.handleLessThanCondition(dataRows, cellId, value, operator);
		} else {
			throw new RuntimeException("Unsupported operator: " + operator);
		}
		return dataRows;
	}

	@Override
	public void updateRows(DataRow... dataRows) {
		for (DataRow dataRow : dataRows) {
			this.getRows().put(dataRow.getRowId(), dataRow);
		}
	}

	@Override
	public void deleteRows(int... rowIds) {
		for (int rowId : rowIds) {
			this.getRows().remove(rowId);
		}
	}

}
