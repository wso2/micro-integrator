# WSO2 Micro Integrator 4.2.0-m1 Performance Test Results

During each release, we execute various automated performance test scenarios and publish the results.

| Test Scenarios | Description |
| --- | --- |
| XSLT Proxy | Having XSLT transformations in request and response paths |

Our test client is [Apache JMeter](https://jmeter.apache.org/index.html). We test each scenario for a fixed duration of
time. We split the test results into warmup and measurement parts and use the measurement part to compute the
performance metrics.

Test scenarios use a [Netty](https://netty.io/) based back-end service which echoes back any request
posted to it after a specified period of time.

We run the performance tests under different numbers of concurrent users, message sizes (payloads) and back-end service
delays.

The main performance metrics:

1. **Throughput**: The number of requests that the WSO2 Micro Integrator 4.2.0-m1 processes during a specific time interval (e.g. per second).
2. **Response Time**: The end-to-end latency for an operation of invoking a service in WSO2 Micro Integrator 4.2.0-m1 . The complete distribution of response times was recorded.

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


System information for WSO2 Micro Integrator 4.2.0-m1 in 1st AWS CloudFormation stack.

| Class | Subclass | Description | Value |
| --- | --- | --- | --- |
| AWS | EC2 | AMI-ID | ami-0ae87df03b4940655 |
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
| Operating System | Distribution | Kernel | Linux ip-10-0-1-242 5.4.0-1096-aws #104~18.04.1-Ubuntu SMP Wed Jan 25 19:58:53 UTC 2023 x86_64 x86_64 x86_64 GNU/Linux |


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

|  Scenario Name | Heap Size | Concurrent Users | Message Size (Bytes) | Back-end Service Delay (ms) | Error % | Throughput (Requests/sec) | Average Response Time (ms) | Standard Deviation of Response Time (ms) | 99th Percentile of Response Time (ms) | WSO2 Micro Integrator 4.2.0-m1 GC Throughput (%) | Average WSO2 Micro Integrator 4.2.0-m1 Memory Footprint After Full GC (M) |
|---|---:|---:|---:|---:|---:|---:|---:|---:|---:|---:|---:|
|  XSLT Proxy | 2G | 100 | 500 | 0 | 0 | 1243.19 | 60.26 | 194.62 | 208 | 97.35 |  |
|  XSLT Proxy | 2G | 100 | 1000 | 0 | 0 | 1025.8 | 65.86 | 203.58 | 220 | N/A | N/A |
|  XSLT Proxy | 2G | 100 | 10000 | 0 | 0 | 316.29 | 316.04 | 173 | 807 | N/A | N/A |
|  XSLT Proxy | 2G | 100 | 100000 | 0 | 0 | 25.12 | 3935.8 | 454.99 | 4991 | N/A | N/A |
|  XSLT Proxy | 2G | 200 | 500 | 0 | 0 | 1905.53 | 104.84 | 61.33 | 309 | N/A | N/A |
|  XSLT Proxy | 2G | 200 | 1000 | 0 | 0 | 1537.67 | 129.94 | 210.75 | 365 | N/A | N/A |
|  XSLT Proxy | 2G | 200 | 10000 | 0 | 0 | 295.09 | 676.96 | 274.67 | 1431 | N/A | N/A |
|  XSLT Proxy | 2G | 200 | 100000 | 0 | 0 | 23.4 | 8388.57 | 918.6 | 10623 | N/A | N/A |
|  XSLT Proxy | 2G | 500 | 500 | 0 | 0 | 1827.17 | 273.41 | 120.25 | 635 | N/A | N/A |
|  XSLT Proxy | 2G | 500 | 1000 | 0 | 0 | 1454.02 | 343.68 | 148.98 | 783 | N/A | N/A |
|  XSLT Proxy | 2G | 500 | 10000 | 0 | 0 | 237.26 | 2093.77 | 552.55 | 3631 | N/A | N/A |
|  XSLT Proxy | 2G | 500 | 100000 | 0 | 0 | 21.11 | 22301 | 3098.57 | 29823 | N/A | N/A |
|  XSLT Proxy | 2G | 1000 | 500 | 0 | 0 | 1553.99 | 642.64 | 266.61 | 1639 | N/A | N/A |
|  XSLT Proxy | 2G | 1000 | 1000 | 0 | 0 | 1190.83 | 837.36 | 343.73 | 1967 | N/A | N/A |
|  XSLT Proxy | 2G | 1000 | 10000 | 0 | 0 | 224.12 | 4405.02 | 861.28 | 6591 | N/A | N/A |
|  XSLT Proxy | 2G | 1000 | 100000 | 0 | 0 | 15.42 | 54614.88 | 4588.43 | 65279 | N/A | N/A |
