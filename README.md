# Micro Integrator

## Building the docker image

You can build the docker image for micro integrator by setting the system property `docker.skip` to false when running
maven build.

```bash
mvn clean install -Ddocker.skip=false
```
