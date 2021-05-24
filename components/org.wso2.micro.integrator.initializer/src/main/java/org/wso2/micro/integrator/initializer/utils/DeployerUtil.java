/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.micro.integrator.initializer.utils;

import org.apache.axiom.om.OMAttribute;
import org.apache.axiom.om.OMElement;
import org.apache.synapse.api.API;
import org.apache.synapse.api.version.VersionStrategy;
import org.apache.synapse.config.xml.rest.VersionStrategyFactory;

import javax.xml.namespace.QName;

public class DeployerUtil {

    /**
     * Partially build a synapse API for deployment purposes.
     * @param apiElement OMElement of API configuration.
     * @return API
     */
    public static API partiallyBuildAPI(OMElement apiElement) {
        OMAttribute nameAtt = apiElement.getAttribute(new QName("name"));
        OMAttribute contextAtt = apiElement.getAttribute(new QName("context"));
        API api = new API(nameAtt.getAttributeValue(), contextAtt.getAttributeValue());
        VersionStrategy vStrategy = VersionStrategyFactory.createVersioningStrategy(api, apiElement);
        api.setVersionStrategy(vStrategy);
        return api;
    }
}
