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
import org.apache.olingo.commons.api.edm.EdmProperty;
import org.apache.olingo.commons.api.http.HttpStatusCode;
import org.apache.olingo.server.api.ODataApplicationException;
import org.wso2.micro.integrator.dataservices.core.odata.ODataConstants;

import java.util.Locale;

public class UntypedOperand extends VisitorOperand {

    public UntypedOperand(final String literal) {
        super(literal);
    }

    @Override
    public TypedOperand asTypedOperand() throws ODataApplicationException {
        return determineType();
    }

    @Override
    public TypedOperand asTypedOperand(final EdmPrimitiveType... types) throws ODataApplicationException {
        final String literal = (String) value;
        Object newValue;
        // First try the null literal
        if ((newValue = tryCast(literal, ODataConstants.primitiveNull)) != null) {
            return new TypedOperand(newValue, ODataConstants.primitiveNull);
        }
        // Than try the given types
        for (EdmPrimitiveType type : types) {
            newValue = tryCast(literal, type);
            if (newValue != null) {
                return new TypedOperand(newValue, type);
            }
        }
        throw new ODataApplicationException("Cast failed", HttpStatusCode.INTERNAL_SERVER_ERROR.getStatusCode(),
                                            Locale.ROOT);
    }

    public TypedOperand determineType() throws ODataApplicationException {
        final String literal = (String) value;
        Object newValue;
        // Null literal
        if (ODataConstants.primitiveNull.validate(literal, null, null, null, null, null)) {
            return new TypedOperand(null, ODataConstants.primitiveNull);
        }
        // String
        if ((newValue = tryCast(literal, ODataConstants.primitiveString)) != null) {
            return new TypedOperand(newValue, ODataConstants.primitiveString);
        }
        // Boolean
        if ((newValue = tryCast(literal, ODataConstants.primitiveBoolean)) != null) {
            return new TypedOperand(newValue, ODataConstants.primitiveBoolean);
        }
        // Date
        if ((newValue = tryCast(literal, ODataConstants.primitiveDateTimeOffset)) != null) {
            return new TypedOperand(newValue, ODataConstants.primitiveDateTimeOffset);
        }
        if ((newValue = tryCast(literal, ODataConstants.primitiveDate)) != null) {
            return new TypedOperand(newValue, ODataConstants.primitiveDate);
        }
        if ((newValue = tryCast(literal, ODataConstants.primitiveTimeOfDay)) != null) {
            return new TypedOperand(newValue, ODataConstants.primitiveTimeOfDay);
        }
        if ((newValue = tryCast(literal, ODataConstants.primitiveDuration)) != null) {
            return new TypedOperand(newValue, ODataConstants.primitiveDuration);
        }
        // Integer
        if ((newValue = tryCast(literal, ODataConstants.primitiveSByte)) != null) {
            return new TypedOperand(newValue, ODataConstants.primitiveSByte);
        }
        if ((newValue = tryCast(literal, ODataConstants.primitiveByte)) != null) {
            return new TypedOperand(newValue, ODataConstants.primitiveByte);
        }
        if ((newValue = tryCast(literal, ODataConstants.primitiveInt16)) != null) {
            return new TypedOperand(newValue, ODataConstants.primitiveInt16);
        }
        if ((newValue = tryCast(literal, ODataConstants.primitiveInt32)) != null) {
            return new TypedOperand(newValue, ODataConstants.primitiveInt32);
        }
        if ((newValue = tryCast(literal, ODataConstants.primitiveInt64)) != null) {
            return new TypedOperand(newValue, ODataConstants.primitiveInt64);
        }
        // Decimal
        if ((newValue = tryCast(literal, ODataConstants.primitiveDecimal)) != null) {
            return new TypedOperand(newValue, ODataConstants.primitiveDecimal);
        }
        // Float
        if ((newValue = tryCast(literal, ODataConstants.primitiveSingle)) != null) {
            return new TypedOperand(newValue, ODataConstants.primitiveSingle);
        }
        if ((newValue = tryCast(literal, ODataConstants.primitiveGuid)) != null) {
            return new TypedOperand(newValue, ODataConstants.primitiveGuid);
        }
        if ((newValue = tryCast(literal, ODataConstants.primitiveDouble)) != null) {
            return new TypedOperand(newValue, ODataConstants.primitiveDouble);
        }
        if ((newValue = tryCast(literal, ODataConstants.primitiveBinary)) != null) {
            return new TypedOperand(newValue, ODataConstants.primitiveBinary);
        }
        throw new ODataApplicationException("Could not determine type for literal " + literal,
                                            HttpStatusCode.INTERNAL_SERVER_ERROR.getStatusCode(), Locale.ROOT);
    }

    @Override
    public EdmProperty getEdmProperty() {
        return null;
    }
}
