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
package org.wso2.micro.integrator.dataservices.core.odata.expression.operand;

import org.apache.olingo.commons.api.edm.EdmPrimitiveType;
import org.apache.olingo.commons.api.edm.EdmPrimitiveTypeException;
import org.apache.olingo.commons.api.edm.EdmProperty;
import org.apache.olingo.commons.api.edm.EdmType;
import org.apache.olingo.server.api.ODataApplicationException;
import org.wso2.micro.integrator.dataservices.core.odata.ODataConstants;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.HashMap;

public abstract class VisitorOperand {
    final static private HashMap<EdmType, Class<?>> defaultTypeMapping = new HashMap<>();
    protected Object value;

    public VisitorOperand(final Object value) {
        this.value = value;
        defaultTypeMapping.put(ODataConstants.primitiveByte, BigInteger.class);
        defaultTypeMapping.put(ODataConstants.primitiveSByte, BigInteger.class);
        defaultTypeMapping.put(ODataConstants.primitiveInt16, BigInteger.class);
        defaultTypeMapping.put(ODataConstants.primitiveInt32, BigInteger.class);
        defaultTypeMapping.put(ODataConstants.primitiveInt64, BigInteger.class);
        defaultTypeMapping.put(ODataConstants.primitiveSingle, BigDecimal.class);
        defaultTypeMapping.put(ODataConstants.primitiveDouble, BigDecimal.class);
        defaultTypeMapping.put(ODataConstants.primitiveDecimal, BigDecimal.class);
    }

    public abstract TypedOperand asTypedOperand() throws ODataApplicationException;

    public abstract TypedOperand asTypedOperand(EdmPrimitiveType[] types) throws ODataApplicationException;

    public abstract EdmProperty getEdmProperty();

    public Object getValue() {
        return value;
    }

    protected Object castTo(final String value, final EdmPrimitiveType type) throws EdmPrimitiveTypeException {
        final EdmProperty edmProperty = getEdmProperty();
        if (edmProperty != null) {
            return type.valueOfString(value, edmProperty.isNullable(), edmProperty.getMaxLength(),
                                      edmProperty.getPrecision(), edmProperty.getScale(), edmProperty.isUnicode(),
                                      getDefaultType(type));
        } else {
            return type.valueOfString(value, null, null, null, null, null, getDefaultType(type));
        }
    }

    protected Class<?> getDefaultType(final EdmPrimitiveType type) {
        return defaultTypeMapping.get(type) != null ? defaultTypeMapping.get(type) : type.getDefaultType();
    }

    protected Object tryCast(final String literal, final EdmPrimitiveType type) {
        try {
            return castTo(type.fromUriLiteral(literal), type);
        } catch (EdmPrimitiveTypeException e) {
            return null;
        }
    }

}
