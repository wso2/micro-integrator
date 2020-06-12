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
package org.wso2.micro.integrator.identity.entitlement.mediator;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.om.OMNode;
import org.apache.axiom.om.OMText;
import org.apache.axiom.om.impl.llom.util.AXIOMUtil;
import org.apache.axiom.soap.SOAP11Constants;
import org.apache.axiom.soap.SOAP12Constants;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.soap.SOAPFactory;
import org.apache.axis2.AxisFault;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.ContinuationState;
import org.apache.synapse.ManagedLifecycle;
import org.apache.synapse.Mediator;
import org.apache.synapse.MessageContext;
import org.apache.synapse.SynapseConstants;
import org.apache.synapse.SynapseException;
import org.apache.synapse.SynapseLog;
import org.apache.synapse.config.SynapseConfiguration;
import org.apache.synapse.continuation.ContinuationStackManager;
import org.apache.synapse.continuation.ReliantContinuationState;
import org.apache.synapse.core.SynapseEnvironment;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.apache.synapse.debug.constructs.EnclosedInlinedSequence;
import org.apache.synapse.mediators.AbstractMediator;
import org.apache.synapse.mediators.FlowContinuableMediator;
import org.apache.synapse.mediators.base.SequenceMediator;
import org.apache.synapse.util.AXIOMUtils;
import org.apache.synapse.util.MessageHelper;
import org.jaxen.JaxenException;
import org.wso2.micro.core.util.CryptoException;
import org.wso2.micro.core.util.CryptoUtil;
import org.wso2.micro.integrator.identity.entitlement.mediator.callback.EntitlementCallbackHandler;
import org.wso2.micro.integrator.identity.entitlement.mediator.callback.UTEntitlementCallbackHandler;
import org.wso2.micro.integrator.identity.entitlement.proxy.Attribute;
import org.wso2.micro.integrator.identity.entitlement.proxy.PEPProxy;
import org.wso2.micro.integrator.identity.entitlement.proxy.PEPProxyConfig;
import org.wso2.micro.integrator.identity.entitlement.proxy.ProxyConstants;
import org.wso2.micro.integrator.identity.entitlement.proxy.exception.EntitlementProxyException;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import javax.xml.namespace.QName;

public class EntitlementMediator extends AbstractMediator
        implements ManagedLifecycle, FlowContinuableMediator, EnclosedInlinedSequence {

    private static final Log log = LogFactory.getLog(EntitlementMediator.class);

    private String remoteServiceUserName;
    private String remoteServicePassword;
    private String remoteServiceUrl;
    private String remoteServiceUserNameKey;
    private String remoteServicePasswordKey;
    private String remoteServiceUrlKey;
    private String callbackClass;
    private String client;
    private String thriftPort;
    private String thriftHost;
    private String reuseSession;
    private String cacheType;
    private int invalidationInterval;
    private int maxCacheEntries;
    EntitlementCallbackHandler callback = null;
    /* The reference to the sequence which will execute when access is denied   */
    private String onRejectSeqKey = null;
    /* The in-line sequence which will execute when access is denied */
    private Mediator onRejectMediator = null;
    /* The reference to the sequence which will execute when access is allowed  */
    private String onAcceptSeqKey = null;
    /* The in-line sequence which will execute when access is allowed */
    private Mediator onAcceptMediator = null;
    /* The reference to the obligations sequence   */
    private String obligationsSeqKey = null;
    /* The in-line obligation sequence */
    private Mediator obligationsMediator = null;
    /* The reference to the advice sequence */
    private String adviceSeqKey = null;
    /* The in-line advice sequence */
    private Mediator adviceMediator = null;
    private PEPProxy pepProxy;
    private PEPProxyConfig config;
    private boolean keyInvolved = false;

    private final String ORIGINAL_ENTITLEMENT_PAYLOAD = "ORIGINAL_ENTITLEMENT_PAYLOAD";
    private final String ENTITLEMENT_DECISION = "ENTITLEMENT_DECISION";
    private final String ENTITLEMENT_ADVICE = "ENTITLEMENT_ADVICE";

    /**
     * {@inheritDoc}
     */
    public boolean mediate(MessageContext synCtx) {

        if (synCtx.getEnvironment().isDebuggerEnabled()) {
            if (super.divertMediationRoute(synCtx)) {
                return true;
            }
        }

        String decisionString;
        String userName;
        String serviceName;
        String operationName;
        String action;
        String resourceName;
        Attribute[] otherAttributes;
        PEPProxy resolvedPepProxy;

        if (log.isDebugEnabled()) {
            log.debug("Mediation for Entitlement started");
        }

        resolvedPepProxy = pepProxy;

        if (keyInvolved) {
            try {
                resolvedPepProxy = resolveEntitlementServerDynamicConfigs(synCtx);
            } catch (EntitlementProxyException e) {
                log.error("Error while initializing the PEP Proxy" + e);
                throw new SynapseException("Error while initializing the Entitlement PEP Proxy");
            }
        }

        try {
            userName = callback.getUserName(synCtx);
            serviceName = callback.findServiceName(synCtx);
            operationName = callback.findOperationName(synCtx);
            action = callback.findAction(synCtx);
            otherAttributes = callback.findOtherAttributes(synCtx);

            if (userName == null) {
                throw new SynapseException("User name not provided for the Entitlement mediator - can't proceed");
            }

            if (operationName != null) {
                resourceName = serviceName + "/" + operationName;
            } else {
                resourceName = serviceName;
            }

            if (otherAttributes == null) {
                otherAttributes = new Attribute[0];
            }

            if (log.isDebugEnabled()) {
                StringBuilder debugOtherAttributes = new StringBuilder();
                debugOtherAttributes
                        .append("Subject ID is : " + userName + " Resource ID is : " + resourceName + " Action ID is : "
                                        + action + ".");
                if (otherAttributes.length > 0) {
                    debugOtherAttributes.append("Other attributes are ");
                    for (int i = 0; i < otherAttributes.length; i++) {
                        debugOtherAttributes.append("Attribute ID : ").append(otherAttributes[i].getId())
                                .append(" of Category : ").append(otherAttributes[i].getCategory())
                                .append(" of Type : ").append(otherAttributes[i].getType()).append(" and Value : ")
                                .append(otherAttributes[i].getValue());
                        if (i < otherAttributes.length - 2) {
                            debugOtherAttributes.append(", ");
                        } else if (i == otherAttributes.length - 2) {
                            debugOtherAttributes.append(" and ");
                        } else {
                            debugOtherAttributes.append(".");
                        }
                    }
                }
                log.debug(debugOtherAttributes);
            }

            // if decision cache is disabled
            // Creating the XACML 3.0 Attributes to Send XACML Request
            Attribute[] tempArr = new Attribute[otherAttributes.length + 3];
            tempArr[0] = new Attribute("urn:oasis:names:tc:xacml:1.0:subject-category:access-subject",
                                       "urn:oasis:names:tc:xacml:1.0:subject:subject-id",
                                       ProxyConstants.DEFAULT_DATA_TYPE, userName);
            tempArr[1] = new Attribute("urn:oasis:names:tc:xacml:3.0:attribute-category:action",
                                       "urn:oasis:names:tc:xacml:1.0:action:action-id",
                                       ProxyConstants.DEFAULT_DATA_TYPE, action);
            tempArr[2] = new Attribute("urn:oasis:names:tc:xacml:3.0:attribute-category:resource",
                                       "urn:oasis:names:tc:xacml:1.0:resource:resource-id",
                                       ProxyConstants.DEFAULT_DATA_TYPE, resourceName);
            for (int i = 0; i < otherAttributes.length; i++) {
                tempArr[3 + i] = otherAttributes[i];
            }

            decisionString = resolvedPepProxy.getDecision(tempArr);
            String simpleDecision;
            OMElement obligations;
            OMElement advice;
            if (decisionString != null) {
                String nameSpace = null;
                OMElement decisionElement = AXIOMUtil.stringToOM(decisionString);
                OMNamespace omNamespace = decisionElement.getDefaultNamespace();
                if (omNamespace != null) {
                    nameSpace = omNamespace.getNamespaceURI();
                }
                if (nameSpace == null) {
                    simpleDecision = decisionElement.getFirstChildWithName(new QName("Result")).
                            getFirstChildWithName(new QName("Decision")).getText();
                    obligations = decisionElement.getFirstChildWithName(new QName("Result")).
                            getFirstChildWithName(new QName("Obligations"));
                    advice = decisionElement.getFirstChildWithName(new QName("Result")).
                            getFirstChildWithName(new QName("AssociatedAdvice"));
                } else {
                    simpleDecision = decisionElement.getFirstChildWithName(new QName(nameSpace, "Result")).
                            getFirstChildWithName(new QName(nameSpace, "Decision")).getText();
                    obligations = decisionElement.getFirstChildWithName(new QName(nameSpace, "Result")).
                            getFirstChildWithName(new QName(nameSpace, "Obligations"));
                    advice = decisionElement.getFirstChildWithName(new QName(nameSpace, "Result")).
                            getFirstChildWithName(new QName(nameSpace, "AssociatedAdvice"));
                }
                if (log.isDebugEnabled()) {
                    log.debug("Entitlement Decision is : " + simpleDecision);
                }
            } else {
                //undefined decision;
                throw new SynapseException("Undefined Decision is received");
            }

            synCtx.setProperty(ORIGINAL_ENTITLEMENT_PAYLOAD, synCtx.getEnvelope());
            synCtx.setProperty(ENTITLEMENT_DECISION, simpleDecision);
            synCtx.setProperty(ENTITLEMENT_ADVICE, advice);

            // assume entitlement mediator always acts as base PEP
            // then behavior for not-applicable and indeterminate results are undefined
            // but here assume to be deny
            if ("Permit".equals(simpleDecision) || "Deny".equals(simpleDecision)) {

                MessageContext obligationsSynCtx = null;
                MessageContext adviceSynCtx = null;
                // 1st check for advice
                if (advice != null) {
                    adviceSynCtx = getOMElementInserted(advice, getClonedMessageContext(synCtx));
                    if (adviceSeqKey != null) {
                        SequenceMediator sequence = (SequenceMediator) adviceSynCtx.getSequence(adviceSeqKey);
                        // Clear the continuation stack. So adviceSynCtx will not flow through the
                        // rest of the mediators place in this flow
                        ContinuationStackManager.clearStack(adviceSynCtx);
                        adviceSynCtx.getEnvironment().injectAsync(adviceSynCtx, sequence);
                    } else if (adviceMediator != null) {
                        ContinuationStackManager.
                                addReliantContinuationState(adviceSynCtx, 0, getMediatorPosition());
                        adviceSynCtx.getEnvironment().injectAsync(adviceSynCtx, (SequenceMediator) adviceMediator);
                    }
                }

                if (obligations != null) {
                    obligationsSynCtx = getOMElementInserted(obligations, getClonedMessageContext(synCtx));
                    boolean result;
                    if (obligationsSeqKey != null) {
                        ContinuationStackManager.
                                addReliantContinuationState(obligationsSynCtx, 1, getMediatorPosition());
                        obligationsSynCtx.setProperty(ContinuationStackManager.SKIP_CONTINUATION_STATE, true);
                        result = obligationsSynCtx.getSequence(obligationsSeqKey).
                                mediate(obligationsSynCtx);
                        Boolean isContinuationCall = (Boolean) obligationsSynCtx
                                .getProperty(SynapseConstants.CONTINUATION_CALL);
                        if (result) {
                            ContinuationStackManager.removeReliantContinuationState(obligationsSynCtx);
                        } else if (!result && isContinuationCall != null && isContinuationCall) {
                            // If result is false due to presence of a Call mediator, stop the flow
                            return false;
                        }
                    } else {
                        ContinuationStackManager.
                                addReliantContinuationState(obligationsSynCtx, 2, getMediatorPosition());
                        result = obligationsMediator.mediate(obligationsSynCtx);
                        Boolean isContinuationCall = (Boolean) obligationsSynCtx
                                .getProperty(SynapseConstants.CONTINUATION_CALL);
                        if (result) {
                            ContinuationStackManager.removeReliantContinuationState(obligationsSynCtx);
                        } else if (!result && isContinuationCall != null && isContinuationCall) {
                            // If result is false due to presence of a Call mediator, stop the flow
                            return false;
                        }
                    }

                    if (!result) {
                        // if return false, obligations are not correctly performed.
                        // So message is mediated through the OnReject sequence
                        if (log.isDebugEnabled()) {
                            log.debug("Obligations are not correctly performed");
                        }
                        simpleDecision = "Deny";
                    }
                }
            }

            return executeDecisionMessageFlow(synCtx, simpleDecision);
        } catch (SynapseException e) {
            log.error(e);
            throw e;
        } catch (Exception e) {
            log.error("Error occurred while evaluating the policy", e);
            throw new SynapseException("Error occurred while evaluating the policy");
        }

    }

    private boolean executeDecisionMessageFlow(MessageContext synCtx, String simpleDecision) {
        if ("Permit".equals(simpleDecision)) {
            if (log.isDebugEnabled()) {
                log.debug("User is authorized to perform the action");
            }
            if (onAcceptSeqKey != null) {
                ContinuationStackManager.updateSeqContinuationState(synCtx, getMediatorPosition());
                return synCtx.getSequence(onAcceptSeqKey).mediate(synCtx);
            } else if (onAcceptMediator != null) {
                ContinuationStackManager.addReliantContinuationState(synCtx, 3, getMediatorPosition());
                boolean result = onAcceptMediator.mediate(synCtx);
                if (result) {
                    ContinuationStackManager.removeReliantContinuationState(synCtx);
                }
                return result;
            } else {
                if (log.isDebugEnabled()) {
                    log.debug("OnAccept sequence is not defined.");
                }
                return true;
            }
        } else {
            if (log.isDebugEnabled()) {
                log.debug("User is not authorized to perform the action");
            }
            if (onRejectSeqKey != null) {
                ContinuationStackManager.updateSeqContinuationState(synCtx, getMediatorPosition());
                return synCtx.getSequence(onRejectSeqKey).mediate(synCtx);
            } else if (onRejectMediator != null) {
                ContinuationStackManager.addReliantContinuationState(synCtx, 4, getMediatorPosition());
                boolean result = onRejectMediator.mediate(synCtx);
                if (result) {
                    ContinuationStackManager.removeReliantContinuationState(synCtx);
                }
                return result;
            } else {
                if (log.isDebugEnabled()) {
                    log.debug("OnReject sequence is not defined.");
                }
                throw new SynapseException("User is not authorized to perform the action");
            }
        }
    }

    public boolean mediate(MessageContext synCtx, ContinuationState continuationState) {
        SynapseLog synLog = getLog(synCtx);

        if (synLog.isTraceOrDebugEnabled()) {
            synLog.traceOrDebug("Entitlement mediator : Mediating from ContinuationState");
        }

        if (keyInvolved) {
            try {
                resolveEntitlementServerDynamicConfigs(synCtx);
            } catch (EntitlementProxyException e) {
                log.error("Error while initializing the PEP Proxy" + e);
                throw new SynapseException("Error while initializing the Entitlement PEP Proxy");
            }
        }

        boolean result = false;
        int subBranch = ((ReliantContinuationState) continuationState).getSubBranch();
        if (subBranch == 0) {   // For Advice mediator
            if (!continuationState.hasChild()) {
                result = ((SequenceMediator) adviceMediator).mediate(synCtx, continuationState.getPosition() + 1);
                if (result) {
                    // Stop the flow after executing all the mediators
                    ContinuationStackManager.clearStack(synCtx);
                    return false;
                }
            } else {
                FlowContinuableMediator mediator = (FlowContinuableMediator) ((SequenceMediator) adviceMediator)
                        .getChild(continuationState.getPosition());
                result = mediator.mediate(synCtx, continuationState.getChildContState());
            }
        } else if (subBranch == 1 || subBranch == 2) {    // For Obligation

            SequenceMediator sequenceMediator;
            if (subBranch == 1) {
                sequenceMediator = (SequenceMediator) synCtx.getSequence(obligationsSeqKey);
            } else {
                sequenceMediator = (SequenceMediator) obligationsMediator;
            }

            if (!continuationState.hasChild()) {

                result = sequenceMediator.mediate(synCtx, continuationState.getPosition() + 1);
                Boolean isContinuationCall = (Boolean) synCtx.getProperty(SynapseConstants.CONTINUATION_CALL);

                if (!result && isContinuationCall != null && isContinuationCall) {
                    // If result is false due to presence of a Call mediator, stop the flow
                    return false;
                } else {
                    ContinuationStackManager.removeReliantContinuationState(synCtx);

                    String decision = (String) synCtx.getProperty(ENTITLEMENT_DECISION);
                    if (!result) {
                        decision = "Deny";
                    }

                    // Set back the original payload
                    OMElement originalEnv = (OMElement) synCtx.getProperty(ORIGINAL_ENTITLEMENT_PAYLOAD);
                    try {
                        synCtx.setEnvelope(AXIOMUtils.getSOAPEnvFromOM(originalEnv));
                    } catch (AxisFault axisFault) {
                        handleException("Error while setting the original envelope back", synCtx);
                    }

                    result = executeDecisionMessageFlow(synCtx, decision);
                    if (result) {
                        // Just adding a dummy state back, which will be removed at the Sequence when returning.
                        ContinuationStackManager.addReliantContinuationState(synCtx, 1, getMediatorPosition());
                    }
                }
            } else {
                FlowContinuableMediator mediator = (FlowContinuableMediator) sequenceMediator
                        .getChild(continuationState.getPosition());
                result = mediator.mediate(synCtx, continuationState.getChildContState());
            }
        } else if (subBranch == 3) {    // For onAcceptMediator
            if (!continuationState.hasChild()) {
                result = ((SequenceMediator) onAcceptMediator).mediate(synCtx, continuationState.getPosition() + 1);
            } else {
                FlowContinuableMediator mediator = (FlowContinuableMediator) ((SequenceMediator) onAcceptMediator)
                        .getChild(continuationState.getPosition());
                result = mediator.mediate(synCtx, continuationState.getChildContState());
            }
        } else if (subBranch == 4) {    // For onReject Mediator
            if (!continuationState.hasChild()) {
                result = ((SequenceMediator) onRejectMediator).mediate(synCtx, continuationState.getPosition() + 1);
            } else {
                FlowContinuableMediator mediator = (FlowContinuableMediator) ((SequenceMediator) onRejectMediator)
                        .getChild(continuationState.getPosition());
                result = mediator.mediate(synCtx, continuationState.getChildContState());
            }
        }
        return result;
    }

    private Object loadClass(String className) throws AxisFault {
        try {
            Class clazz = Thread.currentThread().getContextClassLoader().loadClass(className);
            return clazz.newInstance();
        } catch (Exception e) {
            log.error("Error occurred while loading " + className, e);
        }
        return null;
    }

    public void init(SynapseEnvironment synEnv) {

        try {
            if (callbackClass != null && callbackClass.trim().length() > 0) {
                Object loadedClass = loadClass(callbackClass);
                if (loadedClass instanceof EntitlementCallbackHandler) {
                    callback = (EntitlementCallbackHandler) loadedClass;
                }
            } else {
                callback = new UTEntitlementCallbackHandler();
            }

            String remoteServiceUrlResolved = remoteServiceUrl;
            String remoteServiceUsernameResolved = remoteServiceUserName;
            String remoteServicePasswordResolved = remoteServicePassword;

            if (remoteServiceUrlKey != null && remoteServiceUrlKey.trim().length() > 0) {
                remoteServiceUrlResolved = resolveRegistryEntryText(synEnv, remoteServiceUrlKey);
                keyInvolved = true;
            }

            if (remoteServiceUserNameKey != null && remoteServiceUserNameKey.trim().length() > 0) {
                remoteServiceUsernameResolved = resolveRegistryEntryText(synEnv, remoteServiceUserNameKey);
                keyInvolved = true;
            }

            if (remoteServicePasswordKey != null && remoteServicePasswordKey.trim().length() > 0) {
                remoteServicePasswordResolved = resolveRegistryEntryText(synEnv, remoteServicePasswordKey);
                keyInvolved = true;
            }

            Map<String, Map<String, String>> appToPDPClientConfigMap = new HashMap<String, Map<String, String>>();
            Map<String, String> clientConfigMap = new HashMap<String, String>();

            if (client != null && client.equals(EntitlementConstants.SOAP)) {
                clientConfigMap.put(EntitlementConstants.CLIENT, client);
                clientConfigMap.put(EntitlementConstants.SERVER_URL, remoteServiceUrlResolved);
                clientConfigMap.put(EntitlementConstants.USERNAME, remoteServiceUsernameResolved);
                clientConfigMap.put(EntitlementConstants.PASSWORD, remoteServicePasswordResolved);
                clientConfigMap.put(EntitlementConstants.REUSE_SESSION, reuseSession);
            } else if (client != null && client.equals(EntitlementConstants.BASIC_AUTH)) {
                clientConfigMap.put(EntitlementConstants.CLIENT, client);
                clientConfigMap.put(EntitlementConstants.SERVER_URL, remoteServiceUrlResolved);
                clientConfigMap.put(EntitlementConstants.USERNAME, remoteServiceUsernameResolved);
                clientConfigMap.put(EntitlementConstants.PASSWORD, remoteServicePasswordResolved);
            } else if (client != null && client.equals(EntitlementConstants.THRIFT)) {
                clientConfigMap.put(EntitlementConstants.CLIENT, client);
                clientConfigMap.put(EntitlementConstants.SERVER_URL, remoteServiceUrlResolved);
                clientConfigMap.put(EntitlementConstants.USERNAME, remoteServiceUsernameResolved);
                clientConfigMap.put(EntitlementConstants.PASSWORD, remoteServicePasswordResolved);
                clientConfigMap.put(EntitlementConstants.REUSE_SESSION, reuseSession);
                clientConfigMap.put(EntitlementConstants.THRIFT_HOST, thriftHost);
                clientConfigMap.put(EntitlementConstants.THRIFT_PORT, thriftPort);
            } else if (client != null && client.equals(EntitlementConstants.WS_XACML)) {
                clientConfigMap.put(EntitlementConstants.CLIENT, client);
                clientConfigMap.put(EntitlementConstants.SERVER_URL, remoteServiceUrlResolved);
                clientConfigMap.put(EntitlementConstants.USERNAME, remoteServiceUsernameResolved);
                clientConfigMap.put(EntitlementConstants.PASSWORD, remoteServicePasswordResolved);
            } else if (client == null) {
                clientConfigMap.put(EntitlementConstants.SERVER_URL, remoteServiceUrlResolved);
                clientConfigMap.put(EntitlementConstants.USERNAME, remoteServiceUsernameResolved);
                clientConfigMap.put(EntitlementConstants.PASSWORD, remoteServicePasswordResolved);
            } else {
                log.error("EntitlementMediator initialization error: Unsupported client");
                throw new SynapseException("EntitlementMediator initialization error: Unsupported client");
            }

            appToPDPClientConfigMap
                    .put(EntitlementConstants.PDP_CONFIG_MAP_ENTITLEMENT_MEDIATOR_ENTRY, clientConfigMap);
            config = new PEPProxyConfig(appToPDPClientConfigMap,
                                        EntitlementConstants.PDP_CONFIG_MAP_ENTITLEMENT_MEDIATOR_ENTRY, cacheType,
                                        invalidationInterval, maxCacheEntries);

            try {
                pepProxy = new PEPProxy(config);
            } catch (EntitlementProxyException e) {
                log.error("Error while initializing the PEP Proxy" + e);
                throw new SynapseException("Error while initializing the Entitlement PEP Proxy");
            }

            if (onAcceptMediator instanceof ManagedLifecycle) {
                ((ManagedLifecycle) onAcceptMediator).init(synEnv);
            }
            if (onRejectMediator instanceof ManagedLifecycle) {
                ((ManagedLifecycle) onRejectMediator).init(synEnv);
            }
            if (obligationsMediator instanceof ManagedLifecycle) {
                ((ManagedLifecycle) obligationsMediator).init(synEnv);
            }
            if (adviceMediator instanceof ManagedLifecycle) {
                ((ManagedLifecycle) adviceMediator).init(synEnv);
            }

        } catch (AxisFault e) {
            String msg = "Error initializing entitlement mediator : " + e.getMessage();
            log.error(msg, e);
            throw new SynapseException(msg, e);
        }
    }

    @Override
    public void destroy() {

        remoteServiceUserName = null;
        remoteServicePassword = null;
        remoteServiceUrl = null;
        remoteServiceUserNameKey = null;
        remoteServicePasswordKey = null;
        remoteServiceUrlKey = null;
        callbackClass = null;
        client = null;
        thriftPort = null;
        thriftHost = null;
        reuseSession = null;
        cacheType = null;
        callback = null;
        onRejectSeqKey = null;
        onAcceptSeqKey = null;
        obligationsSeqKey = null;
        adviceSeqKey = null;
        pepProxy = null;

        if (onAcceptMediator instanceof ManagedLifecycle) {
            ((ManagedLifecycle) onAcceptMediator).destroy();
        }
        if (onRejectMediator instanceof ManagedLifecycle) {
            ((ManagedLifecycle) onRejectMediator).destroy();
        }
        if (obligationsMediator instanceof ManagedLifecycle) {
            ((ManagedLifecycle) obligationsMediator).destroy();
        }
        if (adviceMediator instanceof ManagedLifecycle) {
            ((ManagedLifecycle) adviceMediator).destroy();
        }

    }

    /**
     * Clone the provided message context
     *
     * @param synCtx - MessageContext which is subjected to the cloning
     * @return MessageContext the cloned message context
     */
    private MessageContext getClonedMessageContext(MessageContext synCtx) {

        MessageContext newCtx = null;
        try {
            newCtx = MessageHelper.cloneMessageContext(synCtx);
            // Set isServerSide property in the cloned message context
            ((Axis2MessageContext) newCtx).getAxis2MessageContext()
                    .setServerSide(((Axis2MessageContext) synCtx).getAxis2MessageContext().isServerSide());
        } catch (AxisFault axisFault) {
            handleException("Error cloning the message context", axisFault, synCtx);
        }
        return newCtx;
    }

    /**
     * Create a new SOAP envelope and insert the
     * the given omElement into its body.
     *
     * @param synCtx - original message context
     * @return newCtx created by the iteration
     * @throws AxisFault      if there is a message creation failure
     * @throws JaxenException if the expression evauation failure
     */
    private MessageContext getOMElementInserted(OMElement omElement, MessageContext synCtx)
            throws AxisFault, JaxenException {

        Iterator<OMNode> children = synCtx.getEnvelope().getBody().getChildren();
        while (children.hasNext()) {
            children.next().detach();
        }
        synCtx.getEnvelope().getBody().addChild(omElement);
        return synCtx;
    }

    /* Creating a soap response according the the soap namespce uri */
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

    /**
     * Resolves the registry key and evaluates the value for encoded content
     * This method uses SynapseEnvironment to resolve the keys
     *
     * @param synEnv      SynapseEnvironment when using this in init phase
     * @param regEntryKey registry entry key to be resolved
     * @return Resolved and decoded reg entry
     */
    private String resolveRegistryEntryText(SynapseEnvironment synEnv, String regEntryKey) {
        Object regEntry = synEnv.getSynapseConfiguration().getRegistry().lookup(regEntryKey);
        String resolvedValue = "";
        if (regEntry instanceof OMElement) {
            OMElement e = (OMElement) regEntry;
            resolvedValue = e.toString();
        } else if (regEntry instanceof OMText) {
            resolvedValue = ((OMText) regEntry).getText();
        } else if (regEntry instanceof String) {
            resolvedValue = (String) regEntry;
        }

        if (resolvedValue.startsWith(EntitlementConstants.ENCODE_PREFIX)) {
            try {
                resolvedValue = new String(
                        CryptoUtil.getDefaultCryptoUtil().base64DecodeAndDecrypt(resolvedValue.substring(4)));
            } catch (CryptoException e) {
                log.error("Error decrypting key " + e);
            }
        }

        return resolvedValue;

    }

    /**
     * Resolves the registry key and evaluates the value for encoded content
     * This method uses Message Context to resolve the keys
     *
     * @param synCtx      MessageContext when using this in mediate phase
     * @param regEntryKey registry entry key to be resolved
     * @return Resolved and decoded reg entry
     */
    private String resolveRegistryEntryText(MessageContext synCtx, String regEntryKey) {
        Object regEntry = synCtx.getEntry(regEntryKey);
        String resolvedValue = "";
        if (regEntry instanceof OMElement) {
            OMElement e = (OMElement) regEntry;
            resolvedValue = e.toString();
        } else if (regEntry instanceof OMText) {
            resolvedValue = ((OMText) regEntry).getText();
        } else if (regEntry instanceof String) {
            resolvedValue = (String) regEntry;
        }

        if (resolvedValue.startsWith(EntitlementConstants.ENCODE_PREFIX)) {
            try {
                resolvedValue = new String(
                        CryptoUtil.getDefaultCryptoUtil().base64DecodeAndDecrypt(resolvedValue.substring(4)));
            } catch (CryptoException e) {
                log.error("Error decrypting key " + e);
            }
        }

        return resolvedValue;
    }

    /**
     * This method resolves the dynamic configs used to init pepProxy in the runtime
     *
     * @param synCtx to resolve registry entries
     * @throws EntitlementProxyException If pepproxy init fails
     */
    private PEPProxy resolveEntitlementServerDynamicConfigs(MessageContext synCtx) throws EntitlementProxyException {

        if (remoteServiceUrlKey != null && remoteServiceUrlKey.trim().length() > 0) {
            config.getAppToPDPClientConfigMap().get(EntitlementConstants.PDP_CONFIG_MAP_ENTITLEMENT_MEDIATOR_ENTRY)
                    .put(EntitlementConstants.SERVER_URL, resolveRegistryEntryText(synCtx, remoteServiceUrlKey));
        }

        if (remoteServiceUserNameKey != null && remoteServiceUserNameKey.trim().length() > 0) {
            config.getAppToPDPClientConfigMap().get(EntitlementConstants.PDP_CONFIG_MAP_ENTITLEMENT_MEDIATOR_ENTRY)
                    .put(EntitlementConstants.USERNAME, resolveRegistryEntryText(synCtx, remoteServiceUserNameKey));
        }

        if (remoteServicePasswordKey != null && remoteServicePasswordKey.trim().length() > 0) {
            config.getAppToPDPClientConfigMap().get(EntitlementConstants.PDP_CONFIG_MAP_ENTITLEMENT_MEDIATOR_ENTRY)
                    .put(EntitlementConstants.PASSWORD, resolveRegistryEntryText(synCtx, remoteServicePasswordKey));
        }

        return new PEPProxy(config);
    }

    public String getCallbackClass() {
        return callbackClass;
    }

    public void setCallbackClass(String callbackClass) {
        this.callbackClass = callbackClass;
    }

    public String getRemoteServiceUserName() {
        return remoteServiceUserName;
    }

    public void setRemoteServiceUserName(String remoteServiceUserName) {
        this.remoteServiceUserName = remoteServiceUserName;
    }

    public String getRemoteServiceUserNameKey() {
        return remoteServiceUserNameKey;
    }

    public void setRemoteServiceUserNameKey(String remoteServiceUserNameKey) {
        this.remoteServiceUserNameKey = remoteServiceUserNameKey;
    }

    public String getRemoteServicePasswordKey() {
        return remoteServicePasswordKey;
    }

    public void setRemoteServicePasswordKey(String remoteServicePasswordKey) {
        this.remoteServicePasswordKey = remoteServicePasswordKey;
    }

    public String getRemoteServicePassword() {
        if (remoteServicePassword != null && !remoteServicePassword.isEmpty() && !remoteServicePassword
                .startsWith(EntitlementConstants.ENCODE_PREFIX)) {
            try {
                return EntitlementConstants.ENCODE_PREFIX + CryptoUtil.getDefaultCryptoUtil()
                        .encryptAndBase64Encode(remoteServicePassword.getBytes());
            } catch (CryptoException e) {
                log.error(e);
            }
        }
        return remoteServicePassword;
    }

    public void setRemoteServicePassword(String remoteServicePassword) {
        if (remoteServicePassword.startsWith(EntitlementConstants.ENCODE_PREFIX)) {
            try {
                this.remoteServicePassword = new String(
                        CryptoUtil.getDefaultCryptoUtil().base64DecodeAndDecrypt(remoteServicePassword.substring(4)));
            } catch (CryptoException e) {
                log.error(e);
            }
        } else {
            this.remoteServicePassword = remoteServicePassword;
        }
    }

    public String getRemoteServiceUrl() {
        return remoteServiceUrl;
    }

    public void setRemoteServiceUrl(String remoteServiceUrl) {
        this.remoteServiceUrl = remoteServiceUrl;
    }

    public String getRemoteServiceUrlKey() {
        return remoteServiceUrlKey;
    }

    public void setRemoteServiceUrlKey(String remoteServiceUrlKey) {
        this.remoteServiceUrlKey = remoteServiceUrlKey;
    }

    public String getCacheType() {
        return cacheType;
    }

    public void setCacheType(String cacheType) {
        this.cacheType = cacheType;
    }

    public int getInvalidationInterval() {
        return invalidationInterval;
    }

    public void setInvalidationInterval(int invalidationInterval) {
        this.invalidationInterval = invalidationInterval;
    }

    public int getMaxCacheEntries() {
        return maxCacheEntries;
    }

    public void setMaxCacheEntries(int maxCacheEntries) {
        this.maxCacheEntries = maxCacheEntries;
    }

    public String getClient() {
        return client;
    }

    public void setClient(String client) {
        this.client = client;
    }

    public String getThriftPort() {
        return thriftPort;
    }

    public void setThriftPort(String thriftPort) {
        this.thriftPort = thriftPort;
    }

    public String getThriftHost() {
        return thriftHost;
    }

    public void setThriftHost(String thriftHost) {
        this.thriftHost = thriftHost;
    }

    public String getReuseSession() {
        return reuseSession;
    }

    public void setReuseSession(String reuseSession) {
        this.reuseSession = reuseSession;
    }

    public String getOnRejectSeqKey() {
        return onRejectSeqKey;
    }

    public void setOnRejectMediator(Mediator onRejectMediator) {
        this.onRejectMediator = onRejectMediator;
    }

    public String getOnAcceptSeqKey() {
        return onAcceptSeqKey;
    }

    public void setOnAcceptMediator(Mediator onAcceptMediator) {
        this.onAcceptMediator = onAcceptMediator;
    }

    public Mediator getOnRejectMediator() {
        return onRejectMediator;
    }

    public void setOnRejectSeqKey(String onRejectSeqKey) {
        this.onRejectSeqKey = onRejectSeqKey;
    }

    public Mediator getOnAcceptMediator() {
        return onAcceptMediator;
    }

    public void setOnAcceptSeqKey(String onAcceptSeqKey) {
        this.onAcceptSeqKey = onAcceptSeqKey;
    }

    public String getObligationsSeqKey() {
        return obligationsSeqKey;
    }

    public void setObligationsMediator(Mediator obligationsMediator) {
        this.obligationsMediator = obligationsMediator;
    }

    public Mediator getObligationsMediator() {
        return obligationsMediator;
    }

    public void setObligationsSeqKey(String obligationsSeqKey) {
        this.obligationsSeqKey = obligationsSeqKey;
    }

    public Mediator getAdviceMediator() {
        return adviceMediator;
    }

    public void setAdviceMediator(Mediator adviceMediator) {
        this.adviceMediator = adviceMediator;
    }

    public String getAdviceSeqKey() {
        return adviceSeqKey;
    }

    public void setAdviceSeqKey(String adviceSeqKey) {
        this.adviceSeqKey = adviceSeqKey;
    }

    @Override
    public Mediator getInlineSequence(SynapseConfiguration synCfg, int inlinedSeqIdentifier) {
        if (inlinedSeqIdentifier == 0) {
            if (onRejectMediator != null) {
                return onRejectMediator;
            } else if (onRejectSeqKey != null) {
                return synCfg.getSequence(onRejectSeqKey);
            }
        } else if (inlinedSeqIdentifier == 1) {
            if (onAcceptMediator != null) {
                return onAcceptMediator;
            } else if (onAcceptSeqKey != null) {
                return synCfg.getSequence(onAcceptSeqKey);
            }
        } else if (inlinedSeqIdentifier == 2) {
            if (obligationsMediator != null) {
                return obligationsMediator;
            } else if (obligationsSeqKey != null) {
                return synCfg.getSequence(obligationsSeqKey);
            }
        } else if (inlinedSeqIdentifier == 3) {
            if (adviceMediator != null) {
                return adviceMediator;
            } else if (adviceSeqKey != null) {
                return synCfg.getSequence(adviceSeqKey);
            }
        }
        return null;
    }

}
