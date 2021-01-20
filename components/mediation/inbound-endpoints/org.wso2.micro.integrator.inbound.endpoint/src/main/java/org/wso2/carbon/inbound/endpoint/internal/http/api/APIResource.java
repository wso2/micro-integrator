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
package org.wso2.carbon.inbound.endpoint.internal.http.api;

import org.apache.synapse.MessageContext;
import org.apache.synapse.SynapseException;
import org.apache.synapse.api.dispatch.DispatcherHelper;
import org.apache.synapse.api.dispatch.URITemplateHelper;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.apache.synapse.transport.passthru.util.RelayUtils;

import java.util.Set;

/**
 * {@code APIResource} is the abstract implementation of a Resource in an Internal API.
 * <p>
 * An {@link InternalAPI} must have one or more Resources. So if we want to register an internal api into EI,
 * we need to create one or more Resources extending this abstract class and make it accessible through
 * {@link InternalAPI#getResources()} method.
 */
public abstract class APIResource {

    private DispatcherHelper dispatcherHelper;

    /**
     * Gets the HTTP methods supported by this Resource.
     *
     * @return the supported HTTP methods
     */
    public abstract Set<String> getMethods();

    /**
     * Invokes the API Resource.
     *
     * @param synCtx the Synapse Message Context
     * @return whether to continue post invocation tasks
     */
    public abstract boolean invoke(MessageContext synCtx);

    /**
     * Constructor for creating an API Resource.
     *
     * @param urlTemplate the url template of the Resource
     */
    public APIResource(String urlTemplate) {
        dispatcherHelper = new URITemplateHelper(urlTemplate);
    }

    /**
     * Gets the {@link DispatcherHelper} related to the the Resource.
     *
     * @return the DispatcherHelper
     */
    public final DispatcherHelper getDispatcherHelper() {
        return dispatcherHelper;
    }

    /**
     * Builds the message by consuming the input stream.
     *
     * @param synCtx the Synapse Message Context
     */
    public final void buildMessage(MessageContext synCtx) {
        try {
            RelayUtils.buildMessage(((Axis2MessageContext) synCtx).getAxis2MessageContext(), false);
        } catch (Exception e) {
            throw new SynapseException("Error while building the message. " + e.getMessage(), e);
        }
    }

}
