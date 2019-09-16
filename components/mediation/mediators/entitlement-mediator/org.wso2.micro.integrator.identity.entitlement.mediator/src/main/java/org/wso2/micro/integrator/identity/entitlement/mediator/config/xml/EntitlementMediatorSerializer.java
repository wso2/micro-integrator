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
package org.wso2.micro.integrator.identity.entitlement.mediator.config.xml;

import org.apache.axiom.om.OMElement;
import org.apache.synapse.Mediator;
import org.apache.synapse.config.xml.AbstractMediatorSerializer;
import org.apache.synapse.config.xml.SequenceMediatorSerializer;
import org.apache.synapse.config.xml.XMLConfigConstants;
import org.apache.synapse.mediators.base.SequenceMediator;
import org.wso2.micro.integrator.identity.entitlement.mediator.EntitlementConstants;
import org.wso2.micro.integrator.identity.entitlement.mediator.EntitlementMediator;

public class EntitlementMediatorSerializer extends AbstractMediatorSerializer {

    /**
     * {@inheritDoc}
     */
    public String getMediatorClassName() {
        return EntitlementMediator.class.getName();
    }

    /**
     * {@inheritDoc}
     */
    public OMElement serializeSpecificMediator(Mediator mediator) {
        if (!(mediator instanceof EntitlementMediator)) {
            handleException("Unsupported mediator passed in for serialization : " + mediator.getType());
        }

        EntitlementMediator entitlement = null;
        OMElement entitlementElem = null;

        entitlement = (EntitlementMediator) mediator;
        entitlementElem = fac.createOMElement("entitlementService", synNS);
        saveTracingState(entitlementElem, entitlement);
        if (entitlement.getRemoteServiceUrl() != null && !entitlement.getRemoteServiceUrl().isEmpty()) {
            entitlementElem
                    .addAttribute(fac.createOMAttribute("remoteServiceUrl", nullNS, entitlement.getRemoteServiceUrl()));
        } else if (entitlement.getRemoteServiceUrlKey() != null && !entitlement.getRemoteServiceUrlKey().isEmpty()) {
            entitlementElem.addAttribute(
                    fac.createOMAttribute("remoteServiceUrlKey", nullNS, entitlement.getRemoteServiceUrlKey()));
        }

        if (entitlement.getRemoteServiceUserName() != null && !entitlement.getRemoteServiceUserName().isEmpty()) {
            entitlementElem.addAttribute(
                    fac.createOMAttribute("remoteServiceUserName", nullNS, entitlement.getRemoteServiceUserName()));
        } else if (entitlement.getRemoteServiceUserNameKey() != null && !entitlement.getRemoteServiceUserNameKey()
                .isEmpty()) {
            entitlementElem.addAttribute(fac.createOMAttribute("remoteServiceUserNameKey", nullNS,
                                                               entitlement.getRemoteServiceUserNameKey()));
        }

        if (entitlement.getRemoteServicePassword() != null && !entitlement.getRemoteServicePassword().isEmpty()) {
            entitlementElem.addAttribute(
                    fac.createOMAttribute("remoteServicePassword", nullNS, entitlement.getRemoteServicePassword()));
        } else if (entitlement.getRemoteServicePasswordKey() != null && !entitlement.getRemoteServicePasswordKey()
                .isEmpty()) {
            entitlementElem.addAttribute(fac.createOMAttribute("remoteServicePasswordKey", nullNS,
                                                               entitlement.getRemoteServicePasswordKey()));
        }

        if (entitlement.getCallbackClass() != null) {
            entitlementElem
                    .addAttribute(fac.createOMAttribute("callbackClass", nullNS, entitlement.getCallbackClass()));
        }

        if (entitlement.getCacheType() != null) {
            entitlementElem.addAttribute(fac.createOMAttribute("cacheType", nullNS, entitlement.getCacheType()));
        }

        if (entitlement.getInvalidationInterval() != 0) {
            entitlementElem.addAttribute(fac.createOMAttribute("invalidationInterval", nullNS, Integer.toString(
                    entitlement.getInvalidationInterval())));
        }

        if (entitlement.getMaxCacheEntries() != 0) {
            entitlementElem.addAttribute(fac.createOMAttribute("maxCacheEntries", nullNS,
                                                               Integer.toString(entitlement.getMaxCacheEntries())));
        }

        if (entitlement.getClient() != null) {
            entitlementElem
                    .addAttribute(fac.createOMAttribute(EntitlementConstants.CLIENT, nullNS, entitlement.getClient()));
        }

        if (entitlement.getThriftHost() != null) {
            entitlementElem.addAttribute(
                    fac.createOMAttribute(EntitlementConstants.THRIFT_HOST, nullNS, entitlement.getThriftHost()));
        }

        if (entitlement.getThriftPort() != null) {
            entitlementElem.addAttribute(
                    fac.createOMAttribute(EntitlementConstants.THRIFT_PORT, nullNS, entitlement.getThriftPort()));
        }

        if (entitlement.getReuseSession() != null) {
            entitlementElem.addAttribute(
                    fac.createOMAttribute(EntitlementConstants.REUSE_SESSION, nullNS, entitlement.getReuseSession()));
        }

        String onReject = entitlement.getOnRejectSeqKey();
        if (onReject != null) {
            entitlementElem.addAttribute(fac.createOMAttribute(XMLConfigConstants.ONREJECT, nullNS, onReject));
        } else {
            Mediator m = entitlement.getOnRejectMediator();
            SequenceMediatorSerializer serializer = new SequenceMediatorSerializer();
            if (m != null && m instanceof SequenceMediator) {
                OMElement element = serializer.serializeAnonymousSequence(null, (SequenceMediator) m);
                element.setLocalName(XMLConfigConstants.ONREJECT);
                entitlementElem.addChild(element);
            }
        }
        String onAccept = entitlement.getOnAcceptSeqKey();
        if (onAccept != null) {
            entitlementElem.addAttribute(fac.createOMAttribute(XMLConfigConstants.ONACCEPT, nullNS, onAccept));
        } else {
            Mediator m = entitlement.getOnAcceptMediator();
            SequenceMediatorSerializer serializer = new SequenceMediatorSerializer();
            if (m != null && m instanceof SequenceMediator) {
                OMElement element = serializer.serializeAnonymousSequence(null, (SequenceMediator) m);
                element.setLocalName(XMLConfigConstants.ONACCEPT);
                entitlementElem.addChild(element);
            }
        }
        String obligation = entitlement.getObligationsSeqKey();
        if (obligation != null) {
            entitlementElem
                    .addAttribute(fac.createOMAttribute(EntitlementMediatorFactory.OBLIGATIONS, nullNS, obligation));
        } else {
            Mediator m = entitlement.getObligationsMediator();
            SequenceMediatorSerializer serializer = new SequenceMediatorSerializer();
            if (m != null && m instanceof SequenceMediator) {
                OMElement element = serializer.serializeAnonymousSequence(null, (SequenceMediator) m);
                element.setLocalName(EntitlementMediatorFactory.OBLIGATIONS);
                entitlementElem.addChild(element);
            }
        }
        String advice = entitlement.getAdviceSeqKey();
        if (advice != null) {
            entitlementElem.addAttribute(fac.createOMAttribute(EntitlementMediatorFactory.ADVICE, nullNS, advice));
        } else {
            Mediator m = entitlement.getAdviceMediator();
            SequenceMediatorSerializer serializer = new SequenceMediatorSerializer();
            if (m != null && m instanceof SequenceMediator) {
                OMElement element = serializer.serializeAnonymousSequence(null, (SequenceMediator) m);
                element.setLocalName(EntitlementMediatorFactory.ADVICE);
                entitlementElem.addChild(element);
            }
        }

        serializeComments(entitlementElem, entitlement.getCommentsList());

        return entitlementElem;
    }

}
