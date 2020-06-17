# WSO2 Micro Integrator 1.1.0 Performance Test Results

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

1. **Throughput**: The number of requests that the WSO2 Micro Integrator 1.1.0 processes during a specific time interval (e.g. per second).
2. **Response Time**: The end-to-end latency for an operation of invoking a service in WSO2 Micro Integrator 1.1.0 . The complete distribution of response times was recorded.

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


System information for WSO2 Micro Integrator 1.1.0 in 1st AWS CloudFormation stack.

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
| Operating System | Distribution | Kernel | Linux ip-10-0-1-180 5.3.0-1023-aws #25~18.04.1-Ubuntu SMP Fri Jun 5 15:18:30 UTC 2020 x86_64 x86_64 x86_64 GNU/Linux |


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

|  Scenario Name | Heap Size | Concurrent Users | Message Size (Bytes) | Back-end Service Delay (ms) | Error % | Throughput (Requests/sec) | Average Response Time (ms) | Standard Deviation of Response Time (ms) | 99th Percentile of Response Time (ms) | WSO2 Micro Integrator 1.1.0 GC Throughput (%) | Average WSO2 Micro Integrator 1.1.0 Memory Footprint After Full GC (M) |
|---|---:|---:|---:|---:|---:|---:|---:|---:|---:|---:|---:|
|  CBR Proxy | 4G | 500 | 500 | 0 | 0 | 3290.67 | 113.66 | 57.66 | 295 | N/A | N/A |
|  CBR Proxy | 4G | 500 | 500 | 30 | 6.18 | 3292.01 | 113.21 | 139.97 | 305 | N/A | N/A |
|  CBR Proxy | 4G | 500 | 500 | 100 | 0 | 2810.87 | 133.18 | 33.63 | 277 | 97.04 | 26.691 |
|  CBR Proxy | 4G | 500 | 500 | 500 | 0 | 735.97 | 505.05 | 13.33 | 567 | N/A | N/A |
|  CBR Proxy | 4G | 500 | 500 | 1000 | 0 | 366.86 | 1002.49 | 4.5 | 1015 | N/A | N/A |
|  CBR Proxy | 4G | 500 | 1024 | 0 | 0 | 3018.97 | 124.25 | 67.11 | 341 | N/A | N/A |
|  CBR Proxy | 4G | 500 | 1024 | 30 | 100 | 20015 | 16.24 | 27.4 | 141 | N/A | N/A |
|  CBR Proxy | 4G | 500 | 1024 | 100 | 0 | 2546.4 | 146.83 | 38.04 | 293 | 96.82 | 26.895 |
|  CBR Proxy | 4G | 500 | 1024 | 500 | 0 | 735.62 | 504.38 | 10.43 | 555 | N/A | N/A |
|  CBR Proxy | 4G | 500 | 1024 | 1000 | 0 | 367.06 | 1002.38 | 4.13 | 1011 | N/A | N/A |
|  CBR Proxy | 4G | 500 | 10240 | 0 | 0 | 835.16 | 450.59 | 174.01 | 935 | N/A | N/A |
|  CBR Proxy | 4G | 500 | 10240 | 30 | 0 | 842.62 | 444.56 | 159.45 | 911 | N/A | N/A |
|  CBR Proxy | 4G | 500 | 10240 | 100 | 0 | 835.79 | 449.09 | 150.63 | 903 | N/A | N/A |
|  CBR Proxy | 4G | 500 | 10240 | 500 | 0 | 622.4 | 598.29 | 83.62 | 883 | N/A | N/A |
|  CBR Proxy | 4G | 500 | 10240 | 1000 | 0 | 356.01 | 1041.89 | 60.74 | 1303 | N/A | N/A |
|  CBR Proxy | 4G | 500 | 102400 | 0 | 100 | 16879.09 | 19.47 | 30.17 | 160 | N/A | N/A |
|  CBR Proxy | 4G | 500 | 102400 | 30 | 99.89 | 11072.75 | 29.14 | 163.25 | 200 | 91.05 | 26.826 |
|  CBR Proxy | 4G | 500 | 102400 | 100 | 99.95 | 12318.24 | 25.27 | 107.18 | 193 | 91.31 | 26.876 |
|  CBR Proxy | 4G | 500 | 102400 | 500 | 99.97 | 12008.55 | 26.93 | 145.93 | 202 | 90.84 | 26.692 |
|  CBR Proxy | 4G | 500 | 102400 | 1000 | 99.96 | 12386.68 | 24.79 | 132.26 | 207 | 90.93 | 26.827 |
|  CBR Proxy | 4G | 1000 | 500 | 0 | 0 | 2390.15 | 313.74 | 131.88 | 687 | 96.99 | 26.827 |
|  CBR Proxy | 4G | 1000 | 500 | 30 | 0 | 2907.9 | 257.72 | 116.65 | 607 | N/A | N/A |
|  CBR Proxy | 4G | 1000 | 500 | 100 | 100 | 19413.46 | 33.36 | 57.23 | 295 | N/A | N/A |
|  CBR Proxy | 4G | 1000 | 500 | 500 | 0 | 1394.16 | 531.67 | 46.5 | 735 | 97.71 | 26.82 |
|  CBR Proxy | 4G | 1000 | 500 | 1000 | 0 | 729.46 | 1005.05 | 11.96 | 1071 | N/A | N/A |
|  CBR Proxy | 4G | 1000 | 1024 | 0 | 0 | 2952.2 | 253.4 | 119.77 | 607 | N/A | N/A |
|  CBR Proxy | 4G | 1000 | 1024 | 30 | 74.99 | 7043.62 | 100.31 | 174.05 | 707 | N/A | N/A |
|  CBR Proxy | 4G | 1000 | 1024 | 100 | 0 | 2348.23 | 318.68 | 118.19 | 671 | 96.24 | 26.823 |
|  CBR Proxy | 4G | 1000 | 1024 | 500 | 0 | 1404.85 | 527.37 | 39.28 | 703 | N/A | N/A |
|  CBR Proxy | 4G | 1000 | 1024 | 1000 | 0 | 731.15 | 1006.45 | 18.02 | 1103 | N/A | N/A |
|  CBR Proxy | 4G | 1000 | 10240 | 0 | 0 | 800.52 | 932.14 | 324.08 | 1663 | N/A | N/A |
|  CBR Proxy | 4G | 1000 | 10240 | 30 | 0 | 7.45 | 44575.79 | 16317.38 | 66047 | N/A | N/A |
|  CBR Proxy | 4G | 1000 | 10240 | 100 | 100 | 17987.64 | 36.61 | 69.33 | 341 | N/A | N/A |
|  CBR Proxy | 4G | 1000 | 10240 | 500 | 0 | 710.57 | 1049.07 | 257.33 | 1759 | 96.02 | 26.826 |
|  CBR Proxy | 4G | 1000 | 10240 | 1000 | 0 | 632.41 | 1154.65 | 117.47 | 1519 | N/A | N/A |
|  CBR Proxy | 4G | 1000 | 102400 | 0 | 100 | 16795.18 | 39.74 | 68.03 | 351 | N/A | N/A |
|  CBR Proxy | 4G | 1000 | 102400 | 30 | 100 | 15299.26 | 41.6 | 75.19 | 363 | 90.18 | 26.827 |
|  CBR Proxy | 4G | 1000 | 102400 | 100 | 100 | 15235.01 | 41.91 | 73.07 | 353 | 85.61 | 26.824 |
|  CBR Proxy | 4G | 1000 | 102400 | 500 | 100 | 15713.9 | 41.04 | 72.68 | 363 | 90.6 | 26.891 |
|  CBR Proxy | 4G | 1000 | 102400 | 1000 | 100 | 14728.09 | 41.84 | 74.43 | 363 | 90.7 | 26.827 |
|  CBR SOAP Header Proxy | 4G | 500 | 500 | 0 | 0 | 2623.64 | 143.14 | 72.86 | 359 | 96.6 | 26.896 |
|  CBR SOAP Header Proxy | 4G | 500 | 500 | 30 | 0 | 3516.45 | 106.95 | 50.33 | 279 | N/A | N/A |
|  CBR SOAP Header Proxy | 4G | 500 | 500 | 100 | 86.89 | 9363.58 | 36.92 | 95.69 | 243 | N/A | N/A |
|  CBR SOAP Header Proxy | 4G | 500 | 500 | 500 | 0 | 736.72 | 503.47 | 9.47 | 535 | 99.16 | 26.703 |
|  CBR SOAP Header Proxy | 4G | 500 | 500 | 1000 | 0 | 366.88 | 1002.43 | 4.46 | 1011 | N/A | N/A |
|  CBR SOAP Header Proxy | 4G | 500 | 1024 | 0 | 0 | 3387.06 | 110.82 | 61.94 | 317 | N/A | N/A |
|  CBR SOAP Header Proxy | 4G | 500 | 1024 | 30 | 78.54 | 8647.16 | 40.78 | 89.29 | 245 | N/A | N/A |
|  CBR SOAP Header Proxy | 4G | 500 | 1024 | 100 | 0 | 2792.48 | 134.05 | 34.25 | 283 | 97.04 | 26.823 |
|  CBR SOAP Header Proxy | 4G | 500 | 1024 | 500 | 0 | 738.59 | 503.95 | 13.34 | 575 | N/A | N/A |
|  CBR SOAP Header Proxy | 4G | 500 | 1024 | 1000 | 0 | 366.39 | 1002.29 | 3.47 | 1011 | N/A | N/A |
|  CBR SOAP Header Proxy | 4G | 500 | 10240 | 0 | 0 | 1233.16 | 303.72 | 121.27 | 623 | N/A | N/A |
|  CBR SOAP Header Proxy | 4G | 500 | 10240 | 30 | 0 | 1232.46 | 304.55 | 116.89 | 643 | N/A | N/A |
|  CBR SOAP Header Proxy | 4G | 500 | 10240 | 100 | 0 | 1210.38 | 309.65 | 107.07 | 615 | N/A | N/A |
|  CBR SOAP Header Proxy | 4G | 500 | 10240 | 500 | 100 | 17093.76 | 19.47 | 36.04 | 163 | N/A | N/A |
|  CBR SOAP Header Proxy | 4G | 500 | 10240 | 1000 | 0 | 364.5 | 1012.41 | 17.4 | 1095 | 99.06 | 26.773 |
|  CBR SOAP Header Proxy | 4G | 500 | 102400 | 0 | 0 | 55.66 | 6008.38 | 7321.14 | 22399 | N/A | N/A |
|  CBR SOAP Header Proxy | 4G | 500 | 102400 | 30 | 100 | 20165.99 | 16.11 | 24.96 | 134 | N/A | N/A |
|  CBR SOAP Header Proxy | 4G | 500 | 102400 | 100 | 98.74 | 5349.49 | 63.93 | 346.4 | 2207 | 91.64 | 26.693 |
|  CBR SOAP Header Proxy | 4G | 500 | 102400 | 500 | 98.44 | 4794.2 | 73.58 | 369.44 | 2431 | 91.66 | 26.825 |
|  CBR SOAP Header Proxy | 4G | 500 | 102400 | 1000 | 98.75 | 5533.03 | 60.04 | 314.55 | 2111 | 91.83 | 26.826 |
|  CBR SOAP Header Proxy | 4G | 1000 | 500 | 0 | 0 | 2505.5 | 298.45 | 130.73 | 687 | 96.23 | 26.823 |
|  CBR SOAP Header Proxy | 4G | 1000 | 500 | 30 | 0 | 3178.59 | 235.95 | 105.43 | 543 | N/A | N/A |
|  CBR SOAP Header Proxy | 4G | 1000 | 500 | 100 | 100 | 19613.2 | 32.95 | 65.25 | 343 | N/A | N/A |
|  CBR SOAP Header Proxy | 4G | 1000 | 500 | 500 | 0 | 1403.56 | 528.04 | 46.22 | 751 | 97.81 | 26.826 |
|  CBR SOAP Header Proxy | 4G | 1000 | 500 | 1000 | 0 | 728.96 | 1006.27 | 19.63 | 1135 | N/A | N/A |
|  CBR SOAP Header Proxy | 4G | 1000 | 1024 | 0 | 0 | 3312.38 | 224.99 | 111 | 555 | N/A | N/A |
|  CBR SOAP Header Proxy | 4G | 1000 | 1024 | 30 | 97.78 | 15746.97 | 42.4 | 87.68 | 377 | N/A | N/A |
|  CBR SOAP Header Proxy | 4G | 1000 | 1024 | 100 | 0 | 2527.5 | 295.54 | 113.25 | 631 | 95.83 | 26.827 |
|  CBR SOAP Header Proxy | 4G | 1000 | 1024 | 500 | 0 | 1433.07 | 518.87 | 30.42 | 671 | N/A | N/A |
|  CBR SOAP Header Proxy | 4G | 1000 | 1024 | 1000 | 0 | 728.55 | 1007.69 | 21.38 | 1127 | N/A | N/A |
|  CBR SOAP Header Proxy | 4G | 1000 | 10240 | 0 | 0 | 1182.65 | 631.88 | 228.35 | 1327 | N/A | N/A |
|  CBR SOAP Header Proxy | 4G | 1000 | 10240 | 30 | 100 | 19725.29 | 31.55 | 59.15 | 287 | N/A | N/A |
|  CBR SOAP Header Proxy | 4G | 1000 | 10240 | 100 | 0 | 971.06 | 770.04 | 254.71 | 1423 | 95.84 | 26.825 |
|  CBR SOAP Header Proxy | 4G | 1000 | 10240 | 500 | 0 | 1058.85 | 702.99 | 135.5 | 1119 | N/A | N/A |
|  CBR SOAP Header Proxy | 4G | 1000 | 10240 | 1000 | 0 | 689.59 | 1069.47 | 83.08 | 1375 | N/A | N/A |
|  CBR SOAP Header Proxy | 4G | 1000 | 102400 | 0 | 100 | 16806.57 | 39.21 | 63.71 | 329 | N/A | N/A |
|  CBR SOAP Header Proxy | 4G | 1000 | 102400 | 30 | 100 | 16180.44 | 39.73 | 71.25 | 341 | 91.21 | 26.825 |
|  CBR SOAP Header Proxy | 4G | 1000 | 102400 | 100 | 100 | 13542.9 | 44.06 | 109.55 | 353 | 93.56 | 26.829 |
|  CBR SOAP Header Proxy | 4G | 1000 | 102400 | 500 | 100 | 16096.73 | 40.17 | 75.96 | 369 | 92.03 | 26.879 |
|  CBR SOAP Header Proxy | 4G | 1000 | 102400 | 1000 | 100 | 17069.72 | 38.71 | 74.93 | 373 | 92.48 | 26.704 |
|  CBR Transport Header Proxy | 4G | 500 | 500 | 0 | 0 | 3137.2 | 119.68 | 71.49 | 359 | 95.73 | 26.825 |
|  CBR Transport Header Proxy | 4G | 500 | 500 | 30 | 59.32 | 7211.53 | 48.63 | 97.08 | 295 | N/A | N/A |
|  CBR Transport Header Proxy | 4G | 500 | 500 | 100 | 0 | 3133.82 | 119.54 | 33.77 | 279 | 96.99 | 26.695 |
|  CBR Transport Header Proxy | 4G | 500 | 500 | 500 | 0 | 735.63 | 503.72 | 12.22 | 543 | N/A | N/A |
|  CBR Transport Header Proxy | 4G | 500 | 500 | 1000 | 0 | 367.32 | 1002.26 | 3.49 | 1007 | N/A | N/A |
|  CBR Transport Header Proxy | 4G | 500 | 1024 | 0 | 59.72 | 8007.12 | 44.02 | 72.31 | 307 | N/A | N/A |
|  CBR Transport Header Proxy | 4G | 500 | 1024 | 30 | 0 | 3212.19 | 116.81 | 61.19 | 339 | 95.33 | 26.822 |
|  CBR Transport Header Proxy | 4G | 500 | 1024 | 100 | 0 | 3139.61 | 119.19 | 34.42 | 303 | N/A | N/A |
|  CBR Transport Header Proxy | 4G | 500 | 1024 | 500 | 0 | 741.41 | 503.6 | 12.29 | 551 | N/A | N/A |
|  CBR Transport Header Proxy | 4G | 500 | 1024 | 1000 | 0 | 366.76 | 1002.77 | 7.39 | 1019 | N/A | N/A |
|  CBR Transport Header Proxy | 4G | 500 | 10240 | 0 | 100 | 20911.91 | 15.57 | 28.79 | 146 | N/A | N/A |
|  CBR Transport Header Proxy | 4G | 500 | 10240 | 30 | 0 | 2423.32 | 154.8 | 60.95 | 369 | 96.4 | 26.917 |
|  CBR Transport Header Proxy | 4G | 500 | 10240 | 100 | 0 | 2556.33 | 146.46 | 43.87 | 339 | N/A | N/A |
|  CBR Transport Header Proxy | 4G | 500 | 10240 | 500 | 0 | 733.33 | 507.1 | 15.26 | 575 | N/A | N/A |
|  CBR Transport Header Proxy | 4G | 500 | 10240 | 1000 | 0 | 367.15 | 1002.1 | 1.88 | 1003 | N/A | N/A |
|  CBR Transport Header Proxy | 4G | 500 | 102400 | 0 | 0 | 927.28 | 402.54 | 191.26 | 923 | N/A | N/A |
|  CBR Transport Header Proxy | 4G | 500 | 102400 | 30 | 0 | 949.62 | 393.73 | 180.36 | 923 | N/A | N/A |
|  CBR Transport Header Proxy | 4G | 500 | 102400 | 100 | 0 | 949.65 | 393.97 | 149.6 | 831 | N/A | N/A |
|  CBR Transport Header Proxy | 4G | 500 | 102400 | 500 | 0 | 657.38 | 564.9 | 75.03 | 819 | N/A | N/A |
|  CBR Transport Header Proxy | 4G | 500 | 102400 | 1000 | 0 | 366.14 | 1007.69 | 12.76 | 1079 | N/A | N/A |
|  CBR Transport Header Proxy | 4G | 1000 | 500 | 0 | 100 | 19854.59 | 32.19 | 66.25 | 331 | N/A | N/A |
|  CBR Transport Header Proxy | 4G | 1000 | 500 | 30 | 0 | 3085.77 | 242.86 | 101.93 | 555 | 95.72 | 26.887 |
|  CBR Transport Header Proxy | 4G | 1000 | 500 | 100 | 0 | 3724.57 | 205.22 | 306.92 | 815 | N/A | N/A |
|  CBR Transport Header Proxy | 4G | 1000 | 500 | 500 | 100 | 18281.03 | 35.62 | 75.23 | 349 | N/A | N/A |
|  CBR Transport Header Proxy | 4G | 1000 | 500 | 1000 | 0 | 730.25 | 1005.15 | 19.25 | 1135 | 99.07 | 26.826 |
|  CBR Transport Header Proxy | 4G | 1000 | 1024 | 0 | 0 | 4494.78 | 165.16 | 92.56 | 447 | N/A | N/A |
|  CBR Transport Header Proxy | 4G | 1000 | 1024 | 30 | 100 | 19614.67 | 32.5 | 64.43 | 323 | N/A | N/A |
|  CBR Transport Header Proxy | 4G | 1000 | 1024 | 100 | 0 | 3808.2 | 196.23 | 71.18 | 445 | 95.47 | 26.893 |
|  CBR Transport Header Proxy | 4G | 1000 | 1024 | 500 | 0 | 1456.92 | 509.78 | 25.37 | 659 | N/A | N/A |
|  CBR Transport Header Proxy | 4G | 1000 | 1024 | 1000 | 0 | 732.38 | 1003.31 | 8.86 | 1031 | N/A | N/A |
|  CBR Transport Header Proxy | 4G | 1000 | 10240 | 0 | 83.71 | 9096.4 | 75.27 | 125.53 | 555 | N/A | N/A |
|  CBR Transport Header Proxy | 4G | 1000 | 10240 | 30 | 0 | 2312.88 | 324.41 | 116.99 | 675 | 96.38 | 26.889 |
|  CBR Transport Header Proxy | 4G | 1000 | 10240 | 100 | 0 | 3051.09 | 244.88 | 81.16 | 531 | N/A | N/A |
|  CBR Transport Header Proxy | 4G | 1000 | 10240 | 500 | 0 | 1411.77 | 529.1 | 50.05 | 759 | N/A | N/A |
|  CBR Transport Header Proxy | 4G | 1000 | 10240 | 1000 | 0 | 520.07 | 1503.13 | 2208.53 | 11583 | N/A | N/A |
|  CBR Transport Header Proxy | 4G | 1000 | 102400 | 0 | 100 | 18643.01 | 35.27 | 77.26 | 379 | N/A | N/A |
|  CBR Transport Header Proxy | 4G | 1000 | 102400 | 30 | 0 | 664.13 | 1120.31 | 236.39 | 1711 | 98.74 | 26.687 |
|  CBR Transport Header Proxy | 4G | 1000 | 102400 | 100 | 0 | 856.18 | 870.85 | 226.88 | 1471 | N/A | N/A |
|  CBR Transport Header Proxy | 4G | 1000 | 102400 | 500 | 0 | 722.83 | 1019.04 | 236.73 | 1623 | N/A | N/A |
|  CBR Transport Header Proxy | 4G | 1000 | 102400 | 1000 | 0 | 656.97 | 1109.42 | 126.18 | 1535 | N/A | N/A |
|  Direct Proxy | 4G | 500 | 500 | 0 | 0 | 3269.41 | 114.57 | 68.34 | 375 | 96.4 | 24.68 |
|  Direct Proxy | 4G | 500 | 500 | 30 | 27.62 | 4919.45 | 74.71 | 127.63 | 319 | N/A | N/A |
|  Direct Proxy | 4G | 500 | 500 | 100 | 0 | 3120.56 | 119.87 | 32.11 | 285 | 97.1 | 26.822 |
|  Direct Proxy | 4G | 500 | 500 | 500 | 0 | 735.48 | 504.55 | 15.67 | 579 | N/A | N/A |
|  Direct Proxy | 4G | 500 | 500 | 1000 | 0 | 366.89 | 1002.39 | 4.25 | 1011 | N/A | N/A |
|  Direct Proxy | 4G | 500 | 1024 | 0 | 0.4 | 2292.7 | 187.68 | 1694.44 | 297 | N/A | N/A |
|  Direct Proxy | 4G | 500 | 1024 | 30 | 0 | 3243.73 | 115.63 | 60.2 | 359 | 95.05 | 26.833 |
|  Direct Proxy | 4G | 500 | 1024 | 100 | 0 | 3199.79 | 116.98 | 33.69 | 303 | N/A | N/A |
|  Direct Proxy | 4G | 500 | 1024 | 500 | 0 | 737.69 | 504.75 | 14.92 | 587 | N/A | N/A |
|  Direct Proxy | 4G | 500 | 1024 | 1000 | 0 | 366.46 | 1002.28 | 3.93 | 1007 | N/A | N/A |
|  Direct Proxy | 4G | 500 | 10240 | 0 | 99.64 | 17664.57 | 17.68 | 41.29 | 162 | N/A | N/A |
|  Direct Proxy | 4G | 500 | 10240 | 30 | 0 | 2414.34 | 155.2 | 60.39 | 355 | 96.55 | 26.705 |
|  Direct Proxy | 4G | 500 | 10240 | 100 | 0 | 2514.95 | 148.33 | 45.38 | 349 | N/A | N/A |
|  Direct Proxy | 4G | 500 | 10240 | 500 | 0 | 734.41 | 505.15 | 11.98 | 559 | N/A | N/A |
|  Direct Proxy | 4G | 500 | 10240 | 1000 | 0 | 366.72 | 1002.5 | 5.47 | 1011 | N/A | N/A |
|  Direct Proxy | 4G | 500 | 102400 | 0 | 0 | 929.49 | 402.83 | 177.88 | 879 | N/A | N/A |
|  Direct Proxy | 4G | 500 | 102400 | 30 | 0 | 945.46 | 396.07 | 183.96 | 903 | N/A | N/A |
|  Direct Proxy | 4G | 500 | 102400 | 100 | 0 | 938.02 | 398.01 | 149.88 | 823 | N/A | N/A |
|  Direct Proxy | 4G | 500 | 102400 | 500 | 0 | 649.45 | 570.73 | 77.65 | 835 | N/A | N/A |
|  Direct Proxy | 4G | 500 | 102400 | 1000 | 0 | 364.26 | 1007.77 | 10.41 | 1063 | N/A | N/A |
|  Direct Proxy | 4G | 1000 | 500 | 0 | 100 | 19410.34 | 32.68 | 63.61 | 297 | N/A | N/A |
|  Direct Proxy | 4G | 1000 | 500 | 30 | 0 | 3368.05 | 222.55 | 101.35 | 551 | 95.18 | 26.749 |
|  Direct Proxy | 4G | 1000 | 500 | 100 | 0.83 | 3920.38 | 192.04 | 259.21 | 467 | N/A | N/A |
|  Direct Proxy | 4G | 1000 | 500 | 500 | 0 | 1413.75 | 522.51 | 40.05 | 719 | 98.39 | 26.708 |
|  Direct Proxy | 4G | 1000 | 500 | 1000 | 0 | 731.37 | 1004.63 | 14.39 | 1071 | N/A | N/A |
|  Direct Proxy | 4G | 1000 | 1024 | 0 | 0 | 4468.08 | 165.92 | 91.83 | 455 | N/A | N/A |
|  Direct Proxy | 4G | 1000 | 1024 | 30 | 98.79 | 9031.65 | 47.98 | 333.23 | 417 | N/A | N/A |
|  Direct Proxy | 4G | 1000 | 1024 | 100 | 0 | 3335.58 | 224.44 | 86.95 | 493 | 95.22 | 26.87 |
|  Direct Proxy | 4G | 1000 | 1024 | 500 | 0 | 1459.19 | 506.92 | 19.98 | 635 | N/A | N/A |
|  Direct Proxy | 4G | 1000 | 1024 | 1000 | 0 | 729.3 | 1003.89 | 12.17 | 1031 | N/A | N/A |
|  Direct Proxy | 4G | 1000 | 10240 | 0 | 86.87 | 9827.25 | 68.64 | 120.85 | 505 | N/A | N/A |
|  Direct Proxy | 4G | 1000 | 10240 | 30 | 0 | 2392.34 | 313.56 | 110.39 | 651 | 96.28 | 26.693 |
|  Direct Proxy | 4G | 1000 | 10240 | 100 | 0 | 3094.64 | 240.91 | 80.3 | 519 | N/A | N/A |
|  Direct Proxy | 4G | 1000 | 10240 | 500 | 0 | 1407.82 | 527.37 | 40.74 | 719 | N/A | N/A |
|  Direct Proxy | 4G | 1000 | 10240 | 1000 | 0 | 726.77 | 1008.54 | 22.32 | 1103 | N/A | N/A |
|  Direct Proxy | 4G | 1000 | 102400 | 0 | 99.87 | 11586.09 | 42.95 | 128.82 | 427 | N/A | N/A |
|  Direct Proxy | 4G | 1000 | 102400 | 30 | 0 | 714.5 | 1036.67 | 334.37 | 1943 | 98.91 | 26.827 |
|  Direct Proxy | 4G | 1000 | 102400 | 100 | 0 | 900.32 | 827.47 | 325.14 | 1703 | N/A | N/A |
|  Direct Proxy | 4G | 1000 | 102400 | 500 | 0 | 849.78 | 869.75 | 214.6 | 1535 | N/A | N/A |
|  Direct Proxy | 4G | 1000 | 102400 | 1000 | 0 | 682.09 | 1076.04 | 79.23 | 1383 | N/A | N/A |
|  XSLT Enhanced Proxy | 4G | 500 | 500 | 0 | 0 | 2671.22 | 140.64 | 104.92 | 527 | N/A | N/A |
|  XSLT Enhanced Proxy | 4G | 500 | 500 | 30 | 0 | 2769.39 | 135.35 | 86.26 | 465 | N/A | N/A |
|  XSLT Enhanced Proxy | 4G | 500 | 500 | 100 | 92.66 | 11582.06 | 29.67 | 74.57 | 229 | N/A | N/A |
|  XSLT Enhanced Proxy | 4G | 500 | 500 | 500 | 0 | 733.78 | 505.51 | 13.12 | 583 | 99 | 26.826 |
|  XSLT Enhanced Proxy | 4G | 500 | 500 | 1000 | 0 | 366.99 | 1002.78 | 5.35 | 1023 | N/A | N/A |
|  XSLT Enhanced Proxy | 4G | 500 | 1024 | 0 | 0 | 2626.64 | 143.1 | 113.39 | 547 | N/A | N/A |
|  XSLT Enhanced Proxy | 4G | 500 | 1024 | 30 | 0 | 2610.04 | 143.81 | 90.77 | 467 | N/A | N/A |
|  XSLT Enhanced Proxy | 4G | 500 | 1024 | 100 | 100 | 20307.27 | 15.82 | 30.22 | 159 | N/A | N/A |
|  XSLT Enhanced Proxy | 4G | 500 | 1024 | 500 | 0 | 728.03 | 511.28 | 20.31 | 615 | 98.91 | 26.903 |
|  XSLT Enhanced Proxy | 4G | 500 | 1024 | 1000 | 0 | 367.09 | 1002.58 | 4.82 | 1015 | N/A | N/A |
|  XSLT Enhanced Proxy | 4G | 500 | 10240 | 0 | 0 | 514.43 | 730.77 | 230 | 1327 | N/A | N/A |
|  XSLT Enhanced Proxy | 4G | 500 | 10240 | 30 | 0 | 563.32 | 665 | 222.87 | 1319 | N/A | N/A |
|  XSLT Enhanced Proxy | 4G | 500 | 10240 | 100 | 0 | 566.61 | 659.73 | 193.43 | 1175 | N/A | N/A |
|  XSLT Enhanced Proxy | 4G | 500 | 10240 | 500 | 0 | 499.11 | 743.26 | 132.27 | 1143 | N/A | N/A |
|  XSLT Enhanced Proxy | 4G | 500 | 10240 | 1000 | 0 | 344.77 | 1070.64 | 62.47 | 1263 | N/A | N/A |
|  XSLT Enhanced Proxy | 4G | 500 | 102400 | 0 | 0 | 66.08 | 5366.94 | 1219.41 | 7519 | N/A | N/A |
|  XSLT Enhanced Proxy | 4G | 500 | 102400 | 30 | 0 | 66.88 | 5268.15 | 941.19 | 7071 | N/A | N/A |
|  XSLT Enhanced Proxy | 4G | 500 | 102400 | 100 | 0 | 66.06 | 5388.73 | 1509.19 | 9151 | N/A | N/A |
|  XSLT Enhanced Proxy | 4G | 500 | 102400 | 500 | 0 | 67.59 | 5320.4 | 1124.63 | 7679 | N/A | N/A |
|  XSLT Enhanced Proxy | 4G | 500 | 102400 | 1000 | 0 | 67.04 | 5300.22 | 1170.12 | 8255 | N/A | N/A |
|  XSLT Enhanced Proxy | 4G | 1000 | 500 | 0 | 100 | 19871.14 | 31.27 | 56.97 | 283 | N/A | N/A |
|  XSLT Enhanced Proxy | 4G | 1000 | 500 | 30 | 0 | 1916.64 | 391.45 | 162.27 | 875 | 96.68 | 26.685 |
|  XSLT Enhanced Proxy | 4G | 1000 | 500 | 100 | 0 | 2757.92 | 271.73 | 116.36 | 675 | N/A | N/A |
|  XSLT Enhanced Proxy | 4G | 1000 | 500 | 500 | 0 | 1385.98 | 535.9 | 49.61 | 735 | N/A | N/A |
|  XSLT Enhanced Proxy | 4G | 1000 | 500 | 1000 | 0 | 730.13 | 1007.91 | 19.26 | 1119 | N/A | N/A |
|  XSLT Enhanced Proxy | 4G | 1000 | 1024 | 0 | 100 | 20354.65 | 32.35 | 71.72 | 359 | N/A | N/A |
|  XSLT Enhanced Proxy | 4G | 1000 | 1024 | 30 | 0 | 1774.6 | 421.37 | 186.36 | 967 | 96.61 | 26.825 |
|  XSLT Enhanced Proxy | 4G | 1000 | 1024 | 100 | 0 | 2428.84 | 308.65 | 129.14 | 751 | N/A | N/A |
|  XSLT Enhanced Proxy | 4G | 1000 | 1024 | 500 | 0 | 1375.49 | 540.2 | 49.14 | 731 | N/A | N/A |
|  XSLT Enhanced Proxy | 4G | 1000 | 1024 | 1000 | 0 | 730.02 | 1008.33 | 18.18 | 1111 | N/A | N/A |
|  XSLT Enhanced Proxy | 4G | 1000 | 10240 | 0 | 98.38 | 8573.47 | 77.1 | 273.99 | 1495 | N/A | N/A |
|  XSLT Enhanced Proxy | 4G | 1000 | 10240 | 30 | 0 | 463.67 | 1600.71 | 415.65 | 2767 | 97.86 | 26.823 |
|  XSLT Enhanced Proxy | 4G | 1000 | 10240 | 100 | 0 | 518 | 1434.7 | 377.15 | 2399 | N/A | N/A |
|  XSLT Enhanced Proxy | 4G | 1000 | 10240 | 500 | 0 | 532.4 | 1376.78 | 331.43 | 2319 | N/A | N/A |
|  XSLT Enhanced Proxy | 4G | 1000 | 10240 | 1000 | 0 | 506.04 | 1442.33 | 301.19 | 2463 | N/A | N/A |
|  XSLT Enhanced Proxy | 4G | 1000 | 102400 | 0 | 99.92 | 7891.87 | 76.7 | 427.19 | 551 | N/A | N/A |
|  XSLT Enhanced Proxy | 4G | 1000 | 102400 | 30 | 94 | 6.02 | 118328.26 | 13266.76 | 129023 | 97.29 | 26.876 |
|  XSLT Enhanced Proxy | 4G | 1000 | 102400 | 100 | 100 | 5302.81 | 101.72 | 886.34 | 487 | 84.47 | 26.825 |
|  XSLT Enhanced Proxy | 4G | 1000 | 102400 | 500 | 99.86 | 6436.7 | 91.69 | 597.34 | 715 | 97.37 | 26.823 |
|  XSLT Enhanced Proxy | 4G | 1000 | 102400 | 1000 | 99.92 | 8032.4 | 76.72 | 437.59 | 623 | 97.23 | 26.821 |
|  XSLT Proxy | 4G | 500 | 500 | 0 | 0 | 1453.21 | 257.02 | 125.97 | 655 | 96.64 | 26.824 |
|  XSLT Proxy | 4G | 500 | 500 | 30 | 0 | 1906.85 | 196.81 | 90.84 | 465 | N/A | N/A |
|  XSLT Proxy | 4G | 500 | 500 | 100 | 0 | 1959.69 | 191.24 | 60.7 | 391 | N/A | N/A |
|  XSLT Proxy | 4G | 500 | 500 | 500 | 0 | 687.46 | 541.47 | 177.63 | 1711 | N/A | N/A |
|  XSLT Proxy | 4G | 500 | 500 | 1000 | 0 | 364.48 | 1004.25 | 10.95 | 1047 | 99.15 | 26.774 |
|  XSLT Proxy | 4G | 500 | 1024 | 0 | 0 | 1686.12 | 222.94 | 111.67 | 551 | N/A | N/A |
|  XSLT Proxy | 4G | 500 | 1024 | 30 | 0 | 1656.99 | 226.29 | 99.2 | 527 | N/A | N/A |
|  XSLT Proxy | 4G | 500 | 1024 | 100 | 0 | 1598.06 | 234.76 | 134.89 | 999 | N/A | N/A |
|  XSLT Proxy | 4G | 500 | 1024 | 500 | 0 | 704.92 | 527.16 | 39.69 | 687 | 98.46 | 26.824 |
|  XSLT Proxy | 4G | 500 | 1024 | 1000 | 0 | 366.49 | 1004.96 | 11.72 | 1063 | N/A | N/A |
|  XSLT Proxy | 4G | 500 | 10240 | 0 | 0 | 349.06 | 1060.23 | 342.63 | 1983 | N/A | N/A |
|  XSLT Proxy | 4G | 500 | 10240 | 30 | 0 | 354.56 | 1055.37 | 342.15 | 1983 | N/A | N/A |
|  XSLT Proxy | 4G | 500 | 10240 | 100 | 0 | 346.17 | 1076.93 | 334.31 | 2007 | N/A | N/A |
|  XSLT Proxy | 4G | 500 | 10240 | 500 | 0 | 351.87 | 1056.85 | 297.33 | 1807 | N/A | N/A |
|  XSLT Proxy | 4G | 500 | 10240 | 1000 | 0 | 304.09 | 1206.93 | 194.97 | 1839 | N/A | N/A |
|  XSLT Proxy | 4G | 500 | 102400 | 0 | 100 | 10519.79 | 28.58 | 120.8 | 225 | N/A | N/A |
|  XSLT Proxy | 4G | 500 | 102400 | 30 | 99.99 | 3666.46 | 77.93 | 801.42 | 347 | 94.12 | 26.817 |
|  XSLT Proxy | 4G | 500 | 102400 | 100 | 100 | 5468.97 | 46.12 | 439.88 | 291 | 94.31 | 26.882 |
|  XSLT Proxy | 4G | 500 | 102400 | 500 | 100 | 5782.68 | 45.26 | 352.62 | 307 | 93.93 | 26.7 |
|  XSLT Proxy | 4G | 500 | 102400 | 1000 | 100 | 6690.05 | 43.54 | 330.43 | 279 | 93.56 | 26.915 |
|  XSLT Proxy | 4G | 1000 | 500 | 0 | 0 | 1428.59 | 524.33 | 234.95 | 1263 | 96.51 | 26.823 |
|  XSLT Proxy | 4G | 1000 | 500 | 30 | 0 | 1741.93 | 430.25 | 167.56 | 879 | N/A | N/A |
|  XSLT Proxy | 4G | 1000 | 500 | 100 | 94.74 | 12055.24 | 55.41 | 101.77 | 467 | N/A | N/A |
|  XSLT Proxy | 4G | 1000 | 500 | 500 | 0 | 1333.86 | 557.02 | 50.94 | 739 | 97.42 | 26.826 |
|  XSLT Proxy | 4G | 1000 | 500 | 1000 | 0 | 721.12 | 1011.87 | 24.64 | 1143 | N/A | N/A |
|  XSLT Proxy | 4G | 1000 | 1024 | 0 | 0 | 1651.58 | 453.43 | 216.26 | 1063 | N/A | N/A |
|  XSLT Proxy | 4G | 1000 | 1024 | 30 | 89.52 | 7200.03 | 96.1 | 221.76 | 691 | N/A | N/A |
|  XSLT Proxy | 4G | 1000 | 1024 | 100 | 0 | 1304.67 | 572.61 | 215.76 | 1199 | 96.49 | 26.832 |
|  XSLT Proxy | 4G | 1000 | 1024 | 500 | 0 | 1249.66 | 594.31 | 77.81 | 859 | N/A | N/A |
|  XSLT Proxy | 4G | 1000 | 1024 | 1000 | 0 | 714.92 | 1027.85 | 43.97 | 1223 | N/A | N/A |
|  XSLT Proxy | 4G | 1000 | 10240 | 0 | 0 | 279.78 | 2363.8 | 1237.98 | 8703 | N/A | N/A |
|  XSLT Proxy | 4G | 1000 | 10240 | 30 | 100 | 17030.77 | 37.61 | 71.25 | 371 | N/A | N/A |
|  XSLT Proxy | 4G | 1000 | 10240 | 100 | 0 | 283.83 | 2572.99 | 714.5 | 4287 | 96.48 | 26.706 |
|  XSLT Proxy | 4G | 1000 | 10240 | 500 | 0 | 299.49 | 2420.54 | 637.31 | 3919 | N/A | N/A |
|  XSLT Proxy | 4G | 1000 | 10240 | 1000 | 99.76 | 9711.43 | 63.6 | 181.38 | 595 | N/A | N/A |
|  XSLT Proxy | 4G | 1000 | 102400 | 0 | 100 | 9031.71 | 55.59 | 220.63 | 437 | 92.32 | 26.828 |
|  XSLT Proxy | 4G | 1000 | 102400 | 30 | 100 | 10302.52 | 57.35 | 106.37 | 523 | 93.7 | 22.163 |
|  XSLT Proxy | 4G | 1000 | 102400 | 100 | 100 | 11704.57 | 50.52 | 95.66 | 427 | 93.21 | 26.826 |
|  XSLT Proxy | 4G | 1000 | 102400 | 500 | 100 | 11705.4 | 51.01 | 88.62 | 429 | 93.35 | 26.732 |
|  XSLT Proxy | 4G | 1000 | 102400 | 1000 | 100 | 10226.37 | 57.44 | 110.01 | 489 | 93.64 | 26.826 |
