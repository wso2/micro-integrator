/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.micro.integrator.dataservices.core;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.axiom.om.impl.jaxp.OMSource;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.xerces.impl.Constants;
import org.apache.xerces.util.SecurityManager;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static org.wso2.micro.core.util.CarbonUtils.getSecuredTransformerFactory;

/**
 * This class is used in transforming data services result using XSLT.
 */
public class XSLTTransformer {

    private static final Log log = LogFactory.getLog(DBUtils.class);

    private String xsltPath;

    private Transformer transformer;

    private XMLInputFactory xmlInputFactory;

    public XSLTTransformer(String xsltPath) throws TransformerConfigurationException,
                                                   DataServiceFault, IOException {
        this.xsltPath = xsltPath;
        TransformerFactory tFactory = getSecuredTransformerFactory();
        try {
            getSecuredDocumentBuilder(false).parse(DBUtils.getInputStreamFromPath(this.getXsltPath()));
        } catch (SAXException e) {
            throw new DataServiceFault(e, "Error in parsing XSLT file " + xsltPath
                                                        + " Possible XML External entity attack, Error - "
                                                        + e.getMessage());
        } catch (ParserConfigurationException e) {
            throw new DataServiceFault(e, "Error initializing secure document builder, Error - " + e.getMessage());
        }
        this.transformer = tFactory.newTransformer(new StreamSource(
                DBUtils.getInputStreamFromPath(this.getXsltPath())));
        this.xmlInputFactory = DBUtils.getXMLInputFactory();
    }

    public String getXsltPath() {
        return xsltPath;
    }

    public Transformer getTransformer() {
        return transformer;
    }

    public XMLInputFactory getXmlInputFactory() {
        return xmlInputFactory;
    }

    /**
     * Transforms the given XML element using the current XSLT transformer and
     * returns the result.
     *
     * @param inputXML The XML data to be transformed
     * @return The transformed XML
     * @throws DataServiceFault
     */
    public OMElement transform(OMElement inputXML) throws DataServiceFault {
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            Source xmlSource = new OMSource(inputXML);
            this.getTransformer().transform(xmlSource, new StreamResult(outputStream));
            ByteArrayInputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray());
            XMLStreamReader reader = this.getXmlInputFactory().createXMLStreamReader(inputStream);
            StAXOMBuilder builder = new StAXOMBuilder(reader);
            return builder.getDocumentElement();
        } catch (Exception e) {
            String msg = "Error in transforming with XSLT: " + e.getMessage();
            throw new DataServiceFault(e, msg);
        }
    }

    /**
     * This method provides a secured document builder which will secure XXE attacks.
     *
     * @param setIgnoreComments whether to set setIgnoringComments in DocumentBuilderFactory.
     * @return DocumentBuilder
     * @throws ParserConfigurationException
     */
    private static DocumentBuilder getSecuredDocumentBuilder(boolean setIgnoreComments) throws
                                                                                        ParserConfigurationException {
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        documentBuilderFactory.setIgnoringComments(setIgnoreComments);
        documentBuilderFactory.setNamespaceAware(true);
        documentBuilderFactory.setXIncludeAware(false);
        documentBuilderFactory.setExpandEntityReferences(false);
        documentBuilderFactory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
        SecurityManager securityManager = new SecurityManager();
        securityManager.setEntityExpansionLimit(0);
        documentBuilderFactory.setAttribute(Constants.XERCES_PROPERTY_PREFIX +
                Constants.SECURITY_MANAGER_PROPERTY, securityManager);
        documentBuilder.setEntityResolver(new EntityResolver() {
            @Override
            public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException {
                throw new SAXException("Possible XML External Entity (XXE) attack. Skip resolving entity");
            }
        });
        return documentBuilder;
    }

}
