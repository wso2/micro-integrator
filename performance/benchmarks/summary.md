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
| System | Processor | Model name | Intel(R) Xeon(R) Platinum 8124M CPU @ 3.00GHz |
| System | Memory | BIOS | 64 KiB |
| System | Memory | System Memory | 4 GiB |
| System | Storage | Block Device: nvme0n1 | 8G |
| Operating System | Distribution | Release | Ubuntu 18.04.6 LTS |
| Operating System | Distribution | Kernel | Linux ip-10-0-1-227 5.4.0-1068-aws #72~18.04.1-Ubuntu SMP Thu Mar 3 08:49:49 UTC 2022 x86_64 x86_64 x86_64 GNU/Linux |


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
|  Direct API | 2G | 100 | 500 | 0 | 0 | 3866.26 | 25.77 | 38.36 | 114 | 91.71 | 257.289 |
|  Direct API | 2G | 100 | 1000 | 0 | 0 | 3851.79 | 25.87 | 42.23 | 113 | N/A | N/A |
|  Direct API | 2G | 100 | 10000 | 0 | 0 | 3310.82 | 30.09 | 32.47 | 117 | N/A | N/A |
|  Direct API | 2G | 100 | 100000 | 0 | 0 | 1416.66 | 70.36 | 46.05 | 186 | N/A | N/A |
|  Direct API | 2G | 200 | 500 | 0 | 0 | 3913.87 | 50.98 | 67.84 | 163 | N/A | N/A |
|  Direct API | 2G | 200 | 1000 | 0 | 0 | 3955.88 | 50.44 | 54 | 163 | N/A | N/A |
|  Direct API | 2G | 200 | 10000 | 0 | 0 | 3348.17 | 59.58 | 50.7 | 176 | N/A | N/A |
|  Direct API | 2G | 200 | 100000 | 0 | 0 | 1394.86 | 143.07 | 75.93 | 313 | N/A | N/A |
|  Direct API | 2G | 500 | 500 | 0 | 0 | 3939.52 | 126.73 | 91.92 | 323 | N/A | N/A |
|  Direct API | 2G | 500 | 1000 | 0 | 0 | 3955.51 | 126.24 | 87.09 | 321 | N/A | N/A |
|  Direct API | 2G | 500 | 10000 | 0 | 0 | 3367.32 | 148.26 | 79.64 | 347 | N/A | N/A |
|  Direct API | 2G | 500 | 100000 | 0 | 0 | 1291.17 | 386.96 | 132.37 | 719 | N/A | N/A |
|  Direct API | 2G | 1000 | 500 | 0 | 0 | 472.22 | 2094.84 | 2163.65 | 7743 | N/A | N/A |
|  Direct API | 2G | 1000 | 1000 | 0 | 0 | 124.23 | 7984.25 | 7490.56 | 35839 | N/A | N/A |
|  Direct API | 2G | 1000 | 10000 | 0 | 0 | 277.29 | 3592.6 | 2649.1 | 12095 | N/A | N/A |
|  Direct API | 2G | 1000 | 100000 | 0 | 0 | 1039.98 | 959.53 | 184.41 | 1423 | N/A | N/A |
