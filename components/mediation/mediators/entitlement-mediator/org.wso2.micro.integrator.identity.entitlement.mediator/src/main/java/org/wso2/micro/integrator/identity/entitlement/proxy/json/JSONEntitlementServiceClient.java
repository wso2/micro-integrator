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

package org.wso2.micro.integrator.identity.entitlement.proxy.json;

import org.wso2.micro.integrator.identity.entitlement.proxy.AbstractEntitlementServiceClient;
import org.wso2.micro.integrator.identity.entitlement.proxy.Attribute;

import java.util.ArrayList;
import java.util.List;

public class JSONEntitlementServiceClient extends AbstractEntitlementServiceClient {

    @Override
    public String getDecision(Attribute[] attributes, String appId) throws Exception {
        return null;
    }

    @Override
    public boolean subjectCanActOnResource(String subjectType, String alias, String actionId, String resourceId,
                                           String domainId, String appId) throws Exception {
        return false;
    }

    @Override
    public boolean subjectCanActOnResource(String subjectType, String alias, String actionId, String resourceId,
                                           Attribute[] attributes, String domainId, String appId) throws Exception {
        return false;
    }

    @Override
    public List<String> getResourcesForAlias(String alias, String appId) throws Exception {
        return new ArrayList<>();
    }

    @Override
    public List<String> getActionableResourcesForAlias(String alias, String appId) throws Exception {
        return null;
    }

    @Override
    public List<String> getActionsForResource(String alias, String resources, String appId) throws Exception {
        return new ArrayList<>();
    }

    @Override
    public List<String> getActionableChildResourcesForAlias(String alias, String parentResource, String action,
                                                            String appId) throws Exception {
        return new ArrayList<>();
    }

}


