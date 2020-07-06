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
| System | Processor | Model name | Intel(R) Xeon(R) Platinum 8275CL CPU @ 3.00GHz |
| System | Memory | BIOS | 64 KiB |
| System | Memory | System memory | 3814092 KiB |
| System | Storage | Block Device: nvme0n1 | 8G |
| Operating System | Distribution | Release | Ubuntu 18.04.4 LTS |
| Operating System | Distribution | Kernel | Linux ip-10-0-1-130 5.3.0-1023-aws #25~18.04.1-Ubuntu SMP Fri Jun 5 15:18:30 UTC 2020 x86_64 x86_64 x86_64 GNU/Linux |


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
|  CBR Proxy | 512M | 100 | 500 | 0 | 0 | 1860.97 | 40.49 | 101.3 | 731 | N/A | N/A |
|  CBR Proxy | 512M | 100 | 500 | 30 | 0 | 1740.52 | 43.32 | 36.05 | 88 | N/A | N/A |
|  CBR Proxy | 512M | 100 | 500 | 100 | 0 | 711.51 | 105.91 | 9.85 | 147 | N/A | N/A |
|  CBR Proxy | 512M | 100 | 500 | 500 | 0 | 149.24 | 501.38 | 1.68 | 503 | N/A | N/A |
|  CBR Proxy | 512M | 100 | 500 | 1000 | 0 | 73.99 | 1002.09 | 1.07 | 1003 | N/A | N/A |
|  CBR Proxy | 512M | 100 | 1024 | 0 | 0 | 2123.82 | 35.61 | 55.1 | 120 | N/A | N/A |
|  CBR Proxy | 512M | 100 | 1024 | 30 | 0 | 1697.98 | 44.37 | 33.03 | 87 | N/A | N/A |
|  CBR Proxy | 512M | 100 | 1024 | 100 | 0 | 714.48 | 105.46 | 9.08 | 145 | N/A | N/A |
|  CBR Proxy | 512M | 100 | 1024 | 500 | 0 | 149.09 | 501.55 | 1.93 | 503 | N/A | N/A |
|  CBR Proxy | 512M | 100 | 1024 | 1000 | 0 | 74.13 | 1002.07 | 0.87 | 1003 | N/A | N/A |
|  CBR Proxy | 512M | 100 | 10240 | 0 | 0 | 715.13 | 104.13 | 50.61 | 245 | N/A | N/A |
|  CBR Proxy | 512M | 100 | 10240 | 30 | 0 | 686.7 | 109.87 | 40.44 | 224 | N/A | N/A |
|  CBR Proxy | 512M | 100 | 10240 | 100 | 0 | 523.58 | 143.83 | 28.55 | 230 | N/A | N/A |
|  CBR Proxy | 512M | 100 | 10240 | 500 | 0 | 147.62 | 506.49 | 9.14 | 543 | N/A | N/A |
|  CBR Proxy | 512M | 100 | 10240 | 1000 | 0 | 73.86 | 1003.91 | 5.59 | 1039 | N/A | N/A |
|  CBR Proxy | 512M | 100 | 102400 | 0 | 0 | 44.43 | 1692.53 | 731.89 | 3407 | N/A | N/A |
|  CBR Proxy | 512M | 100 | 102400 | 30 | 0 | 42.1 | 1783.13 | 749.14 | 3791 | N/A | N/A |
|  CBR Proxy | 512M | 100 | 102400 | 100 | 0 | 42.94 | 1760.81 | 733.2 | 3471 | N/A | N/A |
|  CBR Proxy | 512M | 100 | 102400 | 500 | 0 | 38.33 | 1938.7 | 866.76 | 4127 | N/A | N/A |
|  CBR Proxy | 512M | 100 | 102400 | 1000 | 0 | 34.49 | 2148.52 | 797.31 | 4255 | N/A | N/A |
|  CBR Proxy | 512M | 200 | 500 | 0 | 0 | 1882.95 | 79.86 | 120.73 | 715 | N/A | N/A |
|  CBR Proxy | 512M | 200 | 500 | 30 | 0 | 2014.46 | 74.69 | 102.26 | 683 | N/A | N/A |
|  CBR Proxy | 512M | 200 | 500 | 100 | 0 | 1294.94 | 115.83 | 18.5 | 169 | N/A | N/A |
|  CBR Proxy | 512M | 200 | 500 | 500 | 0 | 296.7 | 502.22 | 4.7 | 531 | N/A | N/A |
|  CBR Proxy | 512M | 200 | 500 | 1000 | 0 | 146.94 | 1002.46 | 5.11 | 1015 | N/A | N/A |
|  CBR Proxy | 512M | 200 | 1024 | 0 | 0 | 2317.28 | 64.9 | 83.78 | 206 | N/A | N/A |
|  CBR Proxy | 512M | 200 | 1024 | 30 | 0 | 2267.5 | 66.29 | 26.16 | 126 | N/A | N/A |
|  CBR Proxy | 512M | 200 | 1024 | 100 | 0 | 1287.26 | 116.54 | 24.15 | 169 | N/A | N/A |
|  CBR Proxy | 512M | 200 | 1024 | 500 | 0 | 296.48 | 502.41 | 4.73 | 535 | N/A | N/A |
|  CBR Proxy | 512M | 200 | 1024 | 1000 | 0 | 147.27 | 1002.35 | 2.69 | 1019 | N/A | N/A |
|  CBR Proxy | 512M | 200 | 10240 | 0 | 0 | 685.61 | 218.21 | 125.69 | 783 | N/A | N/A |
|  CBR Proxy | 512M | 200 | 10240 | 30 | 0 | 684.1 | 219.6 | 123.09 | 819 | N/A | N/A |
|  CBR Proxy | 512M | 200 | 10240 | 100 | 0 | 634.59 | 237.9 | 100.99 | 675 | N/A | N/A |
|  CBR Proxy | 512M | 200 | 10240 | 500 | 0 | 277.95 | 536.52 | 55.18 | 759 | N/A | N/A |
|  CBR Proxy | 512M | 200 | 10240 | 1000 | 0 | 145.63 | 1009.59 | 13.82 | 1071 | N/A | N/A |
|  CBR Proxy | 512M | 200 | 102400 | 0 | 0 | 17.3 | 8395.69 | 4435.61 | 17151 | N/A | N/A |
|  CBR Proxy | 512M | 200 | 102400 | 30 | 0 | 16.94 | 8790.15 | 4843.32 | 18687 | N/A | N/A |
|  CBR Proxy | 512M | 200 | 102400 | 100 | 3.65 | 4.39 | 28661.97 | 35172.79 | 120319 | N/A | N/A |
|  CBR Proxy | 512M | 200 | 102400 | 500 | 0 | 8.55 | 18000.62 | 9919.42 | 35839 | N/A | N/A |
|  CBR Proxy | 512M | 200 | 102400 | 1000 | 0 | 15.88 | 9223.35 | 4110.95 | 16511 | N/A | N/A |
|  CBR SOAP Header Proxy | 512M | 100 | 500 | 0 | 0 | 1614.62 | 46.77 | 127.06 | 751 | N/A | N/A |
|  CBR SOAP Header Proxy | 512M | 100 | 500 | 30 | 0 | 1708.76 | 44.12 | 44.69 | 91 | N/A | N/A |
|  CBR SOAP Header Proxy | 512M | 100 | 500 | 100 | 0 | 703.59 | 107.03 | 18.52 | 146 | N/A | N/A |
|  CBR SOAP Header Proxy | 512M | 100 | 500 | 500 | 0 | 148.96 | 501.57 | 2.35 | 515 | N/A | N/A |
|  CBR SOAP Header Proxy | 512M | 100 | 500 | 1000 | 0 | 74.09 | 1002.11 | 1.03 | 1007 | N/A | N/A |
|  CBR SOAP Header Proxy | 512M | 100 | 1024 | 0 | 0 | 1697.88 | 44.42 | 104.18 | 715 | N/A | N/A |
|  CBR SOAP Header Proxy | 512M | 100 | 1024 | 30 | 0 | 1712.48 | 44.03 | 35.67 | 90 | N/A | N/A |
|  CBR SOAP Header Proxy | 512M | 100 | 1024 | 100 | 0 | 715.21 | 105.58 | 9.55 | 146 | N/A | N/A |
|  CBR SOAP Header Proxy | 512M | 100 | 1024 | 500 | 0 | 149.08 | 501.5 | 1.92 | 505 | N/A | N/A |
|  CBR SOAP Header Proxy | 512M | 100 | 1024 | 1000 | 0 | 74.12 | 1002.09 | 1.09 | 1007 | N/A | N/A |
|  CBR SOAP Header Proxy | 512M | 100 | 10240 | 0 | 0 | 1014.14 | 73.42 | 61.61 | 181 | N/A | N/A |
|  CBR SOAP Header Proxy | 512M | 100 | 10240 | 30 | 0 | 957.25 | 78.73 | 32.08 | 166 | N/A | N/A |
|  CBR SOAP Header Proxy | 512M | 100 | 10240 | 100 | 0 | 587.89 | 128.21 | 36.87 | 204 | N/A | N/A |
|  CBR SOAP Header Proxy | 512M | 100 | 10240 | 500 | 0 | 148.31 | 504.79 | 5.9 | 535 | N/A | N/A |
|  CBR SOAP Header Proxy | 512M | 100 | 10240 | 1000 | 0 | 73.84 | 1002.79 | 4.45 | 1031 | N/A | N/A |
|  CBR SOAP Header Proxy | 512M | 100 | 102400 | 0 | 0 | 77.02 | 980.11 | 475.12 | 2223 | N/A | N/A |
|  CBR SOAP Header Proxy | 512M | 100 | 102400 | 30 | 0 | 79.87 | 949.38 | 439.25 | 2159 | N/A | N/A |
|  CBR SOAP Header Proxy | 512M | 100 | 102400 | 100 | 0 | 78.33 | 970.8 | 445.64 | 2239 | N/A | N/A |
|  CBR SOAP Header Proxy | 512M | 100 | 102400 | 500 | 0 | 66.9 | 1116.82 | 360.66 | 1863 | N/A | N/A |
|  CBR SOAP Header Proxy | 512M | 100 | 102400 | 1000 | 0 | 48.53 | 1537.91 | 433.99 | 2447 | N/A | N/A |
|  CBR SOAP Header Proxy | 512M | 200 | 500 | 0 | 0 | 1575.43 | 95.6 | 155.04 | 771 | N/A | N/A |
|  CBR SOAP Header Proxy | 512M | 200 | 500 | 30 | 0 | 1630.5 | 92.13 | 155.11 | 775 | N/A | N/A |
|  CBR SOAP Header Proxy | 512M | 200 | 500 | 100 | 0 | 1267.33 | 118.43 | 38.56 | 170 | N/A | N/A |
|  CBR SOAP Header Proxy | 512M | 200 | 500 | 500 | 0 | 296.8 | 502.05 | 4.36 | 531 | N/A | N/A |
|  CBR SOAP Header Proxy | 512M | 200 | 500 | 1000 | 0 | 146.96 | 1002.24 | 1.91 | 1015 | N/A | N/A |
|  CBR SOAP Header Proxy | 512M | 200 | 1024 | 0 | 0 | 2295.09 | 65.68 | 83.14 | 667 | N/A | N/A |
|  CBR SOAP Header Proxy | 512M | 200 | 1024 | 30 | 0 | 2309.13 | 64.9 | 50.64 | 121 | N/A | N/A |
|  CBR SOAP Header Proxy | 512M | 200 | 1024 | 100 | 0 | 1303.47 | 114.72 | 18.37 | 168 | N/A | N/A |
|  CBR SOAP Header Proxy | 512M | 200 | 1024 | 500 | 0 | 295.38 | 505.87 | 23.09 | 551 | N/A | N/A |
|  CBR SOAP Header Proxy | 512M | 200 | 1024 | 1000 | 0 | 147.31 | 1002.21 | 1.95 | 1011 | N/A | N/A |
|  CBR SOAP Header Proxy | 512M | 200 | 10240 | 0 | 0 | 1049.02 | 143.32 | 89.97 | 591 | N/A | N/A |
|  CBR SOAP Header Proxy | 512M | 200 | 10240 | 30 | 0 | 1034.78 | 145.25 | 106.71 | 751 | N/A | N/A |
|  CBR SOAP Header Proxy | 512M | 200 | 10240 | 100 | 0 | 864.51 | 173.75 | 79.16 | 739 | N/A | N/A |
|  CBR SOAP Header Proxy | 512M | 200 | 10240 | 500 | 0 | 283.27 | 526.02 | 58.19 | 903 | N/A | N/A |
|  CBR SOAP Header Proxy | 512M | 200 | 10240 | 1000 | 0 | 146.87 | 1003.64 | 6.29 | 1039 | N/A | N/A |
|  CBR SOAP Header Proxy | 512M | 200 | 102400 | 0 | 16.45 | 7.49 | 23394.84 | 43997.39 | 130047 | N/A | N/A |
|  CBR SOAP Header Proxy | 512M | 200 | 102400 | 30 | 100 | 1.09 | 119228.47 | 6406.36 | 132095 | N/A | N/A |
|  CBR SOAP Header Proxy | 512M | 200 | 102400 | 100 | 100 | 12757.36 | 10.82 | 7.29 | 36 | N/A | N/A |
|  CBR SOAP Header Proxy | 512M | 200 | 102400 | 500 | 100 | 12875.35 | 10.77 | 6.98 | 35 | N/A | N/A |
|  CBR SOAP Header Proxy | 512M | 200 | 102400 | 1000 | 100 | 12904.71 | 10.77 | 5.91 | 32 | N/A | N/A |
|  CBR Transport Header Proxy | 512M | 100 | 500 | 0 | 100 | 13211.48 | 5.31 | 2.9 | 15 | N/A | N/A |
|  CBR Transport Header Proxy | 512M | 100 | 500 | 30 | 100 | 13081.78 | 5.39 | 2.74 | 15 | N/A | N/A |
|  CBR Transport Header Proxy | 512M | 100 | 500 | 100 | 100 | 13232.71 | 5.31 | 2.75 | 15 | N/A | N/A |
|  CBR Transport Header Proxy | 512M | 100 | 500 | 500 | 100 | 13209.16 | 5.33 | 2.85 | 15 | N/A | N/A |
|  CBR Transport Header Proxy | 512M | 100 | 500 | 1000 | 100 | 13212.04 | 5.33 | 2.75 | 15 | N/A | N/A |
|  CBR Transport Header Proxy | 512M | 100 | 1024 | 0 | 100 | 13287.07 | 5.29 | 2.82 | 16 | N/A | N/A |
|  CBR Transport Header Proxy | 512M | 100 | 1024 | 30 | 100 | 13092.83 | 5.37 | 2.72 | 15 | N/A | N/A |
|  CBR Transport Header Proxy | 512M | 100 | 1024 | 100 | 100 | 13154.97 | 5.33 | 2.82 | 16 | N/A | N/A |
|  CBR Transport Header Proxy | 512M | 100 | 1024 | 500 | 100 | 13217.68 | 5.32 | 2.69 | 16 | N/A | N/A |
|  CBR Transport Header Proxy | 512M | 100 | 1024 | 1000 | 100 | 13299.83 | 5.3 | 2.92 | 16 | N/A | N/A |
|  CBR Transport Header Proxy | 512M | 100 | 10240 | 0 | 100 | 13052.87 | 5.37 | 3.03 | 17 | N/A | N/A |
|  CBR Transport Header Proxy | 512M | 100 | 10240 | 30 | 100 | 12707.28 | 5.53 | 3.33 | 18 | N/A | N/A |
|  CBR Transport Header Proxy | 512M | 100 | 10240 | 100 | 100 | 13283.26 | 5.29 | 2.99 | 16 | N/A | N/A |
|  CBR Transport Header Proxy | 512M | 100 | 10240 | 500 | 100 | 13217.38 | 5.27 | 2.7 | 15 | N/A | N/A |
|  CBR Transport Header Proxy | 512M | 100 | 10240 | 1000 | 100 | 13190.49 | 5.33 | 3.03 | 16 | N/A | N/A |
|  CBR Transport Header Proxy | 512M | 100 | 102400 | 0 | 100 | 13163.92 | 5.31 | 2.77 | 16 | N/A | N/A |
|  CBR Transport Header Proxy | 512M | 100 | 102400 | 30 | 100 | 13128.49 | 5.33 | 3.04 | 16 | N/A | N/A |
|  CBR Transport Header Proxy | 512M | 100 | 102400 | 100 | 100 | 13219.34 | 5.29 | 2.89 | 16 | N/A | N/A |
|  CBR Transport Header Proxy | 512M | 100 | 102400 | 500 | 100 | 13143.85 | 5.33 | 2.83 | 16 | N/A | N/A |
|  CBR Transport Header Proxy | 512M | 100 | 102400 | 1000 | 100 | 13036.86 | 5.33 | 3.02 | 16 | N/A | N/A |
|  CBR Transport Header Proxy | 512M | 200 | 500 | 0 | 100 | 12821.54 | 10.47 | 7.88 | 39 | N/A | N/A |
|  CBR Transport Header Proxy | 512M | 200 | 500 | 30 | 100 | 12978.83 | 10.62 | 7.04 | 36 | N/A | N/A |
|  CBR Transport Header Proxy | 512M | 200 | 500 | 100 | 100 | 12855.92 | 10.55 | 7.45 | 36 | N/A | N/A |
|  CBR Transport Header Proxy | 512M | 200 | 500 | 500 | 100 | 12912.26 | 10.52 | 7.79 | 37 | N/A | N/A |
|  CBR Transport Header Proxy | 512M | 200 | 500 | 1000 | 100 | 12946.32 | 10.45 | 7.21 | 36 | N/A | N/A |
|  CBR Transport Header Proxy | 512M | 200 | 1024 | 0 | 100 | 12944.91 | 10.54 | 6.44 | 34 | N/A | N/A |
|  CBR Transport Header Proxy | 512M | 200 | 1024 | 30 | 100 | 12881.29 | 10.54 | 6.6 | 35 | N/A | N/A |
|  CBR Transport Header Proxy | 512M | 200 | 1024 | 100 | 100 | 12963.88 | 10.5 | 7.98 | 39 | N/A | N/A |
|  CBR Transport Header Proxy | 512M | 200 | 1024 | 500 | 100 | 12921.57 | 10.57 | 7.27 | 36 | N/A | N/A |
|  CBR Transport Header Proxy | 512M | 200 | 1024 | 1000 | 100 | 12925.76 | 10.55 | 7.21 | 35 | N/A | N/A |
|  CBR Transport Header Proxy | 512M | 200 | 10240 | 0 | 100 | 12883.64 | 10.49 | 6.72 | 35 | N/A | N/A |
|  CBR Transport Header Proxy | 512M | 200 | 10240 | 30 | 100 | 12773.18 | 10.62 | 8.45 | 41 | N/A | N/A |
|  CBR Transport Header Proxy | 512M | 200 | 10240 | 100 | 100 | 12999.27 | 10.48 | 6.5 | 33 | N/A | N/A |
|  CBR Transport Header Proxy | 512M | 200 | 10240 | 500 | 100 | 12994.48 | 10.49 | 6.46 | 35 | N/A | N/A |
|  CBR Transport Header Proxy | 512M | 200 | 10240 | 1000 | 100 | 12830.8 | 10.66 | 7.98 | 38 | N/A | N/A |
|  CBR Transport Header Proxy | 512M | 200 | 102400 | 0 | 100 | 12795.62 | 10.71 | 7.07 | 37 | N/A | N/A |
|  CBR Transport Header Proxy | 512M | 200 | 102400 | 30 | 100 | 12895.36 | 10.55 | 7.23 | 36 | N/A | N/A |
|  CBR Transport Header Proxy | 512M | 200 | 102400 | 100 | 100 | 12647.42 | 10.58 | 8.55 | 43 | N/A | N/A |
|  CBR Transport Header Proxy | 512M | 200 | 102400 | 500 | 100 | 12869.37 | 10.42 | 7.57 | 36 | N/A | N/A |
|  CBR Transport Header Proxy | 512M | 200 | 102400 | 1000 | 100 | 12971.84 | 10.57 | 7.61 | 38 | N/A | N/A |
|  Direct Proxy | 512M | 100 | 500 | 0 | 0 | 2112.89 | 36.02 | 85.24 | 723 | 76.4 | 277.008 |
|  Direct Proxy | 512M | 100 | 500 | 30 | 0 | 1899.07 | 39.69 | 31.35 | 83 | N/A | N/A |
|  Direct Proxy | 512M | 100 | 500 | 100 | 0 | 720.94 | 104.5 | 24.16 | 144 | N/A | N/A |
|  Direct Proxy | 512M | 100 | 500 | 500 | 0 | 149.27 | 501.27 | 1.35 | 503 | N/A | N/A |
|  Direct Proxy | 512M | 100 | 500 | 1000 | 0 | 74.13 | 1002.03 | 0.5 | 1003 | N/A | N/A |
|  Direct Proxy | 512M | 100 | 1024 | 0 | 0 | 2133.07 | 35.32 | 115.51 | 751 | N/A | N/A |
|  Direct Proxy | 512M | 100 | 1024 | 30 | 0 | 1865.42 | 40.37 | 45.17 | 85 | N/A | N/A |
|  Direct Proxy | 512M | 100 | 1024 | 100 | 0 | 716.78 | 105.1 | 17.66 | 148 | N/A | N/A |
|  Direct Proxy | 512M | 100 | 1024 | 500 | 0 | 149.25 | 501.22 | 1.11 | 503 | N/A | N/A |
|  Direct Proxy | 512M | 100 | 1024 | 1000 | 0 | 74.15 | 1002.03 | 0.53 | 1003 | N/A | N/A |
|  Direct Proxy | 512M | 100 | 10240 | 0 | 0 | 1746.55 | 43.16 | 105.35 | 699 | N/A | N/A |
|  Direct Proxy | 512M | 100 | 10240 | 30 | 0 | 1708.68 | 44.06 | 45.65 | 93 | N/A | N/A |
|  Direct Proxy | 512M | 100 | 10240 | 100 | 0 | 710.69 | 105.98 | 20.26 | 148 | N/A | N/A |
|  Direct Proxy | 512M | 100 | 10240 | 500 | 0 | 149.07 | 501.52 | 1.34 | 503 | N/A | N/A |
|  Direct Proxy | 512M | 100 | 10240 | 1000 | 0 | 74.09 | 1002.06 | 0.77 | 1003 | N/A | N/A |
|  Direct Proxy | 512M | 100 | 102400 | 0 | 0 | 1039.78 | 72.31 | 43.23 | 147 | N/A | N/A |
|  Direct Proxy | 512M | 100 | 102400 | 30 | 0 | 969.03 | 77.49 | 23.16 | 149 | N/A | N/A |
|  Direct Proxy | 512M | 100 | 102400 | 100 | 0 | 589.71 | 127.73 | 21.46 | 188 | N/A | N/A |
|  Direct Proxy | 512M | 100 | 102400 | 500 | 0 | 148.38 | 503.65 | 1.94 | 509 | N/A | N/A |
|  Direct Proxy | 512M | 100 | 102400 | 1000 | 0 | 73.75 | 1002.37 | 1.34 | 1007 | N/A | N/A |
|  Direct Proxy | 512M | 200 | 500 | 0 | 0 | 3318.73 | 45.03 | 76.8 | 149 | N/A | N/A |
|  Direct Proxy | 512M | 200 | 500 | 30 | 0 | 2387.87 | 62.95 | 110.43 | 755 | N/A | N/A |
|  Direct Proxy | 512M | 200 | 500 | 100 | 0 | 1369.56 | 109.7 | 13.67 | 161 | N/A | N/A |
|  Direct Proxy | 512M | 200 | 500 | 500 | 0 | 296.6 | 501.79 | 3.66 | 527 | N/A | N/A |
|  Direct Proxy | 512M | 200 | 500 | 1000 | 0 | 147.44 | 1002.13 | 1.52 | 1007 | N/A | N/A |
|  Direct Proxy | 512M | 200 | 1024 | 0 | 0 | 1238.15 | 121.38 | 258.09 | 1383 | N/A | N/A |
|  Direct Proxy | 512M | 200 | 1024 | 30 | 0 | 2109.71 | 71.08 | 119.51 | 731 | N/A | N/A |
|  Direct Proxy | 512M | 200 | 1024 | 100 | 0 | 1284.74 | 116.71 | 62.66 | 168 | N/A | N/A |
|  Direct Proxy | 512M | 200 | 1024 | 500 | 0 | 297.42 | 501.37 | 2.12 | 507 | N/A | N/A |
|  Direct Proxy | 512M | 200 | 1024 | 1000 | 0 | 147.31 | 1002.18 | 1.79 | 1007 | N/A | N/A |
|  Direct Proxy | 512M | 200 | 10240 | 0 | 0 | 2157.15 | 69.69 | 95.96 | 687 | N/A | N/A |
|  Direct Proxy | 512M | 200 | 10240 | 30 | 0 | 2341.07 | 64.12 | 36.65 | 126 | N/A | N/A |
|  Direct Proxy | 512M | 200 | 10240 | 100 | 0 | 1252.55 | 119.82 | 33.6 | 183 | N/A | N/A |
|  Direct Proxy | 512M | 200 | 10240 | 500 | 0 | 296.58 | 502.57 | 5.25 | 535 | N/A | N/A |
|  Direct Proxy | 512M | 200 | 10240 | 1000 | 0 | 146.94 | 1002.15 | 1.54 | 1007 | N/A | N/A |
|  Direct Proxy | 512M | 200 | 102400 | 0 | 0 | 1007.34 | 149.06 | 47.81 | 285 | N/A | N/A |
|  Direct Proxy | 512M | 200 | 102400 | 30 | 0 | 1001.79 | 149.57 | 56.23 | 269 | N/A | N/A |
|  Direct Proxy | 512M | 200 | 102400 | 100 | 0 | 798.38 | 187.77 | 50.44 | 319 | N/A | N/A |
|  Direct Proxy | 512M | 200 | 102400 | 500 | 0 | 292.05 | 510.86 | 12.95 | 567 | N/A | N/A |
|  Direct Proxy | 512M | 200 | 102400 | 1000 | 0 | 146.55 | 1002.97 | 2.51 | 1011 | N/A | N/A |
|  XSLT Enhanced Proxy | 512M | 100 | 500 | 0 | 100 | 13014.2 | 5.39 | 3.24 | 17 | N/A | N/A |
|  XSLT Enhanced Proxy | 512M | 100 | 500 | 30 | 100 | 13245.22 | 5.31 | 2.76 | 15 | N/A | N/A |
|  XSLT Enhanced Proxy | 512M | 100 | 500 | 100 | 100 | 13232.26 | 5.32 | 2.87 | 16 | N/A | N/A |
|  XSLT Enhanced Proxy | 512M | 100 | 500 | 500 | 100 | 13133.02 | 5.35 | 2.92 | 16 | N/A | N/A |
|  XSLT Enhanced Proxy | 512M | 100 | 500 | 1000 | 100 | 13292.75 | 5.31 | 2.87 | 16 | N/A | N/A |
|  XSLT Enhanced Proxy | 512M | 100 | 1024 | 0 | 100 | 13237.93 | 5.28 | 3.23 | 16 | N/A | N/A |
|  XSLT Enhanced Proxy | 512M | 100 | 1024 | 30 | 100 | 13184.03 | 5.33 | 2.69 | 16 | N/A | N/A |
|  XSLT Enhanced Proxy | 512M | 100 | 1024 | 100 | 100 | 13189.45 | 5.31 | 2.85 | 16 | N/A | N/A |
|  XSLT Enhanced Proxy | 512M | 100 | 1024 | 500 | 100 | 13123.96 | 5.36 | 2.85 | 15 | N/A | N/A |
|  XSLT Enhanced Proxy | 512M | 100 | 1024 | 1000 | 100 | 13254.62 | 5.3 | 3 | 16 | N/A | N/A |
|  XSLT Enhanced Proxy | 512M | 100 | 10240 | 0 | 100 | 13190.85 | 5.29 | 2.92 | 16 | N/A | N/A |
|  XSLT Enhanced Proxy | 512M | 100 | 10240 | 30 | 100 | 13252.42 | 5.32 | 2.89 | 16 | N/A | N/A |
|  XSLT Enhanced Proxy | 512M | 100 | 10240 | 100 | 100 | 13157.77 | 5.32 | 3.22 | 16 | N/A | N/A |
|  XSLT Enhanced Proxy | 512M | 100 | 10240 | 500 | 100 | 13183.8 | 5.32 | 2.98 | 16 | N/A | N/A |
|  XSLT Enhanced Proxy | 512M | 100 | 10240 | 1000 | 100 | 13084.16 | 5.36 | 3.12 | 16 | N/A | N/A |
|  XSLT Enhanced Proxy | 512M | 100 | 102400 | 0 | 100 | 13156.49 | 5.32 | 3.07 | 16 | N/A | N/A |
|  XSLT Enhanced Proxy | 512M | 100 | 102400 | 30 | 100 | 13170.91 | 5.32 | 2.83 | 16 | N/A | N/A |
|  XSLT Enhanced Proxy | 512M | 100 | 102400 | 100 | 100 | 13195.05 | 5.33 | 2.95 | 16 | N/A | N/A |
|  XSLT Enhanced Proxy | 512M | 100 | 102400 | 500 | 100 | 13223.1 | 5.3 | 2.85 | 15 | N/A | N/A |
|  XSLT Enhanced Proxy | 512M | 100 | 102400 | 1000 | 100 | 13053.62 | 5.36 | 3.04 | 17 | N/A | N/A |
|  XSLT Enhanced Proxy | 512M | 200 | 500 | 0 | 100 | 12794.83 | 10.57 | 7.38 | 38 | N/A | N/A |
|  XSLT Enhanced Proxy | 512M | 200 | 500 | 30 | 100 | 12909.06 | 10.56 | 6.8 | 36 | N/A | N/A |
|  XSLT Enhanced Proxy | 512M | 200 | 500 | 100 | 100 | 12890.01 | 10.68 | 7.03 | 37 | N/A | N/A |
|  XSLT Enhanced Proxy | 512M | 200 | 500 | 500 | 100 | 12627.43 | 10.78 | 9.59 | 46 | N/A | N/A |
|  XSLT Enhanced Proxy | 512M | 200 | 500 | 1000 | 100 | 12882.91 | 10.51 | 8.46 | 40 | N/A | N/A |
|  XSLT Enhanced Proxy | 512M | 200 | 1024 | 0 | 100 | 12821.13 | 10.69 | 7.04 | 37 | N/A | N/A |
|  XSLT Enhanced Proxy | 512M | 200 | 1024 | 30 | 100 | 12961.98 | 10.47 | 7.55 | 37 | N/A | N/A |
|  XSLT Enhanced Proxy | 512M | 200 | 1024 | 100 | 100 | 12947.92 | 10.47 | 7.4 | 38 | N/A | N/A |
|  XSLT Enhanced Proxy | 512M | 200 | 1024 | 500 | 100 | 12814.78 | 10.76 | 7 | 35 | N/A | N/A |
|  XSLT Enhanced Proxy | 512M | 200 | 1024 | 1000 | 100 | 12560.79 | 10.61 | 10.57 | 51 | N/A | N/A |
|  XSLT Enhanced Proxy | 512M | 200 | 10240 | 0 | 100 | 12943.48 | 10.61 | 7.79 | 39 | N/A | N/A |
|  XSLT Enhanced Proxy | 512M | 200 | 10240 | 30 | 100 | 12739.04 | 10.67 | 8.68 | 42 | N/A | N/A |
|  XSLT Enhanced Proxy | 512M | 200 | 10240 | 100 | 100 | 12813.82 | 10.76 | 7.44 | 38 | N/A | N/A |
|  XSLT Enhanced Proxy | 512M | 200 | 10240 | 500 | 100 | 12890.5 | 10.5 | 6.81 | 36 | N/A | N/A |
|  XSLT Enhanced Proxy | 512M | 200 | 10240 | 1000 | 100 | 12862.7 | 10.67 | 7.04 | 37 | N/A | N/A |
|  XSLT Enhanced Proxy | 512M | 200 | 102400 | 0 | 100 | 13073.51 | 10.46 | 7.82 | 39 | N/A | N/A |
|  XSLT Enhanced Proxy | 512M | 200 | 102400 | 30 | 100 | 12699.87 | 10.89 | 7.46 | 37 | N/A | N/A |
|  XSLT Enhanced Proxy | 512M | 200 | 102400 | 100 | 100 | 12667.03 | 10.75 | 7.88 | 39 | N/A | N/A |
|  XSLT Enhanced Proxy | 512M | 200 | 102400 | 500 | 100 | 12842.41 | 10.72 | 8.15 | 39 | N/A | N/A |
|  XSLT Enhanced Proxy | 512M | 200 | 102400 | 1000 | 100 | 12855.6 | 10.6 | 6.36 | 32 | N/A | N/A |
|  XSLT Proxy | 512M | 100 | 500 | 0 | 0 | 1628.33 | 46.31 | 64.93 | 152 | N/A | N/A |
|  XSLT Proxy | 512M | 100 | 500 | 30 | 0 | 1396.56 | 54.01 | 27.66 | 102 | N/A | N/A |
|  XSLT Proxy | 512M | 100 | 500 | 100 | 0 | 686.48 | 109.79 | 16.27 | 150 | N/A | N/A |
|  XSLT Proxy | 512M | 100 | 500 | 500 | 0 | 149.13 | 502.26 | 2.53 | 519 | N/A | N/A |
|  XSLT Proxy | 512M | 100 | 500 | 1000 | 0 | 74.1 | 1002.25 | 2.09 | 1011 | N/A | N/A |
|  XSLT Proxy | 512M | 100 | 1024 | 0 | 0 | 1549.75 | 48.69 | 31.91 | 157 | N/A | N/A |
|  XSLT Proxy | 512M | 100 | 1024 | 30 | 0 | 1374.22 | 54.88 | 22.11 | 105 | N/A | N/A |
|  XSLT Proxy | 512M | 100 | 1024 | 100 | 0 | 687.9 | 109.48 | 29.61 | 142 | N/A | N/A |
|  XSLT Proxy | 512M | 100 | 1024 | 500 | 0 | 148.76 | 502.62 | 2.88 | 519 | N/A | N/A |
|  XSLT Proxy | 512M | 100 | 1024 | 1000 | 0 | 74.08 | 1002.2 | 1.6 | 1011 | N/A | N/A |
|  XSLT Proxy | 512M | 100 | 10240 | 0 | 0 | 319.44 | 236.05 | 144.79 | 735 | N/A | N/A |
|  XSLT Proxy | 512M | 100 | 10240 | 30 | 0 | 320.05 | 236.07 | 126.77 | 671 | N/A | N/A |
|  XSLT Proxy | 512M | 100 | 10240 | 100 | 0 | 320.97 | 234.89 | 91.73 | 559 | N/A | N/A |
|  XSLT Proxy | 512M | 100 | 10240 | 500 | 0 | 143.87 | 518.78 | 21.94 | 603 | N/A | N/A |
|  XSLT Proxy | 512M | 100 | 10240 | 1000 | 0 | 73.55 | 1007.64 | 4.78 | 1031 | N/A | N/A |
|  XSLT Proxy | 512M | 100 | 102400 | 0 | 0 | 20.11 | 3687.41 | 1066.39 | 6271 | N/A | N/A |
|  XSLT Proxy | 512M | 100 | 102400 | 30 | 0 | 18.79 | 3892.12 | 1167.49 | 6591 | N/A | N/A |
|  XSLT Proxy | 512M | 100 | 102400 | 100 | 0 | 19.51 | 3821.91 | 1073.09 | 6079 | N/A | N/A |
|  XSLT Proxy | 512M | 100 | 102400 | 500 | 0 | 19.65 | 3665.13 | 1127.92 | 6207 | N/A | N/A |
|  XSLT Proxy | 512M | 100 | 102400 | 1000 | 0 | 18.74 | 3924.86 | 1314.76 | 6815 | N/A | N/A |
|  XSLT Proxy | 512M | 200 | 500 | 0 | 0 | 1498.81 | 100.34 | 100.63 | 711 | N/A | N/A |
|  XSLT Proxy | 512M | 200 | 500 | 30 | 0 | 1480.95 | 101.49 | 93.77 | 727 | N/A | N/A |
|  XSLT Proxy | 512M | 200 | 500 | 100 | 0 | 1153.68 | 130.22 | 25.06 | 213 | N/A | N/A |
|  XSLT Proxy | 512M | 200 | 500 | 500 | 0 | 294.66 | 506.59 | 14.29 | 543 | N/A | N/A |
|  XSLT Proxy | 512M | 200 | 500 | 1000 | 0 | 147.11 | 1002.73 | 3.6 | 1023 | N/A | N/A |
|  XSLT Proxy | 512M | 200 | 1024 | 0 | 0 | 1413.17 | 106.47 | 85.91 | 587 | N/A | N/A |
|  XSLT Proxy | 512M | 200 | 1024 | 30 | 0 | 1350.9 | 111.21 | 90.4 | 735 | N/A | N/A |
|  XSLT Proxy | 512M | 200 | 1024 | 100 | 0 | 1148.67 | 130.73 | 40.3 | 261 | N/A | N/A |
|  XSLT Proxy | 512M | 200 | 1024 | 500 | 0 | 290.35 | 511.12 | 20.37 | 599 | N/A | N/A |
|  XSLT Proxy | 512M | 200 | 1024 | 1000 | 0 | 147.18 | 1002.98 | 4.49 | 1031 | N/A | N/A |
|  XSLT Proxy | 512M | 200 | 10240 | 0 | 0 | 261.83 | 574.21 | 308.27 | 1423 | N/A | N/A |
|  XSLT Proxy | 512M | 200 | 10240 | 30 | 0 | 276.03 | 543.9 | 250.33 | 1335 | N/A | N/A |
|  XSLT Proxy | 512M | 200 | 10240 | 100 | 0 | 271.93 | 551.87 | 237.97 | 1167 | N/A | N/A |
|  XSLT Proxy | 512M | 200 | 10240 | 500 | 0 | 226.6 | 654.83 | 183.89 | 1319 | N/A | N/A |
|  XSLT Proxy | 512M | 200 | 10240 | 1000 | 0 | 144.73 | 1020.44 | 22.85 | 1119 | N/A | N/A |
|  XSLT Proxy | 512M | 200 | 102400 | 0 | 0 | 10.67 | 13834.21 | 5165.18 | 23295 | N/A | N/A |
|  XSLT Proxy | 512M | 200 | 102400 | 30 | 0 | 10.48 | 13748.19 | 5016.53 | 23039 | N/A | N/A |
|  XSLT Proxy | 512M | 200 | 102400 | 100 | 0 | 8.78 | 16923.36 | 6726.96 | 28159 | N/A | N/A |
|  XSLT Proxy | 512M | 200 | 102400 | 500 | 0 | 9.3 | 14685.57 | 4183.6 | 22271 | N/A | N/A |
|  XSLT Proxy | 512M | 200 | 102400 | 1000 | 0 | 9.6 | 14890.86 | 6217.69 | 25471 | N/A | N/A |
