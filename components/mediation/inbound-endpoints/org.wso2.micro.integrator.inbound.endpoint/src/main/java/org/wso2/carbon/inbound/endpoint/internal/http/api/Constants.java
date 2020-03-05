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

/**
 * {@code Constants} contains constants related to internal http api implementation.
 */
public class Constants {

    static final String INTERNAL_HTTP_API_PORT = "internal.http.api.port";
    static final String INTERNAL_HTTPS_API_PORT = "internal.https.api.port";
    static final int DEFAULT_INTERNAL_HTTP_API_PORT = 9191;
    static final int DEFAULT_INTERNAL_HTTPS_API_PORT = 9154;
    static final String INTERNAL_HTTP_API_ENABLED = "internal.http.api.enabled";
    public static final String INTERNAL_APIS_FILE = "internal-apis.xml";
    public static final String PREFIX_TO_ENABLE_INTERNAL_APIS = "enable";
}
