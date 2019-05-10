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
    
- ### Building from source
    cd into micro-integrator home directory

    Execute `make install` to build both the **Micro Integrator** and **MIcro Integrator CLI** at once.

    Created Command Line tool packages will be available at cmd/build directory

- ### Running
    Extract the compressed archive generated to a desired location.
    
    Then execute `./mi` to start the application.
    
    Execute `./mi --help` for further instructions.

    NOTE: To execute the tool from anywhere, append the location of the executable (mi) to your $PATH variable.

- ### Command Autocompletion
    Copy the file mi_bash_completion.sh to /etc/bash_completion.d/ and source it with source /etc/bash_completion.d/mi_bash_completion.sh to enable bash auto-completion.

### Configuration 

- ### How to Enable the Management API
    By default the Management Api is disabled. To use the Management Api you must use the system property `-DenableManagementApi` when starting the micro integrator

- ### Management API Address and Port
    To configure the address and the port of the Management Api in the CLI use the [**init**](#init) command. This will generate a file called server_config.yaml which contains the address and the port. If the init command was not used, the address and the port will have the default values

    NOTE: The default address is https://localhost and the port is 9164

## Usage 
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

### Commands
   * #### init
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

* #### show
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
        
        Examples:
            # To list all the apis
            mi show api

            # To get details about specific proxy service
            mi show proxyService SampleProxyService

        Flags:
            -h, --help   Display information and example usage of the command
```

* #### version
```bash
        mi version 
```
