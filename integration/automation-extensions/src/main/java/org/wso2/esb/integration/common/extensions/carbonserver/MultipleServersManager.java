/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.esb.integration.common.extensions.carbonserver;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.automation.engine.exceptions.AutomationFrameworkException;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class MultipleServersManager {

    private static final Log log = LogFactory.getLog(MultipleServersManager.class);
    private Map<String, TestServerManager> servers = new HashMap<String, TestServerManager>();
    private List<String> serverHomes = new ArrayList<>();
    private String deploymentDirectory = null;
    private String registryDirectory = null;

    public MultipleServersManager() {
        // nothing to do
    }

    public void startServers(TestServerManager... serverManagers) throws AutomationFrameworkException {

        int noOfServers = serverManagers.length;
        for (int index = 0; index < noOfServers; ++index) {
            log.info("============================== Configuring server " + (servers.size() + 1)
                             + " ==============================");
            TestServerManager testServerManager = serverManagers[index];
            try {
                String carbonHome = testServerManager.startServer();
                servers.put(carbonHome, testServerManager);
                serverHomes.add(carbonHome);
            } catch (Exception ex) {
                throw new AutomationFrameworkException(ex);
            }
        }
    }

    public void startServersWithDepSync(boolean mountRegistry, TestServerManager... serverManagers)
            throws AutomationFrameworkException {

        int noOfServers = serverManagers.length;
        for (int index = 0; index < noOfServers; ++index) {
            log.info("============================== Configuring server " + (servers.size() + 1)
                             + " ==============================");
            TestServerManager testServerManager = serverManagers[index];
            try {
                String carbonHome;
                if (deploymentDirectory == null) {
                    carbonHome = testServerManager.startServer();
                    deploymentDirectory = String.join(File.separator, carbonHome, "repository", "deployment");
                    if (mountRegistry) {
                        registryDirectory = String.join(File.separator, carbonHome, "registry");
                    }
                } else {
                    carbonHome = testServerManager.startServer(deploymentDirectory, registryDirectory);
                }
                servers.put(carbonHome, testServerManager);
            } catch (Exception ex) {
                throw new AutomationFrameworkException(ex);
            }
        }
    }

    public String getDeploymentDirectory() {
        return deploymentDirectory;
    }

    public List<String> getServerHomes() {
        return serverHomes;
    }

    public void stopAllServers() throws AutomationFrameworkException {

        Iterator iterator = servers.values().iterator();
        while (iterator.hasNext()) {
            TestServerManager serverUtils = (TestServerManager) iterator.next();
            serverUtils.stopServer();
        }
    }

}
