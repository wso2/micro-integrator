# Registry Migration from the EI 6.x.x to EI 7.1
 
## Introduction
The following instructions guide you through migrating the Registry from EI-6.x.x releases to EI-7.1.0 (MI-1.2.0). 
In EI 6.x.x releases, the registry resources were stored in an in-memory database (H2). However, EI 7.0 and EI 7.1 use a file-based registry to store the registry resources. In a case of a migration, this tool allows you to migrate all the registry resources deployed in an EI-6.x.x release to EI-7.x releases by exporting them as a Registry Resource Project or as a Carbon Application (.car file).

## Instructions
1.  Clone the wso2/micro-integrator repo and build the registry-migration-service module in the project.

2.  Copy the registry-migration-service-<version>.jar into an executable folder.

3.  Execute the following command to run the client jar. You can give the System property `log.file.location` to specify a location for the log file. Otherwise, the log file will be created at the jar execution location.

    -   `java -jar <path_to_jar>/registry-migration-service-<version>.jar`
    
    -   `java -Dlog.file.location=<log_file_location> -jar <path_to_jar>/registry-migration-service-<version>.jar`
    
4.  Once you execute the above command, you will be prompted to a list of inputs as follows.

    -   `Please Enter EI Server URL (https://localhost:9443):`  - Enter the EI server URL with the servelet port. The default will be https://localhost:9443.
    
    -   `Please Enter Internal Truststore Location of EI Server:` - Enter the location of the internal Truststore used in the EI server.
    
    -   `Please Enter Internal Truststore Type of EI Server (JKS):` - Enter the type of the internal Truststore used in the EI server. The default will be JKS.
    
    -   `Please Enter Internal Truststore Password of EI Server:` - Enter the password of the internal Truststore used in the EI server.
    
    -   `Please enter the following admin credentials of the EI server.`
    
        -   `Enter username: `
        
        -   `Enter password:`
        
5.  Once the login is successfully completed, you will be asked to choose the option between exporting the registry resources as Registry Resource Project and Carbon Application.

    -   `Select one of the below options`
    
        -   `[1]  Export as Registry Resource Project`
        
        -   `[2]  Export as Carbon Application`
        
        -   `[3]  Exit`
        
    -   `Please enter your numeric choice:`  - Enter a valid number among 1, 2, 3.
   
6.  If the option \[1] Export as Registry Resource Project" is selected, then you will be prompted to the below inputs.

    -   `Please enter Integration Project name:` 
    
    -   `Please enter Group Id (com.example):` - Default will be com.example
    
    -   `Please enter Artifact Id:`  - Default will be the Integration Project name
    
    -   `Please enter Version (1.0.0):` - Default will be 1.0.0
    
    -   `Please enter export destination:`
   
7.  If the option \[2] Export as Carbon Application is selected, then you will be prompted to the below inputs.

    -   `Please enter CAR name:`
    
    -   `Please enter CAR version (1.0.0):` - Default will be 1.0.0
    
    -   `Please enter export destination:`
   
8.  After successfully entering the above inputs and successful execution, the Registry Resource Project or the Carbon Application will be created at the given export location. 

9.  Also, there will be a summary report created at the export location with a file name format similar to registry_export_summary_<date>.txt
