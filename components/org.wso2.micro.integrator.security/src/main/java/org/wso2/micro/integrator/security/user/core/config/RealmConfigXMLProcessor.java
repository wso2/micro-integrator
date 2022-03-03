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
package org.wso2.micro.integrator.security.user.core.config;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMAttribute;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleContext;
import org.wso2.micro.core.util.CarbonException;
import org.wso2.micro.integrator.core.util.MicroIntegratorBaseUtils;
import org.wso2.micro.integrator.security.user.core.UserCoreConstants;
import org.wso2.micro.integrator.security.user.core.UserStoreException;
import org.wso2.micro.integrator.security.user.core.claim.builder.ClaimBuilder;
import org.wso2.micro.integrator.security.user.core.jdbc.JDBCRealmConstants;
import org.wso2.micro.integrator.security.user.core.util.UserCoreUtil;
import org.wso2.securevault.SecretResolver;
import org.wso2.securevault.SecretResolverFactory;
import org.wso2.securevault.commons.MiscellaneousUtil;
import org.wso2.micro.integrator.security.user.api.RealmConfiguration;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.regex.Pattern;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;

import static org.wso2.micro.core.util.CarbonUtils.resolveSystemProperty;

public class RealmConfigXMLProcessor {

    public static final String REALM_CONFIG_FILE = "user-mgt.xml";
    private static final Log log = LogFactory.getLog(RealmConfigXMLProcessor.class);
    private static BundleContext bundleContext;
    InputStream inStream = null;
    private SecretResolver secretResolver;

    public static void setBundleContext(BundleContext bundleContext) {
        RealmConfigXMLProcessor.bundleContext = bundleContext;
    }

    // TODO get a factory or a stream writer - add more props
    public static OMElement serialize(RealmConfiguration realmConfig) {
        OMFactory factory = OMAbstractFactory.getOMFactory();
        OMElement rootElement = factory.createOMElement(new QName(
                UserCoreConstants.RealmConfig.LOCAL_NAME_USER_MANAGER));
        OMElement realmElement = factory.createOMElement(new QName(
                UserCoreConstants.RealmConfig.LOCAL_NAME_REALM));
        String realmName = realmConfig.getRealmClassName();

        OMAttribute propAttr = factory.createOMAttribute(
                UserCoreConstants.RealmConfig.ATTR_NAME_PROP_NAME, null, realmName);
        realmElement.addAttribute(propAttr);

        rootElement.addChild(realmElement);

        OMElement mainConfig = factory.createOMElement(new QName(
                UserCoreConstants.RealmConfig.LOCAL_NAME_CONFIGURATION));
        realmElement.addChild(mainConfig);

        OMElement addAdmin = factory.createOMElement(new QName(
                UserCoreConstants.RealmConfig.LOCAL_NAME_ADD_ADMIN));
        OMElement adminUser = factory.createOMElement(new QName(
                UserCoreConstants.RealmConfig.LOCAL_NAME_ADMIN_USER));
        OMElement adminUserNameElem = factory.createOMElement(new QName(
                UserCoreConstants.RealmConfig.LOCAL_NAME_USER_NAME));
        adminUserNameElem.setText(realmConfig.getAdminUserName());
        OMElement adminPasswordElem = factory.createOMElement(new QName(
                UserCoreConstants.RealmConfig.LOCAL_NAME_PASSWORD));
        addAdmin.setText(UserCoreUtil.removeDomainFromName(realmConfig.getAddAdmin()));
        adminPasswordElem.setText(realmConfig.getAdminPassword());
        adminUser.addChild(adminUserNameElem);
        adminUser.addChild(adminPasswordElem);
        mainConfig.addChild(addAdmin);
        mainConfig.addChild(adminUser);

        OMElement adminRoleNameElem = factory.createOMElement(new QName(
                UserCoreConstants.RealmConfig.LOCAL_NAME_ADMIN_ROLE));
        adminRoleNameElem.setText(UserCoreUtil.removeDomainFromName(realmConfig.getAdminRoleName()));
        mainConfig.addChild(adminRoleNameElem);

        OMElement systemUserNameElem = factory.createOMElement(new QName(
                UserCoreConstants.RealmConfig.LOCAL_NAME_SYSTEM_USER_NAME));
        mainConfig.addChild(systemUserNameElem);

        // adding the anonymous user
        OMElement anonymousUserEle = factory.createOMElement(new QName(
                UserCoreConstants.RealmConfig.LOCAL_NAME_ANONYMOUS_USER));
        OMElement anonymousUserNameElem = factory.createOMElement(new QName(
                UserCoreConstants.RealmConfig.LOCAL_NAME_USER_NAME));
        OMElement anonymousPasswordElem = factory.createOMElement(new QName(
                UserCoreConstants.RealmConfig.LOCAL_NAME_PASSWORD));
        anonymousUserEle.addChild(anonymousUserNameElem);
        anonymousUserEle.addChild(anonymousPasswordElem);
        mainConfig.addChild(anonymousUserEle);

        // adding the everyone role
        OMElement everyoneRoleNameElem = factory.createOMElement(new QName(
                UserCoreConstants.RealmConfig.LOCAL_NAME_EVERYONE_ROLE));
        everyoneRoleNameElem.setText(UserCoreUtil.removeDomainFromName(realmConfig.getEveryOneRoleName()));
        mainConfig.addChild(everyoneRoleNameElem);

        // adding the OverrideUsernameClaimFromInternalUsername
        OMElement isOverrideUsernameClaimFromInternalUsernameElem = factory.createOMElement(new QName(
                UserCoreConstants.RealmConfig.OVERRIDE_USERNAME_CLAIM_FROM_INTERNAL_USERNAME));
        isOverrideUsernameClaimFromInternalUsernameElem.setText(
                realmConfig.getIsOverrideUsernameClaimFromInternalUsername());
        mainConfig.addChild(isOverrideUsernameClaimFromInternalUsernameElem);

        // add the main config properties
        addPropertyElements(factory, mainConfig, null, realmConfig.getDescription(),
                realmConfig.getRealmProperties());
        // add the user store manager properties

        OMElement userStoreManagerElement = factory.createOMElement(new QName(
                UserCoreConstants.RealmConfig.LOCAL_NAME_USER_STORE_MANAGER));
        realmElement.addChild(userStoreManagerElement);
        addPropertyElements(factory, userStoreManagerElement, realmConfig.getUserStoreClass(),
                realmConfig.getDescription(),
                realmConfig.getUserStoreProperties());

        RealmConfiguration secondaryRealmConfiguration = null;
        secondaryRealmConfiguration = realmConfig.getSecondaryRealmConfig();
        while (secondaryRealmConfiguration != null) {
            OMElement secondaryElement = factory.createOMElement(new QName(
                    UserCoreConstants.RealmConfig.LOCAL_NAME_USER_STORE_MANAGER));
            realmElement.addChild(secondaryElement);
            addPropertyElements(factory, secondaryElement,
                    secondaryRealmConfiguration.getUserStoreClass(),
                    secondaryRealmConfiguration.getDescription(),
                    secondaryRealmConfiguration.getUserStoreProperties());
            secondaryRealmConfiguration = secondaryRealmConfiguration.getSecondaryRealmConfig();
        }

        // add the user authorization properties
        OMElement authorizerManagerElement = factory.createOMElement(new QName(
                UserCoreConstants.RealmConfig.LOCAL_NAME_ATHZ_MANAGER));
        realmElement.addChild(authorizerManagerElement);
        addPropertyElements(factory, authorizerManagerElement,
                realmConfig.getAuthorizationManagerClass(),
                realmConfig.getDescription(),
                realmConfig.getAuthzProperties());

        return rootElement;
    }

    private static void addPropertyElements(OMFactory factory, OMElement parent, String className,
                                            String description, Map<String, String> properties) {
        if (className != null) {
            parent.addAttribute(UserCoreConstants.RealmConfig.ATTR_NAME_CLASS, className, null);
        }
        if (description != null) {
            parent.addAttribute(UserCoreConstants.RealmConfig.CLASS_DESCRIPTION, description, null);
        }
        Iterator<Map.Entry<String, String>> ite = properties.entrySet().iterator();
        while (ite.hasNext()) {
            Map.Entry<String, String> entry = ite.next();
            String name = entry.getKey();
            String value = entry.getValue();
            if (value != null) {
                value = resolveSystemProperty(value);
            }
            OMElement propElem = factory.createOMElement(new QName(
                    UserCoreConstants.RealmConfig.LOCAL_NAME_PROPERTY));
            OMAttribute propAttr = factory.createOMAttribute(
                    UserCoreConstants.RealmConfig.ATTR_NAME_PROP_NAME, null, name);
            propElem.addAttribute(propAttr);
            propElem.setText(value);
            parent.addChild(propElem);
        }
    }

    public RealmConfiguration buildRealmConfigurationFromFile() throws UserStoreException {
        OMElement realmElement;
        try {
            realmElement = getRealmElement();

            RealmConfiguration realmConfig = buildRealmConfiguration(realmElement);

            if (inStream != null) {
                inStream.close();
            }
            return realmConfig;
        } catch (Exception e) {
            String message = "Error while reading realm configuration from file";
            if (log.isDebugEnabled()) {
                log.debug(message, e);
            }
            throw new UserStoreException(message, e);
        }

    }

    public RealmConfiguration buildTenantRealmConfiguration(InputStream inStream)
            throws UserStoreException {
        OMElement realmElement;
        try {
            realmElement = preProcessRealmConfig(inStream);

            RealmConfiguration realmConfig = buildTenantRealmConfiguration(realmElement);

            if (inStream != null) {
                inStream.close();
            }
            return realmConfig;
        } catch (RuntimeException e) {
            String message = "An unexpected error occurred while building the realm configuration.";
            if (log.isDebugEnabled()) {
                log.debug(message, e);
            }
            throw new UserStoreException(message, e);
        } catch (Exception e) {
            String message = "Error while reading realm configuration from file";
            if (log.isDebugEnabled()) {
                log.debug(message, e);
            }
            throw new UserStoreException(message, e);
        }

    }

    private RealmConfiguration buildTenantRealmConfiguration(OMElement realmElement) throws UserStoreException {
        return buildRealmConfiguration(realmElement, false);
    }

    private OMElement preProcessRealmConfig(InputStream inStream) throws CarbonException,
            XMLStreamException {
        inStream = MicroIntegratorBaseUtils.replaceSystemVariablesInXml(inStream);
        StAXOMBuilder builder = new StAXOMBuilder(inStream);
        OMElement documentElement = builder.getDocumentElement();

        OMElement realmElement =
                documentElement.getFirstChildWithName(new QName(
                        UserCoreConstants.RealmConfig.LOCAL_NAME_REALM));
        return realmElement;
    }

    public RealmConfiguration buildRealmConfiguration(InputStream inStream)
            throws UserStoreException {
        OMElement realmElement;
        try {
            realmElement = preProcessRealmConfig(inStream);

            RealmConfiguration realmConfig = buildRealmConfiguration(realmElement);

            if (inStream != null) {
                inStream.close();
            }
            return realmConfig;
        } catch (RuntimeException e) {
            String message = "An unexpected error occurred while building the realm configuration.";
            if (log.isDebugEnabled()) {
                log.debug(message, e);
            }
            throw new UserStoreException(message, e);
        } catch (Exception e) {
            String message = "Error while reading realm configuration from file";
            if (log.isDebugEnabled()) {
                log.debug(message, e);
            }
            throw new UserStoreException(message, e);
        }

    }

    public RealmConfiguration buildRealmConfiguration(OMElement realmElem)
            throws UserStoreException {
        return buildRealmConfiguration(realmElem, true);
    }

    public RealmConfiguration buildRealmConfiguration(OMElement realmElem, boolean supperTenant)
            throws UserStoreException {
        RealmConfiguration realmConfig = null;
        String userStoreClass = null;
        String authorizationManagerClass = null;
        String addAdmin = null;
        String adminRoleName = null;
        String adminUserName = null;
        String adminPassword = null;
        String everyOneRoleName = null;
        String realmClass = null;
        String description = null;
        String isOverrideUsernameClaimFromInternalUsername = null;
        Map<String, String> userStoreProperties = null;
        Map<String, String> authzProperties = null;
        Map<String, String> realmProperties = null;
        boolean passwordsExternallyManaged = false;

        realmClass = (String) realmElem.getAttributeValue(new QName(
                UserCoreConstants.RealmConfig.ATTR_NAME_CLASS));

        OMElement mainConfig = realmElem.getFirstChildWithName(new QName(
                UserCoreConstants.RealmConfig.LOCAL_NAME_CONFIGURATION));
        realmProperties = getChildPropertyElements(mainConfig, secretResolver);
        String dbUrl = constructDatabaseURL(realmProperties.get(JDBCRealmConstants.URL));
        realmProperties.put(JDBCRealmConstants.URL, dbUrl);

        if (mainConfig.getFirstChildWithName(new QName(
                UserCoreConstants.RealmConfig.LOCAL_NAME_ADD_ADMIN)) != null
                && !mainConfig
                .getFirstChildWithName(
                        new QName(UserCoreConstants.RealmConfig.LOCAL_NAME_ADD_ADMIN))
                .getText().trim().equals("")) {
            addAdmin = mainConfig
                    .getFirstChildWithName(
                            new QName(UserCoreConstants.RealmConfig.LOCAL_NAME_ADD_ADMIN))
                    .getText().trim();
        } else {
            if (supperTenant) {
                log.error("AddAdmin configuration not found or invalid in user-mgt.xml. Cannot start server!");
                throw new UserStoreException(
                        "AddAdmin configuration not found or invalid user-mgt.xml. Cannot start server!");
            } else {
                log.debug("AddAdmin configuration not found");
                addAdmin = "true";
            }
        }

        OMElement reservedRolesElm = mainConfig.getFirstChildWithName(new QName(
                UserCoreConstants.RealmConfig.LOCAL_NAME_RESERVED_ROLE_NAMES));

        String[] reservedRoles = new String[0];

        if (reservedRolesElm != null && !reservedRolesElm.getText().trim().equals("")) {
            String rolesStr = reservedRolesElm.getText().trim();

            if (rolesStr.contains(",")) {
                reservedRoles = rolesStr.split(",");
            } else {
                reservedRoles = rolesStr.split(";");
            }
        }

        OMElement restrictedDomainsElm = mainConfig.getFirstChildWithName(new QName(
                UserCoreConstants.RealmConfig.LOCAL_NAME_RESTRICTED_DOMAINS_FOR_SELF_SIGN_UP));

        String[] restrictedDomains = new String[0];

        if (restrictedDomainsElm != null && !restrictedDomainsElm.getText().trim().equals("")) {
            String domain = restrictedDomainsElm.getText().trim();

            if (domain.contains(",")) {
                restrictedDomains = domain.split(",");
            } else {
                restrictedDomains = domain.split(";");
            }
        }

        OMElement adminUser = mainConfig.getFirstChildWithName(new QName(
                UserCoreConstants.RealmConfig.LOCAL_NAME_ADMIN_USER));
        adminUserName = adminUser
                .getFirstChildWithName(
                        new QName(UserCoreConstants.RealmConfig.LOCAL_NAME_USER_NAME)).getText()
                .trim();
        OMElement adminPasswordElement =
                adminUser.getFirstChildWithName(new QName(UserCoreConstants.RealmConfig.LOCAL_NAME_PASSWORD));
        adminPassword = MiscellaneousUtil.resolve(adminPasswordElement, secretResolver);
        adminRoleName = mainConfig
                .getFirstChildWithName(
                        new QName(UserCoreConstants.RealmConfig.LOCAL_NAME_ADMIN_ROLE)).getText()
                .trim();
        everyOneRoleName = mainConfig
                .getFirstChildWithName(
                        new QName(UserCoreConstants.RealmConfig.LOCAL_NAME_EVERYONE_ROLE))
                .getText().trim();

        OMElement overrideUsernameClaimEle = mainConfig.getFirstChildWithName(
                new QName(UserCoreConstants.RealmConfig.OVERRIDE_USERNAME_CLAIM_FROM_INTERNAL_USERNAME));
        if (overrideUsernameClaimEle != null) {
            isOverrideUsernameClaimFromInternalUsername = overrideUsernameClaimEle.getText().trim();
        } else {
            isOverrideUsernameClaimFromInternalUsername = "false";
        }

        OMElement authzConfig = realmElem.getFirstChildWithName(new QName(
                UserCoreConstants.RealmConfig.LOCAL_NAME_ATHZ_MANAGER));
        authorizationManagerClass = authzConfig.getAttributeValue(
                new QName(UserCoreConstants.RealmConfig.ATTR_NAME_CLASS)).trim();
        authzProperties = getChildPropertyElements(authzConfig, null);

        Iterator<OMElement> iterator = realmElem.getChildrenWithName(new QName(
                UserCoreConstants.RealmConfig.LOCAL_NAME_USER_STORE_MANAGER));

        RealmConfiguration primaryConfig = null;
        RealmConfiguration tmpConfig = null;

        for (; iterator.hasNext(); ) {
            OMElement usaConfig = iterator.next();
            userStoreClass = usaConfig.getAttributeValue(new QName(
                    UserCoreConstants.RealmConfig.ATTR_NAME_CLASS));
            if (usaConfig.getFirstChildWithName(new QName(UserCoreConstants.RealmConfig.CLASS_DESCRIPTION)) != null) {
                description = usaConfig.getFirstChildWithName(new QName(UserCoreConstants.RealmConfig.CLASS_DESCRIPTION)).getText().trim();
            }
            userStoreProperties = getChildPropertyElements(usaConfig, secretResolver);

            String sIsPasswordExternallyManaged = userStoreProperties
                    .get(UserCoreConstants.RealmConfig.LOCAL_PASSWORDS_EXTERNALLY_MANAGED);

            Map<String, String> multipleCredentialsProperties = getMultipleCredentialsProperties(usaConfig);

            if (null != sIsPasswordExternallyManaged
                    && !sIsPasswordExternallyManaged.trim().equals("")) {
                passwordsExternallyManaged = Boolean.parseBoolean(sIsPasswordExternallyManaged);
            } else {
                if (log.isDebugEnabled()) {
                    log.debug("External password management is disabled.");
                }
            }

            realmConfig = new RealmConfiguration();
            realmConfig.setRealmClassName(realmClass);
            realmConfig.setUserStoreClass(userStoreClass);
            realmConfig.setDescription(description);
            realmConfig.setAuthorizationManagerClass(authorizationManagerClass);
            if (primaryConfig == null) {
                realmConfig.setPrimary(true);
                realmConfig.setAddAdmin(addAdmin);
                realmConfig.setAdminPassword(adminPassword);

                //if domain name not provided, add default primary domain name
                String domain = userStoreProperties.get(UserCoreConstants.RealmConfig.PROPERTY_DOMAIN_NAME);
                if (domain == null) {
                    userStoreProperties.put(UserCoreConstants.RealmConfig.PROPERTY_DOMAIN_NAME,
                            UserCoreConstants.PRIMARY_DEFAULT_DOMAIN_NAME);
                }

                for (int i = 0; i < reservedRoles.length; i++) {
                    realmConfig.addReservedRoleName(reservedRoles[i].trim().toUpperCase());
                }

                for (int i = 0; i < restrictedDomains.length; i++) {
                    realmConfig.addRestrictedDomainForSelfSignUp(restrictedDomains[i].trim()
                            .toUpperCase());
                }

                /*
                if (supperTenant && userStoreProperties.get(UserCoreConstants.TenantMgtConfig.LOCAL_NAME_TENANT_MANAGER) == null) {
                    log.error("Required property '" + UserCoreConstants.TenantMgtConfig.LOCAL_NAME_TENANT_MANAGER
                            + "' not found for the primary UserStoreManager in user_mgt.xml. Cannot start server!");
                    throw new UserStoreException("Required property '" + UserCoreConstants.TenantMgtConfig.LOCAL_NAME_TENANT_MANAGER
                            + "' not found for the primary UserStoreManager in user_mgt.xml. Cannot start server!");
                }*/
            }

            // If the domain name still empty
            String domain = userStoreProperties.get(UserCoreConstants.RealmConfig.PROPERTY_DOMAIN_NAME);
            if (domain == null) {
                log.warn("Required property " + UserCoreConstants.RealmConfig.PROPERTY_DOMAIN_NAME + " missing in secondary user store. Skip adding the user store.");
                continue;
            }
            // Making user stores added using user-mgt.xml non-editable(static) at runtime
            userStoreProperties.put(UserCoreConstants.RealmConfig.STATIC_USER_STORE, "true");

            realmConfig.setEveryOneRoleName(UserCoreConstants.INTERNAL_DOMAIN + UserCoreConstants.DOMAIN_SEPARATOR
                    + everyOneRoleName);
            realmConfig.setAdminRoleName(adminRoleName);
            realmConfig.setAdminUserName(adminUserName);
            realmConfig.setIsOverrideUsernameClaimFromInternalUsername(isOverrideUsernameClaimFromInternalUsername);
            realmConfig.setUserStoreProperties(userStoreProperties);
            realmConfig.setAuthzProperties(authzProperties);
            realmConfig.setRealmProperties(realmProperties);
            realmConfig.setPasswordsExternallyManaged(passwordsExternallyManaged);
            realmConfig.addMultipleCredentialProperties(userStoreClass,
                    multipleCredentialsProperties);

            if (realmConfig
                    .getUserStoreProperty(UserCoreConstants.RealmConfig.PROPERTY_MAX_USER_LIST) == null) {
                realmConfig.getUserStoreProperties().put(
                        UserCoreConstants.RealmConfig.PROPERTY_MAX_USER_LIST,
                        UserCoreConstants.RealmConfig.PROPERTY_VALUE_DEFAULT_MAX_COUNT);
            }

            if (realmConfig.getUserStoreProperty(UserCoreConstants.RealmConfig.PROPERTY_READ_ONLY) == null) {
                realmConfig.getUserStoreProperties().put(
                        UserCoreConstants.RealmConfig.PROPERTY_READ_ONLY,
                        UserCoreConstants.RealmConfig.PROPERTY_VALUE_DEFAULT_READ_ONLY);
            }

            if (primaryConfig == null) {
                primaryConfig = realmConfig;
            } else {
                tmpConfig.setSecondaryRealmConfig(realmConfig);
            }

            tmpConfig = realmConfig;
        }
        if (primaryConfig != null && primaryConfig.isPrimary()) {
            // Check if Admin user name has been provided with domain
            String primaryDomainName = primaryConfig
                    .getUserStoreProperty(UserCoreConstants.RealmConfig.PROPERTY_DOMAIN_NAME);
            String readOnly = primaryConfig
                    .getUserStoreProperty(UserCoreConstants.RealmConfig.PROPERTY_READ_ONLY);
            Boolean isReadOnly = false;
            if (readOnly != null) {
                isReadOnly = Boolean.parseBoolean(readOnly);
            }
            if (primaryDomainName != null && primaryDomainName.trim().length() > 0) {
                if (adminUserName.indexOf(UserCoreConstants.DOMAIN_SEPARATOR) > 0) {
                    // Using the short-circuit. User name comes with the domain name.
                    String adminUserDomain = adminUserName.substring(0,
                            adminUserName.indexOf(UserCoreConstants.DOMAIN_SEPARATOR));
                    if (!primaryDomainName.equalsIgnoreCase(adminUserDomain)) {
                        throw new UserStoreException(
                                "Admin User domain does not match primary user store domain.");
                    }
                } else {
                    primaryConfig.setAdminUserName(UserCoreUtil.addDomainToName(adminUserName,
                            primaryDomainName));
                }
                if (adminRoleName.indexOf(UserCoreConstants.DOMAIN_SEPARATOR) > 0) {
                    // Using the short-circuit. User name comes with the domain name.
                    String adminRoleDomain = adminRoleName.substring(0,
                            adminRoleName.indexOf(UserCoreConstants.DOMAIN_SEPARATOR));

                    if ((!primaryDomainName.equalsIgnoreCase(adminRoleDomain))
                            || (isReadOnly)
                            && (!primaryDomainName
                            .equalsIgnoreCase(UserCoreConstants.INTERNAL_DOMAIN))) {
                        throw new UserStoreException(
                                "Admin Role domain does not match primary user store domain.");
                    }
                }
            }

            // This will be overridden inside the UserStoreManager constructor.
            primaryConfig.setAdminRoleName(UserCoreUtil.addDomainToName(adminRoleName,
                    primaryDomainName));

        }
        return primaryConfig;
    }

    private String constructDatabaseURL(String url) {
        String path;
        if (url != null && url.contains(UserCoreConstants.CARBON_HOME_PARAMETER)) {
            File carbonHomeDir;
            carbonHomeDir = new File(MicroIntegratorBaseUtils.getCarbonHome());
            path = carbonHomeDir.getPath();
            path = path.replaceAll(Pattern.quote("\\"), "/");
            if (carbonHomeDir.exists() && carbonHomeDir.isDirectory()) {
                url = url.replaceAll(Pattern.quote(UserCoreConstants.CARBON_HOME_PARAMETER), path);
            } else {
                log.warn("carbon home invalid");
                String[] tempStrings1 = url.split(Pattern
                        .quote(UserCoreConstants.CARBON_HOME_PARAMETER));
                String dbUrl = tempStrings1[1];
                String[] tempStrings2 = dbUrl.split("/");
                for (int i = 0; i < tempStrings2.length - 1; i++) {
                    url = tempStrings1[0] + tempStrings2[i] + "/";
                }
                url = url + tempStrings2[tempStrings2.length - 1];
            }
        }
        return url;
    }

    private Map<String, String> getChildPropertyElements(OMElement omElement,
                                                         SecretResolver secretResolver) {
        Map<String, String> map = new HashMap<String, String>();
        Iterator<?> ite = omElement.getChildrenWithName(new QName(
                UserCoreConstants.RealmConfig.LOCAL_NAME_PROPERTY));
        while (ite.hasNext()) {
            OMElement propElem = (OMElement) ite.next();
            String propName = propElem.getAttributeValue(new QName(
                    UserCoreConstants.RealmConfig.ATTR_NAME_PROP_NAME));
            String propValue = MiscellaneousUtil.resolve(propElem, secretResolver);
            map.put(propName.trim(), propValue.trim());
        }
        return map;
    }

    private Map<String, String> getMultipleCredentialsProperties(OMElement omElement) {
        Map<String, String> map = new HashMap<String, String>();
        OMElement multipleCredentialsEl = omElement.getFirstChildWithName(new QName(
                UserCoreConstants.RealmConfig.LOCAL_NAME_MULTIPLE_CREDENTIALS));
        if (multipleCredentialsEl != null) {
            Iterator<?> ite = multipleCredentialsEl
                    .getChildrenWithLocalName(UserCoreConstants.RealmConfig.LOCAL_NAME_CREDENTIAL);
            while (ite.hasNext()) {

                Object OMObj = ite.next();
                if (!(OMObj instanceof OMElement)) {
                    continue;
                }
                OMElement credsElem = (OMElement) OMObj;
                String credsType = credsElem.getAttributeValue(new QName(
                        UserCoreConstants.RealmConfig.ATTR_NAME_TYPE));
                String credsClassName = credsElem.getText();
                map.put(credsType.trim(), credsClassName.trim());
            }
        }
        return map;
    }

    private OMElement getRealmElement() throws XMLStreamException, IOException, UserStoreException {
        String carbonHome = MicroIntegratorBaseUtils.getCarbonHome();
        StAXOMBuilder builder = null;

        if (carbonHome != null) {
            File profileConfigXml = new File(MicroIntegratorBaseUtils.getCarbonConfigDirPath(),
                    REALM_CONFIG_FILE);
            if (profileConfigXml.exists()) {
                inStream = new FileInputStream(profileConfigXml);
            }
        } else {
            inStream = RealmConfigXMLProcessor.class.getResourceAsStream(REALM_CONFIG_FILE);
        }

        String warningMessage = "";
        if (inStream == null) {
            URL url;
            if (bundleContext != null) {
                if ((url = bundleContext.getBundle().getResource(REALM_CONFIG_FILE)) != null) {
                    inStream = url.openStream();
                } else {
                    warningMessage = "Bundle context could not find resource "
                            + REALM_CONFIG_FILE
                            + " or user does not have sufficient permission to access the resource.";
                }
            } else {
                if ((url = ClaimBuilder.class.getResource(REALM_CONFIG_FILE)) != null) {
                    inStream = url.openStream();
                    log.error("Using the internal realm configuration. Strictly for non-production purposes.");
                } else {
                    warningMessage = "ClaimBuilder could not find resource "
                            + REALM_CONFIG_FILE
                            + " or user does not have sufficient permission to access the resource.";
                }
            }
        }

        if (inStream == null) {
            String message = "Profile configuration not found. Cause - " + warningMessage;
            if (log.isDebugEnabled()) {
                log.debug(message);
            }
            throw new FileNotFoundException(message);
        }

        try {
            inStream = MicroIntegratorBaseUtils.replaceSystemVariablesInXml(inStream);
        } catch (CarbonException e) {
            throw new UserStoreException(e.getMessage(), e);
        }
        builder = new StAXOMBuilder(inStream);
        OMElement documentElement = builder.getDocumentElement();

        setSecretResolver(documentElement);

        OMElement realmElement = documentElement.getFirstChildWithName(new QName(
                UserCoreConstants.RealmConfig.LOCAL_NAME_REALM));

        return realmElement;
    }

    public void setSecretResolver(OMElement rootElement) {
        secretResolver = SecretResolverFactory.create(rootElement, true);
    }
}
