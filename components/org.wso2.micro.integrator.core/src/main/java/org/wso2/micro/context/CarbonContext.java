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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Filter;
import org.osgi.framework.InvalidSyntaxException;
import org.wso2.micro.core.Constants;
import org.wso2.micro.core.context.CarbonContextDataHolder;
import org.wso2.micro.core.queueing.CarbonQueue;
import org.wso2.micro.core.queueing.CarbonQueueManager;
import org.wso2.micro.integrator.core.util.MicroIntegratorBaseUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

/**
 * This provides the API for sub-tenant programming around
 * <a href="http://wso2.com/products/carbon">WSO2 Carbon</a> and
 * <a href="http://wso2.com/cloud/stratos">WSO2 Stratos</a>. Each CarbonContext will utilize an
 * underlying {@link org.wso2.micro.core.context.CarbonContextDataHolder} instance, which will store the actual data.
 */
public class CarbonContext {

    private static final Log log = LogFactory.getLog(CarbonContext.class);
    // The reason to why we decided to have a CarbonContext and a CarbonContextHolder is to address
    // the potential build issues due to cyclic dependencies. Therefore, any bundle that can access
    // the CarbonContext can also access the CarbonContext holder. But, there are some low-level
    // bundles that can only access the CarbonContext holder. The CarbonContext provides a much
    // cleaner and easy to use API around the CarbonContext holder.

    private static List<String> allowedOSGiServices = new ArrayList<String>();
    private CarbonContextDataHolder carbonContextHolder ;
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

    /**
     * Method to obtain the tenant id on this CarbonContext instance.
     *
     * @return the tenant id.
     */
    public int getTenantId() {
        MicroIntegratorBaseUtils.checkSecurity();
        return Constants.SUPER_TENANT_ID;
    }

    /**
     * Method to obtain the tenant domain on this CarbonContext instance.
     *
     * @return the tenant domain.
     */
    public String getTenantDomain() {
        return Constants.SUPER_TENANT_DOMAIN_NAME;
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

    private static String getOSGiServicesConfigFilePath() {
        String etcDir = System.getProperty("conf.location") + File.separator + "etc";
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

}
