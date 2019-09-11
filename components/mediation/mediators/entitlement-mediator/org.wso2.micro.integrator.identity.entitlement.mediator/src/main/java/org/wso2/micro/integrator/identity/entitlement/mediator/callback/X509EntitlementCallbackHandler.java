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

package org.wso2.micro.integrator.identity.entitlement.mediator.callback;

import org.apache.synapse.MessageContext;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.wso2.micro.integrator.identity.entitlement.proxy.Attribute;
import org.wso2.micro.integrator.identity.entitlement.proxy.ProxyConstants;

import java.security.cert.X509Certificate;

public class X509EntitlementCallbackHandler extends EntitlementCallbackHandler {

    /*
     * (non-Javadoc)
     *
     * @see
     * org.wso2.carbon.identity.entitlement.mediator.callback.EntitlementCallbackHandler#getUserName(org.
     * apache.synapse.MessageContext)
     */
    public String getUserName(MessageContext synCtx) {
        org.apache.axis2.context.MessageContext msgContext;
        Axis2MessageContext axis2Msgcontext = null;
        axis2Msgcontext = (Axis2MessageContext) synCtx;
        msgContext = axis2Msgcontext.getAxis2MessageContext();
        // For WS-Security
        X509Certificate cert = (X509Certificate) msgContext.getProperty("X509Certificate");
        if (cert == null) {
            // For mutual authentication
            Object sslCertObject = msgContext.getProperty("ssl.client.auth.cert.X509");
            javax.security.cert.X509Certificate[] certs = (javax.security.cert.X509Certificate[]) sslCertObject;
            if (certs != null && certs.length > 0) {
                return certs[0].getSubjectDN().getName();
            }
        } else {
            return cert.getSubjectDN().getName();
        }
        return null;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.wso2.carbon.identity.entitlement.mediator.callback.EntitlementCallbackHandler#findOtherAttributes(
     * org.apache.synapse.MessageContext)
     */
    public Attribute[] findOtherAttributes(MessageContext synCtx) {
        org.apache.axis2.context.MessageContext msgContext;
        Axis2MessageContext axis2Msgcontext = null;
        axis2Msgcontext = (Axis2MessageContext) synCtx;
        msgContext = axis2Msgcontext.getAxis2MessageContext();
        String issuer = null;
        String signatureAlgo = null;
        // For WS-Security
        X509Certificate cert = (X509Certificate) msgContext.getProperty("X509Certificate");
        if (cert == null) {
            // For mutual authentication
            Object sslCertObject = msgContext.getProperty("ssl.client.auth.cert.X509");
            javax.security.cert.X509Certificate[] certs = (javax.security.cert.X509Certificate[]) sslCertObject;
            if (certs != null && certs.length > 0) {
                issuer = certs[0].getIssuerDN().getName();
                signatureAlgo = certs[0].getSigAlgName();
            }
        } else {
            issuer = cert.getIssuerDN().getName();
            signatureAlgo = cert.getSigAlgName();
        }
        if (issuer != null && signatureAlgo != null) {
            Attribute issuerAttr = new Attribute("urn:oasis:names:tc:xacml:3.0:attribute-category:environment",
                                                 "IssuerDN", ProxyConstants.DEFAULT_DATA_TYPE, issuer);
            Attribute signatureAlgoAttr = new Attribute("urn:oasis:names:tc:xacml:3.0:attribute-category:environment",
                                                        "SignatureAlgorithm", ProxyConstants.DEFAULT_DATA_TYPE,
                                                        signatureAlgo);
            return new Attribute[] { issuerAttr, signatureAlgoAttr };
        }
        return null;
    }
}
