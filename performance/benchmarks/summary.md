# WSO2 Micro Integrator 4.2.0-beta Performance Test Results

During each release, we execute various automated performance test scenarios and publish the results.

| Test Scenarios | Description |
| --- | --- |
| Direct Proxy | Passthrough proxy service |
| CBR Proxy | Routing the message based on the content of the message body |
| XSLT Proxy | Having XSLT transformations in request and response paths |
| Direct API | Passthrough API service |

Our test client is [Apache JMeter](https://jmeter.apache.org/index.html). We test each scenario for a fixed duration of
time. We split the test results into warmup and measurement parts and use the measurement part to compute the
performance metrics.

Test scenarios use a [Netty](https://netty.io/) based back-end service which echoes back any request
posted to it after a specified period of time.

We run the performance tests under different numbers of concurrent users, message sizes (payloads) and back-end service
delays.

The main performance metrics:

1. **Throughput**: The number of requests that the WSO2 Micro Integrator 4.2.0-beta processes during a specific time interval (e.g. per second).
2. **Response Time**: The end-to-end latency for an operation of invoking a service in WSO2 Micro Integrator 4.2.0-beta . The complete distribution of response times was recorded.

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


System information for WSO2 Micro Integrator 4.2.0-beta in 1st AWS CloudFormation stack.

| Class | Subclass | Description | Value |
| --- | --- | --- | --- |
| AWS | EC2 | AMI-ID | ami-0263e4deb427da90e |
| AWS | EC2 | Instance Type | c5.large |
| System | Processor | CPU(s) | 2 |
| System | Processor | Thread(s) per core | 2 |
| System | Processor | Core(s) per socket | 1 |
| System | Processor | Socket(s) | 1 |
| System | Processor | Model name | Intel(R) Xeon(R) Platinum 8275CL CPU @ 3.00GHz |
| System | Memory | BIOS | 64 KiB |
| System | Memory | System Memory | 4 GiB |
| System | Storage | Block Device: nvme0n1 | 8G |
| Operating System | Distribution | Release | Ubuntu 18.04.6 LTS |
| Operating System | Distribution | Kernel | Linux ip-10-0-1-12 5.4.0-1094-aws #102~18.04.1-Ubuntu SMP Tue Jan 10 21:07:03 UTC 2023 x86_64 x86_64 x86_64 GNU/Linux |


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

|  Scenario Name | Heap Size | Concurrent Users | Message Size (Bytes) | Back-end Service Delay (ms) | Error % | Throughput (Requests/sec) | Average Response Time (ms) | Standard Deviation of Response Time (ms) | 99th Percentile of Response Time (ms) | WSO2 Micro Integrator 4.2.0-beta GC Throughput (%) | Average WSO2 Micro Integrator 4.2.0-beta Memory Footprint After Full GC (M) |
|---|---:|---:|---:|---:|---:|---:|---:|---:|---:|---:|---:|
|  CBR Proxy | 2G | 100 | 500 | 0 | 0 | 940.82 | 106.21 | 385.73 | 2559 | N/A | N/A |
|  CBR Proxy | 2G | 100 | 1000 | 0 | 0 | 770.26 | 129.74 | 427.29 | 2591 | N/A | N/A |
|  CBR Proxy | 2G | 100 | 10000 | 0 | 0 | 836.06 | 119.5 | 92.56 | 357 | N/A | N/A |
|  CBR Proxy | 2G | 100 | 100000 | 0 | 0 | 83.14 | 1199.45 | 318.58 | 2223 | N/A | N/A |
|  CBR Proxy | 2G | 200 | 500 | 0 | 0 | 749.83 | 266.6 | 746.91 | 2799 | N/A | N/A |
|  CBR Proxy | 2G | 200 | 1000 | 0 | 0 | 527.36 | 379.16 | 873.04 | 2895 | N/A | N/A |
|  CBR Proxy | 2G | 200 | 10000 | 0 | 0 | 774.6 | 258.15 | 217.21 | 1255 | N/A | N/A |
|  CBR Proxy | 2G | 200 | 100000 | 0 | 0 | 71.1 | 2790.9 | 772.92 | 4671 | N/A | N/A |
|  CBR Proxy | 2G | 500 | 500 | 0 | 0 | 640.07 | 780.94 | 1273.94 | 5439 | N/A | N/A |
|  CBR Proxy | 2G | 500 | 1000 | 0 | 0 | 559.83 | 892.9 | 1336.71 | 5471 | N/A | N/A |
|  CBR Proxy | 2G | 500 | 10000 | 0 | 0 | 587.82 | 849.2 | 562.88 | 2927 | N/A | N/A |
|  CBR Proxy | 2G | 500 | 100000 | 0 | 0 | 55.99 | 8678.49 | 2325.44 | 13823 | N/A | N/A |
|  CBR Proxy | 2G | 1000 | 500 | 0 | 0 | 736.69 | 1356.39 | 1697.1 | 8447 | N/A | N/A |
|  CBR Proxy | 2G | 1000 | 1000 | 0 | 0 | 604.67 | 1642.39 | 1846 | 8575 | N/A | N/A |
|  CBR Proxy | 2G | 1000 | 10000 | 0 | 0 | 567.05 | 1756.1 | 832.39 | 4351 | N/A | N/A |
|  CBR Proxy | 2G | 1000 | 100000 | 0 | 0 | 40 | 24276.79 | 4633.98 | 31359 | N/A | N/A |
|  Direct API | 2G | 100 | 500 | 0 | 0 | 572.92 | 174.46 | 613.95 | 2655 | N/A | N/A |
|  Direct API | 2G | 100 | 1000 | 0 | 0 | 448.62 | 222.82 | 608.68 | 2687 | N/A | N/A |
|  Direct API | 2G | 100 | 10000 | 0 | 0 | 979.24 | 102.05 | 344.08 | 2575 | N/A | N/A |
|  Direct API | 2G | 100 | 100000 | 0 | 0 | 1174.31 | 85.01 | 22.3 | 160 | N/A | N/A |
|  Direct API | 2G | 200 | 500 | 0 | 0 | 425.89 | 467.9 | 1200.17 | 5343 | N/A | N/A |
|  Direct API | 2G | 200 | 1000 | 0 | 0 | 198.27 | 998.21 | 1786.65 | 7871 | N/A | N/A |
|  Direct API | 2G | 200 | 10000 | 0 | 0 | 766.69 | 260.78 | 715.02 | 2879 | N/A | N/A |
|  Direct API | 2G | 200 | 100000 | 0 | 0 | 1166.64 | 171.22 | 40.27 | 289 | N/A | N/A |
|  Direct API | 2G | 500 | 500 | 0 | 0 | 270.37 | 1826.25 | 3876.78 | 18559 | N/A | N/A |
|  Direct API | 2G | 500 | 1000 | 0 | 0 | 169.6 | 2931.27 | 5404.95 | 26239 | N/A | N/A |
|  Direct API | 2G | 500 | 10000 | 0 | 0 | 396.56 | 1235.65 | 2609.99 | 13311 | N/A | N/A |
|  Direct API | 2G | 500 | 100000 | 0 | 0 | 1104.13 | 452.66 | 84.87 | 683 | N/A | N/A |
|  Direct API | 2G | 1000 | 500 | 0 | 0.1 | 200.76 | 3949.61 | 7473.46 | 33023 | N/A | N/A |
|  Direct API | 2G | 1000 | 1000 | 0 | 0.79 | 242.2 | 3748.76 | 12447.06 | 100351 | N/A | N/A |
|  Direct API | 2G | 1000 | 10000 | 0 | 0 | 550.77 | 1796.02 | 3028.34 | 16255 | N/A | N/A |
|  Direct API | 2G | 1000 | 100000 | 0 | 0 | 1029.47 | 969.41 | 174.56 | 1439 | N/A | N/A |
|  Direct Proxy | 2G | 100 | 500 | 0 | 0 | 4087.63 | 24.39 | 21.96 | 123 | 95.33 |  |
|  Direct Proxy | 2G | 100 | 1000 | 0 | 0 | 4072.71 | 24.48 | 22.33 | 122 | N/A | N/A |
|  Direct Proxy | 2G | 100 | 10000 | 0 | 0 | 3400.11 | 29.33 | 21.86 | 127 | N/A | N/A |
|  Direct Proxy | 2G | 100 | 100000 | 0 | 0 | 1473.62 | 67.73 | 35.41 | 168 | N/A | N/A |
|  Direct Proxy | 2G | 200 | 500 | 0 | 0 | 4069.14 | 49.06 | 35.03 | 192 | N/A | N/A |
|  Direct Proxy | 2G | 200 | 1000 | 0 | 0 | 4024.16 | 49.6 | 35.08 | 189 | N/A | N/A |
|  Direct Proxy | 2G | 200 | 10000 | 0 | 0 | 3486.56 | 57.25 | 35.14 | 197 | N/A | N/A |
|  Direct Proxy | 2G | 200 | 100000 | 0 | 0 | 1456.53 | 137.16 | 67.73 | 317 | N/A | N/A |
|  Direct Proxy | 2G | 500 | 500 | 0 | 0 | 3997.83 | 124.93 | 67.48 | 359 | N/A | N/A |
|  Direct Proxy | 2G | 500 | 1000 | 0 | 0 | 3969.67 | 125.81 | 68.26 | 357 | N/A | N/A |
|  Direct Proxy | 2G | 500 | 10000 | 0 | 0 | 1434.26 | 348.35 | 875.83 | 5215 | N/A | N/A |
|  Direct Proxy | 2G | 500 | 100000 | 0 | 0 | 1139.29 | 438.36 | 172.42 | 827 | N/A | N/A |
|  Direct Proxy | 2G | 1000 | 500 | 0 | 1.42 | 187.94 | 4623.53 | 15223.31 | 120319 | N/A | N/A |
|  Direct Proxy | 2G | 1000 | 1000 | 0 | 0 | 148.09 | 6664.57 | 9037.2 | 45311 | N/A | N/A |
|  Direct Proxy | 2G | 1000 | 10000 | 0 | 0 | 261.89 | 3714.45 | 6418.92 | 37631 | N/A | N/A |
|  Direct Proxy | 2G | 1000 | 100000 | 0 | 0 | 1125.8 | 886.56 | 298.32 | 1631 | N/A | N/A |
|  XSLT Proxy | 2G | 100 | 500 | 0 | 0 | 1355.4 | 73.46 | 128.79 | 249 | N/A | N/A |
|  XSLT Proxy | 2G | 100 | 1000 | 0 | 0 | 1172.79 | 85.19 | 338.25 | 267 | N/A | N/A |
|  XSLT Proxy | 2G | 100 | 10000 | 0 | 0 | 279.85 | 357.21 | 197.19 | 927 | N/A | N/A |
|  XSLT Proxy | 2G | 100 | 100000 | 0 | 99.96 | 97.82 | 1020.64 | 7790.8 | 1271 | N/A | N/A |
|  XSLT Proxy | 2G | 200 | 500 | 0 | 0 | 1119.16 | 178.52 | 298.82 | 2207 | N/A | N/A |
|  XSLT Proxy | 2G | 200 | 1000 | 0 | 0 | 1031.99 | 193.67 | 253.3 | 1559 | N/A | N/A |
|  XSLT Proxy | 2G | 200 | 10000 | 0 | 0 | 261.95 | 761.98 | 341.35 | 1695 | N/A | N/A |
|  XSLT Proxy | 2G | 200 | 100000 | 0 | 100 | 145.12 | 1374.33 | 287.91 | 2143 | N/A | N/A |
|  XSLT Proxy | 2G | 500 | 500 | 0 | 0 | 807.29 | 618.87 | 770.18 | 4223 | N/A | N/A |
|  XSLT Proxy | 2G | 500 | 1000 | 0 | 0 | 825.46 | 605.37 | 582.93 | 2927 | N/A | N/A |
|  XSLT Proxy | 2G | 500 | 10000 | 0 | 0 | 220.65 | 2257.57 | 819.62 | 4543 | N/A | N/A |
|  XSLT Proxy | 2G | 500 | 100000 | 0 | 100 | 134.55 | 3685.25 | 576.75 | 5407 | N/A | N/A |
|  XSLT Proxy | 2G | 1000 | 500 | 0 | 0 | 789.55 | 1263.55 | 1064.73 | 4767 | N/A | N/A |
|  XSLT Proxy | 2G | 1000 | 1000 | 0 | 0 | 758.76 | 1315.7 | 847.9 | 4479 | N/A | N/A |
|  XSLT Proxy | 2G | 1000 | 10000 | 0 | 0 | 208.91 | 4739.98 | 1380.11 | 8447 | N/A | N/A |
|  XSLT Proxy | 2G | 1000 | 100000 | 0 | 100 | 132.19 | 7417.98 | 818.99 | 9535 | N/A | N/A |
