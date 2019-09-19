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

import org.testng.annotations.Test;

import java.security.AccessControlException;

import static org.mockito.Mockito.mock;
import static org.testng.Assert.assertNull;

/**
 * Tests functionality of CarbonSecurityManager class
 */
public class CarbonSecurityManagerTest {
    private CarbonSecurityManager carbonSecurityManager;

    /**
     * Test if creation of an instance of CarbonSecurityManager throws
     * IllegalArgumentException when denied.system.properties is not specified
     */
    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testCarbonSecurityManager() {
        carbonSecurityManager = new CarbonSecurityManager();
    }

    /**
     * Test testCheckPropertyAccess throws AccessControlException for a denied property
     */
    @Test(expectedExceptions = AccessControlException.class)
    public void testCheckPropertyAccessDeniedProperty() {
        String key = "mockDeniedProperty";
        System.setProperty("denied.system.properties", key);
        carbonSecurityManager = new CarbonSecurityManager();
        carbonSecurityManager.checkPropertyAccess(key);
    }

    /**
     * Test if checkAccessThread method checks permission for a Thread
     */
    @Test(expectedExceptions = AccessControlException.class)
    public void testCheckAccessThread() {
        System.setProperty("denied.system.properties", "mockDeniedProperty");
        carbonSecurityManager = new CarbonSecurityManager();
        Thread thread = mock(Thread.class);
        carbonSecurityManager.checkAccess(thread);
    }

    /**
     * Test if checkAccessThread method checks permission for a ThreadGroup
     */
    @Test(expectedExceptions = AccessControlException.class)
    public void testCheckAccessThreadGroup() {
        System.setProperty("denied.system.properties", "mockDeniedProperty");
        carbonSecurityManager = new CarbonSecurityManager();
        ThreadGroup threadGroup = mock(ThreadGroup.class);
        carbonSecurityManager.checkAccess(threadGroup);
    }

    /**
     * Test if checkAccessThread handles a null Thread
     */
    @Test(expectedExceptions = NullPointerException.class)
    public void testCheckAccessNullThread() {
        System.setProperty("denied.system.properties", "mockDeniedProperty");
        carbonSecurityManager = new CarbonSecurityManager();
        Thread thread = null;
        assertNull(thread);
        carbonSecurityManager.checkAccess(thread);
    }

    /**
     * Test if checkAccessThread handles a null ThreadGroup
     */
    @Test(expectedExceptions = NullPointerException.class)
    public void testCheckAccessNullThreadGroup() {
        System.setProperty("denied.system.properties", "mockDeniedProperty");
        carbonSecurityManager = new CarbonSecurityManager();
        ThreadGroup threadGroup = null;
        assertNull(threadGroup);
        carbonSecurityManager.checkAccess(threadGroup);
    }
}
