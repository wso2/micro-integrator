/*
Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.mi.registry.migration;

import org.apache.axis2.AxisFault;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.wso2.carbon.registry.resource.stub.ResourceAdminServiceExceptionException;
import org.wso2.carbon.registry.resource.stub.ResourceAdminServiceStub;
import org.wso2.carbon.registry.resource.stub.beans.xsd.MetadataBean;
import org.wso2.carbon.registry.resource.stub.beans.xsd.ResourceTreeEntryBean;
import org.wso2.mi.registry.migration.exception.RegistryMigrationException;
import org.wso2.mi.registry.migration.utils.MigrationClientUtils;

import java.rmi.RemoteException;

public class ResourceAdminServiceClient {

    private static final Logger LOGGER = LogManager.getLogger(ResourceAdminServiceClient.class);
    private ResourceAdminServiceStub resourceAdminServiceStub;

    ResourceAdminServiceClient(String backEndUrl, String sessionCookie) throws RegistryMigrationException {
        String endPoint = backEndUrl + "/services/ResourceAdminService";
        try {
            resourceAdminServiceStub = new ResourceAdminServiceStub(endPoint);
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Successfully initialized the ResourceAdminServiceStub");
            }
        } catch (AxisFault e) {
            throw new RegistryMigrationException("Error occurred while initializing the ResourceAdminServiceStub. ", e);
        }
        //Authenticate the stub from sessionCooke
        ServiceClient serviceClient;
        Options option;

        serviceClient = resourceAdminServiceStub._getServiceClient();
        option = serviceClient.getOptions();
        option.setManageSession(true);
        option.setTimeOutInMilliSeconds(MigrationClientUtils.SESSION_TIMEOUT_IN_MILLIS);
        option.setProperty(org.apache.axis2.transport.http.HTTPConstants.COOKIE_STRING, sessionCookie);

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(
                    "Successfully authenticated the ResourceAdminServiceStub from the sessionCookie: {}",
                    sessionCookie);
        }
    }

    /**
     * Get text content of the given registry resource.
     *
     * @param path path of the registry resource
     * @return text content of the given registry resource
     * @throws RegistryMigrationException if something goes wrong in the admin service call
     */
    String getTextContent(String path) throws RegistryMigrationException {
        try {
            return resourceAdminServiceStub.getTextContent(path);
        } catch (RemoteException | ResourceAdminServiceExceptionException e) {
            throw new RegistryMigrationException("Error when getting the text content from " + path, e);
        }
    }

    /**
     * Get esource tree entry bean of the given registry resource.
     *
     * @param resourcePath path of the registry resource
     * @return Resource tree entry bean
     * @throws RegistryMigrationException if something goes wrong in the admin service call
     */
    ResourceTreeEntryBean getResourceTreeEntryBean(String resourcePath) throws RegistryMigrationException {
        try {
            return resourceAdminServiceStub.getResourceTreeEntry(resourcePath);
        } catch (RemoteException | ResourceAdminServiceExceptionException e) {
            throw new RegistryMigrationException("Error when getting the resource tree entry bean from " + resourcePath,
                                                 e);
        }
    }

    /**
     * Get the meta data of the given registry resource.
     *
     * @param resourcePath path of the registry resource
     * @return meta data of the given registry resource
     * @throws RegistryMigrationException if something goes wrong in the admin service call
     */
    MetadataBean getMetadata(String resourcePath) throws RegistryMigrationException {
        try {
            return resourceAdminServiceStub.getMetadata(resourcePath);
        } catch (RemoteException | ResourceAdminServiceExceptionException e) {
            throw new RegistryMigrationException("Error when getting the meta data from " + resourcePath, e);
        }
    }
}
