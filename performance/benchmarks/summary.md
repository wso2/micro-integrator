# WSO2 Micro Integrator 1.2.0-alpha Performance Test Results

During each release, we execute various automated performance test scenarios and publish the results.

| Test Scenarios | Description |
| --- | --- |
| Direct Proxy | Passthrough proxy service |
| CBR Proxy | Routing the message based on the content of the message body |
| XSLT Proxy | Having XSLT transformations in request and response paths |
| CBR SOAP Header Proxy | Routing the message based on a SOAP header in the message payload |
| CBR Transport Header Proxy | Routing the message based on an HTTP header in the message |
| XSLT Enhanced Proxy | Having enhanced, Fast XSLT transformations in request and response paths |

Our test client is [Apache JMeter](https://jmeter.apache.org/index.html). We test each scenario for a fixed duration of
time. We split the test results into warmup and measurement parts and use the measurement part to compute the
performance metrics.

Test scenarios use a [Netty](https://netty.io/) based back-end service which echoes back any request
posted to it after a specified period of time.

We run the performance tests under different numbers of concurrent users, message sizes (payloads) and back-end service
delays.

The main performance metrics:

1. **Throughput**: The number of requests that the WSO2 Micro Integrator 1.2.0-alpha processes during a specific time interval (e.g. per second).
2. **Response Time**: The end-to-end latency for an operation of invoking a service in WSO2 Micro Integrator 1.2.0-alpha . The complete distribution of response times was recorded.

In addition to the above metrics, we measure the load average and several memory-related metrics.

The following are the test parameters.

| Test Parameter | Description | Values |
| --- | --- | --- |
| Scenario Name | The name of the test scenario. | Refer to the above table. |
| Heap Size | The amount of memory allocated to the application | 512M |
| Concurrent Users | The number of users accessing the application at the same time. | 500 |
| Message Size (Bytes) | The request payload size in Bytes. | 500, 1024, 10240, 102400 |
| Back-end Delay (ms) | The delay added by the Back-end service. | 0, 30, 100, 500, 1000 |

The duration of each test is **60 seconds**. The warm-up period is **30 seconds**.
The measurement results are collected after the warm-up period.

The performance tests were executed on 1 AWS CloudFormation stack.


System information for WSO2 Micro Integrator 1.2.0-alpha in 1st AWS CloudFormation stack.

| Class | Subclass | Description | Value |
| --- | --- | --- | --- |
| AWS | EC2 | AMI-ID | ami-05801d0a3c8e4c443 |
| AWS | EC2 | Instance Type | c5.large |
| System | Processor | CPU(s) | 2 |
| System | Processor | Thread(s) per core | 2 |
| System | Processor | Core(s) per socket | 1 |
| System | Processor | Socket(s) | 1 |
| System | Processor | Model name | Intel(R) Xeon(R) Platinum 8124M CPU @ 3.00GHz |
| System | Memory | BIOS | 64 KiB |
| System | Memory | System memory | 3785436 KiB |
| System | Storage | Block Device: nvme0n1 | 8G |
| Operating System | Distribution | Release | Ubuntu 18.04.4 LTS |
| Operating System | Distribution | Kernel | Linux ip-10-0-1-60 5.3.0-1017-aws #18~18.04.1-Ubuntu SMP Wed Apr 8 15:12:16 UTC 2020 x86_64 x86_64 x86_64 GNU/Linux |


The following are the measurements collected from each performance test conducted for a given combination of
test parameters.

| Measurement | Description |
| --- | --- |
| Error % | Percentage of requests with errors |
| Average Response Time (ms) | The average response time of a set of results |
| Standard Deviation of Response Time (ms) | The “Standard Deviation” of the response time. |
| 99th Percentile of Response Time (ms) | 99% of the requests took no more than this time. The remaining samples took at least as long as this |
| Throughput (Requests/sec) | The throughput measured in requests per second. |
| Average Memory Footprint After Full GC (M) | The average memory consumed by the application after a full garbage collection event. |

The following is the summary of performance test results collected for the measurement period.

|  Scenario Name | Heap Size | Concurrent Users | Message Size (Bytes) | Back-end Service Delay (ms) | Error % | Throughput (Requests/sec) | Average Response Time (ms) | Standard Deviation of Response Time (ms) | 99th Percentile of Response Time (ms) | WSO2 Micro Integrator 1.2.0-alpha GC Throughput (%) | Average WSO2 Micro Integrator 1.2.0-alpha Memory Footprint After Full GC (M) |
|---|---:|---:|---:|---:|---:|---:|---:|---:|---:|---:|---:|
|  CBR Proxy | 512M | 500 | 500 | 0 | 0 | 1988.33 | 188.47 | 256.16 | 1335 | N/A | N/A |
|  CBR Proxy | 512M | 500 | 500 | 30 | 0 | 1574.46 | 233.53 | 268.04 | 1511 | N/A | N/A |
|  CBR Proxy | 512M | 500 | 500 | 100 | 0 | 1855.85 | 201.67 | 170.29 | 1415 | N/A | N/A |
|  CBR Proxy | 512M | 500 | 500 | 500 | 0 | 724.27 | 513.7 | 41.62 | 735 | N/A | N/A |
|  CBR Proxy | 512M | 500 | 500 | 1000 | 0 | 365.4 | 1003.67 | 5.99 | 1039 | N/A | N/A |
|  CBR Proxy | 512M | 500 | 1024 | 0 | 0 | 1171.61 | 314.29 | 360.13 | 1591 | N/A | N/A |
|  CBR Proxy | 512M | 500 | 1024 | 30 | 0 | 1877.32 | 200.04 | 197.12 | 1167 | N/A | N/A |
|  CBR Proxy | 512M | 500 | 1024 | 100 | 0 | 1994.86 | 187.45 | 54.04 | 357 | N/A | N/A |
|  CBR Proxy | 512M | 500 | 1024 | 500 | 0 | 725.75 | 510.75 | 13.74 | 559 | N/A | N/A |
|  CBR Proxy | 512M | 500 | 1024 | 1000 | 0 | 366.66 | 1004.15 | 7.11 | 1039 | N/A | N/A |
|  CBR Proxy | 512M | 500 | 10240 | 0 | 0 | 412.54 | 906.18 | 421.85 | 1743 | N/A | N/A |
|  CBR Proxy | 512M | 500 | 10240 | 30 | 0 | 411.44 | 910.88 | 437.48 | 1919 | N/A | N/A |
|  CBR Proxy | 512M | 500 | 10240 | 100 | 0 | 416.93 | 899.13 | 456.96 | 2127 | N/A | N/A |
|  CBR Proxy | 512M | 500 | 10240 | 500 | 0 | 365.12 | 1012.31 | 353.87 | 1983 | N/A | N/A |
|  CBR Proxy | 512M | 500 | 10240 | 1000 | 0 | 280.31 | 1329.36 | 268.04 | 2191 | N/A | N/A |
|  CBR Proxy | 512M | 500 | 102400 | 0 | 100 | 1.67 | 122690.19 | 6936.14 | 136191 | N/A | N/A |
|  CBR Proxy | 512M | 500 | 102400 | 30 | 100 | 1.66 | 120064 | 0 | 120319 | N/A | N/A |
|  CBR Proxy | 512M | 500 | 102400 | 100 | 100 | 1.66 | 120064 | 0 | 120319 | N/A | N/A |
|  CBR Proxy | 512M | 500 | 102400 | 500 | 100 | 1.66 | 120064 | 0 | 120319 | N/A | N/A |
|  CBR Proxy | 512M | 500 | 102400 | 1000 | 100 | 1.66 | 120064 | 0 | 120319 | N/A | N/A |
|  CBR SOAP Header Proxy | 512M | 500 | 500 | 0 | 100 | 1.66 | 120064 | 0 | 120319 | N/A | N/A |
|  CBR SOAP Header Proxy | 512M | 500 | 500 | 30 | 100 | 1.66 | 120064 | 0 | 120319 | N/A | N/A |
|  CBR SOAP Header Proxy | 512M | 500 | 500 | 100 | 100 | 1.65 | 120824.8 | 447.48 | 121343 | N/A | N/A |
|  CBR SOAP Header Proxy | 512M | 500 | 500 | 500 | 100 | 1.65 | 120964.63 | 333.34 | 121343 | N/A | N/A |
|  CBR SOAP Header Proxy | 512M | 500 | 500 | 1000 | 100 | 1.65 | 120964.63 | 333.34 | 121343 | N/A | N/A |
|  CBR SOAP Header Proxy | 512M | 500 | 1024 | 0 | 100 | 1.65 | 120964.63 | 333.34 | 121343 | N/A | N/A |
|  CBR SOAP Header Proxy | 512M | 500 | 1024 | 30 | 100 | 1.65 | 120964.63 | 333.34 | 121343 | N/A | N/A |
|  CBR SOAP Header Proxy | 512M | 500 | 1024 | 100 | 100 | 1.65 | 120964.63 | 333.34 | 121343 | N/A | N/A |
|  CBR SOAP Header Proxy | 512M | 500 | 1024 | 500 | 100 | 1.65 | 120960.51 | 338.07 | 121343 | N/A | N/A |
|  CBR SOAP Header Proxy | 512M | 500 | 1024 | 1000 | 100 | 1.65 | 120964.63 | 333.34 | 121343 | N/A | N/A |
|  CBR SOAP Header Proxy | 512M | 500 | 10240 | 0 | 100 | 1.65 | 120960.51 | 338.07 | 121343 | N/A | N/A |
|  CBR SOAP Header Proxy | 512M | 500 | 10240 | 30 | 100 | 1.65 | 120964.63 | 333.34 | 121343 | N/A | N/A |
|  CBR SOAP Header Proxy | 512M | 500 | 10240 | 100 | 100 | 1.65 | 120964.63 | 333.34 | 121343 | N/A | N/A |
|  CBR SOAP Header Proxy | 512M | 500 | 10240 | 500 | 100 | 1.65 | 120964.63 | 333.34 | 121343 | N/A | N/A |
|  CBR SOAP Header Proxy | 512M | 500 | 10240 | 1000 | 100 | 1.65 | 120956.4 | 342.69 | 121343 | N/A | N/A |
|  CBR SOAP Header Proxy | 512M | 500 | 102400 | 0 | 100 | 1.65 | 120960.51 | 338.07 | 121343 | N/A | N/A |
|  CBR SOAP Header Proxy | 512M | 500 | 102400 | 30 | 100 | 1.65 | 120964.63 | 333.34 | 121343 | N/A | N/A |
|  CBR SOAP Header Proxy | 512M | 500 | 102400 | 100 | 100 | 1.65 | 120964.63 | 333.34 | 121343 | N/A | N/A |
|  CBR SOAP Header Proxy | 512M | 500 | 102400 | 500 | 100 | 1.65 | 120964.63 | 333.34 | 121343 | N/A | N/A |
|  CBR SOAP Header Proxy | 512M | 500 | 102400 | 1000 | 100 | 1.65 | 120960.51 | 338.07 | 121343 | N/A | N/A |
|  CBR Transport Header Proxy | 512M | 500 | 500 | 0 | 100 | 1.65 | 120964.63 | 333.34 | 121343 | N/A | N/A |
|  CBR Transport Header Proxy | 512M | 500 | 500 | 30 | 100 | 1.65 | 120964.63 | 333.34 | 121343 | N/A | N/A |
|  CBR Transport Header Proxy | 512M | 500 | 500 | 100 | 100 | 1.65 | 120964.63 | 333.34 | 121343 | N/A | N/A |
|  CBR Transport Header Proxy | 512M | 500 | 500 | 500 | 100 | 1.65 | 120964.63 | 333.34 | 121343 | N/A | N/A |
|  CBR Transport Header Proxy | 512M | 500 | 500 | 1000 | 100 | 1.65 | 120964.63 | 333.34 | 121343 | N/A | N/A |
|  CBR Transport Header Proxy | 512M | 500 | 1024 | 0 | 100 | 1.65 | 120964.63 | 333.34 | 121343 | N/A | N/A |
|  CBR Transport Header Proxy | 512M | 500 | 1024 | 30 | 100 | 1.65 | 120960.51 | 338.07 | 121343 | N/A | N/A |
|  CBR Transport Header Proxy | 512M | 500 | 1024 | 100 | 100 | 1.65 | 120964.63 | 333.34 | 121343 | N/A | N/A |
|  CBR Transport Header Proxy | 512M | 500 | 1024 | 500 | 100 | 1.65 | 120964.63 | 333.34 | 121343 | N/A | N/A |
|  CBR Transport Header Proxy | 512M | 500 | 1024 | 1000 | 100 | 1.65 | 121088 | 0 | 121343 | N/A | N/A |
|  CBR Transport Header Proxy | 512M | 500 | 10240 | 0 | 100 | 1.65 | 121088 | 0 | 121343 | N/A | N/A |
|  CBR Transport Header Proxy | 512M | 500 | 10240 | 30 | 100 | 1.65 | 121088 | 0 | 121343 | N/A | N/A |
|  CBR Transport Header Proxy | 512M | 500 | 10240 | 100 | 100 | 1.65 | 121088 | 0 | 121343 | N/A | N/A |
|  CBR Transport Header Proxy | 512M | 500 | 10240 | 500 | 100 | 1.65 | 121088 | 0 | 121343 | N/A | N/A |
|  CBR Transport Header Proxy | 512M | 500 | 10240 | 1000 | 100 | 1.65 | 121088 | 0 | 121343 | N/A | N/A |
|  CBR Transport Header Proxy | 512M | 500 | 102400 | 0 | 100 | 1.65 | 121088 | 0 | 121343 | N/A | N/A |
|  CBR Transport Header Proxy | 512M | 500 | 102400 | 30 | 100 | 1.65 | 121088 | 0 | 121343 | N/A | N/A |
|  CBR Transport Header Proxy | 512M | 500 | 102400 | 100 | 100 | 1.65 | 121088 | 0 | 121343 | N/A | N/A |
|  CBR Transport Header Proxy | 512M | 500 | 102400 | 500 | 100 | 1.65 | 121088 | 0 | 121343 | N/A | N/A |
|  CBR Transport Header Proxy | 512M | 500 | 102400 | 1000 | 100 | 1.65 | 121088 | 0 | 121343 | N/A | N/A |
|  Direct Proxy | 512M | 500 | 500 | 0 | 0 | 991.74 | 375.4 | 380.1 | 1019 | 59.97 | 321.165 |
|  Direct Proxy | 512M | 500 | 500 | 30 | 0 | 771.5 | 481.77 | 761.92 | 3391 | N/A | N/A |
|  Direct Proxy | 512M | 500 | 500 | 100 | 0 | 1660.49 | 222 | 344.37 | 2143 | N/A | N/A |
|  Direct Proxy | 512M | 500 | 500 | 500 | 0 | 731.99 | 506.26 | 13.24 | 559 | N/A | N/A |
|  Direct Proxy | 512M | 500 | 500 | 1000 | 0 | 367.09 | 1002.98 | 4.98 | 1031 | N/A | N/A |
|  Direct Proxy | 512M | 500 | 1024 | 0 | 0 | 2498.85 | 145.79 | 181.14 | 859 | N/A | N/A |
|  Direct Proxy | 512M | 500 | 1024 | 30 | 0 | 2010.59 | 184.44 | 279.29 | 1487 | N/A | N/A |
|  Direct Proxy | 512M | 500 | 1024 | 100 | 0 | 2001.59 | 187.5 | 180.23 | 835 | N/A | N/A |
|  Direct Proxy | 512M | 500 | 1024 | 500 | 0 | 733.91 | 505.54 | 8.94 | 547 | N/A | N/A |
|  Direct Proxy | 512M | 500 | 1024 | 1000 | 0 | 366.67 | 1003.15 | 6.02 | 1047 | N/A | N/A |
|  Direct Proxy | 512M | 500 | 10240 | 0 | 0 | 1336.94 | 280.61 | 522.1 | 3023 | N/A | N/A |
|  Direct Proxy | 512M | 500 | 10240 | 30 | 0 | 1672.58 | 223.67 | 509.49 | 3023 | N/A | N/A |
|  Direct Proxy | 512M | 500 | 10240 | 100 | 0 | 2051.51 | 182.22 | 66.75 | 309 | N/A | N/A |
|  Direct Proxy | 512M | 500 | 10240 | 500 | 0 | 720.27 | 518.75 | 29.04 | 627 | N/A | N/A |
|  Direct Proxy | 512M | 500 | 10240 | 1000 | 0 | 365.81 | 1003.48 | 5.76 | 1039 | N/A | N/A |
|  Direct Proxy | 512M | 500 | 102400 | 0 | 0 | 920.76 | 404.2 | 189.58 | 895 | N/A | N/A |
|  Direct Proxy | 512M | 500 | 102400 | 30 | 0 | 923.11 | 405.89 | 181.75 | 903 | N/A | N/A |
|  Direct Proxy | 512M | 500 | 102400 | 100 | 0 | 926.1 | 402.48 | 152.37 | 855 | N/A | N/A |
|  Direct Proxy | 512M | 500 | 102400 | 500 | 0 | 636.79 | 581.74 | 75.83 | 803 | N/A | N/A |
|  Direct Proxy | 512M | 500 | 102400 | 1000 | 0 | 363.21 | 1010.04 | 10.65 | 1055 | N/A | N/A |
|  XSLT Enhanced Proxy | 512M | 500 | 500 | 0 | 100 | 1.65 | 121088 | 0 | 121343 | N/A | N/A |
|  XSLT Enhanced Proxy | 512M | 500 | 500 | 30 | 100 | 1.65 | 121088 | 0 | 121343 | N/A | N/A |
|  XSLT Enhanced Proxy | 512M | 500 | 500 | 100 | 100 | 1.65 | 121088 | 0 | 121343 | N/A | N/A |
|  XSLT Enhanced Proxy | 512M | 500 | 500 | 500 | 100 | 1.65 | 121088 | 0 | 121343 | N/A | N/A |
|  XSLT Enhanced Proxy | 512M | 500 | 500 | 1000 | 100 | 1.65 | 121088 | 0 | 121343 | N/A | N/A |
|  XSLT Enhanced Proxy | 512M | 500 | 1024 | 0 | 100 | 1.65 | 121088 | 0 | 121343 | N/A | N/A |
|  XSLT Enhanced Proxy | 512M | 500 | 1024 | 30 | 100 | 1.65 | 121088 | 0 | 121343 | N/A | N/A |
|  XSLT Enhanced Proxy | 512M | 500 | 1024 | 100 | 100 | 1.65 | 121088 | 0 | 121343 | N/A | N/A |
|  XSLT Enhanced Proxy | 512M | 500 | 1024 | 500 | 100 | 1.65 | 121088 | 0 | 121343 | N/A | N/A |
|  XSLT Enhanced Proxy | 512M | 500 | 1024 | 1000 | 100 | 1.65 | 121088 | 0 | 121343 | N/A | N/A |
|  XSLT Enhanced Proxy | 512M | 500 | 10240 | 0 | 100 | 1.65 | 121088 | 0 | 121343 | N/A | N/A |
|  XSLT Enhanced Proxy | 512M | 500 | 10240 | 30 | 100 | 1.65 | 121088 | 0 | 121343 | N/A | N/A |
|  XSLT Enhanced Proxy | 512M | 500 | 10240 | 100 | 100 | 1.65 | 121088 | 0 | 121343 | N/A | N/A |
|  XSLT Enhanced Proxy | 512M | 500 | 10240 | 500 | 100 | 1.65 | 121088 | 0 | 121343 | N/A | N/A |
|  XSLT Enhanced Proxy | 512M | 500 | 10240 | 1000 | 100 | 1.65 | 121088 | 0 | 121343 | N/A | N/A |
|  XSLT Enhanced Proxy | 512M | 500 | 102400 | 0 | 100 | 1.65 | 121088 | 0 | 121343 | N/A | N/A |
|  XSLT Enhanced Proxy | 512M | 500 | 102400 | 30 | 100 | 1.65 | 121088 | 0 | 121343 | N/A | N/A |
|  XSLT Enhanced Proxy | 512M | 500 | 102400 | 100 | 100 | 1.65 | 121088 | 0 | 121343 | N/A | N/A |
|  XSLT Enhanced Proxy | 512M | 500 | 102400 | 500 | 100 | 1.65 | 121088 | 0 | 121343 | N/A | N/A |
|  XSLT Enhanced Proxy | 512M | 500 | 102400 | 1000 | 100 | 1.65 | 121088 | 0 | 121343 | N/A | N/A |
|  XSLT Proxy | 512M | 500 | 500 | 0 | 100 | 1.66 | 120064 | 0 | 120319 | N/A | N/A |
|  XSLT Proxy | 512M | 500 | 500 | 30 | 100 | 1.66 | 120064 | 0 | 120319 | N/A | N/A |
|  XSLT Proxy | 512M | 500 | 500 | 100 | 100 | 1.66 | 120064 | 0 | 120319 | N/A | N/A |
|  XSLT Proxy | 512M | 500 | 500 | 500 | 100 | 1.66 | 120064 | 0 | 120319 | N/A | N/A |
|  XSLT Proxy | 512M | 500 | 500 | 1000 | 100 | 1.66 | 120064 | 0 | 120319 | N/A | N/A |
|  XSLT Proxy | 512M | 500 | 1024 | 0 | 100 | 1.66 | 120064 | 0 | 120319 | N/A | N/A |
|  XSLT Proxy | 512M | 500 | 1024 | 30 | 100 | 1.66 | 120064 | 0 | 120319 | N/A | N/A |
|  XSLT Proxy | 512M | 500 | 1024 | 100 | 100 | 1.66 | 120064 | 0 | 120319 | N/A | N/A |
|  XSLT Proxy | 512M | 500 | 1024 | 500 | 100 | 1.66 | 120064 | 0 | 120319 | N/A | N/A |
|  XSLT Proxy | 512M | 500 | 1024 | 1000 | 100 | 1.66 | 120064 | 0 | 120319 | N/A | N/A |
|  XSLT Proxy | 512M | 500 | 10240 | 0 | 100 | 1.66 | 120064 | 0 | 120319 | N/A | N/A |
|  XSLT Proxy | 512M | 500 | 10240 | 30 | 100 | 1.66 | 120064 | 0 | 120319 | N/A | N/A |
|  XSLT Proxy | 512M | 500 | 10240 | 100 | 100 | 1.66 | 120064 | 0 | 120319 | N/A | N/A |
|  XSLT Proxy | 512M | 500 | 10240 | 500 | 100 | 1.66 | 120064 | 0 | 120319 | N/A | N/A |
|  XSLT Proxy | 512M | 500 | 10240 | 1000 | 100 | 1.66 | 120064 | 0 | 120319 | N/A | N/A |
|  XSLT Proxy | 512M | 500 | 102400 | 0 | 100 | 1.66 | 120064 | 0 | 120319 | N/A | N/A |
|  XSLT Proxy | 512M | 500 | 102400 | 30 | 100 | 1.66 | 120064 | 0 | 120319 | N/A | N/A |
|  XSLT Proxy | 512M | 500 | 102400 | 100 | 100 | 1.66 | 120064 | 0 | 120319 | N/A | N/A |
|  XSLT Proxy | 512M | 500 | 102400 | 500 | 100 | 1.66 | 120064 | 0 | 120319 | N/A | N/A |
|  XSLT Proxy | 512M | 500 | 102400 | 1000 | 100 | 1.66 | 120064 | 0 | 120319 | N/A | N/A |
