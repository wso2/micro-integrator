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

package org.wso2.micro.core.util;

import com.google.gson.Gson;
import org.apache.axiom.om.util.Base64;

/**
 * Holds ciphertext with related metadata.
 */
public class CipherHolder {

    // Base64 encoded ciphertext.
    private String c;

    // Transformation used for encryption, default is "RSA".
    private String t = "RSA";

    // Thumbprint of the certificate.
    private String tp;

    // Digest used to generate certificate thumbprint.
    private String tpd;

    public String getTransformation() {
        return t;
    }

    public void setTransformation(String transformation) {
        this.t = transformation;
    }

    public String getCipherText() {
        return c;
    }

    public byte[] getCipherBase64Decoded() {
        return Base64.decode(c);
    }

    public void setCipherText(String cipher) {
        this.c = cipher;
    }

    public String getThumbPrint() {
        return tp;
    }

    public void setThumbPrint(String tp) {
        this.tp = tp;
    }

    public String getThumbprintDigest() {
        return tpd;
    }

    public void setThumbprintDigest(String digest) {
        this.tpd = digest;
    }

    /**
     * Function to base64 encode ciphertext and set ciphertext
     *
     * @param cipher
     */
    public void setCipherBase64Encoded(byte[] cipher) {
        this.c = Base64.encode(cipher);
    }

    /**
     * Function to set thumbprint
     *
     * @param tp     thumb print
     * @param digest digest (hash algorithm) used for to create thumb print
     */
    public void setThumbPrint(String tp, String digest) {
        this.tp = tp;
        this.tpd = digest;
    }

    @Override
    public String toString() {
        Gson gson = new Gson();
        return gson.toJson(this);
    }
}
