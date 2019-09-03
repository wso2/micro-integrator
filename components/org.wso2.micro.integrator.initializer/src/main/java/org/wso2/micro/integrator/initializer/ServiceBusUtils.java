/*
*  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.micro.integrator.initializer;

import org.apache.axis2.description.Parameter;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.ServerConfigurationInformation;
import org.apache.synapse.ServerContextInformation;
import org.apache.synapse.SynapseException;
import org.wso2.micro.integrator.initializer.persistence.MediationPersistenceManager;
import org.wso2.micro.integrator.core.util.MicroIntegratorBaseUtils;

import java.io.File;

/**
 * A utility class providing ESB specific utilities
 */
@SuppressWarnings({"UnusedDeclaration"})
public class ServiceBusUtils {

    private static final Log log = LogFactory.getLog(ServiceBusUtils.class);

    public static String getSynapseConfigAbsPath(ServerContextInformation contextInformation) {
        String carbonHome = MicroIntegratorBaseUtils.getCarbonHome();
        ServerConfigurationInformation configInfo = getSynapseServerConfigInfo(contextInformation);
        if (configInfo == null) {
            String msg = "Unable to obtain ESB server configuration information";
            log.warn(msg);
            throw new SynapseException(msg);
        }

        File synapseConfigFile = new File(configInfo.getSynapseXMLLocation());
        String synapseConfigPath;
        if (synapseConfigFile.isAbsolute()) {
            synapseConfigPath = synapseConfigFile.getAbsolutePath();
        } else {
            synapseConfigPath = new File(carbonHome.trim(),
                    configInfo.getSynapseXMLLocation()).getAbsolutePath();
        }
        return synapseConfigPath;
    }

    public static ServerConfigurationInformation getSynapseServerConfigInfo(
            ServerContextInformation contextInformation) {
        if (contextInformation != null) {
            return contextInformation.getServerConfigurationInformation();
        } else {
            // If the Synapse ServerManager is not initialized check whether we
            // can get the info from the ServiceBusInitializer
            return ServiceBusInitializer.getServerConfigurationInformation();
        }
    }

    public static MediationPersistenceManager getMediationPersistenceManager(
            AxisConfiguration axisCfg) {
        
        Parameter p = axisCfg.getParameter(
                ServiceBusConstants.PERSISTENCE_MANAGER);
        if (p != null) {
            return (MediationPersistenceManager) p.getValue();
        }

        return null;
    }          

    public static String generateFileName(String name) {
        return name.replaceAll("[\\/?*|:<> ]", "_") + ".xml";
    }
}
