# WSO2 Micro Integrator

[![Build Status](https://wso2.org/jenkins/buildStatus/icon?job=products/micro-integrator)](https://wso2.org/jenkins/job/products/job/micro-integrator/)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)

## Building from the source

Please follow the steps below to build WSO2 Micro Integrator from source code.

1. Clone or download the source code from this repository (https://github.com/wso2/micro-integrator).
2. Run the maven command `mvn clean install` from the root directory of the repository.
3. The generated Micro Integrator distribution can be found at `micro-integrator/distribution/target/wso2mi-<version>.zip`.

### Building the docker image

You can build the docker image for micro integrator by setting the system property `docker.skip` to false when running
maven build. This will build and push the micro-integrator docker image to the local docker registry.

```bash
mvn clean install -Ddocker.skip=false
```

## Licence

WSO2 Micro Integrator is licensed under the [Apache License](http://www.apache.org/licenses/LICENSE-2.0).

## Copyright

(c) 2018, [WSO2 Inc.](http://www.wso2.org) All Rights Reserved.

