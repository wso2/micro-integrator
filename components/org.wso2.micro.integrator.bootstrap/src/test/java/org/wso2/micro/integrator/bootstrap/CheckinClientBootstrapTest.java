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

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.File;
import java.net.MalformedURLException;
import java.nio.file.Paths;

import static org.testng.Assert.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;
import static org.wso2.micro.integrator.bootstrap.Bootstrap.ROOT;

/**
 * Tests functionality of CheckinClientBootstrap class
 */
public class CheckinClientBootstrapTest {
    private CheckinClientBootstrap checkinClientBootstrap;

    /**
     * Create an instance of CheckinClientBootstrap
     */
    @BeforeMethod
    public void setUp() {
        checkinClientBootstrap = new CheckinClientBootstrap();
    }

    /**
     * Test if the file path assigning is successful
     *
     * @throws MalformedURLException if a URL is malformed
     */
    @Test
    public void testAddClassPathEntries() throws MalformedURLException {
        String lib = "lib";
        checkinClientBootstrap.addClassPathEntries();
        File file = Paths
                .get(ROOT + File.separator + lib + File.separator + "core" + File.separator + "WEB-INF" + File.separator
                             + lib + File.separator).toFile();
        assertNotNull(file);
    }

    /**
     * Test if getClassToLoad returns the correct string
     */
    @Test
    public void testGetClassToLoad() {
        assertNotNull(checkinClientBootstrap.getClassToLoad());
        assertEquals(checkinClientBootstrap.getClassToLoad(), "org.wso2.registry.checkin.Client");
    }

    /**
     * Test if getClassToLoad returns the correct string
     */
    @Test
    public void testGetMethodToInvoke() {
        assertNotNull(checkinClientBootstrap.getMethodToInvoke());
        assertEquals(checkinClientBootstrap.getMethodToInvoke(), "start");
    }
}
