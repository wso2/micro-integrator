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
package org.wso2.micro.integrator.server.extensions;

import org.apache.xerces.impl.Constants;
import org.apache.xerces.util.SecurityManager;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.wso2.micro.integrator.server.CarbonLaunchExtension;
import org.wso2.micro.integrator.server.LauncherConstants;
import org.wso2.micro.integrator.server.util.Utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.jar.Attributes;
import java.util.jar.Manifest;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

/**
 * Converts JAR files containing Axis2 Service Deployers into OSGi bundles
 */
public class AxisServiceDeployerBundleCreator implements CarbonLaunchExtension {

    private static final String DEPLOYERS_DIR;

    static {
        String componentsPath = System.getProperty(LauncherConstants.CARBON_COMPONENTS_DIR_PATH);
        if (componentsPath != null) {
            Path path = Paths.get(componentsPath, "axis2deployers");
            DEPLOYERS_DIR = Paths.get(System.getProperty(LauncherConstants.CARBON_HOME)).relativize(path).toString();
        } else {
            DEPLOYERS_DIR = Paths.get("repository", "components", "axis2deployers").toString();
        }
    }

    private static final Logger logger = Logger.getLogger(AxisServiceDeployerBundleCreator.class.getName());

    private static final int ENTITY_EXPANSION_LIMIT = 0;

    public void perform() {
        String dropinsPath = System.getProperty(LauncherConstants.CARBON_DROPINS_DIR_PATH);
        File dropinsFolder;
        if (dropinsPath == null) {
            dropinsFolder = new File(Utils.getCarbonComponentRepo(), "dropins");
        } else {
            dropinsFolder = new File(dropinsPath);
        }
        File dir = Utils.getBundleDirectory(DEPLOYERS_DIR);
        File[] files = dir.listFiles(new Utils.JarFileFilter());
        if (files != null) {

            for (File file : files) {
                ZipInputStream zin = null;
                try {
                    ZipEntry entry;
                    String entryName;
                    InputStream inputStream = new FileInputStream(file);
                    zin = new ZipInputStream(inputStream);
                    boolean validComponentXmlFound = false;
                    while ((entry = zin.getNextEntry()) != null) {
                        entryName = entry.getName();
                        if (entryName.equals("META-INF/component.xml")) {
                            URL url = new URL("jar:file:" + file.getAbsolutePath() + "!/" + entryName);
                            DocumentBuilderFactory dbf = getSecuredDocumentBuilder();
                            DocumentBuilder db = dbf.newDocumentBuilder();
                            Document doc = db.parse(url.openStream());
                            doc.getDocumentElement().normalize();
                            Element rootEle = doc.getDocumentElement();
                            NodeList childNodes = rootEle.getElementsByTagName("deployer");
                            if (childNodes.getLength() > 0) {
                                Element deployerEle = (Element) childNodes.item(0);
                                if (deployerEle.getElementsByTagName("directory").getLength() == 1
                                        && deployerEle.getElementsByTagName("extension").getLength() == 1
                                        && deployerEle.getElementsByTagName("class").getLength() == 1) {
                                    validComponentXmlFound = true;
                                }
                            }
                        }
                    }

                    if (!validComponentXmlFound) {
                        System.out.println(
                                "A valid component.xml was not found in AxisDeployer jar file " + file.getAbsolutePath()
                                        + ". A component.xml file with the "
                                        + "following entries should be placed in the META-INF directory.\n"
                                        + "<deployer>\n" + "\t<directory>[dir]</directory>\n"
                                        + "\t<extension>[extension]</extension>\n" + "\t<class>[some.Class]</class>\n"
                                        + "\t</deployer>\n" + "</deployers>");
                        continue;
                    }

                    Manifest mf = new Manifest();
                    Attributes attribs = mf.getMainAttributes();
                    attribs.putValue(LauncherConstants.DYNAMIC_IMPORT_PACKAGE, "*");
                    attribs.putValue("Axis2Deployer", file.getName());
                    Utils.createBundle(file, dropinsFolder, mf, "");

                } catch (Throwable e) {
                    System.out.println("Cannot create Axis2Deployer bundle from jar file " + file.getAbsolutePath());
                    e.printStackTrace();
                } finally {
                    try {
                        //close the Stream
                        if (zin != null) {
                            zin.close();
                        }
                    } catch (IOException e) {
                        System.out.println("Unable to close the InputStream " + e.getMessage());
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    private static DocumentBuilderFactory getSecuredDocumentBuilder() {

        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        dbf.setXIncludeAware(false);
        dbf.setExpandEntityReferences(false);
        try {
            dbf.setFeature(Constants.SAX_FEATURE_PREFIX + Constants.EXTERNAL_GENERAL_ENTITIES_FEATURE, false);
            dbf.setFeature(Constants.SAX_FEATURE_PREFIX + Constants.EXTERNAL_PARAMETER_ENTITIES_FEATURE, false);
            dbf.setFeature(Constants.XERCES_FEATURE_PREFIX + Constants.LOAD_EXTERNAL_DTD_FEATURE, false);
        } catch (ParserConfigurationException e) {
            logger.log(Level.SEVERE,
                       "Failed to load XML Processor Feature " + Constants.EXTERNAL_GENERAL_ENTITIES_FEATURE + " or "
                               + Constants.EXTERNAL_PARAMETER_ENTITIES_FEATURE + " or "
                               + Constants.LOAD_EXTERNAL_DTD_FEATURE);
        }

        SecurityManager securityManager = new SecurityManager();
        securityManager.setEntityExpansionLimit(ENTITY_EXPANSION_LIMIT);
        dbf.setAttribute(Constants.XERCES_PROPERTY_PREFIX + Constants.SECURITY_MANAGER_PROPERTY, securityManager);

        return dbf;
    }

}
