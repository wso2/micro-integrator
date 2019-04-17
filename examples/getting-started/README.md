# Sending a Simple Message to a Service Using Micro Integrator

This guide walks you through a sample scenario of invoking a simple order processing service via Micro Integrator.

## Prerequisites

- JDK 8
- Maven
- Docker

## Building the scenario

This documentation walks you through the following steps in building the scenario:
1. Build the order processing back-end
2. Build the composite service to invoke the back-end
3. Invoke the composite service


### 1. Build the order processing back-end
 
 The order processing back-end receives a request of the following format.
 ```
{
    "store": {
        "book": [
            {
                "author": "Nigel Rees",
                "title": "Sayings of the Century"
            },
            {
                "author": "J. R. R. Tolkien",
                "title": "The Lord of the Rings",
                "isbn": "0-395-19395-8"
            }
        ]
    }
}

```
Upon invocation, it extracts the order information using the "order" details and responds in the following format.
```
{
    "orderDetails": {
        "store": {
            "book": [
                {
                    "author": "Nigel Rees",
                    "title": "Sayings of the Century"
                },
                {
                    "author": "J. R. R. Tolkien",
                    "title": "The Lord of the Rings",
                    "isbn": "0-395-19395-8"
                }
            ]
        }
    },
    "orderID": "1a23456",
    "price": 25.65,
    "status": "successful"
}
```
You can build the order process back-end and deploy it in Micro Integrator using the following steps:

1. Build the carbon application for the order process back-end by building the project, 'order-process-be' using maven. Following command,
   ```
   $ mvn clean install
   ```
   will create the file 'order-processCompositeApplication_1.0.0.car' in `order-process-be/order-processCompositeApplication/target` directory.
 
2. Use docker cli to create the docker image with the CAPP we created.(Please note that the distribution must be built with -Ddocker.skip=false prior to this step, for the base image to be created)
   ```
   $ docker build -t wso2-mi-order-process-be .
   ```
3. Run the docker image created in the previous step.
   ```
   docker run -d -p 8290:8290 wso2-mi-order-process-be
   ```
   
    If you start the Micro Integrator now, you should be able to access the back-end using the below URL
    ```
    http://localhost:8290/order
    ```

 ### 2. Build the composite service to invoke the back-end
 
 Throughout this section, you will be creating a composite service using WSO2 EI Tool, deploying it in Micro Integrator and running it in Docker. Please follow the below steps to create the composite service.
 
1. [Download and install the EI tool](https://docs.wso2.com/display/EI6xx/Installing+Enterprise+Integrator+Tooling) that is compliant with the distribution.
 
2. Create an ESB solution project as explained [here](https://docs.wso2.com/display/EI6xx/Working+with+EI+Tooling#WorkingwithEITooling-CreatinganESBSolutionProjectCreatinganESBSolutionProject) and name it as 'compositeServiceProject'. Deselect the creation of a 'Registry Project' and a 'Connector Exporter Project' since we will not be needing them for this simple project. 
 
3. Open the project and [create a REST API](https://docs.wso2.com/display/EI6xx/Working+with+EI+Tooling#WorkingwithEITooling-CreatingartifactsforanESBSolutionProject). You can either create an API from scratch and configure it as shown in the following diagram
 
 ![diagram](images/api-config.png) 
 
 or directly import the config from [here](composite-service/composite-service/src/main/synapse-config/api/forwardOrderApi.xml).
 
4. Save the project and export the composite application to a desired location. (Alternatively you can build the project 'composite-service' using maven and the CApp will be located in `composite-service/composite-service-capp/target`. The rest of the document will assume that the Capp resides in `composite-service/composite-service-capp/target`
 
5. Use docker cli to create the docker image with the both back-end and composite service CAPPs we created
   ```
   $ docker build -t wso2-mi-getting-started .
   ```
6. Run the docker image created in the previous step.
   ```
   docker run -d -p 8290:8290 wso2-mi-getting-started
   ```
### 3. Invoke the composite service

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

 
 