/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.micro.integrator.dataservices.core.internal;

import org.apache.axis2.context.ConfigurationContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.micro.core.util.AbstractAxis2ConfigurationContextObserver;

public class DSAxis2ConfigurationContextObserver extends AbstractAxis2ConfigurationContextObserver {

    private static final Log log = LogFactory.getLog(
            DSAxis2ConfigurationContextObserver.class);

    public void createdConfigurationContext(ConfigurationContext configurationContext) {

//		int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
//        try {
//        	PrivilegedCarbonContext.startTenantFlow();
//        	PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantId(tenantId);

//            AxisConfiguration axisConfigOfCurrentTenant = configurationContext.getAxisConfiguration();
//            DeploymentEngine axisDeploymentEngine =
//                    (DeploymentEngine)axisConfigOfCurrentTenant.getConfigurator();
//            
//            DBDeployer dbDeployer = new DBDeployer();
//            dbDeployer.setDirectory(DBConstants.DB_SERVICE_REPO_VALUE);
//            dbDeployer.setExtension(DBConstants.DB_SERVICE_EXTENSION_VALUE);
//            axisDeploymentEngine.addDeployer(dbDeployer, dbDeployer.getRepoDir(),
//                    dbDeployer.getExtension());
//        } catch (Exception e) {
//            log.error("Error in setting tenant details ", e);
//        } finally {
//        	PrivilegedCarbonContext.endTenantFlow();
//        }
    }
    
}
