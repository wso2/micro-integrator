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

package org.wso2.micro.integrator.mediator.publishevent;

import org.apache.synapse.MessageContext;
import org.apache.synapse.SynapseException;
import org.apache.synapse.util.xpath.SynapseXPath;
import org.wso2.carbon.databridge.commons.AttributeType;

/**
 * Property of a Stream Definition.
 */
public class Property {
    private String key = "";
    private String value = null;
    private SynapseXPath expression = null;
    private String defaultValue = "";
    private String type = "";
    public static final String DATA_TYPE_STRING = "STRING";
    public static final String DATA_TYPE_INTEGER = "INTEGER";
    public static final String DATA_TYPE_FLOAT = "FLOAT";
    public static final String DATA_TYPE_DOUBLE = "DOUBLE";
    public static final String DATA_TYPE_BOOLEAN = "BOOLEAN";
    public static final String DATA_TYPE_LONG = "LONG";

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    public SynapseXPath getExpression() {
        return expression;
    }

    public void setExpression(SynapseXPath expression) {
        this.expression = expression;
    }

    public String getType() {
        return type;
    }

    /**
     * Returns the data bridge attribute type of this object.
     *
     * @return Data bridge attribute type of this object
     */
    public AttributeType getDatabridgeAttributeType() throws SynapseException {
        if (DATA_TYPE_STRING.equals(type)) {
            return AttributeType.STRING;
        }
        if (DATA_TYPE_INTEGER.equals(type)) {
            return AttributeType.INT;
        }
        if (DATA_TYPE_FLOAT.equals(type)) {
            return AttributeType.FLOAT;
        }
        if (DATA_TYPE_DOUBLE.equals(type)) {
            return AttributeType.DOUBLE;
        }
        if (DATA_TYPE_BOOLEAN.equals(type)) {
            return AttributeType.BOOL;
        }
        if (DATA_TYPE_LONG.equals(type)) {
            return AttributeType.LONG;
        }
        throw new SynapseException(
                "Invalid attribute type '" + type + "' for " + PublishEventMediatorFactory.getTagName() +
                        " mediator attribute");
    }

    public void setType(String type) {
        this.type = type;
    }

    /**
     * Extracts the property value from message context based on either value of expression attribute.
     *
     * @param messageContext Message context from which the value should be extracted
     * @return Extracted property value
     */
    public Object extractPropertyValue(MessageContext messageContext) throws SynapseException {
        String stringProperty;
        if (expression != null) {
            stringProperty = expression.stringValueOf(messageContext);
        } else {
            stringProperty = getValue();
        }
        //TODO: find whether exprssion didn't match and use default value then only
        if (stringProperty == null || "".equals(stringProperty)) {
            stringProperty = defaultValue;
        }

        if (DATA_TYPE_STRING.equals(getType())) {
            return stringProperty;
        }
        if (DATA_TYPE_INTEGER.equals(getType())) {
            return PropertyTypeConverter.convertToInt(stringProperty);
        }
        if (DATA_TYPE_FLOAT.equals(getType())) {
            return PropertyTypeConverter.convertToFloat(stringProperty);
        }
        if (DATA_TYPE_DOUBLE.equals(getType())) {
            return PropertyTypeConverter.convertToDouble(stringProperty);
        }
        if (DATA_TYPE_BOOLEAN.equals(getType())) {
            return PropertyTypeConverter.convertToBoolean(stringProperty);
        }
        if (DATA_TYPE_LONG.equals(getType())) {
            return PropertyTypeConverter.convertToLong(stringProperty);
        }
        return stringProperty;
    }
}
