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
| Operating System | Distribution | Kernel | Linux ip-10-0-1-132 5.3.0-1023-aws #25~18.04.1-Ubuntu SMP Fri Jun 5 15:18:30 UTC 2020 x86_64 x86_64 x86_64 GNU/Linux |


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
|  CBR Proxy | 512M | 100 | 500 | 0 | 0 | 1793.71 | 42 | 103.36 | 735 | N/A | N/A |
|  CBR Proxy | 512M | 100 | 500 | 30 | 0 | 1761.49 | 42.81 | 28.3 | 89 | N/A | N/A |
|  CBR Proxy | 512M | 100 | 500 | 100 | 0 | 720.03 | 104.7 | 9.09 | 146 | N/A | N/A |
|  CBR Proxy | 512M | 100 | 500 | 500 | 0 | 149.22 | 501.42 | 1.7 | 505 | N/A | N/A |
|  CBR Proxy | 512M | 100 | 500 | 1000 | 0 | 74.12 | 1002.06 | 0.81 | 1003 | N/A | N/A |
|  CBR Proxy | 512M | 100 | 1024 | 0 | 0 | 2032.64 | 37.24 | 68.93 | 155 | N/A | N/A |
|  CBR Proxy | 512M | 100 | 1024 | 30 | 0 | 1698.04 | 44.37 | 33.34 | 89 | N/A | N/A |
|  CBR Proxy | 512M | 100 | 1024 | 100 | 0 | 718.72 | 105.02 | 8.66 | 145 | N/A | N/A |
|  CBR Proxy | 512M | 100 | 1024 | 500 | 0 | 149.15 | 501.72 | 2.82 | 519 | N/A | N/A |
|  CBR Proxy | 512M | 100 | 1024 | 1000 | 0 | 74.09 | 1002.1 | 0.97 | 1007 | N/A | N/A |
|  CBR Proxy | 512M | 100 | 10240 | 0 | 0 | 725.7 | 104.06 | 50.59 | 241 | N/A | N/A |
|  CBR Proxy | 512M | 100 | 10240 | 30 | 0 | 705.65 | 107.24 | 38.04 | 211 | N/A | N/A |
|  CBR Proxy | 512M | 100 | 10240 | 100 | 0 | 522.38 | 144.59 | 39.41 | 255 | N/A | N/A |
|  CBR Proxy | 512M | 100 | 10240 | 500 | 0 | 147.32 | 508.04 | 10.29 | 551 | N/A | N/A |
|  CBR Proxy | 512M | 100 | 10240 | 1000 | 0 | 73.99 | 1003.87 | 5.58 | 1039 | N/A | N/A |
|  CBR Proxy | 512M | 100 | 102400 | 0 | 0 | 42.94 | 1771.48 | 742.4 | 3519 | N/A | N/A |
|  CBR Proxy | 512M | 100 | 102400 | 30 | 0 | 41.97 | 1787.95 | 737.23 | 3375 | N/A | N/A |
|  CBR Proxy | 512M | 100 | 102400 | 100 | 0 | 43.63 | 1719.11 | 731.58 | 3487 | N/A | N/A |
|  CBR Proxy | 512M | 100 | 102400 | 500 | 0 | 39.82 | 1904.02 | 818.44 | 3791 | N/A | N/A |
|  CBR Proxy | 512M | 100 | 102400 | 1000 | 0 | 33.56 | 2222.36 | 1036.17 | 5087 | N/A | N/A |
|  CBR Proxy | 512M | 200 | 500 | 0 | 0 | 1346.8 | 111.75 | 220.34 | 1191 | N/A | N/A |
|  CBR Proxy | 512M | 200 | 500 | 30 | 0 | 1790.98 | 84.13 | 141.29 | 759 | N/A | N/A |
|  CBR Proxy | 512M | 200 | 500 | 100 | 0 | 1258.02 | 119.36 | 19.4 | 172 | N/A | N/A |
|  CBR Proxy | 512M | 200 | 500 | 500 | 0 | 296.18 | 501.85 | 3.37 | 523 | N/A | N/A |
|  CBR Proxy | 512M | 200 | 500 | 1000 | 0 | 147.43 | 1002.34 | 2.77 | 1019 | N/A | N/A |
|  CBR Proxy | 512M | 200 | 1024 | 0 | 0 | 1531.39 | 98.23 | 156.9 | 747 | N/A | N/A |
|  CBR Proxy | 512M | 200 | 1024 | 30 | 0 | 2054.78 | 73.14 | 70.36 | 161 | N/A | N/A |
|  CBR Proxy | 512M | 200 | 1024 | 100 | 0 | 1258.41 | 119.01 | 52.46 | 178 | N/A | N/A |
|  CBR Proxy | 512M | 200 | 1024 | 500 | 0 | 296.34 | 502.61 | 5.61 | 539 | N/A | N/A |
|  CBR Proxy | 512M | 200 | 1024 | 1000 | 0 | 147.13 | 1002.25 | 2.08 | 1015 | N/A | N/A |
|  CBR Proxy | 512M | 200 | 10240 | 0 | 0 | 678.27 | 221.83 | 129.62 | 815 | N/A | N/A |
|  CBR Proxy | 512M | 200 | 10240 | 30 | 0 | 709.6 | 212.2 | 100.25 | 639 | N/A | N/A |
|  CBR Proxy | 512M | 200 | 10240 | 100 | 0 | 659.46 | 226.63 | 91.43 | 619 | N/A | N/A |
|  CBR Proxy | 512M | 200 | 10240 | 500 | 0 | 277.1 | 534.85 | 48.71 | 723 | N/A | N/A |
|  CBR Proxy | 512M | 200 | 10240 | 1000 | 0 | 145.33 | 1013.92 | 35.76 | 1183 | N/A | N/A |
|  CBR Proxy | 512M | 200 | 102400 | 0 | 0 | 17.58 | 8491.99 | 4350.35 | 18943 | N/A | N/A |
|  CBR Proxy | 512M | 200 | 102400 | 30 | 0 | 18.75 | 8024.97 | 3918.4 | 16767 | N/A | N/A |
|  CBR Proxy | 512M | 200 | 102400 | 100 | 0 | 18.27 | 8255.27 | 4190.04 | 17407 | N/A | N/A |
|  CBR Proxy | 512M | 200 | 102400 | 500 | 0 | 14.52 | 10006.14 | 4507.86 | 19199 | N/A | N/A |
|  CBR Proxy | 512M | 200 | 102400 | 1000 | 37.02 | 1.46 | 62783.38 | 71571.41 | 307199 | N/A | N/A |
|  CBR SOAP Header Proxy | 512M | 100 | 500 | 0 | 100 | 0.33 | 120064 | 0 | 120319 | N/A | N/A |
|  CBR SOAP Header Proxy | 512M | 100 | 500 | 30 | 100 | 0.33 | 120064 | 0 | 120319 | N/A | N/A |
|  CBR SOAP Header Proxy | 512M | 100 | 500 | 100 | 100 | 0.33 | 120064 | 0 | 120319 | N/A | N/A |
|  CBR SOAP Header Proxy | 512M | 100 | 500 | 500 | 100 | 0.33 | 120064 | 0 | 120319 | N/A | N/A |
|  CBR SOAP Header Proxy | 512M | 100 | 500 | 1000 | 100 | 0.33 | 120064 | 0 | 120319 | N/A | N/A |
|  CBR SOAP Header Proxy | 512M | 100 | 1024 | 0 | 100 | 0.33 | 120064 | 0 | 120319 | N/A | N/A |
|  CBR SOAP Header Proxy | 512M | 100 | 1024 | 30 | 100 | 0.33 | 120064 | 0 | 120319 | N/A | N/A |
|  CBR SOAP Header Proxy | 512M | 100 | 1024 | 100 | 100 | 0.33 | 120064 | 0 | 120319 | N/A | N/A |
|  CBR SOAP Header Proxy | 512M | 100 | 1024 | 500 | 100 | 0.33 | 120064 | 0 | 120319 | N/A | N/A |
|  CBR SOAP Header Proxy | 512M | 100 | 1024 | 1000 | 100 | 0.33 | 120064 | 0 | 120319 | N/A | N/A |
|  CBR SOAP Header Proxy | 512M | 100 | 10240 | 0 | 100 | 0.33 | 120064 | 0 | 120319 | N/A | N/A |
|  CBR SOAP Header Proxy | 512M | 100 | 10240 | 30 | 100 | 0.33 | 120064 | 0 | 120319 | N/A | N/A |
|  CBR SOAP Header Proxy | 512M | 100 | 10240 | 100 | 100 | 0.33 | 120064 | 0 | 120319 | N/A | N/A |
|  CBR SOAP Header Proxy | 512M | 100 | 10240 | 500 | 100 | 0.33 | 120064 | 0 | 120319 | N/A | N/A |
|  CBR SOAP Header Proxy | 512M | 100 | 10240 | 1000 | 100 | 0.33 | 120064 | 0 | 120319 | N/A | N/A |
|  CBR SOAP Header Proxy | 512M | 100 | 102400 | 0 | 100 | 0.33 | 120064 | 0 | 120319 | N/A | N/A |
|  CBR SOAP Header Proxy | 512M | 100 | 102400 | 30 | 100 | 0.33 | 120064 | 0 | 120319 | N/A | N/A |
|  CBR SOAP Header Proxy | 512M | 100 | 102400 | 100 | 100 | 0.33 | 120064 | 0 | 120319 | N/A | N/A |
|  CBR SOAP Header Proxy | 512M | 100 | 102400 | 500 | 100 | 0.33 | 120064 | 0 | 120319 | N/A | N/A |
|  CBR SOAP Header Proxy | 512M | 100 | 102400 | 1000 | 100 | 0.33 | 120064 | 0 | 120319 | N/A | N/A |
|  CBR SOAP Header Proxy | 512M | 200 | 500 | 0 | 100 | 0.66 | 120064 | 0 | 120319 | N/A | N/A |
|  CBR SOAP Header Proxy | 512M | 200 | 500 | 30 | 100 | 0.66 | 120064 | 0 | 120319 | N/A | N/A |
|  CBR SOAP Header Proxy | 512M | 200 | 500 | 100 | 100 | 0.66 | 120064 | 0 | 120319 | N/A | N/A |
|  CBR SOAP Header Proxy | 512M | 200 | 500 | 500 | 100 | 0.66 | 120064 | 0 | 120319 | N/A | N/A |
|  CBR SOAP Header Proxy | 512M | 200 | 500 | 1000 | 100 | 0.66 | 120064 | 0 | 120319 | N/A | N/A |
|  CBR SOAP Header Proxy | 512M | 200 | 1024 | 0 | 100 | 0.66 | 120064 | 0 | 120319 | N/A | N/A |
|  CBR SOAP Header Proxy | 512M | 200 | 1024 | 30 | 100 | 0.66 | 120064 | 0 | 120319 | N/A | N/A |
|  CBR SOAP Header Proxy | 512M | 200 | 1024 | 100 | 100 | 0.66 | 120064 | 0 | 120319 | N/A | N/A |
|  CBR SOAP Header Proxy | 512M | 200 | 1024 | 500 | 100 | 0.66 | 120064 | 0 | 120319 | N/A | N/A |
|  CBR SOAP Header Proxy | 512M | 200 | 1024 | 1000 | 100 | 0.66 | 120064 | 0 | 120319 | N/A | N/A |
|  CBR SOAP Header Proxy | 512M | 200 | 10240 | 0 | 100 | 0.66 | 120064 | 0 | 120319 | N/A | N/A |
|  CBR SOAP Header Proxy | 512M | 200 | 10240 | 30 | 100 | 0.66 | 120064 | 0 | 120319 | N/A | N/A |
|  CBR SOAP Header Proxy | 512M | 200 | 10240 | 100 | 100 | 0.66 | 120064 | 0 | 120319 | N/A | N/A |
|  CBR SOAP Header Proxy | 512M | 200 | 10240 | 500 | 100 | 0.66 | 120064 | 0 | 120319 | N/A | N/A |
|  CBR SOAP Header Proxy | 512M | 200 | 10240 | 1000 | 100 | 0.66 | 120064 | 0 | 120319 | N/A | N/A |
|  CBR SOAP Header Proxy | 512M | 200 | 102400 | 0 | 100 | 0.66 | 120064 | 0 | 120319 | N/A | N/A |
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
|  CBR Transport Header Proxy | 512M | 100 | 102400 | 30 | 100 | 0.33 | 120461.06 | 498.93 | 121343 | N/A | N/A |
|  CBR Transport Header Proxy | 512M | 100 | 102400 | 100 | 100 | 0.33 | 120481.96 | 503.29 | 121343 | N/A | N/A |
|  CBR Transport Header Proxy | 512M | 100 | 102400 | 500 | 100 | 0.33 | 121088 | 0 | 121343 | N/A | N/A |
|  CBR Transport Header Proxy | 512M | 100 | 102400 | 1000 | 100 | 0.33 | 120670.04 | 503.29 | 121343 | N/A | N/A |
|  CBR Transport Header Proxy | 512M | 200 | 500 | 0 | 100 | 0.66 | 120777.7 | 470.6 | 121343 | N/A | N/A |
|  CBR Transport Header Proxy | 512M | 200 | 500 | 30 | 100 | 0.66 | 120777.7 | 470.6 | 121343 | N/A | N/A |
|  CBR Transport Header Proxy | 512M | 200 | 500 | 100 | 100 | 0.66 | 120777.7 | 470.6 | 121343 | N/A | N/A |
|  CBR Transport Header Proxy | 512M | 200 | 500 | 500 | 100 | 0.66 | 120777.7 | 470.6 | 121343 | N/A | N/A |
|  CBR Transport Header Proxy | 512M | 200 | 500 | 1000 | 100 | 0.66 | 120777.7 | 470.6 | 121343 | N/A | N/A |
|  CBR Transport Header Proxy | 512M | 200 | 1024 | 0 | 100 | 0.66 | 120777.7 | 470.6 | 121343 | N/A | N/A |
|  CBR Transport Header Proxy | 512M | 200 | 1024 | 30 | 100 | 0.66 | 120777.7 | 470.6 | 121343 | N/A | N/A |
|  CBR Transport Header Proxy | 512M | 200 | 1024 | 100 | 100 | 0.66 | 120777.7 | 470.6 | 121343 | N/A | N/A |
|  CBR Transport Header Proxy | 512M | 200 | 1024 | 500 | 100 | 0.66 | 120777.7 | 470.6 | 121343 | N/A | N/A |
|  CBR Transport Header Proxy | 512M | 200 | 1024 | 1000 | 100 | 0.66 | 120767.35 | 474.9 | 121343 | N/A | N/A |
|  CBR Transport Header Proxy | 512M | 200 | 10240 | 0 | 100 | 0.66 | 120777.7 | 470.6 | 121343 | N/A | N/A |
|  CBR Transport Header Proxy | 512M | 200 | 10240 | 30 | 100 | 0.66 | 120777.7 | 470.6 | 121343 | N/A | N/A |
|  CBR Transport Header Proxy | 512M | 200 | 10240 | 100 | 100 | 0.66 | 120777.7 | 470.6 | 121343 | N/A | N/A |
|  CBR Transport Header Proxy | 512M | 200 | 10240 | 500 | 100 | 0.66 | 120777.7 | 470.6 | 121343 | N/A | N/A |
|  CBR Transport Header Proxy | 512M | 200 | 10240 | 1000 | 100 | 0.66 | 120777.7 | 470.6 | 121343 | N/A | N/A |
|  CBR Transport Header Proxy | 512M | 200 | 102400 | 0 | 100 | 0.66 | 120777.7 | 470.6 | 121343 | N/A | N/A |
|  CBR Transport Header Proxy | 512M | 200 | 102400 | 30 | 100 | 0.66 | 120777.7 | 470.6 | 121343 | N/A | N/A |
|  CBR Transport Header Proxy | 512M | 200 | 102400 | 100 | 100 | 0.66 | 120777.7 | 470.6 | 121343 | N/A | N/A |
|  CBR Transport Header Proxy | 512M | 200 | 102400 | 500 | 100 | 0.66 | 120777.7 | 470.6 | 121343 | N/A | N/A |
|  CBR Transport Header Proxy | 512M | 200 | 102400 | 1000 | 100 | 0.66 | 120788.04 | 466.03 | 121343 | N/A | N/A |
|  Direct Proxy | 512M | 100 | 500 | 0 | 0 | 1867.95 | 40.42 | 100.87 | 747 | 72.13 | 297.552 |
|  Direct Proxy | 512M | 100 | 500 | 30 | 0 | 1881.48 | 40.07 | 34.29 | 85 | N/A | N/A |
|  Direct Proxy | 512M | 100 | 500 | 100 | 0 | 730.46 | 103.13 | 7.02 | 140 | N/A | N/A |
|  Direct Proxy | 512M | 100 | 500 | 500 | 0 | 149.28 | 501.25 | 1.65 | 503 | N/A | N/A |
|  Direct Proxy | 512M | 100 | 500 | 1000 | 0 | 74.19 | 1002.01 | 0.45 | 1003 | N/A | N/A |
|  Direct Proxy | 512M | 100 | 1024 | 0 | 0 | 2248.63 | 33.53 | 109.62 | 747 | N/A | N/A |
|  Direct Proxy | 512M | 100 | 1024 | 30 | 0 | 1865.94 | 40.4 | 48.62 | 84 | N/A | N/A |
|  Direct Proxy | 512M | 100 | 1024 | 100 | 0 | 720.67 | 104.73 | 17.73 | 145 | N/A | N/A |
|  Direct Proxy | 512M | 100 | 1024 | 500 | 0 | 149.24 | 501.31 | 1.58 | 509 | N/A | N/A |
|  Direct Proxy | 512M | 100 | 1024 | 1000 | 0 | 74.14 | 1002.07 | 0.93 | 1003 | N/A | N/A |
|  Direct Proxy | 512M | 100 | 10240 | 0 | 0 | 1815.52 | 41.35 | 97.15 | 695 | N/A | N/A |
|  Direct Proxy | 512M | 100 | 10240 | 30 | 0 | 1781.17 | 42.31 | 23.64 | 91 | N/A | N/A |
|  Direct Proxy | 512M | 100 | 10240 | 100 | 0 | 723.37 | 104.09 | 7.07 | 142 | N/A | N/A |
|  Direct Proxy | 512M | 100 | 10240 | 500 | 0 | 149.12 | 501.44 | 1.54 | 503 | N/A | N/A |
|  Direct Proxy | 512M | 100 | 10240 | 1000 | 0 | 74.05 | 1002.05 | 0.76 | 1003 | N/A | N/A |
|  Direct Proxy | 512M | 100 | 102400 | 0 | 0 | 1072.47 | 70.1 | 25.78 | 150 | N/A | N/A |
|  Direct Proxy | 512M | 100 | 102400 | 30 | 0 | 973.79 | 77.15 | 21.95 | 145 | N/A | N/A |
|  Direct Proxy | 512M | 100 | 102400 | 100 | 0 | 589.87 | 127.7 | 22.34 | 194 | N/A | N/A |
|  Direct Proxy | 512M | 100 | 102400 | 500 | 0 | 148.23 | 503.99 | 2.45 | 515 | N/A | N/A |
|  Direct Proxy | 512M | 100 | 102400 | 1000 | 0 | 73.96 | 1002.13 | 0.86 | 1007 | N/A | N/A |
|  Direct Proxy | 512M | 200 | 500 | 0 | 0 | 1942.69 | 77.42 | 168.3 | 811 | N/A | N/A |
|  Direct Proxy | 512M | 200 | 500 | 30 | 0 | 1920.88 | 78.69 | 170 | 799 | N/A | N/A |
|  Direct Proxy | 512M | 200 | 500 | 100 | 0 | 1375.87 | 109.16 | 12.88 | 157 | N/A | N/A |
|  Direct Proxy | 512M | 200 | 500 | 500 | 0 | 296.74 | 501.72 | 3.21 | 519 | N/A | N/A |
|  Direct Proxy | 512M | 200 | 500 | 1000 | 0 | 147.17 | 1002.17 | 1.78 | 1007 | N/A | N/A |
|  Direct Proxy | 512M | 200 | 1024 | 0 | 0 | 2313.64 | 64.38 | 126.34 | 731 | N/A | N/A |
|  Direct Proxy | 512M | 200 | 1024 | 30 | 0 | 1912.33 | 78.47 | 146.99 | 735 | N/A | N/A |
|  Direct Proxy | 512M | 200 | 1024 | 100 | 0 | 1334.9 | 112.44 | 15.96 | 163 | N/A | N/A |
|  Direct Proxy | 512M | 200 | 1024 | 500 | 0 | 296.95 | 501.82 | 3.89 | 527 | N/A | N/A |
|  Direct Proxy | 512M | 200 | 1024 | 1000 | 0 | 147.12 | 1002.17 | 1.7 | 1011 | N/A | N/A |
|  Direct Proxy | 512M | 200 | 10240 | 0 | 0 | 2555.82 | 58.78 | 73.68 | 169 | N/A | N/A |
|  Direct Proxy | 512M | 200 | 10240 | 30 | 0 | 2350.3 | 63.85 | 30.07 | 132 | N/A | N/A |
|  Direct Proxy | 512M | 200 | 10240 | 100 | 0 | 1300.43 | 115.4 | 23.53 | 176 | N/A | N/A |
|  Direct Proxy | 512M | 200 | 10240 | 500 | 0 | 296.7 | 502.39 | 4.74 | 531 | N/A | N/A |
|  Direct Proxy | 512M | 200 | 10240 | 1000 | 0 | 147.23 | 1002.14 | 1.5 | 1007 | N/A | N/A |
|  Direct Proxy | 512M | 200 | 102400 | 0 | 0 | 994.65 | 150.92 | 48.44 | 281 | N/A | N/A |
|  Direct Proxy | 512M | 200 | 102400 | 30 | 0 | 1015.19 | 147.6 | 46.62 | 289 | N/A | N/A |
|  Direct Proxy | 512M | 200 | 102400 | 100 | 0 | 812.97 | 184.61 | 44.21 | 297 | N/A | N/A |
|  Direct Proxy | 512M | 200 | 102400 | 500 | 0 | 291.58 | 510.96 | 13.52 | 567 | N/A | N/A |
|  Direct Proxy | 512M | 200 | 102400 | 1000 | 0 | 146.51 | 1003.08 | 2.86 | 1015 | N/A | N/A |
|  XSLT Enhanced Proxy | 512M | 100 | 500 | 0 | 100 | 0.33 | 120461.06 | 498.93 | 121343 | N/A | N/A |
|  XSLT Enhanced Proxy | 512M | 100 | 500 | 30 | 100 | 0.33 | 120461.06 | 498.93 | 121343 | N/A | N/A |
|  XSLT Enhanced Proxy | 512M | 100 | 500 | 100 | 100 | 0.33 | 120461.06 | 498.93 | 121343 | N/A | N/A |
|  XSLT Enhanced Proxy | 512M | 100 | 500 | 500 | 100 | 0.33 | 120461.06 | 498.93 | 121343 | N/A | N/A |
|  XSLT Enhanced Proxy | 512M | 100 | 500 | 1000 | 100 | 0.33 | 120461.06 | 498.93 | 121343 | N/A | N/A |
|  XSLT Enhanced Proxy | 512M | 100 | 1024 | 0 | 100 | 0.33 | 120461.06 | 498.93 | 121343 | N/A | N/A |
|  XSLT Enhanced Proxy | 512M | 100 | 1024 | 30 | 100 | 0.33 | 120461.06 | 498.93 | 121343 | N/A | N/A |
|  XSLT Enhanced Proxy | 512M | 100 | 1024 | 100 | 100 | 0.33 | 120481.96 | 503.29 | 121343 | N/A | N/A |
|  XSLT Enhanced Proxy | 512M | 100 | 1024 | 500 | 100 | 0.33 | 120481.96 | 503.29 | 121343 | N/A | N/A |
|  XSLT Enhanced Proxy | 512M | 100 | 1024 | 1000 | 100 | 0.33 | 120481.96 | 503.29 | 121343 | N/A | N/A |
|  XSLT Enhanced Proxy | 512M | 100 | 10240 | 0 | 100 | 0.33 | 120481.96 | 503.29 | 121343 | N/A | N/A |
|  XSLT Enhanced Proxy | 512M | 100 | 10240 | 30 | 100 | 0.33 | 120481.96 | 503.29 | 121343 | N/A | N/A |
|  XSLT Enhanced Proxy | 512M | 100 | 10240 | 100 | 100 | 0.33 | 120481.96 | 503.29 | 121343 | N/A | N/A |
|  XSLT Enhanced Proxy | 512M | 100 | 10240 | 500 | 100 | 0.33 | 120481.96 | 503.29 | 121343 | N/A | N/A |
|  XSLT Enhanced Proxy | 512M | 100 | 10240 | 1000 | 100 | 0.33 | 120481.96 | 503.29 | 121343 | N/A | N/A |
|  XSLT Enhanced Proxy | 512M | 100 | 102400 | 0 | 100 | 0.33 | 120481.96 | 503.29 | 121343 | N/A | N/A |
|  XSLT Enhanced Proxy | 512M | 100 | 102400 | 30 | 100 | 0.33 | 120481.96 | 503.29 | 121343 | N/A | N/A |
|  XSLT Enhanced Proxy | 512M | 100 | 102400 | 100 | 100 | 0.33 | 120481.96 | 503.29 | 121343 | N/A | N/A |
|  XSLT Enhanced Proxy | 512M | 100 | 102400 | 500 | 100 | 0.33 | 121088 | 0 | 121343 | N/A | N/A |
|  XSLT Enhanced Proxy | 512M | 100 | 102400 | 1000 | 100 | 0.33 | 120690.94 | 498.93 | 121343 | N/A | N/A |
|  XSLT Enhanced Proxy | 512M | 200 | 500 | 0 | 100 | 0.66 | 120777.7 | 470.6 | 121343 | N/A | N/A |
|  XSLT Enhanced Proxy | 512M | 200 | 500 | 30 | 100 | 0.66 | 120777.7 | 470.6 | 121343 | N/A | N/A |
|  XSLT Enhanced Proxy | 512M | 200 | 500 | 100 | 100 | 0.66 | 120777.7 | 470.6 | 121343 | N/A | N/A |
|  XSLT Enhanced Proxy | 512M | 200 | 500 | 500 | 100 | 0.66 | 120777.7 | 470.6 | 121343 | N/A | N/A |
|  XSLT Enhanced Proxy | 512M | 200 | 500 | 1000 | 100 | 0.66 | 120777.7 | 470.6 | 121343 | N/A | N/A |
|  XSLT Enhanced Proxy | 512M | 200 | 1024 | 0 | 100 | 0.66 | 120767.35 | 474.9 | 121343 | N/A | N/A |
|  XSLT Enhanced Proxy | 512M | 200 | 1024 | 30 | 100 | 0.66 | 120777.7 | 470.6 | 121343 | N/A | N/A |
|  XSLT Enhanced Proxy | 512M | 200 | 1024 | 100 | 100 | 0.66 | 120777.7 | 470.6 | 121343 | N/A | N/A |
|  XSLT Enhanced Proxy | 512M | 200 | 1024 | 500 | 100 | 0.66 | 120767.35 | 474.9 | 121343 | N/A | N/A |
|  XSLT Enhanced Proxy | 512M | 200 | 1024 | 1000 | 100 | 0.66 | 120777.7 | 470.6 | 121343 | N/A | N/A |
|  XSLT Enhanced Proxy | 512M | 200 | 10240 | 0 | 100 | 0.66 | 120777.7 | 470.6 | 121343 | N/A | N/A |
|  XSLT Enhanced Proxy | 512M | 200 | 10240 | 30 | 100 | 0.66 | 120777.7 | 470.6 | 121343 | N/A | N/A |
|  XSLT Enhanced Proxy | 512M | 200 | 10240 | 100 | 100 | 0.66 | 120777.7 | 470.6 | 121343 | N/A | N/A |
|  XSLT Enhanced Proxy | 512M | 200 | 10240 | 500 | 100 | 0.66 | 120777.7 | 470.6 | 121343 | N/A | N/A |
|  XSLT Enhanced Proxy | 512M | 200 | 10240 | 1000 | 100 | 0.66 | 120777.7 | 470.6 | 121343 | N/A | N/A |
|  XSLT Enhanced Proxy | 512M | 200 | 102400 | 0 | 100 | 0.66 | 120777.7 | 470.6 | 121343 | N/A | N/A |
|  XSLT Enhanced Proxy | 512M | 200 | 102400 | 30 | 100 | 0.66 | 120777.7 | 470.6 | 121343 | N/A | N/A |
|  XSLT Enhanced Proxy | 512M | 200 | 102400 | 100 | 100 | 0.66 | 120777.7 | 470.6 | 121343 | N/A | N/A |
|  XSLT Enhanced Proxy | 512M | 200 | 102400 | 500 | 100 | 0.66 | 120777.7 | 470.6 | 121343 | N/A | N/A |
|  XSLT Enhanced Proxy | 512M | 200 | 102400 | 1000 | 100 | 0.66 | 120788.04 | 466.03 | 121343 | N/A | N/A |
|  XSLT Proxy | 512M | 100 | 500 | 0 | 100 | 0.33 | 120064 | 0 | 120319 | N/A | N/A |
|  XSLT Proxy | 512M | 100 | 500 | 30 | 100 | 0.33 | 120064 | 0 | 120319 | N/A | N/A |
|  XSLT Proxy | 512M | 100 | 500 | 100 | 100 | 0.33 | 120064 | 0 | 120319 | N/A | N/A |
|  XSLT Proxy | 512M | 100 | 500 | 500 | 100 | 0.33 | 120064 | 0 | 120319 | N/A | N/A |
|  XSLT Proxy | 512M | 100 | 500 | 1000 | 100 | 0.33 | 120064 | 0 | 120319 | N/A | N/A |
|  XSLT Proxy | 512M | 100 | 1024 | 0 | 100 | 0.33 | 120064 | 0 | 120319 | N/A | N/A |
|  XSLT Proxy | 512M | 100 | 1024 | 30 | 100 | 0.33 | 120064 | 0 | 120319 | N/A | N/A |
|  XSLT Proxy | 512M | 100 | 1024 | 100 | 100 | 0.33 | 120064 | 0 | 120319 | N/A | N/A |
|  XSLT Proxy | 512M | 100 | 1024 | 500 | 100 | 0.33 | 120064 | 0 | 120319 | N/A | N/A |
|  XSLT Proxy | 512M | 100 | 1024 | 1000 | 100 | 0.33 | 120064 | 0 | 120319 | N/A | N/A |
|  XSLT Proxy | 512M | 100 | 10240 | 0 | 100 | 0.33 | 120064 | 0 | 120319 | N/A | N/A |
|  XSLT Proxy | 512M | 100 | 10240 | 30 | 100 | 0.33 | 120064 | 0 | 120319 | N/A | N/A |
|  XSLT Proxy | 512M | 100 | 10240 | 100 | 100 | 0.33 | 120064 | 0 | 120319 | N/A | N/A |
|  XSLT Proxy | 512M | 100 | 10240 | 500 | 100 | 0.33 | 120064 | 0 | 120319 | N/A | N/A |
|  XSLT Proxy | 512M | 100 | 10240 | 1000 | 100 | 0.33 | 120064 | 0 | 120319 | N/A | N/A |
|  XSLT Proxy | 512M | 100 | 102400 | 0 | 100 | 0.33 | 120064 | 0 | 120319 | N/A | N/A |
|  XSLT Proxy | 512M | 100 | 102400 | 30 | 100 | 0.33 | 120064 | 0 | 120319 | N/A | N/A |
|  XSLT Proxy | 512M | 100 | 102400 | 100 | 100 | 0.81 | 40165.33 | 9809.79 | 59135 | N/A | N/A |
|  XSLT Proxy | 512M | 100 | 102400 | 500 | 100 | 0.33 | 120064 | 0 | 120319 | N/A | N/A |
|  XSLT Proxy | 512M | 100 | 102400 | 1000 | 100 | 0.33 | 120064 | 0 | 120319 | N/A | N/A |
|  XSLT Proxy | 512M | 200 | 500 | 0 | 100 | 0.66 | 120064 | 0 | 120319 | N/A | N/A |
|  XSLT Proxy | 512M | 200 | 500 | 30 | 100 | 0.66 | 120064 | 0 | 120319 | N/A | N/A |
|  XSLT Proxy | 512M | 200 | 500 | 100 | 100 | 0.66 | 120064 | 0 | 120319 | N/A | N/A |
|  XSLT Proxy | 512M | 200 | 500 | 500 | 100 | 0.66 | 120064 | 0 | 120319 | N/A | N/A |
|  XSLT Proxy | 512M | 200 | 500 | 1000 | 100 | 0.66 | 120064 | 0 | 120319 | N/A | N/A |
|  XSLT Proxy | 512M | 200 | 1024 | 0 | 100 | 0.66 | 120064 | 0 | 120319 | N/A | N/A |
|  XSLT Proxy | 512M | 200 | 1024 | 30 | 100 | 0.66 | 120064 | 0 | 120319 | N/A | N/A |
|  XSLT Proxy | 512M | 200 | 1024 | 100 | 100 | 0.66 | 120064 | 0 | 120319 | N/A | N/A |
|  XSLT Proxy | 512M | 200 | 1024 | 500 | 100 | 0.66 | 120064 | 0 | 120319 | N/A | N/A |
|  XSLT Proxy | 512M | 200 | 1024 | 1000 | 100 | 0.66 | 120064 | 0 | 120319 | N/A | N/A |
|  XSLT Proxy | 512M | 200 | 10240 | 0 | 100 | 0.66 | 120064 | 0 | 120319 | N/A | N/A |
|  XSLT Proxy | 512M | 200 | 10240 | 30 | 100 | 0.66 | 120064 | 0 | 120319 | N/A | N/A |
|  XSLT Proxy | 512M | 200 | 10240 | 100 | 100 | 0.66 | 120064 | 0 | 120319 | N/A | N/A |
|  XSLT Proxy | 512M | 200 | 10240 | 500 | 100 | 0.66 | 120064 | 0 | 120319 | N/A | N/A |
|  XSLT Proxy | 512M | 200 | 10240 | 1000 | 100 | 0.66 | 120064 | 0 | 120319 | N/A | N/A |
|  XSLT Proxy | 512M | 200 | 102400 | 0 | 100 | 0.66 | 120064 | 0 | 120319 | N/A | N/A |
|  XSLT Proxy | 512M | 200 | 102400 | 30 | 100 | 0.66 | 120064 | 0 | 120319 | N/A | N/A |
|  XSLT Proxy | 512M | 200 | 102400 | 100 | 100 | 0.66 | 120064 | 0 | 120319 | N/A | N/A |
|  XSLT Proxy | 512M | 200 | 102400 | 500 | 100 | 0.66 | 120064 | 0 | 120319 | N/A | N/A |
|  XSLT Proxy | 512M | 200 | 102400 | 1000 | 100 | 0.66 | 120064 | 0 | 120319 | N/A | N/A |
