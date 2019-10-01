/*                                                                             
 * Copyright 2004,2005 The Apache Software Foundation.                         
 *                                                                             
 * Licensed under the Apache License, Version 2.0 (the "License");             
 * you may not use this file except in compliance with the License.            
 * You may obtain a copy of the License at                                     
 *                                                                             
 *      http://www.apache.org/licenses/LICENSE-2.0                             
 *                                                                             
 * Unless required by applicable law or agreed to in writing, software         
 * distributed under the License is distributed on an "AS IS" BASIS,           
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.    
 * See the License for the specific language governing permissions and         
 * limitations under the License.                                              
 */
package org.wso2.micro.core;

import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.deployment.DeploymentEngine;
import org.apache.axis2.description.TransportInDescription;
import org.apache.axis2.transport.TransportListener;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleContext;
import org.osgi.util.tracker.ServiceTracker;
import org.wso2.micro.integrator.core.internal.CarbonCoreDataHolder;
import org.wso2.micro.integrator.core.util.MicroIntegratorBaseUtils;

import java.lang.management.ManagementPermission;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.management.Query;
import javax.management.QueryExp;

/**
 * Class for handling Server management functionalilty.
 * <p/>
 * At the moment, handles only maintenance mode operations
 */
public class ServerManagement {

    private static final Log log = LogFactory.getLog(ServerManagement.class);
    private static final long TIMEOUT = 60 * 1000;
    private Map<String, TransportInDescription> inTransports;
    private ConfigurationContext serverConfigContext;
    private static CarbonCoreDataHolder dataHolder = CarbonCoreDataHolder.getInstance();

    public ServerManagement(Map<String, TransportInDescription> inTransports,
                            ConfigurationContext serverConfigContext) {
        this.inTransports = inTransports;
        this.serverConfigContext = serverConfigContext;
    }

    /**
     * Method to switch a node to maintenance mode.
     * <p/>
     * Here is the sequence of events:
     * <p/>
     * <ol>
     * <li>Client calls this method</li>
     * <li>The server stops accepting new requests/connections, but continues to stay alive so
     * that old requests & connections can be served</li>
     * <li>Once all requests have been processed, the method returns</li
     * </ol>
     * @throws Exception - on errors while starting maintenance
     */
    public void startMaintenance() throws Exception {
        SecurityManager secMan = System.getSecurityManager();
        if (secMan != null) {
            secMan.checkPermission(new ManagementPermission("control"));
        }
        log.info("Starting to switch to maintenance mode...");
        for (TransportInDescription tinDesc : inTransports.values()) {
            TransportListener transport = tinDesc.getReceiver();
            transport.stop();
        }
        log.info("Stopped all transport listeners");

        waitForRequestCompletion();
    }

    /**
     * Method the switch to Maintenance mode when the server is shutting down. In addition to the
     * normal maintenance mode, here we have to wait until all deployment threads are executed.
     *
     * @throws Exception - on errors while starting maintenance
     */
    public void startMaintenanceForShutDown() throws Exception {
        startMaintenance();
        waitForDeploymentThreadCompletion();
        //cleanCAppWorkDir();
        waitForServerTaskCompletion();
    }

    private void waitForServerTaskCompletion() {
        MicroIntegratorBaseUtils.checkSecurity();
        log.info("Waiting for server task completion...");
        BundleContext bundleContext = dataHolder.getBundleContext();
        if (bundleContext != null) {
            @SuppressWarnings("unchecked")
            ServiceTracker tracker = new ServiceTracker(bundleContext,
                                                        WaitBeforeShutdownObserver.class.getName(),
                                                        null);
            tracker.open();
            Object[] services = tracker.getServices();
            if (services != null) {
                for (Object service : services) {
                    ((WaitBeforeShutdownObserver) service).startingShutdown();

                }
                boolean allTasksComplete;
                do {
                    // Assume all tasks are completed by now.
                    allTasksComplete = true;
                    for (Object service : services) {
                        allTasksComplete &= ((WaitBeforeShutdownObserver) service).isTaskComplete();
                    }
                    // check again if at least one task is not complete.
                } while (!allTasksComplete);
            }
            tracker.close();
        }
        log.info("All server tasks have been completed.");
    }

    /**
     * Wait until all deployment tasks have completed
     */
    private void waitForDeploymentThreadCompletion() {
        MicroIntegratorBaseUtils.checkSecurity();
        log.info("Waiting for deployment completion...");

        // Stop all deployment tasks by calling cleanup on the super-tenant & tenant configurators
        //invoking  CarbonDeploymentSchedulerExtenders for all tenants before shutdown.
        //(this is done for the purpose of persisting all remaining stat data.)


        serverConfigContext.getAxisConfiguration().getConfigurator().cleanup();
        // uncomment following to execute registered deployment  scheduler extenders for super
        // tenant in maintenance mode.
        /*DeploymentUtils.invokeCarbonDeploymentSchedulerExtenders(
                serverConfigContext.getAxisConfiguration());*/

        boolean isDeploymentSchedulerRunning;
        log.info("All deployment tasks have been completed.");
    }

    private boolean isDeploymentSchedulerRunning(ConfigurationContext configurationContext) {
        DeploymentEngine deploymentEngine =
                (DeploymentEngine) configurationContext.getAxisConfiguration().getConfigurator();
        return deploymentEngine.isDeploymentTaskRunning();
    }

    /**
     * Wait till all service requests have been serviced. This method will only wait for a maximum
     * of {@link ServerManagement#TIMEOUT}
     *
     * @throws Exception If an error occurs while trying to connect to the Tomcat MBean
     */
    public void waitForRequestCompletion() throws Exception {
        SecurityManager secMan = System.getSecurityManager();
        if (secMan != null) {
            secMan.checkPermission(new ManagementPermission("control"));
        }
        log.info("Waiting for request service completion...");
        /**
         * Get all MBeans with names such as Catalina:type=RequestProcessor,worker=http-9762,name=HttpRequest<n>
         * & Catalina:type=RequestProcessor,worker=http-9762,name=HttpsRequest<n>
         */
        MBeanServer mbs = MbeanManagementFactory.getMBeanServer();
        boolean areRequestsInService;
        long start = System.currentTimeMillis();
        do {
            // Check whether there are any processors which are currently in the SERVICE stage (3)
            QueryExp query = Query.eq(Query.attr("stage"), Query.value(3));  // 3 = org.apache.coyote.Constants.STAGE_SERVICE
            Set set = mbs.queryNames(new ObjectName("Catalina:type=RequestProcessor,*"), query);
            if (set.size() > 0) {
                areRequestsInService = true;
                if (System.currentTimeMillis() - start > TIMEOUT) {
                    log.warn("Timeout occurred even though there are active connections.");
                    break;
                }
                Thread.sleep(2000);
            } else {
                areRequestsInService = false;
            }
        } while (areRequestsInService);
        log.info("All requests have been served.");
    }

    /**
     * Method to change the state of a node from "maintenance" to "normal"
     *
     * @throws Exception If an error occurs while trying to connect to the Tomcat MBean
     */
    public void endMaintenance() throws Exception {
        SecurityManager secMan = System.getSecurityManager();
        if (secMan != null) {
            secMan.checkPermission(new ManagementPermission("control"));
        }
        log.info("Switching to normal mode...");
        for (Iterator iter = inTransports.values().iterator(); iter.hasNext();) {
            TransportInDescription tinDesc = (TransportInDescription) iter.next();
            TransportListener transport = tinDesc.getReceiver();
            transport.start();
        }
        log.info("Switched to normal mode");
    }
}
