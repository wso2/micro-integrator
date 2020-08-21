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
| Heap Size | The amount of memory allocated to the application | 1G, 2G |
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
| Operating System | Distribution | Kernel | Linux ip-10-0-1-10 5.3.0-1032-aws #34~18.04.2-Ubuntu SMP Fri Jul 24 10:06:28 UTC 2020 x86_64 x86_64 x86_64 GNU/Linux |


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
|  Direct API | 1G | 100 | 500 | 0 | 0 | 3758.63 | 20.09 | 45.23 | 85 | N/A | N/A |
|  Direct API | 1G | 100 | 1000 | 0 | 0 | 3901.9 | 19.36 | 19.56 | 83 | N/A | N/A |
|  Direct API | 1G | 100 | 10000 | 0 | 0 | 3345.74 | 22.59 | 21.22 | 85 | N/A | N/A |
|  Direct API | 1G | 100 | 100000 | 0 | 0 | 1435.4 | 52.68 | 22.97 | 123 | N/A | N/A |
|  Direct API | 1G | 200 | 500 | 0 | 0 | 3363.87 | 44.65 | 108.6 | 137 | N/A | N/A |
|  Direct API | 1G | 200 | 1000 | 0 | 0 | 3334.25 | 45.08 | 92.23 | 142 | N/A | N/A |
|  Direct API | 1G | 200 | 10000 | 0 | 0 | 3312.79 | 45.33 | 32.94 | 129 | N/A | N/A |
|  Direct API | 1G | 200 | 100000 | 0 | 0 | 1347.52 | 111.44 | 39 | 227 | N/A | N/A |
|  Direct API | 1G | 500 | 500 | 0 | 0 | 2379.67 | 150.76 | 208.77 | 1511 | N/A | N/A |
|  Direct API | 1G | 500 | 1000 | 0 | 0 | 2896.65 | 129.41 | 185.53 | 367 | N/A | N/A |
|  Direct API | 1G | 500 | 10000 | 0 | 0 | 2991.33 | 125.24 | 91.42 | 297 | N/A | N/A |
|  Direct API | 1G | 500 | 100000 | 0 | 0 | 1239.94 | 302.59 | 95.24 | 551 | N/A | N/A |
|  Direct API | 1G | 1000 | 500 | 0 | 0 | 2764.68 | 270.19 | 362.79 | 1703 | N/A | N/A |
|  Direct API | 1G | 1000 | 1000 | 0 | 0 | 2959.35 | 252.22 | 246.81 | 1535 | N/A | N/A |
|  Direct API | 1G | 1000 | 10000 | 0 | 0 | 2811.88 | 265.21 | 243.98 | 1143 | N/A | N/A |
|  Direct API | 1G | 1000 | 100000 | 0 | 0 | 1153.39 | 648.68 | 207.61 | 1319 | N/A | N/A |
|  Direct API | 2G | 100 | 500 | 0 | 0 | 3575.26 | 21.12 | 60.61 | 81 | N/A | N/A |
|  Direct API | 2G | 100 | 1000 | 0 | 0 | 3542.94 | 21.3 | 58.47 | 79 | N/A | N/A |
|  Direct API | 2G | 100 | 10000 | 0 | 0 | 3285.44 | 22.96 | 19.75 | 85 | N/A | N/A |
|  Direct API | 2G | 100 | 100000 | 0 | 0 | 1387.47 | 54.46 | 21.39 | 119 | N/A | N/A |
|  Direct API | 2G | 200 | 500 | 0 | 0 | 3459.07 | 43.46 | 90.92 | 137 | N/A | N/A |
|  Direct API | 2G | 200 | 1000 | 0 | 0 | 3201.9 | 46.88 | 112.29 | 152 | N/A | N/A |
|  Direct API | 2G | 200 | 10000 | 0 | 0 | 3290.45 | 45.68 | 32.71 | 133 | N/A | N/A |
|  Direct API | 2G | 200 | 100000 | 0 | 0 | 1268.81 | 118.44 | 67.26 | 253 | N/A | N/A |
|  Direct API | 2G | 500 | 500 | 0 | 0 | 3234.1 | 115.77 | 185.65 | 1311 | N/A | N/A |
|  Direct API | 2G | 500 | 1000 | 0 | 0 | 3184.26 | 117.63 | 172.92 | 1087 | N/A | N/A |
|  Direct API | 2G | 500 | 10000 | 0 | 0 | 3228.33 | 116.11 | 88.61 | 295 | N/A | N/A |
|  Direct API | 2G | 500 | 100000 | 0 | 0 | 1263.86 | 296.88 | 94.55 | 551 | N/A | N/A |
|  Direct API | 2G | 1000 | 500 | 0 | 0 | 3399.74 | 218.97 | 249.44 | 1935 | N/A | N/A |
|  Direct API | 2G | 1000 | 1000 | 0 | 0 | 3522.83 | 210.34 | 237.9 | 1631 | N/A | N/A |
|  Direct API | 2G | 1000 | 10000 | 0 | 0 | 3031.66 | 245.85 | 194.09 | 1287 | N/A | N/A |
|  Direct API | 2G | 1000 | 100000 | 0 | 0 | 1190.85 | 629.69 | 193.31 | 1287 | N/A | N/A |
|  Direct Proxy | 1G | 100 | 500 | 0 | 0 | 2672.7 | 28.28 | 60.54 | 124 | 95.17 | 110.336 |
|  Direct Proxy | 1G | 100 | 1000 | 0 | 0 | 3453.38 | 21.87 | 39.62 | 92 | N/A | N/A |
|  Direct Proxy | 1G | 100 | 10000 | 0 | 0 | 3193.02 | 23.63 | 37.53 | 81 | N/A | N/A |
|  Direct Proxy | 1G | 100 | 100000 | 0 | 0 | 1414.8 | 53.38 | 20.28 | 114 | N/A | N/A |
|  Direct Proxy | 1G | 200 | 500 | 0 | 0 | 2827.29 | 53.22 | 168.28 | 194 | N/A | N/A |
|  Direct Proxy | 1G | 200 | 1000 | 0 | 0 | 3411.97 | 43.96 | 46.95 | 162 | N/A | N/A |
|  Direct Proxy | 1G | 200 | 10000 | 0 | 0 | 2947.82 | 50.95 | 68.89 | 163 | N/A | N/A |
|  Direct Proxy | 1G | 200 | 100000 | 0 | 0 | 1295.89 | 116 | 45.16 | 242 | N/A | N/A |
|  Direct Proxy | 1G | 500 | 500 | 0 | 0 | 3717.91 | 100.89 | 144.6 | 291 | N/A | N/A |
|  Direct Proxy | 1G | 500 | 1000 | 0 | 0 | 3931.12 | 95.3 | 118.45 | 269 | N/A | N/A |
|  Direct Proxy | 1G | 500 | 10000 | 0 | 0 | 3254.67 | 115.05 | 93.83 | 325 | N/A | N/A |
|  Direct Proxy | 1G | 500 | 100000 | 0 | 0 | 1299.54 | 288.53 | 91.58 | 535 | N/A | N/A |
|  Direct Proxy | 1G | 1000 | 500 | 0 | 0 | 3677.19 | 202.28 | 223.66 | 1559 | N/A | N/A |
|  Direct Proxy | 1G | 1000 | 1000 | 0 | 0 | 3842.06 | 194.02 | 142.04 | 487 | N/A | N/A |
|  Direct Proxy | 1G | 1000 | 10000 | 0 | 0 | 3287.99 | 226.41 | 107.69 | 543 | N/A | N/A |
|  Direct Proxy | 1G | 1000 | 100000 | 0 | 0 | 1268.62 | 589.72 | 175.75 | 1071 | N/A | N/A |
|  Direct Proxy | 2G | 100 | 500 | 0 | 0 | 3673.05 | 20.57 | 58.16 | 91 | N/A | N/A |
|  Direct Proxy | 2G | 100 | 1000 | 0 | 0 | 3673.71 | 20.3 | 56.33 | 85 | N/A | N/A |
|  Direct Proxy | 2G | 100 | 10000 | 0 | 0 | 3147.59 | 23.99 | 46.16 | 86 | N/A | N/A |
|  Direct Proxy | 2G | 100 | 100000 | 0 | 0 | 1428.85 | 52.88 | 22.91 | 126 | N/A | N/A |
|  Direct Proxy | 2G | 200 | 500 | 0 | 0 | 3871.8 | 38.79 | 81.55 | 124 | N/A | N/A |
|  Direct Proxy | 2G | 200 | 1000 | 0 | 0 | 3924.83 | 38.29 | 68.26 | 140 | N/A | N/A |
|  Direct Proxy | 2G | 200 | 10000 | 0 | 0 | 3346.67 | 44.9 | 46.38 | 138 | N/A | N/A |
|  Direct Proxy | 2G | 200 | 100000 | 0 | 0 | 1395.48 | 107.76 | 39.32 | 215 | N/A | N/A |
|  Direct Proxy | 2G | 500 | 500 | 0 | 0 | 3888.65 | 96.48 | 104.63 | 250 | N/A | N/A |
|  Direct Proxy | 2G | 500 | 1000 | 0 | 0 | 3937.88 | 95 | 62.72 | 255 | N/A | N/A |
|  Direct Proxy | 2G | 500 | 10000 | 0 | 0 | 3212.63 | 116.54 | 114.4 | 319 | N/A | N/A |
|  Direct Proxy | 2G | 500 | 100000 | 0 | 0 | 1262.18 | 297.08 | 132.47 | 1071 | N/A | N/A |
|  Direct Proxy | 2G | 1000 | 500 | 0 | 0 | 3279.45 | 226.14 | 189.42 | 1415 | N/A | N/A |
|  Direct Proxy | 2G | 1000 | 1000 | 0 | 0 | 3226.38 | 224.36 | 176.1 | 1391 | N/A | N/A |
|  Direct Proxy | 2G | 1000 | 10000 | 0 | 0 | 3026.85 | 245.78 | 179.82 | 1311 | N/A | N/A |
|  Direct Proxy | 2G | 1000 | 100000 | 0 | 0 | 1207.59 | 617.97 | 173.17 | 1095 | N/A | N/A |
