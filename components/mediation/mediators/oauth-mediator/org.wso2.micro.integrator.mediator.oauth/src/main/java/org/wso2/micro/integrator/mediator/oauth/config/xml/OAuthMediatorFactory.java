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


import org.apache.axiom.om.OMAttribute;
import org.apache.axiom.om.OMElement;
import org.apache.synapse.Mediator;
import org.apache.synapse.config.xml.AbstractMediatorFactory;
import org.apache.synapse.config.xml.XMLConfigConstants;
import org.apache.synapse.SynapseException;
import org.wso2.micro.integrator.mediator.oauth.OAuthMediator;

import javax.xml.namespace.QName;
import java.util.Properties;

/**
 * <oauthService remoteServiceUrl = "https://identityserever/services/OAuthService"/>
 */
public class OAuthMediatorFactory extends AbstractMediatorFactory {

    private static final QName ELEMENT_OAUTH = new QName(XMLConfigConstants.SYNAPSE_NAMESPACE, "oauthService");
    private static final QName ATTR_NAME_SERVICE_EPR = new QName("remoteServiceUrl");
    private static final QName ATTR_NAME_USERNAME = new QName("username");
    private static final QName ATTR_NAME_PASSWORD = new QName("password");

    /**
     * {@inheritDoc}
     */
    public Mediator createSpecificMediator(OMElement element, Properties properties) {
        if (!ELEMENT_OAUTH.equals(element.getQName())) {
            handleException("Unable to create the OAuth mediator. "
                    + "Unexpected element as the OAuth mediator configuration");
        }

        OAuthMediator mediator = null;
        OMAttribute remoteServiceUrl = null;
        OMAttribute username = null;
        OMAttribute password = null;

        mediator = new OAuthMediator();

        remoteServiceUrl = element.getAttribute(ATTR_NAME_SERVICE_EPR);
        if (remoteServiceUrl != null && remoteServiceUrl.getAttributeValue() != null) {
            mediator.setRemoteServiceUrl(remoteServiceUrl.getAttributeValue());
        } else {
            throw new SynapseException(
                    "The 'remoteServiceUrl' attribute is required for the OAuth mediator");
        }
        username = element.getAttribute(ATTR_NAME_USERNAME);
        if (username != null && username.getAttributeValue() != null) {
            mediator.setUsername(username.getAttributeValue());
        } else {
            throw new SynapseException("The 'username' attribute is required for the OAuth mediator");
        }
        password = element.getAttribute(ATTR_NAME_PASSWORD);
        if (password != null && password.getAttributeValue() != null) {
            mediator.setPassword(password.getAttributeValue());
        } else {
            throw new SynapseException("The 'password' attribute is required for the OAuth mediator");
        }

        addAllCommentChildrenToList(element, mediator.getCommentsList());

        return mediator;
    }

    /**
     * {@inheritDoc}
     */
    public QName getTagQName() {
        return ELEMENT_OAUTH;
    }

}
