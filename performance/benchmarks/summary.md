# WSO2 Micro Integrator 4.10-beta Performance Test Results

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

1. **Throughput**: The number of requests that the WSO2 Micro Integrator 4.10-beta processes during a specific time interval (e.g. per second).
2. **Response Time**: The end-to-end latency for an operation of invoking a service in WSO2 Micro Integrator 4.10-beta . The complete distribution of response times was recorded.

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


System information for WSO2 Micro Integrator 4.10-beta in 1st AWS CloudFormation stack.

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
| Operating System | Distribution | Kernel | Linux ip-10-0-1-183 5.4.0-1068-aws #72~18.04.1-Ubuntu SMP Thu Mar 3 08:49:49 UTC 2022 x86_64 x86_64 x86_64 GNU/Linux |


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

|  Scenario Name | Heap Size | Concurrent Users | Message Size (Bytes) | Back-end Service Delay (ms) | Error % | Throughput (Requests/sec) | Average Response Time (ms) | Standard Deviation of Response Time (ms) | 99th Percentile of Response Time (ms) | WSO2 Micro Integrator 4.10-beta GC Throughput (%) | Average WSO2 Micro Integrator 4.10-beta Memory Footprint After Full GC (M) |
|---|---:|---:|---:|---:|---:|---:|---:|---:|---:|---:|---:|
|  CBR Proxy | 2G | 100 | 500 | 0 | 0 | 3151.34 | 31.63 | 39.62 | 121 | 93.03 | 216.265 |
|  CBR Proxy | 2G | 100 | 1000 | 0 | 0 | 2894.92 | 34.44 | 46.53 | 120 | N/A | N/A |
|  CBR Proxy | 2G | 100 | 10000 | 0 | 0 | 1028.51 | 97.1 | 59.04 | 283 | N/A | N/A |
|  CBR Proxy | 2G | 100 | 100000 | 0 | 0 | 107.13 | 931.9 | 232.16 | 1735 | N/A | N/A |
|  CBR Proxy | 2G | 200 | 500 | 0 | 0 | 3259.36 | 61.23 | 52.62 | 186 | N/A | N/A |
|  CBR Proxy | 2G | 200 | 1000 | 0 | 0 | 2922.28 | 68.29 | 67.08 | 202 | N/A | N/A |
|  CBR Proxy | 2G | 200 | 10000 | 0 | 0 | 1003.04 | 199.26 | 102.09 | 503 | N/A | N/A |
|  CBR Proxy | 2G | 200 | 100000 | 0 | 0 | 80.29 | 2475.64 | 918.5 | 4863 | N/A | N/A |
|  CBR Proxy | 2G | 500 | 500 | 0 | 0 | 3215.16 | 155.33 | 86.83 | 391 | N/A | N/A |
|  CBR Proxy | 2G | 500 | 1000 | 0 | 0 | 2911.43 | 171.54 | 102.74 | 433 | N/A | N/A |
|  CBR Proxy | 2G | 500 | 10000 | 0 | 0 | 917.73 | 544.56 | 193.89 | 1063 | N/A | N/A |
|  CBR Proxy | 2G | 500 | 100000 | 0 | 0 | 40.27 | 12287.95 | 3818.03 | 20095 | N/A | N/A |
|  CBR Proxy | 2G | 1000 | 500 | 0 | 0 | 2699.29 | 369.98 | 179.25 | 851 | N/A | N/A |
|  CBR Proxy | 2G | 1000 | 1000 | 0 | 0 | 2848.74 | 350.98 | 155.88 | 779 | N/A | N/A |
|  CBR Proxy | 2G | 1000 | 10000 | 0 | 0 | 737.95 | 1351.8 | 510.33 | 3023 | N/A | N/A |
|  CBR Proxy | 2G | 1000 | 100000 | 0 | 100 | 4.24 | 120064 | 0 | 120319 | N/A | N/A |
