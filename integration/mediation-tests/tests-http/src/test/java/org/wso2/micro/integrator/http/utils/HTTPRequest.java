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

/**
 * This class is used to hold the HTTP request information used HTTP Core test cases.
 */
public class HTTPRequest {

    private RequestMethods method;
    private PayloadSize payloadSize;
    private boolean enableSSL;

    public HTTPRequest(RequestMethods method, PayloadSize payloadSize, boolean enableSSL) {

        this.method = method;
        this.payloadSize = payloadSize;
        this.enableSSL = enableSSL;
    }

    public PayloadSize getPayloadSize() {

        return payloadSize;
    }

    public void setPayloadSize(PayloadSize payloadSize) {

        this.payloadSize = payloadSize;
    }

    public RequestMethods getMethod() {

        return method;
    }

    public void setMethod(RequestMethods method) {

        this.method = method;
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
        builder.append("HTTPRequest [method=").append(method).append(", SSL=").append(enableSSL).append(", payload" +
                "=").append(payloadSize).append("]");
        return builder.toString();
    }
}
