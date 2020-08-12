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
| Message Size (Bytes) | The request payload size in Bytes. | 512000 |
| Back-end Delay (ms) | The delay added by the Back-end service. | 0 |

The duration of each test is **60 seconds**. The warm-up period is **30 seconds**.
The measurement results are collected after the warm-up period.

The performance tests were executed on 1 AWS CloudFormation stack.


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
| Operating System | Distribution | Kernel | Linux ip-10-0-1-236 5.3.0-1032-aws #34~18.04.2-Ubuntu SMP Fri Jul 24 10:06:28 UTC 2020 x86_64 x86_64 x86_64 GNU/Linux |


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
|  CBR Proxy | 2G | 100 | 512000 | 0 | 0 | 6.86 | 9640.71 | 2765.94 | 15231 | N/A | N/A |
|  CBR Proxy | 2G | 200 | 512000 | 0 | 0 | 3.52 | 36915.2 | 7178.76 | 49919 | N/A | N/A |
|  CBR Proxy | 2G | 500 | 512000 | 0 | 93.25 | 0.31 | 568860.44 | 161419.7 | 798719 | N/A | N/A |
|  CBR Proxy | 2G | 1000 | 512000 | 0 | 100 | 2.78 | 120299.91 | 3711.39 | 120319 | N/A | N/A |
|  CBR SOAP Header Proxy | 2G | 100 | 512000 | 0 | 0 | 17.27 | 4239.99 | 1797.57 | 7327 | N/A | N/A |
|  CBR SOAP Header Proxy | 2G | 200 | 512000 | 0 | 0 | 10.4 | 13551.39 | 4462.42 | 23039 | N/A | N/A |
|  CBR SOAP Header Proxy | 2G | 500 | 512000 | 0 | 100 | 0.47 | 408625.23 | 186913.15 | 552959 | N/A | N/A |
|  CBR SOAP Header Proxy | 2G | 1000 | 512000 | 0 | 100 | 19527.12 | 30.25 | 46.16 | 233 | N/A | N/A |
|  CBR Transport Header Proxy | 2G | 100 | 512000 | 0 | 100 | 24592.72 | 2.54 | 2.09 | 7 | N/A | N/A |
|  CBR Transport Header Proxy | 2G | 200 | 512000 | 0 | 100 | 24298.85 | 5.23 | 5.03 | 18 | N/A | N/A |
|  CBR Transport Header Proxy | 2G | 500 | 512000 | 0 | 100 | 22592.69 | 14.23 | 21.86 | 114 | N/A | N/A |
|  CBR Transport Header Proxy | 2G | 1000 | 512000 | 0 | 100 | 19533.71 | 30.43 | 51.34 | 269 | N/A | N/A |
|  Direct Proxy | 2G | 100 | 512000 | 0 | 0 | 236.67 | 318.79 | 76.89 | 519 | 99.21 | 28.217 |
|  Direct Proxy | 2G | 200 | 512000 | 0 | 0 | 259 | 577.09 | 155.94 | 1007 | N/A | N/A |
|  Direct Proxy | 2G | 500 | 512000 | 0 | 0 | 229.01 | 1616.91 | 432.59 | 2927 | N/A | N/A |
|  Direct Proxy | 2G | 1000 | 512000 | 0 | 0 | 210.06 | 3462.1 | 873.15 | 5887 | N/A | N/A |
|  XSLT Enhanced Proxy | 2G | 100 | 512000 | 0 | 100 | 24449.98 | 2.55 | 2.06 | 7 | N/A | N/A |
|  XSLT Enhanced Proxy | 2G | 200 | 512000 | 0 | 100 | 24236.16 | 5.26 | 5.72 | 22 | N/A | N/A |
|  XSLT Enhanced Proxy | 2G | 500 | 512000 | 0 | 100 | 22569.86 | 14.09 | 22.82 | 121 | N/A | N/A |
|  XSLT Enhanced Proxy | 2G | 1000 | 512000 | 0 | 100 | 19270.85 | 31.12 | 49.47 | 259 | N/A | N/A |
|  XSLT Proxy | 2G | 100 | 512000 | 0 | 0 | 3.55 | 18080 | 2721.53 | 24191 | N/A | N/A |
|  XSLT Proxy | 2G | 200 | 512000 | 0 | 0 | 1.91 | 61403.08 | 5401.59 | 74239 | N/A | N/A |
|  XSLT Proxy | 2G | 500 | 512000 | 0 | 87.92 | 0.43 | 581108.41 | 72843.56 | 618495 | N/A | N/A |
|  XSLT Proxy | 2G | 1000 | 512000 | 0 | 100 | 0.77 | 228411.24 | 106321.67 | 346111 | N/A | N/A |
