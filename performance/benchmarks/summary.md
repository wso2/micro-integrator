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
| Concurrent Users | The number of users accessing the application at the same time. | 100 |
| Message Size (Bytes) | The request payload size in Bytes. | 500, 1024, 10240, 102400 |
| Back-end Delay (ms) | The delay added by the Back-end service. | 0, 30, 100, 500, 1000 |

The duration of each test is **60 seconds**. The warm-up period is **30 seconds**.
The measurement results are collected after the warm-up period.

The performance tests were executed on 1 AWS CloudFormation stack.


System information for WSO2 Micro Integrator 1.2.0-alpha in 1st AWS CloudFormation stack.

| Class | Subclass | Description | Value |
| --- | --- | --- | --- |
| AWS | EC2 | AMI-ID | ami-05801d0a3c8e4c443 |
| AWS | EC2 | Instance Type | c5.large |
| System | Processor | CPU(s) | 2 |
| System | Processor | Thread(s) per core | 2 |
| System | Processor | Core(s) per socket | 1 |
| System | Processor | Socket(s) | 1 |
| System | Processor | Model name | Intel(R) Xeon(R) Platinum 8124M CPU @ 3.00GHz |
| System | Memory | BIOS | 64 KiB |
| System | Memory | System memory | 3785428 KiB |
| System | Storage | Block Device: nvme0n1 | 8G |
| Operating System | Distribution | Release | Ubuntu 18.04.4 LTS |
| Operating System | Distribution | Kernel | Linux ip-10-0-1-64 5.3.0-1017-aws #18~18.04.1-Ubuntu SMP Wed Apr 8 15:12:16 UTC 2020 x86_64 x86_64 x86_64 GNU/Linux |


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
|  CBR Proxy | 512M | 100 | 500 | 0 | 0 | 1826.16 | 41.3 | 99.31 | 723 | N/A | N/A |
|  CBR Proxy | 512M | 100 | 500 | 30 | 0 | 1702.52 | 44.28 | 28.14 | 93 | N/A | N/A |
|  CBR Proxy | 512M | 100 | 500 | 100 | 0 | 718.58 | 104.92 | 9.04 | 145 | N/A | N/A |
|  CBR Proxy | 512M | 100 | 500 | 500 | 0 | 149.28 | 501.46 | 1.78 | 505 | N/A | N/A |
|  CBR Proxy | 512M | 100 | 500 | 1000 | 0 | 74.17 | 1002.1 | 1.18 | 1003 | N/A | N/A |
|  CBR Proxy | 512M | 100 | 1024 | 0 | 0 | 1960.26 | 38.44 | 72.01 | 132 | N/A | N/A |
|  CBR Proxy | 512M | 100 | 1024 | 30 | 0 | 1647.04 | 45.79 | 28.37 | 93 | N/A | N/A |
|  CBR Proxy | 512M | 100 | 1024 | 100 | 0 | 706.64 | 106.6 | 10.47 | 149 | N/A | N/A |
|  CBR Proxy | 512M | 100 | 1024 | 500 | 0 | 148.92 | 501.55 | 2.09 | 509 | N/A | N/A |
|  CBR Proxy | 512M | 100 | 1024 | 1000 | 0 | 74.11 | 1002.08 | 0.9 | 1007 | N/A | N/A |
|  CBR Proxy | 512M | 100 | 10240 | 0 | 0 | 656.98 | 113.34 | 63.82 | 275 | N/A | N/A |
|  CBR Proxy | 512M | 100 | 10240 | 30 | 0 | 639.46 | 117.84 | 42.51 | 238 | N/A | N/A |
|  CBR Proxy | 512M | 100 | 10240 | 100 | 0 | 501.98 | 150.32 | 32.72 | 254 | N/A | N/A |
|  CBR Proxy | 512M | 100 | 10240 | 500 | 0 | 147.42 | 507.56 | 8.71 | 547 | N/A | N/A |
|  CBR Proxy | 512M | 100 | 10240 | 1000 | 0 | 74.01 | 1003.46 | 4.81 | 1039 | N/A | N/A |
|  CBR Proxy | 512M | 100 | 102400 | 0 | 0 | 40.12 | 1890.84 | 771.2 | 3711 | N/A | N/A |
|  CBR Proxy | 512M | 100 | 102400 | 30 | 0 | 39.36 | 1921.32 | 832.38 | 3887 | N/A | N/A |
|  CBR Proxy | 512M | 100 | 102400 | 100 | 0 | 39.22 | 1913.2 | 740.21 | 3679 | N/A | N/A |
|  CBR Proxy | 512M | 100 | 102400 | 500 | 0 | 40.84 | 1828.45 | 716.8 | 3775 | N/A | N/A |
|  CBR Proxy | 512M | 100 | 102400 | 1000 | 0 | 33.06 | 2288.89 | 848.93 | 5055 | N/A | N/A |
|  CBR SOAP Header Proxy | 512M | 100 | 500 | 0 | 0 | 1914.79 | 39.41 | 110.47 | 735 | N/A | N/A |
|  CBR SOAP Header Proxy | 512M | 100 | 500 | 30 | 0 | 1729.81 | 43.57 | 45.9 | 90 | N/A | N/A |
|  CBR SOAP Header Proxy | 512M | 100 | 500 | 100 | 0 | 712.9 | 105.66 | 18.11 | 146 | N/A | N/A |
|  CBR SOAP Header Proxy | 512M | 100 | 500 | 500 | 0 | 149.19 | 501.32 | 1.79 | 505 | N/A | N/A |
|  CBR SOAP Header Proxy | 512M | 100 | 500 | 1000 | 0 | 74.14 | 1002.12 | 1.25 | 1007 | N/A | N/A |
|  CBR SOAP Header Proxy | 512M | 100 | 1024 | 0 | 0 | 1924.52 | 39.14 | 88.61 | 695 | N/A | N/A |
|  CBR SOAP Header Proxy | 512M | 100 | 1024 | 30 | 0 | 1684.32 | 44.77 | 31.87 | 94 | N/A | N/A |
|  CBR SOAP Header Proxy | 512M | 100 | 1024 | 100 | 0 | 710.75 | 106.15 | 14.65 | 150 | N/A | N/A |
|  CBR SOAP Header Proxy | 512M | 100 | 1024 | 500 | 0 | 149 | 501.38 | 1.84 | 505 | N/A | N/A |
|  CBR SOAP Header Proxy | 512M | 100 | 1024 | 1000 | 0 | 74.12 | 1002.08 | 1.03 | 1003 | N/A | N/A |
|  CBR SOAP Header Proxy | 512M | 100 | 10240 | 0 | 0 | 959.89 | 78.56 | 45.08 | 185 | N/A | N/A |
|  CBR SOAP Header Proxy | 512M | 100 | 10240 | 30 | 0 | 895.85 | 84.18 | 32.77 | 168 | N/A | N/A |
|  CBR SOAP Header Proxy | 512M | 100 | 10240 | 100 | 0 | 584.95 | 128.75 | 22.41 | 200 | N/A | N/A |
|  CBR SOAP Header Proxy | 512M | 100 | 10240 | 500 | 0 | 148.29 | 504.47 | 5.54 | 535 | N/A | N/A |
|  CBR SOAP Header Proxy | 512M | 100 | 10240 | 1000 | 0 | 73.73 | 1002.93 | 4.33 | 1031 | N/A | N/A |
|  CBR SOAP Header Proxy | 512M | 100 | 102400 | 0 | 0 | 76.75 | 984.93 | 470.72 | 2335 | N/A | N/A |
|  CBR SOAP Header Proxy | 512M | 100 | 102400 | 30 | 0 | 76.83 | 976.44 | 495.51 | 2479 | N/A | N/A |
|  CBR SOAP Header Proxy | 512M | 100 | 102400 | 100 | 0 | 76.48 | 980.52 | 461.69 | 2431 | N/A | N/A |
|  CBR SOAP Header Proxy | 512M | 100 | 102400 | 500 | 0 | 64.15 | 1165.79 | 399.45 | 2079 | N/A | N/A |
|  CBR SOAP Header Proxy | 512M | 100 | 102400 | 1000 | 0 | 51.4 | 1456.33 | 339.13 | 2319 | N/A | N/A |
|  CBR Transport Header Proxy | 512M | 100 | 500 | 0 | 0 | 2487.57 | 30.57 | 100.77 | 691 | N/A | N/A |
|  CBR Transport Header Proxy | 512M | 100 | 500 | 30 | 0 | 1832.87 | 41.16 | 39.73 | 87 | N/A | N/A |
|  CBR Transport Header Proxy | 512M | 100 | 500 | 100 | 0 | 717.27 | 105.07 | 22.26 | 148 | N/A | N/A |
|  CBR Transport Header Proxy | 512M | 100 | 500 | 500 | 0 | 149.24 | 501.23 | 1.46 | 503 | N/A | N/A |
|  CBR Transport Header Proxy | 512M | 100 | 500 | 1000 | 0 | 74.12 | 1002.07 | 0.93 | 1003 | N/A | N/A |
|  CBR Transport Header Proxy | 512M | 100 | 1024 | 0 | 0 | 1800.23 | 41.88 | 116.75 | 743 | N/A | N/A |
|  CBR Transport Header Proxy | 512M | 100 | 1024 | 30 | 0 | 1810.08 | 41.67 | 30.8 | 92 | N/A | N/A |
|  CBR Transport Header Proxy | 512M | 100 | 1024 | 100 | 0 | 729.71 | 103.3 | 7.06 | 143 | N/A | N/A |
|  CBR Transport Header Proxy | 512M | 100 | 1024 | 500 | 0 | 149.3 | 501.19 | 1.25 | 503 | N/A | N/A |
|  CBR Transport Header Proxy | 512M | 100 | 1024 | 1000 | 0 | 74.12 | 1002.05 | 0.76 | 1003 | N/A | N/A |
|  CBR Transport Header Proxy | 512M | 100 | 10240 | 0 | 0 | 2364.45 | 31.84 | 54.44 | 106 | N/A | N/A |
|  CBR Transport Header Proxy | 512M | 100 | 10240 | 30 | 0 | 1704.37 | 44.18 | 35.5 | 91 | N/A | N/A |
|  CBR Transport Header Proxy | 512M | 100 | 10240 | 100 | 0 | 709.19 | 106.22 | 28.75 | 149 | N/A | N/A |
|  CBR Transport Header Proxy | 512M | 100 | 10240 | 500 | 0 | 149.28 | 501.47 | 1.94 | 505 | N/A | N/A |
|  CBR Transport Header Proxy | 512M | 100 | 10240 | 1000 | 0 | 74.11 | 1002.06 | 0.81 | 1003 | N/A | N/A |
|  CBR Transport Header Proxy | 512M | 100 | 102400 | 0 | 0 | 919.83 | 81.82 | 45.2 | 159 | N/A | N/A |
|  CBR Transport Header Proxy | 512M | 100 | 102400 | 30 | 0 | 868.16 | 86.57 | 40.86 | 166 | N/A | N/A |
|  CBR Transport Header Proxy | 512M | 100 | 102400 | 100 | 0 | 575.77 | 130.53 | 23.63 | 206 | N/A | N/A |
|  CBR Transport Header Proxy | 512M | 100 | 102400 | 500 | 0 | 148.25 | 504.12 | 2.34 | 515 | N/A | N/A |
|  CBR Transport Header Proxy | 512M | 100 | 102400 | 1000 | 0 | 73.92 | 1002.47 | 1.71 | 1007 | N/A | N/A |
|  Direct Proxy | 512M | 100 | 500 | 0 | 0 | 2048.39 | 36.84 | 85.63 | 771 | 77.37 | 269.947 |
|  Direct Proxy | 512M | 100 | 500 | 30 | 0 | 1870.71 | 40.28 | 37.15 | 84 | N/A | N/A |
|  Direct Proxy | 512M | 100 | 500 | 100 | 0 | 716.06 | 105.22 | 23.93 | 147 | N/A | N/A |
|  Direct Proxy | 512M | 100 | 500 | 500 | 0 | 149.31 | 501.26 | 1.46 | 503 | N/A | N/A |
|  Direct Proxy | 512M | 100 | 500 | 1000 | 0 | 74.1 | 1002.04 | 0.7 | 1003 | N/A | N/A |
|  Direct Proxy | 512M | 100 | 1024 | 0 | 0 | 1554.5 | 48.51 | 143.56 | 803 | N/A | N/A |
|  Direct Proxy | 512M | 100 | 1024 | 30 | 0 | 1806.88 | 41.72 | 47.32 | 92 | N/A | N/A |
|  Direct Proxy | 512M | 100 | 1024 | 100 | 0 | 726.49 | 103.79 | 10.62 | 144 | N/A | N/A |
|  Direct Proxy | 512M | 100 | 1024 | 500 | 0 | 149.08 | 501.21 | 1.2 | 503 | N/A | N/A |
|  Direct Proxy | 512M | 100 | 1024 | 1000 | 0 | 74.14 | 1002.04 | 0.63 | 1003 | N/A | N/A |
|  Direct Proxy | 512M | 100 | 10240 | 0 | 0 | 2057.26 | 36.63 | 72.56 | 127 | N/A | N/A |
|  Direct Proxy | 512M | 100 | 10240 | 30 | 0 | 1690.16 | 44.57 | 38.73 | 95 | N/A | N/A |
|  Direct Proxy | 512M | 100 | 10240 | 100 | 0 | 721.36 | 104.51 | 7.24 | 141 | N/A | N/A |
|  Direct Proxy | 512M | 100 | 10240 | 500 | 0 | 149.12 | 501.54 | 1.85 | 503 | N/A | N/A |
|  Direct Proxy | 512M | 100 | 10240 | 1000 | 0 | 74.11 | 1002.08 | 0.87 | 1007 | N/A | N/A |
|  Direct Proxy | 512M | 100 | 102400 | 0 | 0 | 947.83 | 79.38 | 27.86 | 159 | N/A | N/A |
|  Direct Proxy | 512M | 100 | 102400 | 30 | 0 | 898.64 | 83.63 | 24.87 | 157 | N/A | N/A |
|  Direct Proxy | 512M | 100 | 102400 | 100 | 0 | 571.67 | 131.66 | 24.02 | 204 | N/A | N/A |
|  Direct Proxy | 512M | 100 | 102400 | 500 | 0 | 148.02 | 504.95 | 4.39 | 527 | N/A | N/A |
|  Direct Proxy | 512M | 100 | 102400 | 1000 | 0 | 73.92 | 1002.49 | 1.55 | 1007 | N/A | N/A |
|  XSLT Enhanced Proxy | 512M | 100 | 500 | 0 | 0 | 1667.91 | 45.25 | 89.65 | 323 | N/A | N/A |
|  XSLT Enhanced Proxy | 512M | 100 | 500 | 30 | 0 | 1538.23 | 49.02 | 27.95 | 97 | N/A | N/A |
|  XSLT Enhanced Proxy | 512M | 100 | 500 | 100 | 0 | 702.58 | 107.28 | 9.93 | 146 | N/A | N/A |
|  XSLT Enhanced Proxy | 512M | 100 | 500 | 500 | 0 | 148.96 | 501.78 | 2.51 | 519 | N/A | N/A |
|  XSLT Enhanced Proxy | 512M | 100 | 500 | 1000 | 0 | 74.08 | 1002.14 | 1.38 | 1007 | N/A | N/A |
|  XSLT Enhanced Proxy | 512M | 100 | 1024 | 0 | 0 | 1909.55 | 39.52 | 75.67 | 170 | N/A | N/A |
|  XSLT Enhanced Proxy | 512M | 100 | 1024 | 30 | 0 | 1511.03 | 49.88 | 19.99 | 96 | N/A | N/A |
|  XSLT Enhanced Proxy | 512M | 100 | 1024 | 100 | 0 | 699.69 | 107.7 | 10.83 | 149 | N/A | N/A |
|  XSLT Enhanced Proxy | 512M | 100 | 1024 | 500 | 0 | 148.91 | 501.82 | 2.52 | 515 | N/A | N/A |
|  XSLT Enhanced Proxy | 512M | 100 | 1024 | 1000 | 0 | 74.01 | 1002.14 | 1.41 | 1007 | N/A | N/A |
|  XSLT Enhanced Proxy | 512M | 100 | 10240 | 0 | 0 | 490.21 | 153.97 | 62.83 | 325 | N/A | N/A |
|  XSLT Enhanced Proxy | 512M | 100 | 10240 | 30 | 0 | 476.58 | 158.34 | 55.26 | 315 | N/A | N/A |
|  XSLT Enhanced Proxy | 512M | 100 | 10240 | 100 | 0 | 415.9 | 180.99 | 45.87 | 311 | N/A | N/A |
|  XSLT Enhanced Proxy | 512M | 100 | 10240 | 500 | 0 | 147.11 | 507.4 | 6.73 | 535 | N/A | N/A |
|  XSLT Enhanced Proxy | 512M | 100 | 10240 | 1000 | 0 | 73.7 | 1004.26 | 4.75 | 1031 | N/A | N/A |
|  XSLT Enhanced Proxy | 512M | 100 | 102400 | 0 | 0 | 61.82 | 1206.47 | 336.19 | 2047 | N/A | N/A |
|  XSLT Enhanced Proxy | 512M | 100 | 102400 | 30 | 0 | 64.08 | 1162.49 | 323.99 | 1879 | N/A | N/A |
|  XSLT Enhanced Proxy | 512M | 100 | 102400 | 100 | 0 | 64.15 | 1161.67 | 316.07 | 2015 | N/A | N/A |
|  XSLT Enhanced Proxy | 512M | 100 | 102400 | 500 | 0 | 60.55 | 1226.28 | 255.38 | 1871 | N/A | N/A |
|  XSLT Enhanced Proxy | 512M | 100 | 102400 | 1000 | 0 | 54.29 | 1355.02 | 155.1 | 1719 | N/A | N/A |
|  XSLT Proxy | 512M | 100 | 500 | 0 | 0 | 1621.27 | 46.5 | 45.87 | 156 | N/A | N/A |
|  XSLT Proxy | 512M | 100 | 500 | 30 | 0 | 1321.1 | 57.18 | 49.51 | 109 | N/A | N/A |
|  XSLT Proxy | 512M | 100 | 500 | 100 | 0 | 684.86 | 110.06 | 11.74 | 151 | N/A | N/A |
|  XSLT Proxy | 512M | 100 | 500 | 500 | 0 | 149.16 | 502.17 | 3.03 | 523 | N/A | N/A |
|  XSLT Proxy | 512M | 100 | 500 | 1000 | 0 | 74.13 | 1002.19 | 1.79 | 1011 | N/A | N/A |
|  XSLT Proxy | 512M | 100 | 1024 | 0 | 0 | 1428.32 | 52.83 | 43.55 | 178 | N/A | N/A |
|  XSLT Proxy | 512M | 100 | 1024 | 30 | 0 | 1288.66 | 58.52 | 54.43 | 120 | N/A | N/A |
|  XSLT Proxy | 512M | 100 | 1024 | 100 | 0 | 674.52 | 111.69 | 11.43 | 148 | N/A | N/A |
|  XSLT Proxy | 512M | 100 | 1024 | 500 | 0 | 148.76 | 503.17 | 3.93 | 527 | N/A | N/A |
|  XSLT Proxy | 512M | 100 | 1024 | 1000 | 0 | 74.05 | 1002.27 | 2.17 | 1015 | N/A | N/A |
|  XSLT Proxy | 512M | 100 | 10240 | 0 | 0 | 299.73 | 251.54 | 148.34 | 723 | N/A | N/A |
|  XSLT Proxy | 512M | 100 | 10240 | 30 | 0 | 299.96 | 251.6 | 130.1 | 707 | N/A | N/A |
|  XSLT Proxy | 512M | 100 | 10240 | 100 | 0 | 299.66 | 251.59 | 99.03 | 599 | N/A | N/A |
|  XSLT Proxy | 512M | 100 | 10240 | 500 | 0 | 141.48 | 527.76 | 38.01 | 675 | N/A | N/A |
|  XSLT Proxy | 512M | 100 | 10240 | 1000 | 0 | 73.5 | 1007.53 | 4.65 | 1039 | N/A | N/A |
|  XSLT Proxy | 512M | 100 | 102400 | 0 | 0 | 19.07 | 3799.88 | 1084.83 | 6431 | N/A | N/A |
|  XSLT Proxy | 512M | 100 | 102400 | 30 | 0 | 17.52 | 4102.55 | 1336 | 6847 | N/A | N/A |
|  XSLT Proxy | 512M | 100 | 102400 | 100 | 0 | 17.89 | 4067.12 | 1184.04 | 6911 | N/A | N/A |
|  XSLT Proxy | 512M | 100 | 102400 | 500 | 0 | 17.37 | 4163.73 | 1504.44 | 7775 | N/A | N/A |
|  XSLT Proxy | 512M | 100 | 102400 | 1000 | 0 | 16.93 | 4300.38 | 1303.12 | 7903 | N/A | N/A |
