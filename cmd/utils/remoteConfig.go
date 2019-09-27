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
)

var RemoteConfigData RemoteConfig

func init() {

	InitRemoteConfigData()
}

func (remoteConfig *RemoteConfig) AddRemote(name string, host string, port string) error {

	remotes := &RemoteConfigData.Remotes
	if _, exists := (*remotes)[name]; exists {
		return errors.New("remote already added")
	}

	remote := Remote{Url: host, Port: port}
	(*remotes)[name] = remote

	return nil
}

func (remoteConfig *RemoteConfig) UpdateRemote(name string, host string, port string) error {

	remotes := &RemoteConfigData.Remotes
	if _, exists := (*remotes)[name]; !exists {
		return errors.New("no such remote: " + name)
	}

	remote := Remote{Url: host, Port: port}
	(*remotes)[name] = remote

	return nil
}

// update the access token of the current remote
func (remoteConfig *RemoteConfig) UpdateCurrentRemoteToken(accessToken string) error {
	currentRemote := RemoteConfigData.Remotes[RemoteConfigData.CurrentServer]

	remotes := &RemoteConfigData.Remotes

	remote := Remote{Url: currentRemote.Url, Port: currentRemote.Port, AccessToken: accessToken}
	(*remotes)[RemoteConfigData.CurrentServer] = remote

	return nil
}

func (remoteConfig *RemoteConfig) RemoveRemote(name string) error {

	if name == DefaultRemoteName {
		return errors.New("cannot remove default remote")
	}
	remotes := &RemoteConfigData.Remotes
	if _, exists := (*remotes)[name]; !exists {
		return errors.New("no such remote")
	}

	delete(*remotes, name)

	if remoteConfig.CurrentServer == name {
		remoteConfig.CurrentServer = DefaultRemoteName
	}

	return nil
}

func (remoteConfig *RemoteConfig) SelectRemote(name string) error {

	remotes := &RemoteConfigData.Remotes
	if _, exists := (*remotes)[name]; !exists {
		return errors.New("no such remote")
	}

	remoteConfig.CurrentServer = name

	return nil
}

func (remoteConfig *RemoteConfig) Load(filePath string) {

	Logln(LogPrefixInfo + "RemoteConfig: Reading config file: " + filePath)

	remoteConfig.Reset()

	data, err := ioutil.ReadFile(filePath)
	if err != nil {
		HandleErrorAndExit("RemoteConfig: Error reading: "+filePath, err)
	}

	err = yaml.Unmarshal(data, remoteConfig)
	if err != nil {
		HandleErrorAndExit("RemoteConfig: Error unmarshal: "+filePath, err)
	}
}

func (remoteConfig *RemoteConfig) Persist(filePath string) {

	Logln(LogPrefixInfo + "RemoteConfig: Writing config file: " + filePath)

	data, err := yaml.Marshal(remoteConfig)
	if err != nil {
		HandleErrorAndExit("RemoteConfig: Error marshal: "+filePath, err)
	}

	err = ioutil.WriteFile(filePath, data, 0644)
	if err != nil {
		HandleErrorAndExit("RemoteConfig: Error writing: "+filePath, err)
	}
}

func (remoteConfig *RemoteConfig) Reset() {
	RemoteConfigData = RemoteConfig{}
	RemoteConfigData.Remotes = make(map[string]Remote)
	RemoteConfigData.CurrentServer = ""
}
