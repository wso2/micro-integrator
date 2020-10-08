/*
Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.mi.registry.migration.utils;

import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.apache.maven.model.io.xpp3.MavenXpp3Writer;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.wso2.mi.registry.migration.exception.ArchiveException;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class FileUtils {

    private FileUtils() {
    }

    /**
     * Write the given content to the target location.
     *
     * @param filePath target file path
     * @param content  content to be written
     * @throws IOException if something goes wrong while writing the content
     */
    public static void write(String filePath, String content) throws IOException {
        try (BufferedWriter writer1 = new BufferedWriter(new FileWriter(filePath))) {
            writer1.write(content);
        }
    }

    /**
     * Zip a folder.
     *
     * @param srcFolder   file path of the archive directory
     * @param destZipFile file path of the .car file
     * @throws ArchiveException if something goes wrong while archiving the folder
     */
    public static void zipFolder(String srcFolder, String destZipFile) throws ArchiveException {
        try (FileOutputStream fileWriter = new FileOutputStream(destZipFile);
             ZipOutputStream zip = new ZipOutputStream(fileWriter)) {

            addFolderContentsToZip(srcFolder, zip);
            zip.flush();
        } catch (IOException ex) {
            throw new ArchiveException(MigrationClientUtils.ARCHIVE_EXCEPTION_MSG, ex);
        }
    }

    /**
     * Add contents in the given folder in the .car file.
     *
     * @param srcFolder file path of the archive directory
     * @param zip       ZipOutputStream
     */
    private static void addFolderContentsToZip(String srcFolder, ZipOutputStream zip) throws ArchiveException {
        File folder = new File(srcFolder);
        String[] fileList = folder.list();
        if (fileList == null) {
            return;
        }
        try {
            int i = 0;
            while (true) {
                if (fileList.length == i) break;
                if (new File(folder, fileList[i]).isDirectory()) {
                    zip.putNextEntry(new ZipEntry(fileList[i] + "/"));
                    zip.closeEntry();
                }
                addToZip(MigrationClientUtils.EMPTY_STRING, srcFolder + "/" + fileList[i], zip);
                i++;
            }
        } catch (IOException ex) {
            throw new ArchiveException(MigrationClientUtils.ARCHIVE_EXCEPTION_MSG, ex);
        }
    }

    /**
     * Add the given folder to the given location in the .car file.
     *
     * @param path    file path in the .car file
     * @param srcFile file to be included in the .car file
     * @param zip     ZipOutputStream
     */
    private static void addToZip(String path, String srcFile, ZipOutputStream zip) throws ArchiveException {
        File folder = new File(srcFile);
        if (folder.isDirectory()) {
            addFolderToZip(path, srcFile, zip);
        } else {
            // Transfer bytes from in to out
            byte[] buf = new byte[1024];
            int len;
            try (FileInputStream in = new FileInputStream(srcFile)) {
                if (path.trim().equals(MigrationClientUtils.EMPTY_STRING)) {
                    zip.putNextEntry(new ZipEntry(folder.getName()));
                } else {
                    zip.putNextEntry(new ZipEntry(path + MigrationClientUtils.URL_SEPARATOR + folder.getName()));
                }
                while ((len = in.read(buf)) > 0) {
                    zip.write(buf, 0, len);
                }
            } catch (IOException ex) {
                throw new ArchiveException(MigrationClientUtils.ARCHIVE_EXCEPTION_MSG, ex);
            }
        }
    }

    /**
     * Add the given folder to the given location in the .car file.
     *
     * @param path      file path in the .car file
     * @param srcFolder folder to be included in the .car file
     * @param zip       ZipOutputStream
     */
    private static void addFolderToZip(String path, String srcFolder, ZipOutputStream zip) throws ArchiveException {
        File folder = new File(srcFolder);
        String[] fileList = folder.list();
        if (fileList == null) {
            return;
        }
        try {
            int i = 0;
            while (true) {
                if (fileList.length == i) break;
                String newPath = folder.getName();
                if (!path.equalsIgnoreCase(MigrationClientUtils.EMPTY_STRING)) {
                    newPath = path + MigrationClientUtils.URL_SEPARATOR + newPath;
                }
                if (new File(folder, fileList[i]).isDirectory()) {
                    zip.putNextEntry(
                            new ZipEntry(newPath + MigrationClientUtils.URL_SEPARATOR + fileList[i] + MigrationClientUtils.URL_SEPARATOR));
                }
                addToZip(newPath, srcFolder + MigrationClientUtils.URL_SEPARATOR + fileList[i], zip);
                i++;
            }
        } catch (IOException ex) {
            throw new ArchiveException(MigrationClientUtils.ARCHIVE_EXCEPTION_MSG, ex);
        }
    }

    /**
     * Create a MavenProject object from stream.
     *
     * @param reader BufferedReader instance
     * @return MavenProject instance
     * @throws IOException            if something goes wrong when reading the pom file
     * @throws XmlPullParserException if something goes wrong when parsing the pom file
     */
    public static MavenProject getMavenProject(BufferedReader reader) throws IOException, XmlPullParserException {
        MavenXpp3Reader mavenXpp3Reader = new MavenXpp3Reader();
        Model model = mavenXpp3Reader.read(reader);

        return new MavenProject(model);
    }

    /**
     * Save the pom file in the target location.
     *
     * @param project MavenProject instance
     * @param file    target location
     * @throws IOException if something goes wrong when writing the pom file to the target location
     */
    public static void saveMavenProject(MavenProject project, File file) throws IOException {
        if (file.getParentFile() != null && file.getParentFile().mkdirs()) {
            MavenXpp3Writer mavenXpp3writer = new MavenXpp3Writer();
            FileWriter fileWriter = new FileWriter(file);
            mavenXpp3writer.write(fileWriter, project.getModel());
            fileWriter.close();
        }
    }
}
