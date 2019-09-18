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

package org.wso2.micro.integrator.crypto.impl.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.wso2.carbon.crypto.api.CryptoService;
import org.wso2.carbon.crypto.api.ExternalCryptoProvider;
import org.wso2.carbon.crypto.api.InternalCryptoProvider;
import org.wso2.carbon.crypto.api.KeyResolver;
import org.wso2.carbon.crypto.api.PrivateKeyRetriever;
import org.wso2.micro.integrator.core.services.CarbonServerConfigurationService;
import org.wso2.micro.integrator.crypto.impl.DefaultCryptoService;

import java.util.HashMap;
import java.util.Map;

/**
 * The class which is used for deal with the OSGi runtime for service registration and injection.
 */
@Component(name = "org.wso2.micro.integrator.crypto.impl.internal.CryptoImplComponent",
        immediate = true)
public class CryptoImplComponent {

    private final static Log log = LogFactory.getLog(CryptoImplComponent.class);

    private ServiceRegistration<CryptoService> cryptoServiceRegistration;
    private DefaultCryptoService defaultCryptoService;
    private ServiceRegistration<PrivateKeyRetriever> privateKeyRetrieverRegistration;
    private Map<String, Integer> keyResolverPriorities;

    public CryptoImplComponent() {

        this.defaultCryptoService = new DefaultCryptoService();
        keyResolverPriorities = new HashMap<>();
    }

    @Activate
    public void activate(ComponentContext context) {

        try {
            BundleContext bundleContext = context.getBundleContext();

            cryptoServiceRegistration = bundleContext
                    .registerService(CryptoService.class, this.defaultCryptoService, null);

            if (log.isDebugEnabled()) {
                log.debug(String.format("'%s' has been registered as an implementation of '%s'",
                                        defaultCryptoService.getClass().getCanonicalName(),
                                        CryptoService.class.getCanonicalName()));
            }

            privateKeyRetrieverRegistration = bundleContext
                    .registerService(PrivateKeyRetriever.class, this.defaultCryptoService, null);

            if (log.isDebugEnabled()) {
                log.debug(String.format("'%s' has been registered as an implementation of '%s'",
                                        defaultCryptoService.getClass().getCanonicalName(),
                                        PrivateKeyRetriever.class.getCanonicalName()));
                log.debug("'org.wso2.carbon.crypto.impl' bundle has been activated.");
            }

        } catch (Exception e) {
            String errorMessage = "An error occurred while activating org.wso2.carbon.crypto.impl component";
            log.error(errorMessage, e);
        }
    }

    @Deactivate
    public void deactivate(ComponentContext context) {

        cryptoServiceRegistration.unregister();
        privateKeyRetrieverRegistration.unregister();
    }

    @Reference(name = "serverConfigurationService",
            service = CarbonServerConfigurationService.class,
            policy = ReferencePolicy.DYNAMIC,
            cardinality = ReferenceCardinality.MULTIPLE,
            unbind = "unsetServerConfigurationService")
    public void setServerConfigurationService(CarbonServerConfigurationService serverConfigurationService) {

        // Read the internal crypto provider class name from the conf file.
        this.defaultCryptoService.setInternalCryptoProviderClassName(
                serverConfigurationService.getFirstProperty("CryptoService.InternalCryptoProviderClassName"));

        // Read the external crypto provider class name from the conf file.
        this.defaultCryptoService.setExternalCryptoProviderClassName(
                serverConfigurationService.getFirstProperty("CryptoService.ExternalCryptoProviderClassName"));

        // Read resolver configurations
        readAndOverrideKeyResolverPriorities(serverConfigurationService.getDocumentElement());
    }

    private void readAndOverrideKeyResolverPriorities(Element configurationRoot) {

        NodeList keyResolversNodesCandidates = configurationRoot.getElementsByTagName("KeyResolvers");

        if (keyResolversNodesCandidates != null) {

            Node keyResolversNode = keyResolversNodesCandidates.item(0);
            NodeList childNodes = keyResolversNode.getChildNodes();

            for (int i = 0; i < childNodes.getLength(); i++) {

                Node childElement = childNodes.item(i);

                if (childElement.getNodeType() == Node.ELEMENT_NODE && "KeyResolver"
                        .equals(childElement.getNodeName())) {

                    NamedNodeMap attributes = childElement.getAttributes();

                    String keyResolverClassName = attributes.getNamedItem("className").getTextContent();
                    int keyResolverPriority = Integer.parseInt(attributes.getNamedItem("priority").getTextContent());

                    // Store the priorities since the resolvers might not be injected yet.
                    keyResolverPriorities.put(keyResolverClassName, keyResolverPriority);
                }
            }
        }

        overrideKeyResolverPriorities();
    }

    private void overrideKeyResolverPriorities() {

        for (Map.Entry<String, Integer> priorityMapping : keyResolverPriorities.entrySet()) {
            defaultCryptoService.overrideKeyResolverPriority(priorityMapping.getKey(), priorityMapping.getValue());
        }
    }

    public void unsetServerConfigurationService(CarbonServerConfigurationService serverConfigurationService) {
        // Do nothing
    }

    @Reference(name = "keyResolver",
            service = KeyResolver.class,
            cardinality = ReferenceCardinality.MULTIPLE,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetKeyResolver")
    public void setKeyResolver(KeyResolver keyResolver) {

        this.defaultCryptoService.registerKeyResolver(keyResolver);

        // Re-order the resolvers whenever a new resolver is registered.
        overrideKeyResolverPriorities();
    }

    public void unsetKeyResolver(KeyResolver keyResolver) {

        this.defaultCryptoService.unregisterKeyResolver(keyResolver);
    }

    @Reference(name = "internalCryptoProvider",
            service = InternalCryptoProvider.class,
            cardinality = ReferenceCardinality.MULTIPLE,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetInternalCryptoProvider")
    public void setInternalCryptoProvider(InternalCryptoProvider internalCryptoProvider) {

        this.defaultCryptoService.registerInternalCryptoProvider(internalCryptoProvider);
        if (log.isDebugEnabled()) {
            log.debug(String.format("'%s' has been injected as an internal crypto provider.",
                                    internalCryptoProvider.getClass().getCanonicalName()));
        }
    }

    public void unsetInternalCryptoProvider(InternalCryptoProvider internalCryptoProvider) {

        this.defaultCryptoService.unregisterInternalCryptoProvider(internalCryptoProvider);
    }

    @Reference(name = "externalCryptoProvider",
            service = ExternalCryptoProvider.class,
            cardinality = ReferenceCardinality.MULTIPLE,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetExternalCryptoProvider")
    public void setExternalCryptoProvider(ExternalCryptoProvider externalCryptoProvider) {

        this.defaultCryptoService.registerExternalCryptoProvider(externalCryptoProvider);
        if (log.isDebugEnabled()) {
            log.debug(String.format("'%s' has been injected as an external crypto provider.",
                                    externalCryptoProvider.getClass().getCanonicalName()));
        }
    }

    public void unsetExternalCryptoProvider(ExternalCryptoProvider externalCryptoProvider) {

        this.defaultCryptoService.unregisterExternalCryptoProvider(externalCryptoProvider);
    }
}
