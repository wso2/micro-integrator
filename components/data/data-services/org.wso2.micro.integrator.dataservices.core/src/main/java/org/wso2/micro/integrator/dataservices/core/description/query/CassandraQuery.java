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

import com.datastax.driver.core.BatchStatement;
import com.datastax.driver.core.ColumnDefinitions;
import com.datastax.driver.core.DataType;
import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.SimpleStatement;
import org.apache.axis2.databinding.utils.ConverterUtil;
import org.apache.commons.codec.binary.Base64;
import org.wso2.micro.integrator.dataservices.common.DBConstants;
import org.wso2.micro.integrator.dataservices.common.DBConstants.DataTypes;
import org.wso2.micro.integrator.dataservices.core.DBUtils;import org.wso2.micro.integrator.dataservices.core.DataServiceFault;import org.wso2.micro.integrator.dataservices.core.description.config.CassandraConfig;import org.wso2.micro.integrator.dataservices.core.description.event.EventTrigger;import org.wso2.micro.integrator.dataservices.core.dispatch.DispatchStatus;import org.wso2.micro.integrator.dataservices.core.engine.DataEntry;import org.wso2.micro.integrator.dataservices.core.engine.DataService;import org.wso2.micro.integrator.dataservices.core.engine.InternalParam;import org.wso2.micro.integrator.dataservices.core.engine.InternalParamCollection;import org.wso2.micro.integrator.dataservices.core.engine.ParamValue;import org.wso2.micro.integrator.dataservices.core.engine.QueryParam;import org.wso2.micro.integrator.dataservices.core.engine.Result;

import javax.xml.stream.XMLStreamWriter;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * This class represents Cassandra-CQL data services query implementation.
 */
public class CassandraQuery extends ExpressionQuery {

    private CassandraConfig config;

    private PreparedStatement statement;

    /**
     * thread local variable to keep a batch statement in batch processing
     */
    private ThreadLocal<BatchStatement> batchStatement = new ThreadLocal<BatchStatement>() {
        protected synchronized BatchStatement initialValue() {
            return null;
        }
    };

    public CassandraQuery(DataService dataService, String queryId, String query, List<QueryParam> queryParams,
                          Result result, String configId, EventTrigger inputEventTrigger,
                          EventTrigger outputEventTrigger, Map<String, String> advancedProperties,
                          String inputNamespace) throws DataServiceFault {
        super(dataService, queryId, queryParams, query, result, configId, inputEventTrigger, outputEventTrigger,
              advancedProperties, inputNamespace);
        this.init(query);
        try {
            this.config = (CassandraConfig) this.getDataService().getConfig(this.getConfigId());
        } catch (ClassCastException e) {
            throw new DataServiceFault(e, "Configuration is not a Cassandra config:" + this.getConfigId());
        }
    }

    public PreparedStatement getStatement() {
        return statement;
    }

    public Session getSession() {
        return this.config.getSession();
    }

    public boolean isNativeBatchRequestsSupported() {
        return this.config.isNativeBatchRequestsSupported();
    }

    private Object[] bindParams(InternalParamCollection params) throws DataServiceFault {
        int count = params.getSize();
        List<Object> values = new ArrayList<>(count);
        InternalParam param;
        for (int i = 1; i <= count; i++) {
            param = params.getParam(i);
            switch (param.getSqlType()) {
                case DataTypes.STRING:
                    values.add(param.getValue().toString());
                    break;
                case DataTypes.BIGINT:
                    values.add(param.getValue() == null ? null : Long.parseLong(param.getValue().getValueAsString()));
                    break;
                case DataTypes.BINARY:
                    values.add(param.getValue() == null ? null :
                               this.base64DecodeByteBuffer(param.getValue().getValueAsString()));
                    break;
                case DataTypes.BIT:
                    values.add(param.getValue() == null ? null :
                               Boolean.parseBoolean(param.getValue().getValueAsString()));
                    break;
                case DataTypes.BLOB:
                    values.add(param.getValue() == null ? null :
                               this.base64DecodeByteBuffer(param.getValue().getValueAsString()));
                    break;
                case DataTypes.BOOLEAN:
                    values.add(param.getValue() == null ? null :
                               Boolean.parseBoolean(param.getValue().getValueAsString()));
                    break;
                case DataTypes.CHAR:
                    values.add(param.getValue() == null ? null : param.getValue().getValueAsString());
                    break;
                case DataTypes.CLOB:
                    values.add(param.getValue() == null ? null : param.getValue().getValueAsString());
                    break;
                case DataTypes.DATE:
                    values.add(param.getValue() == null ? null : DBUtils.getDate(param.getValue().getValueAsString()));
                    break;
                case DataTypes.DECIMAL:
                    values.add(param.getValue() == null ? null : new BigDecimal(param.getValue().getValueAsString()));
                    break;
                case DataTypes.DOUBLE:
                    values.add(
                            param.getValue() == null ? null : Double.parseDouble(param.getValue().getValueAsString()));
                    break;
                case DataTypes.FLOAT:
                    values.add(param.getValue() == null ? null : Float.parseFloat(param.getValue().getValueAsString()));
                    break;
                case DataTypes.INTEGER:
                    values.add(param.getValue() == null ? null : Integer.parseInt(param.getValue().getValueAsString()));
                    break;
                case DataTypes.LONG:
                    values.add(param.getValue() == null ? null : Long.parseLong(param.getValue().getValueAsString()));
                    break;
                case DataTypes.LONG_VARBINARY:
                    values.add(param.getValue() == null ? null :
                               this.base64DecodeByteBuffer(param.getValue().getValueAsString()));
                    break;
                case DataTypes.NUMERIC:
                    values.add(param.getValue() == null ? null : new BigDecimal(param.getValue().getValueAsString()));
                    break;
                case DataTypes.NVARCHAR:
                    values.add(param.getValue().getValueAsString());
                    break;
                case DataTypes.QUERY_STRING:
                    values.add(param.getValue().getValueAsString());
                    break;
                case DataTypes.REAL:
                    values.add(param.getValue() == null ? null : Float.parseFloat(param.getValue().getValueAsString()));
                    break;
                case DataTypes.SMALLINT:
                    values.add(param.getValue() == null ? null : Integer.parseInt(param.getValue().getValueAsString()));
                    break;
                case DataTypes.TEXT:
                    values.add(param.getValue().getValueAsString());
                    break;
                case DataTypes.TIME:
                    values.add(param.getValue() == null ? null : DBUtils.getDate(param.getValue().getValueAsString()));
                    break;
                case DataTypes.TIMESTAMP:
                    values.add(param.getValue() == null ? null : DBUtils.getDate(param.getValue().getValueAsString()));
                    break;
                case DataTypes.TINYINT:
                    values.add(param.getValue() == null ? null : Integer.parseInt(param.getValue().getValueAsString()));
                    break;
                case DataTypes.VARBINARY:
                    values.add(param.getValue() == null ? null :
                               this.base64DecodeByteBuffer(param.getValue().getValueAsString()));
                    break;
                case DataTypes.VARCHAR:
                    values.add(param.getValue().getValueAsString());
                    break;
                case DataTypes.VARINT:
                    values.add(param.getValue() == null ? null : new BigInteger(param.getValue().getValueAsString()));
                    break;
                case DataTypes.UUID:
                    values.add(param.getValue() == null ? null : UUID.fromString(param.getValue().getValueAsString()));
                    break;
                case DataTypes.INETADDRESS:
                    try {
                        values.add(param.getValue() == null ? null :
                                   InetAddress.getByName(param.getValue().getValueAsString()));
                    } catch (UnknownHostException e) {
                        throw new DataServiceFault(e);
                    }
                    break;
                default:
                    values.add(param.getValue().toString());
            }
        }
        return values.toArray();
    }

    private void checkAndCreateStatement() throws DataServiceFault {
        if (this.statement == null) {
            synchronized (this) {
                if (this.statement == null) {
                    Session session = this.getSession();
                    this.statement = session.prepare(this.getQuery());
                }
            }
        }
    }

    @Override
    public Object runPreQuery(InternalParamCollection params, int queryLevel) throws DataServiceFault {
        ResultSet rs = null;
         /*
            There is no point of creating prepared statements for dynamic queries
         */
        if (isDynamicQuery(params)) {
            Object[] result = this.processDynamicQuery(this.getQuery(), params);
            String dynamicCql = (String) result[0];
            int currentParamCount = (Integer) result[1];
            String processedSQL = this.createProcessedQuery(dynamicCql, params, currentParamCount);
            if (DispatchStatus.isBatchRequest() && this.isNativeBatchRequestsSupported()) {
                /* handle batch requests */
                if (DispatchStatus.isFirstBatchRequest()) {
                    this.batchStatement.set(new BatchStatement());
                }
                SimpleStatement simpleStatement = new SimpleStatement(processedSQL);
                this.batchStatement.get().add(simpleStatement);
                if (DispatchStatus.isLastBatchRequest()) {
                    this.getSession().execute(this.batchStatement.get());
                }
            } else {
                SimpleStatement statement = new SimpleStatement(processedSQL, this.bindParams(params));
                rs = this.getSession().execute(statement);
            }
        } else {
            this.checkAndCreateStatement();
            if (DispatchStatus.isBatchRequest() && this.isNativeBatchRequestsSupported()) {
                /* handle batch requests */
                if (DispatchStatus.isFirstBatchRequest()) {
                    this.batchStatement.set(new BatchStatement());
                }
                this.batchStatement.get().add(this.getStatement().bind(this.bindParams(params)));
                if (DispatchStatus.isLastBatchRequest()) {
                    this.getSession().execute(this.batchStatement.get());
                }
            } else {
                rs = this.getSession().execute(this.getStatement().bind(this.bindParams(params)));
            }
        }
        return rs;
    }

    @Override
    public void runPostQuery(Object result, XMLStreamWriter xmlWriter, InternalParamCollection params, int queryLevel)
            throws DataServiceFault {
        ResultSet rs = (ResultSet) result;
        if (this.hasResult()) {
            Iterator<Row> itr = rs.iterator();
            Row row;
            DataEntry dataEntry;
            ColumnDefinitions defs = rs.getColumnDefinitions();
            while (itr.hasNext()) {
                row = itr.next();
                dataEntry = this.getDataEntryFromRow(row, defs);
                this.writeResultEntry(xmlWriter, dataEntry, params, queryLevel);
            }
        }
    }

    private DataEntry getDataEntryFromRow(Row row, ColumnDefinitions defs) throws DataServiceFault {
        boolean useColumnNumbers = this.isUsingColumnNumbers();
        DataType columnType;
        DataEntry entry = new DataEntry();
        ParamValue paramValue = null;
        for (int i = 0; i < defs.size(); i++) {
            columnType = defs.getType(i);
            if (columnType.getName().equals(DataType.Name.ASCII)) {
                paramValue = new ParamValue(row.getString(i));
            } else if (columnType.getName().equals(DataType.Name.VARCHAR)) {
                paramValue = new ParamValue(row.getString(i));
            } else if (columnType.getName().equals(DataType.Name.TEXT)) {
                paramValue = new ParamValue(row.getString(i));
            } else if (columnType.getName().equals(DataType.Name.BIGINT)) {
                paramValue = new ParamValue(row.isNull(i) ? null : Long.toString(row.getLong(i)));
            } else if (columnType.getName().equals(DataType.Name.BLOB)) {
                paramValue = new ParamValue(this.base64EncodeByteBuffer(row.getBytes(i)));
            } else if (columnType.getName().equals(DataType.Name.BOOLEAN)) {
                paramValue = new ParamValue(row.isNull(i) ? null : Boolean.toString(row.getBool(i)));
            } else if (columnType.getName().equals(DataType.Name.COUNTER)) {
                paramValue = new ParamValue(row.isNull(i) ? null : Long.toString(row.getLong(i)));
            } else if (columnType.getName().equals(DataType.Name.CUSTOM)) {
                paramValue = new ParamValue(this.base64EncodeByteBuffer(row.getBytes(i)));
            } else if (columnType.getName().equals(DataType.Name.DECIMAL)) {
                paramValue = new ParamValue(row.isNull(i) ? null : row.getDecimal(i).toString());
            } else if (columnType.getName().equals(DataType.Name.DOUBLE)) {
                paramValue = new ParamValue(row.isNull(i) ? null : Double.toString(row.getDouble(i)));
            } else if (columnType.getName().equals(DataType.Name.FLOAT)) {
                paramValue = new ParamValue(row.isNull(i) ? null : Float.toString(row.getFloat(i)));
            } else if (columnType.getName().equals(DataType.Name.INET)) {
                paramValue = new ParamValue(row.isNull(i) ? null :row.getInet(i).toString());
            } else if (columnType.getName().equals(DataType.Name.INT)) {
                paramValue = new ParamValue(row.isNull(i) ? null : Integer.toString(row.getInt(i)));
            } else if (columnType.getName().equals(DataType.Name.LIST)) {
                paramValue = new ParamValue(row.isNull(i) ? null : Arrays.toString(row.getList(i, Object.class)
                                                                                      .toArray()));
            } else if (columnType.getName().equals(DataType.Name.MAP)) {
                paramValue = new ParamValue(row.getMap(i, Object.class, Object.class).toString());
            } else if (columnType.getName().equals(DataType.Name.SET)) {
                paramValue = new ParamValue(row.getSet(i, Object.class).toString());
            } else if (columnType.getName().equals(DataType.Name.TIMESTAMP)) {
                paramValue = new ParamValue(row.isNull(i) ? null : ConverterUtil.convertToString(row.getTimestamp(i)));
            } else if (columnType.getName().equals(DataType.Name.TIMEUUID)) {
                paramValue = new ParamValue(row.getUUID(i).toString());
            } else if (columnType.getName().equals(DataType.Name.UUID)) {
                paramValue = new ParamValue(row.getUUID(i).toString());
            } else if (columnType.getName().equals(DataType.Name.VARINT)) {
                paramValue = new ParamValue(row.getVarint(i).toString());
            } else if (columnType.getName().equals(DataType.Name.UDT)) {
                paramValue = new ParamValue(row.isNull(i) ? null : ConverterUtil.convertToString(row.getUDTValue(i)));
            } else if (columnType.getName().equals(DataType.Name.TUPLE)) {
                paramValue = new ParamValue(row.isNull(i) ? null : ConverterUtil.convertToString(row.getTupleValue(i)));
            } else if (columnType.getName().equals(DataType.Name.TINYINT)) {
                paramValue = new ParamValue(row.isNull(i) ? null : Integer.toString(row.getByte(i)));
            } else if (columnType.getName().equals(DataType.Name.SMALLINT)) {
                paramValue = new ParamValue(row.isNull(i) ? null : Integer.toString(row.getShort(i)));
            } else if (columnType.getName().equals(DataType.Name.TIME)) {
                paramValue = new ParamValue(row.isNull(i) ? null : ConverterUtil.convertToString(row.getTime(i)));
            } else if (columnType.getName().equals(DataType.Name.DATE)) {
                paramValue = new ParamValue(row.isNull(i) ? null : ConverterUtil.convertToString(row.getDate(i)));
            }
            entry.addValue(useColumnNumbers ? Integer.toString(i) : defs.getName(i), paramValue);
        }
        return entry;
    }

    private String base64EncodeByteBuffer(ByteBuffer byteBuffer) throws DataServiceFault {
        byte[] data = byteBuffer.array();
        byte[] base64Data = Base64.encodeBase64(data);
        try {
            return new String(base64Data, DBConstants.DEFAULT_CHAR_SET_TYPE);
        } catch (UnsupportedEncodingException e) {
            throw new DataServiceFault(e, "Error in encoding result binary data: " + e.getMessage());
        }
    }

    private ByteBuffer base64DecodeByteBuffer(String data) throws DataServiceFault {
        try {
            byte[] buff = Base64.decodeBase64(data.getBytes(DBConstants.DEFAULT_CHAR_SET_TYPE));
            ByteBuffer result = ByteBuffer.allocate(buff.length);
            result.put(buff);
            return result;
        } catch (UnsupportedEncodingException e) {
            throw new DataServiceFault(e, "Error in decoding input base64 data: " + e.getMessage());
        }
    }

}
