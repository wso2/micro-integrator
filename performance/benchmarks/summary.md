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
| Heap Size | The amount of memory allocated to the application | 512M |
| Concurrent Users | The number of users accessing the application at the same time. | 100, 200 |
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
| System | Processor | Model name | Intel(R) Xeon(R) Platinum 8275CL CPU @ 3.00GHz |
| System | Memory | BIOS | 64 KiB |
| System | Memory | System memory | 3814092 KiB |
| System | Storage | Block Device: nvme0n1 | 8G |
| Operating System | Distribution | Release | Ubuntu 18.04.4 LTS |
| Operating System | Distribution | Kernel | Linux ip-10-0-1-24 5.3.0-1023-aws #25~18.04.1-Ubuntu SMP Fri Jun 5 15:18:30 UTC 2020 x86_64 x86_64 x86_64 GNU/Linux |


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
|  CBR Proxy | 512M | 100 | 500 | 0 | 0 | 1962.31 | 38.43 | 108.54 | 735 | N/A | N/A |
|  CBR Proxy | 512M | 100 | 500 | 30 | 0 | 1742.06 | 43.27 | 45.66 | 86 | N/A | N/A |
|  CBR Proxy | 512M | 100 | 500 | 100 | 0 | 715.24 | 105.3 | 15.07 | 144 | N/A | N/A |
|  CBR Proxy | 512M | 100 | 500 | 500 | 0 | 149.2 | 501.33 | 1.74 | 505 | N/A | N/A |
|  CBR Proxy | 512M | 100 | 500 | 1000 | 0 | 74.05 | 1002.05 | 0.75 | 1003 | N/A | N/A |
|  CBR Proxy | 512M | 100 | 1024 | 0 | 0 | 1921.46 | 39.21 | 86.65 | 643 | N/A | N/A |
|  CBR Proxy | 512M | 100 | 1024 | 30 | 0 | 1696.62 | 44.39 | 29.29 | 90 | N/A | N/A |
|  CBR Proxy | 512M | 100 | 1024 | 100 | 0 | 718.88 | 105.06 | 8.46 | 142 | N/A | N/A |
|  CBR Proxy | 512M | 100 | 1024 | 500 | 0 | 149.07 | 501.44 | 1.8 | 507 | N/A | N/A |
|  CBR Proxy | 512M | 100 | 1024 | 1000 | 0 | 74.12 | 1002.07 | 0.92 | 1003 | N/A | N/A |
|  CBR Proxy | 512M | 100 | 10240 | 0 | 0 | 154.53 | 108.1 | 816.72 | 237 | N/A | N/A |
|  CBR Proxy | 512M | 100 | 10240 | 30 | 0 | 689.18 | 109.42 | 44.03 | 232 | N/A | N/A |
|  CBR Proxy | 512M | 100 | 10240 | 100 | 0 | 533.64 | 141.16 | 28.68 | 237 | N/A | N/A |
|  CBR Proxy | 512M | 100 | 10240 | 500 | 0 | 147.81 | 506.15 | 6.8 | 539 | N/A | N/A |
|  CBR Proxy | 512M | 100 | 10240 | 1000 | 0 | 73.96 | 1003.82 | 5.34 | 1039 | N/A | N/A |
|  CBR Proxy | 512M | 100 | 102400 | 0 | 0 | 42.46 | 1771.23 | 829.16 | 3567 | N/A | N/A |
|  CBR Proxy | 512M | 100 | 102400 | 30 | 0 | 44.49 | 1691.04 | 731.4 | 3471 | N/A | N/A |
|  CBR Proxy | 512M | 100 | 102400 | 100 | 0 | 43 | 1747.49 | 755.33 | 3343 | N/A | N/A |
|  CBR Proxy | 512M | 100 | 102400 | 500 | 0 | 40.12 | 1853.71 | 803.99 | 3775 | N/A | N/A |
|  CBR Proxy | 512M | 100 | 102400 | 1000 | 0 | 36.69 | 2027.99 | 784.26 | 5119 | N/A | N/A |
|  CBR Proxy | 512M | 200 | 500 | 0 | 0 | 2100.39 | 71.23 | 96.11 | 683 | N/A | N/A |
|  CBR Proxy | 512M | 200 | 500 | 30 | 0 | 2220.81 | 67.59 | 74.38 | 138 | N/A | N/A |
|  CBR Proxy | 512M | 200 | 500 | 100 | 0 | 1279.78 | 117.37 | 52.32 | 170 | N/A | N/A |
|  CBR Proxy | 512M | 200 | 500 | 500 | 0 | 296.13 | 502.23 | 4.78 | 531 | N/A | N/A |
|  CBR Proxy | 512M | 200 | 500 | 1000 | 0 | 147.35 | 1002.16 | 1.59 | 1011 | N/A | N/A |
|  CBR Proxy | 512M | 200 | 1024 | 0 | 0 | 2343.69 | 64.01 | 61.9 | 167 | N/A | N/A |
|  CBR Proxy | 512M | 200 | 1024 | 30 | 0 | 2240.17 | 66.98 | 58.16 | 125 | N/A | N/A |
|  CBR Proxy | 512M | 200 | 1024 | 100 | 0 | 1330.05 | 112.91 | 14.41 | 159 | N/A | N/A |
|  CBR Proxy | 512M | 200 | 1024 | 500 | 0 | 294.91 | 505.72 | 20.7 | 559 | N/A | N/A |
|  CBR Proxy | 512M | 200 | 1024 | 1000 | 0 | 147.24 | 1002.23 | 2.19 | 1011 | N/A | N/A |
|  CBR Proxy | 512M | 200 | 10240 | 0 | 0 | 685.74 | 219.42 | 127.26 | 747 | N/A | N/A |
|  CBR Proxy | 512M | 200 | 10240 | 30 | 0 | 145 | 224.21 | 840.58 | 731 | N/A | N/A |
|  CBR Proxy | 512M | 200 | 10240 | 100 | 0 | 654.67 | 229.36 | 88.24 | 631 | N/A | N/A |
|  CBR Proxy | 512M | 200 | 10240 | 500 | 0 | 281.88 | 528.37 | 38.18 | 659 | N/A | N/A |
|  CBR Proxy | 512M | 200 | 10240 | 1000 | 0 | 144.41 | 1012.86 | 31.66 | 1135 | N/A | N/A |
|  CBR Proxy | 512M | 200 | 102400 | 0 | 0 | 19.06 | 7698.84 | 4075.9 | 16383 | N/A | N/A |
|  CBR Proxy | 512M | 200 | 102400 | 30 | 0 | 18.41 | 8197.33 | 4570.54 | 18175 | N/A | N/A |
|  CBR Proxy | 512M | 200 | 102400 | 100 | 0 | 16.6 | 9012.59 | 5317.21 | 20095 | N/A | N/A |
|  CBR Proxy | 512M | 200 | 102400 | 500 | 0 | 18.14 | 8135.84 | 3862.94 | 16639 | N/A | N/A |
|  CBR Proxy | 512M | 200 | 102400 | 1000 | 0 | 13.72 | 10919.85 | 5016.9 | 20991 | N/A | N/A |
|  CBR SOAP Header Proxy | 512M | 100 | 500 | 0 | 0 | 1703.46 | 44.24 | 114.25 | 703 | N/A | N/A |
|  CBR SOAP Header Proxy | 512M | 100 | 500 | 30 | 0 | 1744.66 | 43.23 | 41.93 | 88 | N/A | N/A |
|  CBR SOAP Header Proxy | 512M | 100 | 500 | 100 | 0 | 718.51 | 104.92 | 9.13 | 146 | N/A | N/A |
|  CBR SOAP Header Proxy | 512M | 100 | 500 | 500 | 0 | 149.05 | 501.27 | 1.51 | 505 | N/A | N/A |
|  CBR SOAP Header Proxy | 512M | 100 | 500 | 1000 | 0 | 74.1 | 1002.07 | 0.89 | 1003 | N/A | N/A |
|  CBR SOAP Header Proxy | 512M | 100 | 1024 | 0 | 0 | 1911.6 | 39.5 | 83.81 | 663 | N/A | N/A |
|  CBR SOAP Header Proxy | 512M | 100 | 1024 | 30 | 0 | 1713.71 | 44 | 33.21 | 90 | N/A | N/A |
|  CBR SOAP Header Proxy | 512M | 100 | 1024 | 100 | 0 | 718.23 | 104.89 | 9.23 | 147 | N/A | N/A |
|  CBR SOAP Header Proxy | 512M | 100 | 1024 | 500 | 0 | 149.3 | 501.39 | 1.98 | 505 | N/A | N/A |
|  CBR SOAP Header Proxy | 512M | 100 | 1024 | 1000 | 0 | 74.09 | 1002.06 | 0.91 | 1003 | N/A | N/A |
|  CBR SOAP Header Proxy | 512M | 100 | 10240 | 0 | 0 | 1014.47 | 74.34 | 49.28 | 176 | N/A | N/A |
|  CBR SOAP Header Proxy | 512M | 100 | 10240 | 30 | 0 | 945.04 | 79.73 | 38.14 | 158 | N/A | N/A |
|  CBR SOAP Header Proxy | 512M | 100 | 10240 | 100 | 0 | 596.43 | 126.31 | 22.29 | 197 | N/A | N/A |
|  CBR SOAP Header Proxy | 512M | 100 | 10240 | 500 | 0 | 148.58 | 503.76 | 4.7 | 531 | N/A | N/A |
|  CBR SOAP Header Proxy | 512M | 100 | 10240 | 1000 | 0 | 74.02 | 1002.68 | 3.74 | 1031 | N/A | N/A |
|  CBR SOAP Header Proxy | 512M | 100 | 102400 | 0 | 0 | 80.3 | 943.81 | 451.47 | 2287 | N/A | N/A |
|  CBR SOAP Header Proxy | 512M | 100 | 102400 | 30 | 0 | 81.88 | 924.41 | 429.97 | 2175 | N/A | N/A |
|  CBR SOAP Header Proxy | 512M | 100 | 102400 | 100 | 0 | 78.27 | 958.66 | 454.18 | 2399 | N/A | N/A |
|  CBR SOAP Header Proxy | 512M | 100 | 102400 | 500 | 0 | 68.24 | 1098.94 | 344.8 | 1967 | N/A | N/A |
|  CBR SOAP Header Proxy | 512M | 100 | 102400 | 1000 | 0 | 51.05 | 1471.63 | 429.41 | 2383 | N/A | N/A |
|  CBR SOAP Header Proxy | 512M | 200 | 500 | 0 | 0 | 1231.63 | 122.02 | 219.14 | 759 | N/A | N/A |
|  CBR SOAP Header Proxy | 512M | 200 | 500 | 30 | 0 | 1791.72 | 84.09 | 143.63 | 759 | N/A | N/A |
|  CBR SOAP Header Proxy | 512M | 200 | 500 | 100 | 0 | 1268.24 | 118.36 | 19.92 | 176 | N/A | N/A |
|  CBR SOAP Header Proxy | 512M | 200 | 500 | 500 | 0 | 297.07 | 501.87 | 3.43 | 527 | N/A | N/A |
|  CBR SOAP Header Proxy | 512M | 200 | 500 | 1000 | 0 | 146.94 | 1002.28 | 2.33 | 1011 | N/A | N/A |
|  CBR SOAP Header Proxy | 512M | 200 | 1024 | 0 | 0 | 2076.77 | 72.05 | 96.51 | 691 | N/A | N/A |
|  CBR SOAP Header Proxy | 512M | 200 | 1024 | 30 | 0 | 1978.11 | 75.89 | 103.21 | 671 | N/A | N/A |
|  CBR SOAP Header Proxy | 512M | 200 | 1024 | 100 | 0 | 1257.79 | 119.41 | 52.55 | 179 | N/A | N/A |
|  CBR SOAP Header Proxy | 512M | 200 | 1024 | 500 | 0 | 297.12 | 502.35 | 4.89 | 535 | N/A | N/A |
|  CBR SOAP Header Proxy | 512M | 200 | 1024 | 1000 | 0 | 147.22 | 1002.26 | 2.28 | 1015 | N/A | N/A |
|  CBR SOAP Header Proxy | 512M | 200 | 10240 | 0 | 0 | 948.44 | 158.61 | 100.3 | 783 | N/A | N/A |
|  CBR SOAP Header Proxy | 512M | 200 | 10240 | 30 | 0 | 1034.6 | 145.32 | 61.74 | 307 | N/A | N/A |
|  CBR SOAP Header Proxy | 512M | 200 | 10240 | 100 | 0 | 874.01 | 171.81 | 69.72 | 317 | N/A | N/A |
|  CBR SOAP Header Proxy | 512M | 200 | 10240 | 500 | 0 | 285.67 | 523.35 | 33.75 | 635 | N/A | N/A |
|  CBR SOAP Header Proxy | 512M | 200 | 10240 | 1000 | 0 | 146.96 | 1004.09 | 6.5 | 1047 | N/A | N/A |
|  CBR SOAP Header Proxy | 512M | 200 | 102400 | 0 | 14.58 | 7.67 | 21306.96 | 41719.78 | 127999 | N/A | N/A |
|  CBR SOAP Header Proxy | 512M | 200 | 102400 | 30 | 100 | 0.66 | 120064 | 0 | 120319 | N/A | N/A |
|  CBR SOAP Header Proxy | 512M | 200 | 102400 | 100 | 100 | 0.66 | 120064 | 0 | 120319 | N/A | N/A |
|  CBR SOAP Header Proxy | 512M | 200 | 102400 | 500 | 100 | 0.66 | 120064 | 0 | 120319 | N/A | N/A |
|  CBR SOAP Header Proxy | 512M | 200 | 102400 | 1000 | 100 | 0.66 | 120064 | 0 | 120319 | N/A | N/A |
|  CBR Transport Header Proxy | 512M | 100 | 500 | 0 | 100 | 0.33 | 120064 | 0 | 120319 | N/A | N/A |
|  CBR Transport Header Proxy | 512M | 100 | 500 | 30 | 100 | 0.33 | 120064 | 0 | 120319 | N/A | N/A |
|  CBR Transport Header Proxy | 512M | 100 | 500 | 100 | 100 | 0.33 | 120064 | 0 | 120319 | N/A | N/A |
|  CBR Transport Header Proxy | 512M | 100 | 500 | 500 | 100 | 0.33 | 120064 | 0 | 120319 | N/A | N/A |
|  CBR Transport Header Proxy | 512M | 100 | 500 | 1000 | 100 | 0.33 | 120064 | 0 | 120319 | N/A | N/A |
|  CBR Transport Header Proxy | 512M | 100 | 1024 | 0 | 100 | 0.33 | 120064 | 0 | 120319 | N/A | N/A |
|  CBR Transport Header Proxy | 512M | 100 | 1024 | 30 | 100 | 0.33 | 120064 | 0 | 120319 | N/A | N/A |
|  CBR Transport Header Proxy | 512M | 100 | 1024 | 100 | 100 | 0.33 | 120064 | 0 | 120319 | N/A | N/A |
|  CBR Transport Header Proxy | 512M | 100 | 1024 | 500 | 100 | 0.33 | 120064 | 0 | 120319 | N/A | N/A |
|  CBR Transport Header Proxy | 512M | 100 | 1024 | 1000 | 100 | 0.33 | 120064 | 0 | 120319 | N/A | N/A |
|  CBR Transport Header Proxy | 512M | 100 | 10240 | 0 | 100 | 0.33 | 120064 | 0 | 120319 | N/A | N/A |
|  CBR Transport Header Proxy | 512M | 100 | 10240 | 30 | 100 | 0.33 | 120064 | 0 | 120319 | N/A | N/A |
|  CBR Transport Header Proxy | 512M | 100 | 10240 | 100 | 100 | 0.33 | 120064 | 0 | 120319 | N/A | N/A |
|  CBR Transport Header Proxy | 512M | 100 | 10240 | 500 | 100 | 0.33 | 120064 | 0 | 120319 | N/A | N/A |
|  CBR Transport Header Proxy | 512M | 100 | 10240 | 1000 | 100 | 0.33 | 120064 | 0 | 120319 | N/A | N/A |
|  CBR Transport Header Proxy | 512M | 100 | 102400 | 0 | 100 | 0.33 | 120064 | 0 | 120319 | N/A | N/A |
|  CBR Transport Header Proxy | 512M | 100 | 102400 | 30 | 100 | 0.33 | 120064 | 0 | 120319 | N/A | N/A |
|  CBR Transport Header Proxy | 512M | 100 | 102400 | 100 | 100 | 0.33 | 120064 | 0 | 120319 | N/A | N/A |
|  CBR Transport Header Proxy | 512M | 100 | 102400 | 500 | 100 | 0.33 | 120064 | 0 | 120319 | N/A | N/A |
|  CBR Transport Header Proxy | 512M | 100 | 102400 | 1000 | 100 | 0.33 | 120064 | 0 | 120319 | N/A | N/A |
|  CBR Transport Header Proxy | 512M | 200 | 500 | 0 | 100 | 0.66 | 120064 | 0 | 120319 | N/A | N/A |
|  CBR Transport Header Proxy | 512M | 200 | 500 | 30 | 100 | 0.66 | 120064 | 0 | 120319 | N/A | N/A |
|  CBR Transport Header Proxy | 512M | 200 | 500 | 100 | 100 | 0.66 | 120064 | 0 | 120319 | N/A | N/A |
|  CBR Transport Header Proxy | 512M | 200 | 500 | 500 | 100 | 0.66 | 120064 | 0 | 120319 | N/A | N/A |
|  CBR Transport Header Proxy | 512M | 200 | 500 | 1000 | 100 | 0.66 | 120064 | 0 | 120319 | N/A | N/A |
|  CBR Transport Header Proxy | 512M | 200 | 1024 | 0 | 100 | 0.66 | 120064 | 0 | 120319 | N/A | N/A |
|  CBR Transport Header Proxy | 512M | 200 | 1024 | 30 | 100 | 0.66 | 120064 | 0 | 120319 | N/A | N/A |
|  CBR Transport Header Proxy | 512M | 200 | 1024 | 100 | 100 | 0.66 | 120064 | 0 | 120319 | N/A | N/A |
|  CBR Transport Header Proxy | 512M | 200 | 1024 | 500 | 100 | 0.66 | 120064 | 0 | 120319 | N/A | N/A |
|  CBR Transport Header Proxy | 512M | 200 | 1024 | 1000 | 100 | 0.66 | 120064 | 0 | 120319 | N/A | N/A |
|  CBR Transport Header Proxy | 512M | 200 | 10240 | 0 | 100 | 0.66 | 120064 | 0 | 120319 | N/A | N/A |
|  CBR Transport Header Proxy | 512M | 200 | 10240 | 30 | 100 | 0.66 | 120064 | 0 | 120319 | N/A | N/A |
|  CBR Transport Header Proxy | 512M | 200 | 10240 | 100 | 100 | 0.66 | 120064 | 0 | 120319 | N/A | N/A |
|  CBR Transport Header Proxy | 512M | 200 | 10240 | 500 | 100 | 0.66 | 120064 | 0 | 120319 | N/A | N/A |
|  CBR Transport Header Proxy | 512M | 200 | 10240 | 1000 | 100 | 0.66 | 120064 | 0 | 120319 | N/A | N/A |
|  CBR Transport Header Proxy | 512M | 200 | 102400 | 0 | 100 | 0.66 | 120064 | 0 | 120319 | N/A | N/A |
|  CBR Transport Header Proxy | 512M | 200 | 102400 | 30 | 100 | 0.66 | 120064 | 0 | 120319 | N/A | N/A |
|  CBR Transport Header Proxy | 512M | 200 | 102400 | 100 | 100 | 0.66 | 120064 | 0 | 120319 | N/A | N/A |
|  CBR Transport Header Proxy | 512M | 200 | 102400 | 500 | 100 | 0.66 | 120064 | 0 | 120319 | N/A | N/A |
|  CBR Transport Header Proxy | 512M | 200 | 102400 | 1000 | 100 | 0.66 | 120064 | 0 | 120319 | N/A | N/A |
|  Direct Proxy | 512M | 100 | 500 | 0 | 0 | 2278.65 | 33.06 | 75.52 | 151 | 76.04 | 275.753 |
|  Direct Proxy | 512M | 100 | 500 | 30 | 0 | 1846.11 | 40.8 | 44.93 | 86 | N/A | N/A |
|  Direct Proxy | 512M | 100 | 500 | 100 | 0 | 719.19 | 104.77 | 25.25 | 146 | N/A | N/A |
|  Direct Proxy | 512M | 100 | 500 | 500 | 0 | 149.02 | 501.19 | 1.34 | 503 | N/A | N/A |
|  Direct Proxy | 512M | 100 | 500 | 1000 | 0 | 74.2 | 1002.05 | 0.61 | 1003 | N/A | N/A |
|  Direct Proxy | 512M | 100 | 1024 | 0 | 0 | 1884.66 | 40 | 129.48 | 751 | N/A | N/A |
|  Direct Proxy | 512M | 100 | 1024 | 30 | 0 | 1794.83 | 42.03 | 51.71 | 92 | N/A | N/A |
|  Direct Proxy | 512M | 100 | 1024 | 100 | 0 | 719.6 | 104.8 | 22.55 | 143 | N/A | N/A |
|  Direct Proxy | 512M | 100 | 1024 | 500 | 0 | 149.3 | 501.16 | 1.21 | 503 | N/A | N/A |
|  Direct Proxy | 512M | 100 | 1024 | 1000 | 0 | 74.11 | 1002.01 | 0.37 | 1003 | N/A | N/A |
|  Direct Proxy | 512M | 100 | 10240 | 0 | 0 | 2090.07 | 36.03 | 77.8 | 627 | N/A | N/A |
|  Direct Proxy | 512M | 100 | 10240 | 30 | 0 | 1697.16 | 44.35 | 27.45 | 97 | N/A | N/A |
|  Direct Proxy | 512M | 100 | 10240 | 100 | 0 | 715.59 | 105.25 | 19.39 | 145 | N/A | N/A |
|  Direct Proxy | 512M | 100 | 10240 | 500 | 0 | 149.21 | 501.46 | 1.58 | 503 | N/A | N/A |
|  Direct Proxy | 512M | 100 | 10240 | 1000 | 0 | 74.09 | 1002.18 | 4.51 | 1007 | N/A | N/A |
|  Direct Proxy | 512M | 100 | 102400 | 0 | 0 | 1031.41 | 72.88 | 54.47 | 182 | N/A | N/A |
|  Direct Proxy | 512M | 100 | 102400 | 30 | 0 | 988.31 | 76.01 | 34.87 | 156 | N/A | N/A |
|  Direct Proxy | 512M | 100 | 102400 | 100 | 0 | 627.14 | 119.83 | 17.03 | 176 | N/A | N/A |
|  Direct Proxy | 512M | 100 | 102400 | 500 | 0 | 148.39 | 503.81 | 2.49 | 515 | N/A | N/A |
|  Direct Proxy | 512M | 100 | 102400 | 1000 | 0 | 73.97 | 1002.23 | 1.29 | 1007 | N/A | N/A |
|  Direct Proxy | 512M | 200 | 500 | 0 | 0 | 1891.73 | 79.64 | 214.83 | 1391 | N/A | N/A |
|  Direct Proxy | 512M | 200 | 500 | 30 | 0 | 2256.67 | 65.44 | 121.04 | 751 | N/A | N/A |
|  Direct Proxy | 512M | 200 | 500 | 100 | 0 | 1361.44 | 110.24 | 13.58 | 158 | N/A | N/A |
|  Direct Proxy | 512M | 200 | 500 | 500 | 0 | 296.7 | 501.54 | 2.89 | 519 | N/A | N/A |
|  Direct Proxy | 512M | 200 | 500 | 1000 | 0 | 147.35 | 1002.18 | 1.95 | 1007 | N/A | N/A |
|  Direct Proxy | 512M | 200 | 1024 | 0 | 0 | 1523.73 | 98.57 | 214.53 | 1295 | N/A | N/A |
|  Direct Proxy | 512M | 200 | 1024 | 30 | 0 | 1757.53 | 85.48 | 196.55 | 1359 | N/A | N/A |
|  Direct Proxy | 512M | 200 | 1024 | 100 | 0 | 1279.08 | 117.26 | 61.56 | 170 | N/A | N/A |
|  Direct Proxy | 512M | 200 | 1024 | 500 | 0 | 297.1 | 501.44 | 2.58 | 515 | N/A | N/A |
|  Direct Proxy | 512M | 200 | 1024 | 1000 | 0 | 147.17 | 1002.1 | 1.18 | 1003 | N/A | N/A |
|  Direct Proxy | 512M | 200 | 10240 | 0 | 0 | 2317.44 | 64.85 | 83.8 | 679 | N/A | N/A |
|  Direct Proxy | 512M | 200 | 10240 | 30 | 0 | 2321.87 | 64.66 | 54.03 | 130 | N/A | N/A |
|  Direct Proxy | 512M | 200 | 10240 | 100 | 0 | 1269.87 | 118.16 | 39.5 | 177 | N/A | N/A |
|  Direct Proxy | 512M | 200 | 10240 | 500 | 0 | 296.78 | 501.93 | 3.45 | 519 | N/A | N/A |
|  Direct Proxy | 512M | 200 | 10240 | 1000 | 0 | 147.32 | 1002.16 | 1.66 | 1007 | N/A | N/A |
|  Direct Proxy | 512M | 200 | 102400 | 0 | 0 | 1024.78 | 146.34 | 76.92 | 361 | N/A | N/A |
|  Direct Proxy | 512M | 200 | 102400 | 30 | 0 | 1028.07 | 145.84 | 61.23 | 315 | N/A | N/A |
|  Direct Proxy | 512M | 200 | 102400 | 100 | 0 | 880.5 | 170.23 | 52.14 | 323 | N/A | N/A |
|  Direct Proxy | 512M | 200 | 102400 | 500 | 0 | 293.4 | 507.72 | 9.46 | 555 | N/A | N/A |
|  Direct Proxy | 512M | 200 | 102400 | 1000 | 0 | 146.95 | 1002.86 | 3.79 | 1011 | N/A | N/A |
|  XSLT Enhanced Proxy | 512M | 100 | 500 | 0 | 100 | 0.33 | 120064 | 0 | 120319 | N/A | N/A |
|  XSLT Enhanced Proxy | 512M | 100 | 500 | 30 | 100 | 0.33 | 120064 | 0 | 120319 | N/A | N/A |
|  XSLT Enhanced Proxy | 512M | 100 | 500 | 100 | 100 | 0.33 | 120064 | 0 | 120319 | N/A | N/A |
|  XSLT Enhanced Proxy | 512M | 100 | 500 | 500 | 100 | 0.33 | 120064 | 0 | 120319 | N/A | N/A |
|  XSLT Enhanced Proxy | 512M | 100 | 500 | 1000 | 100 | 0.33 | 120064 | 0 | 120319 | N/A | N/A |
|  XSLT Enhanced Proxy | 512M | 100 | 1024 | 0 | 100 | 0.33 | 120064 | 0 | 120319 | N/A | N/A |
|  XSLT Enhanced Proxy | 512M | 100 | 1024 | 30 | 100 | 0.33 | 120064 | 0 | 120319 | N/A | N/A |
|  XSLT Enhanced Proxy | 512M | 100 | 1024 | 100 | 100 | 0.33 | 120064 | 0 | 120319 | N/A | N/A |
|  XSLT Enhanced Proxy | 512M | 100 | 1024 | 500 | 100 | 0.33 | 120064 | 0 | 120319 | N/A | N/A |
|  XSLT Enhanced Proxy | 512M | 100 | 1024 | 1000 | 100 | 0.33 | 120064 | 0 | 120319 | N/A | N/A |
|  XSLT Enhanced Proxy | 512M | 100 | 10240 | 0 | 100 | 0.33 | 120064 | 0 | 120319 | N/A | N/A |
|  XSLT Enhanced Proxy | 512M | 100 | 10240 | 30 | 100 | 0.33 | 120064 | 0 | 120319 | N/A | N/A |
|  XSLT Enhanced Proxy | 512M | 100 | 10240 | 100 | 100 | 0.33 | 120064 | 0 | 120319 | N/A | N/A |
|  XSLT Enhanced Proxy | 512M | 100 | 10240 | 500 | 100 | 0.33 | 120064 | 0 | 120319 | N/A | N/A |
|  XSLT Enhanced Proxy | 512M | 100 | 10240 | 1000 | 100 | 0.33 | 120064 | 0 | 120319 | N/A | N/A |
|  XSLT Enhanced Proxy | 512M | 100 | 102400 | 0 | 100 | 0.33 | 120064 | 0 | 120319 | N/A | N/A |
|  XSLT Enhanced Proxy | 512M | 100 | 102400 | 30 | 100 | 0.33 | 120064 | 0 | 120319 | N/A | N/A |
|  XSLT Enhanced Proxy | 512M | 100 | 102400 | 100 | 100 | 0.33 | 120064 | 0 | 120319 | N/A | N/A |
|  XSLT Enhanced Proxy | 512M | 100 | 102400 | 500 | 100 | 0.33 | 120064 | 0 | 120319 | N/A | N/A |
|  XSLT Enhanced Proxy | 512M | 100 | 102400 | 1000 | 100 | 0.33 | 120064 | 0 | 120319 | N/A | N/A |
|  XSLT Enhanced Proxy | 512M | 200 | 500 | 0 | 100 | 0.66 | 120064 | 0 | 120319 | N/A | N/A |
|  XSLT Enhanced Proxy | 512M | 200 | 500 | 30 | 100 | 0.66 | 120064 | 0 | 120319 | N/A | N/A |
|  XSLT Enhanced Proxy | 512M | 200 | 500 | 100 | 100 | 0.66 | 120064 | 0 | 120319 | N/A | N/A |
|  XSLT Enhanced Proxy | 512M | 200 | 500 | 500 | 100 | 0.66 | 120064 | 0 | 120319 | N/A | N/A |
|  XSLT Enhanced Proxy | 512M | 200 | 500 | 1000 | 100 | 0.66 | 120064 | 0 | 120319 | N/A | N/A |
|  XSLT Enhanced Proxy | 512M | 200 | 1024 | 0 | 100 | 0.66 | 120064 | 0 | 120319 | N/A | N/A |
|  XSLT Enhanced Proxy | 512M | 200 | 1024 | 30 | 100 | 0.66 | 120064 | 0 | 120319 | N/A | N/A |
|  XSLT Enhanced Proxy | 512M | 200 | 1024 | 100 | 100 | 0.66 | 120064 | 0 | 120319 | N/A | N/A |
|  XSLT Enhanced Proxy | 512M | 200 | 1024 | 500 | 100 | 0.66 | 120064 | 0 | 120319 | N/A | N/A |
|  XSLT Enhanced Proxy | 512M | 200 | 1024 | 1000 | 100 | 0.66 | 120064 | 0 | 120319 | N/A | N/A |
|  XSLT Enhanced Proxy | 512M | 200 | 10240 | 0 | 100 | 0.66 | 120064 | 0 | 120319 | N/A | N/A |
|  XSLT Enhanced Proxy | 512M | 200 | 10240 | 30 | 100 | 0.66 | 120064 | 0 | 120319 | N/A | N/A |
|  XSLT Enhanced Proxy | 512M | 200 | 10240 | 100 | 100 | 0.66 | 120064 | 0 | 120319 | N/A | N/A |
|  XSLT Enhanced Proxy | 512M | 200 | 10240 | 500 | 100 | 0.66 | 120064 | 0 | 120319 | N/A | N/A |
|  XSLT Enhanced Proxy | 512M | 200 | 10240 | 1000 | 100 | 0.66 | 120384.65 | 474.9 | 121343 | N/A | N/A |
|  XSLT Enhanced Proxy | 512M | 200 | 102400 | 0 | 100 | 0.66 | 120777.7 | 470.6 | 121343 | N/A | N/A |
|  XSLT Enhanced Proxy | 512M | 200 | 102400 | 30 | 100 | 0.66 | 120777.7 | 470.6 | 121343 | N/A | N/A |
|  XSLT Enhanced Proxy | 512M | 200 | 102400 | 100 | 100 | 0.66 | 120777.7 | 470.6 | 121343 | N/A | N/A |
|  XSLT Enhanced Proxy | 512M | 200 | 102400 | 500 | 100 | 0.66 | 120777.7 | 470.6 | 121343 | N/A | N/A |
|  XSLT Enhanced Proxy | 512M | 200 | 102400 | 1000 | 100 | 0.66 | 120777.7 | 470.6 | 121343 | N/A | N/A |
|  XSLT Proxy | 512M | 100 | 500 | 0 | 0 | 1730.54 | 43.57 | 33.96 | 133 | N/A | N/A |
|  XSLT Proxy | 512M | 100 | 500 | 30 | 0 | 1374.25 | 55 | 40.34 | 104 | N/A | N/A |
|  XSLT Proxy | 512M | 100 | 500 | 100 | 0 | 679.58 | 110.9 | 11.31 | 149 | N/A | N/A |
|  XSLT Proxy | 512M | 100 | 500 | 500 | 0 | 148.86 | 502.08 | 2.67 | 515 | N/A | N/A |
|  XSLT Proxy | 512M | 100 | 500 | 1000 | 0 | 74.08 | 1002.16 | 1.51 | 1007 | N/A | N/A |
|  XSLT Proxy | 512M | 100 | 1024 | 0 | 0 | 1397.35 | 54.02 | 45.03 | 164 | N/A | N/A |
|  XSLT Proxy | 512M | 100 | 1024 | 30 | 0 | 1267.37 | 59.53 | 18.86 | 117 | N/A | N/A |
|  XSLT Proxy | 512M | 100 | 1024 | 100 | 0 | 669.17 | 112.82 | 12.75 | 158 | N/A | N/A |
|  XSLT Proxy | 512M | 100 | 1024 | 500 | 0 | 148.95 | 502.72 | 2.61 | 515 | N/A | N/A |
|  XSLT Proxy | 512M | 100 | 1024 | 1000 | 0 | 74.04 | 1002.48 | 2.8 | 1023 | N/A | N/A |
|  XSLT Proxy | 512M | 100 | 10240 | 0 | 0 | 315.53 | 238.9 | 131 | 639 | N/A | N/A |
|  XSLT Proxy | 512M | 100 | 10240 | 30 | 0 | 321.34 | 234.68 | 127.49 | 691 | N/A | N/A |
|  XSLT Proxy | 512M | 100 | 10240 | 100 | 0 | 318.9 | 236.67 | 96.03 | 599 | N/A | N/A |
|  XSLT Proxy | 512M | 100 | 10240 | 500 | 0 | 143.95 | 519.18 | 26.75 | 635 | N/A | N/A |
|  XSLT Proxy | 512M | 100 | 10240 | 1000 | 0 | 73.4 | 1008.31 | 6.99 | 1047 | N/A | N/A |
|  XSLT Proxy | 512M | 100 | 102400 | 0 | 0 | 19.95 | 3689.12 | 1164.24 | 6655 | N/A | N/A |
|  XSLT Proxy | 512M | 100 | 102400 | 30 | 0 | 19.56 | 3751.58 | 1066.44 | 6175 | N/A | N/A |
|  XSLT Proxy | 512M | 100 | 102400 | 100 | 0 | 17.96 | 4233.36 | 1706.43 | 7743 | N/A | N/A |
|  XSLT Proxy | 512M | 100 | 102400 | 500 | 0 | 19.91 | 3676.61 | 897.08 | 6143 | N/A | N/A |
|  XSLT Proxy | 512M | 100 | 102400 | 1000 | 0 | 18.45 | 3917.03 | 1036.02 | 6047 | N/A | N/A |
|  XSLT Proxy | 512M | 200 | 500 | 0 | 0 | 1672.63 | 89.84 | 81.62 | 259 | N/A | N/A |
|  XSLT Proxy | 512M | 200 | 500 | 30 | 0 | 1740.39 | 86.39 | 66.19 | 252 | N/A | N/A |
|  XSLT Proxy | 512M | 200 | 500 | 100 | 0 | 1232.3 | 121.67 | 39.51 | 183 | N/A | N/A |
|  XSLT Proxy | 512M | 200 | 500 | 500 | 0 | 295.58 | 503.67 | 6.03 | 539 | N/A | N/A |
|  XSLT Proxy | 512M | 200 | 500 | 1000 | 0 | 147.16 | 1003.59 | 9.91 | 1031 | N/A | N/A |
|  XSLT Proxy | 512M | 200 | 1024 | 0 | 0 | 1419.81 | 105.9 | 100.09 | 723 | N/A | N/A |
|  XSLT Proxy | 512M | 200 | 1024 | 30 | 0 | 1424.78 | 105.53 | 81.15 | 603 | N/A | N/A |
|  XSLT Proxy | 512M | 200 | 1024 | 100 | 0 | 1121.19 | 134.06 | 35.94 | 208 | N/A | N/A |
|  XSLT Proxy | 512M | 200 | 1024 | 500 | 0 | 294.9 | 504.88 | 6.84 | 539 | N/A | N/A |
|  XSLT Proxy | 512M | 200 | 1024 | 1000 | 0 | 147.21 | 1002.83 | 3.95 | 1031 | N/A | N/A |
|  XSLT Proxy | 512M | 200 | 10240 | 0 | 0 | 275.87 | 542.68 | 267.9 | 1343 | N/A | N/A |
|  XSLT Proxy | 512M | 200 | 10240 | 30 | 0 | 275.66 | 543.56 | 254.94 | 1279 | N/A | N/A |
|  XSLT Proxy | 512M | 200 | 10240 | 100 | 0 | 278.81 | 538.58 | 234.85 | 1199 | N/A | N/A |
|  XSLT Proxy | 512M | 200 | 10240 | 500 | 0 | 242.13 | 615.68 | 130.5 | 1103 | N/A | N/A |
|  XSLT Proxy | 512M | 200 | 10240 | 1000 | 0 | 143.73 | 1026.3 | 28.18 | 1135 | N/A | N/A |
|  XSLT Proxy | 512M | 200 | 102400 | 0 | 0 | 9 | 15874 | 6840.98 | 26751 | N/A | N/A |
|  XSLT Proxy | 512M | 200 | 102400 | 30 | 0 | 9.91 | 14688.84 | 4971.53 | 24447 | N/A | N/A |
|  XSLT Proxy | 512M | 200 | 102400 | 100 | 0 | 10.45 | 13424.77 | 5107.9 | 21759 | N/A | N/A |
|  XSLT Proxy | 512M | 200 | 102400 | 500 | 0 | 10.23 | 13868.57 | 4810.65 | 22527 | N/A | N/A |
|  XSLT Proxy | 512M | 200 | 102400 | 1000 | 0 | 7.98 | 17992.34 | 5947.19 | 28799 | N/A | N/A |
