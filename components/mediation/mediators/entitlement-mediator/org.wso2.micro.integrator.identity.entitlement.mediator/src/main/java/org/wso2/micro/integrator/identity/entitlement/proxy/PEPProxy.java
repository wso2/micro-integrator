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

import org.wso2.micro.integrator.identity.entitlement.proxy.exception.EntitlementProxyException;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class PEPProxy {

    public static final String SIMPLE = "simple";
    public static final String CARBON = "carbon";
    public static final String URN_OASIS_NAMES_TC_XACML_1_0_SUBJECT_CATEGORY_ACCESS_SUBJECT = "urn:oasis:names:tc:xacml:1.0:subject-category:access-subject";
    public static final String URN_OASIS_NAMES_TC_XACML_1_0_SUBJECT_SUBJECT_ID = "urn:oasis:names:tc:xacml:1.0:subject:subject-id";
    public static final String URN_OASIS_NAMES_TC_XACML_3_0_ATTRIBUTE_CATEGORY_ACTION = "urn:oasis:names:tc:xacml:3.0:attribute-category:action";
    public static final String URN_OASIS_NAMES_TC_XACML_1_0_ACTION_ACTION_ID = "urn:oasis:names:tc:xacml:1.0:action:action-id";
    public static final String URN_OASIS_NAMES_TC_XACML_3_0_ATTRIBUTE_CATEGORY_RESOURCE = "urn:oasis:names:tc:xacml:3.0:attribute-category:resource";
    public static final String URN_OASIS_NAMES_TC_XACML_1_0_RESOURCE_RESOURCE_ID = "urn:oasis:names:tc:xacml:1.0:resource:resource-id";
    public static final String URN_OASIS_NAMES_TC_XACML_3_0_ATTRIBUTE_CATEGORY_ENVIRONMENT = "urn:oasis:names:tc:xacml:3.0:attribute-category:environment";
    public static final String URN_OASIS_NAMES_TC_XACML_1_0_ENVIRONMENT_ENVIRONMENT_ID = "urn:oasis:names:tc:xacml:1.0:environment:environment-id";
    private String defaultAppId;
    private Map<String, AbstractEntitlementServiceClient> appToPDPClientMap;
    private PEPProxyCache cache;

    /**
     * Creating the PDP Proxy instance and initializing it
     *
     * @throws Exception
     */
    public PEPProxy(PEPProxyConfig config) throws EntitlementProxyException {
        defaultAppId = config.getDefaultAppId();

        if (config.getCacheType() != null && (SIMPLE.equals(config.getCacheType()) || CARBON
                .equals(config.getCacheType()))) {
            cache = new PEPProxyCache(config.getCacheType(), config.getInvalidationInterval(),
                                      config.getMaxCacheEntries());
        }
        appToPDPClientMap = PEPProxyFactory.getAppToPDPClientMap(config.getAppToPDPClientConfigMap());
    }

    /**
     * This method is used to get the Entitlement decision for the set of Attributes using The Default AppID
     *
     * @param attributes XACML 3.0 Attribute Set
     * @return the Entitlement Decision as a String
     * @throws Exception
     */
    public String getDecision(Attribute[] attributes) throws Exception {
        return getDecision(attributes, defaultAppId);
    }

    /**
     * This method is used to get the Entitlement decision for the set of Attributes using The Provided AppID
     *
     * @param attributes XACML 3.0 Attribute Set
     * @return the Entitlement Decision as a String
     * @throws Exception
     */
    public String getDecision(Attribute[] attributes, String appId) throws Exception {
        AbstractEntitlementServiceClient appProxy;
        if (!appToPDPClientMap.containsKey(appId)) {
            throw new EntitlementProxyException("Invalid App Id");
        } else {
            appProxy = appToPDPClientMap.get(appId);
        }
        if (cache != null) {
            String key = generateKey(attributes);
            String decision = cache.get(key);
            if (decision != null) {
                return decision;
            } else {
                decision = appProxy.getDecision(attributes, appId);
                cache.put(key, decision);
                return decision;
            }
        } else {
            return appProxy.getDecision(attributes, appId);
        }

    }

    /**
     * This method is used to get the Entitlement decision for the provided subject,resource,action and environment using the default appID of the PDP defaultProxy
     *
     * @param subject     XACML 2.0 subject
     * @param resource    XACML 2.0 resource
     * @param action      XACML 2.0 action
     * @param environment XACML 2.0 environments
     * @return the Entitlement Decision as a String
     * @throws Exception
     */
    public String getDecision(String subject, String resource, String action, String environment) throws Exception {
        return getDecision(subject, resource, action, environment, defaultAppId);
    }

    /**
     * This method is used to get the Entitlement decision for the provided subject,resource,action and environment using the provided appID of the PDP defaultProxy
     *
     * @param subject     XACML 2.0 subject
     * @param resource    XACML 2.0 resource
     * @param action      XACML 2.0 action
     * @param environment XACML 2.0 environments
     * @param appId       specific appID in the PDP Proxy, there can be many PDPs configured for appID. Each App can have distinct PDPs
     * @return the Entitlement Decision as a String
     * @throws Exception
     */
    public String getDecision(String subject, String resource, String action, String environment, String appId)
            throws Exception {

        if (!appToPDPClientMap.containsKey(appId)) {
            throw new EntitlementProxyException("Invalid App Id");
        }
        Attribute subjectAttribute = new Attribute(URN_OASIS_NAMES_TC_XACML_1_0_SUBJECT_CATEGORY_ACCESS_SUBJECT,
                                                   URN_OASIS_NAMES_TC_XACML_1_0_SUBJECT_SUBJECT_ID,
                                                   ProxyConstants.DEFAULT_DATA_TYPE, subject);
        Attribute actionAttribute = new Attribute(URN_OASIS_NAMES_TC_XACML_3_0_ATTRIBUTE_CATEGORY_ACTION,
                                                  URN_OASIS_NAMES_TC_XACML_1_0_ACTION_ACTION_ID,
                                                  ProxyConstants.DEFAULT_DATA_TYPE, action);
        Attribute resourceAttribute = new Attribute(URN_OASIS_NAMES_TC_XACML_3_0_ATTRIBUTE_CATEGORY_RESOURCE,
                                                    URN_OASIS_NAMES_TC_XACML_1_0_RESOURCE_RESOURCE_ID,
                                                    ProxyConstants.DEFAULT_DATA_TYPE, resource);
        Attribute environmentAttribute = new Attribute(URN_OASIS_NAMES_TC_XACML_3_0_ATTRIBUTE_CATEGORY_ENVIRONMENT,
                                                       URN_OASIS_NAMES_TC_XACML_1_0_ENVIRONMENT_ENVIRONMENT_ID,
                                                       ProxyConstants.DEFAULT_DATA_TYPE, environment);
        Attribute[] tempArr = { subjectAttribute, actionAttribute, resourceAttribute, environmentAttribute };
        return getDecision(tempArr, appId);
    }

    public boolean subjectCanActOnResource(String subjectType, String alias, String actionId, String resourceId,
                                           String domainId) throws Exception {
        return subjectCanActOnResource(subjectType, alias, actionId, resourceId, domainId, defaultAppId);
    }

    public boolean subjectCanActOnResource(String subjectType, String alias, String actionId, String resourceId,
                                           String domainId, String appId) throws Exception {
        AbstractEntitlementServiceClient appProxy = null;
        if (!appToPDPClientMap.containsKey(appId)) {
            throw new EntitlementProxyException("Invalid App Id");
        } else {
            appProxy = appToPDPClientMap.get(appId);
        }
        return appProxy.subjectCanActOnResource(subjectType, alias, actionId, resourceId, domainId, appId);
    }

    public boolean subjectCanActOnResource(String subjectType, String alias, String actionId, String resourceId,
                                           Attribute[] attributes, String domainId) throws Exception {
        return subjectCanActOnResource(subjectType, alias, actionId, resourceId, attributes, domainId, defaultAppId);
    }

    public boolean subjectCanActOnResource(String subjectType, String alias, String actionId, String resourceId,
                                           Attribute[] attributes, String domainId, String appId) throws Exception {
        AbstractEntitlementServiceClient appProxy = null;
        if (!appToPDPClientMap.containsKey(appId)) {
            throw new EntitlementProxyException("Invalid App Id");
        } else {
            appProxy = appToPDPClientMap.get(appId);
        }
        return appProxy.subjectCanActOnResource(subjectType, alias, actionId, resourceId, attributes, domainId, appId);
    }

    public List<String> getActionableChildResourcesForAlias(String alias, String parentResource, String action)
            throws Exception {
        return getActionableChildResourcesForAlias(alias, parentResource, action, defaultAppId);
    }

    public List<String> getActionableChildResourcesForAlias(String alias, String parentResource, String action,
                                                            String appId) throws Exception {
        AbstractEntitlementServiceClient appProxy = null;
        if (!appToPDPClientMap.containsKey(appId)) {
            throw new EntitlementProxyException("Invalid App Id");
        } else {
            appProxy = appToPDPClientMap.get(appId);
        }
        return appProxy.getActionableChildResourcesForAlias(alias, parentResource, action, appId);
    }

    public List<String> getResourcesForAlias(String alias) throws Exception {
        return getResourcesForAlias(alias, defaultAppId);
    }

    public List<String> getResourcesForAlias(String alias, String appId) throws Exception {
        AbstractEntitlementServiceClient appProxy = null;
        if (!appToPDPClientMap.containsKey(appId)) {
            throw new EntitlementProxyException("Invalid App Id");
        } else {
            appProxy = appToPDPClientMap.get(appId);
        }
        return appProxy.getResourcesForAlias(alias, appId);
    }

    public List<String> getActionableResourcesForAlias(String alias) throws Exception {
        return getActionableResourcesForAlias(alias, defaultAppId);
    }

    public List<String> getActionableResourcesForAlias(String alias, String appId) throws Exception {
        AbstractEntitlementServiceClient appProxy = null;
        if (!appToPDPClientMap.containsKey(appId)) {
            throw new EntitlementProxyException("Invalid App Id");
        } else {
            appProxy = appToPDPClientMap.get(appId);
        }
        return appProxy.getActionableResourcesForAlias(alias, appId);
    }

    public List<String> getActionsForResource(String alias, String resources) throws Exception {
        return getActionsForResource(alias, resources, defaultAppId);
    }

    public List<String> getActionsForResource(String alias, String resources, String appId) throws Exception {
        AbstractEntitlementServiceClient appProxy = null;
        if (!appToPDPClientMap.containsKey(appId)) {
            throw new EntitlementProxyException("Invalid App Id");
        } else {
            appProxy = appToPDPClientMap.get(appId);
        }
        return appProxy.getActionsForResource(alias, resources, appId);
    }

    private String generateKey(Attribute[] attributes) {
        int key = 1;
        key = 11 * key + ((attributes == null) ? 0 : Arrays.hashCode(attributes));
        key = 31 * key + ((defaultAppId == null) ? 0 : defaultAppId.hashCode());
        return Integer.toString(key);
    }

    public void clear() {
        if (cache != null) {
            cache.clear();
        }
    }

}
