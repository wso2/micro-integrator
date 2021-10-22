/*
 *  Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *   WSO2 Inc. licenses this file to you under the Apache License,
 *   Version 2.0 (the "License"); you may not use this file except
 *   in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing,
 *   software distributed under the License is distributed on an
 *   "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *   KIND, either express or implied.  See the License for the
 *   specific language governing permissions and limitations
 *   under the License.
 */

package org.wso2.micro.integrator.inbound.endpoint.protocol.file;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemOptions;
import org.apache.commons.vfs2.impl.DefaultFileSystemManager;
import org.apache.commons.vfs2.impl.StandardFileSystemManager;
import org.apache.synapse.commons.vfs.VFSConstants;
import org.apache.synapse.commons.vfs.VFSUtils;
import org.apache.synapse.core.SynapseEnvironment;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.wso2.carbon.inbound.endpoint.protocol.file.FilePollingConsumer;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.Properties;

@RunWith(Parameterized.class)
public class FilePollingConsumerParameterizedTest {

    private String fileUri;
    private String inFileUri;
    private String outFileUri;
    private String expectedResult;
    private String protocol;

    public FilePollingConsumerParameterizedTest(String protocol, String processingUri, String inFileUri,
                                                String outFileUri, String out) {
        this.fileUri = processingUri;
        this.inFileUri = inFileUri;
        this.outFileUri = outFileUri;
        this.expectedResult = out;
        this.protocol = protocol;
    }

    @Parameterized.Parameters
    public static Collection primeNumbers() {
        return Arrays.asList(new Object[][]{
                {"file", "in/a.txt", "in", "out", "out"},
//                {"ftp", "in/a.txt", "in", "out", "out"},
//                {"ftp", "in/a.txt?a=b", "in?a=b", "out", "out"},
//                {"ftp", "in/a.txt", "in", "out?c=d", "out?c=d"},
//                {"ftp", "in/a.txt", "in?a=b", "out?c=d", "out?c=d"},
                {"file", "in/child1/child2/a.txt", "in", "out", "out/child1/child2"}});
//                {"ftp", "in/child1/child2/a.txt", "in", "out", "out/child1/child2"},
//                {"ftp", "in/child1/child2/a.txt", "in?a=b", "out", "out/child1/child2"},
//                {"ftp", "in/child1/child2/a.txt?a=b", "in?a=b", "out?c=d", "out/child1/child2?c=d"}});
    }

    @Test
    public void testOutFileUri() throws Exception {

        Properties vfsProperties = new Properties();

        String basePath = "";
        if ("file".equals(protocol)) {
            basePath = new File(getClass().getClassLoader().getResource("").getFile()).getAbsolutePath() + "/";
        } else if ("ftp".equals(protocol)) {
            basePath = "ftp://localhost/";
        }
        String inFileAbsoluteUri = basePath + inFileUri;
        vfsProperties.put(VFSConstants.TRANSPORT_FILE_FILE_URI, inFileAbsoluteUri);

        //Create PollingConsumer
        Constructor constructor = FilePollingConsumer.class.getConstructor(Properties.class, String.class,
                                                                           SynapseEnvironment.class, long.class);
        FilePollingConsumer pollingConsumer = (FilePollingConsumer) constructor.newInstance(vfsProperties, null,
                                                                                            null, 10);

        //Initialize consumer
        Method initFileCheck = pollingConsumer.getClass().getDeclaredMethod("initFileCheck");
        initFileCheck.setAccessible(true);
        initFileCheck.invoke(pollingConsumer);

        //Resolve file object
        DefaultFileSystemManager fsManager;
        FileSystemOptions fso;
        StandardFileSystemManager fsm = new StandardFileSystemManager();
        fsm.setConfiguration(getClass().getClassLoader().getResource("providers.xml"));
        fsm.init();
        fsManager = fsm;
        String processingFileUri = basePath + fileUri;
        fso = VFSUtils.attachFileSystemOptions(VFSUtils.parseSchemeFileOptions(processingFileUri, vfsProperties),
                                               fsManager);
        FileObject fileObject = fsManager.resolveFile(processingFileUri, fso);

        //Invoke test method
        Class[] paramString = new Class[2];
        paramString[0] = FileObject.class;
        paramString[1] = String.class;

        Method resolveActualOutUrl = pollingConsumer.getClass().getDeclaredMethod("resolveActualOutUrl",
                                                                                  paramString);
        resolveActualOutUrl.setAccessible(true);
        String resolvedOutPath = (String) resolveActualOutUrl.invoke(pollingConsumer, fileObject,
                                                                     basePath + outFileUri);

        Assert.assertEquals(basePath + expectedResult, resolvedOutPath);
    }
}
