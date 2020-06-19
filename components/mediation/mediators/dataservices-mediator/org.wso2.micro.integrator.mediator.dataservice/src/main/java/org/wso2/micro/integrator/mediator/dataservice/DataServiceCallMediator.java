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
 * /
 */

package org.wso2.micro.integrator.mediator.dataservice;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.om.impl.OMNamespaceImpl;
import org.apache.axiom.soap.SOAP11Constants;
import org.apache.axiom.soap.SOAP12Constants;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.soap.SOAPFactory;
import org.apache.axis2.AxisFault;
import org.apache.axis2.description.AxisService;
import org.apache.synapse.MessageContext;
import org.apache.synapse.SynapseLog;
import org.apache.synapse.config.xml.SynapsePath;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.apache.synapse.mediators.AbstractMediator;
import org.apache.synapse.transport.passthru.PassThroughConstants;
import org.apache.synapse.util.MessageHelper;
import org.w3c.dom.Document;
import org.wso2.micro.integrator.dataservices.core.DataServiceFault;
import org.wso2.micro.integrator.dataservices.core.DataServiceProcessor;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.StringReader;
import java.util.List;
import java.util.regex.Matcher;

public class DataServiceCallMediator extends AbstractMediator {

    private String dsName;
    private Operations operations;
    public enum OperationsType {SINGLE_REQ, REQUEST_BOX, BATCH_REQ}
    private String targetType;
    private String propertyName;
    private OMFactory fac;
    private OMNamespace omNamespace;
    private SynapseLog synLog;


    public boolean mediate(MessageContext messageContext) {

        synLog = getLog(messageContext);
        String serviceName = getDsName();

        if (synLog.isTraceOrDebugEnabled()) {
            synLog.traceOrDebug("The invoking data service name : " + getDsName());
        }
        omNamespace = new OMNamespaceImpl(DataServiceCallMediatorConstants.PAYLOAD_NAME_SPACE_URI,
                DataServiceCallMediatorConstants.PAYLOAD_PREFIX);
        fac = OMAbstractFactory.getOMFactory();
        try {
            // clone the message context to append payloads to invoke dataservice
            MessageContext cloneMessageContext = MessageHelper.cloneMessageContext(messageContext);
            // Casting the synapse message context to axis2 message context
            org.apache.axis2.context.MessageContext axis2MessageContext = ((Axis2MessageContext) cloneMessageContext)
                    .getAxis2MessageContext();
            // Get the Axis service name of the give dataservice name
            AxisService axisService = axis2MessageContext.getConfigurationContext().getAxisConfiguration()
                    .getService(serviceName);
            if (axisService != null) {
                if (synLog.isTraceOrDebugEnabled()) {
                    synLog.traceOrDebug("The axisService :" + axisService.getName());
                    synLog.traceOrDebug("The service name space URI : " + axisService.getTargetNamespace());
                }
                // Set the axis service into the axis2 message context
                axis2MessageContext.setAxisService(axisService);
                OMElement payload = addRootOperation(axis2MessageContext, messageContext);
                if (axis2MessageContext.getEnvelope().getBody().getFirstElement() != null) {
                    axis2MessageContext.getEnvelope().getBody().getFirstElement().detach();
                }
                axis2MessageContext.getEnvelope().getBody().addChild(payload);
                dispatchToService(axis2MessageContext, messageContext);
            } else {
                handleException("The data service,  named '" + serviceName + "' does not exist. ", messageContext);
            }
        } catch (AxisFault axisFault) {
            if (synLog.isTraceOrDebugEnabled()) {
                synLog.traceOrDebug(axisFault.getMessage());
            }
            synLog.error(axisFault.getMessage());
            handleException("AxisFault occurred.",axisFault,messageContext);
        }
        return true;
    }
    
    private OMElement addRootOperation (org.apache.axis2.context.MessageContext axis2MessageContext,
                                   MessageContext messageContext) {
        Operations rootOperations = getOperations();
        String rootOpName;
        switch (rootOperations.getType()) {
            case REQUEST_BOX: {
                rootOpName = DataServiceCallMediatorConstants.REQUEST_BOX;
                break;
            }
            case BATCH_REQ: {
                Operation firstOp = (Operation) rootOperations.getOperations().get(0);
                rootOpName = firstOp.getOperationName() + DataServiceCallMediatorConstants.BATCH_REQ_SUFFIX;
                break;
            }
            case SINGLE_REQ:
            default: {
                Operation singleOp = (Operation) rootOperations.getOperations().get(0);
                rootOpName = singleOp.getOperationName();
                break;
            }
        }
        QName rootOpQName = new QName(rootOpName);
        axis2MessageContext.getAxisMessage().getAxisOperation().setName(rootOpQName);
        OMElement payload = fac.createOMElement(rootOpName, omNamespace);
        addOperations(rootOperations, payload, messageContext);
        return payload;
    }

    private void dispatchToService(org.apache.axis2.context.MessageContext axis2MessageContext,
                                   MessageContext messageContext) {

        try {
            OMElement omElement = DataServiceProcessor.dispatch(axis2MessageContext);
            if (omElement != null) {
                if (synLog.isTraceOrDebugEnabled()) {
                    synLog.traceOrDebug("The result OMElement from the dataservice : " + omElement);
                }
                //set the result payload as property according to the target type
                if (DataServiceCallMediatorConstants.TARGET_PROPERTY_TYPE.equals(targetType)) {
                    messageContext.setProperty(propertyName, omElement);
                    if (synLog.isTraceOrDebugEnabled()) {
                        synLog.traceOrDebug("The result property : " + messageContext.
                                getProperty(propertyName));
                    }
                } else {
                    //	set the result payload as envelope in to message context according to the target type
                    ((Axis2MessageContext)messageContext).getAxis2MessageContext().
                            removeProperty(PassThroughConstants.NO_ENTITY_BODY);
                    messageContext.getEnvelope().getBody().addChild(omElement);
                }
            } else {
                messageContext.setEnvelope(createDefaultSOAPEnvelope(messageContext));
            }
        } catch (DataServiceFault dataServiceFault) {
            if (synLog.isTraceOrDebugEnabled()) {
                synLog.traceOrDebug(dataServiceFault.getMessage());
            }
            synLog.error(dataServiceFault.getMessage());
            handleException("DataService exception occurred while accessing the dataservice to do the operation",
                    dataServiceFault, messageContext);

        } catch (AxisFault axisFault) {
            if (synLog.isTraceOrDebugEnabled()) {
                synLog.traceOrDebug(axisFault.getMessage());
            }
            handleException("Error while creating response payload", axisFault, messageContext);
        }
    }

    private void addOperations(Operations operations, OMElement payload, MessageContext msgCtx) {

        for (Object operationObj : operations.getOperations()) {
            if (operationObj instanceof Operation) {
                Operation nestedOperation = (Operation) operationObj;
                String operationName = nestedOperation.getOperationName();
                OMElement nestedPayload = fac.createOMElement(operationName, omNamespace);
                for (Param param : nestedOperation.getParams()) {
                    addParams(param, nestedPayload, msgCtx);
                }
                payload.addChild(nestedPayload);
            } else if (operationObj instanceof Operations) {
                Operations rootOperations = (Operations) operationObj;
                String rootOpName;
                if (rootOperations.getType() == OperationsType.SINGLE_REQ) {
                    Operation singleOp = (Operation) rootOperations.getOperations().get(0);
                    rootOpName = singleOp.getOperationName();
                } else {
                    rootOpName = rootOperations.getType().toString().toLowerCase();
                }
                OMElement nestedOpEle = fac.createOMElement(rootOpName, omNamespace);
                addOperations(rootOperations, nestedOpEle, msgCtx);
                payload.addChild(nestedOpEle);
            }
        }
    }

    private void addParams(Param param, OMElement payload, MessageContext msgCtx) {
        OMElement omElement = fac.createOMElement(param.getParamName(), omNamespace);
        String paramValue = "";
        if (param.getParamValue() != null) {
            paramValue = param.getParamValue();
        } else if (param.getParamExpression() != null) {
            paramValue = param.getParamExpression().stringValueOf(msgCtx);
            if (DataServiceCallMediatorConstants.XML_TYPE.equals(param.getParamType()) &&
                    !isJson(paramValue.trim(), param.getParamExpression())) {
                paramValue = escapeXMLEnvelope(msgCtx, paramValue);
            }
            paramValue = Matcher.quoteReplacement(paramValue);
        }
        omElement.setText(paramValue);
        payload.addChild(omElement);
    }

    /**
     * Helper function that returns true if value passed is of JSON type and expression is JSON.
     */
    private boolean isJson(String value, SynapsePath expression) {
        return !(value == null || value.trim().isEmpty()) && (value.trim().charAt(0) == '{'
                || value.trim().charAt(0) == '[') && expression.getPathType().equals(SynapsePath.JSON_PATH);
    }

    /**
     * Escapes XML special characters
     *
     * @param msgCtx Message Context
     * @param value XML String which needs to be escaped
     * @return XML special char escaped string
     */
    private String escapeXMLEnvelope(MessageContext msgCtx, String value) {
        String xmlVersion = "1.0"; //Default is set to 1.0

        try {
            xmlVersion = checkXMLVersion(msgCtx);
        } catch (IOException e) {
            log.error("Error reading message envelope", e);
        } catch (SAXException e) {
            log.error("Error parsing message envelope", e);
        } catch (ParserConfigurationException e) {
            log.error("Error building message envelope document", e);
        }

        if("1.1".equals(xmlVersion)) {
            return org.apache.commons.text.StringEscapeUtils.escapeXml11(value);
        } else {
            return org.apache.commons.text.StringEscapeUtils.escapeXml10(value);
        }
    }

    /**
     * Checks and returns XML version of the envelope
     *
     * @param msgCtx Message Context
     * @return xmlVersion in XML Declaration
     * @throws ParserConfigurationException failure in building message envelope document
     * @throws IOException Error reading message envelope
     * @throws SAXException Error parsing message envelope
     */
    private String checkXMLVersion(MessageContext msgCtx) throws IOException, SAXException,
            ParserConfigurationException {
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
        InputSource inputSource = new InputSource(new StringReader(msgCtx.getEnvelope().toString()));
        Document document = documentBuilder.parse(inputSource);
        return document.getXmlVersion();
    }

    /* Creating a soap response according the the soap namespace uri */
    private SOAPEnvelope createDefaultSOAPEnvelope(MessageContext inMsgCtx) {

        String soapNamespace = inMsgCtx.getEnvelope().getNamespace().getNamespaceURI();
        SOAPFactory soapFactory = null;
        if (soapNamespace.equals(SOAP11Constants.SOAP_ENVELOPE_NAMESPACE_URI)) {
            soapFactory = OMAbstractFactory.getSOAP11Factory();
        } else if (soapNamespace.equals(SOAP12Constants.SOAP_ENVELOPE_NAMESPACE_URI)) {
            soapFactory = OMAbstractFactory.getSOAP12Factory();
        } else {
            log.error("Unknown SOAP Envelope");
        }
        return soapFactory.getDefaultEnvelope();
    }

    public void setDsName(String dsName) {
        this.dsName = dsName;
    }

    public String getDsName() {
        return dsName;
    }

    public Operations getOperations() {
        return operations;
    }

    public void setOperations(Operations operations) {
        this.operations = operations;
    }

    public String getPropertyName() {
        return propertyName;
    }

    public void setPropertyName(String propertyName) {
        this.propertyName = propertyName;
    }

    public String getTargetType() {
        return targetType;
    }

    public void setTargetType(String targetType) {
        this.targetType = targetType;
    }

    public class Operations {

        private OperationsType type;
        private List operations;

        Operations(OperationsType type, List operations) {
            this.type = type;
            this.operations = operations;
        }

        public OperationsType getType() {
            return type;
        }

        public List getOperations() {
            return operations;
        }
    }

    public class Operation {

        private String operationName;
        private List<Param> params;

        Operation(String name, List<Param> params) {
            this.operationName = name;
            this.params = params;
        }

        public String getOperationName() {
            return operationName;
        }

        public List<Param> getParams() {
            return params;
        }
    }

    public class Param {

        private String paramName;
        private String paramValue;
        private String paramType;
        private String evaluator;
        private SynapsePath paramExpression;

        Param(String name, String type) {
            this.paramName = name;
            this.paramType = type;
        }

        public String getParamName() {
            return paramName;
        }

        public String getParamValue() {
            return paramValue;
        }

        public String getParamType() {
            return paramType;
        }

        public void setParamValue(String paramValue) {
            this.paramValue = paramValue;
        }

        public String getEvaluator() {
            return evaluator;
        }

        public void setEvaluator(String evaluator) {
            this.evaluator = evaluator;
        }

        public SynapsePath getParamExpression() {
            return paramExpression;
        }

        public void setParamExpression(SynapsePath paramExpression) {
            this.paramExpression = paramExpression;
        }
    }

}
