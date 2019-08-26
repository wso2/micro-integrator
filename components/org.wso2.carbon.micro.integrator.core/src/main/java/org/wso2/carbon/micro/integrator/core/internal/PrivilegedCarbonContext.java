/*
*  Copyright (c) 2005-2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.wso2.carbon.micro.integrator.core.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Filter;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.util.tracker.ServiceTracker;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.context.RegistryType;
import org.wso2.carbon.context.internal.CarbonContextDataHolder;
import org.wso2.carbon.context.internal.OSGiDataHolder;
import org.wso2.carbon.registry.api.Registry;
import org.wso2.carbon.user.api.TenantManager;
import org.wso2.carbon.user.api.UserRealm;
import org.wso2.carbon.user.api.UserRealmService;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.utils.CarbonUtils;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.List;

/**
 * This CarbonContext provides users the ability to carry out privileged actions such as
 * switching tenant flows, setting tenant ID etc.
 */
public class PrivilegedCarbonContext extends CarbonContext {

    private static OSGiDataHolder dataHolder = OSGiDataHolder.getInstance();
    private static final Log log = LogFactory.getLog(PrivilegedCarbonContext.class);

    // Private constructor accepting a CarbonContext holder.
    private PrivilegedCarbonContext(CarbonContextDataHolder carbonContextHolder) {
        super(carbonContextHolder);
    }

    /**
     * Starts a tenant flow. This will stack the current CarbonContext and begin a new nested flow
     * which can have an entirely different context. This is ideal for scenarios where multiple
     * super-tenant and sub-tenant phases are required within as a single block of execution.
     *
     * @see CarbonContextDataHolder#startTenantFlow()
     */
    public static void startTenantFlow() {
        CarbonUtils.checkSecurity();
        getThreadLocalCarbonContext().getCarbonContextDataHolder().startTenantFlow();
    }

    /**
     * This will end the tenant flow and restore the previous CarbonContext.
     *
     * @see CarbonContextDataHolder#endTenantFlow()
     */
    public static void endTenantFlow() {
        CarbonUtils.checkSecurity();
        getThreadLocalCarbonContext().getCarbonContextDataHolder().endTenantFlow();
    }

    public static void unloadTenant(int tenantId) {
        CarbonUtils.checkSecurity();
        CarbonContextDataHolder.unloadTenant(tenantId);
    }

    public static void destroyCurrentContext() {
        CarbonUtils.checkSecurity();
        CarbonContextDataHolder.destroyCurrentCarbonContextHolder();
    }

    /**
     *
     * @return PrivilegedCarbonContext from the current thread
     */
    public static PrivilegedCarbonContext getThreadLocalCarbonContext(){
        CarbonUtils.checkSecurity();
        return new PrivilegedCarbonContext(CarbonContextDataHolder.getThreadLocalCarbonContextHolder());
    }

    /**
     * Method to set the tenant id on this CarbonContext instance. This method will not
     * automatically calculate the tenant domain based on the tenant id.
     *
     * @param tenantId the tenant id.
     */
    public void setTenantId(int tenantId) {
        setTenantId(tenantId, false);
    }

    /**
     * Method to set the tenant id on this CarbonContext instance.
     *
     * @param tenantId            the tenant id.
     * @param resolveTenantDomain whether the tenant domain should be calculated based on this
     *                            tenant id.
     */
    public void setTenantId(int tenantId, boolean resolveTenantDomain) {
        getCarbonContextDataHolder().setTenantId(tenantId);
        if (!resolveTenantDomain ) {
            return;
        }else if(tenantId == MultitenantConstants.SUPER_TENANT_ID) {
            setTenantDomain(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME);
            return;
        }
        resolveTenantDomain(tenantId);
    }

    /**
     * Method to set the username on this CarbonContext instance.
     *
     * @param username the username.
     */
    public void setUsername(String username) {
        getCarbonContextDataHolder().setUsername(username);
    }

    /**
     * Method to set the tenant domain on this CarbonContext instance. This method will not
     * automatically calculate the tenant id based on the tenant domain.
     *
     * @param tenantDomain the tenant domain.
     */
    public void setTenantDomain(String tenantDomain) {
        setTenantDomain(tenantDomain, false);
    }

    /**
     * Method to set the tenant domain on this CarbonContext instance.
     *
     * @param tenantDomain    the tenant domain.
     * @param resolveTenantId whether the tenant id should be calculated based on this tenant
     *                        domain.
     */
    public void setTenantDomain(String tenantDomain, boolean resolveTenantId) {
        getCarbonContextDataHolder().setTenantDomain(tenantDomain);
        if (!resolveTenantId) {
            return;
        }
        resolveTenantId(tenantDomain);
    }

    /**
     * Method to obtain the tenant domain on this CarbonContext instance. This method can optionally
     * resolve the tenant domain using the tenant id that is already posses.
     *
     * @param resolve whether the tenant domain should be calculated based on the tenant id that is
     *                already known.
     * @return the tenant domain.
     */
    public String getTenantDomain(boolean resolve) {
        if (resolve && getTenantDomain() == null &&
            (getTenantId() > 0 || getTenantId() == MultitenantConstants.SUPER_TENANT_ID)) {
            resolveTenantDomain(getTenantId());
        }
        return getTenantDomain();
    }

    /**
     * Method to obtain the tenant id on this CarbonContext instance. This method can optionally
     * resolve the tenant id using the tenant domain that is already posses.
     *
     * @param resolve whether the tenant id should be calculated based on the tenant domain that is
     *                already known.
     * @return the tenant id.
     */
    public int getTenantId(boolean resolve) {
        if (resolve && getTenantId() == MultitenantConstants.INVALID_TENANT_ID && getTenantDomain() != null) {
            resolveTenantId(getTenantDomain());
        }
        return getTenantId();
    }

    /**
     * Resolve the tenant domain using the tenant id.
     *
     * @param tenantId the tenant id.
     */
    private void resolveTenantDomain(int tenantId) {
        TenantManager tenantManager = getTenantManager();
        if (tenantManager != null) {
            try {
                log.debug("Resolving tenant domain from tenant id");
                setTenantDomain(tenantManager.getDomain(tenantId));
            } catch (UserStoreException ignored) {
                // Exceptions in here, are due to issues with DB Connections. The UM Kernel takes
                // care of logging these exceptions. For us, this is of no importance. This is
                // because we are only attempting to resolve the tenant domain, which might not
                // always be possible.
            }
        }
    }

    /**
     * Resolve the tenant id using the tenant domain.
     *
     * @param tenantDomain the tenant domain.
     */
    private void resolveTenantId(String tenantDomain) {
        TenantManager tenantManager = getTenantManager();
        if (tenantManager != null) {
            try {
                log.debug("Resolving tenant id from tenant domain");
                setTenantId(tenantManager.getTenantId(tenantDomain));
            } catch (UserStoreException ignored) {
                // Exceptions in here, are due to issues with DB Connections. The UM Kernel takes
                // care of logging these exceptions. For us, this is of no importance. This is
                // because we are only attempting to resolve the tenant id, which might not always
                // be possible.
            }
        }
    }

    /**
     * Utility method to obtain the tenant manager from the realm service. This will only work in an
     * OSGi environment.
     *
     * @return tenant manager.
     */
    private TenantManager getTenantManager() {
        try {
            UserRealmService realmService = dataHolder.getUserRealmService();
            if (realmService != null) {
                return realmService.getTenantManager();
            }
        } catch (Exception ignored) {
            // We don't mind any exception occurring here. Our intention is provide a tenant manager
            // here. It is perfectly valid to not have a tenant manager in some situations.
        }
        return null;
    }

    /**
     * Method to set an instance of a registry on this CarbonContext instance.
     *
     * @param type     the type of registry to set.
     * @param registry the registry instance.
     */
    public void setRegistry(RegistryType type, Registry registry) {
        if (registry != null) {
            CarbonContextDataHolder carbonContextDataHolder = getCarbonContextDataHolder();
            switch (type) {
                case USER_CONFIGURATION:
                    log.trace("Setting config user registry instance.");
                    carbonContextDataHolder.setConfigUserRegistry(registry);
                    break;

                case SYSTEM_CONFIGURATION:
                    log.trace("Setting config system registry instance.");
                    carbonContextDataHolder.setConfigSystemRegistry(registry);
                    break;
                case USER_GOVERNANCE:
                    log.trace("Setting governance user registry instance.");
                    carbonContextDataHolder.setGovernanceUserRegistry(registry);
                    break;
                case SYSTEM_GOVERNANCE:
                    log.trace("Setting governance system registry instance.");
                    carbonContextDataHolder.setGovernanceSystemRegistry(registry);
                    break;
                case LOCAL_REPOSITORY:
                    log.trace("Setting local repository instance.");
                    carbonContextDataHolder.setLocalRepository(registry);
                    break;
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    public Registry getRegistry(RegistryType type) {
        Registry registry = super.getRegistry(type);
        if (registry != null) {
            return registry;
        }
        switch (type) {
            case SYSTEM_CONFIGURATION:
                try {
                    int tenantId = getTenantId();
                    if (tenantId != MultitenantConstants.INVALID_TENANT_ID) {
                        registry =
                                dataHolder.getRegistryService().getConfigSystemRegistry(tenantId);
                        setRegistry(RegistryType.SYSTEM_CONFIGURATION, registry);
                        return registry;
                    }
                } catch (Exception ignored) {
                    // If we can't obtain an instance of the registry, we'll simply return null. The
                    // errors that lead to this situation will be logged by the Registry Kernel.
                }
                return null;

            case SYSTEM_GOVERNANCE:
                try {
                    int tenantId = getTenantId();
                    if (tenantId != MultitenantConstants.INVALID_TENANT_ID) {
                        registry =
                                dataHolder.getRegistryService().getGovernanceSystemRegistry(
                                        tenantId);
                        setRegistry(RegistryType.SYSTEM_GOVERNANCE, registry);
                        return registry;
                    }
                } catch (Exception ignored) {
                    // If we can't obtain an instance of the registry, we'll simply return null. The
                    // errors that lead to this situation will be logged by the Registry Kernel.
                }
                return null;

            default:
                return null;
        }
    }

    public void setUserRealm(UserRealm userRealm) {
        getCarbonContextDataHolder().setUserRealm(userRealm);
    }

    /**
     * Obtain the first OSGi service found for interface or class <code>clazz</code>
     * @param clazz The type of the OSGi service
     * @return The OSGi service
     * @deprecated please use {@link #getOSGiService(Class, Hashtable)}instead
     */
    @Deprecated
    public Object getOSGiService(Class clazz) {
        return getOSGiService(clazz, null);
    }

    /**
     * Obtain the OSGi services found for interface or class <code>clazz</code>
     * @param clazz The type of the OSGi service
     * @return The List of OSGi services
     * @deprecated please use {@link #getOSGiServices(Class, Hashtable)} instead
     */
    @Deprecated
    public List<Object> getOSGiServices(Class clazz) {
        return getOSGiServices(clazz, null);
    }

    /**
     * Obtain the first OSGi service found for interface or class <code>clazz</code>  and props
     *
     * @param props attribute list that filter the service
     * @param clazz The type of the OSGi service
     * @return The OSGi service
     */
    public Object getOSGiService(Class clazz, Hashtable<String, String> props) {
        ServiceTracker serviceTracker = null;
        try {
            BundleContext bundleContext = dataHolder.getBundleContext();
            Filter osgiFilter = createFilter(bundleContext, clazz, props);
            serviceTracker = new ServiceTracker(bundleContext, osgiFilter, null);
            serviceTracker.open();
            return serviceTracker.getServices()[0];
        } catch (InvalidSyntaxException e) {
            log.error("Invalid syntax for filter passed for service : " + clazz.getName(), e);
        } finally {
            if (serviceTracker != null) {
                serviceTracker.close();
            }
        }
        return null;
    }

    /**
     * Obtain the OSGi services found for interface or class <code>clazz</code> and props
     *
     * @param props attribute list that filter the service list
     * @param clazz The type of the OSGi service
     * @return The List of OSGi services
     */
    public List<Object> getOSGiServices(Class clazz, Hashtable<String, String> props) {
        ServiceTracker serviceTracker = null;
        List<Object> services = new ArrayList<Object>();
        try {
            BundleContext bundleContext = dataHolder.getBundleContext();
            Filter osgiFilter = createFilter(bundleContext, clazz, props);
            serviceTracker = new ServiceTracker(bundleContext, osgiFilter, null);
            serviceTracker.open();
            Collections.addAll(services, serviceTracker.getServices());
        } catch (InvalidSyntaxException e) {
            log.error("Invalid syntax for filter passed for service : " + clazz.getName(), e);
        } finally {
            if (serviceTracker != null) {
                serviceTracker.close();
            }
        }
        return services;
    }

    public void setApplicationName(String applicationName) {
        getCarbonContextDataHolder().setApplicationName(applicationName);
    }
}
