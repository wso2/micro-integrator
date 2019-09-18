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

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleContext;
import org.wso2.micro.integrator.core.util.MicroIntegratorBaseUtils;
import org.wso2.micro.integrator.security.user.api.TenantMgtConfiguration;
import org.wso2.micro.integrator.security.user.core.UserCoreConstants;
import org.wso2.micro.integrator.security.user.core.UserStoreException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;

/**
 * This reads the tenant-config.xml through axiom api and constructs an object of
 * TenantMgtConfiguration
 */
public class TenantMgtXMLProcessor {

    private static final String TENANT_MGT_XML = "tenant-mgt.xml";
    private static Log log = LogFactory.getLog(TenantMgtXMLProcessor.class);
    private BundleContext bundleContext;

    public void setBundleContext(BundleContext bundleContext) {
        this.bundleContext = bundleContext;
    }

    /**
     * Build an object of TenantMgtConfiguration reading the tenant-mgt.xml file
     *
     * @return
     * @throws UserStoreException
     */
    public TenantMgtConfiguration buildTenantMgtConfigFromFile(String tenantManagerClass)
            throws UserStoreException {

        try {
            OMElement tenantMgtConfigElement = getTenantMgtConfigElement();
            return buildTenantMgtConfiguration(tenantMgtConfigElement, tenantManagerClass);
        } catch (XMLStreamException e) {
            String error_Message = "Error in reading tenant-mgt.xml";
            if (log.isDebugEnabled()) {
                log.debug(error_Message, e);
            }
            throw new UserStoreException(error_Message);
        } catch (IOException e) {
            String error_Message = "Error in reading tenant-mgt.xml file.";
            if (log.isDebugEnabled()) {
                log.debug(error_Message, e);
            }
            throw new UserStoreException(error_Message);
        }
    }

    /**
     * Build the tenant configuration given the document element in tenant-mgt.xml
     *
     * @param tenantMgtConfigElement
     * @return
     * @throws UserStoreException
     */
    public TenantMgtConfiguration buildTenantMgtConfiguration(OMElement tenantMgtConfigElement, String tenantManagerClass)
            throws UserStoreException {
        Map<String, String> tenantMgtProperties = null;
        TenantMgtConfiguration tenantMgtConfiguration = new TenantMgtConfiguration();

        Iterator<OMElement> iterator = tenantMgtConfigElement.getChildrenWithName(
                new QName(UserCoreConstants.TenantMgtConfig.LOCAL_NAME_TENANT_MANAGER));

        for (; iterator.hasNext(); ) {
            OMElement tenantManager = iterator.next();

            if (tenantManagerClass != null && tenantManagerClass.equals(tenantManager.getAttributeValue(new QName(
                    UserCoreConstants.TenantMgtConfig.ATTRIBUTE_NAME_CLASS)))) {

                tenantMgtProperties = readChildPropertyElements(tenantManager);

                tenantMgtConfiguration.setTenantManagerClass(tenantManagerClass);
                tenantMgtConfiguration.setTenantStoreProperties(tenantMgtProperties);

                return tenantMgtConfiguration;
            }
        }
        String errorMessage = "Error in locating TenantManager compatible with PrimaryUserStore."
                + " Required a TenantManager using " + tenantManagerClass + " in tenant-mgt.xml.";
        if (log.isDebugEnabled()) {
            log.debug(errorMessage);
        }
        throw new UserStoreException(errorMessage);
    }

    private Map<String, String> readChildPropertyElements(OMElement parentElement) {

        Map<String, String> tenantMgtConfigProperties = new HashMap<String, String>();
        Iterator ite = parentElement.getChildrenWithName(new QName(
                UserCoreConstants.TenantMgtConfig.LOCAL_NAME_PROPERTY));
        while (ite.hasNext()) {
            OMElement propertyElement = (OMElement) ite.next();
            String propertyName = propertyElement.getAttributeValue(new QName(
                    UserCoreConstants.TenantMgtConfig.ATTR_NAME_PROPERTY_NAME));
            String propertyValue = propertyElement.getText();
            tenantMgtConfigProperties.put(propertyName, propertyValue);
        }
        return tenantMgtConfigProperties;
    }

    private OMElement getTenantMgtConfigElement() throws IOException, XMLStreamException {
        InputStream inStream = null;
        File tenantConfigXml = new File(MicroIntegratorBaseUtils.getCarbonConfigDirPath(), TENANT_MGT_XML);
        if (tenantConfigXml.exists()) {
            inStream = new FileInputStream(tenantConfigXml);
        }

        String warningMessage = "";
        if (inStream == null) {
            URL url;
            if (bundleContext != null) {
                if ((url = bundleContext.getBundle().getResource(TENANT_MGT_XML)) != null) {
                    inStream = url.openStream();
                } else {
                    warningMessage = "Bundle context could not find resource "
                            + TENANT_MGT_XML
                            + " or user does not have sufficient permission to access the resource.";
                }
            } else {
                if ((url = this.getClass().getClassLoader().getResource(TENANT_MGT_XML)) != null) {
                    inStream = url.openStream();
                } else {
                    warningMessage = "Could not find resource "
                            + TENANT_MGT_XML
                            + " or user does not have sufficient permission to access the resource.";
                }
            }
        }

        if (inStream == null) {
            String message = "Tenant configuration not found. Cause - " + warningMessage;
            if (log.isDebugEnabled()) {
                log.debug(message);
            }
            throw new FileNotFoundException(message);
        }

        StAXOMBuilder builder = new StAXOMBuilder(inStream);
        OMElement documentElement = builder.getDocumentElement();

        if (inStream != null) {
            inStream.close();
        }

        return documentElement;
    }
}
