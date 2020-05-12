/*
Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.micro.integrator.initializer.handler.transaction;

import org.apache.axis2.transport.base.BaseConstants;
import org.apache.synapse.AbstractSynapseHandler;
import org.apache.synapse.MessageContext;
import org.apache.synapse.core.axis2.Axis2MessageContext;

public class TransactionCountHandler extends AbstractSynapseHandler {

    private static long transactionCount = 0;

    @Override
    public boolean handleRequestInFlow(MessageContext messageContext) {
        org.apache.axis2.context.MessageContext axis2MessageContext =
                ((Axis2MessageContext) messageContext).getAxis2MessageContext();
        Object transactionProperty = axis2MessageContext.getProperty(BaseConstants.INTERNAL_TRANSACTION_COUNTED);
        // increment the transaction count by 1 if the INTERNAL_TRANSACTION_COUNTED message property has not been set
        // or if the value of the property is set to false.
        if (!(transactionProperty instanceof Boolean && (Boolean) transactionProperty)) {
            axis2MessageContext.setProperty(BaseConstants.INTERNAL_TRANSACTION_COUNTED, true);
            transactionCount += 1;
        }
        return true;
    }

    @Override
    public boolean handleRequestOutFlow(MessageContext messageContext) {
        return true;
    }

    @Override
    public boolean handleResponseInFlow(MessageContext messageContext) {
        return true;
    }

    @Override
    public boolean handleResponseOutFlow(MessageContext messageContext) {
        return true;
    }

    public static long getTransactionCount() {
        return transactionCount;
    }
}
