# WSO2 Micro Integrator 1.2.0-Prometheus Performance Test Results

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

1. **Throughput**: The number of requests that the WSO2 Micro Integrator 1.2.0-Prometheus processes during a specific time interval (e.g. per second).
2. **Response Time**: The end-to-end latency for an operation of invoking a service in WSO2 Micro Integrator 1.2.0-Prometheus . The complete distribution of response times was recorded.

In addition to the above metrics, we measure the load average and several memory-related metrics.

The following are the test parameters.

| Test Parameter | Description | Values |
| --- | --- | --- |
| Scenario Name | The name of the test scenario. | Refer to the above table. |
| Heap Size | The amount of memory allocated to the application | 1G |
| Concurrent Users | The number of users accessing the application at the same time. | 500, 1000 |
| Message Size (Bytes) | The request payload size in Bytes. | 500, 1024, 10240, 102400 |
| Back-end Delay (ms) | The delay added by the Back-end service. | 0, 30, 100, 500, 1000 |

The duration of each test is **60 seconds**. The warm-up period is **30 seconds**.
The measurement results are collected after the warm-up period.

The performance tests were executed on 1 AWS CloudFormation stack.


System information for WSO2 Micro Integrator 1.2.0-Prometheus in 1st AWS CloudFormation stack.

| Class | Subclass | Description | Value |
| --- | --- | --- | --- |
| AWS | EC2 | AMI-ID | ami-0ac80df6eff0e70b5 |
| AWS | EC2 | Instance Type | c5.xlarge |
| System | Processor | CPU(s) | 4 |
| System | Processor | Thread(s) per core | 2 |
| System | Processor | Core(s) per socket | 2 |
| System | Processor | Socket(s) | 1 |
| System | Processor | Model name | Intel(R) Xeon(R) Platinum 8124M CPU @ 3.00GHz |
| System | Memory | BIOS | 64 KiB |
| System | Memory | System memory | 7792924 KiB |
| System | Storage | Block Device: nvme0n1 | 8G |
| Operating System | Distribution | Release | Ubuntu 18.04.4 LTS |
| Operating System | Distribution | Kernel | Linux ip-10-0-1-21 5.3.0-1023-aws #25~18.04.1-Ubuntu SMP Fri Jun 5 15:18:30 UTC 2020 x86_64 x86_64 x86_64 GNU/Linux |


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
|  CBR Proxy | 1G | 500 | 500 | 0 | 0 | 1391.1 | 271.9 | 397.76 | 1111 | N/A | N/A |
|  CBR Proxy | 1G | 500 | 500 | 30 | 0 | 4248.4 | 89.09 | 102.22 | 195 | N/A | N/A |
|  CBR Proxy | 1G | 500 | 500 | 100 | 0 | 2882.98 | 129.9 | 25.59 | 205 | N/A | N/A |
|  CBR Proxy | 1G | 500 | 500 | 500 | 0 | 735.24 | 505.87 | 20.96 | 555 | N/A | N/A |
|  CBR Proxy | 1G | 500 | 500 | 1000 | 0 | 367.68 | 1002.41 | 3.1 | 1023 | N/A | N/A |
|  CBR Proxy | 1G | 500 | 1024 | 0 | 0 | 4461.71 | 84.23 | 120.57 | 943 | N/A | N/A |
|  CBR Proxy | 1G | 500 | 1024 | 30 | 0 | 3702.63 | 101.47 | 133.4 | 899 | N/A | N/A |
|  CBR Proxy | 1G | 500 | 1024 | 100 | 0 | 2784.4 | 134.69 | 62.05 | 217 | N/A | N/A |
|  CBR Proxy | 1G | 500 | 1024 | 500 | 0 | 734.87 | 505.81 | 16.38 | 551 | N/A | N/A |
|  CBR Proxy | 1G | 500 | 1024 | 1000 | 0 | 367.27 | 1002.5 | 3.38 | 1023 | N/A | N/A |
|  CBR Proxy | 1G | 500 | 10240 | 0 | 0 | 1081.11 | 346.69 | 168 | 903 | N/A | N/A |
|  CBR Proxy | 1G | 500 | 10240 | 30 | 0 | 1105.24 | 340.13 | 159.97 | 879 | N/A | N/A |
|  CBR Proxy | 1G | 500 | 10240 | 100 | 0 | 1086.03 | 345.84 | 148.62 | 891 | N/A | N/A |
|  CBR Proxy | 1G | 500 | 10240 | 500 | 0 | 661.91 | 562.3 | 89.94 | 935 | N/A | N/A |
|  CBR Proxy | 1G | 500 | 10240 | 1000 | 0 | 360.19 | 1027.78 | 47.91 | 1239 | N/A | N/A |
|  CBR Proxy | 1G | 500 | 102400 | 0 | 0 | 19.86 | 18885.04 | 8454.55 | 31615 | N/A | N/A |
|  CBR Proxy | 1G | 500 | 102400 | 30 | 0 | 20.71 | 18210.38 | 7979.2 | 33279 | N/A | N/A |
|  CBR Proxy | 1G | 500 | 102400 | 100 | 0 | 18.66 | 20732.8 | 9800.95 | 36607 | N/A | N/A |
|  CBR Proxy | 1G | 500 | 102400 | 500 | 0 | 17.18 | 21056.35 | 9023.13 | 35839 | N/A | N/A |
|  CBR Proxy | 1G | 500 | 102400 | 1000 | 84.72 | 2.79 | 103036.46 | 43045.58 | 162815 | N/A | N/A |
|  CBR Proxy | 1G | 1000 | 500 | 0 | 100 | 20840.82 | 29.2 | 50.1 | 263 | N/A | N/A |
|  CBR Proxy | 1G | 1000 | 500 | 30 | 100 | 20882.31 | 29.23 | 52.82 | 259 | N/A | N/A |
|  CBR Proxy | 1G | 1000 | 500 | 100 | 100 | 20262.24 | 29.77 | 52.47 | 273 | N/A | N/A |
|  CBR Proxy | 1G | 1000 | 500 | 500 | 100 | 20630.71 | 30.06 | 48.84 | 252 | N/A | N/A |
|  CBR Proxy | 1G | 1000 | 500 | 1000 | 100 | 20010.37 | 29.58 | 48.98 | 261 | N/A | N/A |
|  CBR Proxy | 1G | 1000 | 1024 | 0 | 100 | 20362.71 | 30.54 | 47.47 | 241 | N/A | N/A |
|  CBR Proxy | 1G | 1000 | 1024 | 30 | 100 | 21042.48 | 29.22 | 49.29 | 265 | N/A | N/A |
|  CBR Proxy | 1G | 1000 | 1024 | 100 | 100 | 20338.31 | 30.4 | 53.89 | 277 | N/A | N/A |
|  CBR Proxy | 1G | 1000 | 1024 | 500 | 100 | 20719.5 | 29.41 | 45.02 | 233 | N/A | N/A |
|  CBR Proxy | 1G | 1000 | 1024 | 1000 | 100 | 20083.21 | 29.64 | 48.06 | 246 | N/A | N/A |
|  CBR Proxy | 1G | 1000 | 10240 | 0 | 100 | 20262.24 | 30.72 | 53.18 | 285 | N/A | N/A |
|  CBR Proxy | 1G | 1000 | 10240 | 30 | 100 | 21110.53 | 29.13 | 48.99 | 254 | N/A | N/A |
|  CBR Proxy | 1G | 1000 | 10240 | 100 | 100 | 19834.47 | 29.99 | 47.26 | 246 | N/A | N/A |
|  CBR Proxy | 1G | 1000 | 10240 | 500 | 100 | 20720.45 | 29.12 | 47.37 | 245 | N/A | N/A |
|  CBR Proxy | 1G | 1000 | 10240 | 1000 | 100 | 20417.15 | 30.49 | 51.23 | 263 | N/A | N/A |
|  CBR Proxy | 1G | 1000 | 102400 | 0 | 100 | 20237.73 | 30.18 | 52.37 | 269 | N/A | N/A |
|  CBR Proxy | 1G | 1000 | 102400 | 30 | 100 | 20517.02 | 29 | 46.4 | 246 | N/A | N/A |
|  CBR Proxy | 1G | 1000 | 102400 | 100 | 100 | 20478.82 | 29.75 | 46.65 | 238 | N/A | N/A |
|  CBR Proxy | 1G | 1000 | 102400 | 500 | 100 | 20134.59 | 30.53 | 49.83 | 257 | N/A | N/A |
|  CBR Proxy | 1G | 1000 | 102400 | 1000 | 100 | 21124.04 | 28.84 | 52.78 | 275 | N/A | N/A |
|  CBR SOAP Header Proxy | 1G | 500 | 500 | 0 | 100 | 23876.52 | 13.55 | 24.98 | 137 | N/A | N/A |
|  CBR SOAP Header Proxy | 1G | 500 | 500 | 30 | 100 | 24006.21 | 13.44 | 24.34 | 135 | N/A | N/A |
|  CBR SOAP Header Proxy | 1G | 500 | 500 | 100 | 100 | 24275.05 | 13.32 | 23.63 | 126 | N/A | N/A |
|  CBR SOAP Header Proxy | 1G | 500 | 500 | 500 | 100 | 24040.86 | 13.62 | 23.54 | 126 | N/A | N/A |
|  CBR SOAP Header Proxy | 1G | 500 | 500 | 1000 | 100 | 23672.6 | 13.71 | 24.92 | 141 | N/A | N/A |
|  CBR SOAP Header Proxy | 1G | 500 | 1024 | 0 | 100 | 23484.18 | 13.89 | 25.23 | 132 | N/A | N/A |
|  CBR SOAP Header Proxy | 1G | 500 | 1024 | 30 | 100 | 24167.87 | 13.22 | 23.72 | 133 | N/A | N/A |
|  CBR SOAP Header Proxy | 1G | 500 | 1024 | 100 | 100 | 23912.23 | 13.55 | 25.29 | 142 | N/A | N/A |
|  CBR SOAP Header Proxy | 1G | 500 | 1024 | 500 | 100 | 24026.99 | 13.35 | 24.56 | 133 | N/A | N/A |
|  CBR SOAP Header Proxy | 1G | 500 | 1024 | 1000 | 100 | 23755.19 | 13.51 | 24.47 | 135 | N/A | N/A |
|  CBR SOAP Header Proxy | 1G | 500 | 10240 | 0 | 100 | 23866.13 | 13.59 | 22.77 | 125 | N/A | N/A |
|  CBR SOAP Header Proxy | 1G | 500 | 10240 | 30 | 100 | 23729.68 | 13.54 | 24.54 | 132 | N/A | N/A |
|  CBR SOAP Header Proxy | 1G | 500 | 10240 | 100 | 100 | 23982.27 | 13.55 | 23.16 | 128 | N/A | N/A |
|  CBR SOAP Header Proxy | 1G | 500 | 10240 | 500 | 100 | 23949.82 | 13.39 | 22.12 | 120 | N/A | N/A |
|  CBR SOAP Header Proxy | 1G | 500 | 10240 | 1000 | 100 | 24081.67 | 13.43 | 24.2 | 132 | N/A | N/A |
|  CBR SOAP Header Proxy | 1G | 500 | 102400 | 0 | 100 | 24087.41 | 13.51 | 23.36 | 127 | N/A | N/A |
|  CBR SOAP Header Proxy | 1G | 500 | 102400 | 30 | 100 | 24199.46 | 13.4 | 21.79 | 118 | N/A | N/A |
|  CBR SOAP Header Proxy | 1G | 500 | 102400 | 100 | 100 | 24116.36 | 13.46 | 24.54 | 132 | N/A | N/A |
|  CBR SOAP Header Proxy | 1G | 500 | 102400 | 500 | 100 | 23716.17 | 13.73 | 23.31 | 129 | N/A | N/A |
|  CBR SOAP Header Proxy | 1G | 500 | 102400 | 1000 | 100 | 24094.22 | 13.46 | 23.36 | 127 | N/A | N/A |
|  CBR SOAP Header Proxy | 1G | 1000 | 500 | 0 | 100 | 21620.29 | 27.28 | 49.93 | 265 | N/A | N/A |
|  CBR SOAP Header Proxy | 1G | 1000 | 500 | 30 | 100 | 20773.36 | 30.05 | 60.25 | 307 | N/A | N/A |
|  CBR SOAP Header Proxy | 1G | 1000 | 500 | 100 | 100 | 20142.26 | 29.69 | 55.18 | 291 | N/A | N/A |
|  CBR SOAP Header Proxy | 1G | 1000 | 500 | 500 | 100 | 20385.46 | 28.77 | 51.97 | 271 | N/A | N/A |
|  CBR SOAP Header Proxy | 1G | 1000 | 500 | 1000 | 100 | 21070.12 | 28.92 | 52.21 | 271 | N/A | N/A |
|  CBR SOAP Header Proxy | 1G | 1000 | 1024 | 0 | 100 | 20938.04 | 29.36 | 51.85 | 271 | N/A | N/A |
|  CBR SOAP Header Proxy | 1G | 1000 | 1024 | 30 | 100 | 21274.94 | 28.04 | 46.81 | 249 | N/A | N/A |
|  CBR SOAP Header Proxy | 1G | 1000 | 1024 | 100 | 100 | 20941.9 | 28.99 | 52.15 | 277 | N/A | N/A |
|  CBR SOAP Header Proxy | 1G | 1000 | 1024 | 500 | 100 | 21021.78 | 29.64 | 58.02 | 289 | N/A | N/A |
|  CBR SOAP Header Proxy | 1G | 1000 | 1024 | 1000 | 100 | 20693.4 | 29.04 | 54.33 | 285 | N/A | N/A |
|  CBR SOAP Header Proxy | 1G | 1000 | 10240 | 0 | 100 | 20872.73 | 28.75 | 56.44 | 289 | N/A | N/A |
|  CBR SOAP Header Proxy | 1G | 1000 | 10240 | 30 | 100 | 20615.05 | 29.58 | 56.71 | 295 | N/A | N/A |
|  CBR SOAP Header Proxy | 1G | 1000 | 10240 | 100 | 100 | 20629.16 | 29.96 | 56.65 | 295 | N/A | N/A |
|  CBR SOAP Header Proxy | 1G | 1000 | 10240 | 500 | 100 | 20816.23 | 29.48 | 55.86 | 305 | N/A | N/A |
|  CBR SOAP Header Proxy | 1G | 1000 | 10240 | 1000 | 100 | 21464.17 | 28.63 | 52.83 | 271 | N/A | N/A |
|  CBR SOAP Header Proxy | 1G | 1000 | 102400 | 0 | 100 | 20806.3 | 29.48 | 57.86 | 293 | N/A | N/A |
|  CBR SOAP Header Proxy | 1G | 1000 | 102400 | 30 | 100 | 20975.4 | 29.22 | 57.12 | 295 | N/A | N/A |
|  CBR SOAP Header Proxy | 1G | 1000 | 102400 | 100 | 100 | 20547.66 | 30.09 | 54.32 | 299 | N/A | N/A |
|  CBR SOAP Header Proxy | 1G | 1000 | 102400 | 500 | 100 | 20621.74 | 29.9 | 52.37 | 279 | N/A | N/A |
|  CBR SOAP Header Proxy | 1G | 1000 | 102400 | 1000 | 100 | 20472.47 | 29.56 | 55.77 | 301 | N/A | N/A |
|  CBR Transport Header Proxy | 1G | 500 | 500 | 0 | 100 | 23576.45 | 13.7 | 23.56 | 128 | N/A | N/A |
|  CBR Transport Header Proxy | 1G | 500 | 500 | 30 | 100 | 23700.12 | 13.61 | 23.53 | 130 | N/A | N/A |
|  CBR Transport Header Proxy | 1G | 500 | 500 | 100 | 100 | 24095.53 | 13.46 | 24.22 | 130 | N/A | N/A |
|  CBR Transport Header Proxy | 1G | 500 | 500 | 500 | 100 | 23846.1 | 13.47 | 23.89 | 132 | N/A | N/A |
|  CBR Transport Header Proxy | 1G | 500 | 500 | 1000 | 100 | 23481.55 | 13.61 | 22.36 | 119 | N/A | N/A |
|  CBR Transport Header Proxy | 1G | 500 | 1024 | 0 | 100 | 23594.55 | 13.64 | 22.73 | 122 | N/A | N/A |
|  CBR Transport Header Proxy | 1G | 500 | 1024 | 30 | 100 | 23600.58 | 13.84 | 24 | 126 | N/A | N/A |
|  CBR Transport Header Proxy | 1G | 500 | 1024 | 100 | 100 | 24158.84 | 13.37 | 26.42 | 144 | N/A | N/A |
|  CBR Transport Header Proxy | 1G | 500 | 1024 | 500 | 100 | 23507.14 | 13.85 | 24.58 | 133 | N/A | N/A |
|  CBR Transport Header Proxy | 1G | 500 | 1024 | 1000 | 100 | 23878.28 | 13.65 | 24.94 | 135 | N/A | N/A |
|  CBR Transport Header Proxy | 1G | 500 | 10240 | 0 | 100 | 23330.01 | 13.94 | 23.72 | 127 | N/A | N/A |
|  CBR Transport Header Proxy | 1G | 500 | 10240 | 30 | 100 | 23756.59 | 13.53 | 23.03 | 124 | N/A | N/A |
|  CBR Transport Header Proxy | 1G | 500 | 10240 | 100 | 100 | 23552.89 | 13.69 | 23.99 | 127 | N/A | N/A |
|  CBR Transport Header Proxy | 1G | 500 | 10240 | 500 | 100 | 24045.5 | 13.41 | 23.48 | 128 | N/A | N/A |
|  CBR Transport Header Proxy | 1G | 500 | 10240 | 1000 | 100 | 24204.59 | 13.31 | 23.75 | 130 | N/A | N/A |
|  CBR Transport Header Proxy | 1G | 500 | 102400 | 0 | 100 | 23811.86 | 13.52 | 24.78 | 130 | N/A | N/A |
|  CBR Transport Header Proxy | 1G | 500 | 102400 | 30 | 100 | 23888.61 | 13.59 | 23.43 | 127 | N/A | N/A |
|  CBR Transport Header Proxy | 1G | 500 | 102400 | 100 | 100 | 23453.17 | 13.65 | 23.64 | 129 | N/A | N/A |
|  CBR Transport Header Proxy | 1G | 500 | 102400 | 500 | 100 | 23950.02 | 13.36 | 23.11 | 128 | N/A | N/A |
|  CBR Transport Header Proxy | 1G | 500 | 102400 | 1000 | 100 | 23890.71 | 13.38 | 24.53 | 129 | N/A | N/A |
|  CBR Transport Header Proxy | 1G | 1000 | 500 | 0 | 100 | 20473.67 | 28.72 | 52.66 | 267 | N/A | N/A |
|  CBR Transport Header Proxy | 1G | 1000 | 500 | 30 | 100 | 21274.69 | 27.9 | 49.33 | 257 | N/A | N/A |
|  CBR Transport Header Proxy | 1G | 1000 | 500 | 100 | 100 | 20633.83 | 29.69 | 57.07 | 293 | N/A | N/A |
|  CBR Transport Header Proxy | 1G | 1000 | 500 | 500 | 100 | 20579.79 | 30.02 | 52.04 | 273 | N/A | N/A |
|  CBR Transport Header Proxy | 1G | 1000 | 500 | 1000 | 100 | 20875.9 | 29.48 | 56.49 | 295 | N/A | N/A |
|  CBR Transport Header Proxy | 1G | 1000 | 1024 | 0 | 100 | 20698.98 | 29.24 | 54.22 | 283 | N/A | N/A |
|  CBR Transport Header Proxy | 1G | 1000 | 1024 | 30 | 100 | 20620.22 | 28.99 | 57.03 | 297 | N/A | N/A |
|  CBR Transport Header Proxy | 1G | 1000 | 1024 | 100 | 100 | 20771.25 | 29.21 | 53.59 | 281 | N/A | N/A |
|  CBR Transport Header Proxy | 1G | 1000 | 1024 | 500 | 100 | 20778.87 | 29.25 | 53.33 | 281 | N/A | N/A |
|  CBR Transport Header Proxy | 1G | 1000 | 1024 | 1000 | 100 | 20933.97 | 28.33 | 49.61 | 252 | N/A | N/A |
|  CBR Transport Header Proxy | 1G | 1000 | 10240 | 0 | 100 | 20230.45 | 29.6 | 59.49 | 319 | N/A | N/A |
|  CBR Transport Header Proxy | 1G | 1000 | 10240 | 30 | 100 | 20177.14 | 30.13 | 59.66 | 313 | N/A | N/A |
|  CBR Transport Header Proxy | 1G | 1000 | 10240 | 100 | 100 | 20402.9 | 30.35 | 58.88 | 299 | N/A | N/A |
|  CBR Transport Header Proxy | 1G | 1000 | 10240 | 500 | 100 | 20872.28 | 30.01 | 67.68 | 353 | N/A | N/A |
|  CBR Transport Header Proxy | 1G | 1000 | 10240 | 1000 | 100 | 21235.46 | 28.71 | 55.11 | 281 | N/A | N/A |
|  CBR Transport Header Proxy | 1G | 1000 | 102400 | 0 | 100 | 21089.95 | 28.97 | 55.74 | 291 | N/A | N/A |
|  CBR Transport Header Proxy | 1G | 1000 | 102400 | 30 | 100 | 20571.06 | 28.99 | 54.18 | 283 | N/A | N/A |
|  CBR Transport Header Proxy | 1G | 1000 | 102400 | 100 | 100 | 20504.86 | 28.98 | 59.12 | 291 | N/A | N/A |
|  CBR Transport Header Proxy | 1G | 1000 | 102400 | 500 | 100 | 20949.52 | 29.18 | 63 | 333 | N/A | N/A |
|  CBR Transport Header Proxy | 1G | 1000 | 102400 | 1000 | 100 | 21235.94 | 28.34 | 54.64 | 283 | N/A | N/A |
|  Direct Proxy | 1G | 500 | 500 | 0 | 0 | 5507.76 | 67.37 | 108.32 | 831 | 85.04 | 406.345 |
|  Direct Proxy | 1G | 500 | 500 | 30 | 0 | 4514.88 | 83.39 | 188.79 | 1247 | N/A | N/A |
|  Direct Proxy | 1G | 500 | 500 | 100 | 0 | 3085.64 | 121.42 | 25.05 | 196 | N/A | N/A |
|  Direct Proxy | 1G | 500 | 500 | 500 | 0 | 731.65 | 507.63 | 32.51 | 699 | N/A | N/A |
|  Direct Proxy | 1G | 500 | 500 | 1000 | 0 | 367.32 | 1002.21 | 2.18 | 1011 | N/A | N/A |
|  Direct Proxy | 1G | 500 | 1024 | 0 | 0 | 4210.92 | 90.04 | 242.63 | 1359 | N/A | N/A |
|  Direct Proxy | 1G | 500 | 1024 | 30 | 0 | 4623.6 | 81.15 | 126.23 | 887 | N/A | N/A |
|  Direct Proxy | 1G | 500 | 1024 | 100 | 0 | 3097.85 | 121.04 | 43.27 | 193 | N/A | N/A |
|  Direct Proxy | 1G | 500 | 1024 | 500 | 0 | 741.07 | 502.53 | 6.62 | 547 | N/A | N/A |
|  Direct Proxy | 1G | 500 | 1024 | 1000 | 0 | 367.26 | 1002.35 | 2.96 | 1019 | N/A | N/A |
|  Direct Proxy | 1G | 500 | 10240 | 0 | 0 | 3728.46 | 98.47 | 147.72 | 967 | N/A | N/A |
|  Direct Proxy | 1G | 500 | 10240 | 30 | 0 | 4034.62 | 93.52 | 121.34 | 249 | N/A | N/A |
|  Direct Proxy | 1G | 500 | 10240 | 100 | 0 | 2840.01 | 131.18 | 30.44 | 221 | N/A | N/A |
|  Direct Proxy | 1G | 500 | 10240 | 500 | 0 | 723.42 | 512.19 | 53.02 | 859 | N/A | N/A |
|  Direct Proxy | 1G | 500 | 10240 | 1000 | 0 | 367.32 | 1002.19 | 2.06 | 1007 | N/A | N/A |
|  Direct Proxy | 1G | 500 | 102400 | 0 | 0 | 1687.34 | 222.58 | 81.36 | 441 | N/A | N/A |
|  Direct Proxy | 1G | 500 | 102400 | 30 | 0 | 1727.95 | 217.12 | 73.42 | 409 | N/A | N/A |
|  Direct Proxy | 1G | 500 | 102400 | 100 | 0 | 1613.25 | 232.36 | 73.52 | 447 | N/A | N/A |
|  Direct Proxy | 1G | 500 | 102400 | 500 | 0 | 721.46 | 514.99 | 20.91 | 595 | N/A | N/A |
|  Direct Proxy | 1G | 500 | 102400 | 1000 | 0 | 366.65 | 1003.7 | 4.95 | 1031 | N/A | N/A |
|  Direct Proxy | 1G | 1000 | 500 | 0 | 0 | 5396.58 | 135.77 | 154.87 | 971 | N/A | N/A |
|  Direct Proxy | 1G | 1000 | 500 | 30 | 0 | 3561.17 | 212.26 | 359.47 | 1551 | N/A | N/A |
|  Direct Proxy | 1G | 1000 | 500 | 100 | 0 | 4329.94 | 173.45 | 149.18 | 1079 | N/A | N/A |
|  Direct Proxy | 1G | 1000 | 500 | 500 | 0 | 1452.96 | 512.26 | 38.33 | 663 | N/A | N/A |
|  Direct Proxy | 1G | 1000 | 500 | 1000 | 0 | 733.52 | 1002.91 | 5.18 | 1039 | N/A | N/A |
|  Direct Proxy | 1G | 1000 | 1024 | 0 | 0 | 2657.27 | 282.33 | 399.17 | 1471 | N/A | N/A |
|  Direct Proxy | 1G | 1000 | 1024 | 30 | 0 | 3510.16 | 213.41 | 282.79 | 1287 | N/A | N/A |
|  Direct Proxy | 1G | 1000 | 1024 | 100 | 0 | 4527.89 | 165.01 | 110.38 | 759 | N/A | N/A |
|  Direct Proxy | 1G | 1000 | 1024 | 500 | 0 | 1420 | 523.96 | 63.79 | 923 | N/A | N/A |
|  Direct Proxy | 1G | 1000 | 1024 | 1000 | 0 | 734.32 | 1004.26 | 9.49 | 1063 | N/A | N/A |
|  Direct Proxy | 1G | 1000 | 10240 | 0 | 0 | 4130.79 | 181.18 | 110.99 | 531 | N/A | N/A |
|  Direct Proxy | 1G | 1000 | 10240 | 30 | 0 | 4563.74 | 165.56 | 215.52 | 1271 | N/A | N/A |
|  Direct Proxy | 1G | 1000 | 10240 | 100 | 0 | 3689.4 | 203.17 | 140.87 | 995 | N/A | N/A |
|  Direct Proxy | 1G | 1000 | 10240 | 500 | 0 | 1430.84 | 520.89 | 67.32 | 1047 | N/A | N/A |
|  Direct Proxy | 1G | 1000 | 10240 | 1000 | 0 | 733.67 | 1003.38 | 5.94 | 1039 | N/A | N/A |
|  Direct Proxy | 1G | 1000 | 102400 | 0 | 0 | 1544.72 | 484.84 | 161.17 | 1071 | N/A | N/A |
|  Direct Proxy | 1G | 1000 | 102400 | 30 | 0 | 1555.43 | 481.65 | 163.96 | 1167 | N/A | N/A |
|  Direct Proxy | 1G | 1000 | 102400 | 100 | 0 | 1617.12 | 463.09 | 132.89 | 815 | N/A | N/A |
|  Direct Proxy | 1G | 1000 | 102400 | 500 | 0 | 1258.2 | 592.83 | 97.8 | 911 | N/A | N/A |
|  Direct Proxy | 1G | 1000 | 102400 | 1000 | 0 | 715.01 | 1031.41 | 60.3 | 1287 | N/A | N/A |
|  XSLT Enhanced Proxy | 1G | 500 | 500 | 0 | 100 | 24286.9 | 13.2 | 25.45 | 140 | N/A | N/A |
|  XSLT Enhanced Proxy | 1G | 500 | 500 | 30 | 100 | 24019.33 | 13.29 | 25.26 | 134 | N/A | N/A |
|  XSLT Enhanced Proxy | 1G | 500 | 500 | 100 | 100 | 24299.9 | 13.23 | 25.19 | 136 | N/A | N/A |
|  XSLT Enhanced Proxy | 1G | 500 | 500 | 500 | 100 | 24343 | 13.21 | 24.88 | 138 | N/A | N/A |
|  XSLT Enhanced Proxy | 1G | 500 | 500 | 1000 | 100 | 24351.82 | 13.07 | 24.05 | 133 | N/A | N/A |
|  XSLT Enhanced Proxy | 1G | 500 | 1024 | 0 | 100 | 23839.09 | 13.6 | 27.28 | 145 | N/A | N/A |
|  XSLT Enhanced Proxy | 1G | 500 | 1024 | 30 | 100 | 23731.09 | 13.51 | 25.99 | 136 | N/A | N/A |
|  XSLT Enhanced Proxy | 1G | 500 | 1024 | 100 | 100 | 24164.67 | 13.28 | 23.62 | 129 | N/A | N/A |
|  XSLT Enhanced Proxy | 1G | 500 | 1024 | 500 | 100 | 24499.14 | 13.01 | 24.57 | 131 | N/A | N/A |
|  XSLT Enhanced Proxy | 1G | 500 | 1024 | 1000 | 100 | 24154.1 | 13.39 | 24.64 | 135 | N/A | N/A |
|  XSLT Enhanced Proxy | 1G | 500 | 10240 | 0 | 100 | 23494.21 | 13.61 | 26.27 | 141 | N/A | N/A |
|  XSLT Enhanced Proxy | 1G | 500 | 10240 | 30 | 100 | 23953.33 | 13.51 | 24.81 | 135 | N/A | N/A |
|  XSLT Enhanced Proxy | 1G | 500 | 10240 | 100 | 100 | 24046.43 | 13.18 | 25.12 | 133 | N/A | N/A |
|  XSLT Enhanced Proxy | 1G | 500 | 10240 | 500 | 100 | 24271.91 | 13.35 | 25.9 | 143 | N/A | N/A |
|  XSLT Enhanced Proxy | 1G | 500 | 10240 | 1000 | 100 | 23861.55 | 13.38 | 25.11 | 135 | N/A | N/A |
|  XSLT Enhanced Proxy | 1G | 500 | 102400 | 0 | 100 | 24611.4 | 13.09 | 24.25 | 131 | N/A | N/A |
|  XSLT Enhanced Proxy | 1G | 500 | 102400 | 30 | 100 | 24234.15 | 13.28 | 24.74 | 134 | N/A | N/A |
|  XSLT Enhanced Proxy | 1G | 500 | 102400 | 100 | 100 | 24302.09 | 13.31 | 24.29 | 128 | N/A | N/A |
|  XSLT Enhanced Proxy | 1G | 500 | 102400 | 500 | 100 | 24313.3 | 13.23 | 25.27 | 136 | N/A | N/A |
|  XSLT Enhanced Proxy | 1G | 500 | 102400 | 1000 | 100 | 23843.34 | 13.54 | 26.22 | 144 | N/A | N/A |
|  XSLT Enhanced Proxy | 1G | 1000 | 500 | 0 | 100 | 20857.77 | 29.25 | 65.19 | 333 | N/A | N/A |
|  XSLT Enhanced Proxy | 1G | 1000 | 500 | 30 | 100 | 20572.46 | 30.57 | 65.61 | 339 | N/A | N/A |
|  XSLT Enhanced Proxy | 1G | 1000 | 500 | 100 | 100 | 21638.38 | 27.57 | 57 | 297 | N/A | N/A |
|  XSLT Enhanced Proxy | 1G | 1000 | 500 | 500 | 100 | 21024.26 | 28.42 | 60.09 | 333 | N/A | N/A |
|  XSLT Enhanced Proxy | 1G | 1000 | 500 | 1000 | 100 | 21072.29 | 28.36 | 62.82 | 317 | N/A | N/A |
|  XSLT Enhanced Proxy | 1G | 1000 | 1024 | 0 | 100 | 21032.94 | 29.54 | 56.72 | 285 | N/A | N/A |
|  XSLT Enhanced Proxy | 1G | 1000 | 1024 | 30 | 100 | 21215 | 28.8 | 59.24 | 291 | N/A | N/A |
|  XSLT Enhanced Proxy | 1G | 1000 | 1024 | 100 | 100 | 21163.58 | 29.35 | 59.14 | 303 | N/A | N/A |
|  XSLT Enhanced Proxy | 1G | 1000 | 1024 | 500 | 100 | 20681.46 | 29.49 | 67.76 | 353 | N/A | N/A |
|  XSLT Enhanced Proxy | 1G | 1000 | 1024 | 1000 | 100 | 21257.81 | 28.76 | 62.68 | 319 | N/A | N/A |
|  XSLT Enhanced Proxy | 1G | 1000 | 10240 | 0 | 100 | 21794.9 | 28.05 | 57.1 | 293 | N/A | N/A |
|  XSLT Enhanced Proxy | 1G | 1000 | 10240 | 30 | 100 | 21029.2 | 28.4 | 65.86 | 343 | N/A | N/A |
|  XSLT Enhanced Proxy | 1G | 1000 | 10240 | 100 | 100 | 20862.15 | 28.75 | 57.56 | 291 | N/A | N/A |
|  XSLT Enhanced Proxy | 1G | 1000 | 10240 | 500 | 100 | 20963.82 | 29.1 | 55.88 | 289 | N/A | N/A |
|  XSLT Enhanced Proxy | 1G | 1000 | 10240 | 1000 | 100 | 21359.2 | 28.66 | 58.57 | 311 | N/A | N/A |
|  XSLT Enhanced Proxy | 1G | 1000 | 102400 | 0 | 100 | 21583.66 | 28.31 | 58.45 | 297 | N/A | N/A |
|  XSLT Enhanced Proxy | 1G | 1000 | 102400 | 30 | 100 | 20822.91 | 29.4 | 55.32 | 281 | N/A | N/A |
|  XSLT Enhanced Proxy | 1G | 1000 | 102400 | 100 | 100 | 20883.4 | 29.41 | 57.3 | 297 | N/A | N/A |
|  XSLT Enhanced Proxy | 1G | 1000 | 102400 | 500 | 100 | 20915.94 | 29.12 | 57.81 | 295 | N/A | N/A |
|  XSLT Enhanced Proxy | 1G | 1000 | 102400 | 1000 | 100 | 21316.36 | 28.75 | 61.01 | 311 | N/A | N/A |
|  XSLT Proxy | 1G | 500 | 500 | 0 | 100 | 23611.85 | 13.82 | 26.24 | 138 | N/A | N/A |
|  XSLT Proxy | 1G | 500 | 500 | 30 | 100 | 23644.61 | 13.57 | 23.1 | 119 | N/A | N/A |
|  XSLT Proxy | 1G | 500 | 500 | 100 | 100 | 24000.7 | 13.42 | 25.89 | 143 | N/A | N/A |
|  XSLT Proxy | 1G | 500 | 500 | 500 | 100 | 23953.17 | 13.44 | 25.09 | 133 | N/A | N/A |
|  XSLT Proxy | 1G | 500 | 500 | 1000 | 100 | 24155.53 | 13.29 | 23.46 | 129 | N/A | N/A |
|  XSLT Proxy | 1G | 500 | 1024 | 0 | 100 | 24252.48 | 13.35 | 21.61 | 115 | N/A | N/A |
|  XSLT Proxy | 1G | 500 | 1024 | 30 | 100 | 24307.01 | 13.39 | 23.07 | 124 | N/A | N/A |
|  XSLT Proxy | 1G | 500 | 1024 | 100 | 100 | 23693.63 | 13.65 | 24.55 | 128 | N/A | N/A |
|  XSLT Proxy | 1G | 500 | 1024 | 500 | 100 | 23763.08 | 13.62 | 22.71 | 123 | N/A | N/A |
|  XSLT Proxy | 1G | 500 | 1024 | 1000 | 100 | 23963.55 | 13.39 | 23.72 | 131 | N/A | N/A |
|  XSLT Proxy | 1G | 500 | 10240 | 0 | 100 | 23847.96 | 13.59 | 22.79 | 122 | N/A | N/A |
|  XSLT Proxy | 1G | 500 | 10240 | 30 | 100 | 23643.12 | 13.83 | 24.29 | 137 | N/A | N/A |
|  XSLT Proxy | 1G | 500 | 10240 | 100 | 100 | 24273 | 13.33 | 23.18 | 124 | N/A | N/A |
|  XSLT Proxy | 1G | 500 | 10240 | 500 | 100 | 23680.65 | 13.73 | 23.67 | 129 | N/A | N/A |
|  XSLT Proxy | 1G | 500 | 10240 | 1000 | 100 | 23998.41 | 13.38 | 23.57 | 128 | N/A | N/A |
|  XSLT Proxy | 1G | 500 | 102400 | 0 | 100 | 23734.52 | 13.63 | 23.7 | 128 | N/A | N/A |
|  XSLT Proxy | 1G | 500 | 102400 | 30 | 100 | 24042.25 | 13.27 | 23.58 | 130 | N/A | N/A |
|  XSLT Proxy | 1G | 500 | 102400 | 100 | 100 | 23987.94 | 13.57 | 25.21 | 134 | N/A | N/A |
|  XSLT Proxy | 1G | 500 | 102400 | 500 | 100 | 24130.38 | 13.31 | 23.9 | 122 | N/A | N/A |
|  XSLT Proxy | 1G | 500 | 102400 | 1000 | 100 | 23715.52 | 13.43 | 22.36 | 116 | N/A | N/A |
|  XSLT Proxy | 1G | 1000 | 500 | 0 | 100 | 20877.52 | 27.95 | 57.15 | 291 | N/A | N/A |
|  XSLT Proxy | 1G | 1000 | 500 | 30 | 100 | 20609.49 | 28.86 | 50.96 | 265 | N/A | N/A |
|  XSLT Proxy | 1G | 1000 | 500 | 100 | 100 | 20559.75 | 29.48 | 50.93 | 265 | N/A | N/A |
|  XSLT Proxy | 1G | 1000 | 500 | 500 | 100 | 21394.14 | 28.99 | 54.02 | 275 | N/A | N/A |
|  XSLT Proxy | 1G | 1000 | 500 | 1000 | 100 | 20194.1 | 30.09 | 56.98 | 293 | N/A | N/A |
|  XSLT Proxy | 1G | 1000 | 1024 | 0 | 100 | 20947.63 | 28.78 | 58.06 | 295 | N/A | N/A |
|  XSLT Proxy | 1G | 1000 | 1024 | 30 | 100 | 20427.46 | 30.38 | 54.68 | 279 | N/A | N/A |
|  XSLT Proxy | 1G | 1000 | 1024 | 100 | 100 | 21611.77 | 28.07 | 51.85 | 275 | N/A | N/A |
|  XSLT Proxy | 1G | 1000 | 1024 | 500 | 100 | 21282.2 | 28.72 | 54.68 | 285 | N/A | N/A |
|  XSLT Proxy | 1G | 1000 | 1024 | 1000 | 100 | 21385.22 | 29.04 | 56.16 | 299 | N/A | N/A |
|  XSLT Proxy | 1G | 1000 | 10240 | 0 | 100 | 21412.26 | 28.33 | 47.48 | 257 | N/A | N/A |
|  XSLT Proxy | 1G | 1000 | 10240 | 30 | 100 | 21128.39 | 28.48 | 55.13 | 289 | N/A | N/A |
|  XSLT Proxy | 1G | 1000 | 10240 | 100 | 100 | 21221.57 | 28.52 | 57.84 | 315 | N/A | N/A |
|  XSLT Proxy | 1G | 1000 | 10240 | 500 | 100 | 21394.94 | 28.15 | 55.92 | 287 | N/A | N/A |
|  XSLT Proxy | 1G | 1000 | 10240 | 1000 | 100 | 20605.47 | 30.12 | 57.27 | 307 | N/A | N/A |
|  XSLT Proxy | 1G | 1000 | 102400 | 0 | 100 | 20408.89 | 28.7 | 55.56 | 289 | N/A | N/A |
|  XSLT Proxy | 1G | 1000 | 102400 | 30 | 100 | 21072.83 | 29.3 | 55 | 289 | N/A | N/A |
|  XSLT Proxy | 1G | 1000 | 102400 | 100 | 100 | 20879.25 | 29.29 | 53.08 | 269 | N/A | N/A |
|  XSLT Proxy | 1G | 1000 | 102400 | 500 | 100 | 20125.9 | 29.1 | 52.79 | 269 | N/A | N/A |
|  XSLT Proxy | 1G | 1000 | 102400 | 1000 | 100 | 20622.25 | 29.68 | 56.7 | 295 | N/A | N/A |
