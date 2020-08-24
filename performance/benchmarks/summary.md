# WSO2 Micro Integrator 1.2.0 Performance Test Results

During each release, we execute various automated performance test scenarios and publish the results.

| Test Scenarios | Description |
| --- | --- |
| Iterate and Aggregate Proxy | Iterate over a payload and call backend and aggregate the response |

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
| Concurrent Users | The number of users accessing the application at the same time. | 100, 200, 500 |
| Message Size (Bytes) | The request payload size in Bytes. | 500 |
| Back-end Delay (ms) | The delay added by the Back-end service. | 0 |

The duration of each test is **900 seconds**. The warm-up period is **300 seconds**.
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
| System | Memory | System memory | 3785216 KiB |
| System | Storage | Block Device: nvme0n1 | 8G |
| Operating System | Distribution | Release | Ubuntu 18.04.5 LTS |
| Operating System | Distribution | Kernel | Linux ip-10-0-1-191 5.3.0-1032-aws #34~18.04.2-Ubuntu SMP Fri Jul 24 10:06:28 UTC 2020 x86_64 x86_64 x86_64 GNU/Linux |
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
| Operating System | Distribution | Kernel | Linux ip-10-0-1-127 5.3.0-1032-aws #34~18.04.2-Ubuntu SMP Fri Jul 24 10:06:28 UTC 2020 x86_64 x86_64 x86_64 GNU/Linux |


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
|  Iterate and Aggregate Proxy | 2G | 100 | N/A | 0 | 0 | 913.26 | 109.42 | 62.42 | 265 | 92.37 | 221.233 |
|  Iterate and Aggregate Proxy | 2G | 100 | N/A | 0 | 0 | 501.27 | 199.48 | 91.73 | 437 | N/A | N/A |
|  Iterate and Aggregate Proxy | 2G | 100 | N/A | 0 | 0 | 241.41 | 414.42 | 177.6 | 1095 | N/A | N/A |
|  Iterate and Aggregate Proxy | 2G | 100 | N/A | 0 | 0 | 67.99 | 1469.63 | 581.88 | 2751 | N/A | N/A |
|  Iterate and Aggregate Proxy | 2G | 100 | N/A | 0 | 0 | 23.94 | 4160.55 | 1174.42 | 7199 | N/A | N/A |
|  Iterate and Aggregate Proxy | 2G | 200 | N/A | 0 | 0 | 864.88 | 231.26 | 103.42 | 509 | N/A | N/A |
|  Iterate and Aggregate Proxy | 2G | 200 | N/A | 0 | 0 | 360.66 | 554.61 | 684.11 | 4959 | N/A | N/A |
|  Iterate and Aggregate Proxy | 2G | 500 | N/A | 0 | 0 | 690.15 | 724.2 | 332.47 | 1799 | 72.22 | 369.92 |
|  Iterate and Aggregate Proxy | 2G | 500 | N/A | 0 | 0 | 305.74 | 1633.3 | 624.76 | 3087 | N/A | N/A |
|  Iterate and Aggregate Proxy | 2G | 500 | N/A | 0 | 0 | 116.87 | 4259.47 | 1216.36 | 7679 | N/A | N/A |
|  Iterate and Aggregate Proxy | 2G | 500 | N/A | 0 | 0 | 22.04 | 22093.27 | 4601.36 | 34815 | N/A | N/A |
|  Iterate and Aggregate Proxy | 2G | 500 | N/A | 0 | 0 | 8.22 | 56731.17 | 7925.57 | 74751 | N/A | N/A |
