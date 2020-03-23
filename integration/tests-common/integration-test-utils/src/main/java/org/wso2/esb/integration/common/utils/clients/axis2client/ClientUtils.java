/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *   WSO2 Inc. licenses this file to you under the Apache License,
 *   Version 2.0 (the "License"); you may not use this file except
 *   in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing,
 *   software distributed under the License is distributed on an
 *   "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *   KIND, either express or implied. See the License for the
 *   specific language governing permissions and limitations
 *   under the License.
 */

package org.wso2.esb.integration.common.utils.clients.axis2client;

/**
 * Util class to be used by the axis2 clients.
 */
public class ClientUtils {

    private ClientUtils(){
        //Adds a private constructor since this class cannot be instantiated from elsewhere.
    }

    /**
     * Returns the readTimeout to be set depending on the operating system.
     *
     * @return the read timeout in milli seconds.
     */
    static int getReadTimeout() {
        //Increase ReadTimeOut to 3 mins in Windows environments since the operation execution is taking longer in
        // Windows
        if (System.getProperty("os.name").toLowerCase().contains("windows")) {
            return 180000;
        }
        return 45000;
    }
}
