# WSO2 Micro Integrator 4.0.0 Performance Test Results

During each release, we execute various automated performance test scenarios and publish the results.

| Test Scenarios | Description |
| --- | --- |
| Direct Proxy | Passthrough proxy service |
| Direct API | Passthrough API service |
| CBR SOAP Header Proxy | Routing the message based on a SOAP header in the message payload |
| CBR Transport Header Proxy | Routing the message based on an HTTP header in the message |
| CBR Proxy | Routing the message based on the content of the message body |
| XSLT Proxy | Having XSLT transformations in request and response paths |

Our test client is [Apache JMeter](https://jmeter.apache.org/index.html). We test each scenario for a fixed duration of
time. We split the test results into warmup and measurement parts and use the measurement part to compute the
performance metrics.

Test scenarios use a [Netty](https://netty.io/) based back-end service which echoes back any request
posted to it after a specified period of time.

We run the performance tests under different numbers of concurrent users, message sizes (payloads) and back-end service
delays.

The main performance metrics:

1. **Throughput**: The number of requests that the WSO2 Micro Integrator 4.0.0 processes during a specific time interval (e.g. per second).
2. **Response Time**: The end-to-end latency for an operation of invoking a service in WSO2 Micro Integrator 4.0.0 . The complete distribution of response times was recorded.

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


System information for WSO2 Micro Integrator 4.0.0 in 1st AWS CloudFormation stack.

| Class | Subclass | Description | Value |
| --- | --- | --- | --- |
| AWS | EC2 | AMI-ID | ami-0747bdcabd34c712a |
| AWS | EC2 | Instance Type | c5.large |
| System | Processor | CPU(s) | 2 |
| System | Processor | Thread(s) per core | 2 |
| System | Processor | Core(s) per socket | 1 |
| System | Processor | Socket(s) | 1 |
| System | Processor | Model name | Intel(R) Xeon(R) Platinum 8124M CPU @ 3.00GHz |
| System | Memory | BIOS | 64 KiB |
| System | Memory | System memory | 3785188 KiB |
| System | Storage | Block Device: nvme0n1 | 8G |
| Operating System | Distribution | Release | Ubuntu 18.04.5 LTS |
| Operating System | Distribution | Kernel | Linux ip-10-0-1-26 5.4.0-1045-aws #47~18.04.1-Ubuntu SMP Tue Apr 13 15:58:14 UTC 2021 x86_64 x86_64 x86_64 GNU/Linux |


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

|  Scenario Name | Heap Size | Concurrent Users | Message Size (Bytes) | Back-end Service Delay (ms) | Error % | Throughput (Requests/sec) | Average Response Time (ms) | Standard Deviation of Response Time (ms) | 99th Percentile of Response Time (ms) | WSO2 Micro Integrator 4.0.0 GC Throughput (%) | Average WSO2 Micro Integrator 4.0.0 Memory Footprint After Full GC (M) |
|---|---:|---:|---:|---:|---:|---:|---:|---:|---:|---:|---:|
|  Direct API | 2G | 100 | 500 | 0 | 0 | 256.59 | 389.59 | 955.94 | 3151 | N/A | N/A |
|  Direct API | 2G | 100 | 1000 | 0 | 0 | 213.3 | 468.73 | 1038.73 | 3199 | N/A | N/A |
|  Direct API | 2G | 100 | 10000 | 0 | 0 | 505.43 | 197.75 | 672.1 | 3055 | N/A | N/A |
|  Direct API | 2G | 100 | 100000 | 0 | 0 | 1235.02 | 80.74 | 30.38 | 207 | N/A | N/A |
|  Direct API | 2G | 200 | 500 | 0 | 0 | 142.54 | 1402.96 | 1699.56 | 6559 | N/A | N/A |
|  Direct API | 2G | 200 | 1000 | 0 | 0 | 186.27 | 1073.42 | 1543.95 | 6463 | N/A | N/A |
|  Direct API | 2G | 200 | 10000 | 0 | 0 | 287.49 | 695.54 | 1202.07 | 3279 | N/A | N/A |
|  Direct API | 2G | 200 | 100000 | 0 | 0 | 1181.79 | 168.9 | 49.14 | 327 | N/A | N/A |
|  Direct API | 2G | 500 | 500 | 0 | 0 | 114.27 | 4307.38 | 4055.56 | 19583 | N/A | N/A |
|  Direct API | 2G | 500 | 1000 | 0 | 0 | 188.29 | 2629.03 | 2627.3 | 12671 | N/A | N/A |
|  Direct API | 2G | 500 | 10000 | 0 | 0 | 331.14 | 1509.39 | 1802.51 | 6751 | N/A | N/A |
|  Direct API | 2G | 500 | 100000 | 0 | 0 | 1129.14 | 442.43 | 98.46 | 699 | N/A | N/A |
|  Direct API | 2G | 1000 | 500 | 0 | 0 | 114.5 | 6408.09 | 5746.17 | 26751 | N/A | N/A |
|  Direct API | 2G | 1000 | 1000 | 0 | 0 | 157.26 | 6205.34 | 5888.49 | 29439 | N/A | N/A |
|  Direct API | 2G | 1000 | 10000 | 0 | 0 | 189.21 | 5242.51 | 3807.11 | 16639 | N/A | N/A |
|  Direct API | 2G | 1000 | 100000 | 0 | 0 | 1072.47 | 929.87 | 172.28 | 1351 | N/A | N/A |
|  Direct Proxy | 2G | 100 | 500 | 0 | 0 | 4203.41 | 23.7 | 38.04 | 109 | 91.83 | 246.502 |
|  Direct Proxy | 2G | 100 | 1000 | 0 | 0 | 4228.91 | 23.55 | 37.33 | 108 | N/A | N/A |
|  Direct Proxy | 2G | 100 | 10000 | 0 | 0 | 3597.87 | 27.68 | 28.7 | 112 | N/A | N/A |
|  Direct Proxy | 2G | 100 | 100000 | 0 | 0 | 1541.12 | 64.66 | 31.19 | 145 | N/A | N/A |
|  Direct Proxy | 2G | 200 | 500 | 0 | 0 | 4268.31 | 46.72 | 54.2 | 157 | N/A | N/A |
|  Direct Proxy | 2G | 200 | 1000 | 0 | 0 | 4266.64 | 46.75 | 50.79 | 156 | N/A | N/A |
|  Direct Proxy | 2G | 200 | 10000 | 0 | 0 | 3607.31 | 55.3 | 42.58 | 166 | N/A | N/A |
|  Direct Proxy | 2G | 200 | 100000 | 0 | 0 | 1469.57 | 135.77 | 44.24 | 244 | N/A | N/A |
|  Direct Proxy | 2G | 500 | 500 | 0 | 0 | 4216.88 | 118.4 | 93.49 | 301 | N/A | N/A |
|  Direct Proxy | 2G | 500 | 1000 | 0 | 0 | 4213.42 | 118.5 | 93.95 | 309 | N/A | N/A |
|  Direct Proxy | 2G | 500 | 10000 | 0 | 0 | 802.46 | 622.68 | 1140.18 | 3551 | N/A | N/A |
|  Direct Proxy | 2G | 500 | 100000 | 0 | 0 | 1144.43 | 436.47 | 89.56 | 679 | N/A | N/A |
|  Direct Proxy | 2G | 1000 | 500 | 0 | 0 | 118.5 | 8236.81 | 6878.66 | 31743 | N/A | N/A |
|  Direct Proxy | 2G | 1000 | 1000 | 0 | 0 | 117.86 | 8089.82 | 7296.51 | 34559 | N/A | N/A |
|  Direct Proxy | 2G | 1000 | 10000 | 0 | 0 | 223.4 | 4431.86 | 3457.65 | 16191 | N/A | N/A |
|  Direct Proxy | 2G | 1000 | 100000 | 0 | 0 | 1092.06 | 913.55 | 171.2 | 1343 | N/A | N/A |
|  CBR SOAP Header Proxy | 2G | 100 | 500 | 0 | 0 | 596.84 | 167.46 | 603.29 | 2975 | N/A | N/A |
|  CBR SOAP Header Proxy | 2G | 100 | 1000 | 0 | 0 | 692.74 | 144.26 | 538.64 | 2911 | N/A | N/A |
|  CBR SOAP Header Proxy | 2G | 100 | 10000 | 0 | 0 | 1188.13 | 84.03 | 130.93 | 283 | N/A | N/A |
|  CBR SOAP Header Proxy | 2G | 100 | 100000 | 0 | 0 | 186.48 | 535.94 | 174.13 | 959 | N/A | N/A |
|  CBR SOAP Header Proxy | 2G | 200 | 500 | 0 | 0 | 426.66 | 468.66 | 1009.81 | 3215 | N/A | N/A |
|  CBR SOAP Header Proxy | 2G | 200 | 1000 | 0 | 0 | 499.63 | 400.16 | 913.84 | 3119 | N/A | N/A |
|  CBR SOAP Header Proxy | 2G | 200 | 10000 | 0 | 0 | 1164.48 | 171.63 | 195.57 | 443 | N/A | N/A |
|  CBR SOAP Header Proxy | 2G | 200 | 100000 | 0 | 0 | 150.73 | 1323.57 | 512.29 | 2975 | N/A | N/A |
|  CBR SOAP Header Proxy | 2G | 500 | 500 | 0 | 0 | 408.28 | 1224.3 | 1569.37 | 3743 | N/A | N/A |
|  CBR SOAP Header Proxy | 2G | 500 | 1000 | 0 | 0 | 273.21 | 1829.4 | 1985.13 | 7039 | N/A | N/A |
|  CBR SOAP Header Proxy | 2G | 500 | 10000 | 0 | 0 | 1016.81 | 491.4 | 461.06 | 3231 | N/A | N/A |
|  CBR SOAP Header Proxy | 2G | 500 | 100000 | 0 | 0 | 89.99 | 5511.07 | 2022.88 | 10367 | N/A | N/A |
|  CBR SOAP Header Proxy | 2G | 1000 | 500 | 0 | 0 | 214.25 | 4618.26 | 3495.35 | 16255 | N/A | N/A |
|  CBR SOAP Header Proxy | 2G | 1000 | 1000 | 0 | 0 | 150.81 | 6505.12 | 5103.71 | 25855 | N/A | N/A |
|  CBR SOAP Header Proxy | 2G | 1000 | 10000 | 0 | 0 | 728.97 | 1359.8 | 1032.94 | 4047 | N/A | N/A |
|  CBR SOAP Header Proxy | 2G | 1000 | 100000 | 0 | 0 | 29.86 | 31815.94 | 6966.25 | 46335 | N/A | N/A |
|  CBR Transport Header Proxy | 2G | 100 | 500 | 0 | 0 | 206.64 | 483.86 | 1041.71 | 3103 | N/A | N/A |
|  CBR Transport Header Proxy | 2G | 100 | 1000 | 0 | 0 | 230.38 | 433.99 | 992.3 | 3119 | N/A | N/A |
|  CBR Transport Header Proxy | 2G | 100 | 10000 | 0 | 0 | 473.27 | 211.23 | 678.29 | 2911 | N/A | N/A |
|  CBR Transport Header Proxy | 2G | 100 | 100000 | 0 | 0 | 1209.48 | 82.45 | 32.05 | 220 | N/A | N/A |
|  CBR Transport Header Proxy | 2G | 200 | 500 | 0 | 0 | 157.24 | 1271.52 | 1731.19 | 6431 | N/A | N/A |
|  CBR Transport Header Proxy | 2G | 200 | 1000 | 0 | 0 | 147.35 | 1351.22 | 1680.3 | 6399 | N/A | N/A |
|  CBR Transport Header Proxy | 2G | 200 | 10000 | 0 | 0 | 303.95 | 657.83 | 1154.5 | 3167 | N/A | N/A |
|  CBR Transport Header Proxy | 2G | 200 | 100000 | 0 | 0 | 1175.61 | 169.79 | 49.89 | 335 | N/A | N/A |
|  CBR Transport Header Proxy | 2G | 500 | 500 | 0 | 0 | 140.88 | 3497.28 | 3546.14 | 15679 | N/A | N/A |
|  CBR Transport Header Proxy | 2G | 500 | 1000 | 0 | 0 | 121 | 4071.73 | 3913.05 | 18687 | N/A | N/A |
|  CBR Transport Header Proxy | 2G | 500 | 10000 | 0 | 0 | 271.85 | 1827.5 | 2048.36 | 9279 | N/A | N/A |
|  CBR Transport Header Proxy | 2G | 500 | 100000 | 0 | 0 | 1121.12 | 445.5 | 96.24 | 695 | N/A | N/A |
|  CBR Transport Header Proxy | 2G | 1000 | 500 | 0 | 0 | 127.01 | 7579.35 | 6623.48 | 31359 | N/A | N/A |
|  CBR Transport Header Proxy | 2G | 1000 | 1000 | 0 | 0 | 126.69 | 7751.2 | 7199.54 | 36863 | N/A | N/A |
|  CBR Transport Header Proxy | 2G | 1000 | 10000 | 0 | 0 | 148.19 | 6627.1 | 4842.95 | 22143 | N/A | N/A |
|  CBR Transport Header Proxy | 2G | 1000 | 100000 | 0 | 0 | 1078.59 | 923.65 | 172.01 | 1351 | N/A | N/A |
|  CBR Proxy | 2G | 100 | 500 | 0 | 0 | 3474.97 | 28.66 | 31.84 | 108 | 93.33 | 195.391 |
|  CBR Proxy | 2G | 100 | 1000 | 0 | 0 | 3129.2 | 31.84 | 37.75 | 111 | N/A | N/A |
|  CBR Proxy | 2G | 100 | 10000 | 0 | 0 | 1128.79 | 88.41 | 56.42 | 271 | N/A | N/A |
|  CBR Proxy | 2G | 100 | 100000 | 0 | 0 | 119.21 | 837.31 | 225.47 | 1567 | N/A | N/A |
|  CBR Proxy | 2G | 200 | 500 | 0 | 0 | 3520.42 | 56.66 | 42.33 | 178 | N/A | N/A |
|  CBR Proxy | 2G | 200 | 1000 | 0 | 0 | 3156.5 | 63.21 | 56.36 | 194 | N/A | N/A |
|  CBR Proxy | 2G | 200 | 10000 | 0 | 0 | 1100.93 | 181.48 | 92.72 | 459 | N/A | N/A |
|  CBR Proxy | 2G | 200 | 100000 | 0 | 0 | 89.17 | 2234.84 | 808.55 | 4255 | N/A | N/A |
|  CBR Proxy | 2G | 500 | 500 | 0 | 0 | 3472.8 | 143.76 | 74.97 | 373 | N/A | N/A |
|  CBR Proxy | 2G | 500 | 1000 | 0 | 0 | 3114.5 | 160.29 | 88.42 | 403 | N/A | N/A |
|  CBR Proxy | 2G | 500 | 10000 | 0 | 0 | 999.88 | 499.66 | 190.44 | 1011 | N/A | N/A |
|  CBR Proxy | 2G | 500 | 100000 | 0 | 0 | 43.35 | 11302.74 | 3841.35 | 18559 | N/A | N/A |
|  CBR Proxy | 2G | 1000 | 500 | 0 | 0 | 2857.01 | 349.57 | 162.35 | 815 | N/A | N/A |
|  CBR Proxy | 2G | 1000 | 1000 | 0 | 0 | 3016.05 | 331.13 | 153.7 | 747 | N/A | N/A |
|  CBR Proxy | 2G | 1000 | 10000 | 0 | 0 | 803.79 | 1240.18 | 465.62 | 2831 | N/A | N/A |
|  CBR Proxy | 2G | 1000 | 100000 | 0 | 100 | 7.12 | 120377.22 | 2799.62 | 146431 | N/A | N/A |
|  XSLT Proxy | 2G | 100 | 500 | 0 | 0 | 1547.71 | 52.34 | 436.93 | 170 | 94.68 | 142.103 |
|  XSLT Proxy | 2G | 100 | 1000 | 0 | 0 | 1592.75 | 62.46 | 338.69 | 196 | N/A | N/A |
|  XSLT Proxy | 2G | 100 | 10000 | 0 | 0 | 360.72 | 276.95 | 152.31 | 723 | N/A | N/A |
|  XSLT Proxy | 2G | 100 | 100000 | 0 | 0 | 32 | 3098.78 | 589.29 | 4607 | N/A | N/A |
|  XSLT Proxy | 2G | 200 | 500 | 0 | 0 | 1972.36 | 101.23 | 62.2 | 283 | N/A | N/A |
|  XSLT Proxy | 2G | 200 | 1000 | 0 | 0 | 1608.68 | 124.15 | 72.39 | 331 | N/A | N/A |
|  XSLT Proxy | 2G | 200 | 10000 | 0 | 0 | 353.27 | 565.41 | 248.76 | 1255 | N/A | N/A |
|  XSLT Proxy | 2G | 200 | 100000 | 0 | 0 | 27.51 | 7129.06 | 1388.78 | 9919 | N/A | N/A |
|  XSLT Proxy | 2G | 500 | 500 | 0 | 0 | 1871.08 | 267.12 | 126.28 | 655 | 92.67 | 241.46 |
|  XSLT Proxy | 2G | 500 | 1000 | 0 | 0 | 1606.98 | 311.04 | 141.12 | 739 | N/A | N/A |
|  XSLT Proxy | 2G | 500 | 10000 | 0 | 0 | 312.37 | 1594.62 | 451.21 | 2943 | N/A | N/A |
|  XSLT Proxy | 2G | 500 | 100000 | 0 | 0 | 14.29 | 31965.47 | 5126.3 | 45567 | N/A | N/A |
|  XSLT Proxy | 2G | 1000 | 500 | 0 | 0 | 1676.77 | 595.24 | 217.13 | 1223 | N/A | N/A |
|  XSLT Proxy | 2G | 1000 | 1000 | 0 | 0 | 1425.89 | 699.79 | 250.84 | 1479 | N/A | N/A |
|  XSLT Proxy | 2G | 1000 | 10000 | 0 | 0 | 268.24 | 3694.27 | 927.61 | 5983 | N/A | N/A |
|  XSLT Proxy | 2G | 1000 | 100000 | 0 | 96.46 | 5.69 | 120001.16 | 4870.34 | 136191 | N/A | N/A |

