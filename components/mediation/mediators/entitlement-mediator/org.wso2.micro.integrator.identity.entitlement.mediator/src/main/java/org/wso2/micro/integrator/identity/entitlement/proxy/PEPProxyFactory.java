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

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.micro.integrator.identity.entitlement.proxy.exception.EntitlementProxyException;
import org.wso2.micro.integrator.identity.entitlement.proxy.soap.authenticationAdmin.SOAPEntitlementServiceClient;
import org.wso2.micro.integrator.identity.entitlement.proxy.soap.basicAuth.BasicAuthEntitlementServiceClient;
import org.wso2.micro.integrator.identity.entitlement.proxy.thrift.ThriftEntitlementServiceClient;
import org.wso2.micro.integrator.identity.entitlement.proxy.wsxacml.WSXACMLEntitlementServiceClient;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class PEPProxyFactory {

    private static final Log log = LogFactory.getLog(PEPProxyFactory.class);
    public static final String USER_NAME = "userName";
    public static final String PASSWORD = "password";
    public static final String REUSE_SESSION = "reuseSession";
    public static final String SERVER_URL = "serverUrl";
    public static final String THRIFT_HOST = "thriftHost";
    public static final String THRIFT_PORT = "thriftPort";
    public static final String AUTHORIZED_COOKIE = "authorizedCookie";

    private PEPProxyFactory() {

    }

    public static Map<String, AbstractEntitlementServiceClient> getAppToPDPClientMap(
            Map<String, Map<String, String>> appToPDPClientConfigMap) throws EntitlementProxyException {

        Map<String, AbstractEntitlementServiceClient> appToPDPClientMap = new HashMap<>();

        Set<String> appList = appToPDPClientConfigMap.keySet();
        Iterator appListItr = appList.iterator();
        while (appListItr.hasNext()) {
            String appId = (String) appListItr.next();
            Map<String, String> appConfig = appToPDPClientConfigMap.get(appId);
            String client = appConfig.get("client");
            if (client == null || client.trim().length() == 0) {
                log.debug("Using BasicAuthEntitlementServiceClient by default");
                client = "basicAuth";
            }
            if (!ProxyConstants.JSON.equals(client) && !ProxyConstants.SOAP.equals(client) && !ProxyConstants.THRIFT
                    .equals(client) && !ProxyConstants.BASIC_AUTH.equals(client) && !ProxyConstants.WS_XACML
                    .equals(client)) {
                throw new EntitlementProxyException("Invalid client. Should be json, soap, thrift or wsXacml");
            }

            if (ProxyConstants.SOAP.equals(client)) {
                if (appConfig.get(SERVER_URL) == null || appConfig.get(SERVER_URL).length() == 0) {
                    throw new EntitlementProxyException("serverUrl cannot be null or empty");
                }
                String serverUrl = appConfig.get(SERVER_URL).trim();
                if (!serverUrl.endsWith("/")) {
                    serverUrl += "/";
                }

                boolean reuseSession = true;
                if (appConfig.get(REUSE_SESSION) != null) {
                    reuseSession = Boolean.parseBoolean(appConfig.get(REUSE_SESSION));
                }

                if (StringUtils.isNotEmpty(appConfig.get(AUTHORIZED_COOKIE))) {
                    //if authorized cookie is available
                    appToPDPClientMap.put(appId,
                                          new SOAPEntitlementServiceClient(serverUrl, appConfig.get(AUTHORIZED_COOKIE),
                                                                           reuseSession));
                } else if (StringUtils.isNotEmpty(appConfig.get(USER_NAME)) && StringUtils
                        .isNotEmpty(appConfig.get(PASSWORD))) {
                    //if authorized credentials are available
                    appToPDPClientMap.put(appId, new SOAPEntitlementServiceClient(serverUrl, appConfig.get(USER_NAME),
                                                                                  appConfig.get(PASSWORD),
                                                                                  reuseSession));
                } else {
                    //when non of the authenticators available, trigger an exception
                    throw new EntitlementProxyException(
                            "Authentication failed. Either authorized cookie or username/password required to proceed.");
                }
            } else if (ProxyConstants.BASIC_AUTH.equals(client)) {
                if (appConfig.get(SERVER_URL) == null || appConfig.get(SERVER_URL).length() == 0) {
                    throw new EntitlementProxyException("serverUrl cannot be null or empty");
                }
                String serverUrl = appConfig.get(SERVER_URL).trim();
                if (!serverUrl.endsWith("/")) {
                    serverUrl += "/";
                }
                if (appConfig.get(USER_NAME) == null || appConfig.get(USER_NAME).length() == 0) {
                    throw new EntitlementProxyException("userName cannot be null or empty");
                }
                if (appConfig.get(PASSWORD) == null || appConfig.get(PASSWORD).length() == 0) {
                    throw new EntitlementProxyException("password cannot be null or empty");
                }
                appToPDPClientMap.put(appId, new BasicAuthEntitlementServiceClient(serverUrl, appConfig.get(USER_NAME),
                                                                                   appConfig.get(PASSWORD)));
            } else if (ProxyConstants.THRIFT.equals(client)) {
                if (appConfig.get(SERVER_URL) == null || appConfig.get(SERVER_URL).length() == 0) {
                    throw new EntitlementProxyException("serverUrl cannot be null or empty");
                }
                String serverUrl = appConfig.get(SERVER_URL).trim();
                if (!serverUrl.endsWith("/")) {
                    serverUrl += "/";
                }
                if (appConfig.get(USER_NAME) == null || appConfig.get(USER_NAME).length() == 0) {
                    throw new EntitlementProxyException("userName cannot be null or empty");
                }
                if (appConfig.get(PASSWORD) == null || appConfig.get(PASSWORD).length() == 0) {
                    throw new EntitlementProxyException("password cannot be null or empty");
                }
                if (appConfig.get(THRIFT_HOST) == null || appConfig.get(THRIFT_HOST).length() == 0) {
                    throw new EntitlementProxyException("thriftHost cannot be null or empty");
                }
                int thriftPort;
                if (appConfig.get(THRIFT_PORT) == null || appConfig.get(THRIFT_PORT).length() == 0) {
                    thriftPort = ProxyConstants.DEFAULT_THRIFT_PORT;
                } else {
                    thriftPort = Integer.parseInt(appConfig.get(THRIFT_PORT));
                }
                boolean reuseSession = true;
                if (appConfig.get(REUSE_SESSION) != null) {
                    reuseSession = Boolean.parseBoolean(appConfig.get(REUSE_SESSION));
                }
                appToPDPClientMap.put(appId, new ThriftEntitlementServiceClient(serverUrl, appConfig.get(USER_NAME),
                                                                                appConfig.get(PASSWORD),
                                                                                appConfig.get(THRIFT_HOST), thriftPort,
                                                                                reuseSession));
            } else if (ProxyConstants.WS_XACML.equals(client)) {
                if (appConfig.get(SERVER_URL) == null || appConfig.get(SERVER_URL).length() == 0) {
                    throw new EntitlementProxyException("serverUrl cannot be null or empty");
                }
                String serverUrl = appConfig.get(SERVER_URL).trim();
                if (!serverUrl.endsWith("/")) {
                    serverUrl += "/";
                }
                if (appConfig.get(USER_NAME) == null || appConfig.get(USER_NAME).length() == 0) {
                    throw new EntitlementProxyException("userName cannot be null or empty");
                }
                if (appConfig.get(PASSWORD) == null || appConfig.get(PASSWORD).length() == 0) {
                    throw new EntitlementProxyException("password cannot be null or empty");
                }
                appToPDPClientMap.put(appId, new WSXACMLEntitlementServiceClient(serverUrl, appConfig.get(USER_NAME),
                                                                                 appConfig.get(PASSWORD)));
            }
        }
        return appToPDPClientMap;
    }

}
