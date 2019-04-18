# Management Console for WSO2 Micro Integrator

Command Line tool for managing the WSO2 Micro Integrator

## Getting Started

### Building from source 

- ### Setting up the development environment
    1. Install [Go 1.12.x](https://golang.org/dl)
    2. Fork the [repository](https://github.com/wso2/micro-integrator)
    3. Clone your fork into any directory
    5. `cd` into cloned directory and then cd into `micro-integrator`
    6. Execute `mvn clean install` to build the project
    
- ### Running
    `cd` into `micro-integrator/cmd/bin`
    
    Then execute `./micli` to start the application.
    
    Execute `./micli --help` for further instructions.

### Configuration 
- ### Management API Address and Port
    To configure the address and the port of the Management Api in the CLI use the [**init**](#init) command. This will generate a file called server_config.yaml which contains the address and the port. If the init command was not used, the address and the port will have the default values

    NOTE: The default address is http://localhost and the port is 9091

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
            micli init [flags]

        Flags:
            Required:
                --server, -s
                --port, -p

        Examples:
            micli init -s http://localhost -p 9091
```
   * #### list
```bash
        Usage:
            micli list [COMMANDS] [flags]

        Available Commands:
            apis             List all the APIs
            carbonApps       List all the Carbon Applications
            endpoints        List all the Endpoints
            inboundEndpoints List all the Inbound Endpoints
            sequences        List all the Sequences
            proxyServices    List all the Proxy Services
            services         List all the Services
            tasks            List all the Tasks

        Examples:
            micli list carbonApps
            micli list apis
```

* #### show
```bash
        Usage:
            micli show [COMMANDS] [flags]

        Flags:
            Required:
                --name, -n
               
        Available Commands:
            api             Get information about the specified API
            carbonApp       Get information about the specified Carbon Application
            endpoint        Get information about the specified Endpoint
            inboundEndpoint Get information about the specified Inbound Endpoint
            sequence        Get information about the specified Sequence
            proxyService    Get information about the specified Proxy Service
            service         Get information about the specified Service
            task            Get information about the specified Task
        
        Examples:
            micli show api -n TestAPI
            micli show proxyService -n SampleProxyService
```

* #### version
```bash
        micli version 
```
