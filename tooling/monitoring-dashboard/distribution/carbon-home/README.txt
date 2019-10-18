Monitoring Dashboard for WSO2 Micro Integrator
======================================================================

Welcome to the Monitoring Dashboard for WSO2 Micro Integrator @product.version@
This is a lightweight UI server that hosts the React application used to implement the monitoring dashboard.


Running the Monitoring Dashboard
======================================================================
1. Go to the DASHBOARD_HOME/bin directory and run the dashboard.sh file for Linux and Unix or the dashboard.bat file for Windows.
2. Access the dashboard login page found at https://localhost:9743/dashboad
3. To connect the monitoring dashboard to a WSO2 Micro Integrator instance, the instance should have the management API enabled.
   This can be done by starting the WSO2 Micro Integrator with the -DenableManagementApi property as follows.
   sh micro-integrator.sh -DenableManagementApi - Linux and Unix.

More information regarding the dashboard configurations can be found at
(https://ei.docs.wso2.com/en/latest/micro-integrator/administer-and-observe/working-with-monitoring-dashboard/)


Known issues of WSO2 MI @product.version@
======================================================================

     - https://github.com/wso2/micro-integrator/issues

Support
======================================================================

WSO2 Inc. offers a variety of development and production support
programs, ranging from Web-based support up through normal business
hours, to premium 24x7 phone support.

For additional support information please refer to http://wso2.com/support/

For more information on WSO2 Micro Integrator, visit the GitHub page (https://github.com/wso2/micro-integrator)

--------------------------------------------------------------------------------
(c) Copyright 2019 WSO2 Inc.