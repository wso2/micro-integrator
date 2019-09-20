/**
 *  Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.micro.integrator.initializer.handler;

import org.apache.synapse.AbstractSynapseHandler;
import org.apache.synapse.MessageContext;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.apache.ws.security.kerberos.KrbSession;
import org.apache.ws.security.kerberos.KrbSessionCache;

/**
 * Handler to set external properties to the synapse message context
 */
public class SynapseExternalPropertyConfigurator extends AbstractSynapseHandler {

    private final String KRB_SESSION = "__WS_SEC_KRB_SESSION";
    private final String SEC_MODULE = "rampart";

    @Override
    public boolean handleRequestInFlow(MessageContext synCtx) {


        //For WS-Security Kerberos Scenario
        if (((Axis2MessageContext) synCtx).getAxis2MessageContext().isEngaged(SEC_MODULE)) {
            // We need to get the session from thread local context and assign it the message context.
            // In the response path we need to do the vise-versa.
            // This has to be done because in synapse two threads are used in request and response flows
            KrbSessionCache sessionCache = KrbSessionCache.getInstance();
            if (sessionCache != null) {
                KrbSession krbSession = sessionCache.getCurrentSession();
                if (krbSession != null) {
                    synCtx.setProperty(KRB_SESSION, krbSession);
                }
            }
        }
        return true;
    }

    @Override
    public boolean handleRequestOutFlow(MessageContext synCtx) {
        return true;
    }

    @Override
    public boolean handleResponseInFlow(MessageContext synCtx) {
        return true;
    }

    @Override
    public boolean handleResponseOutFlow(MessageContext synCtx) {

        //For WS-Security Kerberos Scenario
        if (((Axis2MessageContext) synCtx).getAxis2MessageContext().isEngaged(SEC_MODULE)) {
            Object obj = synCtx.getProperty(KRB_SESSION);
            if (obj != null && obj instanceof KrbSession) {
                KrbSessionCache.getInstance().setCurrentSession((KrbSession) obj);
            }
        }
        return true;
    }
}
