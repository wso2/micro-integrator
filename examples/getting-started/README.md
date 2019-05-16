# Sending a Simple Message to a Service Using Micro Integrator

This guide walks you through a sample scenario of invoking a simple backend via Micro Integrator.

Following are the sections available in this guide.

- [What you'll build](#what-you'll-build)
- [Prerequisites](#prerequisites)
- [Implementation](#implementation)
- [Deployment](#deployment)
- [Testing](#testing)

## What You'll Build

In this guide, you will be creating a composite service which will process order requests upon invocation. The following diagram illustrates the use case clearly.

![scenario](images/scenario.png)

You will be invoking a composite service which wraps the back-end service to process orders. The composite service, upon invocation, will forward the request received without any alteration to the back-end service and will forward the response back to the client intact.   

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

It receives order details in a request and responds by appending the 'orderID', 'price' and 'status' to the order details.

You can build the order process back-end and deploy it in Micro Integrator using the following step:

1. Build the carbon application for the order process back-end by building the project, 'order-process-be' using maven. Following command,
   ```
   $ cd <mi-work-directory>/examples/getting-started/order-process-be
   $ mvn clean install
   ```
   will create the file 'order-processCompositeApplication_1.0.0.car' in `order-processCompositeApplication/target` directory.
 
 #### 2. Create the composite service to invoke the back-end
 
 Throughout this section, you will be creating a composite service using WSO2 EI Tool, deploying it in Micro Integrator. Please follow the below steps to create the composite service.
 
1. [Download and install the EI tool](https://docs.wso2.com/display/EI6xx/Installing+Enterprise+Integrator+Tooling) that is compliant with the distribution.
 
2. Create an ESB solution project as explained [here](https://docs.wso2.com/display/EI6xx/Working+with+EI+Tooling#WorkingwithEITooling-CreatinganESBSolutionProjectCreatinganESBSolutionProject) and name it as 'compositeServiceProject'. Deselect the creation of a 'Registry Project' and a 'Connector Exporter Project' since we will not be needing them for this simple project. 
 
3. Open the project and [create a REST API](https://docs.wso2.com/display/EI6xx/Working+with+EI+Tooling#WorkingwithEITooling-CreatingartifactsforanESBSolutionProject). You can either create an API from scratch and configure it as shown in the following diagram  or directly import the config from [here](composite-service/composite-service/src/main/synapse-config/api/forwardOrderApi.xml).
 ![diagram](images/api-config.png) 
 
4. Save the project and export the composite application to a desired location. (You can refer [How to working with EI Tooling](https://docs.wso2.com/display/EI6xx/Working+with+EI+Tooling#WorkingwithEITooling-PackagingESBartifacts) document for more information about this)

**Note -** Alternatively, you can build the project 'composite-service' using maven and 'composite-serviceCompositeApplication_1.0.0.car' C-App will be located in `<mi-work-directory>/examples/getting-started/composite-service/composite-service-capp/target` when you use the following command.
```
   $ cd <mi-work-directory>/examples/getting-started/composite-service
   $ mvn clean install
   ```
## Deployment

Here we have illustrated three types of deployments from which you can choose one for the rest of this example. Throughout this section, it will be assumed that you have created two C-apps for 'composite-service' and 'order-process-backend' which reside in ```<mi-work-directory>/examples/getting-started/composite-service/composite-service-capp/target``` and ```<mi-work-directory>/examples/getting-started/order-process-be/order-processCompositeApplication/target``` folders.

### Local Deployment

Even though the Micro Integrator integrator is specifically designed to be container native, it is possible for you to deploy services and run the Micro Integrator locally. Since it does not support hot-deployment of artifacts, it is required for you to start/restart the server after copying the artifacts into the correct location so that the services are available for use.

You would be deploying the 2 services, 'order-process-backend' and the 'composite-service' in 2 different WSO2 Micro Integrator instances. Therefore, it is required to have 2 separate distribution of MI. We will be referring to them as MI1 and MI2 in the following sections respectively.

#### Deploying the backend locally

Assume you have followed steps in the section, [Create the order processing back-end.](#1-create-the-order-processing-back-end)
   
1. Copy the `order-process-be/order-processCompositeApplication/target/order-processCompositeApplication_1.0.0.car` into `<MI1_HOME>/repository/deployment/server/carbonapps`

2. Start a Micro Integrator instance(MI1) with a [port offset.](../../doc/configuring-port-offset.md) (The reason why we start the server with a port offset is because we will be starting another instance of WSO2MI for the composite service to as explained in the [section below,](#deploying-the-composite-service-locally) leading to a port conflict.)

   **Note -** If you want to start Micro-Integrator with a portOffset=1 you have to pass the parameter as ```-DportOffset=11```, because Micro-Integrator starts with a default portOffset=10.
      
3. You should be able to access the back-end using the below curl command
   ```
   curl --header "Content-Type: application/json" --request POST  --data '{"store": {"book": [{"author": "Nigel Rees","title": "Sayings of the Century"},{"author": "J. R. R. Tolkien","title": "The Lord of the Rings","isbn": "0-395-19395-8"}]}}' http://localhost:8291/order
   ```
#### Deploying the composite service locally

Assume you have followed steps 1-4 in the section [Create the composite service to invoke the back-end.](#2-create-the-composite-service-to-invoke-the-back-end)

1. Copy the `composite-service/composite-service-capp/target/composite-serviceCompositeApplication_1.0.0.car` into `<MI2_HOME>/repository/deployment/server/carbonapps`

2. Start the Micro Integrator instance(MI2).

You have successfully deployed 'order-process-backend' and the 'composite-service' services locally. Now you can test your deployments by using [Testing](#Testing) section.

### Docker Deployment

We assume that you have followed all the steps in the section [Create the order processing back-end](#1-create-the-order-processing-back-end) and [Create the composite service to invoke the back-end.](#2-create-the-composite-service-to-invoke-the-back-end)
In [Create the composite service to invoke the back-end](#2-create-the-composite-service-to-invoke-the-back-end) step 3 you need to replace 'order-process-backend' endpoint address in [forwardOrderApi.xml](https://github.com/wso2/micro-integrator/blob/master/examples/getting-started/composite-service/composite-service/src/main/synapse-config/api/forwardOrderApi.xml) file as follows because in Docker containers, each container has its own localhost.
```xml
<endpoint>
    <address uri="http://backend:8290/order"/>
</endpoint>
```

Here we are going to use **docker-compose** command for docker deployment. Therefore, we need a ```docker-compose.yml``` file which includes all the details about the Docker containers. This is the docker-compose.yml file we are going to use in this example.    

```yml
version: "3.7"
services:
  service:
    image: wso2/mi-getting-started:latest
    build:
        context: ./composite-service
        dockerfile: Dockerfile
    ports:
      - 8290:8290
  backend:
    container_name: backend
    image: wso2/mi-order-process-be:latest
    build:
        context: ./order-process-be
        dockerfile: Dockerfile
    expose:
      - 8290
    ports:
      - 8291:8290
```

We have already added this file in to the ```<mi-work-directory>/examples/getting-started/``` directory. You can use the following command to navigate to that file and execute it.
   ```
   $ cd <mi-work-directory>/examples/getting-started/
   $ docker-compose up -d
   ```
You have successfully deployed 'order-process-backend' and the 'composite-service' services in docker. Now you can test your deployments by using [Testing](#Testing) section.


### Kubernetes Deployment

Please follow all the steps in the section [Create the order processing back-end](#1-create-the-order-processing-back-end) and
[Create the composite service to invoke the back-end.](#2-create-the-composite-service-to-invoke-the-back-end) if you have not already done. In step 3 of
[Create the composite service to invoke the back-end](#2-create-the-composite-service-to-invoke-the-back-end), you need
to replace 'order-process-backend' endpoint address in [forwardOrderApi.xml](https://github.com/wso2/micro-integrator/blob/master/examples/getting-started/composite-service/composite-service/src/main/synapse-config/api/forwardOrderApi.xml) file as follows because in docker containers each container has their own localhost.
```xml
<endpoint>
    <address uri="http://order-process-be-service:8290/order"/>
</endpoint>
```

We are using [Minikube](https://github.com/kubernetes/minikube) to test deploying to a Kubernetes cluster in this
example. We assume that you already have a working minikube setup locally. If not please follow the
[installation guide](https://kubernetes.io/docs/tasks/tools/install-minikube/).

**Note**: You have to build the docker image while the docker CLI is using the Minikube’s built-in Docker daemon.
Otherwise, the docker image will not be available in the Minikube environment. You can execute `eval $(minikube
docker-env)` to use the Minikube’s built-in Docker daemon. Please refer
[Use local images by re-using the Docker daemon](https://kubernetes.io/docs/setup/minikube/#use-local-images-by-re-using-the-docker-daemon)
for more details.

1. Create docker images for the order processing back-end and the composite service to invoke the back-end.

   ```
   $ docker build -t wso2/mi-order-process-be order-process-be/
   $ docker build -t wso2/mi-getting-started composite-service/
   ```

2. The [k8s-deployment.yaml](k8s-deployment.yaml) file is the Kubernetes artifact descriptor used to deploy the micro
integrator service in a Kubernetes cluster. You can use `kubectl create` to deploy.

    ```
    $ kubectl create -f k8s-deployment.yaml
    ```

3. Check whether all the Kubernetes artifacts are deployed successfully by executing the following command.
    ```
    $ kubectl get all

    NAME                                                 READY   STATUS    RESTARTS   AGE
    pod/mi-getting-started-deployment-5b459bc5b9-w5r2q   1/1     Running   0          87m
    pod/mi-order-process-be-deployment-bdd8b577d-z2zn9   1/1     Running   0          87m

    NAME                                 TYPE        CLUSTER-IP       EXTERNAL-IP   PORT(S)          AGE
    service/kubernetes                   ClusterIP   10.96.0.1        <none>        443/TCP          105d
    service/mi-getting-started-service   NodePort    10.101.64.224    <none>        8290:32100/TCP   87m
    service/order-process-be-service     ClusterIP   10.106.186.211   <none>        8290/TCP         87m

    NAME                                             READY   UP-TO-DATE   AVAILABLE   AGE
    deployment.apps/mi-getting-started-deployment    1/1     1            1           87m
    deployment.apps/mi-order-process-be-deployment   1/1     1            1           87m

    NAME                                                       DESIRED   CURRENT   READY   AGE
    replicaset.apps/mi-getting-started-deployment-5b459bc5b9   1         1         1       87m
    replicaset.apps/mi-order-process-be-deployment-bdd8b577d   1         1         1       87m

    ```

4. Follow the section on [Testing](#testing). Please note that you will have to use the following URL to invoke the
   composite service
   ```
   http://MINIKUBE_IP:32100/forward
   ```

## Testing
#### Verifying the invocation

Invoke the composite service using the following curl command
```
curl --header "Content-Type: application/json" --request POST   --data '{"store": {"book": [{"author": "Nigel Rees","title": "Sayings of the Century"},{"author": "J. R. R. Tolkien","title": "The Lord of the Rings","isbn": "0-395-19395-8"}]}}' http://localhost:8290/forward
```
Upon invocation, you should be able to observe the following response
 ```
{
	"orderDetails":{"store": {"book": [{"author": "Nigel Rees","title": "Sayings of the Century"},{"author": "J. R. R. Tolkien","title": "The Lord of the Rings","isbn": "0-395-19395-8"}]}},
	"orderID":"1a23456",
	"price":25.65,
	"status":"successful"
}
```

**Note**: Please use `http://MINIKUBE_IP:32100/forward` to invoke the composite service if you have deployed in Kubernetes.

#### Debugging the mediation

Alternatively, the Integrator Tool brings in the capability of debugging the mediation flow with the tool interactively. For more information on debugging the mediation flow, please refer to [this blog.](https://medium.com/@rosensilva/debugging-integration-flows-using-wso2-enterprise-integrator-16bc127732d)

## Tracing and Monitoring with Micro Integrator

#### Monitoring Statistics with Prometheus

Please refer the section [Configuring and Monitoring statistics using Prometheus](
../../doc/working-with-the-micro-integrator.md#configuring-and-Monitoring-statistics-using-Prometheus) for more 
information on 
this. 
 
