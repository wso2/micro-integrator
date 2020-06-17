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
| Heap Size | The amount of memory allocated to the application | 4G |
| Concurrent Users | The number of users accessing the application at the same time. | 500, 1000 |
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
| Operating System | Distribution | Kernel | Linux ip-10-0-1-175 5.3.0-1023-aws #25~18.04.1-Ubuntu SMP Fri Jun 5 15:18:30 UTC 2020 x86_64 x86_64 x86_64 GNU/Linux |


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
|  CBR Proxy | 4G | 500 | 500 | 0 | 0 | 3392.81 | 110.57 | 58.69 | 301 | N/A | N/A |
|  CBR Proxy | 4G | 500 | 500 | 30 | 100 | 19250.41 | 17.03 | 27.34 | 147 | N/A | N/A |
|  CBR Proxy | 4G | 500 | 500 | 100 | 0 | 2833.4 | 132.16 | 33.94 | 283 | 96.95 | 27.024 |
|  CBR Proxy | 4G | 500 | 500 | 500 | 0 | 737.31 | 503.06 | 9.23 | 539 | N/A | N/A |
|  CBR Proxy | 4G | 500 | 500 | 1000 | 0 | 367.9 | 1002.16 | 2.31 | 1007 | N/A | N/A |
|  CBR Proxy | 4G | 500 | 1024 | 0 | 0 | 2987.7 | 125.71 | 65.67 | 335 | N/A | N/A |
|  CBR Proxy | 4G | 500 | 1024 | 30 | 92.81 | 8434.14 | 40.01 | 367.52 | 252 | N/A | N/A |
|  CBR Proxy | 4G | 500 | 1024 | 100 | 0 | 2542.68 | 147.25 | 39.81 | 307 | 96.91 | 26.997 |
|  CBR Proxy | 4G | 500 | 1024 | 500 | 0 | 740.58 | 502.93 | 10.75 | 531 | N/A | N/A |
|  CBR Proxy | 4G | 500 | 1024 | 1000 | 0 | 367.05 | 1002.89 | 8.73 | 1015 | N/A | N/A |
|  CBR Proxy | 4G | 500 | 10240 | 0 | 0 | 828.64 | 452.98 | 188.36 | 995 | N/A | N/A |
|  CBR Proxy | 4G | 500 | 10240 | 30 | 0 | 832.41 | 449.69 | 168.8 | 931 | N/A | N/A |
|  CBR Proxy | 4G | 500 | 10240 | 100 | 0 | 828.76 | 452.87 | 152.4 | 879 | N/A | N/A |
|  CBR Proxy | 4G | 500 | 10240 | 500 | 0 | 609.44 | 608.97 | 84.8 | 867 | N/A | N/A |
|  CBR Proxy | 4G | 500 | 10240 | 1000 | 100 | 17332.9 | 18.51 | 29.85 | 159 | N/A | N/A |
|  CBR Proxy | 4G | 500 | 102400 | 0 | 99.82 | 8057.63 | 39.14 | 289.49 | 241 | 91.02 | 27.062 |
|  CBR Proxy | 4G | 500 | 102400 | 30 | 99.98 | 12575.85 | 24.3 | 103.79 | 189 | 91.05 | 27 |
|  CBR Proxy | 4G | 500 | 102400 | 100 | 99.92 | 11020.48 | 27.67 | 157.27 | 228 | 90.86 | 27.01 |
|  CBR Proxy | 4G | 500 | 102400 | 500 | 100 | 12522.93 | 24.39 | 108.45 | 206 | 90.67 | 27 |
|  CBR Proxy | 4G | 500 | 102400 | 1000 | 99.99 | 13014.45 | 24.07 | 98.77 | 203 | 91.19 | 27 |
|  CBR Proxy | 4G | 1000 | 500 | 0 | 0 | 2310.9 | 324.12 | 135.16 | 699 | 96.24 | 27.012 |
|  CBR Proxy | 4G | 1000 | 500 | 30 | 0 | 2932.45 | 255.92 | 119.91 | 619 | N/A | N/A |
|  CBR Proxy | 4G | 1000 | 500 | 100 | 97.96 | 15955.64 | 40.52 | 78.16 | 357 | N/A | N/A |
|  CBR Proxy | 4G | 1000 | 500 | 500 | 0 | 1395.43 | 530.38 | 49.84 | 763 | 97.79 | 27.045 |
|  CBR Proxy | 4G | 1000 | 500 | 1000 | 0 | 730.67 | 1005.96 | 16.19 | 1103 | N/A | N/A |
|  CBR Proxy | 4G | 1000 | 1024 | 0 | 0 | 2912.79 | 256.97 | 122.97 | 639 | N/A | N/A |
|  CBR Proxy | 4G | 1000 | 1024 | 30 | 75.14 | 7432.46 | 95.04 | 134.7 | 535 | N/A | N/A |
|  CBR Proxy | 4G | 1000 | 1024 | 100 | 0 | 2298.66 | 325.51 | 121.2 | 671 | 96.15 | 27.022 |
|  CBR Proxy | 4G | 1000 | 1024 | 500 | 0 | 1408.14 | 526.55 | 34.52 | 667 | N/A | N/A |
|  CBR Proxy | 4G | 1000 | 1024 | 1000 | 0 | 728.96 | 1006.03 | 14.75 | 1087 | N/A | N/A |
|  CBR Proxy | 4G | 1000 | 10240 | 0 | 0 | 810.83 | 924.62 | 318.18 | 1711 | N/A | N/A |
|  CBR Proxy | 4G | 1000 | 10240 | 30 | 99.95 | 17363.13 | 36.31 | 70.9 | 341 | N/A | N/A |
|  CBR Proxy | 4G | 1000 | 10240 | 100 | 0 | 681.56 | 1088.16 | 348.6 | 1919 | 95.43 | 26.982 |
|  CBR Proxy | 4G | 1000 | 10240 | 500 | 0 | 758.69 | 981.07 | 234.12 | 1647 | N/A | N/A |
|  CBR Proxy | 4G | 1000 | 10240 | 1000 | 0 | 625.82 | 1172.21 | 142.82 | 1679 | N/A | N/A |
|  CBR Proxy | 4G | 1000 | 102400 | 0 | 100 | 17215.96 | 38.08 | 69.1 | 357 | N/A | N/A |
|  CBR Proxy | 4G | 1000 | 102400 | 30 | 100 | 15803.93 | 41.84 | 85 | 415 | 90.02 | 26.999 |
|  CBR Proxy | 4G | 1000 | 102400 | 100 | 100 | 16088.02 | 40.18 | 79.87 | 391 | 89.49 | 27.048 |
|  CBR Proxy | 4G | 1000 | 102400 | 500 | 100 | 11467.8 | 47.12 | 124.71 | 389 | 79.25 | 27.003 |
|  CBR Proxy | 4G | 1000 | 102400 | 1000 | 100 | 15917.33 | 40.72 | 82.24 | 411 | 87.32 | 26.984 |
|  CBR SOAP Header Proxy | 4G | 500 | 500 | 0 | 0 | 2521.36 | 148.89 | 82.96 | 421 | 96.5 | 26.999 |
|  CBR SOAP Header Proxy | 4G | 500 | 500 | 30 | 0 | 3458.86 | 108.43 | 52.65 | 295 | N/A | N/A |
|  CBR SOAP Header Proxy | 4G | 500 | 500 | 100 | 92.42 | 11313.81 | 30.28 | 83.83 | 231 | N/A | N/A |
|  CBR SOAP Header Proxy | 4G | 500 | 500 | 500 | 0 | 739.45 | 503.49 | 10.14 | 547 | 99.08 | 27.023 |
|  CBR SOAP Header Proxy | 4G | 500 | 500 | 1000 | 0 | 367.18 | 1002.32 | 3.9 | 1007 | N/A | N/A |
|  CBR SOAP Header Proxy | 4G | 500 | 1024 | 0 | 0 | 3325.69 | 112.67 | 62.83 | 315 | N/A | N/A |
|  CBR SOAP Header Proxy | 4G | 500 | 1024 | 30 | 66.75 | 6273.91 | 56.44 | 122.31 | 331 | N/A | N/A |
|  CBR SOAP Header Proxy | 4G | 500 | 1024 | 100 | 0 | 2708.13 | 138.3 | 39.4 | 303 | 96.75 | 27.021 |
|  CBR SOAP Header Proxy | 4G | 500 | 1024 | 500 | 0 | 739.34 | 505.34 | 13.36 | 571 | N/A | N/A |
|  CBR SOAP Header Proxy | 4G | 500 | 1024 | 1000 | 0 | 367.34 | 1002.4 | 4.33 | 1011 | N/A | N/A |
|  CBR SOAP Header Proxy | 4G | 500 | 10240 | 0 | 0 | 1229.97 | 304.75 | 124.94 | 651 | N/A | N/A |
|  CBR SOAP Header Proxy | 4G | 500 | 10240 | 30 | 0 | 1234.81 | 304.01 | 117.17 | 655 | N/A | N/A |
|  CBR SOAP Header Proxy | 4G | 500 | 10240 | 100 | 0 | 1205.3 | 311.23 | 108.1 | 639 | N/A | N/A |
|  CBR SOAP Header Proxy | 4G | 500 | 10240 | 500 | 0 | 683.65 | 542.44 | 44.99 | 707 | N/A | N/A |
|  CBR SOAP Header Proxy | 4G | 500 | 10240 | 1000 | 0 | 362.21 | 1017.82 | 32.31 | 1151 | N/A | N/A |
|  CBR SOAP Header Proxy | 4G | 500 | 102400 | 0 | 99.98 | 13863.67 | 23.27 | 85.98 | 168 | N/A | N/A |
|  CBR SOAP Header Proxy | 4G | 500 | 102400 | 30 | 99.06 | 6414.71 | 52.44 | 280.24 | 1863 | 91.99 | 27.001 |
|  CBR SOAP Header Proxy | 4G | 500 | 102400 | 100 | 99.4 | 8140.97 | 40.41 | 220.66 | 321 | 92.05 | 27.002 |
|  CBR SOAP Header Proxy | 4G | 500 | 102400 | 500 | 98.75 | 5449.78 | 63.14 | 327.57 | 2191 | 92.02 | 27.002 |
|  CBR SOAP Header Proxy | 4G | 500 | 102400 | 1000 | 98.57 | 4865.73 | 70.75 | 365.18 | 2383 | 91.67 | 27.02 |
|  CBR SOAP Header Proxy | 4G | 1000 | 500 | 0 | 0 | 2476.14 | 303.29 | 129.98 | 663 | 96.15 | 27.007 |
|  CBR SOAP Header Proxy | 4G | 1000 | 500 | 30 | 0 | 3191.14 | 235.31 | 105.9 | 551 | N/A | N/A |
|  CBR SOAP Header Proxy | 4G | 1000 | 500 | 100 | 100 | 20060.41 | 32.8 | 65.43 | 321 | N/A | N/A |
|  CBR SOAP Header Proxy | 4G | 1000 | 500 | 500 | 0 | 1388.81 | 533.73 | 51.61 | 763 | 97.8 | 27.025 |
|  CBR SOAP Header Proxy | 4G | 1000 | 500 | 1000 | 0 | 731.33 | 1007.74 | 21.75 | 1143 | N/A | N/A |
|  CBR SOAP Header Proxy | 4G | 1000 | 1024 | 0 | 0 | 3229.84 | 231.52 | 112.09 | 571 | N/A | N/A |
|  CBR SOAP Header Proxy | 4G | 1000 | 1024 | 30 | 91.74 | 9649 | 71.77 | 260.45 | 543 | N/A | N/A |
|  CBR SOAP Header Proxy | 4G | 1000 | 1024 | 100 | 0 | 2507.22 | 298.11 | 108.85 | 619 | 96.03 | 27.003 |
|  CBR SOAP Header Proxy | 4G | 1000 | 1024 | 500 | 0 | 1403.68 | 533.4 | 44.02 | 719 | N/A | N/A |
|  CBR SOAP Header Proxy | 4G | 1000 | 1024 | 1000 | 0 | 728.7 | 1005.12 | 12.73 | 1063 | N/A | N/A |
|  CBR SOAP Header Proxy | 4G | 1000 | 10240 | 0 | 0 | 1137.63 | 655.88 | 229.25 | 1271 | N/A | N/A |
|  CBR SOAP Header Proxy | 4G | 1000 | 10240 | 30 | 99.56 | 16221.25 | 39.4 | 96.71 | 365 | N/A | N/A |
|  CBR SOAP Header Proxy | 4G | 1000 | 10240 | 100 | 0 | 980.04 | 758.81 | 236.15 | 1311 | 96.19 | 26.984 |
|  CBR SOAP Header Proxy | 4G | 1000 | 10240 | 500 | 0 | 1071.11 | 696.46 | 128.28 | 1071 | N/A | N/A |
|  CBR SOAP Header Proxy | 4G | 1000 | 10240 | 1000 | 0 | 674.09 | 1098.95 | 94.54 | 1399 | N/A | N/A |
|  CBR SOAP Header Proxy | 4G | 1000 | 102400 | 0 | 100 | 16856.74 | 38.5 | 75.13 | 367 | N/A | N/A |
|  CBR SOAP Header Proxy | 4G | 1000 | 102400 | 30 | 100 | 15861.9 | 40.64 | 80.81 | 395 | 92.58 | 27.031 |
|  CBR SOAP Header Proxy | 4G | 1000 | 102400 | 100 | 100 | 16192.24 | 40.53 | 78.56 | 377 | 92.47 | 26.999 |
|  CBR SOAP Header Proxy | 4G | 1000 | 102400 | 500 | 100 | 14861.54 | 42.57 | 86.38 | 399 | 91.8 | 27.021 |
|  CBR SOAP Header Proxy | 4G | 1000 | 102400 | 1000 | 100 | 16877.62 | 38.7 | 70.64 | 353 | 92.73 | 26.998 |
|  CBR Transport Header Proxy | 4G | 500 | 500 | 0 | 0 | 3226.12 | 116.37 | 68.26 | 359 | 95.88 | 26.986 |
|  CBR Transport Header Proxy | 4G | 500 | 500 | 30 | 62.13 | 7835.2 | 45.48 | 68.82 | 285 | N/A | N/A |
|  CBR Transport Header Proxy | 4G | 500 | 500 | 100 | 0 | 3021.56 | 123.8 | 41.33 | 335 | 96.58 | 27.005 |
|  CBR Transport Header Proxy | 4G | 500 | 500 | 500 | 0 | 735.25 | 504.38 | 12.01 | 563 | N/A | N/A |
|  CBR Transport Header Proxy | 4G | 500 | 500 | 1000 | 0 | 366.91 | 1002.39 | 4.48 | 1007 | N/A | N/A |
|  CBR Transport Header Proxy | 4G | 500 | 1024 | 0 | 24.7 | 5152.39 | 71.07 | 101.78 | 379 | N/A | N/A |
|  CBR Transport Header Proxy | 4G | 500 | 1024 | 30 | 0 | 3183.76 | 117.79 | 59.75 | 347 | 95.62 | 26.962 |
|  CBR Transport Header Proxy | 4G | 500 | 1024 | 100 | 0 | 3122.37 | 119.9 | 33.27 | 297 | N/A | N/A |
|  CBR Transport Header Proxy | 4G | 500 | 1024 | 500 | 0 | 737.21 | 502.71 | 7.64 | 531 | N/A | N/A |
|  CBR Transport Header Proxy | 4G | 500 | 1024 | 1000 | 0 | 367.37 | 1002.29 | 4 | 1007 | N/A | N/A |
|  CBR Transport Header Proxy | 4G | 500 | 10240 | 0 | 100 | 20578.19 | 15.71 | 27.61 | 151 | N/A | N/A |
|  CBR Transport Header Proxy | 4G | 500 | 10240 | 30 | 0 | 2498.42 | 150.22 | 64.22 | 397 | 96.41 | 27 |
|  CBR Transport Header Proxy | 4G | 500 | 10240 | 100 | 0 | 2548.51 | 146.9 | 45.06 | 351 | N/A | N/A |
|  CBR Transport Header Proxy | 4G | 500 | 10240 | 500 | 0 | 739.85 | 504.38 | 13.04 | 555 | N/A | N/A |
|  CBR Transport Header Proxy | 4G | 500 | 10240 | 1000 | 0 | 366.88 | 1002.72 | 6.62 | 1019 | N/A | N/A |
|  CBR Transport Header Proxy | 4G | 500 | 102400 | 0 | 0 | 947.28 | 395.3 | 197.22 | 951 | N/A | N/A |
|  CBR Transport Header Proxy | 4G | 500 | 102400 | 30 | 0 | 968.19 | 387.11 | 178.77 | 915 | N/A | N/A |
|  CBR Transport Header Proxy | 4G | 500 | 102400 | 100 | 0 | 960.09 | 390.07 | 154.77 | 851 | N/A | N/A |
|  CBR Transport Header Proxy | 4G | 500 | 102400 | 500 | 0 | 644.67 | 577.13 | 97.5 | 923 | N/A | N/A |
|  CBR Transport Header Proxy | 4G | 500 | 102400 | 1000 | 0 | 364.75 | 1006.94 | 8.5 | 1047 | N/A | N/A |
|  CBR Transport Header Proxy | 4G | 1000 | 500 | 0 | 100 | 19143.92 | 33.23 | 66.98 | 317 | N/A | N/A |
|  CBR Transport Header Proxy | 4G | 1000 | 500 | 30 | 0 | 3235.33 | 232.16 | 100.76 | 559 | 95.36 | 27.022 |
|  CBR Transport Header Proxy | 4G | 1000 | 500 | 100 | 25.43 | 4629.05 | 156.42 | 127.74 | 591 | N/A | N/A |
|  CBR Transport Header Proxy | 4G | 1000 | 500 | 500 | 0 | 1421.21 | 520.92 | 36.92 | 723 | 98.28 | 27.02 |
|  CBR Transport Header Proxy | 4G | 1000 | 500 | 1000 | 0 | 728.41 | 1005.72 | 15.2 | 1095 | N/A | N/A |
|  CBR Transport Header Proxy | 4G | 1000 | 1024 | 0 | 0 | 4331.91 | 171.02 | 91.27 | 447 | N/A | N/A |
|  CBR Transport Header Proxy | 4G | 1000 | 1024 | 30 | 100 | 20268.43 | 31.88 | 62.31 | 311 | N/A | N/A |
|  CBR Transport Header Proxy | 4G | 1000 | 1024 | 100 | 0 | 3570.33 | 209.61 | 83.23 | 491 | 94.95 | 27.02 |
|  CBR Transport Header Proxy | 4G | 1000 | 1024 | 500 | 0 | 1441.68 | 514.51 | 27.88 | 643 | N/A | N/A |
|  CBR Transport Header Proxy | 4G | 1000 | 1024 | 1000 | 0 | 732.93 | 1003.98 | 12.28 | 1039 | N/A | N/A |
|  CBR Transport Header Proxy | 4G | 1000 | 10240 | 0 | 88.93 | 10791.07 | 62.33 | 103.87 | 451 | N/A | N/A |
|  CBR Transport Header Proxy | 4G | 1000 | 10240 | 30 | 0 | 2428.24 | 308.99 | 111.81 | 643 | 96.25 | 27.004 |
|  CBR Transport Header Proxy | 4G | 1000 | 10240 | 100 | 0 | 3057.09 | 244.1 | 81.21 | 507 | N/A | N/A |
|  CBR Transport Header Proxy | 4G | 1000 | 10240 | 500 | 0 | 1396.31 | 528.91 | 52.26 | 823 | N/A | N/A |
|  CBR Transport Header Proxy | 4G | 1000 | 10240 | 1000 | 38.74 | 1054.18 | 689.29 | 501.45 | 2039 | N/A | N/A |
|  CBR Transport Header Proxy | 4G | 1000 | 102400 | 0 | 0 | 699.93 | 1062.37 | 303.94 | 1887 | 98.82 | 26.984 |
|  CBR Transport Header Proxy | 4G | 1000 | 102400 | 30 | 0 | 876.35 | 851.08 | 319.68 | 1687 | N/A | N/A |
|  CBR Transport Header Proxy | 4G | 1000 | 102400 | 100 | 0 | 874.7 | 845.23 | 340.28 | 1759 | N/A | N/A |
|  CBR Transport Header Proxy | 4G | 1000 | 102400 | 500 | 0 | 832.99 | 885.31 | 261.2 | 1687 | N/A | N/A |
|  CBR Transport Header Proxy | 4G | 1000 | 102400 | 1000 | 0 | 657.89 | 1113.46 | 138.77 | 1599 | N/A | N/A |
|  Direct Proxy | 4G | 500 | 500 | 0 | 0 | 3014.12 | 124.51 | 74.85 | 379 | 95.68 | 28.943 |
|  Direct Proxy | 4G | 500 | 500 | 30 | 30.19 | 5051.07 | 70.95 | 97.02 | 311 | N/A | N/A |
|  Direct Proxy | 4G | 500 | 500 | 100 | 0 | 3045.73 | 122.97 | 36 | 305 | 96.94 | 27.02 |
|  Direct Proxy | 4G | 500 | 500 | 500 | 0 | 738.47 | 502.59 | 8.37 | 527 | N/A | N/A |
|  Direct Proxy | 4G | 500 | 500 | 1000 | 0 | 367.25 | 1002.2 | 2.69 | 1007 | N/A | N/A |
|  Direct Proxy | 4G | 500 | 1024 | 0 | 3.47 | 4421.53 | 84.58 | 70.06 | 297 | N/A | N/A |
|  Direct Proxy | 4G | 500 | 1024 | 30 | 0 | 3522.43 | 106.62 | 52.69 | 299 | 95.7 | 27.042 |
|  Direct Proxy | 4G | 500 | 1024 | 100 | 0 | 3135.75 | 119.37 | 32.7 | 293 | N/A | N/A |
|  Direct Proxy | 4G | 500 | 1024 | 500 | 0 | 736.32 | 503.63 | 12.04 | 535 | N/A | N/A |
|  Direct Proxy | 4G | 500 | 1024 | 1000 | 0 | 366.38 | 1002.5 | 5.97 | 1011 | N/A | N/A |
|  Direct Proxy | 4G | 500 | 10240 | 0 | 100 | 19892.34 | 16.18 | 22.19 | 116 | N/A | N/A |
|  Direct Proxy | 4G | 500 | 10240 | 30 | 0 | 2357.99 | 158.89 | 66.91 | 421 | 96.43 | 26.997 |
|  Direct Proxy | 4G | 500 | 10240 | 100 | 0 | 2590.76 | 144.4 | 41.06 | 329 | N/A | N/A |
|  Direct Proxy | 4G | 500 | 10240 | 500 | 0 | 734.54 | 504.88 | 11.02 | 551 | N/A | N/A |
|  Direct Proxy | 4G | 500 | 10240 | 1000 | 0 | 366.83 | 1002.61 | 5.52 | 1015 | N/A | N/A |
|  Direct Proxy | 4G | 500 | 102400 | 0 | 0 | 865.71 | 433.53 | 121.1 | 787 | N/A | N/A |
|  Direct Proxy | 4G | 500 | 102400 | 30 | 0 | 922.25 | 406.77 | 107.79 | 707 | N/A | N/A |
|  Direct Proxy | 4G | 500 | 102400 | 100 | 0 | 931.86 | 402.73 | 100.38 | 683 | N/A | N/A |
|  Direct Proxy | 4G | 500 | 102400 | 500 | 0 | 665.87 | 557.73 | 62.6 | 783 | N/A | N/A |
|  Direct Proxy | 4G | 500 | 102400 | 1000 | 0 | 364.7 | 1008.24 | 11.35 | 1055 | N/A | N/A |
|  Direct Proxy | 4G | 1000 | 500 | 0 | 100 | 18591.59 | 34.17 | 53.61 | 265 | N/A | N/A |
|  Direct Proxy | 4G | 1000 | 500 | 30 | 0 | 3240.49 | 232.25 | 115.4 | 603 | 95.01 | 27.002 |
|  Direct Proxy | 4G | 1000 | 500 | 100 | 51.89 | 5991.05 | 117.97 | 136.52 | 755 | N/A | N/A |
|  Direct Proxy | 4G | 1000 | 500 | 500 | 0 | 1431.58 | 519.36 | 35.31 | 703 | 98.44 | 27.027 |
|  Direct Proxy | 4G | 1000 | 500 | 1000 | 0 | 732.76 | 1004.51 | 13.46 | 1063 | N/A | N/A |
|  Direct Proxy | 4G | 1000 | 1024 | 0 | 0 | 4174.65 | 178.5 | 89.01 | 467 | N/A | N/A |
|  Direct Proxy | 4G | 1000 | 1024 | 30 | 100 | 19514.14 | 33.05 | 48.18 | 247 | N/A | N/A |
|  Direct Proxy | 4G | 1000 | 1024 | 100 | 0 | 3317.01 | 225.83 | 83.99 | 497 | 95.75 | 27.022 |
|  Direct Proxy | 4G | 1000 | 1024 | 500 | 0 | 1449.08 | 513.59 | 30.2 | 683 | N/A | N/A |
|  Direct Proxy | 4G | 1000 | 1024 | 1000 | 0 | 732.89 | 1003.29 | 8.84 | 1031 | N/A | N/A |
|  Direct Proxy | 4G | 1000 | 10240 | 0 | 0 | 2991.7 | 249.29 | 97.93 | 515 | N/A | N/A |
|  Direct Proxy | 4G | 1000 | 10240 | 30 | 100 | 19331.59 | 34.25 | 60.96 | 321 | N/A | N/A |
|  Direct Proxy | 4G | 1000 | 10240 | 100 | 0 | 2609.66 | 286.7 | 92.91 | 575 | 96.36 | 27.023 |
|  Direct Proxy | 4G | 1000 | 10240 | 500 | 0 | 1406.48 | 528.23 | 43.71 | 731 | N/A | N/A |
|  Direct Proxy | 4G | 1000 | 10240 | 1000 | 0 | 730.93 | 1005.52 | 13.97 | 1071 | N/A | N/A |
|  Direct Proxy | 4G | 1000 | 102400 | 0 | 0 | 857.38 | 871.01 | 220.21 | 1511 | N/A | N/A |
|  Direct Proxy | 4G | 1000 | 102400 | 30 | 0 | 875.24 | 851.81 | 218.89 | 1455 | N/A | N/A |
|  Direct Proxy | 4G | 1000 | 102400 | 100 | 0 | 877.87 | 848.73 | 209.6 | 1375 | N/A | N/A |
|  Direct Proxy | 4G | 1000 | 102400 | 500 | 0 | 776.53 | 944.27 | 265.11 | 1631 | N/A | N/A |
|  Direct Proxy | 4G | 1000 | 102400 | 1000 | 0 | 667.93 | 1089.08 | 102.57 | 1431 | N/A | N/A |
|  XSLT Enhanced Proxy | 4G | 500 | 500 | 0 | 0 | 2722.39 | 137.99 | 97.92 | 461 | N/A | N/A |
|  XSLT Enhanced Proxy | 4G | 500 | 500 | 30 | 0 | 2787.87 | 134.67 | 89.73 | 449 | N/A | N/A |
|  XSLT Enhanced Proxy | 4G | 500 | 500 | 100 | 87.99 | 9507.04 | 35.67 | 69.76 | 247 | N/A | N/A |
|  XSLT Enhanced Proxy | 4G | 500 | 500 | 500 | 0 | 731.42 | 507.96 | 14.6 | 579 | 98.83 | 30.004 |
|  XSLT Enhanced Proxy | 4G | 500 | 500 | 1000 | 0 | 365.71 | 1003.02 | 6.71 | 1031 | N/A | N/A |
|  XSLT Enhanced Proxy | 4G | 500 | 1024 | 0 | 0 | 2592.13 | 144.8 | 113.91 | 575 | N/A | N/A |
|  XSLT Enhanced Proxy | 4G | 500 | 1024 | 30 | 0 | 2634.19 | 142.69 | 83.98 | 447 | N/A | N/A |
|  XSLT Enhanced Proxy | 4G | 500 | 1024 | 100 | 100 | 19563.14 | 16.78 | 25.44 | 136 | N/A | N/A |
|  XSLT Enhanced Proxy | 4G | 500 | 1024 | 500 | 0 | 730.95 | 509.23 | 17.67 | 599 | 98.79 | 29.705 |
|  XSLT Enhanced Proxy | 4G | 500 | 1024 | 1000 | 0 | 367.06 | 1002.45 | 4.17 | 1015 | N/A | N/A |
|  XSLT Enhanced Proxy | 4G | 500 | 10240 | 0 | 0 | 526.1 | 711.08 | 220.12 | 1335 | N/A | N/A |
|  XSLT Enhanced Proxy | 4G | 500 | 10240 | 30 | 0 | 575.54 | 649.18 | 220.63 | 1383 | N/A | N/A |
|  XSLT Enhanced Proxy | 4G | 500 | 10240 | 100 | 0 | 580.26 | 645.92 | 195.94 | 1175 | N/A | N/A |
|  XSLT Enhanced Proxy | 4G | 500 | 10240 | 500 | 0 | 498.49 | 747.41 | 141.55 | 1183 | N/A | N/A |
|  XSLT Enhanced Proxy | 4G | 500 | 10240 | 1000 | 0 | 333.58 | 1102.27 | 114.33 | 1487 | N/A | N/A |
|  XSLT Enhanced Proxy | 4G | 500 | 102400 | 0 | 0 | 69.03 | 5197.03 | 905.79 | 7039 | N/A | N/A |
|  XSLT Enhanced Proxy | 4G | 500 | 102400 | 30 | 0 | 66.44 | 5387.29 | 1305.64 | 8191 | N/A | N/A |
|  XSLT Enhanced Proxy | 4G | 500 | 102400 | 100 | 18.53 | 72.27 | 5057.3 | 1344.72 | 7551 | N/A | N/A |
|  XSLT Enhanced Proxy | 4G | 500 | 102400 | 500 | 0 | 61.55 | 5741.08 | 1331.8 | 9087 | 97.51 | 37.229 |
|  XSLT Enhanced Proxy | 4G | 500 | 102400 | 1000 | 0 | 64.26 | 5452.89 | 1025.21 | 7487 | N/A | N/A |
|  XSLT Enhanced Proxy | 4G | 1000 | 500 | 0 | 100 | 19989.63 | 32.33 | 50.03 | 267 | N/A | N/A |
|  XSLT Enhanced Proxy | 4G | 1000 | 500 | 30 | 0 | 1910.8 | 392.13 | 186.12 | 959 | 96.6 | 29.34 |
|  XSLT Enhanced Proxy | 4G | 1000 | 500 | 100 | 0 | 2726.57 | 274.67 | 120.49 | 699 | N/A | N/A |
|  XSLT Enhanced Proxy | 4G | 1000 | 500 | 500 | 2.27 | 1415.03 | 528.35 | 43.08 | 683 | N/A | N/A |
|  XSLT Enhanced Proxy | 4G | 1000 | 500 | 1000 | 0 | 729.94 | 1007.9 | 17.84 | 1119 | 98.76 | 31.426 |
|  XSLT Enhanced Proxy | 4G | 1000 | 1024 | 0 | 0 | 2459.1 | 303.98 | 148.31 | 759 | N/A | N/A |
|  XSLT Enhanced Proxy | 4G | 1000 | 1024 | 30 | 51.83 | 4107.97 | 173.77 | 214.56 | 1079 | N/A | N/A |
|  XSLT Enhanced Proxy | 4G | 1000 | 1024 | 100 | 0 | 1728.61 | 432.95 | 163.49 | 907 | 97.4 | 30.014 |
|  XSLT Enhanced Proxy | 4G | 1000 | 1024 | 500 | 0 | 1388.39 | 535.57 | 42.46 | 711 | N/A | N/A |
|  XSLT Enhanced Proxy | 4G | 1000 | 1024 | 1000 | 0 | 724.3 | 1009.2 | 25.81 | 1151 | N/A | N/A |
|  XSLT Enhanced Proxy | 4G | 1000 | 10240 | 0 | 0 | 522.47 | 1420.46 | 377.86 | 2335 | N/A | N/A |
|  XSLT Enhanced Proxy | 4G | 1000 | 10240 | 30 | 91.36 | 3886.79 | 175.73 | 416.67 | 2159 | N/A | N/A |
|  XSLT Enhanced Proxy | 4G | 1000 | 10240 | 100 | 0 | 396.62 | 1859.17 | 485.51 | 3343 | 98.11 | 30.581 |
|  XSLT Enhanced Proxy | 4G | 1000 | 10240 | 500 | 0 | 523.47 | 1411.36 | 326.03 | 2255 | N/A | N/A |
|  XSLT Enhanced Proxy | 4G | 1000 | 10240 | 1000 | 0 | 516.25 | 1418.22 | 274.94 | 2175 | N/A | N/A |
|  XSLT Enhanced Proxy | 4G | 1000 | 102400 | 0 | 38.5 | 49.1 | 16645.8 | 9042.27 | 33023 | N/A | N/A |
|  XSLT Enhanced Proxy | 4G | 1000 | 102400 | 30 | 99.97 | 6183.45 | 93.46 | 695.99 | 571 | 97.28 | 38.29 |
|  XSLT Enhanced Proxy | 4G | 1000 | 102400 | 100 | 91.34 | 11.2 | 53262.57 | 11049.43 | 66047 | 97.13 | 46.668 |
|  XSLT Enhanced Proxy | 4G | 1000 | 102400 | 500 | 100 | 7109.99 | 77.02 | 454.3 | 509 | 96.92 | 41.488 |
|  XSLT Enhanced Proxy | 4G | 1000 | 102400 | 1000 | 99.68 | 5058.3 | 113.87 | 737.04 | 1031 | 96.99 | 37.674 |
|  XSLT Proxy | 4G | 500 | 500 | 0 | 0 | 1384.46 | 271.36 | 130.9 | 667 | 96.61 | 30.623 |
|  XSLT Proxy | 4G | 500 | 500 | 30 | 0 | 1622.38 | 231.47 | 105.11 | 551 | N/A | N/A |
|  XSLT Proxy | 4G | 500 | 500 | 100 | 0 | 1918.88 | 195.64 | 63.2 | 403 | N/A | N/A |
|  XSLT Proxy | 4G | 500 | 500 | 500 | 38 | 1068.15 | 342.2 | 234.5 | 643 | N/A | N/A |
|  XSLT Proxy | 4G | 500 | 500 | 1000 | 0 | 366.47 | 1005.29 | 12.92 | 1071 | 98.96 | 31.321 |
|  XSLT Proxy | 4G | 500 | 1024 | 0 | 0 | 1649.03 | 227.74 | 108.31 | 543 | N/A | N/A |
|  XSLT Proxy | 4G | 500 | 1024 | 30 | 0 | 1657.73 | 226.17 | 90.03 | 495 | N/A | N/A |
|  XSLT Proxy | 4G | 500 | 1024 | 100 | 0 | 1430.42 | 263.66 | 331.02 | 3151 | N/A | N/A |
|  XSLT Proxy | 4G | 500 | 1024 | 500 | 100 | 14513.2 | 23.22 | 35.03 | 181 | N/A | N/A |
|  XSLT Proxy | 4G | 500 | 1024 | 1000 | 0 | 365.58 | 1007.59 | 16.37 | 1095 | 98.98 | 31.384 |
|  XSLT Proxy | 4G | 500 | 10240 | 0 | 0 | 337.37 | 1103.94 | 353.95 | 1991 | N/A | N/A |
|  XSLT Proxy | 4G | 500 | 10240 | 30 | 0 | 342.99 | 1083.25 | 419.58 | 2479 | N/A | N/A |
|  XSLT Proxy | 4G | 500 | 10240 | 100 | 0 | 348.43 | 1070.43 | 344.86 | 1951 | N/A | N/A |
|  XSLT Proxy | 4G | 500 | 10240 | 500 | 0 | 345.14 | 1071.31 | 309.39 | 1943 | N/A | N/A |
|  XSLT Proxy | 4G | 500 | 10240 | 1000 | 0 | 293.69 | 1257.73 | 214.92 | 1983 | N/A | N/A |
|  XSLT Proxy | 4G | 500 | 102400 | 0 | 100 | 16726.56 | 18.51 | 30.21 | 162 | N/A | N/A |
|  XSLT Proxy | 4G | 500 | 102400 | 30 | 100 | 7514.4 | 38.7 | 219.14 | 267 | 92.9 | 58.788 |
|  XSLT Proxy | 4G | 500 | 102400 | 100 | 100 | 8512.9 | 32.36 | 113.3 | 265 | 93.11 | 62.9 |
|  XSLT Proxy | 4G | 500 | 102400 | 500 | 99.99 | 7749.63 | 37.82 | 269.11 | 244 | 92.87 | 60.059 |
|  XSLT Proxy | 4G | 500 | 102400 | 1000 | 100 | 8414.79 | 34.11 | 163.78 | 218 | 93.12 | 51.459 |
|  XSLT Proxy | 4G | 1000 | 500 | 0 | 0 | 1371.44 | 544.9 | 216.85 | 1167 | 96.53 | 32.477 |
|  XSLT Proxy | 4G | 1000 | 500 | 30 | 0 | 1511.29 | 493.49 | 208.57 | 1111 | N/A | N/A |
|  XSLT Proxy | 4G | 1000 | 500 | 100 | 94.71 | 11585.82 | 57.42 | 118.01 | 563 | N/A | N/A |
|  XSLT Proxy | 4G | 1000 | 500 | 500 | 0 | 1317.35 | 564 | 56.3 | 783 | 97.4 | 30.983 |
|  XSLT Proxy | 4G | 1000 | 500 | 1000 | 0 | 723.43 | 1014.73 | 23.13 | 1111 | N/A | N/A |
|  XSLT Proxy | 4G | 1000 | 1024 | 0 | 0 | 1637.14 | 459.06 | 204.45 | 1063 | N/A | N/A |
|  XSLT Proxy | 4G | 1000 | 1024 | 30 | 98.36 | 6935.03 | 68.3 | 382.51 | 675 | N/A | N/A |
|  XSLT Proxy | 4G | 1000 | 1024 | 100 | 0 | 1241.52 | 601.6 | 222.58 | 1239 | 96.32 | 31.321 |
|  XSLT Proxy | 4G | 1000 | 1024 | 500 | 0 | 1218.24 | 609.97 | 92.7 | 931 | N/A | N/A |
|  XSLT Proxy | 4G | 1000 | 1024 | 1000 | 0 | 714.9 | 1027.76 | 37.44 | 1183 | N/A | N/A |
|  XSLT Proxy | 4G | 1000 | 10240 | 0 | 98.55 | 8269.64 | 80.94 | 274.33 | 1759 | N/A | N/A |
|  XSLT Proxy | 4G | 1000 | 10240 | 30 | 0 | 231.32 | 3155.41 | 824.31 | 5311 | 96.68 | 35.192 |
|  XSLT Proxy | 4G | 1000 | 10240 | 100 | 0 | 313.65 | 2340.77 | 631.16 | 4015 | N/A | N/A |
|  XSLT Proxy | 4G | 1000 | 10240 | 500 | 93.77 | 2770.85 | 242.68 | 788.96 | 4479 | N/A | N/A |
|  XSLT Proxy | 4G | 1000 | 10240 | 1000 | 0 | 289.11 | 2475.71 | 637.33 | 4095 | 96.19 | 35.992 |
|  XSLT Proxy | 4G | 1000 | 102400 | 0 | 100 | 12406.61 | 52.77 | 91.49 | 461 | N/A | N/A |
|  XSLT Proxy | 4G | 1000 | 102400 | 30 | 100 | 10839.58 | 55.51 | 95.47 | 465 | 93.09 | 69.205 |
|  XSLT Proxy | 4G | 1000 | 102400 | 100 | 100 | 15026.13 | 41.38 | 80.89 | 407 | 93.55 | 68.687 |
|  XSLT Proxy | 4G | 1000 | 102400 | 500 | 100 | 5892.38 | 78.43 | 388.14 | 583 | 93.44 | 85.224 |
|  XSLT Proxy | 4G | 1000 | 102400 | 1000 | 100 | 10927.07 | 55.01 | 90.53 | 433 | 92.1 | 81.357 |
