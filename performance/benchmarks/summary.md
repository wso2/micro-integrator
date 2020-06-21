# WSO2 Micro Integrator 1.2.0-alpha Performance Test Results

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

1. **Throughput**: The number of requests that the WSO2 Micro Integrator 1.2.0-alpha processes during a specific time interval (e.g. per second).
2. **Response Time**: The end-to-end latency for an operation of invoking a service in WSO2 Micro Integrator 1.2.0-alpha . The complete distribution of response times was recorded.

In addition to the above metrics, we measure the load average and several memory-related metrics.

The following are the test parameters.

| Test Parameter | Description | Values |
| --- | --- | --- |
| Scenario Name | The name of the test scenario. | Refer to the above table. |
| Heap Size | The amount of memory allocated to the application | 512M |
| Concurrent Users | The number of users accessing the application at the same time. | 100, 200 |
| Message Size (Bytes) | The request payload size in Bytes. | 500, 1024, 10240, 102400 |
| Back-end Delay (ms) | The delay added by the Back-end service. | 0, 30, 100, 500, 1000 |

The duration of each test is **60 seconds**. The warm-up period is **30 seconds**.
The measurement results are collected after the warm-up period.

The performance tests were executed on 1 AWS CloudFormation stack.


System information for WSO2 Micro Integrator 1.2.0-alpha in 1st AWS CloudFormation stack.

| Class | Subclass | Description | Value |
| --- | --- | --- | --- |
| AWS | EC2 | AMI-ID | ami-0ac80df6eff0e70b5 |
| AWS | EC2 | Instance Type | c5.xlarge |
| System | Processor | CPU(s) | 4 |
| System | Processor | Thread(s) per core | 2 |
| System | Processor | Core(s) per socket | 2 |
| System | Processor | Socket(s) | 1 |
| System | Processor | Model name | Intel(R) Xeon(R) Platinum 8275CL CPU @ 3.00GHz |
| System | Memory | BIOS | 64 KiB |
| System | Memory | System memory | 7850268 KiB |
| System | Storage | Block Device: nvme0n1 | 8G |
| Operating System | Distribution | Release | Ubuntu 18.04.4 LTS |
| Operating System | Distribution | Kernel | Linux ip-10-0-1-97 5.3.0-1023-aws #25~18.04.1-Ubuntu SMP Fri Jun 5 15:18:30 UTC 2020 x86_64 x86_64 x86_64 GNU/Linux |


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

|  Scenario Name | Heap Size | Concurrent Users | Message Size (Bytes) | Back-end Service Delay (ms) | Error % | Throughput (Requests/sec) | Average Response Time (ms) | Standard Deviation of Response Time (ms) | 99th Percentile of Response Time (ms) | WSO2 Micro Integrator 1.2.0-alpha GC Throughput (%) | Average WSO2 Micro Integrator 1.2.0-alpha Memory Footprint After Full GC (M) |
|---|---:|---:|---:|---:|---:|---:|---:|---:|---:|---:|---:|
|  CBR Proxy | 512M | 100 | 500 | 0 | 0 | 2486.18 | 30.55 | 116.09 | 615 | N/A | N/A |
|  CBR Proxy | 512M | 100 | 500 | 30 | 0 | 2065.03 | 36.55 | 17.71 | 66 | N/A | N/A |
|  CBR Proxy | 512M | 100 | 500 | 100 | 0 | 730.04 | 103.17 | 11.8 | 129 | N/A | N/A |
|  CBR Proxy | 512M | 100 | 500 | 500 | 0 | 148.7 | 501.33 | 1.21 | 503 | N/A | N/A |
|  CBR Proxy | 512M | 100 | 500 | 1000 | 0 | 74.17 | 1002.03 | 0.48 | 1003 | N/A | N/A |
|  CBR Proxy | 512M | 100 | 1024 | 0 | 0 | 2028.31 | 37.17 | 125.48 | 543 | N/A | N/A |
|  CBR Proxy | 512M | 100 | 1024 | 30 | 0 | 2028.46 | 37.15 | 14.01 | 66 | N/A | N/A |
|  CBR Proxy | 512M | 100 | 1024 | 100 | 0 | 734.16 | 102.72 | 5.07 | 128 | N/A | N/A |
|  CBR Proxy | 512M | 100 | 1024 | 500 | 0 | 149.15 | 501.45 | 1.48 | 503 | N/A | N/A |
|  CBR Proxy | 512M | 100 | 1024 | 1000 | 0 | 73.95 | 1002.07 | 0.75 | 1003 | N/A | N/A |
|  CBR Proxy | 512M | 100 | 10240 | 0 | 0 | 1075.57 | 69.25 | 41.97 | 199 | N/A | N/A |
|  CBR Proxy | 512M | 100 | 10240 | 30 | 0 | 1002.96 | 75.14 | 31.41 | 161 | N/A | N/A |
|  CBR Proxy | 512M | 100 | 10240 | 100 | 0 | 659.01 | 114.33 | 13.41 | 162 | N/A | N/A |
|  CBR Proxy | 512M | 100 | 10240 | 500 | 0 | 148.27 | 504.47 | 4.15 | 527 | N/A | N/A |
|  CBR Proxy | 512M | 100 | 10240 | 1000 | 0 | 74 | 1003.03 | 7.23 | 1019 | N/A | N/A |
|  CBR Proxy | 512M | 100 | 102400 | 0 | 0 | 65.63 | 1147.69 | 506.77 | 2559 | N/A | N/A |
|  CBR Proxy | 512M | 100 | 102400 | 30 | 0 | 66.45 | 1136.57 | 563.52 | 2703 | N/A | N/A |
|  CBR Proxy | 512M | 100 | 102400 | 100 | 0 | 67.02 | 1117.57 | 468.19 | 2431 | N/A | N/A |
|  CBR Proxy | 512M | 100 | 102400 | 500 | 0 | 58.75 | 1274.24 | 549.8 | 2895 | N/A | N/A |
|  CBR Proxy | 512M | 100 | 102400 | 1000 | 0 | 49.64 | 1488.9 | 513.47 | 3295 | N/A | N/A |
|  CBR Proxy | 512M | 200 | 500 | 0 | 0 | 1687.91 | 88.35 | 264.97 | 759 | N/A | N/A |
|  CBR Proxy | 512M | 200 | 500 | 30 | 0 | 1697.75 | 88.64 | 153.38 | 707 | N/A | N/A |
|  CBR Proxy | 512M | 200 | 500 | 100 | 0 | 1372.5 | 109.4 | 22.29 | 149 | N/A | N/A |
|  CBR Proxy | 512M | 200 | 500 | 500 | 0 | 297.09 | 501.51 | 2.43 | 515 | N/A | N/A |
|  CBR Proxy | 512M | 200 | 500 | 1000 | 0 | 147.47 | 1002.1 | 1.17 | 1007 | N/A | N/A |
|  CBR Proxy | 512M | 200 | 1024 | 0 | 0 | 2508.42 | 60.16 | 179.97 | 723 | N/A | N/A |
|  CBR Proxy | 512M | 200 | 1024 | 30 | 0 | 2386.01 | 63.28 | 103.51 | 651 | N/A | N/A |
|  CBR Proxy | 512M | 200 | 1024 | 100 | 0 | 1387.96 | 108.35 | 10.2 | 140 | N/A | N/A |
|  CBR Proxy | 512M | 200 | 1024 | 500 | 0 | 297.47 | 501.57 | 2.39 | 515 | N/A | N/A |
|  CBR Proxy | 512M | 200 | 1024 | 1000 | 0 | 147.53 | 1002.74 | 8.18 | 1015 | N/A | N/A |
|  CBR Proxy | 512M | 200 | 10240 | 0 | 0 | 201.63 | 155.04 | 699.37 | 485 | N/A | N/A |
|  CBR Proxy | 512M | 200 | 10240 | 30 | 0 | 1016.2 | 147.88 | 63.46 | 363 | N/A | N/A |
|  CBR Proxy | 512M | 200 | 10240 | 100 | 0 | 901.91 | 166.68 | 70.04 | 515 | N/A | N/A |
|  CBR Proxy | 512M | 200 | 10240 | 500 | 0 | 293.6 | 508.18 | 9.54 | 547 | N/A | N/A |
|  CBR Proxy | 512M | 200 | 10240 | 1000 | 0 | 146.93 | 1003.79 | 4.99 | 1031 | N/A | N/A |
|  CBR Proxy | 512M | 200 | 102400 | 0 | 0 | 26.38 | 5635.06 | 3503.54 | 14463 | N/A | N/A |
|  CBR Proxy | 512M | 200 | 102400 | 30 | 0 | 24.17 | 6319.99 | 3680.73 | 15551 | N/A | N/A |
|  CBR Proxy | 512M | 200 | 102400 | 100 | 0 | 24.85 | 5981.68 | 3947.6 | 15807 | N/A | N/A |
|  CBR Proxy | 512M | 200 | 102400 | 500 | 0 | 25.02 | 6016.83 | 3698.53 | 15231 | N/A | N/A |
|  CBR Proxy | 512M | 200 | 102400 | 1000 | 0 | 25.13 | 5985.69 | 3530.52 | 14463 | N/A | N/A |
|  CBR SOAP Header Proxy | 512M | 100 | 500 | 0 | 0 | 1498.8 | 50.39 | 179.47 | 627 | N/A | N/A |
|  CBR SOAP Header Proxy | 512M | 100 | 500 | 30 | 0 | 2015.73 | 37.37 | 25.45 | 67 | N/A | N/A |
|  CBR SOAP Header Proxy | 512M | 100 | 500 | 100 | 0 | 727.38 | 103.7 | 16.04 | 131 | N/A | N/A |
|  CBR SOAP Header Proxy | 512M | 100 | 500 | 500 | 0 | 149.11 | 501.28 | 1.21 | 505 | N/A | N/A |
|  CBR SOAP Header Proxy | 512M | 100 | 500 | 1000 | 0 | 73.88 | 1002.04 | 0.51 | 1003 | N/A | N/A |
|  CBR SOAP Header Proxy | 512M | 100 | 1024 | 0 | 0 | 2297.81 | 32.77 | 151.86 | 523 | N/A | N/A |
|  CBR SOAP Header Proxy | 512M | 100 | 1024 | 30 | 0 | 2030.02 | 37.13 | 21.28 | 68 | N/A | N/A |
|  CBR SOAP Header Proxy | 512M | 100 | 1024 | 100 | 0 | 731.59 | 102.91 | 5.51 | 130 | N/A | N/A |
|  CBR SOAP Header Proxy | 512M | 100 | 1024 | 500 | 0 | 149.18 | 501.32 | 1.25 | 505 | N/A | N/A |
|  CBR SOAP Header Proxy | 512M | 100 | 1024 | 1000 | 0 | 74.14 | 1002.1 | 0.94 | 1007 | N/A | N/A |
|  CBR SOAP Header Proxy | 512M | 100 | 10240 | 0 | 0 | 1603.19 | 46.98 | 31.15 | 138 | N/A | N/A |
|  CBR SOAP Header Proxy | 512M | 100 | 10240 | 30 | 0 | 1369.21 | 55.28 | 23.26 | 109 | N/A | N/A |
|  CBR SOAP Header Proxy | 512M | 100 | 10240 | 100 | 0 | 700.34 | 107.53 | 10.23 | 150 | N/A | N/A |
|  CBR SOAP Header Proxy | 512M | 100 | 10240 | 500 | 0 | 148.76 | 503.02 | 2.32 | 519 | N/A | N/A |
|  CBR SOAP Header Proxy | 512M | 100 | 10240 | 1000 | 0 | 73.96 | 1002.31 | 2.02 | 1015 | N/A | N/A |
|  CBR SOAP Header Proxy | 512M | 100 | 102400 | 0 | 0 | 134.16 | 562.77 | 262.04 | 1303 | N/A | N/A |
|  CBR SOAP Header Proxy | 512M | 100 | 102400 | 30 | 0 | 132.72 | 569.03 | 265.66 | 1327 | N/A | N/A |
|  CBR SOAP Header Proxy | 512M | 100 | 102400 | 100 | 0 | 125.56 | 602.24 | 248.24 | 1335 | N/A | N/A |
|  CBR SOAP Header Proxy | 512M | 100 | 102400 | 500 | 0 | 86.15 | 875.95 | 248.71 | 1407 | N/A | N/A |
|  CBR SOAP Header Proxy | 512M | 100 | 102400 | 1000 | 0 | 62.25 | 1218.9 | 231.1 | 1767 | N/A | N/A |
|  CBR SOAP Header Proxy | 512M | 200 | 500 | 0 | 0 | 1565.51 | 96.31 | 260.91 | 1087 | N/A | N/A |
|  CBR SOAP Header Proxy | 512M | 200 | 500 | 30 | 0 | 2026.9 | 74.1 | 154.21 | 987 | N/A | N/A |
|  CBR SOAP Header Proxy | 512M | 200 | 500 | 100 | 0 | 1411.13 | 106.58 | 10.48 | 143 | N/A | N/A |
|  CBR SOAP Header Proxy | 512M | 200 | 500 | 500 | 0 | 297.98 | 501.44 | 2.11 | 515 | N/A | N/A |
|  CBR SOAP Header Proxy | 512M | 200 | 500 | 1000 | 0 | 147.15 | 1002.16 | 1.55 | 1007 | N/A | N/A |
|  CBR SOAP Header Proxy | 512M | 200 | 1024 | 0 | 0 | 2416.83 | 59.22 | 142.76 | 703 | N/A | N/A |
|  CBR SOAP Header Proxy | 512M | 200 | 1024 | 30 | 0 | 2554.68 | 58.67 | 78.21 | 503 | N/A | N/A |
|  CBR SOAP Header Proxy | 512M | 200 | 1024 | 100 | 0 | 1405.15 | 106.97 | 15.19 | 140 | N/A | N/A |
|  CBR SOAP Header Proxy | 512M | 200 | 1024 | 500 | 0 | 296.84 | 501.9 | 3.62 | 527 | N/A | N/A |
|  CBR SOAP Header Proxy | 512M | 200 | 1024 | 1000 | 0 | 147.44 | 1002.13 | 1.29 | 1007 | N/A | N/A |
|  CBR SOAP Header Proxy | 512M | 200 | 10240 | 0 | 0 | 1574.88 | 95.63 | 61.41 | 293 | N/A | N/A |
|  CBR SOAP Header Proxy | 512M | 200 | 10240 | 30 | 0 | 1443.14 | 104.21 | 92.56 | 555 | N/A | N/A |
|  CBR SOAP Header Proxy | 512M | 200 | 10240 | 100 | 0 | 1093.95 | 137.32 | 46.81 | 419 | N/A | N/A |
|  CBR SOAP Header Proxy | 512M | 200 | 10240 | 500 | 0 | 294.78 | 506.3 | 13.43 | 555 | N/A | N/A |
|  CBR SOAP Header Proxy | 512M | 200 | 10240 | 1000 | 0 | 147.15 | 1002.44 | 2.56 | 1019 | N/A | N/A |
|  CBR SOAP Header Proxy | 512M | 200 | 102400 | 0 | 0 | 58.79 | 2547.99 | 1412.68 | 6175 | N/A | N/A |
|  CBR SOAP Header Proxy | 512M | 200 | 102400 | 30 | 0 | 63.97 | 2349.65 | 1136.89 | 5439 | N/A | N/A |
|  CBR SOAP Header Proxy | 512M | 200 | 102400 | 100 | 0 | 66.36 | 2268.79 | 1214.57 | 5311 | N/A | N/A |
|  CBR SOAP Header Proxy | 512M | 200 | 102400 | 500 | 0 | 59.93 | 2487.6 | 1155.64 | 5407 | N/A | N/A |
|  CBR SOAP Header Proxy | 512M | 200 | 102400 | 1000 | 0 | 50.39 | 2925.58 | 1237.93 | 6367 | N/A | N/A |
|  CBR Transport Header Proxy | 512M | 100 | 500 | 0 | 0 | 2358.63 | 32.24 | 162.41 | 575 | N/A | N/A |
|  CBR Transport Header Proxy | 512M | 100 | 500 | 30 | 0 | 2100.4 | 35.9 | 18.05 | 69 | N/A | N/A |
|  CBR Transport Header Proxy | 512M | 100 | 500 | 100 | 0 | 728.38 | 103.27 | 15.99 | 134 | N/A | N/A |
|  CBR Transport Header Proxy | 512M | 100 | 500 | 500 | 0 | 149.14 | 501.2 | 0.98 | 503 | N/A | N/A |
|  CBR Transport Header Proxy | 512M | 100 | 500 | 1000 | 0 | 74.17 | 1002.07 | 0.79 | 1003 | N/A | N/A |
|  CBR Transport Header Proxy | 512M | 100 | 1024 | 0 | 0 | 2539.59 | 29.79 | 127.59 | 509 | N/A | N/A |
|  CBR Transport Header Proxy | 512M | 100 | 1024 | 30 | 0 | 2110.17 | 35.78 | 12.65 | 68 | N/A | N/A |
|  CBR Transport Header Proxy | 512M | 100 | 1024 | 100 | 0 | 737.03 | 102.15 | 4.29 | 127 | N/A | N/A |
|  CBR Transport Header Proxy | 512M | 100 | 1024 | 500 | 0 | 149.04 | 501.32 | 1.25 | 503 | N/A | N/A |
|  CBR Transport Header Proxy | 512M | 100 | 1024 | 1000 | 0 | 73.9 | 1002.04 | 0.6 | 1003 | N/A | N/A |
|  CBR Transport Header Proxy | 512M | 100 | 10240 | 0 | 0 | 1682.97 | 44.85 | 137.53 | 595 | N/A | N/A |
|  CBR Transport Header Proxy | 512M | 100 | 10240 | 30 | 0 | 2011.12 | 37.49 | 16.51 | 73 | N/A | N/A |
|  CBR Transport Header Proxy | 512M | 100 | 10240 | 100 | 0 | 734.07 | 102.78 | 5 | 130 | N/A | N/A |
|  CBR Transport Header Proxy | 512M | 100 | 10240 | 500 | 0 | 149.24 | 501.54 | 1.42 | 503 | N/A | N/A |
|  CBR Transport Header Proxy | 512M | 100 | 10240 | 1000 | 0 | 74.15 | 1002.04 | 0.62 | 1003 | N/A | N/A |
|  CBR Transport Header Proxy | 512M | 100 | 102400 | 0 | 0 | 1533.91 | 47.03 | 35.86 | 148 | N/A | N/A |
|  CBR Transport Header Proxy | 512M | 100 | 102400 | 30 | 0 | 1389.24 | 52.98 | 29.18 | 114 | N/A | N/A |
|  CBR Transport Header Proxy | 512M | 100 | 102400 | 100 | 0 | 697.55 | 107.49 | 9.74 | 147 | N/A | N/A |
|  CBR Transport Header Proxy | 512M | 100 | 102400 | 500 | 0 | 148.57 | 503.38 | 1.55 | 509 | N/A | N/A |
|  CBR Transport Header Proxy | 512M | 100 | 102400 | 1000 | 0 | 73.82 | 1002.38 | 1.37 | 1007 | N/A | N/A |
|  CBR Transport Header Proxy | 512M | 200 | 500 | 0 | 0 | 2527.43 | 58.56 | 313.69 | 775 | N/A | N/A |
|  CBR Transport Header Proxy | 512M | 200 | 500 | 30 | 0 | 2305.26 | 65.39 | 135.15 | 671 | N/A | N/A |
|  CBR Transport Header Proxy | 512M | 200 | 500 | 100 | 0 | 1434.56 | 104.79 | 8.73 | 139 | N/A | N/A |
|  CBR Transport Header Proxy | 512M | 200 | 500 | 500 | 0 | 297.54 | 501.66 | 4.86 | 519 | N/A | N/A |
|  CBR Transport Header Proxy | 512M | 200 | 500 | 1000 | 0 | 147.29 | 1002.09 | 1.09 | 1003 | N/A | N/A |
|  CBR Transport Header Proxy | 512M | 200 | 1024 | 0 | 0 | 2634.51 | 55.02 | 178.11 | 699 | N/A | N/A |
|  CBR Transport Header Proxy | 512M | 200 | 1024 | 30 | 0 | 2668.37 | 56.77 | 91.37 | 497 | N/A | N/A |
|  CBR Transport Header Proxy | 512M | 200 | 1024 | 100 | 0 | 1431.95 | 104.85 | 8.52 | 139 | N/A | N/A |
|  CBR Transport Header Proxy | 512M | 200 | 1024 | 500 | 0 | 297.52 | 501.49 | 2.43 | 515 | N/A | N/A |
|  CBR Transport Header Proxy | 512M | 200 | 1024 | 1000 | 0 | 147.36 | 1002.13 | 1.35 | 1007 | N/A | N/A |
|  CBR Transport Header Proxy | 512M | 200 | 10240 | 0 | 0 | 2413.06 | 61.62 | 165.94 | 627 | N/A | N/A |
|  CBR Transport Header Proxy | 512M | 200 | 10240 | 30 | 0 | 2298.3 | 65.43 | 97.36 | 563 | N/A | N/A |
|  CBR Transport Header Proxy | 512M | 200 | 10240 | 100 | 0 | 1408.96 | 106.46 | 14.56 | 147 | N/A | N/A |
|  CBR Transport Header Proxy | 512M | 200 | 10240 | 500 | 0 | 296.88 | 501.88 | 2.89 | 519 | N/A | N/A |
|  CBR Transport Header Proxy | 512M | 200 | 10240 | 1000 | 0 | 147.33 | 1002.11 | 1.17 | 1007 | N/A | N/A |
|  CBR Transport Header Proxy | 512M | 200 | 102400 | 0 | 0 | 1490.28 | 95.01 | 59.6 | 301 | N/A | N/A |
|  CBR Transport Header Proxy | 512M | 200 | 102400 | 30 | 0 | 1479.09 | 97.87 | 46.34 | 267 | N/A | N/A |
|  CBR Transport Header Proxy | 512M | 200 | 102400 | 100 | 0 | 1172.61 | 126.59 | 41.07 | 220 | N/A | N/A |
|  CBR Transport Header Proxy | 512M | 200 | 102400 | 500 | 0 | 295.77 | 504.11 | 4.11 | 527 | N/A | N/A |
|  CBR Transport Header Proxy | 512M | 200 | 102400 | 1000 | 0 | 146.88 | 1002.49 | 2.22 | 1011 | N/A | N/A |
|  Direct Proxy | 512M | 100 | 500 | 0 | 0 | 2442.21 | 30.94 | 178.32 | 531 | 55.7 | 355.648 |
|  Direct Proxy | 512M | 100 | 500 | 30 | 0 | 2020.07 | 36.7 | 39.8 | 67 | N/A | N/A |
|  Direct Proxy | 512M | 100 | 500 | 100 | 0 | 733.44 | 102.87 | 9.77 | 130 | N/A | N/A |
|  Direct Proxy | 512M | 100 | 500 | 500 | 0 | 149.17 | 501.17 | 0.79 | 503 | N/A | N/A |
|  Direct Proxy | 512M | 100 | 500 | 1000 | 0 | 74.17 | 1002.02 | 0.44 | 1003 | N/A | N/A |
|  Direct Proxy | 512M | 100 | 1024 | 0 | 0 | 2726.13 | 27.03 | 137.39 | 463 | N/A | N/A |
|  Direct Proxy | 512M | 100 | 1024 | 30 | 0 | 2117.79 | 35.63 | 17.93 | 65 | N/A | N/A |
|  Direct Proxy | 512M | 100 | 1024 | 100 | 0 | 730.95 | 103.34 | 13.9 | 133 | N/A | N/A |
|  Direct Proxy | 512M | 100 | 1024 | 500 | 0 | 149.38 | 501.2 | 0.95 | 503 | N/A | N/A |
|  Direct Proxy | 512M | 100 | 1024 | 1000 | 0 | 74.21 | 1002.06 | 0.72 | 1003 | N/A | N/A |
|  Direct Proxy | 512M | 100 | 10240 | 0 | 0 | 1833.21 | 41.09 | 143.51 | 607 | N/A | N/A |
|  Direct Proxy | 512M | 100 | 10240 | 30 | 0 | 2048.69 | 36.73 | 17.14 | 69 | N/A | N/A |
|  Direct Proxy | 512M | 100 | 10240 | 100 | 0 | 737.5 | 102.12 | 3.76 | 123 | N/A | N/A |
|  Direct Proxy | 512M | 100 | 10240 | 500 | 0 | 148.99 | 501.39 | 1.09 | 503 | N/A | N/A |
|  Direct Proxy | 512M | 100 | 10240 | 1000 | 0 | 74.15 | 1002.04 | 0.61 | 1003 | N/A | N/A |
|  Direct Proxy | 512M | 100 | 102400 | 0 | 0 | 1524.28 | 47.36 | 35.84 | 159 | N/A | N/A |
|  Direct Proxy | 512M | 100 | 102400 | 30 | 0 | 1410.52 | 52.14 | 32.95 | 111 | N/A | N/A |
|  Direct Proxy | 512M | 100 | 102400 | 100 | 0 | 703.84 | 106.72 | 14.57 | 139 | N/A | N/A |
|  Direct Proxy | 512M | 100 | 102400 | 500 | 0 | 148.4 | 503.24 | 1.38 | 507 | N/A | N/A |
|  Direct Proxy | 512M | 100 | 102400 | 1000 | 0 | 73.73 | 1002.23 | 1.12 | 1007 | N/A | N/A |
|  Direct Proxy | 512M | 200 | 500 | 0 | 0 | 2850.3 | 52.99 | 293.99 | 619 | N/A | N/A |
|  Direct Proxy | 512M | 200 | 500 | 30 | 0 | 2611.61 | 56.78 | 114.87 | 659 | N/A | N/A |
|  Direct Proxy | 512M | 200 | 500 | 100 | 0 | 1448.4 | 103.97 | 7.34 | 135 | N/A | N/A |
|  Direct Proxy | 512M | 200 | 500 | 500 | 0 | 297.81 | 501.27 | 1.46 | 509 | N/A | N/A |
|  Direct Proxy | 512M | 200 | 500 | 1000 | 0 | 147.46 | 1002.13 | 1.54 | 1007 | N/A | N/A |
|  Direct Proxy | 512M | 200 | 1024 | 0 | 0 | 2136.98 | 68.49 | 245.56 | 967 | N/A | N/A |
|  Direct Proxy | 512M | 200 | 1024 | 30 | 0 | 2862.04 | 52.52 | 74.66 | 477 | N/A | N/A |
|  Direct Proxy | 512M | 200 | 1024 | 100 | 0 | 1448.73 | 103.83 | 7.17 | 136 | N/A | N/A |
|  Direct Proxy | 512M | 200 | 1024 | 500 | 0 | 296.8 | 501.4 | 2.21 | 515 | N/A | N/A |
|  Direct Proxy | 512M | 200 | 1024 | 1000 | 0 | 147.01 | 1002.09 | 1.17 | 1003 | N/A | N/A |
|  Direct Proxy | 512M | 200 | 10240 | 0 | 0 | 1875.94 | 80.28 | 216.31 | 875 | N/A | N/A |
|  Direct Proxy | 512M | 200 | 10240 | 30 | 0 | 1984.38 | 75.8 | 147.69 | 887 | N/A | N/A |
|  Direct Proxy | 512M | 200 | 10240 | 100 | 0 | 1391.07 | 107.94 | 11.3 | 151 | N/A | N/A |
|  Direct Proxy | 512M | 200 | 10240 | 500 | 0 | 296.76 | 501.76 | 2.94 | 523 | N/A | N/A |
|  Direct Proxy | 512M | 200 | 10240 | 1000 | 0 | 147.14 | 1002.07 | 0.87 | 1003 | N/A | N/A |
|  Direct Proxy | 512M | 200 | 102400 | 0 | 0 | 1496.68 | 95.49 | 65.25 | 351 | N/A | N/A |
|  Direct Proxy | 512M | 200 | 102400 | 30 | 0 | 1516.81 | 95.79 | 45.25 | 254 | N/A | N/A |
|  Direct Proxy | 512M | 200 | 102400 | 100 | 0 | 1198.52 | 123.96 | 27.78 | 213 | N/A | N/A |
|  Direct Proxy | 512M | 200 | 102400 | 500 | 0 | 296.04 | 504.04 | 3.26 | 523 | N/A | N/A |
|  Direct Proxy | 512M | 200 | 102400 | 1000 | 0 | 147.23 | 1002.35 | 1.72 | 1007 | N/A | N/A |
|  XSLT Enhanced Proxy | 512M | 100 | 500 | 0 | 0 | 1973.13 | 38.31 | 123.08 | 627 | N/A | N/A |
|  XSLT Enhanced Proxy | 512M | 100 | 500 | 30 | 0 | 1878.29 | 40.1 | 34.68 | 68 | N/A | N/A |
|  XSLT Enhanced Proxy | 512M | 100 | 500 | 100 | 0 | 731.81 | 103.15 | 5.27 | 128 | N/A | N/A |
|  XSLT Enhanced Proxy | 512M | 100 | 500 | 500 | 0 | 149.2 | 501.71 | 1.68 | 509 | N/A | N/A |
|  XSLT Enhanced Proxy | 512M | 100 | 500 | 1000 | 0 | 73.89 | 1002.12 | 1.04 | 1007 | N/A | N/A |
|  XSLT Enhanced Proxy | 512M | 100 | 1024 | 0 | 0 | 2146.53 | 35.12 | 112.47 | 619 | N/A | N/A |
|  XSLT Enhanced Proxy | 512M | 100 | 1024 | 30 | 0 | 1908.72 | 39.51 | 21.69 | 68 | N/A | N/A |
|  XSLT Enhanced Proxy | 512M | 100 | 1024 | 100 | 0 | 733.44 | 102.9 | 4.74 | 126 | N/A | N/A |
|  XSLT Enhanced Proxy | 512M | 100 | 1024 | 500 | 0 | 149.01 | 501.76 | 1.58 | 505 | N/A | N/A |
|  XSLT Enhanced Proxy | 512M | 100 | 1024 | 1000 | 0 | 73.87 | 1002.07 | 0.84 | 1003 | N/A | N/A |
|  XSLT Enhanced Proxy | 512M | 100 | 10240 | 0 | 0 | 914.8 | 82.41 | 44.58 | 221 | N/A | N/A |
|  XSLT Enhanced Proxy | 512M | 100 | 10240 | 30 | 0 | 881.86 | 85.48 | 32.73 | 184 | N/A | N/A |
|  XSLT Enhanced Proxy | 512M | 100 | 10240 | 100 | 0 | 623.35 | 120.82 | 22.07 | 194 | N/A | N/A |
|  XSLT Enhanced Proxy | 512M | 100 | 10240 | 500 | 0 | 148.43 | 504.17 | 2.93 | 519 | N/A | N/A |
|  XSLT Enhanced Proxy | 512M | 100 | 10240 | 1000 | 0 | 73.85 | 1002.79 | 2.08 | 1015 | N/A | N/A |
|  XSLT Enhanced Proxy | 512M | 100 | 102400 | 0 | 0 | 125.75 | 599.12 | 190.08 | 1087 | N/A | N/A |
|  XSLT Enhanced Proxy | 512M | 100 | 102400 | 30 | 0 | 127.03 | 594.52 | 187.67 | 1087 | N/A | N/A |
|  XSLT Enhanced Proxy | 512M | 100 | 102400 | 100 | 0 | 130.17 | 577.67 | 174.45 | 1007 | N/A | N/A |
|  XSLT Enhanced Proxy | 512M | 100 | 102400 | 500 | 0 | 109.75 | 681.73 | 99.32 | 987 | N/A | N/A |
|  XSLT Enhanced Proxy | 512M | 100 | 102400 | 1000 | 0 | 70.36 | 1047.88 | 33.24 | 1159 | N/A | N/A |
|  XSLT Enhanced Proxy | 512M | 200 | 500 | 0 | 0 | 2238.99 | 67.48 | 155.06 | 679 | N/A | N/A |
|  XSLT Enhanced Proxy | 512M | 200 | 500 | 30 | 0 | 2327.39 | 64.63 | 80.32 | 535 | N/A | N/A |
|  XSLT Enhanced Proxy | 512M | 200 | 500 | 100 | 0 | 1393.95 | 107.8 | 10.28 | 144 | N/A | N/A |
|  XSLT Enhanced Proxy | 512M | 200 | 500 | 500 | 0 | 296.14 | 502.14 | 4.67 | 523 | N/A | N/A |
|  XSLT Enhanced Proxy | 512M | 200 | 500 | 1000 | 0 | 147.36 | 1002.21 | 1.8 | 1011 | N/A | N/A |
|  XSLT Enhanced Proxy | 512M | 200 | 1024 | 0 | 0 | 1776.79 | 84.19 | 199.1 | 1039 | N/A | N/A |
|  XSLT Enhanced Proxy | 512M | 200 | 1024 | 30 | 0 | 1990.6 | 75.62 | 109.92 | 667 | N/A | N/A |
|  XSLT Enhanced Proxy | 512M | 200 | 1024 | 100 | 0 | 1376.91 | 108.95 | 11.52 | 151 | N/A | N/A |
|  XSLT Enhanced Proxy | 512M | 200 | 1024 | 500 | 0 | 297.12 | 503.34 | 7.41 | 527 | N/A | N/A |
|  XSLT Enhanced Proxy | 512M | 200 | 1024 | 1000 | 0 | 147.1 | 1002.19 | 1.57 | 1011 | N/A | N/A |
|  XSLT Enhanced Proxy | 512M | 200 | 10240 | 0 | 0 | 922.69 | 163.05 | 77.7 | 393 | N/A | N/A |
|  XSLT Enhanced Proxy | 512M | 200 | 10240 | 30 | 0 | 920.36 | 163.56 | 62.51 | 345 | N/A | N/A |
|  XSLT Enhanced Proxy | 512M | 200 | 10240 | 100 | 0 | 859.87 | 174.85 | 44.69 | 313 | N/A | N/A |
|  XSLT Enhanced Proxy | 512M | 200 | 10240 | 500 | 0 | 292.48 | 510.16 | 18.77 | 595 | N/A | N/A |
|  XSLT Enhanced Proxy | 512M | 200 | 10240 | 1000 | 0 | 146.87 | 1003.25 | 2.87 | 1019 | N/A | N/A |
|  XSLT Enhanced Proxy | 512M | 200 | 102400 | 0 | 0 | 105.21 | 1418.11 | 433.43 | 2591 | N/A | N/A |
|  XSLT Enhanced Proxy | 512M | 200 | 102400 | 30 | 0 | 104.06 | 1433.48 | 389.83 | 2447 | N/A | N/A |
|  XSLT Enhanced Proxy | 512M | 200 | 102400 | 100 | 0 | 108.21 | 1377.84 | 415.15 | 2383 | N/A | N/A |
|  XSLT Enhanced Proxy | 512M | 200 | 102400 | 500 | 0 | 111.58 | 1324.27 | 381.58 | 2287 | N/A | N/A |
|  XSLT Enhanced Proxy | 512M | 200 | 102400 | 1000 | 0 | 108.53 | 1359.13 | 203.82 | 1975 | N/A | N/A |
|  XSLT Proxy | 512M | 100 | 500 | 0 | 0 | 2189.41 | 34.29 | 80.96 | 467 | N/A | N/A |
|  XSLT Proxy | 512M | 100 | 500 | 30 | 0 | 1863.47 | 40.47 | 14.16 | 67 | N/A | N/A |
|  XSLT Proxy | 512M | 100 | 500 | 100 | 0 | 724.19 | 104.07 | 5.66 | 127 | N/A | N/A |
|  XSLT Proxy | 512M | 100 | 500 | 500 | 0 | 149.14 | 501.89 | 1.94 | 507 | N/A | N/A |
|  XSLT Proxy | 512M | 100 | 500 | 1000 | 0 | 73.88 | 1002.14 | 1.15 | 1007 | N/A | N/A |
|  XSLT Proxy | 512M | 100 | 1024 | 0 | 0 | 1765.3 | 42.7 | 106.91 | 599 | N/A | N/A |
|  XSLT Proxy | 512M | 100 | 1024 | 30 | 0 | 1727.95 | 43.63 | 24.43 | 73 | N/A | N/A |
|  XSLT Proxy | 512M | 100 | 1024 | 100 | 0 | 721.81 | 104.48 | 5.55 | 127 | N/A | N/A |
|  XSLT Proxy | 512M | 100 | 1024 | 500 | 0 | 149.16 | 502.22 | 1.76 | 507 | N/A | N/A |
|  XSLT Proxy | 512M | 100 | 1024 | 1000 | 0 | 74.08 | 1002.2 | 1.52 | 1015 | N/A | N/A |
|  XSLT Proxy | 512M | 100 | 10240 | 0 | 0 | 498.79 | 151.41 | 97.31 | 441 | N/A | N/A |
|  XSLT Proxy | 512M | 100 | 10240 | 30 | 0 | 497.16 | 151.92 | 76.56 | 407 | N/A | N/A |
|  XSLT Proxy | 512M | 100 | 10240 | 100 | 0 | 474.23 | 158.85 | 45.3 | 313 | N/A | N/A |
|  XSLT Proxy | 512M | 100 | 10240 | 500 | 0 | 147.55 | 507.18 | 3.68 | 523 | N/A | N/A |
|  XSLT Proxy | 512M | 100 | 10240 | 1000 | 0 | 73.75 | 1006.74 | 3.03 | 1023 | N/A | N/A |
|  XSLT Proxy | 512M | 100 | 102400 | 0 | 0 | 32.08 | 2306.44 | 675.71 | 3887 | N/A | N/A |
|  XSLT Proxy | 512M | 100 | 102400 | 30 | 0 | 30.29 | 2463.85 | 779.5 | 4351 | N/A | N/A |
|  XSLT Proxy | 512M | 100 | 102400 | 100 | 0 | 31.12 | 2361.44 | 644.76 | 3951 | N/A | N/A |
|  XSLT Proxy | 512M | 100 | 102400 | 500 | 0 | 32.92 | 2278.94 | 616.48 | 3535 | N/A | N/A |
|  XSLT Proxy | 512M | 100 | 102400 | 1000 | 0 | 29.2 | 2514.48 | 819.3 | 4639 | N/A | N/A |
|  XSLT Proxy | 512M | 200 | 500 | 0 | 0 | 1653.77 | 91.03 | 186.26 | 739 | N/A | N/A |
|  XSLT Proxy | 512M | 200 | 500 | 30 | 0 | 1917.22 | 78.88 | 118.45 | 667 | N/A | N/A |
|  XSLT Proxy | 512M | 200 | 500 | 100 | 0 | 1364.76 | 110 | 10.03 | 141 | N/A | N/A |
|  XSLT Proxy | 512M | 200 | 500 | 500 | 0 | 296.7 | 503.4 | 4.46 | 523 | N/A | N/A |
|  XSLT Proxy | 512M | 200 | 500 | 1000 | 0 | 147.64 | 1002.23 | 1.83 | 1015 | N/A | N/A |
|  XSLT Proxy | 512M | 200 | 1024 | 0 | 0 | 1682.32 | 88.96 | 160.7 | 623 | N/A | N/A |
|  XSLT Proxy | 512M | 200 | 1024 | 30 | 0 | 1785.12 | 84.45 | 99.39 | 663 | N/A | N/A |
|  XSLT Proxy | 512M | 200 | 1024 | 100 | 0 | 1321.57 | 112.09 | 13.74 | 158 | N/A | N/A |
|  XSLT Proxy | 512M | 200 | 1024 | 500 | 0 | 296.44 | 503.2 | 4.14 | 527 | N/A | N/A |
|  XSLT Proxy | 512M | 200 | 1024 | 1000 | 0 | 147.11 | 1002.48 | 2.68 | 1019 | N/A | N/A |
|  XSLT Proxy | 512M | 200 | 10240 | 0 | 0 | 438.29 | 343.03 | 232.24 | 1159 | N/A | N/A |
|  XSLT Proxy | 512M | 200 | 10240 | 30 | 0 | 433.49 | 347.12 | 195.07 | 927 | N/A | N/A |
|  XSLT Proxy | 512M | 200 | 10240 | 100 | 0 | 432.61 | 347.08 | 167.8 | 871 | N/A | N/A |
|  XSLT Proxy | 512M | 200 | 10240 | 500 | 0 | 285.77 | 520.49 | 19.87 | 591 | N/A | N/A |
|  XSLT Proxy | 512M | 200 | 10240 | 1000 | 0 | 146.54 | 1008.75 | 6.58 | 1039 | N/A | N/A |
|  XSLT Proxy | 512M | 200 | 102400 | 0 | 0 | 17.43 | 8386.34 | 3581.12 | 16255 | N/A | N/A |
|  XSLT Proxy | 512M | 200 | 102400 | 30 | 0 | 18.46 | 7814.88 | 3047.2 | 15103 | N/A | N/A |
|  XSLT Proxy | 512M | 200 | 102400 | 100 | 0 | 17.72 | 8352.04 | 3823.37 | 16895 | N/A | N/A |
|  XSLT Proxy | 512M | 200 | 102400 | 500 | 0 | 17.9 | 8084.98 | 3188.05 | 15359 | N/A | N/A |
|  XSLT Proxy | 512M | 200 | 102400 | 1000 | 0 | 18.4 | 7807.39 | 3319.32 | 16063 | N/A | N/A |
