# WSO2 Micro Integrator 1.2.0 Performance Test Results

During each release, we execute various automated performance test scenarios and publish the results.

| Test Scenarios | Description |
| --- | --- |
| Direct Proxy | Passthrough proxy service |
| XSLT Proxy | Having XSLT transformations in request and response paths |
| Direct API | Passthrough API service |
| Message Building Proxy | Message Building Proxy service |
| Clone & Aggregate With 2 Backend Proxy | Clone payload and send to 2 backends and aggregate the response back |
| Clone & Aggregate With 4 Backend Proxy | Clone payload and send to 4 backends and aggregate the response back |
| Enrich Back & Forth Proxy | Enrich payload to a property and enrich back in the response |

Our test client is [Apache JMeter](https://jmeter.apache.org/index.html). We test each scenario for a fixed duration of
time. We split the test results into warmup and measurement parts and use the measurement part to compute the
performance metrics.

Test scenarios use a [Netty](https://netty.io/) based back-end service which echoes back any request
posted to it after a specified period of time.

We run the performance tests under different numbers of concurrent users, message sizes (payloads) and back-end service
delays.

The main performance metrics:

1. **Throughput**: The number of requests that the WSO2 Micro Integrator 1.2.0 processes during a specific time interval (e.g. per second).
2. **Response Time**: The end-to-end latency for an operation of invoking a service in WSO2 Micro Integrator 1.2.0 . The complete distribution of response times was recorded.

In addition to the above metrics, we measure the load average and several memory-related metrics.

The following are the test parameters.

| Test Parameter | Description | Values |
| --- | --- | --- |
| Scenario Name | The name of the test scenario. | Refer to the above table. |
| Heap Size | The amount of memory allocated to the application | 2G |
| Concurrent Users | The number of users accessing the application at the same time. | 100, 200 |
| Message Size (Bytes) | The request payload size in Bytes. | 500, 1000, 10000, 100000 |
| Back-end Delay (ms) | The delay added by the Back-end service. | 0 |

The duration of each test is **60 seconds**. The warm-up period is **30 seconds**.
The measurement results are collected after the warm-up period.

The performance tests were executed on 2 AWS CloudFormation stacks.


System information for WSO2 Micro Integrator 1.2.0 in 1st AWS CloudFormation stack.

| Class | Subclass | Description | Value |
| --- | --- | --- | --- |
| AWS | EC2 | AMI-ID | ami-0bcc094591f354be2 |
| AWS | EC2 | Instance Type | c5.large |
| System | Processor | CPU(s) | 2 |
| System | Processor | Thread(s) per core | 2 |
| System | Processor | Core(s) per socket | 1 |
| System | Processor | Socket(s) | 1 |
| System | Processor | Model name | Intel(R) Xeon(R) Platinum 8124M CPU @ 3.00GHz |
| System | Memory | BIOS | 64 KiB |
| System | Memory | System memory | 3785232 KiB |
| System | Storage | Block Device: nvme0n1 | 8G |
| Operating System | Distribution | Release | Ubuntu 18.04.5 LTS |
| Operating System | Distribution | Kernel | Linux ip-10-0-1-162 5.3.0-1032-aws #34~18.04.2-Ubuntu SMP Fri Jul 24 10:06:28 UTC 2020 x86_64 x86_64 x86_64 GNU/Linux |
System information for WSO2 Micro Integrator 1.2.0 in 2nd AWS CloudFormation stack.

| Class | Subclass | Description | Value |
| --- | --- | --- | --- |
| AWS | EC2 | AMI-ID | ami-0bcc094591f354be2 |
| AWS | EC2 | Instance Type | c5.large |
| System | Processor | CPU(s) | 2 |
| System | Processor | Thread(s) per core | 2 |
| System | Processor | Core(s) per socket | 1 |
| System | Processor | Socket(s) | 1 |
| System | Processor | Model name | Intel(R) Xeon(R) Platinum 8124M CPU @ 3.00GHz |
| System | Memory | BIOS | 64 KiB |
| System | Memory | System memory | 3785216 KiB |
| System | Storage | Block Device: nvme0n1 | 8G |
| Operating System | Distribution | Release | Ubuntu 18.04.5 LTS |
| Operating System | Distribution | Kernel | Linux ip-10-0-1-52 5.3.0-1032-aws #34~18.04.2-Ubuntu SMP Fri Jul 24 10:06:28 UTC 2020 x86_64 x86_64 x86_64 GNU/Linux |


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

|  Scenario Name | Heap Size | Concurrent Users | Message Size (Bytes) | Back-end Service Delay (ms) | Error % | Throughput (Requests/sec) | Average Response Time (ms) | Standard Deviation of Response Time (ms) | 99th Percentile of Response Time (ms) | WSO2 Micro Integrator 1.2.0 GC Throughput (%) | Average WSO2 Micro Integrator 1.2.0 Memory Footprint After Full GC (M) |
|---|---:|---:|---:|---:|---:|---:|---:|---:|---:|---:|---:|
|  Clone & Aggregate With 4 Backend Proxy | 2G | 100 | 500 | 0 | 0 | 1339.56 | 56.29 | 37.96 | 187 | N/A | N/A |
|  Clone & Aggregate With 4 Backend Proxy | 2G | 100 | 1000 | 0 | 0 | 1020.9 | 73.93 | 68 | 250 | N/A | N/A |
|  Clone & Aggregate With 4 Backend Proxy | 2G | 100 | 10000 | 0 | 0 | 213.1 | 354.02 | 161.34 | 819 | N/A | N/A |
|  Clone & Aggregate With 4 Backend Proxy | 2G | 100 | 100000 | 0 | 0 | 14.95 | 4910.17 | 1765.43 | 9407 | N/A | N/A |
|  Clone & Aggregate With 4 Backend Proxy | 2G | 200 | 500 | 0 | 0 | 1242.98 | 120.99 | 69.19 | 323 | N/A | N/A |
|  Clone & Aggregate With 4 Backend Proxy | 2G | 200 | 1000 | 0 | 0 | 989.73 | 152.03 | 90.15 | 421 | N/A | N/A |
|  Clone & Aggregate With 4 Backend Proxy | 2G | 200 | 10000 | 0 | 0 | 202.81 | 738.41 | 289.59 | 1463 | N/A | N/A |
|  Clone & Aggregate With 4 Backend Proxy | 2G | 200 | 100000 | 0 | 0 | 11.07 | 12510.22 | 2794.7 | 18175 | N/A | N/A |
|  Clone & Aggregate With 2 Backend Proxy | 2G | 100 | 500 | 0 | 0 | 1444.47 | 52.24 | 30.31 | 154 | N/A | N/A |
|  Clone & Aggregate With 2 Backend Proxy | 2G | 100 | 1000 | 0 | 0 | 1127.26 | 66.93 | 40.93 | 198 | N/A | N/A |
|  Clone & Aggregate With 2 Backend Proxy | 2G | 100 | 10000 | 0 | 0 | 215.85 | 349.65 | 165.45 | 823 | N/A | N/A |
|  Clone & Aggregate With 2 Backend Proxy | 2G | 100 | 100000 | 0 | 0 | 14.96 | 4869.32 | 1287.27 | 7327 | N/A | N/A |
|  Clone & Aggregate With 2 Backend Proxy | 2G | 200 | 500 | 0 | 0 | 1370.57 | 109.82 | 70.36 | 277 | N/A | N/A |
|  Clone & Aggregate With 2 Backend Proxy | 2G | 200 | 1000 | 0 | 0 | 1080.2 | 139.18 | 74.99 | 375 | N/A | N/A |
|  Clone & Aggregate With 2 Backend Proxy | 2G | 200 | 10000 | 0 | 0 | 203.71 | 732.68 | 296.86 | 1511 | N/A | N/A |
|  Clone & Aggregate With 2 Backend Proxy | 2G | 200 | 100000 | 0 | 0 | 11.23 | 12107.49 | 3599.4 | 19583 | N/A | N/A |
|  Direct API | 2G | 100 | 500 | 0 | 0 | 4074.52 | 18.45 | 15.95 | 101 | N/A | N/A |
|  Direct API | 2G | 100 | 1000 | 0 | 0 | 3994.6 | 18.84 | 16.82 | 103 | N/A | N/A |
|  Direct API | 2G | 100 | 10000 | 0 | 0 | 3442.6 | 21.84 | 17.57 | 108 | N/A | N/A |
|  Direct API | 2G | 100 | 100000 | 0 | 0 | 1425.44 | 52.81 | 29.14 | 127 | N/A | N/A |
|  Direct API | 2G | 200 | 500 | 0 | 0 | 4150.56 | 35.84 | 24.81 | 134 | N/A | N/A |
|  Direct API | 2G | 200 | 1000 | 0 | 0 | 4049.18 | 37.05 | 26.86 | 142 | N/A | N/A |
|  Direct API | 2G | 200 | 10000 | 0 | 0 | 3418.49 | 43.86 | 29.27 | 156 | N/A | N/A |
|  Direct API | 2G | 200 | 100000 | 0 | 0 | 1306.17 | 114.96 | 62.83 | 241 | N/A | N/A |
|  Direct Proxy | 2G | 100 | 500 | 0 | 0 | 3046.19 | 24.74 | 22.46 | 126 | 94.96 | 28.043 |
|  Direct Proxy | 2G | 100 | 1000 | 0 | 0 | 3607.11 | 20.87 | 43.06 | 110 | N/A | N/A |
|  Direct Proxy | 2G | 100 | 10000 | 0 | 0 | 3528.93 | 21.31 | 18.44 | 102 | N/A | N/A |
|  Direct Proxy | 2G | 100 | 100000 | 0 | 0 | 1486.16 | 50.62 | 22.54 | 145 | N/A | N/A |
|  Direct Proxy | 2G | 200 | 500 | 0 | 0 | 3090.93 | 48.58 | 35.88 | 181 | 94.31 | 28.278 |
|  Direct Proxy | 2G | 200 | 1000 | 0 | 0 | 3612.57 | 41.56 | 31.39 | 159 | N/A | N/A |
|  Direct Proxy | 2G | 200 | 10000 | 0 | 0 | 3478.29 | 43.14 | 38.68 | 148 | N/A | N/A |
|  Direct Proxy | 2G | 200 | 100000 | 0 | 0 | 1412.58 | 106.24 | 39.26 | 241 | N/A | N/A |
|  Enrich Back & Forth Proxy | 2G | 100 | 500 | 0 | 0 | 2238.57 | 33.66 | 30.34 | 170 | N/A | N/A |
|  Enrich Back & Forth Proxy | 2G | 100 | 1000 | 0 | 0 | 1859.96 | 40.53 | 44.24 | 164 | N/A | N/A |
|  Enrich Back & Forth Proxy | 2G | 100 | 10000 | 0 | 0 | 468.47 | 161.12 | 105.15 | 483 | N/A | N/A |
|  Enrich Back & Forth Proxy | 2G | 100 | 100000 | 0 | 0 | 49.2 | 1525.38 | 409.33 | 2527 | N/A | N/A |
|  Enrich Back & Forth Proxy | 2G | 200 | 500 | 0 | 0 | 2145.61 | 69.95 | 51.3 | 247 | N/A | N/A |
|  Enrich Back & Forth Proxy | 2G | 200 | 1000 | 0 | 0 | 1830.1 | 81.92 | 54.25 | 263 | N/A | N/A |
|  Enrich Back & Forth Proxy | 2G | 200 | 10000 | 0 | 0 | 457.5 | 328.99 | 170.79 | 835 | N/A | N/A |
|  Enrich Back & Forth Proxy | 2G | 200 | 100000 | 0 | 0 | 37.6 | 3938.91 | 1304.77 | 7103 | N/A | N/A |
|  Message Building Proxy | 2G | 100 | 500 | 0 | 0 | 2338.63 | 32.2 | 22.23 | 116 | N/A | N/A |
|  Message Building Proxy | 2G | 100 | 1000 | 0 | 0 | 1464.18 | 51.52 | 53.67 | 164 | N/A | N/A |
|  Message Building Proxy | 2G | 100 | 10000 | 0 | 0 | 171.96 | 439.61 | 164.92 | 903 | N/A | N/A |
|  Message Building Proxy | 2G | 100 | 100000 | 0 | 0 | 8.14 | 8680.03 | 1483.51 | 11839 | N/A | N/A |
|  Message Building Proxy | 2G | 200 | 500 | 0 | 0 | 2317.54 | 64.82 | 37.83 | 190 | N/A | N/A |
|  Message Building Proxy | 2G | 200 | 1000 | 0 | 0 | 1462.38 | 102.66 | 62.35 | 293 | N/A | N/A |
|  Message Building Proxy | 2G | 200 | 10000 | 0 | 0 | 165.3 | 906.57 | 351.51 | 1783 | N/A | N/A |
|  Message Building Proxy | 2G | 200 | 100000 | 0 | 0 | 7.15 | 18474.05 | 4092.79 | 26367 | N/A | N/A |
|  XSLT Proxy | 2G | 100 | 500 | 0 | 0 | 1843.18 | 40.89 | 42.33 | 145 | N/A | N/A |
|  XSLT Proxy | 2G | 100 | 1000 | 0 | 0 | 1634.09 | 46.14 | 32.13 | 159 | N/A | N/A |
|  XSLT Proxy | 2G | 100 | 10000 | 0 | 0 | 345.98 | 218.19 | 135.68 | 623 | N/A | N/A |
|  XSLT Proxy | 2G | 100 | 100000 | 0 | 0 | 32.46 | 2269.69 | 493.45 | 3567 | N/A | N/A |
|  XSLT Proxy | 2G | 200 | 500 | 0 | 0 | 1630.77 | 92.09 | 64.17 | 279 | N/A | N/A |
|  XSLT Proxy | 2G | 200 | 1000 | 0 | 0 | 1598.14 | 94.11 | 55.97 | 293 | N/A | N/A |
|  XSLT Proxy | 2G | 200 | 10000 | 0 | 0 | 341.96 | 438.61 | 225.11 | 1039 | N/A | N/A |
|  XSLT Proxy | 2G | 200 | 100000 | 0 | 0 | 28.63 | 5120.06 | 1477.57 | 8895 | N/A | N/A |
