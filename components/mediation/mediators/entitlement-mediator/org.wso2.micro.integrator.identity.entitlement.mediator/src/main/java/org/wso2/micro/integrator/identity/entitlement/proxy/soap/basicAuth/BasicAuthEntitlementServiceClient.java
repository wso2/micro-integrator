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

package org.wso2.micro.integrator.identity.entitlement.proxy.soap.basicAuth;

import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.apache.axis2.description.TransportOutDescription;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.axis2.transport.http.HttpTransportProperties;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpConnectionManager;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.pool.impl.GenericObjectPool;
import org.wso2.carbon.identity.entitlement.stub.EntitlementServiceStub;
import org.wso2.carbon.identity.entitlement.stub.dto.EntitledAttributesDTO;
import org.wso2.carbon.identity.entitlement.stub.dto.EntitledResultSetDTO;
import org.wso2.micro.integrator.identity.entitlement.proxy.AbstractEntitlementServiceClient;
import org.wso2.micro.integrator.identity.entitlement.proxy.Attribute;
import org.wso2.micro.integrator.identity.entitlement.proxy.ProxyConstants;
import org.wso2.micro.integrator.identity.entitlement.proxy.XACMLRequetBuilder;
import org.wso2.micro.integrator.identity.entitlement.proxy.exception.EntitlementProxyException;
import org.wso2.micro.integrator.identity.entitlement.proxy.soap.util.EntitlementServiceStubFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class BasicAuthEntitlementServiceClient extends AbstractEntitlementServiceClient {

    private static final String ENTITLEMENT_SERVICE_NAME = "EntitlementService";
    private static final Log log = LogFactory.getLog(BasicAuthEntitlementServiceClient.class);
    private static final String DEFAULT_CLIENT_REPO =
            "repository" + File.separator + "deployment" + File.separator + "client";
    private static final String DEFAULT_AXIS2_XML =
            "repository" + File.separator + "conf" + File.separator + "axis2" + File.separator
                    + "axis2_blocking_client.xml";
    private static final int MAX_CONNECTIONS_PER_HOST = 200;
    private static final String XACML_DECISION_PERMIT = "Permit";
    public static final String URN_OASIS_NAMES_TC_XACML_1_0_SUBJECT_CATEGORY_ACCESS_SUBJECT = "urn:oasis:names:tc:xacml:1.0:subject-category:access-subject";
    public static final String URN_OASIS_NAMES_TC_XACML_3_0_ATTRIBUTE_CATEGORY_ACTION = "urn:oasis:names:tc:xacml:3.0:attribute-category:action";
    public static final String URN_OASIS_NAMES_TC_XACML_1_0_ACTION_ACTION_ID = "urn:oasis:names:tc:xacml:1.0:action:action-id";
    public static final String URN_OASIS_NAMES_TC_XACML_3_0_ATTRIBUTE_CATEGORY_RESOURCE = "urn:oasis:names:tc:xacml:3.0:attribute-category:resource";
    public static final String URN_OASIS_NAMES_TC_XACML_1_0_RESOURCE_RESOURCE_ID = "urn:oasis:names:tc:xacml:1.0:resource:resource-id";
    public static final String URN_OASIS_NAMES_TC_XACML_3_0_ATTRIBUTE_CATEGORY_ENVIRONMENT = "urn:oasis:names:tc:xacml:3.0:attribute-category:environment";
    public static final String URN_OASIS_NAMES_TC_XACML_1_0_ENVIRONMENT_ENVIRONMENT_ID = "urn:oasis:names:tc:xacml:1.0:environment:environment-id";

    private String serverUrl;
    private GenericObjectPool serviceStubPool;
    private HttpTransportProperties.Authenticator authenticator;
    private ConfigurationContext configurationContext;

    public BasicAuthEntitlementServiceClient(String serverUrl, String userName, String password) {
        this.serverUrl = serverUrl;
        authenticator = new HttpTransportProperties.Authenticator();
        authenticator.setUsername(userName);
        authenticator.setPassword(password);
        authenticator.setPreemptiveAuthentication(true);

        try {
            initConfigurationContext();
        } catch (AxisFault e) {
            log.error("Error initializing Axis2 configuration context", e);
        } catch (Exception e) {
            log.error("Error initializing default Axis2 configuration context", e);
        }
    }

    private void initConfigurationContext() throws Exception {
        HttpConnectionManager multiThreadedHttpConnectionManager = new MultiThreadedHttpConnectionManager();
        HttpClient httpClient = new HttpClient(multiThreadedHttpConnectionManager);

        File configFile = new File(DEFAULT_AXIS2_XML);

        if (!configFile.exists()) {
            configurationContext = ConfigurationContextFactory.createDefaultConfigurationContext();
            configurationContext.setProperty(HTTPConstants.DEFAULT_MAX_CONNECTIONS_PER_HOST, MAX_CONNECTIONS_PER_HOST);
        } else {
            configurationContext = ConfigurationContextFactory.
                    createConfigurationContextFromFileSystem(DEFAULT_CLIENT_REPO, DEFAULT_AXIS2_XML);
        }
        configurationContext.setProperty(HTTPConstants.CACHED_HTTP_CLIENT, httpClient);
        configurationContext.setProperty(HTTPConstants.REUSE_HTTP_CLIENT, Constants.VALUE_TRUE);

        Map<String, TransportOutDescription> transportsOut = configurationContext.getAxisConfiguration()
                .getTransportsOut();

        for (TransportOutDescription transportOutDescription : transportsOut.values()) {
            if (Constants.TRANSPORT_HTTP.equals(transportOutDescription.getName()) || Constants.TRANSPORT_HTTPS
                    .equals(transportOutDescription.getName())) {
                transportOutDescription.getSender().init(configurationContext, transportOutDescription);
            }
        }
    }

    @Override
    public String getDecision(Attribute[] attributes, String appId) throws Exception {
        EntitlementServiceStub stub = null;
        try {
            String xacmlRequest = XACMLRequetBuilder.buildXACML3Request(attributes);
            stub = getEntitlementStub(serverUrl);
            return getDecision(xacmlRequest, stub);
        } finally {
            if (stub != null) {
                stub._getServiceClient().cleanupTransport();
                serviceStubPool.returnObject(stub);
            }
        }
    }

    @Override
    public boolean subjectCanActOnResource(String subjectType, String alias, String actionId, String resourceId,
                                           String domainId, String appId) throws Exception {

        Attribute subjectAttribute = new Attribute(URN_OASIS_NAMES_TC_XACML_1_0_SUBJECT_CATEGORY_ACCESS_SUBJECT,
                                                   subjectType, ProxyConstants.DEFAULT_DATA_TYPE, alias);
        Attribute actionAttribute = new Attribute(URN_OASIS_NAMES_TC_XACML_3_0_ATTRIBUTE_CATEGORY_ACTION,
                                                  URN_OASIS_NAMES_TC_XACML_1_0_ACTION_ACTION_ID,
                                                  ProxyConstants.DEFAULT_DATA_TYPE, actionId);
        Attribute resourceAttribute = new Attribute(URN_OASIS_NAMES_TC_XACML_3_0_ATTRIBUTE_CATEGORY_RESOURCE,
                                                    URN_OASIS_NAMES_TC_XACML_1_0_RESOURCE_RESOURCE_ID,
                                                    ProxyConstants.DEFAULT_DATA_TYPE, resourceId);
        Attribute environmentAttribute = new Attribute(URN_OASIS_NAMES_TC_XACML_3_0_ATTRIBUTE_CATEGORY_ENVIRONMENT,
                                                       URN_OASIS_NAMES_TC_XACML_1_0_ENVIRONMENT_ENVIRONMENT_ID,
                                                       ProxyConstants.DEFAULT_DATA_TYPE, domainId);
        Attribute[] tempArr = { subjectAttribute, actionAttribute, resourceAttribute, environmentAttribute };
        String xacmlRequest = XACMLRequetBuilder.buildXACML3Request(tempArr);
        EntitlementServiceStub stub = null;
        try {
            stub = getEntitlementStub(serverUrl);
            String result = getDecision(xacmlRequest, stub);
            return result.contains(XACML_DECISION_PERMIT);
        } finally {
            if (stub != null) {
                stub._getServiceClient().cleanupTransport();
                serviceStubPool.returnObject(stub);
            }
        }
    }

    @Override
    public boolean subjectCanActOnResource(String subjectType, String alias, String actionId, String resourceId,
                                           Attribute[] attributes, String domainId, String appId) throws Exception {

        Attribute[] attrs = new Attribute[attributes.length + 4];
        attrs[0] = new Attribute(URN_OASIS_NAMES_TC_XACML_1_0_SUBJECT_CATEGORY_ACCESS_SUBJECT, subjectType,
                                 ProxyConstants.DEFAULT_DATA_TYPE, alias);
        for (int i = 0; i < attributes.length; i++) {
            attrs[i + 1] = new Attribute(URN_OASIS_NAMES_TC_XACML_1_0_SUBJECT_CATEGORY_ACCESS_SUBJECT,
                                         attributes[i].getType(), attributes[i].getId(), attributes[i].getValue());
        }
        attrs[attrs.length - 3] = new Attribute(URN_OASIS_NAMES_TC_XACML_3_0_ATTRIBUTE_CATEGORY_ACTION,
                                                URN_OASIS_NAMES_TC_XACML_1_0_ACTION_ACTION_ID,
                                                ProxyConstants.DEFAULT_DATA_TYPE, actionId);
        attrs[attrs.length - 2] = new Attribute(URN_OASIS_NAMES_TC_XACML_3_0_ATTRIBUTE_CATEGORY_RESOURCE,
                                                URN_OASIS_NAMES_TC_XACML_1_0_RESOURCE_RESOURCE_ID,
                                                ProxyConstants.DEFAULT_DATA_TYPE, resourceId);
        attrs[attrs.length - 1] = new Attribute(URN_OASIS_NAMES_TC_XACML_3_0_ATTRIBUTE_CATEGORY_ENVIRONMENT,
                                                URN_OASIS_NAMES_TC_XACML_1_0_ENVIRONMENT_ENVIRONMENT_ID,
                                                ProxyConstants.DEFAULT_DATA_TYPE, domainId);
        String xacmlRequest = XACMLRequetBuilder.buildXACML3Request(attrs);
        EntitlementServiceStub stub = null;
        try {
            stub = getEntitlementStub(serverUrl);
            String result = getDecision(xacmlRequest, stub);
            return result.contains(XACML_DECISION_PERMIT);
        } finally {
            if (stub != null) {
                stub._getServiceClient().cleanupTransport();
                serviceStubPool.returnObject(stub);
            }
        }
    }

    @Override
    public List<String> getResourcesForAlias(String alias, String appId) throws Exception {
        EntitlementServiceStub stub = null;
        try {
            stub = getEntitlementStub(serverUrl);
            return getResources(getEntitledAttributes(alias, null, ProxyConstants.SUBJECT_ID, null, false, stub));
        } finally {
            if (stub != null) {
                stub._getServiceClient().cleanupTransport();
                serviceStubPool.returnObject(stub);
            }
        }
    }

    @Override
    public List<String> getActionableResourcesForAlias(String alias, String appId) throws Exception {
        EntitlementServiceStub stub = null;
        try {
            stub = getEntitlementStub(serverUrl);
            return getResources(getEntitledAttributes(alias, null, ProxyConstants.SUBJECT_ID, null, true, stub));
        } finally {
            if (stub != null) {
                stub._getServiceClient().cleanupTransport();
                serviceStubPool.returnObject(stub);
            }
        }
    }

    @Override
    public List<String> getActionsForResource(String alias, String resource, String appId) throws Exception {
        EntitlementServiceStub stub = null;
        try {
            stub = getEntitlementStub(serverUrl);
            return getActions(getEntitledAttributes(alias, resource, ProxyConstants.SUBJECT_ID, null, false, stub));
        } finally {
            if (stub != null) {
                stub._getServiceClient().cleanupTransport();
                serviceStubPool.returnObject(stub);
            }
        }
    }

    @Override
    public List<String> getActionableChildResourcesForAlias(String alias, String parentResource, String action,
                                                            String appId) throws Exception {
        EntitlementServiceStub stub = null;
        try {
            stub = getEntitlementStub(serverUrl);
            return getResources(
                    getEntitledAttributes(alias, parentResource, ProxyConstants.SUBJECT_ID, action, true, stub));
        } finally {
            if (stub != null) {
                stub._getServiceClient().cleanupTransport();
                serviceStubPool.returnObject(stub);
            }
        }
    }

    private EntitlementServiceStub getEntitlementStub(String serverUrl) throws Exception {

        if (configurationContext == null) {
            throw new EntitlementProxyException(
                    "Cannot initialize EntitlementServiceStub with null Axis2 " + "configuration context.");
        }
        if (serviceStubPool == null) {
            serviceStubPool = new GenericObjectPool(
                    new EntitlementServiceStubFactory(configurationContext, serverUrl + ENTITLEMENT_SERVICE_NAME,
                                                      authenticator));
        }
        return (EntitlementServiceStub) serviceStubPool.borrowObject();
    }

    private String getDecision(String request, EntitlementServiceStub stub) throws Exception {
        return stub.getDecision(request);
    }

    private EntitledAttributesDTO[] getEntitledAttributes(String subjectName, String resourceName, String subjectId,
                                                          String action, boolean enableChildSearch,
                                                          EntitlementServiceStub stub) throws Exception {
        EntitledResultSetDTO results;
        results = stub.getEntitledAttributes(subjectName, resourceName, subjectId, action, enableChildSearch);
        return results.getEntitledAttributesDTOs();
    }

    private List<String> getResources(EntitledAttributesDTO[] entitledAttrs) {
        List<String> list = new ArrayList<>();
        if (entitledAttrs != null) {
            for (EntitledAttributesDTO dto : entitledAttrs) {
                list.add(dto.getResourceName());
            }
        }

        return list;
    }

    private List<String> getActions(EntitledAttributesDTO[] entitledAttrs) {
        List<String> list = new ArrayList<>();

        if (entitledAttrs != null) {
            for (EntitledAttributesDTO dto : entitledAttrs) {
                list.add(dto.getAction());
            }
        }
        return list;
    }

}
