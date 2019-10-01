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

package org.wso2.micro.integrator.dataservices.core.odata;

import org.apache.axis2.context.ConfigurationContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.micro.integrator.dataservices.core.internal.DataServicesDSComponent;
import org.wso2.micro.core.Constants;
import org.wso2.micro.core.util.ConfigurationContextService;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This class stores the OData Service handlers for services.
 */
public class ODataServiceRegistry {
    private static Log log = LogFactory.getLog(ODataServiceRegistry.class);

    private static ODataServiceRegistry instance;

    private Map<String, ConcurrentHashMap<String, ODataServiceHandler>> registry = new ConcurrentHashMap<>();

    public ODataServiceRegistry() {
        // ignore
    }

    public static ODataServiceRegistry getInstance() {
        if (instance == null) {
            synchronized (ODataServiceRegistry.class) {
                if (instance == null) {
                    instance = new ODataServiceRegistry();
                }
            }
        }
        return instance;
    }

    public void registerODataService(String dataServiceName, ODataServiceHandler handler, String tenantDomain) {
        ConcurrentHashMap<String, ODataServiceHandler> oDataServiceHandlerMap = this.registry.get(tenantDomain);
        if (oDataServiceHandlerMap == null) {
            oDataServiceHandlerMap = new ConcurrentHashMap<>();
            this.registry.put(tenantDomain, oDataServiceHandlerMap);
        }
        oDataServiceHandlerMap.putIfAbsent(dataServiceName, handler);
    }

    public ODataServiceHandler getServiceHandler(String serviceKey, String tenantDomain) {
        // Load tenant configs
        if (null == this.registry.get(tenantDomain) &&
            !Constants.SUPER_TENANT_DOMAIN_NAME.equals(tenantDomain)) {
            try {
                ConfigurationContextService contextService = DataServicesDSComponent.getContextService();
                ConfigurationContext configContext;
                if (contextService != null) {
                    // Getting server's configContext instance
                    configContext = contextService.getServerConfigContext();
                } else {
                    throw new ODataServiceFault("ConfigurationContext is not found.");
                }
            } catch (Exception e) {
                log.error("ConfigurationContext is not found.", e);
            }
        }
        if (this.registry.get(tenantDomain) != null) {
            return this.registry.get(tenantDomain).get(serviceKey);
        } else {
            return null;
        }
    }

    public void removeODataService(String tenantDomain, String serviceName) {
        this.registry.get(tenantDomain).remove(serviceName);
    }
}
