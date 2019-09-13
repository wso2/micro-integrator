/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.micro.integrator.initializer.handler;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.MessageContext;
import org.apache.synapse.carbonext.TenantInfoConfigurator;
import org.wso2.micro.core.Constants;

/**
 * This TenantInfoConfigurator is required to populate Synapse configurations in {@link org.apache.synapse.config.SynapseConfigUtils}
 *
 * Note: Even though Micro integrator does not support multi tenancy, it has super tenant to seamlessly work with other
 * components that it depends on
 */
public class MITenantInfoConfigurator implements TenantInfoConfigurator {
    private static final Log logger = LogFactory.getLog(MITenantInfoConfigurator.class);

    @Override
    public boolean extractTenantInfo(MessageContext messageContext) {
        if (logger.isDebugEnabled()) {
            logger.debug("Extracting Tenant Info...");
        }
        // Micro integrator does not support multi tenancy. It only have super tenant
        String tenantDomain = Constants.SUPER_TENANT_DOMAIN_NAME;
        int tenantId = Constants.SUPER_TENANT_ID;
        messageContext.setProperty("tenant.info.domain", tenantDomain);
        messageContext.setProperty("tenant.info.id", tenantId);
        if (logger.isDebugEnabled()) {
            logger.info("tenant domain: " + tenantDomain + ", tenant id: " + tenantId);
        }
        return true;
    }

    @Override
    public boolean applyTenantInfo(MessageContext messageContext) {
        if (logger.isDebugEnabled()) {
            logger.info("Applying Tenant Info...");
        }
        // Nothing to do here since Micro Integrator does not support multi tenancy
        return true;
    }
}
