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

package org.wso2.micro.integrator.dataservices.core.odata;

/**
 * This class represents a data column properties. This class is used to store column specific data to create odata service.
 */
public class DataColumn {

    /**
     * Name of the table column.
     */
    private String columnName;

    /**
     * Data type of the table column.
     */
    private ODataDataType columnType;

    /**
     * Ordinal position of the table column.
     */
    private int ordinalPosition;

    /**
     * Is the table column support nullable.
     */
    private boolean nullable;

    /**
     * Precision of the table column.
     */
    private int precision;

    /**
     * Scale of the table column.
     */
    private int scale;

    /**
     * Default value of the table column.
     */
    private String defaultValue;

    private boolean isAutoIncrement;

    /**
     * Maximum length of the table column.
     */
    private int maxLength;

    public DataColumn(String columnName, ODataDataType columnType, int order, boolean isNullable, int length, boolean isAutoIncrement) {
        this.columnName = columnName;
        this.columnType = columnType;
        this.ordinalPosition = order;
        this.nullable = isNullable;
        this.maxLength = length;
        this.isAutoIncrement = isAutoIncrement;
    }

    public DataColumn(String columnName, ODataDataType columnType, boolean isNullable) {
        this.columnName = columnName;
        this.columnType = columnType;
        this.nullable = isNullable;
        this.maxLength = Integer.MAX_VALUE;
    }

    public enum ODataDataType {
        BINARY,
        BOOLEAN,
        BYTE,
        SBYTE,
        DATE,
        DATE_TIMEOFFSET,
        TIMEOFDAY,
        DURATION,
        DECIMAL,
        SINGLE,
        DOUBLE,
        GUID,
        INT16,
        INT32,
        INT64,
        STRING,
        STREAM,
        GEOGRAPHY,
        GEOGRAPHY_POINT,
        GEOGRAPHY_LINE_STRING,
        GEOGRAPHY_POLYGON,
        GEOGRAPHY_MULTIPOINT,
        GEOGRAPHY_MULTILINE_STRING,
        GEOGRAPHY_MULTIPOLYGON,
        GEOGRAPHY_COLLECTION,
        GEOMETRY,
        GEOMETRY_POINT,
        GEOMETRY_LINE_STRING,
        GEOMETRY_POLYGON,
        GEOMETRY_MULTIPOINT,
        GEOMETRY_MULTILINE_STRING,
        GEOMETRY_MULTIPOLYGON,
        GEOMETRY_COLLECTION
    }

    public String getColumnName() {
        return columnName;
    }

    public ODataDataType getColumnType() {
        return columnType;
    }

    public int getMaxLength() {
        return maxLength;
    }

    public void setMaxLength(int maxLength) {
        this.maxLength = maxLength;
    }

    public void setPrecision(int precision) {
        this.precision = precision;
    }

    public void setScale(int scale) {
        this.scale = scale;
    }

    public int getOrdinalPosition() {
        return ordinalPosition;
    }

    public boolean isNullable() {
        return nullable;
    }

    public int getPrecision() {
        return precision;
    }

    public int getScale() {
        return scale;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    public boolean isAutoIncrement() {
        return isAutoIncrement;
    }
}
