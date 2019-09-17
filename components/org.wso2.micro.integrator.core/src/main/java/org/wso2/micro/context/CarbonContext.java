/*
 *  Copyright (c) 2005-2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */
package org.wso2.micro.context;


import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URI;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Filter;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.util.tracker.ServiceTracker;
import org.wso2.carbon.base.CarbonBaseUtils;
import org.wso2.carbon.context.RegistryType;
import org.wso2.carbon.context.internal.CarbonContextDataHolder;
import org.wso2.carbon.context.internal.OSGiDataHolder;
import org.wso2.carbon.queuing.CarbonQueue;
import org.wso2.carbon.queuing.CarbonQueueManager;
import org.wso2.carbon.registry.api.Registry;
import org.wso2.carbon.user.api.UserRealm;
import org.wso2.carbon.utils.CarbonUtils;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

/**
 * This provides the API for sub-tenant programming around
 * <a href="http://wso2.com/products/carbon">WSO2 Carbon</a> and
 * <a href="http://wso2.com/cloud/stratos">WSO2 Stratos</a>. Each CarbonContext will utilize an
 * underlying {@link org.wso2.carbon.context.internal.CarbonContextDataHolder} instance, which will store the actual data.
 */
public class CarbonContext {

    private static final Log log = LogFactory.getLog(org.wso2.micro.context.CarbonContext.class);
    // The reason to why we decided to have a CarbonContext and a CarbonContextHolder is to address
    // the potential build issues due to cyclic dependencies. Therefore, any bundle that can access
    // the CarbonContext can also access the CarbonContext holder. But, there are some low-level
    // bundles that can only access the CarbonContext holder. The CarbonContext provides a much
    // cleaner and easy to use API around the CarbonContext holder.

    private static List<String> allowedOSGiServices = new ArrayList<String>();
    private CarbonContextDataHolder carbonContextHolder = null;
    private static OSGiDataHolder dataHolder = OSGiDataHolder.getInstance();
    private static final String OSGI_SERVICES_PROPERTIES_FILE = "carboncontext-osgi-services.properties";

    static {
        FileInputStream fileInputStream = null;
        String osgiServicesFilename = getOSGiServicesConfigFilePath();
        try {
            Properties osgiServices = new Properties();
            File configFile = new File(osgiServicesFilename);
            if (configFile.exists()) { // this is an optional file
                fileInputStream = new FileInputStream(configFile);
                osgiServices.load(fileInputStream);
                Set<String> propNames = osgiServices.stringPropertyNames();
                for (String propName : propNames) {
                    allowedOSGiServices.add(osgiServices.getProperty(propName));
                }
            }
        } catch (IOException e) {
            log.error("Cannot load " + osgiServicesFilename, e);
        } finally {
            if (fileInputStream != null) {
                try {
                    fileInputStream.close();
                } catch (IOException e) {
                    log.warn("Could not close FileInputStream of file " + osgiServicesFilename, e);
                }
            }
        }
    }
    /**
     * Creates a CarbonContext using the given CarbonContext holder as its backing instance.
     *
     * @param carbonContextHolder the CarbonContext holder that backs this CarbonContext object.
     *
     * @see CarbonContextDataHolder
     */
    protected CarbonContext(CarbonContextDataHolder carbonContextHolder) {
        this.carbonContextHolder = carbonContextHolder;
    }

    /**
     * Utility method to obtain the current CarbonContext holder after an instance of a
     * CarbonContext has been created.
     *
     * @return the current CarbonContext holder
     */
    protected CarbonContextDataHolder getCarbonContextDataHolder() {
        return carbonContextHolder;
    }

    public static org.wso2.micro.context.CarbonContext getThreadLocalCarbonContext(){
        return new org.wso2.micro.context.CarbonContext(CarbonContextDataHolder.getThreadLocalCarbonContextHolder());
    }

    /**
     * Method to obtain the tenant id on this CarbonContext instance.
     *
     * @return the tenant id.
     */
    public int getTenantId() {
        CarbonBaseUtils.checkSecurity();
        return getCarbonContextDataHolder().getTenantId();
    }

    /**
     * Method to obtain the username on this CarbonContext instance.
     *
     * @return the username.
     */
    public String getUsername() {
        return getCarbonContextDataHolder().getUsername();
    }

    /**
     * Method to obtain the tenant domain on this CarbonContext instance.
     *
     * @return the tenant domain.
     */
    public String getTenantDomain() {
        return getCarbonContextDataHolder().getTenantDomain();
    }

    /**
     * Method to obtain an instance of a registry on this CarbonContext instance.
     *
     * @param type the type of registry required.
     *
     * @return the requested registry instance.
     */
    public Registry getRegistry(RegistryType type) {
        int tenantId = AccessController.doPrivileged(new PrivilegedAction<Integer>() {
            @Override
            public Integer run() {
                return getTenantId();
            }
        });
        Registry registry;
        switch (type) {
            case USER_CONFIGURATION:
                if (tenantId != MultitenantConstants.INVALID_TENANT_ID) {
                    try {
                        registry = dataHolder.getRegistryService().getConfigUserRegistry(getUsername(), tenantId);
                        return registry;
                    } catch (Exception e) {
                        // If we can't obtain an instance of the registry, we'll simply return null. The
                        // errors that lead to this situation will be logged by the Registry Kernel.
                    }
                    return null;
                }
            case SYSTEM_CONFIGURATION:
                if (tenantId != MultitenantConstants.INVALID_TENANT_ID) {
                    try {
                        registry = dataHolder.getRegistryService().getConfigSystemRegistry(tenantId);
                        return registry;
                    } catch (Exception e) {
                        // If we can't obtain an instance of the registry, we'll simply return null. The
                        // errors that lead to this situation will be logged by the Registry Kernel.
                    }
                    return null;
                }
            case USER_GOVERNANCE:
                if (tenantId != MultitenantConstants.INVALID_TENANT_ID) {
                    try {
                        registry = dataHolder.getRegistryService().getGovernanceUserRegistry(getUsername(), tenantId);
                        return registry;
                    } catch (Exception e) {
                        // If we can't obtain an instance of the registry, we'll simply return null. The
                        // errors that lead to this situation will be logged by the Registry Kernel.
                    }
                    return null;
                }
            case SYSTEM_GOVERNANCE:
                if (tenantId != MultitenantConstants.INVALID_TENANT_ID) {
                    try {
                        registry = dataHolder.getRegistryService().getGovernanceSystemRegistry(tenantId);
                        return registry;
                    } catch (Exception e) {
                        // If we can't obtain an instance of the registry, we'll simply return null. The
                        // errors that lead to this situation will be logged by the Registry Kernel.
                    }
                    return null;
                }
            case LOCAL_REPOSITORY:
                if (tenantId != MultitenantConstants.INVALID_TENANT_ID) {
                    try {
                        registry = dataHolder.getRegistryService().getLocalRepository(tenantId);
                        return registry;
                    } catch (Exception e) {
                        // If we can't obtain an instance of the registry, we'll simply return null. The
                        // errors that lead to this situation will be logged by the Registry Kernel.
                    }
                    return null;
                }
            default:
                return null;
        }
    }

    /**
     * Method to obtain the user realm on this CarbonContext instance.
     *
     * @return the user realm instance.
     */
    public UserRealm getUserRealm() {
        return getCarbonContextDataHolder().getUserRealm();
    }

    /**
     * Method to obtain a named queue instance.
     *
     * @param name the name of the queue instance.
     *
     * @return the queue instance.
     */
    @Deprecated
    public CarbonQueue<?> getQueue(String name) {
        return CarbonQueueManager.getInstance().getQueue(name);
    }

    /**
     * Method to obtain a JNDI-context with the given initialization properties.
     *
     * @param properties the properties required to create the JNDI-contNDext instance.
     *
     * @return the JNDI-context.
     * @throws NamingException if the operation failed.
     */
    @SuppressWarnings("rawtypes")
    public Context getJNDIContext(Hashtable properties) throws NamingException {
        return new InitialContext(properties);
    }

    /**
     * Method to obtain a JNDI-context.
     *
     * @return the JNDI-context.
     * @throws NamingException if the operation failed.
     */
    public Context getJNDIContext() throws NamingException {
        return new InitialContext();
    }

    /**
     * Method to discover a set of service endpoints belonging the defined scopes..
     *
     * @param scopes the scopes in which to look-up for the service.
     *
     * @return a list of service endpoints.
     */
    public String[] discover(URI[] scopes) {
        try {
            return CarbonContextDataHolder.getDiscoveryServiceProvider().probe(null, scopes, null,
                                                                               getCarbonContextDataHolder().getTenantId());
        } catch (Exception ignored) {
            // If an exception occurs, simply return no endpoints. The discovery component will
            // be responsible of reporting any errors.
            return new String[0];
        }
    }

    /**
     * Obtain the first OSGi service found for interface or class <code>clazz</code>
     * @param clazz The type of the OSGi service
     * @return The OSGi service
     * @deprecated please use {@link #getOSGiService(Class, Hashtable)} instead
     */
    @Deprecated
    public Object getOSGiService(Class clazz) {
        return getOSGiService(clazz, null);
    }

    /**
     * Obtain the OSGi services found for interface or class <code>clazz</code>
     *
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
        final Class osgiServiceClass = clazz;
        final Hashtable<String, String> properties = props;
        if (allowedOSGiServices.contains(clazz.getName())) {

            return AccessController.doPrivileged(new PrivilegedAction<Object>() {
                @Override
                public Object run() {
                    ServiceTracker serviceTracker = null;
                    try {
                        BundleContext bundleContext = dataHolder.getBundleContext();
                        Filter osgiFilter = createFilter(bundleContext, osgiServiceClass, properties);
                        serviceTracker = new ServiceTracker(bundleContext, osgiFilter, null);
                        serviceTracker.open();
                        return serviceTracker.getServices()[0];
                    } catch (InvalidSyntaxException e) {
                        log.error("Error creating osgi filter from properties", e);
                    } finally {
                        if (serviceTracker != null) {
                            serviceTracker.close();
                        }
                    }
                    return null;
                }
            });

        } else {
            throw new SecurityException("OSGi service " + clazz.getName() +
                    " cannot be accessed via CarbonContext");
        }
    }

    /**
     * Obtain the OSGi services found for interface or class <code>clazz</code> and props
     *
     * @param props attribute list that filter the service list
     * @param clazz The type of the OSGi service
     * @return The List of OSGi services
     */
    public List<Object> getOSGiServices(Class clazz, Hashtable<String, String> props) {
        final Class osgiServiceClass = clazz;
        final Hashtable<String, String> properties = props;
        if (allowedOSGiServices.contains(clazz.getName())) {

            return AccessController.doPrivileged(new PrivilegedAction<List<Object>>() {
                @Override
                public List<Object> run() {
                    ServiceTracker serviceTracker = null;
                    List<Object> services = new ArrayList<Object>();
                    try {
                        BundleContext bundleContext = dataHolder.getBundleContext();
                        Filter osgiFilter = createFilter(bundleContext, osgiServiceClass, properties);
                        serviceTracker = new ServiceTracker(bundleContext, osgiFilter, null);
                        serviceTracker.open();
                        Collections.addAll(services, serviceTracker.getServices());
                    } catch (InvalidSyntaxException e) {
                        log.error("Error creating osgi filter from properties", e);
                    } finally {
                        if (serviceTracker != null) {
                            serviceTracker.close();
                        }
                    }
                    return services;
                }
            });
        } else {
            new SecurityException("OSGi service " + clazz.getName() +
                    " cannot be accessed via CarbonContext");
        }
        return new ArrayList<Object>();
    }

    private static String getOSGiServicesConfigFilePath() {
        String etcDir = CarbonUtils.getEtcCarbonConfigDirPath();
        return etcDir + File.separator + OSGI_SERVICES_PROPERTIES_FILE;
    }

    /**
     * Create filter from the bundle context adding class name and properties
     *
     * @param bundleContext BundleContext
     * @param clazz The type of the OSGi service
     * @param props attribute list that filter the service list
     * @return Filter
     * @throws InvalidSyntaxException
     */
    protected Filter createFilter(BundleContext bundleContext, Class clazz, Hashtable<String, String> props)
            throws InvalidSyntaxException {
        StringBuilder buf = new StringBuilder();
        buf.append("(objectClass=" + clazz.getName() + ")");
        if (props != null && !props.isEmpty()) {
            buf.insert(0, "(&");
            for (Map.Entry<String, String> entry : props.entrySet()) {
                buf.append("(" + entry.getKey() + "=" + entry.getValue() + ")");
            }
            buf.append(")");
        }
        return bundleContext.createFilter(buf.toString());
    }

    public String getApplicationName() {
        return getCarbonContextDataHolder().getApplicationName();
    }
}
