# WSO2 Micro Integrator

[![Build Status](https://wso2.org/jenkins/buildStatus/icon?job=products/micro-integrator)](https://wso2.org/jenkins/job/products/job/micro-integrator/)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)

# WSO2 Micro Integrator

## Introduction

In recent times we have witnessed the growth of approaches like Continuous Integration and Continuous Deployment (CI/CD), which has subsequently led to the rise of microservices. Microservices Architecture (MSA) became more popular due to the rise of cloud native technologies and the assistance it provides for agile development. Microservices are designed to start up very quickly, perform a specific task, and then shut down.

WSO2 Enterprise Integration (EI) is an open source integration platform and the Micro Integrator is a cloud native variant of WSO2 EI.

The Micro Integrator is a lightweight integration framework built for developers who would like to create and run their integrations and integrate microservices. Micro Integrator provides developers the freedom to quickly develop and run their integrations in a Microservices Architecture (MSA) environment and to deploy them using CICD. So with the Micro Integrator, developers are able to develop and manage composite microservices. 

Furthermore, since the startup time is a critical aspect when working with microservices, the Micro Integrator profile is designed with an improved startup time than the conventional WSO2 ESB or the ESB profile of WSO2 EI. Thus, with this improved startup time, the Micro Integrator profile becomes container-friendly for you to work with your microservices in a container-based architecture. The Micro Integrator works natively on the Kubernetes ecosystem and it makes deployment with Docker a simple process.

## Why Micro Integrator?

The Micro Integrator offers configuration-based mediation and an orchestration runtime developed to handle microservices integration scenarios. 

In the modern world, you will not be able to satisfy most of the real-world business use cases with atomic microservices. The Micro Integrator brings all the required functionalities for you to reuse and orchestrate atomic microservices to create composite microservices. 

The startup speed makes Micro Integrator ideal for the microservices world. While developed specifically for container-based deployments and MSA, Micro Integrator encompasses the following key attributes that are essential for a microservice ready integration solution. 

- Faster startup time (<5s)
- Low memory footprint
- Stateless services
- Immutable services

Furthermore, almost all the important features available in WSO2 EI is available in the Micro Integrator.

## Building from the source

Please follow the steps below to build WSO2 Micro Integrator from source code.

1. Clone or download the source code from this repository (https://github.com/wso2/micro-integrator).
2. Run the maven command `mvn clean install` from the root directory of the repository.
3. The generated Micro Integrator distribution can be found at `micro-integrator/distribution/target/wso2mi-<version>.zip`.

### Building the Docker image

You can build the Docker image for micro integrator by setting the system property `docker.skip` to false when running
maven build. This will build and push the micro-integrator Docker image to the local Docker registry.

```bash
mvn clean install -Ddocker.skip=false
```

## Licence

WSO2 Micro Integrator is licensed under the [Apache License](http://www.apache.org/licenses/LICENSE-2.0).

## Copyright

(c) 2018, [WSO2 Inc.](http://www.wso2.org) All Rights Reserved.

