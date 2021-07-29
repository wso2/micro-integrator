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
package org.wso2.carbon.inbound.endpoint.internal.http.api;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import javax.xml.namespace.QName;

import org.apache.axiom.om.OMElement;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.SynapseException;
import org.apache.synapse.api.cors.CORSConfiguration;
import org.apache.synapse.commons.util.MiscellaneousUtil;
import org.apache.synapse.transport.passthru.core.ssl.SSLConfiguration;
import org.wso2.carbon.inbound.endpoint.osgi.service.InboundEndpointServiceDSComponent;
import org.wso2.carbon.inbound.endpoint.persistence.PersistenceUtils;
import org.wso2.micro.core.util.StringUtils;
import org.wso2.micro.integrator.core.util.MicroIntegratorBaseUtils;
import org.wso2.securevault.SecretResolver;
import org.wso2.securevault.SecretResolverFactory;

/**
 * {@code ConfigurationLoader} contains utilities to load configuration file content required for Internal APIs
 * implementation.
 */
public class ConfigurationLoader {

    private static Log log = LogFactory.getLog(ConfigurationLoader.class);

    private static final QName ROOT_Q = new QName("internalApis");
    private static final QName API_Q = new QName("api");
    private static final QName CLASS_Q = new QName("class");
    private static final QName NAME_ATT = new QName("name");
    private static final QName PROTOCOL_Q = new QName("protocol");
    private static final QName HANDLERS_Q = new QName("handlers");
    private static final QName RESOURCES_Q = new QName("resources");
    private static final QName USER_STORE_Q = new QName("userStore");
    private static final QName USERS_Q = new QName("users");
    private static final QName USER_Q = new QName("user");
    private static final QName USERNAME_Q = new QName("username");
    private static final QName PASSWORD_Q = new QName("password");
    private static final QName IS_ADMIN_Q = new QName("isAdmin");
    private static final QName STORE_PASSWORD_Q = new QName("Password");
    private static final QName KEY_PASSWORD_Q = new QName("KeyPassword");

    private static final String APIS = "apis";
    private static final String SSL_CONFIG = "sslConfig";
    private static final String KEYSTORE_ATT = "keystore";
    private static final String TRUSTSTORE_ATT = "truststore";
    private static final String SSL_VERIFY_CLIENT_ATT = "sslVerifyClient";
    private static final String SSL_PROTOCOL_ATT = "sslProtocol";
    private static final String HTTPS_PROTOCOLS_ATT = "httpsProtocols";
    private static final String CERTIFICATE_REVOCATION_VERIFIER_ATT = "certificateRevocationVerifier";
    private static final String PREFERRED_CIPHERS_ATT = "preferredCiphers";

    private static SSLConfiguration sslConfiguration;
    private static boolean sslConfiguredSuccessfully;
    private static Map<String, UserInfo> userMap;

    private static List<InternalAPI> internalHttpApiList = new ArrayList<>();
    private static List<InternalAPI> internalHttpsApiList = new ArrayList<>();

    private static String internalInboundHttpPortProperty;
    private static String internalInboundHttpsPortProperty;

    private static SecretResolver secretResolver;

    private static final int PORT_OFFSET = PersistenceUtils.getPortOffset();

    public static void loadInternalApis(String apiFilePath) {

        OMElement apiConfig = MiscellaneousUtil.loadXMLConfig(apiFilePath);

        if (apiConfig != null) {

            if (!ROOT_Q.equals(apiConfig.getQName())) {
                handleException("Invalid internal api configuration file");
            }

            setSecretResolver(apiConfig);
            populateUserStore(apiConfig);

            Iterator apiIterator = apiConfig.getChildrenWithLocalName(APIS);

            if (apiIterator.hasNext()) {

                OMElement apis = (OMElement) apiIterator.next();
                Iterator apiList = apis.getChildrenWithName(API_Q);
                if (apiList != null) {

                    Iterator sslConfigIterator = apiConfig.getChildrenWithLocalName(SSL_CONFIG);
                    if (sslConfigIterator.hasNext()) {
                        sslConfiguration = setSslConfig((OMElement) sslConfigIterator.next());
                    }

                    while (apiList.hasNext()) {

                        OMElement apiElement = (OMElement) apiList.next();
                        String name = null;

                        if (apiElement.getAttribute(NAME_ATT) != null) {
                            name = apiElement.getAttributeValue(NAME_ATT);
                            if (name == null || name.isEmpty()) {
                                handleException("Name not specified in one or more handlers");
                            }
                            if (!Boolean.parseBoolean(
                                    System.getProperty(Constants.PREFIX_TO_ENABLE_INTERNAL_APIS + name))) {
                                continue;
                            }
                        } else {
                            handleException("Name not defined in one or more handlers");
                        }

                        if (apiElement.getAttribute(CLASS_Q) != null) {
                            String className = apiElement.getAttributeValue(CLASS_Q);
                            if (!className.isEmpty()) {

                                InternalAPI internalApi = createApi(className);
                                internalApi.setName(name);
                                populateHandlers(apiElement, internalApi);
                                internalApi.setCORSConfiguration(getCORSConfiguration(apiElement));
                                if (apiElement.getAttribute(PROTOCOL_Q) != null) {

                                    String protocols = apiElement.getAttributeValue(PROTOCOL_Q);
                                    if (!protocols.isEmpty()) {

                                        String[] protocolList = protocols.split(" ");
                                        for (String protocol : protocolList) {
                                            switch (protocol) {
                                            case "http":
                                                internalHttpApiList.add(internalApi);
                                                break;
                                            case "https":
                                                internalHttpsApiList.add(internalApi);
                                                break;
                                            default:
                                                handleException("Unsupported Protocol found for Internal API");
                                            }
                                        }

                                    } else {
                                        log.warn("No protocol specified for InternalAPI : " + name
                                                         + ". Hence it will not be enabled.");
                                    }
                                } else {
                                    log.warn("Protocol not defined for InternalAPI : " + name
                                                     + ". Hence it will not be enabled.");
                                }
                            } else {
                                handleException("Class name is null for Internal InternalAPI name : " + name);
                            }
                        } else {
                            handleException("Class name not defined for Internal InternalAPI named : " + name);
                        }
                    }
                }
            }
        }
    }

    /**
     * Populates the userList hashMap by userStore OM element
     */
    private static void populateUserStore(OMElement apiConfig) {
        OMElement userStoreOM = apiConfig.getFirstChildWithName(USER_STORE_Q);
        if (Objects.nonNull(userStoreOM)) {
            userMap = populateUsers(userStoreOM.getFirstChildWithName(USERS_Q));
        } else {
            userMap = null;
        }
    }

    /**
     * Populates individual users.
     *
     * @param users the parent element of users
     * @return map of users against UserInfo config
     */
    private static Map<String, UserInfo> populateUsers(OMElement users) {
        HashMap<String, UserInfo> userMap = new HashMap<>();
        if (users != null) {
            @SuppressWarnings("unchecked")
            Iterator<OMElement> usersIterator = users.getChildrenWithName(USER_Q);
            if (usersIterator != null) {
                while (usersIterator.hasNext()) {
                    OMElement userElement = usersIterator.next();
                    OMElement userNameElement = userElement.getFirstChildWithName(USERNAME_Q);
                    OMElement passwordElement = userElement.getFirstChildWithName(PASSWORD_Q);
                    OMElement isAdminElement = userElement.getFirstChildWithName(IS_ADMIN_Q);
                    if (userNameElement != null && passwordElement != null) {
                        String userName = userNameElement.getText();
                        if (userMap.containsKey(userName)) {
                            handleException("Error parsing the file based user store. User: " + userName + " defined "
                                            + "more than once. ");
                        }
                        boolean isAdmin = false;
                        if (isAdminElement != null) {
                            isAdmin = Boolean.parseBoolean(isAdminElement.getText().trim());
                        }
                        userMap.put(userName, new UserInfo(userName,
                                resolveSecret(passwordElement.getText()).toCharArray(), isAdmin));
                    }
                }
            }
        }
        return userMap;
    }

    /**
     * Checks if the text is protected and returns decrypted text if protected, else returns the plain text
     * @param text
     * @return Decrypted text if protected else plain text
     */
    private static String resolveSecret(String text) {
        String alias = org.wso2.securevault.commons.MiscellaneousUtil.getProtectedToken(text);
        if (!StringUtils.isEmpty(alias)) {
            if (!secretResolver.isInitialized()) {
                secretResolver.init(InboundEndpointServiceDSComponent.getSecretCallbackHandlerService().getSecretCallbackHandler());
            }
            return secretResolver.resolve(alias);
        }
        return text;
    }

    private static void populateHandlers(OMElement apiElement, InternalAPI api) {

        List<InternalAPIHandler> handlerList = new ArrayList<>();
        OMElement handlersElement = apiElement.getFirstChildWithName(HANDLERS_Q);
        if (handlersElement != null) {
            Iterator<OMElement> handlers = handlersElement.getChildElements();

            while (handlers.hasNext()) {
                OMElement handlerElement = handlers.next();
                if (handlerElement.getAttribute(NAME_ATT) != null) {
                    String handlerName = handlerElement.getAttributeValue(NAME_ATT);
                    if (handlerElement.getAttribute(CLASS_Q) != null) {
                        String handlerClass = handlerElement.getAttributeValue(CLASS_Q);
                        OMElement resourcesElement = handlerElement.getFirstChildWithName(RESOURCES_Q);
                        List<String> resourcesList = new ArrayList<>();
                        if (Objects.nonNull(resourcesElement)) {
                            Iterator resources = resourcesElement.getChildElements();
                            while (resources.hasNext()) {
                                OMElement resource = (OMElement) resources.next();
                                resourcesList.add(resource.getText());
                            }
                        }
                        InternalAPIHandler handler = createHandler(handlerClass, api.getContext(), resourcesList);
                        handler.setName(handlerName);
                        handlerList.add(handler);
                    } else {
                        handleException(
                                "Class attribute is not defined in " + handlerElement.getAttributeValue(NAME_ATT));
                    }
                } else {
                    handleException("Name not defined in one or more handlers");
                }
            }
        }
        api.setHandlers(handlerList);
    }

    private static InternalAPIHandler createHandler(String classFQName, String context, List<String> resources) {

        try {
            Constructor c = Class.forName(classFQName).getConstructor(String.class);
            Object obj = c.newInstance(context);
            if (obj instanceof InternalAPIHandler) {
                InternalAPIHandler internalAPIHandler = (InternalAPIHandler) obj;
                internalAPIHandler.setResources(resources);
                return internalAPIHandler;
            } else {
                throw new SynapseException("Error creating Internal InternalAPIHandler. "
                                                   + "The InternalAPIHandler should be of type InternalAPIHandler");
            }
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | NoSuchMethodException
                | InvocationTargetException e) {
            throw new SynapseException("Error creating Internal InternalAPIHandler for class name : " + classFQName, e);
        }
    }

    private static CORSConfiguration getCORSConfiguration(OMElement apiElement) {
        InternalAPICORSConfiguration config = new InternalAPICORSConfiguration();
        OMElement corsElement = apiElement.getFirstChildWithName(new QName("cors"));
        if (corsElement != null) {
            String enabled = corsElement.getFirstChildWithName(new QName("enabled")).getText();
            String origins = corsElement.getFirstChildWithName(new QName("allowedOrigins")).getText();
            String headers = corsElement.getFirstChildWithName(new QName("allowedHeaders")).getText();

            config.setEnabled(Boolean.valueOf(enabled));
            config.setAllowedOrigins(origins);
            config.setAllowedHeaders(headers);
        }
        return config;
    }

    private static InternalAPI createApi(String classFQName) {

        try {
            Object obj = Class.forName(classFQName).newInstance();
            if (obj instanceof InternalAPI) {
                return (InternalAPI) obj;
            } else {
                throw new SynapseException(
                        "Error creating Internal InternalAPI. The InternalAPI should be of type InternalAPI");
            }
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
            throw new SynapseException("Error creating Internal InternalAPI for class name : " + classFQName, e);
        }
    }

    public static Map<String, UserInfo> getUserMap() {
        return userMap;
    }

    public static int getInternalInboundHttpPort() {

        return getPort(Constants.INTERNAL_HTTP_API_PORT, internalInboundHttpPortProperty,
                       Constants.DEFAULT_INTERNAL_HTTP_API_PORT);
    }

    public static int getInternalInboundHttpsPort() {

        return getPort(Constants.INTERNAL_HTTPS_API_PORT, internalInboundHttpsPortProperty,
                       Constants.DEFAULT_INTERNAL_HTTPS_API_PORT);
    }

    /**
     * Sets the SecretResolver the document OMElement.
     *
     * @param rootElement Document OMElement
     */
    private static void setSecretResolver(OMElement rootElement) {
        secretResolver = SecretResolverFactory.create(rootElement, true);
    }

    private static int getPort(String propertyName, String portProperty, int defaultPort) {

        int port = defaultPort;
        if (portProperty != null) {
            try {
                port = Integer.parseInt(portProperty);
            } catch (NumberFormatException ex) {
                handleException(propertyName + " is not in proper format", ex);
            }
        }
        return port + PORT_OFFSET;
    }

    private static void handleException(String msg) {
        log.error(msg);
        throw new SynapseException(msg);
    }

    private static void handleException(String msg, Exception ex) {
        log.error(msg, ex);
        throw new SynapseException(msg, ex);
    }

    public static SSLConfiguration getSslConfiguration() {
        return sslConfiguration;
    }

    public static List<InternalAPI> getHttpInternalApis() {
        return internalHttpApiList;
    }

    public static List<InternalAPI> getHttpsInternalApis() {
        return internalHttpsApiList;
    }

    public static boolean isSslConfiguredSuccessfully() {
        return sslConfiguredSuccessfully;
    }

    /**
     * Reads and check from the synapse properties file whether the Internal api is enabled.
     *
     * @return - whether internal api is enabled in synapse properties file.
     */
    public static boolean isInternalApiEnabled() {

        File synapseProperties = Paths.get(MicroIntegratorBaseUtils.getCarbonConfigDirPath(), "synapse.properties")
                .toFile();
        Properties properties = new Properties();
        try (InputStream inputStream = new FileInputStream(synapseProperties)) {
            properties.load(inputStream);
        } catch (FileNotFoundException e) {
            handleException("synapse.properties file not found", e);
        } catch (IOException e) {
            handleException("Error while reading synapse.properties file", e);
        }
        String internalInboundEnabledProperty = properties.getProperty(Constants.INTERNAL_HTTP_API_ENABLED);
        if (internalInboundEnabledProperty == null) {
            return false;
        }
        boolean isEnabled = Boolean.parseBoolean(internalInboundEnabledProperty);
        if (isEnabled) {
            internalInboundHttpPortProperty = properties.getProperty(Constants.INTERNAL_HTTP_API_PORT);
            internalInboundHttpsPortProperty = properties.getProperty(Constants.INTERNAL_HTTPS_API_PORT);
        }
        return isEnabled;
    }

    private static SSLConfiguration setSslConfig(OMElement sslConfig) {

        Iterator iterator = sslConfig.getChildElements();

        String trustStore = null;
        String keyStore = null;
        String clientAuth = null;
        String httpsProtocols = null;
        String revocationVerifier = null;
        String sslProtocol = null;
        String prefferedCiphers = null;

        while (iterator.hasNext()) {

            OMElement parameter = (OMElement) iterator.next();
            String attributeName = parameter.getAttributeValue(NAME_ATT);
            OMElement element = parameter.getFirstElement();

            if (element != null) {

                switch (attributeName) {
                case KEYSTORE_ATT:
                    OMElement keystorePasswordEl = element.getFirstChildWithName(STORE_PASSWORD_Q);
                    OMElement keyPasswordEl = element.getFirstChildWithName(KEY_PASSWORD_Q);
                    if (keystorePasswordEl == null) {
                        handleException("Cannot proceed because Password element is missing in KeyStore");
                    }
                    if (keyPasswordEl == null) {
                        handleException("Cannot proceed because KeyPassword element is missing in KeyStore");
                    }
                    keystorePasswordEl.setText(resolveSecret(keystorePasswordEl.getText()));
                    keyPasswordEl.setText(resolveSecret(keyPasswordEl.getText()));
                    keyStore = element.toString();
                    break;
                case TRUSTSTORE_ATT:
                    OMElement truststorePasswordEl = element.getFirstChildWithName(STORE_PASSWORD_Q);
                    if (truststorePasswordEl == null) {
                        handleException("Cannot proceed because Password element is missing in TrustStore");
                    }
                    truststorePasswordEl.setText(resolveSecret(truststorePasswordEl.getText()));
                    trustStore = element.toString();
                    break;
                case CERTIFICATE_REVOCATION_VERIFIER_ATT:
                    revocationVerifier = element.toString();
                    break;
                default:
                    handleException("Invalid parameter found for internal API ssl configuration");
                }

            } else {

                String value = parameter.getText();

                switch (attributeName) {
                case SSL_PROTOCOL_ATT:
                    sslProtocol = value;
                    break;
                case PREFERRED_CIPHERS_ATT:
                    prefferedCiphers = value;
                    break;
                case HTTPS_PROTOCOLS_ATT:
                    httpsProtocols = value;
                    break;
                case SSL_VERIFY_CLIENT_ATT:
                    clientAuth = value;
                    break;
                default:
                    handleException("Invalid parameter found for internal API ssl configuration");
                }
            }
        }

        if (keyStore == null) {
            log.error("Keystore must be specified to configure internal Https Api.");
        } else {
            sslConfiguredSuccessfully = true;
        }

        return new SSLConfiguration(keyStore, trustStore, clientAuth, httpsProtocols, revocationVerifier, sslProtocol,
                                    prefferedCiphers);
    }

    public static void destroy() {
        internalHttpApiList = new ArrayList<>();
        internalHttpsApiList = new ArrayList<>();
    }

}
