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
)

// Check whether the file exists.
func IsFileExist(path string) bool {
	if _, err := os.Stat(path); err != nil {
		if os.IsNotExist(err) {
			return false
		} else {
			HandleErrorAndExit("Unable to find file "+path, err)
		}
	}
	return true
}

func GetServerConfigFilePath() string {

	userHomeDir, err := os.UserHomeDir()
	if err != nil {
		HandleErrorAndExit("Error getting user home directory: ", err)
	}
	serverConfigFilePath := filepath.Join(userHomeDir, ServerConfigFileName)
	return serverConfigFilePath
}

func GetFileContent(filePath string) string {
	data, err := ioutil.ReadFile(filePath)
	if err != nil {
		HandleErrorAndExit("Error reading: "+filePath, err)
	}

	return string(data)
}
