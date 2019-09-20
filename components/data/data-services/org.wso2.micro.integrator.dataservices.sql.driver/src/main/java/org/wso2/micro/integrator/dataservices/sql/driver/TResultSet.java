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
package org.wso2.micro.integrator.dataservices.sql.driver;

import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.Array;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Date;
import java.sql.NClob;
import java.sql.Ref;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.RowId;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.sql.SQLWarning;
import java.sql.SQLXML;
import java.sql.Statement;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.poi.ss.usermodel.Cell;
import org.wso2.micro.integrator.dataservices.sql.driver.processor.reader.DataCell;
import org.wso2.micro.integrator.dataservices.sql.driver.processor.reader.DataRow;
import org.wso2.micro.integrator.dataservices.sql.driver.processor.reader.DataTable;
import org.wso2.micro.integrator.dataservices.sql.driver.query.ColumnInfo;

public class TResultSet implements ResultSet {

    private Statement stmt;

    private DataTable data;

    private int currentRowPosition = -1;

    private boolean isClosed = false;

    private ColumnInfo[] columns;

    private List<DataRow> rows;

    public TResultSet(Statement stmt, DataTable data, ColumnInfo[] columns) throws SQLException {
        this.stmt = stmt;
        this.data = data;
        this.columns = columns;
        Collection<DataRow> tmp = getData().getRows().values();
        this.rows = new ArrayList<DataRow>(tmp);
    }

    public boolean next() throws SQLException {
        determineResultSetState();
        if (isFirst()) {
            currentRowPosition = 0;
        }
        currentRowPosition++;
        return (getCurrentRowPosition() - 1 < getRows().size());
    }

    public void close() throws SQLException {
        determineResultSetState();
        this.data = null;
        isClosed = true;
    }

    public boolean wasNull() throws SQLException {
        determineResultSetState();
        return false;
    }

    public String getString(int columnIndex) throws SQLException {
        determineResultSetState();
        Object value = getValue(columnIndex);
        return (value != null) ? value.toString() : null;
    }

    public boolean getBoolean(int columnIndex) throws SQLException {
        determineResultSetState();
        return Boolean.parseBoolean(getValue(columnIndex).toString());
    }

    public byte getByte(int columnIndex) throws SQLException {
        determineResultSetState();
        throw new SQLFeatureNotSupportedException("Data type not supported");
    }

    public short getShort(int columnIndex) throws SQLException {
        determineResultSetState();
        String value = getValue(columnIndex).toString();
        return Short.parseShort(value.substring(0, value.indexOf(".")));
    }

    public int getInt(int columnIndex) throws SQLException {
        determineResultSetState();
        String value = getValue(columnIndex).toString();
        return Integer.parseInt(value.substring(0, value.indexOf(".")));
    }

    public long getLong(int columnIndex) throws SQLException {
        determineResultSetState();
        return Long.parseLong(getValue(columnIndex).toString());
    }

    public float getFloat(int columnIndex) throws SQLException {
        determineResultSetState();
        return Float.parseFloat(getValue(columnIndex).toString());
    }

    public double getDouble(int columnIndex) throws SQLException {
        determineResultSetState();
        return Double.parseDouble(getValue(columnIndex).toString());
    }

    public BigDecimal getBigDecimal(int columnIndex, int scale) throws SQLException {
        determineResultSetState();
        return BigDecimal.valueOf(Long.parseLong(getValue(columnIndex).toString()));
    }

    public byte[] getBytes(int columnIndex) throws SQLException {
        determineResultSetState();
        throw new SQLFeatureNotSupportedException("Data type not supported");
    }

    public Date getDate(int columnIndex) throws SQLException {
        determineResultSetState();
        throw new SQLFeatureNotSupportedException("Data type not supported");
    }

    public Time getTime(int columnIndex) throws SQLException {
        determineResultSetState();
        throw new SQLFeatureNotSupportedException("Data type not supported");
    }

    public Timestamp getTimestamp(int columnIndex) throws SQLException {
        determineResultSetState();
        throw new SQLFeatureNotSupportedException("Data type not supported");
    }

    public InputStream getAsciiStream(int columnIndex) throws SQLException {
        determineResultSetState();
        throw new SQLFeatureNotSupportedException("Data type not supported");
    }

    public InputStream getUnicodeStream(int columnIndex) throws SQLException {
        determineResultSetState();
        throw new SQLFeatureNotSupportedException("Data type not supported");
    }

    public InputStream getBinaryStream(int columnIndex) throws SQLException {
        determineResultSetState();
        return null;
    }

    public String getString(String columnLabel) throws SQLException {
        int columnIndex = findColumn(columnLabel);
        Object value = getValue(columnIndex);
        return (value != null) ? value.toString() : null;
    }

    public boolean getBoolean(String columnLabel) throws SQLException {
        int columnIndex = findColumn(columnLabel);
        return Boolean.parseBoolean(getValue(columnIndex).toString());
    }

    public byte getByte(String columnLabel) throws SQLException {
        determineResultSetState();
        throw new SQLFeatureNotSupportedException("Data type not supported");
    }

    public short getShort(String columnLabel) throws SQLException {
        int columnIndex = findColumn(columnLabel);
        String value = getValue(columnIndex).toString();
        return Short.parseShort(value.substring(0, value.indexOf(".")));
    }

    public int getInt(String columnLabel) throws SQLException {
        int columnIndex = findColumn(columnLabel);
        String value = getValue(columnIndex).toString();
        int tmp = value.indexOf(".");
        if (tmp == -1) {
            throw new SQLException("Invalid value to be returned as an integer");
        }
        return Integer.parseInt(value.substring(0, tmp));
    }

    public long getLong(String columnLabel) throws SQLException {
        int columnIndex = findColumn(columnLabel);
        return Long.parseLong(getValue(columnIndex).toString());
    }

    public float getFloat(String columnLabel) throws SQLException {
        int columnIndex = findColumn(columnLabel);
        return Float.parseFloat(getValue(columnIndex).toString());
    }

    public double getDouble(String columnLabel) throws SQLException {
        int columnIndex = findColumn(columnLabel);
        return Double.parseDouble(getValue(columnIndex).toString());
    }

    public BigDecimal getBigDecimal(String columnLabel, int scale) throws SQLException {
        int columnIndex = findColumn(columnLabel);
        DataCell cell = getCurrentRow().getCells().get(columnIndex);
        if (cell == null) {
            throw new SQLException("Error occurred while extracting the value");
        }
        if (cell.getCellType() != Cell.CELL_TYPE_NUMERIC) {
            throw new SQLException("Value cannot be cast to a string");
        }
        return BigDecimal.valueOf(Long.parseLong(cell.getCellValue().toString()));
    }

    public byte[] getBytes(String columnLabel) throws SQLException {
        determineResultSetState();
        throw new SQLFeatureNotSupportedException("Data type not supported");
    }

    public Date getDate(String columnLabel) throws SQLException {
        determineResultSetState();
        throw new SQLFeatureNotSupportedException("Data type not supported");
    }

    public Time getTime(String columnLabel) throws SQLException {
        determineResultSetState();
        throw new SQLFeatureNotSupportedException("Data type not supported");
    }

    public Timestamp getTimestamp(String columnLabel) throws SQLException {
        determineResultSetState();
        throw new SQLFeatureNotSupportedException("Data type not supported");
    }

    public InputStream getAsciiStream(String columnLabel) throws SQLException {
        determineResultSetState();
        throw new SQLFeatureNotSupportedException("Data type not supported");
    }

    public InputStream getUnicodeStream(String columnLabel) throws SQLException {
        determineResultSetState();
        throw new SQLFeatureNotSupportedException("Data type not supported");
    }

    public InputStream getBinaryStream(String columnLabel) throws SQLException {
        determineResultSetState();
        throw new SQLFeatureNotSupportedException("Data type not supported");
    }

    public SQLWarning getWarnings() throws SQLException {
        determineResultSetState();
        throw new SQLFeatureNotSupportedException("Data type not supported");
    }

    public void clearWarnings() throws SQLException {
        determineResultSetState();
        throw new SQLFeatureNotSupportedException("Data type not supported");
    }

    public String getCursorName() throws SQLException {
        determineResultSetState();
        throw new SQLFeatureNotSupportedException("Data type not supported");
    }

    public ResultSetMetaData getMetaData() throws SQLException {
        determineResultSetState();
        return new TResultSetMetaData(getColumns(), getColumns().length);
    }

    public Object getObject(int columnIndex) throws SQLException {
        determineResultSetState();
        DataCell cell = getCurrentRow().getCells().get(columnIndex);
        if (cell == null) {
            throw new SQLException("Error occurred while extracting the value");
        }
        return cell.getCellValue();
    }

    public Object getObject(String columnLabel) throws SQLException {
        int columnIndex = findColumn(columnLabel);
        DataCell cell = getCurrentRow().getCells().get(columnIndex);
        if (cell == null) {
            throw new SQLException("Error occurred while extracting the value");
        }
        return cell.getCellValue();
    }

    public int findColumn(String columnLabel) throws SQLException {
        determineResultSetState();
        Object o = getData().getHeader(columnLabel);
        if (o == null) {
            throw new SQLException("Invalid column name '" + columnLabel + "'");
        }
        return Integer.parseInt(o.toString());
    }

    public Reader getCharacterStream(int columnIndex) throws SQLException {
        determineResultSetState();
        throw new SQLFeatureNotSupportedException("Data type not supported");
    }

    public Reader getCharacterStream(String columnLabel) throws SQLException {
        determineResultSetState();
        throw new SQLFeatureNotSupportedException("Data type not supported");
    }

    public BigDecimal getBigDecimal(int columnIndex) throws SQLException {
        determineResultSetState();
        throw new SQLFeatureNotSupportedException("Data type not supported");
    }

    public BigDecimal getBigDecimal(String columnLabel) throws SQLException {
        determineResultSetState();
        throw new SQLFeatureNotSupportedException("Data type not supported");
    }

    public boolean isBeforeFirst() throws SQLException {
        determineResultSetState();
        return (getCurrentRowPosition() == -1);
    }

    public boolean isAfterLast() throws SQLException {
        determineResultSetState();
        return false;
    }

    public boolean isFirst() throws SQLException {
        determineResultSetState();
        return (getCurrentRowPosition() == -1);
    }

    public boolean isLast() throws SQLException {
        determineResultSetState();
        return false;
    }

    public void beforeFirst() throws SQLException {
        determineResultSetState();
    }

    public void afterLast() throws SQLException {
        determineResultSetState();
    }

    public boolean first() throws SQLException {
        determineResultSetState();
        return (getCurrentRowPosition() == 0);
    }

    public boolean last() throws SQLException {
        determineResultSetState();
        return (getCurrentRowPosition() == getRows().size() - 1);
    }

    public int getRow() throws SQLException {
        determineResultSetState();
        return getCurrentRowPosition();
    }

    public boolean absolute(int row) throws SQLException {
        determineResultSetState();
        return false;
    }

    public boolean relative(int rows) throws SQLException {
        determineResultSetState();
        return false;
    }

    public boolean previous() throws SQLException {
        determineResultSetState();
        return false;
    }

    public void setFetchDirection(int direction) throws SQLException {
        determineResultSetState();
    }

    public int getFetchDirection() throws SQLException {
        determineResultSetState();
        return 0;
    }

    public void setFetchSize(int rows) throws SQLException {
        determineResultSetState();
    }

    public int getFetchSize() throws SQLException {
        determineResultSetState();
        return 0;
    }

    public int getType() throws SQLException {
        determineResultSetState();
        return 0;
    }

    public int getConcurrency() throws SQLException {
        determineResultSetState();
        return 0;
    }

    public boolean rowUpdated() throws SQLException {
        determineResultSetState();
        return false;
    }

    public boolean rowInserted() throws SQLException {
        determineResultSetState();
        return false;
    }

    public boolean rowDeleted() throws SQLException {
        determineResultSetState();
        return false;
    }

    public void updateNull(int columnIndex) throws SQLException {
        determineResultSetState();
        throw new SQLFeatureNotSupportedException("Operation not supported");
    }

    public void updateBoolean(int columnIndex, boolean x) throws SQLException {
        determineResultSetState();
        throw new SQLFeatureNotSupportedException("Operation not supported");
    }

    public void updateByte(int columnIndex, byte x) throws SQLException {
        determineResultSetState();
        throw new SQLFeatureNotSupportedException("Operation not supported");
    }

    public void updateShort(int columnIndex, short x) throws SQLException {
        determineResultSetState();
        throw new SQLFeatureNotSupportedException("Operation not supported");
    }

    public void updateInt(int columnIndex, int x) throws SQLException {
        determineResultSetState();
        throw new SQLFeatureNotSupportedException("Operation not supported");
    }

    public void updateLong(int columnIndex, long x) throws SQLException {
        determineResultSetState();
        throw new SQLFeatureNotSupportedException("Operation not supported");
    }

    public void updateFloat(int columnIndex, float x) throws SQLException {
        determineResultSetState();
        throw new SQLFeatureNotSupportedException("Operation not supported");
    }

    public void updateDouble(int columnIndex, double x) throws SQLException {
        determineResultSetState();
        throw new SQLFeatureNotSupportedException("Operation not supported");
    }

    public void updateBigDecimal(int columnIndex, BigDecimal x) throws SQLException {
        determineResultSetState();
        throw new SQLFeatureNotSupportedException("Operation not supported");
    }

    public void updateString(int columnIndex, String x) throws SQLException {
        determineResultSetState();
        throw new SQLFeatureNotSupportedException("Operation not supported");
    }

    public void updateBytes(int columnIndex, byte[] x) throws SQLException {
        determineResultSetState();
        throw new SQLFeatureNotSupportedException("Operation not supported");
    }

    public void updateDate(int columnIndex, Date x) throws SQLException {
        determineResultSetState();
        throw new SQLFeatureNotSupportedException("Operation not supported");
    }

    public void updateTime(int columnIndex, Time x) throws SQLException {
        determineResultSetState();
        throw new SQLFeatureNotSupportedException("Operation not supported");
    }

    public void updateTimestamp(int columnIndex, Timestamp x) throws SQLException {
        determineResultSetState();
        throw new SQLFeatureNotSupportedException("Operation not supported");
    }

    public void updateAsciiStream(int columnIndex, InputStream x, int length) throws SQLException {
        determineResultSetState();
        throw new SQLFeatureNotSupportedException("Operation not supported");
    }

    public void updateBinaryStream(int columnIndex, InputStream x, int length) throws SQLException {
        determineResultSetState();
        throw new SQLFeatureNotSupportedException("Operation not supported");
    }

    public void updateCharacterStream(int columnIndex, Reader x, int length) throws SQLException {
        determineResultSetState();
        throw new SQLFeatureNotSupportedException("Operation not supported");
    }

    public void updateObject(int columnIndex, Object x, int scaleOrLength) throws SQLException {
        determineResultSetState();
        throw new SQLFeatureNotSupportedException("Operation not supported");
    }

    public void updateObject(int columnIndex, Object x) throws SQLException {
        determineResultSetState();
        throw new SQLFeatureNotSupportedException("Operation not supported");
    }

    public void updateNull(String columnLabel) throws SQLException {
        determineResultSetState();
        throw new SQLFeatureNotSupportedException("Operation not supported");
    }

    public void updateBoolean(String columnLabel, boolean x) throws SQLException {
        determineResultSetState();
        throw new SQLFeatureNotSupportedException("Operation not supported");
    }

    public void updateByte(String columnLabel, byte x) throws SQLException {
        determineResultSetState();
        throw new SQLFeatureNotSupportedException("Operation not supported");
    }

    public void updateShort(String columnLabel, short x) throws SQLException {
        determineResultSetState();
        throw new SQLFeatureNotSupportedException("Operation not supported");
    }

    public void updateInt(String columnLabel, int x) throws SQLException {
        determineResultSetState();
        throw new SQLFeatureNotSupportedException("Operation not supported");
    }

    public void updateLong(String columnLabel, long x) throws SQLException {
        determineResultSetState();
        throw new SQLFeatureNotSupportedException("Operation not supported");
    }

    public void updateFloat(String columnLabel, float x) throws SQLException {
        determineResultSetState();
        throw new SQLFeatureNotSupportedException("Operation not supported");
    }

    public void updateDouble(String columnLabel, double x) throws SQLException {
        determineResultSetState();
        throw new SQLFeatureNotSupportedException("Operation not supported");
    }

    public void updateBigDecimal(String columnLabel, BigDecimal x) throws SQLException {
        determineResultSetState();
        throw new SQLFeatureNotSupportedException("Operation not supported");
    }

    public void updateString(String columnLabel, String x) throws SQLException {
        determineResultSetState();
        throw new SQLFeatureNotSupportedException("Operation not supported");
    }

    public void updateBytes(String columnLabel, byte[] x) throws SQLException {
        determineResultSetState();
        throw new SQLFeatureNotSupportedException("Operation not supported");
    }

    public void updateDate(String columnLabel, Date x) throws SQLException {
        determineResultSetState();
        throw new SQLFeatureNotSupportedException("Operation not supported");
    }

    public void updateTime(String columnLabel, Time x) throws SQLException {
        determineResultSetState();
        throw new SQLFeatureNotSupportedException("Operation not supported");
    }

    public void updateTimestamp(String columnLabel, Timestamp x) throws SQLException {
        determineResultSetState();
        throw new SQLFeatureNotSupportedException("Operation not supported");
    }

    public void updateAsciiStream(String columnLabel, InputStream x,
                                  int length) throws SQLException {
        determineResultSetState();
        throw new SQLFeatureNotSupportedException("Operation not supported");
    }

    public void updateBinaryStream(String columnLabel, InputStream x,
                                   int length) throws SQLException {
        determineResultSetState();
        throw new SQLFeatureNotSupportedException("Operation not supported");
    }

    public void updateCharacterStream(String columnLabel, Reader reader,
                                      int length) throws SQLException {
        determineResultSetState();
        throw new SQLFeatureNotSupportedException("Operation not supported");
    }

    public void updateObject(String columnLabel, Object x, int scaleOrLength) throws SQLException {
        determineResultSetState();
        throw new SQLFeatureNotSupportedException("Operation not supported");
    }

    public void updateObject(String columnLabel, Object x) throws SQLException {
        determineResultSetState();
        throw new SQLFeatureNotSupportedException("Operation not supported");
    }

    public void insertRow() throws SQLException {
        determineResultSetState();
    }

    public void updateRow() throws SQLException {
        determineResultSetState();
    }

    public void deleteRow() throws SQLException {
        determineResultSetState();
    }

    public void refreshRow() throws SQLException {
        determineResultSetState();
    }

    public void cancelRowUpdates() throws SQLException {
        determineResultSetState();
    }

    public void moveToInsertRow() throws SQLException {
        determineResultSetState();
    }

    public void moveToCurrentRow() throws SQLException {
        determineResultSetState();
    }

    public Statement getStatement() throws SQLException {
        determineResultSetState();
        return stmt;
    }

    public Object getObject(int columnIndex, Map<String, Class<?>> map) throws SQLException {
        determineResultSetState();
        throw new SQLFeatureNotSupportedException("Operation not supported");
    }

    public Ref getRef(int columnIndex) throws SQLException {
        determineResultSetState();
        throw new SQLFeatureNotSupportedException("Data type is not supported");
    }

    public Blob getBlob(int columnIndex) throws SQLException {
        determineResultSetState();
        throw new SQLFeatureNotSupportedException("Data type is not supported");
    }

    public Clob getClob(int columnIndex) throws SQLException {
        determineResultSetState();
        throw new SQLFeatureNotSupportedException("Data type is not supported");
    }

    public Array getArray(int columnIndex) throws SQLException {
        determineResultSetState();
        throw new SQLFeatureNotSupportedException("Data type is not supported");
    }

    public Object getObject(String columnLabel, Map<String, Class<?>> map) throws SQLException {
        determineResultSetState();
        throw new SQLFeatureNotSupportedException("Data type is not supported");
    }

    public <T> T getObject(String columnLabel, Class<T> type) throws SQLException {
        throw new SQLFeatureNotSupportedException("Data type is not supported");
    }

    public <T> T getObject(int columnIndex, Class<T> type) throws SQLException {
        throw new SQLFeatureNotSupportedException("Data type is not supported");
    }

    public Ref getRef(String columnLabel) throws SQLException {
        determineResultSetState();
        throw new SQLFeatureNotSupportedException("Data type is not supported");
    }

    public Blob getBlob(String columnLabel) throws SQLException {
        determineResultSetState();
        throw new SQLFeatureNotSupportedException("Data type is not supported");
    }

    public Clob getClob(String columnLabel) throws SQLException {
        determineResultSetState();
        throw new SQLFeatureNotSupportedException("Data type is not supported");
    }

    public Array getArray(String columnLabel) throws SQLException {
        determineResultSetState();
        throw new SQLFeatureNotSupportedException("Data type is not supported");
    }

    public Date getDate(int columnIndex, Calendar cal) throws SQLException {
        determineResultSetState();
        throw new SQLFeatureNotSupportedException("Data type is not supported");
    }

    public Date getDate(String columnLabel, Calendar cal) throws SQLException {
        determineResultSetState();
        throw new SQLFeatureNotSupportedException("Data type is not supported");
    }

    public Time getTime(int columnIndex, Calendar cal) throws SQLException {
        determineResultSetState();
        throw new SQLFeatureNotSupportedException("Data type is not supported");
    }

    public Time getTime(String columnLabel, Calendar cal) throws SQLException {
        determineResultSetState();
        throw new SQLFeatureNotSupportedException("Data type is not supported");
    }

    public Timestamp getTimestamp(int columnIndex, Calendar cal) throws SQLException {
        determineResultSetState();
        throw new SQLFeatureNotSupportedException("Data type is not supported");
    }

    public Timestamp getTimestamp(String columnLabel, Calendar cal) throws SQLException {
        determineResultSetState();
        throw new SQLFeatureNotSupportedException("Data type is not supported");
    }

    public URL getURL(int columnIndex) throws SQLException {
        determineResultSetState();
        throw new SQLFeatureNotSupportedException("Data type is not supported");
    }

    public URL getURL(String columnLabel) throws SQLException {
        determineResultSetState();
        throw new SQLFeatureNotSupportedException("Data type is not supported");
    }

    public void updateRef(int columnIndex, Ref x) throws SQLException {
        determineResultSetState();
        throw new SQLFeatureNotSupportedException("Operation is not supported");
    }

    public void updateRef(String columnLabel, Ref x) throws SQLException {
        determineResultSetState();
        throw new SQLFeatureNotSupportedException("Operation is not supported");
    }

    public void updateBlob(int columnIndex, Blob x) throws SQLException {
        determineResultSetState();
        throw new SQLFeatureNotSupportedException("Operation is not supported");
    }

    public void updateBlob(String columnLabel, Blob x) throws SQLException {
        determineResultSetState();
        throw new SQLFeatureNotSupportedException("Operation is not supported");
    }

    public void updateClob(int columnIndex, Clob x) throws SQLException {
        determineResultSetState();
        throw new SQLFeatureNotSupportedException("Operation is not supported");
    }

    public void updateClob(String columnLabel, Clob x) throws SQLException {
        determineResultSetState();
        throw new SQLFeatureNotSupportedException("Operation is not supported");
    }

    public void updateArray(int columnIndex, Array x) throws SQLException {
        determineResultSetState();
        throw new SQLFeatureNotSupportedException("Operation is not supported");
    }

    public void updateArray(String columnLabel, Array x) throws SQLException {
        determineResultSetState();
        throw new SQLFeatureNotSupportedException("Operation is not supported");
    }

    public RowId getRowId(int columnIndex) throws SQLException {
        determineResultSetState();
        throw new SQLFeatureNotSupportedException("Operation is not supported");
    }

    public RowId getRowId(String columnLabel) throws SQLException {
        determineResultSetState();
        throw new SQLFeatureNotSupportedException("Operation is not supported");
    }

    public void updateRowId(int columnIndex, RowId x) throws SQLException {
        determineResultSetState();
        throw new SQLFeatureNotSupportedException("Operation is not supported");
    }

    public void updateRowId(String columnLabel, RowId x) throws SQLException {
        determineResultSetState();
        throw new SQLFeatureNotSupportedException("Operation is not supported");
    }

    public int getHoldability() throws SQLException {
        determineResultSetState();
        throw new SQLFeatureNotSupportedException("Operation is not supported");
    }

    public boolean isClosed() throws SQLException {
        return isClosed;
    }

    public void updateNString(int columnIndex, String nString) throws SQLException {
        determineResultSetState();
        throw new SQLFeatureNotSupportedException("Operation is not supported");
    }

    public void updateNString(String columnLabel, String nString) throws SQLException {
        determineResultSetState();
        throw new SQLFeatureNotSupportedException("Operation is not supported");
    }

    public void updateNClob(int columnIndex, NClob nClob) throws SQLException {
        determineResultSetState();
        throw new SQLFeatureNotSupportedException("Operation is not supported");
    }

    public void updateNClob(String columnLabel, NClob nClob) throws SQLException {
        determineResultSetState();
        throw new SQLFeatureNotSupportedException("Operation is not supported");
    }

    public NClob getNClob(int columnIndex) throws SQLException {
        determineResultSetState();
        throw new SQLFeatureNotSupportedException("Data type is not supported");
    }

    public NClob getNClob(String columnLabel) throws SQLException {
        determineResultSetState();
        throw new SQLFeatureNotSupportedException("Data type is not supported");
    }

    public SQLXML getSQLXML(int columnIndex) throws SQLException {
        determineResultSetState();
        throw new SQLFeatureNotSupportedException("Data type is not supported");
    }

    public SQLXML getSQLXML(String columnLabel) throws SQLException {
        determineResultSetState();
        throw new SQLFeatureNotSupportedException("Data type is not supported");
    }

    public void updateSQLXML(int columnIndex, SQLXML xmlObject) throws SQLException {
        determineResultSetState();
        throw new SQLFeatureNotSupportedException("Operation is not supported");
    }

    public void updateSQLXML(String columnLabel, SQLXML xmlObject) throws SQLException {
        determineResultSetState();
        throw new SQLFeatureNotSupportedException("Operation is not supported");
    }

    public String getNString(int columnIndex) throws SQLException {
        determineResultSetState();
        throw new SQLFeatureNotSupportedException("Operation is not supported");
    }

    public String getNString(String columnLabel) throws SQLException {
        determineResultSetState();
        throw new SQLFeatureNotSupportedException("Operation is not supported");
    }

    public Reader getNCharacterStream(int columnIndex) throws SQLException {
        determineResultSetState();
        throw new SQLFeatureNotSupportedException("Operation is not supported");
    }

    public Reader getNCharacterStream(String columnLabel) throws SQLException {
        determineResultSetState();
        throw new SQLFeatureNotSupportedException("Operation is not supported");
    }

    public void updateNCharacterStream(int columnIndex, Reader x, long length) throws SQLException {
        determineResultSetState();
        throw new SQLFeatureNotSupportedException("Operation is not supported");
    }

    public void updateNCharacterStream(String columnLabel, Reader reader, long length) throws SQLException {
        determineResultSetState();
        throw new SQLFeatureNotSupportedException("Operation is not supported");
    }

    public void updateAsciiStream(int columnIndex, InputStream x, long length) throws SQLException {
        determineResultSetState();
        throw new SQLFeatureNotSupportedException("Operation is not supported");
    }

    public void updateBinaryStream(int columnIndex, InputStream x, long length) throws SQLException {
        determineResultSetState();
        throw new SQLFeatureNotSupportedException("Operation is not supported");
    }

    public void updateCharacterStream(int columnIndex, Reader x, long length) throws SQLException {
        determineResultSetState();
        throw new SQLFeatureNotSupportedException("Operation is not supported");
    }

    public void updateAsciiStream(String columnLabel, InputStream x, long length) throws SQLException {
        determineResultSetState();
        throw new SQLFeatureNotSupportedException("Operation is not supported");
    }

    public void updateBinaryStream(String columnLabel, InputStream x, long length) throws SQLException {
        determineResultSetState();
        throw new SQLFeatureNotSupportedException("Operation is not supported");
    }

    public void updateCharacterStream(String columnLabel, Reader reader, long length) throws SQLException {
        determineResultSetState();
        throw new SQLFeatureNotSupportedException("Operation is not supported");
    }

    public void updateBlob(int columnIndex, InputStream inputStream, long length) throws SQLException {
        determineResultSetState();
        throw new SQLFeatureNotSupportedException("Operation is not supported");
    }

    public void updateBlob(String columnLabel, InputStream inputStream, long length) throws SQLException {
        determineResultSetState();
        throw new SQLFeatureNotSupportedException("Operation is not supported");
    }

    public void updateClob(int columnIndex, Reader reader, long length) throws SQLException {
        determineResultSetState();
        throw new SQLFeatureNotSupportedException("Operation is not supported");
    }

    public void updateClob(String columnLabel, Reader reader, long length) throws SQLException {
        determineResultSetState();
        throw new SQLFeatureNotSupportedException("Operation is not supported");
    }

    public void updateNClob(int columnIndex, Reader reader, long length) throws SQLException {
        determineResultSetState();
        throw new SQLFeatureNotSupportedException("Operation is not supported");
    }

    public void updateNClob(String columnLabel, Reader reader, long length) throws SQLException {
        determineResultSetState();
        throw new SQLFeatureNotSupportedException("Operation is not supported");
    }

    public void updateNCharacterStream(int columnIndex, Reader x) throws SQLException {
        determineResultSetState();
        throw new SQLFeatureNotSupportedException("Operation is not supported");
    }

    public void updateNCharacterStream(String columnLabel, Reader reader) throws SQLException {
        determineResultSetState();
        throw new SQLFeatureNotSupportedException("Operation is not supported");
    }

    public void updateAsciiStream(int columnIndex, InputStream x) throws SQLException {
        determineResultSetState();
        throw new SQLFeatureNotSupportedException("Operation is not supported");
    }

    public void updateBinaryStream(int columnIndex, InputStream x) throws SQLException {
        determineResultSetState();
        throw new SQLFeatureNotSupportedException("Operation is not supported");
    }

    public void updateCharacterStream(int columnIndex, Reader x) throws SQLException {
        determineResultSetState();
        throw new SQLFeatureNotSupportedException("Operation is not supported");
    }

    public void updateAsciiStream(String columnLabel, InputStream x) throws SQLException {
        determineResultSetState();
        throw new SQLFeatureNotSupportedException("Operation is not supported");
    }

    public void updateBinaryStream(String columnLabel, InputStream x) throws SQLException {
        determineResultSetState();
        throw new SQLFeatureNotSupportedException("Operation is not supported");
    }

    public void updateCharacterStream(String columnLabel, Reader reader) throws SQLException {
        determineResultSetState();
        throw new SQLFeatureNotSupportedException("Operation is not supported");
    }

    public void updateBlob(int columnIndex, InputStream inputStream) throws SQLException {
        determineResultSetState();
        throw new SQLFeatureNotSupportedException("Operation is not supported");
    }

    public void updateBlob(String columnLabel, InputStream inputStream) throws SQLException {
        determineResultSetState();
        throw new SQLFeatureNotSupportedException("Operation is not supported");
    }

    public void updateClob(int columnIndex, Reader reader) throws SQLException {
        determineResultSetState();
        throw new SQLFeatureNotSupportedException("Operation is not supported");
    }

    public void updateClob(String columnLabel, Reader reader) throws SQLException {
        determineResultSetState();
        throw new SQLFeatureNotSupportedException("Operation is not supported");
    }

    public void updateNClob(int columnIndex, Reader reader) throws SQLException {
        determineResultSetState();
        throw new SQLFeatureNotSupportedException("Operation is not supported");
    }

    public void updateNClob(String columnLabel, Reader reader) throws SQLException {
        determineResultSetState();
        throw new SQLFeatureNotSupportedException("Operation is not supported");
    }

    public <T> T unwrap(Class<T> iface) throws SQLException {
        determineResultSetState();
        throw new SQLFeatureNotSupportedException("Operation is not supported");
    }

    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        determineResultSetState();
        throw new SQLFeatureNotSupportedException("Operation is not supported");
    }

    public int getCurrentRowPosition() {
        return currentRowPosition;
    }

    private DataTable getData() {
        return data;
    }

    private DataRow getCurrentRow() {
        return getRows().get(getCurrentRowPosition() - 1);
    }

    private List<DataRow> getRows() {
        return rows;
    }

    private void determineResultSetState() throws SQLException {
        if (isClosed()) {
            throw new SQLException("Result set has already been closed");
        }
    }

    public ColumnInfo[] getColumns() {
        return columns;
    }

    private Object getValue(int columnIndex) throws SQLException {
        DataCell cell = getCurrentRow().getCells().get(columnIndex);
        if (cell == null) {
            throw new SQLException("Error occurred while extracting the value");
        }
        return cell.getCellValue();
    }


}
