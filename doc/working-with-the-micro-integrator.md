# Working with the Micro Integrator

The Micro Integrator is designed in a highly container-friendly manner, and thereby, it is ideal for you to use it for
Microservices Architecture (MSA)-based solutions, which are deployed in container-based environments. Because its
start-up time is faster than the ESB profile, the Micro Integrator profile allows you to perform advanced integration
scenarios without sacrificing the speed required for a container-based deployment architecture.

For information on what the Micro Integrator profile brings with this release, see
[releases](https://github.com/wso2/micro-integrator/releases).

## Micro Integrator in a VM

### Running Micro Integrator

You can download the product distribution from the [releases](https://github.com/wso2/micro-integrator/releases) page or
 using the [product installers](https://docs.wso2.com/display/EI6xx/Installing+the+Product). If you chose to download
 the product distribution you will have to unzip it and put it in a preferred location (e.g.
 /usr/lib/wso2/wso2mi-1.0.0). Let's call this location MI_HOME in the rest of the document. You can use the following
 command depend on the platform to start Micro Integrator.

 - MacOS/Linux/CentOS - `sh <MI_HOME>/bin/micro-integrator.sh`
 - Windows - `<MI_HOME>/bin/micro-integrator.bat`

By default the HTTP listener port is 8290 and the default HTTPS listener port is 8253

### Stopping Micro Integrator

To stop the Micro Integrator runtime, press Ctrl+C in the command window.

### Deploying artifacts in the Micro Integrator profile

WSO2 Micro Integrator allows you to perform all your integration needs with the use of ESB artifacts, which could be within a wide range of APIs, services, endpoints, tasks and so on. An artifact comprises of a set of configurations which defines the request/response flow where, the configuration is based on [Apache Synapse](http://synapse.apache.org/userguide/config.html).

WSO2 EI tool is specifically designed with the capability of designing, developing, testing and deploying artifacts required to perform your integration. You can develop your integration solution in an [ESB Solutions Project](https://docs.wso2.com/display/EI6xx/Working+with+EI+Tooling#WorkingwithEITooling-CreatinganESBSolutionProjectCreatinganESBSolutionProject) via WSO2 EI Tooling, import the project as a [Composite
Application](https://docs.wso2.com/display/ADMIN44x/Working+with+Composite+Applications), and add the CAR file to the `<MI_HOME>/wso2/micro-integrator/repository/deployment/server/carbonapps`
directory to deploy.

Note: WSO2 Micro Integrator does not support hot deployment. Therefore, you need to restart the Micro Integrator after copying the artifacts, in order to get them deployed.

### Configuring the Micro Integrator profile

All configuration files related to the Micro Integrator profile are located in the `<MI-HOME>/wso2/micro-integrator/conf`
directory.

## Micro Integrator with Docker

Micro Integrator is also distributed as a base docker image which you can use to create a deployable docker image with
the required integration artifacts and configuration. You can follow the instructions in [Building the Docker Image](.
./#building-the-docker-image) to build the Micro Integrator docker image and publish it in the local docker registry.

### Deploying artifacts in the Micro Integrator profile

Micro Integrator docker image reads and deploys carbon application in the
`/home/wso2ei/wso2mi/wso2/micro-integrator/repository/deployment/server/carbonapps` directory. Therefore you can use a
simple Docker file like the following to create a docker image with the integration artifacts.

```docker
FROM wso2/micro-integrator:latest

COPY hello-world-capp_1.0.0.car /home/wso2ei/wso2mi/wso2/micro-integrator/repository/deployment/server/carbonapps
```

Then you can simply use the docker CLI to create the docker image.

```
$ docker build -t wso2-mi-hello-world .
```

### Running Micro Integrator

To start the docker image with the artifacts we can use the docker CLI as following.

```
docker run -d -p 8290:8290 -p 8253:8253 --name=wso2-mi-container wso2-mi-hello-world
```

### Stopping Micro Integrator

To stop the container you can use the docker cli command `docker container stop` command.

```
$ docker container stop wso2-mi-container
```

### Configuring the Micro Integrator profile

All configuration files related to the Micro Integrator profile are located in the
`/home/wso2ei/wso2mi/wso2/micro-integrator/conf` directory. You can either mount a volume to that location or copy
required configurations to modify the defaults in the Micro Integrator base image.

### Trying out a sample scenario

For instructions on trying out a simple use case using the micro Integrator profile, see
[Hello world sample](examples/hello-world).

## Micro Integrator with Kubernetes

Kubernetes is an open-source container orchestration system for automating application deployment, scaling, and
management. This section has some guidelines you can follow, if you are planning to use a Kubernetes cluster to deploy
the Micro Integrator solutions.

First of all, you will have to create a custom docker image with the required synapse artifacts, configurations and
third-party dependencies. You can follow the section on [Micro Integrator with Docker](#micro-integrator-with-docker)
for instructions about creating a custom docker image using the available base Micro Integrator docker image. We can
then use this custom image to deploy the Micro Integrator solution to a Kubernetes cluster. One advantage of having an
immutable custom docker image is that you can easily implement a CI/CD pipeline where you can systematically test the
solution before deploying in the production environment.

After we have created the docker image with the artifacts we can use it to create pods and configure the k8s deployments
and services to match our requirements. A sample Kubernetes YAML configureation that you can use to deploy a micro
integrator custom image is mentioned below. Please not that the docker image `wso2-mi-hello-world` is the one we
created in [Micro Integrator with Docker](#micro-integrator-with-docker).

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: mi-helloworld-deployment
  labels:
    event: mi-helloworld
spec:
  strategy:
    type: Recreate
  replicas: 2
  selector:
    matchLabels:
      event: mi-helloworld
  template:
    metadata:
      labels:
        event: mi-helloworld
    spec:
      containers:
      -
        image: wso2-mi-hello-world
        name: helloworld
        imagePullPolicy: IfNotPresent
        ports:
        -
          name: web
          containerPort: 8290
        -
          name: web-secure
          containerPort: 8253
---
apiVersion: v1
kind: Service
metadata:
  name: mi-helloworld-service
  labels:
    event: mi-helloworld
spec:
  ports:
    -
      name: web
      port: 8290
      targetPort: 8290
    -
      name: web-secure
      port: 8253
      targetPort: 8253
  selector:
    event: mi-helloworld
```

### Trying out a sample scenario

For instructions on trying out a simple use case using the micro Integrator profile, see the section on
[Deploying to a Kubernetes Cluster](examples/hello-world#deploying-to-a-kubernetes-cluster-optional)
in the [Hello world sample](examples/hello-world).

## Configuring the file-based registry

The H2 database-based registry is not available in the Micro Integrator profile. Instead, it has a file system based
registry, which provides the same functionality. Thus, by default, the `<MI_HOME>/wso2/micro-integrator/registry`
directory will act as the registry to store registry artifacts etc. This main registry directory will consist of the
following sub-registry directories.

These sub-registry directories will be created automatically when you deploy registry artifacts. If you did not deploy
any registry artifacts, you can create them manually.

- Local: To store local artifacts of the product server that are not shared with the other products in the deployment.
- Config: To store all product-specific artifacts that are shared between similar product instances.
- Governance: To store all artifacts that are relevant to the governance of the product.

If you want to change the default locations of the registry directories, uncomment and change the following configuration
 in the `<MI_HOME>/wso2/microIntegrator/repository/deployment/server/synapse-config/default/directoryregistry.xml` file.

```xml
<registry xmlns="http://ws.apache.org/ns/synapse" provider="org.wso2.carbon.mediation.registry.MicroIntegratorRegistry">
    <parameter name="cachableDuration">15000</parameter>
    <!--
        Uncomment below parameters (ConfigRegRoot, GovRegRoot, LocalRegRoot) to configure registry root paths
        Default : <MI_HOME>/wso2/micro-integrator/registry/{governance | config | local}
    -->
    <!--
    <parameter name="ConfigRegRoot">{Root directory path for configuration Registry}</parameter>
    <parameter name="GovRegRoot">{Root directory path for governance Registry}</parameter>
    <parameter name="LocalRegRoot">{Root directory path for local Registry}</parameter>
    -->
</registry>
```