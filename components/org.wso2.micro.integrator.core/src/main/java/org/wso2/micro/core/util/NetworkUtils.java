package org.wso2.micro.core.util;
/*
 * Copyright 2005,2006 WSO2, Inc. http://www.wso2.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.net.SocketException;

/**
 * Utility classes for networking
 */
public final class NetworkUtils {

    /**
     * what we return when we cannot determine our hostname.
     * We use this rather than 'localhost' as if DNS is very confused,
     * localhost can map to different machines than "self".
     */
    public static final String LOCALHOST = "127.0.0.1";

    /**
     * loopback address in IPV6
     */
    public static final String LOCALHOST_IPV6 = "0:0:0:0:0:0:0:1";

    private static String hostName;

    /**
     * Host name used for mgt console
     */
    private static String mgtHostName;

    /**
     * keep this uninstantiable.
     */
    private NetworkUtils() {
    }

    public static void init(String hostName, String mgtHostName) throws SocketException {
        if (hostName == null) {
            NetworkUtils.hostName = org.apache.axis2.util.Utils.getIpAddress();
        } else {
            NetworkUtils.hostName = hostName;
        }
        if (mgtHostName == null) {
            NetworkUtils.mgtHostName = org.apache.axis2.util.Utils.getIpAddress();
        } else {
            NetworkUtils.mgtHostName = mgtHostName;
        }
    }

    /**
     * Get the string defining the hostname of the system, as taken from
     * the default network adapter of the system. There is no guarantee that
     * this will be fully qualified, or that it is the hostname used by external
     * machines to access the server.
     * If we cannot determine the name, then we return the default hostname,
     * which is defined by {@link #LOCALHOST}
     *
     * @return a string name of the host.
     */
    public static String getLocalHostname() throws SocketException {
        if (hostName == null) {
            hostName = org.apache.axis2.util.Utils.getIpAddress();
        }
        return hostName;
    }

    /**
     * Returns the host name to be used by the Management Console
     * @return host name to be used by the Management Console
     */
    public static String getMgtHostName(){
        return mgtHostName;
    }
}
