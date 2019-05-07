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
   `cd` into `micro-integrator/cmd`
   
    Execute `./build.sh -t micli.go -v 1.0.0 -f` to build for all platforms. (Can only be built in Mac or Linux)
      
    Created packages will be available at `build/target` directory
    
- ### Running
    Extract the compressed archive generated to a desired location.
    
    Then execute `./micli` to start the application.
    
    Execute `./micli --help` for further instructions.
    
    NOTE: To execute the tool from anywhere, append the location of the executable (micli) to your $PATH variable.

### Configuration 
- ### Inbound Listening Port
    The default listening port of the Management API is `9091`

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
