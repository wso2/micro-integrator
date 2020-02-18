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

package org.wso2.micro.integrator.crypto.provider;

import org.apache.commons.lang.RandomStringUtils;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.File;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.PublicKey;
import javax.crypto.Cipher;

import static org.testng.Assert.assertEquals;

public class KeyStoreBasedInternalCryptoProviderTest {

    public static final String KEY_STORE_FILE_NAME = "keystore.jks";
    public static final String KEY_STORE_PASSWORD = "keystore-password";
    public static final String KEY_ALIAS = "key-alias";
    public static final String KEY_PASSWORD = "key-password";
    public static final String CHAR_ENCODING_UTF_8 = "UTF-8";
    KeyStoreBasedInternalCryptoProvider jksCryptoProvider;
    PublicKey publicKey;
    PrivateKey privateKey;
    private KeyStore keyStore;

    /**
     * This data provider provides an array of encryption algorithms.
     *
     * @return
     */
    @DataProvider(name = "encryptionAlgorithms")
    public static Object[][] getEncryptionAlgorithms() {

        return new Object[][] { { "RSA" } };
    }

    @BeforeClass
    public void init() throws Exception {

        keyStore = getKeyStore();
        publicKey = keyStore.getCertificate(KEY_ALIAS).getPublicKey();
        privateKey = (PrivateKey) keyStore.getKey(KEY_ALIAS, KEY_PASSWORD.toCharArray());
        jksCryptoProvider = new KeyStoreBasedInternalCryptoProvider(keyStore, KEY_ALIAS, KEY_PASSWORD);
    }

    @Test(dataProvider = "encryptionAlgorithms")
    public void testDecrypting(String algorithm) throws Exception {

        int plaintextLength = 50;
        String plaintext = RandomStringUtils.random(plaintextLength);

        Cipher cipher = Cipher.getInstance(algorithm);
        cipher.init(Cipher.ENCRYPT_MODE, publicKey);

        byte[] ciphertext = cipher.doFinal(plaintext.getBytes(CHAR_ENCODING_UTF_8));

        assertEquals(new String(jksCryptoProvider.decrypt(ciphertext, algorithm, null), CHAR_ENCODING_UTF_8),
                     plaintext);
    }

    @Test(dataProvider = "encryptionAlgorithms")
    public void testEncrypting(String algorithm) throws Exception {

        int plaintextLength = 50;
        String plaintext = RandomStringUtils.random(plaintextLength);

        byte[] ciphertext = jksCryptoProvider.encrypt(plaintext.getBytes(CHAR_ENCODING_UTF_8), algorithm, null);

        Cipher cipher = Cipher.getInstance(algorithm);
        cipher.init(Cipher.DECRYPT_MODE, privateKey);

        assertEquals(new String(cipher.doFinal(ciphertext), CHAR_ENCODING_UTF_8), plaintext);
    }

    private KeyStore getKeyStore() throws Exception {

        KeyStore keyStore = KeyStore.getInstance("JKS");
        keyStore.load(this.getClass().getResourceAsStream("/" + KEY_STORE_FILE_NAME), KEY_STORE_PASSWORD.toCharArray());
        return keyStore;
    }

}
