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
	"net/http"
	"net/http/httptest"
	"reflect"
	"strings"
	"testing"
)

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

	resp, err := GetArtifactList(server.URL, &APIList{})
	list := resp.(*APIList)

	if list.Count != 2 {
		t.Errorf("Incorrect count. Exptected %d, got %d\n", 2, list.Count)
	}

	if err != nil {
		t.Error("Error" + err.Error())
	}
}

func TestUnmarshalDataOK(t *testing.T) {
	server := httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		w.WriteHeader(http.StatusOK)
		if r.Method != http.MethodGet {
			t.Errorf("Expected method '%s', got '%s'\n", http.MethodGet, r.Method)
		}
		if !strings.Contains(r.URL.String(), "apiName=HealthcareAPI") {
			t.Errorf("Expected query param '%s', got '%s'\n", "apiName=HealthcareAPI", r.URL.String())
		}
		
		w.Header().Set(HeaderContentType, HeaderValueApplicationJSON)

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
		w.Write([]byte(body))
	}))
	defer server.Close()

	params := make(map[string]string)
	params["apiName"] = "HealthcareAPI"

	resp, err := UnmarshalData(server.URL, params, &API{})
	api := resp.(*API)

	expected := API{
		Tracing: "disabled",
		Stats:   "disabled",
		Name:    "HealthcareAPI",
		Resources: []Resource{
			{
				Methods: []string{"GET"},
				Url:     "/querydoctor/{category}",
			},
		},
		Version: "N/A",
		Url:     "http://172.17.0.1:8290/healthcare",
	}

	if !reflect.DeepEqual(*api, expected) {
		t.Errorf("Unexpected API struct. Exptected %v, got %v\n", expected, *api)
	}

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
	defer server.Close()

	resp, err := UnmarshalData(server.URL, params, &API{})

	if resp != nil {
		t.Error("Response should be nil")
	}

	if err == nil {
		t.Error("Error " + err.Error())
	}
}