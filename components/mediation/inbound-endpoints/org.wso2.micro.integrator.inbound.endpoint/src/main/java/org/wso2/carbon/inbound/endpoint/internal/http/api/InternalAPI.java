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

import org.apache.synapse.api.cors.CORSConfiguration;

import java.util.List;

/**
 * {@code InternalAPI} is the interface that need to be implemented in order to register an internal API into EI.
 */
public interface InternalAPI {

    /**
     * Gets the API Resources.
     *
     * @return the array of API Resources
     */
    APIResource[] getResources();

    /**
     * Gets the API context.
     *
     * @return the API context
     */
    String getContext();

    /**
     * Gets the API name.
     *
     * @return the name of the API
     */
    String getName();

    /**
     * Sets the name of the API
     *
     * @param name name of the API
     */
    void setName(String name);

    /**
     * Sets the handlers associated with the API
     *
     * @param handlerList list of handlers
     */
    void setHandlers(List<InternalAPIHandler> handlerList);

    /**
     * Gets the handlers associated with the API
     */
    List<InternalAPIHandler> getHandlers();

    /**
     * Sets the CORS Configuration for the internal API
     */
    void setCORSConfiguration(CORSConfiguration corsConfiguration);

    /**
     * Gets the CORS Configuration of the API
     */
    CORSConfiguration getCORSConfiguration();
}
