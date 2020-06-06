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
| Concurrent Users | The number of users accessing the application at the same time. | 200 |
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
| Operating System | Distribution | Kernel | Linux ip-10-0-1-96 5.3.0-1017-aws #18~18.04.1-Ubuntu SMP Wed Apr 8 15:12:16 UTC 2020 x86_64 x86_64 x86_64 GNU/Linux |


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
|  CBR Proxy | 512M | 200 | 500 | 0 | 0 | 2536.64 | 59.27 | 79.63 | 159 | N/A | N/A |
|  CBR Proxy | 512M | 200 | 500 | 30 | 0 | 2360.59 | 63.66 | 61.58 | 119 | N/A | N/A |
|  CBR Proxy | 512M | 200 | 500 | 100 | 0 | 1313.05 | 114.36 | 23.16 | 167 | N/A | N/A |
|  CBR Proxy | 512M | 200 | 500 | 500 | 0 | 294.96 | 506.46 | 30.62 | 639 | N/A | N/A |
|  CBR Proxy | 512M | 200 | 500 | 1000 | 0 | 146.96 | 1002.16 | 1.67 | 1007 | N/A | N/A |
|  CBR Proxy | 512M | 200 | 1024 | 0 | 0 | 1626.87 | 92.39 | 174.16 | 827 | N/A | N/A |
|  CBR Proxy | 512M | 200 | 1024 | 30 | 0 | 1991.88 | 75.65 | 92.8 | 160 | N/A | N/A |
|  CBR Proxy | 512M | 200 | 1024 | 100 | 0 | 1236.28 | 121.54 | 18.8 | 174 | N/A | N/A |
|  CBR Proxy | 512M | 200 | 1024 | 500 | 0 | 296.73 | 502.96 | 5.06 | 531 | N/A | N/A |
|  CBR Proxy | 512M | 200 | 1024 | 1000 | 0 | 147.04 | 1002.31 | 2.57 | 1015 | N/A | N/A |
|  CBR Proxy | 512M | 200 | 10240 | 0 | 0 | 609.39 | 247.32 | 118.97 | 703 | N/A | N/A |
|  CBR Proxy | 512M | 200 | 10240 | 30 | 0 | 643.39 | 233.62 | 120.42 | 815 | N/A | N/A |
|  CBR Proxy | 512M | 200 | 10240 | 100 | 0 | 624.49 | 240.76 | 91.15 | 679 | N/A | N/A |
|  CBR Proxy | 512M | 200 | 10240 | 500 | 0 | 278.43 | 535.31 | 40.75 | 707 | N/A | N/A |
|  CBR Proxy | 512M | 200 | 10240 | 1000 | 0 | 145.21 | 1013.49 | 31.13 | 1151 | N/A | N/A |
|  CBR Proxy | 512M | 200 | 102400 | 0 | 0 | 13.74 | 11239.1 | 6467.95 | 24063 | N/A | N/A |
|  CBR Proxy | 512M | 200 | 102400 | 30 | 3.88 | 4.4 | 35924.08 | 45194.99 | 120319 | N/A | N/A |
|  CBR Proxy | 512M | 200 | 102400 | 100 | 0 | 11.84 | 12800.57 | 6779.45 | 26111 | N/A | N/A |
|  CBR Proxy | 512M | 200 | 102400 | 500 | 0 | 13.26 | 10813.33 | 4930.71 | 19839 | N/A | N/A |
|  CBR Proxy | 512M | 200 | 102400 | 1000 | 0 | 13.53 | 10776.27 | 4694.1 | 19711 | N/A | N/A |
|  CBR SOAP Header Proxy | 512M | 200 | 500 | 0 | 0 | 1966.22 | 76.4 | 125.96 | 763 | N/A | N/A |
|  CBR SOAP Header Proxy | 512M | 200 | 500 | 30 | 0 | 2210.03 | 67.97 | 61.11 | 147 | N/A | N/A |
|  CBR SOAP Header Proxy | 512M | 200 | 500 | 100 | 0 | 1256.37 | 119.38 | 57.51 | 182 | N/A | N/A |
|  CBR SOAP Header Proxy | 512M | 200 | 500 | 500 | 0 | 295.9 | 502.35 | 4.54 | 531 | N/A | N/A |
|  CBR SOAP Header Proxy | 512M | 200 | 500 | 1000 | 0 | 147.2 | 1002.31 | 2.57 | 1015 | N/A | N/A |
|  CBR SOAP Header Proxy | 512M | 200 | 1024 | 0 | 0 | 2179.23 | 68.89 | 82.64 | 699 | N/A | N/A |
|  CBR SOAP Header Proxy | 512M | 200 | 1024 | 30 | 0 | 2193.69 | 68.52 | 54.06 | 137 | N/A | N/A |
|  CBR SOAP Header Proxy | 512M | 200 | 1024 | 100 | 0 | 1241.45 | 121.04 | 51.32 | 181 | N/A | N/A |
|  CBR SOAP Header Proxy | 512M | 200 | 1024 | 500 | 0 | 295.93 | 502.95 | 5.59 | 535 | N/A | N/A |
|  CBR SOAP Header Proxy | 512M | 200 | 1024 | 1000 | 0 | 146.88 | 1002.18 | 1.73 | 1011 | N/A | N/A |
|  CBR SOAP Header Proxy | 512M | 200 | 10240 | 0 | 0 | 230.19 | 160.57 | 713.19 | 687 | N/A | N/A |
|  CBR SOAP Header Proxy | 512M | 200 | 10240 | 30 | 0 | 977.31 | 153.87 | 80.47 | 591 | N/A | N/A |
|  CBR SOAP Header Proxy | 512M | 200 | 10240 | 100 | 0 | 849.86 | 177.22 | 73.48 | 551 | N/A | N/A |
|  CBR SOAP Header Proxy | 512M | 200 | 10240 | 500 | 0 | 292.47 | 510.75 | 12.77 | 563 | N/A | N/A |
|  CBR SOAP Header Proxy | 512M | 200 | 10240 | 1000 | 0 | 146.9 | 1004.89 | 8.31 | 1047 | N/A | N/A |
|  CBR SOAP Header Proxy | 512M | 200 | 102400 | 0 | 0 | 20.84 | 7628.06 | 9395.17 | 31615 | N/A | N/A |
|  CBR SOAP Header Proxy | 512M | 200 | 102400 | 30 | 0 | 36.09 | 4136.43 | 2257.38 | 10623 | N/A | N/A |
|  CBR SOAP Header Proxy | 512M | 200 | 102400 | 100 | 0 | 36.45 | 4059.64 | 1834.29 | 8831 | N/A | N/A |
|  CBR SOAP Header Proxy | 512M | 200 | 102400 | 500 | 0 | 33.84 | 4372.33 | 2463.13 | 10943 | N/A | N/A |
|  CBR SOAP Header Proxy | 512M | 200 | 102400 | 1000 | 0 | 30.73 | 4834.03 | 2105.12 | 9407 | N/A | N/A |
|  CBR Transport Header Proxy | 512M | 200 | 500 | 0 | 0 | 1855.25 | 80.32 | 154.72 | 775 | N/A | N/A |
|  CBR Transport Header Proxy | 512M | 200 | 500 | 30 | 0 | 2474.17 | 60.63 | 70.49 | 124 | N/A | N/A |
|  CBR Transport Header Proxy | 512M | 200 | 500 | 100 | 0 | 1326.3 | 113.29 | 35.74 | 161 | N/A | N/A |
|  CBR Transport Header Proxy | 512M | 200 | 500 | 500 | 0 | 297.26 | 501.79 | 3.52 | 523 | N/A | N/A |
|  CBR Transport Header Proxy | 512M | 200 | 500 | 1000 | 0 | 147.3 | 1002.15 | 1.7 | 1007 | N/A | N/A |
|  CBR Transport Header Proxy | 512M | 200 | 1024 | 0 | 0 | 3209.34 | 46.72 | 74.85 | 146 | N/A | N/A |
|  CBR Transport Header Proxy | 512M | 200 | 1024 | 30 | 0 | 2433.55 | 62.11 | 103.64 | 775 | N/A | N/A |
|  CBR Transport Header Proxy | 512M | 200 | 1024 | 100 | 0 | 1330.38 | 112.77 | 22.74 | 166 | N/A | N/A |
|  CBR Transport Header Proxy | 512M | 200 | 1024 | 500 | 0 | 296.82 | 501.83 | 3.43 | 523 | N/A | N/A |
|  CBR Transport Header Proxy | 512M | 200 | 1024 | 1000 | 0 | 147.29 | 1002.12 | 1.54 | 1003 | N/A | N/A |
|  CBR Transport Header Proxy | 512M | 200 | 10240 | 0 | 0 | 2543.89 | 58.97 | 68.16 | 160 | N/A | N/A |
|  CBR Transport Header Proxy | 512M | 200 | 10240 | 30 | 0 | 2172.41 | 69.05 | 82.46 | 715 | N/A | N/A |
|  CBR Transport Header Proxy | 512M | 200 | 10240 | 100 | 0 | 1287.28 | 116.63 | 17.2 | 174 | N/A | N/A |
|  CBR Transport Header Proxy | 512M | 200 | 10240 | 500 | 0 | 296.35 | 502.7 | 4.41 | 531 | N/A | N/A |
|  CBR Transport Header Proxy | 512M | 200 | 10240 | 1000 | 0 | 147.26 | 1002.12 | 1.54 | 1003 | N/A | N/A |
|  CBR Transport Header Proxy | 512M | 200 | 102400 | 0 | 0 | 990.14 | 151.77 | 74.71 | 365 | N/A | N/A |
|  CBR Transport Header Proxy | 512M | 200 | 102400 | 30 | 0 | 991.24 | 151.47 | 64.2 | 333 | N/A | N/A |
|  CBR Transport Header Proxy | 512M | 200 | 102400 | 100 | 0 | 869.56 | 172.57 | 45.82 | 319 | N/A | N/A |
|  CBR Transport Header Proxy | 512M | 200 | 102400 | 500 | 0 | 292.92 | 509.33 | 10.43 | 559 | N/A | N/A |
|  CBR Transport Header Proxy | 512M | 200 | 102400 | 1000 | 0 | 146.78 | 1003.34 | 3.19 | 1019 | N/A | N/A |
|  Direct Proxy | 512M | 200 | 500 | 0 | 0 | 1617.28 | 93.14 | 179.78 | 855 | 68.9 | 305.391 |
|  Direct Proxy | 512M | 200 | 500 | 30 | 0 | 2383.25 | 62.99 | 98.12 | 651 | N/A | N/A |
|  Direct Proxy | 512M | 200 | 500 | 100 | 0 | 1308.49 | 114.72 | 46.37 | 165 | N/A | N/A |
|  Direct Proxy | 512M | 200 | 500 | 500 | 0 | 296.12 | 502.18 | 4.79 | 535 | N/A | N/A |
|  Direct Proxy | 512M | 200 | 500 | 1000 | 0 | 147.29 | 1002.13 | 1.44 | 1007 | N/A | N/A |
|  Direct Proxy | 512M | 200 | 1024 | 0 | 0 | 2640.72 | 56.87 | 130.45 | 779 | N/A | N/A |
|  Direct Proxy | 512M | 200 | 1024 | 30 | 0 | 2455.11 | 60.03 | 89.98 | 118 | N/A | N/A |
|  Direct Proxy | 512M | 200 | 1024 | 100 | 0 | 1353.07 | 110.86 | 14.92 | 166 | N/A | N/A |
|  Direct Proxy | 512M | 200 | 1024 | 500 | 0 | 296.25 | 502.2 | 4.49 | 527 | N/A | N/A |
|  Direct Proxy | 512M | 200 | 1024 | 1000 | 0 | 147.03 | 1002.13 | 1.62 | 1003 | N/A | N/A |
|  Direct Proxy | 512M | 200 | 10240 | 0 | 0 | 1644.19 | 91.52 | 177.66 | 819 | N/A | N/A |
|  Direct Proxy | 512M | 200 | 10240 | 30 | 0 | 1830.96 | 82.3 | 131.45 | 791 | N/A | N/A |
|  Direct Proxy | 512M | 200 | 10240 | 100 | 0 | 1241.27 | 120.84 | 37.9 | 188 | N/A | N/A |
|  Direct Proxy | 512M | 200 | 10240 | 500 | 0 | 296.87 | 502.36 | 3.54 | 523 | N/A | N/A |
|  Direct Proxy | 512M | 200 | 10240 | 1000 | 0 | 147.14 | 1002.16 | 1.64 | 1007 | N/A | N/A |
|  Direct Proxy | 512M | 200 | 102400 | 0 | 0 | 986.51 | 152.25 | 73.49 | 351 | N/A | N/A |
|  Direct Proxy | 512M | 200 | 102400 | 30 | 0 | 999.17 | 150.17 | 64.61 | 339 | N/A | N/A |
|  Direct Proxy | 512M | 200 | 102400 | 100 | 0 | 824.02 | 182.04 | 83.55 | 347 | N/A | N/A |
|  Direct Proxy | 512M | 200 | 102400 | 500 | 0 | 294.14 | 506.88 | 7.88 | 543 | N/A | N/A |
|  Direct Proxy | 512M | 200 | 102400 | 1000 | 0 | 146.74 | 1003.56 | 3.41 | 1019 | N/A | N/A |
|  XSLT Enhanced Proxy | 512M | 200 | 500 | 0 | 0 | 2136.21 | 70.43 | 102.71 | 643 | N/A | N/A |
|  XSLT Enhanced Proxy | 512M | 200 | 500 | 30 | 0 | 2038.72 | 74.23 | 87.37 | 759 | N/A | N/A |
|  XSLT Enhanced Proxy | 512M | 200 | 500 | 100 | 0 | 1250.73 | 120.19 | 24.78 | 180 | N/A | N/A |
|  XSLT Enhanced Proxy | 512M | 200 | 500 | 500 | 0 | 295.5 | 503.46 | 5.73 | 535 | N/A | N/A |
|  XSLT Enhanced Proxy | 512M | 200 | 500 | 1000 | 0 | 146.78 | 1002.45 | 3.14 | 1019 | N/A | N/A |
|  XSLT Enhanced Proxy | 512M | 200 | 1024 | 0 | 0 | 2001.76 | 75.16 | 100.81 | 497 | N/A | N/A |
|  XSLT Enhanced Proxy | 512M | 200 | 1024 | 30 | 0 | 1900.19 | 79.13 | 84.86 | 723 | N/A | N/A |
|  XSLT Enhanced Proxy | 512M | 200 | 1024 | 100 | 0 | 1239.45 | 121.04 | 17.17 | 173 | N/A | N/A |
|  XSLT Enhanced Proxy | 512M | 200 | 1024 | 500 | 0 | 292.67 | 506.96 | 20.43 | 555 | N/A | N/A |
|  XSLT Enhanced Proxy | 512M | 200 | 1024 | 1000 | 0 | 147.17 | 1002.41 | 3.09 | 1023 | N/A | N/A |
|  XSLT Enhanced Proxy | 512M | 200 | 10240 | 0 | 0 | 511.8 | 294.29 | 116.04 | 711 | N/A | N/A |
|  XSLT Enhanced Proxy | 512M | 200 | 10240 | 30 | 0 | 511.21 | 293.74 | 115.7 | 715 | N/A | N/A |
|  XSLT Enhanced Proxy | 512M | 200 | 10240 | 100 | 0 | 503.11 | 298.79 | 102.02 | 659 | N/A | N/A |
|  XSLT Enhanced Proxy | 512M | 200 | 10240 | 500 | 0 | 267.34 | 557.45 | 59.27 | 807 | N/A | N/A |
|  XSLT Enhanced Proxy | 512M | 200 | 10240 | 1000 | 0 | 146.45 | 1008.38 | 9.62 | 1047 | N/A | N/A |
|  XSLT Enhanced Proxy | 512M | 200 | 102400 | 0 | 0 | 59.83 | 2464.8 | 694.25 | 4543 | N/A | N/A |
|  XSLT Enhanced Proxy | 512M | 200 | 102400 | 30 | 0 | 60.18 | 2431.09 | 651.95 | 4255 | N/A | N/A |
|  XSLT Enhanced Proxy | 512M | 200 | 102400 | 100 | 0 | 60.05 | 2449.96 | 712.78 | 4255 | N/A | N/A |
|  XSLT Enhanced Proxy | 512M | 200 | 102400 | 500 | 0 | 61.38 | 2408.28 | 591.29 | 3727 | N/A | N/A |
|  XSLT Enhanced Proxy | 512M | 200 | 102400 | 1000 | 0 | 60.37 | 2406.44 | 575.67 | 3919 | N/A | N/A |
|  XSLT Proxy | 512M | 200 | 500 | 0 | 0 | 1550.55 | 97.03 | 83.27 | 265 | N/A | N/A |
|  XSLT Proxy | 512M | 200 | 500 | 30 | 0 | 1535.14 | 97.9 | 64.48 | 226 | N/A | N/A |
|  XSLT Proxy | 512M | 200 | 500 | 100 | 0 | 1119 | 134.1 | 27 | 214 | N/A | N/A |
|  XSLT Proxy | 512M | 200 | 500 | 500 | 0 | 294.73 | 505.12 | 7.28 | 539 | N/A | N/A |
|  XSLT Proxy | 512M | 200 | 500 | 1000 | 0 | 147 | 1003.81 | 10.38 | 1031 | N/A | N/A |
|  XSLT Proxy | 512M | 200 | 1024 | 0 | 0 | 1397.51 | 107.61 | 84.33 | 615 | N/A | N/A |
|  XSLT Proxy | 512M | 200 | 1024 | 30 | 0 | 1408.12 | 106.73 | 71.18 | 479 | N/A | N/A |
|  XSLT Proxy | 512M | 200 | 1024 | 100 | 0 | 1129.34 | 132.93 | 65.25 | 559 | N/A | N/A |
|  XSLT Proxy | 512M | 200 | 1024 | 500 | 0 | 294.65 | 505.24 | 7.19 | 539 | N/A | N/A |
|  XSLT Proxy | 512M | 200 | 1024 | 1000 | 0 | 146.7 | 1003.32 | 7.62 | 1031 | N/A | N/A |
|  XSLT Proxy | 512M | 200 | 10240 | 0 | 0 | 249.95 | 600.59 | 305.9 | 1471 | N/A | N/A |
|  XSLT Proxy | 512M | 200 | 10240 | 30 | 0 | 250.54 | 597.31 | 282.91 | 1391 | N/A | N/A |
|  XSLT Proxy | 512M | 200 | 10240 | 100 | 0 | 255.77 | 587.67 | 260.85 | 1431 | N/A | N/A |
|  XSLT Proxy | 512M | 200 | 10240 | 500 | 0 | 214.44 | 695.89 | 189.38 | 1287 | N/A | N/A |
|  XSLT Proxy | 512M | 200 | 10240 | 1000 | 0 | 143.12 | 1036.43 | 62.98 | 1351 | N/A | N/A |
|  XSLT Proxy | 512M | 200 | 102400 | 0 | 0 | 10.95 | 13029.62 | 3915.15 | 20095 | N/A | N/A |
|  XSLT Proxy | 512M | 200 | 102400 | 30 | 0 | 11.02 | 13012.11 | 4138.69 | 20095 | N/A | N/A |
|  XSLT Proxy | 512M | 200 | 102400 | 100 | 0 | 10.14 | 14288.64 | 5147.92 | 24063 | N/A | N/A |
|  XSLT Proxy | 512M | 200 | 102400 | 500 | 0 | 9.36 | 15291.34 | 5153.68 | 24447 | N/A | N/A |
|  XSLT Proxy | 512M | 200 | 102400 | 1000 | 0 | 8.2 | 16947.14 | 6682.34 | 27647 | N/A | N/A |
