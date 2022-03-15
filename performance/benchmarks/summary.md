# WSO2 Micro Integrator 4.1.0-beta Performance Test Results

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

1. **Throughput**: The number of requests that the WSO2 Micro Integrator 4.1.0-beta processes during a specific time interval (e.g. per second).
2. **Response Time**: The end-to-end latency for an operation of invoking a service in WSO2 Micro Integrator 4.1.0-beta . The complete distribution of response times was recorded.

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


System information for WSO2 Micro Integrator 4.1.0-beta in 1st AWS CloudFormation stack.

| Class | Subclass | Description | Value |
| --- | --- | --- | --- |
| AWS | EC2 | AMI-ID | ami-0d73480446600f555 |
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
| Operating System | Distribution | Kernel | Linux ip-10-0-1-203 5.4.0-1068-aws #72~18.04.1-Ubuntu SMP Thu Mar 3 08:49:49 UTC 2022 x86_64 x86_64 x86_64 GNU/Linux |


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

|  Scenario Name | Heap Size | Concurrent Users | Message Size (Bytes) | Back-end Service Delay (ms) | Error % | Throughput (Requests/sec) | Average Response Time (ms) | Standard Deviation of Response Time (ms) | 99th Percentile of Response Time (ms) | WSO2 Micro Integrator 4.1.0-beta GC Throughput (%) | Average WSO2 Micro Integrator 4.1.0-beta Memory Footprint After Full GC (M) |
|---|---:|---:|---:|---:|---:|---:|---:|---:|---:|---:|---:|
|  CBR Proxy | 2G | 100 | 500 | 0 | 0 | 644.89 | 154.99 | 564.87 | 2911 | N/A | N/A |
|  CBR Proxy | 2G | 100 | 1000 | 0 | 0 | 707.41 | 141.27 | 522.48 | 2895 | N/A | N/A |
|  CBR Proxy | 2G | 100 | 10000 | 0 | 0 | 890.43 | 112.17 | 100.24 | 333 | N/A | N/A |
|  CBR Proxy | 2G | 100 | 100000 | 0 | 0 | 100.21 | 996.03 | 263.37 | 1999 | N/A | N/A |
|  CBR Proxy | 2G | 200 | 500 | 0 | 0 | 384.94 | 519.46 | 1057.06 | 3215 | N/A | N/A |
|  CBR Proxy | 2G | 200 | 1000 | 0 | 0 | 507.72 | 392.7 | 890.73 | 3055 | N/A | N/A |
|  CBR Proxy | 2G | 200 | 10000 | 0 | 0 | 870.89 | 229.41 | 158.8 | 571 | N/A | N/A |
|  CBR Proxy | 2G | 200 | 100000 | 0 | 0 | 78.52 | 2535.38 | 946.44 | 4831 | N/A | N/A |
|  CBR Proxy | 2G | 500 | 500 | 0 | 0 | 303.62 | 1646.28 | 1690.24 | 6719 | N/A | N/A |
|  CBR Proxy | 2G | 500 | 1000 | 0 | 0 | 333.23 | 1499.92 | 1760.64 | 6623 | N/A | N/A |
|  CBR Proxy | 2G | 500 | 10000 | 0 | 0 | 781.3 | 639.62 | 280.87 | 1271 | N/A | N/A |
|  CBR Proxy | 2G | 500 | 100000 | 0 | 0 | 38.83 | 12759.27 | 4302.35 | 20095 | N/A | N/A |
|  CBR Proxy | 2G | 1000 | 500 | 0 | 0 | 148.69 | 6655.64 | 5056.79 | 24831 | N/A | N/A |
|  CBR Proxy | 2G | 1000 | 1000 | 0 | 0 | 206.36 | 4793.69 | 3625.94 | 16511 | N/A | N/A |
|  CBR Proxy | 2G | 1000 | 10000 | 0 | 0 | 517.62 | 1925.75 | 1029.47 | 4287 | N/A | N/A |
|  CBR Proxy | 2G | 1000 | 100000 | 0 | 100 | 17184.01 | 49.3 | 546.5 | 361 | N/A | N/A |
|  Direct Proxy | 2G | 100 | 500 | 0 | 0 | 4103.48 | 24.28 | 40.75 | 111 | 91.89 | 251.49 |
|  Direct Proxy | 2G | 100 | 1000 | 0 | 0 | 4094.93 | 24.33 | 38.35 | 111 | N/A | N/A |
|  Direct Proxy | 2G | 100 | 10000 | 0 | 0 | 3610.4 | 27.59 | 24.99 | 112 | N/A | N/A |
|  Direct Proxy | 2G | 100 | 100000 | 0 | 0 | 1630.22 | 61.12 | 41.11 | 156 | N/A | N/A |
|  Direct Proxy | 2G | 200 | 500 | 0 | 0 | 4220.96 | 47.27 | 63.06 | 158 | N/A | N/A |
|  Direct Proxy | 2G | 200 | 1000 | 0 | 0 | 4233.04 | 47.13 | 53.15 | 159 | N/A | N/A |
|  Direct Proxy | 2G | 200 | 10000 | 0 | 0 | 3703.99 | 53.86 | 40.41 | 163 | N/A | N/A |
|  Direct Proxy | 2G | 200 | 100000 | 0 | 0 | 1612.83 | 123.71 | 61.51 | 269 | N/A | N/A |
|  Direct Proxy | 2G | 500 | 500 | 0 | 0 | 4243.19 | 117.67 | 86.59 | 301 | N/A | N/A |
|  Direct Proxy | 2G | 500 | 1000 | 0 | 0 | 4172.04 | 119.7 | 101.86 | 305 | N/A | N/A |
|  Direct Proxy | 2G | 500 | 10000 | 0 | 0 | 573.27 | 871.87 | 1340.71 | 3583 | N/A | N/A |
|  Direct Proxy | 2G | 500 | 100000 | 0 | 0 | 1218.8 | 409.98 | 147.44 | 739 | N/A | N/A |
|  Direct Proxy | 2G | 1000 | 500 | 0 | 0 | 104.56 | 9428.4 | 7941.85 | 37631 | N/A | N/A |
|  Direct Proxy | 2G | 1000 | 1000 | 0 | 0.01 | 91.36 | 8255.84 | 7149.11 | 34047 | N/A | N/A |
|  Direct Proxy | 2G | 1000 | 10000 | 0 | 0 | 253.79 | 3921.56 | 3093.65 | 12991 | N/A | N/A |
|  Direct Proxy | 2G | 1000 | 100000 | 0 | 0 | 1209.03 | 825.53 | 219.54 | 1359 | N/A | N/A |
