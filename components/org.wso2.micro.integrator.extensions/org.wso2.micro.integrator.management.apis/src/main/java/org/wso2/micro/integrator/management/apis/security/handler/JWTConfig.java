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
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.micro.core.util.CarbonException;
import org.wso2.micro.integrator.core.util.MicroIntegratorBaseUtils;
import org.wso2.securevault.SecretResolver;
import org.wso2.securevault.SecretResolverFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Objects;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;

/**
 * This class reads through internal-apis.xml and generates JWTConfigDTO
 */
public class JWTConfig {

    private static final Log LOG = LogFactory.getLog(JWTConfig.class);
    private SecretResolver secretResolver;
    private static JWTConfig JWT_CONFIG_INSTANCE;
    private static JWTConfigDTO JWT_CONFIG_DTO;

    private JWTConfig() {
    }

    public static JWTConfig getInstance() {
        if(JWT_CONFIG_INSTANCE == null ) {
            JWT_CONFIG_INSTANCE = new JWTConfig();
            setJwtConfigDto(JWT_CONFIG_INSTANCE.populateJWTConfigDTO());
        }
        return JWT_CONFIG_INSTANCE;
    }

    /**
     * Returns the JWTConfigDTO from the internal-apis.xml JWT security handler.
     *
     * @return JWTConfigDTO config details
     */
    private JWTConfigDTO populateJWTConfigDTO() {
        JWTConfigDTO jwtDTO = new JWTConfigDTO();
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
                        if ("JWTTokenSecurityHandler".equals(handlerOM.getAttributeValue(new QName("name")))) {
                            OMElement tokenStoreConfigOM = handlerOM.getFirstChildWithName(new QName("TokenStoreConfig"));
                            if (Objects.nonNull(tokenStoreConfigOM)) {
                                OMElement sizeElem = tokenStoreConfigOM.getFirstChildWithName(new QName("MaxSize"));
                                if(Objects.nonNull(sizeElem)) {
                                    jwtDTO.setTokenStoreSize(Integer.parseInt(sizeElem.getText()));
                                }
                                OMElement removeOldestElem = tokenStoreConfigOM.getFirstChildWithName(new QName("RemoveOldestTokenOnOverflow"));
                                if(Objects.nonNull(removeOldestElem)) {
                                    jwtDTO.setRemoveOldestElementOnOverflow(Boolean.parseBoolean(removeOldestElem.getText()));
                                }
                                OMElement cleanupIntervalElem = tokenStoreConfigOM.getFirstChildWithName(new QName("TokenCleanupTaskInterval"));
                                if(Objects.nonNull(cleanupIntervalElem)) {
                                    jwtDTO.setCleanupThreadInterval(Integer.parseInt(cleanupIntervalElem.getText()));
                                }
                            } else {
                                LOG.fatal("Token Store config has not been defined in file " + mgtApiUserConfig.getAbsolutePath() + " Using default values");
                            }
                            OMElement tokenConfigOM = handlerOM.getFirstChildWithName(new QName("TokenConfig"));
                            if (Objects.nonNull(tokenConfigOM)) {
                                OMElement sizeElem = tokenConfigOM.getFirstChildWithName(new QName("size"));
                                OMElement expiryElem = tokenConfigOM.getFirstChildWithName(new QName("expiry"));
                                if(Objects.nonNull(expiryElem)) {
                                    jwtDTO.setExpiry(expiryElem.getText());
                                }
                                if(Objects.nonNull(sizeElem)){
                                    jwtDTO.setTokenSize(sizeElem.getText());
                                }
                            } else {
                                LOG.fatal("Token config has not been defined in file " + mgtApiUserConfig.getAbsolutePath() + " Using default values");
                            }
                            OMElement userStoreOM = handlerOM.getFirstChildWithName(new QName("UserStore"));
                            if (Objects.nonNull(userStoreOM)) {
                                jwtDTO.setUsers(populateUserList(userStoreOM.getFirstChildWithName(new QName("users"))));
                            } else {
                                jwtDTO.setUseCarbonUserStore(true);
                                LOG.info("User store config has not been defined in file " + mgtApiUserConfig.getAbsolutePath() + " Using carbon user store settings");
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

        return jwtDTO;
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
     * Populates the userList hashMap by user store OM element
     * @return HashMap<String, char []> Map of credential pairs
     */
    private HashMap<String, char []> populateUserList(OMElement users) {
        HashMap<String, char []> userList = new HashMap<String, char []>();
        if (users != null) {
            Iterator usersIterator = users.getChildrenWithName(new QName("user"));
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
        return userList;
    }

    /**
     * Sets the SecretResolver the document OMElement.
     *
     * @param rootElement Document OMElement
     */
    private void setSecretResolver(OMElement rootElement) {
        this.secretResolver = SecretResolverFactory.create(rootElement, true);
    }

    public JWTConfigDTO getJwtConfigDto() {
        return JWT_CONFIG_DTO;
    }

    private static void setJwtConfigDto(JWTConfigDTO jwtConfigDto) {
        JWT_CONFIG_DTO = jwtConfigDto;
    }

}

