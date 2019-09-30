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
	"os"
	"reflect"
	"testing"
)

// AssertEqual checks if values are equal
func AssertEqual(t *testing.T, expected interface{}, received interface{}) {
	if expected != received {
		// debug.PrintStack()
		t.Errorf("Expected (type %v) \n%v \nReceived (type %v) \n%v", reflect.TypeOf(expected), expected, reflect.TypeOf(received), received)
	}
}

func setupTestCase(t *testing.T) func(t *testing.T) {
	t.Log("setup test case")
	_ = os.Remove(GetServerConfigFilePath())
	InitRemoteConfigData()

	return func(t *testing.T) {
		t.Log("teardown test case")
		_ = os.Remove(GetServerConfigFilePath())
	}
}

func TestAddServer(t *testing.T) {

	teardownTestCase := setupTestCase(t)
	defer teardownTestCase(t)

	err := RemoteConfigData.AddRemote("testServer1", "localhost", "1234")
	if err != nil {
		t.Error("Error adding a server: ", err)
	}

	RemoteConfigData.Persist(GetServerConfigFilePath())
	RemoteConfigData.Load(GetServerConfigFilePath())

	expectedContent :=
		`servers:
  default:
    server_address: localhost
    server_port: "9164"
    access_token: ""
  testServer1:
    server_address: localhost
    server_port: "1234"
    access_token: ""
current_server: default
`
	AssertEqual(t, expectedContent, GetFileContent(GetServerConfigFilePath()))

	expectedURL := "https://localhost:9164/management/"
	AssertEqual(t, expectedURL, GetRESTAPIBase())
}

func TestUpdateServer(t *testing.T) {

	teardownTestCase := setupTestCase(t)
	defer teardownTestCase(t)

	err := RemoteConfigData.AddRemote("testServer1", "localhost", "1234")
	if err != nil {
		t.Error("Error adding a server: ", err)
	}

	err = RemoteConfigData.UpdateRemote("testServer1", "localhost2", "1235")
	if err != nil {
		t.Error("Error updating a server: ", err)
	}

	RemoteConfigData.Persist(GetServerConfigFilePath())
	RemoteConfigData.Load(GetServerConfigFilePath())

	expectedContent :=
		`servers:
  default:
    server_address: localhost
    server_port: "9164"
    access_token: ""
  testServer1:
    server_address: localhost2
    server_port: "1235"
    access_token: ""
current_server: default
`
	AssertEqual(t, expectedContent, GetFileContent(GetServerConfigFilePath()))

	expectedURL := "https://localhost:9164/management/"
	AssertEqual(t, expectedURL, GetRESTAPIBase())
}

func TestRemoveServer(t *testing.T) {

	teardownTestCase := setupTestCase(t)
	defer teardownTestCase(t)

	err := RemoteConfigData.AddRemote("testServer1", "localhost", "1234")
	if err != nil {
		t.Error("Error adding a server: ", err)
	}

	err = RemoteConfigData.AddRemote("testServer2", "localhost22", "1236")
	if err != nil {
		t.Error("Error adding a server: ", err)
	}

	err = RemoteConfigData.RemoveRemote("testServer1")
	if err != nil {
		t.Error("Error removing a server: ", err)
	}

	RemoteConfigData.Persist(GetServerConfigFilePath())
	RemoteConfigData.Load(GetServerConfigFilePath())

	expectedContent :=
		`servers:
  default:
    server_address: localhost
    server_port: "9164"
    access_token: ""
  testServer2:
    server_address: localhost22
    server_port: "1236"
    access_token: ""
current_server: default
`
	AssertEqual(t, expectedContent, GetFileContent(GetServerConfigFilePath()))

	expectedURL := "https://localhost:9164/management/"
	AssertEqual(t, expectedURL, GetRESTAPIBase())
}

func TestRemoveServerError(t *testing.T) {

	teardownTestCase := setupTestCase(t)
	defer teardownTestCase(t)

	err := RemoteConfigData.AddRemote("default", "localhost", "1234")
	if err == nil {
		t.Error("Error: should not allow to remove the default remote: ", err)
	}
}

func TestSelectServer(t *testing.T) {

	teardownTestCase := setupTestCase(t)
	defer teardownTestCase(t)

	err := RemoteConfigData.AddRemote("testServer1", "localhost", "1234")
	if err != nil {
		t.Error("Error adding a server: ", err)
	}

	err = RemoteConfigData.AddRemote("testServer2", "localhost2", "1235")
	if err != nil {
		t.Error("Error adding a server: ", err)
	}

	err = RemoteConfigData.SelectRemote("testServer2")
	if err != nil {
		t.Error("Error selecting a server: ", err)
	}

	RemoteConfigData.Persist(GetServerConfigFilePath())
	RemoteConfigData.Load(GetServerConfigFilePath())

	expectedContent :=
		`servers:
  default:
    server_address: localhost
    server_port: "9164"
    access_token: ""
  testServer1:
    server_address: localhost
    server_port: "1234"
    access_token: ""
  testServer2:
    server_address: localhost2
    server_port: "1235"
    access_token: ""
current_server: testServer2
`
	AssertEqual(t, expectedContent, GetFileContent(GetServerConfigFilePath()))

	expectedURL := "https://localhost2:1235/management/"
	AssertEqual(t, expectedURL, GetRESTAPIBase())
}

func TestDefaultBehavior(t *testing.T) {

	teardownTestCase := setupTestCase(t)
	defer teardownTestCase(t)

	expectedURL := "https://localhost:9164/management/"
	AssertEqual(t, expectedURL, GetRESTAPIBase())
}
