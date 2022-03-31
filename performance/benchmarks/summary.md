# WSO2 Micro Integrator 4.1.0 Performance Test Results

During each release, we execute various automated performance test scenarios and publish the results.

| Test Scenarios | Description |
| --- | --- |
| CBR Proxy | Routing the message based on the content of the message body |

Our test client is [Apache JMeter](https://jmeter.apache.org/index.html). We test each scenario for a fixed duration of
time. We split the test results into warmup and measurement parts and use the measurement part to compute the
performance metrics.

Test scenarios use a [Netty](https://netty.io/) based back-end service which echoes back any request
posted to it after a specified period of time.

We run the performance tests under different numbers of concurrent users, message sizes (payloads) and back-end service
delays.

The main performance metrics:

1. **Throughput**: The number of requests that the WSO2 Micro Integrator 4.1.0 processes during a specific time interval (e.g. per second).
2. **Response Time**: The end-to-end latency for an operation of invoking a service in WSO2 Micro Integrator 4.1.0 . The complete distribution of response times was recorded.

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


System information for WSO2 Micro Integrator 4.1.0 in 1st AWS CloudFormation stack.

| Class | Subclass | Description | Value |
| --- | --- | --- | --- |
| AWS | EC2 | AMI-ID | ami-0d73480446600f555 |
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
| Operating System | Distribution | Kernel | Linux ip-10-0-1-188 5.4.0-1068-aws #72~18.04.1-Ubuntu SMP Thu Mar 3 08:49:49 UTC 2022 x86_64 x86_64 x86_64 GNU/Linux |


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

|  Scenario Name | Heap Size | Concurrent Users | Message Size (Bytes) | Back-end Service Delay (ms) | Error % | Throughput (Requests/sec) | Average Response Time (ms) | Standard Deviation of Response Time (ms) | 99th Percentile of Response Time (ms) | WSO2 Micro Integrator 4.1.0 GC Throughput (%) | Average WSO2 Micro Integrator 4.1.0 Memory Footprint After Full GC (M) |
|---|---:|---:|---:|---:|---:|---:|---:|---:|---:|---:|---:|
|  CBR Proxy | 2G | 100 | 500 | 0 | 0 | 3376.45 | 29.52 | 28.12 | 116 | 93.42 | 174.236 |
|  CBR Proxy | 2G | 100 | 1000 | 0 | 0 | 3048.04 | 32.71 | 38.87 | 119 | N/A | N/A |
|  CBR Proxy | 2G | 100 | 10000 | 0 | 0 | 1088.06 | 91.76 | 57.09 | 273 | N/A | N/A |
|  CBR Proxy | 2G | 100 | 100000 | 0 | 0 | 114.69 | 870.25 | 225.05 | 1567 | N/A | N/A |
|  CBR Proxy | 2G | 200 | 500 | 0 | 0 | 3408.93 | 58.54 | 47.18 | 183 | N/A | N/A |
|  CBR Proxy | 2G | 200 | 1000 | 0 | 0 | 3063.17 | 65.15 | 59.37 | 200 | N/A | N/A |
|  CBR Proxy | 2G | 200 | 10000 | 0 | 0 | 1058.92 | 188.75 | 93.81 | 457 | N/A | N/A |
|  CBR Proxy | 2G | 200 | 100000 | 0 | 0 | 85.06 | 2338.49 | 865.14 | 4511 | N/A | N/A |
|  CBR Proxy | 2G | 500 | 500 | 0 | 0 | 3373.87 | 147.95 | 79.03 | 369 | N/A | N/A |
|  CBR Proxy | 2G | 500 | 1000 | 0 | 0 | 3055.24 | 163.5 | 89.35 | 413 | N/A | N/A |
|  CBR Proxy | 2G | 500 | 10000 | 0 | 0 | 967.7 | 516.21 | 192.58 | 1079 | N/A | N/A |
|  CBR Proxy | 2G | 500 | 100000 | 0 | 0 | 41.51 | 11857.4 | 3804.75 | 19199 | N/A | N/A |
|  CBR Proxy | 2G | 1000 | 500 | 0 | 0 | 2821.24 | 354.05 | 157.66 | 819 | N/A | N/A |
|  CBR Proxy | 2G | 1000 | 1000 | 0 | 0 | 2925.4 | 341.39 | 170.28 | 835 | N/A | N/A |
|  CBR Proxy | 2G | 1000 | 10000 | 0 | 0 | 767.73 | 1295.15 | 520.99 | 2959 | N/A | N/A |
|  CBR Proxy | 2G | 1000 | 100000 | 0 | 100 | 4.56 | 119136.7 | 12346.79 | 179199 | N/A | N/A |
