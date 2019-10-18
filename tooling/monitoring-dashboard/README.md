# Monitoring Dashboard for WSO2 Micro Integrator

## Building from the source

### Setting up the development environment
1. Install Node.js [8.X.X](https://nodejs.org/en/download/releases/).
2. Fork the [Micro Integrator repository](https://github.com/wso2/micro-integrator).
3. Clone your fork into any directory.
4. Access into cloned directory and then navigate into `micro-integrator/tooling/monitoring-dashboard`. This 
   will be the <DASHBOARD_REPO> for future reference.
5. Run the script available by doing the following Apache Maven command.
```mvn clean install```
6. wso2mi-monitoring-dashboard-version.zip can be found in
 `<DASHBOARD_REPO>/distribution/target`
 
### Running
- Extract the compressed archive generated to a desired location.
    ```
    cd to the <DASHBOARD_HOME>/bin
    
    Execute `dashboard.sh or dashboard.bat as appropriate.
    ```

- Load the login page with the dashboard context. i.e: https://localhost:9743/dashboard
- Please note that in order to connect the monitoring dashboard to an instance of WSO2 Micro integrator, the
  said instance should have the Management Api enabled. 
  
### How to Enable the Management API
   Please note that Management Api is disabled by default. To use the Management Api you must use the 
   system property `-DenableManagementApi` when starting the micro integrator. This can be done by
   navigating to <DASHBOARD_HOME>/bin and executing the script as follows.
   1. `sh micro-integrator.sh -DenableManagementApi` - Linux and Unix 
   2. `docker run -p 8290:8290 -p 9164:9164 -e JAVA_OPTS="-DenableManagementApi=true" <Docker_Image_Name>` - Docker
    
### Management API Address and Port
   The Management API address and Port is required when logging into the dashboard.
   NOTE: The default hostname is localhost and the port is 9164.
