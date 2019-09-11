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
package org.wso2.micro.integrator.mediator.oauth.config.xml;

import org.apache.axiom.om.OMElement;
import org.apache.synapse.Mediator;
import org.apache.synapse.config.xml.AbstractMediatorSerializer;
import org.wso2.micro.integrator.mediator.oauth.OAuthMediator;

public class OAuthMediatorSerializer extends AbstractMediatorSerializer {

    /**
     * {@inheritDoc}
     */
    public String getMediatorClassName() {
        return OAuthMediator.class.getName();
    }

    /**
     * {@inheritDoc}
     */
    public OMElement serializeSpecificMediator(Mediator mediator) {

        if (!(mediator instanceof OAuthMediator)) {
            handleException("Unsupported mediator passed in for serialization : "
                    + mediator.getType());
        }

        OAuthMediator oauth = null;
        OMElement oauthElem = null;

        oauth = (OAuthMediator) mediator;
        oauthElem = fac.createOMElement("oauthService", synNS);
        saveTracingState(oauthElem, oauth);
        oauthElem.addAttribute(fac.createOMAttribute("remoteServiceUrl", nullNS, oauth
                .getRemoteServiceUrl()));
        if (oauth.getUsername() != null) {
            oauthElem.addAttribute(fac.createOMAttribute("username", nullNS, oauth.getUsername()));
        }
        if (oauth.getPassword() != null) {
            oauthElem.addAttribute(fac.createOMAttribute("password", nullNS, oauth.getPassword()));
        }
        oauth = (OAuthMediator) mediator;

        serializeComments(oauthElem, ((OAuthMediator) mediator).getCommentsList());

        return oauthElem;
    }
}
