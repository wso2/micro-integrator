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

import org.apache.axiom.om.OMElement;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.rampart.policy.model.KerberosConfig;
import org.wso2.micro.core.Constants;
import org.wso2.micro.integrator.security.SecurityConfigParams;

import javax.xml.namespace.QName;
import java.util.Iterator;

/**
This utility class parses security configuration element and builds Security Config Parameters.
<p/>
* Sample Config
* -------------
* <sec:CarbonSecConfig xmlns:sec="http://www.wso2.org/products/carbon/security">
*         <sec:Trust>
*             <sec:property name="org.wso2.carbon.security.crypto.truststores">
*                 wso2carbon.jks,
*             </sec:property>
*             <sec:property name="org.wso2.carbon.security.crypto.privatestore">wso2carbon.jks</sec:property>
*             <sec:property name="org.wso2.carbon.security.crypto.alias">wso2carbon</sec:property>
*         </sec:Trust>
*         <sec:Authorization>
*             <sec:property name="org.wso2.carbon.security.allowedroles">admin,sys-admin</sec:property>
*         </sec:Authorization>
*         <sec:Kerberos>
*             <sec:property name="service.principal.password" encrypted="true">kuv2MubUUveMyv6GeHrXr9il59ajJIqUI4eoYHcgGKf/BBFOWn96NTjJQI+wYbWjKW6r79S7L7ZzgYeWx7DlGbff5X3pBN2Gh9yV0BHP1E93QtFqR7uTWi141Tr7V7ZwScwNqJbiNoV+vyLbsqKJE7T3nP8Ih9Y6omygbcLcHzg=</sec:property>
*             <sec:property name="service.principal.name">esb/localhost</sec:property>
*         </sec:Kerberos>
* </sec:CarbonSecConfig>
*/
public class SecurityConfigParamBuilder {

    public static final QName SECURITY_CONFIG_QNAME = new QName(Constants.SECURITY_NAMESPACE,
            Constants.CARBON_SEC_CONFIG);

    private static QName propertyQName = new QName(Constants.SECURITY_NAMESPACE,
            Constants.PROPERTY_LABEL);
    private static QName nameQName = new QName(Constants.NAME_LABEL);
    private static QName encryptedQName = new QName(Constants.ENCRYPTED);

    private static QName trustQName = new QName(Constants.SECURITY_NAMESPACE, Constants.TRUST);
    private static QName authorizationQName = new QName(Constants.SECURITY_NAMESPACE,
            Constants.AUTHORIZATION);
    private static QName kerberosQName = new QName(Constants.SECURITY_NAMESPACE, Constants.KERBEROS);

    private static final Log log = LogFactory.getLog(SecurityConfigParamBuilder.class);

    private SecurityConfigParamBuilder(){}

    /**
     * Parse security configuration element and build the Security Config Parameters
     *
     * @param config Config Element
     * @return SecurityConfigParams
     */
    public static SecurityConfigParams getSecurityParams(OMElement config) {

        SecurityConfigParams securityConfigParams = new SecurityConfigParams();

        if (config != null) {
            if (log.isDebugEnabled()) {
                log.debug("Config Element : " + config.toString());
            }

            Iterator iterator = config.getChildElements();

            while (iterator.hasNext()) {
                OMElement configCategoryElem = (OMElement) iterator.next();

                if (authorizationQName.equals(configCategoryElem.getQName())) {
                    Iterator authorizationPropsElem = configCategoryElem.getChildElements();

                    while (authorizationPropsElem.hasNext()) {
                        OMElement authorizationProperty = (OMElement) authorizationPropsElem.next();

                        if (propertyQName.equals(authorizationProperty.getQName())) {
                            String name = authorizationProperty.getAttributeValue(nameQName);
                            String value = authorizationProperty.getText().trim();
                            if (log.isDebugEnabled()) {
                                log.debug("Authorization Config property name : " + name +
                                          " value : " + value);
                            }
                            if (Constants.ALLOWED_ROLES_PARAM_NAME.equals(name)) {
                                securityConfigParams.setAllowedRoles(value);
                            }
                        }
                    }
                }
            }
        }

        return securityConfigParams;
    }

}
