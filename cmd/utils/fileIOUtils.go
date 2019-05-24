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
	"errors"
	"gopkg.in/yaml.v2"
	"io/ioutil"
	"os"
)

// Write Server configuration to the yaml file
// @param c : data
// @param serverConfigFilePath : Path to file where env endpoints are stored
func WriteServerConfigFile(c interface{}, envConfigFilePath string) {
	data, err := yaml.Marshal(&c)
	if err != nil {
		HandleErrorAndExit("Unable to write configuration to file.", err)
	}

	err = ioutil.WriteFile(envConfigFilePath, data, 0644)
	if err != nil {
		HandleErrorAndExit("Unable to write configuration to file.", err)
	}
}

// Read and return Server Configuration from the yaml file
func GetServerConfigFromFile(filePath string) *ServerConfig {
	data, err := ioutil.ReadFile(filePath)
	if err != nil {
		HandleErrorAndExit("ServerConfig: File Not Found: "+filePath, err)
	}

	var serverConfig ServerConfig
	if err := serverConfig.ParseServerConfigFromFile(data); err != nil {
		HandleErrorAndExit("ServerConfig: Error parsing "+filePath, err)
	}

	return &serverConfig
}

// Read and validate contents of server_config.yaml
// will throw errors if the any of the lines is blank
func (serverConfig *ServerConfig) ParseServerConfigFromFile(data []byte) error {

	if err := yaml.Unmarshal(data, serverConfig); err != nil {
		return err
	}

	if serverConfig.Url == "" {
		return errors.New("Blank Host")
	}
	if serverConfig.Port == "" {
		return errors.New("Blank Port")
	}

	return nil
}

// Check whether the file exists.
func IsFileExist(path string) (isFileExist bool) {
	if _, err := os.Stat(path); err != nil {
		if os.IsNotExist(err) {
			return false
		} else {
			HandleErrorAndExit("Unable to find file "+path, err)
		}
	}
	return true
}
