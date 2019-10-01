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
package org.wso2.micro.integrator.dataservices.core;

import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.deployment.repository.util.DeploymentFileData;
import org.apache.axis2.deployment.util.Utils;
import org.apache.axis2.description.AxisService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.micro.core.Constants;
import org.wso2.micro.integrator.dataservices.common.DBConstants;

import java.io.File;

/**
 * This class represents a runnable class which re-deployes a data service after a fixed internal,
 * this is used for data services that were unable to deploy earlier but will be deployable later.
 */
public class FaultyServiceRectifier implements Runnable {

	private static final Log log = LogFactory.getLog(FaultyServiceRectifier.class);

	private ConfigurationContext configurationCtx;

	private DeploymentFileData deploymentFileData;
	
	private int tenantId;

	public FaultyServiceRectifier(AxisService service,
			DeploymentFileData deploymentData, ConfigurationContext configCtx) {
		this.deploymentFileData = deploymentData;
		this.configurationCtx = configCtx;
		try {
		    this.tenantId = Constants.SUPER_TENANT_ID; // PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
		} catch (Throwable e) {
			/* this is done in the case of running unit tests, the above code fails */
			this.tenantId = -1;
		}
	}
	
	/**
	 * Retrieve the service name from the deployment path.
	 * @param deploymentFilePath Deployment file path
	 * @return The service name
	 */
	public static String getServiceNameFromPath(String repoDir, File deploymentFile) {
		String heirarchy = Utils.getServiceHierarchy(deploymentFile.getAbsolutePath(), repoDir);
		String name = deploymentFile.getName();
		int index = name.lastIndexOf("." + DBConstants.DBS_FILE_EXTENSION);
		if (index == -1) {
			return null;
		}
		name = name.substring(0, index);
		return heirarchy + name;
	}

	public void run() {
		String deploymentFilePath = null;
		try {
//			PrivilegedCarbonContext.startTenantFlow();
//			PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantId(this.tenantId, true);
			deploymentFilePath = deploymentFileData.getFile().getAbsolutePath();
	        /*
	            Security Comment :
	            This path is trustworthy, path is configured in the dbs file.
	         */
			File file = new File(deploymentFilePath);
			if (file.isFile()) {
				if (log.isDebugEnabled()) {
					log.debug(
							"Trying to re-establish faulty database connection for data services :"
									+ deploymentFilePath);
				}

				DBDeployer dbDeployer = (DBDeployer) this.configurationCtx.getProperty(DBConstants.DB_SERVICE_DEPLOYER);

				/* configurationCtx can be null when the tenant unload.
                               Therefore this task terminates */
                               if (configurationCtx.getAxisConfiguration() == null) {
                                       return;
                               }

				/* check if the service is already deployed */
				if (configurationCtx.getAxisConfiguration().getService(
						getServiceNameFromPath(dbDeployer.getRepoDir(), file)) != null) {
					return;
				}
				
//				configurationCtx.getAxisConfiguration().getFaultyServices().remove(
//						deploymentFileName);
				this.configurationCtx.getAxisConfiguration().removeFaultyService(deploymentFilePath);
				/* send the dataservice configuration through re-deployment */				
				dbDeployer.deploy(deploymentFileData);
			} else {
				if (log.isDebugEnabled()) {
					log.debug("Deployment file " + deploymentFilePath
							+ " does not exist.Removing timer task");
				}
			}
		} catch (Exception e) {
			log.error("Error in faulty service rectifier", e);
		}
//		finally {
//			PrivilegedCarbonContext.endTenantFlow();
//		}
	}

}
