# WSO2 Micro Integrator Command Line tool (MI-CLI)

View and manage the micro integrator runtime using the “mi” command line tool. Some of the usages of the command line tool include,
1. Get a list of deployment runtime artifacts
2. Inspect details of each runtime artifact such as a proxy service or an API.
3. Get the invocation endpoint of an artifact

### Running

Extract the compressed archive generated to the desired location.

Add the MI CLI bin folder to PATH in unix-based Operating System (Linux, Solaris and Mac OS X)

`export PATH=/path/to/mi/cli/directory/bin:$PATH`

Then execute, 

`$ mi`

Execute mi --help for further instructions.

### Add Command Autocompletion

Run via curl:
`$ sh -c "$(curl -fsSL https://raw.githubusercontent.com/wso2/micro-integrator/1.0.x/cmd/mi_bash_completion.sh)"`

or wget:
`$ sh -c "$(wget -qO- https://raw.githubusercontent.com/wso2/micro-integrator/1.0.x/cmd/mi_bash_completion.sh)"`

Then, 

`$ source <your path>/mi_bash_completion.sh`

### Configuration

##### Q. How to Enable the Management API

By default the Management API is disabled. To use the Management API you must use the system property `-DenableManagementApi` when starting the micro integrator

**NOTE: These APIs are not protected, do not enable this in production. Take extra measures to secure this port if you are enabling this in production.**

##### Q. Management API Address and Port

To configure the address and the port of the Management API in the CLI use the [**init**](#init) command. This will generate a file called server_config.yaml which contains the address and the port. If the init command was not used, the address and the port will have the default values

NOTE: The default address is https://localhost and the port is 9164

### Usage
```bash
     mi [command]
```

#### Global Flags
```bash
    --verbose
        Enable verbose logs (Provides more information on execution)
    --help, -h
        Display information and example usage of a command
```
#### Commands
* ##### init
```bash
    Usage:
        mi init
    
    Examples:
        mi init
        Enter following parameters to configure the cli
        Host name(default localhost): abc.com
        Port number(default 9164): 9595
        CLI configuration is successful
```
* ##### show
```bash
    Usage:
        mi show [command] [argument] [flag]
               
    Available Commands:
        api [api-name]                  Get information about one or more Apis
        carbonapp [app-name]            Get information about one or more Carbon Apps
        endpoint [endpoint-name]        Get information about one or more Endpoints
        inboundendpoint [inbound-name]  Get information about one or more Inbounds
        proxyservice [proxy-name]       Get information about one or more Proxies 
        sequence [sequence-name]        Get information about one or more Sequences
        task [task-name]                Get information about one or more Task

    You can specify the singular or plural forms for a command. Following commands will have the same output
        mi show api sampleApi
        mi show apis sampleApi      
    argument: specify the name of the artifact. If omitted, summarized details for all artifacts are displayed. For example mi show carbonapps will display a summary of all the carbon apps	
	
    Examples:
        # To list all the apis
        mi show api

        # To get details about specific proxy service
        mi show proxyService SampleProxyService

    Flags:
        -h, --help   Display information and example usage of the command
```
* ##### version
```bash
        mi version 
```