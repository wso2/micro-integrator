# WSO2 Micro Integrator 4.1.0-beta Performance Test Results

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
| Operating System | Distribution | Kernel | Linux ip-10-0-1-49 5.4.0-1068-aws #72~18.04.1-Ubuntu SMP Thu Mar 3 08:49:49 UTC 2022 x86_64 x86_64 x86_64 GNU/Linux |


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
|  Direct Proxy | 2G | 100 | 500 | 0 | 0 | 4007.71 | 24.85 | 37.06 | 112 | 91.87 | 235.13 |
|  Direct Proxy | 2G | 100 | 1000 | 0 | 0 | 4034.43 | 24.69 | 39.39 | 110 | N/A | N/A |
|  Direct Proxy | 2G | 100 | 10000 | 0 | 0 | 3509.39 | 28.37 | 38.63 | 113 | N/A | N/A |
|  Direct Proxy | 2G | 100 | 100000 | 0 | 0 | 1550.45 | 64.26 | 39.8 | 155 | N/A | N/A |
|  Direct Proxy | 2G | 200 | 500 | 0 | 0 | 4125.58 | 48.35 | 53.59 | 160 | N/A | N/A |
|  Direct Proxy | 2G | 200 | 1000 | 0 | 0 | 4122.21 | 48.4 | 53.13 | 159 | N/A | N/A |
|  Direct Proxy | 2G | 200 | 10000 | 0 | 0 | 3579.67 | 55.73 | 43.4 | 169 | N/A | N/A |
|  Direct Proxy | 2G | 200 | 100000 | 0 | 0 | 1519.36 | 131.33 | 59.68 | 275 | N/A | N/A |
|  Direct Proxy | 2G | 500 | 500 | 0 | 0 | 4095.44 | 121.89 | 85.49 | 311 | N/A | N/A |
|  Direct Proxy | 2G | 500 | 1000 | 0 | 0 | 4081.98 | 122.33 | 90.44 | 315 | N/A | N/A |
|  Direct Proxy | 2G | 500 | 10000 | 0 | 0 | 1383.57 | 361.15 | 790.79 | 3487 | N/A | N/A |
|  Direct Proxy | 2G | 500 | 100000 | 0 | 0 | 1168.53 | 427.51 | 125.5 | 731 | N/A | N/A |
|  Direct Proxy | 2G | 1000 | 500 | 0 | 0 | 153.01 | 6330.89 | 5946.59 | 30975 | N/A | N/A |
|  Direct Proxy | 2G | 1000 | 1000 | 0 | 0 | 138.52 | 7057.5 | 6700.82 | 31615 | N/A | N/A |
|  Direct Proxy | 2G | 1000 | 10000 | 0 | 0 | 240.55 | 4136.81 | 3082.2 | 12863 | N/A | N/A |
|  Direct Proxy | 2G | 1000 | 100000 | 0 | 0 | 1165.12 | 856.58 | 219.18 | 1391 | N/A | N/A |
