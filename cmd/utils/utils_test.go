/*
* Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
* WSO2 Inc. licenses this file to you under the Apache License,
* Version 2.0 (the "License"); you may not use this file except
* in compliance with the License.
* You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied. See the License for the
* specific language governing permissions and limitations
* under the License.
 */

package utils

import (
	"github.com/lithammer/dedent"
	"github.com/wso2/micro-integrator/cmd/utils/artifactUtils"
	"net/http"
	"net/http/httptest"
	"reflect"
	"strings"
	"testing"
)

func createServer(t *testing.T, param string, body string) *httptest.Server {

	server := httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		w.WriteHeader(http.StatusOK)
		if r.Method != http.MethodGet {
			t.Errorf("Expected method '%s', got '%s'\n", http.MethodGet, r.Method)
		}
		if !strings.Contains(r.URL.String(), param) {
			t.Errorf("Expected query param '%s', got '%s'\n", param, r.URL.String())
		}

		w.Header().Set(HeaderContentType, HeaderValueApplicationJSON)

		w.Write([]byte(body))
	}))
	return server
}

func compareStruct(t *testing.T, result, expected interface{}) {
	if !reflect.DeepEqual(result, expected) {
		t.Errorf("Unexpected Inbound Endpoint struct.\nExptected:\n%v\nGot\n%v\n", expected, result)
	}
}

func createParamMap(key, value string) map[string]string {
	params := make(map[string]string)
	params[key] = value
	return params
}

func TestInvokeGETRequestUnreachable(t *testing.T) {
	var httpStub = httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		if r.Method != http.MethodGet {
			t.Errorf("Expected 'GET', got '%s'\n", r.Method)
		}
		w.WriteHeader(http.StatusInternalServerError)
	}))
	defer httpStub.Close()

	resp, err := InvokeGETRequest(httpStub.URL, make(map[string]string), nil)
	if resp.StatusCode() != http.StatusInternalServerError {
		t.Errorf("Error in InvokeGETRequest(): %s\n", err)
	}
}

func TestInvokeGETRequestOK(t *testing.T) {
	var httpStub = httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		if r.Method != http.MethodGet {
			t.Errorf("Expected 'GET', got '%s'\n", r.Method)
		}
		w.WriteHeader(http.StatusOK)
	}))
	defer httpStub.Close()

	resp, err := InvokeGETRequest(httpStub.URL, make(map[string]string), nil)
	if resp.StatusCode() != http.StatusOK {
		t.Errorf("Error in InvokeGETRequest(): %s\n", err)
	}
}

func TestGetArtifactListOK(t *testing.T) {
	server := httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		w.WriteHeader(http.StatusOK)
		if r.Method != http.MethodGet {
			t.Errorf("Expected method '%s', got '%s'\n", http.MethodGet, r.Method)
		}
		w.Header().Set(HeaderContentType, HeaderValueApplicationJSON)

		body := dedent.Dedent(`
		{
			"count": 2,
			"list": [{
						"name": "test1",
						"url": "http://172.17.0.1:8290/test1"
					},
					{
						"name": "test2",
						"url": "http://172.17.0.1:8290/test2"
					}
			]
		}`)
		w.Write([]byte(body))
	}))
	defer server.Close()

	resp, err := UnmarshalData(server.URL, nil, nil, &artifactUtils.APIList{})
	list := resp.(*artifactUtils.APIList)

	if list.Count != 2 {
		t.Errorf("Incorrect count. Exptected %d, got %d\n", 2, list.Count)
	}

	if err != nil {
		t.Error("Error" + err.Error())
	}
}

func TestUnmarshalDataApiOK(t *testing.T) {

	body := dedent.Dedent(`
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
	}`)

	server := createServer(t, "apiName=HealthcareAPI", body)
	defer server.Close()

	params := createParamMap("apiName", "HealthcareAPI")

	resp, err := UnmarshalData(server.URL, nil, params, &artifactUtils.API{})
	api := resp.(*artifactUtils.API)

	expected := artifactUtils.API{
		Tracing: "disabled",
		Stats:   "disabled",
		Name:    "HealthcareAPI",
		Resources: []artifactUtils.Resource{
			{
				Methods: []string{"GET"},
				Url:     "/querydoctor/{category}",
			},
		},
		Version: "N/A",
		Url:     "http://172.17.0.1:8290/healthcare",
	}

	compareStruct(t, *api, expected)

	if err != nil {
		t.Error("Error" + err.Error())
	}
}

func TestUnmarshalDataAppOK(t *testing.T) {

	body := dedent.Dedent(`
	{
		"name": "SampleServicesCompositeApplication",
		"version": "1.0.0",
		"artifacts": [
			{
			"name": "HealthcareAPI",
			"type": "api"
			}
		]
	}`)

	server := createServer(t, "carbonAppName=SampleServicesCompositeApplication", body)
	defer server.Close()

	params := createParamMap("carbonAppName", "SampleServicesCompositeApplication")

	resp, err := UnmarshalData(server.URL, nil, params, &artifactUtils.CompositeApp{})
	capp := resp.(*artifactUtils.CompositeApp)

	expected := artifactUtils.CompositeApp{
		Name:    "SampleServicesCompositeApplication",
		Version: "1.0.0",
		Artifacts: []artifactUtils.Artifact{
			{
				Name: "HealthcareAPI",
				Type: "api",
			},
		},
	}

	compareStruct(t, *capp, expected)

	if err != nil {
		t.Error("Error" + err.Error())
	}
}

func TestUnmarshalDataEndpointOK(t *testing.T) {

	body := dedent.Dedent(`
	{
		"method": "POST",
		"stats": "disabled",
		"name": "ClemencyEP",
		"type": "http",
		"url": "http://localhost:9090/clemency/categories/{uri.var.category}/reserve"
	}`)

	server := createServer(t, "endpointName=ClemencyEP", body)
	defer server.Close()

	params := createParamMap("endpointName", "ClemencyEP")

	resp, err := UnmarshalData(server.URL, nil, params, &artifactUtils.Endpoint{})
	endpoint := resp.(*artifactUtils.Endpoint)

	expected := artifactUtils.Endpoint{
		Name:   "ClemencyEP",
		Type:   "http",
		Method: "POST",
		Stats:  "disabled",
		Url:    "http://localhost:9090/clemency/categories/{uri.var.category}/reserve",
	}

	compareStruct(t, *endpoint, expected)

	if err != nil {
		t.Error("Error" + err.Error())
	}
}

func TestUnmarshalDataInboundEndpointOK(t *testing.T) {

	body := dedent.Dedent(`
	{
		"protocol": "http",
		"tracing": "disabled",
		"stats": "disabled",
		"name": "TestInbound",
		"parameters": [
			{
				"name": "inbound.http.port",
				"value": "8000"
			}
		]
	}`)

	server := createServer(t, "inboundEndpointName=TestInbound", body)
	defer server.Close()

	params := createParamMap("inboundEndpointName", "TestInbound")

	resp, err := UnmarshalData(server.URL, nil, params, &artifactUtils.InboundEndpoint{})
	inboundEndpoint := resp.(*artifactUtils.InboundEndpoint)

	expected := artifactUtils.InboundEndpoint{
		Name:    "TestInbound",
		Type:    "http",
		Tracing: "disabled",
		Stats:   "disabled",
		Parameters: []artifactUtils.Parameter{
			{
				Name:  "inbound.http.port",
				Value: "8000",
			},
		},
	}

	compareStruct(t, *inboundEndpoint, expected)

	if err != nil {
		t.Error("Error" + err.Error())
	}
}

func TestUnmarshalDataProxyOK(t *testing.T) {

	body := dedent.Dedent(`
	{
		"tracing": "disabled",
		"stats": "disabled",
		"name": "TestProxy",
		"wsdl1_1": "http://ThinkPad-X1-Carbon-3rd:8290/services/TestProxy?wsdl",
		"wsdl2_0": "http://ThinkPad-X1-Carbon-3rd:8290/services/TestProxy?wsdl2"
	}`)

	server := createServer(t, "proxyServiceName=TestProxy", body)
	defer server.Close()

	params := createParamMap("proxyServiceName", "TestProxy")

	resp, err := UnmarshalData(server.URL, nil, params, &artifactUtils.Proxy{})
	proxy := resp.(*artifactUtils.Proxy)

	expected := artifactUtils.Proxy{
		Tracing: "disabled",
		Stats:   "disabled",
		Name:    "TestProxy",
		Wsdl11:  "http://ThinkPad-X1-Carbon-3rd:8290/services/TestProxy?wsdl",
		Wsdl20:  "http://ThinkPad-X1-Carbon-3rd:8290/services/TestProxy?wsdl2",
	}

	compareStruct(t, *proxy, expected)

	if err != nil {
		t.Error("Error" + err.Error())
	}
}

func TestUnmarshalDataSequenceOK(t *testing.T) {

	body := dedent.Dedent(`
	{
		"container": "[ Deployed From Artifact Container: SampleInboundCompositeApplication ]",
		"tracing": "disabled",
		"mediators": [
			"LogMediator"
		],
		"stats": "disabled",
		"name": "InjectXMLSequence"
	}`)

	server := createServer(t, "sequenceName=InjectXMLSequence", body)
	defer server.Close()

	params := createParamMap("sequenceName", "InjectXMLSequence")

	resp, err := UnmarshalData(server.URL, nil, params, &artifactUtils.Sequence{})
	sequence := resp.(*artifactUtils.Sequence)

	expected := artifactUtils.Sequence{
		Container: "[ Deployed From Artifact Container: SampleInboundCompositeApplication ]",
		Tracing:   "disabled",
		Mediators: []string{
			"LogMediator",
		},
		Stats: "disabled",
		Name:  "InjectXMLSequence",
	}

	compareStruct(t, *sequence, expected)

	if err != nil {
		t.Error("Error" + err.Error())
	}
}

func TestUnmarshalDataTaskOK(t *testing.T) {

	body := dedent.Dedent(`
	{
		"triggerInterval": "5000",
		"name": "InjectXMLTask",
		"triggerType": "simple",
		"triggerCount": "10"
	}`)

	server := createServer(t, "taskName=InjectXMLTask", body)
	defer server.Close()

	params := createParamMap("taskName", "InjectXMLTask")

	resp, err := UnmarshalData(server.URL, nil, params, &artifactUtils.Task{})
	sequence := resp.(*artifactUtils.Task)

	expected := artifactUtils.Task{
		TriggerInterval: "5000",
		Name:            "InjectXMLTask",
		Type:            "simple",
		TriggerCount:    "10",
	}

	compareStruct(t, *sequence, expected)

	if err != nil {
		t.Error("Error" + err.Error())
	}
}

func TestUnmarshalDataNotFound(t *testing.T) {
	server := httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		w.WriteHeader(http.StatusNotFound)
		if r.Method != http.MethodGet {
			t.Errorf("Expected method '%s', got '%s'\n", http.MethodGet, r.Method)
		}
	}))
	defer server.Close()

	params := make(map[string]string)
	params["apiName"] = "ABC"

	resp, err := UnmarshalData(server.URL, nil, params, &artifactUtils.API{})

	if resp != nil {
		t.Error("Response should be nil")
	}

	if err == nil {
		t.Error("Error " + err.Error())
	}
}

func TestUnmarshalDataBadRequest(t *testing.T) {
	server := httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		w.WriteHeader(http.StatusNotFound)
		if r.Method != http.MethodGet {
			t.Errorf("Expected method '%s', got '%s'\n", http.MethodGet, r.Method)
		}
		body := dedent.Dedent(`
		{
			"Error": "Invalid log level abc"
		}`)
		w.Write([]byte(body))
	}))
	defer server.Close()

	resp, err := UnmarshalData(server.URL, nil, nil, &Logger{})

	if resp == nil {
		t.Error(`Response should be, "Error": "Invalid log level abc"`)
	}

	if err == nil {
		t.Error("Error " + err.Error())
	}
}
