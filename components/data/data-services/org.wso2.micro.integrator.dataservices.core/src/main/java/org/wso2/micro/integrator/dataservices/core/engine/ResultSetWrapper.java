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
package org.wso2.micro.integrator.dataservices.core.engine;

import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.*;
import java.util.Calendar;
import java.util.Map;

public class ResultSetWrapper implements ResultSet {

    private ResultSet rs;

    public ResultSetWrapper (ResultSet rs) {
        this.rs = rs;
    }

    public ResultSet getResultSet() {
        return rs;
    }

    @Override
    public boolean next() throws SQLException {
        return this.getResultSet().next();
    }

    @Override
    public void close() throws SQLException {
        this.getResultSet().close();
    }

    @Override
    public boolean wasNull() throws SQLException {
        return this.getResultSet().wasNull();
    }

    @Override
    public String getString(int columnIndex) throws SQLException {
        return this.getResultSet().getString(columnIndex);
    }

    @Override
    public boolean getBoolean(int columnIndex) throws SQLException {
        return this.getResultSet().getBoolean(columnIndex);
    }

    @Override
    public byte getByte(int columnIndex) throws SQLException {
        return this.getResultSet().getByte(columnIndex);
    }

    @Override
    public short getShort(int columnIndex) throws SQLException {
        return this.getResultSet().getShort(columnIndex);
    }

    @Override
    public int getInt(int columnIndex) throws SQLException {
        return this.getResultSet().getInt(columnIndex);
    }

    @Override
    public long getLong(int columnIndex) throws SQLException {
        return this.getResultSet().getLong(columnIndex);
    }

    @Override
    public float getFloat(int columnIndex) throws SQLException {
        return this.getResultSet().getFloat(columnIndex);
    }

    @Override
    public double getDouble(int columnIndex) throws SQLException {
        return this.getResultSet().getDouble(columnIndex);
    }

    @SuppressWarnings("deprecation")
	@Override
    public BigDecimal getBigDecimal(int columnIndex, int scale) throws SQLException {
        return this.getResultSet().getBigDecimal(columnIndex, scale);
    }

    @Override
    public byte[] getBytes(int columnIndex) throws SQLException {
        return this.getResultSet().getBytes(columnIndex);
    }

    @Override
    public Date getDate(int columnIndex) throws SQLException {
        return this.getResultSet().getDate(columnIndex);
    }

    @Override
    public Time getTime(int columnIndex) throws SQLException {
        return this.getResultSet().getTime(columnIndex);
    }

    @Override
    public Timestamp getTimestamp(int columnIndex) throws SQLException {
        return this.getResultSet().getTimestamp(columnIndex);
    }

    @Override
    public InputStream getAsciiStream(int columnIndex) throws SQLException {
        return this.getResultSet().getAsciiStream(columnIndex);
    }

    @SuppressWarnings("deprecation")
	@Override
    public InputStream getUnicodeStream(int columnIndex) throws SQLException {
        return this.getResultSet().getUnicodeStream(columnIndex);
    }

    @Override
    public InputStream getBinaryStream(int columnIndex) throws SQLException {
        return this.getResultSet().getBinaryStream(columnIndex);
    }

    @Override
    public String getString(String columnLabel) throws SQLException {
        return this.getResultSet().getString(columnLabel);
    }

    @Override
    public boolean getBoolean(String columnLabel) throws SQLException {
        return this.getResultSet().getBoolean(columnLabel);
    }

    @Override
    public byte getByte(String columnLabel) throws SQLException {
        return this.getResultSet().getByte(columnLabel);
    }

    @Override
    public short getShort(String columnLabel) throws SQLException {
        return this.getResultSet().getShort(columnLabel);
    }

    @Override
    public int getInt(String columnLabel) throws SQLException {
        return this.getResultSet().getInt(columnLabel);
    }

    @Override
    public long getLong(String columnLabel) throws SQLException {
        return this.getResultSet().getLong(columnLabel);
    }

    @Override
    public float getFloat(String columnLabel) throws SQLException {
        return this.getResultSet().getFloat(columnLabel);
    }

    @Override
    public double getDouble(String columnLabel) throws SQLException {
        return this.getResultSet().getDouble(columnLabel);
    }

    @SuppressWarnings("deprecation")
	@Override
    public BigDecimal getBigDecimal(String columnLabel, int scale) throws SQLException {
        return this.getResultSet().getBigDecimal(columnLabel, scale);
    }

    @Override
    public byte[] getBytes(String columnLabel) throws SQLException {
        return this.getResultSet().getBytes(columnLabel);
    }

    @Override
    public Date getDate(String columnLabel) throws SQLException {
        return this.getResultSet().getDate(columnLabel);
    }

    @Override
    public Time getTime(String columnLabel) throws SQLException {
        return this.getResultSet().getTime(columnLabel);
    }

    @Override
    public Timestamp getTimestamp(String columnLabel) throws SQLException {
        return this.getResultSet().getTimestamp(columnLabel);
    }

    @Override
    public InputStream getAsciiStream(String columnLabel) throws SQLException {
        return this.getResultSet().getAsciiStream(columnLabel);
    }

    @SuppressWarnings("deprecation")
	@Override
    public InputStream getUnicodeStream(String columnLabel) throws SQLException {
        return this.getResultSet().getUnicodeStream(columnLabel);
    }

    @Override
    public InputStream getBinaryStream(String columnLabel) throws SQLException {
        return this.getResultSet().getBinaryStream(columnLabel);
    }

    @Override
    public SQLWarning getWarnings() throws SQLException {
        return this.getResultSet().getWarnings();
    }

    @Override
    public void clearWarnings() throws SQLException {
        this.getResultSet().clearWarnings();
    }

    @Override
    public String getCursorName() throws SQLException {
        return this.getResultSet().getCursorName();
    }

    @Override
    public ResultSetMetaData getMetaData() throws SQLException {
        ResultSetMetaData rsMedata;
        try {
            rsMedata = this.getResultSet().getMetaData();
            return rsMedata;
        } catch (SQLException e) {
            rsMedata = new ResultSetMetadataWrapper(this.getResultSet());
            return rsMedata;
        }
    }

    @Override
    public Object getObject(int columnIndex) throws SQLException {
        return this.getResultSet().getObject(columnIndex);
    }

    @Override
    public Object getObject(String columnLabel) throws SQLException {
        return this.getResultSet().getObject(columnLabel);
    }

    @Override
    public int findColumn(String columnLabel) throws SQLException {
        return this.getResultSet().findColumn(columnLabel);
    }

    @Override
    public Reader getCharacterStream(int columnIndex) throws SQLException {
        return this.getResultSet().getCharacterStream(columnIndex);
    }

    @Override
    public Reader getCharacterStream(String columnLabel) throws SQLException {
        return this.getResultSet().getCharacterStream(columnLabel);
    }

    @Override
    public BigDecimal getBigDecimal(int columnIndex) throws SQLException {
        return this.getResultSet().getBigDecimal(columnIndex);
    }

    @Override
    public BigDecimal getBigDecimal(String columnLabel) throws SQLException {
        return this.getResultSet().getBigDecimal(columnLabel);
    }

    @Override
    public boolean isBeforeFirst() throws SQLException {
        return this.getResultSet().isBeforeFirst();
    }

    @Override
    public boolean isAfterLast() throws SQLException {
        return this.getResultSet().isAfterLast();
    }

    @Override
    public boolean isFirst() throws SQLException {
        return this.getResultSet().isFirst();
    }

    @Override
    public boolean isLast() throws SQLException {
        return this.getResultSet().isLast();
    }

    @Override
    public void beforeFirst() throws SQLException {
        this.getResultSet().beforeFirst();
    }

    @Override
    public void afterLast() throws SQLException {
        this.getResultSet().afterLast();
    }

    @Override
    public boolean first() throws SQLException {
        return this.getResultSet().first();
    }

    @Override
    public boolean last() throws SQLException {
        return this.getResultSet().last();
    }

    @Override
    public int getRow() throws SQLException {
        return this.getResultSet().getRow();
    }

    @Override
    public boolean absolute(int row) throws SQLException {
        return this.getResultSet().absolute(row);
    }

    @Override
    public boolean relative(int rows) throws SQLException {
        return this.getResultSet().relative(rows);
    }

    @Override
    public boolean previous() throws SQLException {
        return this.getResultSet().previous();
    }

    @Override
    public void setFetchDirection(int direction) throws SQLException {
        this.getResultSet().setFetchDirection(direction);
    }

    @Override
    public int getFetchDirection() throws SQLException {
        return this.getResultSet().getFetchDirection();
    }

    @Override
    public void setFetchSize(int rows) throws SQLException {
        this.getResultSet().setFetchSize(rows);
    }

    @Override
    public int getFetchSize() throws SQLException {
        return this.getResultSet().getFetchSize();
    }

    @Override
    public int getType() throws SQLException {
        return this.getResultSet().getType();
    }

    @Override
    public int getConcurrency() throws SQLException {
        return this.getResultSet().getConcurrency();
    }

    @Override
    public boolean rowUpdated() throws SQLException {
        return this.getResultSet().rowUpdated();
    }

    @Override
    public boolean rowInserted() throws SQLException {
        return this.getResultSet().rowInserted();
    }

    @Override
    public boolean rowDeleted() throws SQLException {
        return this.getResultSet().rowDeleted();
    }

    @Override
    public void updateNull(int columnIndex) throws SQLException {
        this.getResultSet().updateNull(columnIndex);
    }

    @Override
    public void updateBoolean(int columnIndex, boolean x) throws SQLException {
        this.getResultSet().updateBoolean(columnIndex, x);
    }

    @Override
    public void updateByte(int columnIndex, byte x) throws SQLException {
        this.getResultSet().updateByte(columnIndex, x);
    }

    @Override
    public void updateShort(int columnIndex, short x) throws SQLException {
        this.getResultSet().updateShort(columnIndex, x);
    }

    @Override
    public void updateInt(int columnIndex, int x) throws SQLException {
        this.getResultSet().updateInt(columnIndex, x);
    }

    @Override
    public void updateLong(int columnIndex, long x) throws SQLException {
        this.getResultSet().updateLong(columnIndex, x);
    }

    @Override
    public void updateFloat(int columnIndex, float x) throws SQLException {
        this.getResultSet().updateFloat(columnIndex, x);
    }

    @Override
    public void updateDouble(int columnIndex, double x) throws SQLException {
        this.getResultSet().updateDouble(columnIndex, x);
    }

    @Override
    public void updateBigDecimal(int columnIndex, BigDecimal x) throws SQLException {
        this.getResultSet().updateBigDecimal(columnIndex, x);
    }

    @Override
    public void updateString(int columnIndex, String x) throws SQLException {
        this.getResultSet().updateString(columnIndex, x);
    }

    @Override
    public void updateBytes(int columnIndex, byte[] x) throws SQLException {
        this.getResultSet().updateBytes(columnIndex, x);
    }

    @Override
    public void updateDate(int columnIndex, Date x) throws SQLException {
        this.getResultSet().updateDate(columnIndex, x);
    }

    @Override
    public void updateTime(int columnIndex, Time x) throws SQLException {
        this.getResultSet().updateTime(columnIndex, x);
    }

    @Override
    public void updateTimestamp(int columnIndex, Timestamp x) throws SQLException {
        this.getResultSet().updateTimestamp(columnIndex, x);
    }

    @Override
    public void updateAsciiStream(int columnIndex, InputStream x, int length) throws SQLException {
        this.getResultSet().updateAsciiStream(columnIndex, x);
    }

    @Override
    public void updateBinaryStream(int columnIndex, InputStream x, int length) throws SQLException {
        this.getResultSet().updateBinaryStream(columnIndex, x);
    }

    @Override
    public void updateCharacterStream(int columnIndex, Reader x, int length) throws SQLException {
        this.getResultSet().updateCharacterStream(columnIndex, x);
    }

    @Override
    public void updateObject(int columnIndex, Object x, int scaleOrLength) throws SQLException {
        this.getResultSet().updateObject(columnIndex, x, scaleOrLength);
    }

    @Override
    public void updateObject(int columnIndex, Object x) throws SQLException {
        this.getResultSet().updateObject(columnIndex, x);
    }

    @Override
    public void updateNull(String columnLabel) throws SQLException {
        this.getResultSet().updateNull(columnLabel);
    }

    @Override
    public void updateBoolean(String columnLabel, boolean x) throws SQLException {
        this.getResultSet().updateBoolean(columnLabel, x);
    }

    @Override
    public void updateByte(String columnLabel, byte x) throws SQLException {
        this.getResultSet().updateByte(columnLabel, x);
    }

    @Override
    public void updateShort(String columnLabel, short x) throws SQLException {
        this.getResultSet().updateShort(columnLabel, x);
    }

    @Override
    public void updateInt(String columnLabel, int x) throws SQLException {
        this.getResultSet().updateInt(columnLabel, x);
    }

    @Override
    public void updateLong(String columnLabel, long x) throws SQLException {
        this.getResultSet().updateLong(columnLabel, x);
    }

    @Override
    public void updateFloat(String columnLabel, float x) throws SQLException {
        this.getResultSet().updateFloat(columnLabel, x);
    }

    @Override
    public void updateDouble(String columnLabel, double x) throws SQLException {
        this.getResultSet().updateDouble(columnLabel, x);
    }

    @Override
    public void updateBigDecimal(String columnLabel, BigDecimal x) throws SQLException {
        this.getResultSet().updateBigDecimal(columnLabel, x);
    }

    @Override
    public void updateString(String columnLabel, String x) throws SQLException {
        this.getResultSet().updateString(columnLabel, x);
    }

    @Override
    public void updateBytes(String columnLabel, byte[] x) throws SQLException {
        this.getResultSet().updateBytes(columnLabel, x);
    }

    @Override
    public void updateDate(String columnLabel, Date x) throws SQLException {
        this.getResultSet().updateDate(columnLabel, x);
    }

    @Override
    public void updateTime(String columnLabel, Time x) throws SQLException {
        this.getResultSet().updateTime(columnLabel, x);
    }

    @Override
    public void updateTimestamp(String columnLabel, Timestamp x) throws SQLException {
        this.getResultSet().updateTimestamp(columnLabel, x);
    }

    @Override
    public void updateAsciiStream(String columnLabel, InputStream x, int length) throws SQLException {
        this.getResultSet().updateAsciiStream(columnLabel, x);
    }

    @Override
    public void updateBinaryStream(String columnLabel, InputStream x, int length) throws SQLException {
        this.getResultSet().updateBinaryStream(columnLabel, x);
    }

    @Override
    public void updateCharacterStream(String columnLabel, Reader reader, int length) throws SQLException {
        this.getResultSet().updateCharacterStream(columnLabel, reader, length);
    }

    @Override
    public void updateObject(String columnLabel, Object x, int scaleOrLength) throws SQLException {
        this.getResultSet().updateObject(columnLabel, x, scaleOrLength);
    }

    @Override
    public void updateObject(String columnLabel, Object x) throws SQLException {
        this.getResultSet().updateObject(columnLabel, x);
    }

    @Override
    public void insertRow() throws SQLException {
        this.getResultSet().insertRow();
    }

    @Override
    public void updateRow() throws SQLException {
        this.getResultSet().updateRow();
    }

    @Override
    public void deleteRow() throws SQLException {
        this.getResultSet().deleteRow();
    }

    @Override
    public void refreshRow() throws SQLException {
        this.getResultSet().deleteRow();
    }

    @Override
    public void cancelRowUpdates() throws SQLException {
        this.getResultSet().cancelRowUpdates();
    }

    @Override
    public void moveToInsertRow() throws SQLException {
        this.getResultSet().moveToInsertRow();
    }

    @Override
    public void moveToCurrentRow() throws SQLException {
        this.getResultSet().moveToCurrentRow();
    }

    @Override
    public Statement getStatement() throws SQLException {
        return this.getResultSet().getStatement();
    }

    @Override
    public Object getObject(int columnIndex, Map<String, Class<?>> map) throws SQLException {
        return this.getResultSet().getObject(columnIndex, map);
    }

    @Override
    public Ref getRef(int columnIndex) throws SQLException {
        return this.getResultSet().getRef(columnIndex);
    }

    @Override
    public Blob getBlob(int columnIndex) throws SQLException {
        return this.getResultSet().getBlob(columnIndex);
    }

    @Override
    public Clob getClob(int columnIndex) throws SQLException {
        return this.getResultSet().getClob(columnIndex);
    }

    @Override
    public Array getArray(int columnIndex) throws SQLException {
        return this.getResultSet().getArray(columnIndex);
    }

    @Override
    public Object getObject(String columnLabel, Map<String, Class<?>> map) throws SQLException {
        return this.getResultSet().getObject(columnLabel, map);
    }

    @Override
    public Ref getRef(String columnLabel) throws SQLException {
        return this.getResultSet().getRef(columnLabel);
    }

    @Override
    public Blob getBlob(String columnLabel) throws SQLException {
        return this.getResultSet().getBlob(columnLabel);
    }

    @Override
    public Clob getClob(String columnLabel) throws SQLException {
        return this.getResultSet().getClob(columnLabel);
    }

    @Override
    public Array getArray(String columnLabel) throws SQLException {
        return this.getResultSet().getArray(columnLabel);
    }

    @Override
    public Date getDate(int columnIndex, Calendar cal) throws SQLException {
        return this.getResultSet().getDate(columnIndex, cal);
    }

    @Override
    public Date getDate(String columnLabel, Calendar cal) throws SQLException {
        return this.getResultSet().getDate(columnLabel, cal);
    }

    @Override
    public Time getTime(int columnIndex, Calendar cal) throws SQLException {
        return this.getResultSet().getTime(columnIndex, cal);
    }

    @Override
    public Time getTime(String columnLabel, Calendar cal) throws SQLException {
        return this.getResultSet().getTime(columnLabel, cal);
    }

    @Override
    public Timestamp getTimestamp(int columnIndex, Calendar cal) throws SQLException {
        return this.getResultSet().getTimestamp(columnIndex, cal);
    }

    @Override
    public Timestamp getTimestamp(String columnLabel, Calendar cal) throws SQLException {
        return this.getResultSet().getTimestamp(columnLabel, cal);
    }

    @Override
    public URL getURL(int columnIndex) throws SQLException {
        return this.getResultSet().getURL(columnIndex);
    }

    @Override
    public URL getURL(String columnLabel) throws SQLException {
        return this.getResultSet().getURL(columnLabel);
    }

    @Override
    public void updateRef(int columnIndex, Ref x) throws SQLException {
        this.getResultSet().updateRef(columnIndex, x);
    }

    @Override
    public void updateRef(String columnLabel, Ref x) throws SQLException {
        this.getResultSet().updateRef(columnLabel, x);
    }

    @Override
    public void updateBlob(int columnIndex, Blob x) throws SQLException {
        this.getResultSet().updateBlob(columnIndex, x);
    }

    @Override
    public void updateBlob(String columnLabel, Blob x) throws SQLException {
        this.getResultSet().updateBlob(columnLabel, x);
    }

    @Override
    public void updateClob(int columnIndex, Clob x) throws SQLException {
        this.getResultSet().updateClob(columnIndex, x);
    }

    @Override
    public void updateClob(String columnLabel, Clob x) throws SQLException {
        this.getResultSet().updateClob(columnLabel, x);
    }

    @Override
    public void updateArray(int columnIndex, Array x) throws SQLException {
        this.getResultSet().updateArray(columnIndex, x);
    }

    @Override
    public void updateArray(String columnLabel, Array x) throws SQLException {
        this.getResultSet().updateArray(columnLabel, x);
    }

    @Override
    public RowId getRowId(int columnIndex) throws SQLException {
        return this.getResultSet().getRowId(columnIndex);
    }

    @Override
    public RowId getRowId(String columnLabel) throws SQLException {
        return this.getResultSet().getRowId(columnLabel);
    }

    @Override
    public void updateRowId(int columnIndex, RowId x) throws SQLException {
        this.getResultSet().updateRowId(columnIndex, x);
    }

    @Override
    public void updateRowId(String columnLabel, RowId x) throws SQLException {
        this.getResultSet().updateRowId(columnLabel, x);
    }

    @Override
    public int getHoldability() throws SQLException {
        return this.getResultSet().getHoldability();
    }

    @Override
    public boolean isClosed() throws SQLException {
        return this.getResultSet().isClosed();
    }

    @Override
    public void updateNString(int columnIndex, String nString) throws SQLException {
        this.getResultSet().updateNString(columnIndex, nString);
    }

    @Override
    public void updateNString(String columnLabel, String nString) throws SQLException {
        this.getResultSet().updateNString(columnLabel, nString);
    }

    @Override
    public void updateNClob(int columnIndex, NClob nClob) throws SQLException {
        this.getResultSet().updateNClob(columnIndex, nClob);
    }

    @Override
    public void updateNClob(String columnLabel, NClob nClob) throws SQLException {
        this.getResultSet().updateNClob(columnLabel, nClob);
    }

    @Override
    public NClob getNClob(int columnIndex) throws SQLException {
        return this.getResultSet().getNClob(columnIndex);
    }

    @Override
    public NClob getNClob(String columnLabel) throws SQLException {
        return this.getResultSet().getNClob(columnLabel);
    }

    @Override
    public SQLXML getSQLXML(int columnIndex) throws SQLException {
        return this.getResultSet().getSQLXML(columnIndex);
    }

    @Override
    public SQLXML getSQLXML(String columnLabel) throws SQLException {
        return this.getResultSet().getSQLXML(columnLabel);
    }

    @Override
    public void updateSQLXML(int columnIndex, SQLXML xmlObject) throws SQLException {
        this.getResultSet().updateSQLXML(columnIndex, xmlObject);
    }

    @Override
    public void updateSQLXML(String columnLabel, SQLXML xmlObject) throws SQLException {
        this.getResultSet().updateSQLXML(columnLabel, xmlObject);
    }

    @Override
    public String getNString(int columnIndex) throws SQLException {
        return this.getResultSet().getNString(columnIndex);
    }

    @Override
    public String getNString(String columnLabel) throws SQLException {
        return this.getResultSet().getNString(columnLabel);
    }

    @Override
    public Reader getNCharacterStream(int columnIndex) throws SQLException {
        return this.getResultSet().getNCharacterStream(columnIndex);
    }

    @Override
    public Reader getNCharacterStream(String columnLabel) throws SQLException {
        return this.getResultSet().getNCharacterStream(columnLabel);
    }

    @Override
    public void updateNCharacterStream(int columnIndex, Reader x, long length) throws SQLException {
        this.getResultSet().updateNCharacterStream(columnIndex, x, length);
    }

    @Override
    public void updateNCharacterStream(String columnLabel, Reader reader,
                                       long length) throws SQLException {
        this.getResultSet().updateNCharacterStream(columnLabel, reader, length);
    }

    @Override
    public void updateAsciiStream(int columnIndex, InputStream x, long length) throws SQLException {
        this.getResultSet().updateAsciiStream(columnIndex, x, length);
    }

    @Override
    public void updateBinaryStream(int columnIndex, InputStream x,
                                   long length) throws SQLException {
        this.getResultSet().updateBinaryStream(columnIndex, x, length);
    }

    @Override
    public void updateCharacterStream(int columnIndex, Reader x,
                                      long length) throws SQLException {
        this.getResultSet().updateCharacterStream(columnIndex, x, length);
    }

    @Override
    public void updateAsciiStream(String columnLabel, InputStream x,
                                  long length) throws SQLException {
        this.getResultSet().updateAsciiStream(columnLabel, x, length);
    }

    @Override
    public void updateBinaryStream(String columnLabel, InputStream x,
                                   long length) throws SQLException {
        this.getResultSet().updateBinaryStream(columnLabel, x, length);
    }

    @Override
    public void updateCharacterStream(String columnLabel, Reader reader,
                                      long length) throws SQLException {
        this.getResultSet().updateCharacterStream(columnLabel, reader, length);
    }

    @Override
    public void updateBlob(int columnIndex, InputStream inputStream,
                           long length) throws SQLException {
        this.getResultSet().updateBlob(columnIndex, inputStream, length);
    }

    @Override
    public void updateBlob(String columnLabel, InputStream inputStream,
                           long length) throws SQLException {
        this.getResultSet().updateBlob(columnLabel, inputStream, length);
    }

    @Override
    public void updateClob(int columnIndex, Reader reader, long length) throws SQLException {
        this.getResultSet().updateClob(columnIndex, reader, length);
    }

    @Override
    public void updateClob(String columnLabel, Reader reader, long length) throws SQLException {
        this.getResultSet().updateClob(columnLabel, reader, length);
    }

    @Override
    public void updateNClob(int columnIndex, Reader reader, long length) throws SQLException {
        this.getResultSet().updateNClob(columnIndex, reader, length);
    }

    @Override
    public void updateNClob(String columnLabel, Reader reader, long length) throws SQLException {
        this.getResultSet().updateNClob(columnLabel, reader, length);
    }

    @Override
    public void updateNCharacterStream(int columnIndex, Reader x) throws SQLException {
        this.getResultSet().updateNCharacterStream(columnIndex, x);
    }

    @Override
    public void updateNCharacterStream(String columnLabel, Reader reader) throws SQLException {
        this.getResultSet().updateNCharacterStream(columnLabel, reader);
    }

    @Override
    public void updateAsciiStream(int columnIndex, InputStream x) throws SQLException {
        this.getResultSet().updateAsciiStream(columnIndex, x);
    }

    @Override
    public void updateBinaryStream(int columnIndex, InputStream x) throws SQLException {
        this.getResultSet().updateBinaryStream(columnIndex, x);
    }

    @Override
    public void updateCharacterStream(int columnIndex, Reader x) throws SQLException {
        this.getResultSet().updateCharacterStream(columnIndex, x);
    }

    @Override
    public void updateAsciiStream(String columnLabel, InputStream x) throws SQLException {
        this.getResultSet().updateAsciiStream(columnLabel, x);
    }

    @Override
    public void updateBinaryStream(String columnLabel, InputStream x) throws SQLException {
        this.getResultSet().updateBinaryStream(columnLabel, x);
    }

    @Override
    public void updateCharacterStream(String columnLabel, Reader reader) throws SQLException {
        this.getResultSet().updateCharacterStream(columnLabel, reader);
    }

    @Override
    public void updateBlob(int columnIndex, InputStream inputStream) throws SQLException {
        this.getResultSet().updateBlob(columnIndex, inputStream);
    }

    @Override
    public void updateBlob(String columnLabel, InputStream inputStream) throws SQLException {
        this.getResultSet().updateBlob(columnLabel, inputStream);
    }

    @Override
    public void updateClob(int columnIndex, Reader reader) throws SQLException {
        this.getResultSet().updateClob(columnIndex, reader);
    }

    @Override
    public void updateClob(String columnLabel, Reader reader) throws SQLException {
        this.getResultSet().updateClob(columnLabel, reader);
    }

    @Override
    public void updateNClob(int columnIndex, Reader reader) throws SQLException {
        this.getResultSet().updateNClob(columnIndex, reader);
    }

    @Override
    public void updateNClob(String columnLabel, Reader reader) throws SQLException {
        this.getResultSet().updateNClob(columnLabel, reader);
    }

    @Override //need to remove the annotation if need to support this in java 1.6 too(backward compatibility)
    public <T> T getObject(int columnIndex, Class<T> type) throws SQLException {
        return this.getResultSet().getObject(columnIndex,type);
        //need to us below reflection code for the purpose of backward compatibility. unless this won't get compiled
        //with java 6 since this method is introduced in java 7
//        try {
//            Class[] paramString = new Class[2];
//            paramString[0] = Integer.TYPE;
//            paramString[1] = Class.class;
//            Method method = this.getResultSet().getClass().getDeclaredMethod("getObject", paramString);
//            return (T) method.invoke(this.getResultSet(), columnIndex, type);
//        } catch (NoSuchMethodException e) {
//            throw new UnsupportedOperationException("Method not supported in current implementation - method not found", e);
//        } catch (IllegalAccessException e) {
//            throw new UnsupportedOperationException("Method not supported in current implementation - Illegal Access attempt", e);
//        } catch (InvocationTargetException e) {
//            throw new UnsupportedOperationException("Method not supported in current implementation - Invocation exception", e);
//        }
    }

    @Override  //need to remove the annotation if need to support this in java 1.6 too(backward compatibility)
    public <T> T getObject(String columnLabel, Class<T> type) throws SQLException {
        return this.getResultSet().getObject(columnLabel,type);
        //need to us below reflection code for the purpose of backward compatibility. unless this won't get compiled
        //with java 6 since this method is introduced in java 7
//        try {
//            Class[] paramString = new Class[2];
//            paramString[0] = String.class;
//            paramString[1] = Class.class;
//            Method method = this.getResultSet().getClass().getDeclaredMethod("getObject", paramString);
//            return (T) method.invoke(this.getResultSet(), columnLabel, type);
//        } catch (NoSuchMethodException e) {
//            throw new UnsupportedOperationException("Method not supported in current implementation - method not found", e);
//        } catch (IllegalAccessException e) {
//            throw new UnsupportedOperationException("Method not supported in current implementation - Illegal Access attempt", e);
//        } catch (InvocationTargetException e) {
//            throw new UnsupportedOperationException("Method not supported in current implementation - Invocation exception", e);
//        }
    }

    @Override
    public <T> T unwrap(Class<T> iface) throws SQLException {
        return this.getResultSet().unwrap(iface);
    }

    @Override
    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        return this.getResultSet().isWrapperFor(iface);
    }

    /* Custom ResultSetMetadata implementation to support SQL Array type */

    private class ResultSetMetadataWrapper implements ResultSetMetaData {

        private ResultSet rs;

        public ResultSetMetadataWrapper(ResultSet rs) {
            this.rs = rs;
        }

        public ResultSet getResultSet() {
            return rs;
        }

        @Override
        public int getColumnCount() throws SQLException {
            return 2;
        }

        @Override
        public boolean isAutoIncrement(int column) throws SQLException {
            return false;
        }

        @Override
        public boolean isCaseSensitive(int column) throws SQLException {
            return false;
        }

        @Override
        public boolean isSearchable(int column) throws SQLException {
            return false;
        }

        @Override
        public boolean isCurrency(int column) throws SQLException {
            return false;
        }

        @Override
        public int isNullable(int column) throws SQLException {
            return 0;
        }

        @Override
        public boolean isSigned(int column) throws SQLException {
            return false;
        }

        @Override
        public int getColumnDisplaySize(int column) throws SQLException {
            return 0;
        }

        @Override
        public String getColumnLabel(int column) throws SQLException {
            return String.valueOf(column - 1);
        }

        @Override
        public String getColumnName(int column) throws SQLException {
            return null;
        }

        @Override
        public String getSchemaName(int column) throws SQLException {
            return null;
        }

        @Override
        public int getPrecision(int column) throws SQLException {
            return 0;
        }

        @Override
        public int getScale(int column) throws SQLException {
            return 0;
        }

        @Override
        public String getTableName(int column) throws SQLException {
            return null;
        }

        @Override
        public String getCatalogName(int column) throws SQLException {
            return null;
        }

        @Override
        public int getColumnType(int column) throws SQLException {
            Object o = this.getResultSet().getObject(column);
            if (o instanceof Integer) {
                return Types.INTEGER;
            } else if (o instanceof String) {
                return Types.VARCHAR;
            } else if (o instanceof Array) {
                return Types.ARRAY;
            } else if (o instanceof BigDecimal) {
                return Types.INTEGER;
            } else if (o instanceof Blob) {
                return Types.BLOB;
            } else if (o instanceof Boolean) {
                return Types.BOOLEAN;
            } else if (o instanceof Clob) {
                return Types.CLOB;
            } else if (o instanceof Date) {
                return Types.DATE;
            } else if (o instanceof Double) {
                return Types.DOUBLE;
            } else if (o instanceof Float) {
                return Types.FLOAT;
            } else if (o instanceof Ref) {
                return Types.REF;
            } else if (o instanceof RowId) {
                return Types.ROWID;
            } else if (o instanceof Short) {
                return Types.INTEGER;
            } else if (o instanceof SQLXML) {
                return Types.SQLXML;
            } else if (o instanceof Struct) {
                return Types.STRUCT;
            } else if (o instanceof Time) {
                return Types.TIME;
            } else if (o instanceof Timestamp) {
                return Types.TIMESTAMP;
            }
            return Types.OTHER;
        }

        @Override
        public String getColumnTypeName(int column) throws SQLException {
            return null;
        }

        @Override
        public boolean isReadOnly(int column) throws SQLException {
            return false;
        }

        @Override
        public boolean isWritable(int column) throws SQLException {
            return false;
        }

        @Override
        public boolean isDefinitelyWritable(int column) throws SQLException {
            return false;
        }

        @Override
        public String getColumnClassName(int column) throws SQLException {
            return null;
        }

        @Override
        public <T> T unwrap(Class<T> iface) throws SQLException {
            return null;
        }

        @Override
        public boolean isWrapperFor(Class<?> iface) throws SQLException {
            return false;
        }
    }

}
