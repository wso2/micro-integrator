/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.micro.core.deployment;

import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.axis2.AxisFault;
import org.apache.axis2.deployment.RepositoryListener;
import org.apache.axis2.deployment.scheduler.SchedulerTask;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * This task takes care of deployment in WSO2 Carbon servers.
 * <p/>
 * It will do a deployment synchronization, followed by hot deployment
 */
public class CarbonDeploymentSchedulerTask extends SchedulerTask {

    /**
     * Indicates whether a Deployment repo update has to be performed
     */
    public static final String REPO_UPDATE_REQUIRED = "repo.update.required";
    private static final Integer REPO_UPDATE_MIN_TIME_SECONDS = 300;
    private static final Integer REPO_UPDATE_MAX_TIME_SECONDS = 900;
    private static final Integer DEPLOYMENT_INTERVAL = 15;

    private static final Log log = LogFactory.getLog(
            org.wso2.micro.core.deployment.CarbonDeploymentSchedulerTask.class);
    private AxisConfiguration axisConfig;

    public CarbonDeploymentSchedulerTask(RepositoryListener listener,
                                         AxisConfiguration axisConfig) {
        super(listener, axisConfig);
        this.axisConfig = axisConfig;

        try {
            axisConfig.addParameter(REPO_UPDATE_REQUIRED, new AtomicBoolean(false));
        } catch (AxisFault axisFault) {
            log.error("Cannot add repo.update.required parameter");
        }
    }

    public synchronized void runAxisDeployment() {
        super.run();
    }

    @Override
    public synchronized void run() {
        try {
            runAxisDeployment(); // artifact meta files which need to be committed may be generated during this super
            // .run() call

        } catch (Throwable t) {
            // we cannot let exceptions to be handled in the executor framework. It will kill the thread altogether
            log.error("Error while running deployment scheduler.. ", t);
        }
    }
}
