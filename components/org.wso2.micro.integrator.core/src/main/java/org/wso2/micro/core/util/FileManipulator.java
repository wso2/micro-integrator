/*                                                                             
 * Copyright 2005,2006 WSO2, Inc. http://www.wso2.org
 *                                                                             
 * Licensed under the Apache License, Version 2.0 (the "License");             
 * you may not use this file except in compliance with the License.            
 * You may obtain a copy of the License at                                     
 *                                                                             
 *      http://www.apache.org/licenses/LICENSE-2.0                             
 *                                                                             
 * Unless required by applicable law or agreed to in writing, software         
 * distributed under the License is distributed on an "AS IS" BASIS,           
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.    
 * See the License for the specific language governing permissions and         
 * limitations under the License.                                              
 */
package org.wso2.micro.core.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * A set of useful utility for File manipulation operations
 */
@SuppressWarnings("unused")
public class FileManipulator {
    private static final Log log = LogFactory.getLog(FileManipulator.class);

    /**
     * Deletes all files and subdirectories under dir.
     * Returns true if all deletions were successful.
     * If a deletion fails, the method stops attempting to delete and returns false.
     *
     * @param dir The directory to be deleted
     * @return true if the directory and its descendents were deleted
     */
    public static boolean deleteDir(File dir) {
        if (dir.isDirectory()) {
            String[] children = dir.list();
            if (children != null) {
                for (String child : children) {
                    boolean success = deleteDir(new File(dir, child));
                    if (!success) {
                        return false;
                    }
                }
            }
        }

        // The directory is now empty so delete it
        return dir.delete();
    }

    /**
     * Copies all files under srcDir to dstDir.
     * If dstDir does not exist, it will be created.
     * @param srcDir The source dir
     * @param dstDir The destination dir
     * @throws IOException If an error occrs while copying
     */
    public static void copyDir(File srcDir, File dstDir) throws IOException {
        if (srcDir.isDirectory()) {
            if (!dstDir.exists() && !dstDir.mkdir()) {
                throw new IOException("Fail to create the directory: " + dstDir.getAbsolutePath());
            }

            String[] children = srcDir.list();
            for (String aChildren : children) {
                copyDir(new File(srcDir, aChildren),
                        new File(dstDir, aChildren));
            }
        } else {
            copyFile(srcDir, dstDir);
        }
    }

    /**
     * Copies src file to dst file.
     * If the dst file does not exist, it is created
     *
     * @param src The source file
     * @param dst The destiination file
     * @throws IOException If an Exception occurs while copying
     */
    public static void copyFile(File src, File dst) throws IOException {
        String dstAbsPath = dst.getAbsolutePath();
        String dstDir = dstAbsPath.substring(0, dstAbsPath.lastIndexOf(File.separator));
        File dir = new File(dstDir);
        if (!dir.exists() && !dir.mkdirs()) {
            throw new IOException("Fail to create the directory: " + dir.getAbsolutePath());
        }

        InputStream in = new FileInputStream(src);
        OutputStream out = null;
        try {
            out = new FileOutputStream(dst);

            // Transfer bytes from in to out
            byte[] buf = new byte[10240];
            int len;
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
        } finally {
            try {
                in.close();
            } catch (IOException e) {
                log.warn("Unable to close the InputStream " + e.getMessage(), e);
            }

            try {
                if (out != null) {
                    out.close();
                }
            } catch (IOException e) {
                log.warn("Unable to close the OutputStream " + e.getMessage(), e);
            }
        }
    }

    /**
     * Copies src file to dst directory.
     * If the dst directory does not exist, it is created
     * @param src  The file to be copied
     * @param dst  The destination directory to which the file has to be copied
     * @throws IOException If an error occurs while copying
     */
    public static void copyFileToDir(File src, File dst) throws IOException {
        String dstAbsPath = dst.getAbsolutePath();
        String dstDir = dstAbsPath.substring(0, dstAbsPath.lastIndexOf(File.separator));
        File dir = new File(dstDir);
        if(!dir.exists() && !dir.mkdirs()){
            throw new IOException("Fail to create the directory: " + dir.getAbsolutePath());
        }
        
        File file = new File(dstAbsPath + File.separator + src.getName());  
        copyFile(src, file);
    }

    /**
     * Get the list of file with a prefix of <code>fileNamePrefix</code> &amp; an extension of
     * <code>extension</code>
     *
     * @param sourceDir      The directory in which to search the files
     * @param fileNamePrefix The prefix to look for
     * @param extension      The extension to look for
     * @return The list of file with a prefix of <code>fileNamePrefix</code> &amp; an extension of
     *         <code>extension</code>
     */
    public static File[] getMatchingFiles(String sourceDir, String fileNamePrefix, String extension) {
        List<File> fileList = new ArrayList<File>();
        File libDir = new File(sourceDir);
        String libDirPath = libDir.getAbsolutePath();
        String[] items = libDir.list();
        if (items != null) {
            for (String item : items) {
                if (fileNamePrefix != null && extension != null) {
                    if (item.startsWith(fileNamePrefix) && item.endsWith(extension)) {
                        fileList.add(new File(libDirPath + File.separator + item));
                    }
                } else if (fileNamePrefix == null && extension != null) {
                    if (item.endsWith(extension)) {
                        fileList.add(new File(libDirPath + File.separator + item));
                    }
                } else if (fileNamePrefix != null) {
                    if (item.startsWith(fileNamePrefix)) {
                        fileList.add(new File(libDirPath + File.separator + item));
                    }
                } else {
                    fileList.add(new File(libDirPath + File.separator + item));
                }
            }
            return fileList.toArray(new File[fileList.size()]);
        }
        return new File[0];
    }

    /**
     * @see #deleteDir(File)
     * @param directory The directory to be deleted
     */
    public static void deleteDir(String directory) {
        deleteDir(new File(directory));
    }
}
