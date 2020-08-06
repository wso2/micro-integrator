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
| Heap Size | The amount of memory allocated to the application | 2G |
| Concurrent Users | The number of users accessing the application at the same time. | 100, 200, 500, 1000 |
| Message Size (Bytes) | The request payload size in Bytes. | 500, 1024, 10240, 102400 |
| Back-end Delay (ms) | The delay added by the Back-end service. | 0 |

The duration of each test is **60 seconds**. The warm-up period is **30 seconds**.
The measurement results are collected after the warm-up period.

The performance tests were executed on 2 AWS CloudFormation stacks.


System information for WSO2 Micro Integrator 1.2.0 in 1st AWS CloudFormation stack.

| Class | Subclass | Description | Value |
| --- | --- | --- | --- |
| AWS | EC2 | AMI-ID | ami-07df16d0682f1fa59 |
| AWS | EC2 | Instance Type | c5.large |
| System | Processor | CPU(s) | 2 |
| System | Processor | Thread(s) per core | 2 |
| System | Processor | Core(s) per socket | 1 |
| System | Processor | Socket(s) | 1 |
| System | Processor | Model name | Intel(R) Xeon(R) Platinum 8124M CPU @ 3.00GHz |
| System | Memory | BIOS | 64 KiB |
| System | Memory | System memory | 3785216 KiB |
| System | Storage | Block Device: nvme0n1 | 8G |
| Operating System | Distribution | Release | Ubuntu 18.04.4 LTS |
| Operating System | Distribution | Kernel | Linux ip-10-0-1-48 5.3.0-1032-aws #34~18.04.2-Ubuntu SMP Fri Jul 24 10:06:28 UTC 2020 x86_64 x86_64 x86_64 GNU/Linux |
System information for WSO2 Micro Integrator 1.2.0 in 2nd AWS CloudFormation stack.

| Class | Subclass | Description | Value |
| --- | --- | --- | --- |
| AWS | EC2 | AMI-ID | ami-07df16d0682f1fa59 |
| AWS | EC2 | Instance Type | c5.large |
| System | Processor | CPU(s) | 2 |
| System | Processor | Thread(s) per core | 2 |
| System | Processor | Core(s) per socket | 1 |
| System | Processor | Socket(s) | 1 |
| System | Processor | Model name | Intel(R) Xeon(R) Platinum 8124M CPU @ 3.00GHz |
| System | Memory | BIOS | 64 KiB |
| System | Memory | System memory | 3785216 KiB |
| System | Storage | Block Device: nvme0n1 | 8G |
| Operating System | Distribution | Release | Ubuntu 18.04.4 LTS |
| Operating System | Distribution | Kernel | Linux ip-10-0-1-99 5.3.0-1032-aws #34~18.04.2-Ubuntu SMP Fri Jul 24 10:06:28 UTC 2020 x86_64 x86_64 x86_64 GNU/Linux |


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
|  CBR Proxy | 2G | 100 | 500 | 0 | 0 | 3213.49 | 23.44 | 16.65 | 96 | N/A | N/A |
|  CBR Proxy | 2G | 100 | 1024 | 0 | 0 | 2655.13 | 28.36 | 46.19 | 99 | N/A | N/A |
|  CBR Proxy | 2G | 100 | 10240 | 0 | 0 | 763.36 | 98.86 | 48.05 | 243 | N/A | N/A |
|  CBR Proxy | 2G | 100 | 102400 | 0 | 0 | 76.24 | 985.11 | 406.17 | 2639 | N/A | N/A |
|  CBR Proxy | 2G | 200 | 500 | 0 | 0 | 2998.07 | 50.09 | 75.92 | 153 | N/A | N/A |
|  CBR Proxy | 2G | 200 | 1024 | 0 | 0 | 2695.5 | 55.77 | 77.88 | 161 | N/A | N/A |
|  CBR Proxy | 2G | 200 | 10240 | 0 | 0 | 773.31 | 192.97 | 87.54 | 429 | N/A | N/A |
|  CBR Proxy | 2G | 200 | 102400 | 0 | 0 | 60.22 | 2481.05 | 1002.13 | 4895 | N/A | N/A |
|  CBR Proxy | 2G | 500 | 500 | 0 | 0 | 3206.4 | 116.7 | 64.35 | 323 | N/A | N/A |
|  CBR Proxy | 2G | 500 | 1024 | 0 | 0 | 2738.98 | 136.97 | 97.6 | 379 | N/A | N/A |
|  CBR Proxy | 2G | 500 | 10240 | 0 | 0 | 737.5 | 508.9 | 213.29 | 1103 | N/A | N/A |
|  CBR Proxy | 2G | 500 | 102400 | 0 | 0 | 35.81 | 10356.49 | 3872.92 | 17023 | N/A | N/A |
|  CBR Proxy | 2G | 1000 | 500 | 0 | 0 | 2693.76 | 278.94 | 138.99 | 671 | N/A | N/A |
|  CBR Proxy | 2G | 1000 | 1024 | 0 | 0 | 2281.03 | 327.35 | 206.89 | 1359 | N/A | N/A |
|  CBR Proxy | 2G | 1000 | 10240 | 0 | 0 | 618.91 | 1207.07 | 513.15 | 2879 | N/A | N/A |
|  CBR Proxy | 2G | 1000 | 102400 | 0 | 0 | 15.95 | 46127.8 | 8972.91 | 61695 | N/A | N/A |
|  CBR SOAP Header Proxy | 2G | 100 | 500 | 0 | 0 | 3304.55 | 22.78 | 17.55 | 99 | N/A | N/A |
|  CBR SOAP Header Proxy | 2G | 100 | 1024 | 0 | 0 | 3060.66 | 24.59 | 17.77 | 100 | N/A | N/A |
|  CBR SOAP Header Proxy | 2G | 100 | 10240 | 0 | 0 | 1120.2 | 67.35 | 33.1 | 166 | N/A | N/A |
|  CBR SOAP Header Proxy | 2G | 100 | 102400 | 0 | 0 | 152.63 | 493.53 | 180.73 | 951 | N/A | N/A |
|  CBR SOAP Header Proxy | 2G | 200 | 500 | 0 | 0 | 3272.25 | 45.91 | 29.47 | 147 | N/A | N/A |
|  CBR SOAP Header Proxy | 2G | 200 | 1024 | 0 | 0 | 3123.31 | 48.12 | 30.55 | 160 | N/A | N/A |
|  CBR SOAP Header Proxy | 2G | 200 | 10240 | 0 | 0 | 1093.83 | 138.43 | 92.87 | 323 | N/A | N/A |
|  CBR SOAP Header Proxy | 2G | 200 | 102400 | 0 | 0 | 120.62 | 1249.78 | 552.84 | 2959 | N/A | N/A |
|  CBR SOAP Header Proxy | 2G | 500 | 500 | 0 | 0 | 2859.24 | 131.19 | 88.7 | 399 | N/A | N/A |
|  CBR SOAP Header Proxy | 2G | 500 | 1024 | 0 | 0 | 2613.1 | 144.06 | 92 | 407 | N/A | N/A |
|  CBR SOAP Header Proxy | 2G | 500 | 10240 | 0 | 0 | 978.09 | 384.33 | 190.03 | 1223 | N/A | N/A |
|  CBR SOAP Header Proxy | 2G | 500 | 102400 | 0 | 0 | 77.17 | 4731.26 | 2294.23 | 10367 | N/A | N/A |
|  CBR SOAP Header Proxy | 2G | 1000 | 500 | 0 | 0 | 2733.25 | 272.44 | 139.41 | 639 | N/A | N/A |
|  CBR SOAP Header Proxy | 2G | 1000 | 1024 | 0 | 0 | 2460.73 | 305.01 | 178.64 | 927 | N/A | N/A |
|  CBR SOAP Header Proxy | 2G | 1000 | 10240 | 0 | 0 | 882.86 | 845.99 | 398.09 | 2495 | N/A | N/A |
|  CBR SOAP Header Proxy | 2G | 1000 | 102400 | 0 | 0 | 39.01 | 19057.95 | 7021.43 | 30975 | N/A | N/A |
|  CBR Transport Header Proxy | 2G | 100 | 500 | 0 | 0 | 3810.42 | 19.75 | 45.08 | 102 | N/A | N/A |
|  CBR Transport Header Proxy | 2G | 100 | 1024 | 0 | 0 | 3623.59 | 20.75 | 59.31 | 103 | N/A | N/A |
|  CBR Transport Header Proxy | 2G | 100 | 10240 | 0 | 0 | 2725.48 | 27.59 | 28.32 | 111 | N/A | N/A |
|  CBR Transport Header Proxy | 2G | 100 | 102400 | 0 | 0 | 957.47 | 78.52 | 27.56 | 187 | N/A | N/A |
|  CBR Transport Header Proxy | 2G | 200 | 500 | 0 | 0 | 4056.94 | 36.96 | 27.18 | 145 | N/A | N/A |
|  CBR Transport Header Proxy | 2G | 200 | 1024 | 0 | 0 | 4159.04 | 36.09 | 26.89 | 138 | N/A | N/A |
|  CBR Transport Header Proxy | 2G | 200 | 10240 | 0 | 0 | 2876.93 | 52.09 | 28.79 | 152 | N/A | N/A |
|  CBR Transport Header Proxy | 2G | 200 | 102400 | 0 | 0 | 892.12 | 168.38 | 52.2 | 315 | N/A | N/A |
|  CBR Transport Header Proxy | 2G | 500 | 500 | 0 | 0 | 3389.24 | 110.89 | 77.88 | 345 | N/A | N/A |
|  CBR Transport Header Proxy | 2G | 500 | 1024 | 0 | 0 | 3392.93 | 109.22 | 74.83 | 353 | N/A | N/A |
|  CBR Transport Header Proxy | 2G | 500 | 10240 | 0 | 0 | 2449.18 | 152.92 | 127.77 | 395 | N/A | N/A |
|  CBR Transport Header Proxy | 2G | 500 | 102400 | 0 | 0 | 857.23 | 437.18 | 206.29 | 1047 | N/A | N/A |
|  CBR Transport Header Proxy | 2G | 1000 | 500 | 0 | 0 | 3081.32 | 240.95 | 207.34 | 655 | N/A | N/A |
|  CBR Transport Header Proxy | 2G | 1000 | 1024 | 0 | 0 | 3330.34 | 223.66 | 171.06 | 599 | N/A | N/A |
|  CBR Transport Header Proxy | 2G | 1000 | 10240 | 0 | 0 | 2654.01 | 281.94 | 126.28 | 615 | N/A | N/A |
|  CBR Transport Header Proxy | 2G | 1000 | 102400 | 0 | 0 | 807.67 | 920.77 | 368.18 | 1903 | N/A | N/A |
|  Direct Proxy | 2G | 100 | 500 | 0 | 0 | 2958.15 | 25.46 | 23.26 | 133 | 94.41 | 28.186 |
|  Direct Proxy | 2G | 100 | 1024 | 0 | 0 | 3552.89 | 21.19 | 49.44 | 110 | N/A | N/A |
|  Direct Proxy | 2G | 100 | 10240 | 0 | 0 | 2760.35 | 27.24 | 28.96 | 112 | N/A | N/A |
|  Direct Proxy | 2G | 100 | 102400 | 0 | 0 | 980.26 | 76.73 | 26.97 | 181 | N/A | N/A |
|  Direct Proxy | 2G | 200 | 500 | 0 | 0 | 4025.74 | 36.49 | 27.71 | 140 | N/A | N/A |
|  Direct Proxy | 2G | 200 | 1024 | 0 | 0 | 4327.59 | 34.65 | 25.69 | 138 | N/A | N/A |
|  Direct Proxy | 2G | 200 | 10240 | 0 | 0 | 2911.83 | 51.49 | 29.52 | 158 | N/A | N/A |
|  Direct Proxy | 2G | 200 | 102400 | 0 | 0 | 914.25 | 164.24 | 51.07 | 305 | N/A | N/A |
|  Direct Proxy | 2G | 500 | 500 | 0 | 0 | 3028.85 | 124.04 | 70.69 | 347 | 95.58 | 28.156 |
|  Direct Proxy | 2G | 500 | 1024 | 0 | 0 | 3716.3 | 100.77 | 58.77 | 293 | N/A | N/A |
|  Direct Proxy | 2G | 500 | 10240 | 0 | 0 | 2996.02 | 125.41 | 57.33 | 295 | N/A | N/A |
|  Direct Proxy | 2G | 500 | 102400 | 0 | 0 | 845.5 | 443.34 | 122.42 | 787 | N/A | N/A |
|  Direct Proxy | 2G | 1000 | 500 | 0 | 0 | 4082.35 | 182.11 | 110.9 | 505 | N/A | N/A |
|  Direct Proxy | 2G | 1000 | 1024 | 0 | 0 | 3988.01 | 187.64 | 199.5 | 493 | N/A | N/A |
|  Direct Proxy | 2G | 1000 | 10240 | 0 | 0 | 2844.9 | 263.66 | 192.14 | 635 | N/A | N/A |
|  Direct Proxy | 2G | 1000 | 102400 | 0 | 0 | 834.95 | 893.64 | 321.39 | 1711 | N/A | N/A |
|  XSLT Enhanced Proxy | 2G | 100 | 500 | 0 | 0 | 2505.52 | 30.06 | 26.54 | 132 | N/A | N/A |
|  XSLT Enhanced Proxy | 2G | 100 | 1024 | 0 | 0 | 2198.15 | 34.34 | 40.67 | 155 | N/A | N/A |
|  XSLT Enhanced Proxy | 2G | 100 | 10240 | 0 | 0 | 535.01 | 140.88 | 60.12 | 313 | N/A | N/A |
|  XSLT Enhanced Proxy | 2G | 100 | 102400 | 0 | 0 | 69.46 | 1076.91 | 341.82 | 2047 | N/A | N/A |
|  XSLT Enhanced Proxy | 2G | 200 | 500 | 0 | 0 | 2449.05 | 61.4 | 59.35 | 269 | N/A | N/A |
|  XSLT Enhanced Proxy | 2G | 200 | 1024 | 0 | 0 | 2325.07 | 64.64 | 60.11 | 283 | N/A | N/A |
|  XSLT Enhanced Proxy | 2G | 200 | 10240 | 0 | 0 | 546.79 | 275.47 | 105.3 | 579 | N/A | N/A |
|  XSLT Enhanced Proxy | 2G | 200 | 102400 | 0 | 0 | 68.6 | 2157.15 | 557 | 3615 | N/A | N/A |
|  XSLT Enhanced Proxy | 2G | 500 | 500 | 0 | 0 | 2173.15 | 172.49 | 176.25 | 603 | N/A | N/A |
|  XSLT Enhanced Proxy | 2G | 500 | 1024 | 0 | 0 | 2121.14 | 176.81 | 134.16 | 615 | N/A | N/A |
|  XSLT Enhanced Proxy | 2G | 500 | 10240 | 0 | 0 | 528.04 | 709.97 | 222.57 | 1255 | N/A | N/A |
|  XSLT Enhanced Proxy | 2G | 500 | 102400 | 0 | 0 | 60.79 | 5924.8 | 1470.44 | 9663 | N/A | N/A |
|  XSLT Enhanced Proxy | 2G | 1000 | 500 | 0 | 0 | 2341.1 | 320.31 | 254.13 | 1799 | N/A | N/A |
|  XSLT Enhanced Proxy | 2G | 1000 | 1024 | 0 | 0 | 2218.3 | 337.04 | 215.07 | 1279 | N/A | N/A |
|  XSLT Enhanced Proxy | 2G | 1000 | 10240 | 0 | 0 | 517.29 | 1441.32 | 452.75 | 2879 | N/A | N/A |
|  XSLT Enhanced Proxy | 2G | 1000 | 102400 | 0 | 0 | 57.47 | 11691.64 | 1647.89 | 14463 | N/A | N/A |
|  XSLT Proxy | 2G | 100 | 500 | 0 | 0 | 1903.9 | 39.66 | 27.05 | 136 | N/A | N/A |
|  XSLT Proxy | 2G | 100 | 1024 | 0 | 0 | 1493.18 | 50.5 | 40.73 | 160 | N/A | N/A |
|  XSLT Proxy | 2G | 100 | 10240 | 0 | 0 | 327.45 | 230.53 | 134.43 | 627 | N/A | N/A |
|  XSLT Proxy | 2G | 100 | 102400 | 0 | 0 | 31.53 | 2340.56 | 533.13 | 3663 | N/A | N/A |
|  XSLT Proxy | 2G | 200 | 500 | 0 | 0 | 1853.94 | 81.11 | 72.95 | 236 | N/A | N/A |
|  XSLT Proxy | 2G | 200 | 1024 | 0 | 0 | 1568.97 | 95.82 | 57.56 | 287 | N/A | N/A |
|  XSLT Proxy | 2G | 200 | 10240 | 0 | 0 | 328.42 | 457.28 | 204.11 | 1039 | N/A | N/A |
|  XSLT Proxy | 2G | 200 | 102400 | 0 | 0 | 27 | 5298.08 | 1519.17 | 8767 | N/A | N/A |
|  XSLT Proxy | 2G | 500 | 500 | 0 | 0 | 1484 | 251.65 | 132.02 | 647 | N/A | N/A |
|  XSLT Proxy | 2G | 500 | 1024 | 0 | 0 | 1405.05 | 267.62 | 134.71 | 667 | N/A | N/A |
|  XSLT Proxy | 2G | 500 | 10240 | 0 | 0 | 300.64 | 1242.44 | 423.98 | 2463 | N/A | N/A |
|  XSLT Proxy | 2G | 500 | 102400 | 0 | 0 | 17.8 | 18845.27 | 3116.01 | 26495 | N/A | N/A |
|  XSLT Proxy | 2G | 1000 | 500 | 0 | 0 | 1640.52 | 456.68 | 203.58 | 1063 | N/A | N/A |
|  XSLT Proxy | 2G | 1000 | 1024 | 0 | 0 | 1363.57 | 549.46 | 236.47 | 1191 | N/A | N/A |
|  XSLT Proxy | 2G | 1000 | 10240 | 0 | 0 | 261.56 | 2787.58 | 900.23 | 5055 | N/A | N/A |
|  XSLT Proxy | 2G | 1000 | 102400 | 0 | 0 | 11.08 | 48776.5 | 4692.4 | 58623 | N/A | N/A |
