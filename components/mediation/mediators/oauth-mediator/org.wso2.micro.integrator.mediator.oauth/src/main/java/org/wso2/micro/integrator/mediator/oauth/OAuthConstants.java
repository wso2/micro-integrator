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
package org.wso2.micro.integrator.mediator.oauth;

public final class OAuthConstants {

    // OAuth request parameters
    public static final String OAUTH_VERSION = "oauth_version";
    public static final String OAUTH_NONCE = "oauth_nonce";
    public static final String OAUTH_TIMESTAMP = "oauth_timestamp";
    public static final String OAUTH_CONSUMER_KEY = "oauth_consumer_key";
    public static final String OAUTH_CALLBACK = "oauth_callback";
    public static final String OAUTH_SIGNATURE_METHOD = "oauth_signature_method";
    public static final String OAUTH_SIGNATURE = "oauth_signature";
    public static final String SCOPE = "scope";
    public static final String OAUTH_DISPLAY_NAME = "xoauth_displayname";
    // OAuth response parameters
    public static final String OAUTH_TOKEN = "oauth_token";
    public static final String OAUTH_TOKEN_SECRET = "oauth_token_secret";
    public static final String OAUTH_CALLBACK_CONFIRMED = "oauth_callback_confirmed";
    public static final String OAUTH_VERIFIER = "oauth_verifier";
    public static final String ASSOCIATION_OAUTH_CONSUMER_TOKEN = "ASSOCIATION_OAUTH_CONSUMER_TOKEN";
    public static final String OAUTHORIZED_USER = "OAUTHORIZED_USER";
    // OAuth 2.0 parameters
    public static final String BEARER = "Bearer ";
    public static final String ACCESS_TOKEN = "access_token";
    public static final String BEARER_TOKEN_TYPE = "bearer";
    public static final String OAUTH2_SCOPE_VALIDATION_ENABLED = "oauth2_scope_validation_enabled";
}
