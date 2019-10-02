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

package org.wso2.carbon.inbound.endpoint.persistence;

import org.apache.synapse.inbound.InboundProcessorParams;
import org.apache.synapse.transport.passthru.core.ssl.SSLConfiguration;

public class InboundEndpointInfoDTO {

    private String tenantDomain;
    private String protocol;
    private String endpointName;
    private SSLConfiguration sslConfiguration;
    private InboundProcessorParams inboundParams;

    public InboundEndpointInfoDTO(String tenantDomain, String protocol, String endpointName,
                                  InboundProcessorParams inboundParams) {
        this.tenantDomain = tenantDomain;
        this.protocol = protocol;
        this.endpointName = endpointName;
        this.inboundParams = inboundParams;
    }

    public String getTenantDomain() {
        return tenantDomain;
    }

    public String getProtocol() {
        return protocol;
    }

    public String getEndpointName() {
        return endpointName;
    }

    public void setEndpointName(String endpointName) {
        this.endpointName = endpointName;
    }

    public InboundProcessorParams getInboundParams() {
        return this.inboundParams;
    }

    public SSLConfiguration getSslConfiguration() {
        return sslConfiguration;
    }

    public void setSslConfiguration(SSLConfiguration sslConfiguration) {
        this.sslConfiguration = sslConfiguration;
    }
}
