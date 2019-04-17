# Sending a Simple Message to a Service Using Micro Integrator

This guide walks you through a sample scenario of invoking a simple backend via Micro Integrator.

Following are the sections available in this guide.

- What you'll build
- Prerequisites
- Implementation
- Deployment
- Testing

## What You'll Build

During this guide, you will be creating a composite service which, upon invocation will call a that which processes order requests. The following diagram illustrates the use case clearly.

![scenario](images/scenario.png)

You will be invoking a composite service which wraps the back-end service to process orders. The composite service, upon invocation will forward the request received without any alteration to the back-end service and will forward the response back to the client intact.   

## Prerequisites

- JDK 8
- Maven
- Docker - version 17.09.0-ce or higher
- [Micro Integrator distribution](https://github.com/wso2/micro-integrator)
- [Integrator Tooling Distribution](https://wso2.com/integration/tooling/)

## Implementation

You will be following the below steps in building the scenario:
1. Create the order processing back-end
2. Create the composite service to invoke the back-end

#### 1. Create the order processing back-end
 
The behaviour of the order processing back-end, in a nutshell, is shown below.

![backend](images/backend.png)

It receives order details in a request and and responds by appending the 'orderID', 'price' and 'status' to the order details.

You can build the order process back-end and deploy it in Micro Integrator using the following steps:

1. Build the carbon application for the order process back-end by building the project, 'order-process-be' using maven. Following command,
   ```
   $ cd <mi-work-directory>/examples/getting-started/
   $ mvn clean install
   ```
   will create the file 'order-processCompositeApplication_1.0.0.car' in `order-process-be/order-processCompositeApplication/target` directory.
 
2. Use docker cli to create the docker image with the CAPP we created.(Please note that the distribution must be built with -Ddocker.skip=false prior to this step, for the base image to be created)
   ```
   $ docker build -t wso2/mi-order-process-be .
   ```
3. Run the docker image created in the previous step.
   ```
   docker run -d -p 8291:8290 wso2/mi-order-process-be
   ```
   
    If you start the Micro Integrator now, you should be able to access the back-end using the below URL
    ```
    http://localhost:8291/order
    ```

 #### 2. Build the composite service to invoke the back-end
 
 Throughout this section, you will be creating a composite service using WSO2 EI Tool, deploying it in Micro Integrator and running it in Docker. Please follow the below steps to create the composite service.
 
1. [Download and install the EI tool](https://docs.wso2.com/display/EI6xx/Installing+Enterprise+Integrator+Tooling) that is compliant with the distribution.
 
2. Create an ESB solution project as explained [here](https://docs.wso2.com/display/EI6xx/Working+with+EI+Tooling#WorkingwithEITooling-CreatinganESBSolutionProjectCreatinganESBSolutionProject) and name it as 'compositeServiceProject'. Deselect the creation of a 'Registry Project' and a 'Connector Exporter Project' since we will not be needing them for this simple project. 
 
3. Open the project and [create a REST API](https://docs.wso2.com/display/EI6xx/Working+with+EI+Tooling#WorkingwithEITooling-CreatingartifactsforanESBSolutionProject). You can either create an API from scratch and configure it as shown in the following diagram  or directly import the config from [here](composite-service/composite-service/src/main/synapse-config/api/forwardOrderApi.xml).
 
 ![diagram](images/api-config.png) 
 
4. Save the project and export the composite application to a desired location. (Alternatively you can build the project 'composite-service' using maven and the CApp will be located in `composite-service/composite-service-capp/target`. The rest of the document will assume that the Capp resides in `composite-service/composite-service-capp/target`
 
5. Use docker cli to create the docker image with the both back-end and composite service CAPPs we created
   ```
   $ cd <mi-work-directory>/examples/getting-started/composite-service
   $ docker build -t wso2/mi-getting-started .
   ```
6. Run the docker image created in the previous step.
   ```
   docker run -d -p 8290:8290 wso2/mi-getting-started
   ```
## Deployment
#### Local Deployment


## 5. Testing
#### Verifying the invocation

Invoke the composite service using the following curl command
```
curl --header "Content-Type: application/json" --request POST   --data '{"store": {"book": [{"author": "Nigel Rees","title": "Sayings of the Century"},{"author": "J. R. R. Tolkien","title": "The Lord of the Rings","isbn": "0-395-19395-8"}]}}' http://localhost:8290/forward
```
Upon invocation you should be able to observe the following response
 ```
{
	"orderDetails":{"store": {"book": [{"author": "Nigel Rees","title": "Sayings of the Century"},{"author": "J. R. R. Tolkien","title": "The Lord of the Rings","isbn": "0-395-19395-8"}]}},
	"orderID":"1a23456",
	"price":25.65,
	"status":"successful"
}
```
#### Debugging the mediation

Alternatively, the Integrator Tool brings in the capability of debugging the mediation flow with the the tool interactively. for more information on debugging the mediation flow, please refer to [this blog.](https://medium.com/@rosensilva/debugging-integration-flows-using-wso2-enterprise-integrator-16bc127732d)

 
 