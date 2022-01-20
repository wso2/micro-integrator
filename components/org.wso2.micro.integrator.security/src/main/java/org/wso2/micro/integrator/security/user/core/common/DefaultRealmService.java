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

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleContext;
import org.wso2.micro.core.Constants;
import org.wso2.micro.core.util.CarbonException;
import org.wso2.micro.integrator.core.internal.MicroIntegratorBaseConstants;
import org.wso2.micro.integrator.core.util.MicroIntegratorBaseUtils;
import org.wso2.micro.integrator.security.user.api.RealmConfiguration;
import org.wso2.micro.integrator.security.user.api.Tenant;
import org.wso2.micro.integrator.security.user.api.TenantMgtConfiguration;
import org.wso2.micro.integrator.security.user.core.UserCoreConstants;
import org.wso2.micro.integrator.security.user.core.UserRealm;
import org.wso2.micro.integrator.security.user.core.UserStoreException;
import org.wso2.micro.integrator.security.user.core.claim.builder.ClaimBuilder;
import org.wso2.micro.integrator.security.user.core.config.RealmConfigXMLProcessor;
import org.wso2.micro.integrator.security.user.core.config.TenantMgtXMLProcessor;
import org.wso2.micro.integrator.security.user.core.config.multitenancy.MultiTenantRealmConfigBuilder;
import org.wso2.micro.integrator.security.user.core.profile.builder.ProfileConfigurationBuilder;
import org.wso2.micro.integrator.security.user.core.service.RealmService;
import org.wso2.micro.integrator.security.user.core.tenant.TenantManager;
import org.wso2.micro.integrator.security.user.core.util.DatabaseUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.nio.file.Paths;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Map;
import javax.sql.DataSource;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;

public class DefaultRealmService implements RealmService {

    private static final Log log = LogFactory.getLog(DefaultRealmService.class);
    private static final String PRIMARY_TENANT_REALM = "primary";
    private static final String DB_CHECK_SQL = "select * from UM_SYSTEM_USER";
    //to track whether this is the first time initialization of the pack.
    private static boolean isFirstInitialization = true;
    private RealmCache realmCache = RealmCache.getInstance();
    private BundleContext bc;
    private RealmConfiguration bootstrapRealmConfig;
    private TenantMgtConfiguration tenantMgtConfiguration;
    private DataSource dataSource;
    private OMElement parentElement;
    private TenantManager tenantManager;
    private UserRealm bootstrapRealm;
    private MultiTenantRealmConfigBuilder multiTenantBuilder = null;
    //map to store and pass the connections to database and ldap which are created in this class.
    private Map<String, Object> properties = new Hashtable<String, Object>();

    public DefaultRealmService(BundleContext bc, RealmConfiguration realmConfig) throws Exception {
        if (realmConfig != null) {
            this.bootstrapRealmConfig = realmConfig;
        } else {
            this.bootstrapRealmConfig = buildBootStrapRealmConfig();
        }
//        this.tenantMgtConfiguration = buildTenantMgtConfig(bc,
//                this.bootstrapRealmConfig.getUserStoreProperty(UserCoreConstants.TenantMgtConfig.LOCAL_NAME_TENANT_MANAGER));
        this.dataSource = DatabaseUtil.getRealmDataSource(bootstrapRealmConfig);
        // TODO We do not need to init DB for now
        //initializeDatabase(dataSource);
        properties.put(UserCoreConstants.DATA_SOURCE, dataSource);
        properties.put(UserCoreConstants.FIRST_STARTUP_CHECK, isFirstInitialization);

        //this.tenantManager = this.initializeTenantManger(this.getTenantConfigurationElement(bc));
//        this.tenantManager = this.initializeTenantManger(this.tenantMgtConfiguration);
//        this.tenantManager.setBundleContext(bc);
        //initialize existing partitions if applicable with the particular tenant manager.
//        this.tenantManager.initializeExistingPartitions();
        // initializing the bootstrapRealm
        this.bc = bc;
        bootstrapRealm = initializeRealm(bootstrapRealmConfig, Constants.SUPER_TENANT_ID);
        Dictionary<String, String> dictionary = new Hashtable<String, String>();
        dictionary.put(UserCoreConstants.REALM_GENRE, UserCoreConstants.DELEGATING_REALM);
        if (bc != null) {
            // note that in a case of we don't run this in an OSGI envrionment
            // like checkin-client,
            // we need to avoid the registration of the service
            bc.registerService(UserRealm.class.getName(), bootstrapRealm, dictionary);
        }

    }

    public DefaultRealmService(BundleContext bc) throws Exception {
        this(bc, null);
    }

    /**
     * Non OSGI constructor
     */
    /*public DefaultRealmService(RealmConfiguration realmConfig, TenantManager tenantManager)
            throws Exception {
        this.bootstrapRealmConfig = realmConfig;
        this.dataSource = DatabaseUtil.getRealmDataSource(bootstrapRealmConfig);
        properties.put(UserCoreConstants.DATA_SOURCE, dataSource);
        this.tenantMgtConfiguration = buildTenantMgtConfig(bc,
                this.bootstrapRealmConfig.getUserStoreProperty(UserCoreConstants.TenantMgtConfig.LOCAL_NAME_TENANT_MANAGER));
        this.tenantManager = tenantManager;
        bootstrapRealm = initializeRealm(bootstrapRealmConfig, Constants.SUPER_TENANT_ID);
    }*/

    private RealmConfiguration buildBootStrapRealmConfig() throws UserStoreException {
        this.parentElement = getConfigurationElement();
        OMElement realmElement = parentElement.getFirstChildWithName(new QName(
                UserCoreConstants.RealmConfig.LOCAL_NAME_REALM));
        RealmConfigXMLProcessor rmProcessor = new RealmConfigXMLProcessor();
        rmProcessor.setSecretResolver(parentElement);
        return rmProcessor.buildRealmConfiguration(realmElement);
    }

    private TenantMgtConfiguration buildTenantMgtConfig(BundleContext bc, String tenantManagerClass)
            throws UserStoreException {
        TenantMgtXMLProcessor tenantMgtXMLProcessor = new TenantMgtXMLProcessor();
        tenantMgtXMLProcessor.setBundleContext(bc);
        return tenantMgtXMLProcessor.buildTenantMgtConfigFromFile(tenantManagerClass);
    }

    @Override
    public org.wso2.micro.integrator.security.user.api.UserRealm getTenantUserRealm(final int tenantId)
            throws org.wso2.micro.integrator.security.user.api.UserStoreException {

        try {
            return AccessController.doPrivileged(new PrivilegedExceptionAction<org.wso2.micro.integrator.security.user.api.UserRealm>() {
                @Override
                public org.wso2.micro.integrator.security.user.api.UserRealm run() throws Exception {
                    return getTenantUserRealmInternal(tenantId);
                }
            });
        } catch (PrivilegedActionException e) {
            throw (org.wso2.micro.integrator.security.user.api.UserStoreException) e.getException();
        }
    }

    private org.wso2.micro.integrator.security.user.api.UserRealm getTenantUserRealmInternal(int tenantId)
            throws org.wso2.micro.integrator.security.user.api.UserStoreException {
        if (tenantId == Constants.SUPER_TENANT_ID) {
            return bootstrapRealm;
        }
        throw new UserStoreException("Multi-tenancy support is not available in WSO2 MI");
    }

    @Override
    public UserRealm getCachedUserRealm(int tenantId) throws UserStoreException {
        return (UserRealm) realmCache.getUserRealm(tenantId, PRIMARY_TENANT_REALM);
    }

    @Override
    public void clearCachedUserRealm(int tenantId) throws UserStoreException {
        realmCache.clearFromCache(tenantId, PRIMARY_TENANT_REALM);
    }

    @Override
    public UserRealm getUserRealm(final RealmConfiguration tenantRealmConfig) throws UserStoreException {

        try {
            return AccessController.doPrivileged(new PrivilegedExceptionAction<UserRealm>() {
                @Override
                public UserRealm run() throws Exception {
                    return getUserRealmInternal(tenantRealmConfig);
                }
            });
        } catch (PrivilegedActionException e) {
            throw (UserStoreException) e.getException();
        }
    }

    private UserRealm getUserRealmInternal(RealmConfiguration tenantRealmConfig) throws UserStoreException {
        UserRealm userRealm = null;
        int tenantId = tenantRealmConfig.getTenantId();
        if (tenantId == Constants.SUPER_TENANT_ID) {
            return bootstrapRealm;
        }
        throw new UserStoreException("Multi-tenancy support is not available in WSO2 MI. Hence unable to provide " +
                "UserRealm for tenantId: " + tenantRealmConfig.getTenantId());
    }

    @SuppressWarnings("rawtypes")
    public UserRealm initializeRealm(RealmConfiguration realmConfig, int tenantId)
            throws UserStoreException {
        ClaimBuilder.setBundleContext(bc);
        ProfileConfigurationBuilder.setBundleContext(bc);
        UserRealm userRealm = null;
        try {
            Class clazz = Class.forName(realmConfig.getRealmClassName());
            userRealm = (UserRealm) clazz.newInstance();
            userRealm.init(realmConfig, properties, tenantId);
        } catch (Exception e) {
            if (!realmConfig.isPrimary()) {
                String msg = "Cannot initialize the realm.";
                log.warn(msg, e);
            } else {
                String msg = "Cannot initialize the realm.";
                if (log.isDebugEnabled()) {
                    log.debug(msg, e);
                }
                throw new UserStoreException(msg, e);
            }
        }
        return userRealm;
    }

    // TODO : Move this into RealmConfigXMLProcessor

    private OMElement getConfigurationElement() throws UserStoreException {
        InputStream inStream = null;
        try {
            String userMgt = MicroIntegratorBaseUtils.getUserMgtXMLPath();
            if (userMgt != null) {
                File userMgtXml = new File(userMgt);
                if (!userMgtXml.exists()) {
                    String msg = "Instance of a WSO2 User Manager has not been created. user-mgt.xml is not found.";
                    throw new FileNotFoundException(msg);
                }
                inStream = new FileInputStream(userMgtXml);
            } else {
                String confPath = System.getProperty(MicroIntegratorBaseConstants.CARBON_CONFIG_DIR_PATH);
                if (confPath == null) {
                    inStream = this.getClass().getClassLoader().getResourceAsStream(Paths.get("repository", "conf", "user-mgt.xml").toString());
                } else {
                    String relativeConfDirPath = Paths.get(System.getProperty("carbon.home")).relativize(Paths.get(confPath)).toString();
                    inStream = this.getClass().getClassLoader().getResourceAsStream(Paths.get(relativeConfDirPath, "user-mgt.xml").toString());
                }
                if (inStream == null) {
                    String msg = "Instance of a WSO2 User Manager has not been created. user-mgt.xml is not found. Please set the carbon.home";
                    throw new FileNotFoundException(msg);
                }
            }

            StAXOMBuilder builder = new StAXOMBuilder(MicroIntegratorBaseUtils.replaceSystemVariablesInXml(inStream));
            return builder.getDocumentElement();
        } catch (FileNotFoundException e) {
            //e.getMessage() contains meaningful message
            if (log.isDebugEnabled()) {
                log.debug(e.getMessage(), e);
            }
            throw new UserStoreException(e.getMessage(), e);
        } catch (XMLStreamException e) {
            if (log.isDebugEnabled()) {
                log.debug(e.getMessage(), e);
            }
            throw new UserStoreException(e.getMessage(), e);
        } catch (CarbonException e) {
            String errorMessage = "Error occurred while replacing System variables in XML";
            if (log.isDebugEnabled()) {
                log.debug(errorMessage, e);
            }
            throw new UserStoreException(errorMessage, e);
        } finally {
            if (inStream != null) {
                try {
                    inStream.close();
                } catch (IOException e) {
                    log.error("Couldn't close the InputStream" + e.getMessage(), e);
                }
            }
        }
    }

    /*private void initializeDatabase(DataSource ds) throws Exception {
        String value = System.getProperty("setup");
        if (value != null) {
            DatabaseCreator databaseCreator = new DatabaseCreator(ds);
            try {
                if (!databaseCreator.isDatabaseStructureCreated(DB_CHECK_SQL)) {
                    databaseCreator.createRegistryDatabase();
                } else {
                    isFirstInitialization = false;
                    log.info("Database already exists. Not creating a new database.");
                }
            } catch (Exception e) {
                String msg = "Error in creating the database";
                if (log.isDebugEnabled()) {
                    log.debug(msg, e);
                }
                throw new Exception(msg, e);
            }
        }
    }*/

    @SuppressWarnings({"unchecked", "rawtypes"})
    private TenantManager initializeTenantManger(TenantMgtConfiguration tenantMgtConfiguration)
            throws Exception {
        TenantManager tenantManager = null;
        // read the tenant manager from tenant-mgt.xml
        //String className = configElement.getAttribute(new QName("class")).getAttributeValue();
        String className = tenantMgtConfiguration.getTenantManagerClass();
        Class clazz = Class.forName(className);

        Constructor constructor = clazz.getConstructor(OMElement.class, Map.class);
        /*put the tenantMgtConfiguration and realm configuration inside the property map that is
        passed to tenant manager constructor. These are mainly used by LDAPTenantManager*/
        properties.put(UserCoreConstants.TENANT_MGT_CONFIGURATION, tenantMgtConfiguration);
        properties.put(UserCoreConstants.REALM_CONFIGURATION, bootstrapRealmConfig);

        //tenant config OMElement passed to the constructor is not used anymore. Hence passing a null.
        Object newObject = constructor.newInstance(null, properties);
        tenantManager = (TenantManager) newObject;

        return tenantManager;
    }

    @Override
    public RealmConfiguration getBootstrapRealmConfiguration() {
        return bootstrapRealmConfig;
    }

    @Override
    public void setBootstrapRealmConfiguration(RealmConfiguration realmConfiguration) {
        this.bootstrapRealmConfig = realmConfiguration;
    }

    @Override
    public UserRealm getBootstrapRealm() throws UserStoreException {
        return bootstrapRealm;
    }

    @Override
    public void setTenantManager(org.wso2.micro.integrator.security.user.api.TenantManager tenantManager)
            throws org.wso2.micro.integrator.security.user.api.UserStoreException {
        throw new org.wso2.micro.integrator.security.user.api.UserStoreException("Multi-tenancy is not available in " +
                "WSO2 Micro Integrator");
    }

    @Override
    public TenantManager getTenantManager() {
        return this.tenantManager;
    }

    @Override
    public void setTenantManager(TenantManager tenantManager) {
        log.error("Multi-tenancy is not available in WSO2 Micro Integrator");
    }

    @Override
    public TenantMgtConfiguration getTenantMgtConfiguration() {
        log.error("This is not available in WSO2 Micro Integrator");
        return null;
    }

    @SuppressWarnings("rawtypes")
    @Override
    public MultiTenantRealmConfigBuilder getMultiTenantRealmConfigBuilder()
            throws UserStoreException {
        throw new UserStoreException("Multi-tenancy is not available in WSO2 Micro Integrator");
    }

    private void errorEncountered(Exception e) throws UserStoreException {
        String msg = "Exception while creating multi tenant builder " + e.getMessage();
        if (log.isDebugEnabled()) {
            log.debug(msg, e);
        }
        throw new UserStoreException(msg, e);
    }
}



