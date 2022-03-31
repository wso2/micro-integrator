# WSO2 Micro Integrator 4.1.0 Performance Test Results

During each release, we execute various automated performance test scenarios and publish the results.

| Test Scenarios | Description |
| --- | --- |
| Direct API | Passthrough API service |

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
| Operating System | Distribution | Kernel | Linux ip-10-0-1-223 5.4.0-1068-aws #72~18.04.1-Ubuntu SMP Thu Mar 3 08:49:49 UTC 2022 x86_64 x86_64 x86_64 GNU/Linux |


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
|  Direct API | 2G | 100 | 500 | 0 | 0 | 4137.34 | 24.07 | 37.69 | 109 | 91.95 | 264.718 |
|  Direct API | 2G | 100 | 1000 | 0 | 0 | 4145 | 24.03 | 38.74 | 107 | N/A | N/A |
|  Direct API | 2G | 100 | 10000 | 0 | 0 | 3548.03 | 28.07 | 37.07 | 110 | N/A | N/A |
|  Direct API | 2G | 100 | 100000 | 0 | 0 | 1586.99 | 62.78 | 40.67 | 166 | N/A | N/A |
|  Direct API | 2G | 200 | 500 | 0 | 0 | 4234.8 | 47.11 | 61.84 | 157 | N/A | N/A |
|  Direct API | 2G | 200 | 1000 | 0 | 0 | 4189.87 | 47.61 | 63.14 | 160 | N/A | N/A |
|  Direct API | 2G | 200 | 10000 | 0 | 0 | 3680.5 | 54.19 | 37.96 | 166 | N/A | N/A |
|  Direct API | 2G | 200 | 100000 | 0 | 0 | 1577.76 | 126.47 | 65.71 | 289 | N/A | N/A |
|  Direct API | 2G | 500 | 500 | 0 | 0 | 4200.13 | 118.87 | 103.05 | 299 | N/A | N/A |
|  Direct API | 2G | 500 | 1000 | 0 | 0 | 4184.07 | 119.36 | 101.75 | 315 | N/A | N/A |
|  Direct API | 2G | 500 | 10000 | 0 | 0 | 3448.15 | 144.78 | 149.6 | 335 | N/A | N/A |
|  Direct API | 2G | 500 | 100000 | 0 | 0 | 1241.61 | 402.3 | 144.56 | 735 | N/A | N/A |
|  Direct API | 2G | 1000 | 500 | 0 | 0 | 344.05 | 2876.34 | 2790.77 | 11967 | N/A | N/A |
|  Direct API | 2G | 1000 | 1000 | 0 | 0 | 99.74 | 9619.2 | 8171.72 | 36863 | N/A | N/A |
|  Direct API | 2G | 1000 | 10000 | 0 | 0 | 256.21 | 3856.33 | 3159.27 | 15295 | N/A | N/A |
|  Direct API | 2G | 1000 | 100000 | 0 | 0 | 1205.45 | 827.74 | 254.05 | 1447 | N/A | N/A |
