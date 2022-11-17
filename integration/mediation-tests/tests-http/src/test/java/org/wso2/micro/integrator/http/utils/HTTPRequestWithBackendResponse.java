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
 * This class is used to hold the HTTP request and Mock backend response information used for HTTP Core test cases.
 */
public class HTTPRequestWithBackendResponse {

    private BackendResponse backendResponse;
    private HTTPRequest httpRequest;

    public HTTPRequestWithBackendResponse(HTTPRequest httpRequest, BackendResponse backendResponse) {

        this.backendResponse = backendResponse;
        this.httpRequest = httpRequest;
    }

    public BackendResponse getBackendResponse() {

        return backendResponse;
    }

    public void setBackendResponse(BackendResponse backendResponse) {

        this.backendResponse = backendResponse;
    }

    public HTTPRequest getHttpRequest() {

        return httpRequest;
    }

    public void setHttpRequest(HTTPRequest httpRequest) {

        this.httpRequest = httpRequest;
    }

    @Override
    public String toString() {

        return httpRequest + " -|- " + backendResponse;
    }
}
