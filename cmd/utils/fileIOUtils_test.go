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
	"io/ioutil"
	"os"
	"path/filepath"
	"testing"
)

const testServerConfigFileName = "test_server_config.yaml"

var testServerConfigFilePath = filepath.Join(CurrentDir, testServerConfigFileName)

var serverConfig = new(ServerConfig)

func initSampleServerConfig() {
	serverConfig.Url = "https://localhost"
	serverConfig.Port = "9797"
}

func writeServerConfig() {
	initSampleServerConfig()
	WriteServerConfigFile(serverConfig, testServerConfigFilePath)
}

func removeConfigFile(t *testing.T) {
	var err = os.Remove(testServerConfigFilePath)
	if err != nil {
		t.Errorf("Error deleting file " + testServerConfigFilePath)
	}
}

func TestWriteServerConfigFile(t *testing.T) {
	writeServerConfig()
	removeConfigFile(t)
}

func TestGetServerConfigFromFile(t *testing.T) {
	writeServerConfig()
	serverConfigReturned := GetServerConfigFromFile(testServerConfigFilePath)
	if serverConfigReturned.Url != serverConfig.Url ||
		serverConfigReturned.Port != serverConfig.Port {
		t.Errorf("Error in GetServerConfigFromFile()")
	}
	removeConfigFile(t)
}

// test case 1 - correct server config file
func TestParseServerConfigFromFile1(t *testing.T) {
	var config ServerConfig
	writeServerConfig()
	data, err := ioutil.ReadFile(testServerConfigFilePath)
	if err != nil {
		t.Error("Error")
	}
	config.ParseServerConfigFromFile(data)
	removeConfigFile(t)
}

// test case 2 - incorrect server config file (blank url)
func TestParseServerConfigFromFile2(t *testing.T) {

	serverConfig := new(ServerConfig)

	serverConfig.Url = ""
	serverConfig.Port = "9797"
	WriteServerConfigFile(serverConfig, testServerConfigFilePath)

	data, _ := ioutil.ReadFile(testServerConfigFilePath)

	err := serverConfig.ParseServerConfigFromFile(data)
	if err == nil {
		t.Errorf("Expected '%s', got '%s' instead\n", "error", err)
	}

	defer os.Remove(testServerConfigFilePath)
}

// test case 3 - incorrect server config file (blank port)
func TestParseServerConfigFromFile3(t *testing.T) {

	serverConfig := new(ServerConfig)

	serverConfig.Url = "https://localhost"
	serverConfig.Port = ""
	WriteServerConfigFile(serverConfig, testServerConfigFilePath)

	data, _ := ioutil.ReadFile(testServerConfigFilePath)

	err := serverConfig.ParseServerConfigFromFile(data)
	if err == nil {
		t.Errorf("Expected '%s', got '%s' instead\n", "error", err)
	}

	defer os.Remove(testServerConfigFilePath)
}

// test case 1 - for a file that does not exist
func TestIsFileExist1(t *testing.T) {
	isFileExist := IsFileExist("random-string")
	if isFileExist {
		t.Errorf("Expected '%t' for a file that does not exist, got '%t' instead\n", false, true)
	}
}

// test for a file that does exist
func TestIsFileExist2(t *testing.T) {
	isFileExist := IsFileExist("./fileIOUtils.go")
	if !isFileExist {
		t.Errorf("Expected '%t' for a file that does exist,  got '%t' instead\n", true, false)
	}
}
