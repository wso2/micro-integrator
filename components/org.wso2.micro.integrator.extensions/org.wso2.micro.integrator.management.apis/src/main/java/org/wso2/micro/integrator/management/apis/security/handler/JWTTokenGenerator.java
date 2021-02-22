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

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.wso2.micro.application.deployer.AppDeployerUtils;
import org.wso2.micro.core.util.KeyStoreManager;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Date;

/**
 * This class generates the JWT token with the provided information in JWTTokenInfoDTO
 */
public class JWTTokenGenerator {

    /**
     * Generate JWT Token with JWTTokenInfo object
     *
     * @param jwtToken JWT Token info object
     * @return Serialized JWT token
     * @throws JOSEException
     * @throws NoSuchAlgorithmException
     */
    public String generateJWTToken(JWTTokenInfoDTO jwtToken) throws JOSEException, NoSuchAlgorithmException {

        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(AuthConstants.TOKEN_STORE_KEY_ALGORITHM);
        keyPairGenerator.initialize(Integer.parseInt(JWTConfig.getInstance().getJwtConfigDto().getTokenSize()));
        RSAKey rsaJWK = generateRSAKey(jwtToken, keyPairGenerator); //Currently uses generated key pair

        SignedJWT signedJWT = populateSignedJWTToken(jwtToken, rsaJWK);

        JWSSigner signer = new RSASSASigner(rsaJWK);
        signedJWT.sign(signer);

        return signedJWT.serialize();
    }

    /**
     * Builds RSAKey with generated key pair
     *
     * @param jwtTokenDTO      JWT Token info object
     * @param keyPairGenerator keyPairGenerator
     * @return RSAKey built RSA Key which can be used to sign
     */
    private RSAKey generateRSAKey(JWTTokenInfoDTO jwtTokenDTO, KeyPairGenerator keyPairGenerator) {

        KeyPair keyPair = keyPairGenerator.generateKeyPair();
        jwtTokenDTO.setGeneratedKeyPair(keyPair);
        RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();
        RSAPrivateKey privateKey = (RSAPrivateKey) keyPair.getPrivate();
        RSAKey.Builder builder = new RSAKey.Builder(publicKey)
                .privateKey(privateKey);
        RSAKey rsaKey = builder.keyID(jwtTokenDTO.getToken()).build();
        jwtTokenDTO.setRsaKey(rsaKey);
        return rsaKey;
    }

    /**
     * Builds RSAKey using key store.
     *
     * @param jwtTokenDTO      token info object
     * @param keyPairGenerator key pair generator
     * @return RSAKey built RSA Key which can be used to sign
     * @throws Exception
     */
    private RSAKey generateRSAKeyWithKeyStore(JWTTokenInfoDTO jwtTokenDTO, KeyPairGenerator keyPairGenerator) throws Exception {

        KeyStore keystore = KeyStoreManager.getInstance(AppDeployerUtils.getTenantId()).getPrimaryKeyStore();
        KeyPair keyPair = keyPairGenerator.generateKeyPair();
        RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();
        RSAPrivateKey privateKey = (RSAPrivateKey) keyPair.getPrivate();
        RSAKey.Builder builder = new RSAKey.Builder(publicKey)
                .privateKey(privateKey).keyStore(keystore);
        return builder.keyID(jwtTokenDTO.getToken()).build();
    }

    /**
     * Populate JWT Token with defined claim set
     *
     * @param jwtTokenDTO token info object
     * @param rsaJWK      RSAKey
     * @return Signable JWT object
     */
    private SignedJWT populateSignedJWTToken(JWTTokenInfoDTO jwtTokenDTO, RSAKey rsaJWK) {

        JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
                .subject(jwtTokenDTO.getUsername())
                .issuer(jwtTokenDTO.getIssuer())
                .expirationTime(new Date(jwtTokenDTO.getExpiry()))
                .claim("scope", jwtTokenDTO.getScope())
                .build();
        //Add additional claims if needed
        return new SignedJWT(
                new JWSHeader.Builder(JWSAlgorithm.RS256).keyID(rsaJWK.getKeyID()).build(),
                claimsSet);
    }

}
