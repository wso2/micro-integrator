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
package org.wso2.micro.integrator.security.user.core.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleContext;
import org.wso2.micro.integrator.security.user.api.UserRealmService;
import org.wso2.micro.integrator.security.user.core.common.DefaultRealmService;
import org.wso2.micro.integrator.security.user.core.service.RealmService;
import org.wso2.micro.integrator.security.user.core.util.UserCoreUtil;

import java.lang.management.ManagementPermission;

/**
 * This is one of the first bundles that start in Carbon.
 * <p/>
 * ServerConfiguration object is not available to this bundle.
 * Therefore we read properties but do not keep a reference to it.
 */
public class Activator extends BundleCheckActivator {

    private static final Log log = LogFactory.getLog(Activator.class);

    public void startDeploy(BundleContext bundleContext) throws Exception {

        if (Boolean.parseBoolean(System.getProperty("NonUserCoreMode"))) {
            return;
        }

        // Need permissions in order to instantiate user core
        SecurityManager secMan = System.getSecurityManager();

        if (secMan != null) {
            secMan.checkPermission(new ManagementPermission("control"));
        }
        try {
            RealmService realmService = new DefaultRealmService(bundleContext);
            bundleContext.registerService(new String[]{RealmService.class.getName(), UserRealmService.class.getName()},
                    realmService, null);
            UserCoreUtil.setRealmService(realmService);
        } catch (Throwable e) {
            String msg = "Cannot start User Manager Core bundle";
            log.error(msg, e);
            // do not throw exceptions;
        }
    }

    public String getName() {
        return "UserCore";
    }

    public void stop(BundleContext bundleContext) throws Exception {

    }

}
