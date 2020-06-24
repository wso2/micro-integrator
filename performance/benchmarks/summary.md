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
| Operating System | Distribution | Kernel | Linux ip-10-0-1-234 5.3.0-1023-aws #25~18.04.1-Ubuntu SMP Fri Jun 5 15:18:30 UTC 2020 x86_64 x86_64 x86_64 GNU/Linux |


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
|  CBR Proxy | 512M | 100 | 500 | 0 | 0 | 1762.4 | 42.8 | 114.85 | 755 | N/A | N/A |
|  CBR Proxy | 512M | 100 | 500 | 30 | 0 | 1713.61 | 44.02 | 23.08 | 94 | N/A | N/A |
|  CBR Proxy | 512M | 100 | 500 | 100 | 0 | 699.63 | 107.6 | 14.76 | 150 | N/A | N/A |
|  CBR Proxy | 512M | 100 | 500 | 500 | 0 | 149 | 501.54 | 1.73 | 505 | N/A | N/A |
|  CBR Proxy | 512M | 100 | 500 | 1000 | 0 | 74.11 | 1002.11 | 1.1 | 1007 | N/A | N/A |
|  CBR Proxy | 512M | 100 | 1024 | 0 | 0 | 2039.71 | 36.91 | 65.71 | 123 | N/A | N/A |
|  CBR Proxy | 512M | 100 | 1024 | 30 | 0 | 1626.27 | 46.41 | 30.16 | 97 | N/A | N/A |
|  CBR Proxy | 512M | 100 | 1024 | 100 | 0 | 707.54 | 106.53 | 10.5 | 149 | N/A | N/A |
|  CBR Proxy | 512M | 100 | 1024 | 500 | 0 | 148.94 | 501.73 | 2.09 | 509 | N/A | N/A |
|  CBR Proxy | 512M | 100 | 1024 | 1000 | 0 | 74.09 | 1002.11 | 1.1 | 1007 | N/A | N/A |
|  CBR Proxy | 512M | 100 | 10240 | 0 | 0.01 | 139.48 | 119.92 | 860.06 | 281 | N/A | N/A |
|  CBR Proxy | 512M | 100 | 10240 | 30 | 0 | 638.9 | 118.13 | 44.09 | 239 | N/A | N/A |
|  CBR Proxy | 512M | 100 | 10240 | 100 | 0 | 495.81 | 152.07 | 33.35 | 249 | N/A | N/A |
|  CBR Proxy | 512M | 100 | 10240 | 500 | 0 | 147.61 | 507.97 | 8.85 | 547 | N/A | N/A |
|  CBR Proxy | 512M | 100 | 10240 | 1000 | 0 | 73.84 | 1004.18 | 5.54 | 1039 | N/A | N/A |
|  CBR Proxy | 512M | 100 | 102400 | 0 | 0 | 40.27 | 1872.92 | 810.56 | 3711 | N/A | N/A |
|  CBR Proxy | 512M | 100 | 102400 | 30 | 0 | 40.76 | 1858.88 | 741.02 | 3711 | N/A | N/A |
|  CBR Proxy | 512M | 100 | 102400 | 100 | 0 | 40.89 | 1809.25 | 781.39 | 3631 | N/A | N/A |
|  CBR Proxy | 512M | 100 | 102400 | 500 | 0 | 40.34 | 1850.73 | 663.45 | 3647 | N/A | N/A |
|  CBR Proxy | 512M | 100 | 102400 | 1000 | 0 | 31.7 | 2311.65 | 1022.93 | 4671 | N/A | N/A |
|  CBR Proxy | 512M | 200 | 500 | 0 | 0 | 1895.8 | 79.19 | 109.38 | 747 | N/A | N/A |
|  CBR Proxy | 512M | 200 | 500 | 30 | 0 | 2236.81 | 67.2 | 37.75 | 132 | N/A | N/A |
|  CBR Proxy | 512M | 200 | 500 | 100 | 0 | 1242.38 | 120.77 | 59.22 | 178 | N/A | N/A |
|  CBR Proxy | 512M | 200 | 500 | 500 | 0 | 296.61 | 502.54 | 5.46 | 539 | N/A | N/A |
|  CBR Proxy | 512M | 200 | 500 | 1000 | 0 | 146.8 | 1002.23 | 1.9 | 1011 | N/A | N/A |
|  CBR Proxy | 512M | 200 | 1024 | 0 | 0 | 2167.48 | 69.41 | 119.31 | 779 | N/A | N/A |
|  CBR Proxy | 512M | 200 | 1024 | 30 | 0 | 2059.3 | 73.05 | 34.99 | 143 | N/A | N/A |
|  CBR Proxy | 512M | 200 | 1024 | 100 | 0 | 1252.58 | 119.69 | 18.89 | 176 | N/A | N/A |
|  CBR Proxy | 512M | 200 | 1024 | 500 | 0 | 296.24 | 503.38 | 6.38 | 543 | N/A | N/A |
|  CBR Proxy | 512M | 200 | 1024 | 1000 | 0 | 147.25 | 1002.28 | 2.47 | 1015 | N/A | N/A |
|  CBR Proxy | 512M | 200 | 10240 | 0 | 0 | 645.39 | 231.82 | 111.55 | 691 | N/A | N/A |
|  CBR Proxy | 512M | 200 | 10240 | 30 | 0 | 629.85 | 239.72 | 130.39 | 831 | N/A | N/A |
|  CBR Proxy | 512M | 200 | 10240 | 100 | 0 | 616.81 | 243.49 | 91.74 | 659 | N/A | N/A |
|  CBR Proxy | 512M | 200 | 10240 | 500 | 0 | 273.54 | 545.64 | 52.12 | 735 | N/A | N/A |
|  CBR Proxy | 512M | 200 | 10240 | 1000 | 0 | 145.49 | 1020.56 | 42.04 | 1183 | N/A | N/A |
|  CBR Proxy | 512M | 200 | 102400 | 0 | 0 | 15.33 | 9591.79 | 5023.31 | 18687 | N/A | N/A |
|  CBR Proxy | 512M | 200 | 102400 | 30 | 0 | 15.48 | 9742.93 | 5427.24 | 21375 | N/A | N/A |
|  CBR Proxy | 512M | 200 | 102400 | 100 | 0 | 16.86 | 8926.12 | 4401.21 | 18303 | N/A | N/A |
|  CBR Proxy | 512M | 200 | 102400 | 500 | 0 | 12.36 | 11861.09 | 5875.79 | 24191 | N/A | N/A |
|  CBR Proxy | 512M | 200 | 102400 | 1000 | 0 | 12.62 | 11451.71 | 4446.64 | 20863 | N/A | N/A |
|  CBR SOAP Header Proxy | 512M | 100 | 500 | 0 | 0 | 2093.94 | 36.25 | 100.19 | 723 | N/A | N/A |
|  CBR SOAP Header Proxy | 512M | 100 | 500 | 30 | 0 | 1695.66 | 44.48 | 43.39 | 91 | N/A | N/A |
|  CBR SOAP Header Proxy | 512M | 100 | 500 | 100 | 0 | 707.3 | 106.55 | 22.32 | 150 | N/A | N/A |
|  CBR SOAP Header Proxy | 512M | 100 | 500 | 500 | 0 | 149.07 | 501.67 | 3.78 | 505 | N/A | N/A |
|  CBR SOAP Header Proxy | 512M | 100 | 500 | 1000 | 0 | 74.15 | 1002.11 | 1.11 | 1007 | N/A | N/A |
|  CBR SOAP Header Proxy | 512M | 100 | 1024 | 0 | 0 | 1466.81 | 51.46 | 129.7 | 755 | N/A | N/A |
|  CBR SOAP Header Proxy | 512M | 100 | 1024 | 30 | 0 | 1638.32 | 46.03 | 50.97 | 93 | N/A | N/A |
|  CBR SOAP Header Proxy | 512M | 100 | 1024 | 100 | 0 | 705.21 | 106.81 | 22.54 | 149 | N/A | N/A |
|  CBR SOAP Header Proxy | 512M | 100 | 1024 | 500 | 0 | 149.09 | 501.7 | 1.9 | 507 | N/A | N/A |
|  CBR SOAP Header Proxy | 512M | 100 | 1024 | 1000 | 0 | 74.09 | 1002.12 | 1.22 | 1007 | N/A | N/A |
|  CBR SOAP Header Proxy | 512M | 100 | 10240 | 0 | 0 | 940.5 | 79.14 | 49.59 | 191 | N/A | N/A |
|  CBR SOAP Header Proxy | 512M | 100 | 10240 | 30 | 0 | 874.92 | 86.26 | 42.66 | 178 | N/A | N/A |
|  CBR SOAP Header Proxy | 512M | 100 | 10240 | 100 | 0 | 579.31 | 129.89 | 25.1 | 211 | N/A | N/A |
|  CBR SOAP Header Proxy | 512M | 100 | 10240 | 500 | 0 | 147.82 | 506.94 | 15.77 | 543 | N/A | N/A |
|  CBR SOAP Header Proxy | 512M | 100 | 10240 | 1000 | 0 | 73.79 | 1002.87 | 4.1 | 1031 | N/A | N/A |
|  CBR SOAP Header Proxy | 512M | 100 | 102400 | 0 | 0 | 76.19 | 989.98 | 457.46 | 2287 | N/A | N/A |
|  CBR SOAP Header Proxy | 512M | 100 | 102400 | 30 | 0 | 74.19 | 1020.23 | 451.12 | 2287 | N/A | N/A |
|  CBR SOAP Header Proxy | 512M | 100 | 102400 | 100 | 0 | 74.05 | 1014.26 | 463.9 | 2415 | N/A | N/A |
|  CBR SOAP Header Proxy | 512M | 100 | 102400 | 500 | 0 | 63.34 | 1169.82 | 403.38 | 2335 | N/A | N/A |
|  CBR SOAP Header Proxy | 512M | 100 | 102400 | 1000 | 0 | 50.19 | 1482.57 | 382.31 | 2399 | N/A | N/A |
|  CBR SOAP Header Proxy | 512M | 200 | 500 | 0 | 0 | 1490.76 | 100.37 | 164.02 | 771 | N/A | N/A |
|  CBR SOAP Header Proxy | 512M | 200 | 500 | 30 | 0 | 1641.64 | 91.44 | 156.66 | 763 | N/A | N/A |
|  CBR SOAP Header Proxy | 512M | 200 | 500 | 100 | 0 | 1290.96 | 116.42 | 20.78 | 183 | N/A | N/A |
|  CBR SOAP Header Proxy | 512M | 200 | 500 | 500 | 0 | 296.56 | 502.59 | 5.1 | 531 | N/A | N/A |
|  CBR SOAP Header Proxy | 512M | 200 | 500 | 1000 | 0 | 146.95 | 1002.6 | 6.83 | 1019 | N/A | N/A |
|  CBR SOAP Header Proxy | 512M | 200 | 1024 | 0 | 0 | 2363.57 | 63.57 | 51.85 | 170 | N/A | N/A |
|  CBR SOAP Header Proxy | 512M | 200 | 1024 | 30 | 0 | 2132.7 | 70.39 | 57.19 | 134 | N/A | N/A |
|  CBR SOAP Header Proxy | 512M | 200 | 1024 | 100 | 0 | 1298.54 | 115.46 | 23.21 | 174 | N/A | N/A |
|  CBR SOAP Header Proxy | 512M | 200 | 1024 | 500 | 0 | 293.33 | 507.83 | 29.93 | 567 | N/A | N/A |
|  CBR SOAP Header Proxy | 512M | 200 | 1024 | 1000 | 0 | 147.25 | 1002.23 | 2.13 | 1011 | N/A | N/A |
|  CBR SOAP Header Proxy | 512M | 200 | 10240 | 0 | 0 | 964.15 | 156.12 | 86.62 | 425 | N/A | N/A |
|  CBR SOAP Header Proxy | 512M | 200 | 10240 | 30 | 0 | 968.81 | 155.18 | 96.22 | 807 | N/A | N/A |
|  CBR SOAP Header Proxy | 512M | 200 | 10240 | 100 | 0 | 847.43 | 176.95 | 76.89 | 547 | N/A | N/A |
|  CBR SOAP Header Proxy | 512M | 200 | 10240 | 500 | 0 | 288.35 | 515.71 | 29.42 | 611 | N/A | N/A |
|  CBR SOAP Header Proxy | 512M | 200 | 10240 | 1000 | 0 | 146.87 | 1004.48 | 7.52 | 1047 | N/A | N/A |
|  CBR SOAP Header Proxy | 512M | 200 | 102400 | 0 | 0 | 33.96 | 4365.88 | 2089.36 | 8767 | N/A | N/A |
|  CBR SOAP Header Proxy | 512M | 200 | 102400 | 30 | 0 | 35.39 | 4207.7 | 2115.3 | 10239 | N/A | N/A |
|  CBR SOAP Header Proxy | 512M | 200 | 102400 | 100 | 0 | 38.15 | 3952.96 | 2044.14 | 9407 | N/A | N/A |
|  CBR SOAP Header Proxy | 512M | 200 | 102400 | 500 | 0 | 32.43 | 4631.01 | 2391.38 | 10943 | N/A | N/A |
|  CBR SOAP Header Proxy | 512M | 200 | 102400 | 1000 | 0 | 32.3 | 4544 | 2185.39 | 10239 | N/A | N/A |
|  CBR Transport Header Proxy | 512M | 100 | 500 | 0 | 0 | 2791.74 | 27.09 | 66.49 | 125 | N/A | N/A |
|  CBR Transport Header Proxy | 512M | 100 | 500 | 30 | 0 | 1797.73 | 41.94 | 38.28 | 91 | N/A | N/A |
|  CBR Transport Header Proxy | 512M | 100 | 500 | 100 | 0 | 710.37 | 106 | 25.58 | 151 | N/A | N/A |
|  CBR Transport Header Proxy | 512M | 100 | 500 | 500 | 0 | 149.25 | 501.48 | 1.71 | 505 | N/A | N/A |
|  CBR Transport Header Proxy | 512M | 100 | 500 | 1000 | 0 | 74.07 | 1002.07 | 0.87 | 1007 | N/A | N/A |
|  CBR Transport Header Proxy | 512M | 100 | 1024 | 0 | 0 | 1950.74 | 38.81 | 106.02 | 739 | N/A | N/A |
|  CBR Transport Header Proxy | 512M | 100 | 1024 | 30 | 0 | 1776.75 | 42.65 | 33.03 | 94 | N/A | N/A |
|  CBR Transport Header Proxy | 512M | 100 | 1024 | 100 | 0 | 723.64 | 104.05 | 8.34 | 147 | N/A | N/A |
|  CBR Transport Header Proxy | 512M | 100 | 1024 | 500 | 0 | 148.95 | 501.49 | 2.02 | 505 | N/A | N/A |
|  CBR Transport Header Proxy | 512M | 100 | 1024 | 1000 | 0 | 74.04 | 1002.13 | 1.35 | 1007 | N/A | N/A |
|  CBR Transport Header Proxy | 512M | 100 | 10240 | 0 | 0 | 2007.42 | 37.51 | 92.39 | 723 | N/A | N/A |
|  CBR Transport Header Proxy | 512M | 100 | 10240 | 30 | 0 | 1591.17 | 47.35 | 46.55 | 105 | N/A | N/A |
|  CBR Transport Header Proxy | 512M | 100 | 10240 | 100 | 0 | 704.36 | 106.97 | 24.94 | 154 | N/A | N/A |
|  CBR Transport Header Proxy | 512M | 100 | 10240 | 500 | 0 | 149.2 | 501.76 | 1.63 | 505 | N/A | N/A |
|  CBR Transport Header Proxy | 512M | 100 | 10240 | 1000 | 0 | 74.07 | 1002.07 | 0.9 | 1003 | N/A | N/A |
|  CBR Transport Header Proxy | 512M | 100 | 102400 | 0 | 0 | 900 | 83.61 | 58.75 | 208 | N/A | N/A |
|  CBR Transport Header Proxy | 512M | 100 | 102400 | 30 | 0 | 878.8 | 85.54 | 42.89 | 193 | N/A | N/A |
|  CBR Transport Header Proxy | 512M | 100 | 102400 | 100 | 0 | 593.12 | 126.91 | 22.32 | 203 | N/A | N/A |
|  CBR Transport Header Proxy | 512M | 100 | 102400 | 500 | 0 | 148.49 | 504.41 | 2.82 | 515 | N/A | N/A |
|  CBR Transport Header Proxy | 512M | 100 | 102400 | 1000 | 0 | 73.66 | 1002.75 | 1.86 | 1007 | N/A | N/A |
|  CBR Transport Header Proxy | 512M | 200 | 500 | 0 | 0 | 2502.56 | 59.51 | 100.62 | 727 | N/A | N/A |
|  CBR Transport Header Proxy | 512M | 200 | 500 | 30 | 0 | 2227.97 | 67.34 | 107.1 | 699 | N/A | N/A |
|  CBR Transport Header Proxy | 512M | 200 | 500 | 100 | 0 | 1303.36 | 115.26 | 46.69 | 173 | N/A | N/A |
|  CBR Transport Header Proxy | 512M | 200 | 500 | 500 | 0 | 296.45 | 502.05 | 4.61 | 531 | N/A | N/A |
|  CBR Transport Header Proxy | 512M | 200 | 500 | 1000 | 0 | 146.9 | 1002.12 | 1.36 | 1007 | N/A | N/A |
|  CBR Transport Header Proxy | 512M | 200 | 1024 | 0 | 0 | 2034.11 | 73.91 | 163.08 | 795 | N/A | N/A |
|  CBR Transport Header Proxy | 512M | 200 | 1024 | 30 | 0 | 2000.31 | 73.76 | 147.79 | 815 | N/A | N/A |
|  CBR Transport Header Proxy | 512M | 200 | 1024 | 100 | 0 | 1334.22 | 112.59 | 16.06 | 169 | N/A | N/A |
|  CBR Transport Header Proxy | 512M | 200 | 1024 | 500 | 0 | 296.77 | 502.07 | 4 | 527 | N/A | N/A |
|  CBR Transport Header Proxy | 512M | 200 | 1024 | 1000 | 0 | 147.31 | 1002.19 | 1.96 | 1007 | N/A | N/A |
|  CBR Transport Header Proxy | 512M | 200 | 10240 | 0 | 0 | 1919.49 | 78.37 | 108.27 | 743 | N/A | N/A |
|  CBR Transport Header Proxy | 512M | 200 | 10240 | 30 | 0 | 2083.27 | 72.08 | 69.3 | 147 | N/A | N/A |
|  CBR Transport Header Proxy | 512M | 200 | 10240 | 100 | 0 | 1235.76 | 121.39 | 51.5 | 189 | N/A | N/A |
|  CBR Transport Header Proxy | 512M | 200 | 10240 | 500 | 0 | 296.66 | 502.58 | 4.59 | 527 | N/A | N/A |
|  CBR Transport Header Proxy | 512M | 200 | 10240 | 1000 | 0 | 147.12 | 1002.2 | 1.93 | 1007 | N/A | N/A |
|  CBR Transport Header Proxy | 512M | 200 | 102400 | 0 | 0 | 924.12 | 162.44 | 85.15 | 415 | N/A | N/A |
|  CBR Transport Header Proxy | 512M | 200 | 102400 | 30 | 0 | 927.83 | 161.87 | 68.91 | 355 | N/A | N/A |
|  CBR Transport Header Proxy | 512M | 200 | 102400 | 100 | 0 | 799.64 | 187.4 | 58.18 | 375 | N/A | N/A |
|  CBR Transport Header Proxy | 512M | 200 | 102400 | 500 | 0 | 291.33 | 511.57 | 13.91 | 575 | N/A | N/A |
|  CBR Transport Header Proxy | 512M | 200 | 102400 | 1000 | 0 | 146.7 | 1003.94 | 3.8 | 1019 | N/A | N/A |
|  Direct Proxy | 512M | 100 | 500 | 0 | 0 | 2088.99 | 36.22 | 84.45 | 707 | 77.43 | 270.149 |
|  Direct Proxy | 512M | 100 | 500 | 30 | 0 | 1833.52 | 41.12 | 37.37 | 85 | N/A | N/A |
|  Direct Proxy | 512M | 100 | 500 | 100 | 0 | 729.02 | 103.32 | 6.92 | 142 | N/A | N/A |
|  Direct Proxy | 512M | 100 | 500 | 500 | 0 | 149.06 | 501.4 | 1.72 | 503 | N/A | N/A |
|  Direct Proxy | 512M | 100 | 500 | 1000 | 0 | 74.07 | 1002.07 | 0.79 | 1007 | N/A | N/A |
|  Direct Proxy | 512M | 100 | 1024 | 0 | 0 | 2119.12 | 35.59 | 117.15 | 791 | N/A | N/A |
|  Direct Proxy | 512M | 100 | 1024 | 30 | 0 | 1817.37 | 41.46 | 32.74 | 91 | N/A | N/A |
|  Direct Proxy | 512M | 100 | 1024 | 100 | 0 | 714.66 | 105.43 | 21.13 | 148 | N/A | N/A |
|  Direct Proxy | 512M | 100 | 1024 | 500 | 0 | 149.22 | 501.37 | 1.6 | 503 | N/A | N/A |
|  Direct Proxy | 512M | 100 | 1024 | 1000 | 0 | 74.1 | 1002.04 | 0.55 | 1003 | N/A | N/A |
|  Direct Proxy | 512M | 100 | 10240 | 0 | 0 | 1959.37 | 38.45 | 86.85 | 659 | N/A | N/A |
|  Direct Proxy | 512M | 100 | 10240 | 30 | 0 | 1669.44 | 45.12 | 38.56 | 98 | N/A | N/A |
|  Direct Proxy | 512M | 100 | 10240 | 100 | 0 | 712.06 | 106 | 12.2 | 149 | N/A | N/A |
|  Direct Proxy | 512M | 100 | 10240 | 500 | 0 | 149.17 | 501.75 | 1.57 | 505 | N/A | N/A |
|  Direct Proxy | 512M | 100 | 10240 | 1000 | 0 | 74.03 | 1002.07 | 0.8 | 1007 | N/A | N/A |
|  Direct Proxy | 512M | 100 | 102400 | 0 | 0 | 922.3 | 81.55 | 35.31 | 152 | N/A | N/A |
|  Direct Proxy | 512M | 100 | 102400 | 30 | 0 | 882.17 | 85.25 | 24.56 | 159 | N/A | N/A |
|  Direct Proxy | 512M | 100 | 102400 | 100 | 0 | 560.76 | 134.05 | 37.63 | 208 | N/A | N/A |
|  Direct Proxy | 512M | 100 | 102400 | 500 | 0 | 148.27 | 503.83 | 2 | 509 | N/A | N/A |
|  Direct Proxy | 512M | 100 | 102400 | 1000 | 0 | 73.66 | 1002.62 | 1.64 | 1007 | N/A | N/A |
|  Direct Proxy | 512M | 200 | 500 | 0 | 0 | 2847.78 | 52.78 | 111.02 | 711 | N/A | N/A |
|  Direct Proxy | 512M | 200 | 500 | 30 | 0 | 2524.9 | 59.5 | 73.98 | 117 | N/A | N/A |
|  Direct Proxy | 512M | 200 | 500 | 100 | 0 | 1336.79 | 112.12 | 21.84 | 168 | N/A | N/A |
|  Direct Proxy | 512M | 200 | 500 | 500 | 0 | 297.01 | 502.07 | 4.53 | 531 | N/A | N/A |
|  Direct Proxy | 512M | 200 | 500 | 1000 | 0 | 147.22 | 1002.13 | 1.54 | 1003 | N/A | N/A |
|  Direct Proxy | 512M | 200 | 1024 | 0 | 0 | 1796.02 | 83.79 | 179.83 | 823 | N/A | N/A |
|  Direct Proxy | 512M | 200 | 1024 | 30 | 0 | 1977.78 | 76.18 | 159.16 | 831 | N/A | N/A |
|  Direct Proxy | 512M | 200 | 1024 | 100 | 0 | 1328.57 | 113.29 | 16.53 | 170 | N/A | N/A |
|  Direct Proxy | 512M | 200 | 1024 | 500 | 0 | 296.78 | 501.6 | 2.29 | 511 | N/A | N/A |
|  Direct Proxy | 512M | 200 | 1024 | 1000 | 0 | 147.26 | 1002.19 | 1.79 | 1011 | N/A | N/A |
|  Direct Proxy | 512M | 200 | 10240 | 0 | 0 | 2273.35 | 65.45 | 68.32 | 250 | N/A | N/A |
|  Direct Proxy | 512M | 200 | 10240 | 30 | 0 | 2097.66 | 71.57 | 61.5 | 144 | N/A | N/A |
|  Direct Proxy | 512M | 200 | 10240 | 100 | 0 | 1237.63 | 121.05 | 47.52 | 181 | N/A | N/A |
|  Direct Proxy | 512M | 200 | 10240 | 500 | 0 | 296.4 | 502.67 | 4.89 | 531 | N/A | N/A |
|  Direct Proxy | 512M | 200 | 10240 | 1000 | 0 | 146.97 | 1002.18 | 1.74 | 1007 | N/A | N/A |
|  Direct Proxy | 512M | 200 | 102400 | 0 | 0 | 832.65 | 180.43 | 62.33 | 365 | N/A | N/A |
|  Direct Proxy | 512M | 200 | 102400 | 30 | 0 | 865.08 | 173.37 | 51.73 | 311 | N/A | N/A |
|  Direct Proxy | 512M | 200 | 102400 | 100 | 0 | 733.44 | 204.88 | 54.15 | 353 | N/A | N/A |
|  Direct Proxy | 512M | 200 | 102400 | 500 | 0 | 291.6 | 509.77 | 9.61 | 551 | N/A | N/A |
|  Direct Proxy | 512M | 200 | 102400 | 1000 | 0 | 146.59 | 1003.9 | 3.04 | 1015 | N/A | N/A |
|  XSLT Enhanced Proxy | 512M | 100 | 500 | 0 | 0 | 1791.77 | 42.12 | 94.91 | 747 | N/A | N/A |
|  XSLT Enhanced Proxy | 512M | 100 | 500 | 30 | 0 | 1500.83 | 50.25 | 40.47 | 103 | N/A | N/A |
|  XSLT Enhanced Proxy | 512M | 100 | 500 | 100 | 0 | 693.63 | 108.79 | 19.32 | 152 | N/A | N/A |
|  XSLT Enhanced Proxy | 512M | 100 | 500 | 500 | 0 | 149.09 | 502.07 | 2.36 | 509 | N/A | N/A |
|  XSLT Enhanced Proxy | 512M | 100 | 500 | 1000 | 0 | 74.06 | 1002.12 | 1.17 | 1007 | N/A | N/A |
|  XSLT Enhanced Proxy | 512M | 100 | 1024 | 0 | 0 | 1866.64 | 40.41 | 59.26 | 165 | N/A | N/A |
|  XSLT Enhanced Proxy | 512M | 100 | 1024 | 30 | 0 | 1534.11 | 49.17 | 27.52 | 93 | N/A | N/A |
|  XSLT Enhanced Proxy | 512M | 100 | 1024 | 100 | 0 | 687.97 | 109.52 | 11.25 | 152 | N/A | N/A |
|  XSLT Enhanced Proxy | 512M | 100 | 1024 | 500 | 0 | 148.93 | 502.33 | 2.98 | 523 | N/A | N/A |
|  XSLT Enhanced Proxy | 512M | 100 | 1024 | 1000 | 0 | 74.07 | 1002.26 | 2.02 | 1011 | N/A | N/A |
|  XSLT Enhanced Proxy | 512M | 100 | 10240 | 0 | 0 | 498.64 | 149.15 | 60.71 | 313 | N/A | N/A |
|  XSLT Enhanced Proxy | 512M | 100 | 10240 | 30 | 0 | 486.81 | 154.98 | 54.56 | 313 | N/A | N/A |
|  XSLT Enhanced Proxy | 512M | 100 | 10240 | 100 | 0 | 426.23 | 176.87 | 42.77 | 301 | N/A | N/A |
|  XSLT Enhanced Proxy | 512M | 100 | 10240 | 500 | 0 | 147.61 | 507.55 | 6.94 | 539 | N/A | N/A |
|  XSLT Enhanced Proxy | 512M | 100 | 10240 | 1000 | 0 | 73.54 | 1004.2 | 4.13 | 1031 | N/A | N/A |
|  XSLT Enhanced Proxy | 512M | 100 | 102400 | 0 | 0 | 64.05 | 1162.8 | 351.15 | 2127 | N/A | N/A |
|  XSLT Enhanced Proxy | 512M | 100 | 102400 | 30 | 0 | 64.59 | 1159.34 | 327.53 | 2031 | N/A | N/A |
|  XSLT Enhanced Proxy | 512M | 100 | 102400 | 100 | 0 | 64.87 | 1148.21 | 319.81 | 2079 | N/A | N/A |
|  XSLT Enhanced Proxy | 512M | 100 | 102400 | 500 | 0 | 63 | 1181.8 | 274.36 | 1927 | N/A | N/A |
|  XSLT Enhanced Proxy | 512M | 100 | 102400 | 1000 | 0 | 54.87 | 1357.61 | 185.43 | 1863 | N/A | N/A |
|  XSLT Enhanced Proxy | 512M | 200 | 500 | 0 | 0 | 1870.78 | 80.33 | 93.81 | 615 | N/A | N/A |
|  XSLT Enhanced Proxy | 512M | 200 | 500 | 30 | 0 | 1919.25 | 78.25 | 35.82 | 176 | N/A | N/A |
|  XSLT Enhanced Proxy | 512M | 200 | 500 | 100 | 0 | 1214.85 | 123.77 | 31.78 | 187 | N/A | N/A |
|  XSLT Enhanced Proxy | 512M | 200 | 500 | 500 | 0 | 294.81 | 505.62 | 9.26 | 547 | N/A | N/A |
|  XSLT Enhanced Proxy | 512M | 200 | 500 | 1000 | 0 | 147.19 | 1002.44 | 2.82 | 1023 | N/A | N/A |
|  XSLT Enhanced Proxy | 512M | 200 | 1024 | 0 | 0 | 1755.97 | 85.66 | 136.19 | 807 | N/A | N/A |
|  XSLT Enhanced Proxy | 512M | 200 | 1024 | 30 | 0 | 1734.96 | 86.7 | 62.48 | 200 | N/A | N/A |
|  XSLT Enhanced Proxy | 512M | 200 | 1024 | 100 | 0 | 1213.27 | 123.59 | 19.94 | 184 | N/A | N/A |
|  XSLT Enhanced Proxy | 512M | 200 | 1024 | 500 | 0 | 295.79 | 503.62 | 5.76 | 535 | N/A | N/A |
|  XSLT Enhanced Proxy | 512M | 200 | 1024 | 1000 | 0 | 147.21 | 1003.41 | 11.37 | 1031 | N/A | N/A |
|  XSLT Enhanced Proxy | 512M | 200 | 10240 | 0 | 0 | 499.3 | 301.24 | 110.61 | 595 | N/A | N/A |
|  XSLT Enhanced Proxy | 512M | 200 | 10240 | 30 | 0 | 500.27 | 300.42 | 100.17 | 575 | N/A | N/A |
|  XSLT Enhanced Proxy | 512M | 200 | 10240 | 100 | 0 | 489.29 | 306.91 | 87.08 | 535 | N/A | N/A |
|  XSLT Enhanced Proxy | 512M | 200 | 10240 | 500 | 0 | 273.29 | 545.36 | 42.73 | 687 | N/A | N/A |
|  XSLT Enhanced Proxy | 512M | 200 | 10240 | 1000 | 0 | 145.93 | 1009.18 | 10.2 | 1055 | N/A | N/A |
|  XSLT Enhanced Proxy | 512M | 200 | 102400 | 0 | 0 | 60 | 2443.74 | 701.07 | 4223 | N/A | N/A |
|  XSLT Enhanced Proxy | 512M | 200 | 102400 | 30 | 0 | 59.36 | 2467.9 | 632.7 | 3983 | N/A | N/A |
|  XSLT Enhanced Proxy | 512M | 200 | 102400 | 100 | 0 | 59.72 | 2452.16 | 709.34 | 4287 | N/A | N/A |
|  XSLT Enhanced Proxy | 512M | 200 | 102400 | 500 | 0 | 59.47 | 2445.92 | 566.8 | 3743 | N/A | N/A |
|  XSLT Enhanced Proxy | 512M | 200 | 102400 | 1000 | 0 | 60.11 | 2446.43 | 640.87 | 4447 | N/A | N/A |
|  XSLT Proxy | 512M | 100 | 500 | 0 | 0 | 1544.27 | 48.86 | 53.77 | 162 | N/A | N/A |
|  XSLT Proxy | 512M | 100 | 500 | 30 | 0 | 1320.84 | 57.08 | 42.7 | 112 | N/A | N/A |
|  XSLT Proxy | 512M | 100 | 500 | 100 | 0 | 692.54 | 108.73 | 21.27 | 151 | N/A | N/A |
|  XSLT Proxy | 512M | 100 | 500 | 500 | 0 | 149.04 | 502.6 | 2.68 | 515 | N/A | N/A |
|  XSLT Proxy | 512M | 100 | 500 | 1000 | 0 | 74 | 1002.24 | 1.97 | 1007 | N/A | N/A |
|  XSLT Proxy | 512M | 100 | 1024 | 0 | 0 | 1290.28 | 58.45 | 54.74 | 182 | N/A | N/A |
|  XSLT Proxy | 512M | 100 | 1024 | 30 | 0 | 1181.93 | 63.8 | 43.86 | 132 | N/A | N/A |
|  XSLT Proxy | 512M | 100 | 1024 | 100 | 0 | 674.69 | 111.74 | 15.91 | 152 | N/A | N/A |
|  XSLT Proxy | 512M | 100 | 1024 | 500 | 0 | 148.77 | 502.86 | 2.83 | 519 | N/A | N/A |
|  XSLT Proxy | 512M | 100 | 1024 | 1000 | 0 | 74 | 1002.25 | 1.96 | 1011 | N/A | N/A |
|  XSLT Proxy | 512M | 100 | 10240 | 0 | 0 | 300.52 | 250.82 | 147.59 | 731 | N/A | N/A |
|  XSLT Proxy | 512M | 100 | 10240 | 30 | 0 | 301.22 | 250.42 | 125.34 | 667 | N/A | N/A |
|  XSLT Proxy | 512M | 100 | 10240 | 100 | 0 | 298.42 | 252.69 | 106.83 | 643 | N/A | N/A |
|  XSLT Proxy | 512M | 100 | 10240 | 500 | 0 | 143.59 | 521.1 | 27.77 | 639 | N/A | N/A |
|  XSLT Proxy | 512M | 100 | 10240 | 1000 | 0 | 73.55 | 1007.62 | 4.95 | 1039 | N/A | N/A |
|  XSLT Proxy | 512M | 100 | 102400 | 0 | 0 | 19.17 | 3876.07 | 1176.18 | 6847 | N/A | N/A |
|  XSLT Proxy | 512M | 100 | 102400 | 30 | 0 | 19.17 | 3829.12 | 971.05 | 5919 | N/A | N/A |
|  XSLT Proxy | 512M | 100 | 102400 | 100 | 0 | 17.4 | 4286.06 | 1357.86 | 6815 | N/A | N/A |
|  XSLT Proxy | 512M | 100 | 102400 | 500 | 0 | 18 | 4112.77 | 1183.49 | 6847 | N/A | N/A |
|  XSLT Proxy | 512M | 100 | 102400 | 1000 | 0 | 17.64 | 4123.17 | 1108.89 | 6527 | N/A | N/A |
|  XSLT Proxy | 512M | 200 | 500 | 0 | 0 | 1507.42 | 99.88 | 98.17 | 355 | N/A | N/A |
|  XSLT Proxy | 512M | 200 | 500 | 30 | 0 | 1537.39 | 97.92 | 96.79 | 763 | N/A | N/A |
|  XSLT Proxy | 512M | 200 | 500 | 100 | 0 | 1152.75 | 130.29 | 21.21 | 197 | N/A | N/A |
|  XSLT Proxy | 512M | 200 | 500 | 500 | 0 | 294.4 | 508.22 | 24.5 | 579 | N/A | N/A |
|  XSLT Proxy | 512M | 200 | 500 | 1000 | 0 | 147.24 | 1002.67 | 3.85 | 1031 | N/A | N/A |
|  XSLT Proxy | 512M | 200 | 1024 | 0 | 0 | 1309.45 | 114.86 | 101.01 | 731 | N/A | N/A |
|  XSLT Proxy | 512M | 200 | 1024 | 30 | 0 | 1330.06 | 113.03 | 93.93 | 759 | N/A | N/A |
|  XSLT Proxy | 512M | 200 | 1024 | 100 | 0 | 1106.1 | 135.53 | 22.96 | 209 | N/A | N/A |
|  XSLT Proxy | 512M | 200 | 1024 | 500 | 0 | 294.63 | 505.63 | 7.02 | 539 | N/A | N/A |
|  XSLT Proxy | 512M | 200 | 1024 | 1000 | 0 | 147.09 | 1002.97 | 4.61 | 1031 | N/A | N/A |
|  XSLT Proxy | 512M | 200 | 10240 | 0 | 0 | 254.36 | 592.14 | 270.01 | 1295 | N/A | N/A |
|  XSLT Proxy | 512M | 200 | 10240 | 30 | 0 | 256.15 | 586.94 | 270.08 | 1359 | N/A | N/A |
|  XSLT Proxy | 512M | 200 | 10240 | 100 | 0 | 254.62 | 587.9 | 257.38 | 1311 | N/A | N/A |
|  XSLT Proxy | 512M | 200 | 10240 | 500 | 0 | 229.9 | 648.16 | 155.7 | 1159 | N/A | N/A |
|  XSLT Proxy | 512M | 200 | 10240 | 1000 | 0 | 142.46 | 1039.04 | 43.37 | 1207 | N/A | N/A |
|  XSLT Proxy | 512M | 200 | 102400 | 0 | 0 | 8.7 | 17307.23 | 8255.72 | 31871 | N/A | N/A |
|  XSLT Proxy | 512M | 200 | 102400 | 30 | 0 | 9.73 | 13910.41 | 4889.02 | 21759 | N/A | N/A |
|  XSLT Proxy | 512M | 200 | 102400 | 100 | 0 | 10.31 | 13923.28 | 5012.45 | 23551 | N/A | N/A |
|  XSLT Proxy | 512M | 200 | 102400 | 500 | 0 | 9.41 | 15241.84 | 6392.87 | 26623 | N/A | N/A |
|  XSLT Proxy | 512M | 200 | 102400 | 1000 | 0 | 9.85 | 13718.34 | 3730.65 | 20223 | N/A | N/A |
