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

public class KerberosEntitlementCallbackHandler extends EntitlementCallbackHandler {

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
        String clientPrinciple = (String) msgContext.getOptions().getProperty("client.principal.name");
        return clientPrinciple.substring(0, clientPrinciple.indexOf("@"));
    }
}
