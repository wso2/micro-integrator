# Working with the Micro Integrator

The Micro Integrator is designed in a highly container-friendly manner, and thereby, it is ideal for you to use it for
Microservices Architecture (MSA)-based solutions, which are deployed in container-based environments. Because its
start-up time is faster than the ESB profile, the Micro Integrator profile allows you to perform advanced integration
scenarios without sacrificing the speed required for a container-based deployment architecture.

For information on what the Micro Integrator profile brings with this release, see
[releases](https://github.com/wso2/micro-integrator/releases).

## Micro Integrator in a VM

### Running Micro Integrator

You can download the product distribution from the [releases](https://github.com/wso2/micro-integrator/releases) page or
 using the (product installers)[https://docs.wso2.com/display/EI6xx/Installing+the+Product]. If you chose to download
 the product distribution you will have to unzip it and put it in a preferred location (e.g.
 /usr/lib/wso2/wso2mi-1.0.0). Let's call this location MI_HOME in the rest of the section. You can use the following
 command depend on the platform to start Micro Integrator.

 - MacOS/Linux/CentOS - `sh MI_HOME/bin/micro-integrator.sh`
 - Windows - `MI_HOME/bin/micro-integrator.bat`

By default the HTTP listener port is 8290 and the default HTTPS listener port is 8253

### Stopping Micro Integrator

To stop the Micro Integrator runtime, press Ctrl+C in the command window.

### Deploying artifacts in the Micro Integrator profile

After creating your artifacts in an ESB Solutions Project via WSO2 EI Tooling, import the project as a Composite
Application, and add the CAR file to the <MI_HOME>/wso2/micro-integrator/repository/deployment/server/carbonapps
directory to deploy.

Restart the Micro Integrator profile after deploying the artifacts.

### Configuring the Micro Integrator profile

All configuration files related to the Micro Integrator profile are located in the <MI-HOME>/wso2/micro-integrator/conf
directory.

#### Configuring the file-based registry

The H2 database-based registry is not available in the Micro Integrator profile. Instead, it has a file system based
registry, which provides the same functionality. Thus, by default, the <MI_HOME>/wso2/micro-integrator/registry
directory will act as the registry to store registry artifacts etc. This main registry directory will consist of the
following sub-registry directories.

These sub-registry directories will be created automatically when you deploy registry artifacts. If you did not deploy
any registry artifacts, you can create them manually.

- Local: To store local artifacts of the product server that are not shared with the other products in the deployment.
- Config: To store all product-specific artifacts that are shared between similar product instances.
- Governance: To store all artifacts that are relevant to the governance of the product.

If you want to change the default locations of the registry directories, uncomment and change the following configuration
 in the <MI_HOME>/wso2/microIntegrator/repository/deployment/server/synapse-config/default/directoryregistry.xml file.

```xml
<registry xmlns="http://ws.apache.org/ns/synapse" provider="org.wso2.carbon.mediation.registry.MicroIntegratorRegistry">
    <parameter name="cachableDuration">15000</parameter>
    <!--
        Uncomment below parameters (ConfigRegRoot, GovRegRoot, LocalRegRoot) to configure registry root paths
        Default : <EI_HOME>/wso2/micro-integrator/registry/{governance | config | local}
    -->
    <!--
    <parameter name="ConfigRegRoot">{Root directory path for configuration Registry}</parameter>
    <parameter name="GovRegRoot">{Root directory path for governance Registry}</parameter>
    <parameter name="LocalRegRoot">{Root directory path for local Registry}</parameter>
    -->
</registry>
```

