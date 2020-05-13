/*
Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.micro.integrator.initializer.handler.transaction.security;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.micro.integrator.initializer.handler.transaction.TransactionConstants;
import org.wso2.micro.integrator.initializer.handler.transaction.exception.TransactionCounterException;
import org.wso2.micro.integrator.initializer.handler.transaction.exception.TransactionCounterInitializationException;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.InvalidKeyException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.xml.bind.DatatypeConverter;

/**
 * This class will provide the required methods to encrypt a given value.
 */
public class CryptoUtil {

    private static final Log LOG = LogFactory.getLog(CryptoUtil.class);

    private CryptoUtil() {
    }

    /**
     * Initializes the Cipher.
     *
     * @return cipher.
     * @throws TransactionCounterInitializationException - when something goes wrong while initializing the cipher.
     */
    public static Cipher initializeCipher() throws TransactionCounterInitializationException {
        Cipher cipher;
        KeyStore primaryKeyStore = getKeyStore(getAbsolutePathToKeyStoreLocation(),
                                               TransactionConstants.KEYSTORE_PASSWORD,
                                               TransactionConstants.KEY_TYPE);
        try {
            Certificate certs = primaryKeyStore.getCertificate(TransactionConstants.KEY_ALIAS);
            cipher = Cipher.getInstance(TransactionConstants.ENCRYPTION_ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, certs);
            LOG.debug(
                    "Successfully initialized the Cipher to be used in the transaction count encryption process in "
                            + "the Transaction Count Handler component.");
        } catch (KeyStoreException | NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException e) {
            throw new TransactionCounterInitializationException("Error initializing Cipher ", e);
        }
        return cipher;
    }

    /**
     * Get the keyStore located in the given location.
     *
     * @param location      location of the keyStore.
     * @param storePassword password of the keyStore.
     * @param storeType     type of the KeyStore.
     * @return - KeyStore.
     * @throws TransactionCounterInitializationException - when something goes wrong while loading the KeyStore for the
     *                                                   given location.
     */
    private static KeyStore getKeyStore(String location, String storePassword, String storeType)
            throws TransactionCounterInitializationException {
        try (BufferedInputStream bufferedInputStream = new BufferedInputStream(new FileInputStream(location));) {
            KeyStore keyStore = KeyStore.getInstance(storeType);
            keyStore.load(bufferedInputStream, storePassword.toCharArray());
            return keyStore;
        } catch (KeyStoreException | IOException | NoSuchAlgorithmException | CertificateException e) {
            throw new TransactionCounterInitializationException("Error loading keyStore from ' " + location + " ' ", e);
        }
    }

    /**
     * Encrypt the plain text value.
     *
     * @param cipher         init cipher
     * @param plainTextValue transaction count in plain text.
     * @return encrypted transaction count
     * @throws TransactionCounterException
     */
    public static String doEncryption(Cipher cipher, String plainTextValue) throws TransactionCounterException {
        String encodedValue;
        try {
            byte[] encryptedPassword = cipher.doFinal(plainTextValue.getBytes());
            encodedValue = DatatypeConverter.printBase64Binary(encryptedPassword);
        } catch (BadPaddingException | IllegalBlockSizeException e) {
            throw new TransactionCounterException("Error encrypting transaction count ", e);
        }
        return encodedValue;
    }

    private static String getAbsolutePathToKeyStoreLocation() {
        Path keyStoreLocation = Paths.get(TransactionConstants.DEFAULT_SECURITY_RESOURCE_DIR_PATH,
                                          TransactionConstants.TRUSTSTORE_FILE);
        return keyStoreLocation.toString();
    }
}
