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
package org.wso2.micro.integrator.security.user.core.profile.builder;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleContext;
import org.wso2.micro.integrator.core.util.MicroIntegratorBaseUtils;
import org.wso2.micro.integrator.security.user.core.UserCoreConstants;
import org.wso2.micro.integrator.security.user.core.UserStoreException;
import org.wso2.micro.integrator.security.user.core.profile.ProfileConfiguration;
import org.wso2.micro.integrator.security.user.core.profile.dao.ProfileConfigDAO;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.sql.DataSource;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;

public class ProfileConfigurationBuilder {

    public static final String LOCAL_NAME_USER_PROFILES = "UserProfiles";
    public static final String ATTR_PROFILE_NAME = "profileName";
    public static final String ATTR_PROFILE_CONFIG_NAME = "configName";
    public static final String LOCAL_NAME_PROFILE_CONFIG = "ProfileConfiguration";
    public static final String LOCAL_NAME_CLAIM = "Claim";
    public static final String LOCAL_NAME_CLAIM_URI = "ClaimURI";
    public static final String LOCAL_NAME_ATTR_ID = "AttributeID";
    public static final String LOCAL_NAME_PROFILES = "Profiles";
    public static final String LOCAL_NAME_PROFILE = "Profile";
    public static final String LOCAL_NAME_CLAIM_BEHAVIOR = "ClaimBehavior";
    public static final String ATTR_DIALECT_URI = "dialectURI";
    private static final String PROFILE_CONFIG = "profile-config.xml";
    private static Log log = LogFactory.getLog(ProfileConfigurationBuilder.class);
    private static BundleContext bundleContext;
    int tenantId;
    private InputStream inStream = null;

    public ProfileConfigurationBuilder(int tenantId) {
        this.tenantId = tenantId;
    }

    public static void setBundleContext(BundleContext bundleContext) {
        ProfileConfigurationBuilder.bundleContext = bundleContext;
    }

    public Map<String, ProfileConfiguration> buildProfileConfigurationFromDatabase(DataSource ds, String realmName)
            throws ProfileBuilderException {
        try {
            ProfileConfigDAO profileDAO = new ProfileConfigDAO(ds, tenantId);
            Map<String, ProfileConfiguration> profileConfigs = profileDAO.loadProfileConfigs();
            return profileConfigs;
        } catch (UserStoreException e) {
            if (log.isDebugEnabled()) {
                log.debug(e.getMessage(), e);
            }
            throw new ProfileBuilderException(e.getMessage(), e);
        }
    }

    /**
     * @return
     * @throws ProfileBuilderException
     */
    public Map<String, ProfileConfiguration> buildProfileConfigurationFromConfigFile()
            throws ProfileBuilderException {
        OMElement element = null;
        String message = null;
        Iterator<OMElement> configIterator = null;
        Map<String, ProfileConfiguration> profileConfigs = null;

        try {
            element = getRootElement();
        } catch (Exception e) {
            message = "Error while reading profile configuration";
            if (log.isDebugEnabled()) {
                log.debug(message, e);
            }
            throw new ProfileBuilderException(message, e);
        }

        configIterator = element
                .getChildrenWithLocalName(LOCAL_NAME_PROFILE_CONFIG);

        profileConfigs = new HashMap<String, ProfileConfiguration>();

        while (configIterator.hasNext()) {
            OMElement profileElem = configIterator.next();
            String dialectName = null;
            String profileConfigName = null;
            Iterator<OMElement> profileIter = null;
            List<String> hidden = null;
            List<String> overridden = null;
            List<String> inherited = null;
            ProfileConfiguration profileConfiguration = null;

            profileConfigName = profileElem.getAttribute(
                    new QName(ATTR_PROFILE_CONFIG_NAME)).getAttributeValue();
            dialectName = profileElem.getAttribute(new QName(ATTR_DIALECT_URI))
                    .getAttributeValue();
            profileIter = profileElem.getChildrenWithLocalName(LOCAL_NAME_CLAIM);

            hidden = new ArrayList<String>();
            overridden = new ArrayList<String>();
            inherited = new ArrayList<String>();

            while (profileIter.hasNext()) {
                OMElement claimElem = null;
                String claimURI = null;
                String behavior = null;

                claimElem = profileIter.next();
                claimURI = claimElem.getFirstChildWithName(
                        new QName(LOCAL_NAME_CLAIM_URI)).getText();
                behavior = claimElem.getFirstChildWithName(
                        new QName(LOCAL_NAME_CLAIM_BEHAVIOR)).getText();
                if (behavior.equals(UserCoreConstants.CLAIM_HIDDEN)) {
                    hidden.add(claimURI);
                } else if (behavior.equals(UserCoreConstants.CLAIM_OVERRIDEN)) {
                    overridden.add(claimURI);
                } else {
                    inherited.add(claimURI);
                }
            }
            profileConfiguration = new ProfileConfiguration(profileConfigName, hidden, overridden,
                    inherited);
            profileConfiguration.setDialectName(dialectName);
            profileConfigs.put(profileConfigName, profileConfiguration);

        }

        try {
            if (inStream != null) {
                inStream.close();
            }
        } catch (IOException e) {
            if (log.isDebugEnabled()) {
                log.debug(e.getMessage(), e);
            }
            throw new ProfileBuilderException(e.getMessage(), e);
        }


        return profileConfigs;
    }

    /**
     * @return
     * @throws XMLStreamException
     * @throws IOException
     * @throws ProfileBuilderException
     */
    private OMElement getRootElement() throws XMLStreamException, IOException,
            ProfileBuilderException {
        String carbonHome = null;
        StAXOMBuilder builder = null;

        File claimConfigXml = new File(MicroIntegratorBaseUtils.getCarbonConfigDirPath(), PROFILE_CONFIG);
        if (claimConfigXml.exists()) {
            inStream = new FileInputStream(claimConfigXml);
        }

        URL profileConfig;
        if (inStream == null) {
            if (bundleContext != null) {
                profileConfig = getProfileUrl(false);
                inStream = profileConfig.openStream();
            } else {
                profileConfig = getProfileUrl(true);

            }

            inStream = profileConfig.openStream();
        }

        builder = new StAXOMBuilder(inStream);
        OMElement documentElement = builder.getDocumentElement();

        return documentElement;
    }

    private URL getProfileUrl(boolean loadFromThisClassPath)
            throws FileNotFoundException {
        URL resourceUrl;

        if (!loadFromThisClassPath) {
            resourceUrl = bundleContext.getBundle().getResource(PROFILE_CONFIG);
        } else {
            resourceUrl = this.getClass().getClassLoader().getResource(PROFILE_CONFIG);
        }

        if (resourceUrl == null) {
            String message = "Profile configuration not found in " + PROFILE_CONFIG;
            log.warn(message);
            throw new FileNotFoundException(message);
        }

        return resourceUrl;
    }

}
