/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.micro.integrator.management.apis;

import org.apache.synapse.MessageContext;
import org.apache.synapse.config.SynapseConfiguration;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.wso2.carbon.inbound.endpoint.internal.http.api.APIResource;

import java.util.Objects;
import java.util.Set;

/**
 * Adapter class to bridge {@link APIResource} and {@link MiApiResource}.
 */
public class ApiResourceAdapter extends APIResource {

    private final MiApiResource messageStoreResource;

    ApiResourceAdapter(String prefixMessageStore, MiApiResource messageStoreResource) {
        super(prefixMessageStore);
        this.messageStoreResource = messageStoreResource;
    }

    @Override
    public Set<String> getMethods() {
        return messageStoreResource.getMethods();
    }

    @Override
    public boolean invoke(MessageContext messageContext) {
        org.apache.axis2.context.MessageContext axis2MessageContext
                = ((Axis2MessageContext) messageContext).getAxis2MessageContext();
        SynapseConfiguration synapseConfiguration = messageContext.getConfiguration();

        if (Objects.isNull(axis2MessageContext) || Objects.isNull(synapseConfiguration)) {
            return false;
        }

        return messageStoreResource.invoke(messageContext, axis2MessageContext, synapseConfiguration);
    }
}
