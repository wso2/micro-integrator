/*
 * Copyright (c) 2023, WSO2 LLC. (http://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.micro.integrator.initializer.synapse.deployer;

import org.junit.Test;
import org.wso2.micro.integrator.initializer.deployment.synapse.deployer.FileRegistryResourceDeployer;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class FileRegistryResourceDeployerTest {

    /**
     * Test the traverseDirectory method of the FileRegistryResourceDeployer class
     * @throws IOException
     */
    @Test
    public void testTraverseDirectory() throws IOException {
        // Create a temporary directory and some files inside it
        String tempDirName = "tempDir";
        File tempDir = new File(tempDirName);
        tempDir.mkdir();

        String fileName1 = "file1.txt";
        File tempFile1 = new File(tempDir, fileName1);
        FileWriter writer1 = new FileWriter(tempFile1);
        writer1.write("test1");
        writer1.close();
        String fileName2 = "file2.mp4";
        File tempFile2 = new File(tempDir, fileName2);
        FileWriter writer2 = new FileWriter(tempFile2);
        writer2.write("test2");
        writer2.close();

        File metaDir = new File(tempDir, ".meta");
        metaDir.mkdir();
        File tempFile1Meta = new File(metaDir, "~file1.txt.xml");
        FileWriter writer3 = new FileWriter(tempFile1Meta);
        String mediaType1 = "text/plain";
        writer3.write("<resource name=\"file1.txt\" isCollection=\"false\" path=\"/_system/config/custom/testnew/file1.txt\" registryUrl=\"https://localhost:9443/registry\" status=\"added\">\n" +
                "<mediaType>" + mediaType1 + "</mediaType>\n" +
                "</resource>");
        writer3.close();
        File tempFile2Meta = new File(metaDir, "~file2.mp4.xml");
        FileWriter writer4 = new FileWriter(tempFile2Meta);
        String mediaType2 = "video/mp4";
        writer4.write("<resource name=\"file2.mp4\" isCollection=\"false\" path=\"/_system/config/custom/testnew/file2.mp4\" registryUrl=\"https://localhost:9443/registry\" status=\"added\">\n" +
                "<mediaType>" + mediaType2 + "</mediaType>\n" +
                "</resource>");
        writer4.close();

        File subDir = new File(tempDir, "subDir");
        subDir.mkdir();

        String fileName3 = "file3.txt";
        File tempFile3 = new File(subDir, fileName3);
        FileWriter writer5 = new FileWriter(tempFile3);
        writer5.write("test1");
        writer5.close();
        String fileName4 = "file4.mp4";
        File tempFile4 = new File(subDir, fileName4);
        FileWriter writer6 = new FileWriter(tempFile4);
        writer6.write("test2");
        writer6.close();

        File metaSubDir = new File(subDir, ".meta");
        metaSubDir.mkdir();
        File tempFile3Meta = new File(metaSubDir, "~file3.txt.xml");
        FileWriter writer7 = new FileWriter(tempFile3Meta);
        String mediaType3 = "text/plain";
        writer7.write("<resource name=\"file3.txt\" isCollection=\"false\" path=\"/_system/config/custom/testnew/file1.txt\" registryUrl=\"https://localhost:9443/registry\" status=\"added\">\n" +
                "<mediaType>" + mediaType3 + "</mediaType>\n" +
                "</resource>");
        writer7.close();
        File tempFile4Meta = new File(metaSubDir, "~file4.mp4.xml");
        FileWriter writer8 = new FileWriter(tempFile4Meta);
        String mediaType4 = "video/mp4";
        writer8.write("<resource name=\"file4.mp4\" isCollection=\"false\" path=\"/_system/config/custom/testnew/file2.mp4\" registryUrl=\"https://localhost:9443/registry\" status=\"added\">\n" +
                "<mediaType>" + mediaType4 + "</mediaType>\n" +
                "</resource>");
        writer8.close();

        ArrayList<FileRegistryResourceDeployer.SubFileInfo> fileList = FileRegistryResourceDeployer.traverseDirectory(tempDir);
        assertEquals(4, fileList.size()); // Only two non-excluded files
        for (FileRegistryResourceDeployer.SubFileInfo subFileInfo : fileList) {
            if (subFileInfo.getFile().getName().equals(fileName1)) {
                assertEquals(mediaType1, subFileInfo.getMediaType());
            } else if (subFileInfo.getFile().getName().equals(fileName2)) {
                assertEquals(mediaType2, subFileInfo.getMediaType());
            } else if (subFileInfo.getFile().getName().equals(fileName3)) {
                assertEquals(mediaType3, subFileInfo.getMediaType());
            } else if (subFileInfo.getFile().getName().equals(fileName4)) {
                assertEquals(mediaType4, subFileInfo.getMediaType());
            } else {
                fail("Unexpected file found");
            }
        }
        tempFile1Meta.delete();
        tempFile2Meta.delete();
        metaDir.delete();
        tempFile1.delete();
        tempFile2.delete();
        tempDir.delete();
        tempFile3Meta.delete();
        tempFile4Meta.delete();
        metaSubDir.delete();
        tempFile3.delete();
        tempFile4.delete();
        subDir.delete();

    }

    /**
     * Test the isFileIgnored method of the FileRegistryResourceDeployer class
     */
    @Test
    public void testIsFileIgnored() {
        // Create a temporary directory and some files inside it
        File tempDir = new File("tempDir");
        tempDir.mkdir();
        File metaDir = new File(tempDir, ".meta");
        metaDir.mkdir();
        File tempFile1 = new File(tempDir, "file1.txt");
        File tempFile2 = new File(metaDir, "file2.mp4");

        assertTrue(FileRegistryResourceDeployer.isFileIgnored(metaDir));
        assertFalse(FileRegistryResourceDeployer.isFileIgnored(tempDir));
        assertFalse(FileRegistryResourceDeployer.isFileIgnored(tempFile1));
        assertFalse(FileRegistryResourceDeployer.isFileIgnored(tempFile2));

        // Clean up the temporary directory and files
        tempFile2.delete();
        tempFile1.delete();
        metaDir.delete();
        tempDir.delete();
    }

}
