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
package org.wso2.micro.integrator.security.user.core.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.micro.integrator.security.user.core.UserStoreException;

import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.DirContext;

public class JNDIUtil {

    private static Log log = LogFactory.getLog(JNDIUtil.class);

    public static void closeContext(DirContext dirContext) throws UserStoreException {
        try {
            if (dirContext != null) {
                dirContext.close();
            }
        } catch (NamingException e) {
            String errorMessage = "Error in closing connection context.";
            log.error(errorMessage, e);
        }
    }

    /**
     * Util method to close the used NamingEnumerations to free up resources.
     *
     * @param namingEnumeration
     */
    public static void closeNamingEnumeration(NamingEnumeration<?> namingEnumeration) {

        if (namingEnumeration != null) {
            try {
                namingEnumeration.close();
            } catch (NamingException e) {
                String errorMessage = "Error in closing NamingEnumeration.";
                log.error(errorMessage, e);
            }
        }

    }

}