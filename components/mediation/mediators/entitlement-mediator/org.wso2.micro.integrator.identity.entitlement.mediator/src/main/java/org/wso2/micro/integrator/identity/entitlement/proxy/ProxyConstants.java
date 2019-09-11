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

package org.wso2.micro.integrator.identity.entitlement.proxy;

public final class ProxyConstants {

    public static final String JSON = "json";
    public static final String SOAP = "soap";
    public static final String THRIFT = "thrift";
    public static final String BASIC_AUTH = "basicAuth";
    public static final String WS_XACML = "wsXacml";

    public static final String PEP_PROXY_CACHE_MANAGER = "PEP_PROXY_CACHE_MANAGER";
    public static final String DECISION_CACHE = "DECISION_CACHE";
    public static final int MAX_CACHE_SIZE = 10000;

    public static final String SESSION_TIME_OUT = "50977";

    public static final String SUBJECT_ID = "urn:oasis:names:tc:xacml:1.0:subject:subject-id";

    public static final String TRUST_STORE = "javax.net.ssl.trustStore";
    public static final String TRUST_STORE_PASSWORD = "javax.net.ssl.trustStorePassword";
    public static final int THRIFT_TIME_OUT = 30000;
    public static final int DEFAULT_THRIFT_PORT = 10500;

    public static final String DEFAULT_DATA_TYPE = "string";

    private ProxyConstants() {

    }

}
