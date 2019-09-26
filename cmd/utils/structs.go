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

type RemoteConfig struct {
	Remotes       Remotes `yaml:"servers"`
	CurrentServer string  `yaml:"current_server"`
}

type Remotes map[string]Remote

type Remote struct {
	Url  string `yaml:"server_address"`
	Port string `yaml:"server_port"`
}

type Logger struct {
	Name       string `json:"name"`
	ParentName string `json:"parent"`
	LogLevel   string `json:"level"`
}

type Service struct {
	Name        string `json:"name"`
	Description string `json:"description"`
	Type        string `json:"type"`
	Status      string `json:"status"`
	TryItURL    string `json:"tryItUrl"`
}

type ServerSummary struct {
	Name     string `json:"name"`
	Version  string `json:"version"`
	Location string `json:"location"`
}

type RegistrationResponse struct {
	ClientSecretExpiresAt string `xml:"client_secret_expires_at"`
	ClientID              string `xml:"client_id"`
	ClientSecret          string `xml:"client_secret"`
	ClientName            string `xml:"client_name"`
}

type TokenResponse struct {
	AccessToken  string `xml:"access_token"`
	RefreshToken string `xml:"refresh_token"`
	TokenType    string `xml:"token_type"`
	ExpiresIn    int32  `xml:"expires_in"`
}

type IterableStringArray interface {
	GetCount() int32
	GetDataIterator() <-chan []string
}
