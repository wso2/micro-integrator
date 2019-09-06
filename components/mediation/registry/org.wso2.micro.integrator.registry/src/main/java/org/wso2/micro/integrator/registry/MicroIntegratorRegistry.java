/**
* Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
* WSO2 Inc. licenses this file to you under the Apache License,
* Version 2.0 (the "License"); you may not use this file except
* in compliance with the License.
* you may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied. See the License for the
* specific language governing permissions and limitations
*
*/

package org.wso2.micro.integrator.registry;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMException;
import org.apache.axiom.om.OMNode;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.axiom.om.impl.llom.OMDocumentImpl;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.SynapseException;
import org.apache.synapse.registry.AbstractRegistry;
import org.apache.synapse.registry.RegistryEntry;
import org.apache.synapse.util.SynapseBinaryDataSource;

import javax.activation.DataHandler;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.*;
import java.net.*;
import java.nio.file.Paths;
import java.util.*;

import static org.wso2.micro.integrator.registry.MicroIntegratorRegistryConstants.URL_SEPARATOR;

public class MicroIntegratorRegistry extends AbstractRegistry {

    private static final Log log = LogFactory.getLog(MicroIntegratorRegistry.class);

    private static final int DELETE_RETRY_SLEEP_TIME = 10;
    private static final long DEFAULT_CACHABLE_DURATION = 0;
    private static final int MAX_KEYS = 200;

    public static final int FILE = 1;
    public static final int HTTP = 2;
    public static final int HTTPS = 3;

    /**
     * File system path corresponding to the FILE url path. This is a system depending path
     * used for accessing resources as files.
     */
    private String localRegistry = null;

    private String configRegistry = null;

    private String govRegistry = null;

    private Map<String, String> fileExtensionMediaTypeMap = null;

    /**
     * Specifies whether the registry is in the local host or a remote registry.
     * Local host means the same computer as ESB is running.
     */
    private int registryType = MicroIntegratorRegistryConstants.LOCAL_HOST_REGISTRY;

    /**
     * Contains the protocol for the registry. Allowd values are FILE, HTTP and HTTPS.
     */
    private int registryProtocol = FILE;


    public MicroIntegratorRegistry() {
        //default registry is file system based resided in carbon home
        String defaultFSRegRoot = RegistryHelper.getHome().replace(File.separator, URL_SEPARATOR);
        if (!defaultFSRegRoot.endsWith(URL_SEPARATOR)) {
            defaultFSRegRoot = defaultFSRegRoot + URL_SEPARATOR;
        }
        //Default registry root : <CARBON_HOME>/registry/
        defaultFSRegRoot += "registry" + URL_SEPARATOR;

        //create default file system paths for registry
        //Default registry local registry location : <CARBON_HOME>/registry/local
        this.localRegistry = getUri(defaultFSRegRoot, "local");
        //Default registry config registry location : <CARBON_HOME>/registry/config
        this.configRegistry = getUri(defaultFSRegRoot, "config");
        //Default registry governance registry location : <CARBON_HOME>/registry/governance
        this.govRegistry = getUri(defaultFSRegRoot, "governance");
    }

    private String getUri(String defaultFSRegRoot, String subDirectory) {
        return Paths.get(defaultFSRegRoot + subDirectory).toUri().normalize().toString()
                + MicroIntegratorRegistryConstants.URL_SEPARATOR;
    }

    @Override
    public void init(Properties properties) {

        super.init(properties);
        for (Object o : properties.keySet()) {
            if (o != null) {
                String name = (String) o;
                String value = (String) properties.get(name);
                addConfigProperty(name, value);
            }
        }
        this.fileExtensionMediaTypeMap =  createFileExtensionsMap();
        log.debug("EI lightweight registry is initialized.");

    }

    @Override
    public OMNode lookup(String key) {
        if (log.isDebugEnabled()) {
            log.debug("==> Repository fetch of resource with key : " + key);
        }

        String resolvedRegKeyPath = resolveRegistryURI(key);
        URLConnection urlConnection;
        URL url = null;
        try {
            url = new URL(resolvedRegKeyPath);
        } catch (MalformedURLException e) {
            handleException("Invalid path '" + resolvedRegKeyPath + "' for URL", e);
        }

        if (url == null) {
            handleException("Unable to create URL for target resource : " + key);
        }

        if ("file".equals(url.getProtocol())) {
            try {
                url.openStream();
            } catch (IOException e) {
                if (log.isDebugEnabled()) {
                    log.debug("Error occurred while accessing registry resource: " + key, e);
                }
                return null;
            }
        }

        try {
            urlConnection = url.openConnection();
            urlConnection.connect();
        } catch (IOException e) {
            return null;
        }

        InputStream input = null;
        try {
            input = urlConnection.getInputStream();
        } catch (IOException e) {
            handleException("Error when getting a stream from the URL", e);
        }

        if (input == null) {
            return null;
        }

        BufferedInputStream inputStream = new BufferedInputStream(input);
        OMNode result = null;
        try {
            XMLStreamReader parser = XMLInputFactory.newInstance().
                    createXMLStreamReader(inputStream);
            StAXOMBuilder builder = new StAXOMBuilder(parser);
            result = builder.getDocumentElement();

        } catch (OMException | XMLStreamException ignored) {

            if (log.isDebugEnabled()) {
                log.debug("The resource at the provided URL isn't well-formed XML,So,takes it as a text");
            }

            try {
                inputStream.close();
            } catch (IOException e) {
                log.error("Error in closing the input stream. ", e);
            }

            try {
                result = readNonXML(url);
            } catch (IOException e) {
                log.error("Error occurred while retrieving text content from registry artifact", e);
                result = null;
            }

        } finally {
            try {
                if (result != null && result.getParent() != null) {
                    result.detach();
                    OMDocumentImpl parent = new OMDocumentImpl(OMAbstractFactory.getOMFactory());
                    parent.addChild(result);
                }
                inputStream.close();
            } catch (IOException e) {
                log.error("Error in closing the input stream.", e);
            }

        }
        return result;
    }

    @Override
    public boolean isResourceExists(String key) {
        String resolvedRegKeyPath = resolveRegistryURI(key);
        try {
            // here, a URL object is created in order to remove the protocol from the file path
            File file = new File(new URL(resolvedRegKeyPath).getFile());
            return file.exists();
        } catch (MalformedURLException e) {
            log.error("Error in fetching resource: " + key, e);
            return false;
        }
    }

    /**
     * The micro integrator expects the properties of a directory to be available inside the given directory as a
     * property file. For an example, if a directory key, conf:/foo/bar is passed as the key, the micro integrator
     * registry expects the properties to be available in the file, conf:/foo/bar/bar.properties. For a file,
     * conf:/foo/bar/example.xml, the properties need to be given in the file, conf:/foo/bar/example.xml.properties
     *
     * @param key the path to the directory
     * @return the properties defined
     */
    public Properties lookupProperties(String key) {
        if (log.isDebugEnabled()) {
            log.debug("==> Repository fetch of resource with key : " + key);
        }
        String resolvedRegKeyPath = resolveRegistryURI(key);
        Properties result = new Properties();

        URL url = null;
        // get the path to the relevant property file
        try {
            resolvedRegKeyPath = getPropertyFileURI(resolvedRegKeyPath);
            url = new URL(resolvedRegKeyPath);
        } catch (MalformedURLException e) {
            handleException("Invalid path '" + resolvedRegKeyPath + "' for URL", e);
        }

        if (url == null) {
            handleException("Unable to create URL for target resource : " + key);
        }

        if ("file".equals(url.getProtocol())) {
            //Check existence of the file
            try {
                url.openStream();
            } catch (IOException e) {
                if (log.isDebugEnabled()) {
                    log.debug("Error occurred while accessing registry resource: " + key, e);
                }
                return null;
            }
        }

        try {
            URLConnection urlConnection = url.openConnection();
            urlConnection.connect();
            try (InputStream input = urlConnection.getInputStream()) {
                if (input == null) {
                    return null;
                }
                result.load(input);
            }
        } catch (IOException e) {
            log.error("Error in loading properties", e);
            return null;
        }
        return result;
    }

    /**
     * This methods append the properties file to the resource URL
     *
     * @param originalURL the path to the resource
     * @return URL of the relevant property file
     */
    private String getPropertyFileURI(String originalURL) throws MalformedURLException {

        originalURL = originalURL.trim();
        // here, a URL object is created in order to remove the protocol from the file path
        boolean isDirectory = new File(new URL(originalURL).getFile()).isDirectory();
        if (!isDirectory) {
            // if the url is a file, the property file is expected to be present as a sibling
            if (originalURL.endsWith(URL_SEPARATOR)) {
                originalURL = originalURL.substring(0, originalURL.length() - 1);
            }
            return originalURL + MicroIntegratorRegistryConstants.PROPERTY_EXTENTION;
        }
        // if the url is a folder, the property file is expected to be present as a child
        String[] pathSegments = originalURL.split(URL_SEPARATOR);
        String folderName = pathSegments[pathSegments.length - 1];
        if (originalURL.endsWith(URL_SEPARATOR)) {
            return originalURL + folderName + MicroIntegratorRegistryConstants.PROPERTY_EXTENTION;
        }
        return originalURL + URL_SEPARATOR + folderName + MicroIntegratorRegistryConstants.PROPERTY_EXTENTION;
    }

    @Override
    public RegistryEntry getRegistryEntry(String key) {

        // get information from the actual resource
        MediationRegistryEntryImpl entryEmbedded = new MediationRegistryEntryImpl();

        try {
            URL url = new URL(resolveRegistryURI(key));
            if ("file".equals(url.getProtocol())) {
                try {
                    url.openStream();
                } catch (IOException e) {
                    log.error("Error occurred while accessing registry resource: " + key, e);
                    return null;
                }
            }
            URLConnection urlc = url.openConnection();

            entryEmbedded.setKey(key);
            entryEmbedded.setName(url.getFile());
            entryEmbedded.setType(MicroIntegratorRegistryConstants.FILE);
            entryEmbedded.setDescription("Resource at : " + url.toString());
            entryEmbedded.setLastModified(urlc.getLastModified());
            entryEmbedded.setVersion(urlc.getLastModified());

            if (urlc.getExpiration() > 0) {
                entryEmbedded.setCachableDuration(
                        urlc.getExpiration() - System.currentTimeMillis());
            } else {
                entryEmbedded.setCachableDuration(getCachableDuration());
            }

        } catch (MalformedURLException e) {
            handleException("Invalid URL reference " + resolveRegistryURI(key), e);
        } catch (IOException e) {
            handleException("IO Error reading from URL " + resolveRegistryURI(key), e);
        }

        return entryEmbedded;
    }

    @Override
    public OMNode lookupFormat(String key) {
        //TODO verify this
        return lookup(key);
    }

    @Override
    public RegistryEntry[] getChildren(RegistryEntry entry) {

        if (entry == null) {
            // give the children of the root
            // null or key = "" stands for root of local registry

            MediationRegistryEntryImpl registryEntry = new MediationRegistryEntryImpl();
            registryEntry.setKey(MicroIntegratorRegistryConstants.LOCAL_REGISTRY_PREFIX + "/");
            entry = registryEntry;
        }

        String resourcePath = resolveRegistryURI(entry.getKey());
        String resourceRootEntry = entry.getKey();

        if (registryType == MicroIntegratorRegistryConstants.LOCAL_HOST_REGISTRY) {

            // registry is in the local FILE system. access it directly.
            File file = null;
            try {
                file = new File(new URI(resourcePath));
            } catch (URISyntaxException e) {
                handleException(e.getMessage(), e);
            }

            if (file == null || !file.isDirectory()) {
                return null;
            }

            if (!resourceRootEntry.endsWith(URL_SEPARATOR)) {
                resourceRootEntry += URL_SEPARATOR;
            }

            String[] children = file.list();
            RegistryEntry[] entries = new RegistryEntry[children.length];

            for (int i = 0; i < children.length; i++) {
                MediationRegistryEntryImpl registryEntry = new MediationRegistryEntryImpl();

                //Set registry entry key
                registryEntry.setKey(resourceRootEntry + children[i]);

                // set if the registry entry is a FILE or a FOLDER
                try {
                    File entryFile = new File(new URI(resourcePath + URL_SEPARATOR + children[i]));
                    if (entryFile.isDirectory()) {
                        registryEntry.setType(MicroIntegratorRegistryConstants.FOLDER);
                    } else {
                        registryEntry.setType(MicroIntegratorRegistryConstants.FILE);
                    }
                    entries[i] = registryEntry;
                } catch (URISyntaxException e) {
                    handleException("Error occurred while checking file type due to :" + e.getMessage(), e);
                }

            }
            return entries;

        } else if (registryType == MicroIntegratorRegistryConstants.REMOTE_HOST_REGISTRY) {
            // TODO : implement for remote registries.
            log.warn("Remote registry functionality not implemented yet");
        }

        return null;
    }

    @Override
    public RegistryEntry[] getDescendants(RegistryEntry entry) {

        ArrayList<RegistryEntry> list = new ArrayList<RegistryEntry>();

        fillDescendants(entry, list);

        RegistryEntry[] descendants = new RegistryEntry[list.size()];
        for (int i = 0; i < list.size(); i++) {
            descendants[i] = list.get(i);
        }

        return descendants;
    }

    @Override
    public void delete(String path) {
        if (registryType == MicroIntegratorRegistryConstants.LOCAL_HOST_REGISTRY) {
            removeResource(path);
        } else {
            // Warn the user that unable to delete remote registry resources
            log.warn("Deleting remote resources NOT SUPPORTED. Unable to delete: " + path);
        }
    }

    @Override
    public void newResource(String path, boolean isDirectory) {
        if (registryType == MicroIntegratorRegistryConstants.LOCAL_HOST_REGISTRY) {
            String resolvedPath = resolveRegistryURI(path);

            if (isDirectory && !resolvedPath.endsWith(URL_SEPARATOR)) {
                resolvedPath += URL_SEPARATOR;
            }

            String parent = getParentPath(resolvedPath, isDirectory);
            String fileName = getResourceName(resolvedPath);
            try {
                addResource(parent, fileName, !isDirectory);
            } catch (Exception e) {
                handleException("Error when adding a new resource", e);
            }
        } else {
            // Warn the user that unable to create resources in remote registry resources
            log.warn("Creating new resources in remote registry is NOT SUPPORTED. Unable to create: " + path);
        }
    }

    @Override
    public void newNonEmptyResource(String path, boolean isDirectory, String contentType, String content, String propertyName) {

        if (registryType == MicroIntegratorRegistryConstants.LOCAL_HOST_REGISTRY) {
            String targetPath = resolveRegistryURI(path);

            if (isDirectory && !targetPath.endsWith(URL_SEPARATOR)) {
                targetPath += URL_SEPARATOR;
            }

            String parent = getParentPath(targetPath, isDirectory);
            String fileName = getResourceName(targetPath);

            try {
                writeToFile(new URI(parent), fileName, content);
            } catch (Exception e) {
                handleException("Error when adding a new resource", e);
            }

        } else {
            log.warn("Creating new resource in remote registry is NOT SUPPORTED. Unable to create: " + path);
        }
    }


    /**
     * Updates the registry resource pointed by the given key.
     *
     * @param path   Key of the resource to be updated
     * @param value New value of the resource
     */
    @Override
    public void updateResource(String path, Object value) {
        if (registryType == MicroIntegratorRegistryConstants.LOCAL_HOST_REGISTRY) {
            try {
                File file = new File(new URI(resolveRegistryURI(path)));
                if (file.exists()) {
                    try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
                        writer.write(value.toString());
                        writer.flush();
                    } catch (IOException e) {
                        handleException("Couldn't write to registry entry: " + path, e);
                    }
                }
            } catch (URISyntaxException e) {
                handleException("Error occurred while updating resource: " + path, e);
            }
        } else {
            log.warn("Updating remote registry is NOT SUPPORTED. Unable to update: " + path);
        }
    }


    @Override
    public void updateRegistryEntry(RegistryEntry entry) {
        //Nothing to do here
    }


    private void handleException(String msg, Exception e) {
        log.error(msg, e);
        throw new SynapseException(msg, e);
    }

    private void handleException(String msg) {
        log.error(msg);
        throw new SynapseException(msg);
    }

    private long getCachableDuration() {
        String cachableDuration = (String) properties.get("cachableDuration");
        return cachableDuration == null ? DEFAULT_CACHABLE_DURATION : Long.parseLong(cachableDuration);
    }

    private void fillDescendants(RegistryEntry parent, ArrayList<RegistryEntry> list) {

        RegistryEntry[] entries = getChildren(parent);
        if (entries != null) {
            for (RegistryEntry entry : entries) {

                if (list.size() > MAX_KEYS) {
                    break;
                }

                fillDescendants(entry, list);
            }
        } else {
            list.add(parent);
        }
    }

    /**
     * Removes the local file system based registry resource identified by the given key.
     * If the key points to a directory, all its subdirectories and files in those directories will be deleted.
     *
     * @param key resource key
     */
    private void removeResource(String key) {
        try {
            File resource = new File(new URI(resolveRegistryURI(key)));
            if (resource.exists()) {
                if (resource.isFile()) {
                    deleteFile(resource);
                } else if (resource.isDirectory()) {
                    deleteDirectory(resource);
                }

            } else {
                handleException("Parent folder: " + key + " does not exists.");
            }
        } catch (URISyntaxException e) {
            handleException("Error occurred due to invalid URI while removing resource: " + key, e);
        }

    }

    private void deleteFile(File file) {

        boolean success = file.delete();
        if (!success) {
            // try with this work around to overcome a known bug in windows
            // work around:
            // run garbage collector and sleep for some time and delete
            // if still didn't delete,
            // rename FILE to a temp FILE in "temp" dir and mark it to delete on exist

            System.gc();
            try {
                Thread.sleep(DELETE_RETRY_SLEEP_TIME);
            } catch (InterruptedException e) {
                // ignore the exception
                log.error("Sleep wait interrupted while waiting for second retry to delete registry resource" ,e);
            }

            success = file.delete();
            if (!success) {
                int suffix = 1;
                File renamedFile;

                File tempDir = new File("temp");
                if (!tempDir.exists()) {
                    tempDir.mkdir();
                }

                do {
                    String changedName = "d" + suffix + file.getName();
                    renamedFile = new File(tempDir, changedName);
                    suffix++;
                } while (renamedFile.exists());

                if (file.renameTo(renamedFile)) {
                    renamedFile.deleteOnExit();
                } else {
                    handleException("Cannot delete the resource: " + file.getName());
                }
            }
        }
    }

    private void deleteDirectory(File dir) {

        File[] children = dir.listFiles();
        for (File child : children) {
            if (child != null) {
                if (child.isFile()) {
                    deleteFile(child);
                } else if (child.isDirectory()) {
                    deleteDirectory(child);
                }
            }
        }

        boolean success = dir.delete();
        if (!success) {
            handleException("Unable to delete the resource: " + dir.getName());
        }
    }

    /**
     * @param resourcePath If the resource is a directory it must end with URL_SEPARATOR
     * @param isDirectory
     * @return
     */
    private String getParentPath(String resourcePath, boolean isDirectory) {
        if (resourcePath != null) {
            String tempPath = resourcePath;
            if (isDirectory) {
                tempPath = resourcePath.substring(0, resourcePath.lastIndexOf(URL_SEPARATOR));
            }

            return tempPath.substring(0, tempPath.lastIndexOf(URL_SEPARATOR));
        }
        return "";
    }

    private String getResourceName(String path) {
        if (path != null) {
            String correctedPath = path;
            if (path.endsWith(URL_SEPARATOR)) {
                correctedPath = path.substring(0, path.lastIndexOf(URL_SEPARATOR));
            }
            return correctedPath.substring(correctedPath.lastIndexOf(URL_SEPARATOR) + 1, correctedPath.length());
        }
        return "";

    }

    /**
     * Adds a new resource to the registry.
     *
     * @param parentName   Key of the parent of the new resource
     * @param resourceName Name of the new resource
     * @param isLeaf       Specifies whether the new resource is a leaf or not. In a FILE system based
     *                     registry, leaf is a FILE and non-leaf is a FOLDER.
     * @throws Exception if an error occurs while creating the resources
     */
    private void addResource(String parentName, String resourceName, boolean isLeaf) throws Exception {

        if (isLeaf) {
            createFile(new URI(parentName), resourceName);
        } else {
            createFolder(new URI(parentName), resourceName);
        }

    }

    private void createFile(URI parentName, String newFileName) throws Exception {
        /*
            search for parent. if found, create the new FILE in it
        */
        File parent = new File(parentName);
        if (!parent.exists() && !parent.mkdirs()) {
            handleException("Unable to create parent directory: " + parentName);
        }
        File newFile = new File(parent, newFileName);
        if (!newFile.createNewFile()) {
            handleException("Couldn't create resource: " + newFileName);
        }
    }

    /**
     * Function to write to file, create if not exists including directory structure
     *
     * @param parentName
     * @param newFileName
     * @throws Exception
     */
    private void writeToFile(URI parentName, String newFileName, String content) throws Exception {
        /*
            search for parent. if found, create the new FILE in it
        */
        File parent = new File(parentName);
        if (!parent.exists() && !parent.mkdirs()) {
            handleException("Unable to create parent directory: " + parentName);
        }
        File newFile = new File(parent, newFileName);
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(newFile))) {
            writer.write(content);
            writer.flush();

            if (log.isDebugEnabled()) {
                log.debug("Successfully content written to file : " + parentName + URL_SEPARATOR + newFileName);
            }
        } catch (IOException e) {
            handleException("Couldn't write to registry resource: " + parentName + URL_SEPARATOR + newFileName, e);
        }
    }

    private void createFolder(URI parentName, String newFolderName) throws Exception {
        /*
            search for parent. if found, create the new FOLDER in it.
        */
        File parent = new File(parentName);
        if (parent.exists() || parent.mkdirs()) {
            File newEntry = new File(parent, newFolderName);
            boolean success = newEntry.mkdir();
            if (!success) {
                handleException("Couldn't create folder: " + newFolderName);
            }

        } else {
            handleException("Parent folder: " + parentName + " cannot be created.");
        }
    }

    /**
     * Function to resolve the registry key and generate absolute URI of the registry resource
     *
     * @param key
     * @return
     */
    private String resolveRegistryURI(String key) {
        String resolvedPath = null;

        if (key != null || !key.isEmpty()) {
            String registryRoot = "";
            String resourcePath = "";
            if (key.startsWith(MicroIntegratorRegistryConstants.CONFIG_REGISTRY_PREFIX)) {
                registryRoot = configRegistry;
                resourcePath = key.substring(MicroIntegratorRegistryConstants.CONFIG_REGISTRY_PREFIX.length());

            } else if (key.startsWith(MicroIntegratorRegistryConstants.GOVERNANCE_REGISTRY_PREFIX)) {
                registryRoot = govRegistry;
                resourcePath = key.substring(MicroIntegratorRegistryConstants.GOVERNANCE_REGISTRY_PREFIX.length());

            } else if (key.startsWith(MicroIntegratorRegistryConstants.LOCAL_REGISTRY_PREFIX)) {
                registryRoot = localRegistry;
                resourcePath = key.substring(MicroIntegratorRegistryConstants.LOCAL_REGISTRY_PREFIX.length());

            } else {
                registryRoot = govRegistry;
                resourcePath = key;
            }

            if (resourcePath.startsWith(URL_SEPARATOR)) {
                resourcePath = resourcePath.substring(1);
            }

            resolvedPath = registryRoot + resourcePath;
        }

        return resolvedPath;
    }


    /**
     * Function to retrieve resource content as text
     *
     * @param url
     * @return
     * @throws IOException
     */
    private OMNode readNonXML (URL url) throws IOException {
        String mediaType = lookUpFileMediaType(url.getPath());
        URLConnection urlConnection = url.openConnection();
        urlConnection.connect();

        try (InputStream inputStream = urlConnection.getInputStream()) {

            if (inputStream == null) {
                return null;
            }

            if (MicroIntegratorRegistryConstants.DEFAULT_MEDIA_TYPE.equals(mediaType)) {
                StringBuilder strBuilder = new StringBuilder();
                try (BufferedReader bReader = new BufferedReader(new InputStreamReader(inputStream))) {
                    String line;
                    while ((line = bReader.readLine()) != null) {
                        strBuilder.append(line);
                    }
                }
                return OMAbstractFactory.getOMFactory().createOMText(strBuilder.toString());
            } else {
                return OMAbstractFactory.getOMFactory().createOMText(
                        new DataHandler(new SynapseBinaryDataSource(inputStream, mediaType)), true);
            }
        }
    }

    /**
     * Configure the ESB registry using registry parameters.
     * <p/>
     * root: FILE:directory -   registry is on local host
     * directory is used to access metadata
     * <p/>
     * root: http/https:location -  has to specify one of the following settings
     * localRegistry - location of the local registry
     * metadataService - url of the service to access metadata
     * If none of above parameters are given "registry" FOLDER is taken as the local registry.
     *
     * @param name name of the config
     * @param value value of the config
     */
    private void addConfigProperty(String name, String value) {

        if (name != null && value != null) {
            if (log.isDebugEnabled()) {
                log.debug("Processing registry configuration property : [Name: " + name + " Value: "+ value + "]");
            }

            if (name.equals(MicroIntegratorRegistryConstants.CONF_REG_ROOT) ||
                    name.equals(MicroIntegratorRegistryConstants.GOV_REG_ROOT) ||
                    name.equals(MicroIntegratorRegistryConstants.LOCAL_REG_ROOT)) {
                try {
                    URL rootPathUrl = new URL(value);
                    if (MicroIntegratorRegistryConstants.PROTOCOL_FILE.equals(rootPathUrl.getProtocol())) {
                        registryProtocol = FILE;
                        registryType = MicroIntegratorRegistryConstants.LOCAL_HOST_REGISTRY;

                        //Check existence of the target location
                        try {
                            rootPathUrl.openStream();
                        } catch (IOException e) {
                            // If the registry is filesystem based, user may have provided the URI relative to the CARBON_HOME
                            if (log.isDebugEnabled()) {
                                log.debug("Configured registry path does not exists. Hence check existence " +
                                        "relative to CARBON_HOME");
                            }
                            String pathFromCarbonHome = RegistryHelper.getHome();
                            if (!pathFromCarbonHome.endsWith(URL_SEPARATOR)) {
                                pathFromCarbonHome += URL_SEPARATOR;
                            }
                            pathFromCarbonHome = rootPathUrl.getProtocol() + ":" + pathFromCarbonHome + value;
                            rootPathUrl = new URL(pathFromCarbonHome);
                            try {
                                rootPathUrl.openStream();
                                value = pathFromCarbonHome;
                            } catch (IOException e1) {
                                //Unable to open input stream to target location
                                handleException("Unable to open a connection to url : " + rootPathUrl, e1);
                            }
                        }

                        if (!value.endsWith(URL_SEPARATOR)) {
                            value += URL_SEPARATOR;
                        }

                    } else if (MicroIntegratorRegistryConstants.PROTOCOL_HTTP.equals(rootPathUrl.getProtocol())) {
                        registryProtocol = HTTP;
                        registryType = MicroIntegratorRegistryConstants.REMOTE_HOST_REGISTRY;
                        if (!value.endsWith(URL_SEPARATOR)) {
                            value += URL_SEPARATOR;
                        }

                    } else if (MicroIntegratorRegistryConstants.PROTOCOL_HTTPS.equals(rootPathUrl.getProtocol())) {
                        registryProtocol = HTTPS;
                        if (!value.endsWith(URL_SEPARATOR)) {
                            value += URL_SEPARATOR;
                        }

                    }

                    // Set config/gov/local registry properties
                    if (MicroIntegratorRegistryConstants.CONF_REG_ROOT.equals(name)) {
                        configRegistry = value;
                        if (log.isDebugEnabled()) {
                            log.debug("Configuration Registry Location : " + configRegistry);
                        }
                    } else if (MicroIntegratorRegistryConstants.GOV_REG_ROOT.equals(name)) {
                        govRegistry = value;
                        if (log.isDebugEnabled()) {
                            log.debug("Governance Registry Location : " + govRegistry);
                        }
                    } else {
                        localRegistry = value;
                        if (log.isDebugEnabled()) {
                            log.debug("Local Registry Location : " + localRegistry);
                        }
                    }

                } catch (MalformedURLException e) {
                    // don't set the root if this is not a valid URL
                    handleException("Registry root should be a valid URL.", e);
                }
            }
        } else {
            log.debug("Name and Value must need");
        }
    }

    @Override
    public Properties getResourceProperties(String entryKey) {

        Properties properties = new Properties();
        Properties resourceProperties = lookupProperties(entryKey);
        if (resourceProperties != null) {
            for (Object key : resourceProperties.keySet()) {
                Object value = resourceProperties.get(key);
                if (value instanceof List) {
                    if (((List) value).size() > 0) {
                        properties.put(key, ((List) value).get(0));
                    }
                } else {
                    properties.put(key, value);
                }
            }
            return properties;
        }
        return null;
    }

    /**
     * Populate file extension to media type mapping
     *
     * @return Map which contains file extension to media type mapping
     */
    private Map<String, String> createFileExtensionsMap() {

        Map<String, String> extensionContentTypeMap = new HashMap<>();

        extensionContentTypeMap.put(".xml", "application/xml");
        extensionContentTypeMap.put(".js", "application/javascript");
        extensionContentTypeMap.put(".css", "text/css");
        extensionContentTypeMap.put(".html", "text/html");
        extensionContentTypeMap.put(".sql", "text/plain");
        extensionContentTypeMap.put(".xsd", "Schema");
        extensionContentTypeMap.put(".xsl", "application/xsl+xml");
        extensionContentTypeMap.put(".xslt", "application/xslt+xml");
        extensionContentTypeMap.put(".zip", "application/zip");
        extensionContentTypeMap.put(".wsdl", "WSDL");

        return extensionContentTypeMap;
    }

    /**
     * Loopkup media-type for the relevant file from the mappings
     *
     * @param fileUrl File URL of the registry resource
     * @return Media-type of the file
     */
    private String lookUpFileMediaType(String fileUrl) {
        String extension = "";
        int indexOfExtensionDot = fileUrl.lastIndexOf('.');
        if (indexOfExtensionDot > 0) {
            extension = fileUrl.substring(indexOfExtensionDot);
        }

        String mediaType = MicroIntegratorRegistryConstants.DEFAULT_MEDIA_TYPE;
        if (fileExtensionMediaTypeMap.containsKey(extension)) {
            mediaType = fileExtensionMediaTypeMap.get(extension);
        }
        return mediaType;
    }
}
