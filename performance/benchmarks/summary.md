# WSO2 Micro Integrator 1.2.0 Performance Test Results

During each release, we execute various automated performance test scenarios and publish the results.

| Test Scenarios | Description |
| --- | --- |
| Direct Proxy | Passthrough proxy service |
| CBR Proxy | Routing the message based on the content of the message body |
| XSLT Proxy | Having XSLT transformations in request and response paths |
| CBR SOAP Header Proxy | Routing the message based on a SOAP header in the message payload |
| CBR Transport Header Proxy | Routing the message based on an HTTP header in the message |
| XSLT Enhanced Proxy | Having enhanced, Fast XSLT transformations in request and response paths |

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
| Message Size (Bytes) | The request payload size in Bytes. | 500, 1024, 10240, 102400 |
| Back-end Delay (ms) | The delay added by the Back-end service. | 0 |

The duration of each test is **60 seconds**. The warm-up period is **30 seconds**.
The measurement results are collected after the warm-up period.

The performance tests were executed on 1 AWS CloudFormation stack.


System information for WSO2 Micro Integrator 1.2.0 in 1st AWS CloudFormation stack.

| Class | Subclass | Description | Value |
| --- | --- | --- | --- |
| AWS | EC2 | AMI-ID | ami-07df16d0682f1fa59 |
| AWS | EC2 | Instance Type | c5.large |
| System | Processor | CPU(s) | 2 |
| System | Processor | Thread(s) per core | 2 |
| System | Processor | Core(s) per socket | 1 |
| System | Processor | Socket(s) | 1 |
| System | Processor | Model name | Intel(R) Xeon(R) Platinum 8275CL CPU @ 3.00GHz |
| System | Memory | BIOS | 64 KiB |
| System | Memory | System memory | 3813888 KiB |
| System | Storage | Block Device: nvme0n1 | 8G |
| Operating System | Distribution | Release | Ubuntu 18.04.4 LTS |
| Operating System | Distribution | Kernel | Linux ip-10-0-1-206 5.3.0-1032-aws #34~18.04.2-Ubuntu SMP Fri Jul 24 10:06:28 UTC 2020 x86_64 x86_64 x86_64 GNU/Linux |


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
|  CBR Proxy | 1G | 100 | 500 | 0 | 0 | 3244.13 | 23.28 | 39.34 | 94 | N/A | N/A |
|  CBR Proxy | 1G | 100 | 1024 | 0 | 0 | 2897.08 | 26.13 | 35.11 | 88 | N/A | N/A |
|  CBR Proxy | 1G | 100 | 10240 | 0 | 0 | 801.81 | 93.35 | 46.83 | 230 | N/A | N/A |
|  CBR Proxy | 1G | 100 | 102400 | 0 | 0 | 67.94 | 1112.35 | 454.71 | 2335 | N/A | N/A |
|  CBR Proxy | 1G | 200 | 500 | 0 | 0 | 2951.96 | 50.99 | 95.59 | 152 | N/A | N/A |
|  CBR Proxy | 1G | 200 | 1024 | 0 | 0 | 2871.04 | 52.43 | 50.22 | 163 | N/A | N/A |
|  CBR Proxy | 1G | 200 | 10240 | 0 | 0 | 792.4 | 188.76 | 93.57 | 469 | N/A | N/A |
|  CBR Proxy | 1G | 200 | 102400 | 0 | 0 | 49.65 | 3011.38 | 1524.54 | 7327 | N/A | N/A |
|  CBR Proxy | 1G | 500 | 500 | 0 | 0 | 2638.94 | 142.54 | 126.55 | 383 | N/A | N/A |
|  CBR Proxy | 1G | 500 | 1024 | 0 | 0 | 2635.97 | 142.66 | 82.22 | 383 | N/A | N/A |
|  CBR Proxy | 1G | 500 | 10240 | 0 | 0 | 672.06 | 557.66 | 276.22 | 1455 | N/A | N/A |
|  CBR Proxy | 1G | 500 | 102400 | 0 | 0 | 10.63 | 36269.72 | 14989.56 | 55551 | N/A | N/A |
|  CBR Proxy | 1G | 1000 | 500 | 0 | 0 | 2424.7 | 309.06 | 183.88 | 1455 | N/A | N/A |
|  CBR Proxy | 1G | 1000 | 1024 | 0 | 0 | 2162.38 | 345.91 | 150.96 | 755 | N/A | N/A |
|  CBR Proxy | 1G | 1000 | 10240 | 0 | 0 | 512.06 | 1453.55 | 642 | 2975 | N/A | N/A |
|  CBR Proxy | 1G | 1000 | 102400 | 0 | 100 | 3.54 | 118922.14 | 2884.32 | 120319 | N/A | N/A |
|  CBR SOAP Header Proxy | 1G | 100 | 500 | 0 | 100 | 24692.23 | 2.54 | 2.09 | 7 | N/A | N/A |
|  CBR SOAP Header Proxy | 1G | 100 | 1024 | 0 | 100 | 24974.77 | 2.51 | 2.07 | 7 | N/A | N/A |
|  CBR SOAP Header Proxy | 1G | 100 | 10240 | 0 | 100 | 24764.87 | 2.53 | 2.72 | 7 | N/A | N/A |
|  CBR SOAP Header Proxy | 1G | 100 | 102400 | 0 | 100 | 24869.11 | 2.52 | 2.08 | 7 | N/A | N/A |
|  CBR SOAP Header Proxy | 1G | 200 | 500 | 0 | 100 | 24559.47 | 5.18 | 5.64 | 22 | N/A | N/A |
|  CBR SOAP Header Proxy | 1G | 200 | 1024 | 0 | 100 | 24553.76 | 5.2 | 5.55 | 22 | N/A | N/A |
|  CBR SOAP Header Proxy | 1G | 200 | 10240 | 0 | 100 | 24560.2 | 5.2 | 5.46 | 23 | N/A | N/A |
|  CBR SOAP Header Proxy | 1G | 200 | 102400 | 0 | 100 | 24728.71 | 5.16 | 5.7 | 22 | N/A | N/A |
|  CBR SOAP Header Proxy | 1G | 500 | 500 | 0 | 100 | 22998.48 | 13.89 | 25.9 | 143 | N/A | N/A |
|  CBR SOAP Header Proxy | 1G | 500 | 1024 | 0 | 100 | 23321.55 | 13.73 | 25.19 | 139 | N/A | N/A |
|  CBR SOAP Header Proxy | 1G | 500 | 10240 | 0 | 100 | 23222.23 | 13.83 | 28.54 | 154 | N/A | N/A |
|  CBR SOAP Header Proxy | 1G | 500 | 102400 | 0 | 100 | 23603.12 | 13.58 | 25.43 | 140 | N/A | N/A |
|  CBR SOAP Header Proxy | 1G | 1000 | 500 | 0 | 100 | 20926.95 | 29.19 | 64.13 | 327 | N/A | N/A |
|  CBR SOAP Header Proxy | 1G | 1000 | 1024 | 0 | 100 | 19930.84 | 31.44 | 75.08 | 401 | N/A | N/A |
|  CBR SOAP Header Proxy | 1G | 1000 | 10240 | 0 | 100 | 20267.37 | 30.73 | 65.37 | 335 | N/A | N/A |
|  CBR SOAP Header Proxy | 1G | 1000 | 102400 | 0 | 100 | 20487.84 | 30.07 | 57.97 | 297 | N/A | N/A |
|  CBR Transport Header Proxy | 1G | 100 | 500 | 0 | 100 | 24798.79 | 2.52 | 2.41 | 7 | N/A | N/A |
|  CBR Transport Header Proxy | 1G | 100 | 1024 | 0 | 100 | 24840.43 | 2.52 | 2.14 | 7 | N/A | N/A |
|  CBR Transport Header Proxy | 1G | 100 | 10240 | 0 | 100 | 24871.63 | 2.52 | 2.38 | 7 | N/A | N/A |
|  CBR Transport Header Proxy | 1G | 100 | 102400 | 0 | 100 | 24863.49 | 2.51 | 2.1 | 7 | N/A | N/A |
|  CBR Transport Header Proxy | 1G | 200 | 500 | 0 | 100 | 24663.61 | 5.18 | 5.51 | 23 | N/A | N/A |
|  CBR Transport Header Proxy | 1G | 200 | 1024 | 0 | 100 | 24591.22 | 5.2 | 6 | 22 | N/A | N/A |
|  CBR Transport Header Proxy | 1G | 200 | 10240 | 0 | 100 | 24463.84 | 5.23 | 5.99 | 21 | N/A | N/A |
|  CBR Transport Header Proxy | 1G | 200 | 102400 | 0 | 100 | 24352.51 | 5.23 | 5.59 | 22 | N/A | N/A |
|  CBR Transport Header Proxy | 1G | 500 | 500 | 0 | 100 | 22996.62 | 13.89 | 25.01 | 137 | N/A | N/A |
|  CBR Transport Header Proxy | 1G | 500 | 1024 | 0 | 100 | 22475.25 | 14.24 | 25.99 | 140 | N/A | N/A |
|  CBR Transport Header Proxy | 1G | 500 | 10240 | 0 | 100 | 22846.58 | 14.28 | 26.98 | 147 | N/A | N/A |
|  CBR Transport Header Proxy | 1G | 500 | 102400 | 0 | 100 | 23380.85 | 13.81 | 26.45 | 147 | N/A | N/A |
|  CBR Transport Header Proxy | 1G | 1000 | 500 | 0 | 100 | 20475.2 | 30.48 | 72.4 | 353 | N/A | N/A |
|  CBR Transport Header Proxy | 1G | 1000 | 1024 | 0 | 100 | 20681.3 | 30.05 | 60.47 | 323 | N/A | N/A |
|  CBR Transport Header Proxy | 1G | 1000 | 10240 | 0 | 100 | 20071.53 | 30.96 | 67.34 | 341 | N/A | N/A |
|  CBR Transport Header Proxy | 1G | 1000 | 102400 | 0 | 100 | 20448.25 | 30.17 | 67.9 | 339 | N/A | N/A |
|  Direct Proxy | 1G | 100 | 500 | 0 | 0 | 2866.26 | 26.42 | 58.29 | 112 | 94.77 | 121.031 |
|  Direct Proxy | 1G | 100 | 1024 | 0 | 0 | 3686.95 | 20.52 | 60.73 | 80 | N/A | N/A |
|  Direct Proxy | 1G | 100 | 10240 | 0 | 0 | 3084.02 | 24.52 | 18.1 | 81 | N/A | N/A |
|  Direct Proxy | 1G | 100 | 102400 | 0 | 0 | 1051.28 | 71.95 | 22.91 | 151 | N/A | N/A |
|  Direct Proxy | 1G | 200 | 500 | 0 | 0 | 3177.85 | 47.24 | 115.41 | 204 | N/A | N/A |
|  Direct Proxy | 1G | 200 | 1024 | 0 | 0 | 3558.87 | 42.28 | 69.72 | 148 | N/A | N/A |
|  Direct Proxy | 1G | 200 | 10240 | 0 | 0 | 2794.87 | 53.87 | 65.23 | 154 | N/A | N/A |
|  Direct Proxy | 1G | 200 | 102400 | 0 | 0 | 980.49 | 153.47 | 67.15 | 303 | N/A | N/A |
|  Direct Proxy | 1G | 500 | 500 | 0 | 0 | 3731.58 | 100.52 | 182.84 | 1271 | N/A | N/A |
|  Direct Proxy | 1G | 500 | 1024 | 0 | 0 | 3979.2 | 93.54 | 142.62 | 273 | N/A | N/A |
|  Direct Proxy | 1G | 500 | 10240 | 0 | 0 | 3044.23 | 123.34 | 68.06 | 291 | N/A | N/A |
|  Direct Proxy | 1G | 500 | 102400 | 0 | 0 | 895.69 | 418.38 | 117.71 | 739 | N/A | N/A |
|  Direct Proxy | 1G | 1000 | 500 | 0 | 0 | 4121.26 | 180.23 | 143.7 | 639 | N/A | N/A |
|  Direct Proxy | 1G | 1000 | 1024 | 0 | 0 | 4209.58 | 177 | 98.12 | 501 | N/A | N/A |
|  Direct Proxy | 1G | 1000 | 10240 | 0 | 0 | 3035.82 | 246.22 | 121.63 | 539 | N/A | N/A |
|  Direct Proxy | 1G | 1000 | 102400 | 0 | 0 | 875.53 | 853.41 | 242.05 | 1535 | N/A | N/A |
|  XSLT Enhanced Proxy | 1G | 100 | 500 | 0 | 100 | 24983.84 | 2.51 | 2.07 | 7 | N/A | N/A |
|  XSLT Enhanced Proxy | 1G | 100 | 1024 | 0 | 100 | 24979.25 | 2.51 | 2.33 | 7 | N/A | N/A |
|  XSLT Enhanced Proxy | 1G | 100 | 10240 | 0 | 100 | 24935.77 | 2.51 | 2.17 | 7 | N/A | N/A |
|  XSLT Enhanced Proxy | 1G | 100 | 102400 | 0 | 100 | 24806.97 | 2.53 | 2.11 | 7 | N/A | N/A |
|  XSLT Enhanced Proxy | 1G | 200 | 500 | 0 | 100 | 24712.74 | 5.16 | 5.75 | 23 | N/A | N/A |
|  XSLT Enhanced Proxy | 1G | 200 | 1024 | 0 | 100 | 24623.18 | 5.18 | 5.51 | 22 | N/A | N/A |
|  XSLT Enhanced Proxy | 1G | 200 | 10240 | 0 | 100 | 24747.3 | 5.16 | 5.77 | 22 | N/A | N/A |
|  XSLT Enhanced Proxy | 1G | 200 | 102400 | 0 | 100 | 24677.91 | 5.18 | 5.25 | 20 | N/A | N/A |
|  XSLT Enhanced Proxy | 1G | 500 | 500 | 0 | 100 | 23044.33 | 13.76 | 25.73 | 145 | N/A | N/A |
|  XSLT Enhanced Proxy | 1G | 500 | 1024 | 0 | 100 | 23461.82 | 13.8 | 26.74 | 152 | N/A | N/A |
|  XSLT Enhanced Proxy | 1G | 500 | 10240 | 0 | 100 | 23050.97 | 13.87 | 26.99 | 146 | N/A | N/A |
|  XSLT Enhanced Proxy | 1G | 500 | 102400 | 0 | 100 | 22729.89 | 14.13 | 27.31 | 145 | N/A | N/A |
|  XSLT Enhanced Proxy | 1G | 1000 | 500 | 0 | 100 | 20836.77 | 29.39 | 58.31 | 303 | N/A | N/A |
|  XSLT Enhanced Proxy | 1G | 1000 | 1024 | 0 | 100 | 20194.07 | 29.84 | 67.62 | 345 | N/A | N/A |
|  XSLT Enhanced Proxy | 1G | 1000 | 10240 | 0 | 100 | 19625.7 | 31.29 | 64.37 | 333 | N/A | N/A |
|  XSLT Enhanced Proxy | 1G | 1000 | 102400 | 0 | 100 | 20131.64 | 30.05 | 65.57 | 335 | N/A | N/A |
|  XSLT Proxy | 1G | 100 | 500 | 0 | 100 | 24811.87 | 2.52 | 2.66 | 7 | N/A | N/A |
|  XSLT Proxy | 1G | 100 | 1024 | 0 | 100 | 24871.11 | 2.52 | 2.15 | 7 | N/A | N/A |
|  XSLT Proxy | 1G | 100 | 10240 | 0 | 100 | 24829.63 | 2.52 | 2.03 | 7 | N/A | N/A |
|  XSLT Proxy | 1G | 100 | 102400 | 0 | 100 | 24887.76 | 2.52 | 2.13 | 7 | N/A | N/A |
|  XSLT Proxy | 1G | 200 | 500 | 0 | 100 | 24611.81 | 5.19 | 6.17 | 24 | N/A | N/A |
|  XSLT Proxy | 1G | 200 | 1024 | 0 | 100 | 24587.26 | 5.2 | 5.67 | 24 | N/A | N/A |
|  XSLT Proxy | 1G | 200 | 10240 | 0 | 100 | 24675.81 | 5.18 | 5.93 | 23 | N/A | N/A |
|  XSLT Proxy | 1G | 200 | 102400 | 0 | 100 | 24634.25 | 5.18 | 5.64 | 22 | N/A | N/A |
|  XSLT Proxy | 1G | 500 | 500 | 0 | 100 | 23099.93 | 13.93 | 24.81 | 130 | N/A | N/A |
|  XSLT Proxy | 1G | 500 | 1024 | 0 | 100 | 23208.53 | 13.85 | 27.34 | 148 | N/A | N/A |
|  XSLT Proxy | 1G | 500 | 10240 | 0 | 100 | 23026.05 | 13.96 | 28.93 | 152 | N/A | N/A |
|  XSLT Proxy | 1G | 500 | 102400 | 0 | 100 | 23422.67 | 13.84 | 25.64 | 140 | N/A | N/A |
|  XSLT Proxy | 1G | 1000 | 500 | 0 | 100 | 19759.43 | 31.02 | 70.34 | 363 | N/A | N/A |
|  XSLT Proxy | 1G | 1000 | 1024 | 0 | 100 | 20393.44 | 30.57 | 68.35 | 357 | N/A | N/A |
|  XSLT Proxy | 1G | 1000 | 10240 | 0 | 100 | 19849.06 | 31.26 | 66.67 | 337 | N/A | N/A |
|  XSLT Proxy | 1G | 1000 | 102400 | 0 | 100 | 20666.62 | 28.81 | 64.22 | 341 | N/A | N/A |
