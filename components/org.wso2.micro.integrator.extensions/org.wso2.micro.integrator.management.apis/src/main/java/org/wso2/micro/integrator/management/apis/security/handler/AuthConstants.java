/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.micro.integrator.management.apis.security.handler;

public class AuthConstants {
    //Response Status Codes
    public static final int SC_UNAUTHORIZED = 401;
    public static final int SC_FORBIDDEN = 403;

    //Response Header Strings
    public static final String HTTP_STATUS_CODE = "HTTP_SC";
    public static final String RESPONSE = "RESPONSE";
    public static final String TRUE = "true";
    public static final String NO_ENTITY_BODY = "NO_ENTITY_BODY";
    public static final String WWW_AUTHENTICATE = "WWW_Authenticate";
    public static final String WWW_AUTH_METHOD = "Basic realm=\"WSO2 EI\"";

    //JWT Token constants
    public static final int JWT_TOKEN_STORE_DEFAULT_SIZE = 256;
    public static final String TOKEN_STORE_KEY_ALGORITHM = "RSA";
    public static final String BASIC_AUTH_HEADER_TOKEN_TYPE = "Basic";
    public static final String BEARER_AUTH_HEADER_TOKEN_TYPE = "Bearer";
    public static final String RESPONSE_JSON_TOKEN_FIELD = "AccessToken";
    public static final String JWT_TOKEN_DEFAULT_SCOPE = "default";
    public static final String JWT_TOKEN_ADMIN_SCOPE = "admin";
    public static final String DEFAULT_ISSUER_NAME = "Admin";
    public static final int JWT_TOKEN_DEFAULT_SIZE = 2048;
    public static final long DEFAULT_EXPIRY_DURATION = 60;

}
