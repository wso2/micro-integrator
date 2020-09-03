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
import org.wso2.mi.migration.migrate.mi.MIPasswordMigrationClient;

@Component(
        name = "org.wso2.mi.migration.client",
        immediate = true)
public class MIMigrationServiceComponent {

    private static final Log log = LogFactory.getLog(MIMigrationServiceComponent.class);

    @Activate
    protected void activate(ComponentContext context) {

        log.info("WSO2 MI migration bundle is activated");
        // current support is only for mi110 -> mi120
        if (System.getProperty("migrate.from.product.version").startsWith("mi110")) {
            migratePasswords();
        } else {
            log.error("Provided product version is invalid");
        }
        System.exit(0);
    }

    @Deactivate
    protected void deactivate(ComponentContext context) {
        log.info("WSO2 MI migration bundle is deactivated");
    }

    /**
     * Migrate passwords
     */
    private void migratePasswords() {
        log.info("Initiating WSO2 MI password migration");
        try {
            MIPasswordMigrationClient passwordMigrationClient = new MIPasswordMigrationClient();
            passwordMigrationClient.migratePasswords();
            log.info("Successfully completed password migration");
        } catch (Throwable e) {
            log.error("Error occurred during password migration", e);
        }
    }
}
