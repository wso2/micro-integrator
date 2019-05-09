## Configuring the Port Offset

The portOffset feature in WSO2 Micro Integrator allows you to run multiple WSO2 products, multiple instances of Micro Integrator, or WSO2 product clusters on the same machine or VM.

 The port offset defines the number by which all ports defined in the runtime such as the HTTP/S ports will be offset. e.g. if the HTTP port is defined as 9763 & portOffset is 1, the effective HTTP port will be 9764. 

 The portOffset can be specified as a System property or it can be defined in the <MI_HOME>/conf/carbon.xml file

 PortOffset can be passed in during server startup as follows:

 ```./micro-integrator.sh -DportOffset=3```

 PortOffset can be set in the carbon.xml as follows, under the Ports section:

 ```<Offset>3</Offset>```
 
 Please note that a port offset value of 10 is added in the `carbon.xml` by default.