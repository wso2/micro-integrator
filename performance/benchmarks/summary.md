# WSO2 Micro Integrator 4.2.0-beta Performance Test Results

During each release, we execute various automated performance test scenarios and publish the results.

| Test Scenarios | Description |
| --- | --- |
| Direct Proxy | Passthrough proxy service |

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
| Operating System | Distribution | Kernel | Linux ip-10-0-1-8 5.4.0-1094-aws #102~18.04.1-Ubuntu SMP Tue Jan 10 21:07:03 UTC 2023 x86_64 x86_64 x86_64 GNU/Linux |


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
|  Direct Proxy | 2G | 100 | 500 | 0 | 0 | 4666.9 | 21.35 | 11.84 | 67 | 99.4 |  |
|  Direct Proxy | 2G | 100 | 1000 | 0 | 0 | 4660.83 | 21.38 | 12.16 | 70 | N/A | N/A |
|  Direct Proxy | 2G | 100 | 10000 | 0 | 0 | 3839.44 | 25.96 | 14 | 80 | N/A | N/A |
|  Direct Proxy | 2G | 100 | 100000 | 0 | 0 | 1535.06 | 65.01 | 19.45 | 110 | N/A | N/A |
|  Direct Proxy | 2G | 200 | 500 | 0 | 0 | 4758.99 | 41.93 | 20.41 | 112 | N/A | N/A |
|  Direct Proxy | 2G | 200 | 1000 | 0 | 0 | 4782.27 | 41.73 | 20.43 | 112 | N/A | N/A |
|  Direct Proxy | 2G | 200 | 10000 | 0 | 0 | 3726.28 | 53.57 | 26.61 | 142 | N/A | N/A |
|  Direct Proxy | 2G | 200 | 100000 | 0 | 0 | 1432.51 | 139.43 | 33.1 | 223 | N/A | N/A |
|  Direct Proxy | 2G | 500 | 500 | 0 | 0 | 4785.1 | 104.33 | 45.85 | 248 | N/A | N/A |
|  Direct Proxy | 2G | 500 | 1000 | 0 | 0 | 4770.2 | 104.67 | 45.44 | 245 | N/A | N/A |
|  Direct Proxy | 2G | 500 | 10000 | 0 | 0 | 3924.91 | 127.24 | 53.76 | 287 | N/A | N/A |
|  Direct Proxy | 2G | 500 | 100000 | 0 | 0 | 1353.42 | 369.44 | 66.35 | 543 | N/A | N/A |
|  Direct Proxy | 2G | 1000 | 500 | 0 | 0 | 4784.44 | 208.66 | 79.35 | 435 | N/A | N/A |
|  Direct Proxy | 2G | 1000 | 1000 | 0 | 0 | 4720.55 | 211.54 | 79.99 | 443 | N/A | N/A |
|  Direct Proxy | 2G | 1000 | 10000 | 0 | 0 | 3892.33 | 256.41 | 95.13 | 531 | N/A | N/A |
|  Direct Proxy | 2G | 1000 | 100000 | 0 | 0 | 1285.56 | 776.41 | 114.57 | 1071 | N/A | N/A |
