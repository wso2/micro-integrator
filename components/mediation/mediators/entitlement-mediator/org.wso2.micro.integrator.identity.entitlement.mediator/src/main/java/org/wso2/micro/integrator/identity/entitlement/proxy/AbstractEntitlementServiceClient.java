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

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.llom.util.AXIOMUtil;

import java.util.List;
import javax.xml.namespace.QName;

public abstract class AbstractEntitlementServiceClient {

    public abstract String getDecision(Attribute[] attributes, String appId) throws Exception;

    public abstract boolean subjectCanActOnResource(String subjectType, String alias, String actionId,
                                                    String resourceId, String domainId, String appId) throws Exception;

    public abstract boolean subjectCanActOnResource(String subjectType, String alias, String actionId,
                                                    String resourceId, Attribute[] attributes, String domainId,
                                                    String appId) throws Exception;

    public abstract List<String> getResourcesForAlias(String alias, String appId) throws Exception;

    public abstract List<String> getActionableResourcesForAlias(String alias, String appId) throws Exception;

    public abstract List<String> getActionableChildResourcesForAlias(String alias, String parentResource, String action,
                                                                     String appId) throws Exception;

    public abstract List<String> getActionsForResource(String alias, String resources, String appId) throws Exception;

    public OMElement[] getStatusOMElement(String xmlstring) throws Exception {
        OMElement response = null;
        OMElement result = null;
        OMElement[] decision = new OMElement[3];

        response = AXIOMUtil.stringToOM(xmlstring);
        result = response.getFirstChildWithName(new QName("Result"));
        if (result != null) {
            decision[0] = result.getFirstChildWithName(new QName("Decision"));
            decision[1] = result.getFirstChildWithName(new QName("Obligations"));
            decision[2] = result.getFirstChildWithName(new QName("AssociatedAdvice"));
        }
        return decision;
    }

    public String getStatus(String xmlstring) throws Exception {
        OMElement response = null;
        OMElement result = null;
        OMElement decision = null;

        response = AXIOMUtil.stringToOM(xmlstring);
        result = response.getFirstChildWithName(new QName("Result"));
        if (result != null) {
            decision = result.getFirstChildWithName(new QName("Decision"));
            if (decision != null) {
                return decision.getText();
            }
        }

        return "Invalid Status";
    }

}
