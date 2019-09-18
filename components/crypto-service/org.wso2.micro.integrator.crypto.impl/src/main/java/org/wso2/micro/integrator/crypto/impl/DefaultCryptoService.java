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

package org.wso2.micro.integrator.crypto.impl;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.crypto.api.CertificateInfo;
import org.wso2.carbon.crypto.api.CryptoContext;
import org.wso2.carbon.crypto.api.CryptoException;
import org.wso2.carbon.crypto.api.CryptoService;
import org.wso2.carbon.crypto.api.ExternalCryptoProvider;
import org.wso2.carbon.crypto.api.HybridEncryptionInput;
import org.wso2.carbon.crypto.api.HybridEncryptionOutput;
import org.wso2.carbon.crypto.api.InternalCryptoProvider;
import org.wso2.carbon.crypto.api.KeyResolver;
import org.wso2.carbon.crypto.api.PrivateKeyInfo;
import org.wso2.carbon.crypto.api.PrivateKeyRetriever;

import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The default implementation of {@link CryptoService}.
 */
public class DefaultCryptoService implements CryptoService, PrivateKeyRetriever {

    private static final Log log = LogFactory.getLog(DefaultCryptoService.class);
    private Map<String, InternalCryptoProvider> internalCryptoProviders;
    private Map<String, ExternalCryptoProvider> externalCryptoProviders;
    private List<KeyResolver> keyResolvers;
    private String internalCryptoProviderClassName;
    private String externalCryptoProviderClassName;

    public DefaultCryptoService() {

        init();
    }

    private void init() {

        externalCryptoProviders = new HashMap<>();
        internalCryptoProviders = new HashMap<>();
        keyResolvers = new ArrayList<>();
    }

    /**
     * @param cleartext               Cleartext to be encrypted.
     * @param algorithm               The encryption / decryption algorithm
     * @param javaSecurityAPIProvider The Java Security API provider.
     * @return
     * @throws CryptoException
     */
    @Override
    public byte[] encrypt(byte[] cleartext, String algorithm, String javaSecurityAPIProvider) throws CryptoException {

        failIfInternalCryptoInputsAreNotValid(cleartext, algorithm, "'Internal Encryption'");

        if (log.isDebugEnabled()) {

            log.debug(String.format("Encrypting data using the algorithm '%s' and the Java Security API provider '%s'.",
                                    algorithm, javaSecurityAPIProvider));
        }

        if (areInternalCryptoProvidersAvailable()) {

            InternalCryptoProvider mostSuitableInternalProvider = getMostSuitableInternalProvider();

            if (log.isDebugEnabled()) {

                log.debug(String.format("Internal providers are available. The most suitable provider is '%s'",
                                        mostSuitableInternalProvider.getClass().getCanonicalName()));
            }
            return mostSuitableInternalProvider.encrypt(cleartext, algorithm, javaSecurityAPIProvider);
        } else {
            String errorMessage = String.format("No internal crypto providers available. Correctly register "
                                                        + "a service implementation of '%s' as an OSGi service",
                                                InternalCryptoProvider.class);
            throw new CryptoException(errorMessage);
        }
    }

    /**
     * @param ciphertext              The ciphertext to be decrypted.
     * @param algorithm               The signature + hashing algorithm to be used in signing.
     * @param javaSecurityAPIProvider The Java Security API provider.
     * @return
     * @throws CryptoException
     */
    @Override
    public byte[] decrypt(byte[] ciphertext, String algorithm, String javaSecurityAPIProvider) throws CryptoException {

        failIfInternalCryptoInputsAreNotValid(ciphertext, algorithm, "'Internal Encryption'");

        if (log.isDebugEnabled()) {

            log.debug(String.format("Decrypting data using the algorithm '%s' and the Java Security API provider '%s'.",
                                    algorithm, javaSecurityAPIProvider));
        }

        if (areInternalCryptoProvidersAvailable()) {

            InternalCryptoProvider mostSuitableInternalProvider = getMostSuitableInternalProvider();

            if (log.isDebugEnabled()) {

                log.debug(String.format("Internal providers are available. The most suitable provider is '%s'",
                                        mostSuitableInternalProvider.getClass().getCanonicalName()));
            }

            return mostSuitableInternalProvider.decrypt(ciphertext, algorithm, javaSecurityAPIProvider);
        } else {
            String errorMessage = String.format("No internal crypto providers available. Correctly register "
                                                        + "a service implementation of '%s' as an OSGi service",
                                                InternalCryptoProvider.class);
            throw new CryptoException(errorMessage);
        }
    }

    /**
     * @param data                    The content which the signature should be generated against.
     * @param algorithm               The signature + hashing algorithm to be used in signing.
     * @param javaSecurityAPIProvider The Java Security API provider.
     * @param cryptoContext           The context information which is needed to discover the private key.
     * @return
     * @throws CryptoException
     */
    @Override
    public byte[] sign(byte[] data, String algorithm, String javaSecurityAPIProvider, CryptoContext cryptoContext)
            throws CryptoException {

        failIfExternalCryptoInputIsInvalid(data, algorithm, cryptoContext, "'Signing'");

        if (log.isDebugEnabled()) {
            log.debug(String.format("Signing data using the algorithm '%s' and the Java Security API provider '%s'; %s",
                                    algorithm, javaSecurityAPIProvider, cryptoContext));
        }

        PrivateKeyInfo privateKeyInfo = getPrivateKeyInfo(cryptoContext);

        if (privateKeyInfo == null) {
            throw new CryptoException("Private key info could not be found for " + cryptoContext);
        } else {
            if (log.isDebugEnabled()) {
                log.debug(String.format("Private key info found. %s %s", privateKeyInfo, cryptoContext));
            }
        }

        if (areExternalCryptoProvidersAvailable()) {

            ExternalCryptoProvider mostSuitableExternalProvider = getMostSuitableExternalProvider();

            if (log.isDebugEnabled()) {
                log.debug(String.format("External providers are available. The most suitable provider is '%s'",
                                        mostSuitableExternalProvider.getClass().getCanonicalName()));
            }

            return mostSuitableExternalProvider
                    .sign(data, algorithm, javaSecurityAPIProvider, cryptoContext, privateKeyInfo);
        } else {
            String errorMessage = String.format("No external crypto providers available. Correctly register "
                                                        + "a service implementation of '%s' as an OSGi service",
                                                ExternalCryptoProvider.class);
            throw new CryptoException(errorMessage);
        }
    }

    /**
     * @param ciphertext              The content which the signature should be generated against.
     * @param algorithm               The signature + hashing algorithm to be used in signing.
     * @param javaSecurityAPIProvider The Java Security API provider.
     * @param cryptoContext           The context information which is needed to discover the private key.
     * @return
     * @throws CryptoException
     */
    @Override
    public byte[] decrypt(byte[] ciphertext, String algorithm, String javaSecurityAPIProvider,
                          CryptoContext cryptoContext) throws CryptoException {

        failIfExternalCryptoInputIsInvalid(ciphertext, algorithm, cryptoContext, "'External Decrypt'");

        if (log.isDebugEnabled()) {
            log.debug(String.format(
                    "Decrypting data using the algorithm '%s' and the " + "Java Security API provider '%s'; %s",
                    algorithm, javaSecurityAPIProvider, cryptoContext));
        }

        PrivateKeyInfo privateKeyInfo = getPrivateKeyInfo(cryptoContext);

        if (privateKeyInfo == null) {
            throw new CryptoException("Private key info could not be found for : " + cryptoContext);
        } else {
            if (log.isDebugEnabled()) {
                log.debug(String.format("Private key info found. %s %s", privateKeyInfo, cryptoContext));
            }
        }

        if (areExternalCryptoProvidersAvailable()) {

            ExternalCryptoProvider mostSuitableExternalProvider = getMostSuitableExternalProvider();

            if (log.isDebugEnabled()) {
                log.debug(String.format("External providers are available. The most suitable provider is '%s'",
                                        mostSuitableExternalProvider.getClass().getCanonicalName()));
            }

            return mostSuitableExternalProvider
                    .decrypt(ciphertext, algorithm, javaSecurityAPIProvider, cryptoContext, privateKeyInfo);
        } else {
            String errorMessage = String.format("No external crypto providers available. Correctly register "
                                                        + "a service implementation of '%s' as an OSGi service",
                                                ExternalCryptoProvider.class);
            throw new CryptoException(errorMessage);
        }
    }

    /**
     * @param cleartext               The cleartext to be encrypted.
     * @param algorithm               The signature + hashing algorithm to be used in signing.
     * @param javaSecurityAPIProvider The Java Security API provider.
     * @param cryptoContext           The context information which is needed to discover the public key of the
     *                                external entity.
     * @return
     * @throws CryptoException
     */
    @Override
    public byte[] encrypt(byte[] cleartext, String algorithm, String javaSecurityAPIProvider,
                          CryptoContext cryptoContext) throws CryptoException {

        failIfExternalCryptoInputIsInvalid(cleartext, algorithm, cryptoContext, "'External Encrypt'");

        if (log.isDebugEnabled()) {
            log.debug(String.format(
                    "Encrypting data using the algorithm '%s' and the " + "Java Security API provider '%s'; %s",
                    algorithm, javaSecurityAPIProvider, cryptoContext));
        }

        CertificateInfo certificateInfo = getCertificateInfo(cryptoContext);

        if (certificateInfo == null) {
            throw new CryptoException("Certificate info could not be found for : " + cryptoContext);
        } else {
            if (log.isDebugEnabled()) {
                log.debug(String.format("Certificate info found. %s %s", certificateInfo, cryptoContext));
            }
        }

        if (areExternalCryptoProvidersAvailable()) {

            ExternalCryptoProvider mostSuitableExternalProvider = getMostSuitableExternalProvider();

            if (log.isDebugEnabled()) {
                log.debug(String.format("External providers are available. The most suitable provider is '%s'",
                                        mostSuitableExternalProvider.getClass().getCanonicalName()));
            }

            return mostSuitableExternalProvider
                    .encrypt(cleartext, algorithm, javaSecurityAPIProvider, cryptoContext, certificateInfo);
        } else {
            String errorMessage = String.format("No external crypto providers available. Correctly register "
                                                        + "a service implementation of '%s' as an OSGi service",
                                                ExternalCryptoProvider.class);
            throw new CryptoException(errorMessage);
        }
    }

    /**
     * @param data                    The data which was the signature generated on.
     * @param signature               The signature bytes of data.
     * @param algorithm               The signature + hashing algorithm to be used in signing.
     * @param javaSecurityAPIProvider The Java Security API provider.
     * @param cryptoContext           The context information which is needed to discover the public key of the
     *                                external entity.
     * @return
     * @throws CryptoException
     */
    @Override
    public boolean verifySignature(byte[] data, byte[] signature, String algorithm, String javaSecurityAPIProvider,
                                   CryptoContext cryptoContext) throws CryptoException {

        failIfSignatureVerificationInputIsInvalid(data, signature, algorithm, cryptoContext);

        if (log.isDebugEnabled()) {
            log.debug(String.format(
                    "Verifying the signature using the algorithm '%s' and the " + "Java Security API provider '%s'; %s",
                    algorithm, javaSecurityAPIProvider, cryptoContext));
        }

        CertificateInfo certificateInfo = getCertificateInfo(cryptoContext);

        if (certificateInfo == null) {
            throw new CryptoException("Certificate info could not be found for : " + cryptoContext);
        } else {
            if (log.isDebugEnabled()) {
                log.debug(String.format("Certificate info found. %s %s", certificateInfo, cryptoContext));
            }
        }

        if (areExternalCryptoProvidersAvailable()) {

            ExternalCryptoProvider mostSuitableExternalProvider = getMostSuitableExternalProvider();

            if (log.isDebugEnabled()) {
                log.debug(String.format("External providers are available. The most suitable provider is '%s'",
                                        mostSuitableExternalProvider.getClass().getCanonicalName()));
            }

            return mostSuitableExternalProvider
                    .verifySignature(data, signature, algorithm, javaSecurityAPIProvider, cryptoContext,
                                     certificateInfo);
        } else {
            String errorMessage = String.format("No external crypto providers available. Correctly register "
                                                        + "a service implementation of '%s' as an OSGi service",
                                                ExternalCryptoProvider.class);
            throw new CryptoException(errorMessage);
        }
    }

    /**
     * @param cryptoContext The context information which is used to discover the public key of the external entity.
     * @return
     * @throws CryptoException
     */
    @Override
    public Certificate getCertificate(CryptoContext cryptoContext) throws CryptoException {

        if (log.isDebugEnabled()) {
            log.debug(String.format("Retrieving the certificate using %s", cryptoContext));
        }

        CertificateInfo certificateInfo = getCertificateInfo(cryptoContext);

        if (certificateInfo == null) {
            throw new CryptoException("Certificate info could not be found for : " + cryptoContext);
        } else {
            if (log.isDebugEnabled()) {
                log.debug(String.format("Certificate info found. %s %s", certificateInfo, cryptoContext));
            }
        }

        if (certificateInfo.getCertificate() != null) {

            if (log.isDebugEnabled()) {
                log.debug("Certificate is available in certificate info.");
            }
            return certificateInfo.getCertificate();
        } else if (areExternalCryptoProvidersAvailable()) {

            if (log.isDebugEnabled()) {
                log.debug("Certificate is NOT available in certificate info. Delegating search to the providers.");
            }

            ExternalCryptoProvider mostSuitableExternalProvider = getMostSuitableExternalProvider();

            if (log.isDebugEnabled()) {
                log.debug(String.format("External providers are available. The most suitable provider is '%s'",
                                        mostSuitableExternalProvider.getClass().getCanonicalName()));
            }

            return mostSuitableExternalProvider.getCertificate(cryptoContext, certificateInfo);
        } else {
            String errorMessage = String.format("No external crypto providers available. Correctly register "
                                                        + "a service implementation of '%s' as an OSGi service",
                                                ExternalCryptoProvider.class);
            throw new CryptoException(errorMessage);
        }
    }

    /**
     * @param hybridEncryptionInput Input data for hybrid encryption.
     * @param symmetricAlgorithm    The symmetric encryption/decryption algorithm.
     * @param asymmetricAlgorithm   The asymmetric encryption/decryption algorithm.
     * @param javaSecurityProvider  The Java Security API provider.
     * @param cryptoContext         The context information which is used to discover the public key of the external entity.
     * @return {@link HybridEncryptionOutput} data related to hybrid encryption.
     * @throws CryptoException
     */
    @Override
    public HybridEncryptionOutput hybridEncrypt(HybridEncryptionInput hybridEncryptionInput, String symmetricAlgorithm,
                                                String asymmetricAlgorithm, String javaSecurityProvider,
                                                CryptoContext cryptoContext) throws CryptoException {

        failIfHybridEncryptOperationInputsAreInvalid(hybridEncryptionInput.getPlainData(), symmetricAlgorithm,
                                                     asymmetricAlgorithm, cryptoContext);
        if (log.isDebugEnabled()) {
            log.debug(String.format("Encrypting data using the asymmetric algorithm '%s' and symmetric "
                                            + "algorithm '%s' with Java Security API provider '%s'; %s",
                                    asymmetricAlgorithm, symmetricAlgorithm, javaSecurityProvider, cryptoContext));
        }
        CertificateInfo certificateInfo = getCertificateInfo(cryptoContext);
        if (certificateInfo == null) {
            throw new CryptoException("Certificate info could not be found for : " + cryptoContext);
        } else {
            if (log.isDebugEnabled()) {
                log.debug(String.format("Certificate info found. %s %s", certificateInfo, cryptoContext));
            }
        }
        if (areExternalCryptoProvidersAvailable()) {
            ExternalCryptoProvider mostSuitableExternalProvider = getMostSuitableExternalProvider();
            if (log.isDebugEnabled()) {
                log.debug(String.format("External providers are available. The most suitable provider is '%s'",
                                        mostSuitableExternalProvider.getClass().getCanonicalName()));
            }
            return mostSuitableExternalProvider
                    .hybridEncrypt(hybridEncryptionInput, symmetricAlgorithm, asymmetricAlgorithm, javaSecurityProvider,
                                   cryptoContext, certificateInfo);
        } else {
            String errorMessage = String.format("No external crypto providers available. Correctly register "
                                                        + "a service implementation of '%s' as an OSGi service",
                                                ExternalCryptoProvider.class);
            throw new CryptoException(errorMessage);
        }
    }

    /**
     * @param hybridEncryptionOutput {@link HybridEncryptionOutput} ciphered data with parameters.
     * @param symmetricAlgorithm     The symmetric encryption/decryption algorithm.
     * @param asymmetricAlgorithm    The asymmetric encryption/decryption algorithm.
     * @param javaSecurityProvider   The Java Security API provider.
     * @param cryptoContext          The context information which is used to discover the public key of the external entity.
     * @return plain data
     * @throws CryptoException
     */
    @Override
    public byte[] hybridDecrypt(HybridEncryptionOutput hybridEncryptionOutput, String symmetricAlgorithm,
                                String asymmetricAlgorithm, String javaSecurityProvider, CryptoContext cryptoContext)
            throws CryptoException {

        failIfHybridDecryptOperationInputsAreInvalid(hybridEncryptionOutput, symmetricAlgorithm, asymmetricAlgorithm,
                                                     cryptoContext);
        if (log.isDebugEnabled()) {
            log.debug(String.format("Decrypting data using the asymmetric algorithm '%s' and "
                                            + "symmetric algorithm '%s' the Java Security API provider '%s'; %s",
                                    asymmetricAlgorithm, symmetricAlgorithm, javaSecurityProvider, cryptoContext));
        }
        PrivateKeyInfo privateKeyInfo = getPrivateKeyInfo(cryptoContext);
        if (privateKeyInfo == null) {
            throw new CryptoException("Private key info could not be found for : " + cryptoContext);
        } else {
            if (log.isDebugEnabled()) {
                log.debug(String.format("Private key info found. %s %s", privateKeyInfo, cryptoContext));
            }
        }
        if (areExternalCryptoProvidersAvailable()) {
            ExternalCryptoProvider mostSuitableExternalProvider = getMostSuitableExternalProvider();
            if (log.isDebugEnabled()) {
                log.debug(String.format("External providers are available. The most suitable provider is '%s'",
                                        mostSuitableExternalProvider.getClass().getCanonicalName()));
            }
            return mostSuitableExternalProvider
                    .hybridDecrypt(hybridEncryptionOutput, symmetricAlgorithm, asymmetricAlgorithm,
                                   javaSecurityProvider, cryptoContext, privateKeyInfo);
        } else {
            String errorMessage = String.format("No external crypto providers available. Correctly register "
                                                        + "a service implementation of '%s' as an OSGi service",
                                                ExternalCryptoProvider.class);
            throw new CryptoException(errorMessage);
        }
    }

    /**
     * @param cryptoContext
     * @return
     * @throws CryptoException
     */
    @Override
    public PrivateKey getPrivateKey(CryptoContext cryptoContext) throws CryptoException {

        PrivateKeyInfo privateKeyInfo = getPrivateKeyInfo(cryptoContext);

        if (privateKeyInfo == null) {
            throw new CryptoException("Private key info could not be found for : " + cryptoContext);
        } else {
            if (log.isDebugEnabled()) {
                log.debug(String.format("Private key info found. %s %s", privateKeyInfo, cryptoContext));
            }
        }

        if (areExternalCryptoProvidersAvailable()) {

            ExternalCryptoProvider mostSuitableExternalProvider = getMostSuitableExternalProvider();

            if (log.isDebugEnabled()) {
                log.debug(String.format("External providers are available. The most suitable provider is '%s'",
                                        mostSuitableExternalProvider.getClass().getCanonicalName()));
            }

            return mostSuitableExternalProvider.getPrivateKey(cryptoContext, privateKeyInfo);
        } else {
            String errorMessage = String.format("No external crypto providers available. Correctly register "
                                                        + "a service implementation of '%s' as an OSGi service",
                                                ExternalCryptoProvider.class);
            throw new CryptoException(errorMessage);
        }
    }

    // ------------ Management methods of the default crypto service starts here. --------------------

    /**
     * Registers a new key resolver.
     *
     * @param keyResolver The key resolver to be registered.
     */
    public void registerKeyResolver(KeyResolver keyResolver) {

        if (log.isDebugEnabled()) {
            log.debug("Registering key resolver : " + keyResolver);
        }

        this.keyResolvers.add(keyResolver);
        reorderKeyResolversByPriority();
    }

    /**
     * Unregisters the given key resolver.
     *
     * @param keyResolver The key resolver to be unregistered.
     */
    public void unregisterKeyResolver(KeyResolver keyResolver) {

        if (log.isDebugEnabled()) {
            log.debug("Unregistering key resolver : " + keyResolver);
        }

        this.keyResolvers.remove(keyResolver);
    }

    /**
     * Registers a {@link InternalCryptoProvider} with this service.
     * These providers are used for internal crypto needs.
     *
     * @param internalCryptoProvider
     */
    public void registerInternalCryptoProvider(InternalCryptoProvider internalCryptoProvider) {

        if (log.isDebugEnabled()) {
            log.debug("Registering internal crypto provider : " + internalCryptoProvider);
        }

        internalCryptoProviders.put(internalCryptoProvider.getClass().getName(), internalCryptoProvider);
    }

    /**
     * Unregisters all the registered providers.
     */
    public void unregisterAllInternalCryptoProviders() {

        if (log.isDebugEnabled()) {
            log.debug("Unregistering all internal crypto providers.");
        }

        if (areInternalCryptoProvidersAvailable()) {
            internalCryptoProviders.clear();
        }
    }

    /**
     * Unregisters the given internal crypto provider.
     *
     * @param internalCryptoProvider
     */
    public void unregisterInternalCryptoProvider(InternalCryptoProvider internalCryptoProvider) {

        if (log.isDebugEnabled()) {
            log.debug("Registering internal crypto provider : " + internalCryptoProvider);
        }

        internalCryptoProviders.remove(internalCryptoProvider.getClass().getCanonicalName());
    }

    /**
     * Returns true if there is at least one {@link InternalCryptoProvider} registered with the service,
     * false otherwise.
     *
     * @return
     */
    public boolean areInternalCryptoProvidersAvailable() {

        return !internalCryptoProviders.isEmpty();
    }

    /**
     * Returns the most suitable {@link InternalCryptoProvider} for the operations, among the registered providers.
     *
     * @return The most suitable provider.
     */
    public InternalCryptoProvider getMostSuitableInternalProvider() throws CryptoException {

        if (log.isDebugEnabled()) {
            log.debug("Looking for the most suitable internal crypto provider.");
        }

        InternalCryptoProvider mostSuitableProvider;

        if (isInternalCryptoProviderConfiguredInConfigFile()) {

            if (log.isDebugEnabled()) {
                log.debug("Configured internal crypto provider class name: " + internalCryptoProviderClassName);
            }

            mostSuitableProvider = internalCryptoProviders.get(internalCryptoProviderClassName);

            if (mostSuitableProvider == null) {
                String errorMessage = String.format("The configured internal crypto provider class name: '%s' "
                                                            + "has not been registered as a service.",
                                                    internalCryptoProviderClassName);

                throw new CryptoException(errorMessage);
            }
        } else {

            if (log.isDebugEnabled()) {
                log.debug("Internal crypto provider class name is not configured.");
            }

            if (internalCryptoProviders.isEmpty()) {
                mostSuitableProvider = null;
            } else if (internalCryptoProviders.size() > 1) {

                String errorMessage = "There are more than one internal crypto providers available. "
                        + "But the preferred one is not configured in the config file. Please configure one.";

                throw new CryptoException(errorMessage);
            } else {

                if (log.isDebugEnabled()) {
                    log.debug("Only one internal crypto provider has been registered. "
                                      + "Considering it as the most suitable one.");
                }

                // Only one provider is available. Treat it as the most suitable provider.
                mostSuitableProvider = ((Map.Entry<String, InternalCryptoProvider>) (internalCryptoProviders.entrySet()
                        .toArray()[0])).getValue();
            }
        }

        return mostSuitableProvider;
    }

    /**
     * Registers a {@link ExternalCryptoProvider} with this service.
     * These providers are used for crypto needs which involves an external entity
     * (e.g. Signing messages for an external application).
     *
     * @param provider
     */
    public void registerExternalCryptoProvider(ExternalCryptoProvider provider) {

        if (log.isDebugEnabled()) {
            log.debug("Registering external crypto provider : " + provider);
        }

        externalCryptoProviders.put(provider.getClass().getName(), provider);
    }

    /**
     * Unregisters the given external crypto provider.
     *
     * @param externalCryptoProvider
     */
    public void unregisterExternalCryptoProvider(ExternalCryptoProvider externalCryptoProvider) {

        if (log.isDebugEnabled()) {
            log.debug("Unregistering external crypto provider : " + externalCryptoProvider);
        }

        externalCryptoProviders.remove(externalCryptoProvider.getClass().getCanonicalName());
    }

    /**
     * Unregisters all the registered external crypto providers.
     */
    public void unregisterAllExternalCryptoProviders() {

        if (log.isDebugEnabled()) {
            log.debug("Unregistering all external crypto providers.");
        }

        if (areExternalCryptoProvidersAvailable()) {
            externalCryptoProviders.clear();
        }

    }

    /**
     * Returns true if there is at least one {@link ExternalCryptoProvider} registered with the service, false otherwise.
     *
     * @return
     */
    public boolean areExternalCryptoProvidersAvailable() {

        return !externalCryptoProviders.isEmpty();
    }

    /**
     * Returns the most suitable {@link ExternalCryptoProvider} for the operations, among the registered providers.
     *
     * @return The most suitable provider.
     */
    public ExternalCryptoProvider getMostSuitableExternalProvider() throws CryptoException {

        if (log.isDebugEnabled()) {
            log.debug("Looking for the most suitable external crypto provider.");
        }

        ExternalCryptoProvider mostSuitableExternalProvider;

        if (isExternalCryptoProviderConfiguredInConfigFile()) {

            if (log.isDebugEnabled()) {
                log.debug("Configured external crypto provider class name: " + externalCryptoProviderClassName);
            }

            mostSuitableExternalProvider = externalCryptoProviders.get(externalCryptoProviderClassName);

            if (mostSuitableExternalProvider == null) {
                String errorMessage = String.format("The configured external crypto provider class name: '%s' "
                                                            + "has not been registered as a service.",
                                                    externalCryptoProviderClassName);

                throw new CryptoException(errorMessage);
            }

        } else {

            if (log.isDebugEnabled()) {
                log.debug("External crypto provider class name is not configured.");
            }

            if (externalCryptoProviders.isEmpty()) {
                mostSuitableExternalProvider = null;
            } else if (externalCryptoProviders.size() > 1) {
                String errorMessage = "There are more than one external crypto providers available. "
                        + "But the preferred one is not configured in the config file. Please configure one.";

                throw new CryptoException(errorMessage);
            } else {

                if (log.isDebugEnabled()) {
                    log.debug("Only one external crypto provider has been registered. "
                                      + "Considering it as the most suitable one.");
                }

                // Only one provider is available. Treat it as the most suitable provider.
                mostSuitableExternalProvider = ((Map.Entry<String, ExternalCryptoProvider>) (externalCryptoProviders
                        .entrySet().toArray()[0])).getValue();
            }
        }

        return mostSuitableExternalProvider;
    }

    /**
     * Sets the preferred internal provider class name, which was read from a configuration.
     *
     * @param internalCryptoProviderClassName The preferred internal crypto provider class name.
     */
    public void setInternalCryptoProviderClassName(String internalCryptoProviderClassName) {

        this.internalCryptoProviderClassName = internalCryptoProviderClassName;
    }

    /**
     * Sets the preferred external provider class name, which was read from a configuration.
     *
     * @param externalCryptoProviderClassName The preferred external crypto provider class name.
     */
    public void setExternalCryptoProviderClassName(String externalCryptoProviderClassName) {

        this.externalCryptoProviderClassName = externalCryptoProviderClassName;
    }

    /**
     * Overrides the priority of the given key resolver, with the given new priority.
     *
     * @param keyResolverClassName The class name of the key resolver whose priority should be overridden.
     * @param keyResolverPriority  The new priority.
     */
    public void overrideKeyResolverPriority(String keyResolverClassName, int keyResolverPriority) {

        for (KeyResolver keyResolver : keyResolvers) {
            if (keyResolver.getClass().getCanonicalName().equals(keyResolverClassName)) {

                if (log.isDebugEnabled()) {
                    log.debug(String.format("Setting %d as the priority of the key resolver '%s'", keyResolverPriority,
                                            keyResolverClassName));
                }

                keyResolver.setPriority(keyResolverPriority);
                reorderKeyResolversByPriority();
                break;
            }
        }
    }

    private void reorderKeyResolversByPriority() {

        if (log.isDebugEnabled()) {
            log.debug("Re-ordering key resolvers by priority.");
        }

        Collections.sort(keyResolvers, new Comparator<KeyResolver>() {
            @Override
            public int compare(KeyResolver k1, KeyResolver k2) {

                if (k1.getPriority() == k2.getPriority()) {
                    return 0;
                } else if (k1.getPriority() < k2.getPriority()) {
                    return -1; // Lesser number for priority means higher priority and earlier index in the list.
                } else {
                    return 1;
                }
            }
        });
    }

    private PrivateKeyInfo getPrivateKeyInfo(CryptoContext cryptoContext) {

        if (log.isDebugEnabled()) {
            log.debug("Finding private key info for " + cryptoContext);
        }

        for (KeyResolver privateKeyResolver : keyResolvers) {
            if (privateKeyResolver.isApplicable(cryptoContext)) {

                if (log.isDebugEnabled()) {
                    log.debug(String.format("%s is applicable for %s", privateKeyResolver, cryptoContext));
                }

                PrivateKeyInfo privateKeyInfo = privateKeyResolver.getPrivateKeyInfo(cryptoContext);

                // If private key info is null, continue with other resolvers, else return.
                if (privateKeyInfo != null) {
                    return privateKeyInfo;
                } else {

                    if (log.isDebugEnabled()) {
                        log.debug(String.format(
                                "Private key info was not returned by %s. " + "Continuing with remaining resolvers.",
                                privateKeyResolver));
                    }

                    continue;
                }
            }
        }

        return null;
    }

    private CertificateInfo getCertificateInfo(CryptoContext cryptoContext) {

        if (log.isDebugEnabled()) {
            log.debug("Finding certificate info for " + cryptoContext);
        }

        for (KeyResolver privateKeyResolver : keyResolvers) {
            if (privateKeyResolver.isApplicable(cryptoContext)) {

                if (log.isDebugEnabled()) {
                    log.debug(String.format("%s is applicable for %s", privateKeyResolver, cryptoContext));
                }

                CertificateInfo certificateInfo = privateKeyResolver.getCertificateInfo(cryptoContext);

                // If certificate key info is null, continue with other resolvers, else return.
                if (certificateInfo != null) {
                    return certificateInfo;
                } else {

                    if (log.isDebugEnabled()) {
                        log.debug(String.format(
                                "Certificate info was not returned by %s. " + "Continuing with remaining resolvers.",
                                privateKeyResolver));
                    }

                    continue;
                }
            }
        }
        return null;
    }

    private boolean isInternalCryptoProviderConfiguredInConfigFile() {

        return StringUtils.isNotBlank(internalCryptoProviderClassName);
    }

    private boolean isExternalCryptoProviderConfiguredInConfigFile() {

        return StringUtils.isNotBlank(externalCryptoProviderClassName);
    }

    private void failIfInternalCryptoInputsAreNotValid(byte[] data, String algorithm, String operation)
            throws CryptoException {

        if (data == null) {
            throw new CryptoException(String.format("Content provided for the %s operation can't be null", operation));
        }

        if (StringUtils.isBlank(algorithm)) {
            throw new CryptoException("Algorithm can't be empty");
        }
    }

    private void failIfExternalCryptoInputIsInvalid(byte[] data, String algorithm, CryptoContext cryptoContext,
                                                    String operation) throws CryptoException {

        if (data == null) {
            throw new CryptoException(String.format("Content provided for the %s operation can't be null", operation));
        }

        if (StringUtils.isBlank(algorithm)) {
            throw new CryptoException("Algorithm can't be empty");
        }

        if (cryptoContext == null) {
            throw new CryptoException("Crypto context can't be null");
        }
    }

    private void failIfSignatureVerificationInputIsInvalid(byte[] data, byte[] signature, String algorithm,
                                                           CryptoContext cryptoContext) throws CryptoException {

        String operation = "'Signature Validation'";
        if (signature == null) {
            throw new CryptoException(
                    String.format("Signature provided for the %s operation " + "can't be null", operation));
        }
        failIfExternalCryptoInputIsInvalid(data, algorithm, cryptoContext, operation);
    }

    private void failIfHybridEncryptOperationInputsAreInvalid(byte[] data, String symmetricAlgorithm,
                                                              String asymmetricAlgorithm, CryptoContext cryptoContext)
            throws CryptoException {

        String errorMessage;
        if (StringUtils.isBlank(symmetricAlgorithm)) {
            errorMessage = String.format("'%s' symmetric algorithm can't be empty.", symmetricAlgorithm);
            throw new CryptoException(errorMessage);
        }
        failIfExternalCryptoInputIsInvalid(data, asymmetricAlgorithm, cryptoContext, "'External Encrypt'");
    }

    private void failIfHybridDecryptOperationInputsAreInvalid(HybridEncryptionOutput hybridEncryptionOutput,
                                                              String symmetricAlgorithm, String asymmetricAlgorithm,
                                                              CryptoContext cryptoContext) throws CryptoException {

        String errorMessage;
        if (StringUtils.isBlank(symmetricAlgorithm)) {
            errorMessage = String.format("'%s' symmetric algorithm can't be empty.", symmetricAlgorithm);
            throw new CryptoException(errorMessage);
        }
        if (hybridEncryptionOutput == null) {
            errorMessage = String.format("Decryption data input can't be null.");
            throw new CryptoException(errorMessage);
        }
        if (StringUtils.isBlank(asymmetricAlgorithm)) {
            errorMessage = String.format("'%s' asymmetric algorithm can't be empty.", asymmetricAlgorithm);
            throw new CryptoException(errorMessage);
        }
        if (cryptoContext == null) {
            errorMessage = "Crypto context can't be null.";
            throw new CryptoException(errorMessage);
        }
    }
}
