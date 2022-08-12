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

package org.wso2.micro.integrator.bootstrap;

import java.util.ArrayList;
import java.util.List;

/**
 * This is the carbon security manager. We need to deny certain property accesses through
 * security manager. Therefore we had to extend security manager.
 */
public class CarbonSecurityManager extends SecurityManager {

    private List<String> deniedProperties = new ArrayList<String>();
    private static final RuntimePermission MODIFY_THREADGROUP_PERMISSION = new RuntimePermission("modifyThreadGroup");

    public CarbonSecurityManager() {
        super();
        String deniedSystemPropertyString = System.getProperty("denied.system.properties");
        if (deniedSystemPropertyString != null) {
            String[] systemProperties = deniedSystemPropertyString.split(",");

            for (String systemProperty : systemProperties) {
                deniedProperties.add(systemProperty.trim());
            }
        } else {
            throw new IllegalArgumentException("denied.system.properties property is not specified.");
        }

        printDeniedSystemProperties();
    }

    private void printDeniedSystemProperties() {

        for (String restrictedPackage : deniedProperties) {
            System.out.println("Property pattern " + restrictedPackage + " is restricted for tenant code.");
        }
    }

    @Override
    public void checkPropertyAccess(String key) {

        /**
         * If given property matches a property in deniedProperties then
         * call super class's checkPropertiesAccess. This call will success
         * only if all permission is given. Else it will fail.
         */
        for (String restrictedProperty : deniedProperties) {
            if (key.matches(restrictedProperty)) {
                super.checkPropertiesAccess();
            }
        }
        super.checkPropertyAccess(key);
    }

    /**
     * Access check for a thread group. Untrusted code should not be able to do any operations
     * to a thread group (including thread.start()).
     *
     * @param g The thread group.
     */
    public void checkAccess(ThreadGroup g) {

        if (g == null) {
            throw new NullPointerException("thread group can't be null");
        }

        checkPermission(MODIFY_THREADGROUP_PERMISSION);
    }

    /**
     * Access check for a thread. Untrusted code should not be able to do any operations
     * to a thread (including thread.start()).
     *
     * @param t The thread.
     */
    public void checkAccess(Thread t) {

        if (t == null) {
            throw new NullPointerException("thread can't be null");
        }

        checkPermission(MODIFY_THREADGROUP_PERMISSION);
    }

}
