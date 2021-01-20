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

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * This class holds CORS configurations made in internal-apis.xml file
 */
public class InternalAPICORSConfiguration implements CORSConfiguration {
    private boolean enabled = false;
    private Set<String> allowedOrigins;
    private String allowedHeaders;

    @Override
    public Set<String> getAllowedOrigins() {

        return allowedOrigins;
    }

    @Override
    public String getAllowedHeaders() {

        return allowedHeaders;
    }

    @Override
    public boolean isEnabled() {

        return enabled;
    }

    public void setEnabled(boolean enabled) {

        this.enabled = enabled;
    }

    public void setAllowedOrigins(String origins) {

        allowedOrigins = new HashSet<>(Arrays.asList(origins.split(",")));
    }

    public void setAllowedHeaders(String allowedHeaders) {

        this.allowedHeaders = allowedHeaders;
    }
}
