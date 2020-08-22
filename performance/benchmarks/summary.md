# WSO2 Micro Integrator 1.2.0 Performance Test Results

During each release, we execute various automated performance test scenarios and publish the results.

| Test Scenarios | Description |
| --- | --- |
| Direct Proxy | Passthrough proxy service |
| Direct API | Passthrough API service |

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
| Heap Size | The amount of memory allocated to the application | 1G |
| Concurrent Users | The number of users accessing the application at the same time. | 100, 200, 500, 1000 |
| Message Size (Bytes) | The request payload size in Bytes. | 500, 1000, 10000, 100000 |
| Back-end Delay (ms) | The delay added by the Back-end service. | 0 |

The duration of each test is **60 seconds**. The warm-up period is **30 seconds**.
The measurement results are collected after the warm-up period.

The performance tests were executed on 1 AWS CloudFormation stack.


System information for WSO2 Micro Integrator 1.2.0 in 1st AWS CloudFormation stack.

| Class | Subclass | Description | Value |
| --- | --- | --- | --- |
| AWS | EC2 | AMI-ID | ami-0bcc094591f354be2 |
| AWS | EC2 | Instance Type | c5.xlarge |
| System | Processor | CPU(s) | 4 |
| System | Processor | Thread(s) per core | 2 |
| System | Processor | Core(s) per socket | 2 |
| System | Processor | Socket(s) | 1 |
| System | Processor | Model name | Intel(R) Xeon(R) Platinum 8124M CPU @ 3.00GHz |
| System | Memory | BIOS | 64 KiB |
| System | Memory | System memory | 7792736 KiB |
| System | Storage | Block Device: nvme0n1 | 8G |
| Operating System | Distribution | Release | Ubuntu 18.04.5 LTS |
| Operating System | Distribution | Kernel | Linux ip-10-0-1-133 5.3.0-1032-aws #34~18.04.2-Ubuntu SMP Fri Jul 24 10:06:28 UTC 2020 x86_64 x86_64 x86_64 GNU/Linux |


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
|  Direct API | 1G | 100 | 500 | 0 | 0 | 2516.02 | 30.12 | 131.77 | 895 | N/A | N/A |
|  Direct API | 1G | 100 | 1000 | 0 | 0 | 3942.45 | 19.21 | 77.41 | 234 | N/A | N/A |
|  Direct API | 1G | 100 | 10000 | 0 | 0 | 4890.5 | 15.43 | 40.88 | 187 | N/A | N/A |
|  Direct API | 1G | 100 | 100000 | 0 | 0 | 2409.51 | 31.33 | 23.94 | 117 | N/A | N/A |
|  Direct API | 1G | 200 | 500 | 0 | 0 | 5383.02 | 27.92 | 70.5 | 277 | N/A | N/A |
|  Direct API | 1G | 200 | 1000 | 0 | 0 | 4305.48 | 34.96 | 143.59 | 851 | N/A | N/A |
|  Direct API | 1G | 200 | 10000 | 0 | 0 | 3447.67 | 43.16 | 146.51 | 915 | N/A | N/A |
|  Direct API | 1G | 200 | 100000 | 0 | 0 | 2442.47 | 61.78 | 42.9 | 170 | N/A | N/A |
|  Direct API | 1G | 500 | 500 | 0 | 0 | 3097.9 | 118.95 | 280.86 | 1175 | N/A | N/A |
|  Direct API | 1G | 500 | 1000 | 0 | 0 | 1856.49 | 203.14 | 557.65 | 2927 | N/A | N/A |
|  Direct API | 1G | 500 | 10000 | 0 | 0 | 1514.12 | 247.84 | 409.4 | 1759 | N/A | N/A |
|  Direct API | 1G | 500 | 100000 | 0 | 0 | 2370.55 | 158.01 | 87.06 | 335 | N/A | N/A |
|  Direct API | 1G | 1000 | 500 | 0 | 0 | 353.06 | 2109.64 | 1369.18 | 6239 | N/A | N/A |
|  Direct API | 1G | 1000 | 1000 | 0 | 0 | 435.32 | 1684.38 | 1221.47 | 7135 | N/A | N/A |
|  Direct API | 1G | 1000 | 10000 | 0 | 0 | 449.37 | 1591.46 | 1074.84 | 4895 | N/A | N/A |
|  Direct API | 1G | 1000 | 100000 | 0 | 0 | 2248.07 | 332.81 | 141.69 | 1167 | N/A | N/A |
|  Direct Proxy | 1G | 100 | 500 | 0 | 0 | 4817.37 | 15.84 | 64.86 | 173 | 82.62 | 480.473 |
|  Direct Proxy | 1G | 100 | 1000 | 0 | 0 | 4626.21 | 16.43 | 55.81 | 206 | N/A | N/A |
|  Direct Proxy | 1G | 100 | 10000 | 0 | 0 | 4555.53 | 16.59 | 65.51 | 189 | N/A | N/A |
|  Direct Proxy | 1G | 100 | 100000 | 0 | 0 | 2470.94 | 30.5 | 23.04 | 113 | N/A | N/A |
|  Direct Proxy | 1G | 200 | 500 | 0 | 0 | 4164.37 | 36.41 | 140.11 | 895 | N/A | N/A |
|  Direct Proxy | 1G | 200 | 1000 | 0 | 0 | 2956.71 | 51.24 | 224.69 | 975 | N/A | N/A |
|  Direct Proxy | 1G | 200 | 10000 | 0 | 0 | 2920.09 | 51.55 | 171.7 | 975 | N/A | N/A |
|  Direct Proxy | 1G | 200 | 100000 | 0 | 0 | 2482.31 | 60.59 | 40.41 | 159 | N/A | N/A |
|  Direct Proxy | 1G | 500 | 500 | 0 | 0 | 1878.34 | 199.21 | 448.2 | 1943 | N/A | N/A |
|  Direct Proxy | 1G | 500 | 1000 | 0 | 0 | 1655.86 | 226.79 | 467.93 | 1791 | N/A | N/A |
|  Direct Proxy | 1G | 500 | 10000 | 0 | 0 | 886.97 | 426.86 | 638.16 | 2703 | N/A | N/A |
|  Direct Proxy | 1G | 500 | 100000 | 0 | 0 | 2407.13 | 155.27 | 97.11 | 335 | N/A | N/A |
|  Direct Proxy | 1G | 1000 | 500 | 0 | 0 | 261.93 | 2777.16 | 1628.95 | 7551 | N/A | N/A |
|  Direct Proxy | 1G | 1000 | 1000 | 0 | 0 | 343.11 | 2075.84 | 1191.15 | 5599 | N/A | N/A |
|  Direct Proxy | 1G | 1000 | 10000 | 0 | 0 | 991.08 | 731.67 | 924.58 | 4479 | N/A | N/A |
|  Direct Proxy | 1G | 1000 | 100000 | 0 | 0 | 2343.15 | 319.39 | 128.89 | 991 | N/A | N/A |
