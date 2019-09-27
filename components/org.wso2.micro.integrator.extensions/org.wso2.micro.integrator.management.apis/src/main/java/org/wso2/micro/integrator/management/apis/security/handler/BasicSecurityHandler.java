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

package org.wso2.micro.integrator.management.apis.security.handler;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.micro.core.util.CarbonException;
import org.wso2.micro.integrator.core.util.MicroIntegratorBaseUtils;
import org.wso2.securevault.SecretResolver;
import org.wso2.securevault.SecretResolverFactory;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;

/**
 * This class extends the SecurityHandlerAdapter to create a basic security handler with a user store defined in
 * internal-apis.xml.
 */
public class BasicSecurityHandler extends SecurityHandlerAdapter {

    private static final Log LOG = LogFactory.getLog(BasicSecurityHandler.class);

    private String name;
    private SecretResolver secretResolver;
    private Map<String, char[]> userList = null;

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    protected Boolean authenticate(String authHeaderToken) {

        String decodedCredentials = new String(new Base64().decode(authHeaderToken.getBytes()));
        String[] usernamePasswordArray = decodedCredentials.split(":");
        // Avoid possible array index out of bound errors
        if (usernamePasswordArray.length != 2) {
            return false;
        }
        String userNameFromHeader = usernamePasswordArray[0];
        String passwordFromHeader = usernamePasswordArray[1];
        if (userList == null) {
            populateUserList();
        }
        if (!userList.isEmpty()) {
                for (String userNameFromStore : userList.keySet()) {
                    if (userNameFromStore.equals(userNameFromHeader)) {
                        String passwordFromStore = String.valueOf(userList.get(userNameFromStore));
                        if (isValid(passwordFromStore) && passwordFromStore.equals(passwordFromHeader)) {
                            return true;
                        }
                    }
                }
            }

        return false;
    }

    /**
     * Returns the users OMElement from the internal-apis.xml BasicSecurityHandler.
     *
     * @return OMElement users
     */
    private OMElement getUsersElem() {

        File mgtApiUserConfig = new File(MicroIntegratorBaseUtils.getCarbonConfigDirPath(), "internal-apis.xml");
        try (InputStream fileInputStream = new FileInputStream(mgtApiUserConfig)) {
            OMElement documentElement = getOMElementFromFile(fileInputStream);
            setSecretResolver(documentElement);
            Iterator internalApis = documentElement.getFirstChildWithName(new QName("apis")).getChildrenWithName(new QName("api"));
            while (internalApis.hasNext()) {
                OMElement apiOM = (OMElement) internalApis.next();
                String apiName = apiOM.getAttributeValue(new QName("name"));
                if ("ManagementApi".equals(apiName)) {
                    OMElement handlersOM = apiOM.getFirstChildWithName(new QName("handlers"));
                    Iterator handlerOMList = handlersOM.getChildrenWithName(new QName("handler"));
                    while (handlerOMList.hasNext()) {
                        OMElement handlerOM = (OMElement) handlerOMList.next();
                        if ("BasicSecurityHandler".equals(handlerOM.getAttributeValue(new QName("name")))) {
                            OMElement userStoreOM = handlerOM.getFirstChildWithName(new QName("UserStore"));
                            if (Objects.nonNull(userStoreOM)) {
                                return userStoreOM.getFirstChildWithName(new QName("users"));
                            } else {
                                LOG.fatal("UserStore has not been defined in file " + mgtApiUserConfig.getAbsolutePath());
                            }
                        }
                    }
                }
            }
        } catch (IOException exception) {
            LOG.error("internal-apis.xml file not found ", exception);
        } catch (CarbonException exception) {
            LOG.error("Error when processing file " + mgtApiUserConfig.getAbsolutePath(), exception);
        } catch (XMLStreamException exception) {
            LOG.error("Error when building configuration from file " + mgtApiUserConfig.getAbsolutePath(), exception);
        }

        return null;
    }

    /**
     * Returns the document OMElement from the internal-apis.xml file.
     *
     * @param fileInputStream input stream of internal-apis.xml
     * @return OMelement of internal-apis.xml
     */
    private OMElement getOMElementFromFile(InputStream fileInputStream) throws CarbonException, XMLStreamException {

        InputStream inputStream = MicroIntegratorBaseUtils.replaceSystemVariablesInXml(fileInputStream);
        StAXOMBuilder builder = new StAXOMBuilder(inputStream);
        return builder.getDocumentElement();
    }

    /**
     * Sets the SecretResolver the document OMElement.
     *
     * @param rootElement Document OMElement
     */
    private void setSecretResolver(OMElement rootElement) {
        this.secretResolver = SecretResolverFactory.create(rootElement, true);
    }

    /**
     * Checks if a given value is not null and not empty.
     *
     * @param value String value
     */
    private Boolean isValid(String value) {
        return (Objects.nonNull(value) && !value.isEmpty());
    }

    /**
     * Populates the userList hashMap by user store OM element
     */
    private void populateUserList() {

        userList = new HashMap<>();
        OMElement usersElement = getUsersElem();
        if (usersElement != null) {
            Iterator usersIterator = usersElement.getChildrenWithName(new QName("user"));
            if (usersIterator != null) {
                while (usersIterator.hasNext()) {
                    OMElement userElement = (OMElement) usersIterator.next();
                    OMElement userNameElement = userElement.getFirstChildWithName(new QName("username"));
                    OMElement passwordElement = userElement.getFirstChildWithName(new QName("password"));
                    if (userNameElement != null && passwordElement != null) {
                        userList.put(userNameElement.getText(), passwordElement.getText().toCharArray());
                    }
                }
            }
        }
    }
}
