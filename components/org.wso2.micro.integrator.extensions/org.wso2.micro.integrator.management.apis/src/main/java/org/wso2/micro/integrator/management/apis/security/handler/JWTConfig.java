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
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.micro.core.util.CarbonException;
import org.wso2.micro.integrator.management.apis.ManagementApiParser;
import org.wso2.micro.integrator.management.apis.ManagementApiUndefinedException;
import org.wso2.securevault.SecretResolver;

import java.io.IOException;
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
        try{
            OMElement apiOM = ManagementApiParser.getManagementApiElement();
            OMElement handlersOM = apiOM.getFirstChildWithName(new QName("handlers"));
            Iterator<OMElement> handlerOMList = handlersOM.getChildrenWithName(new QName("handler"));
            while (handlerOMList.hasNext()) {
                OMElement handlerOM = handlerOMList.next();
                if ("JWTTokenSecurityHandler".equals(handlerOM.getAttributeValue(new QName("name")))) {
                    jwtDTO.setJwtHandlerEngaged(true);
                    OMElement tokenStoreConfigOM = handlerOM.getFirstChildWithName(new QName("TokenStoreConfig"));
                    if (Objects.nonNull(tokenStoreConfigOM)) {
                        OMElement sizeElem = tokenStoreConfigOM.getFirstChildWithName(new QName("MaxSize"));
                        if (Objects.nonNull(sizeElem)) {
                            jwtDTO.setTokenStoreSize(Integer.parseInt(sizeElem.getText()));
                        }
                        OMElement removeOldestElem = tokenStoreConfigOM.getFirstChildWithName(new QName(
                                "RemoveOldestTokenOnOverflow"));
                        if (Objects.nonNull(removeOldestElem)) {
                            jwtDTO.setRemoveOldestElementOnOverflow(Boolean.parseBoolean(removeOldestElem.getText()));
                        }
                        OMElement cleanupIntervalElem = tokenStoreConfigOM.getFirstChildWithName(new QName(
                                "TokenCleanupTaskInterval"));
                        if (Objects.nonNull(cleanupIntervalElem)) {
                            jwtDTO.setCleanupThreadInterval(Integer.parseInt(cleanupIntervalElem.getText()));
                        }
                    } else {
                        LOG.fatal("Token Store config has not been defined in file "
                                  + ManagementApiParser.getConfigurationFilePath() + " Using default values");
                    }
                    OMElement tokenConfigOM = handlerOM.getFirstChildWithName(new QName("TokenConfig"));
                    if (Objects.nonNull(tokenConfigOM)) {
                        OMElement sizeElem = tokenConfigOM.getFirstChildWithName(new QName("size"));
                        OMElement expiryElem = tokenConfigOM.getFirstChildWithName(new QName("expiry"));
                        if (Objects.nonNull(expiryElem)) {
                            jwtDTO.setExpiry(expiryElem.getText());
                        }
                        if (Objects.nonNull(sizeElem)) {
                            jwtDTO.setTokenSize(sizeElem.getText());
                        }
                    } else {
                        LOG.fatal("Token config has not been defined in file "
                                  + ManagementApiParser.getConfigurationFilePath() + " Using default values");
                    }
                }
            }
        } catch (IOException exception) {
            LOG.error("internal-apis.xml file not found ", exception);
        } catch (CarbonException exception) {
            LOG.error("Error when processing file " + ManagementApiParser.getConfigurationFilePath(), exception);
        } catch (XMLStreamException exception) {
            LOG.error("Error when building configuration from file " + ManagementApiParser.getConfigurationFilePath()
                    , exception);
        } catch (ManagementApiUndefinedException e) {
            LOG.error("Error building the JWT configuration. ", e);
        }
        return jwtDTO;
    }

    public JWTConfigDTO getJwtConfigDto() {
        return JWT_CONFIG_DTO;
    }

    private static void setJwtConfigDto(JWTConfigDTO jwtConfigDto) {
        JWT_CONFIG_DTO = jwtConfigDto;
    }

}

