# Management Console for WSO2 Micro Integrator

Command Line tool for managing the WSO2 Micro Integrator

## Getting Started

### Building from source 

- ### Setting up the development environment
    1. Install [Go 1.12.x](https://golang.org/dl)
    2. Fork the [repository](https://github.com/wso2/micro-integrator)
    3. Clone your fork into any directory
    5. `cd` into cloned directory and then cd into `micro-integrator/cmd`
    6. Execute `go mod vendor` or `go mod download` to download all the dependencies
    
- ### Building
    cd into micro-integrator home directory

    Execute `make install` to build both the **Micro Integrator** and **MIcro Integrator CLI** at once.

    Created packages will be available at build/target directory

- ### Running
    Extract the compressed archive generated to a desired location.
    
    Then execute `./micli` to start the application.
    
    Execute `./micli --help` for further instructions.

    NOTE: To execute the tool from anywhere, append the location of the executable (micli) to your $PATH variable.

### Configuration 

- ### How to Enable the Management API
    By default the Management API is disabled. To use the Management API you must use the system property `-DenableManagementInternalAPI` when starting the micro integrator

- ### Management API Address and Port
    To configure the address and the port of the Management Api in the CLI use the [**init**](#init) command. This will generate a file called server_config.yaml which contains the address and the port. If the init command was not used, the address and the port will have the default values

    NOTE: The default address is http://localhost and the port is 9191

## Usage 
```bash
     micli [command]
```

#### Global Flags
```bash
    --verbose
        Enable verbose logs (Provides more information on execution)
    --help, -h
        Display information and example usage of a command
```

### Commands
   * #### init
```bash
        Usage:
            micli init

        Examples:
            micli init
            Follow the instructions below to configure the CLI
            Enter Host name: abc.com
            Enter Port number: 9595
            CLI configuration is successful
```

* #### show
```bash
        Usage:
            micli show [command] [argument] [flag]
               
        Available Commands:
            api [api-name]                  Get information about the API specified by argument [apiname]
                                            If not specified, list all the apis
            carbonapp [app-name]            Get information about the Carbon App specified by argument [appname]
                                            If not specified, list all the carbon apps
            endpoint [endpoint-name]        Get information about the Endpoint specified by argument [endpointname]
                                            If not specified, list all the endpoints
            inboundendpoint [inbound-name]  Get information about the Inbound specified by argument [inboundname]
                                            If not specified, list all the inbound endpoints
            proxyservice [proxy-name]       Get information about the Proxy specified by argument [proxyname]
                                            If not specified, list all the proxies
            sequence [sequence-name]        Get information about the Sequence specified by argument [sequencename]
                                            If not specified, list all the sequences
            task [task-name]                Get information about the Task specified by argument [taskname]
                                            If not specified, list all the tasks
        
        Examples:
            # To list all the apis
            micli show api

            # To get details about specific proxy service
            micli show proxyService SampleProxyService

        Flags:
            -h, --help   Display information and example usage of the command
```

* #### version
```bash
        micli version 
```