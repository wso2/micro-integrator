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
import org.apache.axis2.context.ServiceContext;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.wso2.carbon.authenticator.stub.AuthenticationAdminStub;
import org.wso2.carbon.authenticator.stub.LoginAuthenticationExceptionException;
import org.wso2.carbon.authenticator.stub.LogoutAuthenticationExceptionException;
import org.wso2.mi.registry.migration.exception.RegistryMigrationException;

import java.rmi.RemoteException;

class LoginAdminServiceClient {

    private static final Logger LOGGER = LogManager.getLogger(LoginAdminServiceClient.class);
    private AuthenticationAdminStub authenticationAdminStub;

    /**
     * Constructor.
     *
     * @param backEndUrl url of the backend server
     * @throws RegistryMigrationException when something wrong happens while initializing the AuthenticationAdminStub
     */
    LoginAdminServiceClient(String backEndUrl) throws RegistryMigrationException {
        String endPoint = backEndUrl + "/services/AuthenticationAdmin";
        try {
            authenticationAdminStub = new AuthenticationAdminStub(endPoint);
            if (LOGGER.isDebugEnabled()){
                LOGGER.debug("Successfully initialized the AuthenticationAdminStub.");
            }
        } catch (AxisFault e) {
            throw new RegistryMigrationException("Error occurred while initializing the AuthenticationAdminStub", e);
        }
    }

    /**
     * Authenticate a user.
     *
     * @param userName      username
     * @param password      password
     * @param remoteAddress remote address
     * @return session cookie if authentication is successful. Null otherwise.
     * @throws RegistryMigrationException if something wrong happens while authenticating using the given credentials
     */
    String authenticate(String userName, String password, String remoteAddress) throws RegistryMigrationException {

        String sessionCookie;
        boolean isLogin;
        try {
            isLogin = authenticationAdminStub.login(userName, password, remoteAddress);
        } catch (RemoteException | LoginAuthenticationExceptionException e) {
            throw new RegistryMigrationException("Login failed!! \n" + e.getMessage(), e);
        }

        if (isLogin) {
            LOGGER.info("\n\nLogin Successful!!");
            ServiceContext serviceContext = authenticationAdminStub.
                    _getServiceClient().getLastOperationContext().getServiceContext();
            sessionCookie = (String) serviceContext.getProperty(HTTPConstants.COOKIE_STRING);
            if (LOGGER.isDebugEnabled()){
                LOGGER.debug("Session Cookie: {}", sessionCookie);
            }
        } else {
            throw new RegistryMigrationException(
                    "Login failed!! Please make sure your username and password are correct.");
        }
        return sessionCookie;
    }

    /**
     * Log out a user.
     */
    void logOut() {
        try {
            authenticationAdminStub.logout();
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Successfully logged out from the server.");
            }
        } catch (RemoteException | LogoutAuthenticationExceptionException e) {
            LOGGER.error("Unable to logout from the server..", e);
        }
    }
}
