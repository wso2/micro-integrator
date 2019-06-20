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
   * #### remote
```bash
        Usage:
            mi remote [command] [arguments]
                       
        Available Commands:
            add [nick-name] [host] [port]        Add a Micro Integrator
            remove [nick-name]                   Remove a Micro Integrator
            update [nick-name] [host] [port]     Update a Micro Integrator
            select [nick-name]                   Select a Micro Integrator on which commands are executed
            show                                 Show available Micro Integrators

        Examples:
            # To add a Micro Integrator
            mi remote add TestServer 192.168.1.15 9164
            
            # To remove a Micro Integrator
            mi remote remove TestServer
            
            # To update a Micro Integrator
            mi remote update TestServer 192.168.1.17 9164
            
            # To select a Micro Integrator
            mi remote select TestServer
            
            # To show available Micro Integrators
            mi remote show
```
   * #### api
```bash
        Usage:
            mi api [command] [argument]

        Available Commands:
            show [api-name]                      Get information about one or more Apis

        Examples:
            # To List all the apis
            mi api show

            # To get details about a specific api
            mi api show sampleApi
```
   * #### compositeapp
```bash
        Usage:
            mi compositeapp [command] [argument]

        Available Commands:
            show [app-name]                      Get information about one or more Composite apps

        Examples:
            # To List all the composite apps
            mi compositeapp show

            # To get details about a specific composite app
            mi compositeapp show sampleApp
```
   * #### endpoint
```bash
        Usage:
            mi endpoint [command] [argument]

        Available Commands:
            show [endpoint-name]                 Get information about one or more Endpoints

        Examples:
            # To List all the endpoints
            mi endpoint show

            # To get details about a specific endpoint
            mi endpoint show sampleEndpoint
```
   * #### inboundendpoint
```bash
        Usage:
            mi inboundendpoint [command] [argument]

        Available Commands:
            show [inboundendpoint-name]          Get information about one or more Inbounds

        Examples:
            # To List all the inbound endpoints
            mi inboundendpoint show

            # To get details about a specific inbound endpoint
            mi inboundendpoint show sampleEndpoint
```
   * #### proxyservice
```bash
        Usage:
            mi proxyservice [command] [argument]

        Available Commands:
            show [proxyservice-name]             Get information about one or more Proxies

        Examples:
            # To List all the proxy services
            mi proxyservice show

            # To get details about a specific proxy service
            mi proxyservice show sampleProxy
```
   * #### sequence
```bash
        Usage:
            mi sequence [command] [argument]

        Available Commands:
            show [sequence-name]                 Get information about one or more Sequences

        Examples:
            # To List all the sequences
            mi sequence show

            # To get details about a specific sequence
            mi sequence show sampleProxy
```
   * #### task
```bash
        Usage:
            mi task [command] [argument]

        Available Commands:
            show [task-name]                     Get information about one or more Tasks

        Examples:
            # To List all the tasks
            mi task show

            # To get details about a specific task
            mi task show sampleProxy
```
* #### version
```bash
        mi version 
```
