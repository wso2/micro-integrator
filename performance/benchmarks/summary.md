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
| Message Size (Bytes) | The request payload size in Bytes. | 204800 |
| Back-end Delay (ms) | The delay added by the Back-end service. | 0 |

The duration of each test is **60 seconds**. The warm-up period is **30 seconds**.
The measurement results are collected after the warm-up period.

The performance tests were executed on 2 AWS CloudFormation stacks.


System information for WSO2 Micro Integrator 1.2.0 in 1st AWS CloudFormation stack.

| Class | Subclass | Description | Value |
| --- | --- | --- | --- |
| AWS | EC2 | AMI-ID | ami-0bcc094591f354be2 |
| AWS | EC2 | Instance Type | c5.large |
| System | Processor | CPU(s) | 2 |
| System | Processor | Thread(s) per core | 2 |
| System | Processor | Core(s) per socket | 1 |
| System | Processor | Socket(s) | 1 |
| System | Processor | Model name | Intel(R) Xeon(R) Platinum 8124M CPU @ 3.00GHz |
| System | Memory | BIOS | 64 KiB |
| System | Memory | System memory | 3785216 KiB |
| System | Storage | Block Device: nvme0n1 | 8G |
| Operating System | Distribution | Release | Ubuntu 18.04.5 LTS |
| Operating System | Distribution | Kernel | Linux ip-10-0-1-188 5.3.0-1032-aws #34~18.04.2-Ubuntu SMP Fri Jul 24 10:06:28 UTC 2020 x86_64 x86_64 x86_64 GNU/Linux |
System information for WSO2 Micro Integrator 1.2.0 in 2nd AWS CloudFormation stack.

| Class | Subclass | Description | Value |
| --- | --- | --- | --- |
| AWS | EC2 | AMI-ID | ami-0bcc094591f354be2 |
| AWS | EC2 | Instance Type | c5.large |
| System | Processor | CPU(s) | 2 |
| System | Processor | Thread(s) per core | 2 |
| System | Processor | Core(s) per socket | 1 |
| System | Processor | Socket(s) | 1 |
| System | Processor | Model name | Intel(R) Xeon(R) Platinum 8124M CPU @ 3.00GHz |
| System | Memory | BIOS | 64 KiB |
| System | Memory | System memory | 3785216 KiB |
| System | Storage | Block Device: nvme0n1 | 8G |
| Operating System | Distribution | Release | Ubuntu 18.04.5 LTS |
| Operating System | Distribution | Kernel | Linux ip-10-0-1-159 5.3.0-1032-aws #34~18.04.2-Ubuntu SMP Fri Jul 24 10:06:28 UTC 2020 x86_64 x86_64 x86_64 GNU/Linux |


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
|  CBR Proxy | 2G | 100 | 204800 | 0 | 0 | 52.81 | 1418.9 | 558.86 | 3311 | N/A | N/A |
|  CBR Proxy | 2G | 200 | 204800 | 0 | 0 | 43.3 | 3428.89 | 1471.09 | 7103 | N/A | N/A |
|  CBR Proxy | 2G | 500 | 204800 | 0 | 0 | 17.5 | 20144.15 | 5035.18 | 33023 | N/A | N/A |
|  CBR Proxy | 2G | 1000 | 204800 | 0 | 100 | 4.19 | 121609.73 | 3348.97 | 136191 | N/A | N/A |
|  CBR SOAP Header Proxy | 2G | 100 | 204800 | 0 | 0 | 109 | 690.33 | 237.3 | 1383 | N/A | N/A |
|  CBR SOAP Header Proxy | 2G | 200 | 204800 | 0 | 0 | 76.48 | 1968.7 | 937.01 | 4255 | N/A | N/A |
|  CBR SOAP Header Proxy | 2G | 500 | 204800 | 0 | 7.89 | 10.51 | 13116.49 | 31332.22 | 120319 | N/A | N/A |
|  CBR SOAP Header Proxy | 2G | 1000 | 204800 | 0 | 19.51 | 8.48 | 34014.4 | 42510.62 | 120319 | N/A | N/A |
|  CBR Transport Header Proxy | 2G | 100 | 204800 | 0 | 0 | 808.58 | 92.94 | 48.1 | 242 | N/A | N/A |
|  CBR Transport Header Proxy | 2G | 200 | 204800 | 0 | 0 | 767.08 | 195.41 | 97.7 | 485 | N/A | N/A |
|  CBR Transport Header Proxy | 2G | 500 | 204800 | 0 | 0.66 | 125.85 | 1091.72 | 9687.41 | 631 | N/A | N/A |
|  CBR Transport Header Proxy | 2G | 1000 | 204800 | 0 | 1.39 | 118.95 | 2312.48 | 13989.29 | 120319 | N/A | N/A |
|  Direct Proxy | 2G | 100 | 204800 | 0 | 0 | 626.68 | 120.14 | 51.41 | 265 | 98.56 | 28.303 |
|  Direct Proxy | 2G | 200 | 204800 | 0 | 0 | 783.85 | 191.35 | 98.49 | 475 | N/A | N/A |
|  Direct Proxy | 2G | 500 | 204800 | 0 | 0 | 523.99 | 710.66 | 175.2 | 1223 | 98.9 | 28.121 |
|  Direct Proxy | 2G | 1000 | 204800 | 0 | 0 | 582.54 | 1276.33 | 320.74 | 2255 | N/A | N/A |
|  XSLT Enhanced Proxy | 2G | 100 | 204800 | 0 | 0 | 48.87 | 1523.54 | 425.64 | 2655 | N/A | N/A |
|  XSLT Enhanced Proxy | 2G | 200 | 204800 | 0 | 0 | 45.99 | 3225.35 | 940.28 | 5247 | N/A | N/A |
|  XSLT Enhanced Proxy | 2G | 500 | 204800 | 0 | 7.93 | 10.45 | 13385.77 | 31313.8 | 120319 | N/A | N/A |
|  XSLT Enhanced Proxy | 2G | 1000 | 204800 | 0 | 15.74 | 10.52 | 27082.93 | 40230.95 | 120319 | N/A | N/A |
|  XSLT Proxy | 2G | 100 | 204800 | 0 | 0 | 19.22 | 3874.77 | 961.06 | 5695 | N/A | N/A |
|  XSLT Proxy | 2G | 200 | 204800 | 0 | 0 | 16.93 | 8073.38 | 2164.19 | 12479 | N/A | N/A |
|  XSLT Proxy | 2G | 500 | 204800 | 0 | 0 | 7.05 | 42608.38 | 5026.61 | 53503 | N/A | N/A |
|  XSLT Proxy | 2G | 1000 | 204800 | 0 | 85.42 | 2.61 | 129130.29 | 35878.67 | 246783 | N/A | N/A |
