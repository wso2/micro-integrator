# WSO2 Micro Integrator 4.1.0-beta Performance Test Results

During each release, we execute various automated performance test scenarios and publish the results.

| Test Scenarios | Description |
| --- | --- |
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
| Operating System | Distribution | Kernel | Linux ip-10-0-1-57 5.4.0-1068-aws #72~18.04.1-Ubuntu SMP Thu Mar 3 08:49:49 UTC 2022 x86_64 x86_64 x86_64 GNU/Linux |


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
|  XSLT Proxy | 2G | 100 | 500 | 0 | 0 | 1872.74 | 53.29 | 184.45 | 183 | 93.71 | 193.681 |
|  XSLT Proxy | 2G | 100 | 1000 | 0 | 0 | 1205.05 | 61.79 | 388.06 | 195 | N/A | N/A |
|  XSLT Proxy | 2G | 100 | 10000 | 0 | 0 | 355.72 | 280.92 | 156.86 | 739 | N/A | N/A |
|  XSLT Proxy | 2G | 100 | 100000 | 0 | 0 | 32.07 | 3099.61 | 570.82 | 4639 | N/A | N/A |
|  XSLT Proxy | 2G | 200 | 500 | 0 | 0 | 1985.89 | 100.35 | 66.42 | 285 | N/A | N/A |
|  XSLT Proxy | 2G | 200 | 1000 | 0 | 0 | 1611.88 | 123.91 | 75.85 | 347 | N/A | N/A |
|  XSLT Proxy | 2G | 200 | 10000 | 0 | 0 | 348.6 | 573.29 | 258.25 | 1303 | N/A | N/A |
|  XSLT Proxy | 2G | 200 | 100000 | 0 | 0 | 27.62 | 7124.14 | 1352.68 | 9919 | N/A | N/A |
|  XSLT Proxy | 2G | 500 | 500 | 0 | 0 | 1924.12 | 259.79 | 120.92 | 599 | N/A | N/A |
|  XSLT Proxy | 2G | 500 | 1000 | 0 | 0 | 1587.99 | 314.72 | 138.61 | 739 | N/A | N/A |
|  XSLT Proxy | 2G | 500 | 10000 | 0 | 0 | 309.54 | 1608.61 | 464.37 | 3007 | N/A | N/A |
|  XSLT Proxy | 2G | 500 | 100000 | 0 | 0 | 15.25 | 30712.31 | 5807.04 | 45823 | N/A | N/A |
|  XSLT Proxy | 2G | 1000 | 500 | 0 | 0 | 1633.19 | 611.79 | 230.03 | 1311 | N/A | N/A |
|  XSLT Proxy | 2G | 1000 | 1000 | 0 | 0 | 1426.46 | 699.67 | 275.47 | 1679 | N/A | N/A |
|  XSLT Proxy | 2G | 1000 | 10000 | 0 | 0 | 265.74 | 3720.37 | 942.78 | 5983 | N/A | N/A |
|  XSLT Proxy | 2G | 1000 | 100000 | 0 | 97.52 | 4.81 | 125059.69 | 18168.58 | 185343 | N/A | N/A |
