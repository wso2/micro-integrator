# WSO2 Micro Integrator 4.2.0-beta-performance Performance Test Results

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

1. **Throughput**: The number of requests that the WSO2 Micro Integrator 4.2.0-beta-performance processes during a specific time interval (e.g. per second).
2. **Response Time**: The end-to-end latency for an operation of invoking a service in WSO2 Micro Integrator 4.2.0-beta-performance . The complete distribution of response times was recorded.

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


System information for WSO2 Micro Integrator 4.2.0-beta-performance in 1st AWS CloudFormation stack.

| Class | Subclass | Description | Value |
| --- | --- | --- | --- |
| AWS | EC2 | AMI-ID | ami-0263e4deb427da90e |
| AWS | EC2 | Instance Type | c5.large |
| System | Processor | CPU(s) | 2 |
| System | Processor | Thread(s) per core | 2 |
| System | Processor | Core(s) per socket | 1 |
| System | Processor | Socket(s) | 1 |
| System | Processor | Model name | Intel(R) Xeon(R) Platinum 8124M CPU @ 3.00GHz |
| System | Memory | BIOS | 64 KiB |
| System | Memory | System Memory | 4 GiB |
| System | Storage | Block Device: nvme0n1 | 8G |
| Operating System | Distribution | Release | Ubuntu 18.04.6 LTS |
| Operating System | Distribution | Kernel | Linux ip-10-0-1-79 5.4.0-1094-aws #102~18.04.1-Ubuntu SMP Tue Jan 10 21:07:03 UTC 2023 x86_64 x86_64 x86_64 GNU/Linux |


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

|  Scenario Name | Heap Size | Concurrent Users | Message Size (Bytes) | Back-end Service Delay (ms) | Error % | Throughput (Requests/sec) | Average Response Time (ms) | Standard Deviation of Response Time (ms) | 99th Percentile of Response Time (ms) | WSO2 Micro Integrator 4.2.0-beta-performance GC Throughput (%) | Average WSO2 Micro Integrator 4.2.0-beta-performance Memory Footprint After Full GC (M) |
|---|---:|---:|---:|---:|---:|---:|---:|---:|---:|---:|---:|
|  CBR Proxy | 2G | 100 | 500 | 0 | 0 | 5602.69 | 17.78 | 10.9 | 59 | N/A | N/A |
|  CBR Proxy | 2G | 100 | 1000 | 0 | 0 | 4700.14 | 21.2 | 11.87 | 64 | N/A | N/A |
|  CBR Proxy | 2G | 100 | 10000 | 0 | 0 | 1159.28 | 86.15 | 52.39 | 259 | N/A | N/A |
|  CBR Proxy | 2G | 100 | 100000 | 0 | 0 | 85.02 | 1174.15 | 309.7 | 2175 | N/A | N/A |
|  CBR Proxy | 2G | 200 | 500 | 0 | 0 | 5663.64 | 35.23 | 18.65 | 100 | N/A | N/A |
|  CBR Proxy | 2G | 200 | 1000 | 0 | 0 | 4753.09 | 42 | 21.98 | 118 | N/A | N/A |
|  CBR Proxy | 2G | 200 | 10000 | 0 | 0 | 1157.32 | 172.64 | 88.24 | 443 | N/A | N/A |
|  CBR Proxy | 2G | 200 | 100000 | 0 | 0 | 71.84 | 2763.26 | 741.76 | 4703 | N/A | N/A |
|  CBR Proxy | 2G | 500 | 500 | 0 | 0 | 5587.25 | 89.33 | 42.75 | 233 | N/A | N/A |
|  CBR Proxy | 2G | 500 | 1000 | 0 | 0 | 4732.5 | 105.46 | 48.42 | 265 | N/A | N/A |
|  CBR Proxy | 2G | 500 | 10000 | 0 | 0 | 1145.11 | 436.35 | 185.79 | 939 | N/A | N/A |
|  CBR Proxy | 2G | 500 | 100000 | 0 | 0 | 54.7 | 8863.62 | 2026.44 | 13823 | N/A | N/A |
|  CBR Proxy | 2G | 1000 | 500 | 0 | 0 | 5440.77 | 183.44 | 71.77 | 391 | N/A | N/A |
|  CBR Proxy | 2G | 1000 | 1000 | 0 | 0 | 4688.19 | 212.82 | 83.59 | 459 | N/A | N/A |
|  CBR Proxy | 2G | 1000 | 10000 | 0 | 0 | 799.74 | 1246.19 | 491.96 | 2559 | N/A | N/A |
|  CBR Proxy | 2G | 1000 | 100000 | 0 | 0 | 37.81 | 24522.49 | 5845.16 | 31487 | N/A | N/A |
|  Direct API | 2G | 100 | 500 | 0 | 0 | 8257.89 | 12.05 | 11.06 | 61 | N/A | N/A |
|  Direct API | 2G | 100 | 1000 | 0 | 0 | 8339.04 | 11.93 | 11.05 | 60 | N/A | N/A |
|  Direct API | 2G | 100 | 10000 | 0 | 0 | 6096.78 | 16.33 | 13.51 | 74 | N/A | N/A |
|  Direct API | 2G | 100 | 100000 | 0 | 0 | 1841.53 | 54.19 | 8.87 | 78 | N/A | N/A |
|  Direct API | 2G | 200 | 500 | 0 | 0 | 8466.97 | 23.55 | 16.84 | 90 | N/A | N/A |
|  Direct API | 2G | 200 | 1000 | 0 | 0 | 8432.03 | 23.65 | 16.6 | 89 | 99.47 |  |
|  Direct API | 2G | 200 | 10000 | 0 | 0 | 5959.8 | 33.48 | 20.25 | 106 | 99.64 |  |
|  Direct API | 2G | 200 | 100000 | 0 | 0 | 1688.32 | 118.28 | 18.71 | 169 | 99.75 |  |
|  Direct API | 2G | 500 | 500 | 0 | 0 | 8110.75 | 61.53 | 31.47 | 165 | 99.65 |  |
|  Direct API | 2G | 500 | 1000 | 0 | 0 | 8138.61 | 61.3 | 31.84 | 169 | 99.59 |  |
|  Direct API | 2G | 500 | 10000 | 0 | 0 | 6089.91 | 81.95 | 39.14 | 204 | 99.59 |  |
|  Direct API | 2G | 500 | 100000 | 0 | 0 | 1547.31 | 323.17 | 46.98 | 445 | 99.63 |  |
|  Direct API | 2G | 1000 | 500 | 0 | 0 | 8050.54 | 123.7 | 51.89 | 279 | 99.55 |  |
|  Direct API | 2G | 1000 | 1000 | 0 | 0 | 7819.1 | 127.26 | 53.01 | 287 | 99.49 |  |
|  Direct API | 2G | 1000 | 10000 | 0 | 0 | 5988.24 | 166.61 | 66.81 | 351 | 99.47 |  |
|  Direct API | 2G | 1000 | 100000 | 0 | 0 | 1468.15 | 678.79 | 93.6 | 899 | 99.5 |  |
|  Direct Proxy | 2G | 100 | 500 | 0 | 0 | 8904.42 | 11.17 | 10.92 | 61 | 99.48 |  |
|  Direct Proxy | 2G | 100 | 1000 | 0 | 0 | 8662.46 | 11.48 | 11.3 | 63 | N/A | N/A |
|  Direct Proxy | 2G | 100 | 10000 | 0 | 0 | 6104.45 | 16.31 | 10.96 | 59 | N/A | N/A |
|  Direct Proxy | 2G | 100 | 100000 | 0 | 0 | 1815.71 | 54.96 | 8.92 | 79 | N/A | N/A |
|  Direct Proxy | 2G | 200 | 500 | 0 | 0 | 8629.99 | 23.1 | 16.75 | 89 | N/A | N/A |
|  Direct Proxy | 2G | 200 | 1000 | 0 | 0 | 8660.41 | 23.02 | 17.18 | 93 | N/A | N/A |
|  Direct Proxy | 2G | 200 | 10000 | 0 | 0 | 6142.54 | 32.48 | 18.78 | 97 | N/A | N/A |
|  Direct Proxy | 2G | 200 | 100000 | 0 | 0 | 1724.55 | 115.81 | 18.14 | 165 | N/A | N/A |
|  Direct Proxy | 2G | 500 | 500 | 0 | 0 | 8351.93 | 59.75 | 31.3 | 167 | N/A | N/A |
|  Direct Proxy | 2G | 500 | 1000 | 0 | 0 | 8101.39 | 61.6 | 29.7 | 157 | N/A | N/A |
|  Direct Proxy | 2G | 500 | 10000 | 0 | 0 | 6165.13 | 80.96 | 38.48 | 201 | N/A | N/A |
|  Direct Proxy | 2G | 500 | 100000 | 0 | 0 | 1535.12 | 325.76 | 46.71 | 447 | N/A | N/A |
|  Direct Proxy | 2G | 1000 | 500 | 0 | 0 | 8156.27 | 122.02 | 51.85 | 281 | N/A | N/A |
|  Direct Proxy | 2G | 1000 | 1000 | 0 | 0 | 8038.81 | 123.88 | 51.69 | 279 | N/A | N/A |
|  Direct Proxy | 2G | 1000 | 10000 | 0 | 0 | 6267.89 | 158.88 | 65.48 | 355 | N/A | N/A |
|  Direct Proxy | 2G | 1000 | 100000 | 0 | 0 | 1488.36 | 669.33 | 95.01 | 891 | N/A | N/A |
|  XSLT Proxy | 2G | 100 | 500 | 0 | 0 | 2572.73 | 38.79 | 22.43 | 118 | N/A | N/A |
|  XSLT Proxy | 2G | 100 | 1000 | 0 | 0 | 1884.87 | 52.97 | 181.58 | 170 | N/A | N/A |
|  XSLT Proxy | 2G | 100 | 10000 | 0 | 0 | 307.7 | 324.85 | 176.25 | 819 | N/A | N/A |
|  XSLT Proxy | 2G | 100 | 100000 | 0 | 0 | 22.92 | 4303.55 | 496.23 | 5471 | N/A | N/A |
|  XSLT Proxy | 2G | 200 | 500 | 0 | 0 | 2625.04 | 76.07 | 39.16 | 207 | N/A | N/A |
|  XSLT Proxy | 2G | 200 | 1000 | 0 | 0 | 1895.57 | 105.19 | 55.73 | 287 | N/A | N/A |
|  XSLT Proxy | 2G | 200 | 10000 | 0 | 0 | 290.58 | 687.41 | 319.62 | 1567 | N/A | N/A |
|  XSLT Proxy | 2G | 200 | 100000 | 0 | 0 | 21.52 | 9056.2 | 1217.94 | 11839 | N/A | N/A |
|  XSLT Proxy | 2G | 500 | 500 | 0 | 0 | 2588.16 | 193 | 82.32 | 451 | N/A | N/A |
|  XSLT Proxy | 2G | 500 | 1000 | 0 | 0 | 1861.2 | 268.53 | 110.43 | 603 | N/A | N/A |
|  XSLT Proxy | 2G | 500 | 10000 | 0 | 0 | 237.64 | 2084.1 | 765.39 | 4319 | N/A | N/A |
|  XSLT Proxy | 2G | 500 | 100000 | 0 | 0 | 19.47 | 24059.02 | 3525.32 | 32127 | N/A | N/A |
|  XSLT Proxy | 2G | 1000 | 500 | 0 | 0 | 2010.2 | 495.92 | 202.09 | 1143 | N/A | N/A |
|  XSLT Proxy | 2G | 1000 | 1000 | 0 | 0 | 1485.71 | 672.37 | 263 | 1471 | N/A | N/A |
|  XSLT Proxy | 2G | 1000 | 10000 | 0 | 0 | 225.51 | 4377.67 | 1384.42 | 8095 | N/A | N/A |
|  XSLT Proxy | 2G | 1000 | 100000 | 0 | 0 | 15.52 | 54651.93 | 5176.38 | 66047 | N/A | N/A |
