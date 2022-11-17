/*
 * Copyright (c) 2022, WSO2 LLC (http://www.wso2.com).
 *
 * WSO2 LLC licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.micro.integrator.http.utils;

import static org.wso2.micro.integrator.http.utils.Constants.HTTPS_BACKEND_PORT;
import static org.wso2.micro.integrator.http.utils.Constants.HTTP_BACKEND_PORT;

/**
 * This class is used to hold the Backend response information used for HTTP Core test cases.
 */
public class BackendResponse {

    public enum Protocol {
        /**
         * HTTP protocol
         */
        HTTP("http"),
        /**
         * HTTPS protocol
         */
        HTTPS("https");

        private final String protocol;

        Protocol(String protocol) {

            this.protocol = protocol;
        }

        @Override
        public String toString() {

            return this.protocol;
        }
    }

    private PayloadSize backendPayloadSize;
    private boolean enableSSL;
    private int port;
    private Protocol protocol;

    public BackendResponse(PayloadSize backendPayloadSize, boolean enableSSL) {

        this.backendPayloadSize = backendPayloadSize;
        this.enableSSL = enableSSL;
        if (enableSSL) {
            protocol = Protocol.HTTPS;
            port = HTTPS_BACKEND_PORT;
        } else {
            protocol = Protocol.HTTP;
            port = HTTP_BACKEND_PORT;
        }
    }

    public PayloadSize getBackendPayloadSize() {

        return backendPayloadSize;
    }

    public void setBackendPayloadSize(PayloadSize backendPayloadSize) {

        this.backendPayloadSize = backendPayloadSize;
    }

    public int getPort() {

        return port;
    }

    public void setPort(int port) {

        this.port = port;
    }

    public Protocol getProtocol() {

        return protocol;
    }

    public void setProtocol(Protocol protocol) {

        this.protocol = protocol;
    }

    public boolean isSSLEnabled() {

        return enableSSL;
    }

    public void setSSLEnabled(boolean enableSSL) {

        this.enableSSL = enableSSL;
    }

    @Override
    public String toString() {

        StringBuilder builder = new StringBuilder();
        builder.append("BackendResponse [SSL=").append(enableSSL).append(", payload" +
                "=").append(backendPayloadSize).append("]");
        return builder.toString();
    }
}
