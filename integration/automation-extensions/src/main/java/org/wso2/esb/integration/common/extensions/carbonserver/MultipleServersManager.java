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

import org.wso2.carbon.automation.engine.exceptions.AutomationFrameworkException;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class MultipleServersManager {

    private Map<String, TestServerManager> servers = new HashMap<String, TestServerManager>();

    public MultipleServersManager() {
        // nothing to do
    }

    public void startServers(TestServerManager... serverManagers) throws AutomationFrameworkException {

        int noOfServers = serverManagers.length;
        for (int index = 0; index < noOfServers; ++index) {
            TestServerManager testServerManager = serverManagers[index];
            try {
                String carbonHome = testServerManager.startServer();
                servers.put(carbonHome, testServerManager);
            } catch (Exception ex) {
                throw new AutomationFrameworkException(ex);
            }
        }
    }

    public void stopAllServers() throws AutomationFrameworkException {

        Iterator iterator = servers.values().iterator();
        while (iterator.hasNext()) {
            TestServerManager serverUtils = (TestServerManager) iterator.next();
            serverUtils.stopServer();
        }
    }
}
