# Data Mapper Config Migration Tool for WSO2 Micro Integrator

Saxon version is upgraded and from MI 4.2.0 onwards. As per the issue discussed in https://saxonica.plan.io/issues/4104
the reserved namespaces should be updated in the default xslt stylesheet used in data mapper transformations.
This tool updates the xslt stylesheet of the carbon applications.

## How to run the tool

1. Build DataMapperConfigMigrator using maven

`mvn clean package`

2. Copy the set of carbon applications which uses data mapper to a different location.

3. Run the jar file providing the location of carbon applications(which uses data mapper)

`java -jar target/data-mapper-config-migration-service-1.0.jar <Absolute_Path_To_Current_Capp_Location>`

4. The migrated capps will be available in a folder names `migrated_capps`.

(c) Copyright 2022 WSO2 LLC.
