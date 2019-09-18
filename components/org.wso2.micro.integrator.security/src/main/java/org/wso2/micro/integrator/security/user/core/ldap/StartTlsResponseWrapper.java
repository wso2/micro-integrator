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
package org.wso2.micro.integrator.security.user.core.ldap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;
import javax.naming.ldap.StartTlsResponse;

/**
 * This is a wrapper class of StartTlsResponse class. Wrapped the StartTlsResponse with a reference counter
 * because, sub contexts can be initiated from main LdapContext. So need to use one TLS connection until
 * all the contexts get close.
 */
public class StartTlsResponseWrapper {

    private static Log log = LogFactory.getLog(StartTlsResponseWrapper.class);
    /* Keep a reference counter due to sub contexts can be created out of main ldapContext. Until we close the last
    context we need to keep TLS response. */
    private int referenceCounter = 0;
    private StartTlsResponse startTlsResponse;

    public StartTlsResponseWrapper(StartTlsResponse startTlsResponse) {

        this.startTlsResponse = startTlsResponse;
    }

    /**
     * Method to close the used startTLS Response.
     */
    public void close() {

        referenceCounter--;
        if (referenceCounter == 0) {
            try {
                this.startTlsResponse.close();
                if (log.isDebugEnabled()) {
                    log.debug("Closing the StartTLS connection with LDAP server");
                }
            } catch (IOException e) {
                log.error("Error occurred when closing StartTLS connection.", e);
            }
        }
    }

    /**
     * Method to increase the reference counter.
     */
    public void incrementReferenceCounter() {

        referenceCounter++;
    }
}
