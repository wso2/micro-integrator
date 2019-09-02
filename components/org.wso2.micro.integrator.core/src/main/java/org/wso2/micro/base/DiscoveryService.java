/*
 *  Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied. See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.wso2.micro.base;

import java.net.URI;
import javax.xml.namespace.QName;

/**
 * This represents an OSGi service that has WS-Discovery capabilities. A WS-Discovery implementation
 * will add a client of this type as an OSGi service, so that it can be used to discover services.
 */
public interface DiscoveryService {

    /**
     * Method to probe for services.
     *
     * @param types    the port types.
     * @param scopes   the scopes in which to look-up for the service.
     * @param matchBy  the rule used for matching.
     * @param tenantId the identifier of the tenant.
     * @return a list of service endpoints
     * @throws Exception on error
     */
    String[] probe(QName[] types, URI[] scopes, String matchBy, int tenantId) throws Exception;
}