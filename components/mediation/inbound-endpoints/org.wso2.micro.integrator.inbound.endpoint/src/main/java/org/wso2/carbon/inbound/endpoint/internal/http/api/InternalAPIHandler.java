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

import java.util.List;

/**
 * {@code InternalAPIHandler} is the interface that need to be implemented in order to set handlers to an internal API.
 */
public interface InternalAPIHandler {

    /**
     * Gets the Handler name.
     *
     * @return the name of the Handler
     */
    String getName();

    /**
     * Sets the name of the Handler
     *
     * @param name name of the Handler
     */
    void setName(String name);

    /**
     * Invoke the handler logic
     *
     * @return state
     */
    Boolean invoke(MessageContext synCtx);

    /**
     * Sets the resources for the handler to be applied to.
     */
    void setResources(List<String> resources);

    /**
     * Get the resources handled by this handler.
     */
    List<String> getResources();

}
