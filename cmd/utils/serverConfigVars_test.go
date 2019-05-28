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
	"testing"
	"strings"
)

func TestSetConfigVarsWithoutFile(t *testing.T) {
	err := SetConfigVars(testServerConfigFilePath)
	if err != nil {
		t.Errorf("Error in configuration file " + testServerConfigFilePath)
	}
	if strings.Compare(RESTAPIBase, DefaultRESTAPIBase) != 0 {
		t.Errorf("Expected url '%s', got '%s'\n", DefaultRESTAPIBase, RESTAPIBase)
	}
}

func TestSetConfigVarsWithFile(t *testing.T) {
	writeServerConfig()
	err := SetConfigVars(testServerConfigFilePath)
	if err != nil {
		t.Errorf("Error in configuration file " + testServerConfigFilePath)
	}
	var url = "https://localhost:9797/management/"
	if strings.Compare(RESTAPIBase, url) != 0 {
		t.Errorf("Expected url '%s', got '%s'\n", url, RESTAPIBase)
	}
	defer removeConfigFile(t)
}