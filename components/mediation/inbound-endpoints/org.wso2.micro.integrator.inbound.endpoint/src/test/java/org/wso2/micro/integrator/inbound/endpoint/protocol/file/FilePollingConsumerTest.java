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

import org.apache.synapse.commons.vfs.VFSConstants;
import org.junit.Assert;
import org.junit.Test;
import org.wso2.carbon.inbound.endpoint.protocol.file.FilePollingConsumer;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.Objects;
import java.util.Properties;

public class FilePollingConsumerTest {

    /**
     * transport.vfs.FileURI = <in_location>
     *
     * @throws IOException if an error occurs while creating the required directory structure
     */
    @Test
    public void testPollingFilesInParent() throws IOException {

        String path = getInFilePath("testFilesInParent");
        createFilesInPath(path);
        TestFileInjectHandler fileInjectHandler = poll(path, ".*.txt", null, null, null, false);
        Assert.assertEquals(2, fileInjectHandler.getFileNames().size());
        Assert.assertFalse("Source file is not deleted.", (Files.exists(Paths.get(path + "/a.txt"))));
    }

    /**
     * transport.vfs.FileURI = <in_location>/*
     *
     * @throws IOException if an error occurs while creating the required directory structure
     */
    @Test
    public void testPollingFilesInParentWithSubDirectorySupport() throws IOException {

        String path = getInFilePath("testFilesInParent");
        createFilesInPath(path);
        TestFileInjectHandler fileInjectHandler = poll(modifyUriToIncludeSubDirectories(path), ".*.txt", null, null,
                                                       null, false);
        Assert.assertEquals(2, fileInjectHandler.getFileNames().size());
    }

    /**
     * transport.vfs.FileURI = <in_location>
     * transport.vfs.ActionAfterProcess = MOVE
     * transport.vfs.MoveAfterProcess = <out_location>
     *
     * @throws IOException if an error occurs while creating the required directory structure
     */
    @Test
    public void testMoveAfterPollingFilesInParent() throws IOException {

        String path = getInFilePath("testFilesInParent");
        createFilesInPath(path);
        String outPath = getOutFilePath("testFilesInParent");
        TestFileInjectHandler fileInjectHandler = poll(path, ".*.txt", outPath, null, null, false);
        Assert.assertEquals(2, fileInjectHandler.getFileNames().size());
        Assert.assertTrue("a.txt has not been moved correctly.", (Files.exists(Paths.get(outPath + "/a.txt"))));
        Assert.assertTrue("b.txt has not been moved correctly.", (Files.exists(Paths.get(outPath + "/b.txt"))));
    }

    /**
     * transport.vfs.FileURI = <in_location>
     * transport.vfs.ActionAfterFailure = MOVE
     * transport.vfs.MoveAfterFailure = <fail_location>
     *
     * @throws IOException if an error occurs while creating the required directory structure
     */
    @Test
    public void testMoveAfterFailurePollingFilesInParent() throws IOException {

        String path = getInFilePath("testFilesInParent");
        createFilesInPath(path);
        String failPath = getFailFilePath("testFilesInParent");
        clearPath(failPath);
        TestFileInjectHandler fileInjectHandler = poll(path, ".*.txt", null, failPath, null, true);
        Assert.assertEquals(2, fileInjectHandler.getFileNames().size());
        Assert.assertTrue("a.txt has not been moved correctly.", (Files.exists(Paths.get(failPath + "/a.txt"))));
        Assert.assertTrue("b.txt has not been moved correctly.", (Files.exists(Paths.get(failPath + "/b.txt"))));
    }


    /**
     * transport.vfs.FileURI = <in_location>/*
     *
     * @throws IOException if an error occurs while creating the required directory structure
     */
    @Test
    public void testPollingFilesInChild() throws IOException {

        String inPath = getInFilePath("testFilesInChildren");
        createFilesInPath(inPath);
        String child1PathString = inPath + "/child1/";
        Files.createDirectories(Paths.get(child1PathString));
        createFilesInPath(child1PathString);

        String child2PathString = inPath + "/child2/";
        Files.createDirectories(Paths.get(child2PathString));
        createFilesInPath(child2PathString);
        TestFileInjectHandler fileInjectHandler = poll(modifyUriToIncludeSubDirectories(inPath), ".*",
                                                       null, null, null, false);
        Assert.assertEquals(6, fileInjectHandler.getFileNames().size());
    }

    /**
     * This is written to test backward compatibility.
     * transport.vfs.FileURI = <in_location>
     *
     * @throws IOException if an error occurs while creating the required directory structure If an error occurs during reading files
     */
    @Test
    public void testPollingFilesSkippingChildren() throws IOException {

        String inPath = getInFilePath("testFilesInChildren");
        createFilesInPath(inPath);
        String child1PathString = inPath + "/child1/";
        Files.createDirectories(Paths.get(child1PathString));
        createFilesInPath(child1PathString);

        String child2PathString = inPath + "/child2/";
        Files.createDirectories(Paths.get(child2PathString));
        createFilesInPath(child2PathString);
        TestFileInjectHandler fileInjectHandler = poll(inPath, ".*", null, null, null, false);
        Assert.assertEquals(2, fileInjectHandler.getFileNames().size());
    }

    /**
     * transport.vfs.FileURI = <in_location>/*
     * transport.vfs.ActionAfterProcess = MOVE
     * transport.vfs.MoveAfterProcess = <out_location>
     *
     * @throws IOException if an error occurs while creating the required directory structure
     */
    @Test
    public void testMoveAfterPollingFilesInChild() throws IOException {

        String inPath = getInFilePath("testFilesInChildren");
        createFilesInPath(inPath);
        String child1PathString = inPath + "/child1/";
        Files.createDirectories(Paths.get(child1PathString));
        createFilesInPath(child1PathString);

        String child2PathString = inPath + "/child2/";
        Files.createDirectories(Paths.get(child2PathString));
        createFilesInPath(child2PathString);
        String outPath = getOutFilePath("testFilesInChildren");
        TestFileInjectHandler fileInjectHandler = poll(modifyUriToIncludeSubDirectories(inPath), ".*", outPath, null,
                                                       null, false);
        Assert.assertEquals(6, fileInjectHandler.getFileNames().size());
        Assert.assertTrue("a.txt has not been moved correctly.", (Files.exists(Paths.get(outPath + "/a.txt"))));
        Assert.assertTrue("b.txt has not been moved correctly.", (Files.exists(Paths.get(outPath + "/b.txt"))));
        Assert.assertFalse("b.txt has not been moved correctly.", (Files.exists(Paths.get(outPath + "/child1"))));
        Assert.assertFalse("b.txt has not been moved correctly.", (Files.exists(Paths.get(outPath + "/child2"))));
    }

    /**
     * transport.vfs.FileURI = <in_location>/*
     * transport.vfs.ActionAfterFailure = MOVE
     * transport.vfs.MoveAfterFailure = <fail_location>
     *
     * @throws IOException if an error occurs while creating the required directory structure
     */
    @Test
    public void testMoveAfterFailurePollingFilesInChild() throws IOException {

        String inPath = getInFilePath("testFilesInChildren");
        createFilesInPath(inPath);

        String child1PathString = inPath + "/child1/";
        Files.createDirectories(Paths.get(child1PathString));
        createFilesInPath(child1PathString);

        String child2PathString = inPath + "/child2/";
        Files.createDirectories(Paths.get(child2PathString));
        createFilesInPath(child2PathString);

        String failPath = getFailFilePath("testFilesInChildren");
        TestFileInjectHandler fileInjectHandler = poll(modifyUriToIncludeSubDirectories(inPath), ".*", null, failPath,
                                                       null, true);
        Assert.assertEquals(6, fileInjectHandler.getFileNames().size());
        Assert.assertTrue("a.txt has not been moved correctly.", (Files.exists(Paths.get(failPath + "/a.txt"))));
        Assert.assertTrue("b.txt has not been moved correctly.", (Files.exists(Paths.get(failPath + "/b.txt"))));
        Assert.assertFalse("b.txt has not been moved correctly.", (Files.exists(Paths.get(failPath + "/child1"))));
        Assert.assertFalse("b.txt has not been moved correctly.", (Files.exists(Paths.get(failPath + "/child2"))));
    }

    /**
     * transport.vfs.FileURI = <in_location>/*
     * transport.vfs.ActionAfterProcess = MOVE
     * transport.vfs.MoveAfterProcess = <out_location>/*
     *
     * @throws IOException if an error occurs while creating the required directory structure
     */
    @Test
    public void testMoveAfterPollingFilesInChildWithWildCardForMove() throws IOException {

        String inPath = getInFilePath("testFilesInChildren");
        createFilesInPath(inPath);
        String child1PathString = inPath + "/child1/";
        Files.createDirectories(Paths.get(child1PathString));
        createFilesInPath(child1PathString);

        String child2PathString = inPath + "/child2/";
        Files.createDirectories(Paths.get(child2PathString));
        createFilesInPath(child2PathString);
        String outPath = getOutFilePath("testFilesInChildren");
        TestFileInjectHandler fileInjectHandler = poll(modifyUriToIncludeSubDirectories(inPath), ".*",
                                                       modifyUriToIncludeSubDirectories(outPath), null, null, false);
        Assert.assertEquals(6, fileInjectHandler.getFileNames().size());
        Assert.assertTrue("a.txt has not been moved correctly.", (Files.exists(Paths.get(outPath + "/a.txt"))));
        Assert.assertTrue("b.txt has not been moved correctly.", (Files.exists(Paths.get(outPath + "/b.txt"))));
        Assert.assertTrue("a.txt has not been moved correctly.", (Files.exists(Paths.get(outPath + "/child1/a.txt"))));
        Assert.assertTrue("a.txt has not been moved correctly.", (Files.exists(Paths.get(outPath + "/child1/b.txt"))));
        Assert.assertTrue("b.txt has not been moved correctly.", (Files.exists(Paths.get(outPath + "/child2/a.txt"))));
        Assert.assertTrue("b.txt has not been moved correctly.", (Files.exists(Paths.get(outPath + "/child2/b.txt"))));
    }


    /**
     * transport.vfs.FileURI = <in_location>/*
     * transport.vfs.ActionAfterFailure = MOVE
     * transport.vfs.MoveAfterFailure = <fail_location>/*
     *
     * @throws IOException if an error occurs while creating the required directory structure
     */
    @Test
    public void testMoveAfterFailurePollingFilesInChildWithWildCardForMove() throws IOException {

        String inPath = getInFilePath("testFilesInChildren");
        createFilesInPath(inPath);

        String child1PathString = inPath + "/child1/";
        Files.createDirectories(Paths.get(child1PathString));
        createFilesInPath(child1PathString);

        String child2PathString = inPath + "/child2/";
        Files.createDirectories(Paths.get(child2PathString));
        createFilesInPath(child2PathString);

        String failPath = getFailFilePath("testFilesInChildren");
        TestFileInjectHandler fileInjectHandler = poll(modifyUriToIncludeSubDirectories(inPath), ".*",
                                                       null, modifyUriToIncludeSubDirectories(failPath), null, true);
        Assert.assertEquals(6, fileInjectHandler.getFileNames().size());
        Assert.assertTrue("a.txt has not been moved correctly.", (Files.exists(Paths.get(failPath + "/a.txt"))));
        Assert.assertTrue("b.txt has not been moved correctly.", (Files.exists(Paths.get(failPath + "/b.txt"))));
        Assert.assertTrue("a.txt has not been moved correctly.", (Files.exists(Paths.get(failPath + "/child1/a.txt"))));
        Assert.assertTrue("a.txt has not been moved correctly.", (Files.exists(Paths.get(failPath + "/child1/b.txt"))));
        Assert.assertTrue("b.txt has not been moved correctly.", (Files.exists(Paths.get(failPath + "/child2/a.txt"))));
        Assert.assertTrue("b.txt has not been moved correctly.", (Files.exists(Paths.get(failPath + "/child2/b.txt"))));
    }

    /**
     * transport.vfs.FileURI = <in_location>/*
     *
     * @throws IOException if an error occurs while creating the required directory structure
     */
    @Test
    public void testPollingFilesInChainedChildren() throws IOException {

        String inPath = getInFilePath("testFilesInChildrenRecursively");
        createFilesInPath(inPath);
        String child1PathString = inPath + "/child1/";
        Files.createDirectories(Paths.get(child1PathString));
        createFilesInPath(child1PathString);

        String child2PathString = child1PathString + "/child2/";
        Files.createDirectories(Paths.get(child2PathString));
        createFilesInPath(child2PathString);
        TestFileInjectHandler fileInjectHandler = poll(modifyUriToIncludeSubDirectories(inPath), ".*", null, null,
                                                       null, false);
        Assert.assertEquals(6, fileInjectHandler.getFileNames().size());
    }

    /**
     * transport.vfs.FileURI = <in_location>/*
     * transport.vfs.ActionAfterProcess = MOVE
     * transport.vfs.MoveAfterProcess = <out_location>
     *
     * @throws IOException if an error occurs while creating the required directory structure
     */
    @Test
    public void testMoveAfterPollingFilesInChainedChildren() throws IOException {

        String inPath = getInFilePath("testFilesInChildrenRecursively");
        createFilesInPath(inPath);
        String child1PathString = inPath + "/child1/";
        Files.createDirectories(Paths.get(child1PathString));
        createFilesInPath(child1PathString);

        String child2PathString = child1PathString + "/child2/";
        Files.createDirectories(Paths.get(child2PathString));
        createFilesInPath(child2PathString);
        String outPath = getOutFilePath("testFilesInChildrenRecursively");

        TestFileInjectHandler fileInjectHandler = poll(modifyUriToIncludeSubDirectories(inPath), ".*", outPath, null,
                                                       null, false);
        Assert.assertEquals(6, fileInjectHandler.getFileNames().size());
        Assert.assertTrue("a.txt has not been moved correctly.", (Files.exists(Paths.get(outPath + "/a.txt"))));
        Assert.assertTrue("b.txt has not been moved correctly.", (Files.exists(Paths.get(outPath + "/b.txt"))));
        Assert.assertFalse("b.txt has not been moved correctly.", (Files.exists(Paths.get(outPath + "/child1"))));
        Assert.assertFalse("b.txt has not been moved correctly.",
                           (Files.exists(Paths.get(outPath + "/child1/child2"))));
    }

    /**
     * transport.vfs.FileURI = <in_location>/*
     * transport.vfs.ActionAfterFailure = MOVE
     * transport.vfs.MoveAfterFailure = <fail_location>
     *
     * @throws IOException if an error occurs while creating the required directory structure
     */
    @Test
    public void testMoveAfterFailurePollingFilesInChainedChildren() throws IOException {

        String inPath = getInFilePath("testFilesInChildrenRecursively");
        createFilesInPath(inPath);
        String child1PathString = inPath + "/child1/";
        Files.createDirectories(Paths.get(child1PathString));
        createFilesInPath(child1PathString);

        String child2PathString = child1PathString + "/child2/";
        Files.createDirectories(Paths.get(child2PathString));
        createFilesInPath(child2PathString);

        String failPath = getFailFilePath("testFilesInChildrenRecursively");
        TestFileInjectHandler fileInjectHandler = poll(modifyUriToIncludeSubDirectories(inPath), ".*", null,
                                                       failPath, null,
                                                       true);
        Assert.assertEquals(6, fileInjectHandler.getFileNames().size());
        Assert.assertTrue("a.txt has not been moved correctly.", (Files.exists(Paths.get(failPath + "/a.txt"))));
        Assert.assertTrue("b.txt has not been moved correctly.", (Files.exists(Paths.get(failPath + "/b.txt"))));
        Assert.assertFalse("b.txt has not been moved correctly.", (Files.exists(Paths.get(failPath + "/child1"))));
        Assert.assertFalse("b.txt has not been moved correctly.",
                           (Files.exists(Paths.get(failPath + "/child1/child2"))));
    }

    /**
     * transport.vfs.FileURI = <in_location>/*
     * transport.vfs.ActionAfterProcess = MOVE
     * transport.vfs.MoveAfterProcess = <out_location>/*
     *
     * @throws IOException if an error occurs while creating the required directory structure
     */
    @Test
    public void testMoveAfterPollingFilesInChainedChildrenWithWildCardForMove() throws IOException {

        String inPath = getInFilePath("testFilesInChildrenRecursively");
        createFilesInPath(inPath);
        String child1PathString = inPath + "/child1/";
        Files.createDirectories(Paths.get(child1PathString));
        createFilesInPath(child1PathString);

        String child2PathString = child1PathString + "/child2/";
        Files.createDirectories(Paths.get(child2PathString));
        createFilesInPath(child2PathString);

        String outPath = getOutFilePath("testFilesInChildrenRecursively");
        TestFileInjectHandler fileInjectHandler = poll(modifyUriToIncludeSubDirectories(inPath), ".*",
                                                       modifyUriToIncludeSubDirectories(outPath), null, null, false);
        Assert.assertEquals(6, fileInjectHandler.getFileNames().size());
        Assert.assertTrue("a.txt has not been moved correctly.", (Files.exists(Paths.get(outPath + "/a.txt"))));
        Assert.assertTrue("b.txt has not been moved correctly.", (Files.exists(Paths.get(outPath + "/b.txt"))));
        Assert.assertTrue("a.txt has not been moved correctly.", (Files.exists(Paths.get(outPath + "/child1/a.txt"))));
        Assert.assertTrue("a.txt has not been moved correctly.", (Files.exists(Paths.get(outPath + "/child1/b.txt"))));
        Assert.assertTrue("a.txt has not been moved correctly.",
                          (Files.exists(Paths.get(outPath + "/child1/child2/a.txt"))));
        Assert.assertTrue("b.txt has not been moved correctly.",
                          (Files.exists(Paths.get(outPath + "/child1/child2/b.txt"))));
    }

    /**
     * transport.vfs.FileURI = <in_location>/*
     * transport.vfs.ActionAfterFailure = MOVE
     * transport.vfs.MoveAfterFailure = <fail_location>/*
     *
     * @throws IOException if an error occurs while creating the required directory structure
     */
    @Test
    public void testMoveAfterFailurePollingFilesInChainedChildrenWithWildCardForMove() throws IOException {

        String inPath = getInFilePath("testFilesInChildrenRecursively");
        createFilesInPath(inPath);
        String child1PathString = inPath + "/child1/";
        Files.createDirectories(Paths.get(child1PathString));
        createFilesInPath(child1PathString);

        String child2PathString = child1PathString + "/child2/";
        Files.createDirectories(Paths.get(child2PathString));
        createFilesInPath(child2PathString);

        String failPath = getFailFilePath("testFilesInChildrenRecursively");
        TestFileInjectHandler fileInjectHandler = poll(modifyUriToIncludeSubDirectories(inPath), ".*",
                                                       null, modifyUriToIncludeSubDirectories(failPath), null, true);
        Assert.assertEquals(6, fileInjectHandler.getFileNames().size());
        Assert.assertTrue("a.txt has not been moved correctly.", (Files.exists(Paths.get(failPath + "/a.txt"))));
        Assert.assertTrue("b.txt has not been moved correctly.", (Files.exists(Paths.get(failPath + "/b.txt"))));
        Assert.assertTrue("a.txt has not been moved correctly.", (Files.exists(Paths.get(failPath + "/child1/a.txt"))));
        Assert.assertTrue("a.txt has not been moved correctly.", (Files.exists(Paths.get(failPath + "/child1/b.txt"))));
        Assert.assertTrue("a.txt has not been moved correctly.",
                          (Files.exists(Paths.get(failPath + "/child1/child2/a.txt"))));
        Assert.assertTrue("b.txt has not been moved correctly.",
                          (Files.exists(Paths.get(failPath + "/child1/child2/b.txt"))));
    }

    /**
     * transport.vfs.FileURI = <in_location>
     *
     * @throws IOException if an error occurs while creating the required directory structure
     */
    @Test()
    public void testPollingEmptyDirectory() throws IOException {

        String inPath = getBaseFilePath("") + "/empty/in/";
        clearPath(inPath);
        Files.createDirectories(Paths.get(inPath));
        TestFileInjectHandler fileInjectHandler = poll(inPath, ".*", null, null, null, false);
        Assert.assertEquals(0, fileInjectHandler.getFileNames().size());
    }

    /**
     * transport.vfs.FileURI = <in_location>/a.txt
     *
     * @throws IOException if an error occurs while creating the required directory structure
     */
    @Test()
    public void testPollingFileAsUri() throws IOException {

        String inPath = getInFilePath("testFileAsUri");
        Files.createDirectories(Paths.get(inPath));

        inPath = inPath + File.separator + "a.txt";
        File textFile = new File(inPath);
        assert textFile.createNewFile();

        String outPath = getOutFilePath("testFileAsUri");
        TestFileInjectHandler fileInjectHandler = poll(inPath, null, outPath, null, null, false);
        Assert.assertEquals(1, fileInjectHandler.getFileNames().size());
        Assert.assertTrue("a.txt has not been moved correctly.",
                          (Files.exists(Paths.get(outPath + "/a.txt"))));
    }

    /**
     * transport.vfs.FileURI = <in_location>
     * transport.vfs.FileSizeLimit = 20
     *
     * @throws IOException if an error occurs while creating the required directory structure
     */
    @Test
    public void testPollingFilesWithSizeLimit() throws IOException {

        String inPath = getBaseFilePath("testFileSizeLimit") + "/in/";
        clearPath(inPath);

        //Write content to the small file
        String smallFilePath = inPath + File.separator + "small.txt";
        File smallFile = new File(smallFilePath);
        assert smallFile.createNewFile();
        String smallContent = "Hello World!!!";
        Files.write(smallFile.toPath(), smallContent.getBytes());

        //Write content to the small file
        String largeFilePath = inPath + File.separator + "large.txt";
        File largeFile = new File(largeFilePath);
        assert largeFile.createNewFile();
        String largeContent = "Hello World!!!\n"
                              + "Please don't process me because I'm too large to be processed.\n"
                              + "I'm not vey big but they say I'm too much :D";
        Files.write(largeFile.toPath(), largeContent.getBytes());

        Properties properties = new Properties();
        properties.put(VFSConstants.TRANSPORT_FILE_SIZE_LIMIT, "20");
        TestFileInjectHandler fileInjectHandler = poll(inPath, ".*.txt", null, null, properties, false);
        Assert.assertEquals(1, fileInjectHandler.getFileNames().size());
        Assert.assertFalse("File below size limit is not processed.", (Files.exists(Paths.get(inPath + "/small.txt"))));
        Assert.assertTrue("File exceeding size limit is processed.", (Files.exists(Paths.get(inPath + "/large.txt"))));
    }

    private TestFileInjectHandler poll(String inPath, String fileNamePattern, String moveAfterProcess,
                                       String moveAfterFailure, Properties additionalProperties, boolean mockFailure) {
        Properties vfsProperties = getVfsProperties(inPath, fileNamePattern, moveAfterProcess, moveAfterFailure);
        if (Objects.nonNull(additionalProperties)) {
            vfsProperties.putAll(additionalProperties);
        }
        FilePollingConsumer pollingConsumer = new FilePollingConsumer(vfsProperties, null, null, 10);
        TestFileInjectHandler fileInjectHandler = new TestFileInjectHandler(null, null, false, null, null);
        fileInjectHandler.setMockFailure(mockFailure);
        pollingConsumer.registerHandler(fileInjectHandler);
        pollingConsumer.poll();
        fileInjectHandler.getFileNames().forEach(System.out::println);
        return fileInjectHandler;
    }

    private Properties getVfsProperties(String inPath, String fileNamePattern, String moveAfterProcess,
                                        String moveAfterFailure) {
        Properties vfsProperties = new Properties();
        vfsProperties.put(VFSConstants.TRANSPORT_FILE_FILE_URI, inPath);
        if (Objects.nonNull(fileNamePattern)) {
            vfsProperties.put(VFSConstants.TRANSPORT_FILE_FILE_NAME_PATTERN, fileNamePattern);
        }
        if (Objects.nonNull(moveAfterProcess)) {
            vfsProperties.put(VFSConstants.TRANSPORT_FILE_ACTION_AFTER_PROCESS, "MOVE");
            vfsProperties.put(VFSConstants.TRANSPORT_FILE_MOVE_AFTER_PROCESS, moveAfterProcess);
        }
        if (Objects.nonNull(moveAfterFailure)) {
            vfsProperties.put(VFSConstants.TRANSPORT_FILE_ACTION_AFTER_FAILURE, "MOVE");
            vfsProperties.put(VFSConstants.TRANSPORT_FILE_MOVE_AFTER_FAILURE, moveAfterFailure);
        }
        return vfsProperties;
    }

    private void createFilesInPath(String path) throws IOException {

        File textFile = new File(path + File.separator + "a.txt");
        assert textFile.createNewFile();
        File textFile2 = new File(path + File.separator + "b.txt");
        assert textFile2.createNewFile();
    }


    private void clearPath(String pathString) throws IOException {
        Path path = Paths.get(pathString);
        try {
            Files.walk(path)
                    .sorted(Comparator.reverseOrder())
                    .map(Path::toFile)
                    .forEach(File::delete);
            Files.delete(path);
        } catch (NoSuchFileException e) {
            //ignore
        }
        Files.createDirectories(path);
    }

    private String getInFilePath(String srcPath) throws IOException {
        String inPath = getBaseFilePath(srcPath) + "/in/";
        clearPath(inPath);
        return inPath;
    }

    private String getOutFilePath(String srcPath) throws IOException {
        String outPath = getBaseFilePath(srcPath) + "/out/";
        clearPath(outPath);
        return outPath;
    }

    private String getFailFilePath(String srcPath) throws IOException {
        String failPath = getBaseFilePath(srcPath) + "/fail/";
        clearPath(failPath);
        return failPath;
    }

    private String getBaseFilePath(String srcPath) {
        ClassLoader classLoader = getClass().getClassLoader();
        return new File(Objects.requireNonNull(classLoader.getResource("fileInbound/" + srcPath)).getFile()).getAbsolutePath();
    }

    private String modifyUriToIncludeSubDirectories(String inPath) {
        return inPath + "/*";
    }

}
