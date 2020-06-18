/*
 *
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  you may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */

package org.wso2.micro.integrator.mediator.dataservice;

import org.apache.axiom.om.OMAttribute;
import org.apache.axiom.om.OMElement;
import org.apache.synapse.Mediator;
import org.apache.synapse.SynapseConstants;
import org.apache.synapse.config.xml.AbstractMediatorFactory;
import org.apache.synapse.config.xml.SynapseJsonPathFactory;
import org.apache.synapse.config.xml.SynapsePath;
import org.apache.synapse.config.xml.SynapseXPathFactory;
import org.apache.synapse.config.xml.XMLConfigConstants;
import org.apache.synapse.util.xpath.SynapseXPath;
import org.jaxen.JaxenException;
import org.wso2.micro.integrator.mediator.dataservice.DataServiceCallMediator.OperationsType;
import org.wso2.micro.integrator.mediator.dataservice.DataServiceCallMediator.Operations;
import org.wso2.micro.integrator.mediator.dataservice.DataServiceCallMediator.Param;


import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

/**
 * Factory for {@link DataServiceCallMediator} instances.
 * <p>
 * Configuration syntax:
 * <pre>
 * &lt;dataServicesCall serviceName = ""&gt;
 *      &lt;operations type="single"/&gt;
 *          &lt;operation name=""&gt;
 *                  &lt;param name="string"&gt; &lt;param/&gt;
 *          &lt;/operation&gt;
 *      &lt;/operations&gt;
 *      &lt;target  type="envelope"|"property" name="string"/&gt;
 * &lt;/dataServicesCall&gt;
 * </pre>
 */

public class DataServiceCallMediatorFactory extends AbstractMediatorFactory {

    private static final QName DSSCALL_Q =
            new QName(SynapseConstants.SYNAPSE_NAMESPACE, DataServiceCallMediatorConstants.DATA_SERVICES_CALL);
    private static final QName TARGET_Q = new QName(
            XMLConfigConstants.SYNAPSE_NAMESPACE, DataServiceCallMediatorConstants.TARGET);
    private static final QName NAME_Q = new QName(DataServiceCallMediatorConstants.NAME);
    private static final QName TYPE_Q = new QName(DataServiceCallMediatorConstants.TYPE);


    public Mediator createSpecificMediator(OMElement elem, Properties properties) {

        DataServiceCallMediator mediator = new DataServiceCallMediator();
        processAuditStatus(mediator, elem);
        String dsName = elem.getAttributeValue(new QName(DataServiceCallMediatorConstants.SERVICE_NAME));
        if (dsName == null) {
            handleException("The 'serviceName' attribute in 'dataServicesCall' element  is missing " +
                    "in the configuration.");
        }
        mediator.setDsName(dsName);
        OMElement operationsTypeElement = elem.getFirstElement();
        if (!operationsTypeElement.getLocalName().equals(DataServiceCallMediatorConstants.OPERATIONS)) {
            handleException("The 'operations' element in 'dataServicesCall' element  is missing in the configuration.");
        }
        String operationType = operationsTypeElement.getAttributeValue(new QName(DataServiceCallMediatorConstants.TYPE));
        if (operationType == null) {
            handleException("The 'type' attribute in 'operations' element  is missing in the configuration.");
        }
        OperationsType operationsType = OperationsType.valueOf(operationType.toUpperCase());
        List operationList = extractOperations(operationsTypeElement, mediator);
        Operations operations = mediator.new Operations(operationsType, operationList);
        mediator.setOperations(operations);

        OMElement targetElement = elem.getFirstChildWithName(TARGET_Q);
        if (targetElement != null) {
            OMAttribute typeAtr = targetElement.getAttribute(TYPE_Q);
            if (typeAtr != null) {
                String type = typeAtr.getAttributeValue();
                mediator.setTargetType(type);
                if (type.equals(DataServiceCallMediatorConstants.PROPERTY)) {
                    OMAttribute propertyAtr = targetElement.getAttribute(NAME_Q);
                    if (propertyAtr != null) {
                        if (!"".equals(propertyAtr.getAttributeValue())) {
                            String propertyName = propertyAtr.getAttributeValue();
                            mediator.setPropertyName(propertyName);
                        } else {
                            handleException("The 'name' attribute in 'target' element is empty. " +
                                    "Please enter a value.");
                        }
                    } else {
                        handleException("The 'name' attribute in 'target' element  is missing " +
                                "in the configuration.");
                    }
                }
            } else {
                handleException("The 'type' attribute in 'target' element is required for the configuration");
            }
        } else {
            handleException("The 'target' element is missing in the configuration.");
        }
        return mediator;
    }

    private List extractOperations(OMElement operationsElemet, DataServiceCallMediator mediator) {
        List operationsList = new ArrayList();
        Iterator operationIterator = operationsElemet.getChildElements();
        while (operationIterator.hasNext()) {
            OMElement operationEle = (OMElement) operationIterator.next();
            if (DataServiceCallMediatorConstants.OPERATION.equals(operationEle.getLocalName())) {
                OMAttribute nameAtr = operationEle.getAttribute(new QName(DataServiceCallMediatorConstants.NAME));
                if (nameAtr != null) {
                    String operationName = nameAtr.getAttributeValue();
                    List<Param> paramList = new ArrayList<>();
                    if (operationEle.getFirstElement() != null && DataServiceCallMediatorConstants.PARAM.
                            equals(operationEle.getFirstElement().getLocalName())) {
                        paramList = extractParams(operationEle, mediator);
                    }
                    operationsList.add(mediator.new Operation(operationName, paramList));
                }
            } else if (DataServiceCallMediatorConstants.OPERATIONS.equals(operationEle.getLocalName())) {
                String operationType = operationEle.getAttributeValue(new QName(DataServiceCallMediatorConstants.TYPE));
                OperationsType operationsType = OperationsType.valueOf(operationType.toUpperCase());
                operationsList.add(mediator.new Operations(operationsType, extractOperations(operationEle, mediator)));
            } else {
                handleException("The 'operation' element is missing in the configuration.");
            }
        }
        return operationsList;
    }

    private List<Param> extractParams(OMElement elem, DataServiceCallMediator mediator) {
        List<Param> paramList = new ArrayList<>();
        if (elem != null) {
            Iterator paramsIterator = elem.getChildrenWithName(new QName(DataServiceCallMediatorConstants.PARAM));
            while (paramsIterator.hasNext()) {
                OMElement paramElement = (OMElement) paramsIterator.next();
                OMAttribute nameAtr = paramElement.getAttribute(new QName(DataServiceCallMediatorConstants.NAME));

                if (nameAtr != null) {
                    String paramName = nameAtr.getAttributeValue();
                    String paramValue = paramElement.getAttributeValue(new QName(DataServiceCallMediatorConstants.VALUE));
                    String paramType = paramElement.getAttributeValue(new QName(DataServiceCallMediatorConstants.TYPE));
                    String evaluator = paramElement.getAttributeValue(new QName(DataServiceCallMediatorConstants.
                            EVALUATOR));
                    String paramExpression = paramElement.getAttributeValue(new QName(DataServiceCallMediatorConstants.
                            EXPRESSION));
                    Param param = mediator.new Param(paramName, paramType);
                    param.setParamValue(paramValue);
                    param.setEvaluator(evaluator);
                    try {
                        if (paramExpression != null) {
                            if (evaluator != null && evaluator.equals(DataServiceCallMediatorConstants.JSON_TYPE)) {
                                if (paramExpression.startsWith("json-eval(")) {
                                    paramExpression = paramExpression.substring(10, paramExpression.length() - 1);
                                }
                                param.setParamExpression(SynapseJsonPathFactory.getSynapseJsonPath(paramExpression));
                                // we have to explicitly define the path type since we are not going to mark
                                // JSON Path's with prefix "json-eval(".
                                param.getParamExpression().setPathType(SynapsePath.JSON_PATH);
                            } else {
                                SynapseXPath sxp = SynapseXPathFactory.getSynapseXPath(paramElement, ATT_EXPRN);
                                //we need to disable stream Xpath forcefully
                                sxp.setForceDisableStreamXpath(Boolean.TRUE);
                                param.setParamExpression(sxp);
                                param.getParamExpression().setPathType(SynapsePath.X_PATH);
                            }
                        }
                    } catch (JaxenException e) {
                        handleException("Invalid XPath expression for attribute expression : " +
                                paramExpression, e);
                    }
                    paramList.add(param);
                }
            }
        }
        return paramList;
    }

    public QName getTagQName() {
        return DSSCALL_Q;
    }
}