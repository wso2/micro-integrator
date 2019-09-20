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

package org.wso2.micro.integrator.identity.entitlement.mediator;

import org.apache.synapse.config.xml.XMLConfigConstants;

import javax.xml.namespace.QName;

/**
 * Constants are stored
 */
public class EntitlementConstants {

    public static final QName ELEMENT_ENTITLEMENT = new QName(XMLConfigConstants.SYNAPSE_NAMESPACE,
                                                              "entitlementService");

    public static final QName ATTR_SERVER_URL = new QName("remoteServiceUrl");

    public static final QName ATTR_USER_NAME = new QName("remoteServiceUserName");

    public static final QName ATTR_NAME_PASSWORD = new QName("remoteServicePassword");

    public static final QName ATTR_SERVER_URL_KEY = new QName("remoteServiceUrlKey");

    public static final QName ATTR_USER_NAME_KEY = new QName("remoteServiceUserNameKey");

    public static final QName ATTR_NAME_PASSWORD_KEY = new QName("remoteServicePasswordKey");

    public static final QName ATTR_CALLBACK_CLASS = new QName("callbackClass");

    public static final QName ATTR_CACHE_TYPE = new QName("cacheType");

    public static final QName ATTR_INVALIDATION_INTERVAL = new QName("invalidationInterval");

    public static final QName ATTR_MAX_CACHE_ENTRIES = new QName("maxCacheEntries");

    public static final QName ATTR_THRIFT_HOST = new QName("thriftHost");

    public static final QName ATTR_THRIFT_PORT = new QName("thriftPort");

    public static final QName ATTR_REUSE_SESSION = new QName("reuseSession");

    public static final QName ATTR_CLIENT = new QName("client");

    public static final String SERVER_URL = "serverUrl";

    public static final String USERNAME = "userName";

    public static final String PASSWORD = "password";

    public static final String THRIFT_HOST = "thriftHost";

    public static final String THRIFT_PORT = "thriftPort";

    public static final String CLIENT = "client";

    public static final String REUSE_SESSION = "reuseSession";

    public static final String JSON = "json";

    public static final String SOAP = "soap";

    public static final String THRIFT = "thrift";

    public static final String BASIC_AUTH = "basicAuth";

    public static final String WS_XACML = "wsXacml";

    public static final String ENCODE_PREFIX = "enc:";

    public static final String PDP_CONFIG_MAP_ENTITLEMENT_MEDIATOR_ENTRY = "EntitlementMediator";

}
