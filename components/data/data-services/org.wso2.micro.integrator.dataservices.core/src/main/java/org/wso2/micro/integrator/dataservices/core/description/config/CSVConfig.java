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

import au.com.bytecode.opencsv.CSVReader;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.micro.integrator.dataservices.common.DBConstants;
import org.wso2.micro.integrator.dataservices.common.DBConstants.DataSourceTypes;
import org.wso2.micro.integrator.dataservices.core.DBUtils;
import org.wso2.micro.integrator.dataservices.core.DataServiceFault;
import org.wso2.micro.integrator.dataservices.core.engine.DataService;
import org.wso2.micro.integrator.dataservices.core.odata.ODataDataHandler;
import org.wso2.micro.integrator.dataservices.core.odata.ODataServiceFault;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Map;

/**
 * This class represents a CSV based data source configuration.
 */
public class CSVConfig extends Config {

    private static final Log log = LogFactory.getLog(
            CSVConfig.class);

    public static final char DEFAULT_QUOTE_CHAR = '"';

    private String csvDataSourcePath;


    private char columnSeparator;

    private int startingRow;

    private int headerRow;

    private int maxRowCount;

    private boolean hasHeader;

    private Map<Integer, String> columnMappings;

    public CSVConfig(DataService dataService, String configId, Map<String, String> properties, boolean odataEnable)
            throws DataServiceFault {
        super(dataService, configId, DataSourceTypes.CSV, properties, odataEnable);

        this.csvDataSourcePath = this.getProperty(DBConstants.CSV.DATASOURCE);
        String columnSeparatorStr = this.getProperty(DBConstants.CSV.COLUMN_SEPARATOR);
        this.columnSeparator = extractColumnSeparator(columnSeparatorStr);

        String tmpStartingRow = this.getProperty(DBConstants.CSV.STARTING_ROW);
        if (tmpStartingRow != null) {
            this.startingRow = Integer.parseInt(tmpStartingRow);
        } else {
            this.startingRow = 1;
        }

        String tmpMaxRowCount = this.getProperty(DBConstants.CSV.MAX_ROW_COUNT);
        if (tmpMaxRowCount != null) {
            this.maxRowCount = Integer.parseInt(tmpMaxRowCount);
        } else {
            this.maxRowCount = -1;
        }

        String tmpHasHeader = this.getProperty(DBConstants.CSV.HAS_HEADER);
        if (tmpHasHeader != null) {
            this.hasHeader = Boolean.parseBoolean(tmpHasHeader);
        } else {
            this.hasHeader = false;
        }

        String tmpHeaderRow = this.getProperty(DBConstants.CSV.HEADER_ROW);
        if (tmpHeaderRow != null) {
            this.headerRow = Integer.parseInt(tmpHeaderRow);
        } else {
            this.headerRow = 1;
        }
        
        try {
            this.columnMappings = DBUtils.createColumnMappings(this.getHeader());
        } catch (IOException e) {
            throw new DataServiceFault("Error in creating CSV column mappings.");
        }
    }

    private char extractColumnSeparator(String value) {
        if (DBUtils.isEmptyString(value)) {
            return ',';
        }
        value = value.trim();
        if (value.equals("\\t")) { // tab
            return '\t';
        }
        if (value.equals("\\s")) { // space
            return ' ';
        }
        if (value.startsWith("\\u")) {
            try {
                return (char) Integer.parseInt(value.substring(2), 16);
            } catch (NumberFormatException e) {
                // ignore
                return ',';
            }
        }
        if (value.length() > 1) {
            return ',';
        } else {
            return value.charAt(0);
        }
    }

    public CSVReader createCSVReader() throws IOException, DataServiceFault {
        return this.createCSVReader(this.getStartingRow() - 1);
    }

    private CSVReader createCSVReader(int skipLineNo)
            throws IOException, DataServiceFault {
        InputStream ins = DBUtils.getInputStreamFromPath(
                this.getCsvDataSourcePath());
        InputStreamReader insr = new InputStreamReader(ins);
        return new CSVReader(insr, this.getColumnSeparator(),
                             CSVConfig.DEFAULT_QUOTE_CHAR, skipLineNo);
    }

    private String[] getHeader() throws IOException, DataServiceFault {
        if (!this.isHasHeader()) {
            return null;
        }

        CSVReader reader = null;
        try {
            reader = this.createCSVReader(this.getHeaderRow() - 1);
            return reader.readNext();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    log.error("Error in closing CSV reader", e);
                }
            }
        }
    }

    public char getColumnSeparator() {
        return columnSeparator;
    }

    public String getCsvDataSourcePath() {
        return csvDataSourcePath;
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

    public Map<Integer, String> getColumnMappings() {
        return columnMappings;
    }

    @Override
    public boolean isActive() {
        try {
            CSVReader reader = this.createCSVReader();
            reader.close();
            return true;
        } catch (Exception e) {
            log.error("Error in checking CSV config availability", e);
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
