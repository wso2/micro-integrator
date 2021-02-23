/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.mi.migration.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.mi.migration.migrate.ei.EIPasswordMigrationClient;

/**
 * This class implements the secret migration service component for EI and ESB.
 */

@Component(
        name = "org.wso2.ei.migration.client",
        immediate = true)
public class EIMigrationServiceComponent {

    private static final Log log = LogFactory.getLog(EIMigrationServiceComponent.class);

    @Activate
    protected void activate(ComponentContext context) {

        log.info("WSO2 EI to MI migration bundle is activated");
        String product = System.getProperty("migrate.from.product.version");
        // Supports both EI and ESB. If changes need to be done with specific versions,
        // include an inner if block as needed.
        if (product.contains("esb")) {
            migrateSecrets(true);
        } else if (product.contains("ei")){
           migrateSecrets(false);
        } else {
            log.error("Provided product version is invalid");
        }
        System.exit(0);
    }

    @Deactivate
    protected void deactivate(ComponentContext context) {
        log.info("WSO2 EI to MI migration bundle is deactivated");
    }

    @Reference(
            name = "registry.service",
            service = org.wso2.carbon.registry.core.service.RegistryService.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetRegistryService")
    protected void setRegistryService(RegistryService registryService) {
        EIRegistryDataHolder.setRegistryService(registryService);
    }

    protected void unsetRegistryService(RegistryService registryService) {
        EIRegistryDataHolder.setRegistryService(null);
    }

    /**
     * Migrate passwords
     */
    private void migrateSecrets(Boolean isESB) {
        log.info("Initiating WSO2 EI password migration");
        try {
            EIPasswordMigrationClient eiMigrationClient = new EIPasswordMigrationClient();
            eiMigrationClient.migratePasswords(isESB);
            log.info("Successfully completed password migration");
        } catch (Throwable e) {
            log.error("Error while migrating secure vault passwords", e);
        }
    }
}
