/*
 * Copyright (c) 2022, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.micro.integrator.dataservices.core.odata.expression;

import java.text.ParseException;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import org.apache.olingo.commons.api.data.Property;
import org.apache.olingo.commons.api.edm.EdmEnumType;
import org.apache.olingo.commons.api.edm.EdmProperty;
import org.apache.olingo.commons.api.edm.EdmType;
import org.apache.olingo.commons.api.http.HttpStatusCode;
import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.uri.UriResource;
import org.apache.olingo.server.api.uri.UriResourceLambdaAll;
import org.apache.olingo.server.api.uri.UriResourceLambdaAny;
import org.apache.olingo.server.api.uri.UriResourceProperty;
import org.apache.olingo.server.api.uri.queryoption.expression.BinaryOperatorKind;
import org.apache.olingo.server.api.uri.queryoption.expression.Expression;
import org.apache.olingo.server.api.uri.queryoption.expression.ExpressionVisitor;
import org.apache.olingo.server.api.uri.queryoption.expression.Literal;
import org.apache.olingo.server.api.uri.queryoption.expression.Member;
import org.apache.olingo.server.api.uri.queryoption.expression.MethodKind;
import org.apache.olingo.server.api.uri.queryoption.expression.UnaryOperatorKind;
import org.wso2.micro.integrator.dataservices.core.odata.DataColumn;
import org.wso2.micro.integrator.dataservices.core.odata.ODataEntry;
import org.wso2.micro.integrator.dataservices.core.odata.ODataServiceFault;
import org.wso2.micro.integrator.dataservices.core.odata.ODataUtils;
import org.wso2.micro.integrator.dataservices.core.odata.expression.operand.TypedOperand;
import org.wso2.micro.integrator.dataservices.core.odata.expression.operand.UntypedOperand;
import org.wso2.micro.integrator.dataservices.core.odata.expression.operand.VisitorOperand;
import org.wso2.micro.integrator.dataservices.core.odata.expression.operation.BinaryOperator;
import org.wso2.micro.integrator.dataservices.core.odata.expression.operation.MethodCallOperator;
import org.wso2.micro.integrator.dataservices.core.odata.expression.operation.UnaryOperator;

/**
 * This class is used override comparison operations of OData entries
 * with binary operators.
 */
public class ExpressionVisitorODataEntryImpl implements ExpressionVisitor<VisitorOperand> {

    static private Collection<DataColumn> tableMetaData;
    final private ODataEntry entity;

    public ExpressionVisitorODataEntryImpl(final ODataEntry entity) {
        this.entity = entity;
    }

    public static void setTableMetaData(Collection<DataColumn> tableMetaData) {
        ExpressionVisitorODataEntryImpl.tableMetaData = tableMetaData;
    }

    @Override
    public VisitorOperand visitBinaryOperator(final BinaryOperatorKind operator, final VisitorOperand left,
                                              final VisitorOperand right) throws ODataApplicationException {
        final BinaryOperator binaryOperator = new BinaryOperator(left, right);
        switch (operator) {
            case AND:
                return binaryOperator.andOperator();
            case OR:
                return binaryOperator.orOperator();
            case EQ:
                return binaryOperator.equalsOperator();
            case NE:
                return binaryOperator.notEqualsOperator();
            case GE:
                return binaryOperator.greaterEqualsOperator();
            case GT:
                return binaryOperator.greaterThanOperator();
            case LE:
                return binaryOperator.lessEqualsOperator();
            case LT:
                return binaryOperator.lessThanOperator();
            case ADD:
                /* fall through */
            case SUB:
                /* fall through */
            case MUL:
                /* fall through */
            case DIV:
                /* fall through */
            case MOD:
                return binaryOperator.arithmeticOperator(operator);
            default:
                return throwNotImplemented();
        }
    }

    @Override
    public VisitorOperand visitUnaryOperator(final UnaryOperatorKind operator, final VisitorOperand operand)
            throws ODataApplicationException {
        final UnaryOperator unaryOperator = new UnaryOperator(operand);
        switch (operator) {
            case MINUS:
                return unaryOperator.minusOperation();
            case NOT:
                return unaryOperator.notOperation();
            default:
                // Can't happen.
                return throwNotImplemented();
        }
    }

    @Override
    public VisitorOperand visitMethodCall(final MethodKind methodCall, final List<VisitorOperand> parameters)
            throws ODataApplicationException {
        final MethodCallOperator methodCallOperation = new MethodCallOperator(parameters);

        switch (methodCall) {
            case ENDSWITH:
                return methodCallOperation.endsWith();
            case INDEXOF:
                return methodCallOperation.indexOf();
            case STARTSWITH:
                return methodCallOperation.startsWith();
            case TOLOWER:
                return methodCallOperation.toLower();
            case TOUPPER:
                return methodCallOperation.toUpper();
            case TRIM:
                return methodCallOperation.trim();
            case SUBSTRING:
                return methodCallOperation.substring();
            case CONTAINS:
                return methodCallOperation.contains();
            case CONCAT:
                return methodCallOperation.concat();
            case LENGTH:
                return methodCallOperation.length();
            case YEAR:
                return methodCallOperation.year();
            case MONTH:
                return methodCallOperation.month();
            case DAY:
                return methodCallOperation.day();
            case HOUR:
                return methodCallOperation.hour();
            case MINUTE:
                return methodCallOperation.minute();
            case SECOND:
                return methodCallOperation.second();
            case FRACTIONALSECONDS:
                return methodCallOperation.fractionalSeconds();
            case ROUND:
                return methodCallOperation.round();
            case FLOOR:
                return methodCallOperation.floor();
            case CEILING:
                return methodCallOperation.ceiling();
            default:
                return throwNotImplemented();
        }
    }

    @Override
    public VisitorOperand visitLambdaExpression(final String lambdaFunction, final String lambdaVariable,
                                                final Expression expression) throws ODataApplicationException {
        return throwNotImplemented();
    }

    @Override
    public VisitorOperand visitLiteral(Literal literal) {
        return new UntypedOperand(literal.getText());
    }

    @Override
    public VisitorOperand visitMember(Member member) throws ODataApplicationException {
        final List<UriResource> uriResourceParts = member.getResourcePath().getUriResourceParts();
        int size = uriResourceParts.size();
        if (uriResourceParts.get(0) instanceof UriResourceProperty) {
            EdmProperty currentEdmProperty = ((UriResourceProperty) uriResourceParts.get(0)).getProperty();
            Property currentProperty = null;
            try {
                currentProperty = getProperty(currentEdmProperty);
            } catch (ODataServiceFault e) {
                throw new ODataApplicationException(e.getMessage(),
                                                    HttpStatusCode.INTERNAL_SERVER_ERROR.getStatusCode(), Locale.ROOT);
            }
            return new TypedOperand(currentProperty.getValue(), currentEdmProperty.getType(), currentEdmProperty);
        } else if (uriResourceParts.get(size - 1) instanceof UriResourceLambdaAll) {
            return throwNotImplemented();
        } else if (uriResourceParts.get(size - 1) instanceof UriResourceLambdaAny) {
            return throwNotImplemented();
        } else {
            return throwNotImplemented();
        }
    }

    private Property getProperty(EdmProperty edmProperty) throws ODataServiceFault {
        Property currentProperty = null;
        for (DataColumn column : this.tableMetaData) {
            String columnName = column.getColumnName();
            try {
                if (columnName.equals(edmProperty.getName())) {
                    currentProperty = ODataUtils.createPrimitive(column.getColumnType(), columnName,
                                                                 this.entity.getValue(columnName));
                    break;
                }
            } catch (ParseException | ODataServiceFault e) {
                throw new ODataServiceFault("Error occurred when reading properties");
            }
        }
        return currentProperty;
    }

    @Override
    public VisitorOperand visitAlias(final String aliasName) throws ODataApplicationException {
        return throwNotImplemented();
    }

    @Override
    public VisitorOperand visitTypeLiteral(final EdmType type) throws ODataApplicationException {
        return throwNotImplemented();
    }

    @Override
    public VisitorOperand visitLambdaReference(final String variableName) throws ODataApplicationException {
        return throwNotImplemented();
    }

    @Override
    public VisitorOperand visitEnum(final EdmEnumType type, final List<String> enumValues)
            throws ODataApplicationException {
        return throwNotImplemented();
    }

    @Override
    public VisitorOperand visitBinaryOperator(BinaryOperatorKind binaryOperatorKind,
            VisitorOperand visitorOperand, List<VisitorOperand> list) {
        return null;
    }

    private VisitorOperand throwNotImplemented() throws ODataApplicationException {
        throw new ODataApplicationException("Not implemented", HttpStatusCode.NOT_IMPLEMENTED.getStatusCode(),
                                            Locale.ROOT);
    }
}
