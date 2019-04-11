# Hello World Sample

This sample implements a simple service exposed via the HTTP protocol. When the service gets invoked, a simple message
is generated and sent back to the client as the response.

## Prerequisites

- JDK 8
- Maven
- Docker

## Running the sample

1. Use maven to build the carbon application for the sample project. Following command will create the file
`hello-world-capp_1.0.0.car` in `hello-world-capp/target` directory.
   ```
   $ mvn clean install
   ```
2. Use docker cli to create the docker image with the CAPP we created in step 1.
   ```
   $ docker build -t wso2-mi-hello-world .
   ```
3. Run the docker image created in step 2.
   ```
   docker run -d -p 8290:8290 wso2-mi-hello-world
   ```
4. Invoke the service using an HTTP client like cURL.
   ```
   curl http://localhost:8290/services/HelloWorld
   ```
