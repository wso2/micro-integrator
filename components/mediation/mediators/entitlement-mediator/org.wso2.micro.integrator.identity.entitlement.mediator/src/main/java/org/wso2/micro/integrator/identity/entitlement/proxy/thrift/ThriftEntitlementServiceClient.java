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

package org.wso2.micro.integrator.identity.entitlement.proxy.thrift;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.thrift.TException;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TSSLTransportFactory;
import org.apache.thrift.transport.TTransport;
import org.wso2.micro.integrator.identity.entitlement.proxy.AbstractEntitlementServiceClient;
import org.wso2.micro.integrator.identity.entitlement.proxy.Attribute;
import org.wso2.micro.integrator.identity.entitlement.proxy.ProxyConstants;
import org.wso2.micro.integrator.identity.entitlement.proxy.XACMLRequetBuilder;
import org.wso2.micro.integrator.identity.entitlement.proxy.exception.EntitlementProxyException;
import org.wso2.micro.integrator.identity.entitlement.proxy.generatedCode.EntitlementException;
import org.wso2.micro.integrator.identity.entitlement.proxy.generatedCode.EntitlementThriftClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ThriftEntitlementServiceClient extends AbstractEntitlementServiceClient {

    private static final Log log = LogFactory.getLog(ThriftEntitlementServiceClient.class);
    public static final String URN_OASIS_NAMES_TC_XACML_1_0_SUBJECT_CATEGORY_ACCESS_SUBJECT = "urn:oasis:names:tc:xacml:1.0:subject-category:access-subject";
    public static final String URN_OASIS_NAMES_TC_XACML_3_0_ATTRIBUTE_CATEGORY_ACTION = "urn:oasis:names:tc:xacml:3.0:attribute-category:action";
    public static final String URN_OASIS_NAMES_TC_XACML_1_0_ACTION_ACTION_ID = "urn:oasis:names:tc:xacml:1.0:action:action-id";
    public static final String URN_OASIS_NAMES_TC_XACML_3_0_ATTRIBUTE_CATEGORY_RESOURCE = "urn:oasis:names:tc:xacml:3.0:attribute-category:resource";
    public static final String URN_OASIS_NAMES_TC_XACML_1_0_RESOURCE_RESOURCE_ID = "urn:oasis:names:tc:xacml:1.0:resource:resource-id";
    public static final String URN_OASIS_NAMES_TC_XACML_3_0_ATTRIBUTE_CATEGORY_ENVIRONMENT = "urn:oasis:names:tc:xacml:3.0:attribute-category:environment";
    public static final String URN_OASIS_NAMES_TC_XACML_1_0_ENVIRONMENT_ENVIRONMENT_ID = "urn:oasis:names:tc:xacml:1.0:environment:environment-id";

    private String trustStore = System.getProperty(ProxyConstants.TRUST_STORE);
    private String trustStorePass = System.getProperty(ProxyConstants.TRUST_STORE_PASSWORD);
    private String serverUrl;
    private String userName;
    private String password;
    private String thriftHost;
    private int thriftPort;
    private boolean reuseSession = true;

    private Map<String, Authenticator> authenticators = new ConcurrentHashMap<>();

    public ThriftEntitlementServiceClient(String serverUrl, String username, String password, String thriftHost,
                                          int thriftPort, boolean reuseSession) {
        this.serverUrl = serverUrl;
        this.userName = username;
        this.password = password;
        this.thriftHost = thriftHost;
        this.thriftPort = thriftPort;
        this.reuseSession = reuseSession;
    }

    @Override
    public String getDecision(Attribute[] attributes, String appId) throws Exception {
        String xacmlRequest = XACMLRequetBuilder.buildXACML3Request(attributes);
        EntitlementThriftClient.Client client = getThriftClient();
        Authenticator authenticator = getAuthenticator(serverUrl, userName, password);
        return getDecision(xacmlRequest, client, authenticator);
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
        EntitlementThriftClient.Client client = getThriftClient();
        Authenticator authenticator = getAuthenticator(serverUrl, userName, password);
        String decision = getDecision(xacmlRequest, client, authenticator);
        if (decision != null) {
            return decision.contains("Permit");
        } else {
            return false;
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
        EntitlementThriftClient.Client client = getThriftClient();
        Authenticator authenticator = getAuthenticator(serverUrl, userName, password);
        String decision = getDecision(xacmlRequest, client, authenticator);
        if (decision != null) {
            return decision.contains("Permit");
        } else {
            return false;
        }
    }

    @Override
    public List<String> getResourcesForAlias(String alias, String appId) {
        return new ArrayList<>();
    }

    @Override
    public List<String> getActionableResourcesForAlias(String alias, String appId) {
        return new ArrayList<>();
    }

    @Override
    public List<String> getActionsForResource(String alias, String resources, String appId) {
        return new ArrayList<>();
    }

    @Override
    public List<String> getActionableChildResourcesForAlias(String alias, String parentResource, String action,
                                                            String appId) throws Exception {
        return new ArrayList<>();
    }

    private String getDecision(String xacmlRequest, EntitlementThriftClient.Client client, Authenticator authenticator)
            throws EntitlementProxyException {
        try {
            return client.getDecision(xacmlRequest, authenticator.getSessionId(false));
        } catch (TException e) {
            if (log.isDebugEnabled()) {
                log.debug("Thrift entitlement exception  : ", e);
            }
            throw new EntitlementProxyException(
                    "Error while getting decision from PDP using " + "ThriftEntitlementServiceClient", e);
        } catch (EntitlementException e) {
            if (log.isDebugEnabled()) {
                log.debug("Exception occurred : ", e);
            }
            try {
                return client.getDecision(xacmlRequest, authenticator.getSessionId(true));
            } catch (TException | EntitlementException e1) {
                throw new EntitlementProxyException(
                        "Error occurred while getting the decision from PDP using " + "ThriftEntitlementServiceClient",
                        e1);
            } catch (EntitlementProxyException e1) {
                throw new EntitlementProxyException("Error occurred while re-authenticating the thrift client", e1);
            }
        }
    }

    private Authenticator getAuthenticator(String serverUrl, String userName, String password) throws Exception {
        if (reuseSession && authenticators.containsKey(serverUrl)) {
            return authenticators.get(serverUrl);
        }

        Authenticator authenticator = new Authenticator(userName, password, serverUrl + "thriftAuthenticator");
        authenticators.put(serverUrl, authenticator);
        return authenticator;
    }

    private EntitlementThriftClient.Client getThriftClient() throws Exception {

        TSSLTransportFactory.TSSLTransportParameters param = new TSSLTransportFactory.TSSLTransportParameters();
        param.setTrustStore(trustStore, trustStorePass);
        TTransport transport;
        transport = TSSLTransportFactory.getClientSocket(thriftHost, thriftPort, ProxyConstants.THRIFT_TIME_OUT, param);
        TProtocol protocol = new TBinaryProtocol(transport);
        return new EntitlementThriftClient.Client(protocol);
    }
}
