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

import org.apache.olingo.commons.api.edm.EdmPrimitiveType;
import org.apache.olingo.commons.api.edm.EdmType;
import org.apache.olingo.commons.api.http.HttpStatusCode;
import org.apache.olingo.server.api.ODataApplicationException;
import org.wso2.micro.integrator.dataservices.core.odata.ODataConstants;
import org.wso2.micro.integrator.dataservices.core.odata.expression.operand.TypedOperand;
import org.wso2.micro.integrator.dataservices.core.odata.expression.operand.VisitorOperand;
import org.wso2.micro.integrator.dataservices.core.odata.expression.primitive.EdmNull;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.math.RoundingMode;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class MethodCallOperator {

    final private List<VisitorOperand> parameters;

    public MethodCallOperator(final List<VisitorOperand> parameters) {
        this.parameters = parameters;
    }

    public VisitorOperand endsWith() throws ODataApplicationException {
        return stringFunction(new StringFunction() {
            @Override
            public Object perform(final List<String> params) {
                return params.get(0).endsWith(params.get(1));
            }
        }, ODataConstants.primitiveBoolean);
    }

    public VisitorOperand indexOf() throws ODataApplicationException {
        return stringFunction(new StringFunction() {
            @Override
            public Object perform(final List<String> params) {
                // If the first string do not contain the second string, return -1.
                return params.get(0).indexOf(params.get(1));
            }
        }, ODataConstants.primitiveInt32);
    }

    public VisitorOperand startsWith() throws ODataApplicationException {
        return stringFunction(new StringFunction() {
            @Override
            public Object perform(final List<String> params) {
                return params.get(0).startsWith(params.get(1));
            }
        }, ODataConstants.primitiveBoolean);
    }

    public VisitorOperand toLower() throws ODataApplicationException {
        return stringFunction(new StringFunction() {
            @Override
            public Object perform(final List<String> params) {
                return params.get(0).toLowerCase();
            }
        }, ODataConstants.primitiveString);
    }

    public VisitorOperand toUpper() throws ODataApplicationException {
        return stringFunction(new StringFunction() {
            @Override
            public Object perform(final List<String> params) {
                return params.get(0).toUpperCase();
            }
        }, ODataConstants.primitiveString);
    }

    public VisitorOperand trim() throws ODataApplicationException {
        return stringFunction(new StringFunction() {
            @Override
            public Object perform(final List<String> params) {
                return params.get(0).trim();
            }
        }, ODataConstants.primitiveString);
    }

    public VisitorOperand substring() throws ODataApplicationException {
        final TypedOperand valueOperand = parameters.get(0).asTypedOperand();
        final TypedOperand startOperand = parameters.get(1).asTypedOperand();
        if (valueOperand.isNull() || startOperand.isNull()) {
            return new TypedOperand(null, ODataConstants.primitiveString);
        } else if (valueOperand.is(ODataConstants.primitiveString) && startOperand.isIntegerType()) {
            final String value = valueOperand.getTypedValue(String.class);
            int start = Math.min(startOperand.getTypedValue(BigInteger.class).intValue(), value.length());
            start = start < 0 ? 0 : start;
            int end = value.length();
            if (parameters.size() == 3) {
                final TypedOperand lengthOperand = parameters.get(2).asTypedOperand();
                if (lengthOperand.isNull()) {
                    return new TypedOperand(null, ODataConstants.primitiveString);
                } else if (lengthOperand.isIntegerType()) {
                    end = Math.min(start + lengthOperand.getTypedValue(BigInteger.class).intValue(), value.length());
                    end = end < 0 ? 0 : end;
                } else {
                    throw new ODataApplicationException("Third substring parameter should be Edm.Int32",
                                                        HttpStatusCode.BAD_REQUEST.getStatusCode(), Locale.ROOT);
                }
            }
            return new TypedOperand(value.substring(start, end), ODataConstants.primitiveString);
        } else {
            throw new ODataApplicationException(
                    "Substring has invalid parameters. First parameter should be Edm.String," +
                    " second parameter should be Edm.Int32", HttpStatusCode.BAD_REQUEST.getStatusCode(), Locale.ROOT);
        }
    }

    public VisitorOperand contains() throws ODataApplicationException {
        return stringFunction(new StringFunction() {
            @Override
            public Object perform(final List<String> params) {
                return params.get(0).contains(params.get(1));
            }
        }, ODataConstants.primitiveBoolean);
    }

    public VisitorOperand concat() throws ODataApplicationException {
        return stringFunction(new StringFunction() {
            @Override
            public Object perform(final List<String> params) {
                return params.get(0) + params.get(1);
            }
        }, ODataConstants.primitiveString);
    }

    public VisitorOperand length() throws ODataApplicationException {
        return stringFunction(new StringFunction() {
            @Override
            public Object perform(final List<String> params) {
                return params.get(0).length();
            }
        }, ODataConstants.primitiveInt32);
    }

    public VisitorOperand year() throws ODataApplicationException {
        return dateFunction(new DateFunction() {
            @Override
            public Object perform(final Calendar calendar, final TypedOperand operand) {
                return calendar.get(Calendar.YEAR);
            }
        }, ODataConstants.primitiveInt32, ODataConstants.primitiveDateTimeOffset, ODataConstants.primitiveDate);
    }

    public VisitorOperand month() throws ODataApplicationException {
        return dateFunction(new DateFunction() {
            @Override
            public Object perform(final Calendar calendar, final TypedOperand operand) {
                // Month is 0-based!
                return calendar.get(Calendar.MONTH) + 1;
            }
        }, ODataConstants.primitiveInt32, ODataConstants.primitiveDateTimeOffset, ODataConstants.primitiveDate);
    }

    public VisitorOperand day() throws ODataApplicationException {
        return dateFunction(new DateFunction() {
            @Override
            public Object perform(final Calendar calendar, final TypedOperand operand) {
                return calendar.get(Calendar.DAY_OF_MONTH);
            }
        }, ODataConstants.primitiveInt32, ODataConstants.primitiveDateTimeOffset, ODataConstants.primitiveDate);
    }

    public VisitorOperand hour() throws ODataApplicationException {
        return dateFunction(new DateFunction() {
            @Override
            public Object perform(final Calendar calendar, final TypedOperand operand) {
                return calendar.get(Calendar.HOUR_OF_DAY);
            }
        }, ODataConstants.primitiveInt32, ODataConstants.primitiveDateTimeOffset, ODataConstants.primitiveTimeOfDay);
    }

    public VisitorOperand minute() throws ODataApplicationException {
        return dateFunction(new DateFunction() {
            @Override
            public Object perform(final Calendar calendar, final TypedOperand operand) {
                return calendar.get(Calendar.MINUTE);
            }
        }, ODataConstants.primitiveInt32, ODataConstants.primitiveDateTimeOffset, ODataConstants.primitiveTimeOfDay);
    }

    public VisitorOperand second() throws ODataApplicationException {
        return dateFunction(new DateFunction() {
            @Override
            public Object perform(final Calendar calendar, final TypedOperand operand) {
                return calendar.get(Calendar.SECOND);
            }
        }, ODataConstants.primitiveInt32, ODataConstants.primitiveDateTimeOffset, ODataConstants.primitiveTimeOfDay);
    }

    public VisitorOperand fractionalSeconds() throws ODataApplicationException {
        return dateFunction(new DateFunction() {
            @Override
            public Object perform(final Calendar calendar, final TypedOperand operand) {
                if (operand.getValue() instanceof Timestamp) {
                    return new BigDecimal(operand.getTypedValue(Timestamp.class).getNanos())
                            .divide(BigDecimal.valueOf(1000 * 1000 * 1000));
                } else {
                    return new BigDecimal(calendar.get(Calendar.MILLISECOND)).divide(BigDecimal.valueOf(1000));
                }
            }
        }, ODataConstants.primitiveDecimal, ODataConstants.primitiveDateTimeOffset, ODataConstants.primitiveTimeOfDay);
    }

    public VisitorOperand round() throws ODataApplicationException {
        final TypedOperand operand = parameters.get(0).asTypedOperand();
        if (operand.isNull()) {
            return operand;
        } else if (operand.isDecimalType()) {
            return new TypedOperand(
                    operand.getTypedValue(BigDecimal.class).round(new MathContext(1, RoundingMode.HALF_UP)),
                    operand.getType());
        } else {
            throw new ODataApplicationException("Invalid type", HttpStatusCode.BAD_REQUEST.getStatusCode(),
                                                Locale.ROOT);
        }
    }

    public VisitorOperand floor() throws ODataApplicationException {
        final TypedOperand operand = parameters.get(0).asTypedOperand();
        if (operand.isNull()) {
            return operand;
        } else if (operand.isDecimalType()) {
            return new TypedOperand(
                    operand.getTypedValue(BigDecimal.class).round(new MathContext(1, RoundingMode.FLOOR)),
                    operand.getType());
        } else {
            throw new ODataApplicationException("Invalid type", HttpStatusCode.BAD_REQUEST.getStatusCode(),
                                                Locale.ROOT);
        }
    }

    public VisitorOperand ceiling() throws ODataApplicationException {
        final TypedOperand operand = parameters.get(0).asTypedOperand();
        if (operand.isNull()) {
            return operand;
        } else if (operand.isDecimalType()) {
            return new TypedOperand(
                    operand.getTypedValue(BigDecimal.class).round(new MathContext(1, RoundingMode.CEILING)),
                    operand.getType());
        } else {
            throw new ODataApplicationException("Invalid type", HttpStatusCode.BAD_REQUEST.getStatusCode(),
                                                Locale.ROOT);
        }
    }

    private interface StringFunction {
        Object perform(List<String> params);
    }

    private interface DateFunction {
        Object perform(Calendar calendar, TypedOperand operand);
    }

    private VisitorOperand dateFunction(final DateFunction function, final EdmType returnType,
                                        final EdmPrimitiveType... expectedTypes) throws ODataApplicationException {
        final TypedOperand operand = parameters.get(0).asTypedOperand();

        if (operand.isNull()) {
            return new TypedOperand(null, EdmNull.getInstance());
        } else {
            if (operand.is(expectedTypes)) {
                Calendar calendar;
                if (operand.is(ODataConstants.primitiveDate)) {
                    calendar = operand.getTypedValue(Calendar.class);
                } else if (operand.is(ODataConstants.primitiveDateTimeOffset)) {
                    final Timestamp timestamp = operand.getTypedValue(Timestamp.class);
                    calendar = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
                    calendar.setTimeInMillis(timestamp.getTime());
                } else if (operand.is(ODataConstants.primitiveTimeOfDay)) {
                    calendar = operand.getTypedValue(Calendar.class);
                } else {
                    throw new ODataApplicationException("Invalid type", HttpStatusCode.BAD_REQUEST.getStatusCode(),
                                                        Locale.ROOT);
                }
                return new TypedOperand(function.perform(calendar, operand), returnType);
            } else {
                throw new ODataApplicationException("Invalid type", HttpStatusCode.BAD_REQUEST.getStatusCode(),
                                                    Locale.ROOT);
            }
        }
    }

    private VisitorOperand stringFunction(final StringFunction function, final EdmType returnValue)
            throws ODataApplicationException {
        List<String> stringParameters = getParametersAsString();
        if (stringParameters.contains(null)) {
            return new TypedOperand(null, EdmNull.getInstance());
        } else {
            return new TypedOperand(function.perform(stringParameters), returnValue);
        }
    }

    private List<String> getParametersAsString() throws ODataApplicationException {
        List<String> result = new ArrayList<>();
        for (VisitorOperand param : parameters) {
            TypedOperand operand = param.asTypedOperand();
            if (operand.isNull()) {
                result.add(null);
            } else if (operand.is(ODataConstants.primitiveString)) {
                result.add(operand.getTypedValue(String.class));
            } else {
                throw new ODataApplicationException("Invalid parameter. Expected Edm.String",
                                                    HttpStatusCode.BAD_REQUEST.getStatusCode(), Locale.ROOT);
            }
        }
        return result;
    }
}
