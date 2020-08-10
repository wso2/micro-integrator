# WSO2 Micro Integrator 1.1.0 Performance Test Results

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

1. **Throughput**: The number of requests that the WSO2 Micro Integrator 1.1.0 processes during a specific time interval (e.g. per second).
2. **Response Time**: The end-to-end latency for an operation of invoking a service in WSO2 Micro Integrator 1.1.0 . The complete distribution of response times was recorded.

In addition to the above metrics, we measure the load average and several memory-related metrics.

The following are the test parameters.

| Test Parameter | Description | Values |
| --- | --- | --- |
| Scenario Name | The name of the test scenario. | Refer to the above table. |
| Heap Size | The amount of memory allocated to the application | 1G |
| Concurrent Users | The number of users accessing the application at the same time. | 100, 200, 500, 1000 |
| Message Size (Bytes) | The request payload size in Bytes. | 500, 1024, 10240, 102400 |
| Back-end Delay (ms) | The delay added by the Back-end service. | 0 |

The duration of each test is **60 seconds**. The warm-up period is **30 seconds**.
The measurement results are collected after the warm-up period.

The performance tests were executed on 2 AWS CloudFormation stacks.


System information for WSO2 Micro Integrator 1.1.0 in 1st AWS CloudFormation stack.

| Class | Subclass | Description | Value |
| --- | --- | --- | --- |
| AWS | EC2 | AMI-ID | ami-07df16d0682f1fa59 |
| AWS | EC2 | Instance Type | c5.large |
| System | Processor | CPU(s) | 2 |
| System | Processor | Thread(s) per core | 2 |
| System | Processor | Core(s) per socket | 1 |
| System | Processor | Socket(s) | 1 |
| System | Processor | Model name | Intel(R) Xeon(R) Platinum 8124M CPU @ 3.00GHz |
| System | Memory | BIOS | 64 KiB |
| System | Memory | System memory | 3785216 KiB |
| System | Storage | Block Device: nvme0n1 | 8G |
| Operating System | Distribution | Release | Ubuntu 18.04.4 LTS |
| Operating System | Distribution | Kernel | Linux ip-10-0-1-143 5.3.0-1032-aws #34~18.04.2-Ubuntu SMP Fri Jul 24 10:06:28 UTC 2020 x86_64 x86_64 x86_64 GNU/Linux |
System information for WSO2 Micro Integrator 1.1.0 in 2nd AWS CloudFormation stack.

| Class | Subclass | Description | Value |
| --- | --- | --- | --- |
| AWS | EC2 | AMI-ID | ami-07df16d0682f1fa59 |
| AWS | EC2 | Instance Type | c5.large |
| System | Processor | CPU(s) | 2 |
| System | Processor | Thread(s) per core | 2 |
| System | Processor | Core(s) per socket | 1 |
| System | Processor | Socket(s) | 1 |
| System | Processor | Model name | Intel(R) Xeon(R) Platinum 8124M CPU @ 3.00GHz |
| System | Memory | BIOS | 64 KiB |
| System | Memory | System memory | 3785428 KiB |
| System | Storage | Block Device: nvme0n1 | 8G |
| Operating System | Distribution | Release | Ubuntu 18.04.4 LTS |
| Operating System | Distribution | Kernel | Linux ip-10-0-1-75 5.3.0-1032-aws #34~18.04.2-Ubuntu SMP Fri Jul 24 10:06:28 UTC 2020 x86_64 x86_64 x86_64 GNU/Linux |


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

|  Scenario Name | Heap Size | Concurrent Users | Message Size (Bytes) | Back-end Service Delay (ms) | Error % | Throughput (Requests/sec) | Average Response Time (ms) | Standard Deviation of Response Time (ms) | 99th Percentile of Response Time (ms) | WSO2 Micro Integrator 1.1.0 GC Throughput (%) | Average WSO2 Micro Integrator 1.1.0 Memory Footprint After Full GC (M) |
|---|---:|---:|---:|---:|---:|---:|---:|---:|---:|---:|---:|
|  CBR Proxy | 1G | 100 | 500 | 0 | 0 | 3543.49 | 21.26 | 23.28 | 77 | N/A | N/A |
|  CBR Proxy | 1G | 100 | 1024 | 0 | 0 | 2767.34 | 27.47 | 64.53 | 90 | N/A | N/A |
|  CBR Proxy | 1G | 100 | 10240 | 0 | 0 | 774.99 | 94.85 | 46.67 | 234 | N/A | N/A |
|  CBR Proxy | 1G | 100 | 102400 | 0 | 0 | 59.21 | 1273.07 | 490.19 | 2559 | N/A | N/A |
|  CBR Proxy | 1G | 200 | 500 | 0 | 0 | 3172.88 | 47.36 | 71.74 | 130 | N/A | N/A |
|  CBR Proxy | 1G | 200 | 1024 | 0 | 0 | 2908.05 | 51.62 | 68.06 | 160 | N/A | N/A |
|  CBR Proxy | 1G | 200 | 10240 | 0 | 0 | 759.09 | 197.95 | 99.43 | 523 | N/A | N/A |
|  CBR Proxy | 1G | 200 | 102400 | 0 | 0 | 46.28 | 3233.32 | 1373.93 | 7007 | N/A | N/A |
|  CBR Proxy | 1G | 500 | 500 | 0 | 0 | 3270.29 | 114.72 | 106.91 | 331 | N/A | N/A |
|  CBR Proxy | 1G | 500 | 1024 | 0 | 0 | 2904.64 | 129.27 | 73.03 | 341 | N/A | N/A |
|  CBR Proxy | 1G | 500 | 10240 | 0 | 0 | 646.84 | 580.47 | 288.72 | 1551 | N/A | N/A |
|  CBR Proxy | 1G | 500 | 102400 | 0 | 0 | 9.19 | 42193.05 | 14349.81 | 59391 | N/A | N/A |
|  CBR Proxy | 1G | 1000 | 500 | 0 | 0 | 2468.62 | 302.98 | 154.28 | 839 | N/A | N/A |
|  CBR Proxy | 1G | 1000 | 1024 | 0 | 0 | 2180.75 | 343.75 | 162.82 | 1119 | N/A | N/A |
|  CBR Proxy | 1G | 1000 | 10240 | 0 | 0 | 476.21 | 1552.34 | 689.07 | 3295 | N/A | N/A |
|  CBR Proxy | 1G | 1000 | 102400 | 0 | 100 | 3.69 | 120064 | 0 | 120319 | N/A | N/A |
|  CBR SOAP Header Proxy | 1G | 100 | 500 | 0 | 0 | 3010.99 | 25.01 | 55.15 | 106 | N/A | N/A |
|  CBR SOAP Header Proxy | 1G | 100 | 1024 | 0 | 0 | 3104.3 | 24.24 | 29.3 | 98 | N/A | N/A |
|  CBR SOAP Header Proxy | 1G | 100 | 10240 | 0 | 0 | 1070.74 | 70.42 | 37.46 | 180 | N/A | N/A |
|  CBR SOAP Header Proxy | 1G | 100 | 102400 | 0 | 0 | 121.23 | 625.11 | 295.86 | 1599 | N/A | N/A |
|  CBR SOAP Header Proxy | 1G | 200 | 500 | 0 | 0 | 3401.94 | 43.83 | 71 | 135 | N/A | N/A |
|  CBR SOAP Header Proxy | 1G | 200 | 1024 | 0 | 0 | 3252.93 | 46.13 | 81.01 | 142 | N/A | N/A |
|  CBR SOAP Header Proxy | 1G | 200 | 10240 | 0 | 0 | 1149.63 | 130.06 | 57.61 | 297 | N/A | N/A |
|  CBR SOAP Header Proxy | 1G | 200 | 102400 | 0 | 0 | 81.26 | 1843.08 | 864.14 | 4415 | N/A | N/A |
|  CBR SOAP Header Proxy | 1G | 500 | 500 | 0 | 100 | 22606.94 | 14.23 | 38.22 | 141 | N/A | N/A |
|  CBR SOAP Header Proxy | 1G | 500 | 1024 | 0 | 100 | 22011.83 | 14.58 | 21.99 | 117 | N/A | N/A |
|  CBR SOAP Header Proxy | 1G | 500 | 10240 | 0 | 100 | 23814.87 | 13.43 | 24.53 | 129 | N/A | N/A |
|  CBR SOAP Header Proxy | 1G | 500 | 102400 | 0 | 100 | 23366.22 | 13.72 | 23.04 | 122 | N/A | N/A |
|  CBR SOAP Header Proxy | 1G | 1000 | 500 | 0 | 100 | 20183.65 | 29.61 | 54.09 | 299 | N/A | N/A |
|  CBR SOAP Header Proxy | 1G | 1000 | 1024 | 0 | 100 | 20908.3 | 28.75 | 51.33 | 269 | N/A | N/A |
|  CBR SOAP Header Proxy | 1G | 1000 | 10240 | 0 | 100 | 19881.17 | 29.94 | 56.09 | 309 | N/A | N/A |
|  CBR SOAP Header Proxy | 1G | 1000 | 102400 | 0 | 100 | 20133.35 | 30.4 | 56.99 | 305 | N/A | N/A |
|  CBR Transport Header Proxy | 1G | 100 | 500 | 0 | 0 | 3700.35 | 20.32 | 32.14 | 109 | N/A | N/A |
|  CBR Transport Header Proxy | 1G | 100 | 1024 | 0 | 0 | 3443.51 | 21.81 | 73.19 | 113 | N/A | N/A |
|  CBR Transport Header Proxy | 1G | 100 | 10240 | 0 | 0 | 2846.63 | 26.44 | 48.09 | 112 | N/A | N/A |
|  CBR Transport Header Proxy | 1G | 100 | 102400 | 0 | 0 | 1021.05 | 73.67 | 51.06 | 199 | N/A | N/A |
|  CBR Transport Header Proxy | 1G | 200 | 500 | 0 | 0 | 3695.17 | 40.59 | 115.54 | 139 | N/A | N/A |
|  CBR Transport Header Proxy | 1G | 200 | 1024 | 0 | 0 | 4054.8 | 37.01 | 64.52 | 126 | N/A | N/A |
|  CBR Transport Header Proxy | 1G | 200 | 10240 | 0 | 0 | 3004.36 | 49.98 | 64.67 | 138 | N/A | N/A |
|  CBR Transport Header Proxy | 1G | 200 | 102400 | 0 | 0 | 984.09 | 152.49 | 77.94 | 365 | N/A | N/A |
|  CBR Transport Header Proxy | 1G | 500 | 500 | 0 | 100 | 23478.38 | 13.66 | 22.55 | 120 | N/A | N/A |
|  CBR Transport Header Proxy | 1G | 500 | 1024 | 0 | 100 | 23250.27 | 13.73 | 21.9 | 118 | N/A | N/A |
|  CBR Transport Header Proxy | 1G | 500 | 10240 | 0 | 100 | 23199.51 | 13.84 | 25.37 | 136 | N/A | N/A |
|  CBR Transport Header Proxy | 1G | 500 | 102400 | 0 | 100 | 23463.42 | 13.6 | 22.68 | 124 | N/A | N/A |
|  CBR Transport Header Proxy | 1G | 1000 | 500 | 0 | 100 | 20059.09 | 30.21 | 54.22 | 283 | N/A | N/A |
|  CBR Transport Header Proxy | 1G | 1000 | 1024 | 0 | 100 | 20818.43 | 29.47 | 55.65 | 309 | N/A | N/A |
|  CBR Transport Header Proxy | 1G | 1000 | 10240 | 0 | 100 | 20289.23 | 30.22 | 58.75 | 315 | N/A | N/A |
|  CBR Transport Header Proxy | 1G | 1000 | 102400 | 0 | 100 | 19833.9 | 29.93 | 49.03 | 252 | N/A | N/A |
|  Direct Proxy | 1G | 100 | 500 | 0 | 0 | 3155.09 | 23.88 | 47.16 | 106 | 92.76 | 90 |
|  Direct Proxy | 1G | 100 | 1024 | 0 | 0 | 4043.83 | 18.58 | 58.5 | 76 | N/A | N/A |
|  Direct Proxy | 1G | 100 | 10240 | 0 | 0 | 3137.91 | 23.97 | 43.51 | 84 | N/A | N/A |
|  Direct Proxy | 1G | 100 | 102400 | 0 | 0 | 1087.84 | 69.12 | 36.67 | 179 | N/A | N/A |
|  Direct Proxy | 1G | 200 | 500 | 0 | 0 | 3898.1 | 38.48 | 103.91 | 138 | N/A | N/A |
|  Direct Proxy | 1G | 200 | 1024 | 0 | 0 | 4357.77 | 34.68 | 69.34 | 112 | N/A | N/A |
|  Direct Proxy | 1G | 200 | 10240 | 0 | 0 | 3217.76 | 46.64 | 35.34 | 140 | N/A | N/A |
|  Direct Proxy | 1G | 200 | 102400 | 0 | 0 | 1003.14 | 149.53 | 76.27 | 357 | N/A | N/A |
|  Direct Proxy | 1G | 500 | 500 | 0 | 0 | 2930.62 | 127.95 | 173.66 | 417 | 92.24 | 178.139 |
|  Direct Proxy | 1G | 500 | 1024 | 0 | 0 | 3015.94 | 124.58 | 203.52 | 1415 | N/A | N/A |
|  Direct Proxy | 1G | 500 | 10240 | 0 | 0 | 2825.31 | 132.68 | 144.88 | 323 | N/A | N/A |
|  Direct Proxy | 1G | 500 | 102400 | 0 | 0 | 904.28 | 414.94 | 120.83 | 751 | N/A | N/A |
|  Direct Proxy | 1G | 1000 | 500 | 0 | 0 | 3299.04 | 225.12 | 333.18 | 1759 | N/A | N/A |
|  Direct Proxy | 1G | 1000 | 1024 | 0 | 0 | 3592.63 | 208.41 | 210.2 | 1663 | N/A | N/A |
|  Direct Proxy | 1G | 1000 | 10240 | 0 | 0 | 2747.38 | 271.04 | 223.57 | 1095 | N/A | N/A |
|  Direct Proxy | 1G | 1000 | 102400 | 0 | 0 | 835 | 894.41 | 250.78 | 1583 | N/A | N/A |
|  XSLT Enhanced Proxy | 1G | 100 | 500 | 0 | 0 | 2707.56 | 27.8 | 42.24 | 114 | N/A | N/A |
|  XSLT Enhanced Proxy | 1G | 100 | 1024 | 0 | 0 | 2437.96 | 31.17 | 42.92 | 134 | N/A | N/A |
|  XSLT Enhanced Proxy | 1G | 100 | 10240 | 0 | 0 | 542.53 | 139.05 | 59.22 | 301 | N/A | N/A |
|  XSLT Enhanced Proxy | 1G | 100 | 102400 | 0 | 0 | 69.84 | 1071.4 | 315.7 | 1871 | N/A | N/A |
|  XSLT Enhanced Proxy | 1G | 200 | 500 | 0 | 0 | 2768.83 | 54.26 | 54.89 | 245 | N/A | N/A |
|  XSLT Enhanced Proxy | 1G | 200 | 1024 | 0 | 0 | 2523.3 | 59.53 | 51.32 | 236 | N/A | N/A |
|  XSLT Enhanced Proxy | 1G | 200 | 10240 | 0 | 0 | 564.86 | 266.05 | 103.7 | 567 | N/A | N/A |
|  XSLT Enhanced Proxy | 1G | 200 | 102400 | 0 | 0 | 67.14 | 2200.11 | 678.7 | 3839 | N/A | N/A |
|  XSLT Enhanced Proxy | 1G | 500 | 500 | 0 | 100 | 23490.7 | 13.58 | 23.88 | 129 | N/A | N/A |
|  XSLT Enhanced Proxy | 1G | 500 | 1024 | 0 | 100 | 23617.12 | 13.71 | 25.51 | 143 | N/A | N/A |
|  XSLT Enhanced Proxy | 1G | 500 | 10240 | 0 | 100 | 23294.18 | 13.72 | 24.09 | 131 | N/A | N/A |
|  XSLT Enhanced Proxy | 1G | 500 | 102400 | 0 | 100 | 23792.73 | 13.59 | 24.39 | 134 | N/A | N/A |
|  XSLT Enhanced Proxy | 1G | 1000 | 500 | 0 | 100 | 20325.76 | 29.45 | 58.47 | 309 | N/A | N/A |
|  XSLT Enhanced Proxy | 1G | 1000 | 1024 | 0 | 100 | 20324.75 | 29.5 | 52.78 | 273 | N/A | N/A |
|  XSLT Enhanced Proxy | 1G | 1000 | 10240 | 0 | 100 | 19671.75 | 30.82 | 60.28 | 319 | N/A | N/A |
|  XSLT Enhanced Proxy | 1G | 1000 | 102400 | 0 | 100 | 20128.88 | 30.78 | 49.94 | 259 | N/A | N/A |
|  XSLT Proxy | 1G | 100 | 500 | 0 | 0 | 1827.52 | 41.26 | 34.84 | 148 | N/A | N/A |
|  XSLT Proxy | 1G | 100 | 1024 | 0 | 0 | 1432.62 | 52.65 | 54.56 | 175 | N/A | N/A |
|  XSLT Proxy | 1G | 100 | 10240 | 0 | 0 | 323.16 | 233.17 | 131.34 | 583 | N/A | N/A |
|  XSLT Proxy | 1G | 100 | 102400 | 0 | 0 | 25.59 | 2908.94 | 775.58 | 4703 | N/A | N/A |
|  XSLT Proxy | 1G | 200 | 500 | 0 | 0 | 2047.93 | 73.45 | 41.64 | 200 | N/A | N/A |
|  XSLT Proxy | 1G | 200 | 1024 | 0 | 0 | 1633.43 | 92.13 | 53.23 | 269 | N/A | N/A |
|  XSLT Proxy | 1G | 200 | 10240 | 0 | 0 | 313.72 | 480.39 | 225.08 | 1199 | N/A | N/A |
|  XSLT Proxy | 1G | 200 | 102400 | 0 | 0 | 20.53 | 7095.98 | 1883.04 | 11647 | N/A | N/A |
|  XSLT Proxy | 1G | 500 | 500 | 0 | 100 | 1.66 | 120064 | 0 | 120319 | N/A | N/A |
|  XSLT Proxy | 1G | 500 | 1024 | 0 | 100 | 1.66 | 120064 | 0 | 120319 | N/A | N/A |
|  XSLT Proxy | 1G | 500 | 10240 | 0 | 100 | 1.66 | 120064 | 0 | 120319 | N/A | N/A |
|  XSLT Proxy | 1G | 500 | 102400 | 0 | 100 | 1.66 | 120064 | 0 | 120319 | N/A | N/A |
|  XSLT Proxy | 1G | 1000 | 500 | 0 | 100 | 3.32 | 120064 | 0 | 120319 | N/A | N/A |
|  XSLT Proxy | 1G | 1000 | 1024 | 0 | 100 | 3.32 | 120064 | 0 | 120319 | N/A | N/A |
|  XSLT Proxy | 1G | 1000 | 10240 | 0 | 100 | 3.32 | 120064 | 0 | 120319 | N/A | N/A |
|  XSLT Proxy | 1G | 1000 | 102400 | 0 | 100 | 3.32 | 120064 | 0 | 120319 | N/A | N/A |
