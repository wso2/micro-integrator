/*
 * Copyright (c) 2023, WSO2 LLC. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
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

package org.wso2.micro.integrator.security.user.core.file;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.wso2.micro.core.util.CarbonException;
import org.wso2.micro.integrator.core.util.MicroIntegratorBaseUtils;
import org.wso2.micro.integrator.security.UnsupportedSecretTypeException;
import org.wso2.micro.integrator.security.user.api.Properties;
import org.wso2.micro.integrator.security.user.api.RealmConfiguration;
import org.wso2.micro.integrator.security.user.api.Tenant;
import org.wso2.micro.integrator.security.user.api.UserStoreException;
import org.wso2.micro.integrator.security.user.core.UserCoreConstants;
import org.wso2.micro.integrator.security.user.core.UserRealm;
import org.wso2.micro.integrator.security.user.core.claim.ClaimManager;
import org.wso2.micro.integrator.security.user.core.common.AbstractUserStoreManager;
import org.wso2.micro.integrator.security.user.core.common.RoleContext;
import org.wso2.micro.integrator.security.user.core.dto.UserInfoDTO;
import org.wso2.micro.integrator.security.user.core.profile.ProfileConfigurationManager;
import org.wso2.micro.integrator.security.user.core.util.JDBCRealmUtil;
import org.wso2.micro.integrator.security.util.Secret;
import org.wso2.securevault.SecretResolver;
import org.wso2.securevault.SecretResolverFactory;
import org.wso2.securevault.commons.MiscellaneousUtil;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;

/**
 * This class is used to authenticate, authorize users against the File based user store defined in internal-apis.xml
 */
public class FileBasedUserStoreManager extends AbstractUserStoreManager {

    private static final Log log = LogFactory.getLog(FileBasedUserStoreManager.class);
    private static final FileBasedUserStoreManager userStoreManager = new FileBasedUserStoreManager();
    private static Map<String, UserInfoDTO> usersList;
    private static boolean isInitialized = false;
    private static final String USER_STORE = "userStore";
    private static final String USERS = "users";
    private static final String USER = "user";
    private static final String USERNAME = "username";
    private static final String PASSWORD = "password";
    private static final String IS_ADMIN = "isAdmin";
    private static SecretResolver secretResolver;

    private FileBasedUserStoreManager() {
        initializeUserStore();
    }

    /**
     * Method to retrieve FileBasedUserStoreManager
     *
     * @return FileBasedUserStoreManager
     */
    public static FileBasedUserStoreManager getUserStoreManager() {
        return userStoreManager;
    }

    /**
     * Authenticate the user against the file based user store.
     *
     * @param username the user to be authenticated
     * @param password the password used for authentication
     * @return true if authenticated
     */
    public boolean authenticate(String username, String password) {
        if (usersList.containsKey(username)) {
            String passwordFromStore = String.valueOf(usersList.get(username).getPassword());
            return StringUtils.isNotBlank(passwordFromStore) && passwordFromStore.equals(password);
        }
        return false;
    }

    /**
     * Method to assert if a user is an admin
     *
     * @param username the user to be validated as an admin
     * @return true if the admin role is assigned to the user
     */
    public boolean isAdmin(String username) {
        if (usersList.containsKey(username)) {
            UserInfoDTO userInfo = usersList.get(username);
            return userInfo.isAdmin();
        }
        return false;
    }

    /**
     * Method to check whether a user exists in the FileBasedUserStoreManager.
     *
     * @param username the user to be checked.
     * @return true if the user exists.
     */
    public boolean isUserExists(String username) {
        return usersList.containsKey(username);
    }

    /**
     * Method to check whether FileBasedUserStoreManager is initialized
     *
     * @return true if successfully initialized
     */
    public boolean isInitialized() {
        return isInitialized;
    }

    /**
     * Method to initialize FileBasedUserStoreManager using the internal-apis.xml
     */
    private static void initializeUserStore() {
        OMElement documentElement = null;
        // Fetch users from internal-apis.xml
        File mgtApiUserConfig = new File(MicroIntegratorBaseUtils.getCarbonConfigDirPath(), "internal-apis.xml");
        try (InputStream fileInputStream = Files.newInputStream(mgtApiUserConfig.toPath())) {
            InputStream inputStream = MicroIntegratorBaseUtils.replaceSystemVariablesInXml(fileInputStream);
            StAXOMBuilder builder = new StAXOMBuilder(inputStream);
            documentElement = builder.getDocumentElement();
        } catch (IOException | CarbonException | XMLStreamException e) {
            log.error("Error parsing the file based user store. Error reading internal-apis.xml", e);
        }
        if (documentElement == null) {
            log.error("Error parsing the file based user store. Error reading internal-apis.xml");
            return;
        }
        secretResolver = SecretResolverFactory.create(documentElement, true);
        OMElement userStoreOM = (OMElement) documentElement.getChildrenWithName(new QName(USER_STORE)).next();
        if (Objects.nonNull(userStoreOM)) {
            usersList = populateUsers(userStoreOM.getFirstChildWithName(new QName(USERS)));
            isInitialized = true;
        } else {
            log.error("Error parsing the file based user store. User store element not found in internal-apis.xml");
        }
    }

    @Override
    public Map<String, String> getProperties(Tenant tenant) throws UserStoreException {
        return null;
    }

    @Override
    public boolean isMultipleProfilesAllowed() {
        return false;
    }

    @Override
    public void addRememberMe(String userName, String token) throws UserStoreException {

    }

    @Override
    public boolean isValidRememberMeToken(String userName, String token) throws UserStoreException {
        return false;
    }

    @Override
    public Properties getDefaultUserStoreProperties() {
        return null;
    }

    @Override
    public String[] getProfileNames(String userName) {
        return new String[0];
    }

    @Override
    public String[] getAllProfileNames() {
        return new String[0];
    }

    @Override
    public boolean isReadOnly() throws org.wso2.micro.integrator.security.user.core.UserStoreException {
        return false;
    }

    @Override
    public int getUserId(String username) {
        return 0;
    }

    @Override
    public int getTenantId(String username) throws org.wso2.micro.integrator.security.user.core.UserStoreException {
        return 0;
    }

    @Override
    public int getTenantId() throws org.wso2.micro.integrator.security.user.core.UserStoreException {
        return 0;
    }

    @Override
    public Map<String, String> getProperties(org.wso2.micro.integrator.security.user.core.tenant.Tenant tenant)
            throws org.wso2.micro.integrator.security.user.core.UserStoreException {
        return null;
    }

    @Override
    public boolean isBulkImportSupported() {
        return false;
    }

    @Override
    public RealmConfiguration getRealmConfiguration() {
        return this.realmConfig;
    }

    @Override
    protected Map<String, String> getUserPropertyValues(String userName, String[] propertyNames, String profileName) throws org.wso2.micro.integrator.security.user.core.UserStoreException {
        return null;
    }

    @Override
    protected boolean doCheckExistingRole(String roleName) throws org.wso2.micro.integrator.security.user.core.UserStoreException {
        return false;
    }

    @Override
    protected RoleContext createRoleContext(String roleName) throws org.wso2.micro.integrator.security.user.core.UserStoreException {
        return null;
    }

    @Override
    protected boolean doCheckExistingUser(String userName) throws org.wso2.micro.integrator.security.user.core.UserStoreException {
        return false;
    }

    @Override
    protected String[] getUserListFromProperties(String property, String value, String profileName) throws org.wso2.micro.integrator.security.user.core.UserStoreException {
        return new String[0];
    }

    @Override
    protected boolean doAuthenticate(String userName, Object credential) throws org.wso2.micro.integrator.security.user.core.UserStoreException {
        if (credential == null || ((Secret) credential).getChars().length == 0) {
            return false;
        } else {
            try {
                Secret password = Secret.getSecret(credential);
                return authenticate(userName, new String(password.getChars()));
            } catch (UnsupportedSecretTypeException e) {
                return false;
            }
        }
    }

    @Override
    protected void doAddUser(String userName, Object credential, String[] roleList, Map<String, String> claims, String profileName, boolean requirePasswordChange) throws org.wso2.micro.integrator.security.user.core.UserStoreException {

    }

    @Override
    protected void doUpdateCredential(String userName, Object newCredential, Object oldCredential)
            throws org.wso2.micro.integrator.security.user.core.UserStoreException {
    }

    @Override
    protected void doUpdateCredentialByAdmin(String userName, Object newCredential)
            throws org.wso2.micro.integrator.security.user.core.UserStoreException {
    }

    @Override
    protected void doDeleteUser(String userName)
            throws org.wso2.micro.integrator.security.user.core.UserStoreException {
    }

    @Override
    protected void doSetUserClaimValue(String userName, String claimURI, String claimValue, String profileName)
            throws org.wso2.micro.integrator.security.user.core.UserStoreException {

    }

    @Override
    protected void doSetUserClaimValues(String userName, Map<String, String> claims, String profileName)
            throws org.wso2.micro.integrator.security.user.core.UserStoreException {
    }

    @Override
    protected void doDeleteUserClaimValue(String userName, String claimURI, String profileName)
            throws org.wso2.micro.integrator.security.user.core.UserStoreException {
    }

    @Override
    protected void doDeleteUserClaimValues(String userName, String[] claims, String profileName)
            throws org.wso2.micro.integrator.security.user.core.UserStoreException {
    }

    @Override
    protected void doUpdateUserListOfRole(String roleName, String[] deletedUsers, String[] newUsers)
            throws org.wso2.micro.integrator.security.user.core.UserStoreException {

    }

    @Override
    protected void doUpdateRoleListOfUser(String userName, String[] deletedRoles, String[] newRoles)
            throws org.wso2.micro.integrator.security.user.core.UserStoreException {

    }

    @Override
    protected String[] doGetExternalRoleListOfUser(String userName, String filter)
            throws org.wso2.micro.integrator.security.user.core.UserStoreException {
        return new String[0];
    }

    @Override
    protected String[] doGetSharedRoleListOfUser(String userName, String tenantDomain, String filter)
            throws org.wso2.micro.integrator.security.user.core.UserStoreException {
        return new String[0];
    }

    @Override
    protected void doAddRole(String roleName, String[] userList, boolean shared)
            throws org.wso2.micro.integrator.security.user.core.UserStoreException {

    }

    @Override
    protected void doDeleteRole(String roleName)
            throws org.wso2.micro.integrator.security.user.core.UserStoreException {

    }

    @Override
    protected void doUpdateRoleName(String roleName, String newRoleName)
            throws org.wso2.micro.integrator.security.user.core.UserStoreException {

    }

    @Override
    protected String[] doGetRoleNames(String filter, int maxItemLimit)
            throws org.wso2.micro.integrator.security.user.core.UserStoreException {
        // admin is the only role supported in file based user store
        return new String[]{"admin"};
    }

    @Override
    protected String[] doListUsers(String filter, int maxItemLimit)
            throws org.wso2.micro.integrator.security.user.core.UserStoreException {
        return usersList.keySet().toArray(new String[0]);
    }

    @Override
    protected String[] doGetDisplayNamesForInternalRole(String[] userNames)
            throws org.wso2.micro.integrator.security.user.core.UserStoreException {
        return new String[0];
    }

    @Override
    public boolean doCheckIsUserInRole(String userName, String roleName)
            throws org.wso2.micro.integrator.security.user.core.UserStoreException {
        return false;
    }

    @Override
    protected String[] doGetSharedRoleNames(String tenantDomain, String filter, int maxItemLimit)
            throws org.wso2.micro.integrator.security.user.core.UserStoreException {
        return new String[0];
    }

    @Override
    protected String[] doGetUserListOfRole(String roleName, String filter)
            throws org.wso2.micro.integrator.security.user.core.UserStoreException {
        return new String[0];
    }


    public FileBasedUserStoreManager(RealmConfiguration realmConfig, Map<String, Object> properties,
                                     ClaimManager claimManager, ProfileConfigurationManager profileManager,
                                     UserRealm realm, Integer tenantId)
            throws org.wso2.micro.integrator.security.user.core.UserStoreException {
        this(realmConfig, tenantId);
        this.claimManager = claimManager;
        this.userRealm = realm;
    }

    public FileBasedUserStoreManager(RealmConfiguration realmConfig, int tenantId)
            throws org.wso2.micro.integrator.security.user.core.UserStoreException {
        this.realmConfig = realmConfig;
        this.tenantId = tenantId;
        realmConfig.setUserStoreProperties(JDBCRealmUtil.getSQL(realmConfig
                .getUserStoreProperties()));

        // new properties after carbon core 4.0.7 release.
        if (realmConfig.getUserStoreProperty(UserCoreConstants.RealmConfig.READ_GROUPS_ENABLED) != null) {
            readGroupsEnabled = Boolean.parseBoolean(realmConfig
                    .getUserStoreProperty(UserCoreConstants.RealmConfig.READ_GROUPS_ENABLED));
        }

        if (realmConfig.getUserStoreProperty(UserCoreConstants.RealmConfig.WRITE_GROUPS_ENABLED) != null) {
            writeGroupsEnabled = Boolean.parseBoolean(realmConfig
                    .getUserStoreProperty(UserCoreConstants.RealmConfig.WRITE_GROUPS_ENABLED));
        } else {
            if (!isReadOnly()) {
                writeGroupsEnabled = true;
            }
        }

        // This property is now deprecated
        if (realmConfig
                .getUserStoreProperty(UserCoreConstants.RealmConfig.PROPERTY_INTERNAL_ROLES_ONLY) != null) {
            boolean internalRolesOnly = Boolean
                    .parseBoolean(realmConfig
                            .getUserStoreProperty(UserCoreConstants.RealmConfig.PROPERTY_INTERNAL_ROLES_ONLY));
            if (internalRolesOnly) {
                readGroupsEnabled = false;
                writeGroupsEnabled = false;
            } else {
                readGroupsEnabled = true;
                writeGroupsEnabled = true;
            }
        }

        if (writeGroupsEnabled) {
            readGroupsEnabled = true;
        }
        doInitialSetup(true);
    }

    /**
     * Populates individual users.
     *
     * @param users the parent element of users
     * @return map of users against UserInfo config
     */
    private static Map<String, UserInfoDTO> populateUsers(OMElement users) {
        HashMap<String, UserInfoDTO> userMap = new HashMap<>();
        if (users != null) {
            @SuppressWarnings("unchecked")
            Iterator<OMElement> usersIterator = users.getChildrenWithName(new QName(USER));
            if (usersIterator != null) {
                while (usersIterator.hasNext()) {
                    OMElement userElement = usersIterator.next();
                    OMElement userNameElement = userElement.getFirstChildWithName(new QName(USERNAME));
                    OMElement passwordElement = userElement.getFirstChildWithName(new QName(PASSWORD));
                    OMElement isAdminElement = userElement.getFirstChildWithName(new QName(IS_ADMIN));
                    if (userNameElement != null && passwordElement != null) {
                        String userName = userNameElement.getText();
                        if (userMap.containsKey(userName)) {
                            System.out.println("Error parsing the file based user store. User: " + userName + " defined "
                                    + "more than once.");
                        }
                        boolean isAdmin = false;
                        if (isAdminElement != null) {
                            isAdmin = Boolean.parseBoolean(isAdminElement.getText().trim());
                        }
                        userMap.put(userName, new UserInfoDTO(userName,
                                resolveSecret(passwordElement.getText()).toCharArray(), isAdmin));
                    }
                }
            }
        }
        return userMap;
    }

    /**
     * Checks if the text is protected and returns decrypted text if protected, else returns the plain text
     *
     * @param text text to be resolved
     * @return Decrypted text if protected else plain text
     */
    private static String resolveSecret(String text) {
        String alias = MiscellaneousUtil.getProtectedToken(text);
        if (!StringUtils.isEmpty(alias)) {
            if (secretResolver.isInitialized()) {
                return MiscellaneousUtil.resolve(alias, secretResolver);
            }
        }
        return text;
    }
}
