/*
Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
import org.wso2.carbon.registry.properties.stub.PropertiesAdminServiceRegistryExceptionException;
import org.wso2.carbon.registry.properties.stub.PropertiesAdminServiceStub;
import org.wso2.carbon.registry.properties.stub.beans.xsd.PropertiesBean;
import org.wso2.mi.registry.migration.exception.RegistryMigrationException;
import org.wso2.mi.registry.migration.utils.MigrationClientUtils;

import java.rmi.RemoteException;

public class PropertiesAdminServiceClient {

    private static final Logger LOGGER = LogManager.getLogger(ResourceAdminServiceClient.class);
    private PropertiesAdminServiceStub propertiesAdminServiceStub;

    PropertiesAdminServiceClient(String backEndUrl, String sessionCookie) throws RegistryMigrationException {
        String endPoint = backEndUrl + "/services/PropertiesAdminService";
        try {
            propertiesAdminServiceStub = new PropertiesAdminServiceStub(endPoint);
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Successfully initialized the PropertiesAdminServiceClient");
            }
        } catch (AxisFault e) {
            throw new RegistryMigrationException("Error occurred while initializing the PropertiesAdminServiceClient. ", e);
        }
        //Authenticate the stub from sessionCooke
        ServiceClient serviceClient;
        Options option;

        serviceClient = propertiesAdminServiceStub._getServiceClient();
        option = serviceClient.getOptions();
        option.setManageSession(true);
        option.setTimeOutInMilliSeconds(MigrationClientUtils.SESSION_TIMEOUT_IN_MILLIS);
        option.setProperty(org.apache.axis2.transport.http.HTTPConstants.COOKIE_STRING, sessionCookie);

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(
                    "Successfully authenticated the PropertiesAdminServiceClient from the sessionCookie: {}",
                    sessionCookie);
        }
    }

    /**
     * Get registry properties given the registry path.
     *
     * @param path - registry path
     * @return - all the properties defined at the registry resource
     * @throws RegistryMigrationException if error occurs while getting properties using the propertiesAdminServiceStub
     */
    PropertiesBean getProperties(String path) throws RegistryMigrationException {
        try {
            return propertiesAdminServiceStub.getProperties(path, "yes");
        } catch (RemoteException | PropertiesAdminServiceRegistryExceptionException e) {
            throw new RegistryMigrationException("Error when getting the registry properties from " + path, e);
        }
    }
}
