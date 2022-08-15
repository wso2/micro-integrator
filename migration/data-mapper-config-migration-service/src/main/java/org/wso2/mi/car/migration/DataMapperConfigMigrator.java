/*
 * Copyright (c) 2022, WSO2 LLC. (http://www.wso2.org) All Rights Reserved.
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
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.mi.car.migration;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Objects;
import java.util.logging.Logger;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class DataMapperConfigMigrator {

    private static final Logger LOGGER = Logger.getLogger(DataMapperConfigMigrator.class.getName());
    private static final int DEFAULT_BUFFER_SIZE = 1024 * 4;

    public static void main(String[] args) {

        if (args.length < 1) {
            exitWithError("File path to old_apps is not provided as an argument.");
        }
        String userInput = args[0];
        String carLocation = userInput.endsWith(File.separator) ?
                userInput.substring(0, userInput.lastIndexOf(File.separator)) : userInput;
        File oldCappFolder = new File(carLocation);
        if (!oldCappFolder.exists() || !oldCappFolder.isDirectory()) {
            exitWithError("File path provided is incorrect. Please enter a valid file path");
        }
        String rootDirectory = carLocation + File.separator + "..";
        String tmpDirectory = createFolder(rootDirectory,"tmp").toString();
        String migratedCAppFolder = createFolder(rootDirectory,"migrated_capps").toString();

        String[] appList = oldCappFolder.list();
        if (null != appList && appList.length > 0) {
            for (String carFile: appList) {
                if (FilenameUtils.getExtension(carFile).equals("car")) {
                    copyFileToDirectory(carLocation, tmpDirectory, carFile);
                }
            }
        } else {
            exitWithError("The provided carbon application folder " + carLocation + " is empty.");
        }
        String[] tmpCAppsList = new File(tmpDirectory).list();
        if (null != tmpCAppsList) {
            for (String file : tmpCAppsList) {
                String carFile = tmpDirectory + File.separator + file;
                String cAppFolder = unzipFile(carFile);
                ArrayList<String> styleSheetList = getXsltStyleSheets(cAppFolder);
                if (styleSheetList.size() > 0) {
                    LOGGER.info(styleSheetList.size() + " xsltStyleSheet file/s are available in " +  file
                            + ". Hence performing migration");
                    deleteFile(carFile);
                    for (String styleSheetPath : styleSheetList) {
                        replaceText(styleSheetPath);
                    }
                    zipFile(cAppFolder.endsWith(File.separator) ?
                            cAppFolder.substring(0, cAppFolder.lastIndexOf(File.separator)) : cAppFolder, carFile);
                    copyFileToDirectory(carFile, migratedCAppFolder);
                } else {
                    LOGGER.info("An xsltStyleSheet file is unavailable in  " + file + ". " +
                            "Hence skipping migration for " + file + ".");
                    copyFileToDirectory(carFile, migratedCAppFolder);
                }
                LOGGER.info("Migration is completed for " + file);
            }
        }

        deleteDirectory(tmpDirectory);
        LOGGER.info("Migration completed successfully. Migrated carbon applications are available in "
                + migratedCAppFolder);
    }

    /**
     * Create a new folder
     * @param root the absolute location of the directory to create the folder
     * @param folderName name of the folder
     * @return path of the created folder
     */
    private static Path createFolder(String root, String folderName) {
        Path path = Paths.get(root + File.separator + folderName);
        Path folderPath = null;
        if (Files.exists(path)) {
            exitWithError("Directory " + folderName + " already exists in path " + root);
        }
        try {
            folderPath = Files.createDirectories(path);
        } catch (IOException e) {
            exitWithError("An error occurred while creating folder " + folderName + ". " + e.getMessage());
        }
        return folderPath;
    }

    /**
     * Copy a file from a folder to a directory
     * @param srcFile absolute location of the folder of the file to be copied
     * @param destDir absolute location of destination
     * @param carFile name of the file to be copied
     */
    private static void copyFileToDirectory(String srcFile, String destDir, String carFile) {
        copyFileToDirectory(srcFile + File.separator + carFile, destDir);
    }

    /**
     * Copy file to a directory
     * @param srcFile the absolute location of the file to be copied
     * @param destDir teh absolute destination of the file to be copied
     */
    private static void copyFileToDirectory(String srcFile, String destDir) {
        try {
            FileUtils.copyFileToDirectory(new File(srcFile), new File(destDir));
        } catch (IOException e) {
            exitWithError("An error occurred when copying " + srcFile + " to " + destDir);
        }
    }

    /**
     * Delete a directory
     * @param source The absolute location of the directory to be deleted
     */
    private static void deleteDirectory(String source) {
        try {
            FileUtils.deleteDirectory(new File(source));
        } catch (IOException e) {
            exitWithError("An error occurred while deleting directory " + source);
        }
    }

    /**
     * Delete a file
     * @param source the absolute location of the file to be deleted
     */
    private static void deleteFile(String source) {
        File file= new File(source);
        if (!file.delete()) {
            exitWithError("An error occurred while deleting file " + source);
        }
    }

    /**
     * Update the file with corresponding elements
     * @param filePath the file path of the stylesheet to be updated
     */
    private static void replaceText(String filePath) {
        File file = new File(filePath);
        String search = "extension-element-prefixes=\"xs own notXSLTCompatible firstElementOfTheInput\"";
        String replace = "extension-element-prefixes=\"own notXSLTCompatible firstElementOfTheInput\" " +
                "exclude-result-prefixes=\"xs\"";
        try {
            String data = FileUtils.readFileToString(file, StandardCharsets.UTF_8);
            data = data.replace(search, replace);
            FileUtils.writeStringToFile(file, data, StandardCharsets.UTF_8);
        } catch (IOException e) {
            exitWithError("An error occurred while updating file " + filePath + ". " + e.getMessage());
        }
    }

    /**
     * Unzip a file
     * @param source the absolute location of the file to be zipped
     * @return the absolute location of the unzipped folder
     */
    private static String unzipFile(String source) {
        String destination = removeExtension(source);
        File destDir = new File(destination);
        byte[] buffer = new byte[1024];
        try {
            ZipInputStream zis = new ZipInputStream(Files.newInputStream(Paths.get(source)));
            ZipEntry zipEntry = zis.getNextEntry();
            while (zipEntry != null) {
                File newFile = newFile(destDir, zipEntry);
                if (zipEntry.isDirectory()) {
                    if (!newFile.isDirectory() && !newFile.mkdirs()) {
                        exitWithError("Failed to create directory " + newFile);
                    }
                } else {
                    // fix for Windows-created archives
                    File parent = newFile.getParentFile();
                    if (!parent.isDirectory() && !parent.mkdirs()) {
                        exitWithError("Failed to create directory " + parent);
                    }

                    // write file content
                    FileOutputStream fos = new FileOutputStream(newFile);
                    int len;
                    while ((len = zis.read(buffer)) > 0) {
                        fos.write(buffer, 0, len);
                    }
                    fos.close();
                }
                zipEntry = zis.getNextEntry();
            }
            zis.closeEntry();
            zis.close();
            return destination;
        } catch (IOException e) {
            exitWithError("An error occurred while unzipping file " + source);
        }
        return destination;
    }

    /**
     * Remove the extension
     * @param fileName name of the file
     * @return name of the file without the extension
     */
    private static String removeExtension(String fileName) {
        int lastIndex = fileName.lastIndexOf('.');
        if (lastIndex != -1) {
            fileName = fileName.substring(0, lastIndex);
        }
        return fileName;
    }

    /**
     * Zip a file
     * @param source the absolute location of the folder to be compressed
     * @param zipFile the name of the compressed file
     */
    private static void zipFile(String source, String zipFile) {
        try {
            ZipOutputStream zipOut = new ZipOutputStream(Files.newOutputStream(Paths.get(zipFile)));
            File srcFile = new File(source);
            for(String fileName : Objects.requireNonNull(srcFile.list())) {
                zipFile(source, fileName, zipOut);
            }
            zipOut.flush();
            zipOut.close();
        } catch (IOException e) {
            exitWithError("An error occurred while compressing file " + source + ". " + e.getMessage());
        }
    }

    /**
     * Zip a file
     * @param source the absolute location of the folder to be compressed
     * @param fileName name of the file/folder
     * @param zipOut zip output stream
     */
    private static void zipFile(String source, String fileName, ZipOutputStream zipOut) {
        File file = new File(source + File.separator + fileName);
        if (file.isDirectory()) {
            addFolderToZip(source, fileName, zipOut);
        } else {
            addFileToZip(source, fileName, zipOut);
        }
    }

    /**
     * Add a file to the zip
     * @param source the absolute location of the folder to be compressed
     * @param fileName the name of the file to be added to the compressed file
     * @param zipOut zip output stream
     */
    private static void addFileToZip(String source, String fileName, ZipOutputStream zipOut) {
        try {
            zipOut.putNextEntry(new ZipEntry(fileName));
            FileInputStream in = new FileInputStream(source + File.separator + fileName);
            byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
            int len;
            while ((len = in.read(buffer)) != -1) {
                zipOut.write(buffer, 0, len);
            }
            in.close();
        } catch (IOException e) {
            exitWithError("An error occurred while adding file " + fileName + " to car file. " + e.getMessage());
        }
    }

    /**
     * Add a folder to a zip
     * @param source the absolute location of the folder to be compressed
     * @param folderName name of the folder to be added to the compressed file
     * @param zipOut zip output stream
     */
    private static void addFolderToZip(String source, String folderName, ZipOutputStream zipOut) {
        try {
            zipOut.putNextEntry(new ZipEntry(folderName + File.separator));
            File folder = new File(source + File.separator + folderName);
            for (String fileName : Objects.requireNonNull(folder.list())) {
                zipFile(source, folderName + File.separator + fileName, zipOut);
            }
        } catch (IOException e) {
            exitWithError("An error occurred while adding folder " + folderName + " to car file. " + e.getMessage());
        }
    }

    /**
     * get the list of xslt style sheets
     * @param folder the absolute location of the folder list xslt style sheets
     * @return the list of absolute locations of xslt style sheets
     */
    private static ArrayList<String> getXsltStyleSheets(String folder) {
        ArrayList<String> xsltStyleSheets = new ArrayList<>();
        return listStyleSheetForFolder(new File(folder), xsltStyleSheets);
    }

    private static ArrayList<String> listStyleSheetForFolder(File folder, ArrayList<String> list) {
        for (final File fileEntry : Objects.requireNonNull(folder.listFiles())) {
            if (fileEntry.isDirectory()) {
                listStyleSheetForFolder(fileEntry, list);
            } else {
                if (fileEntry.getName().endsWith("_xsltStyleSheet.xml")) {
                    list.add(fileEntry.toString());
                }
            }
        }
        return list;
    }

    private static File newFile(File destinationDir, ZipEntry zipEntry) {
        File destFile = new File(destinationDir, zipEntry.getName());
        try {
            String destDirPath = destinationDir.getCanonicalPath();
            String destFilePath = destFile.getCanonicalPath();
            if (!destFilePath.startsWith(destDirPath + File.separator)) {
                exitWithError("Entry is outside of the target dir: " + zipEntry.getName());
            }
        } catch (IOException e) {
            exitWithError("An error occurred while creating new file " + zipEntry.getName() + ". " + e.getMessage());
        }
        return destFile;
    }

    /**
     * Log and terminate the program
     * @param msg the error log message
     */
    private static void exitWithError(String msg) {
        LOGGER.severe(msg);
        LOGGER.info("Migration failed.");
        System.exit(1);
    }
}
