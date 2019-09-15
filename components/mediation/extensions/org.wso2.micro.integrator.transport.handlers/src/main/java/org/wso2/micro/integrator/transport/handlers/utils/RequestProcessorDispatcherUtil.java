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
package org.wso2.micro.integrator.transport.handlers.utils;

import org.apache.axis2.context.ConfigurationContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.config.SynapseConfigUtils;
import org.wso2.micro.core.Constants;

/**
 * This class check whether there is an API found for the given URI in both super tenant and tenant spaces
 */
public class RequestProcessorDispatcherUtil {
    private static final Log log = LogFactory.getLog(RequestProcessorDispatcherUtil.class);
    /**
     * Evaluates the given URI to find any matching API
     *
     * @param requestUri           URI of the request
     * @param configurationContext Configuration Context
     * @return True if API is found, false otherwise
     */
    public static boolean isDispatchToApiGetProcessor(String requestUri, ConfigurationContext configurationContext) {
        String apiName = requestUri.substring(1);
        //check the API in synapse configurations based on current tenant
        if (SynapseConfigUtils.getSynapseConfiguration(Constants.SUPER_TENANT_DOMAIN_NAME).getAPI(apiName) != null) {
            return true;
        }
        return false;
    }
}
