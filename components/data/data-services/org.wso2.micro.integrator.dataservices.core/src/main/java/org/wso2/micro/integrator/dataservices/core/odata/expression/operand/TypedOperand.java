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
import org.apache.olingo.commons.api.http.HttpStatusCode;
import org.apache.olingo.server.api.ODataApplicationException;
import org.wso2.micro.integrator.dataservices.core.odata.ODataConstants;

import java.math.BigDecimal;
import java.util.Locale;

public class TypedOperand extends VisitorOperand {

    final private EdmType type;
    final private EdmProperty edmProperty;

    public TypedOperand(final Object value, final EdmType type) {
        super(value);
        this.type = type;
        edmProperty = null;
    }

    public TypedOperand(final Object value, final EdmType type, final EdmProperty edmProperty) {
        super(value);
        this.type = type;
        this.edmProperty = edmProperty;
    }

    @Override
    public TypedOperand asTypedOperand() throws ODataApplicationException {
        if (!isNull() && value.getClass() != getDefaultType((EdmPrimitiveType) type)) {
            return asTypedOperand((EdmPrimitiveType) type);
        }
        return this;
    }

    @Override
    public TypedOperand asTypedOperand(final EdmPrimitiveType... asTypes) throws ODataApplicationException {
        if (is(ODataConstants.primitiveNull)) {
            return this;
        } else if (isNull()) {
            return new TypedOperand(null, asTypes[0]);
        }
        Object newValue = null;
        for (EdmPrimitiveType asType : asTypes) {
            // Use BigDecimal for unlimited precision
            if (asType.equals(ODataConstants.primitiveDouble) || asType.equals(ODataConstants.primitiveSingle) ||
                asType.equals(ODataConstants.primitiveDecimal)) {
                try {
                    newValue = new BigDecimal(value.toString());
                } catch (NumberFormatException e) {
                    // Nothing to do
                }
            } else {
                // Use type conversion of EdmPrimitive types
                try {
                    final String literal = getLiteral(value);
                    newValue = tryCast(literal, asType);
                } catch (EdmPrimitiveTypeException e) {
                    // Nothing to do
                }
            }
            if (newValue != null) {
                return new TypedOperand(newValue, asType);
            }
        }
        throw new ODataApplicationException("Cast failed", HttpStatusCode.BAD_REQUEST.getStatusCode(), Locale.ROOT);
    }

    public TypedOperand castToCommonType(final VisitorOperand otherOperand) throws ODataApplicationException {
        final TypedOperand other = otherOperand.asTypedOperand();
        final EdmType oType = other.getType();
        // In case of numeric values make sure that the EDM type is equals, check also the java type.
        // So it is possible, that there is an conversation even if the same
        // EdmType is provided.
        // For example consider an Edm16 (internal Integer) and Edm16(internal
        // Short)
        // shortInstance.equals(intInstance) will always be false!
        if (type == oType && value != null && other.getValue() != null &&
            value.getClass() == other.getValue().getClass()) {
            return this;
        } else if (is(ODataConstants.primitiveNull) || other.is(ODataConstants.primitiveNull)) {
            return this;
        }
        if (type.equals(ODataConstants.primitiveDouble) || oType.equals(ODataConstants.primitiveDouble)) {
            return asTypedOperand(ODataConstants.primitiveDouble);
        } else if (type.equals(ODataConstants.primitiveSingle) || oType.equals(ODataConstants.primitiveSingle)) {
            return asTypedOperand(ODataConstants.primitiveSingle);
        } else if (type.equals(ODataConstants.primitiveDecimal) || oType.equals(ODataConstants.primitiveDecimal)) {
            return asTypedOperand(ODataConstants.primitiveDecimal);
        } else if (type.equals(ODataConstants.primitiveInt64) || oType.equals(ODataConstants.primitiveInt64)) {
            return asTypedOperand(ODataConstants.primitiveInt64);
        } else if (type.equals(ODataConstants.primitiveInt32) || oType.equals(ODataConstants.primitiveInt32)) {
            return asTypedOperand(ODataConstants.primitiveInt32);
        } else if (type.equals(ODataConstants.primitiveInt16) || oType.equals(ODataConstants.primitiveInt16)) {
            return asTypedOperand(ODataConstants.primitiveInt16);
        } else {
            return asTypedOperand((EdmPrimitiveType) type);
        }
    }

    public EdmType getType() {
        return type;
    }

    public <T> T getTypedValue(final Class<T> clazz) {
        return clazz.cast(value);
    }

    public boolean isNull() {
        return is(ODataConstants.primitiveNull) || value == null;
    }

    public boolean isIntegerType() {
        return is(ODataConstants.primitiveNull, ODataConstants.primitiveByte, ODataConstants.primitiveSByte,
                  ODataConstants.primitiveInt16, ODataConstants.primitiveInt32, ODataConstants.primitiveInt64);
    }

    public boolean isDecimalType() {
        return is(ODataConstants.primitiveNull, ODataConstants.primitiveSingle, ODataConstants.primitiveDouble,
                  ODataConstants.primitiveDecimal);
    }

    public boolean is(final EdmPrimitiveType... types) {
        for (EdmPrimitiveType type : types) {
            if (type.equals(this.type)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public EdmProperty getEdmProperty() {
        return edmProperty;
    }

    private String getLiteral(final Object value) throws EdmPrimitiveTypeException {
        final EdmProperty edmProperty = getEdmProperty();
        String uriLiteral;
        if (edmProperty != null) {
            uriLiteral = ((EdmPrimitiveType) type)
                    .valueToString(value, edmProperty.isNullable(), edmProperty.getMaxLength(),
                                   edmProperty.getPrecision(), edmProperty.getScale(), edmProperty.isUnicode());
        } else {
            uriLiteral = ((EdmPrimitiveType) type).valueToString(value, null, null, null, null, null);
        }
        return ((EdmPrimitiveType) type).toUriLiteral(uriLiteral);
    }
}
