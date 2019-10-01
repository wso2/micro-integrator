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

- ### Command Autocompletion (For Bash Only)
    Copy the file `mi_bash_completion.sh` to `/etc/bash_completion.d/` and source it with `source /etc/bash_completion.d/mi_bash_completion.sh` to enable bash auto-completion.

### Configuration 

- ### How to Enable the Management API
    By default the Management Api is disabled. To use the Management Api you must use the system property `-DenableManagementApi` when starting the micro integrator

- ### Management API Address and Port
    To configure the address and the port of the Management Api in the CLI use the [**remote**](#remote) command. If no configuration is done, the address and the port will have the default values

    NOTE: The default hostname is localhost and the port is 9164.

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
            add [name] [host] [port]        Add a Micro Integrator
            remove [name]                   Remove a Micro Integrator
            update [name] [host] [port]     Update a Micro Integrator
            select [name]                   Select a Micro Integrator instance on which commands are to be executed
            show                            Show added Micro Integrator instances
            login                           Login to the current Micro Integrator instance
            logout                          Logout of the current Micro Integrator instance

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
            
            # login to the current (selected)  Micro Integrator instance
            mi remote login     # will be prompted for username and password
            
            # login (with inline username and password)
            mi remote login username password # or
```
   * #### log-level
```bash
        Usage:
            mi log-level [command] [arguments]

        Available Commands:
            show [logger-name]                   Show information about a logger
            update [logger-name] [log-level]     Update the log level of a logger

        Examples:
            # Show information about a logger
            mi log-level show org.apache.coyote

            # Update the log level of a logger
            mi log-level update org.apache.coyote DEBUG
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
   * #### dataservice
```bash
        Usage:
            mi dataservice [command] [argument]

        Available Commands:
            show [data-service-name]             Get information about one or more Dataservices

        Examples:
            # To List all the dataservices
            mi dataservice show

            # To get details about a specific task
            mi dataservice show SampleDataService
```
   * #### connectors
 ```bash
         Usage:
             mi connector [command]
 
         Available Commands:
             show             Get information about the connectors
 
         Examples:
             # To List all the connectors
             mi connector show
 ```
   * #### templates
 ```bash
         Usage:
             mi template [command] [template-type] [template-name]
 
         Available Commands:
             show  [template-type]                  Get information about the given template type
             show  [template-type] [template-name]  Get information about the specific template
 
         Examples:
             # To List all the templates
             mi template show

             # To List all the templates of given template type
             mi template show endpoint

             # To get details about a specific template
             mi template show endpoint sampleTemplate
 ```
   * #### messageprocessor
 ```bash
         Usage:
             mi messageprocessor [command] [messageprocessor-name]
 
         Available Commands:
             show  [messageprocessor-name]  Get information about one or more Message Processor
 
         Examples:
             # To List all the message processor
             mi messageprocessor show

             # To get details about a specific message processor
             mi messageprocessor show  sampleMessageProcessor
 ```
   * #### messagestore
 ```bash
         Usage:
             mi messagestore [command] [messagestore-name]
 
         Available Commands:
             show  [messagestore-name]  Get information about one or more Message Store
 
         Examples:
             # To List all the message store
             mi messagestore show

             # To get details about a specific message store
             mi messagestore show  sampleMessageStore
 ```
   * #### localentry
 ```bash
         Usage:
             mi localentry [command] [localentry-name]
 
         Available Commands:
             show  [localentry-name]  Get information about one or more Local Entries
 
         Examples:
             # To List all the local entries
             mi localentry show

             # To get details about a specific local entry
             mi localentry show  sampleLocalEntry
 ```
   * #### version
```bash
        mi version 
```
