# Registry Migration Tool for WSO2 Micro Integrator

This tool creates metadata files for registry resources. 
Metadata files which are used to store media type of the resource were not available in the MI 1.0.0 release.
The tool infers media types from the file extensions of the resource files.

## How to run the tool

1. Build RegistryMigrator using maven 

`mvn clean package`

2. Run the jar file providing the path to MI distribution 

`java -jar target/RegistryMigrator-1.0.jar <Absolute_Path_To_MI_HOME>`

## More information

The metadata related to registry resource is stored in a separate file named <resource_file_name>.meta inside 
the ".metadata" dir which is located where the registry resource is.
At the moment media type is the only metadata stored.

Example registry structure:

    |registry/
    | ---- | governance/
    | -------- | custom1/
    | ------------ | checkJsScript.js
    | ------------ | .metadata/
    | ---------------- | checkJsScript.js.meta

Example .meta file:

    #Thu May 30 11:14:24 IST 2019
    mediaType=application/javascript

(c) Copyright 2019 WSO2 Inc.
