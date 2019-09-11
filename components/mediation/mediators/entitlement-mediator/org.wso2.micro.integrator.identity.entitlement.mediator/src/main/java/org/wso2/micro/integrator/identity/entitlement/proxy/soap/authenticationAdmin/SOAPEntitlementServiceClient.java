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

package org.wso2.micro.integrator.identity.entitlement.proxy.soap.authenticationAdmin;

import org.apache.axis2.AxisFault;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.client.Stub;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.apache.axis2.description.TransportOutDescription;
import org.apache.commons.lang.StringUtils;
import org.wso2.carbon.identity.entitlement.stub.EntitlementPolicyAdminServiceStub;
import org.wso2.carbon.identity.entitlement.stub.EntitlementServiceStub;
import org.wso2.carbon.identity.entitlement.stub.dto.EntitledAttributesDTO;
import org.wso2.carbon.identity.entitlement.stub.dto.EntitledResultSetDTO;
import org.wso2.micro.integrator.identity.entitlement.proxy.AbstractEntitlementServiceClient;
import org.wso2.micro.integrator.identity.entitlement.proxy.Attribute;
import org.wso2.micro.integrator.identity.entitlement.proxy.ProxyConstants;
import org.wso2.micro.integrator.identity.entitlement.proxy.XACMLRequetBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SOAPEntitlementServiceClient extends AbstractEntitlementServiceClient {

    public static final String ACCESS_SUBJECT = "urn:oasis:names:tc:xacml:1.0:subject-category:access-subject";
    public static final String ACTION = "urn:oasis:names:tc:xacml:3.0:attribute-category:action";
    public static final String ACTION_ID = "urn:oasis:names:tc:xacml:1.0:action:action-id";
    public static final String RESOURCE = "urn:oasis:names:tc:xacml:3.0:attribute-category:resource";
    public static final String RESOURCE_ID = "urn:oasis:names:tc:xacml:1.0:resource:resource-id";
    public static final String CATEGORY_ENVIRONMENT = "urn:oasis:names:tc:xacml:3.0:attribute-category:environment";
    public static final String ENVIRONMENT_ID = "urn:oasis:names:tc:xacml:1.0:environment:environment-id";
    private Map<String, EntitlementServiceStub> entitlementStub = new ConcurrentHashMap<String, EntitlementServiceStub>();
    private Map<String, EntitlementPolicyAdminServiceStub> policyAdminStub = new ConcurrentHashMap<String, EntitlementPolicyAdminServiceStub>();
    private Map<String, Authenticator> authenticators = new ConcurrentHashMap<String, Authenticator>();
    private String serverUrl;
    private String userName;
    private String password;
    private boolean reuseSession = true;
    private String authorizedCookie;

    public SOAPEntitlementServiceClient(String serverUrl, String username, String password, boolean reuseSession) {
        this.serverUrl = serverUrl;
        this.userName = username;
        this.password = password;
        this.reuseSession = reuseSession;
    }

    public SOAPEntitlementServiceClient(String serverUrl, String authorizedCookie, boolean reuseSession) {
        this.serverUrl = serverUrl;
        this.authorizedCookie = authorizedCookie;
        this.reuseSession = reuseSession;
    }

    @Override
    public String getDecision(Attribute[] attributes, String appId) throws Exception {
        String xacmlRequest = XACMLRequetBuilder.buildXACML3Request(attributes);
        EntitlementServiceStub stub = getEntitlementStub(serverUrl);
        Authenticator authenticator = getAuthenticator(serverUrl, userName, password, authorizedCookie);
        String result = getDecision(xacmlRequest, stub, authenticator);
        stub._getServiceClient().cleanupTransport();
        return result;
    }

    @Override
    public boolean subjectCanActOnResource(String subjectType, String alias, String actionId, String resourceId,
                                           String domainId, String appId) throws Exception {

        Attribute subjectAttribute = new Attribute(ACCESS_SUBJECT, subjectType, ProxyConstants.DEFAULT_DATA_TYPE,
                                                   alias);
        Attribute actionAttribute = new Attribute(ACTION, ACTION_ID, ProxyConstants.DEFAULT_DATA_TYPE, actionId);
        Attribute resourceAttribute = new Attribute(RESOURCE, RESOURCE_ID, ProxyConstants.DEFAULT_DATA_TYPE,
                                                    resourceId);
        Attribute environmentAttribute = new Attribute(CATEGORY_ENVIRONMENT, ENVIRONMENT_ID,
                                                       ProxyConstants.DEFAULT_DATA_TYPE, domainId);
        Attribute[] tempArr = { subjectAttribute, actionAttribute, resourceAttribute, environmentAttribute };
        String xacmlRequest = XACMLRequetBuilder.buildXACML3Request(tempArr);
        EntitlementServiceStub stub = getEntitlementStub(serverUrl);
        Authenticator authenticator = getAuthenticator(serverUrl, userName, password, authorizedCookie);
        String result = getDecision(xacmlRequest, stub, authenticator);
        stub._getServiceClient().cleanupTransport();
        return result.contains("Permit");
    }

    @Override
    public boolean subjectCanActOnResource(String subjectType, String alias, String actionId, String resourceId,
                                           Attribute[] attributes, String domainId, String appId) throws Exception {

        Attribute[] attrs = new Attribute[attributes.length + 4];
        attrs[0] = new Attribute(ACCESS_SUBJECT, subjectType, ProxyConstants.DEFAULT_DATA_TYPE, alias);
        for (int i = 0; i < attributes.length; i++) {
            attrs[i + 1] = new Attribute(ACCESS_SUBJECT, attributes[i].getType(), attributes[i].getId(),
                                         attributes[i].getValue());
        }
        attrs[attrs.length - 3] = new Attribute(ACTION, ACTION_ID, ProxyConstants.DEFAULT_DATA_TYPE, actionId);
        attrs[attrs.length - 2] = new Attribute(RESOURCE, RESOURCE_ID, ProxyConstants.DEFAULT_DATA_TYPE, resourceId);
        attrs[attrs.length - 1] = new Attribute(CATEGORY_ENVIRONMENT, ENVIRONMENT_ID, ProxyConstants.DEFAULT_DATA_TYPE,
                                                domainId);
        String xacmlRequest = XACMLRequetBuilder.buildXACML3Request(attrs);
        EntitlementServiceStub stub = getEntitlementStub(serverUrl);
        Authenticator authenticator = getAuthenticator(serverUrl, userName, password, authorizedCookie);
        String result = getDecision(xacmlRequest, stub, authenticator);
        stub._getServiceClient().cleanupTransport();
        return result.contains("Permit");
    }

    @Override
    public List<String> getResourcesForAlias(String alias, String appId) throws Exception {

        EntitlementServiceStub stub = getEntitlementStub(serverUrl);
        Authenticator authenticator = getAuthenticator(serverUrl, userName, password, authorizedCookie);
        List<String> results = getResources(
                getEntitledAttributes(alias, null, ProxyConstants.SUBJECT_ID, null, false, stub, authenticator));
        stub._getServiceClient().cleanupTransport();
        return results;
    }

    @Override
    public List<String> getActionableResourcesForAlias(String alias, String appId) throws Exception {

        EntitlementServiceStub stub = getEntitlementStub(serverUrl);
        Authenticator authenticator = getAuthenticator(serverUrl, userName, password, authorizedCookie);
        List<String> results = getResources(
                getEntitledAttributes(alias, null, ProxyConstants.SUBJECT_ID, null, true, stub, authenticator));
        stub._getServiceClient().cleanupTransport();
        return results;
    }

    @Override
    public List<String> getActionsForResource(String alias, String resource, String appId) throws Exception {

        EntitlementServiceStub stub = getEntitlementStub(serverUrl);
        Authenticator authenticator = getAuthenticator(serverUrl, userName, password, authorizedCookie);
        List<String> results = getActions(
                getEntitledAttributes(alias, resource, ProxyConstants.SUBJECT_ID, null, false, stub, authenticator));
        stub._getServiceClient().cleanupTransport();
        return results;
    }

    @Override
    public List<String> getActionableChildResourcesForAlias(String alias, String parentResource, String action,
                                                            String appId) throws Exception {

        EntitlementServiceStub stub = getEntitlementStub(serverUrl);
        Authenticator authenticator = getAuthenticator(serverUrl, userName, password, authorizedCookie);
        List<String> results = getResources(
                getEntitledAttributes(alias, parentResource, ProxyConstants.SUBJECT_ID, action, true, stub,
                                      authenticator));
        stub._getServiceClient().cleanupTransport();
        return results;
    }

    private Authenticator getAuthenticator(String serverUrl, String userName, String password, String authorizedCookie)
            throws Exception {
        if (reuseSession && authenticators.containsKey(serverUrl)) {
            return authenticators.get(serverUrl);

        }

        Authenticator authenticator = null;
        if (StringUtils.isNotEmpty(authorizedCookie)) {
            authenticator = new Authenticator(authorizedCookie, serverUrl + "AuthenticationAdmin");
        } else if (StringUtils.isNotEmpty(userName) && StringUtils.isNotEmpty(password)) {
            authenticator = new Authenticator(userName, password, serverUrl + "AuthenticationAdmin");
        }
        setAuthCookie(false, getEntitlementStub(serverUrl), authenticator);
        setAuthCookie(false, getEntitlementAdminStub(serverUrl), authenticator);
        authenticators.put(serverUrl, authenticator);
        return authenticator;
    }

    private EntitlementServiceStub getEntitlementStub(String serverUrl) throws Exception {

        if (entitlementStub.containsKey(serverUrl)) {
            return entitlementStub.get(serverUrl);
        }
        EntitlementServiceStub stub;
        ConfigurationContext configurationContext = ConfigurationContextFactory.createDefaultConfigurationContext();
        Map<String, TransportOutDescription> transportsOut = configurationContext.getAxisConfiguration()
                .getTransportsOut();
        for (TransportOutDescription transportOutDescription : transportsOut.values()) {
            transportOutDescription.getSender().init(configurationContext, transportOutDescription);
        }
        stub = new EntitlementServiceStub(configurationContext, serverUrl + "EntitlementService");
        entitlementStub.put(serverUrl, stub);
        return stub;
    }

    private EntitlementPolicyAdminServiceStub getEntitlementAdminStub(String serverUrl) throws Exception {

        if (policyAdminStub.containsKey(serverUrl)) {
            return policyAdminStub.get(serverUrl);
        }
        EntitlementPolicyAdminServiceStub stub;
        ConfigurationContext configurationContext = ConfigurationContextFactory.createDefaultConfigurationContext();
        Map<String, TransportOutDescription> transportsOut = configurationContext.getAxisConfiguration()
                .getTransportsOut();
        for (TransportOutDescription transportOutDescription : transportsOut.values()) {
            transportOutDescription.getSender().init(configurationContext, transportOutDescription);
        }
        stub = new EntitlementPolicyAdminServiceStub(configurationContext, serverUrl + "EntitlementPolicyAdminService");

        policyAdminStub.put(serverUrl, stub);
        return stub;
    }

    private String getDecision(String request, EntitlementServiceStub stub, Authenticator authenticator)
            throws Exception {
        try {
            return stub.getDecision(request);
        } catch (AxisFault e) {
            if (ProxyConstants.SESSION_TIME_OUT.equals(e.getFaultCode().getLocalPart())) {
                setAuthCookie(true, stub, authenticator);
                return stub.getDecision(request);
            } else {
                throw e;
            }
        }
    }

    private EntitledAttributesDTO[] getEntitledAttributes(String subjectName, String resourceName, String subjectId,
                                                          String action, boolean enableChildSearch,
                                                          EntitlementServiceStub stub, Authenticator authenticator)
            throws Exception {
        EntitledResultSetDTO results;
        try {
            results = stub.getEntitledAttributes(subjectName, resourceName, subjectId, action, enableChildSearch);
        } catch (AxisFault e) {
            if (ProxyConstants.SESSION_TIME_OUT.equals(e.getFaultCode().getLocalPart())) {
                setAuthCookie(true, stub, authenticator);
                results = stub.getEntitledAttributes(subjectName, resourceName, subjectId, action, enableChildSearch);
            } else {
                throw e;
            }
        }

        return results.getEntitledAttributesDTOs();
    }

    private List<String> getResources(EntitledAttributesDTO[] entitledAttrs) {
        List<String> list = new ArrayList<String>();

        if (entitledAttrs != null) {
            for (EntitledAttributesDTO dto : entitledAttrs) {
                list.add(dto.getResourceName());
            }
        }

        return list;
    }

    private List<String> getActions(EntitledAttributesDTO[] entitledAttrs) {
        List<String> list = new ArrayList<String>();

        if (entitledAttrs != null) {
            for (EntitledAttributesDTO dto : entitledAttrs) {
                list.add(dto.getAction());
            }
        }
        return list;
    }

    private void setAuthCookie(boolean isExpired, Stub stub, Authenticator authenticator) throws Exception {
        ServiceClient client = stub._getServiceClient();
        Options option = client.getOptions();
        option.setManageSession(true);
        option.setProperty(org.apache.axis2.transport.http.HTTPConstants.COOKIE_STRING,
                           authenticator.getCookie(isExpired));
    }

}
