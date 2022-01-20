/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.micro.integrator.security.callback;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ws.security.WSPasswordCallback;
import org.wso2.micro.core.Constants;
import org.wso2.micro.core.util.KeyStoreManager;
import org.wso2.micro.integrator.security.MicroIntegratorSecurityUtils;
import org.wso2.micro.integrator.security.user.api.RealmConfiguration;
import org.wso2.micro.integrator.security.user.api.UserStoreException;
import org.wso2.micro.integrator.security.user.api.UserStoreManager;
import org.wso2.micro.integrator.security.user.core.UserCoreConstants;
import org.wso2.micro.integrator.security.user.core.util.UserCoreUtil;

import java.io.IOException;
import java.security.KeyStore;
import java.util.List;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.UnsupportedCallbackException;

/**
 * This class handles the authentication of the username token via the defined user store.
 * This class can be inherited to write a password callback handler, by implementing the getRealmConfig method.
 */
public abstract class AbstractPasswordCallback implements CallbackHandler {

    protected final Log log = LogFactory.getLog(AbstractPasswordCallback.class);
    private UserStoreManager userStoreManager;
    private RealmConfiguration realmConfig;
    private List<String> allowedRoles = null;

    @Override
    public void handle(Callback[] callbacks) throws IOException, UnsupportedCallbackException {
        try {
            boolean isAuthenticated = false;
            if (realmConfig == null) {
                try {
                    realmConfig = MicroIntegratorSecurityUtils.getRealmConfiguration();
                } catch (UserStoreException e) {
                    log.error("Error occurred while retrieving Realm Configuration", e);
                }

            }
            if (userStoreManager == null) {
                // At this point dataHolder must contain user store manager
                try {
                    userStoreManager = MicroIntegratorSecurityUtils.getUserStoreManager();
                } catch (UserStoreException e) {
                    log.error("Error occurred while retrieving User Store Manager", e);
                }
            }
            for (Callback callback : callbacks) {
                if (callback instanceof WSPasswordCallback) {
                    WSPasswordCallback passwordCallback = (WSPasswordCallback) callback;

                    String username = passwordCallback.getIdentifer();
                    String receivedPasswd = null;
                    switch (passwordCallback.getUsage()) {

                        // TODO - Handle SIGNATURE, DECRYPT AND KERBEROS_TOKEN password callback usages
                        case WSPasswordCallback.SIGNATURE:
                        case WSPasswordCallback.DECRYPT:
                            String password = getPrivateKeyPassword(username);
                            if (password == null) {
                                throw new UnsupportedCallbackException(callback,
                                        "User not available " + "in a trusted store");
                            }
                            passwordCallback.setPassword(password);
                            break;
                        case WSPasswordCallback.USERNAME_TOKEN_UNKNOWN:

                            receivedPasswd = passwordCallback.getPassword();
                            try {
                                if (receivedPasswd != null
                                        && this.authenticateUser(username, receivedPasswd)) {
                                    isAuthenticated = true;
                                } else {
                                    throw new UnsupportedCallbackException(callback, "check failed");
                                }
                            } catch (Exception e) {
                                /*
                                 * As the UnsupportedCallbackException does not accept the exception as a parameter,
                                 * the stack trace is added to the error message.
                                 *
                                 */
                                throw new UnsupportedCallbackException(callback, "Check failed : System error\n" +
                                        MicroIntegratorSecurityUtils.stackTraceToString(e.getStackTrace()));
                            }
                            break;

                        case WSPasswordCallback.USERNAME_TOKEN:

                            /*
                             * In username token scenario, if user sends the digested password, callback handler needs
                             * to provide plain text password. We get plain text password through
                             * UserCredentialRetriever interface, which is implemented by custom user store managers.
                             */

                            UserCredentialRetriever userCredentialRetriever;
                            String storedPassword = null;
                            if (userStoreManager instanceof UserCredentialRetriever) {
                                userCredentialRetriever = (UserCredentialRetriever) userStoreManager;
                                storedPassword = userCredentialRetriever.getPassword(username);
                            } else {
                                log.error("Can not set user password in callback because primary userstore class" +
                                        " has not implemented UserCredentialRetriever interface.");

                            }
                            if (storedPassword != null) {
                                try {
                                    if (!this.authenticateUser(username, storedPassword)) {
                                        log.error("User is not authorized!");
                                        throw new UnsupportedCallbackException(callback, "check failed");
                                    }
                                } catch (Exception e) {
                                    /*
                                     * As the UnsupportedCallbackException does not accept the exception as a parameter,
                                     * the stack trace is added to the error message.
                                     *
                                     */
                                    throw new UnsupportedCallbackException(callback, "Check failed : System error\n" +
                                            MicroIntegratorSecurityUtils.stackTraceToString(e.getStackTrace()));
                                }
                                passwordCallback.setPassword(storedPassword);
                                break;
                            }

                        default:

                            /*
                             * When the password is null WS4J reports an error saying no password available for the
                             * user. But its better if we simply report authentication failure. Therefore setting the
                             * password to be the empty string in this situation.
                             */

                            passwordCallback.setPassword(receivedPasswd);
                            break;
                    }
                    if (isAuthenticated) {
                        return;
                    }
                } else {
                    throw new UnsupportedCallbackException(callback, "Unrecognized Callback");
                }
            }
        } catch (UnsupportedCallbackException | IOException e) {
            if (log.isDebugEnabled()) {
                //logging invlaid attempts
                log.debug("Error in handling PasswordCallbackHandler", e);
                throw e;
            }
            throw e;
        } catch (Exception e) {
            log.error("Error in handling PasswordCallbackHandler", e);
            throw new UnsupportedCallbackException(null, e.getMessage());
        }
    }

    private boolean authenticateUser(String user, String password) throws Exception {
        boolean isAuthenticated;
        try {
            isAuthenticated = userStoreManager.authenticate(user, password);
            String domainName = UserCoreUtil.getDomainFromThreadLocal();
            String usernameWithDomain = addDomainToName(user, domainName);
            return isAuthenticated && hasAllowedRole(usernameWithDomain);
        } catch (Exception e) {
            log.error("Error in authenticating user.", e);
            throw e;
        }
    }

    public RealmConfiguration getRealmConfig() {
        return realmConfig;
    }

    public void setRealmConfig(RealmConfiguration realmConfig) {
        this.realmConfig = realmConfig;
    }

    public void setAllowedRoles(List<String> roles) {
        this.allowedRoles = roles;
    }

    public void removeAllowedRoles() {
        this.allowedRoles = null;
    }

    private String getPrivateKeyPassword(String username) throws IOException, Exception {
        String password = null;
        KeyStoreManager keyMan = KeyStoreManager.getInstance(Constants.SUPER_TENANT_ID);
        KeyStore store = keyMan.getPrimaryKeyStore();
        if (store.containsAlias(username)) {
            password = keyMan.getPrimaryPrivateKeyPasssword();
        }
        return password;
    }

    private boolean hasAllowedRole(String authenticatedUser) throws UserStoreException {
        if (allowedRoles != null) {
            String[] existingRoles = userStoreManager.getRoleListOfUser(authenticatedUser);
            for (String existingRole : existingRoles) {
                if (allowedRoles.contains(existingRole)) {
                    return true;
                }
            }
            return false;
        }
        return true;
    }

    public static String addDomainToName(String name, String domainName) {
        if (domainName != null && name != null && !name.contains(UserCoreConstants.DOMAIN_SEPARATOR) &&
                !"PRIMARY".equalsIgnoreCase(domainName)) {
            if (!"Internal".equalsIgnoreCase(domainName) && !"Workflow".equalsIgnoreCase(domainName) &&
                    !"Application".equalsIgnoreCase(domainName)) {
                name = domainName.toUpperCase() + UserCoreConstants.DOMAIN_SEPARATOR + name;
            } else {
                name = domainName.substring(0, 1).toUpperCase() + domainName.substring(1).toLowerCase() +
                        UserCoreConstants.DOMAIN_SEPARATOR + name;
            }
        }
        return name;
    }

}
