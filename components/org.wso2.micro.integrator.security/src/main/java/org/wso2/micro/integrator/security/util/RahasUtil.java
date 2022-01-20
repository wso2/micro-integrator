/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.micro.integrator.security.util;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axis2.description.Parameter;
import org.apache.rahas.impl.AbstractIssuerConfig;
import org.apache.rahas.impl.SCTIssuerConfig;
import org.apache.rahas.impl.TokenCancelerConfig;
import org.apache.rahas.impl.TokenIssuerUtil;

import javax.xml.namespace.QName;
import java.util.Enumeration;
import java.util.Properties;


public class RahasUtil {

    private RahasUtil(){}

    public static Parameter getSCTIssuerConfigParameter(String cryptoImpl,
                                                        Properties cryptoProperties,
                                                        int keyComputation,
                                                        String proofKeyType,
                                                        boolean addRequestedAttachedRef,
                                                        boolean addRequestedUnattachedRef) throws Exception {

        if (cryptoImpl == null || "".equals(cryptoImpl)) {
            throw new Exception("Crypto impl missing");
        }

        OMFactory fac = OMAbstractFactory.getOMFactory();
        OMElement paramElem = fac.createOMElement(new QName("parameter"), null);
        paramElem.addAttribute(fac.createOMAttribute("name",
                null,
                SCTIssuerConfig.SCT_ISSUER_CONFIG.
                        getLocalPart()));

        paramElem.addAttribute(fac.createOMAttribute("type",
                null, Integer.toString(Parameter.OM_PARAMETER)));

        OMElement elem = fac.createOMElement(
                SCTIssuerConfig.SCT_ISSUER_CONFIG, paramElem);

        OMElement cryptoPropElem = fac.createOMElement(
                AbstractIssuerConfig.CRYPTO_PROPERTIES, elem);
        OMElement cryptoElem = fac.createOMElement(
                AbstractIssuerConfig.CRYPTO, cryptoPropElem);
        cryptoElem.addAttribute(fac.createOMAttribute("provider", null,
                cryptoImpl));

        Enumeration keysEnum = cryptoProperties.keys();
        while (keysEnum.hasMoreElements()) {
            String key = (String) keysEnum.nextElement();
            OMElement prop = fac.createOMElement(new QName("property"), cryptoElem);
            prop.addAttribute(fac.createOMAttribute("name", null, key));
            prop.setText(cryptoProperties.getProperty(key));
        }

        if (!(keyComputation == AbstractIssuerConfig.KeyComputation.KEY_COMP_PROVIDE_ENT ||
                keyComputation == AbstractIssuerConfig.KeyComputation.KEY_COMP_USE_OWN_KEY ||
                keyComputation == AbstractIssuerConfig.KeyComputation.KEY_COMP_USE_REQ_ENT)) {

            keyComputation = AbstractIssuerConfig.KeyComputation.KEY_COMP_USE_OWN_KEY;
        }

        OMElement keyCompElem = fac.createOMElement(
                AbstractIssuerConfig.KeyComputation.KEY_COMPUTATION, elem);
        keyCompElem.setText(Integer.toString(keyComputation));

        if (proofKeyType == null || "".equals(proofKeyType)) {
            proofKeyType = TokenIssuerUtil.BINARY_SECRET;
        } else if (!(TokenIssuerUtil.BINARY_SECRET.equals(proofKeyType)) ||
                TokenIssuerUtil.ENCRYPTED_KEY.equals(proofKeyType)) {
            throw new Exception("Invalid proof token type configuration : " + proofKeyType);
        }

        OMElement proofKeyTypeElem = fac.createOMElement(AbstractIssuerConfig.PROOF_KEY_TYPE, elem);
        proofKeyTypeElem.setText(proofKeyType);

        if (addRequestedAttachedRef) {
            fac.createOMElement(AbstractIssuerConfig.ADD_REQUESTED_ATTACHED_REF, elem);
        }

        if (addRequestedUnattachedRef) {
            fac.createOMElement(AbstractIssuerConfig.ADD_REQUESTED_UNATTACHED_REF, elem);
        }

        Parameter param = new Parameter();
        param.setName(SCTIssuerConfig.SCT_ISSUER_CONFIG.getLocalPart());
        param.setParameterType(Parameter.OM_PARAMETER);
        param.setValue(paramElem);
        param.setParameterElement(paramElem);
        return param;
    }

    public static Parameter getTokenCancelerConfigParameter() {

        OMFactory fac = OMAbstractFactory.getOMFactory();
        OMElement paramElem = fac.createOMElement(new QName("parameter"), null);
        paramElem.addAttribute(fac.createOMAttribute("name",
                null,
                TokenCancelerConfig.TOKEN_CANCELER_CONFIG.
                        getLocalPart()));
        paramElem.addAttribute(fac.createOMAttribute("type",
                null, Integer.toString(Parameter.OM_PARAMETER).
                        toString()));

        fac.createOMElement(TokenCancelerConfig.TOKEN_CANCELER_CONFIG,
                paramElem);
        Parameter param = new Parameter();
        param.setName(TokenCancelerConfig.TOKEN_CANCELER_CONFIG.getLocalPart());
        param.setParameterElement(paramElem);
        param.setValue(paramElem);
        param.setParameterType(Parameter.OM_PARAMETER);
        return param;
    }
}
