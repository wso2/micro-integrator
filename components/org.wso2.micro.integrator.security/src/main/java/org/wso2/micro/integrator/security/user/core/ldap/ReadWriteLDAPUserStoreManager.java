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
package org.wso2.micro.integrator.security.user.core.ldap;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.micro.core.Constants;
import org.wso2.micro.integrator.security.user.core.UserCoreConstants;
import org.wso2.micro.integrator.security.user.api.Properties;
import org.wso2.micro.integrator.security.user.api.Property;
import org.wso2.micro.integrator.security.user.api.RealmConfiguration;
import org.wso2.micro.integrator.security.user.core.UserRealm;
import org.wso2.micro.integrator.security.user.core.UserStoreConfigConstants;
import org.wso2.micro.integrator.security.user.core.UserStoreException;
import org.wso2.micro.integrator.security.user.core.claim.ClaimManager;
import org.wso2.micro.integrator.security.user.core.common.RoleContext;
import org.wso2.micro.integrator.security.user.core.hybrid.HybridRoleManager;
import org.wso2.micro.integrator.security.user.core.hybrid.JdbcHybridRoleManager;
import org.wso2.micro.integrator.security.user.core.profile.ProfileConfigurationManager;
import org.wso2.micro.integrator.security.user.core.tenant.Tenant;
import org.wso2.micro.integrator.security.user.core.util.DatabaseUtil;
import org.wso2.micro.integrator.security.user.core.util.JNDIUtil;
import org.wso2.micro.integrator.security.user.core.util.UserCoreUtil;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.StringTokenizer;
import java.util.regex.Pattern;
import javax.naming.Name;
import javax.naming.NameParser;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttribute;
import javax.naming.directory.BasicAttributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.InvalidAttributeIdentifierException;
import javax.naming.directory.InvalidAttributeValueException;
import javax.naming.directory.NoSuchAttributeException;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.sql.DataSource;

/**
 * This class is capable of get connected to an external or internal LDAP based user store in
 * read/write mode. Create, Update, Delete users and groups are supported.
 */
@SuppressWarnings({})
public class ReadWriteLDAPUserStoreManager extends ReadOnlyLDAPUserStoreManager {

    public static final String PASSWORD_HASH_METHOD = UserStoreConfigConstants.passwordHashMethod;
    public static final String PASSWORD_HASH_METHOD_SHA = "SHA";
    public static final String PASSWORD_HASH_METHOD_MD5 = "MD5";
    public static final String ATTR_NAME_CN = "cn";
    public static final String ATTR_NAME_SN = "sn";
    protected static final String KRB5_PRINCIPAL_NAME_ATTRIBUTE = "krb5PrincipalName";
    protected static final String KRB5_KEY_VERSION_NUMBER_ATTRIBUTE = "krb5KeyVersionNumber";
    protected static final String EMPTY_ATTRIBUTE_STRING = "";
    private static final String MULTI_ATTRIBUTE_SEPARATOR_DESCRIPTION = "This is the separator for multiple claim values";

    private static final String MULTI_ATTRIBUTE_SEPARATOR = "MultiAttributeSeparator";
    private static final ArrayList<Property> RW_LDAP_UM_ADVANCED_PROPERTIES = new ArrayList<Property>();
    private static final String LDAPConnectionTimeout = "LDAPConnectionTimeout";
    private static final String LDAPConnectionTimeoutDescription = "LDAP Connection Timeout";
    private static final String readTimeout = "ReadTimeout";
    private static final String readTimeoutDescription = "Configure this to define the read timeout for LDAP operations";
    private static final String RETRY_ATTEMPTS = "RetryAttempts";
    private static final String LDAPBinaryAttributesDescription = "Configure this to define the LDAP binary attributes " +
            "seperated by a space. Ex:mpegVideo mySpecialKey";

    /* To track whether this is the first time startup of the server. */
    protected static boolean isFirstStartup = true;
    private static Log logger = LogFactory.getLog(ReadWriteLDAPUserStoreManager.class);
    private static Log log = LogFactory.getLog(ReadWriteLDAPUserStoreManager.class);
    private static final String BULK_IMPORT_SUPPORT = "BulkImportSupported";

    protected Random random = new Random();

    protected boolean kdcEnabled = false;

    static {
        setAdvancedProperties();
    }

    public ReadWriteLDAPUserStoreManager() {

    }

    public ReadWriteLDAPUserStoreManager(RealmConfiguration realmConfig,
                                         Map<String, Object> properties, ClaimManager claimManager,
                                         ProfileConfigurationManager profileManager, UserRealm realm, Integer tenantId)
            throws UserStoreException {

        super(realmConfig, properties, claimManager, profileManager, realm, tenantId, true);

        if (log.isDebugEnabled()) {
            log.debug("Read-Write UserStoreManager initialization started "
                    + System.currentTimeMillis());
        }

        this.realmConfig = realmConfig;
        this.claimManager = claimManager;
        this.userRealm = realm;
        this.tenantId = tenantId;
        this.kdcEnabled = UserCoreUtil.isKdcEnabled(realmConfig);

        checkRequiredUserStoreConfigurations();

        dataSource = (DataSource) properties.get(UserCoreConstants.DATA_SOURCE);
        if (dataSource == null) {
            // avoid returning null
            dataSource = DatabaseUtil.getRealmDataSource(realmConfig);
        }
        if (dataSource == null) {
            throw new UserStoreException("Data Source is null");
        }
        properties.put(UserCoreConstants.DATA_SOURCE, dataSource);

        ReadWriteLDAPUserStoreManager.isFirstStartup = (Boolean) properties
                .get(UserCoreConstants.FIRST_STARTUP_CHECK);

        // hybrid role manager used if only users needs to be read-written.
        hybridRoleManager = new JdbcHybridRoleManager(dataSource, tenantId, realmConfig, userRealm);

        // obtain the ldap connection source that was created in
        // DefaultRealmService.
        this.connectionSource = (LDAPConnectionContext) properties
                .get(UserCoreConstants.LDAP_CONNECTION_SOURCE);

        if (connectionSource == null) {
            connectionSource = new LDAPConnectionContext(realmConfig);
        }

        DirContext dirContext = null;
        try {
            dirContext = connectionSource.getContext();
            log.info("LDAP connection created successfully in read-write mode");
        } catch (Exception e) {
            // Skipped to throw a UserStoreException and log the error message in-order to successfully initiate and
            // create the user-store object.
            log.error("Cannot create connection to LDAP server. Connection URL: " + realmConfig
                    .getUserStoreProperty(LDAPConstants.CONNECTION_URL) + " Error message: " + e.getMessage());
        } finally {
            JNDIUtil.closeContext(dirContext);
        }
        this.userRealm = realm;
        //persist domain
        this.persistDomain();
        doInitialSetup();
        if (realmConfig.isPrimary()) {
            addInitialAdminData(Boolean.parseBoolean(realmConfig.getAddAdmin()),
                    !isInitSetupDone());
        }
        /*
		 * Initialize user roles cache as implemented in AbstractUserStoreManager
		 */
        initUserRolesCache();

        initUserCache();

        if (log.isDebugEnabled()) {
            log.debug("Read-Write UserStoreManager initialization ended "
                    + System.currentTimeMillis());
        }
    }

    /**
     * This constructor is not used. So not applying the changes done to above constructor.
     *
     * @param realmConfig
     * @param claimManager
     * @param profileManager
     * @throws UserStoreException
     */
    public ReadWriteLDAPUserStoreManager(RealmConfiguration realmConfig, ClaimManager claimManager,
                                         ProfileConfigurationManager profileManager) throws UserStoreException {
        super(realmConfig, claimManager, profileManager);
    }

    @Override
    public boolean isReadOnly() {
        return false;
    }

    /**
     * @return
     */
    protected String getRealmName() {

        // First check whether realm name is defined in the configuration
        String defaultRealmName = this.realmConfig
                .getUserStoreProperty(UserCoreConstants.RealmConfig.DEFAULT_REALM_NAME);

        if (defaultRealmName != null) {
            return defaultRealmName;
        }

        // If not build the realm name from the search base.
        // Here the realm name will be a concatenation of dc components in the
        // search base.
        String searchBase = this.realmConfig.getUserStoreProperty(LDAPConstants.USER_SEARCH_BASE);

        String[] domainComponents = searchBase.split("dc=");

        StringBuilder builder = new StringBuilder();

        for (String dc : domainComponents) {
            if (!dc.contains("=")) {
                String trimmedDc = dc.trim();
                if (trimmedDc.endsWith(",")) {
                    builder.append(trimmedDc.replace(',', '.'));
                } else {
                    builder.append(trimmedDc);
                }
            }
        }

        return builder.toString().toUpperCase(Locale.ENGLISH);
    }

    @Override
    public void doAddUser(String userName, Object credential, String[] roleList,
                          Map<String, String> claims, String profileName) throws UserStoreException {
        this.doAddUser(userName, credential, roleList, claims, profileName, false);
    }

    @Override
    public void doAddUser(String userName, Object credential, String[] roleList,
                          Map<String, String> claims, String profileName, boolean requirePasswordChange)
            throws UserStoreException {

		/* getting search base directory context */
        DirContext dirContext = getSearchBaseDirectoryContext();

		/* getting add user basic attributes */
        BasicAttributes basicAttributes = getAddUserBasicAttributes(userName);

        BasicAttribute userPassword = new BasicAttribute("userPassword");
        String passwordHashMethod = this.realmConfig.getUserStoreProperty(PASSWORD_HASH_METHOD);
        if (passwordHashMethod == null) {
            passwordHashMethod = realmConfig.getUserStoreProperty("passwordHashMethod");
        }
        byte[] passwordToStore = UserCoreUtil.getPasswordToStore(credential, this.realmConfig.getUserStoreProperty
                (PASSWORD_HASH_METHOD), kdcEnabled);
        userPassword.add(passwordToStore);
        basicAttributes.put(userPassword);

		/* setting claims */
        setUserClaims(claims, basicAttributes, userName);

        try {

            NameParser ldapParser = dirContext.getNameParser("");
            Name compoundName = ldapParser.parse(realmConfig
                    .getUserStoreProperty(LDAPConstants.USER_NAME_ATTRIBUTE) + "=" + escapeSpecialCharactersForDN(userName));

            if (log.isDebugEnabled()) {
                log.debug("Binding user: " + compoundName);
            }
            dirContext.bind(compoundName, null, basicAttributes);
        } catch (NamingException e) {
            String errorMessage = "Cannot access the directory context or "
                                  + "user already exists in the system for user :" + userName;
            if (log.isDebugEnabled()) {
                log.debug(errorMessage, e);
            }
            throw new UserStoreException(errorMessage, e);
        } finally {
            JNDIUtil.closeContext(dirContext);
            // Clearing password byte array
            UserCoreUtil.clearSensitiveBytes(passwordToStore);
        }

        if(roleList != null && roleList.length > 0) {
            try {
            /* update the user roles */
                doUpdateRoleListOfUser(userName, null, roleList);
                if (log.isDebugEnabled()) {
                    log.debug("Roles are added for user  : " + userName + " successfully.");
                }
            } catch (UserStoreException e) {
                String errorMessage = "User is added. But error while updating role list of user : " + userName;
                if (log.isDebugEnabled()) {
                    log.debug(errorMessage, e);
                }
                throw new UserStoreException(errorMessage, e);
            }
        }
    }

    /**
     * Does required checks before adding the user
     *
     * @param userName
     * @param credential
     * @throws UserStoreException
     */
    protected void doAddUserValidityChecks(String userName, Object credential)
            throws UserStoreException {

        if (!checkUserNameValid(userName)) {
            throw new UserStoreException(
                    "User name not valid. User name must be a non null string with following format, "
                            + realmConfig
                            .getUserStoreProperty(UserCoreConstants.RealmConfig.PROPERTY_USER_NAME_JAVA_REG_EX));
        }
        if (!checkUserPasswordValid(credential)) {
            String regularExpression = realmConfig.getUserStoreProperty(UserCoreConstants.RealmConfig
                    .PROPERTY_USER_NAME_JAVA_REG_EX);
            //Inorder to support both UsernameJavaRegEx and UserNameJavaRegEx.
            if (StringUtils.isEmpty(regularExpression) || StringUtils.isEmpty(regularExpression.trim())) {
                regularExpression = realmConfig.getUserStoreProperty(UserCoreConstants.RealmConfig
                        .PROPERTY_USER_NAME_JAVA_REG);
            }
            throw new UserStoreException(
                    "Credential not valid. Credential must be a non null string with following format, "
                            + regularExpression);
        }
        if (isExistingUser(userName)) {
            throw new UserStoreException("User " + userName + " already exist in the LDAP");
        }
    }

    /**
     * Returns the directory context for the user search base
     *
     * @return
     * @throws NamingException
     * @throws UserStoreException
     */
    protected DirContext getSearchBaseDirectoryContext() throws UserStoreException {
        DirContext mainDirContext = this.connectionSource.getContext();
        // assume first search base in case of multiple definitions
        String searchBase = realmConfig.getUserStoreProperty(LDAPConstants.USER_SEARCH_BASE).split("#")[0];
        try {
            return (DirContext) mainDirContext.lookup(escapeDNForSearch(searchBase));
        } catch (NamingException e) {
            String errorMessage = "Can not access the directory context or"
                    + "user already exists in the system";
            if (log.isDebugEnabled()) {
                log.debug(errorMessage, e);
            }
            throw new UserStoreException(errorMessage, e);
        } finally {
            JNDIUtil.closeContext(mainDirContext);
        }
    }

    /**
     * Returns a BasicAttributes object with basic required attributes
     *
     * @param userName
     * @return
     */
    protected BasicAttributes getAddUserBasicAttributes(String userName) {
        BasicAttributes basicAttributes = new BasicAttributes(true);
        String userEntryObjectClassProperty = realmConfig
                .getUserStoreProperty(LDAPConstants.USER_ENTRY_OBJECT_CLASS);
        BasicAttribute objectClass = new BasicAttribute(LDAPConstants.OBJECT_CLASS_NAME);
        String[] objectClassHierarchy = userEntryObjectClassProperty.split("/");
        for (String userObjectClass : objectClassHierarchy) {
            if (userObjectClass != null && !userObjectClass.trim().equals("")) {
                objectClass.add(userObjectClass.trim());
            }
        }
        // If KDC is enabled we have to set KDC specific object classes also
        if (kdcEnabled) {
            // Add Kerberos specific object classes
            objectClass.add("krb5principal");
            objectClass.add("krb5kdcentry");
            objectClass.add("subschema");
        }
        basicAttributes.put(objectClass);
        BasicAttribute userNameAttribute = new BasicAttribute(
                realmConfig.getUserStoreProperty(LDAPConstants.USER_NAME_ATTRIBUTE));
        userNameAttribute.add(userName);
        basicAttributes.put(userNameAttribute);

        if (kdcEnabled) {
            userName = userName + UserCoreConstants.PRINCIPAL_USERNAME_SEPARATOR + Constants.SUPER_TENANT_DOMAIN_NAME;
            String principal = userName + "@" + this.getRealmName();
            BasicAttribute principalAttribute = new BasicAttribute(KRB5_PRINCIPAL_NAME_ATTRIBUTE);
            principalAttribute.add(principal);
            basicAttributes.put(principalAttribute);

            BasicAttribute versionNumberAttribute = new BasicAttribute(
                    KRB5_KEY_VERSION_NUMBER_ATTRIBUTE);
            versionNumberAttribute.add("0");
            basicAttributes.put(versionNumberAttribute);
        }
        return basicAttributes;
    }

    /**
     * Sets the set of claims provided at adding users
     *
     * @param claims
     * @param basicAttributes
     * @throws UserStoreException
     */
    protected void setUserClaims(Map<String, String> claims, BasicAttributes basicAttributes,
                                 String userName) throws UserStoreException {
        BasicAttribute claim;
        boolean debug = log.isDebugEnabled();

        log.debug("Processing user claims");
		/*
		 * we keep boolean values to know whether compulsory attributes 'sn' and 'cn' are set during
		 * setting claims.
		 */
        boolean isSNExists = false;
        boolean isCNExists = false;

        if (claims != null) {
            for (Map.Entry<String, String> entry : claims.entrySet()) {
				/*
				 * LDAP does not allow for empty values. If an attribute has a value it’s stored
				 * with the entry, otherwise it is not. Hence needs to check for empty values before
				 * storing the attribute.
				 */
                if (EMPTY_ATTRIBUTE_STRING.equals(entry.getValue())) {
                    continue;
                }
                // needs to get attribute name from claim mapping
                String claimURI = entry.getKey();

                if (debug) {
                    log.debug("Claim URI: " + claimURI);
                }

                String attributeName = null;
                try {
                    attributeName = getClaimAtrribute(claimURI, userName, null);
                } catch (org.wso2.micro.integrator.security.user.api.UserStoreException e) {
                    String errorMessage = "Error in obtaining claim mapping.";
                    throw new UserStoreException(errorMessage, e);
                }

                if (ATTR_NAME_CN.equals(attributeName)) {
                    isCNExists = true;
                } else if (ATTR_NAME_SN.equals(attributeName)) {
                    isSNExists = true;
                }

                if (debug) {
                    log.debug("Mapped attribute: " + attributeName);
                    log.debug("Attribute value: " + claims.get(entry.getKey()));
                }
                claim = new BasicAttribute(attributeName);
                claim.add(claims.get(entry.getKey()));
                basicAttributes.put(claim);
            }
        }

        // If required attributes cn, sn are not set during claim mapping,
        // set them as user names

        if (!isCNExists) {
            BasicAttribute cn = new BasicAttribute("cn");
            cn.add(userName);
            basicAttributes.put(cn);
        }

        if (!isSNExists) {
            BasicAttribute sn = new BasicAttribute("sn");
            sn.add(userName);
            basicAttributes.put(sn);
        }
    }

    @SuppressWarnings("deprecation")
    @Override
    public void doDeleteUser(String userName) throws UserStoreException {

        boolean debug = log.isDebugEnabled();

        if (debug) {
            log.debug("Deleting user: " + userName);
        }
        // delete user from LDAP group if read-write enabled.
        String userNameAttribute = realmConfig
                .getUserStoreProperty(LDAPConstants.USER_NAME_ATTRIBUTE);
        String searchFilter = realmConfig
                .getUserStoreProperty(LDAPConstants.USER_NAME_SEARCH_FILTER);
        searchFilter = searchFilter.replace("?", escapeSpecialCharactersForFilter(userName));
        String[] returningUserAttributes = new String[]{userNameAttribute};

        DirContext mainDirContext = this.connectionSource.getContext();

        NamingEnumeration<SearchResult> userResults = searchInUserBase(searchFilter,
                returningUserAttributes, SearchControls.SUBTREE_SCOPE, mainDirContext);
        NamingEnumeration<SearchResult> groupResults = null;

        DirContext subDirContext = null;
        try {
            SearchResult userResult = null;
            String userDN = null;
            // here we assume only one user
            // TODO: what to do if there are more than one user
            while (userResults.hasMore()) {
                userResult = userResults.next();
                userDN = userResult.getName();
                log.debug("User DN: " + userDN);
            }

            // LDAP roles of user to delete the mapping

            List<String> roles = new ArrayList<String>();
            String[] externalRoles = doGetExternalRoleListOfUser(userName, "*");
            roles.addAll(Arrays.asList(externalRoles));
            if (isSharedGroupEnabled()) {
                String[] sharedRoles = doGetSharedRoleListOfUser(null, userName, "*");
                if (sharedRoles != null) {
                    roles.addAll(Arrays.asList(sharedRoles));
                }
            }
            String[] rolesOfUser = roles.toArray(new String[roles.size()]);

            if (rolesOfUser.length != 0) {

                String[] returningGroupAttributes = new String[]{realmConfig
                        .getUserStoreProperty(LDAPConstants.MEMBERSHIP_ATTRIBUTE)};
                for (String role : rolesOfUser) {

                    RoleContext context = createRoleContext(role);
                    String searchBase = ((LDAPRoleContext) context).getSearchBase();
                    searchFilter = ((LDAPRoleContext) context).getSearchFilter();
                    role = context.getRoleName();

                    if (role.indexOf(UserCoreConstants.DOMAIN_SEPARATOR) > -1) {
                        role = (role.split(UserCoreConstants.DOMAIN_SEPARATOR))[1];
                    }
                    String grpSearchFilter = searchFilter.replace("?", escapeSpecialCharactersForFilter(role));
                    groupResults =
                            searchInGroupBase(grpSearchFilter, returningGroupAttributes,
                                    SearchControls.SUBTREE_SCOPE, mainDirContext,
                                    searchBase);
                    SearchResult groupResult = null;
                    while (groupResults.hasMore()) {
                        groupResult = groupResults.next();
                    }
                    if (isOnlyUserInRole(userDN, groupResult) && !emptyRolesAllowed) {
                        String errorMessage = "User: " + userName + " is the only user " + "in "
                                + role + "." + "There should be at " + "least one user"
                                + " in the role. Hence can" + " not delete the user.";
                        throw new UserStoreException(errorMessage);
                    }
                }
                // delete role list
                doUpdateRoleListOfUser(userName, rolesOfUser, new String[]{});
            }

            // delete user entry if it exist
            if (userResult != null &&
                    userResult.getAttributes().get(userNameAttribute).get().toString().toLowerCase()
                            .equals(userName.toLowerCase())) {
                if (log.isDebugEnabled()) {
                    log.debug("Deleting " + userDN + " with search base " + userSearchBase);
                }
                subDirContext = (DirContext) mainDirContext.lookup(escapeDNForSearch(userSearchBase));
                subDirContext.destroySubcontext(userDN);
            }
            removeFromUserCache(userName);
        } catch (NamingException e) {
            String errorMessage = "Error occurred while deleting the user : " + userName;
            if (log.isDebugEnabled()) {
                log.debug(errorMessage, e);
            }
            throw new UserStoreException(errorMessage, e);
        } finally {
            JNDIUtil.closeNamingEnumeration(groupResults);
            JNDIUtil.closeNamingEnumeration(userResults);

            JNDIUtil.closeContext(subDirContext);
            JNDIUtil.closeContext(mainDirContext);
        }
    }

    @SuppressWarnings("rawtypes")
    @Override
    public void doUpdateCredential(String userName, Object newCredential, Object oldCredential)
            throws UserStoreException {

        DirContext dirContext = this.connectionSource.getContext();
        DirContext subDirContext = null;
        // first search the existing user entry.
        String searchBase = realmConfig.getUserStoreProperty(LDAPConstants.USER_SEARCH_BASE);
        String searchFilter = realmConfig.getUserStoreProperty(LDAPConstants.USER_NAME_SEARCH_FILTER);
        searchFilter = searchFilter.replace("?", escapeSpecialCharactersForFilter(userName));

        SearchControls searchControls = new SearchControls();
        searchControls.setSearchScope(SearchControls.SUBTREE_SCOPE);
        searchControls.setReturningAttributes(new String[]{"userPassword"});

        NamingEnumeration<SearchResult> namingEnumeration = null;
        NamingEnumeration passwords = null;

        try {
            namingEnumeration = dirContext.search(escapeDNForSearch(searchBase),searchFilter, searchControls);
            // here we assume only one user
            // TODO: what to do if there are more than one user
            SearchResult searchResult = null;
            String passwordHashMethod = realmConfig.getUserStoreProperty(PASSWORD_HASH_METHOD);
            if (passwordHashMethod == null) {
                passwordHashMethod = realmConfig.getUserStoreProperty("passwordHashMethod");
            }
            while (namingEnumeration.hasMore()) {
                searchResult = namingEnumeration.next();

                String dnName = searchResult.getName();
                subDirContext = (DirContext) dirContext.lookup(escapeDNForSearch(searchBase));

                byte[] passwordToStore = UserCoreUtil.getPasswordToStore(newCredential, passwordHashMethod, kdcEnabled);
                try {
                    Attribute passwordAttribute = new BasicAttribute("userPassword");
                    passwordAttribute.add(passwordToStore);
                    BasicAttributes basicAttributes = new BasicAttributes(true);
                    basicAttributes.put(passwordAttribute);
                    subDirContext.modifyAttributes(dnName, DirContext.REPLACE_ATTRIBUTE, basicAttributes);
                } finally {
                    // Clearing password bytes
                    UserCoreUtil.clearSensitiveBytes(passwordToStore);
                }
            }
            // we check whether both carbon admin entry and ldap connection
            // entry are the same
            if (searchResult.getNameInNamespace()
                    .equals(realmConfig.getUserStoreProperty(LDAPConstants.CONNECTION_NAME))) {
                this.connectionSource.updateCredential(newCredential);
            }

        } catch (NamingException e) {
            String errorMessage = "Can not access the directory service for user : " + userName;
            if (log.isDebugEnabled()) {
                log.debug(errorMessage, e);
            }
            throw new UserStoreException(errorMessage, e);
        } finally {
            JNDIUtil.closeNamingEnumeration(passwords);
            JNDIUtil.closeNamingEnumeration(namingEnumeration);

            JNDIUtil.closeContext(subDirContext);
            JNDIUtil.closeContext(dirContext);
        }
    }


    @Override
    public void doUpdateCredentialByAdmin(String userName, Object newCredential)
            throws UserStoreException {

        DirContext dirContext = this.connectionSource.getContext();
        DirContext subDirContext = null;
        // first search the existing user entry.
        String searchBase = realmConfig.getUserStoreProperty(LDAPConstants.USER_SEARCH_BASE);
        String searchFilter = realmConfig
                .getUserStoreProperty(LDAPConstants.USER_NAME_SEARCH_FILTER);
        searchFilter = searchFilter.replace("?", escapeSpecialCharactersForFilter(userName));

        SearchControls searchControls = new SearchControls();
        searchControls.setSearchScope(SearchControls.SUBTREE_SCOPE);
        searchControls.setReturningAttributes(new String[]{"userPassword"});

        NamingEnumeration<SearchResult> namingEnumeration = null;
        NamingEnumeration passwords = null;

        try {
            namingEnumeration = dirContext.search(escapeDNForSearch(searchBase), searchFilter, searchControls);
            // here we assume only one user
            // TODO: what to do if there are more than one user
            // there can be only only on user

            SearchResult searchResult = null;
            while (namingEnumeration.hasMore()) {
                searchResult = namingEnumeration.next();
                String passwordHashMethod = realmConfig.getUserStoreProperty(PASSWORD_HASH_METHOD);
                if (passwordHashMethod == null) {
                    passwordHashMethod = realmConfig.getUserStoreProperty("passwordHashMethod");
                }
                if (!UserCoreConstants.RealmConfig.PASSWORD_HASH_METHOD_PLAIN_TEXT.
                        equalsIgnoreCase(passwordHashMethod)) {
                    Attributes attributes = searchResult.getAttributes();
                    Attribute userPassword = attributes.get("userPassword");
                    // When admin changes other user passwords he do not have to
                    // provide the old password. Here it is only possible to have one password, if there
                    // are more every one should match with the given old password
                    passwords = userPassword.getAll();
                    if (passwords.hasMore()) {
                        byte[] byteArray = (byte[]) passwords.next();
                        String password = new String(byteArray);

                        if (password.startsWith("{")) {
                            passwordHashMethod = password.substring(password.indexOf('{') + 1,
                                    password.indexOf('}'));
                        }
                    }
                }

                String dnName = searchResult.getName();
                subDirContext = (DirContext) dirContext.lookup(escapeDNForSearch(searchBase));

                byte[] passwordToStore = UserCoreUtil.getPasswordToStore(newCredential, passwordHashMethod, kdcEnabled);
                try {
                    Attribute passwordAttribute = new BasicAttribute("userPassword");
                    passwordAttribute.add(passwordToStore);
                    BasicAttributes basicAttributes = new BasicAttributes(true);
                    basicAttributes.put(passwordAttribute);
                    subDirContext.modifyAttributes(dnName, DirContext.REPLACE_ATTRIBUTE, basicAttributes);
                } finally {
                    // Clearing password bytes
                    UserCoreUtil.clearSensitiveBytes(passwordToStore);
                }
            }
            // we check whether both carbon admin entry and ldap connection
            // entry are the same
            if (searchResult.getNameInNamespace().equals(
                    realmConfig.getUserStoreProperty(LDAPConstants.CONNECTION_NAME))) {
                this.connectionSource.updateCredential(newCredential);
            }

        } catch (NamingException e) {
            String errorMessage = "Can not access the directory service for user : " + userName;
            if (log.isDebugEnabled()) {
                log.debug(errorMessage, e);
            }
            throw new UserStoreException(errorMessage, e);
        } finally {
            JNDIUtil.closeNamingEnumeration(passwords);
            JNDIUtil.closeNamingEnumeration(namingEnumeration);

            JNDIUtil.closeContext(subDirContext);
            JNDIUtil.closeContext(dirContext);
        }
    }

    /**
     * @param userName
     * @param newCredential
     * @throws UserStoreException
     */
    protected void doUpdateCredentialsValidityChecks(String userName, Object newCredential)
            throws UserStoreException {
        if (!isExistingUser(userName)) {
            throw new UserStoreException("User " + userName + " does not exisit in the user store");
        }

        if (!checkUserPasswordValid(newCredential)) {
            throw new UserStoreException(
                    "Credential not valid. Credential must be a non null string with following format, "
                            + realmConfig
                            .getUserStoreProperty(UserCoreConstants.RealmConfig.PROPERTY_JAVA_REG_EX));
        }
    }

    @Override
    public Map<String, String> getProperties(Tenant tenant) throws UserStoreException {
        Map<String, String> existingProperties = this.realmConfig.getUserStoreProperties();
        String tenantSufix = getTenantSuffix(tenant.getDomain());
        String propertyName = null;
        Map<String, String> newProperties = new HashMap<String, String>();
        for (Map.Entry<String, String> iter : existingProperties.entrySet()) {
            propertyName = iter.getKey();
            if (propertyName.equals(LDAPConstants.USER_SEARCH_BASE)) {
                newProperties.put(propertyName, tenantSufix);
            } else {
                newProperties.put(propertyName, iter.getValue());
            }
        }
        return newProperties;
    }

    /**
     * @param domain
     * @return
     */
    private String getTenantSuffix(String domain) {
        // here we use a simple algorithum by splitting the domain with .
        String[] domainParts = domain.split("\\.");
        StringBuffer suffixName = new StringBuffer();
        for (String domainPart : domainParts) {
            suffixName.append(",dc=").append(domainPart);
        }
        return suffixName.toString().replaceFirst(",", "");
    }

    /**
     * This method overwrites the method in LDAPUserStoreManager. This implements the functionality
     * of updating user's profile information in LDAP user store.
     *
     * @param userName
     * @param claims
     * @param profileName
     * @throws UserStoreException
     */
    @Override
    public void doSetUserClaimValues(String userName, Map<String, String> claims, String profileName)
            throws UserStoreException {

        // get the LDAP Directory context
        DirContext dirContext = this.connectionSource.getContext();
        DirContext subDirContext = null;
        // search the relevant user entry by user name
        String userSearchBase = realmConfig.getUserStoreProperty(LDAPConstants.USER_SEARCH_BASE);
        String userSearchFilter = realmConfig
                .getUserStoreProperty(LDAPConstants.USER_NAME_SEARCH_FILTER);
        // if user name contains domain name, remove domain name
        String[] userNames = userName.split(UserCoreConstants.DOMAIN_SEPARATOR);
        if (userNames.length > 1) {
            userName = userNames[1];
        }
        userSearchFilter = userSearchFilter.replace("?", escapeSpecialCharactersForFilter(userName));

        SearchControls searchControls = new SearchControls();
        searchControls.setSearchScope(SearchControls.SUBTREE_SCOPE);
        searchControls.setReturningAttributes(null);

        NamingEnumeration<SearchResult> returnedResultList = null;
        String returnedUserEntry = "";

        try {
            returnedResultList = dirContext.search(escapeDNForSearch(userSearchBase), userSearchFilter, searchControls);
            // assume only one user is returned from the search
            // TODO:what if more than one user is returned
            if(returnedResultList.hasMore()){
                returnedUserEntry = returnedResultList.next().getName();
            }

        } catch (NamingException e) {
            String errorMessage = "Results could not be retrieved from the directory context for user : " + userName;
            if (log.isDebugEnabled()) {
                log.debug(errorMessage, e);
            }
            throw new UserStoreException(errorMessage, e);
        } finally {
            JNDIUtil.closeNamingEnumeration(returnedResultList);
        }

        if (profileName == null) {

            profileName = UserCoreConstants.DEFAULT_PROFILE;
        }

        if (claims.get(UserCoreConstants.PROFILE_CONFIGURATION) == null) {

            claims.put(UserCoreConstants.PROFILE_CONFIGURATION,
                    UserCoreConstants.DEFAULT_PROFILE_CONFIGURATION);
        }
        try {
            Attributes updatedAttributes = new BasicAttributes(true);

            for (Map.Entry<String, String> claimEntry : claims.entrySet()) {
                String claimURI = claimEntry.getKey();
                // if there is no attribute for profile configuration in LDAP,
                // skip updating it.
                if (claimURI.equals(UserCoreConstants.PROFILE_CONFIGURATION)) {
                    continue;
                }
                // get the claimMapping related to this claimURI
                String attributeName = getClaimAtrribute(claimURI, userName, null);
                //remove user DN from cache if changing username attribute
                if (realmConfig.getUserStoreProperty(LDAPConstants.USER_NAME_ATTRIBUTE).equals
                        (attributeName)) {
                    removeFromUserCache(userName);
                }
                // if uid attribute value contains domain name, remove domain
                // name
                if (attributeName.equals("uid")) {
                    // if user name contains domain name, remove domain name
                    String uidName = claimEntry.getValue();
                    String[] uidNames = uidName.split(UserCoreConstants.DOMAIN_SEPARATOR);
                    if (uidNames.length > 1) {
                        uidName = uidNames[1];
                        claimEntry.setValue(uidName);
                    }
//                    claimEntry.setValue(escapeISSpecialCharacters(uidName));
                }
                Attribute currentUpdatedAttribute = new BasicAttribute(attributeName);
				/* if updated attribute value is null, remove its values. */
                if (EMPTY_ATTRIBUTE_STRING.equals(claimEntry.getValue())) {
                    currentUpdatedAttribute.clear();
                } else {
                    String userAttributeSeparator = ",";
                    if (claimEntry.getValue() != null && !attributeName.equals("uid") && !attributeName.equals("sn")) {
                        String claimSeparator = realmConfig.getUserStoreProperty(MULTI_ATTRIBUTE_SEPARATOR);
                        if (claimSeparator != null && !claimSeparator.trim().isEmpty()) {
                            userAttributeSeparator = claimSeparator;
                        }
                        if (claimEntry.getValue().contains(userAttributeSeparator)) {
                            String[] claimValues = claimEntry.getValue().split(Pattern.quote(userAttributeSeparator));
                            for (String claimValue : claimValues) {
                                if (claimValue != null && claimValue.trim().length() > 0) {
                                    currentUpdatedAttribute.add(claimValue);
                                }
                            }
                        } else {
                            currentUpdatedAttribute.add(claimEntry.getValue());
                        }
                    } else {
                        currentUpdatedAttribute.add(claimEntry.getValue());
                    }
                }
                updatedAttributes.put(currentUpdatedAttribute);
            }
            // update the attributes in the relevant entry of the directory
            // store

            subDirContext = (DirContext) dirContext.lookup(escapeDNForSearch(userSearchBase));
            subDirContext.modifyAttributes(returnedUserEntry, DirContext.REPLACE_ATTRIBUTE,
                    updatedAttributes);

        } catch (Exception e) {
            handleException(e, userName);
        } finally {
            JNDIUtil.closeContext(subDirContext);
            JNDIUtil.closeContext(dirContext);
        }

    }

    @Override
    public void doSetUserClaimValue(String userName, String claimURI, String value,
                                    String profileName) throws UserStoreException {

        // get the LDAP Directory context
        DirContext dirContext = this.connectionSource.getContext();
        DirContext subDirContext = null;
        // search the relevant user entry by user name
        String userSearchBase = realmConfig.getUserStoreProperty(LDAPConstants.USER_SEARCH_BASE);
        String userSearchFilter = realmConfig
                .getUserStoreProperty(LDAPConstants.USER_NAME_SEARCH_FILTER);
        // if user name contains domain name, remove domain name
        String[] userNames = userName.split(UserCoreConstants.DOMAIN_SEPARATOR);
        if (userNames.length > 1) {
            userName = userNames[1];
        }
        userSearchFilter = userSearchFilter.replace("?", escapeSpecialCharactersForFilter(userName));

        SearchControls searchControls = new SearchControls();
        searchControls.setSearchScope(SearchControls.SUBTREE_SCOPE);
        searchControls.setReturningAttributes(null);

        NamingEnumeration<SearchResult> returnedResultList = null;
        String returnedUserEntry = null;

        try {

            returnedResultList = dirContext.search(escapeDNForSearch(userSearchBase), userSearchFilter, searchControls);
            // assume only one user is returned from the search
            // TODO:what if more than one user is returned
            returnedUserEntry = returnedResultList.next().getName();

        } catch (NamingException e) {
            String errorMessage = "Results could not be retrieved from the directory context for user : " + userName;
            if (log.isDebugEnabled()) {
                log.debug(errorMessage, e);
            }
            throw new UserStoreException(errorMessage, e);
        } finally {
            JNDIUtil.closeNamingEnumeration(returnedResultList);
        }

        try {
            Attributes updatedAttributes = new BasicAttributes(true);
            // if there is no attribute for profile configuration in LDAP, skip
            // updating it.
            // get the claimMapping related to this claimURI
            String attributeName = null;
            attributeName = getClaimAtrribute(claimURI, userName, null);

            Attribute currentUpdatedAttribute = new BasicAttribute(attributeName);
			/* if updated attribute value is null, remove its values. */
            if (EMPTY_ATTRIBUTE_STRING.equals(value)) {
                currentUpdatedAttribute.clear();
            } else {
                if (attributeName.equals("uid") || attributeName.equals("sn")) {
                    currentUpdatedAttribute.add(value);
                } else {
                    String userAttributeSeparator = ",";
                    String claimSeparator = realmConfig.getUserStoreProperty(MULTI_ATTRIBUTE_SEPARATOR);
                    if (claimSeparator != null && !claimSeparator.trim().isEmpty()) {
                        userAttributeSeparator = claimSeparator;
                    }

                    if (value.contains(userAttributeSeparator)) {
                        StringTokenizer st = new StringTokenizer(value, userAttributeSeparator);
                        while (st.hasMoreElements()) {
                            String newVal = st.nextElement().toString();
                            if (newVal != null && newVal.trim().length() > 0) {
                                currentUpdatedAttribute.add(newVal.trim());
                            }
                        }
                    } else {
                        currentUpdatedAttribute.add(value);
                    }

                }
            }
            updatedAttributes.put(currentUpdatedAttribute);

            // update the attributes in the relevant entry of the directory
            // store

            subDirContext = (DirContext) dirContext.lookup(escapeDNForSearch(userSearchBase));
            subDirContext.modifyAttributes(returnedUserEntry, DirContext.REPLACE_ATTRIBUTE,
                    updatedAttributes);

        } catch (Exception e) {
            handleException(e, userName);
        } finally {
            JNDIUtil.closeContext(subDirContext);
            JNDIUtil.closeContext(dirContext);
        }

    }


    @Override
    public void doDeleteUserClaimValue(String userName, String claimURI, String profileName) throws UserStoreException {

        // get the LDAP Directory context
        DirContext dirContext = this.connectionSource.getContext();
        DirContext subDirContext = null;
        // search the relevant user entry by user name
        String userSearchBase = realmConfig.getUserStoreProperty(LDAPConstants.USER_SEARCH_BASE);
        String userSearchFilter = realmConfig
                .getUserStoreProperty(LDAPConstants.USER_NAME_SEARCH_FILTER);
        userSearchFilter = userSearchFilter.replace("?", escapeSpecialCharactersForFilter(userName));

        SearchControls searchControls = new SearchControls();
        searchControls.setSearchScope(SearchControls.SUBTREE_SCOPE);
        searchControls.setReturningAttributes(null);

        NamingEnumeration<SearchResult> returnedResultList = null;
        String returnedUserEntry = null;

        try {

            returnedResultList = dirContext.search(escapeDNForSearch(userSearchBase), userSearchFilter, searchControls);
            // assume only one user is returned from the search
            // TODO:what if more than one user is returned
            returnedUserEntry = returnedResultList.next().getName();

        } catch (NamingException e) {
            String errorMessage = "Results could not be retrieved from the directory context for user : " + userName;
            if (log.isDebugEnabled()) {
                log.debug(errorMessage, e);
            }
            throw new UserStoreException(errorMessage, e);
        } finally {
            JNDIUtil.closeNamingEnumeration(returnedResultList);
        }

        try {
            Attributes updatedAttributes = new BasicAttributes(true);
            // if there is no attribute for profile configuration in LDAP, skip
            // updating it.
            // get the claimMapping related to this claimURI
            String attributeName = null;
            attributeName = getClaimAtrribute(claimURI, userName, null);

            Attribute currentUpdatedAttribute = new BasicAttribute(attributeName);

            updatedAttributes.put(currentUpdatedAttribute);

            subDirContext = (DirContext) dirContext.lookup(escapeDNForSearch(userSearchBase));
            subDirContext.modifyAttributes(returnedUserEntry, DirContext.REMOVE_ATTRIBUTE,
                    updatedAttributes);

        } catch (Exception e) {
            handleException(e, userName);
        } finally {
            JNDIUtil.closeContext(subDirContext);
            JNDIUtil.closeContext(dirContext);
        }
    }

    @Override
    public void doDeleteUserClaimValues(String userName, String[] claims, String profileName) throws UserStoreException {
        // get the LDAP Directory context
        DirContext dirContext = this.connectionSource.getContext();
        DirContext subDirContext = null;
        // search the relevant user entry by user name
        String userSearchBase = realmConfig.getUserStoreProperty(LDAPConstants.USER_SEARCH_BASE);
        String userSearchFilter = realmConfig
                .getUserStoreProperty(LDAPConstants.USER_NAME_SEARCH_FILTER);
        userSearchFilter = userSearchFilter.replace("?", escapeSpecialCharactersForFilter(userName));

        SearchControls searchControls = new SearchControls();
        searchControls.setSearchScope(SearchControls.SUBTREE_SCOPE);
        searchControls.setReturningAttributes(null);

        NamingEnumeration<SearchResult> returnedResultList = null;
        String returnedUserEntry = null;

        try {

            returnedResultList = dirContext.search(escapeDNForSearch(userSearchBase), userSearchFilter, searchControls);
            // assume only one user is returned from the search
            // TODO:what if more than one user is returned
            returnedUserEntry = returnedResultList.next().getName();

        } catch (NamingException e) {
            String errorMessage = "Results could not be retrieved from the directory context for user : " + userName;
            if (log.isDebugEnabled()) {
                log.debug(errorMessage, e);
            }
            throw new UserStoreException(errorMessage, e);
        } finally {
            JNDIUtil.closeNamingEnumeration(returnedResultList);
        }

        try {
            Attributes updatedAttributes = new BasicAttributes(true);
            // if there is no attribute for profile configuration in LDAP, skip
            // updating it.
            // get the claimMapping related to this claimURI

            for (String claimURI : claims) {
                String attributeName = getClaimAtrribute(claimURI, userName, null);
                Attribute currentUpdatedAttribute = new BasicAttribute(attributeName);
                updatedAttributes.put(currentUpdatedAttribute);
            }

            subDirContext = (DirContext) dirContext.lookup(escapeDNForSearch(userSearchBase));
            subDirContext.modifyAttributes(returnedUserEntry, DirContext.REMOVE_ATTRIBUTE,
                    updatedAttributes);

        } catch (Exception e) {
            handleException(e, userName);
        } finally {
            JNDIUtil.closeContext(subDirContext);
            JNDIUtil.closeContext(dirContext);
        }
    }

    /**
     * Add roles by writing groups to LDAP.
     *
     * @param roleName
     * @param userList
     * @throws UserStoreException
     */
    @Override
    public void doAddRole(String roleName, String[] userList, boolean shared)
            throws UserStoreException {

        RoleContext roleContext;
        roleContext = createRoleContext(roleName);
        roleContext.setMembers(userList);
        addLDAPRole(roleContext);
        if (shared && isSharedGroupEnabled()) {
            roleName = roleName + UserCoreConstants.TENANT_DOMAIN_COMBINER + Constants.SUPER_TENANT_DOMAIN_NAME;
            roleContext = createRoleContext(roleName);
            addLDAPRole(roleContext);
        }
    }

    protected void addLDAPRole(RoleContext context) throws UserStoreException {

        String roleName = context.getRoleName();
        String[] userList = context.getMembers();
        String groupEntryObjectClass = ((LDAPRoleContext) context).getGroupEntryObjectClass();
        String groupNameAttribute = ((LDAPRoleContext) context).getRoleNameProperty();
        String searchBase = ((LDAPRoleContext) context).getSearchBase();

        if ((userList == null || userList.length == 0) && !emptyRolesAllowed) {
            String errorMessage = "Can not create empty role. There should be at least "
                    + "one user for the role.";
            throw new UserStoreException(errorMessage);
        } else if (userList == null && emptyRolesAllowed || userList != null && userList.length > 0
                && !emptyRolesAllowed || emptyRolesAllowed) {

            // if (userList.length > 0) {
            DirContext mainDirContext = this.connectionSource.getContext();
            DirContext groupContext = null;
            NamingEnumeration<SearchResult> results = null;

            try {
                // create the attribute set for group entry
                Attributes groupAttributes = new BasicAttributes(true);

                // create group entry's object class attribute
                Attribute objectClassAttribute = new BasicAttribute(LDAPConstants.OBJECT_CLASS_NAME);
                objectClassAttribute.add(groupEntryObjectClass);
                groupAttributes.put(objectClassAttribute);

                // create cn attribute
                Attribute cnAttribute = new BasicAttribute(groupNameAttribute);
                cnAttribute.add(roleName);
                groupAttributes.put(cnAttribute);
                // following check is for if emptyRolesAllowed made this
                // code executed.
                if (userList != null && userList.length > 0) {

                    String memberAttributeName = realmConfig
                            .getUserStoreProperty(LDAPConstants.MEMBERSHIP_ATTRIBUTE);
                    Attribute memberAttribute = new BasicAttribute(memberAttributeName);
                    for (String userName : userList) {

                        if (userName == null || userName.trim().length() == 0) {
                            continue;
                        }
                        // search the user in user search base
                        String searchFilter = realmConfig
                                .getUserStoreProperty(LDAPConstants.USER_NAME_SEARCH_FILTER);
                        searchFilter = searchFilter.replace("?", escapeSpecialCharactersForFilter(userName));
                        results = searchInUserBase(searchFilter, new String[]{},
                                SearchControls.SUBTREE_SCOPE, mainDirContext);
                        // we assume only one user with the given user
                        // name under user search base.
                        SearchResult userResult = null;
                        if (results.hasMore()) {
                            userResult = results.next();
                        } else {
                            String errorMsg = "There is no user with the user name: " + userName
                                    + " to be added to this role.";
                            logger.error(errorMsg);
                            throw new UserStoreException(errorMsg);
                        }
                        // get his DN
                        String userEntryDN = userResult.getNameInNamespace();
                        // put it as member-attribute value
                        memberAttribute.add(userEntryDN);
                    }
                    groupAttributes.put(memberAttribute);
                }

                groupContext = (DirContext) mainDirContext.lookup(escapeDNForSearch(searchBase));
                NameParser ldapParser = groupContext.getNameParser("");
                /*
                     * Name compoundGroupName = ldapParser.parse(groupNameAttributeName + "=" +
                     * roleName);
                     */
                Name compoundGroupName = ldapParser.parse("cn=" + roleName);
                groupContext.bind(compoundGroupName, null, groupAttributes);

            } catch (NamingException e) {
                String errorMsg = "Role: " + roleName + " could not be added.";
                if (log.isDebugEnabled()) {
                    log.debug(errorMsg, e);
                }
                throw new UserStoreException(errorMsg, e);
            } catch (Exception e) {
                String errorMsg = "Role: " + roleName + " could not be added.";
                if (log.isDebugEnabled()) {
                    log.debug(errorMsg, e);
                }
                throw new UserStoreException(errorMsg, e);
            } finally {
                JNDIUtil.closeNamingEnumeration(results);
                JNDIUtil.closeContext(groupContext);
                JNDIUtil.closeContext(mainDirContext);
            }

        }

    }


    /**
     * Update role list of user by writing to LDAP.
     *
     * @param userName
     * @param deletedRoles
     * @param newRoles
     * @throws UserStoreException
     */
    @SuppressWarnings("deprecation")
    @Override
    public void doUpdateRoleListOfUser(String userName, String[] deletedRoles, String[] newRoles)
            throws UserStoreException {

        // get the DN of the user entry
        String userNameDN = this.getNameInSpaceForUserName(userName);
        String membershipAttribute =
                realmConfig.getUserStoreProperty(LDAPConstants.MEMBERSHIP_ATTRIBUTE);
        String roleNameAttribute =
                realmConfig.getUserStoreProperty(LDAPConstants.GROUP_NAME_ATTRIBUTE);

		/*
		 * check deleted roles and delete member entries from relevant groups.
		 */
        String errorMessage = null;
        String roleSearchFilter = null;

        DirContext mainDirContext = this.connectionSource.getContext();

        try {
            if (deletedRoles != null && deletedRoles.length != 0) {
                // perform validation for empty role occurrences before
                // updating in LDAP
                // check whether this is shared roles and where shared roles are
                // enable

                for (String deletedRole : deletedRoles) {
                    LDAPRoleContext context = (LDAPRoleContext) createRoleContext(deletedRole);
                    deletedRole = context.getRoleName();
                    String searchFilter = context.getSearchFilter();
                    roleSearchFilter = searchFilter.replace("?", escapeSpecialCharactersForFilter(deletedRole));
                    String[] returningAttributes = new String[]{membershipAttribute};
                    String searchBase = context.getSearchBase();
                    NamingEnumeration<SearchResult> groupResults =
                            searchInGroupBase(roleSearchFilter,
                                    returningAttributes,
                                    SearchControls.SUBTREE_SCOPE,
                                    mainDirContext,
                                    searchBase);
                    SearchResult resultedGroup = null;
                    if (groupResults.hasMore()) {
                        resultedGroup = groupResults.next();
                    }
                    if (resultedGroup != null && isOnlyUserInRole(userNameDN, resultedGroup) &&
                            !emptyRolesAllowed) {
                        errorMessage =
                                userName + " is the only user in the role: " + deletedRole +
                                        ". Hence can not delete user from role.";
                        throw new UserStoreException(errorMessage);
                    }

                    JNDIUtil.closeNamingEnumeration(groupResults);
                }
                // if empty role violation does not happen, continue
                // updating the LDAP.
                for (String deletedRole : deletedRoles) {

                    if (StringUtils.isNotEmpty(deletedRole)) {
                        LDAPRoleContext context = (LDAPRoleContext) createRoleContext(deletedRole);
                        deletedRole = context.getRoleName();
                        String searchFilter = context.getSearchFilter();

                        if (isExistingRole(deletedRole)) {
                            roleSearchFilter = searchFilter.replace("?", escapeSpecialCharactersForFilter(deletedRole));
                            String[] returningAttributes = new String[]{membershipAttribute, roleNameAttribute};
                            String searchBase = context.getSearchBase();
                            NamingEnumeration<SearchResult> groupResults =
                                    searchInGroupBase(roleSearchFilter,
                                            returningAttributes,
                                            SearchControls.SUBTREE_SCOPE,
                                            mainDirContext,
                                            searchBase);
                            SearchResult resultedGroup = null;
                            String groupDN = null;
                            if (groupResults.hasMore()) {
                                resultedGroup = groupResults.next();
                                groupDN = getGroupName(resultedGroup);
                            }
                            if (resultedGroup != null && isUserInRole(userNameDN, resultedGroup)) {
                                this.modifyUserInRole(userNameDN, groupDN, DirContext.REMOVE_ATTRIBUTE,
                                        searchBase);
                            } else {
                                errorMessage =
                                        "User: " + URLEncoder.encode(userName, String.valueOf
                                                (StandardCharsets.UTF_8)) + " does not belongs to role: " +
                                                URLEncoder.encode(deletedRole, String.valueOf(StandardCharsets.UTF_8));
                                throw new UserStoreException(errorMessage);
                            }

                            JNDIUtil.closeNamingEnumeration(groupResults);

                            // need to update authz cache of user since roles
                            // are deleted
                            String userNameWithDomain = UserCoreUtil.addDomainToName(userName, getMyDomainName());
//                            userRealm.getAuthorizationManager().clearUserAuthorization(userNameWithDomain);

                        } else {
                            errorMessage = "The role: " + URLEncoder.encode(deletedRole, String.valueOf
                                    (StandardCharsets.UTF_8)) + " does not exist.";
                            throw new UserStoreException(errorMessage);
                        }
                    }
                }
            }
            if (newRoles != null && newRoles.length != 0) {

                for (String newRole : newRoles) {

                    if (StringUtils.isNotEmpty(newRole)) {
                        LDAPRoleContext context = (LDAPRoleContext) createRoleContext(newRole);
                        newRole = context.getRoleName();
                        String searchFilter = context.getSearchFilter();

                        if (isExistingRole(newRole)) {
                            roleSearchFilter = searchFilter.replace("?", escapeSpecialCharactersForFilter(newRole));
                            String[] returningAttributes = new String[]{membershipAttribute, roleNameAttribute};
                            String searchBase = context.getSearchBase();

                            NamingEnumeration<SearchResult> groupResults =
                                    searchInGroupBase(roleSearchFilter,
                                            returningAttributes,
                                            SearchControls.SUBTREE_SCOPE,
                                            mainDirContext,
                                            searchBase);
                            SearchResult resultedGroup = null;
                            // assume only one group with given group name
                            String groupDN = null;
                            if (groupResults.hasMore()) {
                                resultedGroup = groupResults.next();
                                groupDN = getGroupName(resultedGroup);
                            }
                            if (resultedGroup != null && !isUserInRole(userNameDN, resultedGroup)) {
                                modifyUserInRole(userNameDN, groupDN, DirContext.ADD_ATTRIBUTE,
                                        searchBase);
                            } else {
                                errorMessage =
                                        "User: " + userName + " already belongs to role: " +
                                                groupDN;
                                throw new UserStoreException(errorMessage);
                            }

                            JNDIUtil.closeNamingEnumeration(groupResults);

                        } else {
                            errorMessage = "The role: " + URLEncoder.encode(newRole, String.valueOf(StandardCharsets.UTF_8)) + " does not exist.";
                            throw new UserStoreException(errorMessage);
                        }
                    }
                }
            }

        } catch (NamingException e) {
            errorMessage = "Error occurred while modifying the role list of user: " + userName;
            if (log.isDebugEnabled()) {
                log.debug(errorMessage, e);
            }
            throw new UserStoreException(errorMessage, e);
        } catch (UnsupportedEncodingException e) {
            errorMessage = "Error occurred while encoding the role value.";
            if (log.isDebugEnabled()) {
                log.debug(errorMessage, e);
            }
            throw new UserStoreException(errorMessage, e);
        } finally {
            JNDIUtil.closeContext(mainDirContext);
        }
    }

    private String getGroupName(SearchResult resultedGroup) throws NamingException {

        Attribute attribute = resultedGroup.getAttributes()
                .get(realmConfig.getUserStoreProperty(LDAPConstants.GROUP_NAME_ATTRIBUTE));
        if (attribute == null) {
            return resultedGroup.getName();
        } else {
            String groupNameAttributeValue = (String) attribute.get();
            return realmConfig.getUserStoreProperty(LDAPConstants.GROUP_NAME_ATTRIBUTE) +
                    "=" + groupNameAttributeValue;
        }
    }

    /**
     * Update the set of users belong to a LDAP role.
     *
     * @param roleName
     * @param deletedUsers
     * @param newUsers
     */
    @SuppressWarnings("deprecation")
    @Override
    public void doUpdateUserListOfRole(String roleName, String[] deletedUsers, String[] newUsers)
            throws UserStoreException {

        String errorMessage = null;
        NamingEnumeration<SearchResult> groupSearchResults = null;

        LDAPRoleContext ctx = (LDAPRoleContext) createRoleContext(roleName);
        roleName = ctx.getRoleName();

        String searchFilter = ctx.getSearchFilter();

        if (isExistingLDAPRole(ctx)) {

            DirContext mainDirContext = this.connectionSource.getContext();

            try {
                searchFilter = searchFilter.replace("?", escapeSpecialCharactersForFilter(roleName));
                String membershipAttributeName =
                        realmConfig.getUserStoreProperty(LDAPConstants.MEMBERSHIP_ATTRIBUTE);
                String roleNameAttribute =
                        realmConfig.getUserStoreProperty(LDAPConstants.GROUP_NAME_ATTRIBUTE);
                String[] returningAttributes = new String[]{membershipAttributeName, roleNameAttribute};

                String searchBase = ctx.getSearchBase();
                groupSearchResults =
                        searchInGroupBase(searchFilter, returningAttributes,
                                SearchControls.SUBTREE_SCOPE,
                                mainDirContext, searchBase);
                SearchResult resultedGroup = null;
                String groupName = null;
                while (groupSearchResults.hasMoreElements()) {
                    resultedGroup = groupSearchResults.next();
                    groupName = getGroupName(resultedGroup);
                }
                // check whether update operations are going to violate non
                // empty role
                // restriction specified in user-mgt.xml by
                // checking whether all users are trying to be deleted
                // before updating LDAP.
                Attribute returnedMemberAttribute =
                        resultedGroup.getAttributes()
                                .get(membershipAttributeName);
                if (!emptyRolesAllowed &&
                        newUsers.length - deletedUsers.length + returnedMemberAttribute.size() == 0) {
                    errorMessage =
                            "There should be at least one member in the role. "
                                    + "Hence can not delete all the members.";
                    throw new UserStoreException(errorMessage);

                } else {
                    List<String> newUserList = new ArrayList<String>();
                    List<String> deleteUserList = new ArrayList<String>();
                    Map<String, String> userDnToUserNameMapping = new HashMap<>();

                    if (newUsers != null && newUsers.length != 0) {
                        String invalidUserList = "";
                        String existingUserList = "";

                        for (String newUser : newUsers) {
                            if (StringUtils.isEmpty(newUser)) {
                                continue;
                            }
                            String userNameDN = getNameInSpaceForUserName(newUser);
                            if (userNameDN == null) {
                                invalidUserList += newUser + " ";
                            } else if (isUserInRole(userNameDN, resultedGroup)) {
                                existingUserList += userNameDN + ",";
                            } else {
                                newUserList.add(userNameDN);
                            }
                        }
                        if (!StringUtils.isEmpty(invalidUserList) || !StringUtils.isEmpty(existingUserList)) {
                            errorMessage = (StringUtils.isEmpty(invalidUserList) ? "" : "'" + invalidUserList
                                    + "' not in the user store. ")
                                    + (StringUtils.isEmpty(existingUserList) ? "" : "'" + existingUserList
                                    + "' already belong to the role : " + roleName);
                            throw new UserStoreException(errorMessage);
                        }
                    }

                    if (deletedUsers != null && deletedUsers.length != 0) {
                        String invalidUserList = "";
                        for (String deletedUser : deletedUsers) {
                            if (StringUtils.isEmpty(deletedUser)) {
                                continue;
                            }
                            String userNameDN = getNameInSpaceForUserName(deletedUser);
                            if (userNameDN == null) {
                                invalidUserList += deletedUser + ",";
                            } else {
                                deleteUserList.add(userNameDN);
                                userDnToUserNameMapping.put(userNameDN, deletedUser);
                            }
                        }
                        if (!StringUtils.isEmpty(invalidUserList)) {
                            errorMessage = "'" + invalidUserList + "' not in the user store.";
                            throw new UserStoreException(errorMessage);
                        }

                    }

                    for (String userNameDN : newUserList) {
                        modifyUserInRole(userNameDN, groupName, DirContext.ADD_ATTRIBUTE, searchBase);
                    }

                    for (String userNameDN : deleteUserList) {
                        modifyUserInRole(userNameDN, groupName, DirContext.REMOVE_ATTRIBUTE, searchBase);
                        // needs to clear authz cache for deleted users
                        String deletedUserName = userDnToUserNameMapping.get(userNameDN);
                        String deletedUserNameWithDomain =
                                UserCoreUtil.addDomainToName(deletedUserName, getMyDomainName());
//                        userRealm.getAuthorizationManager().clearUserAuthorization(deletedUserNameWithDomain);
                    }
                }
            } catch (NamingException e) {
                errorMessage = "Error occurred while modifying the user list of role: " + roleName;
                if (log.isDebugEnabled()) {
                    log.debug(errorMessage, e);
                }
                throw new UserStoreException(errorMessage, e);
            } finally {
                JNDIUtil.closeNamingEnumeration(groupSearchResults);
                JNDIUtil.closeContext(mainDirContext);
            }
        } else {
            errorMessage = "The role: " + roleName + " does not exist.";
            if (log.isDebugEnabled()) {
                log.debug(errorMessage);
            }
            throw new UserStoreException(errorMessage);
        }
    }

    /**
     * Either delete or add user from/to group.
     *
     * @param userNameDN : distinguish name of user entry.
     * @param groupRDN   : relative distinguish name of group entry
     * @param modifyType : modify attribute type in DirCOntext.
     * @throws UserStoreException
     */
    protected void modifyUserInRole(String userNameDN, String groupRDN, int modifyType, String searchBase)
            throws UserStoreException {

        if (log.isDebugEnabled()) {
            logger.debug("Modifying role: " + groupRDN + " with type: " + modifyType + " user: " + userNameDN
                    + " in search base: " + searchBase);
        }

        DirContext mainDirContext = null;
        DirContext groupContext = null;
        try {
            mainDirContext = this.connectionSource.getContext();
            groupContext = (DirContext) mainDirContext.lookup(escapeDNForSearch(searchBase));
            String memberAttributeName = realmConfig.getUserStoreProperty(LDAPConstants.MEMBERSHIP_ATTRIBUTE);
            Attributes modifyingAttributes = new BasicAttributes(true);
            Attribute memberAttribute = new BasicAttribute(memberAttributeName);
            memberAttribute.add(userNameDN);
            modifyingAttributes.put(memberAttribute);

            groupContext.modifyAttributes(groupRDN, modifyType, modifyingAttributes);
            if (log.isDebugEnabled()) {
                logger.debug("User: " + userNameDN + " was successfully " + "modified in LDAP group: "
                        + groupRDN);
            }
        } catch (NamingException e) {
            String errorMessage = "Error occurred while modifying user entry: " + userNameDN
                    + " in LDAP role: " + groupRDN;
            if (log.isDebugEnabled()) {
                log.debug(errorMessage, e);
            }
            throw new UserStoreException(errorMessage);
        } finally {
            JNDIUtil.closeContext(groupContext);
            JNDIUtil.closeContext(mainDirContext);
        }
    }




    /**
     * Check whether user is in the group by searching through its member attributes.
     *
     * @param userDN
     * @param groupEntry
     * @return
     * @throws UserStoreException
     */
    protected boolean isUserInRole(String userDN, SearchResult groupEntry)
            throws UserStoreException {
        boolean isUserInRole = false;
        try {
            Attributes groupAttributes = groupEntry.getAttributes();
            if (groupAttributes != null) {
                // get group's returned attributes
                NamingEnumeration attributes = groupAttributes.getAll();
                // loop through attributes
                while (attributes.hasMoreElements()) {
                    Attribute memberAttribute = (Attribute) attributes.next();
                    String memberAttributeName = realmConfig
                            .getUserStoreProperty(LDAPConstants.MEMBERSHIP_ATTRIBUTE);
                    if (memberAttributeName.equalsIgnoreCase(memberAttribute.getID())) {
                        // loop through attribute values
                        for (int i = 0; i < memberAttribute.size(); i++) {
                            if (userDN.equalsIgnoreCase((String) memberAttribute.get(i))) {
                                return true;
                            }
                        }
                    }

                }

                attributes.close();
            }
        } catch (NamingException e) {
            String errorMessage = "Error occurred while looping through attributes set of group: "
                    + groupEntry.getNameInNamespace();
            if (log.isDebugEnabled()) {
                log.debug(errorMessage, e);
            }
            throw new UserStoreException(errorMessage, e);
        }
        return isUserInRole;
    }

    /**
     * Check whether this is the last/only user in this group.
     *
     * @param userDN
     * @param groupEntry
     * @return groupContext
     */
    @SuppressWarnings("rawtypes")
    protected boolean isOnlyUserInRole(String userDN, SearchResult groupEntry)
            throws UserStoreException {
        boolean isOnlyUserInRole = false;
        try {
            Attributes groupAttributes = groupEntry.getAttributes();
            if (groupAttributes != null) {
                NamingEnumeration attributes = groupAttributes.getAll();
                while (attributes.hasMoreElements()) {
                    Attribute memberAttribute = (Attribute) attributes.next();
                    String memberAttributeName = realmConfig
                            .getUserStoreProperty(LDAPConstants.MEMBERSHIP_ATTRIBUTE);
                    String attributeID = memberAttribute.getID();
                    if (memberAttributeName.equals(attributeID)) {
                        if (memberAttribute.size() == 1 && userDN.equals(memberAttribute.get())) {
                            return true;
                        }
                    }

                }

                attributes.close();

            }
        } catch (NamingException e) {
            String errorMessage = "Error occurred while looping through attributes set of group: "
                    + groupEntry.getNameInNamespace();
            if (log.isDebugEnabled()) {
                log.debug(errorMessage, e);
            }
            throw new UserStoreException(errorMessage, e);
        }
        return isOnlyUserInRole;
    }

    protected void updateLDAPRoleName(RoleContext context, String newRoleName) throws UserStoreException {

        String roleName = context.getRoleName();
        String groupSearchFilter = ((LDAPRoleContext) context).getSearchFilter();
        String roleNameAttributeName = ((LDAPRoleContext) context).getRoleNameProperty();
        String searchBase = ((LDAPRoleContext) context).getSearchBase();

        DirContext mainContext = this.connectionSource.getContext();
        DirContext groupContext = null;
        NamingEnumeration<SearchResult> groupSearchResults = null;

        try {

            groupSearchFilter = groupSearchFilter.replace("?", escapeSpecialCharactersForFilter(roleName));
            String[] returningAttributes = {roleNameAttributeName};
            groupSearchResults = searchInGroupBase(groupSearchFilter, returningAttributes,
                    SearchControls.SUBTREE_SCOPE, mainContext, searchBase);
            SearchResult resultedGroup = null;
            while (groupSearchResults.hasMoreElements()) {
                resultedGroup = groupSearchResults.next();
            }

            if (resultedGroup == null) {
                throw new UserStoreException("Could not find user role " + roleName
                        + " in LDAP server.");
            }

            String groupNameRDN = resultedGroup.getName();
            String newGroupNameRDN = roleNameAttributeName + "=" + newRoleName;

            groupContext = (DirContext) mainContext.lookup(escapeDNForSearch(groupSearchBase));
            groupContext.rename(groupNameRDN, newGroupNameRDN);

            String roleNameWithDomain = UserCoreUtil.addDomainToName(roleName, getMyDomainName());
            String newRoleNameWithDomain = UserCoreUtil.addDomainToName(newRoleName,
                    getMyDomainName());
//            this.userRealm.getAuthorizationManager().resetPermissionOnUpdateRole(
//                    roleNameWithDomain, newRoleNameWithDomain);
        } catch (NamingException e) {
            String errorMessage = "Error occurred while modifying the name of role: " + roleName;
            if (log.isDebugEnabled()) {
                log.debug(errorMessage, e);
            }
            throw new UserStoreException(errorMessage, e);
        } finally {
            JNDIUtil.closeNamingEnumeration(groupSearchResults);
            JNDIUtil.closeContext(groupContext);
            JNDIUtil.closeContext(mainContext);
        }
    }

    @Override
    public void doUpdateRoleName(String roleName, String newRoleName) throws UserStoreException {
        RoleContext roleContext = createRoleContext(roleName);
        updateLDAPRoleName(roleContext, newRoleName);
        if (roleContext.isShared()) {
            roleName = roleName + UserCoreConstants.TENANT_DOMAIN_COMBINER + Constants.SUPER_TENANT_DOMAIN_NAME;
            roleContext = createRoleContext(roleName);
            updateLDAPRoleName(roleContext, newRoleName);
        }
    }

    protected void deleteLDAPRole(RoleContext context) throws UserStoreException {

        String roleName = context.getRoleName();
        String groupSearchFilter = ((LDAPRoleContext) context).getSearchFilter();
        groupSearchFilter = groupSearchFilter.replace("?", escapeSpecialCharactersForFilter(context.getRoleName()));
        String[] returningAttributes = {((LDAPRoleContext) context).getRoleNameProperty()};
        String searchBase = ((LDAPRoleContext) context).getSearchBase();

        DirContext mainDirContext = null;
        DirContext groupContext = null;
        NamingEnumeration<SearchResult> groupSearchResults = null;

        try {

            mainDirContext = this.connectionSource.getContext();
            groupSearchResults = searchInGroupBase(groupSearchFilter, returningAttributes,
                    SearchControls.SUBTREE_SCOPE, mainDirContext, searchBase);
            SearchResult resultedGroup = null;
            while (groupSearchResults.hasMoreElements()) {
                resultedGroup = groupSearchResults.next();
            }

            if (resultedGroup == null) {
                throw new UserStoreException("Could not find specified group/role - " + roleName);
            }

            groupContext = (DirContext) mainDirContext.lookup(escapeDNForSearch(groupSearchBase));
            String groupNameAttributeValue = (String) resultedGroup.getAttributes()
                    .get(realmConfig.getUserStoreProperty(LDAPConstants.GROUP_NAME_ATTRIBUTE))
                    .get();
            String groupName = realmConfig.getUserStoreProperty(LDAPConstants.GROUP_NAME_ATTRIBUTE) + "=" + groupNameAttributeValue;
            if (groupNameAttributeValue.equals(roleName)) {
                groupContext.destroySubcontext(groupName);
            }
        } catch (NamingException e) {
            String errorMessage = "Error occurred while deleting the role: " + roleName;
            if (log.isDebugEnabled()) {
                log.debug(errorMessage, e);
            }
            throw new UserStoreException(errorMessage, e);
        } finally {
            JNDIUtil.closeNamingEnumeration(groupSearchResults);
            JNDIUtil.closeContext(groupContext);
            JNDIUtil.closeContext(mainDirContext);
        }

    }

    /**
     * Delete LDAP group corresponding to the role.
     *
     * @param roleName The role to delete.
     * @throws UserStoreException In case if an error occurred while deleting a role.
     */
    @Override
    public void doDeleteRole(String roleName) throws UserStoreException {

        RoleContext roleContext = createRoleContext(roleName);
        deleteLDAPRole(roleContext);
        if (roleContext.isShared()) {
            roleName = roleName + UserCoreConstants.TENANT_DOMAIN_COMBINER + Constants.SUPER_TENANT_DOMAIN_NAME;
            roleContext = createRoleContext(roleName);
            deleteLDAPRole(roleContext);
        }
    }


    /**
     * Reused methods to search users with various filters
     *
     * @param searchFilter
     * @param returningAttributes
     * @param searchScope
     * @return
     */
    private NamingEnumeration<SearchResult> searchInUserBase(String searchFilter, String[] returningAttributes,
                                                             int searchScope, DirContext rootContext)
            throws UserStoreException {

        if (log.isDebugEnabled()) {
            log.debug("Searching user with " + searchFilter);
        }
        String userBase = realmConfig.getUserStoreProperty(LDAPConstants.USER_SEARCH_BASE);
        SearchControls userSearchControl = new SearchControls();
        userSearchControl.setReturningAttributes(returningAttributes);
        userSearchControl.setSearchScope(searchScope);
        NamingEnumeration<SearchResult> userSearchResults = null;

        try {
            userSearchResults = rootContext.search(escapeDNForSearch(userBase), searchFilter, userSearchControl);
        } catch (NamingException e) {
            String errorMessage = "Error occurred while searching in user base.";
            if (log.isDebugEnabled()) {
                log.debug(errorMessage, e);
            }
            throw new UserStoreException(errorMessage, e);
        }

        return userSearchResults;

    }

    /**
     * Reused method to search groups with various filters.
     *
     * @param searchFilter
     * @param returningAttributes
     * @param searchScope
     * @return
     */
    protected NamingEnumeration<SearchResult> searchInGroupBase(String searchFilter,
                                                                String[] returningAttributes,
                                                                int searchScope,
                                                                DirContext rootContext,
                                                                String searchBase)
            throws UserStoreException {
        SearchControls userSearchControl = new SearchControls();
        userSearchControl.setReturningAttributes(returningAttributes);
        userSearchControl.setSearchScope(searchScope);
        NamingEnumeration<SearchResult> groupSearchResults = null;
        try {
            groupSearchResults = rootContext.search(escapeDNForSearch(searchBase), searchFilter, userSearchControl);
        } catch (NamingException e) {
            String errorMessage = "Error occurred while searching in group base.";
            if (log.isDebugEnabled()) {
                log.debug(errorMessage, e);
            }
            throw new UserStoreException(errorMessage, e);
        }

        return groupSearchResults;
    }

    /**
     * This is to read and validate the required user store configuration for this user store
     * manager to take decisions.
     *
     * @throws UserStoreException
     */
    @Override
    protected void checkRequiredUserStoreConfigurations() throws UserStoreException {

        super.checkRequiredUserStoreConfigurations();

        String userObjectClass = realmConfig
                .getUserStoreProperty(LDAPConstants.USER_ENTRY_OBJECT_CLASS);
        if (userObjectClass == null || userObjectClass.equals("")) {
            throw new UserStoreException(
                    "Required UserEntryObjectClass property is not set at the LDAP configurations");
        }

        if (realmConfig.getUserStoreProperty(UserCoreConstants.RealmConfig.WRITE_GROUPS_ENABLED) != null) {
            writeGroupsEnabled =
                    Boolean.parseBoolean(realmConfig.getUserStoreProperty(UserCoreConstants.RealmConfig.WRITE_GROUPS_ENABLED));
        }

        if (log.isDebugEnabled()) {
            if (writeGroupsEnabled) {
                log.debug("WriteGroups is enabled for " + getMyDomainName());
            } else {
                log.debug("WriteGroups is disabled for " + getMyDomainName());
            }
        }

        if (!writeGroupsEnabled) {
            if (realmConfig.getUserStoreProperty(UserCoreConstants.RealmConfig.READ_GROUPS_ENABLED) != null) {
                readGroupsEnabled =
                        Boolean.parseBoolean(realmConfig.getUserStoreProperty(UserCoreConstants.RealmConfig.READ_GROUPS_ENABLED));
                log.debug("Read LDAP groups enabled: " + readGroupsEnabled);
            }
        } else {
            // Write overwrites Read
            readGroupsEnabled = true;
            log.debug("Read LDAP groups enabled: true");
        }

        emptyRolesAllowed =
                Boolean.parseBoolean(realmConfig.getUserStoreProperty(LDAPConstants.EMPTY_ROLES_ALLOWED));

        String groupEntryObjectClass = realmConfig
                .getUserStoreProperty(LDAPConstants.GROUP_ENTRY_OBJECT_CLASS);
        if (groupEntryObjectClass == null || groupEntryObjectClass.equals("")) {
            throw new UserStoreException(
                    "Required GroupEntryObjectClass property is not set at the LDAP configurations");
        }

        userSearchBase = realmConfig.getUserStoreProperty(LDAPConstants.USER_SEARCH_BASE);
        groupSearchBase = realmConfig.getUserStoreProperty(LDAPConstants.GROUP_SEARCH_BASE);

    }


    private static void setAdvancedProperties() {
        //Set Advanced Properties

        RW_LDAP_UM_ADVANCED_PROPERTIES.clear();
        setAdvancedProperty(UserStoreConfigConstants.SCIMEnabled, "Enable SCIM", "false", UserStoreConfigConstants
                .SCIMEnabledDescription);

        setAdvancedProperty(BULK_IMPORT_SUPPORT, "Bulk Import Support", "true", "Bulk Import Supported");
        setAdvancedProperty(UserStoreConfigConstants.emptyRolesAllowed, "Allow Empty Roles", "true", UserStoreConfigConstants
                .emptyRolesAllowedDescription);


        setAdvancedProperty(UserStoreConfigConstants.passwordHashMethod, "Password Hashing Algorithm", "PLAIN_TEXT",
                UserStoreConfigConstants.passwordHashMethodDescription);
        setAdvancedProperty(MULTI_ATTRIBUTE_SEPARATOR, "Multiple Attribute Separator", ",", MULTI_ATTRIBUTE_SEPARATOR_DESCRIPTION);


        setAdvancedProperty(UserStoreConfigConstants.maxUserNameListLength, "Maximum User List Length", "100", UserStoreConfigConstants
                .maxUserNameListLengthDescription);
        setAdvancedProperty(UserStoreConfigConstants.maxRoleNameListLength, "Maximum Role List Length", "100", UserStoreConfigConstants
                .maxRoleNameListLengthDescription);
        setAdvancedProperty("kdcEnabled", "Enable KDC", "false", "Whether key distribution center enabled");
        setAdvancedProperty("defaultRealmName", "Default Realm Name", "WSO2.ORG", "Default name for the realm");

        setAdvancedProperty(UserStoreConfigConstants.userRolesCacheEnabled, "Enable User Role Cache", "true", UserStoreConfigConstants
                .userRolesCacheEnabledDescription);

        setAdvancedProperty(UserStoreConfigConstants.connectionPoolingEnabled, "Enable LDAP Connection Pooling", "false",
                UserStoreConfigConstants.connectionPoolingEnabledDescription);

        setAdvancedProperty(LDAPConnectionTimeout, "LDAP Connection Timeout", "5000", LDAPConnectionTimeoutDescription);
        setAdvancedProperty(readTimeout, "LDAP Read Timeout", "5000", readTimeoutDescription);
        setAdvancedProperty(RETRY_ATTEMPTS, "Retry Attempts", "0", "Number of retries for" +
                " authentication in case ldap read timed out.");
        setAdvancedProperty("CountRetrieverClass", "Count Implementation", "",
                "Name of the class that implements the count functionality");
        setAdvancedProperty(LDAPConstants.LDAP_ATTRIBUTES_BINARY, "LDAP binary attributes", " ",
                LDAPBinaryAttributesDescription);
        setAdvancedProperty(UserStoreConfigConstants.claimOperationsSupported, UserStoreConfigConstants
                .getClaimOperationsSupportedDisplayName, "true", UserStoreConfigConstants.claimOperationsSupportedDescription);
        setAdvancedProperty(MEMBERSHIP_ATTRIBUTE_RANGE, MEMBERSHIP_ATTRIBUTE_RANGE_DISPLAY_NAME,
                String.valueOf(MEMBERSHIP_ATTRIBUTE_RANGE_VALUE), "Number of maximum users of role returned by the LDAP");
        setAdvancedProperty(LDAPConstants.USER_CACHE_EXPIRY_MILLISECONDS, USER_CACHE_EXPIRY_TIME_ATTRIBUTE_NAME, "",
                USER_CACHE_EXPIRY_TIME_ATTRIBUTE_DESCRIPTION);
        setAdvancedProperty(LDAPConstants.USER_DN_CACHE_ENABLED, USER_DN_CACHE_ENABLED_ATTRIBUTE_NAME, "true",
                USER_DN_CACHE_ENABLED_ATTRIBUTE_DESCRIPTION);
        setAdvancedProperty(UserStoreConfigConstants.STARTTLS_ENABLED,
                UserStoreConfigConstants.STARTTLS_ENABLED_DISPLAY_NAME, "false",
                UserStoreConfigConstants.STARTTLS_ENABLED_DESCRIPTION);
        setAdvancedProperty(UserStoreConfigConstants.CONNECTION_RETRY_DELAY,
                UserStoreConfigConstants.CONNECTION_RETRY_DELAY_DISPLAY_NAME, "120000",
                UserStoreConfigConstants.CONNECTION_RETRY_DELAY_DESCRIPTION);
    }

//
//	/**
//	 * Check and add the initial data to the user store for user manager to start properly.
//	 */
//	private void addInitialAdminData() throws UserStoreException {
//
//		if (realmConfig.getAdminUserName() == null || realmConfig.getAdminRoleName() == null) {
//			throw new UserStoreException(
//					"Admin user name or role name is not valid. Please provide valid values.");
//		}
//
//		String adminUserName = UserCoreUtil.removeDomainFromName(realmConfig.getAdminUserName());
//		String adminRoleName = UserCoreUtil.removeDomainFromName(realmConfig.getAdminRoleName());
//
//		// add admin user if not already added - if it is the first start up
//		// only. this will not affect MT environment. since TenantManager
//		// creates the tenant admin, before initializing UserStoreManager
//		// for the tenant.
//		if (!doCheckExistingUser(adminUserName)) {
//			if (log.isDebugEnabled()) {
//				log.debug("Admin user does not exist. Hence creating the user.");
//			}
//			this.doAddUser(adminUserName, realmConfig.getAdminPassword(), null, null, null);
//		}
//
//		// add admin role, if not already added.
//		if (!isExistingRole(realmConfig.getAdminRoleName())) {
//			if (log.isDebugEnabled()) {
//				log.debug("Admin role does not exist. Hence creating the role.");
//			}
//
//			try {
//				this.addRole(realmConfig.getAdminRoleName(),
//						new String[] { realmConfig.getAdminUserName() }, null, false);
//			} catch (org.wso2.micro.integrator.security.user.api.UserStoreException e) {
//				throw new UserStoreException(e);
//			}
//		}
//		/* since this is at startup, admin user name is sent without domain */
//		if (!super.isUserInRole(adminUserName, adminRoleName)) {
//			// this is when the admin role is changed in the user-mgt.xml
//			if (log.isDebugEnabled()) {
//				log.debug("Admin user is not in the Admin role. Adding the Admin user"
//						+ " to the Admin role");
//			}
//			String[] roles = { realmConfig.getAdminRoleName() };
//			this.updateRoleListOfUser(realmConfig.getAdminUserName(), null, roles);
//		}
//	}

    @Override
    public Properties getDefaultUserStoreProperties() {
        Properties properties = new Properties();
        properties.setMandatoryProperties(ReadWriteLDAPUserStoreConstants.RWLDAP_USERSTORE_PROPERTIES.toArray
                (new Property[ReadWriteLDAPUserStoreConstants.RWLDAP_USERSTORE_PROPERTIES.size()]));
        properties.setOptionalProperties(ReadWriteLDAPUserStoreConstants.OPTINAL_RWLDAP_USERSTORE_PROPERTIES.toArray
                (new Property[ReadWriteLDAPUserStoreConstants.OPTINAL_RWLDAP_USERSTORE_PROPERTIES.size()]));
        properties.setAdvancedProperties(RW_LDAP_UM_ADVANCED_PROPERTIES.toArray
                (new Property[RW_LDAP_UM_ADVANCED_PROPERTIES.size()]));
        return properties;
    }

    private void handleException(Exception e, String userName) throws UserStoreException{
        if (e instanceof InvalidAttributeValueException) {
            String errorMessage = "One or more attribute values provided are incompatible for user : " + userName
                                  + "Please check and try again.";
            if (logger.isDebugEnabled()) {
                logger.debug(errorMessage, e);
            }
            throw new UserStoreException(errorMessage, e);
        } else if (e instanceof InvalidAttributeIdentifierException) {
            String errorMessage = "One or more attributes you are trying to add/update are not "
                                  + "supported by underlying LDAP for user : " + userName;
            if (logger.isDebugEnabled()) {
                logger.debug(errorMessage, e);
            }
            throw new UserStoreException(errorMessage, e);
        } else if (e instanceof NoSuchAttributeException) {
            String errorMessage = "One or more attributes you are trying to add/update are not "
                                  + "supported by underlying LDAP for user : " + userName;
            if (logger.isDebugEnabled()) {
                logger.debug(errorMessage, e);
            }
            throw new UserStoreException(errorMessage, e);
        } else if (e instanceof NamingException) {
            String errorMessage = "Profile information could not be updated in LDAP user store for user : " + userName;
            if (logger.isDebugEnabled()) {
                logger.debug(errorMessage, e);
            }
            throw new UserStoreException(errorMessage, e);
        } else if (e instanceof org.wso2.micro.integrator.security.user.api.UserStoreException) {
            String errorMessage = "Error in obtaining claim mapping for user : " + userName;
            if (logger.isDebugEnabled()) {
                logger.debug(errorMessage, e);
            }
            throw new UserStoreException(errorMessage, e);
        }
    }

    /**
     * Escaping ldap DN special characters in a String value
     *
     * @param text String to replace special characters
     * @return
     */
    private String escapeSpecialCharactersForDN(String text) {
        boolean replaceEscapeCharacters = true;

        String replaceEscapeCharactersAtUserLoginString = realmConfig
                .getUserStoreProperty(UserCoreConstants.RealmConfig.PROPERTY_REPLACE_ESCAPE_CHARACTERS_AT_USER_LOGIN);

        if (replaceEscapeCharactersAtUserLoginString != null) {
            replaceEscapeCharacters = Boolean.parseBoolean(replaceEscapeCharactersAtUserLoginString);
            if (log.isDebugEnabled()) {
                log.debug("Replace escape characters configured to: " + replaceEscapeCharactersAtUserLoginString);
            }
        }

        if (replaceEscapeCharacters) {
            StringBuilder sb = new StringBuilder();
            if ((text.length() > 0) && ((text.charAt(0) == ' ') || (text.charAt(0) == '#'))) {
                sb.append('\\'); // add the leading backslash if needed
            }
            for (int i = 0; i < text.length(); i++) {
                char currentChar = text.charAt(i);
                switch (currentChar) {
                    case '\\':
                        if (text.charAt(i + 1) == '*') {
                            sb.append("*");
                            i++;
                            break;
                        }
                        sb.append("\\\\");
                        break;
                    case ',':
                        sb.append("\\,");
                        break;
                    case '+':
                        sb.append("\\+");
                        break;
                    case '"':
                        sb.append("\\\"");
                        break;
                    case '<':
                        sb.append("\\<");
                        break;
                    case '>':
                        sb.append("\\>");
                        break;
                    case ';':
                        sb.append("\\;");
                        break;
                    default:
                        sb.append(currentChar);
                }
            }
            if ((text.length() > 1) && (text.charAt(text.length() - 1) == ' ')) {
                sb.insert(sb.length() - 1, '\\'); // add the trailing backslash if needed
            }
            if (log.isDebugEnabled()) {
                log.debug("value after escaping special characters in " + text + " : " + sb.toString());
            }
            return sb.toString();
        } else {
            return text;
        }

    }


    /**
     * Escaping ldap search filter special characters in a string
     *
     * @param dnPartial String to replace special characters
     * @return
     */
    private String escapeSpecialCharactersForFilter(String dnPartial) {
        boolean replaceEscapeCharacters = true;

        dnPartial.replace("\\*", "*");

        String replaceEscapeCharactersAtUserLoginString = realmConfig
                .getUserStoreProperty(UserCoreConstants.RealmConfig.PROPERTY_REPLACE_ESCAPE_CHARACTERS_AT_USER_LOGIN);

        if (replaceEscapeCharactersAtUserLoginString != null) {
            replaceEscapeCharacters = Boolean
                    .parseBoolean(replaceEscapeCharactersAtUserLoginString);
            if (log.isDebugEnabled()) {
                log.debug("Replace escape characters configured to: "
                        + replaceEscapeCharactersAtUserLoginString);
            }
        }
        //TODO: implement character escaping for *

        if (replaceEscapeCharacters) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < dnPartial.length(); i++) {
                char currentChar = dnPartial.charAt(i);
                switch (currentChar) {
                    case '\\':
                        sb.append("\\5c");
                        break;
                    case '*':
                        sb.append("\\2a");
                        break;
                    case '(':
                        sb.append("\\28");
                        break;
                    case ')':
                        sb.append("\\29");
                        break;
                    case '\u0000':
                        sb.append("\\00");
                        break;
                    default:
                        sb.append(currentChar);
                }
            }
            return sb.toString();
        } else {
            return dnPartial;
        }
    }

    /**
     * Escaping ldap DN special characters in a String value
     *
     * @param text String to replace special characters
     * @return
     */
    private String escapeSpecialCharactersForDNWithStar(String text) {
        boolean replaceEscapeCharacters = true;
        text.replace("\\*", "*");

        String replaceEscapeCharactersAtUserLoginString = realmConfig
                .getUserStoreProperty(UserCoreConstants.RealmConfig.PROPERTY_REPLACE_ESCAPE_CHARACTERS_AT_USER_LOGIN);

        if (replaceEscapeCharactersAtUserLoginString != null) {
            replaceEscapeCharacters = Boolean
                    .parseBoolean(replaceEscapeCharactersAtUserLoginString);
            if (log.isDebugEnabled()) {
                log.debug("Replace escape characters configured to: "
                        + replaceEscapeCharactersAtUserLoginString);
            }
        }

        if (replaceEscapeCharacters) {
            StringBuilder sb = new StringBuilder();
            if ((text.length() > 0) && ((text.charAt(0) == ' ') || (text.charAt(0) == '#'))) {
                sb.append('\\'); // add the leading backslash if needed
            }
            for (int i = 0; i < text.length(); i++) {
                char currentChar = text.charAt(i);
                switch (currentChar) {
                    case '\\':
                        sb.append("\\\\");
                        break;
                    case ',':
                        sb.append("\\,");
                        break;
                    case '+':
                        sb.append("\\+");
                        break;
                    case '"':
                        sb.append("\\\"");
                        break;
                    case '<':
                        sb.append("\\<");
                        break;
                    case '>':
                        sb.append("\\>");
                        break;
                    case ';':
                        sb.append("\\;");
                        break;
                    case '*':
                        sb.append("\\2a");
                        break;
                    default:
                        sb.append(currentChar);
                }
            }
            if ((text.length() > 1) && (text.charAt(text.length() - 1) == ' ')) {
                sb.insert(sb.length() - 1, '\\'); // add the trailing backslash if needed
            }
            if (log.isDebugEnabled()) {
                log.debug("value after escaping special characters in " + text + " : " + sb.toString());
            }
            return sb.toString();
        } else {
            return text;
        }

    }

    private static void setAdvancedProperty(String name, String displayName, String value,
                                            String description) {
        Property property = new Property(name, value, displayName + "#" + description, null);
        RW_LDAP_UM_ADVANCED_PROPERTIES.add(property);

    }
}
