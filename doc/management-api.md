# WSO2 Micro Integrator Management Api
This specifies a RESTful API for WSO2 Micro Integrator Management

Please see [full swagger definition](https://raw.githubusercontent.com/wso2/micro-integrator/master/doc/management-api.yaml) of the API which is written using [swagger 2.0](http://swagger.io/) specification.


## Version: 1.0.0-RC1

**License:** [Apache 2.0](http://www.apache.org/licenses/LICENSE-2.0.html)

### /applications

#### GET
##### Summary:

Retrieve Applications


##### Description:

This operation provides you a list of available Applications.

Each retrieved Application is represented with a minimal amount of attributes. If you want to get complete details of an Application, you need to use **Get details of an Application** operation.


##### Responses

| Code | Description | Schema |
| ---- | ----------- | ------ |
| 200 | OK. List of Applications is returned. | [ApplicationList](#applicationlist) |

##### Example

```
curl -X GET "https://localhost:9164/management/applications" -H "accept: application/json" -k
```

```json
{
  "count": 1,
  "list": [
    {
      "name": "SampleServicesCompositeApplication",
      "version": "1.0.0"
    }
  ]
}
```

### /applications?carbonAppName={appname}

#### GET
##### Summary:

Get details of an application


##### Description:

This operation can be used to retrieve details of an individual application. You need to provide the name of the application to retrieve it.


##### Parameters

| Name | Located in | Description | Required | Schema |
| ---- | ---------- | ----------- | -------- | ---- |
| carbonAppName | query | Name of the Carbon app | Yes | string |

##### Responses

| Code | Description | Schema |
| ---- | ----------- | ------ |
| 200 | OK. Requested Application is returned | [ApplicationInfo](#applicationinfo) |

##### Example
```
curl -X GET "https://localhost:9164/management/applications?carbonAppName=SampleServicesCompositeApplication" -H "accept: application/json" -k
```

```json
{
  "name": "SampleServicesCompositeApplication",
  "version": "1.0.0",
  "artifacts": [
    {
      "name": "HealthcareAPI",
      "type": "api"
    }
  ]
}
```

| Code | Description | Schema |
| ---- | ----------- | ------ |
| 404 | Not Found. Requested Application doesnot exist |  |

### /apis

#### GET
##### Summary:

Retrieve APIs 


##### Description:

This operation provides you a list of available APIs.

Each retrieved API is represented with a minimal amount of attributes. If you want to get complete details of an API, you need to use **Get details of an API** operation.


##### Responses

| Code | Description | Schema |
| ---- | ----------- | ------ |
| 200 | OK. List of APIs is returned.  | [ApiList](#apilist) |

##### Example

```
curl -X GET "https://localhost:9164/management/apis" -H "accept: application/json" -k
```

```json
{
  "count": 1,
  "list": [
    {
      "name": "HealthcareAPI",
      "url": "http://172.17.0.1:8290/healthcare"
    }
  ]
}
```

### /apis?apiName={apiname}

#### GET
##### Summary:

Get details of an API


##### Description:

This operation can be used to retrieve details of an individual api. You need to provide the name of the api to retrieve it.


##### Parameters

| Name | Located in | Description | Required | Schema |
| ---- | ---------- | ----------- | -------- | ---- |
| apiName | query | Name of the Api | Yes | string |

##### Responses

| Code | Description | Schema |
| ---- | ----------- | ------ |
| 200 | OK. Requested API is returned.  | [ApiInfo](#apiinfo) |

##### Example
```
curl -X GET "https://localhost:9164/management/apis?apiName=HealthcareAPI" -H "accept: application/json" -k
```
```json
{
  "tracing": "disabled",
  "stats": "disabled",
  "name": "HealthcareAPI",
  "resources": [
    {
      "methods": [
        "GET"
      ],
      "url": "/querydoctor/{category}"
    }
  ],
  "version": "N/A",
  "url": "http://172.17.0.1:8290/healthcare"
}
```

| Code | Description | Schema |
| ---- | ----------- | ------ |
| 404 | Not Found. Requested Api doesnot exist |  |

### /endpoints

#### GET
##### Summary:

Retrieve Endpoints 


##### Description:

This operation provides you a list of available Endpoints.

Each retrieved Endpoint is represented with a minimal amount of attributes. If you want to get complete details of an Endpoint, you need to use **Get details of an Endpoint** operation.


##### Responses

| Code | Description | Schema |
| ---- | ----------- | ------ |
| 200 | OK. List of Endpoints is returned. | [EndpointList](#endpointlist) |

##### Example

```
curl -X GET "https://localhost:9164/management/endpoints" -H "accept: application/json" -k
```

```json
{
  "count": 1,
  "list": [
    {
      "method": "POST",
      "name": "ClemencyEP",
      "type": "http",
      "url": "http://localhost:9090/clemency/categories/{uri.var.category}/reserve"
    }
  ]
}
```

### /endpoints?endpointName={endpointname}

#### GET
##### Summary:

Get details of an Endpoint


##### Description:

This operation can be used to retrieve details of an individual endpoint. You need to provide the name of the endpoint to retrieve it.


##### Parameters

| Name | Located in | Description | Required | Schema |
| ---- | ---------- | ----------- | -------- | ---- |
| endpointName | query | Name of the Endpoint | Yes | string |

##### Responses

| Code | Description | Schema |
| ---- | ----------- | ------ |
| 200 | OK. Requested Endpoint is returned. | [EndpointInfo](#endpointinfo) |

##### Example

```
curl -X GET "https://localhost:9164/management/endpoints?endpointName=ClemencyEP" -H "accept: application/json" -k
```

```json
{
  "method": "POST",
  "stats": "disabled",
  "name": "ClemencyEP",
  "type": "http",
  "url": "http://localhost:9090/clemency/categories/{uri.var.category}/reserve"
}
```

| Code | Description | Schema |
| ---- | ----------- | ------ |
| 404 | Not Found. Requested Endpoint doesnot exist |  |

### /inbound-endpoints

#### GET
##### Summary:

Retrieve Inbound Endpoints 


##### Description:

This operation provides you a list of available Inbound Endpoints.

Each retrieved Inbound Endpoint is represented with a minimal amount of attributes. If you want to get complete details of an Inbound Endpoint, you need to use **Get details of an Inbound Endpoint** operation.


##### Responses

| Code | Description | Schema |
| ---- | ----------- | ------ |
| 200 | OK. List of InboundEndpoints is returned. | [InboundEndpointList](#inboundendpointlist) |

##### Example

```
curl -X GET "https://localhost:9164/management/inbound-endpoints" -H "accept: application/json" -k
```

```json
{
  "count": 1,
  "list": [
    {
      "name": "TestInbound",
      "protocol": "http"
    }
  ]
}
```

### /inbound-endpoints?inboundEndpointName={inboundname}

#### GET
##### Summary:

Get details of an Inbound Endpoints 


##### Description:

This operation can be used to retrieve details of an individual Inboundendpoint. You need to provide the name of the Inboundendpoint to retrieve it.


##### Parameters

| Name | Located in | Description | Required | Schema |
| ---- | ---------- | ----------- | -------- | ---- |
| inboundEndpointName | query | Name of the InboundEndpoint | Yes | string |

##### Responses

| Code | Description | Schema |
| ---- | ----------- | ------ |
| 200 | OK. Requested InboundEndpoint is returned. | [InboundEndpointInfo](#inboundendpointinfo) |

##### Example

```
curl -X GET "https://localhost:9164/management/inbound-endpoints?inboundEndpointName=TestInbound" -H "accept: application/json" -k
```

```json
{
  "protocol": "http",
  "tracing": "disabled",
  "stats": "disabled",
  "name": "TestInbound",
  "parameters": [
    {
      "name": "inbound.http.port",
      "value": 8000
    }
  ]
}
```

| Code | Description | Schema |
| ---- | ----------- | ------ |
| 404 | Not Found. Requested InboundEndpoint doesnot exist |  |

### /proxy-services

#### GET
##### Summary:

Retrieve Proxy Services


##### Description:

This operation provides you a list of available Proxy Services.

Each retrieved Proxy Service is represented with a minimal amount of attributes. If you want to get complete details of a Proxy Service, you need to use **Get details of a Proxy Service** operation.


##### Responses

| Code | Description | Schema |
| ---- | ----------- | ------ |
| 200 | OK. List of Proxy Services is returned. | [ProxyServiceList](#proxyservicelist) |

##### Example

```
curl -X GET "https://localhost:9164/management/proxy-services" -H "accept: application/json" -k
```

```json
{
  "count": 1,
  "list": [
    {
      "name": "TestProxy",
      "wsdl1_1": "http://ThinkPad-X1-Carbon-3rd:8290/services/TestProxy?wsdl",
      "wsdl2_0": "http://ThinkPad-X1-Carbon-3rd:8290/services/TestProxy?wsdl2"
    }
  ]
}
```

### /proxy-services?proxyServiceName={proxyname}

#### GET
##### Summary:

Get details of a Proxy Services 


##### Description:

This operation can be used to retrieve details of an individual Proxy Service. You need to provide the name of the Proxy Service to retrieve it.


##### Parameters

| Name | Located in | Description | Required | Schema |
| ---- | ---------- | ----------- | -------- | ---- |
| proxyServiceName | query | Name of the Proxy service | Yes | string |

##### Responses

| Code | Description | Schema |
| ---- | ----------- | ------ |
| 200 | OK. Requested Proxy Service is returned. | [ProxyServiceInfo](#proxyserviceinfo) |

##### Example

```
curl -X GET "https://localhost:9164/management/proxy-services?proxyServiceName=TestProxy" -H "accept: application/json" -k
```

```json
{
  "tracing": "disabled",
  "stats": "disabled",
  "name": "TestProxy",
  "wsdl1_1": "http://ThinkPad-X1-Carbon-3rd:8290/services/TestProxy?wsdl",
  "wsdl2_0": "http://ThinkPad-X1-Carbon-3rd:8290/services/TestProxy?wsdl2"
}
```

| Code | Description | Schema |
| ---- | ----------- | ------ |
| 404 | Not Found. Requested Proxy Service doesnot exist |  |

### /tasks

#### GET
##### Summary:

Retrieve Tasks


##### Description:

This operation provides you a list of available Tasks.

Each retrieved Task is represented with a minimal amount of attributes. If you want to get complete details of a Task, you need to use **Get details of a Task** operation.


##### Responses

| Code | Description | Schema |
| ---- | ----------- | ------ |
| 200 | OK. List of Tasks is returned. | [TaskList](#tasklist) |

##### Example

```
curl -X GET "https://localhost:9164/management/tasks" -H "accept: application/json" -k
```

```json
{
  "count": 1,
  "list": [
    {
      "triggerInterval": 5000,
      "name": "InjectXMLTask",
      "triggerType": "simple",
      "triggerCount": 10
    }
  ]
}
```

### /tasks?taskName={taskname}

#### GET
##### Summary:

Get details of a Task


##### Description:

This operation can be used to retrieve details of an individual Task. You need to provide the name of the Task to retrieve it.


##### Parameters

| Name | Located in | Description | Required | Schema |
| ---- | ---------- | ----------- | -------- | ---- |
| taskName | query | Name of the Task | Yes | string |

##### Responses

| Code | Description | Schema |
| ---- | ----------- | ------ |
| 200 | OK. Requested Task is returned. | [TaskInfo](#taskinfo) |

##### Example

```
curl -X GET "https://localhost:9164/management/tasks?taskName=InjectXMLTask" -H "accept: application/json" -k
```

```json
{
  "triggerInterval": 5000,
  "name": "InjectXMLTask",
  "triggerType": "simple",
  "triggerCount": 10
}
```

| Code | Description | Schema |
| ---- | ----------- | ------ |
| 404 | Not Found. Requested Task doesnot exist |  |

### /sequences

#### GET
##### Summary:

Retrieve Sequences


##### Description:

This operation provides you a list of available Sequences.

Each retrieved Sequence is represented with a minimal amount of attributes. If you want to get complete details of a Sequence, you need to use **Get details of a Sequence** operation.


##### Responses

| Code | Description | Schema |
| ---- | ----------- | ------ |
| 200 | OK. List of Sequences is returned. | [SequenceList](#sequencelist) |

##### Example

```
curl -X GET "https://localhost:9164/management/sequences" -H "accept: application/json" -k
```

```json
{
  "count": 1,
  "list": [
    {
      "container": "[ Deployed From Artifact Container: SampleInboundCompositeApplication ]",
      "tracing": "disabled",
      "stats": "disabled",
      "name": "InjectXMLSequence"
    }
  ]
}
```

### /sequences?sequenceName={sequencename}

#### GET
##### Summary:

Get details of a Sequence


##### Description:

This operation can be used to retrieve details of an individual Sequence. You need to provide the name of the Sequence to retrieve it.


##### Parameters

| Name | Located in | Description | Required | Schema |
| ---- | ---------- | ----------- | -------- | ---- |
| sequenceName | query | Name of the Sequence | Yes | string |

##### Responses

| Code | Description | Schema |
| ---- | ----------- | ------ |
| 200 | OK. List of Sequences is returned. | [SequenceInfo](#sequenceinfo) |

##### Example

```
curl -X GET "https://localhost:9164/management/sequences?sequenceName=InjectXMLSequence" -H "accept: application/json" -k
```

```json
{
  "container": "[ Deployed From Artifact Container: SampleInboundCompositeApplication ]",
  "tracing": "disabled",
  "mediators": [
    "LogMediator"
  ],
  "stats": "disabled",
  "name": "InjectXMLSequence"
}
```

| Code | Description | Schema |
| ---- | ----------- | ------ |
| 404 | Not Found. Requested Sequence doesnot exist |  |

### Models


#### Artifacts

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| name | string |  | No |
| type | string |  | No |

#### ApplicationInfo

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| name | string |  | No |
| version | string |  | No |
| artifacts | [ [Artifacts](#artifacts) ] |  | No |

#### ApplicationSummary

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| name | string |  | No |
| version | string |  | No |

#### ApplicationList

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| count | integer | Number of Carbon Applications returned.
 | No |
| list | [ [ApplicationSummary](#applicationsummary) ] |  | No |

#### Resources

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| methods | [ string ] |  | No |
| url | string |  | No |

#### ApiInfo

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| tracing | string |  | No |
| stats | string |  | No |
| name | string |  | No |
| resources | [ [Resources](#resources) ] |  | No |
| version | string |  | No |
| url | string |  | No |

#### ApiSummary

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| name | string |  | No |
| url | string |  | No |

#### ApiList

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| count | integer |  | No |
| list | [ [ApiSummary](#apisummary) ] |  | No |

#### EndpointSummary

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| method | string |  | No |
| name | string |  | No |
| type | string |  | No |
| url | string |  | No |

#### EndpointList

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| count | integer |  | No |
| list | [ [EndpointSummary](#endpointsummary) ] |  | No |

#### EndpointInfo

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| method | string |  | No |
| stats | string |  | No |
| name | string |  | No |
| type | string |  | No |
| url | string |  | No |

#### InboundEndpointSummary

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| name | string |  | No |
| protocol | string |  | No |

#### InboundEndpointList

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| count | integer |  | No |
| list | [ [InboundEndpointSummary](#inboundendpointsummary) ] |  | No |

#### Parameters

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| name | string |  | No |
| value | string |  | No |

#### InboundEndpointInfo

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| protocol | string |  | No |
| tracing | string |  | No |
| stats | string |  | No |
| name | string |  | No |
| parameters | [ [Parameters](#parameters) ] |  | No |

#### ProxySummary

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| name | string |  | No |
| wsdl1_1 | string |  | No |
| wsdl2_0 | string |  | No |

#### ProxyServiceList

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| count | integer |  | No |
| list | [ [ProxySummary](#proxysummary) ] |  | No |

#### ProxyServiceInfo

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| tracing | string |  | No |
| stats | string |  | No |
| name | string |  | No |
| wsdl1_1 | string |  | No |
| wsdl2_0 | string |  | No |

#### TaskSummary

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| triggerInterval | string |  | No |
| name | string |  | No |
| triggerType | string |  | No |
| triggerCount | string |  | No |

#### TaskList

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| count | integer |  | No |
| list | [ [TaskSummary](#tasksummary) ] |  | No |

#### TaskInfo

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| triggerInterval | string |  | No |
| name | string |  | No |
| triggerType | string |  | No |
| triggerCount | string |  | No |

#### SequenceInfo

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| container | string |  | No |
| tracing | string |  | No |
| mediators | [ string ] |  | No |
| stats | string |  | No |
| name | string |  | No |

#### SequenceSummary

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| container | string |  | No |
| tracing | string |  | No |
| stats | string |  | No |
| name | string |  | No |

#### SequenceList

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| count | integer |  | No |
| list | [ [SequenceSummary](#sequencesummary) ] |  | No |