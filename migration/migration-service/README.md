# Migrate from the Previous Releases

The following instructions guide you through migrating the encrypted passwords in WSO2 Micro-Integrator-1.1.0 to Micro-Integrator-1.2.0. 
In version 1.1.0, secure-vault was used to store sensitive information used in synapse configurations and cipher-tool was used for
sensitive server information. In WSO2 Micro-integrator 1.2.0 all sensitive information (server related and synapse configuration) can simply
be encrypted and stored using cipher-tool without having to differentiate. 
This tool allows you to decrypt the existing passwords in secure-vault as well as cipher-tool. In a case of migrations, you can use
this migration tool to get the plain text values of your existing passwords. The obtained plain-text values can be added into the
[secrets] section of the deployment.toml of WSO2 Micro-Integrator-1.2.0 and get re-encrypted by running the ciphertool.sh -Dconfigure
command from the <MI-HOME>/bin directory.

## Instructions

##### For migration from MI-1.1.0

1. Build the migration-service module in the project.

2. WUM Update your existing MI-1.1.0 distribution (if not already updated)

3. Copy the org.wso2.mi-1.1.0.jar into the dropins folder of your updated MI-1.1.0

4. Start the server with the migrate.from.product.version system property set as follows.

    a. Linux/Unix.
   - `sh micro-integrator.sh -Dmigrate.from.product.version=110`

    b. Windows.
   - `micro-integrator.bat -Dmigrate.from.product.version=110`


5. Upon successful execution the decrypted (plain-text) values of secure-vault.properties and cipher-text.properties will 
be written respectively to <MI-HOME>/migration/secure-vault-decrypted.properties and 
<MI-HOME>/migration/cipher-text-decrypted.properties