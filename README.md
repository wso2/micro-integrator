# WSO2 Micro Integrator

[![Build Status](https://wso2.org/jenkins/buildStatus/icon?job=products/micro-integrator)](https://wso2.org/jenkins/job/products/job/micro-integrator/)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)

[WSO2 Enterprise Integration (EI)](https://wso2.com/integration/) is an open source integration platform and the Micro Integrator is a cloud native variant of WSO2 EI. The Micro Integrator is a lightweight integration framework with an optimized runtime that is ideal for microservices and for hybrid integration requirements. It integrates natively with cloud native ecosystem projects.

The Micro Integrator is built for developers who would like to integrate microservices and cloud native architectures using a configuration-driven approach. The Micro Integrator gives developers the best possible experience for developing, testing, and deploying integrations and tooling. It is also useful for those trying to integrate their legacy systems with cloud native or microservice architectures.

The Micro Integrator also provides an enhanced experience for those using a container-based architecture. The Micro Integrator works natively on the Kubernetes ecosystem and it makes deployment with Docker a simple process. 

## Building from the source

Please follow the steps below to build WSO2 Micro Integrator from source code.

1. Clone or download the source code from this repository (https://github.com/wso2/micro-integrator).
2. Run the maven command `mvn clean install` from the root directory of the repository.
3. The generated Micro Integrator distribution can be found at `micro-integrator/distribution/target/wso2mi-<version>.zip`.

### Building the Docker image

You can build the Docker image for Micro Integrator by setting the system property `docker.skip` to `false` when running
maven build. This builds and pushes the micro-integrator Docker image to the local Docker registry.

```bash
mvn clean install -Ddocker.skip=false
```

## Licence

WSO2 Micro Integrator is licensed under the [Apache License](http://www.apache.org/licenses/LICENSE-2.0).

## Copyright

(c) 2018, [WSO2 Inc.](http://www.wso2.org) All Rights Reserved.

