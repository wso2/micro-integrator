# WSO2 Micro Integrator 1.2.0-Prometheus Performance Test Results

During each release, we execute various automated performance test scenarios and publish the results.

| Test Scenarios | Description |
| --- | --- |
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

1. **Throughput**: The number of requests that the WSO2 Micro Integrator 1.2.0-Prometheus processes during a specific time interval (e.g. per second).
2. **Response Time**: The end-to-end latency for an operation of invoking a service in WSO2 Micro Integrator 1.2.0-Prometheus . The complete distribution of response times was recorded.

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


System information for WSO2 Micro Integrator 1.2.0-Prometheus in 1st AWS CloudFormation stack.

| Class | Subclass | Description | Value |
| --- | --- | --- | --- |
| AWS | EC2 | AMI-ID | ami-0ac80df6eff0e70b5 |
| AWS | EC2 | Instance Type | c5.large |
| System | Processor | CPU(s) | 2 |
| System | Processor | Thread(s) per core | 2 |
| System | Processor | Core(s) per socket | 1 |
| System | Processor | Socket(s) | 1 |
| System | Processor | Model name | Intel(R) Xeon(R) Platinum 8124M CPU @ 3.00GHz |
| System | Memory | BIOS | 64 KiB |
| System | Memory | System memory | 3785420 KiB |
| System | Storage | Block Device: nvme0n1 | 8G |
| Operating System | Distribution | Release | Ubuntu 18.04.4 LTS |
| Operating System | Distribution | Kernel | Linux ip-10-0-1-245 5.3.0-1023-aws #25~18.04.1-Ubuntu SMP Fri Jun 5 15:18:30 UTC 2020 x86_64 x86_64 x86_64 GNU/Linux |


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

|  Scenario Name | Heap Size | Concurrent Users | Message Size (Bytes) | Back-end Service Delay (ms) | Error % | Throughput (Requests/sec) | Average Response Time (ms) | Standard Deviation of Response Time (ms) | 99th Percentile of Response Time (ms) | WSO2 Micro Integrator 1.2.0-Prometheus GC Throughput (%) | Average WSO2 Micro Integrator 1.2.0-Prometheus Memory Footprint After Full GC (M) |
|---|---:|---:|---:|---:|---:|---:|---:|---:|---:|---:|---:|
|  CBR SOAP Header Proxy | 512M | 100 | 500 | 0 | 0 | 2202.6 | 34.2 | 55.12 | 128 | 89.02 | 164.429 |
|  CBR SOAP Header Proxy | 512M | 100 | 500 | 30 | 0 | 1987.86 | 37.93 | 24.32 | 64 | N/A | N/A |
|  CBR SOAP Header Proxy | 512M | 100 | 500 | 100 | 0 | 723.72 | 104.16 | 16.46 | 135 | N/A | N/A |
|  CBR SOAP Header Proxy | 512M | 100 | 500 | 500 | 0 | 149.16 | 501.56 | 2.12 | 507 | N/A | N/A |
|  CBR SOAP Header Proxy | 512M | 100 | 500 | 1000 | 0 | 74.14 | 1002.08 | 1.03 | 1003 | N/A | N/A |
|  CBR SOAP Header Proxy | 512M | 100 | 1024 | 0 | 0 | 1894.78 | 39.77 | 105.56 | 751 | N/A | N/A |
|  CBR SOAP Header Proxy | 512M | 100 | 1024 | 30 | 0 | 1645.99 | 45.8 | 52.41 | 91 | N/A | N/A |
|  CBR SOAP Header Proxy | 512M | 100 | 1024 | 100 | 0 | 708.48 | 106.47 | 20.01 | 146 | N/A | N/A |
|  CBR SOAP Header Proxy | 512M | 100 | 1024 | 500 | 0 | 149.14 | 501.64 | 1.96 | 507 | N/A | N/A |
|  CBR SOAP Header Proxy | 512M | 100 | 1024 | 1000 | 0 | 74.05 | 1002.09 | 1.09 | 1003 | N/A | N/A |
|  CBR SOAP Header Proxy | 512M | 100 | 10240 | 0 | 0 | 949.16 | 78.37 | 52.04 | 199 | N/A | N/A |
|  CBR SOAP Header Proxy | 512M | 100 | 10240 | 30 | 0 | 888.04 | 84.95 | 47.34 | 166 | N/A | N/A |
|  CBR SOAP Header Proxy | 512M | 100 | 10240 | 100 | 0 | 583.42 | 129.25 | 23.61 | 205 | N/A | N/A |
|  CBR SOAP Header Proxy | 512M | 100 | 10240 | 500 | 0 | 148.42 | 505.39 | 6.07 | 535 | N/A | N/A |
|  CBR SOAP Header Proxy | 512M | 100 | 10240 | 1000 | 0 | 73.92 | 1002.74 | 3.73 | 1023 | N/A | N/A |
|  CBR SOAP Header Proxy | 512M | 100 | 102400 | 0 | 0 | 79.13 | 954.26 | 465.58 | 2351 | N/A | N/A |
|  CBR SOAP Header Proxy | 512M | 100 | 102400 | 30 | 0 | 79.43 | 948.46 | 472.15 | 2367 | N/A | N/A |
|  CBR SOAP Header Proxy | 512M | 100 | 102400 | 100 | 0 | 76.51 | 974.9 | 436.47 | 1863 | N/A | N/A |
|  CBR SOAP Header Proxy | 512M | 100 | 102400 | 500 | 0 | 63.81 | 1164.52 | 388.82 | 2159 | N/A | N/A |
|  CBR SOAP Header Proxy | 512M | 100 | 102400 | 1000 | 0 | 50.2 | 1452.45 | 375.37 | 2511 | N/A | N/A |
|  CBR SOAP Header Proxy | 512M | 200 | 500 | 0 | 0 | 1459.39 | 102.26 | 166.6 | 775 | N/A | N/A |
|  CBR SOAP Header Proxy | 512M | 200 | 500 | 30 | 0 | 2183.07 | 68.72 | 70.87 | 146 | N/A | N/A |
|  CBR SOAP Header Proxy | 512M | 200 | 500 | 100 | 0 | 1264.48 | 118.69 | 51.85 | 181 | N/A | N/A |
|  CBR SOAP Header Proxy | 512M | 200 | 500 | 500 | 0 | 296.43 | 502.39 | 5.02 | 535 | N/A | N/A |
|  CBR SOAP Header Proxy | 512M | 200 | 500 | 1000 | 0 | 147.08 | 1002.22 | 2.03 | 1015 | N/A | N/A |
|  CBR SOAP Header Proxy | 512M | 200 | 1024 | 0 | 0 | 2379.7 | 62.95 | 68.08 | 167 | N/A | N/A |
|  CBR SOAP Header Proxy | 512M | 200 | 1024 | 30 | 0 | 2219.97 | 66.93 | 58.88 | 129 | N/A | N/A |
|  CBR SOAP Header Proxy | 512M | 200 | 1024 | 100 | 0 | 1285.93 | 116.55 | 17.24 | 170 | N/A | N/A |
|  CBR SOAP Header Proxy | 512M | 200 | 1024 | 500 | 0 | 296.74 | 502.87 | 5.92 | 539 | N/A | N/A |
|  CBR SOAP Header Proxy | 512M | 200 | 1024 | 1000 | 0 | 145.92 | 1003.78 | 14.19 | 1031 | N/A | N/A |
|  CBR SOAP Header Proxy | 512M | 200 | 10240 | 0 | 0 | 986.89 | 152.36 | 87.76 | 499 | N/A | N/A |
|  CBR SOAP Header Proxy | 512M | 200 | 10240 | 30 | 0 | 973.37 | 154.45 | 93.56 | 719 | N/A | N/A |
|  CBR SOAP Header Proxy | 512M | 200 | 10240 | 100 | 0 | 857.1 | 175.22 | 72.8 | 355 | N/A | N/A |
|  CBR SOAP Header Proxy | 512M | 200 | 10240 | 500 | 0 | 291.65 | 510.01 | 11.8 | 559 | N/A | N/A |
|  CBR SOAP Header Proxy | 512M | 200 | 10240 | 1000 | 0 | 146.27 | 1005.16 | 8.43 | 1055 | N/A | N/A |
|  CBR SOAP Header Proxy | 512M | 200 | 102400 | 0 | 0 | 40.21 | 3651.07 | 1777.4 | 8319 | N/A | N/A |
|  CBR SOAP Header Proxy | 512M | 200 | 102400 | 30 | 0 | 40.3 | 3700.57 | 2040.42 | 8959 | N/A | N/A |
|  CBR SOAP Header Proxy | 512M | 200 | 102400 | 100 | 0 | 40.83 | 3632.45 | 1907.23 | 8703 | N/A | N/A |
|  CBR SOAP Header Proxy | 512M | 200 | 102400 | 500 | 0 | 38.38 | 3885.43 | 2130.73 | 9151 | N/A | N/A |
|  CBR SOAP Header Proxy | 512M | 200 | 102400 | 1000 | 0 | 33.19 | 4418.73 | 2436.26 | 9535 | N/A | N/A |
|  CBR Transport Header Proxy | 512M | 100 | 500 | 0 | 0 | 2203.36 | 34.15 | 86.24 | 695 | N/A | N/A |
|  CBR Transport Header Proxy | 512M | 100 | 500 | 30 | 0 | 1773.53 | 42.51 | 34.98 | 92 | N/A | N/A |
|  CBR Transport Header Proxy | 512M | 100 | 500 | 100 | 0 | 726.83 | 103.64 | 7.38 | 143 | N/A | N/A |
|  CBR Transport Header Proxy | 512M | 100 | 500 | 500 | 0 | 149.33 | 501.26 | 1.54 | 503 | N/A | N/A |
|  CBR Transport Header Proxy | 512M | 100 | 500 | 1000 | 0 | 74.15 | 1002.07 | 0.86 | 1003 | N/A | N/A |
|  CBR Transport Header Proxy | 512M | 100 | 1024 | 0 | 0 | 1933.11 | 39.23 | 139.99 | 739 | N/A | N/A |
|  CBR Transport Header Proxy | 512M | 100 | 1024 | 30 | 0 | 1784.85 | 42.25 | 44.81 | 90 | N/A | N/A |
|  CBR Transport Header Proxy | 512M | 100 | 1024 | 100 | 0 | 718.15 | 104.93 | 20.96 | 149 | N/A | N/A |
|  CBR Transport Header Proxy | 512M | 100 | 1024 | 500 | 0 | 149.11 | 501.47 | 1.94 | 507 | N/A | N/A |
|  CBR Transport Header Proxy | 512M | 100 | 1024 | 1000 | 0 | 74.13 | 1002.05 | 0.72 | 1003 | N/A | N/A |
|  CBR Transport Header Proxy | 512M | 100 | 10240 | 0 | 0 | 2041.71 | 36.82 | 77.51 | 164 | N/A | N/A |
|  CBR Transport Header Proxy | 512M | 100 | 10240 | 30 | 0 | 1648.11 | 45.69 | 30.43 | 102 | N/A | N/A |
|  CBR Transport Header Proxy | 512M | 100 | 10240 | 100 | 0 | 713.02 | 105.68 | 9.12 | 149 | N/A | N/A |
|  CBR Transport Header Proxy | 512M | 100 | 10240 | 500 | 0 | 148.95 | 501.76 | 1.62 | 505 | N/A | N/A |
|  CBR Transport Header Proxy | 512M | 100 | 10240 | 1000 | 0 | 74.15 | 1002.09 | 1.04 | 1007 | N/A | N/A |
|  CBR Transport Header Proxy | 512M | 100 | 102400 | 0 | 0 | 945.35 | 79.54 | 27.13 | 160 | N/A | N/A |
|  CBR Transport Header Proxy | 512M | 100 | 102400 | 30 | 0 | 874.72 | 85.9 | 30.97 | 165 | N/A | N/A |
|  CBR Transport Header Proxy | 512M | 100 | 102400 | 100 | 0 | 583.1 | 128.92 | 22.38 | 195 | N/A | N/A |
|  CBR Transport Header Proxy | 512M | 100 | 102400 | 500 | 0 | 148.44 | 504.31 | 2.68 | 515 | N/A | N/A |
|  CBR Transport Header Proxy | 512M | 100 | 102400 | 1000 | 0 | 73.74 | 1002.67 | 1.82 | 1007 | N/A | N/A |
|  CBR Transport Header Proxy | 512M | 200 | 500 | 0 | 0 | 2656.56 | 56.53 | 118.4 | 743 | N/A | N/A |
|  CBR Transport Header Proxy | 512M | 200 | 500 | 30 | 0 | 2410.16 | 62.5 | 87.2 | 126 | N/A | N/A |
|  CBR Transport Header Proxy | 512M | 200 | 500 | 100 | 0 | 1352.31 | 111.19 | 15.47 | 167 | N/A | N/A |
|  CBR Transport Header Proxy | 512M | 200 | 500 | 500 | 0 | 296.36 | 502.26 | 4.56 | 531 | N/A | N/A |
|  CBR Transport Header Proxy | 512M | 200 | 500 | 1000 | 0 | 147.01 | 1002.16 | 1.85 | 1007 | N/A | N/A |
|  CBR Transport Header Proxy | 512M | 200 | 1024 | 0 | 0 | 1600.78 | 93.96 | 185.09 | 795 | N/A | N/A |
|  CBR Transport Header Proxy | 512M | 200 | 1024 | 30 | 0 | 1749.27 | 85.93 | 176.07 | 815 | N/A | N/A |
|  CBR Transport Header Proxy | 512M | 200 | 1024 | 100 | 0 | 1344.05 | 111.67 | 15.38 | 166 | N/A | N/A |
|  CBR Transport Header Proxy | 512M | 200 | 1024 | 500 | 0 | 296.45 | 501.65 | 3.28 | 523 | N/A | N/A |
|  CBR Transport Header Proxy | 512M | 200 | 1024 | 1000 | 0 | 147 | 1002.22 | 2.08 | 1011 | N/A | N/A |
|  CBR Transport Header Proxy | 512M | 200 | 10240 | 0 | 0 | 2204.65 | 68.06 | 73.59 | 433 | N/A | N/A |
|  CBR Transport Header Proxy | 512M | 200 | 10240 | 30 | 0 | 2136.29 | 70.23 | 42.05 | 146 | N/A | N/A |
|  CBR Transport Header Proxy | 512M | 200 | 10240 | 100 | 0 | 1240.92 | 120.92 | 19.38 | 181 | N/A | N/A |
|  CBR Transport Header Proxy | 512M | 200 | 10240 | 500 | 0 | 297.06 | 502.44 | 4.56 | 531 | N/A | N/A |
|  CBR Transport Header Proxy | 512M | 200 | 10240 | 1000 | 0 | 147.25 | 1002.19 | 1.79 | 1011 | N/A | N/A |
|  CBR Transport Header Proxy | 512M | 200 | 102400 | 0 | 0 | 894.81 | 167.83 | 54.95 | 317 | N/A | N/A |
|  CBR Transport Header Proxy | 512M | 200 | 102400 | 30 | 0 | 920.9 | 162.96 | 48.42 | 291 | N/A | N/A |
|  CBR Transport Header Proxy | 512M | 200 | 102400 | 100 | 0 | 733 | 204.82 | 71.46 | 355 | N/A | N/A |
|  CBR Transport Header Proxy | 512M | 200 | 102400 | 500 | 0 | 291.99 | 510.41 | 11.4 | 559 | N/A | N/A |
|  CBR Transport Header Proxy | 512M | 200 | 102400 | 1000 | 0 | 146.32 | 1004.07 | 4.73 | 1015 | N/A | N/A |
|  XSLT Enhanced Proxy | 512M | 100 | 500 | 0 | 0 | 1885 | 40.03 | 80.88 | 263 | N/A | N/A |
|  XSLT Enhanced Proxy | 512M | 100 | 500 | 30 | 0 | 1519.08 | 49.6 | 36.38 | 99 | N/A | N/A |
|  XSLT Enhanced Proxy | 512M | 100 | 500 | 100 | 0 | 694.35 | 108.64 | 21.11 | 152 | N/A | N/A |
|  XSLT Enhanced Proxy | 512M | 100 | 500 | 500 | 0 | 148.88 | 502.16 | 2.42 | 515 | N/A | N/A |
|  XSLT Enhanced Proxy | 512M | 100 | 500 | 1000 | 0 | 74.01 | 1002.1 | 1.18 | 1007 | N/A | N/A |
|  XSLT Enhanced Proxy | 512M | 100 | 1024 | 0 | 0 | 1916.98 | 39.34 | 63.49 | 171 | N/A | N/A |
|  XSLT Enhanced Proxy | 512M | 100 | 1024 | 30 | 0 | 1596.73 | 47.22 | 27.25 | 86 | N/A | N/A |
|  XSLT Enhanced Proxy | 512M | 100 | 1024 | 100 | 0 | 688.46 | 109.33 | 11.24 | 148 | N/A | N/A |
|  XSLT Enhanced Proxy | 512M | 100 | 1024 | 500 | 0 | 149.11 | 502.23 | 3.06 | 519 | N/A | N/A |
|  XSLT Enhanced Proxy | 512M | 100 | 1024 | 1000 | 0 | 74.09 | 1002.19 | 1.65 | 1011 | N/A | N/A |
|  XSLT Enhanced Proxy | 512M | 100 | 10240 | 0 | 0 | 503.53 | 149.83 | 61.75 | 317 | N/A | N/A |
|  XSLT Enhanced Proxy | 512M | 100 | 10240 | 30 | 0 | 484.21 | 155.65 | 61.3 | 323 | N/A | N/A |
|  XSLT Enhanced Proxy | 512M | 100 | 10240 | 100 | 0 | 424.02 | 177.47 | 40.68 | 293 | N/A | N/A |
|  XSLT Enhanced Proxy | 512M | 100 | 10240 | 500 | 0 | 147 | 507.67 | 7.56 | 539 | N/A | N/A |
|  XSLT Enhanced Proxy | 512M | 100 | 10240 | 1000 | 0 | 73.7 | 1004.29 | 4.01 | 1031 | N/A | N/A |
|  XSLT Enhanced Proxy | 512M | 100 | 102400 | 0 | 0 | 64.08 | 1177.14 | 365.92 | 2111 | N/A | N/A |
|  XSLT Enhanced Proxy | 512M | 100 | 102400 | 30 | 0 | 65.06 | 1143.04 | 343.19 | 2007 | N/A | N/A |
|  XSLT Enhanced Proxy | 512M | 100 | 102400 | 100 | 0 | 65.15 | 1148.8 | 327.84 | 2023 | N/A | N/A |
|  XSLT Enhanced Proxy | 512M | 100 | 102400 | 500 | 0 | 63.85 | 1172.87 | 258.99 | 1991 | N/A | N/A |
|  XSLT Enhanced Proxy | 512M | 100 | 102400 | 1000 | 0 | 53.02 | 1382.4 | 159.42 | 1767 | N/A | N/A |
|  XSLT Enhanced Proxy | 512M | 200 | 500 | 0 | 0 | 1526.47 | 98.53 | 153.98 | 831 | N/A | N/A |
|  XSLT Enhanced Proxy | 512M | 200 | 500 | 30 | 0 | 1800.74 | 83.33 | 84.72 | 244 | N/A | N/A |
|  XSLT Enhanced Proxy | 512M | 200 | 500 | 100 | 0 | 1201.08 | 124.75 | 51.29 | 186 | N/A | N/A |
|  XSLT Enhanced Proxy | 512M | 200 | 500 | 500 | 0 | 296.55 | 502.67 | 4.68 | 531 | N/A | N/A |
|  XSLT Enhanced Proxy | 512M | 200 | 500 | 1000 | 0 | 147.26 | 1002.34 | 2.75 | 1019 | N/A | N/A |
|  XSLT Enhanced Proxy | 512M | 200 | 1024 | 0 | 0 | 1627.13 | 92.31 | 116.83 | 735 | N/A | N/A |
|  XSLT Enhanced Proxy | 512M | 200 | 1024 | 30 | 0 | 1808.05 | 83.07 | 36.96 | 213 | N/A | N/A |
|  XSLT Enhanced Proxy | 512M | 200 | 1024 | 100 | 0 | 1169.55 | 128.36 | 50.35 | 196 | N/A | N/A |
|  XSLT Enhanced Proxy | 512M | 200 | 1024 | 500 | 0 | 295.72 | 503.09 | 5.19 | 535 | N/A | N/A |
|  XSLT Enhanced Proxy | 512M | 200 | 1024 | 1000 | 0 | 147.17 | 1002.4 | 2.9 | 1019 | N/A | N/A |
|  XSLT Enhanced Proxy | 512M | 200 | 10240 | 0 | 0 | 498.97 | 301.45 | 103.31 | 559 | N/A | N/A |
|  XSLT Enhanced Proxy | 512M | 200 | 10240 | 30 | 0 | 497.63 | 302.01 | 97.11 | 571 | N/A | N/A |
|  XSLT Enhanced Proxy | 512M | 200 | 10240 | 100 | 0 | 486 | 309.73 | 90.43 | 563 | N/A | N/A |
|  XSLT Enhanced Proxy | 512M | 200 | 10240 | 500 | 0 | 266.96 | 558.44 | 57.09 | 735 | N/A | N/A |
|  XSLT Enhanced Proxy | 512M | 200 | 10240 | 1000 | 0 | 145.85 | 1008.1 | 7.98 | 1039 | N/A | N/A |
|  XSLT Enhanced Proxy | 512M | 200 | 102400 | 0 | 0 | 60.1 | 2459.63 | 705.87 | 4127 | N/A | N/A |
|  XSLT Enhanced Proxy | 512M | 200 | 102400 | 30 | 0 | 61.68 | 2424.46 | 657.85 | 3999 | N/A | N/A |
|  XSLT Enhanced Proxy | 512M | 200 | 102400 | 100 | 0 | 60.27 | 2436.96 | 647.74 | 3935 | N/A | N/A |
|  XSLT Enhanced Proxy | 512M | 200 | 102400 | 500 | 0 | 61.69 | 2367.92 | 612.37 | 4015 | N/A | N/A |
|  XSLT Enhanced Proxy | 512M | 200 | 102400 | 1000 | 0 | 61.12 | 2376.53 | 570.42 | 3983 | N/A | N/A |
