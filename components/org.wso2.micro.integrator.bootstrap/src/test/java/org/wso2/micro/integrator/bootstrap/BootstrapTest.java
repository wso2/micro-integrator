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

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;

/**
 * Tests functionality of Bootstrap class
 */
public class BootstrapTest {
    private Bootstrap bootstrap = new Bootstrap();
    private File libFile;

    /**
     * Test if loadClass throws ClassNotFoundException when trying to load
     * a class that does not exist within the scope to load the rest of the app
     *
     * @throws Exception for failure of file operations or classloading
     */
    @Test(expectedExceptions = ClassNotFoundException.class)
    public void testLoadClassException() throws Exception {
        ClassLoader cl = new URLClassLoader(new URL[0]);
        assertNotNull(cl);
        cl.loadClass("simple.test.class");
    }

    /**
     * Test launch of carbon server.
     * <p>
     * Necessary system properties are set and a test org.wso2.carbon.Main class
     * is created within the test directory to avoid cyclic dependencies
     *
     * @throws IOException for file operation failures
     */
    @Test
    public void testMain() throws IOException {
        libFile = Files.createTempDirectory("libFile").toFile();
        System.setProperty("carbon.internal.lib.dir.path", libFile.getParent());
        Path carbonHome = Files.createTempDirectory("carbonHome");
        System.setProperty("carbon.home", carbonHome.toString());

        assertNull(System.getProperty("carbon.server.status"));
        Bootstrap.main(new String[] {});
        assertEquals(System.getProperty("carbon.server.status"), "up");
    }

    /**
     * Test if addFileUrl method throws MalformedURLException for unknown protocols
     *
     * @throws MalformedURLException if a URL is malformed
     */
    @Test(expectedExceptions = MalformedURLException.class)
    public void testAddFileUrl() throws MalformedURLException {
        URL obj = new URL("fil://path/to/file");
        File libFile = new File(obj.toString());
        bootstrap.addFileUrl(libFile);
    }

    /**
     * Test if addJarFileUrls method throws malformedURLException for unknown protocols
     *
     * @throws MalformedURLException if a URL is malformed
     */
    @Test(expectedExceptions = MalformedURLException.class)
    public void testAddJarFileUrls() throws MalformedURLException {
        URL obj = new URL("fil://path/to/file");
        File libFile = new File(obj.toString());
        bootstrap.addJarFileUrls(libFile);
    }

    /**
     * Test if the getClassToLoad method returns the correct class
     * to load the rest of the app
     */
    @Test
    public void testGetClassToLoad() {
        assertNotNull(bootstrap.getClassToLoad());
        assertEquals(bootstrap.getClassToLoad(), "org.wso2.micro.integrator.server.Main");
    }

    /**
     * Test if the getMethodToInvoke method returns main method
     * This will be invoked by the class specified by getClassToLoad method
     */
    @Test
    public void testGetMethodToInvoke() {
        assertNotNull(bootstrap.getMethodToInvoke());
        assertEquals(bootstrap.getMethodToInvoke(), "main");
    }
}
