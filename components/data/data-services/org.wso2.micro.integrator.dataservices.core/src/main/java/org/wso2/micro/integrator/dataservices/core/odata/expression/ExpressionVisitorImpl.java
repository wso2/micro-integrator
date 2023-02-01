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
package org.wso2.micro.integrator.dataservices.core.odata.expression;

import org.apache.olingo.commons.api.data.Entity;
import org.apache.olingo.commons.api.data.Property;
import org.apache.olingo.commons.api.edm.EdmBindingTarget;
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
import org.apache.olingo.server.api.uri.queryoption.expression.ExpressionVisitException;
import org.apache.olingo.server.api.uri.queryoption.expression.ExpressionVisitor;
import org.apache.olingo.server.api.uri.queryoption.expression.Literal;
import org.apache.olingo.server.api.uri.queryoption.expression.Member;
import org.apache.olingo.server.api.uri.queryoption.expression.MethodKind;
import org.apache.olingo.server.api.uri.queryoption.expression.UnaryOperatorKind;
import org.wso2.micro.integrator.dataservices.core.odata.expression.operand.TypedOperand;
import org.wso2.micro.integrator.dataservices.core.odata.expression.operand.UntypedOperand;
import org.wso2.micro.integrator.dataservices.core.odata.expression.operand.VisitorOperand;
import org.wso2.micro.integrator.dataservices.core.odata.expression.operation.BinaryOperator;
import org.wso2.micro.integrator.dataservices.core.odata.expression.operation.MethodCallOperator;
import org.wso2.micro.integrator.dataservices.core.odata.expression.operation.UnaryOperator;

import java.util.List;
import java.util.Locale;

public class ExpressionVisitorImpl implements ExpressionVisitor<VisitorOperand> {

    final private Entity entity;

    public ExpressionVisitorImpl(final Entity entity, final EdmBindingTarget bindingTarget) {
        this.entity = entity;
    }

    @Override
    public VisitorOperand visitBinaryOperator(final BinaryOperatorKind operator, final VisitorOperand left,
                                              final VisitorOperand right)
            throws ExpressionVisitException, ODataApplicationException {
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
            throws ExpressionVisitException, ODataApplicationException {
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
            throws ExpressionVisitException, ODataApplicationException {
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
                                                final Expression expression)
            throws ExpressionVisitException, ODataApplicationException {
        return throwNotImplemented();
    }

    @Override
    public VisitorOperand visitLiteral(Literal literal) throws ExpressionVisitException, ODataApplicationException {
        return new UntypedOperand(literal.getText());
    }

    @Override
    public VisitorOperand visitMember(Member member) throws ExpressionVisitException, ODataApplicationException {
        final List<UriResource> uriResourceParts = member.getResourcePath().getUriResourceParts();
        int size = uriResourceParts.size();
        if (uriResourceParts.get(0) instanceof UriResourceProperty) {
            EdmProperty currentEdmProperty = ((UriResourceProperty) uriResourceParts.get(0)).getProperty();
            Property currentProperty = entity.getProperty(currentEdmProperty.getName());
            return new TypedOperand(currentProperty.getValue(), currentEdmProperty.getType(), currentEdmProperty);
        } else if (uriResourceParts.get(size - 1) instanceof UriResourceLambdaAll) {
            return throwNotImplemented();
        } else if (uriResourceParts.get(size - 1) instanceof UriResourceLambdaAny) {
            return throwNotImplemented();
        } else {
            return throwNotImplemented();
        }
    }

    @Override
    public VisitorOperand visitAlias(final String aliasName)
            throws ExpressionVisitException, ODataApplicationException {
        return throwNotImplemented();
    }

    @Override
    public VisitorOperand visitTypeLiteral(final EdmType type)
            throws ExpressionVisitException, ODataApplicationException {
        return throwNotImplemented();
    }

    @Override
    public VisitorOperand visitLambdaReference(final String variableName)
            throws ExpressionVisitException, ODataApplicationException {
        return throwNotImplemented();
    }

    @Override
    public VisitorOperand visitEnum(final EdmEnumType type, final List<String> enumValues)
            throws ExpressionVisitException, ODataApplicationException {
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
