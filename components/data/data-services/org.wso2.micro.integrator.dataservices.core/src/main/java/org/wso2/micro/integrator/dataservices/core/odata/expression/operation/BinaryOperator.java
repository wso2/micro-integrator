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
package org.wso2.micro.integrator.dataservices.core.odata.expression.operation;

import org.apache.olingo.commons.api.edm.EdmType;
import org.apache.olingo.commons.api.http.HttpStatusCode;
import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.uri.queryoption.expression.BinaryOperatorKind;
import org.wso2.micro.integrator.dataservices.core.odata.ODataConstants;
import org.wso2.micro.integrator.dataservices.core.odata.expression.operand.TypedOperand;
import org.wso2.micro.integrator.dataservices.core.odata.expression.operand.VisitorOperand;
import org.wso2.micro.integrator.dataservices.core.odata.expression.primitive.EdmNull;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Locale;

public class BinaryOperator {

    private TypedOperand right;
    private TypedOperand left;

    public BinaryOperator(final VisitorOperand leftOperand, final VisitorOperand rightOperand)
            throws ODataApplicationException {
        left = leftOperand.asTypedOperand();
        right = rightOperand.asTypedOperand();
        left = left.castToCommonType(right);
        right = right.castToCommonType(left);
    }

    public VisitorOperand andOperator() throws ODataApplicationException {
        Boolean result = null;
        if (left.is(ODataConstants.primitiveBoolean) && right.is(ODataConstants.primitiveBoolean)) {
            if (Boolean.TRUE.equals(left.getValue()) && Boolean.TRUE.equals(right.getValue())) {
                result = true;
            } else if (Boolean.FALSE.equals(left.getValue()) || Boolean.FALSE.equals(right.getValue())) {
                result = false;
            }
            return new TypedOperand(result, ODataConstants.primitiveBoolean);
        } else {
            throw new ODataApplicationException("Add operator needs two binary operands",
                                                HttpStatusCode.BAD_REQUEST.getStatusCode(), Locale.ROOT);
        }
    }

    public VisitorOperand orOperator() throws ODataApplicationException {
        Boolean result = null;
        if (left.is(ODataConstants.primitiveBoolean) && right.is(ODataConstants.primitiveBoolean)) {
            if (Boolean.TRUE.equals(left.getValue()) || Boolean.TRUE.equals(right.getValue())) {
                result = true;
            } else if (Boolean.FALSE.equals(left.getValue()) && Boolean.FALSE.equals(right.getValue())) {
                result = false;
            }
            return new TypedOperand(result, ODataConstants.primitiveBoolean);
        } else {
            throw new ODataApplicationException("Or operator needs two binary operands",
                                                HttpStatusCode.BAD_REQUEST.getStatusCode(), Locale.ROOT);
        }
    }

    public VisitorOperand equalsOperator() {
        final boolean result = isBinaryComparisonNecessary() && binaryComparison(ODataConstants.EQUALS);
        return new TypedOperand(result, ODataConstants.primitiveBoolean);
    }

    public VisitorOperand notEqualsOperator() {
        final VisitorOperand equalsOperator = equalsOperator();
        return new TypedOperand(!(Boolean) equalsOperator.getValue(), ODataConstants.primitiveBoolean);
    }

    private boolean isBinaryComparisonNecessary() {
        // binaryComparison() need to be called, if both operand are either null or not null, ^ (bitwise XOR)
        return !(left.isNull() ^ right.isNull());
    }

    public VisitorOperand greaterEqualsOperator() {
        final boolean result =
                isBinaryComparisonNecessary() && binaryComparison(ODataConstants.GREATER_THAN, ODataConstants.EQUALS);
        return new TypedOperand(result, ODataConstants.primitiveBoolean);
    }

    public VisitorOperand greaterThanOperator() {
        final boolean result = isBinaryComparisonNecessary() && binaryComparison(ODataConstants.GREATER_THAN);
        return new TypedOperand(result, ODataConstants.primitiveBoolean);
    }

    public VisitorOperand lessEqualsOperator() {
        final boolean result =
                isBinaryComparisonNecessary() && binaryComparison(ODataConstants.LESS_THAN, ODataConstants.EQUALS);
        return new TypedOperand(result, ODataConstants.primitiveBoolean);
    }

    public VisitorOperand lessThanOperator() {
        final boolean result = isBinaryComparisonNecessary() && binaryComparison(ODataConstants.LESS_THAN);
        return new TypedOperand(result, ODataConstants.primitiveBoolean);
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private boolean binaryComparison(final int... expect) {
        int result;
        if (left.isNull() && right.isNull()) {
            result = 0; // null is equals to null
        } else {
            // left and right are not null!
            if (left.isIntegerType()) {
                result = left.getTypedValue(BigInteger.class).compareTo(right.getTypedValue(BigInteger.class));
            } else if (left.isDecimalType()) {
                result = left.getTypedValue(BigDecimal.class).compareTo(right.getTypedValue(BigDecimal.class));
            } else if (left.getValue().getClass() == right.getValue().getClass() &&
                       left.getValue() instanceof Comparable) {
                result = ((Comparable) left.getValue()).compareTo(right.getValue());
            } else {
                result = left.getValue().equals(right.getValue()) ? 0 : 1;
            }
        }
        for (int expectedValue : expect) {
            if (expectedValue == result) {
                return true;
            }
        }
        return false;
    }

    public VisitorOperand arithmeticOperator(final BinaryOperatorKind operator) throws ODataApplicationException {
        if (left.isNull() || right.isNull()) {
            return new TypedOperand(new Object(), EdmNull.getInstance());
        } else {
            if (left.isIntegerType()) {
                final BigInteger result = integerArithmeticOperation(operator);
                return new TypedOperand(result, determineResultType(result, left));
            } else if (left.isDecimalType()) {
                final BigDecimal result = decimalArithmeticOperation(operator);
                return new TypedOperand(result, determineResultType(result, left));
            } else if (left.is(ODataConstants.primitiveDate, ODataConstants.primitiveDuration,
                               ODataConstants.primitiveDateTimeOffset)) {
                return dateArithmeticOperation(operator);
            } else {
                throw new ODataApplicationException("Invalid type", HttpStatusCode.BAD_REQUEST.getStatusCode(),
                                                    Locale.ROOT);
            }
        }
    }

    private EdmType determineResultType(final Number arithmeticResult, final TypedOperand leftOperand) {
        // Left and right operand have the same typed, so it is enough to check the type of the left operand
        if (leftOperand.isDecimalType()) {
            final BigDecimal value = (BigDecimal) arithmeticResult;
            if (value.compareTo(ODataConstants.EDM_SINGLE_MIN) >= 0 &&
                value.compareTo(ODataConstants.EDM_SINGLE_MAX) <= 0) {
                return ODataConstants.primitiveSingle;
            } else {
                return ODataConstants.primitiveDouble;
            }
        } else {
            final BigInteger value = (BigInteger) arithmeticResult;

            if (value.compareTo(ODataConstants.EDN_SBYTE_MAX) <= 0 &&
                value.compareTo(ODataConstants.EDM_SBYTE_MIN) >= 0) {
                return ODataConstants.primitiveSByte;
            }
            if (value.compareTo(ODataConstants.EDM_BYTE_MAX) <= 0 &&
                value.compareTo(ODataConstants.EDM_BYTE_MIN) >= 0) {
                return ODataConstants.primitiveByte;
            }
            if (value.compareTo(ODataConstants.EDM_INT16_MAX) <= 0 &&
                value.compareTo(ODataConstants.EDM_INT16_MIN) >= 0) {
                return ODataConstants.primitiveInt16;
            }
            if (value.compareTo(ODataConstants.EDM_INT32_MAX) <= 0 &&
                value.compareTo(ODataConstants.EDM_INT32_MIN) >= 0) {
                return ODataConstants.primitiveInt32;
            }
            if (value.compareTo(ODataConstants.EDM_INT64_MAX) <= 0 &&
                value.compareTo(ODataConstants.EDM_INT64_MIN) >= 0) {
                return ODataConstants.primitiveInt64;
            }
            // Choose double instead single because precision is higher (52 bits instead of 23)
            return ODataConstants.primitiveDouble;
        }
    }

    private VisitorOperand dateArithmeticOperation(final BinaryOperatorKind operator) throws ODataApplicationException {
        VisitorOperand result = null;
        if (left.is(ODataConstants.primitiveDate)) {
            if (right.is(ODataConstants.primitiveDate) && operator == BinaryOperatorKind.SUB) {
                long millis = left.getTypedValue(Calendar.class).getTimeInMillis() -
                              left.getTypedValue(Calendar.class).getTimeInMillis();
                result = new TypedOperand(new BigDecimal(millis).divide(ODataConstants.FACTOR_SECOND),
                                          ODataConstants.primitiveDuration);
            } else if (right.is(ODataConstants.primitiveDuration) && operator == BinaryOperatorKind.ADD) {
                long millis = left.getTypedValue(Calendar.class).getTimeInMillis() +
                              (right.getTypedValue(BigDecimal.class).longValue() * ODataConstants.FACTOR_SECOND_INT);
                result = new TypedOperand(new Timestamp(millis), ODataConstants.primitiveDateTimeOffset);
            } else if (right.is(ODataConstants.primitiveDuration) && operator == BinaryOperatorKind.SUB) {
                long millis = left.getTypedValue(Calendar.class).getTimeInMillis() -
                              (right.getTypedValue(BigDecimal.class).longValue() * ODataConstants.FACTOR_SECOND_INT);
                result = new TypedOperand(new Timestamp(millis), ODataConstants.primitiveDateTimeOffset);
            }
        } else if (left.is(ODataConstants.primitiveDuration)) {
            if (right.is(ODataConstants.primitiveDuration) && operator == BinaryOperatorKind.ADD) {
                long seconds = left.getTypedValue(BigDecimal.class).longValue() +
                               right.getTypedValue(BigDecimal.class).longValue();
                result = new TypedOperand(new BigDecimal(seconds), ODataConstants.primitiveDuration);
            } else if (right.is(ODataConstants.primitiveDuration) && operator == BinaryOperatorKind.SUB) {
                long seconds = left.getTypedValue(BigDecimal.class).longValue() -
                               right.getTypedValue(BigDecimal.class).longValue();
                result = new TypedOperand(new BigDecimal(seconds), ODataConstants.primitiveDuration);
            }
        } else if (left.is(ODataConstants.primitiveDateTimeOffset)) {
            if (right.is(ODataConstants.primitiveDuration) && operator == BinaryOperatorKind.ADD) {
                long millis = left.getTypedValue(Timestamp.class).getTime() +
                              (right.getTypedValue(BigDecimal.class).longValue() * ODataConstants.FACTOR_SECOND_INT);
                result = new TypedOperand(new Timestamp(millis), ODataConstants.primitiveDateTimeOffset);
            } else if (right.is(ODataConstants.primitiveDuration) && operator == BinaryOperatorKind.SUB) {
                long millis = left.getTypedValue(Timestamp.class).getTime() -
                              (right.getTypedValue(BigDecimal.class).longValue() * ODataConstants.FACTOR_SECOND_INT);
                result = new TypedOperand(new Timestamp(millis), ODataConstants.primitiveDateTimeOffset);
            } else if (right.is(ODataConstants.primitiveDateTimeOffset) && operator == BinaryOperatorKind.SUB) {
                long millis =
                        left.getTypedValue(Timestamp.class).getTime() - right.getTypedValue(Timestamp.class).getTime();
                result = new TypedOperand(new BigDecimal(millis).divide(ODataConstants.FACTOR_SECOND),
                                          ODataConstants.primitiveDuration);
            }
        }
        if (result == null) {
            throw new ODataApplicationException("Invalid operation / operand",
                                                HttpStatusCode.BAD_REQUEST.getStatusCode(), Locale.ROOT);
        } else {
            return result;
        }
    }

    private BigDecimal decimalArithmeticOperation(final BinaryOperatorKind operator) throws ODataApplicationException {
        final BigDecimal left = this.left.getTypedValue(BigDecimal.class);
        final BigDecimal right = this.right.getTypedValue(BigDecimal.class);
        switch (operator) {
            case ADD:
                return left.add(right);
            case DIV:
                return left.divide(right);
            case MUL:
                return left.multiply(right);
            case SUB:
                return left.subtract(right);
            default:
                throw new ODataApplicationException("Operator not valid", HttpStatusCode.BAD_REQUEST.getStatusCode(),
                                                    Locale.ROOT);
        }
    }

    private BigInteger integerArithmeticOperation(final BinaryOperatorKind operator) throws ODataApplicationException {
        final BigInteger left = this.left.getTypedValue(BigInteger.class);
        final BigInteger right = this.right.getTypedValue(BigInteger.class);
        switch (operator) {
            case ADD:
                return left.add(right);
            case DIV:
                return left.divide(right);
            case MUL:
                return left.multiply(right);
            case SUB:
                return left.subtract(right);
            case MOD:
                return left.mod(right);
            default:
                throw new ODataApplicationException("Operator not valid", HttpStatusCode.BAD_REQUEST.getStatusCode(),
                                                    Locale.ROOT);
        }
    }
}
