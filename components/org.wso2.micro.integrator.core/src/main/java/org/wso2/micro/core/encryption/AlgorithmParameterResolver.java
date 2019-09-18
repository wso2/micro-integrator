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

package org.wso2.micro.core.encryption;

import org.wso2.carbon.crypto.api.CryptoException;

import java.security.SecureRandom;
import java.security.spec.AlgorithmParameterSpec;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.IvParameterSpec;

/**
 * This class resolves parameters for related JCE algorithm.
 */
class AlgorithmParameterResolver {

    private static SecureRandom random = new SecureRandom();

    /**
     * Resolves parameters for symmetric algorithms.
     *
     * @param symmetricAlgorithm : JCE standard name of symmetric algorithm.
     * @return algorithm parameters
     * @throws CryptoException
     */
    static AlgorithmParameterSpec resolveSymmetricAlgorithmParameters(String symmetricAlgorithm)
            throws CryptoException {

        String errorMessage = String.format("'%s' symmetric algorithm is not supported.", symmetricAlgorithm);
        if (symmetricAlgorithm.contains(AlgorithmConstants.ENC_METHOD_AES)) {
            byte[] iv = new byte[AlgorithmConstants.AES.DEFAULT_IV_LENGTH];
            random.nextBytes(iv);
            if (symmetricAlgorithm.contains(AlgorithmConstants.AES.GCM_MODE)) {
                return new GCMParameterSpec(AlgorithmConstants.AES.DEFAULT_GCM_TAG_LENGTH, iv);
            } else if (symmetricAlgorithm.contains(AlgorithmConstants.AES.CFB_MODE) || symmetricAlgorithm
                    .contains(AlgorithmConstants.AES.CBC_MODE) || symmetricAlgorithm
                    .contains(AlgorithmConstants.AES.OFB_MODE)) {
                return new IvParameterSpec(iv);
            } else {
                throw new CryptoException(errorMessage);
            }
        } else if (symmetricAlgorithm.contains(AlgorithmConstants.ENC_METHOD_DES)) {
            byte[] iv = new byte[AlgorithmConstants.DES.DEFAULT_IV_LENGTH];
            random.nextBytes(iv);
            return new IvParameterSpec(iv);
        } else {
            throw new CryptoException(errorMessage);
        }
    }
}
