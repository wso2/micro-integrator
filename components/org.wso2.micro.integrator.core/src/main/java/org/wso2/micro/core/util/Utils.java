/*
 * Copyright 2005-2007 WSO2, Inc. (http://wso2.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wso2.micro.core.util;

import org.apache.axis2.deployment.Deployer;
import org.apache.axis2.deployment.DeploymentEngine;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class Utils {

    private static Log log = LogFactory.getLog(Utils.class);

    public static boolean addCAppDeployer(AxisConfiguration axisConfiguration) {
        boolean successfullyAdded = false;
        try {
            String appsRepo = "carbonapps";
            // Initialize CApp deployer here
            Class deployerClass = Class.
                    forName("org.wso2.carbon.application.deployer.CappAxis2Deployer");

            Deployer deployer = (Deployer) deployerClass.newInstance();
            deployer.setDirectory(appsRepo);
            deployer.setExtension("car");

            //Add the deployer to deployment engine
            //We need to synchronize on the axisConfig object here to avoid issues such as CARBON-14471
            synchronized (axisConfiguration) {
                DeploymentEngine deploymentEngine =
                        (DeploymentEngine) axisConfiguration.getConfigurator();
                deploymentEngine.addDeployer(deployer, appsRepo, "car");
            }
            successfullyAdded = true;
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error while adding CAppDeploymentManager to axis configuration", e);
        }
        return successfullyAdded;
    }

}
