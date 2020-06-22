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

package org.wso2.micro.integrator.mediation.security.vault;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.micro.integrator.mediation.security.vault.util.SecureVaultUtil;
import org.wso2.securevault.CipherFactory;
import org.wso2.securevault.CipherOperationMode;
import org.wso2.securevault.DecryptionProvider;
import org.wso2.securevault.EncodingType;
import org.wso2.securevault.commons.MiscellaneousUtil;
import org.wso2.securevault.definition.CipherInformation;
import org.wso2.securevault.definition.IdentityKeyStoreInformation;
import org.wso2.securevault.definition.KeyStoreInformationFactory;
import org.wso2.securevault.definition.TrustKeyStoreInformation;
import org.wso2.securevault.keystore.IdentityKeyStoreWrapper;
import org.wso2.securevault.keystore.KeyStoreWrapper;
import org.wso2.securevault.keystore.TrustKeyStoreWrapper;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.cert.CertificateException;
import java.util.Properties;
import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;

public class CipherInitializer {
	private static Log log = LogFactory.getLog(CipherInitializer.class);

	private static final String LOCATION = "location";
	private static final String KEY_STORE = "keyStore";
	private static final String DOT = ".";
	private static final String ALGORITHM = "algorithm";
	private static final String DEFAULT_ALGORITHM = "RSA";
	private static final String TRUSTED = "trusted";
	private static final String CIPHER_TRANSFORMATION_SECRET_CONF_PROPERTY = "keystore.identity.CipherTransformation";
	private static final String CIPHER_TRANSFORMATION_SYSTEM_PROPERTY = "org.wso2.CipherTransformation";
	private static CipherInitializer cipherInitializer  = new CipherInitializer();

	private IdentityKeyStoreWrapper identityKeyStoreWrapper;

	private TrustKeyStoreWrapper trustKeyStoreWrapper;

	private DecryptionProvider decryptionProvider = null;

	private Cipher encryptionProvider = null;

	private CipherInitializer() {
		super();
		boolean initPro = false;
		try {
			initPro = init();
			if (initPro) {
				initCipherDecryptProvider();
				initEncrypt();
			} else {
				log.error("Either Configuration properties can not be loaded or No secret"
						  + " repositories have been configured please check PRODUCT_HOME/repository/conf/security "
						  + " refer links related to configure WSO2 Secure vault");
			}
		} catch (CipherToolException e) {
			log.error("Cipher initialization failed", e);
		}

	}

	public static CipherInitializer getInstance() {

	   	return cipherInitializer;
	}

	private boolean init() throws CipherToolException {

		Properties properties = SecureVaultUtil.loadProperties();

		if (properties == null) {
			log.error("KeyStore configuration properties cannot be found");
			return false;
		}

		String configurationFile =
		                           MiscellaneousUtil.getProperty(properties,
		                                                         SecureVaultConstants.PROP_SECRET_MANAGER_CONF,
		                                                         SecureVaultConstants.PROP_DEFAULT_CONF_LOCATION);

		Properties configurationProperties = MiscellaneousUtil.loadProperties(configurationFile);
		if (configurationProperties.isEmpty()) {
			if (log.isDebugEnabled()) {
				log.debug("Configuration properties can not be loaded form : " + configurationFile +
				          " Will use synapse properties");
			}
			configurationProperties = properties;

		}

		// global password provider implementation class if defined in secret
		// manager conf file
		String globalSecretProvider = MiscellaneousUtil.getProperty(configurationProperties,
																	SecureVaultConstants.PROP_SECRET_PROVIDER,
																	null);
		if (globalSecretProvider == null || "".equals(globalSecretProvider)) {
			if (log.isDebugEnabled()) {
				log.debug("No global secret provider is configured.");
			}
		}

		String repositoriesString =
		                            MiscellaneousUtil.getProperty(configurationProperties,
		                                                          SecureVaultConstants.PROP_SECRET_REPOSITORIES,
		                                                          null);
		if (repositoriesString == null || "".equals(repositoriesString)) {
			log.error("No secret repositories have been configured");
			return false;
		}

		String[] repositories = repositoriesString.split(",");
		if (repositories.length == 0) {
			log.error("No secret repositories have been configured");
			return false;
		}

		// Create a KeyStore Information for private key entry KeyStore
		IdentityKeyStoreInformation identityInformation =
		                                                  KeyStoreInformationFactory.createIdentityKeyStoreInformation(properties);

		// Create a KeyStore Information for trusted certificate KeyStore
		TrustKeyStoreInformation trustInformation =
		                                            KeyStoreInformationFactory.createTrustKeyStoreInformation(properties);

		String identityKeyPass = null;
		String identityStorePass = null;
		String trustStorePass = null;
		if (identityInformation != null) {
			identityKeyPass = identityInformation.getKeyPasswordProvider().getResolvedSecret();
			identityStorePass =
			                    identityInformation.getKeyStorePasswordProvider()
			                                       .getResolvedSecret();
		}

		if (trustInformation != null) {
			trustStorePass = trustInformation.getKeyStorePasswordProvider().getResolvedSecret();
		}

		if (!validatePasswords(identityStorePass, identityKeyPass, trustStorePass)) {

			log.error("Either Identity or Trust keystore password is mandatory"
			          + " in order to initialized secret manager.");
			return false;
		}

		identityKeyStoreWrapper = new IdentityKeyStoreWrapper();
		identityKeyStoreWrapper.init(identityInformation, identityKeyPass);

		trustKeyStoreWrapper = new TrustKeyStoreWrapper();
		if (trustInformation != null) {
			trustKeyStoreWrapper.init(trustInformation);
		}

		for (String secretRepo : repositories) {

			String sb = SecureVaultConstants.PROP_SECRET_REPOSITORIES
						+ SecureVaultConstants.DOT
						+ secretRepo
						+ SecureVaultConstants.DOT
						+ SecureVaultConstants.PROP_PROVIDER;
			String provider = MiscellaneousUtil.getProperty(configurationProperties, sb, null);
			if (provider == null || "".equals(provider)) {
				throw new CipherToolException("Repository provider cannot be null ");
			}

			if (log.isDebugEnabled()) {
				log.debug("Initiating a File Based Secret Repository");
			}

		}
		return true;
	}

	private boolean validatePasswords(String identityStorePass, String identityKeyPass,
	                                  String trustStorePass) {
		boolean isValid = false;
		if (trustStorePass != null && !"".equals(trustStorePass)) {
			if (log.isDebugEnabled()) {
				log.debug("Trust Store Password cannot be found.");
			}
			isValid = true;
		} else {
			if (identityStorePass != null && !"".equals(identityStorePass) &&
			    identityKeyPass != null && !"".equals(identityKeyPass)) {
				if (log.isDebugEnabled()) {
					log.debug("Identity Store Password "
					          + "and Identity Store private key Password cannot be found.");
				}
				isValid = true;
			}
		}
		return isValid;
	}

	private void initCipherDecryptProvider() {
		if(decryptionProvider !=null) return;
		Properties properties = SecureVaultUtil.loadProperties();

		// Load algorithm
		String algorithm = getCipherTransformation(properties);

		// Load keyStore
		String buffer = DOT + KEY_STORE;
		String keyStore = MiscellaneousUtil.getProperty(properties, buffer, null);

		KeyStoreWrapper keyStoreWrapper;

		if (TRUSTED.equals(keyStore)) {
			keyStoreWrapper = trustKeyStoreWrapper;

		} else {
			keyStoreWrapper = identityKeyStoreWrapper;
		}

		CipherInformation cipherInformation = new CipherInformation();
		cipherInformation.setAlgorithm(algorithm);
		cipherInformation.setCipherOperationMode(CipherOperationMode.DECRYPT);
		cipherInformation.setInType(EncodingType.BASE64); // TODO
		decryptionProvider = CipherFactory.createCipher(cipherInformation, keyStoreWrapper);

	}


	
	/**
	 * Initializing the encryption key store which uses to encrypt the given
	 * plain text
	 * 
	 */
	private void initEncrypt() throws CipherToolException {
	
		if(encryptionProvider != null) return;
		
		Properties properties = SecureVaultUtil.loadProperties();
			
		String keyStoreFile;
		String keyType;
		String aliasName;
		String password;
		String provider = null;
		Cipher cipher = null;

		keyStoreFile = properties.getProperty("keystore.identity.location");

		File keyStore = new File(keyStoreFile);

		if (!keyStore.exists()) {
			throw new CipherToolException("Primary Key Store Can not be found at Default location");
		}
		
		keyType =  properties.getProperty("keystore.identity.type"); 
		aliasName = properties.getProperty("keystore.identity.alias"); ;
	
		// Create a KeyStore Information for private key entry KeyStore
		IdentityKeyStoreInformation identityInformation = KeyStoreInformationFactory.createIdentityKeyStoreInformation(properties);

		password = identityInformation.getKeyStorePasswordProvider().getResolvedSecret();

		try {
			KeyStore primaryKeyStore = getKeyStore(keyStoreFile, password, keyType, provider);
			java.security.cert.Certificate certs = primaryKeyStore.getCertificate(aliasName);

			String algorithm = getCipherTransformation(properties);

			cipher = Cipher.getInstance(algorithm);
			cipher.init(Cipher.ENCRYPT_MODE, certs);
		} catch (InvalidKeyException | NoSuchAlgorithmException | KeyStoreException | NoSuchPaddingException e) {
			throw new CipherToolException("Error initializing Cipher ", e);
		}
		encryptionProvider = cipher;
	}

	/**
	 * Get the Cipher Transformation to be used by the Cipher. We have the option of configuring this globally as a
	 * System Property '-Dorg.wso2.CipherTransformation', which can be overridden at the 'secret-conf.properties' level
	 * by specifying the property 'keystore.identity.CipherTransformation'. If neither are configured the default 'RSA'
	 * will be used
	 *
	 * @param properties Properties from the 'secret-conf.properties' file
	 * @return Cipher Transformation String
	 */
	private String getCipherTransformation(Properties properties) {
		String cipherTransformation = System.getProperty(CIPHER_TRANSFORMATION_SYSTEM_PROPERTY);

		if (cipherTransformation == null) {
			cipherTransformation = DEFAULT_ALGORITHM;
		}

		return MiscellaneousUtil.getProperty(properties, CIPHER_TRANSFORMATION_SECRET_CONF_PROPERTY,
				cipherTransformation);
	}
	
	/**
	 * get the primary key store instant
	 * 
	 * @param location
	 *            location of key store
	 * @param storePassword
	 *            password of key store
	 * @param storeType
	 *            key store type
	 * @param provider
	 *            key store provider
	 * @return KeyStore instant
	 */
	private static KeyStore getKeyStore(String location, String storePassword, String storeType,
	                                    String provider) throws CipherToolException {

		File keyStoreFile = new File(location);
		if (!keyStoreFile.exists()) {
			throw new CipherToolException("KeyStore can not be found at ' " + keyStoreFile + " '");
		}
		if (storePassword == null) {
			throw new CipherToolException("KeyStore password can not be null");
		}
		if (storeType == null) {
			throw new CipherToolException("KeyStore Type can not be null");
		}
		try (BufferedInputStream bufferedInputStream = new BufferedInputStream(new FileInputStream(keyStoreFile))) {
			KeyStore keyStore;
			if (provider != null) {
				keyStore = KeyStore.getInstance(storeType, provider);
			} else {
				keyStore = KeyStore.getInstance(storeType);
			}
			keyStore.load(bufferedInputStream, storePassword.toCharArray());
			return keyStore;
		} catch (KeyStoreException | IOException | NoSuchAlgorithmException | CertificateException | NoSuchProviderException e) {
			throw new CipherToolException("Error loading keyStore from ' " + location + " ' ", e);
		}
	}

	public DecryptionProvider getDecryptionProvider() {
		return decryptionProvider;
	}

}
