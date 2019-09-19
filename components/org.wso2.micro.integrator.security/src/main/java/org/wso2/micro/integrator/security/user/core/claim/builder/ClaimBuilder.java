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
package org.wso2.micro.integrator.security.user.core.claim.builder;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleContext;
import org.wso2.micro.integrator.core.util.MicroIntegratorBaseUtils;
import org.wso2.micro.integrator.security.user.core.UserStoreException;
import org.wso2.micro.integrator.security.user.core.claim.Claim;
import org.wso2.micro.integrator.security.user.core.claim.ClaimMapping;
import org.wso2.micro.integrator.security.user.core.claim.dao.ClaimDAO;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.sql.DataSource;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;

public class ClaimBuilder {

    public static final String LOCAL_NAME_DIALECTS = "Dialects";
    public static final String LOCAL_NAME_DIALECT = "Dialect";

    public static final String LOCAL_NAME_CLAIM = "Claim";
    public static final String LOCAL_NAME_CLAIM_URI = "ClaimURI";
    public static final String LOCAL_NAME_DISPLAY_NAME = "DisplayName";
    public static final String LOCAL_NAME_DESCRIPTION = "Description";
    public static final String LOCAL_NAME_REQUIRED = "Required";
    public static final String LOCAL_NAME_SUPPORTED_BY_DEFAULT = "SupportedByDefault";
    public static final String LOCAL_NAME_REG_EX = "RegEx";
    public static final String LOCAL_NAME_ATTR_ID = "AttributeID";
    public static final String LOCAL_NAME_PROFILES = "Profiles";
    public static final String LOCAL_NAME_PROFILE = "Profile";
    public static final String LOCAL_NAME_CLAIM_BEHAVIOR = "ClaimBehavior";
    public static final String LOCAL_NAME_DISPLAY_OREDR = "DisplayOrder";
    public static final String LOCAL_NAME_READ_ONLY = "ReadOnly";
    public static final String LOCAL_NAME_CHECKED_ATTR = "CheckedAttribute";


    public static final String ATTR_DIALECT_URI = "dialectURI";
    private static final String CLAIM_CONFIG = "claim-config.xml";
    private static Log log = LogFactory.getLog(ClaimBuilder.class);
    private static BundleContext bundleContext;
    InputStream inStream = null;
    int tenantId;

    public ClaimBuilder(int tenantId) {
        this.tenantId = tenantId;
    }

    public static void setBundleContext(BundleContext bundleContext) {
        ClaimBuilder.bundleContext = bundleContext;
    }

    public Map<String, ClaimMapping> buildClaimMappingsFromDatabase(DataSource ds, String realmName)
            throws ClaimBuilderException {
        Map<String, ClaimMapping> claims = new HashMap<String, ClaimMapping>();
        try {
            ClaimDAO claimDAO = new ClaimDAO(ds, tenantId);
            List<ClaimMapping> lst = claimDAO.loadClaimMappings();
            for (Iterator<ClaimMapping> ite = lst.iterator(); ite.hasNext(); ) {
                ClaimMapping cm = ite.next();
                String uri = cm.getClaim().getClaimUri();
                claims.put(uri, cm);
            }
        } catch (UserStoreException e) {
            log.error(e.getMessage(), e);
        }
        return claims;
    }

    /**
     * @return
     * @throws ClaimBuilderException
     */
    public Map<String, ClaimMapping> buildClaimMappingsFromConfigFile()
            throws ClaimBuilderException {
        OMElement element = null;
        OMElement dialectRoot = null;
        String message = null;
        Iterator<OMElement> dialectIterator = null;
        Map<String, ClaimMapping> claims = null;

        try {
            element = getRootElement();
        } catch (Exception e) {
            message = "Error while reading claim configuration";
            if (log.isDebugEnabled()) {
                log.debug(message, e);
            }
            throw new ClaimBuilderException(message, e);
        }

        dialectRoot = element.getFirstChildWithName(new QName(LOCAL_NAME_DIALECTS));

        if (dialectRoot == null) {
            message = "In valid schema <Dialects> element not present";
            if (log.isDebugEnabled()) {
                log.debug(message);
            }
            throw new ClaimBuilderException(message);
        }

        dialectIterator = dialectRoot.getChildrenWithLocalName(LOCAL_NAME_DIALECT);

        claims = new HashMap<String, ClaimMapping>();

        while (dialectIterator.hasNext()) {
            OMElement dialect = null;
            String dialectUri = null;
            Iterator<OMElement> claimIterator = null;

            dialect = dialectIterator.next();
            dialectUri = dialect.getAttribute(new QName(ATTR_DIALECT_URI)).getAttributeValue();
            claimIterator = dialect.getChildrenWithLocalName(LOCAL_NAME_CLAIM);

            while (claimIterator.hasNext()) {
                OMElement claimElement = null;
                Claim claim = null;
                ClaimMapping claimMapping = null;
                String displayName = null;
                String claimUri = null;
                String attributeId = null;
                String description = null;
                String regEx = null;
                int displayOrder = 0;

                claimElement = claimIterator.next();
                validateSchema(claimElement);

                claim = new Claim();
                claim.setDialectURI(dialectUri);

                claimUri = claimElement.getFirstChildWithName(new QName(LOCAL_NAME_CLAIM_URI))
                        .getText();
                claim.setClaimUri(claimUri);

                if (claimElement.getFirstChildWithName(new QName(LOCAL_NAME_DISPLAY_NAME)) != null) {
                    displayName = claimElement.getFirstChildWithName(
                            new QName(LOCAL_NAME_DISPLAY_NAME)).getText();
                    claim.setDisplayTag(displayName);
                }

                description = claimElement.getFirstChildWithName(new QName(LOCAL_NAME_DESCRIPTION))
                        .getText();
                claim.setDescription(description);

                if (claimElement.getFirstChildWithName(new QName(LOCAL_NAME_REG_EX)) != null) {
                    regEx = claimElement.getFirstChildWithName(new QName(LOCAL_NAME_REG_EX))
                            .getText();
                    claim.setRegEx(regEx);
                }

                if (claimElement.getFirstChildWithName(new QName(LOCAL_NAME_REQUIRED)) != null) {
                    claim.setRequired(true);
                }

                if (claimElement.getFirstChildWithName(new QName(LOCAL_NAME_SUPPORTED_BY_DEFAULT)) != null) {
                    claim.setSupportedByDefault(true);
                }

                if (claimElement.getFirstChildWithName(new QName(LOCAL_NAME_DISPLAY_OREDR)) != null) {
                    displayOrder = Integer.parseInt(claimElement.getFirstChildWithName(
                            new QName(LOCAL_NAME_DISPLAY_OREDR)).getText());
                    claim.setDisplayOrder(displayOrder);
                }

                if (claimElement.getFirstChildWithName(new QName(LOCAL_NAME_READ_ONLY)) != null) {
                    claim.setReadOnly(true);
                }

                if (claimElement.getFirstChildWithName(new QName(LOCAL_NAME_CHECKED_ATTR)) != null) {
                    claim.setReadOnly(true);
                }

                attributeId = claimElement.getFirstChildWithName(new QName(LOCAL_NAME_ATTR_ID))
                        .getText();

                claimMapping = new ClaimMapping();
                claimMapping.setClaim(claim);
                setMappedAttributes(claimMapping, attributeId);

                claims.put(claimUri, claimMapping);
            }
        }


        try {
            if (inStream != null) {
                inStream.close();
            }
        } catch (IOException e) {
            if (log.isDebugEnabled()) {
                log.error(e.getMessage(), e);
            }
            throw new ClaimBuilderException(e.getMessage(), e);
        }

        return claims;
    }

    /**
     * @param claimElement
     * @throws ClaimBuilderException
     */
    private void validateSchema(OMElement claimElement) throws ClaimBuilderException {
        String message = null;

        if (claimElement.getFirstChildWithName(new QName(LOCAL_NAME_CLAIM_URI)) == null) {
            message = "In valid schema <ClaimUri> element not present";
            if (log.isDebugEnabled()) {
                log.debug(message);
            }
            throw new ClaimBuilderException(message);
        }

        if (claimElement.getFirstChildWithName(new QName(LOCAL_NAME_DESCRIPTION)) == null) {
            message = "In valid schema <Description> element not present";
            if (log.isDebugEnabled()) {
                log.debug(message);
            }
            throw new ClaimBuilderException(message);
        }

        if (claimElement.getFirstChildWithName(new QName(LOCAL_NAME_ATTR_ID)) == null) {
            message = "In valid schema <AttributeId> element not present";
            if (log.isDebugEnabled()) {
                log.debug(message);
            }
            throw new ClaimBuilderException(message);
        }
    }

    /**
     * @return
     * @throws XMLStreamException
     * @throws IOException
     * @throws ClaimBuilderException
     */
    private OMElement getRootElement() throws XMLStreamException, IOException,
            ClaimBuilderException {
        StAXOMBuilder builder = null;

        File claimConfigXml = new File(MicroIntegratorBaseUtils.getCarbonConfigDirPath(), CLAIM_CONFIG);
        if (claimConfigXml.exists()) {
            inStream = new FileInputStream(claimConfigXml);
        }

        String warningMessage = "";
        if (inStream == null) {
            URL url;
            if (bundleContext != null) {
                if ((url = bundleContext.getBundle().getResource(CLAIM_CONFIG)) != null) {
                    inStream = url.openStream();
                } else {
                    warningMessage = "Bundle context could not find resource " + CLAIM_CONFIG +
                            " or user does not have sufficient permission to access the resource.";
                }

            } else {

                if ((url = this.getClass().getClassLoader().getResource(CLAIM_CONFIG)) != null) {
                    inStream = url.openStream();
                } else {
                    warningMessage = "ClaimBuilder could not find resource " + CLAIM_CONFIG +
                            " or user does not have sufficient permission to access the resource.";
                }
            }
        }

        if (inStream == null) {
            String message = "Claim configuration not found. Cause - " + warningMessage;
            if (log.isDebugEnabled()) {
                log.debug(message);
            }
            throw new FileNotFoundException(message);
        }

        builder = new StAXOMBuilder(inStream);
        OMElement documentElement = builder.getDocumentElement();

        return documentElement;
    }

    /**
     * Set mapped attributes to claim mapping
     *
     * @param claimMapping claim mappings
     * @param mappedAttribute mapped attributes
     */
    private void setMappedAttributes(ClaimMapping claimMapping, String mappedAttribute) {
        if (mappedAttribute != null) {
            String[] attributes = mappedAttribute.split(";");
            Map<String, String> attrMap = new HashMap<>();

            for (int i = 0; i < attributes.length; i++) {
                int index;
                if ((index = attributes[i].indexOf("/")) > 1 && attributes[i].indexOf("/") == attributes[i]
                        .lastIndexOf("/")) {
                    String domain = attributes[i].substring(0, index);
                    String attrName = attributes[i].substring(index + 1);
                    if (domain != null) {
                        attrMap.put(domain.toUpperCase(), attrName);
                    } else {
                        claimMapping.setMappedAttribute(attributes[i]);
                    }
                } else {
                    claimMapping.setMappedAttribute(attributes[i]);
                }
            }

            if (attrMap.size() > 0) {
                claimMapping.setMappedAttributes(attrMap);
            }
        }
    }
}