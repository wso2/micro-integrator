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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.sql.SQLException;
import java.sql.Struct;
import java.util.ArrayList;
import java.util.List;

/**
 * This class represents a value of a parameter passed to a query.
 * The value types can be of scalar type or arrays.
 */
public class ParamValue {

    public static final int PARAM_VALUE_SCALAR = 0x01;

    public static final int PARAM_VALUE_ARRAY = 0x02;

    public static final int PARAM_VALUE_UDT = 0x03;

    private int valueType;

    private List<ParamValue> arrayValue;

    private String scalarValue;

    private Struct udt;

    private static final Log log = LogFactory.getLog(ParamValue.class);

    /**
     * Constructor which creates a ParamValue object with the type.
     * @param valueType valueTypes as an integer
     */
    public ParamValue(int valueType) {
        this.valueType = valueType;
        this.scalarValue = null;
        this.arrayValue = new ArrayList<ParamValue>();
        this.udt = null;
    }

    /**
     * Constructor which creates a ParamValue object with the scalar value.
     * @param scalarValue scalar value of the ParamValue object as a string.
     */
    public ParamValue(String scalarValue) {
        this.valueType = ParamValue.PARAM_VALUE_SCALAR;
        this.scalarValue = scalarValue;
    }

    /**
     * Constructor which creates a ParamValue object with an SQL struct.
     * @param udt User Defined Type
     */
    public ParamValue(Struct udt) {
        this.valueType = PARAM_VALUE_UDT;
        this.udt = udt;
    }

    /**
     * Converts a ParamValue object containing a scalar to another ParamValue object containing
     * an array.
     * @param value ParamValue object containing a scalar value.
     * @return a ParamValue object containing an array.
     */
    public static ParamValue convertFromScalarToArray(
            ParamValue value) {
        ParamValue
                newVal = new ParamValue(
                ParamValue.PARAM_VALUE_ARRAY);
        newVal.addArrayValue(value);
        return newVal;
    }

    /**
     * This getter method exposes the value type of the ParamValue object
     * which is being used
     *
     * @return value type
     */
    public int getValueType() {
        return valueType;
    }

    /**
     * Returns a scalar value of the calling ParamValue object.
     * @return scalar value
     */
    public String getScalarValue() {
        if (this.getValueType() == PARAM_VALUE_SCALAR) {
            return scalarValue;
        } else if (this.getValueType() == PARAM_VALUE_ARRAY) {
            if (this.getArrayValue().size() > 0) {
                return arrayValue.get(0).getScalarValue();
            }
            return null;
        } else {
            return null;
        }
    }

    /**
     * Returns the value of the ParamValue object as a string depending on the type.
     *
     * If valueType = PARAM_VALUE_SCALAR then
     *    return scalar value as a string;
     * If valueType = PARAM_VALUE_ARRAY then
     *    return a comma separated string of array elements;
     * If valueType = PARAM_VALUE_UDT then
     *    return a comma separated string of UDT attributes;
     *
     * @return a string of comma separated values of an array
     * @throws SQLException SQLException
     */
    public String getValueAsString() {
        if (this.getValueType() == PARAM_VALUE_SCALAR) {
            return this.getScalarValue();
        } else if (this.getValueType() == PARAM_VALUE_ARRAY) {
            StringBuffer strBuff = new StringBuffer();
            strBuff.append("{");
            for (int i = 0; i < arrayValue.size(); i++) {
                strBuff.append(arrayValue.get(i));
                if (i + 1 < arrayValue.size()) {
                    strBuff.append(",");
                }
            }
            strBuff.append("}");
            return strBuff.toString();
        } else if (this.getValueType() == PARAM_VALUE_UDT) {
            String udtName = null;
            StringBuilder strBuilder;
            try {
                udtName = this.udt.getSQLTypeName();
                if (this.udt.getAttributes() != null) {
                    Object[] udtValues = this.udt.getAttributes();
                    strBuilder = new StringBuilder();
                    strBuilder.append("{");
                    for (int i = 0; i < udtValues.length; i++) {
                        strBuilder.append(udtValues[i]);
                        if (i + 1 < udtValues.length) {
                            strBuilder.append(",");
                        }
                    }
                    strBuilder.append("}");
                    return strBuilder.toString();
                }
                return null;
            } catch (SQLException e) {
                String errMessage = "Unable to retrieve values from UDT " + udtName;
                log.error(errMessage, e);
                return null;
            }
        } else {
            return null;
        }
    }

    @Override
    public String toString() {
        return this.getValueAsString();
    }

    /**
     * Adds the given string to current array types ParamValue.
     *
     * @param arrayElement The value to be added
     */
    public void addToArrayValue(ParamValue arrayElement) {
        try {
            arrayValue.add(arrayElement);
        } catch (ClassCastException e) {
            throw new IllegalArgumentException("Trying to add value'" + arrayElement
                    + "' added to an non-array type ParamValue");
        }
    }

    public void setScalarValue(String scalarValue) {
        this.scalarValue = scalarValue;
    }

    public void setArrayValue(List<ParamValue> arrayValue) {
        this.arrayValue = arrayValue;
    }

    public List<ParamValue> getArrayValue() {
        return arrayValue;
    }

    public void addArrayValue(ParamValue value) {
        this.arrayValue.add(value);
    }

    public Struct getUdt() {
        return udt;
    }

}
