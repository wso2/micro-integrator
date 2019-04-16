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

### Deploying to a Kubernetes Cluster (Optional)

We are using [Minikube](https://github.com/kubernetes/minikube) to test deploying to a Kubernetes cluster in this
example. We assume that you already have a working minikube setup locally. If not please follow the
[installation guide](https://kubernetes.io/docs/tasks/tools/install-minikube/).

**Note**: You have to build the docker image while the docker CLI is using the Minikube’s built-in Docker daemon.
Otherwise the docker image will not be available in the Minikube environment. You can execute `eval $(minikube
docker-env)` to use the Minikube’s built-in Docker daemon. Please refer
[Use local images by re-using the Docker daemon](https://kubernetes.io/docs/setup/minikube/#use-local-images-by-re-using-the-docker-daemon)
for more details.

1. The [k8s-deployment.yaml](k8s-deployment.yaml) file is the Kubernetes artifact descriptor used to deploy the micro
integrator service in a Kubernetes cluster. You can use `kubectl create` to deploy.

    ```
    $ kubectl create -f k8s-deployment.yaml
    ```

2. Check whether all the Kubernetes artifacts are deployed successfully by executing the following command.
    ```
    $ kubectl get all

    NAME                                            READY   STATUS    RESTARTS   AGE
    pod/mi-helloworld-deployment-56f58c9676-5srtb   1/1     Running   0          3h35m
    pod/mi-helloworld-deployment-56f58c9676-lv2s5   1/1     Running   0          3h35m

    NAME                            TYPE        CLUSTER-IP       EXTERNAL-IP   PORT(S)          AGE
    service/kubernetes              ClusterIP   10.96.0.1        <none>        443/TCP          78d
    service/mi-helloworld-service   NodePort    10.108.208.112   <none>        8290:32100/TCP   3h35m

    NAME                                       READY   UP-TO-DATE   AVAILABLE   AGE
    deployment.apps/mi-helloworld-deployment   2/2     2            2           3h35m

    NAME                                                  DESIRED   CURRENT   READY   AGE
    replicaset.apps/mi-helloworld-deployment-56f58c9676   2         2         2       3h35m

    ```

3. Invoke the service using an HTTP client like cURL.
   ```
   curl http://MINIKUBE_IP:32100/services/HelloWorld
   ```