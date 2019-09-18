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
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.BundleListener;

import java.util.ArrayList;
import java.util.List;

public abstract class BundleCheckActivator implements BundleActivator {

    private static final Log log = LogFactory.getLog(BundleCheckActivator.class);

    private String DEPLOY_BEFORE = "DeployBefore";

    public void start(final BundleContext bundleContext) throws Exception {

		/*
         * Check for the services that should be deployed before this if there
		 * are any.
		 * if there are such services we wait for such bundles to deploy and
		 * gets the notification
		 * through a service listener.
		 */

        // first check whether there are such services and they have already
        // deployed.
        String pendingBundleName = null;
        final List<Bundle> pendingBundles = new ArrayList<Bundle>();
        for (Bundle bundle : bundleContext.getBundles()) {
            pendingBundleName = (String) bundle.getHeaders().get(DEPLOY_BEFORE);
            if ((pendingBundleName != null) && (pendingBundleName.equals(getName())) &&
                    (bundle.getState() != Bundle.ACTIVE)) {
                // i.e this bundle should be started before the user manager but
                // yet has not started
                pendingBundles.add(bundle);
            }
        }

        if (pendingBundles.isEmpty()) {
            startDeploy(bundleContext);
        } else {
            BundleListener bundleListener = new BundleListener() {
                public void bundleChanged(BundleEvent bundleEvent) {
                    synchronized (pendingBundles) {
                        if (bundleEvent.getType() == BundleEvent.STARTED) {

                            pendingBundles.remove(bundleEvent.getBundle());
                            if (pendingBundles.isEmpty()) {
                                // now start the user manager deployment
                                bundleContext.removeBundleListener(this);
                                try {
                                    startDeploy(bundleContext);
                                } catch (Exception e) {
                                    log.error("Can not start the bundle ", e);
                                }
                            }
                        }
                    }
                }
            };
            bundleContext.addBundleListener(bundleListener);
        }
    }

    public abstract void startDeploy(BundleContext bundleContext) throws Exception;

    public abstract String getName();

}
