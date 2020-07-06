/**
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wso2.micro.integrator.registry;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;
import java.util.Map;

public class MicroIntegratorRegistryListener implements Runnable {

    private static final Log log = LogFactory.getLog(MicroIntegratorRegistryListener.class);
    private final WatchService watcher;
    private final Map<WatchKey, Path> keys;

    /**
     * Creates a WatchService and registers the given directory
     */
    public MicroIntegratorRegistryListener(Path dir) throws IOException {
        this.watcher = FileSystems.getDefault().newWatchService();
        this.keys = new HashMap<>();

        walkAndRegisterDirectories(dir);
    }

    /**
     * Register the given directory with the WatchService; This function will be called by FileVisitor
     *
     * @param dir registry file location
     */
    private void registerDirectory(Path dir) throws IOException
    {
        WatchKey key = dir.register(watcher,
                StandardWatchEventKinds.ENTRY_CREATE,
                StandardWatchEventKinds.ENTRY_DELETE,
                StandardWatchEventKinds.ENTRY_MODIFY);
        keys.put(key, dir);
    }


    /**
     * Register the given directory, and all its sub-directories, with the WatchService.
     *
     * @param start the root path
     */
    private void walkAndRegisterDirectories(final Path start) throws IOException {
        // register directory and sub-directories
        Files.walkFileTree(start, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                registerDirectory(dir);
                return FileVisitResult.CONTINUE;
            }
        });
    }

    public void run() {
        try {
            WatchKey key;
            while ((key = watcher.take()) != null) {
                Path dir = keys.get(key);
                if (dir == null) {
                    log.error("WatchKey for listening to registry resources changes not recognized!!");
                    continue;
                }
                for (WatchEvent<?> event : key.pollEvents()) {
                    @SuppressWarnings("rawtypes")
                    WatchEvent.Kind kind = event.kind();

                    // Context for directory entry event is the file name of entry
                    @SuppressWarnings("unchecked")
                    Path changedFileName = ((WatchEvent<Path>)event).context();
                    Path changedFilePath = dir.resolve(changedFileName);
                    File changedFile = new File(String.valueOf(changedFilePath));
                    if (changedFile.isDirectory()) {
                        continue;
                    }
                    saveFileChanges(changedFile, kind);
                 }
                // reset key
                if (!key.reset()) {
                    break;
                }
            }
        } catch (InterruptedException e) {
            log.error("Error while listening to registry file changes ", e);
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Save changes to the map
     *
     * @param changedFile   changed file
     * @param kind          the triggered event
     */
    private void saveFileChanges(File changedFile, WatchEvent.Kind kind) {
        try {
            String urlStr = Paths.get(changedFile.toString()).toUri().normalize().toString();
            urlStr = refactorURL(urlStr);
            if (StandardWatchEventKinds.ENTRY_DELETE.equals(kind)) {
                MicroIntegratorRegistry.deleteResourceLastModifiedEntry(urlStr);
            } else {
                URL url = new URL(urlStr);
                URLConnection urlConn = url.openConnection();
                MicroIntegratorRegistry.addResourceLastModifiedEntry(urlStr, urlConn.getLastModified());
            }
        } catch (IOException e) {
            log.error("Error while opening connection to the changed file: " + changedFile.getName(), e);
        }
    }

    /**
     * refactor url path to insert '/'
     *
     * @param resourcePath   resource file path
     */
    private String refactorURL (String resourcePath) {
        StringBuilder buf = new StringBuilder(resourcePath);
        int position = 0;
        if (resourcePath.contains("config")) {
            position = resourcePath.indexOf("config") + 6;
        } else if (resourcePath.contains("governance")) {
            position = resourcePath.indexOf("governance") + 10;
        } else if (resourcePath.contains("local")) {
            position = resourcePath.indexOf("local") + 5;
        }
        buf.insert(position, MicroIntegratorRegistryConstants.URL_SEPARATOR);
        return buf.toString();
    }
}
