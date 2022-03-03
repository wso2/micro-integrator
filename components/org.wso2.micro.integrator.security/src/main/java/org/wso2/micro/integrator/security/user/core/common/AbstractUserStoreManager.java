/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.micro.integrator.security.user.core.common;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.micro.integrator.security.UnsupportedSecretTypeException;
import org.wso2.micro.integrator.security.user.core.UserCoreConstants;
import org.wso2.micro.integrator.security.user.api.RealmConfiguration;
import org.wso2.micro.integrator.security.user.core.PaginatedUserStoreManager;
import org.wso2.micro.integrator.security.user.core.Permission;
import org.wso2.micro.integrator.security.user.core.UserRealm;
import org.wso2.micro.integrator.security.user.core.UserStoreConfigConstants;
import org.wso2.micro.integrator.security.user.core.UserStoreException;
import org.wso2.micro.integrator.security.user.core.UserStoreManager;
import org.wso2.micro.integrator.security.user.core.authorization.AuthorizationCache;
import org.wso2.micro.integrator.security.user.core.claim.Claim;
import org.wso2.micro.integrator.security.user.core.claim.ClaimManager;
import org.wso2.micro.integrator.security.user.core.claim.ClaimMapping;
import org.wso2.micro.integrator.security.user.core.config.UserStorePreferenceOrderSupplier;
import org.wso2.micro.integrator.security.user.core.constants.UserCoreClaimConstants;
import org.wso2.micro.integrator.security.user.core.constants.UserCoreErrorConstants;
import org.wso2.micro.integrator.security.user.core.constants.UserCoreErrorConstants.ErrorMessages;
import org.wso2.micro.integrator.security.user.core.dto.RoleDTO;
import org.wso2.micro.integrator.security.user.core.hybrid.HybridRoleManager;
import org.wso2.micro.integrator.security.user.core.hybrid.JdbcHybridRoleManager;
import org.wso2.micro.integrator.security.user.core.internal.UMListenerServiceComponent;
import org.wso2.micro.integrator.security.user.core.jdbc.JDBCUserStoreManager;
import org.wso2.micro.integrator.security.user.core.ldap.LDAPConstants;
import org.wso2.micro.integrator.security.user.core.listener.SecretHandleableListener;
import org.wso2.micro.integrator.security.user.core.listener.UserManagementErrorEventListener;
import org.wso2.micro.integrator.security.user.core.listener.UserOperationEventListener;
import org.wso2.micro.integrator.security.user.core.listener.UserStoreManagerConfigurationListener;
import org.wso2.micro.integrator.security.user.core.listener.UserStoreManagerListener;
import org.wso2.micro.integrator.security.user.core.model.Condition;
import org.wso2.micro.integrator.security.user.core.model.ExpressionCondition;
import org.wso2.micro.integrator.security.user.core.model.ExpressionOperation;
import org.wso2.micro.integrator.security.user.core.model.OperationalCondition;
import org.wso2.micro.integrator.security.user.core.model.OperationalOperation;
import org.wso2.micro.integrator.security.user.core.model.UserClaimSearchEntry;
import org.wso2.micro.integrator.security.user.core.model.UserMgtContext;
import org.wso2.micro.integrator.security.user.core.multiplecredentials.UserAlreadyExistsException;
import org.wso2.micro.integrator.security.user.core.profile.ProfileConfigurationManager;
import org.wso2.micro.integrator.security.user.core.service.RealmService;
import org.wso2.micro.integrator.security.user.core.system.SystemUserRoleManager;
import org.wso2.micro.integrator.security.user.core.util.UserCoreUtil;
import org.wso2.micro.integrator.security.util.Secret;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.nio.CharBuffer;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.sql.DataSource;

import static org.wso2.micro.integrator.security.user.core.constants.UserCoreErrorConstants.ErrorMessages.ERROR_CODE_DUPLICATE_WHILE_ADDING_A_HYBRID_ROLE;
import static org.wso2.micro.integrator.security.user.core.constants.UserCoreErrorConstants.ErrorMessages.ERROR_CODE_DUPLICATE_WHILE_ADDING_A_SYSTEM_ROLE;
import static org.wso2.micro.integrator.security.user.core.constants.UserCoreErrorConstants.ErrorMessages.ERROR_CODE_DUPLICATE_WHILE_ADDING_A_SYSTEM_USER;
import static org.wso2.micro.integrator.security.user.core.constants.UserCoreErrorConstants.ErrorMessages.ERROR_CODE_DUPLICATE_WHILE_ADDING_A_USER;
import static org.wso2.micro.integrator.security.user.core.constants.UserCoreErrorConstants.ErrorMessages.ERROR_CODE_DUPLICATE_WHILE_ADDING_ROLE;

public abstract class AbstractUserStoreManager implements UserStoreManager, PaginatedUserStoreManager {

    protected static final String TRUE_VALUE = "true";
    protected static final String FALSE_VALUE = "false";
    private static final String MAX_LIST_LENGTH = "100";
    private static final int MAX_ITEM_LIMIT_UNLIMITED = -1;
    private static final String MULIPLE_ATTRIBUTE_ENABLE = "MultipleAttributeEnable";
    private static final String DISAPLAY_NAME_CLAIM = "http://wso2.org/claims/displayName";
    private static final String SCIM_USERNAME_CLAIM_URI = "urn:scim:schemas:core:1.0:userName";
    private static final String SCIM2_USERNAME_CLAIM_URI = "urn:ietf:params:scim:schemas:core:2.0:User:userName";
    private static final String USERNAME_CLAIM_URI = "http://wso2.org/claims/username";
    private static final String APPLICATION_DOMAIN = "Application";
    private static final String WORKFLOW_DOMAIN = "Workflow";
    private static final String INVALID_CLAIM_URL = "InvalidClaimUrl";
    private static final String INVALID_USER_NAME = "InvalidUserName";
    private static final String READ_ONLY_STORE = "ReadOnlyUserStoreManager";
    private static final String READ_ONLY_PRIMARY_STORE = "ReadOnlyPrimaryUserStoreManager";
    private static final String ADMIN_USER = "AdminUser";
    private static final String PROPERTY_PASSWORD_ERROR_MSG = "PasswordJavaRegExViolationErrorMsg";
    private static final String MULTI_ATTRIBUTE_SEPARATOR = "MultiAttributeSeparator";
    private static Log log = LogFactory.getLog(AbstractUserStoreManager.class);
    protected int tenantId;
    protected DataSource dataSource = null;
    protected RealmConfiguration realmConfig = null;
    protected ClaimManager claimManager = null;
    protected UserRealm userRealm = null;
    protected HybridRoleManager hybridRoleManager = null;
    // User roles cache
    protected UserRolesCache userRolesCache = null;
    protected SystemUserRoleManager systemUserRoleManager = null;
    protected boolean readGroupsEnabled = false;
    protected boolean writeGroupsEnabled = false;
    private UserStoreManager secondaryUserStoreManager;
    private boolean userRolesCacheEnabled = true;
    private String cacheIdentifier;
    private boolean replaceEscapeCharactersAtUserLogin = true;
    private Map<String, UserStoreManager> userStoreManagerHolder = new HashMap<String, UserStoreManager>();
    private Map<String, Integer> maxUserListCount = null;
    private Map<String, Integer> maxRoleListCount = null;
    private List<UserStoreManagerConfigurationListener> listener = new ArrayList<UserStoreManagerConfigurationListener>();
    private static final ThreadLocal<Boolean> isSecureCall = new ThreadLocal<Boolean>() {
        @Override
        protected Boolean initialValue() {
            return Boolean.FALSE;
        }
    };

    private void setClaimManager(ClaimManager claimManager) throws IllegalAccessException {
        if (Boolean.parseBoolean(realmConfig.getRealmProperty(UserCoreClaimConstants.INITIALIZE_NEW_CLAIM_MANAGER))) {
            this.claimManager = claimManager;
        } else {
            throw new IllegalAccessException("Set claim manager is not allowed");
        }
    }

    /**
     * This method is used by the APIs' in the AbstractUserStoreManager
     * to make compatible with Java Security Manager.
     */
    private Object callSecure(final String methodName, final Object[] objects, final Class[] argTypes)
            throws UserStoreException {

        final AbstractUserStoreManager instance = this;

        isSecureCall.set(Boolean.TRUE);
        final Method method;
        try {
            Class clazz = Class.forName("org.wso2.micro.integrator.security.user.core.common.AbstractUserStoreManager");
            method = clazz.getDeclaredMethod(methodName, argTypes);

        } catch (NoSuchMethodException e) {
            log.error("Error occurred when calling method " + methodName, e);
            throw new UserStoreException(e);
        } catch (ClassNotFoundException e) {
            log.error("Error occurred when calling class " + methodName, e);
            throw new UserStoreException(e);
        }

        try {
            return AccessController.doPrivileged(new PrivilegedExceptionAction<Object>() {
                @Override
                public Object run() throws Exception {
                    return method.invoke(instance, objects);
                }
            });
        } catch (PrivilegedActionException e) {
            if (e.getCause() != null && e.getCause().getCause() != null && e.getCause().getCause() instanceof
                    UserStoreException) {
                // Actual UserStoreException get wrapped with two exceptions
                throw (UserStoreException) e.getCause().getCause();
            } else {
                String msg;
                if (objects != null && argTypes != null) {
                    msg = "Error occurred while accessing Java Security Manager Privilege Block when called by " +
                            "method " + methodName + " with " + objects.length + " length of Objects and argTypes " +
                            Arrays.toString(argTypes);
                } else {
                    msg = "Error occurred while accessing Java Security Manager Privilege Block";
                }
                log.error(msg);
                throw new UserStoreException(msg, e);
            }
        } finally {
            isSecureCall.set(Boolean.FALSE);
        }
    }

    /**
     * This method is used by the support system to read properties
     */
    protected abstract Map<String, String> getUserPropertyValues(String userName,
                                                                 String[] propertyNames, String profileName) throws UserStoreException;
    /**
     * @param roleName
     * @return
     */
    protected abstract boolean doCheckExistingRole(String roleName) throws UserStoreException;

    /**
     * Creates the search base and other relevant parameters for the provided role name
     *
     * @param roleName
     * @return
     */
    protected abstract RoleContext createRoleContext(String roleName) throws UserStoreException;

    /**
     * @param userName
     * @return
     * @throws UserStoreException
     */
    protected abstract boolean doCheckExistingUser(String userName) throws UserStoreException;

    /**
     * Retrieves a list of user names for given user's property in user profile
     *
     * @param property    user property in user profile
     * @param value       value of property
     * @param profileName profile name, can be null. If null the default profile is considered.
     * @return An array of user names
     * @throws UserStoreException if the operation failed
     */
    protected abstract String[] getUserListFromProperties(String property, String value,
                                                          String profileName) throws UserStoreException;

    /**
     * Given the user name and a credential object, the implementation code must validate whether
     * the user is authenticated.
     *
     * @param userName   The user name
     * @param credential The credential of a user
     * @return If the value is true the provided credential match with the user name. False is
     * returned for invalid credential, invalid user name and mismatching credential with
     * user name.
     * @throws UserStoreException An unexpected exception has occurred
     */
    protected abstract boolean doAuthenticate(String userName, Object credential)
            throws UserStoreException;

    /**
     * Add a user to the user store.
     *
     * @param userName              User name of the user
     * @param credential            The credential/password of the user
     * @param roleList              The roles that user belongs
     * @param claims                Properties of the user
     * @param profileName           profile name, can be null. If null the default profile is considered.
     * @param requirePasswordChange whether password required is need
     * @throws UserStoreException An unexpected exception has occurred
     */
    protected abstract void  doAddUser(String userName, Object credential, String[] roleList,
                                      Map<String, String> claims, String profileName, boolean requirePasswordChange)
            throws UserStoreException;

    /**
     * Update the credential/password of the user
     *
     * @param userName      The user name
     * @param newCredential The new credential/password
     * @param oldCredential The old credential/password
     * @throws UserStoreException An unexpected exception has occurred
     */
    protected abstract void doUpdateCredential(String userName, Object newCredential,
                                               Object oldCredential) throws UserStoreException;

    /**
     * Update credential/password by the admin of another user
     *
     * @param userName      The user name
     * @param newCredential The new credential
     * @throws UserStoreException An unexpected exception has occurred
     */
    protected abstract void doUpdateCredentialByAdmin(String userName, Object newCredential)
            throws UserStoreException;

    /**
     * Delete the user with the given user name
     *
     * @param userName The user name
     * @throws UserStoreException An unexpected exception has occurred
     */
    protected abstract void doDeleteUser(String userName) throws UserStoreException;

    /**
     * Set a single user claim value
     *
     * @param userName    The user name
     * @param claimURI    The claim URI
     * @param claimValue  The value
     * @param profileName The profile name, can be null. If null the default profile is considered.
     * @throws UserStoreException An unexpected exception has occurred
     */
    protected abstract void doSetUserClaimValue(String userName, String claimURI,
                                                String claimValue, String profileName) throws UserStoreException;

    /**
     * Set many user claim values
     *
     * @param userName    The user name
     * @param claims      Map of claim URIs against values
     * @param profileName The profile name, can be null. If null the default profile is considered.
     * @throws UserStoreException An unexpected exception has occurred
     */
    protected abstract void doSetUserClaimValues(String userName, Map<String, String> claims,
                                                 String profileName) throws UserStoreException;

    /**
     * o * Delete a single user claim value
     *
     * @param userName    The user name
     * @param claimURI    Name of the claim
     * @param profileName The profile name, can be null. If null the default profile is considered.
     * @throws UserStoreException An unexpected exception has occurred
     */
    protected abstract void doDeleteUserClaimValue(String userName, String claimURI,
                                                   String profileName) throws UserStoreException;

    /**
     * Delete many user claim values.
     *
     * @param userName    The user name
     * @param claims      URIs of the claims to be deleted.
     * @param profileName The profile name, can be null. If null the default profile is considered.
     * @throws UserStoreException An unexpected exception has occurred
     */
    protected abstract void doDeleteUserClaimValues(String userName, String[] claims,
                                                    String profileName) throws UserStoreException;

    /**
     * Update user list of a particular role
     *
     * @param roleName     The role name
     * @param deletedUsers Array of user names, that is going to be removed from the role
     * @param newUsers     Array of user names, that is going to be added to the role
     * @throws UserStoreException An unexpected exception has occurred
     */
    protected abstract void doUpdateUserListOfRole(String roleName, String[] deletedUsers,
                                                   String[] newUsers) throws UserStoreException;

    /**
     * Update role list of a particular user
     *
     * @param userName     The user name
     * @param deletedRoles Array of role names, that is going to be removed from the user
     * @param newRoles     Array of role names, that is going to be added to the user
     * @throws UserStoreException An unexpected exception has occurred
     */
    protected abstract void doUpdateRoleListOfUser(String userName, String[] deletedRoles,
                                                   String[] newRoles) throws UserStoreException;

    /**
     * Only gets the internal roles of the user with internal domain name
     *
     * @param userName Name of the user - who we need to find roles.
     * @return
     * @throws UserStoreException
     */
    protected String[] doGetInternalRoleListOfUser(String userName, String filter) throws UserStoreException {
        if (Boolean.parseBoolean(realmConfig.getUserStoreProperty(MULIPLE_ATTRIBUTE_ENABLE))) {
            String userNameAttribute = realmConfig.getUserStoreProperty(LDAPConstants.USER_NAME_ATTRIBUTE);
            if (userNameAttribute != null && userNameAttribute.trim().length() > 0) {
                Map<String, String> map = getUserPropertyValues(userName, new String[]{userNameAttribute}, null);
                String tempUserName = map.get(userNameAttribute);
                if (tempUserName != null) {
                    userName = tempUserName;
                    if (log.isDebugEnabled()) {
                        log.debug("Replaced user name : " + userName + " from user property value : " + tempUserName);
                    }
                }
            }
        }
        if (log.isDebugEnabled()) {
            log.debug("Retrieving internal roles for user name :  " + userName + " and search filter : " + filter);
        }
        return hybridRoleManager.getHybridRoleListOfUser(userName, filter);
    }

    protected Map<String, List<String>> doGetInternalRoleListOfUsers(List<String> userNames, String domainName)
            throws UserStoreException {

        if (Boolean.parseBoolean(realmConfig.getUserStoreProperty(MULIPLE_ATTRIBUTE_ENABLE))) {
            List<String> updatedUserNameList = new ArrayList<>();
            for (String userName : userNames) {
                String userNameAttribute = realmConfig.getUserStoreProperty(LDAPConstants.USER_NAME_ATTRIBUTE);
                if (userNameAttribute != null && userNameAttribute.trim().length() > 0) {
                    Map<String, String> map = getUserPropertyValues(userName, new String[] { userNameAttribute }, null);
                    String tempUserName = map.get(userNameAttribute);
                    if (tempUserName != null) {
                        updatedUserNameList.add(tempUserName);
                        if (log.isDebugEnabled()) {
                            log.debug(
                                    "Replaced user name : " + userName + " from user property value : " + tempUserName);
                        }
                    } else {
                        updatedUserNameList.add(userName);
                    }
                } else {
                    updatedUserNameList.add(userName);
                }
            }
            userNames = updatedUserNameList;
        }

        return hybridRoleManager.getHybridRoleListOfUsers(userNames, domainName);
    }

    /**
     * Only gets the external roles of the user.
     *
     * @param userName Name of the user - who we need to find roles.
     * @return
     * @throws UserStoreException
     */
    protected abstract String[] doGetExternalRoleListOfUser(String userName, String filter)
            throws UserStoreException;


    /**
     * Returns the shared roles list of the user
     *
     * @param userName
     * @return
     * @throws UserStoreException
     */
    protected abstract String[] doGetSharedRoleListOfUser(String userName,
                                                          String tenantDomain, String filter) throws UserStoreException;

    /**
     * Add role with a list of users and permissions provided.
     *
     * @param roleName
     * @param userList
     * @throws UserStoreException
     */
    protected abstract void doAddRole(String roleName, String[] userList, boolean shared) throws UserStoreException;


    /**
     * delete the role.
     *
     * @param roleName
     * @throws UserStoreException
     */
    protected abstract void doDeleteRole(String roleName) throws UserStoreException;

    /**
     * update the role name with the new name
     *
     * @param roleName
     * @param newRoleName
     * @throws UserStoreException
     */
    protected abstract void doUpdateRoleName(String roleName, String newRoleName)
            throws UserStoreException;

    /**
     * This method would returns the role Name actually this must be implemented in interface. As it
     * is not good to change the API in point release. This has been added to Abstract class
     *
     * @param filter
     * @param maxItemLimit
     * @return
     * @throws .UserStoreException
     */
    protected abstract String[] doGetRoleNames(String filter, int maxItemLimit)
            throws UserStoreException;

    /**
     * @param filter
     * @param maxItemLimit
     * @return
     * @throws UserStoreException
     */
    protected abstract String[] doListUsers(String filter, int maxItemLimit)
            throws UserStoreException;

    /*This is to get the display names of users in hybrid role according to the underlying user store, to be shown in UI*/
    protected abstract String[] doGetDisplayNamesForInternalRole(String[] userNames)
            throws UserStoreException;

    /**
     * To validate username and credential that is given for authentication.
     *
     * @param userName   Name of the user.
     * @param credential Credential of the user.
     * @return false if the validation fails.
     * @throws UserStoreException UserStore Exception.
     */
    private boolean validateUserNameAndCredential(String userName, Object credential) throws UserStoreException {

        boolean isValid = true;
        if (userName == null || credential == null) {
            String message = String.format(ErrorMessages.ERROR_CODE_ERROR_WHILE_PRE_AUTHENTICATION.getMessage(),
                    "Authentication failure. Either Username or Password is null");
            handleOnAuthenticateFailure(ErrorMessages.ERROR_CODE_ERROR_WHILE_PRE_AUTHENTICATION.getCode(), message,
                    userName, credential);
            log.error(message);
            isValid = false;
        }
        return isValid;
    }

    /**
     * {@inheritDoc}
     */
    public final boolean authenticate(final String userName, final Object credential) throws UserStoreException {
        try {
            return AccessController.doPrivileged(new PrivilegedExceptionAction<Boolean>() {
                @Override
                public Boolean run() throws Exception {
                    if (!validateUserNameAndCredential(userName, credential)) {
                        return  false;
                    }
                    int index = userName.indexOf(UserCoreConstants.DOMAIN_SEPARATOR);
                    boolean domainProvided = index > 0;
                    return authenticate(userName, credential, domainProvided);
                }
            });
        } catch (PrivilegedActionException e) {
            if (!(e.getException() instanceof UserStoreException)) {
                handleOnAuthenticateFailure(ErrorMessages.ERROR_CODE_ERROR_WHILE_AUTHENTICATION.getCode(),
                        String.format(ErrorMessages.ERROR_CODE_ERROR_WHILE_AUTHENTICATION.getMessage(), e.getMessage()),
                        userName, credential);
            }
            throw (UserStoreException) e.getException();
        }
    }

    protected boolean authenticate(final String userName, final Object credential, final boolean domainProvided)
            throws UserStoreException {

        try {
            return AccessController.doPrivileged(new PrivilegedExceptionAction<Boolean>() {
                @Override
                public Boolean run() throws Exception {
                    return authenticateInternalIteration(userName, credential, domainProvided);
                }
            });
        } catch (PrivilegedActionException e) {
            throw (UserStoreException) e.getException();
        }

    }

    private boolean authenticateInternalIteration(String userName, Object credential, boolean domainProvided)
            throws UserStoreException {

        List<String> userStorePreferenceOrder = new ArrayList<>();
        // Check whether user store chain needs to be generated or not.
        if (isUserStoreChainNeeded(userStorePreferenceOrder)) {
            if (log.isDebugEnabled()) {
                log.debug("User store chain generation is needed hence generating the user store chain using the user" +
                        " store preference order: " + userStorePreferenceOrder);
            }
            return generateUserStoreChain(userName, credential, domainProvided, userStorePreferenceOrder);
        } else {
            // Authenticate the user.
            return authenticateInternal(userName, credential, domainProvided);
        }
    }

    /**
     * This method is responsible for calling the relevant method from error listeners when there is a failure while
     * authenticating.
     *
     * @param errorCode    Error Code.
     * @param errorMessage Error Message.
     * @param userName     Name of the user.
     * @param credential   Relevant credential provided for authentication.
     * @throws UserStoreException Exception that will be thrown by relevant listener methods.
     */
    private void handleOnAuthenticateFailure(String errorCode, String errorMessage, String userName, Object credential)
            throws UserStoreException {

        for (UserManagementErrorEventListener listener : UMListenerServiceComponent
                .getUserManagementErrorEventListeners()) {
            if (listener.isEnable() && !listener
                    .onAuthenticateFailure(errorCode, errorMessage, userName, credential, this)) {
                return;
            }
        }
    }

    /**
     * @param userName
     * @param credential
     * @param domainProvided
     * @return
     * @throws UserStoreException
     */
    private boolean authenticateInternal(String userName, Object credential, boolean domainProvided)
            throws UserStoreException {

        AbstractUserStoreManager abstractUserStoreManager = this;
        if (this instanceof IterativeUserStoreManager) {
            abstractUserStoreManager = ((IterativeUserStoreManager) this).getAbstractUserStoreManager();
        }

        boolean authenticated = false;

        UserStore userStore = abstractUserStoreManager.getUserStore(userName);
        if (userStore.isRecurssive() && userStore.getUserStoreManager() instanceof AbstractUserStoreManager) {
            return ((AbstractUserStoreManager) userStore.getUserStoreManager()).authenticate(userStore.getDomainFreeName(),
                    credential, domainProvided);
        }

        Secret credentialObj;
        try {
            credentialObj = Secret.getSecret(credential);
        } catch (UnsupportedSecretTypeException e) {
            handleOnAuthenticateFailure(ErrorMessages.ERROR_CODE_UNSUPPORTED_CREDENTIAL_TYPE.getCode(),
                    ErrorMessages.ERROR_CODE_UNSUPPORTED_CREDENTIAL_TYPE.getMessage(), userName, credential);
            throw new UserStoreException(ErrorMessages.ERROR_CODE_UNSUPPORTED_CREDENTIAL_TYPE.toString(), e);
        }

        // #################### Domain Name Free Zone Starts Here ################################

        // #################### <Listeners> #####################################################
        try {
            for (UserStoreManagerListener listener : UMListenerServiceComponent.getUserStoreManagerListeners()) {
                Object credentialArgument;
                if (listener instanceof SecretHandleableListener) {
                    credentialArgument = credentialObj;
                } else {
                    credentialArgument = credential;
                }

                if (!listener.authenticate(userName, credentialArgument, abstractUserStoreManager)) {
                    handleOnAuthenticateFailure(ErrorMessages.ERROR_CODE_ERROR_WHILE_AUTHENTICATION.getCode(),
                            ErrorMessages.ERROR_CODE_ERROR_WHILE_AUTHENTICATION.getMessage(), userName,
                            credentialArgument);
                    return true;
                }
            }

            try {
                for (UserOperationEventListener listener : UMListenerServiceComponent
                        .getUserOperationEventListeners()) {
                    Object credentialArgument;
                    if (listener instanceof SecretHandleableListener) {
                        credentialArgument = credentialObj;
                    } else {
                        credentialArgument = credential;
                    }

                    if (!listener.doPreAuthenticate(userName, credentialArgument, abstractUserStoreManager)) {
                        handleOnAuthenticateFailure(ErrorMessages.ERROR_CODE_ERROR_WHILE_PRE_AUTHENTICATION.getCode(),
                                String.format(ErrorMessages.ERROR_CODE_ERROR_WHILE_PRE_AUTHENTICATION.getMessage(),
                                        UserCoreErrorConstants.PRE_LISTENER_TASKS_FAILED_MESSAGE), userName,
                                credentialArgument);
                        return false;
                    }
                }
            } catch (UserStoreException ex) {
                handleOnAuthenticateFailure(ErrorMessages.ERROR_CODE_ERROR_WHILE_PRE_AUTHENTICATION.getCode(),
                        String.format(ErrorMessages.ERROR_CODE_ERROR_WHILE_PRE_AUTHENTICATION.getMessage(),
                                ex.getMessage()), userName, credential);
                throw ex;
            }
            // #################### </Listeners> #####################################################

            int tenantId = abstractUserStoreManager.getTenantId();

//            try {
//                RealmService realmService = UserCoreUtil.getRealmService();
//                if (realmService != null) {
//                    boolean tenantActive = realmService.getTenantManager().isTenantActive(tenantId);
//
//                    if (!tenantActive) {
//                        String errorCode = ErrorMessages.ERROR_CODE_TENANT_DEACTIVATED.getCode();
//                        String errorMessage = String
//                                .format(ErrorMessages.ERROR_CODE_TENANT_DEACTIVATED.getMessage(), tenantId);
//                        log.warn(errorCode + " - " + errorMessage);
//                        handleOnAuthenticateFailure(errorCode, errorMessage, userName, credential);
//                        return false;
//                    }
//                }
//            } catch (org.wso2.micro.integrator.security.user.api.UserStoreException e) {
//                handleOnAuthenticateFailure(ErrorMessages.ERROR_CODE_ERROR_WHILE_PRE_AUTHENTICATION.getCode(),
//                        String.format(ErrorMessages.ERROR_CODE_ERROR_WHILE_PRE_AUTHENTICATION.getMessage(),
//                                e.getMessage()), userName, credential);
//                throw new UserStoreException("Error while trying to check tenant status for Tenant : " + tenantId, e);
//            }

            // We are here due to two reason. Either there is no secondary UserStoreManager or no
            // domain name provided with user name.

            try {
                // Let's authenticate with the primary UserStoreManager.
                authenticated = abstractUserStoreManager.doAuthenticate(userName, credentialObj);
            } catch (Exception e) {
                handleOnAuthenticateFailure(ErrorMessages.ERROR_CODE_ERROR_WHILE_AUTHENTICATION.getCode(),
                        String.format(ErrorMessages.ERROR_CODE_ERROR_WHILE_AUTHENTICATION.getMessage(), e.getMessage()),
                        userName, credential);
                // We can ignore and proceed. Ignore the results from this user store.

                if (log.isDebugEnabled()) {
                  log.debug("Error occurred while authenticating user: " + userName, e);
                } else {
                  log.error(e);
                }
                authenticated = false;
            }

        } finally {
            credentialObj.clear();
        }

        if (authenticated) {
            // Set domain in thread local variable for subsequent operations
            UserCoreUtil.setDomainInThreadLocal(UserCoreUtil.getDomainName(abstractUserStoreManager.realmConfig));
        }

        // If authentication fails in the previous step and if the user has not specified a
        // domain- then we need to execute chained UserStoreManagers recursively.
        if (!authenticated && !domainProvided) {
            AbstractUserStoreManager userStoreManager;
            if (this instanceof IterativeUserStoreManager) {
                IterativeUserStoreManager iterativeUserStoreManager = (IterativeUserStoreManager) this;
                userStoreManager = iterativeUserStoreManager.nextUserStoreManager();
            } else {
                userStoreManager = (AbstractUserStoreManager) abstractUserStoreManager.getSecondaryUserStoreManager();
            }
            if (userStoreManager != null) {
                authenticated = userStoreManager.authenticate(userName, credential, domainProvided);
            }
        }

        if (!authenticated) {
            handleOnAuthenticateFailure(ErrorMessages.ERROR_CODE_ERROR_WHILE_AUTHENTICATION.getCode(),
                    String.format(ErrorMessages.ERROR_CODE_ERROR_WHILE_AUTHENTICATION.getMessage(),
                            "Authentication failed"), userName, credential);
        }

        try {
            // You cannot change authentication decision in post handler to TRUE
            for (UserOperationEventListener listener : UMListenerServiceComponent.getUserOperationEventListeners()) {
                if (!listener.doPostAuthenticate(userName, authenticated, abstractUserStoreManager)) {
                    handleOnAuthenticateFailure(ErrorMessages.ERROR_CODE_ERROR_WHILE_POST_AUTHENTICATION.getCode(),
                            String.format(ErrorMessages.ERROR_CODE_ERROR_WHILE_POST_AUTHENTICATION.getMessage(),
                                    UserCoreErrorConstants.POST_LISTENER_TASKS_FAILED_MESSAGE), userName, credential);
                    return false;
                }
            }
        } catch (UserStoreException ex) {
            handleOnAuthenticateFailure(ErrorMessages.ERROR_CODE_ERROR_WHILE_POST_AUTHENTICATION.getCode(),
                    String.format(ErrorMessages.ERROR_CODE_ERROR_WHILE_POST_AUTHENTICATION.getMessage(),
                            ex.getMessage()), userName, credential);
            throw ex;
        }

        if (log.isDebugEnabled()) {
            if (!authenticated) {
                log.debug("Authentication failure. Wrong username or password is provided.");
            }
        }

        return authenticated;
    }

    /**
     * This method calls the relevant methods when there is a failure while trying to get the claim value of a user.
     *
     * @param errorCode    Error code.
     * @param errorMessage Error message.
     * @param userName     Name of the user.
     * @param claim        Relevant claim.
     * @param profileName  Name of the profile.
     * @throws UserStoreException Exception that will be thrown by relevant listeners.
     */
    private void handleGetUserClaimValueFailure(String errorCode, String errorMessage, String userName, String claim,
            String profileName) throws UserStoreException {

        for (UserManagementErrorEventListener listener : UMListenerServiceComponent
                .getUserManagementErrorEventListeners()) {
            if (listener.isEnable() && !listener
                    .onGetUserClaimValueFailure(errorCode, errorMessage, userName, claim, profileName, this)) {
                return;
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    public final String getUserClaimValue(String userName, String claim, String profileName)
            throws UserStoreException {

        if (!isSecureCall.get()) {
            Class argTypes[] = new Class[]{String.class, String.class, String.class};
            Object object = callSecure("getUserClaimValue", new Object[]{userName, claim, profileName}, argTypes);
            return (String) object;
        }

        UserStore userStore = getUserStore(userName);
        if (userStore.isRecurssive()) {
            return userStore.getUserStoreManager().getUserClaimValue(userStore.getDomainFreeName(),
                    claim, profileName);
        }

        // #################### Domain Name Free Zone Starts Here ################################
        // If user does not exist, throw an exception

        if (!doCheckExistingUser(userName)) {
            String errorCode = ErrorMessages.ERROR_CODE_NON_EXISTING_USER.getCode();
            String errorMessage = String.format(ErrorMessages.ERROR_CODE_NON_EXISTING_USER.getMessage(), userName,
                    realmConfig.getUserStoreProperty(UserCoreConstants.RealmConfig.PROPERTY_DOMAIN_NAME));
            handleGetUserClaimValueFailure(errorCode, errorMessage, userName, claim, profileName);
            throw new UserStoreException(errorCode + " - " + errorMessage);
        }

        Map<String, String> finalValues;
        try {
            finalValues = doGetUserClaimValues(userName, new String[] { claim }, userStore.getDomainName(),
                    profileName);
        } catch (UserStoreException ex) {
            handleGetUserClaimValueFailure(ErrorMessages.ERROR_CODE_ERROR_WHILE_GETTING_USER_CLAIM_VALUE.getCode(),
                    String.format(ErrorMessages.ERROR_CODE_ERROR_WHILE_GETTING_USER_CLAIM_VALUE.getMessage(),
                            ex.getMessage()), userName, claim, profileName);
            throw ex;
        }

        String value = null;

        if (finalValues != null) {
            value = finalValues.get(claim);
        }

        // #################### <Listeners> #####################################################

        List<String> list = new ArrayList<String>();
        if (value != null) {
            list.add(value);
        }

        try {
            for (UserOperationEventListener listener : UMListenerServiceComponent.getUserOperationEventListeners()) {
                if (listener instanceof AbstractUserOperationEventListener) {
                    AbstractUserOperationEventListener newListener = (AbstractUserOperationEventListener) listener;
                    if (!newListener.doPostGetUserClaimValue(userName, claim, list, profileName, this)) {
                        handleGetUserClaimValueFailure(
                                ErrorMessages.ERROR_CODE_ERROR_DURING_POST_GET_USER_CLAIM_VALUE.getCode(),
                                String.format(
                                        ErrorMessages.ERROR_CODE_ERROR_DURING_POST_GET_USER_CLAIM_VALUE.getMessage(),
                                        UserCoreErrorConstants.POST_LISTENER_TASKS_FAILED_MESSAGE), userName, claim,
                                profileName);
                        break;
                    }
                }
            }
        } catch (UserStoreException ex) {
            handleGetUserClaimValueFailure(ErrorMessages.ERROR_CODE_ERROR_DURING_POST_GET_USER_CLAIM_VALUE.getCode(),
                    String.format(ErrorMessages.ERROR_CODE_ERROR_DURING_POST_GET_USER_CLAIM_VALUE.getMessage(),
                            ex.getMessage()), userName, claim, profileName);
            throw ex;
        }
        // #################### </Listeners> #####################################################

        if (!list.isEmpty()) {
            return list.get(0);
        }
        return value;
    }

    /**
     * This method is responsible for calling relevant listener methods when there is a failure while trying to get
     * user claim values.
     *
     * @param errorCode    Relevant error code.
     * @param errorMessage Relevant error message.
     * @param userName     Name of the user.
     * @param claims       Claims requested.
     * @param profileName  Name of the profile.
     * @throws UserStoreException Exception that will be thrown by the relevant listeners.
     */
    private void handleGetUserClaimValuesFailure(String errorCode, String errorMessage, String userName,
            String[] claims, String profileName) throws UserStoreException {

        for (UserManagementErrorEventListener listener : UMListenerServiceComponent
                .getUserManagementErrorEventListeners()) {
            if (listener.isEnable() && !listener
                    .onGetUserClaimValuesFailure(errorCode, errorMessage, userName, claims, profileName, this)) {
                return;
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    public final Claim[] getUserClaimValues(String userName, String profileName)
            throws UserStoreException {

        if (!isSecureCall.get()) {
            Class argTypes[] = new Class[]{String.class, String.class};
            Object object = callSecure("getUserClaimValues", new Object[]{userName, profileName}, argTypes);
            return (Claim[]) object;
        }

        UserStore userStore = getUserStore(userName);
        if (userStore.isRecurssive()) {
            return userStore.getUserStoreManager().getUserClaimValues(
                    userStore.getDomainFreeName(), profileName);
        }

        // #################### Domain Name Free Zone Starts Here ################################
        // If user does not exist, throw exception
        if (!doCheckExistingUser(userName)) {
            String errorMessage = String.format(ErrorMessages.ERROR_CODE_NON_EXISTING_USER.getMessage(), userName,
                    realmConfig.getUserStoreProperty(UserCoreConstants.RealmConfig.PROPERTY_DOMAIN_NAME));
            String errorCode = ErrorMessages.ERROR_CODE_NON_EXISTING_USER.getCode();
            handleGetUserClaimValuesFailure(errorCode, errorMessage, userName, null, profileName);
            throw new UserStoreException(errorCode + " - " + errorMessage);
        }

        if (profileName == null || profileName.trim().length() == 0) {
            profileName = UserCoreConstants.DEFAULT_PROFILE;
        }

        String[] claims;
        try {
            claims = claimManager.getAllClaimUris();
        } catch (org.wso2.micro.integrator.security.user.api.UserStoreException e) {
            handleGetUserClaimValuesFailure(ErrorMessages.ERROR_CODE_ERROR_WHILE_GETTING_CLAIM_URI.getCode(),
                    String.format(ErrorMessages.ERROR_CODE_ERROR_WHILE_GETTING_CLAIM_URI.getMessage(), e.getMessage()),
                    userName, null, profileName);
            throw new UserStoreException(e);
        }

        Map<String, String> values = this.getUserClaimValues(userName, claims, profileName);
        Claim[] finalValues = new Claim[values.size()];
        int i = 0;
        for (Iterator<Map.Entry<String, String>> ite = values.entrySet().iterator(); ite.hasNext(); ) {
            Map.Entry<String, String> entry = ite.next();
            Claim claim = new Claim();
            claim.setValue(entry.getValue());
            claim.setClaimUri(entry.getKey());
            String displayTag;
            try {
                displayTag = claimManager.getClaim(entry.getKey()).getDisplayTag();
            } catch (org.wso2.micro.integrator.security.user.api.UserStoreException e) {
                throw new UserStoreException(e);
            }
            claim.setDisplayTag(displayTag);
            finalValues[i] = claim;
            i++;
        }

        return finalValues;
    }

    /**
     * {@inheritDoc}
     */
    public final Map<String, String> getUserClaimValues(String userName, String[] claims,
                                                        String profileName) throws UserStoreException {

        if (!isSecureCall.get()) {
            Class argTypes[] = new Class[]{String.class, String[].class, String.class};
            Object object = callSecure("getUserClaimValues", new Object[]{userName, claims, profileName}, argTypes);
            return (Map<String, String>) object;
        }

        UserStore userStore = getUserStore(userName);
        if (userStore.isRecurssive()) {
            return userStore.getUserStoreManager().getUserClaimValues(
                    userStore.getDomainFreeName(), claims, profileName);
        }

        // #################### Domain Name Free Zone Starts Here ################################
        if (!doCheckExistingUser(userName)) {
            String errorMessage = String.format(ErrorMessages.ERROR_CODE_NON_EXISTING_USER.getMessage(), userName,
                    realmConfig.getUserStoreProperty(UserCoreConstants.RealmConfig.PROPERTY_DOMAIN_NAME));
            String errorCode = ErrorMessages.ERROR_CODE_NON_EXISTING_USER.getCode();
            handleGetUserClaimValuesFailure(errorCode, errorMessage, userName, claims, profileName);
            throw new UserStoreException(errorCode + " - " + errorMessage);
        }
        // check for null claim list
        if (claims == null) {
            claims = new String[0];
        }

        Map<String, String> finalValues;
        try {
            finalValues = doGetUserClaimValues(userName, claims, userStore.getDomainName(), profileName);
        } catch (UserStoreException ex) {
            handleGetUserClaimValuesFailure(ErrorMessages.ERROR_CODE_ERROR_WHILE_GETTING_CLAIM_VALUES.getCode(),
                    String.format(ErrorMessages.ERROR_CODE_ERROR_WHILE_GETTING_CLAIM_VALUES.getMessage(),
                            ex.getMessage()), userName, claims, profileName);
            throw ex;
        }

        // #################### <Listeners> #####################################################
        try {
            for (UserOperationEventListener listener : UMListenerServiceComponent.getUserOperationEventListeners()) {
                if (listener instanceof AbstractUserOperationEventListener) {
                    AbstractUserOperationEventListener newListener = (AbstractUserOperationEventListener) listener;
                    if (!newListener
                            .doPostGetUserClaimValues(userStore.getDomainFreeName(), claims, profileName, finalValues,
                                    this)) {
                        handleGetUserClaimValuesFailure(
                                ErrorMessages.ERROR_CODE_ERROR_IN_POST_GET_CLAIM_VALUES.getCode(),
                                String.format(ErrorMessages.ERROR_CODE_ERROR_IN_POST_GET_CLAIM_VALUES.getMessage(),
                                        UserCoreErrorConstants.POST_LISTENER_TASKS_FAILED_MESSAGE), userName, claims,
                                profileName);
                        break;
                    }
                }
            }
        } catch (UserStoreException ex) {
            handleGetUserClaimValuesFailure(ErrorMessages.ERROR_CODE_ERROR_IN_POST_GET_CLAIM_VALUES.getCode(),
                    String.format(ErrorMessages.ERROR_CODE_ERROR_IN_POST_GET_CLAIM_VALUES.getMessage(),
                            ex.getMessage()), userName, claims, profileName);
            throw ex;
        }
        // #################### </Listeners> #####################################################

        return finalValues;
    }

    /**
     * This method is responsible for calling the relevant methods when there is a failure while trying to get the
     * user list.
     *
     * @param errorCode    Error Code.
     * @param errorMessage Error Message.
     * @param claim        Claim URI.
     * @param claimValue   Claim Value.
     * @param profileName  Name of the profile.
     * @throws UserStoreException Exception that will be thrown by relevant listner methods.
     */
    private void handleGetUserListFailure(String errorCode, String errorMessage, String claim, String claimValue,
            String profileName) throws UserStoreException {

        for (UserManagementErrorEventListener listener : UMListenerServiceComponent
                .getUserManagementErrorEventListeners()) {
            if (listener.isEnable() && !listener
                    .onGetUserListFailure(errorCode, errorMessage, claim, claimValue, profileName, this)) {
                return;
            }
        }
    }

    /**
     * This method is responsible for calling the relevant methods when there is a failure while trying to get the
     * user list.
     *
     * @param errorCode    Error Code.
     * @param errorMessage Error Message.
     * @param claim        Claim URI.
     * @param claimValue   Claim Value.
     * @param limit   No of search records.
     * @param offset   Start index of the search.
     * @param profileName  Name of the profile.
     * @throws UserStoreException Exception that will be thrown by relevant listner methods.
     */
    private void handleGetUserListFailure(String errorCode, String errorMessage, String claim, String claimValue, int
            limit, int offset, String profileName) throws UserStoreException {

        for (UserManagementErrorEventListener listener : UMListenerServiceComponent
                .getUserManagementErrorEventListeners()) {
            if (listener.isEnable() && listener instanceof AbstractUserManagementErrorListener
                    && !((AbstractUserManagementErrorListener) listener).onGetUserListFailure(errorCode,
                    errorMessage, claim, claimValue, limit, offset, profileName, this)) {
                return;
            }
        }
    }

    private void handleGetUserListFailure(String errorCode, String errorMassage, Condition condition, String domain,
                                          String profileName, int limit, int offset, String sortBy, String sortOrder)
            throws UserStoreException {

        for (UserManagementErrorEventListener listener : UMListenerServiceComponent
                .getUserManagementErrorEventListeners()) {
            if (listener.isEnable() && listener instanceof AbstractUserManagementErrorListener
                    && !((AbstractUserManagementErrorListener) listener).onGetUserListFailure(errorCode,
                    errorMassage, condition, domain, profileName, limit, offset, sortBy, sortOrder, this)) {
                return;
            }
        }
    }

    /**
     * This method is responsible for calling the relevant methods when there is a failure while trying to get the
     * paginated user list.
     *
     * @param errorCode    Error Code.
     * @param errorMessage Error Message.
     * @param claim        Claim URI.
     * @param claimValue   Claim Value.
     * @param profileName  Name of the profile.
     * @throws UserStoreException Exception that will be thrown by relevant listner methods.
     */
    private void handleGetPaginatedUserListFailure(String errorCode, String errorMessage, String claim, String
            claimValue, String profileName) throws UserStoreException {

        for (UserManagementErrorEventListener listener : UMListenerServiceComponent
                .getUserManagementErrorEventListeners()) {
            if (listener.isEnable() && listener instanceof AbstractUserManagementErrorListener
                    && !((AbstractUserManagementErrorListener) listener).onGetPaginatedUserListFailure(errorCode,
                    errorMessage, claim, claimValue, profileName, this)) {
                return;
            }
        }
    }

    /**
     * This method is responsible for calling the relevant methods when there is a failure while trying to list the
     * paginated users.
     *
     * @param errorCode    Error Code.
     * @param errorMessage Error Message.
     * @param filter       Username Filter.
     * @param limit        No of search results.
     * @param offset       Start index of the search.
     * @throws UserStoreException Exception that will be thrown by relevant listner methods.
     */
    private void handleListPaginatedUsersFailure(String errorCode, String errorMessage, String filter, int limit, int
            offset) throws UserStoreException {

        for (UserManagementErrorEventListener listener : UMListenerServiceComponent
                .getUserManagementErrorEventListeners()) {
            if (listener.isEnable() && listener instanceof AbstractUserManagementErrorListener
                    && !((AbstractUserManagementErrorListener) listener).onListUsersFailure(errorCode,
                    errorMessage, filter, limit, offset, this)) {
                return;
            }
        }
    }

    /**
     * To call the postGetUserList of relevant listeners.
     *
     * @param claim            Claim requested.
     * @param claimValue       Claim values.
     * @param filteredUserList List of filtered users.
     * @param isAuditLogOnly   To indicate whether to call only audit log listener.
     * @throws UserStoreException User Store Exception.
     */
    private void handlePostGetUserList(String claim, String claimValue, List<String> filteredUserList,
            boolean isAuditLogOnly) throws UserStoreException {

        try {
            for (UserOperationEventListener listener : UMListenerServiceComponent.getUserOperationEventListeners()) {
                if (listener instanceof AbstractUserOperationEventListener) {
                    if (isAuditLogOnly && !listener.getClass().getName()
                            .endsWith(UserCoreErrorConstants.AUDIT_LOGGER_CLASS_NAME)) {
                        continue;
                    }
                    AbstractUserOperationEventListener newListener = (AbstractUserOperationEventListener) listener;
                    if (!newListener.doPostGetUserList(claim, claimValue, filteredUserList, this)) {
                        break;
                    }
                }
            }
        } catch (UserStoreException ex) {
            handleGetUserListFailure(ErrorMessages.ERROR_CODE_ERROR_DURING_POST_GET_USER_LIST.getCode(),
                    String.format(ErrorMessages.ERROR_CODE_ERROR_DURING_POST_GET_USER_LIST.getMessage(),
                            ex.getMessage()), claim, claimValue, null);
            throw ex;
        }
    }

    /**
     * To call the postGetUserList of relevant listeners.
     *
     * @param claim            Claim requested.
     * @param claimValue       Claim values.
     * @param filteredUserList List of filtered users.
     * @param limit            No of search results.
     * @param offset           Start index of the search.
     * @param isAuditLogOnly   To indicate whether to call only audit log listener.
     * @throws UserStoreException User Store Exception.
     */
    private void handlePostGetUserList(String claim, String claimValue, List<String> filteredUserList, int limit, int
            offset, boolean isAuditLogOnly) throws UserStoreException {

        try {
            for (UserOperationEventListener listener : UMListenerServiceComponent.getUserOperationEventListeners()) {
                if (listener instanceof AbstractUserOperationEventListener) {
                    if (isAuditLogOnly && !listener.getClass().getName()
                            .endsWith(UserCoreErrorConstants.AUDIT_LOGGER_CLASS_NAME)) {
                        continue;
                    }
                    AbstractUserOperationEventListener newListener = (AbstractUserOperationEventListener) listener;
                    if (!newListener.doPostGetUserList(claim, claimValue, filteredUserList, limit, offset, this)) {
                        break;
                    }
                }
            }
        } catch (UserStoreException ex) {
            handleGetUserListFailure(ErrorMessages.ERROR_CODE_ERROR_DURING_POST_GET_USER_LIST.getCode(),
                    String.format(ErrorMessages.ERROR_CODE_ERROR_DURING_POST_GET_USER_LIST.getMessage(),
                            ex.getMessage()), claim, claimValue, limit, offset, null);
            throw ex;
        }
    }

    private void handlePostGetUserList(Condition condition, String domain, String profileName, int limit, int offset,
                                       String sortBy, String sortOrder, String[] users, boolean isAuditLogOnly)
            throws UserStoreException {

        try {
            for (UserOperationEventListener listener : UMListenerServiceComponent.getUserOperationEventListeners()) {
                if (listener instanceof AbstractUserOperationEventListener) {
                    if (isAuditLogOnly && !listener.getClass().getName()
                            .endsWith(UserCoreErrorConstants.AUDIT_LOGGER_CLASS_NAME)) {
                        continue;
                    }
                    AbstractUserOperationEventListener newListener = (AbstractUserOperationEventListener) listener;
                    if (!newListener.doPostGetUserList(condition, domain, profileName, limit, offset, sortBy,
                            sortOrder, users, this)) {
                        break;
                    }
                }
            }
        } catch (UserStoreException ex) {
            handleGetUserListFailure(ErrorMessages.ERROR_CODE_ERROR_DURING_POST_GET_CONDITIONAL_USER_LIST.getCode(),
                    String.format(ErrorMessages.ERROR_CODE_ERROR_DURING_POST_GET_CONDITIONAL_USER_LIST.getMessage(),
                            ex.getMessage()), condition, domain, profileName, limit, offset, sortBy, sortOrder);
            throw ex;
        }
    }

    private void handlePreGetUserList(Condition condition, String domain, String profileName, int limit, int offset,
                                      String sortBy, String sortOrder) throws UserStoreException {

        try {
            for (UserOperationEventListener listener : UMListenerServiceComponent
                    .getUserOperationEventListeners()) {
                if (listener instanceof AbstractUserOperationEventListener) {
                    AbstractUserOperationEventListener newListener = (AbstractUserOperationEventListener) listener;
                    if (!newListener.doPreGetUserList(condition, domain, profileName, limit, offset, sortBy,
                            sortOrder, this)) {

                        handleGetUserListFailure(ErrorMessages.ERROR_CODE_ERROR_DURING_PRE_GET__CONDITIONAL_USER_LIST.getCode(),
                                String.format(ErrorMessages.ERROR_CODE_ERROR_DURING_PRE_GET__CONDITIONAL_USER_LIST.getMessage(),
                                        UserCoreErrorConstants.PRE_LISTENER_TASKS_FAILED_MESSAGE), condition, domain,
                                profileName, limit, offset, sortBy, sortOrder);
                        break;
                    }
                }
            }
        } catch (UserStoreException ex) {
            handleGetUserListFailure(ErrorMessages.ERROR_CODE_ERROR_DURING_PRE_GET__CONDITIONAL_USER_LIST.getCode(),
                    String.format(ErrorMessages.ERROR_CODE_ERROR_DURING_PRE_GET__CONDITIONAL_USER_LIST.getMessage(),
                            ex.getMessage()), condition, domain,
                    profileName, limit, offset, sortBy, sortOrder);
            throw ex;
        }
    }


    /**
     * To call the postGetPaginatedUserList of relevant listeners.
     *
     * @param claim            Claim requested.
     * @param claimValue       Claim values.
     * @param filteredUserList List of filtered users.
     * @param isAuditLogOnly   To indicate whether to call only audit log listener.
     * @throws UserStoreException User Store Exception.
     */
    private void handlePostGetPaginatedUserList(String claim, String claimValue, List<String> filteredUserList,
                                       boolean isAuditLogOnly) throws UserStoreException {

        try {
            for (UserOperationEventListener listener : UMListenerServiceComponent.getUserOperationEventListeners()) {
                if (listener instanceof AbstractUserOperationEventListener) {
                    if (isAuditLogOnly && !listener.getClass().getName()
                            .endsWith(UserCoreErrorConstants.AUDIT_LOGGER_CLASS_NAME)) {
                        continue;
                    }
                    AbstractUserOperationEventListener newListener = (AbstractUserOperationEventListener) listener;
                    if (!newListener.doPostGetPaginatedUserList(claim, claimValue, filteredUserList, this)) {
                        break;
                    }
                }
            }
        } catch (UserStoreException ex) {
            handleGetPaginatedUserListFailure(ErrorMessages.ERROR_CODE_ERROR_DURING_POST_GET_PAGINATED_USER_LIST.getCode(),
                    String.format(ErrorMessages.ERROR_CODE_ERROR_DURING_POST_GET_PAGINATED_USER_LIST.getMessage(),
                            ex.getMessage()), claim, claimValue, null);
            throw ex;
        }
    }

    /**
     * To call the List paginated users of relevant listeners.
     *
     * @param filter           Username filter.
     * @param limit            No of search results.
     * @param offset           start index of the search.
     * @param filteredUserList List of filtered users.
     * @param isAuditLogOnly   To indicate whether to call only audit log listener.
     * @throws UserStoreException User Store Exception.
     */
    private void handlePostListPaginatedUsers(String filter, int limit, int offset, List<String> filteredUserList,
                                                boolean isAuditLogOnly) throws UserStoreException {

        try {
            for (UserOperationEventListener listener : UMListenerServiceComponent.getUserOperationEventListeners()) {
                if (listener instanceof AbstractUserOperationEventListener) {
                    if (isAuditLogOnly && !listener.getClass().getName()
                            .endsWith(UserCoreErrorConstants.AUDIT_LOGGER_CLASS_NAME)) {
                        continue;
                    }
                    AbstractUserOperationEventListener newListener = (AbstractUserOperationEventListener) listener;
                    if (!newListener.doPostListUsers(filter, limit, offset, filteredUserList, this)) {
                        break;
                    }
                }
            }
        } catch (UserStoreException ex) {
            handleListPaginatedUsersFailure(ErrorMessages.ERROR_CODE_ERROR_DURING_POST_LIST_PAGINATED_USER.getCode(),
                    String.format(ErrorMessages.ERROR_CODE_ERROR_DURING_POST_LIST_PAGINATED_USER.getMessage(),
                            ex.getMessage()), filter, limit, offset);
            throw ex;
        }
    }

    /**
     * If the claim is domain qualified, search the users respective user store. Else we
     * return the users in all the user-stores recursively
     * {@inheritDoc}
     */
    public final String[] getUserList(String claim, String claimValue, String profileName) throws UserStoreException {

        if (!isSecureCall.get()) {
            Class argTypes[] = new Class[]{String.class, String.class, String.class};
            Object object = callSecure("getUserList", new Object[]{claim, claimValue, profileName}, argTypes);
            return (String[]) object;
        }

        if (claim == null) {
            String errorCode = ErrorMessages.ERROR_CODE_INVALID_CLAIM_URI.getCode();
            String errorMessage = String.format(ErrorMessages.ERROR_CODE_INVALID_CLAIM_URI.getMessage(), "");
            handleGetUserListFailure(errorCode, errorMessage, null, claimValue, profileName);
            throw new IllegalArgumentException(ErrorMessages.ERROR_CODE_INVALID_CLAIM_URI.toString());
        }

        if (claimValue == null) {
            handleGetUserListFailure(ErrorMessages.ERROR_CODE_INVALID_CLAIM_VALUE.getCode(),
                    ErrorMessages.ERROR_CODE_INVALID_CLAIM_VALUE.getMessage(), claim, null, profileName);
            throw new IllegalArgumentException(ErrorMessages.ERROR_CODE_INVALID_CLAIM_VALUE.toString());
        }

        if (log.isDebugEnabled()) {
            log.debug("Listing users who having value as " + claimValue + " for the claim " + claim);
        }

        if (USERNAME_CLAIM_URI.equalsIgnoreCase(claim) || SCIM_USERNAME_CLAIM_URI.equalsIgnoreCase(claim) ||
                SCIM2_USERNAME_CLAIM_URI.equalsIgnoreCase(claim)) {

            if (log.isDebugEnabled()) {
                log.debug("Switching to list users using username");
            }

            String[] filteredUsers = listUsers(claimValue, MAX_ITEM_LIMIT_UNLIMITED);

            if (log.isDebugEnabled()) {
                log.debug("Filtered users: " + Arrays.toString(filteredUsers));
            }

            return filteredUsers;
        }

        // Extracting the domain from claimValue.
        String extractedDomain = null;
        int index;
        index = claimValue.indexOf(UserCoreConstants.DOMAIN_SEPARATOR);
        if (index > 0) {
            String names[] = claimValue.split(UserCoreConstants.DOMAIN_SEPARATOR);
            extractedDomain = names[0].trim();
        }

        UserStoreManager userManager = null;
        if (StringUtils.isNotEmpty(extractedDomain)) {
            userManager = getSecondaryUserStoreManager(extractedDomain);
            if (log.isDebugEnabled()) {
                log.debug("Domain: " + extractedDomain + " is passed with the claim and user store manager is loaded" +
                        " for the given domain name.");
            }
        }

        if (userManager instanceof JDBCUserStoreManager && (SCIM_USERNAME_CLAIM_URI.equalsIgnoreCase(claim) ||
                SCIM2_USERNAME_CLAIM_URI.equalsIgnoreCase(claim))) {
            if (userManager.isExistingUser(claimValue)) {
                return new String[] {claimValue};
            } else {
                return new String [0];
            }
        }

        claimValue = UserCoreUtil.removeDomainFromName(claimValue);

        final List<String> filteredUserList = new ArrayList<>();

        if (StringUtils.isNotEmpty(extractedDomain)) {
            try {
                for (UserOperationEventListener listener : UMListenerServiceComponent
                        .getUserOperationEventListeners()) {
                    if (listener instanceof AbstractUserOperationEventListener) {
                        AbstractUserOperationEventListener newListener = (AbstractUserOperationEventListener) listener;
                        if (!newListener.doPreGetUserList(claim, claimValue, filteredUserList, userManager)) {
                            handleGetUserListFailure(ErrorMessages.ERROR_CODE_ERROR_DURING_PRE_GET_USER_LIST.getCode(),
                                    String.format(ErrorMessages.ERROR_CODE_ERROR_DURING_PRE_GET_USER_LIST.getMessage(),
                                            UserCoreErrorConstants.PRE_LISTENER_TASKS_FAILED_MESSAGE), claim,
                                    claimValue, profileName);
                            break;
                        }
                    }
                }
            } catch (UserStoreException ex) {
                handleGetUserListFailure(ErrorMessages.ERROR_CODE_ERROR_DURING_PRE_GET_USER_LIST.getCode(),
                        String.format(ErrorMessages.ERROR_CODE_ERROR_DURING_PRE_GET_USER_LIST.getMessage(),
                                ex.getMessage()), claim, claimValue, profileName);
                throw ex;
            }
        }

        if (log.isDebugEnabled()) {
            log.debug("Pre listener user list: " + filteredUserList + " for domain: " + extractedDomain);
        }

        // Iterate through user stores and check for users for this claim.
        List<String> usersFromUserStore = doGetUserList(claim, claimValue, profileName, extractedDomain, userManager);
        if (log.isDebugEnabled()) {
            log.debug("Users from user store: " + extractedDomain + " : " + usersFromUserStore);
        }
        filteredUserList.addAll(usersFromUserStore);

        if (StringUtils.isNotEmpty(extractedDomain)) {
            handlePostGetUserList(claim, claimValue, filteredUserList, false);
        }

        if (log.isDebugEnabled()) {
            log.debug("Post listener user list: " + filteredUserList + " for domain: " + extractedDomain);
        }

        Collections.sort(filteredUserList);
        return filteredUserList.toArray(new String[0]);
    }

    private List<String> doGetUserList(String claim, String claimValue, String profileName, String extractedDomain,
                                       UserStoreManager userManager)
        throws UserStoreException {

        String property;

        // If domain is present, then we search within that domain only.
        if (StringUtils.isNotEmpty(extractedDomain)) {

            if (userManager == null) {
                if (log.isDebugEnabled()) {
                    log.debug("No user store manager found for domain: " + extractedDomain);
                }
                return Collections.emptyList();
            }

            if (log.isDebugEnabled()) {
                log.debug("Domain found in claim value. Searching only in the " + extractedDomain + " for possible " +
                        "matches");
            }

            try {
                property = claimManager.getAttributeName(extractedDomain, claim);
            } catch (org.wso2.micro.integrator.security.user.api.UserStoreException e) {
                handleGetUserListFailure(ErrorMessages.ERROR_CODE_ERROR_DURING_PRE_GET_USER_LIST.getCode(),
                        String.format(ErrorMessages.ERROR_CODE_ERROR_DURING_PRE_GET_USER_LIST.getMessage(),
                                e.getMessage()), claim, claimValue, profileName);
                throw new UserStoreException(
                        "Error occurred while retrieving attribute name for domain : " + extractedDomain + " and claim "
                                + claim, e);
            }
            if (property == null) {
                if (log.isDebugEnabled()) {
                    log.debug("Could not find matching property for\n" +
                            "claim :" + claim +
                            "domain :" + extractedDomain);
                }
                return Collections.emptyList();
            }

            if (userManager instanceof AbstractUserStoreManager) {
                // Get the user list and return with domain appended.
                try {
                    AbstractUserStoreManager userStoreManager = (AbstractUserStoreManager) userManager;
                    String[] userArray = userStoreManager.getUserListFromProperties(property, claimValue, profileName);
                    if (log.isDebugEnabled()) {
                        log.debug("List of filtered users for: " + extractedDomain + " : " + Arrays.asList(userArray));
                    }
                    return Arrays.asList(UserCoreUtil.addDomainToNames(userArray, extractedDomain));
                } catch (UserStoreException ex) {
                    handleGetUserListFailure(ErrorMessages.ERROR_CODE_ERROR_WHILE_GETTING_USER_LIST.getCode(),
                            String.format(ErrorMessages.ERROR_CODE_ERROR_WHILE_GETTING_USER_LIST.getMessage(),
                                    ex.getMessage()), claim, claimValue, profileName);
                    throw ex;
                }
            } else {
                if (log.isDebugEnabled()) {
                    log.debug("getUserListFromProperties is not supported by this user store: " +
                            userManager.getClass());
                }
                return Collections.emptyList();
            }
        }

        // If domain is not given then search all the user stores.
        if (log.isDebugEnabled()) {
            log.debug("No domain name found in claim value. Searching through all user stores for possible matches");
        }

        List<String> usersFromAllStoresList = new ArrayList<>();
        List<UserStoreManager> userStoreManagers = getUserStoreMangers();

        // Iterate through all of available user store managers.
        for (UserStoreManager userStoreManager : userStoreManagers) {

            // If this is not an instance of Abstract User Store Manger we can ignore the flow since we can't get the
            // domain name.
            if (!(userStoreManager instanceof AbstractUserStoreManager)) {
                continue;
            }

            // For all the user stores append the domain name to the claim and pass it recursively (Including PRIMARY).
            String domainName = ((AbstractUserStoreManager) userStoreManager).getMyDomainName();
            String claimValueWithDomain;
            if (StringUtils.equalsIgnoreCase(domainName, UserCoreConstants.PRIMARY_DEFAULT_DOMAIN_NAME)) {
                claimValueWithDomain = domainName + UserCoreConstants.DOMAIN_SEPARATOR + claimValue;
            } else {
                claimValueWithDomain = UserCoreUtil.addDomainToName(claimValue, domainName);
            }

            if (log.isDebugEnabled()) {
                log.debug("Invoking the get user list for domain: " + domainName + " for claim: " + claim +
                        " value: " + claimValueWithDomain);
            }

            // Recursively call the getUserList method appending the domain to claim value.
            List<String> userList = Arrays.asList(getUserList(claim, claimValueWithDomain, profileName));
            if (log.isDebugEnabled()) {
                log.debug("Secondary user list for domain: " + domainName + " : " + userList);
            }

            usersFromAllStoresList.addAll(userList);
        }

        // Done with all user store processing. Return the user array if not empty.
        return usersFromAllStoresList;
    }

    private List<String> doGetUserList(String claim, String claimValue, String profileName, int limit, int offset,
                                       String extractedDomain, UserStoreManager userManager)
            throws UserStoreException {

        String property;

        // If domain is present, then we search within that domain only.
        if (StringUtils.isNotEmpty(extractedDomain)) {

            if (userManager == null) {
                if (log.isDebugEnabled()) {
                    log.debug("No user store manager found for domain: " + extractedDomain);
                }
                return Collections.emptyList();
            }

            if (log.isDebugEnabled()) {
                log.debug("Domain found in claim value. Searching only in the " + extractedDomain + " for possible " +
                        "matches");
            }

            try {
                property = claimManager.getAttributeName(extractedDomain, claim);
            } catch (org.wso2.micro.integrator.security.user.api.UserStoreException e) {
                handleGetUserListFailure(ErrorMessages.ERROR_CODE_ERROR_DURING_PRE_GET_USER_LIST.getCode(),
                        String.format(ErrorMessages.ERROR_CODE_ERROR_DURING_PRE_GET_USER_LIST.getMessage(),
                                e.getMessage()), claim, claimValue, limit, offset, profileName);
                throw new UserStoreException(
                        "Error occurred while retrieving attribute name for domain : " + extractedDomain + " and claim "
                                + claim, e);
            }
            if (property == null) {
                if (log.isDebugEnabled()) {
                    log.debug("Could not find matching property for\n" +
                            "claim :" + claim +
                            "domain :" + extractedDomain);
                }
                return Collections.emptyList();
            }

            if (userManager instanceof AbstractUserStoreManager) {
                // Get the user list and return with domain appended.
                try {
                    AbstractUserStoreManager userStoreManager = (AbstractUserStoreManager) userManager;
                    PaginatedSearchResult result = userStoreManager.getUserListFromProperties(property, claimValue,
                            profileName, limit, offset);
                    if (log.isDebugEnabled()) {
                        log.debug("List of filtered paginated users for: " + extractedDomain + " : " + Arrays.asList
                                (result.getUsers()));
                    }
                    return Arrays.asList(UserCoreUtil.addDomainToNames(result.getUsers(), extractedDomain));
                } catch (UserStoreException ex) {
                    handleGetUserListFailure(ErrorMessages.ERROR_CODE_ERROR_WHILE_GETTING_USER_LIST.getCode(),
                            String.format(ErrorMessages.ERROR_CODE_ERROR_WHILE_GETTING_USER_LIST.getMessage(),
                                    ex.getMessage()), claim, claimValue, limit, offset, profileName);
                    throw ex;
                }
            } else {
                if (log.isDebugEnabled()) {
                    log.debug("getUserListFromProperties is not supported by this user store: " +
                            userManager.getClass());
                }
                return Collections.emptyList();
            }
        }

        // If domain is not given then search all the user stores.
        if (log.isDebugEnabled()) {
            log.debug("No domain name found in claim value. Searching through all user stores for possible matches");
        }

        List<String> usersFromAllStoresList = new ArrayList<>();
        List<UserStoreManager> userStoreManagers = getUserStoreMangers();
        int nonPaginatedUserCount = 0;

        // Iterate through all of available user store managers.
        for (UserStoreManager userStoreManager : userStoreManagers) {

            // If this is not an instance of Abstract User Store Manger we can ignore the flow since we can't get the
            // domain name.
            if (!(userStoreManager instanceof AbstractUserStoreManager)) {
                continue;
            }

            if (limit <= 0) {
                return usersFromAllStoresList;
            }

            // For all the user stores append the domain name to the claim and pass it recursively (Including PRIMARY).
            String domainName = ((AbstractUserStoreManager) userStoreManager).getMyDomainName();

            try {
                property = claimManager.getAttributeName(domainName, claim);
            } catch (org.wso2.micro.integrator.security.user.api.UserStoreException e) {
                handleGetUserListFailure(ErrorMessages.ERROR_CODE_ERROR_DURING_PRE_GET_USER_LIST.getCode(),
                        String.format(ErrorMessages.ERROR_CODE_ERROR_DURING_PRE_GET_USER_LIST.getMessage(),
                                e.getMessage()), claim, claimValue, limit, offset, profileName);
                throw new UserStoreException(
                        "Error occurred while retrieving attribute name for domain : " + extractedDomain + " and claim "
                                + claim, e);
            }

            // Recursively call the getUserList method appending the domain to claim value.
            PaginatedSearchResult userList = getUserListFromProperties(property, claimValue, profileName, limit, offset);
            if (log.isDebugEnabled()) {
                log.debug("Secondary user list for domain: " + domainName + " : " + userList);
            }
            limit = limit - userList.getUsers().length;
            nonPaginatedUserCount = userList.getSkippedUserCount();

            if (userList.getUsers().length > 0) {
                offset = 1;
            } else {
                offset = offset - nonPaginatedUserCount;
            }

            usersFromAllStoresList.addAll(Arrays.asList(UserCoreUtil.addDomainToNames(userList.getUsers(), domainName)));
        }

        // Done with all user store processing. Return the user array if not empty.
        return usersFromAllStoresList;
    }

    /**
     * Get the list of user store managers available including primary user store manger.
     * @return List of user store managers available.
     */
    private List<UserStoreManager> getUserStoreMangers() {

        List<UserStoreManager> userStoreManagers = new ArrayList<>();
        UserStoreManager currentUserStoreManager = this;

        // Get the list of user store managers(Including PRIMARY). Later we have to iterate through them.
        while (currentUserStoreManager != null) {
            userStoreManagers.add(currentUserStoreManager);
            currentUserStoreManager = currentUserStoreManager.getSecondaryUserStoreManager();
        }

        return userStoreManagers;
    }

    /**
     * This method calls the relevant listener methods when there is a failure while trying to update credentials.
     *
     * @param errorCode     Relevant error code.
     * @param errorMessage  Error message.
     * @param userName      Name of the user.
     * @param newCredential New credential.
     * @param oldCredential Old credential.
     */
    private void handleUpdateCredentialFailure(String errorCode, String errorMessage, String userName,
            Object newCredential, Object oldCredential) throws UserStoreException {

        for (UserManagementErrorEventListener listener : UMListenerServiceComponent
                .getUserManagementErrorEventListeners()) {
            if (listener.isEnable() && !listener
                    .onUpdateCredentialFailure(errorCode, errorMessage, userName, newCredential, oldCredential, this)) {
                return;
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    public final void updateCredential(String userName, Object newCredential, Object oldCredential)
            throws UserStoreException {

        if (!isSecureCall.get()) {
            Class argTypes[] = new Class[]{String.class, Object.class, Object.class};
            callSecure("updateCredential", new Object[]{userName, newCredential, oldCredential}, argTypes);
            return;
        }

        UserStore userStore = getUserStore(userName);
        if (userStore.isRecurssive()) {
            userStore.getUserStoreManager().updateCredential(userStore.getDomainFreeName(),
                    newCredential, oldCredential);
            return;
        }

        // #################### Domain Name Free Zone Starts Here ################################

        if (isReadOnly()) {
            handleUpdateCredentialFailure(ErrorMessages.ERROR_CODE_READONLY_USER_STORE.getCode(),
                    ErrorMessages.ERROR_CODE_READONLY_USER_STORE.getMessage(), userName, newCredential, oldCredential);
            throw new UserStoreException(ErrorMessages.ERROR_CODE_READONLY_USER_STORE.toString());
        }

        Secret newCredentialObj;
        Secret oldCredentialObj;
        try {
            newCredentialObj = Secret.getSecret(newCredential);
            oldCredentialObj = Secret.getSecret(oldCredential);
        } catch (UnsupportedSecretTypeException e) {
            handleUpdateCredentialFailure(ErrorMessages.ERROR_CODE_UNSUPPORTED_CREDENTIAL_TYPE.getCode(),
                    ErrorMessages.ERROR_CODE_UNSUPPORTED_CREDENTIAL_TYPE.getMessage(), userName, newCredential,
                    oldCredential);
            throw new UserStoreException(ErrorMessages.ERROR_CODE_UNSUPPORTED_CREDENTIAL_TYPE.toString());
        }

        // #################### <Listeners> #####################################################
        try {
            try {
                for (UserStoreManagerListener listener : UMListenerServiceComponent.getUserStoreManagerListeners()) {
                    if (listener instanceof SecretHandleableListener) {
                        if (!listener.updateCredential(userName, newCredentialObj, oldCredentialObj, this)) {
                            handleUpdateCredentialFailure(
                                    ErrorMessages.ERROR_CODE_ERROR_DURING_PRE_UPDATE_CREDENTIAL.getCode(),
                                    String.format(
                                            ErrorMessages.ERROR_CODE_ERROR_DURING_PRE_UPDATE_CREDENTIAL.getMessage(),
                                            UserCoreErrorConstants.PRE_LISTENER_TASKS_FAILED_MESSAGE), userName,
                                    newCredential, oldCredential);
                            return;
                        }
                    } else {
                        if (!listener.updateCredential(userName, newCredential, oldCredential, this)) {
                            handleUpdateCredentialFailure(
                                    ErrorMessages.ERROR_CODE_ERROR_DURING_PRE_UPDATE_CREDENTIAL.getCode(),
                                    String.format(
                                            ErrorMessages.ERROR_CODE_ERROR_DURING_PRE_UPDATE_CREDENTIAL.getMessage(),
                                            UserCoreErrorConstants.PRE_LISTENER_TASKS_FAILED_MESSAGE), userName,
                                    newCredential, oldCredential);
                            return;
                        }
                    }
                }

                for (UserOperationEventListener listener : UMListenerServiceComponent
                        .getUserOperationEventListeners()) {

                    if (listener instanceof SecretHandleableListener) {
                        if (!listener.doPreUpdateCredential(userName, newCredentialObj, oldCredentialObj, this)) {
                            handleUpdateCredentialFailure(
                                    ErrorMessages.ERROR_CODE_ERROR_DURING_PRE_UPDATE_CREDENTIAL.getCode(),
                                    String.format(
                                            ErrorMessages.ERROR_CODE_ERROR_DURING_PRE_UPDATE_CREDENTIAL.getMessage(),
                                            UserCoreErrorConstants.PRE_LISTENER_TASKS_FAILED_MESSAGE), userName,
                                    newCredential, oldCredential);
                            return;
                        }
                    } else {
                        if (!listener.doPreUpdateCredential(userName, newCredential, oldCredential, this)) {
                            handleUpdateCredentialFailure(
                                    ErrorMessages.ERROR_CODE_ERROR_DURING_PRE_UPDATE_CREDENTIAL.getCode(),
                                    String.format(
                                            ErrorMessages.ERROR_CODE_ERROR_DURING_PRE_UPDATE_CREDENTIAL.getMessage(),
                                            UserCoreErrorConstants.PRE_LISTENER_TASKS_FAILED_MESSAGE), userName,
                                    newCredential, oldCredential);
                            return;
                        }
                    }
                }
            } catch (UserStoreException e) {
                handleUpdateCredentialFailure(ErrorMessages.ERROR_CODE_ERROR_DURING_PRE_UPDATE_CREDENTIAL.getCode(),
                        String.format(ErrorMessages.ERROR_CODE_ERROR_DURING_PRE_UPDATE_CREDENTIAL.getMessage(),
                                e.getMessage()), userName, newCredential, oldCredential);
                throw e;
            }
            // #################### </Listeners> #####################################################

            // This user name here is domain-less.
            // We directly authenticate user against the selected UserStoreManager.
            boolean isAuth = this.doAuthenticate(userName, oldCredentialObj);

            if (isAuth) {
                if (!checkUserPasswordValid(newCredential)) {
                    String errorMsg = realmConfig.getUserStoreProperty(PROPERTY_PASSWORD_ERROR_MSG);

                    if (errorMsg != null) {
                        String errorMessage = String
                                .format(ErrorMessages.ERROR_CODE_ERROR_DURING_PRE_UPDATE_CREDENTIAL.getMessage(),
                                        errorMsg);
                        String errorCode = ErrorMessages.ERROR_CODE_ERROR_DURING_PRE_UPDATE_CREDENTIAL.getCode();
                        handleUpdateCredentialFailure(errorCode, errorMessage, userName, newCredential, oldCredential);
                        throw new UserStoreException(errorCode + " - " + errorMessage);
                    }

                    String errorMessage = String.format(ErrorMessages.ERROR_CODE_INVALID_PASSWORD.getMessage(),
                            realmConfig.getUserStoreProperty(UserCoreConstants.RealmConfig.PROPERTY_JAVA_REG_EX));
                    String errorCode = ErrorMessages.ERROR_CODE_INVALID_PASSWORD.getCode();
                    handleUpdateCredentialFailure(errorCode, errorMessage, userName, newCredential, oldCredential);
                    throw new UserStoreException(errorCode + " - " + errorMessage);
                }

                try {
                    this.doUpdateCredential(userName, newCredentialObj, oldCredentialObj);
                } catch (UserStoreException ex) {
                    handleUpdateCredentialFailure(ErrorMessages.ERROR_CODE_ERROR_WHILE_UPDATING_CREDENTIAL.getCode(),
                            String.format(ErrorMessages.ERROR_CODE_ERROR_WHILE_UPDATING_CREDENTIAL.getMessage(),
                                    ex.getMessage()), userName, newCredential, oldCredential);
                    throw ex;
                }

                // #################### <Listeners> ##################################################
                try {
                    for (UserOperationEventListener listener : UMListenerServiceComponent
                            .getUserOperationEventListeners()) {
                        if (listener instanceof SecretHandleableListener) {
                            if (!listener.doPostUpdateCredential(userName, newCredentialObj, this)) {
                                handleUpdateCredentialFailure(
                                        ErrorMessages.ERROR_CODE_ERROR_DURING_POST_UPDATE_CREDENTIAL.getCode(),
                                        String.format(ErrorMessages.ERROR_CODE_ERROR_DURING_POST_UPDATE_CREDENTIAL
                                                .getMessage(), "Post update credential tasks failed"), userName,
                                        newCredentialObj, oldCredentialObj);
                                return;
                            }
                        } else {
                            if (!listener.doPostUpdateCredential(userName, newCredential, this)) {
                                handleUpdateCredentialFailure(
                                        ErrorMessages.ERROR_CODE_ERROR_DURING_POST_UPDATE_CREDENTIAL.getCode(),
                                        String.format(ErrorMessages.ERROR_CODE_ERROR_DURING_POST_UPDATE_CREDENTIAL
                                                .getMessage(), "Post update credential tasks failed"), userName,
                                        newCredential, oldCredential);
                                return;
                            }
                        }
                    }
                } catch (UserStoreException ex) {
                    handleUpdateCredentialFailure(
                            ErrorMessages.ERROR_CODE_ERROR_DURING_POST_UPDATE_CREDENTIAL.getCode(),
                            String.format(ErrorMessages.ERROR_CODE_ERROR_DURING_POST_UPDATE_CREDENTIAL.getMessage(),
                                    ex.getMessage()), userName, newCredential, oldCredential);
                    throw ex;
                }
                // #################### </Listeners> ##################################################

                return;
            } else {
                handleUpdateCredentialFailure(ErrorMessages.ERROR_CODE_OLD_CREDENTIAL_DOES_NOT_MATCH.getCode(),
                        ErrorMessages.ERROR_CODE_OLD_CREDENTIAL_DOES_NOT_MATCH.getMessage(), userName, newCredential,
                        oldCredential);
                throw new UserStoreException(ErrorMessages.ERROR_CODE_OLD_CREDENTIAL_DOES_NOT_MATCH.toString());
            }
        } finally {
            newCredentialObj.clear();
            oldCredentialObj.clear();
        }
    }

    /**
     * Handles the failure while there is a failure while update of credentials is done by the admin.
     *
     * @param errorCode     Relevant error code.
     * @param errorMessage  Error message.
     * @param userName      Name of the user.
     * @param newCredential New credential.
     * @throws UserStoreException Exception that could be thrown by the listeners.
     */
    private void handleUpdateCredentialByAdminFailure(String errorCode, String errorMessage, String userName,
            Object newCredential) throws UserStoreException {

        for (UserManagementErrorEventListener listener : UMListenerServiceComponent
                .getUserManagementErrorEventListeners()) {
            if (listener.isEnable() && !listener
                    .onUpdateCredentialByAdminFailure(errorCode, errorMessage, userName, newCredential, this)) {
                return;
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    public final void updateCredentialByAdmin(String userName, Object newCredential)
            throws UserStoreException {

        if (!isSecureCall.get()) {
            Class argTypes[] = new Class[]{String.class, Object.class};
            callSecure("updateCredentialByAdmin", new Object[]{userName, newCredential}, argTypes);
            return;
        }

        UserStore userStore = getUserStore(userName);
        if (userStore.isRecurssive()) {
            userStore.getUserStoreManager().updateCredentialByAdmin(userStore.getDomainFreeName(),
                    newCredential);
            return;
        }

        // #################### Domain Name Free Zone Starts Here ################################

        if (isReadOnly()) {
            handleUpdateCredentialByAdminFailure(ErrorMessages.ERROR_CODE_READONLY_USER_STORE.getCode(),
                    ErrorMessages.ERROR_CODE_READONLY_USER_STORE.getMessage(), userName, newCredential);
            throw new UserStoreException(ErrorMessages.ERROR_CODE_READONLY_USER_STORE.toString());
        }

        Secret newCredentialObj;
        try {
            newCredentialObj = Secret.getSecret(newCredential);
        } catch (UnsupportedSecretTypeException e) {
            handleUpdateCredentialByAdminFailure(ErrorMessages.ERROR_CODE_UNSUPPORTED_CREDENTIAL_TYPE.getCode(),
                    ErrorMessages.ERROR_CODE_UNSUPPORTED_CREDENTIAL_TYPE.getMessage() + " " + e.getMessage(), userName,
                    newCredential);
            throw new UserStoreException(ErrorMessages.ERROR_CODE_UNSUPPORTED_CREDENTIAL_TYPE.toString(), e);
        }

        try {
            try {
                // #################### <Listeners> #####################################################
                for (UserStoreManagerListener listener : UMListenerServiceComponent.getUserStoreManagerListeners()) {
                    Object credentialArgument;
                    if (listener instanceof SecretHandleableListener) {
                        credentialArgument = newCredentialObj;
                    } else {
                        credentialArgument = newCredential;
                    }

                    if (!listener.updateCredentialByAdmin(userName, credentialArgument, this)) {
                        handleUpdateCredentialByAdminFailure(
                                ErrorMessages.ERROR_CODE_ERROR_DURING_PRE_UPDATE_CREDENTIAL_BY_ADMIN.getCode(),
                                String.format(ErrorMessages.ERROR_CODE_ERROR_DURING_PRE_UPDATE_CREDENTIAL_BY_ADMIN
                                        .getMessage(), UserCoreErrorConstants.PRE_LISTENER_TASKS_FAILED_MESSAGE),
                                userName, credentialArgument);
                        return;
                    }
                }

                // using string buffers to allow the password to be changed by listener
                for (UserOperationEventListener listener : UMListenerServiceComponent
                        .getUserOperationEventListeners()) {

                    if (listener instanceof SecretHandleableListener) {
                        if (!listener.doPreUpdateCredentialByAdmin(userName, newCredentialObj, this)) {
                            handleUpdateCredentialByAdminFailure(
                                    ErrorMessages.ERROR_CODE_ERROR_DURING_PRE_UPDATE_CREDENTIAL_BY_ADMIN.getCode(),
                                    String.format(ErrorMessages.ERROR_CODE_ERROR_DURING_PRE_UPDATE_CREDENTIAL_BY_ADMIN
                                            .getMessage(), UserCoreErrorConstants.PRE_LISTENER_TASKS_FAILED_MESSAGE),
                                    userName, newCredentialObj);
                            return;
                        }
                    } else {
                        // using string buffers to allow the password to be changed by listener
                        StringBuffer credBuff = null;
                        if (newCredential == null) { // a default password will be set
                            credBuff = new StringBuffer();
                        } else if (newCredential instanceof String) {
                            credBuff = new StringBuffer((String) newCredential);
                        }

                        if (credBuff != null) {
                            if (!listener.doPreUpdateCredentialByAdmin(userName, credBuff, this)) {
                                handleUpdateCredentialByAdminFailure(
                                        ErrorMessages.ERROR_CODE_ERROR_DURING_PRE_UPDATE_CREDENTIAL_BY_ADMIN.getCode(),
                                        String.format(
                                                ErrorMessages.ERROR_CODE_ERROR_DURING_PRE_UPDATE_CREDENTIAL_BY_ADMIN
                                                        .getMessage(),
                                                UserCoreErrorConstants.PRE_LISTENER_TASKS_FAILED_MESSAGE), userName,
                                        credBuff);
                                return;
                            }
                            // reading the modified value
                            newCredential = credBuff.toString();
                            newCredentialObj.clear();
                            try {
                                newCredentialObj = Secret.getSecret(newCredential);
                            } catch (UnsupportedSecretTypeException e) {
                                handleUpdateCredentialByAdminFailure(
                                        ErrorMessages.ERROR_CODE_UNSUPPORTED_CREDENTIAL_TYPE.getCode(),
                                        ErrorMessages.ERROR_CODE_UNSUPPORTED_CREDENTIAL_TYPE.getMessage() + " " + e
                                                .getMessage(), userName, newCredential);
                                throw new UserStoreException(
                                        ErrorMessages.ERROR_CODE_UNSUPPORTED_CREDENTIAL_TYPE.toString(), e);
                            }
                        }
                    }
                }
            } catch (UserStoreException ex) {
                handleUpdateCredentialByAdminFailure(
                        ErrorMessages.ERROR_CODE_ERROR_DURING_PRE_UPDATE_CREDENTIAL_BY_ADMIN.getCode(),
                        String.format(ErrorMessages.ERROR_CODE_ERROR_DURING_PRE_UPDATE_CREDENTIAL_BY_ADMIN.getMessage(),
                                ex.getMessage()), userName, newCredential);
                throw ex;
            }
            // #################### </Listeners> #####################################################

            if (!checkUserPasswordValid(newCredential)) {
                String errorMsg = realmConfig.getUserStoreProperty(PROPERTY_PASSWORD_ERROR_MSG);

                if (errorMsg != null) {
                    String errorCode = ErrorMessages.ERROR_CODE_ERROR_DURING_PRE_UPDATE_CREDENTIAL_BY_ADMIN.getCode();
                    String errorMessage = String
                            .format(ErrorMessages.ERROR_CODE_ERROR_DURING_PRE_UPDATE_CREDENTIAL_BY_ADMIN.getMessage(),
                                    errorMsg);
                    handleUpdateCredentialByAdminFailure(errorCode, errorMessage, userName, newCredential);
                    throw new UserStoreException(errorCode + " - " + errorMessage);
                }

                String errorCode = ErrorMessages.ERROR_CODE_INVALID_PASSWORD.getCode();
                String errorMessage = String.format(ErrorMessages.ERROR_CODE_INVALID_PASSWORD.getMessage(),
                        realmConfig.getUserStoreProperty(UserCoreConstants.RealmConfig.PROPERTY_JAVA_REG_EX));
                handleUpdateCredentialByAdminFailure(errorCode, errorMessage, userName, newCredential);
                throw new UserStoreException(errorCode + " - " + errorMessage);
            }

            if (!doCheckExistingUser(userStore.getDomainFreeName())) {
                String errorMessage = String.format(ErrorMessages.ERROR_CODE_NON_EXISTING_USER.getMessage(), userName,
                        realmConfig.getUserStoreProperty(UserCoreConstants.RealmConfig.PROPERTY_DOMAIN_NAME));
                String errorCode = ErrorMessages.ERROR_CODE_NON_EXISTING_USER.getCode();
                handleUpdateCredentialByAdminFailure(errorCode, errorMessage, userName, newCredential);
                throw new UserStoreException(errorCode + "-" + errorMessage);
            }

            try {
                doUpdateCredentialByAdmin(userName, newCredentialObj);
            } catch (UserStoreException ex) {
                handleUpdateCredentialByAdminFailure(
                        ErrorMessages.ERROR_CODE_ERROR_WHILE_UPDATING_CREDENTIAL_BY_ADMIN.getCode(),
                        String.format(ErrorMessages.ERROR_CODE_ERROR_WHILE_UPDATING_CREDENTIAL_BY_ADMIN.getMessage(),
                                ex.getMessage()), userName, newCredentialObj);
                throw ex;
            }

            // #################### <Listeners> #####################################################
            try {
                for (UserOperationEventListener listener : UMListenerServiceComponent
                        .getUserOperationEventListeners()) {
                    Object credentialArgument;
                    if (listener instanceof SecretHandleableListener) {
                        credentialArgument = newCredentialObj;
                    } else {
                        credentialArgument = newCredential;
                    }

                    if (!listener.doPostUpdateCredentialByAdmin(userName, credentialArgument, this)) {
                        handleUpdateCredentialByAdminFailure(
                                ErrorMessages.ERROR_CODE_ERROR_DURING_POST_UPDATE_CREDENTIAL_BY_ADMIN.getCode(),
                                String.format(ErrorMessages.ERROR_CODE_ERROR_DURING_POST_UPDATE_CREDENTIAL_BY_ADMIN
                                        .getMessage(), UserCoreErrorConstants.POST_LISTENER_TASKS_FAILED_MESSAGE),
                                userName, newCredential);
                        return;
                    }
                }
            } catch (UserStoreException ex) {
                handleUpdateCredentialByAdminFailure(
                        ErrorMessages.ERROR_CODE_ERROR_DURING_POST_UPDATE_CREDENTIAL_BY_ADMIN.getCode(), String.format(
                                ErrorMessages.ERROR_CODE_ERROR_DURING_POST_UPDATE_CREDENTIAL_BY_ADMIN.getMessage(),
                                ex.getMessage()), userName, newCredential);
                throw ex;
            }
        } finally {
            newCredentialObj.clear();
        }
        // #################### </Listeners> #####################################################

    }

    /**
     * Get the attribute for the provided claim uri and identifier.
     *
     * @param claimURI
     * @param identifier user name or role.
     * @param domainName TODO
     * @return claim attribute value. NULL if attribute is not defined for the
     * claim uri
     * @throws org.wso2.micro.integrator.security.user.api.UserStoreException
     */
    protected String getClaimAtrribute(String claimURI, String identifier, String domainName)
            throws org.wso2.micro.integrator.security.user.api.UserStoreException {
        domainName =
                (domainName == null || domainName.isEmpty())
                        ? (identifier.indexOf(UserCoreConstants.DOMAIN_SEPARATOR) > -1
                        ? identifier.split(UserCoreConstants.DOMAIN_SEPARATOR)[0]
                        : realmConfig.getUserStoreProperty(UserStoreConfigConstants.DOMAIN_NAME))
                        : domainName;
        String attributeName = null;
        if (domainName != null && !domainName.equals(UserStoreConfigConstants.PRIMARY)) {
            attributeName = claimManager.getAttributeName(domainName, claimURI);
        }
        if (attributeName == null || attributeName.isEmpty()) {
            attributeName = claimManager.getAttributeName(claimURI);
        }

        if (attributeName == null) {
            if (UserCoreConstants.PROFILE_CONFIGURATION.equals(claimURI)) {
                attributeName = claimURI;
            } else if (DISAPLAY_NAME_CLAIM.equals(claimURI)) {
                attributeName = this.realmConfig.getUserStoreProperty(LDAPConstants.DISPLAY_NAME_ATTRIBUTE);
            } else {
                throw new UserStoreException("Mapped attribute cannot be found for claim : " + claimURI + " in user " +
                        "store : " + getMyDomainName());
            }
        }

        return attributeName;
    }

    /**
     * This method handles the follow up actions when there is a failure while deleting a user.
     *
     * @param errorCode    Relevant error code.
     * @param errorMessage Relevant error message.
     * @param userName     Name of the user.
     * @throws UserStoreException User Store Exception that could be thrown while doing follow-up actions.
     */
    private void handleDeleteUserFailure(String errorCode, String errorMessage, String userName)
            throws UserStoreException {

        for (UserManagementErrorEventListener listener : UMListenerServiceComponent
                .getUserManagementErrorEventListeners()) {
            if (listener.isEnable() && !listener.onDeleteUserFailure(errorCode, errorMessage, userName, this)) {
                return;
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    public final void deleteUser(String userName) throws UserStoreException {
        // TODO Review whether we need to perform this operation
        if (!isSecureCall.get()) {
            Class argTypes[] = new Class[]{String.class};
            callSecure("deleteUser", new Object[]{userName}, argTypes);
            return;
        }

        UserStore userStore = getUserStore(userName);
        if (userStore.isRecurssive()) {
            userStore.getUserStoreManager().deleteUser(userStore.getDomainFreeName());
            return;
        }

        // #################### Domain Name Free Zone Starts Here ################################

        if (UserCoreUtil.isPrimaryAdminUser(userName, realmConfig)) {
            handleDeleteUserFailure(ErrorMessages.ERROR_CODE_DELETE_ADMIN_USER.getCode(),
                    ErrorMessages.ERROR_CODE_DELETE_ADMIN_USER.getMessage(), userName);
            throw new UserStoreException(ErrorMessages.ERROR_CODE_DELETE_ADMIN_USER.toString());
        }

        if (UserCoreUtil.isRegistryAnnonymousUser(userName)) {
            handleDeleteUserFailure(ErrorMessages.ERROR_CODE_DELETE_ANONYMOUS_USER.getCode(),
                    ErrorMessages.ERROR_CODE_DELETE_ANONYMOUS_USER.getMessage(), userName);
            throw new UserStoreException(ErrorMessages.ERROR_CODE_DELETE_ANONYMOUS_USER.toString());
        }

        if (isReadOnly()) {
            handleDeleteUserFailure(ErrorMessages.ERROR_CODE_READONLY_USER_STORE.getCode(),
                    ErrorMessages.ERROR_CODE_READONLY_USER_STORE.getMessage(), userName);
            throw new UserStoreException(ErrorMessages.ERROR_CODE_READONLY_USER_STORE.toString());
        }

        // #################### <Listeners> #####################################################
        try {
            for (UserStoreManagerListener listener : UMListenerServiceComponent.getUserStoreManagerListeners()) {
                if (!listener.deleteUser(userName, this)) {
                    handleDeleteUserFailure(ErrorMessages.ERROR_CODE_ERROR_DURING_PRE_DELETE_USER.getCode(),
                            String.format(ErrorMessages.ERROR_CODE_ERROR_DURING_PRE_DELETE_USER.getMessage(),
                                    UserCoreErrorConstants.PRE_LISTENER_TASKS_FAILED_MESSAGE), userName);
                    return;
                }
            }
            for (UserOperationEventListener listener : UMListenerServiceComponent.getUserOperationEventListeners()) {
                if (!listener.doPreDeleteUser(userName, this)) {
                    handleDeleteUserFailure(ErrorMessages.ERROR_CODE_ERROR_DURING_PRE_DELETE_USER.getCode(),
                            String.format(ErrorMessages.ERROR_CODE_ERROR_DURING_PRE_DELETE_USER.getMessage(),
                                    UserCoreErrorConstants.PRE_LISTENER_TASKS_FAILED_MESSAGE), userName);

                    return;
                }
            }
        } catch (UserStoreException e) {
            handleDeleteUserFailure(ErrorMessages.ERROR_CODE_ERROR_DURING_PRE_DELETE_USER.getCode(),
                    String.format(ErrorMessages.ERROR_CODE_ERROR_DURING_PRE_DELETE_USER.getMessage(), e.getMessage()),
                    userName);
            throw e;
        }
        // #################### </Listeners> #####################################################

        if (!doCheckExistingUser(userName)) {
            String errorMessage = String.format(ErrorMessages.ERROR_CODE_NON_EXISTING_USER.getMessage(), userName,
                    realmConfig.getUserStoreProperty(UserCoreConstants.RealmConfig.PROPERTY_DOMAIN_NAME));
            String errorCode = ErrorMessages.ERROR_CODE_NON_EXISTING_USER.getCode();
            handleDeleteUserFailure(errorCode, errorMessage, userName);
            throw new UserStoreException(errorCode + " - " + errorMessage);
        }

        // Remove users from internal role mapping
        try {
            hybridRoleManager.deleteUser(UserCoreUtil.addDomainToName(userName, getMyDomainName()));

            doDeleteUser(userName);
        } catch (UserStoreException e) {
            handleDeleteUserFailure(ErrorMessages.ERROR_CODE_ERROR_WHILE_DELETING_USER.getCode(),
                    String.format(ErrorMessages.ERROR_CODE_ERROR_WHILE_DELETING_USER.getMessage(), e.getMessage()),
                    userName);
            throw e;
        }

        // #################### <Listeners> #####################################################
        try {
            for (UserOperationEventListener listener : UMListenerServiceComponent.getUserOperationEventListeners()) {
                if (!listener.doPostDeleteUser(userName, this)) {
                    handleDeleteUserFailure(ErrorMessages.ERROR_CODE_ERROR_DURING_POST_DELETE_USER.getCode(),
                            String.format(ErrorMessages.ERROR_CODE_ERROR_DURING_POST_DELETE_USER.getMessage(),
                                    UserCoreErrorConstants.POST_LISTENER_TASKS_FAILED_MESSAGE), userName);
                    return;
                }
            }
        } catch (UserStoreException ex) {
            handleDeleteUserFailure(ErrorMessages.ERROR_CODE_ERROR_DURING_POST_DELETE_USER.getCode(),
                    String.format(ErrorMessages.ERROR_CODE_ERROR_DURING_POST_DELETE_USER.getMessage(), ex.getMessage()),
                    userName);
            throw ex;
        }
        // #################### </Listeners> #####################################################

    }

    /**
     * This is method is to call the relevant listeners when there is a failure while setting user claim value.
     *
     * @param errorCode    Error code.
     * @param errorMessage Error message.
     * @param userName     Name of the user.
     * @param claimURI     Claim URI.
     * @param claimValue   Claim Value.
     * @param profileName  Name of the profile.
     * @throws UserStoreException UserStore Exception that would be thrown within the listeners.
     */
    private void handleSetUserClaimValueFailure(String errorCode, String errorMessage, String userName, String claimURI,
            String claimValue, String profileName) throws UserStoreException {

        for (UserManagementErrorEventListener listener : UMListenerServiceComponent
                .getUserManagementErrorEventListeners()) {
            if (listener.isEnable() && !listener
                    .onSetUserClaimValueFailure(errorCode, errorMessage, userName, claimURI, claimValue, profileName,
                            this)) {
                return;
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    public final void setUserClaimValue(String userName, String claimURI, String claimValue,
                                        String profileName) throws UserStoreException {

        UserStore userStore = getUserStore(userName);
        if (userStore.isRecurssive()) {
            userStore.getUserStoreManager().setUserClaimValue(userStore.getDomainFreeName(),
                    claimURI, claimValue, profileName);
            return;
        }

        // #################### Domain Name Free Zone Starts Here ################################

        if (!doCheckExistingUser(userName)) {
            String errorMessage = String.format(ErrorMessages.ERROR_CODE_NON_EXISTING_USER.getMessage(), userName,
                    realmConfig.getUserStoreProperty(UserCoreConstants.RealmConfig.PROPERTY_DOMAIN_NAME));
            String errorCode = ErrorMessages.ERROR_CODE_NON_EXISTING_USER.getCode();
            handleSetUserClaimValueFailure(errorCode, errorMessage, userName, claimURI, claimValue, profileName);
            throw new UserStoreException(errorCode + " - " + errorMessage);
        }

        // #################### <Listeners> #####################################################
        try {
            for (UserOperationEventListener listener : UMListenerServiceComponent.getUserOperationEventListeners()) {
                if (!listener.doPreSetUserClaimValue(userName, claimURI, claimValue, profileName, this)) {
                    handleSetUserClaimValueFailure(
                            ErrorMessages.ERROR_CODE_ERROR_DURING_PRE_SET_USER_CLAIM_VALUE.getCode(),
                            String.format(ErrorMessages.ERROR_CODE_ERROR_DURING_PRE_SET_USER_CLAIM_VALUE.getMessage(),
                                    UserCoreErrorConstants.PRE_LISTENER_TASKS_FAILED_MESSAGE), userName, claimURI,
                            claimValue, profileName);
                    return;
                }
            }
        } catch (UserStoreException e) {
            handleSetUserClaimValueFailure(ErrorMessages.ERROR_CODE_ERROR_DURING_PRE_SET_USER_CLAIM_VALUE.getCode(),
                    String.format(ErrorMessages.ERROR_CODE_ERROR_DURING_PRE_SET_USER_CLAIM_VALUE.getMessage(),
                            e.getMessage()), userName, claimURI, claimValue, profileName);
            throw e;
        }
        // #################### </Listeners> #####################################################

        //Check userstore is readonly or not

        if (isReadOnly()) {
            handleSetUserClaimValueFailure(ErrorMessages.ERROR_CODE_READONLY_USER_STORE.getCode(),
                    ErrorMessages.ERROR_CODE_READONLY_USER_STORE.getMessage(), userName, claimURI, claimValue,
                    profileName);
            throw new UserStoreException(ErrorMessages.ERROR_CODE_READONLY_USER_STORE.toString());
        }

        try {
            doSetUserClaimValue(userName, claimURI, claimValue, profileName);
        } catch (UserStoreException e) {
            handleSetUserClaimValueFailure(ErrorMessages.ERROR_CODE_ERROR_WHILE_SETTING_USER_CLAIM_VALUE.getCode(),
                    String.format(ErrorMessages.ERROR_CODE_ERROR_WHILE_SETTING_USER_CLAIM_VALUE.getMessage(),
                            e.getMessage()), userName, claimURI, claimValue, profileName);
            throw e;
        }

        // #################### <Listeners> #####################################################
        try {
            for (UserOperationEventListener listener : UMListenerServiceComponent.getUserOperationEventListeners()) {
                if (!listener.doPostSetUserClaimValue(userName, this)) {
                    handleSetUserClaimValueFailure(
                            ErrorMessages.ERROR_CODE_ERROR_DURING_POST_SET_USER_CLAIM_VALUE.getCode(),
                            String.format(ErrorMessages.ERROR_CODE_ERROR_DURING_POST_SET_USER_CLAIM_VALUE.getMessage(),
                                    UserCoreErrorConstants.POST_LISTENER_TASKS_FAILED_MESSAGE), userName, claimURI,
                            claimValue, profileName);
                    return;
                }
            }
        } catch (UserStoreException e) {
            handleSetUserClaimValueFailure(ErrorMessages.ERROR_CODE_ERROR_DURING_POST_SET_USER_CLAIM_VALUE.getCode(),
                    String.format(ErrorMessages.ERROR_CODE_ERROR_DURING_POST_SET_USER_CLAIM_VALUE.getMessage(),
                            e.getMessage()), userName, claimURI, claimValue, profileName);
            throw e;
        }
        // #################### </Listeners> #####################################################

    }

    /**
     * This method is responsible for calling relevant methods when there is a failure while setting user claim values.
     *
     * @param errorCode    Error code.
     * @param errorMessage Error message.
     * @param userName     Name of the user.
     * @param claims       Relevant claims.
     * @param profileName  Name of the profile.
     * @throws UserStoreException Exception that will be thrown by relevant listeners.
     */
    private void handleSetUserClaimValuesFailure(String errorCode, String errorMessage, String userName,
            Map<String, String> claims, String profileName) throws UserStoreException {

        for (UserManagementErrorEventListener listener : UMListenerServiceComponent
                .getUserManagementErrorEventListeners()) {
            if (listener.isEnable() && !listener
                    .onSetUserClaimValuesFailure(errorCode, errorMessage, userName, claims, profileName, this)) {
                return;
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    public final void setUserClaimValues(String userName, Map<String, String> claims,
                                         String profileName) throws UserStoreException {

        UserStore userStore = getUserStore(userName);
        if (userStore.isRecurssive()) {
            userStore.getUserStoreManager().setUserClaimValues(userStore.getDomainFreeName(),
                    claims, profileName);
            return;
        }

        // #################### Domain Name Free Zone Starts Here ################################

        if (!doCheckExistingUser(userName)) {
            String errorMessage = String.format(ErrorMessages.ERROR_CODE_NON_EXISTING_USER.getMessage(), userName,
                    realmConfig.getUserStoreProperty(UserCoreConstants.RealmConfig.PROPERTY_DOMAIN_NAME));
            String errorCode = ErrorMessages.ERROR_CODE_NON_EXISTING_USER.getCode();
            handleSetUserClaimValuesFailure(errorCode, errorMessage, userName, claims, profileName);
            throw new UserStoreException(errorCode + " - " + errorMessage);
        }
        if (claims == null) {
            claims = new HashMap<>();
        }
        // #################### <Listeners> #####################################################
        try {
            for (UserOperationEventListener listener : UMListenerServiceComponent.getUserOperationEventListeners()) {
                if (!listener.doPreSetUserClaimValues(userName, claims, profileName, this)) {
                    handleSetUserClaimValuesFailure(
                            ErrorMessages.ERROR_CODE_ERROR_DURING_PRE_SET_USER_CLAIM_VALUES.getCode(),
                            String.format(ErrorMessages.ERROR_CODE_ERROR_DURING_PRE_SET_USER_CLAIM_VALUES.getMessage(),
                                    UserCoreErrorConstants.PRE_LISTENER_TASKS_FAILED_MESSAGE), userName, claims,
                            profileName);
                    return;
                }
            }
        } catch (UserStoreException e) {
            handleSetUserClaimValuesFailure(ErrorMessages.ERROR_CODE_ERROR_DURING_PRE_SET_USER_CLAIM_VALUES.getCode(),
                    String.format(ErrorMessages.ERROR_CODE_ERROR_DURING_PRE_SET_USER_CLAIM_VALUES.getMessage(),
                            e.getMessage()), userName, claims, profileName);
            throw e;
        }
        // #################### </Listeners> #####################################################

        //If user store is readonly this method should not get invoked with non empty claim set.

        if (isReadOnly() && !claims.isEmpty()) {
            handleSetUserClaimValuesFailure(ErrorMessages.ERROR_CODE_READONLY_USER_STORE.getCode(),
                    ErrorMessages.ERROR_CODE_READONLY_USER_STORE.getMessage(), userName, claims, profileName);
            throw new UserStoreException(ErrorMessages.ERROR_CODE_READONLY_USER_STORE.toString());
        }

        // set claim values if user store is not read only.

        try {
            if (!isReadOnly()) {
                doSetUserClaimValues(userName, claims, profileName);
            }
        } catch (UserStoreException e) {
            handleSetUserClaimValuesFailure(ErrorMessages.ERROR_CODE_ERROR_WHILE_SETTING_USER_CLAIM_VALUES.getCode(),
                    String.format(ErrorMessages.ERROR_CODE_ERROR_WHILE_SETTING_USER_CLAIM_VALUES.getMessage(),
                            e.getMessage()), userName, claims, profileName);
            throw e;
        }

        // #################### <Listeners> #####################################################
        try {
            for (UserOperationEventListener listener : UMListenerServiceComponent.getUserOperationEventListeners()) {
                if (!listener.doPostSetUserClaimValues(userName, claims, profileName, this)) {
                    handleSetUserClaimValuesFailure(
                            ErrorMessages.ERROR_CODE_ERROR_DURING_POST_SET_USER_CLAIM_VALUES.getCode(),
                            String.format(ErrorMessages.ERROR_CODE_ERROR_DURING_POST_SET_USER_CLAIM_VALUES.getMessage(),
                                    UserCoreErrorConstants.POST_LISTENER_TASKS_FAILED_MESSAGE), userName, claims,
                            profileName);
                    return;
                }
            }
        } catch (UserStoreException e) {
            handleSetUserClaimValuesFailure(ErrorMessages.ERROR_CODE_ERROR_DURING_POST_SET_USER_CLAIM_VALUES.getCode(),
                    String.format(ErrorMessages.ERROR_CODE_ERROR_DURING_POST_SET_USER_CLAIM_VALUES.getMessage(),
                            e.getMessage()), userName, claims, profileName);
            throw e;
        }
        // #################### </Listeners> #####################################################

    }

    /**
     * To handle the listener events when there is a failure while trying to delete the user  claim value.
     *
     * @param errorCode    Relevant error code.
     * @param errorMessage error message
     * @param userName     Name of the user.
     * @param claimURI     Claim URI
     * @param profileName  Name of the profile.
     * @throws UserStoreException Exception that will thrown from listeners.
     */
    private void handleDeleteUserClaimValueFailure(String errorCode, String errorMessage, String userName,
            String claimURI, String profileName) throws UserStoreException {

        for (UserManagementErrorEventListener listener : UMListenerServiceComponent
                .getUserManagementErrorEventListeners()) {
            if (listener.isEnable() && !listener
                    .onDeleteUserClaimValueFailure(errorCode, errorMessage, userName, claimURI, profileName, this)) {
                return;
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    public final void deleteUserClaimValue(String userName, String claimURI, String profileName)
            throws UserStoreException {

        if (!isSecureCall.get()) {
            Class argTypes[] = new Class[]{String.class, String.class, String.class};
            callSecure("deleteUserClaimValue", new Object[]{userName, claimURI, profileName}, argTypes);
            return;
        }

        UserStore userStore = getUserStore(userName);
        if (userStore.isRecurssive()) {
            userStore.getUserStoreManager().deleteUserClaimValue(userStore.getDomainFreeName(),
                    claimURI, profileName);
            return;
        }

        if (isReadOnly()) {
            handleDeleteUserClaimValueFailure(ErrorMessages.ERROR_CODE_READONLY_USER_STORE.getCode(),
                    ErrorMessages.ERROR_CODE_READONLY_USER_STORE.getMessage(), userName, claimURI, profileName);
            throw new UserStoreException(ErrorMessages.ERROR_CODE_READONLY_USER_STORE.toString());
        }

        if (!doCheckExistingUser(userName)) {
            String errorMessage = String.format(ErrorMessages.ERROR_CODE_NON_EXISTING_USER.getMessage(), userName,
                    realmConfig.getUserStoreProperty(UserCoreConstants.RealmConfig.PROPERTY_DOMAIN_NAME));
            String errorCode = ErrorMessages.ERROR_CODE_NON_EXISTING_USER.getCode();
            handleDeleteUserClaimValueFailure(errorCode, errorMessage, userName, claimURI, profileName);
            throw new UserStoreException(errorCode + " - " + errorMessage);
        }

        // #################### <Listeners> #####################################################
        try {
            for (UserOperationEventListener listener : UMListenerServiceComponent.getUserOperationEventListeners()) {
                if (!listener.doPreDeleteUserClaimValue(userName, claimURI, profileName, this)) {
                    handleDeleteUserClaimValueFailure(
                            ErrorMessages.ERROR_CODE_ERROR_DURING_PRE_DELETE_USER_CLAIM_VALUE.getCode(), String.format(
                                    ErrorMessages.ERROR_CODE_ERROR_DURING_PRE_DELETE_USER_CLAIM_VALUE.getMessage(),
                                    UserCoreErrorConstants.PRE_LISTENER_TASKS_FAILED_MESSAGE), userName, claimURI,
                            profileName);
                    return;
                }
            }
            // #################### </Listeners> #####################################################
        } catch (UserStoreException ex) {
            handleDeleteUserClaimValueFailure(
                    ErrorMessages.ERROR_CODE_ERROR_DURING_PRE_DELETE_USER_CLAIM_VALUE.getCode(),
                    String.format(ErrorMessages.ERROR_CODE_ERROR_DURING_PRE_DELETE_USER_CLAIM_VALUE.getMessage(),
                            ex.getMessage()), userName, claimURI, profileName);
            throw ex;
        }

        try {
            doDeleteUserClaimValue(userName, claimURI, profileName);
        } catch (UserStoreException ex) {
            handleDeleteUserClaimValueFailure(ErrorMessages.ERROR_CODE_ERROR_WHILE_DELETING_USER_CLAIM_VALUE.getCode(),
                    String.format(ErrorMessages.ERROR_CODE_ERROR_WHILE_DELETING_USER_CLAIM_VALUE.getMessage(),
                            ex.getMessage()), userName, claimURI, profileName);
            throw ex;
        }

        // #################### <Listeners> #####################################################
        try {
            for (UserOperationEventListener listener : UMListenerServiceComponent.getUserOperationEventListeners()) {
                if (!listener.doPostDeleteUserClaimValue(userName, this)) {
                    handleDeleteUserClaimValueFailure(
                            ErrorMessages.ERROR_CODE_ERROR_DURING_POST_DELETE_USER_CLAIM_VALUE.getCode(), String.format(
                                    ErrorMessages.ERROR_CODE_ERROR_DURING_POST_DELETE_USER_CLAIM_VALUE.getMessage(),
                                    UserCoreErrorConstants.POST_LISTENER_TASKS_FAILED_MESSAGE), userName, claimURI,
                            profileName);
                    return;
                }
            }
        } catch (UserStoreException ex) {
            handleDeleteUserClaimValueFailure(
                    ErrorMessages.ERROR_CODE_ERROR_DURING_POST_DELETE_USER_CLAIM_VALUE.getCode(),
                    String.format(ErrorMessages.ERROR_CODE_ERROR_DURING_POST_DELETE_USER_CLAIM_VALUE.getMessage(),
                            ex.getMessage()), userName, claimURI, profileName);
            throw ex;
        }
        // #################### </Listeners> #####################################################
    }

    /**
     * This method handles a failure when trying to delete user claim values.
     *
     * @param errorCode    Error code.
     * @param errorMessage Error message
     * @param userName     Name of the user.
     * @param claims       Claims
     * @param profileName  Name of the profile.
     * @throws UserStoreException User Store Exception that will be thrown from the relevant listeners.
     */
    private void handleDeleteUserClaimValuesFailure(String errorCode, String errorMessage, String userName,
            String[] claims, String profileName) throws UserStoreException {

        for (UserManagementErrorEventListener listener : UMListenerServiceComponent
                .getUserManagementErrorEventListeners()) {
            if (listener.isEnable() && !listener
                    .onDeleteUserClaimValuesFailure(errorCode, errorMessage, userName, claims, profileName, this)) {
                return;
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    public final void deleteUserClaimValues(String userName, String[] claims, String profileName)
            throws UserStoreException {

        if (!isSecureCall.get()) {
            Class argTypes[] = new Class[] { String.class, String[].class, String.class };
            callSecure("deleteUserClaimValues", new Object[] { userName, claims, profileName }, argTypes);
            return;
        }

        UserStore userStore = getUserStore(userName);
        if (userStore.isRecurssive()) {
            userStore.getUserStoreManager().deleteUserClaimValues(userStore.getDomainFreeName(), claims, profileName);
            return;
        }

        if (isReadOnly()) {
            handleDeleteUserClaimValuesFailure(ErrorMessages.ERROR_CODE_READONLY_USER_STORE.getCode(),
                    ErrorMessages.ERROR_CODE_READONLY_USER_STORE.getMessage(), userName, claims, profileName);
            throw new UserStoreException(ErrorMessages.ERROR_CODE_READONLY_USER_STORE.toString());
        }

        if (!doCheckExistingUser(userName)) {
            String errorMessage = String.format(ErrorMessages.ERROR_CODE_NON_EXISTING_USER.getMessage(), userName,
                    realmConfig.getUserStoreProperty(UserCoreConstants.RealmConfig.PROPERTY_DOMAIN_NAME));
            String errorCode = ErrorMessages.ERROR_CODE_NON_EXISTING_USER.getCode();
            handleDeleteUserClaimValuesFailure(errorCode, errorMessage, userName, claims, profileName);
            throw new UserStoreException(errorCode + " - " + errorMessage);
        }

        if (claims == null) {
            claims = new String[0];
        }
        // #################### <Listeners> #####################################################
        try {
            for (UserOperationEventListener listener : UMListenerServiceComponent.getUserOperationEventListeners()) {
                if (!listener.doPreDeleteUserClaimValues(userName, claims, profileName, this)) {
                    handleDeleteUserClaimValuesFailure(
                            ErrorMessages.ERROR_CODE_ERROR_DURING_PRE_DELETE_USER_CLAIM_VALUES.getCode(), String.format(
                                    ErrorMessages.ERROR_CODE_ERROR_DURING_PRE_DELETE_USER_CLAIM_VALUES.getMessage(),
                                    UserCoreErrorConstants.PRE_LISTENER_TASKS_FAILED_MESSAGE), userName, claims,
                            profileName);
                    return;
                }
            }
            // #################### </Listeners> #####################################################
        } catch (UserStoreException ex) {
            handleDeleteUserClaimValuesFailure(
                    ErrorMessages.ERROR_CODE_ERROR_DURING_PRE_DELETE_USER_CLAIM_VALUES.getCode(),
                    String.format(ErrorMessages.ERROR_CODE_ERROR_DURING_PRE_DELETE_USER_CLAIM_VALUES.getMessage(),
                            ex.getMessage()), userName, claims, profileName);
            throw ex;
        }

        try {
            doDeleteUserClaimValues(userName, claims, profileName);
        } catch (UserStoreException ex) {
            handleDeleteUserClaimValuesFailure(
                    ErrorMessages.ERROR_CODE_ERROR_WHILE_DELETING_USER_CLAIM_VALUES.getCode(),
                    String.format(ErrorMessages.ERROR_CODE_ERROR_WHILE_DELETING_USER_CLAIM_VALUES.getMessage(),
                            ex.getMessage()), userName, claims, profileName);
            throw ex;
        }
        // #################### <Listeners> #####################################################
        try {
            for (UserOperationEventListener listener : UMListenerServiceComponent.getUserOperationEventListeners()) {
                if (!listener.doPostDeleteUserClaimValues(userName, this)) {
                    handleDeleteUserClaimValuesFailure(
                            ErrorMessages.ERROR_CODE_ERROR_DURING_POST_DELETE_USER_CLAIM_VALUES.getCode(),
                            String.format(
                                    ErrorMessages.ERROR_CODE_ERROR_DURING_POST_DELETE_USER_CLAIM_VALUES.getMessage(),
                                    UserCoreErrorConstants.POST_LISTENER_TASKS_FAILED_MESSAGE), userName, claims,
                            profileName);
                    return;
                }
            }
        } catch (UserStoreException ex) {
            handleDeleteUserClaimValuesFailure(
                    ErrorMessages.ERROR_CODE_ERROR_DURING_POST_DELETE_USER_CLAIM_VALUES.getCode(),
                    String.format(ErrorMessages.ERROR_CODE_ERROR_DURING_POST_DELETE_USER_CLAIM_VALUES.getMessage(),
                            ex.getMessage()), userName, claims, profileName);
            throw ex;
        }
        // #################### </Listeners> #####################################################

    }

    /**
     * {@inheritDoc}
     */
    public final void addUser(String userName, Object credential, String[] roleList,
                              Map<String, String> claims, String profileName, boolean requirePasswordChange)
            throws UserStoreException {

        if (!isSecureCall.get()) {
            Class argTypes[] = new Class[]{String.class, Object.class, String[].class, Map.class, String.class,
                    boolean.class};
            callSecure("addUser", new Object[]{userName, credential, roleList, claims, profileName,
                    requirePasswordChange}, argTypes);
            return;
        }

        if (StringUtils.isEmpty(userName)) {
            String regEx = realmConfig
                    .getUserStoreProperty(UserCoreConstants.RealmConfig.PROPERTY_USER_NAME_JAVA_REG_EX);
            //Inorder to support both UsernameJavaRegEx and UserNameJavaRegEx.
            if (StringUtils.isEmpty(regEx) || StringUtils.isEmpty(regEx.trim())) {
                regEx = realmConfig.getUserStoreProperty(UserCoreConstants.RealmConfig.PROPERTY_USER_NAME_JAVA_REG);
            }
            String message = String.format(ErrorMessages.ERROR_CODE_INVALID_USER_NAME.getMessage(), null, regEx);
            String errorCode = ErrorMessages.ERROR_CODE_INVALID_USER_NAME.getCode();
            handleAddUserFailure(errorCode, message, null, credential, roleList, claims, profileName);
            throw new UserStoreException(errorCode + " - " + message);
        }

        UserStore userStore = getUserStore(userName);
        if (userStore.isRecurssive()) {
            userStore.getUserStoreManager()
                    .addUser(userStore.getDomainFreeName(), credential, roleList, claims, profileName,
                            requirePasswordChange);
            return;
        }

        Secret credentialObj;
        try {
            credentialObj = Secret.getSecret(credential);
        } catch (UnsupportedSecretTypeException e) {
            handleAddUserFailure(ErrorMessages.ERROR_CODE_UNSUPPORTED_CREDENTIAL_TYPE.getCode(),
                    ErrorMessages.ERROR_CODE_UNSUPPORTED_CREDENTIAL_TYPE.getMessage(), userName, credential, roleList,
                    claims, profileName);
            throw new UserStoreException(ErrorMessages.ERROR_CODE_UNSUPPORTED_CREDENTIAL_TYPE.toString(), e);
        }

        try {
            if (userStore.isSystemStore()) {
                systemUserRoleManager.addSystemUser(userName, credentialObj, roleList);
                return;
            }

            // #################### Domain Name Free Zone Starts Here ################################

            if (isReadOnly()) {
                handleAddUserFailure(ErrorMessages.ERROR_CODE_READONLY_USER_STORE.getCode(),
                        ErrorMessages.ERROR_CODE_READONLY_USER_STORE.getMessage(), userName, credential, roleList,
                        claims, profileName);
                throw new UserStoreException(ErrorMessages.ERROR_CODE_READONLY_USER_STORE.toString());
            }
            // This happens only once during first startup - adding administrator user/role.
            if (userName.indexOf(UserCoreConstants.DOMAIN_SEPARATOR) > 0) {
                userName = userStore.getDomainFreeName();
                roleList = UserCoreUtil.removeDomainFromNames(roleList);
            }
            if (roleList == null) {
                roleList = new String[0];
            }
            if (claims == null) {
                claims = new HashMap<>();
            }
            // #################### <Listeners> #####################################################
            try {
                for (UserStoreManagerListener listener : UMListenerServiceComponent.getUserStoreManagerListeners()) {
                    Object credentialArgument;
                    if (listener instanceof SecretHandleableListener) {
                        credentialArgument = credentialObj;
                    } else {
                        credentialArgument = credential;
                    }

                    if (!listener.addUser(userName, credentialArgument, roleList, claims, profileName, this)) {
                        handleAddUserFailure(ErrorMessages.ERROR_CODE_ERROR_DURING_PRE_ADD_USER.getCode(),
                                String.format(ErrorMessages.ERROR_CODE_ERROR_DURING_PRE_ADD_USER.getMessage(),
                                        UserCoreErrorConstants.PRE_LISTENER_TASKS_FAILED_MESSAGE), userName, credential,
                                roleList, claims, profileName);
                        return;
                    }
                }
            } catch (UserStoreException ex) {
                handleAddUserFailure(ErrorMessages.ERROR_CODE_ERROR_DURING_PRE_ADD_USER.getCode(),
                        String.format(ErrorMessages.ERROR_CODE_ERROR_DURING_PRE_ADD_USER.getMessage(), ex.getMessage()),
                        userName, credential, roleList, claims, profileName);
                throw ex;
            }

            // String buffers are used to let listeners to modify passwords
            for (UserOperationEventListener listener : UMListenerServiceComponent.getUserOperationEventListeners()) {
                if (listener instanceof SecretHandleableListener) {
                    try {
                        if (!listener.doPreAddUser(userName, credentialObj, roleList, claims, profileName, this)) {
                            handleAddUserFailure(ErrorMessages.ERROR_CODE_ERROR_DURING_PRE_ADD_USER.getCode(),
                                    String.format(ErrorMessages.ERROR_CODE_ERROR_DURING_PRE_ADD_USER.getMessage(),
                                            UserCoreErrorConstants.PRE_LISTENER_TASKS_FAILED_MESSAGE), userName,
                                    credential, roleList, claims, profileName);
                            return;
                        }
                    } catch (UserStoreException ex) {
                        String message = String.format(ErrorMessages.ERROR_CODE_ERROR_DURING_PRE_ADD_USER.getMessage(),
                                ex.getMessage());
                        handleAddUserFailure(ErrorMessages.ERROR_CODE_ERROR_DURING_PRE_ADD_USER.getCode(), message,
                                userName, credential, roleList, claims, profileName);
                        throw ex;
                    }
                } else {
                    // String buffers are used to let listeners to modify passwords
                    StringBuffer credBuff = null;
                    if (credential == null) { // a default password will be set
                        credBuff = new StringBuffer();
                    } else if (credential instanceof String) {
                        credBuff = new StringBuffer((String) credential);
                    }

                    if (credBuff != null) {
                        try {
                            if (!listener.doPreAddUser(userName, credBuff, roleList, claims, profileName, this)) {
                                handleAddUserFailure(ErrorMessages.ERROR_CODE_ERROR_DURING_PRE_ADD_USER.getCode(),
                                        String.format(ErrorMessages.ERROR_CODE_ERROR_DURING_PRE_ADD_USER.getMessage(),
                                                UserCoreErrorConstants.PRE_LISTENER_TASKS_FAILED_MESSAGE), userName,
                                        credential, roleList, claims, profileName);
                                return;
                            }
                        } catch (UserStoreException e) {
                            handleAddUserFailure(ErrorMessages.ERROR_CODE_ERROR_DURING_PRE_ADD_USER.getCode(),
                                    String.format(ErrorMessages.ERROR_CODE_ERROR_DURING_PRE_ADD_USER.getMessage(),
                                            e.getMessage()), userName, credential, roleList, claims, profileName);
                            throw e;
                        }
                        // reading the modified value
                        credential = credBuff.toString();
                        credentialObj.clear();
                        try {
                            credentialObj = Secret.getSecret(credential);
                        } catch (UnsupportedSecretTypeException e) {
                            handleAddUserFailure(ErrorMessages.ERROR_CODE_UNSUPPORTED_CREDENTIAL_TYPE.getCode(),
                                    ErrorMessages.ERROR_CODE_UNSUPPORTED_CREDENTIAL_TYPE.getMessage(), userName,
                                    credential, roleList, claims, profileName);
                            throw new UserStoreException(
                                    ErrorMessages.ERROR_CODE_UNSUPPORTED_CREDENTIAL_TYPE.toString(), e);
                        }
                    }
                }
            }

            if (!checkUserNameValid(userStore.getDomainFreeName())) {
                String regEx = realmConfig
                        .getUserStoreProperty(UserCoreConstants.RealmConfig.PROPERTY_USER_NAME_JAVA_REG_EX);
                //Inorder to support both UsernameJavaRegEx and UserNameJavaRegEx.
                if (StringUtils.isEmpty(regEx) || StringUtils.isEmpty(regEx.trim())) {
                    regEx = realmConfig.getUserStoreProperty(UserCoreConstants.RealmConfig.PROPERTY_USER_NAME_JAVA_REG);
                }
                String message = String
                        .format(ErrorMessages.ERROR_CODE_INVALID_USER_NAME.getMessage(), userStore.getDomainFreeName(),
                                regEx);
                String errorCode = ErrorMessages.ERROR_CODE_INVALID_USER_NAME.getCode();
                handleAddUserFailure(errorCode, message, userName, credential, roleList, claims, profileName);
                throw new UserStoreException(errorCode + " - " + message);
            }

            if (!checkUserPasswordValid(credentialObj)) {
                String regEx = realmConfig.getUserStoreProperty(UserCoreConstants.RealmConfig.PROPERTY_JAVA_REG_EX);
                String message = String.format(ErrorMessages.ERROR_CODE_INVALID_PASSWORD.getMessage(), regEx);
                String errorCode = ErrorMessages.ERROR_CODE_INVALID_PASSWORD.getCode();
                handleAddUserFailure(errorCode, message, userName, credential, roleList, claims, profileName);
                throw new UserStoreException(errorCode + " - " + message);
            }

            if (doCheckExistingUser(userStore.getDomainFreeName())) {
                String message = String.format(ErrorMessages.ERROR_CODE_USER_ALREADY_EXISTS.getMessage(), userName);
                String errorCode = ErrorMessages.ERROR_CODE_USER_ALREADY_EXISTS.getCode();
                handleAddUserFailure(errorCode, message, userName, credential, roleList, claims, profileName);
                throw new UserAlreadyExistsException(errorCode + " - " + message);
            }

            List<String> internalRoles = new ArrayList<String>();
            List<String> externalRoles = new ArrayList<String>();
            int index;
            if (roleList != null) {
                for (String role : roleList) {
                    if (role != null && role.trim().length() > 0) {
                        index = role.indexOf(UserCoreConstants.DOMAIN_SEPARATOR);
                        if (index > 0) {
                            String domain = role.substring(0, index);
                            if (UserCoreConstants.INTERNAL_DOMAIN.equalsIgnoreCase(domain)) {
                                internalRoles.add(UserCoreUtil.removeDomainFromName(role));
                                continue;
                            } else if (APPLICATION_DOMAIN.equalsIgnoreCase(domain) || WORKFLOW_DOMAIN
                                    .equalsIgnoreCase(domain)) {
                                internalRoles.add(role);
                                continue;
                            }
                        }
                        externalRoles.add(UserCoreUtil.removeDomainFromName(role));
                    }
                }
            }

            // check existence of roles and claims before adding user
            for (String internalRole : internalRoles) {
                if (!hybridRoleManager.isExistingRole(internalRole)) {
                    String message = String
                            .format(ErrorMessages.ERROR_CODE_INTERNAL_ROLE_NOT_EXISTS.getMessage(), internalRole);
                    String errorCode = ErrorMessages.ERROR_CODE_INTERNAL_ROLE_NOT_EXISTS.getCode();
                    handleAddUserFailure(errorCode, message, userName, credential, roleList, claims, profileName);
                    throw new UserStoreException(errorCode + " - " + message);
                }
            }

            for (String externalRole : externalRoles) {
                if (!doCheckExistingRole(externalRole)) {
                    String message = String
                            .format(ErrorMessages.ERROR_CODE_EXTERNAL_ROLE_NOT_EXISTS.getMessage(), externalRole);
                    String errorCode = ErrorMessages.ERROR_CODE_EXTERNAL_ROLE_NOT_EXISTS.getCode();
                    handleAddUserFailure(errorCode, message, userName, credential, roleList, claims, profileName);
                    throw new UserStoreException(errorCode + " - " + message);
                }
            }

            if (claims != null) {
                for (Map.Entry<String, String> entry : claims.entrySet()) {
                    ClaimMapping claimMapping;
                    try {
                        claimMapping = (ClaimMapping) claimManager.getClaimMapping(entry.getKey());
                    } catch (org.wso2.micro.integrator.security.user.api.UserStoreException e) {
                        String errorMessage = String
                                .format(ErrorMessages.ERROR_CODE_UNABLE_TO_FETCH_CLAIM_MAPPING.getMessage(),
                                        "persisting user attributes.");
                        String errorCode = ErrorMessages.ERROR_CODE_UNABLE_TO_FETCH_CLAIM_MAPPING.getCode();
                        handleAddUserFailure(errorCode, errorMessage, userName, credential, roleList, claims,
                                profileName);
                        throw new UserStoreException(errorCode + " - " + errorMessage, e);
                    }
                    if (claimMapping == null) {
                        String errorMessage = String
                                .format(ErrorMessages.ERROR_CODE_INVALID_CLAIM_URI.getMessage(), entry.getKey());
                        String errorCode = ErrorMessages.ERROR_CODE_INVALID_CLAIM_URI.getCode();
                        handleAddUserFailure(errorCode, errorMessage, userName, credential, roleList, claims,
                                profileName);
                        throw new UserStoreException(errorCode + " - " + errorMessage);
                    }
                }
            }

            try {
                doAddUser(userName, credentialObj, externalRoles.toArray(new String[externalRoles.size()]), claims,
                        profileName, requirePasswordChange);
            } catch (UserStoreException ex) {
                handleAddUserFailure(ErrorMessages.ERROR_CODE_ERROR_WHILE_ADDING_USER.getCode(),
                        String.format(ErrorMessages.ERROR_CODE_ERROR_WHILE_ADDING_USER.getMessage(), ex.getMessage()),
                        userName, credential, roleList, claims, profileName);
                throw ex;
            }

            if (internalRoles.size() > 0) {
                hybridRoleManager.updateHybridRoleListOfUser(userName, null,
                        internalRoles.toArray(new String[internalRoles.size()]));
            }

            try {
                for (UserOperationEventListener listener : UMListenerServiceComponent
                        .getUserOperationEventListeners()) {
                    Object credentialArgument;
                    if (listener instanceof SecretHandleableListener) {
                        credentialArgument = credentialObj;
                    } else {
                        credentialArgument = credential;
                    }

                    if (!listener.doPostAddUser(userName, credentialArgument, roleList, claims, profileName, this)) {
                        handleAddUserFailure(ErrorMessages.ERROR_CODE_ERROR_DURING_POST_ADD_USER.getCode(),
                                String.format(ErrorMessages.ERROR_CODE_ERROR_DURING_POST_ADD_USER.getMessage(),
                                        UserCoreErrorConstants.POST_LISTENER_TASKS_FAILED_MESSAGE), userName,
                                credential, roleList, claims, profileName);
                        return;
                    }
                }
            } catch (UserStoreException ex) {
                handleAddUserFailure(ErrorMessages.ERROR_CODE_ERROR_DURING_POST_ADD_USER.getCode(),
                        String.format(ErrorMessages.ERROR_CODE_ERROR_DURING_POST_ADD_USER.getMessage(),
                                ex.getMessage()), userName, credential, roleList, claims, profileName);
                throw ex;
            }
        } finally {
            credentialObj.clear();
        }

    }

    /**
     * To handle the erroneous scenario in add user flow.
     *
     * @param errorCode    Relevant error code.
     * @param errorMessage Relevant error message.
     * @param userName     Name of the user.
     * @param credential   Credential
     * @param roleList     List of roles.
     * @param claims       Claims
     * @param profileName  Name of the profile.
     * @throws UserStoreException UserStore Exception that could be thrown during the execution.
     */
    private void handleAddUserFailure(String errorCode, String errorMessage, String userName, Object credential,
            String[] roleList, Map<String, String> claims, String profileName) throws UserStoreException {

        for (UserManagementErrorEventListener listener : UMListenerServiceComponent
                .getUserManagementErrorEventListeners()) {
            if (listener.isEnable() && !listener
                    .onAddUserFailure(errorCode, errorMessage, userName, credential, roleList, claims, profileName,
                            this)) {
                return;
            }
        }
    }


    /**
     * {@inheritDoc}
     */
    public void addUser(String userName, Object credential, String[] roleList,
                        Map<String, String> claims, String profileName) throws UserStoreException {
        this.addUser(userName, credential, roleList, claims, profileName, false);
    }

    public final void updateUserListOfRole(final String roleName, final String[] deletedUsers, final String[] newUsers)
            throws UserStoreException {
        try {
            AccessController.doPrivileged(new PrivilegedExceptionAction<String>() {
                @Override
                public String run() throws Exception {
                    updateUserListOfRoleInternal(roleName, deletedUsers, newUsers);
                    return null;
                }
            });
        } catch (PrivilegedActionException e) {
            if (!(e.getException() instanceof UserStoreException)) {
                handleUpdateUserListOfRoleFailure(ErrorMessages.ERROR_CODE_ERROR_WHILE_UPDATING_ROLE_OF_USER.getCode(),
                        String.format(ErrorMessages.ERROR_CODE_ERROR_WHILE_UPDATING_ROLE_OF_USER.getMessage(),
                                e.getMessage()), roleName, deletedUsers, newUsers);
            }
            throw (UserStoreException) e.getException();
        }
    }

    /**
     * This method calls the relevant listeners that handle failure during update user list of a role.
     *
     * @param errorCode    Relevant error code.
     * @param errorMessage Relevant error message.
     * @param roleName     Name of the role.
     * @param deletedUsers Removed users from a particular role.
     * @param newUsers     Added users from a particular role.
     * @throws UserStoreException Exception that will be thrown from the relevant listeners.
     */
    private void handleUpdateUserListOfRoleFailure(String errorCode, String errorMessage, String roleName,
            String deletedUsers[], String[] newUsers) throws UserStoreException {

        for (UserManagementErrorEventListener listener : UMListenerServiceComponent
                .getUserManagementErrorEventListeners()) {
            if (listener.isEnable() && !listener
                    .onUpdateUserListOfRoleFailure(errorCode, errorMessage, roleName, deletedUsers, newUsers, this)) {
                return;
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    private final void updateUserListOfRoleInternal(String roleName, String[] deletedUsers, String[] newUsers)
            throws UserStoreException {

        String primaryDomain = getMyDomainName();
        if (primaryDomain != null) {
            primaryDomain += UserCoreConstants.DOMAIN_SEPARATOR;
        }

        if (deletedUsers != null && deletedUsers.length > 0) {
            Arrays.sort(deletedUsers);
            // Updating the user list of a role belong to the primary domain.
            if (UserCoreUtil.isPrimaryAdminRole(roleName, realmConfig)) {
                for (int i = 0; i < deletedUsers.length; i++) {
                    if (deletedUsers[i].equalsIgnoreCase(realmConfig.getAdminUserName()) || (primaryDomain
                            + deletedUsers[i]).equalsIgnoreCase(realmConfig.getAdminUserName())) {
                        handleUpdateUserListOfRoleFailure(
                                ErrorMessages.ERROR_CODE_CANNOT_REMOVE_ADMIN_ROLE_FROM_ADMIN.getCode(),
                                ErrorMessages.ERROR_CODE_CANNOT_REMOVE_ADMIN_ROLE_FROM_ADMIN.getMessage(), roleName,
                                deletedUsers, newUsers);
                        throw new UserStoreException(
                                ErrorMessages.ERROR_CODE_CANNOT_REMOVE_ADMIN_ROLE_FROM_ADMIN.toString());
                    }

                }
            }
        }

        UserStore userStore = getUserStore(roleName);

        if (userStore.isHybridRole()) {
            // Check whether someone is trying to update Everyone role.
            if (UserCoreUtil.isEveryoneRole(roleName, realmConfig)) {
                handleUpdateUserListOfRoleFailure(ErrorMessages.ERROR_CODE_CANNOT_UPDATE_EVERYONE_ROLE.getCode(),
                        ErrorMessages.ERROR_CODE_CANNOT_UPDATE_EVERYONE_ROLE.getMessage(), roleName, deletedUsers,
                        newUsers);
                throw new UserStoreException(ErrorMessages.ERROR_CODE_CANNOT_UPDATE_EVERYONE_ROLE.toString());
            }

            if (UserCoreConstants.INTERNAL_DOMAIN.equalsIgnoreCase(userStore.getDomainName())) {
                hybridRoleManager.updateUserListOfHybridRole(userStore.getDomainFreeName(), deletedUsers, newUsers);
                handleDoPostUpdateUserListOfRole(roleName, deletedUsers, newUsers, true);
            } else {
                hybridRoleManager.updateUserListOfHybridRole(userStore.getDomainAwareName(), deletedUsers, newUsers);
                handleDoPostUpdateUserListOfRole(roleName, deletedUsers, newUsers, true);
            }
            return;
        }

        if (userStore.isSystemStore()) {
            systemUserRoleManager.updateUserListOfSystemRole(userStore.getDomainFreeName(),
                    UserCoreUtil.removeDomainFromNames(deletedUsers),
                    UserCoreUtil.removeDomainFromNames(newUsers));
            handleDoPostUpdateUserListOfRole(roleName, deletedUsers, newUsers, true);
            return;
        }

        if (userStore.isRecurssive()) {
            userStore.getUserStoreManager().updateUserListOfRole(userStore.getDomainFreeName(),
                    UserCoreUtil.removeDomainFromNames(deletedUsers),
                    UserCoreUtil.removeDomainFromNames(newUsers));
            return;
        }

        // #################### Domain Name Free Zone Starts Here ################################
        if (deletedUsers == null) {
            deletedUsers = new String[0];
        }
        if (newUsers == null) {
            newUsers = new String[0];
        }
        // #################### <Listeners> #####################################################
        try {
            for (UserOperationEventListener listener : UMListenerServiceComponent.getUserOperationEventListeners()) {
                if (!listener.doPreUpdateUserListOfRole(roleName, deletedUsers, newUsers, this)) {
                    handleUpdateUserListOfRoleFailure(
                            ErrorMessages.ERROR_CODE_ERROR_WHILE_PRE_UPDATE_USERS_OF_ROLE.getCode(),
                            String.format(ErrorMessages.ERROR_CODE_ERROR_WHILE_PRE_UPDATE_USERS_OF_ROLE.getMessage(),
                                    UserCoreErrorConstants.PRE_LISTENER_TASKS_FAILED_MESSAGE), roleName, deletedUsers,
                            newUsers);
                    return;
                }
            }
        } catch (UserStoreException ex) {
            handleUpdateUserListOfRoleFailure(ErrorMessages.ERROR_CODE_ERROR_WHILE_PRE_UPDATE_USERS_OF_ROLE.getCode(),
                    String.format(ErrorMessages.ERROR_CODE_ERROR_WHILE_PRE_UPDATE_USERS_OF_ROLE.getMessage(),
                            ex.getMessage()), roleName, deletedUsers, newUsers);
            throw ex;
        }
        // #################### </Listeners> #####################################################

        if ((deletedUsers != null && deletedUsers.length > 0) || (newUsers != null && newUsers.length > 0)) {
            if (!isReadOnly() && writeGroupsEnabled) {
                try {
                    doUpdateUserListOfRole(userStore.getDomainFreeName(),
                            UserCoreUtil.removeDomainFromNames(deletedUsers),
                            UserCoreUtil.removeDomainFromNames(newUsers));
                } catch (UserStoreException ex) {
                    handleUpdateUserListOfRoleFailure(
                            ErrorMessages.ERROR_CODE_ERROR_DURING_UPDATE_USERS_OF_ROLE.getCode(),
                            String.format(ErrorMessages.ERROR_CODE_ERROR_DURING_UPDATE_USERS_OF_ROLE.getMessage(),
                                    ex.getMessage()), roleName, deletedUsers, newUsers);
                    throw ex;
                }
            } else {
                handleUpdateUserListOfRoleFailure(ErrorMessages.ERROR_CODE_READONLY_USER_STORE.getCode(),
                        ErrorMessages.ERROR_CODE_READONLY_USER_STORE.getMessage(), roleName, deletedUsers, newUsers);
                throw new UserStoreException(ErrorMessages.ERROR_CODE_READONLY_USER_STORE.toString());
            }
        }

        // Call relevant listeners after updating user list of role.
        handleDoPostUpdateUserListOfRole(roleName, deletedUsers, newUsers, false);
    }

    /**
     * This method is responsible for calling the listeners after updating user list of role.
     *
     * @param roleName       Name of the role.
     * @param deletedUsers   Removed users
     * @param newUsers       Added users.
     * @param isAuditLogOnly Indicate whether to call only the audit log listener.
     * @throws UserStoreException Exception that will be thrown by relevant listeners.
     */
    private void handleDoPostUpdateUserListOfRole(String roleName, String[] deletedUsers, String[] newUsers,
            boolean isAuditLogOnly) throws UserStoreException {

        try {
            for (UserOperationEventListener listener : UMListenerServiceComponent.getUserOperationEventListeners()) {
                if (isAuditLogOnly && !listener.getClass().getName()
                        .endsWith(UserCoreErrorConstants.AUDIT_LOGGER_CLASS_NAME)) {
                    continue;
                }
                if (!listener.doPostUpdateUserListOfRole(roleName, deletedUsers, newUsers, this)) {
                    return;
                }
            }
        } catch (UserStoreException ex) {
            handleUpdateUserListOfRoleFailure(ErrorMessages.ERROR_CODE_ERROR_WHILE_POST_UPDATE_USERS_OF_ROLE.getCode(),
                    String.format(ErrorMessages.ERROR_CODE_ERROR_WHILE_POST_UPDATE_USERS_OF_ROLE.getMessage(),
                            ex.getMessage()), roleName, deletedUsers, newUsers);
            throw ex;
        }
    }

    public final void updateRoleListOfUser(final String username, final String[] deletedRoles, final String[] newRoles)
            throws UserStoreException {
        try {
            AccessController.doPrivileged(new PrivilegedExceptionAction<String>() {
                @Override
                public String run() throws Exception {
                    updateRoleListOfUserInternal(username, deletedRoles, newRoles);
                    return null;
                }
            });
        } catch (PrivilegedActionException e) {
            if (!(e.getException() instanceof UserStoreException)) {
                handleUpdateRoleListOfUserFailure(ErrorMessages.ERROR_CODE_ERROR_DURING_UPDATE_USERS_OF_ROLE.getCode(),
                        String.format(ErrorMessages.ERROR_CODE_ERROR_DURING_UPDATE_USERS_OF_ROLE.getMessage(),
                                e.getMessage()), username, deletedRoles, newRoles);
            }
            throw (UserStoreException) e.getException();
        }
    }

    /**
     * This method is responsible for calling the methods of listeners after a failure while trying to update role
     * list of users.
     *
     * @param errorCode    Error code.
     * @param errorMessage Error message.
     * @param userName     User Name
     * @param deletedRoles Removed roles
     * @param newRoles     Assigned roles
     * @throws UserStoreException Exception that will be thrown by relevant listeners.
     */
    private void handleUpdateRoleListOfUserFailure(String errorCode, String errorMessage, String userName,
            String[] deletedRoles, String[] newRoles) throws UserStoreException {

        for (UserManagementErrorEventListener listener : UMListenerServiceComponent
                .getUserManagementErrorEventListeners()) {
            if (listener.isEnable() && !listener
                    .onUpdateRoleListOfUserFailure(errorCode, errorMessage, userName, deletedRoles, newRoles, this)) {
                return;
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    private final void updateRoleListOfUserInternal(String userName, String[] deletedRoles, String[] newRoles)
            throws UserStoreException {

        String primaryDomain = realmConfig
                .getUserStoreProperty(UserCoreConstants.RealmConfig.PROPERTY_DOMAIN_NAME);
        if (primaryDomain != null) {
            primaryDomain += UserCoreConstants.DOMAIN_SEPARATOR;
        }

        if (deletedRoles != null && deletedRoles.length > 0) {
            Arrays.sort(deletedRoles);
            if (UserCoreUtil.isPrimaryAdminUser(userName, realmConfig)) {
                for (int i = 0; i < deletedRoles.length; i++) {
                    if (deletedRoles[i].equalsIgnoreCase(realmConfig.getAdminRoleName()) || (primaryDomain
                            + deletedRoles[i]).equalsIgnoreCase(realmConfig.getAdminRoleName())) {
                        handleUpdateRoleListOfUserFailure(
                                ErrorMessages.ERROR_CODE_CANNOT_REMOVE_ADMIN_ROLE_FROM_ADMIN.getCode(),
                                ErrorMessages.ERROR_CODE_CANNOT_REMOVE_ADMIN_ROLE_FROM_ADMIN.getMessage(), userName,
                                deletedRoles, newRoles);
                        throw new UserStoreException(
                                ErrorMessages.ERROR_CODE_CANNOT_REMOVE_ADMIN_ROLE_FROM_ADMIN.toString());
                    }
                }
            }
        }

        UserStore userStore = getUserStore(userName);
        if (userStore.isRecurssive()) {
            userStore.getUserStoreManager().updateRoleListOfUser(userStore.getDomainFreeName(),
                    UserCoreUtil.removeDomainFromNames(deletedRoles),
                    UserCoreUtil.removeDomainFromNames(newRoles));
            return;
        }

        if (userStore.isSystemStore()) {
            systemUserRoleManager.updateSystemRoleListOfUser(userStore.getDomainFreeName(),
                    UserCoreUtil.removeDomainFromNames(deletedRoles),
                    UserCoreUtil.removeDomainFromNames(newRoles));
            return;
        }

        // #################### Domain Name Free Zone Starts Here ################################
        if (deletedRoles == null) {
            deletedRoles = new String[0];
        }
        if (newRoles == null) {
            newRoles = new String[0];
        }
        // This happens only once during first startup - adding administrator user/role.
        if (userName.indexOf(UserCoreConstants.DOMAIN_SEPARATOR) > 0) {
            userName = userStore.getDomainFreeName();
            deletedRoles = UserCoreUtil.removeDomainFromNames(deletedRoles);
            newRoles = UserCoreUtil.removeDomainFromNames(newRoles);
        }

        List<String> internalRoleDel = new ArrayList<String>();
        List<String> internalRoleNew = new ArrayList<String>();

        List<String> roleDel = new ArrayList<String>();
        List<String> roleNew = new ArrayList<String>();

        if (deletedRoles != null && deletedRoles.length > 0) {
            for (String deleteRole : deletedRoles) {
                if (UserCoreUtil.isEveryoneRole(deleteRole, realmConfig)) {
                    handleUpdateRoleListOfUserFailure(ErrorMessages.ERROR_CODE_CANNOT_UPDATE_EVERYONE_ROLE.getCode(),
                            ErrorMessages.ERROR_CODE_CANNOT_UPDATE_EVERYONE_ROLE.getMessage(), userName, deletedRoles,
                            newRoles);
                    throw new UserStoreException(ErrorMessages.ERROR_CODE_CANNOT_UPDATE_EVERYONE_ROLE.toString());
                }
                String domain = null;
                int index1 = deleteRole.indexOf(UserCoreConstants.DOMAIN_SEPARATOR);
                if (index1 > 0) {
                    domain = deleteRole.substring(0, index1);
                }
                if (APPLICATION_DOMAIN.equalsIgnoreCase(domain) || WORKFLOW_DOMAIN.equalsIgnoreCase(domain)) {
                    internalRoleDel.add(deleteRole);
                } else if (UserCoreConstants.INTERNAL_DOMAIN.equalsIgnoreCase(domain) || this.isReadOnly()) {
                    internalRoleDel.add(UserCoreUtil.removeDomainFromName(deleteRole));
                } else {
                    // This is domain free role name.
                    roleDel.add(UserCoreUtil.removeDomainFromName(deleteRole));
                }
            }
            deletedRoles = roleDel.toArray(new String[roleDel.size()]);
        }

        if (newRoles != null && newRoles.length > 0) {
            for (String newRole : newRoles) {
                if (UserCoreUtil.isEveryoneRole(newRole, realmConfig)) {
                    handleUpdateRoleListOfUserFailure(ErrorMessages.ERROR_CODE_CANNOT_UPDATE_EVERYONE_ROLE.getCode(),
                            ErrorMessages.ERROR_CODE_CANNOT_UPDATE_EVERYONE_ROLE.getMessage(), userName, deletedRoles,
                            newRoles);
                    throw new UserStoreException(ErrorMessages.ERROR_CODE_CANNOT_UPDATE_EVERYONE_ROLE.toString());
                }
                String domain = null;
                int index2 = newRole.indexOf(UserCoreConstants.DOMAIN_SEPARATOR);
                if (index2 > 0) {
                    domain = newRole.substring(0, index2);
                }

                if (UserCoreConstants.INTERNAL_DOMAIN.equalsIgnoreCase(domain)) {
                    // If this is an internal role.
                    internalRoleNew.add(UserCoreUtil.removeDomainFromName(newRole));
                } else if (APPLICATION_DOMAIN.equalsIgnoreCase(domain) || WORKFLOW_DOMAIN.equalsIgnoreCase(domain)) {
                    // If this is an application role or workflow role.
                    internalRoleNew.add(newRole);
                } else if (this.isReadOnly()) {
                    // If this is a readonly user store, we add even normal roles as internal roles.
                    internalRoleNew.add(UserCoreUtil.removeDomainFromName(newRole));
                } else {
                    roleNew.add(UserCoreUtil.removeDomainFromName(newRole));
                }
            }
            newRoles = roleNew.toArray(new String[roleNew.size()]);
        }

        if (internalRoleDel.size() > 0 || internalRoleNew.size() > 0) {
            hybridRoleManager.updateHybridRoleListOfUser(userStore.getDomainFreeName(),
                    internalRoleDel.toArray(new String[internalRoleDel.size()]),
                    internalRoleNew.toArray(new String[internalRoleNew.size()]));
        }

        // #################### <Listeners> #####################################################
        try {
            for (UserOperationEventListener listener : UMListenerServiceComponent.getUserOperationEventListeners()) {
                if (!listener.doPreUpdateRoleListOfUser(userName, deletedRoles, newRoles, this)) {
                    handleUpdateRoleListOfUserFailure(
                            ErrorMessages.ERROR_CODE_ERROR_DURING_PRE_UPDATE_ROLE_OF_USER.getCode(),
                            String.format(ErrorMessages.ERROR_CODE_ERROR_DURING_PRE_UPDATE_ROLE_OF_USER.getMessage(),
                                    UserCoreErrorConstants.PRE_LISTENER_TASKS_FAILED_MESSAGE), userName, deletedRoles,
                            newRoles);
                    return;
                }
            }
        } catch (UserStoreException ex) {
            handleUpdateRoleListOfUserFailure(ErrorMessages.ERROR_CODE_ERROR_DURING_PRE_UPDATE_ROLE_OF_USER.getCode(),
                    String.format(ErrorMessages.ERROR_CODE_ERROR_DURING_PRE_UPDATE_ROLE_OF_USER.getMessage(),
                            ex.getMessage()), userName, deletedRoles, newRoles);
            throw ex;
        }
        // #################### </Listeners> #####################################################

        if ((deletedRoles != null && deletedRoles.length > 0) || (newRoles != null && newRoles.length > 0)) {
            if (!isReadOnly() && writeGroupsEnabled) {
                try {
                    doUpdateRoleListOfUser(userName, deletedRoles, newRoles);
                } catch (UserStoreException ex) {
                    handleUpdateRoleListOfUserFailure(
                            ErrorMessages.ERROR_CODE_ERROR_WHILE_UPDATING_ROLE_OF_USER.getCode(),
                            String.format(ErrorMessages.ERROR_CODE_ERROR_WHILE_UPDATING_ROLE_OF_USER.getMessage(),
                                    ex.getMessage()), userName, deletedRoles, newRoles);
                    throw ex;
                }
            } else {
                handleUpdateRoleListOfUserFailure(ErrorMessages.ERROR_CODE_READONLY_USER_STORE.getCode(),
                        ErrorMessages.ERROR_CODE_READONLY_USER_STORE.getMessage(), userName, deletedRoles, newRoles);
                throw new UserStoreException(ErrorMessages.ERROR_CODE_READONLY_USER_STORE.toString());
            }
        }

        // Call the relevant listeners after updating the role list of user.
        try {
            for (UserOperationEventListener listener : UMListenerServiceComponent.getUserOperationEventListeners()) {
                if (!listener.doPostUpdateRoleListOfUser(userName, deletedRoles, newRoles, this)) {
                    handleUpdateRoleListOfUserFailure(
                            ErrorMessages.ERROR_CODE_ERROR_DURING_POST_UPDATE_ROLE_OF_USER.getCode(),
                            String.format(ErrorMessages.ERROR_CODE_ERROR_DURING_POST_UPDATE_ROLE_OF_USER.getMessage(),
                                    UserCoreErrorConstants.POST_LISTENER_TASKS_FAILED_MESSAGE), userName, deletedRoles,
                            newRoles);
                    return;
                }
            }
        } catch (UserStoreException ex) {
            handleUpdateRoleListOfUserFailure(ErrorMessages.ERROR_CODE_ERROR_DURING_POST_UPDATE_ROLE_OF_USER.getCode(),
                    String.format(ErrorMessages.ERROR_CODE_ERROR_DURING_POST_UPDATE_ROLE_OF_USER.getMessage(),
                            ex.getMessage()), userName, deletedRoles, newRoles);
            throw ex;
        }
    }

    /**
     * This method is responsible for calling the relevant methods when there is a failure while trying to update the
     * role name.
     *
     * @param errorCode    Error code.
     * @param errorMessage Error message
     * @param roleName     Role Name
     * @param newRoleName  New Role Name
     * @throws UserStoreException Exception that will be thrown by relevant methods in listener.
     */
    private void handleUpdateRoleNameFailure(String errorCode, String errorMessage, String roleName, String newRoleName)
            throws UserStoreException {

        for (UserManagementErrorEventListener listener : UMListenerServiceComponent
                .getUserManagementErrorEventListeners()) {
            if (listener.isEnable() && !listener
                    .onUpdateRoleNameFailure(errorCode, errorMessage, roleName, newRoleName, this)) {
                return;
            }
        }
    }

    /**
     * This method is responsible for calling post update role name methods in listeners.
     *
     * @param roleName       Name of the role.
     * @param newRoleName    New role name
     * @param isAuditLogOnly to indicate whether to call only the audit log listener.
     * @throws UserStoreException Exception that will be thrown by relevant listeners.
     */
    private void handlePostUpdateRoleName(String roleName, String newRoleName, boolean isAuditLogOnly)
            throws UserStoreException {

        try {
            boolean internalRole = isAnInternalRole(roleName);
            for (UserOperationEventListener listener : UMListenerServiceComponent.getUserOperationEventListeners()) {
                if (isAuditLogOnly && !listener.getClass().getName()
                        .endsWith(UserCoreErrorConstants.AUDIT_LOGGER_CLASS_NAME)) {
                    continue;
                }

                boolean success = false;
                if (internalRole && listener instanceof AbstractUserOperationEventListener) {
                    success = ((AbstractUserOperationEventListener) listener).doPostUpdateInternalRoleName(roleName,
                            newRoleName, this);
                } else if (internalRole && !(listener instanceof AbstractUserOperationEventListener)) {
                    success = true;
                } else if (!internalRole) {
                    success = listener.doPostUpdateRoleName(roleName, newRoleName, this);
                }

                if (!success) {
                    handleUpdateRoleNameFailure(ErrorMessages.ERROR_CODE_ERROR_DURING_POST_UPDATE_ROLE_NAME.getCode(),
                            String.format(ErrorMessages.ERROR_CODE_ERROR_DURING_POST_UPDATE_ROLE_NAME.getMessage(),
                                    UserCoreErrorConstants.POST_LISTENER_TASKS_FAILED_MESSAGE), roleName, newRoleName);
                    return;
                }
            }
        } catch (UserStoreException ex) {
            handleUpdateRoleNameFailure(ErrorMessages.ERROR_CODE_ERROR_DURING_POST_UPDATE_ROLE_NAME.getCode(),
                    String.format(ErrorMessages.ERROR_CODE_ERROR_DURING_POST_UPDATE_ROLE_NAME.getMessage(),
                            ex.getMessage()), roleName, newRoleName);
            throw ex;
        }
    }

    /**
     * This method is responsible for calling pre update role name methods in listeners.
     *
     * @param roleName       Name of the internal role.
     * @param newRoleName    New internal role name
     * @param isAuditLogOnly to indicate whether to call only the audit log listener.
     * @throws UserStoreException Exception that will be thrown by relevant listeners.
     */
    private boolean handlePreUpdateRoleName(String roleName, String newRoleName, boolean isAuditLogOnly)
            throws UserStoreException {

        try {
            boolean internalRole = isAnInternalRole(roleName);
            for (UserOperationEventListener listener : UMListenerServiceComponent.getUserOperationEventListeners()) {
                if (isAuditLogOnly && !listener.getClass().getName()
                        .endsWith(UserCoreErrorConstants.AUDIT_LOGGER_CLASS_NAME)) {
                    continue;
                }

                boolean success = false;
                if (internalRole && listener instanceof AbstractUserOperationEventListener) {
                    success = ((AbstractUserOperationEventListener) listener).doPreUpdateInternalRoleName(roleName,
                            newRoleName, this);
                }
                if (internalRole && !(listener instanceof AbstractUserOperationEventListener)) {
                    success = true;
                } else if (!internalRole) {
                    success = listener.doPreUpdateRoleName(roleName, newRoleName, this);
                }

                if (!success) {
                    handleUpdateRoleNameFailure(ErrorMessages.ERROR_CODE_ERROR_DURING_PRE_UPDATE_ROLE_NAME.getCode(),
                            String.format(ErrorMessages.ERROR_CODE_ERROR_DURING_PRE_UPDATE_ROLE_NAME.getMessage(),
                                    UserCoreErrorConstants.PRE_LISTENER_TASKS_FAILED_MESSAGE), roleName, newRoleName);
                    return false;
                }
            }
        } catch (UserStoreException ex) {
            handleUpdateRoleNameFailure(ErrorMessages.ERROR_CODE_ERROR_DURING_PRE_UPDATE_ROLE_NAME.getCode(),
                    String.format(ErrorMessages.ERROR_CODE_ERROR_DURING_PRE_UPDATE_ROLE_NAME.getMessage(),
                            ex.getMessage()), roleName, newRoleName);
            throw ex;
        }
        return true;
    }

    /**
     * {@inheritDoc}
     */
    public final void updateRoleName(String roleName, String newRoleName) throws UserStoreException {

        if (!isSecureCall.get()) {
            Class argTypes[] = new Class[]{String.class, String.class};
            callSecure("updateRoleName", new Object[]{roleName, newRoleName}, argTypes);
            return;
        }

        if (UserCoreUtil.isPrimaryAdminRole(newRoleName, realmConfig)) {
            handleUpdateRoleNameFailure(ErrorMessages.ERROR_CODE_CANNOT_UPDATE_ADMIN_ROLE.getCode(),
                    ErrorMessages.ERROR_CODE_CANNOT_UPDATE_ADMIN_ROLE.getMessage(), roleName, newRoleName);
            throw new UserStoreException(ErrorMessages.ERROR_CODE_CANNOT_UPDATE_ADMIN_ROLE.toString());
        }

        if (UserCoreUtil.isEveryoneRole(newRoleName, realmConfig)) {
            handleUpdateRoleNameFailure(ErrorMessages.ERROR_CODE_CANNOT_UPDATE_EVERYONE_ROLE.getCode(),
                    ErrorMessages.ERROR_CODE_CANNOT_UPDATE_EVERYONE_ROLE.getMessage(), roleName, newRoleName);
            throw new UserStoreException(ErrorMessages.ERROR_CODE_CANNOT_UPDATE_EVERYONE_ROLE.toString());
        }

        UserStore userStore = getUserStore(roleName);
        UserStore userStoreNew = getUserStore(newRoleName);

        if (!UserCoreUtil.canRoleBeRenamed(userStore, userStoreNew, realmConfig)) {
            handleUpdateRoleNameFailure(ErrorMessages.ERROR_CODE_CANNOT_RENAME_ROLE.getCode(),
                    ErrorMessages.ERROR_CODE_CANNOT_RENAME_ROLE.getMessage(), roleName, newRoleName);
            throw new UserStoreException(ErrorMessages.ERROR_CODE_CANNOT_RENAME_ROLE.toString());
        }

        if (userStore.isRecurssive()) {
            userStore.getUserStoreManager()
                    .updateRoleName(userStore.getDomainFreeName(), userStoreNew.getDomainFreeName());
            return;
        }

        // #################### Domain Name Free Zone Starts Here ################################

        if (userStore.isHybridRole()) {
            //Invoke pre listeners.
            if (!handlePreUpdateRoleName(roleName, newRoleName, false)) {
                return;
            }
            if (UserCoreConstants.INTERNAL_DOMAIN.equalsIgnoreCase(userStore.getDomainName())) {
                hybridRoleManager.updateHybridRoleName(userStore.getDomainFreeName(),
                        userStoreNew.getDomainFreeName());
            } else {
                hybridRoleManager.updateHybridRoleName(userStore.getDomainAwareName(),
                        userStoreNew.getDomainAwareName());
            }

            // This is a special case. We need to pass roles with domains.
//            userRealm.getAuthorizationManager().resetPermissionOnUpdateRole(
//                    userStore.getDomainAwareName(), userStoreNew.getDomainAwareName());

            // To make sure to maintain the back-ward compatibility, only audit log listener will be called.
            handlePostUpdateRoleName(roleName, newRoleName, false);
            return;
        }

        if (!isRoleNameValid(roleName)) {
            String regEx = realmConfig
                    .getUserStoreProperty(UserCoreConstants.RealmConfig.PROPERTY_ROLE_NAME_JAVA_REG_EX);
            String errorMessage = String
                    .format(ErrorMessages.ERROR_CODE_INVALID_ROLE_NAME.getMessage(), roleName, regEx);
            String errorCode = ErrorMessages.ERROR_CODE_INVALID_ROLE_NAME.getCode();
            handleUpdateRoleNameFailure(errorCode, errorMessage, roleName, newRoleName);
            throw new UserStoreException(errorCode + " - " + errorMessage);
        }

        if (isExistingRole(newRoleName)) {
            String errorMessage = String.format(ErrorMessages.ERROR_CODE_ROLE_ALREADY_EXISTS.getMessage(), newRoleName);
            String errorCode = ErrorMessages.ERROR_CODE_ROLE_ALREADY_EXISTS.getCode();
            handleUpdateRoleNameFailure(errorCode, errorMessage, roleName, newRoleName);
            throw new UserStoreException(errorCode + " - " + errorMessage);
        }

        // #################### <Listeners> #####################################################
        if (!handlePreUpdateRoleName(roleName, newRoleName, false)) {
            return;
        }
        // #################### </Listeners> #####################################################

        if (!isReadOnly() && writeGroupsEnabled) {
            try {
                doUpdateRoleName(userStore.getDomainFreeName(), userStoreNew.getDomainFreeName());
            } catch (UserStoreException ex) {
                handleUpdateRoleNameFailure(ErrorMessages.ERROR_CODE_ERROR_WHILE_UPDATING_ROLE_NAME.getCode(),
                        String.format(ErrorMessages.ERROR_CODE_ERROR_WHILE_UPDATING_ROLE_NAME.getMessage(),
                                ex.getMessage()), roleName, newRoleName);
            }
        } else {
            handleUpdateRoleNameFailure(ErrorMessages.ERROR_CODE_READONLY_USER_STORE.getCode(),
                    ErrorMessages.ERROR_CODE_READONLY_USER_STORE.getMessage(), roleName, newRoleName);
            throw new UserStoreException(ErrorMessages.ERROR_CODE_READONLY_USER_STORE.toString());
        }

        // This is a special case. We need to pass domain aware name.
//        userRealm.getAuthorizationManager().resetPermissionOnUpdateRole(
//                userStore.getDomainAwareName(), userStoreNew.getDomainAwareName());

        // #################### <Listeners> #####################################################
        handlePostUpdateRoleName(roleName, newRoleName, false);
        // #################### </Listeners> #####################################################

    }


    @Override
    public boolean isExistingRole(String roleName, boolean shared) throws org.wso2.micro.integrator.security.user.api.UserStoreException {
        if (shared) {
            return isExistingShareRole(roleName);
        } else {
            return isExistingRole(roleName);
        }
    }

    /**
     * {@inheritDoc}
     */
    public boolean isExistingRole(String roleName) throws UserStoreException {

        if (!isSecureCall.get()) {
            Class argTypes[] = new Class[]{String.class};
            Object object = callSecure("isExistingRole", new Object[]{roleName}, argTypes);
            return (Boolean) object;
        }

        UserStore userStore = getUserStore(roleName);

        if (userStore.isRecurssive()) {
            return userStore.getUserStoreManager().isExistingRole(userStore.getDomainFreeName());
        }

        // #################### Domain Name Free Zone Starts Here ################################

        if (userStore.isSystemStore()) {
            return systemUserRoleManager.isExistingRole(userStore.getDomainFreeName());
        }

        if (userStore.isHybridRole()) {
            boolean exist;

            if (!UserCoreConstants.INTERNAL_DOMAIN.equalsIgnoreCase(userStore.getDomainName())) {
                exist = hybridRoleManager.isExistingRole(userStore.getDomainAwareName());
            } else {
                exist = hybridRoleManager.isExistingRole(userStore.getDomainFreeName());
            }

            return exist;
        }

        // This happens only once during first startup - adding administrator user/role.
        roleName = userStore.getDomainFreeName();

        // you can not check existence of shared role using this method.
        if (isSharedGroupEnabled() && roleName.contains(UserCoreConstants.TENANT_DOMAIN_COMBINER)) {
            return false;
        }

        boolean isExisting = doCheckExistingRole(roleName);

        if (!isExisting && (isReadOnly() || !readGroupsEnabled)) {
            isExisting = hybridRoleManager.isExistingRole(roleName);
        }

        // systemUserRoleManager is not initialized
//        if (!isExisting) {
//            if (systemUserRoleManager.isExistingRole(roleName)) {
//                isExisting = true;
//            }
//        }

        return isExisting;
    }

//////////////////////////////////// Shared role APIs start //////////////////////////////////////////

    /**
     * TODO move to API
     *
     * @param roleName
     * @return
     * @throws UserStoreException
     */
    public boolean isExistingShareRole(String roleName) throws UserStoreException {

        if (!isSecureCall.get()) {
            Class argTypes[] = new Class[]{String.class};
            Object object = callSecure("isExistingShareRole", new Object[]{roleName}, argTypes);
            return (Boolean) object;
        }

        UserStoreManager manager = getUserStoreWithSharedRoles();

        if (manager == null) {
            throw new UserStoreException("Share Groups are not supported by this realm");
        }

        return ((AbstractUserStoreManager) manager).doCheckExistingRole(roleName);
    }

    /**
     * TODO  move to API
     *
     * @param roleName
     * @param deletedUsers
     * @param newUsers
     * @throws UserStoreException
     */
    public void updateUsersOfSharedRole(String roleName,
                                        String[] deletedUsers, String[] newUsers) throws UserStoreException {

        UserStoreManager manager = getUserStoreWithSharedRoles();

        if (manager == null) {
            throw new UserStoreException("Share Groups are not supported by this realm");
        }

        ((AbstractUserStoreManager) manager).doUpdateUserListOfRole(roleName, deletedUsers, newUsers);
    }

    /**
     * TODO move to API
     *
     * @return
     * @throws UserStoreException
     */
    public String[] getSharedRolesOfUser(String userName,
                                         String tenantDomain, String filter) throws UserStoreException {

        UserStore userStore = getUserStore(userName);
        UserStoreManager manager = userStore.getUserStoreManager();

        if (!((AbstractUserStoreManager) manager).isSharedGroupEnabled()) {
            throw new UserStoreException("Share Groups are not supported by user store");
        }

        String[] sharedRoles = ((AbstractUserStoreManager) manager).
                doGetSharedRoleListOfUser(userStore.getDomainFreeName(), tenantDomain, filter);
        return UserCoreUtil.removeDomainFromNames(sharedRoles);
    }

    /**
     * TODO move to API
     *
     * @return
     * @throws UserStoreException
     */
    public String[] getUsersOfSharedRole(String roleName, String filter) throws UserStoreException {

        UserStoreManager manager = getUserStoreWithSharedRoles();

        if (manager == null) {
            throw new UserStoreException("Share Groups are not supported by this realm");
        }

        String[] users = ((AbstractUserStoreManager) manager).doGetUserListOfRole(roleName, filter);
        return UserCoreUtil.removeDomainFromNames(users);
    }

    /**
     * TODO move to API
     *
     * @return
     * @throws UserStoreException
     */
    public String[] getSharedRoleNames(String tenantDomain, String filter,
                                       int maxItemLimit) throws UserStoreException {


        UserStoreManager manager = getUserStoreWithSharedRoles();

        if (manager == null) {
            throw new UserStoreException("Share Groups are not supported by this realm");
        }

        String[] sharedRoles = null;
        try {
            sharedRoles = ((AbstractUserStoreManager) manager).
                    doGetSharedRoleNames(tenantDomain, filter, maxItemLimit);
        } catch (UserStoreException e) {
            throw new UserStoreException("Error while retrieving shared roles", e);
        }
        return UserCoreUtil.removeDomainFromNames(sharedRoles);
    }


    /**
     * TODO move to API
     *
     * @return
     * @throws UserStoreException
     */
    public String[] getSharedRoleNames(String filter, int maxItemLimit) throws UserStoreException {

        UserStoreManager manager = getUserStoreWithSharedRoles();

        if (manager == null) {
            throw new UserStoreException("Share Groups are not supported by this realm");
        }

        String[] sharedRoles = null;
        try {
            sharedRoles = ((AbstractUserStoreManager) manager).
                    doGetSharedRoleNames(null, filter, maxItemLimit);
        } catch (UserStoreException e) {
            throw new UserStoreException("Error while retrieving shared roles", e);
        }
        return UserCoreUtil.removeDomainFromNames(sharedRoles);
    }


    public void addInternalRole(String roleName, String[] userList,
                                org.wso2.micro.integrator.security.user.api.Permission[] permission) throws UserStoreException {
        doAddInternalRole(roleName, userList, permission);
    }

    private UserStoreManager getUserStoreWithSharedRoles() throws UserStoreException {

        if (!isSecureCall.get()) {
            Class argTypes[] = new Class[]{};
            Object object = callSecure("getUserStoreWithSharedRoles", new Object[]{}, argTypes);
            return (UserStoreManager) object;
        }

        UserStoreManager sharedRoleManager = null;

        if (isSharedGroupEnabled()) {
            return this;
        }

        for (Map.Entry<String, UserStoreManager> entry : userStoreManagerHolder.entrySet()) {
            UserStoreManager manager = entry.getValue();
            if (manager != null && ((AbstractUserStoreManager) manager).isSharedGroupEnabled()) {
                if (sharedRoleManager != null) {
                    throw new UserStoreException("There cannot be more than one user store that support" +
                            "shared groups");
                }
                sharedRoleManager = manager;
            }
        }

        return sharedRoleManager;
    }

    /**
     * TODO move to API
     *
     * @param userName
     * @param roleName
     * @return
     * @throws UserStoreException
     */
    public boolean isUserInRole(String userName, String roleName) throws UserStoreException {

        if (!isSecureCall.get()) {
            Class argTypes[] = new Class[]{String.class, String.class};
            Object object = callSecure("isUserInRole", new Object[]{userName, roleName}, argTypes);
            return (Boolean) object;
        }

        if (roleName == null || roleName.trim().length() == 0 || userName == null ||
                userName.trim().length() == 0) {
            return false;
        }

        // anonymous user is always assigned to  anonymous role
        if (UserCoreConstants.REGISTRY_ANONNYMOUS_ROLE_NAME.equalsIgnoreCase(roleName) &&
                UserCoreConstants.REGISTRY_ANONNYMOUS_USERNAME.equalsIgnoreCase(userName)) {
            return true;
        }

        if (!UserCoreConstants.REGISTRY_ANONNYMOUS_USERNAME.equalsIgnoreCase(userName) &&
                realmConfig.getEveryOneRoleName().equalsIgnoreCase(roleName) &&
                !systemUserRoleManager.isExistingSystemUser(UserCoreUtil.
                        removeDomainFromName(userName))) {
            return true;
        }


        String[] roles = null;

        String modifiedUserName = UserCoreConstants.IS_USER_IN_ROLE_CACHE_IDENTIFIER + userName;
        if (UserCoreConstants.INTERNAL_DOMAIN.
                equalsIgnoreCase(UserCoreUtil.extractDomainFromName(roleName))
                || APPLICATION_DOMAIN.equalsIgnoreCase(UserCoreUtil.extractDomainFromName(roleName)) ||
                WORKFLOW_DOMAIN.equalsIgnoreCase(UserCoreUtil.extractDomainFromName(roleName))) {

            String[] internalRoles = doGetInternalRoleListOfUser(userName, roleName);
            if (UserCoreUtil.isContain(roleName, internalRoles)) {
                addToIsUserHasRole(modifiedUserName, roleName, roles);
                return true;
            }
        }

        UserStore userStore = getUserStore(userName);
        if (userStore.isRecurssive()
                && (userStore.getUserStoreManager() instanceof AbstractUserStoreManager)) {
            return ((AbstractUserStoreManager) userStore.getUserStoreManager()).isUserInRole(
                    userStore.getDomainFreeName(), roleName);
        }

        // #################### Domain Name Free Zone Starts Here ################################

        if (userStore.isSystemStore()) {
            return systemUserRoleManager.isUserInRole(userStore.getDomainFreeName(),
                    UserCoreUtil.removeDomainFromName(roleName));
        }
        // admin user is always assigned to admin role if it is in primary user store
        if (realmConfig.isPrimary() && roleName.equalsIgnoreCase(realmConfig.getAdminRoleName()) &&
                userName.equalsIgnoreCase(realmConfig.getAdminUserName())) {
            return true;
        }

        String roleDomainName = UserCoreUtil.extractDomainFromName(roleName);

        String roleDomainNameForForest = realmConfig.
                getUserStoreProperty(UserCoreConstants.RealmConfig.PROPERTY_GROUP_SEARCH_DOMAINS);
        if (roleDomainNameForForest != null && roleDomainNameForForest.trim().length() > 0) {
            String[] values = roleDomainNameForForest.split("#");
            for (String value : values) {
                if (value != null && !value.trim().equalsIgnoreCase(roleDomainName)) {
                    return false;
                }
            }
        } else if (!userStore.getDomainName().equalsIgnoreCase(roleDomainName)) {
            return false;
        }

        boolean success = false;
        if (readGroupsEnabled) {
            success = doCheckIsUserInRole(userStore.getDomainFreeName(),
                    UserCoreUtil.removeDomainFromName(roleName));
        }

        // add to cache
        if (success) {
            addToIsUserHasRole(modifiedUserName, roleName, roles);
        }
        return success;
    }

    /**
     * @param userName
     * @param roleName
     * @return
     * @throws UserStoreException
     */
    public abstract boolean doCheckIsUserInRole(String userName, String roleName) throws UserStoreException;

    /**
     * Helper method
     *
     * @param userName
     * @param roleName
     * @param currentRoles
     */
    private void addToIsUserHasRole(String userName, String roleName, String[] currentRoles) {
        List<String> roles;
        if (currentRoles != null) {
            roles = new ArrayList<String>(Arrays.asList(currentRoles));
        } else {
            roles = new ArrayList<String>();
        }
        roles.add(roleName);
    }

//////////////////////////////////// Shared role APIs finish //////////////////////////////////////////

    /**
     * {@inheritDoc}
     */
    public boolean isExistingUser(String userName) throws UserStoreException {

        if (!isSecureCall.get()) {
            Class argTypes[] = new Class[]{String.class};
            Object object = callSecure("isExistingUser", new Object[]{userName}, argTypes);
            return (Boolean) object;
        }

        if (UserCoreUtil.isRegistrySystemUser(userName)) {
            return true;
        }

        UserStore userStore = getUserStore(userName);
        if (userStore.isRecurssive()) {
            return userStore.getUserStoreManager().isExistingUser(userStore.getDomainFreeName());
        }

        // #################### Domain Name Free Zone Starts Here ################################

        if (userStore.isSystemStore()) {
            return systemUserRoleManager.isExistingSystemUser(userStore.getDomainFreeName());
        }


        return doCheckExistingUser(userStore.getDomainFreeName());

    }

    /**
     * {@inheritDoc}
     */
    public final String[] listUsers(String filter, int maxItemLimit) throws UserStoreException {

        if (!isSecureCall.get()) {
            Class argTypes[] = new Class[]{String.class, int.class};
            Object object = callSecure("listUsers", new Object[]{filter, maxItemLimit}, argTypes);
            return (String[]) object;
        }

        int index;
        index = filter.indexOf(UserCoreConstants.DOMAIN_SEPARATOR);
        String[] userList;

        // Check whether we have a secondary UserStoreManager setup.
        if (index > 0) {
            // Using the short-circuit. User name comes with the domain name.
            String domain = filter.substring(0, index);

            UserStoreManager secManager = getSecondaryUserStoreManager(domain);
            if (secManager != null) {
                // We have a secondary UserStoreManager registered for this domain.
                filter = filter.substring(index + 1);
                if (secManager instanceof AbstractUserStoreManager) {
                    userList = ((AbstractUserStoreManager) secManager).doListUsers(filter, maxItemLimit);
                    handlePostGetUserList(null, null, new ArrayList<>(Arrays.asList(userList)), true);
                    return userList;
                } else {
                    userList = secManager.listUsers(filter, maxItemLimit);
                    handlePostGetUserList(null, null, new ArrayList<>(Arrays.asList(userList)), true);
                    return userList;
                }
            }
        } else if (index == 0) {
            userList = doListUsers(filter.substring(1), maxItemLimit);
            handlePostGetUserList(null, null, new ArrayList<>(Arrays.asList(userList)), true);
            return userList;
        }

        try {
            userList = doListUsers(filter, maxItemLimit);
        } catch (UserStoreException ex) {
            handleGetUserListFailure(ErrorMessages.ERROR_CODE_ERROR_WHILE_GETTING_USER_LIST.getCode(),
                    String.format(ErrorMessages.ERROR_CODE_ERROR_WHILE_GETTING_USER_LIST.getMessage(), ex.getMessage()),
                    null, null, null);
            throw ex;
        }

        String primaryDomain = realmConfig
                .getUserStoreProperty(UserCoreConstants.RealmConfig.PROPERTY_DOMAIN_NAME);

        if (this.getSecondaryUserStoreManager() != null) {
            for (Map.Entry<String, UserStoreManager> entry : userStoreManagerHolder.entrySet()) {
                if (entry.getKey().equalsIgnoreCase(primaryDomain)) {
                    continue;
                }
                UserStoreManager storeManager = entry.getValue();
                if (storeManager instanceof AbstractUserStoreManager) {
                    try {
                        String[] secondUserList = ((AbstractUserStoreManager) storeManager)
                                .doListUsers(filter, maxItemLimit);
                        userList = UserCoreUtil.combineArrays(userList, secondUserList);
                    } catch (UserStoreException ex) {
                        handleGetUserListFailure(ErrorMessages.ERROR_CODE_ERROR_WHILE_GETTING_USER_LIST.getCode(),
                                String.format(ErrorMessages.ERROR_CODE_ERROR_WHILE_GETTING_USER_LIST.getMessage(),
                                        ex.getMessage()), null, null, null);

                        // We can ignore and proceed. Ignore the results from this user store.
                        log.error(ex);
                    }
                } else {
                    String[] secondUserList = storeManager.listUsers(filter, maxItemLimit);
                    userList = UserCoreUtil.combineArrays(userList, secondUserList);
                }
            }
        }

        handlePostGetUserList(null, null, new ArrayList<>(Arrays.asList(userList)), true);
        return userList;
    }

    /**
     * This is to call the relevant post methods in listeners after successful retrieval of user list of a role.
     *
     * @param roleName Name of the role.
     * @param userList List of users.
     * @throws UserStoreException User Store Exception.
     */
    private void handleDoPostGetUserListOfRole(String roleName, String[] userList) throws UserStoreException {

        for (UserOperationEventListener userOperationEventListener : UMListenerServiceComponent
                .getUserOperationEventListeners()) {
            if (userOperationEventListener instanceof AbstractUserOperationEventListener) {
                if (!((AbstractUserOperationEventListener) userOperationEventListener)
                        .doPostGetUserListOfRole(roleName, userList, this)) {
                    return;
                }
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    public final String[] getUserListOfRole(String roleName) throws UserStoreException {

        if (!isSecureCall.get()) {
            Class argTypes[] = new Class[]{String.class};
            Object object = callSecure("getUserListOfRole", new Object[]{roleName}, argTypes);
            return (String[]) object;
        }

        String[] userNames = new String[0];

        // If role does not exit, just return
        if (!isExistingRole(roleName)) {
            handleDoPostGetUserListOfRole(roleName, userNames);
            return userNames;
        }

        UserStore userStore = getUserStore(roleName);

        if (userStore.isRecurssive()) {
            return userStore.getUserStoreManager().getUserListOfRole(userStore.getDomainFreeName());
        }


        // #################### Domain Name Free Zone Starts Here
        // ################################

        if (userStore.isSystemStore()) {
            String[] userList = systemUserRoleManager.getUserListOfSystemRole(userStore.getDomainFreeName());
            handleDoPostGetUserListOfRole(roleName, userList);
            return userList;
        }

        String[] userNamesInHybrid = new String[0];
        if (userStore.isHybridRole()) {
            if (UserCoreConstants.INTERNAL_DOMAIN.equalsIgnoreCase(userStore.getDomainName())) {
                userNamesInHybrid = hybridRoleManager.getUserListOfHybridRole(userStore.getDomainFreeName());
            } else {
                userNamesInHybrid = hybridRoleManager.getUserListOfHybridRole(userStore.getDomainAwareName());
            }

            // remove domain
            List<String> finalNameList = new ArrayList<String>();
            String displayNameAttribute = this.realmConfig.getUserStoreProperty(LDAPConstants.DISPLAY_NAME_ATTRIBUTE);

            if (userNamesInHybrid != null && userNamesInHybrid.length > 0) {
                if (displayNameAttribute != null && displayNameAttribute.trim().length() > 0) {
                    for (String userName : userNamesInHybrid) {
                        String domainName = UserCoreUtil.extractDomainFromName(userName);
                        if (domainName == null || domainName.trim().length() == 0) {
                            finalNameList.add(userName);
                        }
                        UserStoreManager userManager = userStoreManagerHolder.get(domainName);
                        userName = UserCoreUtil.removeDomainFromName(userName);
                        if (userManager != null) {
                            String[] displayNames = null;
                            if (userManager instanceof AbstractUserStoreManager) {
                                // get displayNames
                                displayNames = ((AbstractUserStoreManager) userManager)
                                        .doGetDisplayNamesForInternalRole(new String[] { userName });
                            } else {
                                displayNames = userManager.getRoleNames();
                            }

                            for (String displayName : displayNames) {
                                // if domain names are not added by above method, add it
                                // here
                                String nameWithDomain = UserCoreUtil.addDomainToName(displayName, domainName);
                                finalNameList.add(nameWithDomain);
                            }
                        }
                    }
                } else {
                    handleDoPostGetUserListOfRole(roleName, userNamesInHybrid);
                    return userNamesInHybrid;
                }
            }
            String[] userList = finalNameList.toArray(new String[finalNameList.size()]);
            handleDoPostGetUserListOfRole(roleName, userList);
            return userList;
            // return
            // hybridRoleManager.getUserListOfHybridRole(userStore.getDomainFreeName());
        }

        if (readGroupsEnabled) {
            userNames = doGetUserListOfRole(roleName, "*");
            handleDoPostGetUserListOfRole(roleName, userNames);
        }

        return userNames;
    }

    public String[] getRoleListOfUser(String userName) throws UserStoreException {

        if (!isSecureCall.get()) {
            Class argTypes[] = new Class[]{String.class};
            Object object = callSecure("getRoleListOfUser", new Object[]{userName}, argTypes);
            return (String[]) object;
        }

        String[] roleNames = null;


        // anonymous user is only assigned to  anonymous role
        if (UserCoreConstants.REGISTRY_ANONNYMOUS_USERNAME.equalsIgnoreCase(userName)) {
            return new String[]{UserCoreConstants.REGISTRY_ANONNYMOUS_ROLE_NAME};
        }

        String usernameWithDomain = UserCoreUtil.addDomainToName(userName, getMyDomainName());
        // Check whether roles exist in cache
        roleNames = getRoleListOfUserFromCache(this.tenantId, usernameWithDomain);
        if (roleNames != null && roleNames.length > 0) {
            return roleNames;
        }

        UserStore userStore = getUserStore(userName);
        if (userStore.isRecurssive()) {
            return userStore.getUserStoreManager().getRoleListOfUser(userStore.getDomainFreeName());
        }

        if (userStore.isSystemStore()) {
            return systemUserRoleManager.getSystemRoleListOfUser(userStore.getDomainFreeName());
        }
        // #################### Domain Name Free Zone Starts Here ################################

        roleNames = doGetRoleListOfUser(userName, "*");

        return roleNames;

    }

    /**
     * Getter method for claim manager property specifically to be used in the implementations of
     * UserOperationEventListener implementations
     *
     * @return
     */
    public ClaimManager getClaimManager() {
        return claimManager;
    }

    /**
     * This method is responsible for calling relevant listener methods when there is a failure while trying to add
     * role.
     *
     * @param errorCode    Error code.
     * @param errorMessage Error message.
     * @param roleName     Name of the role.
     * @param userList     List of users to be assigned to the role.
     * @param permissions  Permissions of the role role.
     * @throws UserStoreException Exception that will be thrown by relevant listeners.
     */
    private void handleAddRoleFailure(String errorCode, String errorMessage, String roleName, String[] userList,
            org.wso2.micro.integrator.security.user.api.Permission[] permissions) throws UserStoreException {

        for (UserManagementErrorEventListener listener : UMListenerServiceComponent
                .getUserManagementErrorEventListeners()) {
            if (listener.isEnable() && !listener
                    .onAddRoleFailure(errorCode, errorMessage, roleName, userList, permissions, this)) {
                return;
            }
        }
    }

    /**
     * This method is responsible for calling relevant postAddRole listener methods after successfully adding role.
     *
     * @param roleName       Name of the role.
     * @param userList       List of users.
     * @param permissions    Permissions that are assigned to the role.
     * @param isAuditLogOnly To indicate whether to only call the relevant audit logger.
     * @throws UserStoreException Exception that will be thrown by relevant listeners.
     */
    private void handlePostAddRole(String roleName, String[] userList, org.wso2.micro.integrator.security.user.api.Permission[]
            permissions, boolean isAuditLogOnly) throws UserStoreException {

        try {
            boolean internalRole = isAnInternalRole(roleName);
            for (UserOperationEventListener listener : UMListenerServiceComponent.getUserOperationEventListeners()) {
                if (isAuditLogOnly && !listener.getClass().getName().endsWith(UserCoreErrorConstants.AUDIT_LOGGER_CLASS_NAME)) {
                    continue;
                }

                boolean success = false;
                if (internalRole && listener instanceof AbstractUserOperationEventListener) {
                    success = ((AbstractUserOperationEventListener) listener).doPostAddInternalRole(roleName,
                            userList, permissions, this);
                } else if (internalRole && !(listener instanceof AbstractUserOperationEventListener)) {
                    success = true;
                } else if (!internalRole) {
                    success = listener.doPostAddRole(roleName, userList, permissions, this);
                }

                if (!success) {
                    handleAddRoleFailure(ErrorMessages.ERROR_CODE_ERROR_DURING_POST_ADD_ROLE.getCode(),
                            String.format(ErrorMessages.ERROR_CODE_ERROR_DURING_POST_ADD_ROLE.getMessage(),
                                    UserCoreErrorConstants.POST_LISTENER_TASKS_FAILED_MESSAGE), roleName, userList,
                            permissions);
                    return;
                }
            }
        } catch (UserStoreException ex) {
            handleAddRoleFailure(ErrorMessages.ERROR_CODE_ERROR_DURING_POST_ADD_ROLE.getCode(),
                    String.format(ErrorMessages.ERROR_CODE_ERROR_DURING_POST_ADD_ROLE.getMessage(), ex.getMessage()),
                    roleName, userList, permissions);
            throw ex;
        }
    }

    private boolean handlePreAddRole(String roleName, String[] userList, org.wso2.micro.integrator.security.user.api.Permission[]
            permissions, boolean isAuditLogOnly) throws UserStoreException {

        try {
            boolean internalRole = isAnInternalRole(roleName);
            for (UserOperationEventListener listener : UMListenerServiceComponent.getUserOperationEventListeners()) {
                if (isAuditLogOnly && !listener.getClass().getName().endsWith(UserCoreErrorConstants.AUDIT_LOGGER_CLASS_NAME)) {
                    continue;
                }

                boolean success = false;
                if (internalRole && listener instanceof AbstractUserOperationEventListener) {
                    success = ((AbstractUserOperationEventListener) listener).doPreAddInternalRole(roleName,
                            userList, permissions, this);
                } else if (internalRole && !(listener instanceof AbstractUserOperationEventListener)) {
                    success = true;
                } else if (!internalRole) {
                    success = listener.doPreAddRole(roleName, userList, permissions, this);
                }

                if (!success) {
                    handleAddRoleFailure(ErrorMessages.ERROR_CODE_ERROR_DURING_PRE_ADD_ROLE.getCode(),
                            String.format(ErrorMessages.ERROR_CODE_ERROR_DURING_PRE_ADD_ROLE.getMessage(),
                                    UserCoreErrorConstants.PRE_LISTENER_TASKS_FAILED_MESSAGE), roleName, userList,
                            permissions);
                    return false;
                }
            }
        } catch (UserStoreException ex) {
            handleAddRoleFailure(ErrorMessages.ERROR_CODE_ERROR_DURING_PRE_ADD_ROLE.getCode(),
                    String.format(ErrorMessages.ERROR_CODE_ERROR_DURING_PRE_ADD_ROLE.getMessage(), ex.getMessage()),
                    roleName, userList, permissions);
            throw ex;
        }
        return true;
    }

    /**
     *
     */
    public void addRole(String roleName, String[] userList,
                        org.wso2.micro.integrator.security.user.api.Permission[] permissions, boolean isSharedRole)
            throws org.wso2.micro.integrator.security.user.api.UserStoreException {

        if (StringUtils.isEmpty(roleName)) {
            handleAddRoleFailure(ErrorMessages.ERROR_CODE_CANNOT_ADD_EMPTY_ROLE.getCode(),
                    ErrorMessages.ERROR_CODE_CANNOT_ADD_EMPTY_ROLE.getMessage(), roleName, userList, permissions);
            throw new UserStoreException(ErrorMessages.ERROR_CODE_CANNOT_ADD_EMPTY_ROLE.toString());
        }

        UserStore userStore = getUserStore(roleName);

        if (isSharedRole && !isSharedGroupEnabled()) {
            handleAddRoleFailure(ErrorMessages.ERROR_CODE_SHARED_ROLE_NOT_SUPPORTED.getCode(),
                    ErrorMessages.ERROR_CODE_SHARED_ROLE_NOT_SUPPORTED.getMessage(), roleName, userList, permissions);
            throw new org.wso2.micro.integrator.security.user.api.UserStoreException(
                    ErrorMessages.ERROR_CODE_SHARED_ROLE_NOT_SUPPORTED.toString());
        }

        if (userStore.isHybridRole()) {
            //Invoke Pre listeners for hybrid roles.
            if (!handlePreAddRole(roleName, userList, permissions, false)) {
                return;
            }

            doAddInternalRole(roleName, userList, permissions);

            // Calling only the audit logger, to maintain the back-ward compatibility
            handlePostAddRole(roleName, userList, permissions, false);
            return;
        }

        if (userStore.isRecurssive()) {
            userStore.getUserStoreManager().addRole(userStore.getDomainFreeName(),
                    UserCoreUtil.removeDomainFromNames(userList), permissions, isSharedRole);
            return;
        }

        // #################### Domain Name Free Zone Starts Here ################################
        if (userList == null) {
            userList = new String[0];
        }
        if (permissions == null) {
            permissions = new org.wso2.micro.integrator.security.user.api.Permission[0];
        }
        // This happens only once during first startup - adding administrator user/role.
        if (roleName.indexOf(UserCoreConstants.DOMAIN_SEPARATOR) > 0) {
            roleName = userStore.getDomainFreeName();
            userList = UserCoreUtil.removeDomainFromNames(userList);
        }

        // #################### <Listeners> #####################################################
        if (!handlePreAddRole(roleName, userList, permissions, false)) {
            return;
        }
        // #################### </Listeners> #####################################################

        // Check for validations
        if (isReadOnly()) {
            handleAddRoleFailure(ErrorMessages.ERROR_CODE_READONLY_USER_STORE.getCode(),
                    ErrorMessages.ERROR_CODE_READONLY_USER_STORE.getMessage(), roleName, userList, permissions);
            throw new UserStoreException(ErrorMessages.ERROR_CODE_READONLY_USER_STORE.toString());
        }

        if (!isRoleNameValid(roleName)) {
            String regEx = realmConfig
                    .getUserStoreProperty(UserCoreConstants.RealmConfig.PROPERTY_ROLE_NAME_JAVA_REG_EX);
            String errorMessage = String
                    .format(ErrorMessages.ERROR_CODE_INVALID_ROLE_NAME.getMessage(), roleName, regEx);
            String errorCode = ErrorMessages.ERROR_CODE_INVALID_ROLE_NAME.getCode();
            handleAddRoleFailure(errorCode, errorMessage, roleName, userList, permissions);
            throw new UserStoreException(errorCode + " - " + errorMessage);
        }

        if (doCheckExistingRole(roleName)) {
            handleRoleAlreadyExistException(roleName, userList, permissions);
        }

        String roleWithDomain = null;
        if (writeGroupsEnabled) {
            try {
                // add role in to actual user store
                doAddRole(roleName, userList, isSharedRole);
                roleWithDomain = UserCoreUtil.addDomainToName(roleName, getMyDomainName());
            } catch (UserStoreException ex) {
                handleAddRoleFailure(ErrorMessages.ERROR_CODE_ERROR_WHILE_ADDING_ROLE.getCode(),
                        String.format(ErrorMessages.ERROR_CODE_ERROR_WHILE_ADDING_ROLE.getMessage(), ex.getMessage()),
                        roleName, userList, permissions);
                throw ex;
            }
        } else {
            handleAddRoleFailure(ErrorMessages.ERROR_CODE_WRITE_GROUPS_NOT_ENABLED.getCode(),
                    ErrorMessages.ERROR_CODE_WRITE_GROUPS_NOT_ENABLED.getMessage(), roleName, userList, permissions);
            throw new UserStoreException(ErrorMessages.ERROR_CODE_WRITE_GROUPS_NOT_ENABLED.toString());
        }

        // add permission in to the the permission store
        if (permissions != null) {
            for (org.wso2.micro.integrator.security.user.api.Permission permission : permissions) {
                String resourceId = permission.getResourceId();
                String action = permission.getAction();
                if (resourceId == null || resourceId.trim().length() == 0) {
                    continue;
                }

                if (action == null || action.trim().length() == 0) {
                    // default action value // TODO
                    action = "read";
                }
                // This is a special case. We need to pass domain aware name.
//                userRealm.getAuthorizationManager().authorizeRole(roleWithDomain, resourceId,
//                        action);
            }
        }

        // #################### <Listeners> #####################################################
        handlePostAddRole(roleName, userList, permissions, false);
        // #################### </Listeners> #####################################################

    }

    /**
     * TODO move to API
     *
     * @return
     */
    public boolean isSharedGroupEnabled() {
        String value = realmConfig.getUserStoreProperty(UserCoreConstants.RealmConfig.SHARED_GROUPS_ENABLED);
        try {
            return realmConfig.isPrimary() && !isReadOnly() && TRUE_VALUE.equalsIgnoreCase(value);
        } catch (UserStoreException e) {
            log.error(e);
        }
        return false;
    }

    /**
     * Removes the shared roles relevant to the provided tenant domain
     *
     * @param sharedRoles
     * @param tenantDomain
     */
    protected void filterSharedRoles(List<String> sharedRoles, String tenantDomain) {
        if (tenantDomain != null) {
            for (Iterator<String> i = sharedRoles.iterator(); i.hasNext(); ) {
                String role = i.next();
                if (role.indexOf(tenantDomain) > -1) {
                    i.remove();
                }
            }
        }
    }

    /**
     * This method calls the relevant methods when there is a failure while trying to delete the role.
     *
     * @param errorCode    Error code.
     * @param errorMessage Error message.
     * @param roleName     Name of the roles.
     * @throws UserStoreException Exception that will be thrown by relevant listener methods.
     */
    private void handleDeleteRoleFailure(String errorCode, String errorMessage, String roleName)
            throws UserStoreException {

        for (UserManagementErrorEventListener listener : UMListenerServiceComponent
                .getUserManagementErrorEventListeners()) {
            if (listener.isEnable() && !listener.onDeleteRoleFailure(errorCode, errorMessage, roleName, this)) {
                return;
            }
        }
    }

    /**
     * This method is responsible for calling post delete methods of relevant listeners.
     *
     * @param roleName       Name of the role
     * @param isAuditLogOnly To indicate whether to call only the audit logger.
     * @throws UserStoreException Exception that will be thrown by relevant listener methods.
     */
    private void handleDoPostDeleteRole(String roleName, boolean isAuditLogOnly) throws UserStoreException {

        try {
            boolean internalRole = isAnInternalRole(roleName);
            for (UserOperationEventListener listener : UMListenerServiceComponent.getUserOperationEventListeners()) {
                if (isAuditLogOnly && !listener.getClass().getName()
                        .endsWith(UserCoreErrorConstants.AUDIT_LOGGER_CLASS_NAME)) {
                    continue;
                }

                boolean success = false;
                if (internalRole && listener instanceof AbstractUserOperationEventListener) {
                    success = ((AbstractUserOperationEventListener) listener).doPostDeleteInternalRole(roleName, this);
                } else if (internalRole && !(listener instanceof AbstractUserOperationEventListener)) {
                    success = true;
                } else if (!internalRole) {
                    success = listener.doPostDeleteRole(roleName, this);
                }

                if (!success) {
                    handleDeleteRoleFailure(ErrorMessages.ERROR_CODE_ERROR_DURING_POST_DELETE_ROLE.getCode(),
                            String.format(ErrorMessages.ERROR_CODE_ERROR_DURING_POST_DELETE_ROLE.getMessage(),
                                    UserCoreErrorConstants.POST_LISTENER_TASKS_FAILED_MESSAGE), roleName);
                    return;
                }
            }
        } catch (UserStoreException ex) {
            handleDeleteRoleFailure(ErrorMessages.ERROR_CODE_ERROR_DURING_POST_DELETE_ROLE.getCode(),
                    String.format(ErrorMessages.ERROR_CODE_ERROR_DURING_POST_DELETE_ROLE.getMessage(), ex.getMessage()),
                    roleName);
            throw ex;
        }
    }

    /**
     * This method is responsible for calling pre delete methods of relevant listeners.
     *
     * @param roleName       Name of the role
     * @param isAuditLogOnly To indicate whether to call only the audit logger.
     * @throws UserStoreException Exception that will be thrown by relevant listener methods.
     */
    private boolean handleDoPreDeleteRole(String roleName, boolean isAuditLogOnly) throws UserStoreException {

        try {
            boolean internalRole = isAnInternalRole(roleName);
            for (UserOperationEventListener listener : UMListenerServiceComponent.getUserOperationEventListeners()) {
                if (isAuditLogOnly && !listener.getClass().getName()
                        .endsWith(UserCoreErrorConstants.AUDIT_LOGGER_CLASS_NAME)) {
                    continue;
                }

                boolean success = false;
                if (internalRole && listener instanceof AbstractUserOperationEventListener) {
                    success = ((AbstractUserOperationEventListener) listener).doPreDeleteInternalRole(roleName, this);
                } else if (internalRole && !(listener instanceof AbstractUserOperationEventListener)) {
                    success = true;
                } else if (!internalRole) {
                    success = listener.doPreDeleteRole(roleName, this);
                }

                if (!success) {
                    handleDeleteRoleFailure(ErrorMessages.ERROR_CODE_ERROR_DURING_PRE_DELETE_ROLE.getCode(),
                            String.format(ErrorMessages.ERROR_CODE_ERROR_DURING_PRE_DELETE_ROLE.getMessage(),
                                    UserCoreErrorConstants.PRE_LISTENER_TASKS_FAILED_MESSAGE), roleName);
                    return false;
                }
            }
        } catch (UserStoreException ex) {
            handleDeleteRoleFailure(ErrorMessages.ERROR_CODE_ERROR_DURING_PRE_DELETE_ROLE.getCode(),
                    String.format(ErrorMessages.ERROR_CODE_ERROR_DURING_PRE_DELETE_ROLE.getMessage(), ex.getMessage()),
                    roleName);
            throw ex;
        }
        return true;
    }

    /**
     * Delete the role with the given role name
     *
     * @param roleName The role name
     * @throws org.wso2.micro.integrator.security.user.core.UserStoreException
     */
    public final void deleteRole(String roleName) throws UserStoreException {

        if (!isSecureCall.get()) {
            Class argTypes[] = new Class[]{String.class};
            callSecure("deleteRole", new Object[]{roleName}, argTypes);
            return;
        }

        if (UserCoreUtil.isPrimaryAdminRole(roleName, realmConfig)) {
            handleDeleteRoleFailure(ErrorMessages.ERROR_CODE_CANNOT_DELETE_ADMIN_ROLE.getCode(),
                    ErrorMessages.ERROR_CODE_CANNOT_DELETE_ADMIN_ROLE.getMessage(), roleName);
            throw new UserStoreException(ErrorMessages.ERROR_CODE_CANNOT_DELETE_ADMIN_ROLE.toString());
        }
        if (UserCoreUtil.isEveryoneRole(roleName, realmConfig)) {
            handleDeleteRoleFailure(ErrorMessages.ERROR_CODE_CANNOT_DELETE_EVERYONE_ROLE.getCode(),
                    ErrorMessages.ERROR_CODE_CANNOT_DELETE_EVERYONE_ROLE.getMessage(), roleName);
            throw new UserStoreException(ErrorMessages.ERROR_CODE_CANNOT_DELETE_EVERYONE_ROLE.toString());
        }

        UserStore userStore = getUserStore(roleName);
        if (userStore.isRecurssive()) {
            userStore.getUserStoreManager().deleteRole(userStore.getDomainFreeName());
            return;
        }

        String roleWithDomain = UserCoreUtil.addDomainToName(roleName, getMyDomainName());
        // #################### Domain Name Free Zone Starts Here ################################

        if (userStore.isHybridRole()) {
            // Invoke pre listeners.
            if (!handleDoPreDeleteRole(roleName, false)) {
                return;
            }
            try {
                if (APPLICATION_DOMAIN.equalsIgnoreCase(userStore.getDomainName()) || WORKFLOW_DOMAIN
                        .equalsIgnoreCase(userStore.getDomainName())) {
                    hybridRoleManager.deleteHybridRole(roleName);
                } else {
                    hybridRoleManager.deleteHybridRole(userStore.getDomainFreeName());
                }
            } catch (UserStoreException ex) {
                handleDeleteRoleFailure(ErrorMessages.ERROR_CODE_ERROR_WHILE_DELETE_ROLE.getCode(),
                        String.format(ErrorMessages.ERROR_CODE_ERROR_WHILE_DELETE_ROLE.getMessage(), ex.getMessage()),
                        roleName);
                throw ex;
            }
            handleDoPostDeleteRole(roleName, false);
            return;
        }

        if (!doCheckExistingRole(roleName)) {
            handleDeleteRoleFailure(ErrorMessages.ERROR_CODE_CANNOT_DELETE_NON_EXISTING_ROLE.getCode(),
                    ErrorMessages.ERROR_CODE_CANNOT_DELETE_NON_EXISTING_ROLE.getMessage(), roleName);
            throw new UserStoreException(ErrorMessages.ERROR_CODE_CANNOT_DELETE_NON_EXISTING_ROLE.toString());
        }

        // #################### <Listeners> #####################################################
        if (!handleDoPreDeleteRole(roleName, false)) {
            return;
        }
        // #################### </Listeners> #####################################################
        if (isReadOnly()) {
            handleDeleteRoleFailure(ErrorMessages.ERROR_CODE_READONLY_USER_STORE.getCode(),
                    ErrorMessages.ERROR_CODE_READONLY_USER_STORE.getMessage(), roleName);
            throw new UserStoreException(ErrorMessages.ERROR_CODE_READONLY_USER_STORE.toString());
        }

        if (!writeGroupsEnabled) {
            handleDeleteRoleFailure(ErrorMessages.ERROR_CODE_WRITE_GROUPS_NOT_ENABLED.getCode(),
                    ErrorMessages.ERROR_CODE_WRITE_GROUPS_NOT_ENABLED.getMessage(), roleName);
            throw new UserStoreException(ErrorMessages.ERROR_CODE_WRITE_GROUPS_NOT_ENABLED.toString());
        }
        try {
            doDeleteRole(roleName);
        } catch (UserStoreException ex) {
            handleDeleteRoleFailure(ErrorMessages.ERROR_CODE_ERROR_WHILE_DELETE_ROLE.getCode(),
                    String.format(ErrorMessages.ERROR_CODE_ERROR_WHILE_DELETE_ROLE.getMessage(), ex.getMessage()),
                    roleName);
            throw ex;
        }

        // clear role authorization
//        userRealm.getAuthorizationManager().clearRoleAuthorization(roleWithDomain);

        // Call relevant listeners after deleting the role.
        handleDoPostDeleteRole(roleName, false);

    }

    /**
     * Method to get the password expiration time.
     *
     * @param userName the user name.
     * @return the password expiration time.
     * @throws UserStoreException throw if the operation failed.
     */
    @Override
    public Date getPasswordExpirationTime(String userName) throws UserStoreException {
        UserStore userStore = getUserStore(userName);

        if (userStore.isRecurssive()) {
            return userStore.getUserStoreManager().getPasswordExpirationTime(userStore.getDomainFreeName());
        }

        return null;
    }

    private UserStore getUserStore(final String user) throws UserStoreException {
        try {
            return AccessController.doPrivileged(new PrivilegedExceptionAction<UserStore>() {
                @Override
                public UserStore run() throws Exception {
                    return getUserStoreInternal(user);
                }
            });
        } catch (PrivilegedActionException e) {
            throw (UserStoreException) e.getException();
        }
    }

    /**
     * @return
     * @throws UserStoreException
     */
    private UserStore getUserStoreInternal(String user) throws UserStoreException {

        int index;
        index = user.indexOf(UserCoreConstants.DOMAIN_SEPARATOR);
        UserStore userStore = new UserStore();
        String domainFreeName = null;

        // Check whether we have a secondary UserStoreManager setup.
        if (index > 0) {
            // Using the short-circuit. User name comes with the domain name.
            String domain = user.substring(0, index);
            UserStoreManager secManager = getSecondaryUserStoreManager(domain);
            domainFreeName = user.substring(index + 1);

            if (secManager != null) {
                userStore.setUserStoreManager(secManager);
                userStore.setDomainAwareName(user);
                userStore.setDomainFreeName(domainFreeName);
                userStore.setDomainName(domain);
                userStore.setRecurssive(true);
                return userStore;
            } else {
                if (!domain.equalsIgnoreCase(getMyDomainName())) {
                    if ((UserCoreConstants.INTERNAL_DOMAIN.equalsIgnoreCase(domain)
                            || APPLICATION_DOMAIN.equalsIgnoreCase(domain) || WORKFLOW_DOMAIN.equalsIgnoreCase(domain))) {
                        userStore.setHybridRole(true);
                    } else if (UserCoreConstants.SYSTEM_DOMAIN_NAME.equalsIgnoreCase(domain)) {
                        userStore.setSystemStore(true);
                    } else {
                        throw new UserStoreException("Invalid Domain Name");
                    }
                }

                userStore.setDomainAwareName(user);
                userStore.setDomainFreeName(domainFreeName);
                userStore.setDomainName(domain);
                userStore.setRecurssive(false);
                return userStore;
            }
        }

        String domain = getMyDomainName();
        userStore.setUserStoreManager(this);
        if (index > 0) {
            userStore.setDomainAwareName(user);
            userStore.setDomainFreeName(domainFreeName);
        } else {
            userStore.setDomainAwareName(domain + UserCoreConstants.DOMAIN_SEPARATOR + user);
            userStore.setDomainFreeName(user);
        }
        userStore.setRecurssive(false);
        userStore.setDomainName(domain);

        return userStore;
    }

    /**
     * {@inheritDoc}
     */
    public final UserStoreManager getSecondaryUserStoreManager() {
        return secondaryUserStoreManager;
    }

    /**
     *
     */
    public final void setSecondaryUserStoreManager(UserStoreManager secondaryUserStoreManager) {
        this.secondaryUserStoreManager = secondaryUserStoreManager;
    }

    /**
     * {@inheritDoc}
     */
    public final UserStoreManager getSecondaryUserStoreManager(String userDomain) {
        if (userDomain == null) {
            return null;
        }
        return userStoreManagerHolder.get(userDomain.toUpperCase());
    }

    /**
     * {@inheritDoc}
     */
    public final void addSecondaryUserStoreManager(String userDomain,
                                                   UserStoreManager userStoreManager) {
        if (userDomain != null) {
            userStoreManagerHolder.put(userDomain.toUpperCase(), userStoreManager);
        }
    }

    public final void clearAllSecondaryUserStores() {
        userStoreManagerHolder.clear();

        if (getMyDomainName() != null) {
            userStoreManagerHolder.put(getMyDomainName().toUpperCase(), this);
        }
    }

    /**
     * {@inheritDoc}
     */
    public final String[] getAllSecondaryRoles() throws UserStoreException {

        if (!isSecureCall.get()) {
            Class argTypes[] = new Class[]{};
            Object object = callSecure("getAllSecondaryRoles", new Object[]{}, argTypes);
            return (String[]) object;
        }

        UserStoreManager secondary = this.getSecondaryUserStoreManager();
        List<String> roleList = new ArrayList<String>();
        while (secondary != null) {
            String[] roles = secondary.getRoleNames(true);
            if (roles != null && roles.length > 0) {
                Collections.addAll(roleList, roles);
            }
            secondary = secondary.getSecondaryUserStoreManager();
        }
        return roleList.toArray(new String[roleList.size()]);
    }

    /**
     * @return
     */
    public boolean isSCIMEnabled() {
        String scimEnabled = realmConfig
                .getUserStoreProperty(UserCoreConstants.RealmConfig.PROPERTY_SCIM_ENABLED);
        if (scimEnabled != null) {
            return Boolean.parseBoolean(scimEnabled);
        } else {
            return false;
        }
    }

    /**
     * {@inheritDoc}                  doAddInternalRole
     */
    public final String[] getHybridRoles() throws UserStoreException {
        return hybridRoleManager.getHybridRoles("*");
    }

    /**
     * {@inheritDoc}
     */
    public final String[] getRoleNames() throws UserStoreException {
        return getRoleNames(false);
    }

    /**
     * {@inheritDoc}
     */
    public final String[] getRoleNames(boolean noHybridRoles) throws UserStoreException {
        return getRoleNames("*", MAX_ITEM_LIMIT_UNLIMITED, noHybridRoles, true, true);
    }

    /**
     * @param roleName
     * @param userList
     * @param permissions
     * @throws UserStoreException
     */
    protected void doAddInternalRole(String roleName, String[] userList,
                                     org.wso2.micro.integrator.security.user.api.Permission[] permissions)
            throws UserStoreException {

        // #################### Domain Name Free Zone Starts Here ################################

        if (roleName.contains(UserCoreConstants.DOMAIN_SEPARATOR)
                && roleName.toLowerCase().startsWith(APPLICATION_DOMAIN.toLowerCase())) {
            if (hybridRoleManager.isExistingRole(roleName)) {
                handleRoleAlreadyExistException(roleName, userList, permissions);
            }

            hybridRoleManager.addHybridRole(roleName, userList);

        } else {
            if (hybridRoleManager.isExistingRole(UserCoreUtil.removeDomainFromName(roleName))) {
                handleRoleAlreadyExistException(roleName, userList, permissions);
            }

            hybridRoleManager.addHybridRole(UserCoreUtil.removeDomainFromName(roleName), userList);
        }


        if (permissions != null) {
            for (org.wso2.micro.integrator.security.user.api.Permission permission : permissions) {
                String resourceId = permission.getResourceId();
                String action = permission.getAction();
                // This is a special case. We need to pass domain aware name.
//                userRealm.getAuthorizationManager().authorizeRole(
//                        UserCoreUtil.addInternalDomainName(roleName), resourceId, action);
            }
        }
    }

    /**
     * This method handles role already exists exception.
     *
     * @param roleName    Name of teh role.
     * @param userList    list of users.
     * @param permissions Relevant permissions added for new role.
     * @throws UserStoreException User Store Exception.
     */
    private void handleRoleAlreadyExistException(String roleName, String[] userList,
            org.wso2.micro.integrator.security.user.api.Permission[] permissions) throws UserStoreException {

        String errorCode = ErrorMessages.ERROR_CODE_ROLE_ALREADY_EXISTS.getCode();
        String errorMessage = String.format(ErrorMessages.ERROR_CODE_ROLE_ALREADY_EXISTS.getMessage(), roleName);
        handleAddRoleFailure(errorCode, errorMessage, roleName, userList, permissions);
        throw new UserStoreException(errorCode + " - " + errorMessage);
    }

    /**
     * Returns the set of shared roles which applicable for the logged in tenant
     *
     * @param tenantDomain tenant domain of the shared roles. If this is null,
     *                     returns all shared roles of available tenant domains
     * @param filter
     * @param maxItemLimit
     * @return
     */
    protected abstract String[] doGetSharedRoleNames(String tenantDomain, String filter,
                                                     int maxItemLimit) throws UserStoreException;

    /**
     * TODO This method would returns the role Name actually this must be implemented in interface.
     * As it is not good to change the API in point release. This has been added to Abstract class
     *
     * @param filter
     * @param maxItemLimit
     * @param noInternalRoles
     * @return
     * @throws UserStoreException
     */
    public final String[] getRoleNames(String filter, int maxItemLimit, boolean noInternalRoles,
                                       boolean noSystemRole, boolean noSharedRoles)
            throws UserStoreException {

        if (!isSecureCall.get()) {
            Class argTypes[] = new Class[]{String.class, int.class, boolean.class, boolean.class, boolean.class};
            Object object = callSecure("getRoleNames", new Object[]{filter, maxItemLimit, noInternalRoles,
                    noSystemRole, noSharedRoles}, argTypes);
            return (String[]) object;
        }

        String[] roleList = new String[0];

        if (!noInternalRoles && (filter.toLowerCase().startsWith(APPLICATION_DOMAIN.toLowerCase()))) {
            roleList = hybridRoleManager.getHybridRoles(filter);
        } else if (!noInternalRoles && !isAnInternalRole(filter) && !filter
                .contains(UserCoreConstants.DOMAIN_SEPARATOR)) {
            // When domain name is not present in the filter value.
            if (filter == "*") {
                roleList = hybridRoleManager.getHybridRoles(filter);
            } else {
                // Since Application domain roles are stored in db with the "Application/" prefix, when domain is not
                // present in the filter, need to append the "Application/" before sending for db query.
                String[] applicationDomainRoleArray = hybridRoleManager
                        .getHybridRoles(APPLICATION_DOMAIN + UserCoreConstants.DOMAIN_SEPARATOR + filter);
                String[] internalDomainRoleArray = hybridRoleManager.getHybridRoles(filter);
                List<String> internalOnlyList = new ArrayList<>();
                // When filtering with sw, ew and co there is a possibility of returning results belonging to
                // Application domain.
                for (String filteredRole : internalDomainRoleArray) {
                    if (filteredRole != null && !filteredRole.matches("Application/(.*)")) {
                        // Create Internal domain only list.
                        internalOnlyList.add(filteredRole);
                    }
                }
                roleList = UserCoreUtil.combineArrays(applicationDomainRoleArray,
                        internalOnlyList.toArray(new String[internalOnlyList.size()]));
            }
        } else if (!noInternalRoles) {
            roleList = hybridRoleManager.getHybridRoles(UserCoreUtil.removeDomainFromName(filter));
        }

        if (!noSystemRole) {
            String[] systemRoles = systemUserRoleManager.getSystemRoles();
            roleList = UserCoreUtil.combineArrays(roleList, systemRoles);
        }

        int index;
        index = filter.indexOf(UserCoreConstants.DOMAIN_SEPARATOR);

        // Check whether we have a secondary UserStoreManager setup.
        if (index > 0) {
            // Using the short-circuit. User name comes with the domain name.
            String domain = filter.substring(0, index);

            UserStoreManager secManager = getSecondaryUserStoreManager(domain);
            if (UserCoreConstants.INTERNAL_DOMAIN.equalsIgnoreCase(domain)
                    || APPLICATION_DOMAIN.equalsIgnoreCase(domain) || WORKFLOW_DOMAIN.equalsIgnoreCase(domain)) {
                return roleList;
            }
            if (secManager != null) {
                // We have a secondary UserStoreManager registered for this domain.
                filter = filter.substring(index + 1);
                if (secManager instanceof AbstractUserStoreManager) {
                    if (readGroupsEnabled) {
                        String[] externalRoles = ((AbstractUserStoreManager) secManager)
                                .doGetRoleNames(filter, maxItemLimit);
                        return UserCoreUtil.combineArrays(roleList, externalRoles);
                    }
                } else {
                    String[] externalRoles = secManager.getRoleNames();
                    return UserCoreUtil.combineArrays(roleList, externalRoles);
                }
            } else {
                throw new UserStoreException("Invalid Domain Name");
            }
        } else if (index == 0) {
            if (readGroupsEnabled) {
                String[] externalRoles = doGetRoleNames(filter.substring(index + 1), maxItemLimit);
                return UserCoreUtil.combineArrays(roleList, externalRoles);
            }
        }

        if (readGroupsEnabled) {
            String[] externalRoles = doGetRoleNames(filter, maxItemLimit);
            roleList = UserCoreUtil.combineArrays(externalRoles, roleList);
        }

        String primaryDomain = getMyDomainName();

        if (this.getSecondaryUserStoreManager() != null) {
            for (Map.Entry<String, UserStoreManager> entry : userStoreManagerHolder.entrySet()) {
                if (entry.getKey().equalsIgnoreCase(primaryDomain)) {
                    continue;
                }
                UserStoreManager storeManager = entry.getValue();
                if (storeManager instanceof AbstractUserStoreManager) {
                    try {
                        if (readGroupsEnabled) {
                            String[] secondRoleList = ((AbstractUserStoreManager) storeManager)
                                    .doGetRoleNames(filter, maxItemLimit);
                            roleList = UserCoreUtil.combineArrays(roleList, secondRoleList);
                        }
                    } catch (UserStoreException e) {
                        // We can ignore and proceed. Ignore the results from this user store.
                        log.error(e);
                    }
                } else {
                    roleList = UserCoreUtil.combineArrays(roleList, storeManager.getRoleNames());
                }
            }
        }
        return roleList;
    }

    /**
     * @param userName
     * @param claims
     * @param domainName
     * @return
     * @throws UserStoreException
     */
    private Map<String, String> doGetUserClaimValues(String userName, String[] claims,
                                                     String domainName, String profileName) throws UserStoreException {

        if (!isSecureCall.get()) {
            Class argTypes[] = new Class[]{String.class, String[].class, String.class, String.class};
            Object object = callSecure("doGetUserClaimValues", new Object[]{userName, claims, domainName,
                    profileName}, argTypes);
            return (Map<String, String>) object;
        }

        // Here the user name should be domain-less.
        boolean requireRoles = false;
        boolean requireIntRoles = false;
        boolean requireExtRoles = false;
        String roleClaim = null;

        if (profileName == null || profileName.trim().length() == 0) {
            profileName = UserCoreConstants.DEFAULT_PROFILE;
        }

        Set<String> propertySet = new HashSet<String>();
        for (String claim : claims) {

            // There can be cases some claim values being requested for claims
            // we don't have.
            String property = null;
            try {
                property = getClaimAtrribute(claim, userName, domainName);
            } catch (org.wso2.micro.integrator.security.user.api.UserStoreException e) {
                throw new UserStoreException(e);
            }
            if (property != null
                    && (!UserCoreConstants.ROLE_CLAIM.equalsIgnoreCase(claim)
                    || !UserCoreConstants.INT_ROLE_CLAIM.equalsIgnoreCase(claim) ||
                    !UserCoreConstants.EXT_ROLE_CLAIM.equalsIgnoreCase(claim))) {
                propertySet.add(property);
            }

            if (UserCoreConstants.ROLE_CLAIM.equalsIgnoreCase(claim)) {
                requireRoles = true;
                roleClaim = claim;
            } else if (UserCoreConstants.INT_ROLE_CLAIM.equalsIgnoreCase(claim)) {
                requireIntRoles = true;
                roleClaim = claim;
            } else if (UserCoreConstants.EXT_ROLE_CLAIM.equalsIgnoreCase(claim)) {
                requireExtRoles = true;
                roleClaim = claim;
            }
        }

        String[] properties = propertySet.toArray(new String[propertySet.size()]);
        Map<String, String> uerProperties = this.getUserPropertyValues(userName, properties,
                profileName);

        List<String> getAgain = new ArrayList<String>();
        Map<String, String> finalValues = new HashMap<String, String>();
        boolean isOverrideUsernameClaimEnabled = Boolean.parseBoolean(realmConfig
                .getIsOverrideUsernameClaimFromInternalUsername());

        for (String claim : claims) {
            ClaimMapping mapping;
            try {
                mapping = (ClaimMapping) claimManager.getClaimMapping(claim);
            } catch (org.wso2.micro.integrator.security.user.api.UserStoreException e) {
                throw new UserStoreException(e);
            }
            String property = null;
            String value = null;
            if (mapping != null) {
                if (domainName != null) {
                    Map<String, String> attrMap = mapping.getMappedAttributes();
                    if (attrMap != null) {
                        String attr = null;
                        if ((attr = attrMap.get(domainName.toUpperCase())) != null) {
                            property = attr;
                        } else {
                            property = mapping.getMappedAttribute();
                        }
                    }
                } else {
                    property = mapping.getMappedAttribute();
                }

                value = uerProperties.get(property);

                if (isOverrideUsernameClaimEnabled && USERNAME_CLAIM_URI.equals(mapping.getClaim()
                        .getClaimUri())) {
                    if (log.isDebugEnabled()) {
                        log.debug("The username claim value is overridden by the username :" + userName);
                    }
                    value = userName;
                }
                if (value != null && value.trim().length() > 0) {
                    finalValues.put(claim, value);
                }

            } else {
                if (property == null && claim.equals(DISAPLAY_NAME_CLAIM)) {
                    property = this.realmConfig.getUserStoreProperty(LDAPConstants.DISPLAY_NAME_ATTRIBUTE);
                }

                value = uerProperties.get(property);
                if (value != null && value.trim().length() > 0) {
                    finalValues.put(claim, value);
                }
            }
        }

        if (getAgain.size() > 0) {
            // oh the beautiful recursion
            Map<String, String> mapClaimValues = this.getUserClaimValues(userName,
                    (String[]) getAgain.toArray(new String[getAgain.size()]),
                    profileName);

            Iterator<Map.Entry<String, String>> ite3 = mapClaimValues.entrySet().iterator();
            while (ite3.hasNext()) {
                Map.Entry<String, String> entry = ite3.next();
                if (entry.getValue() != null) {
                    finalValues.put(entry.getKey(), entry.getValue());
                }
            }
        }

        // We treat roles claim in special way.
        String[] roles = null;

        if (requireRoles) {
            roles = getRoleListOfUser(userName);
        } else if (requireIntRoles) {
            roles = doGetInternalRoleListOfUser(userName, "*");
        } else if (requireExtRoles) {

            List<String> rolesList = new ArrayList<String>();
            String[] externalRoles = doGetExternalRoleListOfUser(userName, "*");
            rolesList.addAll(Arrays.asList(externalRoles));
            //if only shared enable
            if (isSharedGroupEnabled()) {
                String[] sharedRoles = doGetSharedRoleListOfUser(userName, null, "*");
                if (sharedRoles != null) {
                    rolesList.addAll(Arrays.asList(sharedRoles));
                }
            }

            roles = rolesList.toArray(new String[rolesList.size()]);
        }

        if (roles != null && roles.length > 0) {
            String userAttributeSeparator = ",";
            String claimSeparator = realmConfig.getUserStoreProperty(MULTI_ATTRIBUTE_SEPARATOR);
            if (claimSeparator != null && !claimSeparator.trim().isEmpty()) {
                userAttributeSeparator = claimSeparator;
            }
            String delim = "";
            StringBuffer roleBf = new StringBuffer();
            for (String role : roles) {
                roleBf.append(delim).append(role);
                delim = userAttributeSeparator;
            }
            finalValues.put(roleClaim, roleBf.toString());
        }

        return finalValues;
    }

    /**
     * @return
     */
    protected String getEveryOneRoleName() {
        return realmConfig.getEveryOneRoleName();
    }

    /**
     * @return
     */
    protected String getAdminRoleName() {
        return realmConfig.getAdminRoleName();
    }

    /**
     * @param credential
     * @return
     * @throws UserStoreException
     */
    protected boolean checkUserPasswordValid(Object credential) throws UserStoreException {

        if (!isSecureCall.get()) {
            Class argTypes[] = new Class[]{Object.class};
            Object object = callSecure("checkUserPasswordValid", new Object[]{credential}, argTypes);
            return (Boolean) object;
        }

        if (credential == null) {
            return false;
        }

        Secret credentialObj;
        try {
            credentialObj = Secret.getSecret(credential);
        } catch (UnsupportedSecretTypeException e) {
            throw new UserStoreException("Unsupported credential type", e);
        }

        try {
            if (credentialObj.getChars().length < 1) {
                return false;
            }

            String regularExpression =
                    realmConfig.getUserStoreProperty(UserCoreConstants.RealmConfig.PROPERTY_JAVA_REG_EX);
            if (regularExpression != null) {
                if (isFormatCorrect(regularExpression, credentialObj.getChars())) {
                    return true;
                } else {
                    if (log.isDebugEnabled()) {
                        log.debug("Submitted password does not match with the regex " + regularExpression);
                    }
                    return false;
                }
            }
            return true;
        } finally {
            credentialObj.clear();
        }

    }

    /**
     * @param userName
     * @return
     * @throws UserStoreException
     */
    protected boolean checkUserNameValid(String userName) throws UserStoreException {

        if (!isSecureCall.get()) {
            Class argTypes[] = new Class[]{String.class};
            Object object = callSecure("checkUserNameValid", new Object[]{userName}, argTypes);
            return (Boolean) object;
        }

        if (userName == null || UserCoreConstants.REGISTRY_SYSTEM_USERNAME.equals(userName)) {
            return false;
        }

        String leadingOrTrailingSpaceAllowedInUserName = realmConfig.getUserStoreProperty(UserCoreConstants
                .RealmConfig.LEADING_OR_TRAILING_SPACE_ALLOWED_IN_USERNAME);
        if (StringUtils.isEmpty(leadingOrTrailingSpaceAllowedInUserName)) {
            // Keeping old behavior for backward-compatibility.
            userName = userName.trim();
        } else {
            if (log.isDebugEnabled()) {
                log.debug("'LeadingOrTrailingSpaceAllowedInUserName' property is set to : " +
                        leadingOrTrailingSpaceAllowedInUserName + ". Hence username trimming will be skipped during " +
                        "validation for the username: " + userName);
            }
        }

        if (userName.length() < 1) {
            return false;
        }

        String regularExpression = realmConfig
                .getUserStoreProperty(UserCoreConstants.RealmConfig.PROPERTY_USER_NAME_JAVA_REG_EX);
        //Inorder to support both UsernameJavaRegEx and UserNameJavaRegEx.
        if (StringUtils.isEmpty(regularExpression) || StringUtils.isEmpty(regularExpression.trim())) {
            regularExpression = realmConfig.getUserStoreProperty(UserCoreConstants.RealmConfig.PROPERTY_USER_NAME_JAVA_REG);
        }

        if (UserCoreUtil.isEmailUserName()) {
            regularExpression = realmConfig
                    .getUserStoreProperty(UserCoreConstants.RealmConfig.PROPERTY_USER_NAME_WITH_EMAIL_JS_REG_EX);

            if (StringUtils.isEmpty(regularExpression) || StringUtils.isEmpty(regularExpression.trim())) {
                regularExpression = realmConfig.getUserStoreProperty(UserCoreConstants.RealmConfig
                        .PROPERTY_USER_NAME_JAVA_REG_EX);
            }

            //Inorder to support both UsernameJavaRegEx and UserNameJavaRegEx.
            if (StringUtils.isEmpty(regularExpression) || StringUtils.isEmpty(regularExpression.trim())) {
                regularExpression = realmConfig.getUserStoreProperty(UserCoreConstants.RealmConfig.PROPERTY_USER_NAME_JAVA_REG);
            }

            if (StringUtils.isEmpty(regularExpression) || StringUtils.isEmpty(regularExpression.trim())) {
                regularExpression = UserCoreConstants.RealmConfig.EMAIL_VALIDATION_REGEX;
            }
        }

        if (regularExpression != null) {
            regularExpression = regularExpression.trim();
        }

        if (StringUtils.isNotEmpty(regularExpression)) {
            if (isFormatCorrect(regularExpression, userName)) {
                return true;
            } else {
                if (log.isDebugEnabled()) {
                    log.debug("Username " + userName + " does not match with the regex "
                            + regularExpression);
                }
                return false;
            }
        }
        return true;
    }

    /**
     * @param roleName
     * @return
     */
    protected boolean isRoleNameValid(String roleName) {
        if (roleName == null) {
            return false;
        }

        if (roleName.length() < 1) {
            return false;
        }

        String regularExpression = realmConfig
                .getUserStoreProperty(UserCoreConstants.RealmConfig.PROPERTY_ROLE_NAME_JAVA_REG_EX);
        if (regularExpression != null) {
            if (!isFormatCorrect(regularExpression, roleName)) {
                return false;
            }
        }

        return true;
    }

    /**
     * @param tenantID
     * @param userName
     * @return
     */
    protected String[] getRoleListOfUserFromCache(int tenantID, String userName) {
        if (userRolesCache != null) {
            String usernameWithDomain = UserCoreUtil.addDomainToName(userName, getMyDomainName());
            return userRolesCache.getRolesListOfUser(cacheIdentifier, tenantID, usernameWithDomain);
        }
        return null;
    }

    /**
     * @param tenantID
     */
    protected void clearUserRolesCacheByTenant(int tenantID) {
        if (userRolesCache != null) {
            userRolesCache.clearCacheByTenant(tenantID);
        }
        AuthorizationCache authorizationCache = AuthorizationCache.getInstance();
        authorizationCache.clearCacheByTenant(tenantID);
    }

    /**
     * @param userName
     */
    protected void clearUserRolesCache(String userName) {
        String usernameWithDomain = UserCoreUtil.addDomainToName(userName, getMyDomainName());
        if (userRolesCache != null) {
            userRolesCache.clearCacheEntry(cacheIdentifier, tenantId, usernameWithDomain);
        }
        AuthorizationCache authorizationCache = AuthorizationCache.getInstance();
        authorizationCache.clearCacheByUser(tenantId, usernameWithDomain);
    }

    /**
     * @param tenantID
     * @param userName
     * @param roleList
     */
    protected void addToUserRolesCache(int tenantID, String userName, String[] roleList) {
        if (userRolesCache != null) {
            String usernameWithDomain = UserCoreUtil.addDomainToName(userName, getMyDomainName());
            String[] rolesWithDomain = UserCoreUtil.addDomainToNames(roleList, getMyDomainName());
            userRolesCache.addToCache(cacheIdentifier, tenantID, usernameWithDomain, rolesWithDomain);
            AuthorizationCache authorizationCache = AuthorizationCache.getInstance();
            authorizationCache.clearCacheByTenant(tenantID);
        }
    }

    /**
     * {@inheritDoc}
     */
    protected void initUserRolesCache() {

        String userRolesCacheEnabledString = (realmConfig
                .getUserStoreProperty(UserCoreConstants.RealmConfig.PROPERTY_ROLES_CACHE_ENABLED));

        String userCoreCacheIdentifier = realmConfig
                .getUserStoreProperty(UserCoreConstants.RealmConfig.PROPERTY_USER_CORE_CACHE_IDENTIFIER);

        if (userCoreCacheIdentifier != null && userCoreCacheIdentifier.trim().length() > 0) {
            cacheIdentifier = userCoreCacheIdentifier;
        } else {
            cacheIdentifier = UserCoreConstants.DEFAULT_CACHE_IDENTIFIER;
        }

        if (userRolesCacheEnabledString != null && !userRolesCacheEnabledString.equals("")) {
            userRolesCacheEnabled = Boolean.parseBoolean(userRolesCacheEnabledString);
            if (log.isDebugEnabled()) {
                log.debug("User Roles Cache is configured to:" + userRolesCacheEnabledString);
            }
        } else {
            if (log.isDebugEnabled()) {
                log.info("User Roles Cache is not configured. Default value: "
                        + userRolesCacheEnabled + " is taken.");
            }
        }

        if (userRolesCacheEnabled) {
            int timeOut = UserCoreConstants.USER_ROLE_CACHE_DEFAULT_TIME_OUT;
            String timeOutString = realmConfig.
                    getUserStoreProperty(UserCoreConstants.RealmConfig.PROPERTY_USER_ROLE_CACHE_TIME_OUT);
            if (timeOutString != null) {
                timeOut = Integer.parseInt(timeOutString);
            }
            userRolesCache = UserRolesCache.getInstance();
            userRolesCache.setTimeOut(timeOut);
        }

    }

    /**
     * @param regularExpression
     * @param attribute
     * @return
     */
    private boolean isFormatCorrect(String regularExpression, String attribute) {
        Pattern p2 = Pattern.compile(regularExpression);
        Matcher m2 = p2.matcher(attribute);
        return m2.matches();
    }

    private boolean isFormatCorrect(String regularExpression, char[] attribute) {

        boolean matches;
        CharBuffer charBuffer = CharBuffer.wrap(attribute);

        Pattern p2 = Pattern.compile(regularExpression);
        Matcher m2 = p2.matcher(charBuffer);
        matches = m2.matches();

        return matches;
    }

    /**
     * This is to replace escape characters in user name at user login if replace escape characters
     * enabled in user-mgt.xml. Some User Stores like ApacheDS stores user names by replacing escape
     * characters. In that case, we have to parse the username accordingly.
     *
     * @param userName
     */
    protected String replaceEscapeCharacters(String userName) {

        if (log.isDebugEnabled()) {
            log.debug("Replacing escape characters in " + userName);
        }
        String replaceEscapeCharactersAtUserLoginString = realmConfig
                .getUserStoreProperty(UserCoreConstants.RealmConfig.PROPERTY_REPLACE_ESCAPE_CHARACTERS_AT_USER_LOGIN);

        if (replaceEscapeCharactersAtUserLoginString != null) {
            replaceEscapeCharactersAtUserLogin = Boolean
                    .parseBoolean(replaceEscapeCharactersAtUserLoginString);
            if (log.isDebugEnabled()) {
                log.debug("Replace escape characters at userlogin is configured to: "
                        + replaceEscapeCharactersAtUserLoginString);
            }
            if (replaceEscapeCharactersAtUserLogin) {
                // Currently only '\' & '\\' are identified as escape characters
                // that needs to be
                // replaced.
                return userName.replaceAll("\\\\", "\\\\\\\\");
            }
        }
        return userName;
    }

    /**
     * TODO: Remove this method. We should not use DTOs
     *
     * @return
     * @throws UserStoreException
     */
    public RoleDTO[] getAllSecondaryRoleDTOs() throws UserStoreException {

        if (!isSecureCall.get()) {
            Class argTypes[] = new Class[]{};
            Object object = callSecure("getAllSecondaryRoleDTOs", new Object[]{}, argTypes);
            return (RoleDTO[]) object;
        }

        UserStoreManager secondary = this.getSecondaryUserStoreManager();
        List<RoleDTO> roleList = new ArrayList<RoleDTO>();
        while (secondary != null) {
            String domain = secondary.getRealmConfiguration().getUserStoreProperty(
                    UserCoreConstants.RealmConfig.PROPERTY_DOMAIN_NAME);
            String[] roles = secondary.getRoleNames(true);
            if (roles != null && roles.length > 0) {
                Collections.addAll(roleList, UserCoreUtil.convertRoleNamesToRoleDTO(roles, domain));
            }
            secondary = secondary.getSecondaryUserStoreManager();
        }
        return roleList.toArray(new RoleDTO[roleList.size()]);
    }

    /**
     * @param roleName
     * @param userList
     * @param permissions
     * @throws UserStoreException
     */
    public void addSystemRole(String roleName, String[] userList, Permission[] permissions)
            throws UserStoreException {

        if (!isRoleNameValid(roleName)) {
            String regEx = realmConfig
                    .getUserStoreProperty(UserCoreConstants.RealmConfig.PROPERTY_ROLE_NAME_JAVA_REG_EX);
            String errorCode = ErrorMessages.ERROR_CODE_INVALID_ROLE_NAME.getCode();
            String errorMessage = String
                    .format(ErrorMessages.ERROR_CODE_INVALID_ROLE_NAME.getMessage(), roleName, regEx);
            handleAddRoleFailure(errorCode, errorMessage, roleName, userList, permissions);
            throw new UserStoreException(errorCode + " - " + errorMessage);
        }

        if (systemUserRoleManager.isExistingRole(roleName)) {
            handleRoleAlreadyExistException(roleName, userList, permissions);
        }
        systemUserRoleManager.addSystemRole(roleName, userList);
    }


    /**
     * @param roleName
     * @param filter
     * @return
     * @throws UserStoreException
     */
    protected abstract String[] doGetUserListOfRole(String roleName, String filter)
            throws UserStoreException;

    /**
     * @param userName
     * @param filter
     * @return
     * @throws UserStoreException
     */
    public final String[] doGetRoleListOfUser(String userName, String filter)
            throws UserStoreException {

        if (!isSecureCall.get()) {
            Class argTypes[] = new Class[]{String.class, String.class};
            Object object = callSecure("doGetRoleListOfUser", new Object[]{userName, filter}, argTypes);
            return (String[]) object;
        }

        String[] roleList = getRoleListOfUserFromCache(this.tenantId, userName);
        if (roleList != null && roleList.length > 0) {
            return roleList;
        }

        String[] internalRoles = doGetInternalRoleListOfUser(userName, filter);

        String[] modifiedExternalRoleList = new String[0];

        if (readGroupsEnabled && doCheckExistingUser(userName)) {
            List<String> roles = new ArrayList<String>();
            String[] externalRoles = doGetExternalRoleListOfUser(userName, "*");
            roles.addAll(Arrays.asList(externalRoles));
            if (isSharedGroupEnabled()) {
                String[] sharedRoles = doGetSharedRoleListOfUser(userName, null, "*");
                if (sharedRoles != null) {
                    roles.addAll(Arrays.asList(sharedRoles));
                }
            }
            modifiedExternalRoleList =
                    UserCoreUtil.addDomainToNames(roles.toArray(new String[roles.size()]),
                            getMyDomainName());
        }

        roleList = UserCoreUtil.combine(internalRoles, Arrays.asList(modifiedExternalRoleList));

        for (UserOperationEventListener userOperationEventListener : UMListenerServiceComponent
                .getUserOperationEventListeners()) {
            if (userOperationEventListener instanceof AbstractUserOperationEventListener) {
                if (!((AbstractUserOperationEventListener) userOperationEventListener)
                        .doPostGetRoleListOfUser(userName, filter, roleList, this)) {
                    break;
                }
            }
        }
        return roleList;
    }

    /**
     * @param filter
     * @return
     * @throws UserStoreException
     */
    public final String[] getHybridRoles(String filter) throws UserStoreException {
        return hybridRoleManager.getHybridRoles(filter);
    }

    /**
     * @param claimList
     * @return
     * @throws UserStoreException
     */
    protected List<String> getMappingAttributeList(List<String> claimList)
            throws UserStoreException {
        ArrayList<String> attributeList = null;
        Iterator<String> claimIter = null;

        attributeList = new ArrayList<String>();
        if (claimList == null) {
            return attributeList;
        }
        claimIter = claimList.iterator();
        while (claimIter.hasNext()) {
            try {
                attributeList.add(claimManager.getAttributeName(claimIter.next()));
            } catch (org.wso2.micro.integrator.security.user.api.UserStoreException e) {
                throw new UserStoreException(e);
            }
        }
        return attributeList;
    }

    protected void doInitialSetup() throws UserStoreException {
        systemUserRoleManager = new SystemUserRoleManager(dataSource, tenantId);
        hybridRoleManager = new JdbcHybridRoleManager(dataSource, tenantId, realmConfig, userRealm);
    }

    /**
     * @return whether this is the initial startup
     * @throws UserStoreException
     */
    protected void doInitialUserAdding() throws UserStoreException {

        String systemUser = UserCoreUtil.removeDomainFromName(UserCoreConstants.REGISTRY_ANONNYMOUS_USERNAME);
        String systemRole = UserCoreUtil.removeDomainFromName(UserCoreConstants.REGISTRY_ANONNYMOUS_ROLE_NAME);

        if (!systemUserRoleManager.isExistingSystemUser(systemUser)) {
            try {
                systemUserRoleManager.addSystemUser(systemUser,
                        UserCoreUtil.getPolicyFriendlyRandomPassword(systemUser), null);
            } catch (UserStoreException e) {
                if (ERROR_CODE_DUPLICATE_WHILE_ADDING_A_SYSTEM_USER.getCode().equals(e.getErrorCode())) {
                    log.warn(String.format("System User :%s has already added. Hence, continue without adding the " +
                                    "user.", systemUser));
                } else {
                    throw e;
                }
            }

        }

        if (!systemUserRoleManager.isExistingRole(systemRole)) {
            try {
                systemUserRoleManager.addSystemRole(systemRole, new String[]{systemUser});
            } catch (UserStoreException e) {
                if (ERROR_CODE_DUPLICATE_WHILE_ADDING_A_SYSTEM_ROLE.getCode().equals(e.getErrorCode())) {
                    log.warn(String.format("System Role :%s is already added. Hence, continue without adding the " +
                                    "role.", systemRole), e);
                } else {
                    throw e;
                }
            }
        }

        if (!hybridRoleManager.isExistingRole(UserCoreUtil.removeDomainFromName(realmConfig
                .getEveryOneRoleName()))) {
            try {
                hybridRoleManager.addHybridRole(
                        UserCoreUtil.removeDomainFromName(realmConfig.getEveryOneRoleName()), null);
            } catch (UserStoreException e) {
                if (ERROR_CODE_DUPLICATE_WHILE_ADDING_A_HYBRID_ROLE.getCode().equals(e.getErrorCode())) {
                    log.warn(String.format("Hybrid Role :%s is already added. Hence, continue without adding the " +
                            "role.", systemRole));
                } else {
                    throw e;
                }
            }
        }
    }


    protected boolean isInitSetupDone() throws UserStoreException {

        boolean isInitialSetUp = false;
        String systemUser = UserCoreUtil.removeDomainFromName(UserCoreConstants.REGISTRY_ANONNYMOUS_USERNAME);
        String systemRole = UserCoreUtil.removeDomainFromName(UserCoreConstants.REGISTRY_ANONNYMOUS_ROLE_NAME);

        if (systemUserRoleManager.isExistingSystemUser(systemUser)) {
            isInitialSetUp = true;
        }

        if (systemUserRoleManager.isExistingRole(systemRole)) {
            isInitialSetUp = true;
        }

        return isInitialSetUp;
    }

    /**
     * @throws UserStoreException
     */
    protected void addInitialAdminData(boolean addAdmin, boolean initialSetup) throws UserStoreException {

        if (realmConfig.getAdminRoleName() == null || realmConfig.getAdminUserName() == null) {
            log.error("Admin user name or role name is not valid. Please provide valid values.");
            throw new UserStoreException(
                    "Admin user name or role name is not valid. Please provide valid values.");
        }
        String adminUserName = UserCoreUtil.removeDomainFromName(realmConfig.getAdminUserName());
        String adminRoleName = UserCoreUtil.removeDomainFromName(realmConfig.getAdminRoleName());
        boolean userExist = false;
        boolean roleExist = false;
        boolean isInternalRole = false;

        try {
            if (Boolean.parseBoolean(this.getRealmConfiguration().getUserStoreProperty(
                    UserCoreConstants.RealmConfig.READ_GROUPS_ENABLED))) {
                roleExist = doCheckExistingRole(adminRoleName);
            }
        } catch (Exception e) {
            //ignore
        }

        if (!roleExist) {
            try {
                roleExist = hybridRoleManager.isExistingRole(adminRoleName);
            } catch (Exception e) {
                //ignore
            }
            if (roleExist) {
                isInternalRole = true;
            }
        }

        try {
            userExist = doCheckExistingUser(adminUserName);
        } catch (Exception e) {
            //ignore
        }

        if (!userExist) {
            if (isReadOnly()) {
                String message = "Admin user cannot be created in primary user store. " +
                        "User store is read only. " +
                        "Please pick a user name which exists in the primary user store as Admin user";
                if (initialSetup) {
                    throw new UserStoreException(message);
                } else if (log.isDebugEnabled()) {
                    log.error(message);
                }
            } else if (addAdmin) {
                try {
                    this.doAddUser(adminUserName, realmConfig.getAdminPassword(),
                            null, null, null, false);
                } catch (Exception e) {
                    String message = "Admin user has not been created. " +
                            "Error occurs while creating Admin user in primary user store.";
                    if (initialSetup) {
                        if (e instanceof UserStoreException && ERROR_CODE_DUPLICATE_WHILE_ADDING_A_USER.getCode
                                ().equals(((UserStoreException) e).getErrorCode())) {
                            log.warn(String.format("Admin User :%s is already added. Hence, continue without adding" +
                                    " the user.", adminUserName));
                        } else {
                            throw new UserStoreException(message, e);
                        }
                    } else if (log.isDebugEnabled()) {
                        log.error(message, e);
                    }
                }
            } else {
                if (initialSetup) {
                    String message = "Admin user cannot be created in primary user store. " +
                            "Add-Admin has been set to false. " +
                            "Please pick a User name which exists in the primary user store as Admin user";
                    if (initialSetup) {
                        throw new UserStoreException(message);
                    } else if (log.isDebugEnabled()) {
                        log.error(message);
                    }
                }
            }
        }


        if (!roleExist) {
            if (addAdmin) {
                if (!isReadOnly() && writeGroupsEnabled) {
                    try {
                        this.doAddRole(adminRoleName, new String[]{adminUserName}, false);
                    } catch (org.wso2.micro.integrator.security.user.api.UserStoreException e) {
                        String message = "Admin role has not been created. " +
                                "Error occurs while creating Admin role in primary user store.";
                        if (initialSetup) {
                            if (ERROR_CODE_DUPLICATE_WHILE_ADDING_ROLE.getCode().equals(((UserStoreException) e)
                                    .getErrorCode())) {
                                log.warn(String.format("Admin Role :%s is already added. Hence, continue without " +
                                        "adding the role.", adminRoleName));
                            } else {
                                throw new UserStoreException(message, e);
                            }
                        } else if (log.isDebugEnabled()) {
                            log.error(message, e);
                        }
                    }
                } else {
                    // creates internal role
                    try {
                        hybridRoleManager.addHybridRole(adminRoleName, new String[]{adminUserName});
                        isInternalRole = true;
                    } catch (Exception e) {
                        String message = "Admin role has not been created. " +
                                "Error occurs while creating Admin role in primary user store.";
                        if (initialSetup) {
                            if (ERROR_CODE_DUPLICATE_WHILE_ADDING_A_HYBRID_ROLE.getCode().equals(((UserStoreException) e)
                                    .getErrorCode())) {
                                log.warn(String.format("Hybrid Admin Role :%s is already added. Hence, continue " +
                                        "without adding the hybrid role.", adminRoleName));
                            } else {
                                throw new UserStoreException(message, e);
                            }
                        } else if (log.isDebugEnabled()) {
                            log.error(message, e);
                        }
                    }
                }
            } else {
                String message = "Admin role cannot be created in primary user store. " +
                        "Add-Admin has been set to false. " +
                        "Please pick a Role name which exists in the primary user store as Admin Role";
                if (initialSetup) {
                    throw new UserStoreException(message);
                } else if (log.isDebugEnabled()) {
                    log.error(message);
                }
            }
        }


        if (isInternalRole) {
            if (!hybridRoleManager.isUserInRole(adminUserName, adminRoleName)) {
                try {
                    hybridRoleManager.updateHybridRoleListOfUser(adminUserName, null,
                            new String[]{adminRoleName});
                } catch (Exception e) {
                    String message = "Admin user has not been assigned to Admin role. " +
                            "Error while assignment is done";
                    if (initialSetup) {
                        throw new UserStoreException(message, e);
                    } else if (log.isDebugEnabled()) {
                        log.error(message, e);
                    }
                }
            }
            realmConfig.setAdminRoleName(UserCoreUtil.addInternalDomainName(adminRoleName));
        } else if (!isReadOnly() && writeGroupsEnabled) {
            if (!this.doCheckIsUserInRole(adminUserName, adminRoleName)) {
                if (addAdmin) {
                    try {
                        this.doUpdateRoleListOfUser(adminUserName, null,
                                new String[]{adminRoleName});
                    } catch (Exception e) {
                        String message = "Admin user has not been assigned to Admin role. " +
                                "Error while assignment is done";
                        if (initialSetup) {
                            throw new UserStoreException(message, e);
                        } else if (log.isDebugEnabled()) {
                            log.error(message, e);
                        }
                    }
                } else {
                    String message = "Admin user cannot be assigned to Admin role " +
                            "Add-Admin has been set to false. Please do the assign it in user store level";
                    if (initialSetup) {
                        throw new UserStoreException(message);
                    } else if (log.isDebugEnabled()) {
                        log.error(message);
                    }
                }
            }
        }

        doInitialUserAdding();
    }

    /**
     * @param type
     * @return
     * @throws UserStoreException
     */
    public Map<String, Integer> getMaxListCount(String type) throws UserStoreException {

        if (!type.equals(UserCoreConstants.RealmConfig.PROPERTY_MAX_USER_LIST)
                && !type.equals(UserCoreConstants.RealmConfig.PROPERTY_MAX_ROLE_LIST)) {
            throw new UserStoreException("Invalid count parameter");
        }

        if (type.equals(UserCoreConstants.RealmConfig.PROPERTY_MAX_USER_LIST)
                && maxUserListCount != null) {
            return maxUserListCount;
        }

        if (type.equals(UserCoreConstants.RealmConfig.PROPERTY_MAX_ROLE_LIST)
                && maxRoleListCount != null) {
            return maxRoleListCount;
        }

        Map<String, Integer> maxListCount = new HashMap<String, Integer>();
        for (Map.Entry<String, UserStoreManager> entry : userStoreManagerHolder.entrySet()) {
            UserStoreManager storeManager = entry.getValue();
            String maxConfig = storeManager.getRealmConfiguration().getUserStoreProperty(type);

            if (maxConfig == null) {
                // set a default value
                maxConfig = MAX_LIST_LENGTH;
            }
            maxListCount.put(entry.getKey(), Integer.parseInt(maxConfig));
        }

        if (realmConfig.getUserStoreProperty(UserCoreConstants.RealmConfig.PROPERTY_DOMAIN_NAME) == null) {
            String maxConfig = realmConfig.getUserStoreProperty(type);
            if (maxConfig == null) {
                // set a default value
                maxConfig = MAX_LIST_LENGTH;
            }
            maxListCount.put(null, Integer.parseInt(maxConfig));
        }

        if (type.equals(UserCoreConstants.RealmConfig.PROPERTY_MAX_USER_LIST)) {
            this.maxUserListCount = maxListCount;
            return this.maxUserListCount;
        } else if (type.equals(UserCoreConstants.RealmConfig.PROPERTY_MAX_ROLE_LIST)) {
            this.maxRoleListCount = maxListCount;
            return this.maxRoleListCount;
        } else {
            throw new UserStoreException("Invalid count parameter");
        }
    }

    /**
     * @return
     */
    protected String getMyDomainName() {
        return UserCoreUtil.getDomainName(realmConfig);
    }

    protected void persistDomain() throws UserStoreException {
        String domain = UserCoreUtil.getDomainName(this.realmConfig);
        if (domain != null) {
            UserCoreUtil.persistDomain(domain, this.tenantId, this.dataSource);
        }
    }

    public void deletePersistedDomain(String domain) throws UserStoreException {
        if (domain != null) {
            if (log.isDebugEnabled()) {
                log.debug("Deleting persisted domain " + domain);
            }
            UserCoreUtil.deletePersistedDomain(domain, this.tenantId, this.dataSource);
        }
    }

    public void updatePersistedDomain(String oldDomain, String newDomain) throws UserStoreException {
        if (oldDomain != null && newDomain != null) {
            // Checks for the newDomain exists already
            // Traverse through realm configuration chain since USM chain doesn't contains the disabled USMs
            RealmConfiguration realmConfigTmp = this.getRealmConfiguration();
            while (realmConfigTmp != null) {
                String domainName = realmConfigTmp.getUserStoreProperty(UserCoreConstants.RealmConfig.PROPERTY_DOMAIN_NAME);
                if (newDomain.equalsIgnoreCase(domainName)) {
                    throw new UserStoreException("Cannot update persisted domain name " + oldDomain + " into " + newDomain + ". New domain name already in use");
                }
                realmConfigTmp = realmConfigTmp.getSecondaryRealmConfig();
            }

            if (log.isDebugEnabled()) {
                log.debug("Renaming persisted domain " + oldDomain + " to " + newDomain);
            }
            UserCoreUtil.updatePersistedDomain(oldDomain, newDomain, this.tenantId, this.dataSource);

        }
    }

    /**
     * Checks whether the role is a shared role or not
     *
     * @param roleName
     * @param roleNameBase
     * @return
     */
    public boolean isSharedRole(String roleName, String roleNameBase) {

        // Only checks the shared groups are enabled
        return isSharedGroupEnabled();
    }

    /**
     * Checks whether the provided role name belongs to the logged in tenant.
     * This check is done using the domain name which is appended at the end of
     * the role name
     *
     * @param roleName
     * @return
     */
    protected boolean isOwnRole(String roleName) {
        return true;
    }

    @Override
    public void addRole(String roleName, String[] userList,
                        org.wso2.micro.integrator.security.user.api.Permission[] permissions)
            throws org.wso2.micro.integrator.security.user.api.UserStoreException {
        addRole(roleName, userList, permissions, false);

    }

    public boolean isOthersSharedRole(String roleName) {
        return false;
    }

    public void notifyListeners(String domainName) {
        for (UserStoreManagerConfigurationListener aListener : listener) {
            aListener.propertyChange(domainName);
        }
    }

    public void addChangeListener(UserStoreManagerConfigurationListener newListener) {
        listener.add(newListener);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private UserStoreManager createSecondaryUserStoreManager(RealmConfiguration realmConfig,
                                                             UserRealm realm) throws UserStoreException {

        if (!isSecureCall.get()) {
            Class argTypes[] = new Class[]{RealmConfiguration.class, UserRealm.class};
            Object object = callSecure("createSecondaryUserStoreManager", new Object[]{realmConfig, realm}, argTypes);
            return (UserStoreManager) object;
        }

        // setting global realm configurations such as everyone role, admin role and admin user
        realmConfig.setEveryOneRoleName(this.realmConfig.getEveryOneRoleName());
        realmConfig.setAdminUserName(this.realmConfig.getAdminUserName());
        realmConfig.setAdminRoleName(this.realmConfig.getAdminRoleName());

        String className = realmConfig.getUserStoreClass();
        if (className == null) {
            String errmsg = "Unable to add user store. UserStoreManager class name is null.";
            log.error(errmsg);
            throw new UserStoreException(errmsg);
        }

        HashMap<String, Object> properties = new HashMap<String, Object>();
        properties.put(UserCoreConstants.DATA_SOURCE, this.dataSource);
        properties.put(UserCoreConstants.FIRST_STARTUP_CHECK, false);

        Class[] initClassOpt1 = new Class[]{RealmConfiguration.class, Map.class,
                ClaimManager.class, ProfileConfigurationManager.class, UserRealm.class,
                Integer.class};
        Object[] initObjOpt1 = new Object[]{realmConfig, properties, realm.getClaimManager(), null, realm,
                tenantId};

        // These two methods won't be used
        Class[] initClassOpt2 = new Class[]{RealmConfiguration.class, Map.class,
                ClaimManager.class, ProfileConfigurationManager.class, UserRealm.class};
        Object[] initObjOpt2 = new Object[]{realmConfig, properties, realm.getClaimManager(), null, realm};

        Class[] initClassOpt3 = new Class[]{RealmConfiguration.class, Map.class};
        Object[] initObjOpt3 = new Object[]{realmConfig, properties};

        try {
            Class clazz = Class.forName(className);
            Constructor constructor = null;
            Object newObject = null;

            if (log.isDebugEnabled()) {
                log.debug("Start initializing class with the first option");
            }

            try {
                constructor = clazz.getConstructor(initClassOpt1);
                newObject = constructor.newInstance(initObjOpt1);
                return (UserStoreManager) newObject;
            } catch (NoSuchMethodException e) {
                // if not found try again.
                if (log.isDebugEnabled()) {
                    log.debug("Cannont initialize " + className + " using the option 1");
                }
            }

            if (log.isDebugEnabled()) {
                log.debug("End initializing class with the first option");
            }

            try {
                constructor = clazz.getConstructor(initClassOpt2);
                newObject = constructor.newInstance(initObjOpt2);
                return (UserStoreManager) newObject;
            } catch (NoSuchMethodException e) {
                // if not found try again.
                if (log.isDebugEnabled()) {
                    log.debug("Cannot initialize " + className + " using the option 2");
                }
            }

            if (log.isDebugEnabled()) {
                log.debug("End initializing class with the second option");
            }

            try {
                constructor = clazz.getConstructor(initClassOpt3);
                newObject = constructor.newInstance(initObjOpt3);
                return (UserStoreManager) newObject;
            } catch (NoSuchMethodException e) {
                // cannot initialize in any of the methods. Throw exception.
                String message = "Cannot initialize " + className + ". Error " + e.getMessage();
                log.error(message);
                throw new UserStoreException(message);
            }

        } catch (Throwable e) {
            log.error("Cannot create " + className, e);
            throw new UserStoreException(e.getMessage() + "Type " + e.getClass(), e);
        }

    }

    /**
     * Adding new User Store Manager to USM chain
     *
     * @param userStoreRealmConfig
     * @param realm
     * @throws UserStoreException
     */
    public void addSecondaryUserStoreManager(RealmConfiguration userStoreRealmConfig,
                                             UserRealm realm) throws UserStoreException {

        if (!isSecureCall.get()) {
            Class argTypes[] = new Class[]{RealmConfiguration.class, UserRealm.class};
            callSecure("addSecondaryUserStoreManager", new Object[]{userStoreRealmConfig, realm}, argTypes);
            return;
        }

        boolean isDisabled = Boolean.parseBoolean(userStoreRealmConfig
                .getUserStoreProperty(UserCoreConstants.RealmConfig.USER_STORE_DISABLED));

        String domainName = userStoreRealmConfig
                .getUserStoreProperty(UserCoreConstants.RealmConfig.PROPERTY_DOMAIN_NAME);

        if (isDisabled) {
            log.warn("Secondary user store disabled with domain " + domainName + ".");
        } else {
            // Creating new UserStoreManager
            UserStoreManager manager = createSecondaryUserStoreManager(userStoreRealmConfig, realm);

            if (domainName != null) {
                if (this.getSecondaryUserStoreManager(domainName) != null) {
                    String errmsg = "Could not initialize new user store manager : " + domainName
                            + " Duplicate domain names not allowed.";
                    if (log.isDebugEnabled()) {
                        log.debug(errmsg);
                    }
                    throw new UserStoreException(errmsg);
                } else {
                    // Fulfilled requirements for adding UserStore,

                    // Now adding UserStoreManager to end of the UserStoreManager chain
                    UserStoreManager tmpUserStoreManager = this;
                    while (tmpUserStoreManager.getSecondaryUserStoreManager() != null) {
                        tmpUserStoreManager = tmpUserStoreManager.getSecondaryUserStoreManager();
                    }
                    tmpUserStoreManager.setSecondaryUserStoreManager(manager);

                    // update domainName-USM map to retrieve USM directly by its domain name
                    this.addSecondaryUserStoreManager(domainName.toUpperCase(), tmpUserStoreManager.getSecondaryUserStoreManager());

                    if (log.isDebugEnabled()) {
                        log.debug("UserStoreManager : " + domainName + "added to the list");
                    }
                }
            } else {
                log.warn("Could not initialize new user store manager.  "
                        + "Domain name is not defined");
            }
        }
    }

    @Override
    public Map<String, List<String>> getRoleListOfUsers(String[] userNames) throws UserStoreException {

        if (!isSecureCall.get()) {
            Class argTypes[] = new Class[] { String[].class };
            Object object = callSecure("getRoleListOfUsers", new Object[] { userNames }, argTypes);
            return (Map<String, List<String>>) object;
        }

        Map<String, List<String>> allRoleNames = new HashMap<>();
        Map<String, List<String>> domainFreeUsers = getDomainFreeUsers(userNames);

        for (Map.Entry<String, List<String>> entry : domainFreeUsers.entrySet()) {
            UserStoreManager secondaryUserStoreManager = getSecondaryUserStoreManager(entry.getKey());
            if (secondaryUserStoreManager instanceof AbstractUserStoreManager) {
                Map<String, List<String>> roleNames = ((AbstractUserStoreManager) secondaryUserStoreManager)
                        .doGetRoleListOfUsers(entry.getValue(), entry.getKey());
                allRoleNames.putAll(roleNames);
            }
        }

        for (UserOperationEventListener listener : UMListenerServiceComponent.getUserOperationEventListeners()) {
            if (listener instanceof AbstractUserOperationEventListener) {
                AbstractUserOperationEventListener newListener = (AbstractUserOperationEventListener) listener;
                if (!newListener.doPostGetRoleListOfUsers(userNames, allRoleNames)) {
                    break;
                }
            }
        }

        return allRoleNames;
    }

    public Map<String, List<String>> doGetRoleListOfUsers(List<String> userNames, String domainName)
            throws UserStoreException {

        if (!isSecureCall.get()) {
            Class argTypes[] = new Class[] { List.class, String.class };
            Object object = callSecure("doGetRoleListOfUsers", new Object[] { userNames, domainName }, argTypes);
            return (Map<String, List<String>>) object;
        }

        Map<String, List<String>> internalRoles = doGetInternalRoleListOfUsers(userNames, domainName);

        Map<String, List<String>> externalRoles = new HashMap<>();
        if (readGroupsEnabled) {
            externalRoles = doGetExternalRoleListOfUsers(userNames);
        }

        Map<String, List<String>> combinedRoles = new HashMap<>();
        if (!internalRoles.isEmpty() && !externalRoles.isEmpty()) {
            for (String userName : userNames) {
                List<String> roles = new ArrayList<>();
                if (internalRoles.get(userName) != null) {
                    roles.addAll(internalRoles.get(userName));
                }
                if (externalRoles.get(userName) != null) {
                    roles.addAll(externalRoles.get(userName));
                }
                if (!roles.isEmpty()) {
                    combinedRoles.put(userName, roles);
                }
            }
        } else if (!internalRoles.isEmpty()) {
            combinedRoles = internalRoles;
        } else if (!externalRoles.isEmpty()) {
            combinedRoles = externalRoles;
        }

        return combinedRoles;
    }

    protected Map<String, List<String>> doGetExternalRoleListOfUsers(List<String> userNames) throws UserStoreException {

        Map<String, List<String>> externalRoleListOfUsers = new HashMap<>();
        for (String userName : userNames) {
            String[] externalRoles = doGetExternalRoleListOfUser(userName, null);
            if (!ArrayUtils.isEmpty(externalRoles)) {
                externalRoleListOfUsers.put(userName, Arrays.asList(externalRoles));
            }
        }
        return externalRoleListOfUsers;
    }

    @Override
    public UserClaimSearchEntry[] getUsersClaimValues(String[] userNames, String[] claims, String profileName) throws
            UserStoreException {

        if (!isSecureCall.get()) {
            Class argTypes[] = new Class[]{String[].class, String[].class, String.class};
            Object object = callSecure("getUsersClaimValues", new Object[]{userNames, claims, profileName},
                    argTypes);
            return (UserClaimSearchEntry[]) object;
        }

        if (StringUtils.isEmpty(profileName)) {
            profileName = UserCoreConstants.DEFAULT_PROFILE;
        }

        UserClaimSearchEntry[] allUsers = new UserClaimSearchEntry[0];
        Map<String, List<String>> domainFreeUsers = getDomainFreeUsers(userNames);

        for (Map.Entry<String, List<String>> entry : domainFreeUsers.entrySet()) {
            UserStoreManager secondaryUserStoreManager = getSecondaryUserStoreManager(entry.getKey());
            if (secondaryUserStoreManager instanceof AbstractUserStoreManager) {
                UserClaimSearchEntry[] users = ((AbstractUserStoreManager) secondaryUserStoreManager)
                        .doGetUsersClaimValues(entry.getValue(), claims, entry.getKey(), profileName);
                allUsers = (UserClaimSearchEntry[]) ArrayUtils.addAll(users, allUsers);
            }
        }

        for (UserOperationEventListener listener : UMListenerServiceComponent.getUserOperationEventListeners()) {
            if (listener instanceof AbstractUserOperationEventListener) {
                AbstractUserOperationEventListener newListener = (AbstractUserOperationEventListener) listener;
                if (!newListener.doPostGetUsersClaimValues(userNames, claims, profileName, allUsers)) {
                    break;
                }
            }
        }

        return allUsers;
    }

    public UserClaimSearchEntry[] doGetUsersClaimValues(List<String> users, String[] claims, String domainName,
            String profileName) throws UserStoreException {

        Set<String> propertySet = new HashSet<>();
        Map<String, String> claimToAttributeMap = new HashMap<>();
        List<UserClaimSearchEntry> userClaimSearchEntryList = new ArrayList<>();
        for (String claim : claims) {
            String property;
            try {
                property = getClaimAtrribute(claim, null, domainName);
            } catch (org.wso2.micro.integrator.security.user.api.UserStoreException e) {
                throw new UserStoreException(e);
            }
            propertySet.add(property);
            claimToAttributeMap.put(claim, property);
        }

        String[] properties = propertySet.toArray(new String[0]);
        Map<String, Map<String, String>> userProperties = this.getUsersPropertyValues(users, properties, profileName);

        for (Map.Entry<String, Map<String, String>> entry : userProperties.entrySet()) {
            UserClaimSearchEntry userClaimSearchEntry = new UserClaimSearchEntry();
            userClaimSearchEntry.setUserName(UserCoreUtil.addDomainToName(entry.getKey(), domainName));
            Map<String, String> userClaims = new HashMap<>();

            for (String claim : claims) {
                for (Map.Entry<String, String> userAttribute : entry.getValue().entrySet()) {
                    if (claimToAttributeMap.get(claim) != null && claimToAttributeMap.get(claim)
                            .equals(userAttribute.getKey())) {
                        userClaims.put(claim, userAttribute.getValue());
                    }
                }
            }
            userClaimSearchEntry.setClaims(userClaims);
            userClaimSearchEntryList.add(userClaimSearchEntry);

        }

        return userClaimSearchEntryList.toArray(new UserClaimSearchEntry[0]);
    }

    private Map<String, List<String>> getDomainFreeUsers(String[] userNames) {

        Map<String, List<String>> domainAwareUsers = new HashMap<>();
        if (ArrayUtils.isNotEmpty(userNames)) {
            for (String username : userNames) {
                String domainName = UserCoreUtil.extractDomainFromName(username);
                if (StringUtils.isEmpty(domainName)) {
                    domainName = UserCoreConstants.PRIMARY_DEFAULT_DOMAIN_NAME;
                }

                List<String> users = domainAwareUsers.get(domainName);
                if (users == null) {
                    users = new ArrayList<>();
                    domainAwareUsers.put(domainName.toUpperCase(), users);
                }
                users.add(UserCoreUtil.removeDomainFromName(username));
            }
        }

        return domainAwareUsers;
    }

    protected Map<String, Map<String, String>> getUsersPropertyValues(List<String> users, String[] propertyNames,
            String profileName) throws UserStoreException {

        Map<String, Map<String, String>> usersPropertyValuesMap = new HashMap<>();
        for (String userName : users) {
            Map<String, String> propertyValuesMap = getUserPropertyValues(userName, propertyNames, profileName);
            if (propertyValuesMap != null && !propertyValuesMap.isEmpty()) {
                usersPropertyValuesMap.put(userName, propertyValuesMap);
            }
        }
        return usersPropertyValuesMap;
    }

    /**
     * Remove given User Store Manager from USM chain
     *
     * @param userStoreDomainName
     * @throws UserStoreException
     */
    public void removeSecondaryUserStoreManager(String userStoreDomainName) throws UserStoreException {

        if (!isSecureCall.get()) {
            Class argTypes[] = new Class[]{String.class};
            callSecure("removeSecondaryUserStoreManager", new Object[]{userStoreDomainName}, argTypes);
            return;
        }

        if (userStoreDomainName == null) {
            throw new UserStoreException("Cannot remove user store. User store domain name is null");
        }
        if ("".equals(userStoreDomainName)) {
            throw new UserStoreException("Cannot remove user store. User store domain name is empty");
        }
//    	if(!this.userStoreManagerHolder.containsKey(userStoreDomainName.toUpperCase())) {
//    		throw new UserStoreException("Cannot remove user store. User store domain name does not exists");
//    	}

        userStoreDomainName = userStoreDomainName.toUpperCase();

        boolean isUSMContainsInMap = false;
        if (this.userStoreManagerHolder.containsKey(userStoreDomainName.toUpperCase())) {
            isUSMContainsInMap = true;
            this.userStoreManagerHolder.remove(userStoreDomainName.toUpperCase());
            if (log.isDebugEnabled()) {
                log.debug("UserStore: " + userStoreDomainName + " removed from map");
            }
        }

        boolean isUSMConatainsInChain = false;
        UserStoreManager prevUserStoreManager = this;
        while (prevUserStoreManager.getSecondaryUserStoreManager() != null) {
            UserStoreManager secondaryUSM = prevUserStoreManager.getSecondaryUserStoreManager();
            if (secondaryUSM.getRealmConfiguration().getUserStoreProperty(UserStoreConfigConstants.DOMAIN_NAME).equalsIgnoreCase(userStoreDomainName)) {
                isUSMConatainsInChain = true;
                // Omit deleting user store manager from the chain
                prevUserStoreManager.setSecondaryUserStoreManager(secondaryUSM.getSecondaryUserStoreManager());
                log.info("User store: " + userStoreDomainName + " of tenant:" + tenantId + " is removed from user store chain.");
                return;
            }
            prevUserStoreManager = secondaryUSM;
        }

        if (!isUSMContainsInMap && isUSMConatainsInChain) {
            throw new UserStoreException("Removed user store manager : " + userStoreDomainName + " didnt exists in userStoreManagerHolder map");
        } else if (isUSMContainsInMap && !isUSMConatainsInChain) {
            throw new UserStoreException("Removed user store manager : " + userStoreDomainName + " didnt exists in user store manager chain");
        }
    }

    public HybridRoleManager getInternalRoleManager() {
        return hybridRoleManager;
    }

    @Override
    public String[] getUserList(String claim, String claimValue, String profileName, int limit, int offset) throws
            UserStoreException {

        if (!isSecureCall.get()) {
            Class argTypes[] = new Class[]{String.class, String.class, String.class, int.class, int.class};
            Object object = callSecure("getUserList", new Object[]{claim, claimValue, profileName, limit, offset},
                    argTypes);
            return (String[]) object;
        }

        if (claim == null) {
            String errorCode = ErrorMessages.ERROR_CODE_INVALID_CLAIM_URI.getCode();
            String errorMessage = String.format(ErrorMessages.ERROR_CODE_INVALID_CLAIM_URI.getMessage(), "");
            handleGetUserListFailure(errorCode, errorMessage, null, claimValue, limit, offset, profileName);
            throw new IllegalArgumentException(ErrorMessages.ERROR_CODE_INVALID_CLAIM_URI.toString());
        }

        if (claimValue == null) {
            handleGetUserListFailure(ErrorMessages.ERROR_CODE_INVALID_CLAIM_VALUE.getCode(), ErrorMessages.
                    ERROR_CODE_INVALID_CLAIM_VALUE.getMessage(), claim, null, limit, offset, profileName);
            throw new IllegalArgumentException(ErrorMessages.ERROR_CODE_INVALID_CLAIM_VALUE.toString());
        }

        if (log.isDebugEnabled()) {
            log.debug("Listing and paginate users who having value as " + claimValue + " for the claim " + claim);
        }

        if (USERNAME_CLAIM_URI.equalsIgnoreCase(claim) || SCIM_USERNAME_CLAIM_URI.equalsIgnoreCase(claim) ||
                SCIM2_USERNAME_CLAIM_URI.equalsIgnoreCase(claim)) {

            if (log.isDebugEnabled()) {
                log.debug("Switching to paginate users using username");
            }

            String[] filteredUsers = listUsers(claimValue, limit, offset);

            if (log.isDebugEnabled()) {
                log.debug("Filtered users: " + Arrays.toString(filteredUsers));
            }

            return filteredUsers;
        }

        // Extracting the domain from claimValue.
        String extractedDomain = null;
        int index;
        index = claimValue.indexOf(UserCoreConstants.DOMAIN_SEPARATOR);
        if (index > 0) {
            String names[] = claimValue.split(UserCoreConstants.DOMAIN_SEPARATOR);
            extractedDomain = names[0].trim();
        }

        UserStoreManager userManager = null;
        if (StringUtils.isNotEmpty(extractedDomain)) {
            userManager = getSecondaryUserStoreManager(extractedDomain);
            if (log.isDebugEnabled()) {
                log.debug("Domain: " + extractedDomain + " is passed with the claim and user store manager is loaded" +
                        " for the given domain name.");
            }
        }

        if (userManager instanceof JDBCUserStoreManager && (SCIM_USERNAME_CLAIM_URI.equalsIgnoreCase(claim) ||
                SCIM2_USERNAME_CLAIM_URI.equalsIgnoreCase(claim))) {
            if (userManager.isExistingUser(claimValue)) {
                return new String[]{claimValue};
            } else {
                return new String[0];
            }
        }

        claimValue = UserCoreUtil.removeDomainFromName(claimValue);

        final List<String> filteredUserList = new ArrayList<>();

        if (StringUtils.isNotEmpty(extractedDomain)) {
            try {
                for (UserOperationEventListener listener : UMListenerServiceComponent
                        .getUserOperationEventListeners()) {
                    if (listener instanceof AbstractUserOperationEventListener) {
                        AbstractUserOperationEventListener newListener = (AbstractUserOperationEventListener) listener;
                        if (!newListener.doPreGetUserList(claim, claimValue, limit, offset, filteredUserList, userManager)) {
                            handleGetUserListFailure(ErrorMessages.ERROR_CODE_ERROR_DURING_PRE_GET_USER_LIST.getCode(),
                                    String.format(ErrorMessages.ERROR_CODE_ERROR_DURING_PRE_GET_USER_LIST.getMessage(),
                                            UserCoreErrorConstants.PRE_LISTENER_TASKS_FAILED_MESSAGE), claim,
                                    claimValue, limit, offset, profileName);
                            break;
                        }
                    }
                }
            } catch (UserStoreException ex) {
                handleGetUserListFailure(ErrorMessages.ERROR_CODE_ERROR_DURING_PRE_GET_USER_LIST.getCode(),
                        String.format(ErrorMessages.ERROR_CODE_ERROR_DURING_PRE_GET_USER_LIST.getMessage(),
                                ex.getMessage()), claim, claimValue, limit, offset, profileName);
                throw ex;
            }
        }

        if (log.isDebugEnabled()) {
            log.debug("Pre listener user list: " + filteredUserList + " for domain: " + extractedDomain);
        }

        // Iterate through user stores and check for users for this claim.
        List<String> usersFromUserStore = doGetUserList(claim, claimValue, profileName, limit,
                offset, extractedDomain, userManager);
        if (log.isDebugEnabled()) {
            log.debug("Users from user store: " + extractedDomain + " : " + usersFromUserStore);
        }
        filteredUserList.addAll(usersFromUserStore);

        if (StringUtils.isNotEmpty(extractedDomain)) {
            handlePostGetUserList(claim, claimValue, filteredUserList, limit, offset, false);
        }

        if (log.isDebugEnabled()) {
            log.debug("Post listener user list pagination: " + filteredUserList + " for domain: " + extractedDomain);
        }

        return filteredUserList.toArray(new String[0]);
    }

    @Override
    public String[] getUserList(Condition condition, String domain, String profileName, int limit, int offset, String sortBy, String
            sortOrder) throws UserStoreException {

        validateCondition(condition);
        if (StringUtils.isNotEmpty(sortBy) && StringUtils.isNotEmpty(sortOrder)) {
            throw new UserStoreException("Sorting is not supported.");
        }

        if (StringUtils.isEmpty(domain)) {
            domain = UserCoreConstants.PRIMARY_DEFAULT_DOMAIN_NAME;
        }

        if (StringUtils.isEmpty(profileName)) {
            profileName = UserCoreConstants.DEFAULT_PROFILE;
        }

        handlePreGetUserList(condition, domain, profileName, limit, offset, sortBy, sortOrder);

        if (log.isDebugEnabled()) {
            log.debug("Pre listener get conditional  user list for domain: " + domain);
        }

        String[] filteredUsers = new String[0];
        UserStoreManager secManager = getSecondaryUserStoreManager(domain);
        if (secManager != null) {
            if (secManager instanceof AbstractUserStoreManager) {
                PaginatedSearchResult users = ((AbstractUserStoreManager) secManager).doGetUserList(condition,
                        profileName, limit, offset, sortBy, sortOrder);
                filteredUsers = users.getUsers();
            }
        }

        handlePostGetUserList(condition, domain, profileName, limit, offset, sortBy, sortOrder, filteredUsers, false);

        if (log.isDebugEnabled()) {
            log.debug("post listener get conditional  user list for domain: " + domain);
        }
        return filteredUsers;
    }

    protected PaginatedSearchResult doGetUserList(Condition condition, String profileName, int limit, int offset,
                                                  String sortBy, String sortOrder) throws UserStoreException {

        return new PaginatedSearchResult();
    }


    @Override
    public String[] listUsers(String filter, int limit, int offset) throws UserStoreException {

        if (!isSecureCall.get()) {
            Class argTypes[] = new Class[]{String.class, int.class, int.class};
            Object object = callSecure("listUsers", new Object[]{filter, limit, offset}, argTypes);
            return (String[]) object;
        }

        int index = filter.indexOf(UserCoreConstants.DOMAIN_SEPARATOR);
        PaginatedSearchResult userList;
        String[] users = new String[0];

        if (offset <= 0) {
            offset = 1;
        }

        if (index > 0) {
            String domain = filter.substring(0, index);

            UserStoreManager secManager = getSecondaryUserStoreManager(domain);
            if (secManager != null) {
                // Secondary UserStoreManager registered for this domain.
                filter = filter.substring(index + 1);
                if (secManager instanceof AbstractUserStoreManager) {
                     userList = ((AbstractUserStoreManager) secManager).doListUsers(filter, limit, offset);
                    handlePostListPaginatedUsers(filter, limit, offset, new ArrayList<>(Arrays.asList(userList.getUsers())),
                            true);
                    return userList.getUsers();
                }
            }
        } else if (index == 0) {
            userList = doListUsers(filter.substring(1), limit, offset);
            handlePostListPaginatedUsers(filter, limit, offset, new ArrayList<>(Arrays.asList(userList.getUsers())), true);
            return userList.getUsers();
        }

        try {
            userList = doListUsers(filter, limit, offset);
            users = UserCoreUtil.combineArrays(users, userList.getUsers());
            limit = limit - users.length;
        } catch (UserStoreException ex) {
            handleGetPaginatedUserListFailure(ErrorMessages.ERROR_CODE_ERROR_WHILE_LISTING_PAGINATED_USERS.getCode(),
                    String.format(ErrorMessages.ERROR_CODE_ERROR_WHILE_LISTING_PAGINATED_USERS.getMessage(), ex.getMessage()),
                    null, null, null);
            throw ex;
        }

        String primaryDomain = realmConfig.getUserStoreProperty(UserCoreConstants.RealmConfig.PROPERTY_DOMAIN_NAME);

        int nonPaginatedUserCount = userList.getSkippedUserCount();
        if (this.getSecondaryUserStoreManager() != null) {
            for (Map.Entry<String, UserStoreManager> entry : userStoreManagerHolder.entrySet()) {
                if (limit <= 0) {
                    return users;
                }
                if (entry.getKey().equalsIgnoreCase(primaryDomain)) {
                    continue;
                }
                UserStoreManager storeManager = entry.getValue();
                if (storeManager instanceof AbstractUserStoreManager) {
                    try {
                        if (userList.getUsers().length > 0) {
                            offset = 1;
                        } else {
                            offset = offset - nonPaginatedUserCount;
                        }

                        PaginatedSearchResult secondUserList = ((AbstractUserStoreManager) storeManager)
                                .doListUsers(filter, limit, offset);
                        nonPaginatedUserCount = secondUserList.getSkippedUserCount();
                        users = UserCoreUtil.combineArrays(users, secondUserList.getUsers());
                        limit = limit - users.length;
                    } catch (UserStoreException ex) {
                        handleGetPaginatedUserListFailure(ErrorMessages.ERROR_CODE_ERROR_WHILE_LISTING_PAGINATED_USERS.getCode(),
                                String.format(ErrorMessages.ERROR_CODE_ERROR_WHILE_LISTING_PAGINATED_USERS.getMessage(),
                                        ex.getMessage()), null, null, null);

                        // We can ignore and proceed. Ignore the results from this user store.
                        log.error(ex);
                    }
                }
            }
        }

        handlePostListPaginatedUsers(filter, limit, offset, new ArrayList<>(Arrays.asList(users)), true);
        return users;
    }

    protected PaginatedSearchResult doListUsers(String filter, int limit, int offset)
            throws UserStoreException{

        if (log.isDebugEnabled()) {
            log.debug("Operation is not supported in: " + this.getClass());
        }
        return new PaginatedSearchResult();
    }

    protected PaginatedSearchResult getUserListFromProperties(String property, String value, String profileName, int
            limit, int offset) throws UserStoreException {

        return new PaginatedSearchResult();
    }

    private void validateCondition(Condition condition) throws UserStoreException {

        if (condition instanceof ExpressionCondition) {
            if (isNotSupportedExpressionOperation(condition)) {
                throw new UserStoreException("Unsupported expression operation: " + condition.getOperation());
            }
        } else if (condition instanceof OperationalCondition) {
            Condition leftCondition = ((OperationalCondition) condition).getLeftCondition();
            validateCondition(leftCondition);
            Condition rightCondition = ((OperationalCondition) condition).getRightCondition();
            String operation = condition.getOperation();
            if (!OperationalOperation.AND.toString().equals(operation)) {
                throw new UserStoreException("Unsupported Conditional operation: " + condition.getOperation());
            }
            validateCondition(rightCondition);
        }
    }

    private boolean isNotSupportedExpressionOperation(Condition condition) {

        return !(ExpressionOperation.EQ.toString().equals(condition.getOperation()) ||
                ExpressionOperation.CO.toString().equals(condition.getOperation()) ||
                ExpressionOperation.SW.toString().equals(condition.getOperation()) ||
                ExpressionOperation.EW.toString().equals(condition.getOperation()));
    }

    private boolean isAnInternalRole(String roleName) {

        return roleName.toLowerCase().startsWith(APPLICATION_DOMAIN.toLowerCase()) || roleName.toLowerCase()
                .startsWith(UserCoreConstants.INTERNAL_DOMAIN.toLowerCase()) || roleName.toLowerCase()
                .startsWith(WORKFLOW_DOMAIN.toLowerCase());
    }

    private List<String> getUserStorePreferenceOrder() throws UserStoreException {

        UserMgtContext userMgtContext = UserCoreUtil.getUserMgtContextFromThreadLocal();
        if (userMgtContext != null) {
            // Retrieve the relevant supplier to generate the user store preference order.
            UserStorePreferenceOrderSupplier<List<String>> userStorePreferenceSupplier = userMgtContext.
                    getUserStorePreferenceOrderSupplier();
            if (userStorePreferenceSupplier != null) {
                // Generate the user store preference order.
                List<String> userStorePreferenceOrder = userStorePreferenceSupplier.get();
                if (userStorePreferenceOrder != null) {
                    return userStorePreferenceOrder;
                }
            }
        }
        return Collections.emptyList();
    }

    private boolean hasUserStorePreferenceChainGenerated() throws UserStoreException {

        return this instanceof IterativeUserStoreManager;
    }

    private boolean isUserStoreChainNeeded(List<String> userStorePreferenceOrder) throws UserStoreException {

        if (this instanceof IterativeUserStoreManager) {
            return false;
        }
        userStorePreferenceOrder.addAll(getUserStorePreferenceOrder());
        return CollectionUtils.isNotEmpty(userStorePreferenceOrder) && !hasUserStorePreferenceChainGenerated();
    }

    private boolean generateUserStoreChain(String userName, Object credential, boolean domainProvided,
                                           List<String> userStorePreferenceOrder) throws UserStoreException {

        IterativeUserStoreManager initialUserStoreManager = null;
        IterativeUserStoreManager prevUserStoreManager = null;
        for (String domainName : userStorePreferenceOrder) {
            UserStoreManager userStoreManager = this.getSecondaryUserStoreManager(domainName);
            // If the user store manager is instance of AbstractUserStoreManager then generate a user store chain using
            // IterativeUserStoreManager.
            if (userStoreManager instanceof AbstractUserStoreManager) {
                if (initialUserStoreManager == null) {
                    prevUserStoreManager = new IterativeUserStoreManager((AbstractUserStoreManager) userStoreManager);
                    initialUserStoreManager = prevUserStoreManager;
                } else {
                    IterativeUserStoreManager currentUserStoreManager = new IterativeUserStoreManager(
                            (AbstractUserStoreManager) userStoreManager);
                    prevUserStoreManager.setNextUserStoreManager(currentUserStoreManager);
                    prevUserStoreManager = currentUserStoreManager;
                }
            } else {
                if (log.isDebugEnabled()) {
                    log.debug("UserStoreManager is not an instance of AbstractUserStoreManager hence authenticate the" +
                            " user through all the available user store list.");
                }
                return authenticateInternal(userName, credential, domainProvided);
            }
        }
        // Authenticate using the initial user store from the user store preference list.
        return initialUserStoreManager.authenticate(userName, credential);
    }
}
