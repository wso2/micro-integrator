# WSO2 Micro Integrator 4.0.0 Performance Test Results

During each release, we execute various automated performance test scenarios and publish the results.

| Test Scenarios | Description |
| --- | --- |
| Direct Proxy | Passthrough proxy service |
| Direct API | Passthrough API service |
| CBR SOAP Header Proxy | Routing the message based on a SOAP header in the message payload |
| CBR Transport Header Proxy | Routing the message based on an HTTP header in the message |
| CBR Proxy | Routing the message based on the content of the message body |
| XSLT Proxy | Having XSLT transformations in request and response paths |

Our test client is [Apache JMeter](https://jmeter.apache.org/index.html). We test each scenario for a fixed duration of
time. We split the test results into warmup and measurement parts and use the measurement part to compute the
performance metrics.

Test scenarios use a [Netty](https://netty.io/) based back-end service which echoes back any request
posted to it after a specified period of time.

We run the performance tests under different numbers of concurrent users, message sizes (payloads) and back-end service
delays.

The main performance metrics:

1. **Throughput**: The number of requests that the WSO2 Micro Integrator 4.0.0 processes during a specific time interval (e.g. per second).
2. **Response Time**: The end-to-end latency for an operation of invoking a service in WSO2 Micro Integrator 4.0.0 . The complete distribution of response times was recorded.

In addition to the above metrics, we measure the load average and several memory-related metrics.

The following are the test parameters.

| Test Parameter | Description | Values |
| --- | --- | --- |
| Scenario Name | The name of the test scenario. | Refer to the above table. |
| Heap Size | The amount of memory allocated to the application | 2G |
| Concurrent Users | The number of users accessing the application at the same time. | 100, 200, 500, 1000 |
| Message Size (Bytes) | The request payload size in Bytes. | 500, 1000, 10000, 100000 |
| Back-end Delay (ms) | The delay added by the Back-end service. | 0 |

The duration of each test is **360 seconds**. The warm-up period is **120 seconds**.
The measurement results are collected after the warm-up period.

The performance tests were executed on 1 AWS CloudFormation stack.


System information for WSO2 Micro Integrator 4.0.0 in 1st AWS CloudFormation stack.

| Class | Subclass | Description | Value |
| --- | --- | --- | --- |
| AWS | EC2 | AMI-ID | ami-0747bdcabd34c712a |
| AWS | EC2 | Instance Type | c5.large |
| System | Processor | CPU(s) | 2 |
| System | Processor | Thread(s) per core | 2 |
| System | Processor | Core(s) per socket | 1 |
| System | Processor | Socket(s) | 1 |
| System | Processor | Model name | Intel(R) Xeon(R) Platinum 8124M CPU @ 3.00GHz |
| System | Memory | BIOS | 64 KiB |
| System | Memory | System memory | 3785188 KiB |
| System | Storage | Block Device: nvme0n1 | 8G |
| Operating System | Distribution | Release | Ubuntu 18.04.5 LTS |
| Operating System | Distribution | Kernel | Linux ip-10-0-1-26 5.4.0-1045-aws #47~18.04.1-Ubuntu SMP Tue Apr 13 15:58:14 UTC 2021 x86_64 x86_64 x86_64 GNU/Linux |


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

|  Scenario Name | Heap Size | Concurrent Users | Message Size (Bytes) | Back-end Service Delay (ms) | Error % | Throughput (Requests/sec) | Average Response Time (ms) | Standard Deviation of Response Time (ms) | 99th Percentile of Response Time (ms) | WSO2 Micro Integrator 4.0.0 GC Throughput (%) | Average WSO2 Micro Integrator 4.0.0 Memory Footprint After Full GC (M) |
|---|---:|---:|---:|---:|---:|---:|---:|---:|---:|---:|---:|
|  Direct API | 2G | 100 | 500 | 0 | 0 | 3975.54 | 25.06 | 36.77 | 112 | 92.4 | 196.363 |
|  Direct API | 2G | 100 | 1000 | 0 | 0 | 4016.06 | 24.81 | 37.63 | 110 | N/A | N/A |
|  Direct API | 2G | 100 | 10000 | 0 | 0 | 3472.02 | 28.69 | 30.48 | 116 | N/A | N/A |
|  Direct API | 2G | 100 | 100000 | 0 | 0 | 1517.39 | 65.67 | 43.74 | 175 | N/A | N/A |
|  Direct API | 2G | 200 | 500 | 0 | 0 | 4107.29 | 48.57 | 57.04 | 160 | N/A | N/A |
|  Direct API | 2G | 200 | 1000 | 0 | 0 | 4106.4 | 48.59 | 50.25 | 161 | N/A | N/A |
|  Direct API | 2G | 200 | 10000 | 0 | 0 | 3536.09 | 56.3 | 42.07 | 172 | N/A | N/A |
|  Direct API | 2G | 200 | 100000 | 0 | 0 | 1506.66 | 132.48 | 69.11 | 309 | N/A | N/A |
|  Direct API | 2G | 500 | 500 | 0 | 0 | 4069.84 | 122.64 | 94.23 | 311 | N/A | N/A |
|  Direct API | 2G | 500 | 1000 | 0 | 0 | 4055.12 | 123.12 | 97.01 | 321 | N/A | N/A |
|  Direct API | 2G | 500 | 10000 | 0 | 0 | 3513.18 | 142.09 | 80.47 | 337 | N/A | N/A |
|  Direct API | 2G | 500 | 100000 | 0 | 0 | 1206.41 | 414 | 149.82 | 759 | N/A | N/A |
|  Direct API | 2G | 1000 | 500 | 0 | 0 | 4021.59 | 248.43 | 169.77 | 575 | 89.65 | 300.26 |
|  Direct API | 2G | 1000 | 1000 | 0 | 0 | 4086.02 | 244.48 | 141.99 | 575 | N/A | N/A |
|  Direct API | 2G | 1000 | 10000 | 0 | 0 | 3470.04 | 287.85 | 158.52 | 607 | N/A | N/A |
|  Direct API | 2G | 1000 | 100000 | 0 | 0 | 1393.55 | 716.26 | 234.68 | 1343 | N/A | N/A |
|  Direct Proxy | 2G | 100 | 500 | 0 | 0 | 4203.41 | 23.7 | 38.04 | 109 | 91.83 | 246.502 |
|  Direct Proxy | 2G | 100 | 1000 | 0 | 0 | 4228.91 | 23.55 | 37.33 | 108 | N/A | N/A |
|  Direct Proxy | 2G | 100 | 10000 | 0 | 0 | 3597.87 | 27.68 | 28.7 | 112 | N/A | N/A |
|  Direct Proxy | 2G | 100 | 100000 | 0 | 0 | 1541.12 | 64.66 | 31.19 | 145 | N/A | N/A |
|  Direct Proxy | 2G | 200 | 500 | 0 | 0 | 4268.31 | 46.72 | 54.2 | 157 | N/A | N/A |
|  Direct Proxy | 2G | 200 | 1000 | 0 | 0 | 4266.64 | 46.75 | 50.79 | 156 | N/A | N/A |
|  Direct Proxy | 2G | 200 | 10000 | 0 | 0 | 3607.31 | 55.3 | 42.58 | 166 | N/A | N/A |
|  Direct Proxy | 2G | 200 | 100000 | 0 | 0 | 1469.57 | 135.77 | 44.24 | 244 | N/A | N/A |
|  Direct Proxy | 2G | 500 | 500 | 0 | 0 | 4033.64 | 123.78 | 117.55 | 323 | 89.56 | 340.016 |
|  Direct Proxy | 2G | 500 | 1000 | 0 | 0 | 4159.01 | 120.05 | 90.32 | 313 | N/A | N/A |
|  Direct Proxy | 2G | 500 | 10000 | 0 | 0 | 3536.64 | 141.01 | 77.96 | 323 | N/A | N/A |
|  Direct Proxy | 2G | 500 | 100000 | 0 | 0 | 1371.6 | 364.02 | 78.14 | 563 | N/A | N/A |
|  Direct Proxy | 2G | 1000 | 500 | 0 | 0 | 3973.43 | 251.57 | 175.3 | 579 | 89.73 | 308.357 |
|  Direct Proxy | 2G | 1000 | 1000 | 0 | 0 | 4067.36 | 245.64 | 165.93 | 567 | N/A | N/A |
|  Direct Proxy | 2G | 1000 | 10000 | 0 | 0 | 3434.74 | 290.96 | 145.72 | 575 | N/A | N/A |
|  Direct Proxy | 2G | 1000 | 100000 | 0 | 0 | 1326.44 | 752.49 | 130.18 | 1119 | N/A | N/A |
|  CBR SOAP Header Proxy | 2G | 100 | 500 | 0 | 0 | 3452.08 | 28.87 | 31.48 | 113 | 93.56 | 167.992 |
|  CBR SOAP Header Proxy | 2G | 100 | 1000 | 0 | 0 | 3299.98 | 30.21 | 32.62 | 112 | N/A | N/A |
|  CBR SOAP Header Proxy | 2G | 100 | 10000 | 0 | 0 | 1521.07 | 65.6 | 42.06 | 201 | N/A | N/A |
|  CBR SOAP Header Proxy | 2G | 100 | 100000 | 0 | 0 | 209.68 | 476.68 | 157.5 | 871 | N/A | N/A |
|  CBR SOAP Header Proxy | 2G | 200 | 500 | 0 | 0 | 3582.67 | 55.7 | 39.05 | 175 | N/A | N/A |
|  CBR SOAP Header Proxy | 2G | 200 | 1000 | 0 | 0 | 3330.62 | 59.92 | 49.51 | 185 | N/A | N/A |
|  CBR SOAP Header Proxy | 2G | 200 | 10000 | 0 | 0 | 1491.74 | 133.89 | 72.93 | 347 | N/A | N/A |
|  CBR SOAP Header Proxy | 2G | 200 | 100000 | 0 | 0 | 162.39 | 1228.71 | 482.52 | 2879 | N/A | N/A |
|  CBR SOAP Header Proxy | 2G | 500 | 500 | 0 | 0 | 3492.68 | 143.01 | 92.84 | 369 | N/A | N/A |
|  CBR SOAP Header Proxy | 2G | 500 | 1000 | 0 | 0 | 3306.38 | 151.01 | 82.14 | 385 | N/A | N/A |
|  CBR SOAP Header Proxy | 2G | 500 | 10000 | 0 | 0 | 1416.36 | 353.05 | 148.23 | 743 | N/A | N/A |
|  CBR SOAP Header Proxy | 2G | 500 | 100000 | 0 | 0 | 99.33 | 5008.19 | 1705.56 | 9535 | N/A | N/A |
|  CBR SOAP Header Proxy | 2G | 1000 | 500 | 0 | 0 | 2977.6 | 335.54 | 158.48 | 743 | N/A | N/A |
|  CBR SOAP Header Proxy | 2G | 1000 | 1000 | 0 | 0 | 3177.62 | 314.39 | 168.27 | 751 | N/A | N/A |
|  CBR SOAP Header Proxy | 2G | 1000 | 10000 | 0 | 0 | 1221.1 | 817.09 | 308.15 | 2063 | N/A | N/A |
|  CBR SOAP Header Proxy | 2G | 1000 | 100000 | 0 | 0 | 34.25 | 28454.08 | 7262.68 | 41215 | N/A | N/A |
|  CBR Transport Header Proxy | 2G | 100 | 500 | 0 | 0 | 4036.35 | 24.7 | 37.29 | 107 | 91.41 | 253.837 |
|  CBR Transport Header Proxy | 2G | 100 | 1000 | 0 | 0 | 4021.85 | 24.78 | 37.41 | 108 | N/A | N/A |
|  CBR Transport Header Proxy | 2G | 100 | 10000 | 0 | 0 | 3432 | 29.03 | 29.77 | 114 | N/A | N/A |
|  CBR Transport Header Proxy | 2G | 100 | 100000 | 0 | 0 | 1505.6 | 66.18 | 26 | 145 | N/A | N/A |
|  CBR Transport Header Proxy | 2G | 200 | 500 | 0 | 0 | 4201.46 | 47.5 | 55.28 | 158 | 90.88 | 253.389 |
|  CBR Transport Header Proxy | 2G | 200 | 1000 | 0 | 0 | 4240.82 | 47.03 | 52.57 | 152 | N/A | N/A |
|  CBR Transport Header Proxy | 2G | 200 | 10000 | 0 | 0 | 3715.9 | 53.69 | 38.28 | 162 | N/A | N/A |
|  CBR Transport Header Proxy | 2G | 200 | 100000 | 0 | 0 | 1587.97 | 125.68 | 64.85 | 283 | N/A | N/A |
|  CBR Transport Header Proxy | 2G | 500 | 500 | 0 | 0 | 4201.9 | 118.8 | 78.77 | 303 | 91.13 | 195.883 |
|  CBR Transport Header Proxy | 2G | 500 | 1000 | 0 | 0 | 4199.77 | 118.75 | 98.94 | 307 | N/A | N/A |
|  CBR Transport Header Proxy | 2G | 500 | 10000 | 0 | 0 | 3619.29 | 137.95 | 93.74 | 331 | N/A | N/A |
|  CBR Transport Header Proxy | 2G | 500 | 100000 | 0 | 0 | 1461.23 | 341.35 | 138.19 | 679 | N/A | N/A |
|  CBR Transport Header Proxy | 2G | 1000 | 500 | 0 | 0 | 4033.94 | 247.68 | 153.86 | 567 | 89.84 | 312.003 |
|  CBR Transport Header Proxy | 2G | 1000 | 1000 | 0 | 0 | 4100.07 | 243.53 | 180.13 | 551 | N/A | N/A |
|  CBR Transport Header Proxy | 2G | 1000 | 10000 | 0 | 0 | 3500.25 | 285.44 | 139.22 | 571 | N/A | N/A |
|  CBR Transport Header Proxy | 2G | 1000 | 100000 | 0 | 0 | 1363.58 | 731.82 | 139.32 | 1159 | 96.46 | 104.162 |
|  CBR Proxy | 2G | 100 | 500 | 0 | 0 | 3474.97 | 28.66 | 31.84 | 108 | 93.33 | 195.391 |
|  CBR Proxy | 2G | 100 | 1000 | 0 | 0 | 3129.2 | 31.84 | 37.75 | 111 | N/A | N/A |
|  CBR Proxy | 2G | 100 | 10000 | 0 | 0 | 1128.79 | 88.41 | 56.42 | 271 | N/A | N/A |
|  CBR Proxy | 2G | 100 | 100000 | 0 | 0 | 119.21 | 837.31 | 225.47 | 1567 | N/A | N/A |
|  CBR Proxy | 2G | 200 | 500 | 0 | 0 | 3520.42 | 56.66 | 42.33 | 178 | N/A | N/A |
|  CBR Proxy | 2G | 200 | 1000 | 0 | 0 | 3156.5 | 63.21 | 56.36 | 194 | N/A | N/A |
|  CBR Proxy | 2G | 200 | 10000 | 0 | 0 | 1100.93 | 181.48 | 92.72 | 459 | N/A | N/A |
|  CBR Proxy | 2G | 200 | 100000 | 0 | 0 | 89.17 | 2234.84 | 808.55 | 4255 | N/A | N/A |
|  CBR Proxy | 2G | 500 | 500 | 0 | 0 | 3472.8 | 143.76 | 74.97 | 373 | N/A | N/A |
|  CBR Proxy | 2G | 500 | 1000 | 0 | 0 | 3114.5 | 160.29 | 88.42 | 403 | N/A | N/A |
|  CBR Proxy | 2G | 500 | 10000 | 0 | 0 | 999.88 | 499.66 | 190.44 | 1011 | N/A | N/A |
|  CBR Proxy | 2G | 500 | 100000 | 0 | 0 | 43.35 | 11302.74 | 3841.35 | 18559 | N/A | N/A |
|  CBR Proxy | 2G | 1000 | 500 | 0 | 0 | 2857.01 | 349.57 | 162.35 | 815 | N/A | N/A |
|  CBR Proxy | 2G | 1000 | 1000 | 0 | 0 | 3016.05 | 331.13 | 153.7 | 747 | N/A | N/A |
|  CBR Proxy | 2G | 1000 | 10000 | 0 | 0 | 803.79 | 1240.18 | 465.62 | 2831 | N/A | N/A |
|  CBR Proxy | 2G | 1000 | 100000 | 0 | 100 | 7.12 | 120377.22 | 2799.62 | 146431 | N/A | N/A |
|  XSLT Proxy | 2G | 100 | 500 | 0 | 0 | 1547.71 | 52.34 | 436.93 | 170 | 94.68 | 142.103 |
|  XSLT Proxy | 2G | 100 | 1000 | 0 | 0 | 1592.75 | 62.46 | 338.69 | 196 | N/A | N/A |
|  XSLT Proxy | 2G | 100 | 10000 | 0 | 0 | 360.72 | 276.95 | 152.31 | 723 | N/A | N/A |
|  XSLT Proxy | 2G | 100 | 100000 | 0 | 0 | 32 | 3098.78 | 589.29 | 4607 | N/A | N/A |
|  XSLT Proxy | 2G | 200 | 500 | 0 | 0 | 1972.36 | 101.23 | 62.2 | 283 | N/A | N/A |
|  XSLT Proxy | 2G | 200 | 1000 | 0 | 0 | 1608.68 | 124.15 | 72.39 | 331 | N/A | N/A |
|  XSLT Proxy | 2G | 200 | 10000 | 0 | 0 | 353.27 | 565.41 | 248.76 | 1255 | N/A | N/A |
|  XSLT Proxy | 2G | 200 | 100000 | 0 | 0 | 27.51 | 7129.06 | 1388.78 | 9919 | N/A | N/A |
|  XSLT Proxy | 2G | 500 | 500 | 0 | 0 | 1871.08 | 267.12 | 126.28 | 655 | 92.67 | 241.46 |
|  XSLT Proxy | 2G | 500 | 1000 | 0 | 0 | 1606.98 | 311.04 | 141.12 | 739 | N/A | N/A |
|  XSLT Proxy | 2G | 500 | 10000 | 0 | 0 | 312.37 | 1594.62 | 451.21 | 2943 | N/A | N/A |
|  XSLT Proxy | 2G | 500 | 100000 | 0 | 0 | 14.29 | 31965.47 | 5126.3 | 45567 | N/A | N/A |
|  XSLT Proxy | 2G | 1000 | 500 | 0 | 0 | 1676.77 | 595.24 | 217.13 | 1223 | N/A | N/A |
|  XSLT Proxy | 2G | 1000 | 1000 | 0 | 0 | 1425.89 | 699.79 | 250.84 | 1479 | N/A | N/A |
|  XSLT Proxy | 2G | 1000 | 10000 | 0 | 0 | 268.24 | 3694.27 | 927.61 | 5983 | N/A | N/A |
|  XSLT Proxy | 2G | 1000 | 100000 | 0 | 96.46 | 5.69 | 120001.16 | 4870.34 | 136191 | N/A | N/A |

