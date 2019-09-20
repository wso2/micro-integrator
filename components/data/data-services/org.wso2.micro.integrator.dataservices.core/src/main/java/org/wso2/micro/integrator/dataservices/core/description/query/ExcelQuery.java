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

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.wso2.micro.integrator.dataservices.core.DBUtils;
import org.wso2.micro.integrator.dataservices.core.DataServiceFault;
import org.wso2.micro.integrator.dataservices.core.description.config.ExcelConfig;
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
 * This class represents a Excel data services query.
 */
public class ExcelQuery extends Query {

    private ExcelConfig config;

    private String workbookName;

    private boolean hasHeader;

    private int startingRow;

    private int headerRow;

    private int maxRowCount;

    private Map<Integer, String> columnMappings;

    public ExcelQuery(DataService dataService, String queryId,
                      List<QueryParam> queryParams, String configId, String workbookName,
                      boolean hasHeader, int startingRow, int headerRow, int maxRowCount, Result result,
                      EventTrigger inputEventTrigger, EventTrigger outputEventTrigger,
                      Map<String, String> advancedProperties, String inputNamespace)
            throws DataServiceFault {
        super(dataService, queryId, queryParams, result, configId, inputEventTrigger,
                outputEventTrigger, advancedProperties, inputNamespace);
        try {
            this.config = (ExcelConfig) this.getDataService().getConfig(this.getConfigId());
        } catch (ClassCastException e) {
            throw new DataServiceFault(e, "Configuration is not an Excel config:" +
                    this.getConfigId());
        }
        this.workbookName = workbookName;
        this.hasHeader = hasHeader;
        this.startingRow = startingRow;
        this.headerRow = headerRow;
        this.maxRowCount = maxRowCount;
        
        try {
            this.columnMappings = DBUtils.createColumnMappings(this.getHeader());
        } catch (Exception e) {
            throw new DataServiceFault(e, "Error in creating Excel column mappings.");
        }
    }

    public Map<Integer, String> getColumnMappings() {
        return columnMappings;
    }

    private String[] getHeader() throws Exception {
        if (!this.isHasHeader()) {
            return null;
        }
        Workbook wb = this.getConfig().createWorkbook();
        Sheet sheet = wb.getSheet(this.getWorkbookName());
        return this.extractRowData(sheet.getRow(this.getHeaderRow() - 1));
    }

    private String[] extractRowData(Row row) {
        if (row == null || row.getLastCellNum() == -1) {
            return null;
        }
        String[] data = new String[row.getLastCellNum()];
        Cell cell;
        for (int i = 0; i < data.length; i++) {
            cell = row.getCell(i);
            if (cell == null) {
                data[i] = "";
                continue;
            }
            switch (cell.getCellType()) {
                case HSSFCell.CELL_TYPE_STRING:
                    data[i] = cell.getRichStringCellValue().getString();
                    break;
                case HSSFCell.CELL_TYPE_BLANK:
                    data[i] = "";
                    break;
                case HSSFCell.CELL_TYPE_BOOLEAN:
                    data[i] = String.valueOf(cell.getBooleanCellValue());
                    break;
                case HSSFCell.CELL_TYPE_FORMULA:
                    data[i] = "{formula}";
                    break;
                case HSSFCell.CELL_TYPE_NUMERIC:
                    data[i] = processNumericValue(cell.getNumericCellValue());
                    break;
            }
        }
        return data;
    }

    private String processNumericValue(double val) {
        if (val == (long) val) {
            return String.valueOf((long) val);
        } else {
            return String.valueOf(val);
        }
    }

    public ExcelConfig getConfig() {
        return config;
    }

    public boolean isHasHeader() {
        return hasHeader;
    }

    public int getMaxRowCount() {
        return maxRowCount;
    }

    public int getStartingRow() {
        return startingRow;
    }

    public int getHeaderRow() {
        return headerRow;
    }

    public String getWorkbookName() {
        return workbookName;
    }

    public Object runPreQuery(InternalParamCollection params, int queryLevel)
            throws DataServiceFault {
        try {
            Workbook wb = this.getConfig().createWorkbook();
            return wb.getSheet(this.getWorkbookName());
        } catch (Exception e) {
            throw new DataServiceFault(e, "Error in ExcelQuery.runQuery.");
        }
    }

    @Override
    public void runPostQuery(Object result, XMLStreamWriter xmlWriter,
                             InternalParamCollection params, int queryLevel) throws DataServiceFault {
        Sheet sheet = (Sheet) result;
        int maxCount = this.getMaxRowCount();
        int i = this.getStartingRow() - 1;
        int count = 0;
        DataEntry dataEntry;
        String[] record;
        Map<Integer, String> columnsMap = this.getColumnMappings();
        boolean useColumnNumbers = this.isUsingColumnNumbers();
        while ((record = this.extractRowData(sheet.getRow(i))) != null) {
            if (maxCount != -1 && count >= maxCount) {
                break;
            }
            dataEntry = new DataEntry();
            for (int j = 0; j < record.length; j++) {
                dataEntry.addValue(useColumnNumbers ? Integer.toString(j + 1) :
                        columnsMap.get(j + 1), new ParamValue(record[j]));
            }
            this.writeResultEntry(xmlWriter, dataEntry, params, queryLevel);
            i++;
            count++;

        }
    }
}
