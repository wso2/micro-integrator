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

type CarbonAppList struct {
	Count      int32              `json:"count"`
	CarbonApps []CarbonAppSummary `json:"list"`
}

type CarbonAppSummary struct {
	Name    string `json:"name"`
	Version string `json:"version"`
}

type CarbonApp struct {
	Name      string     `json:"name"`
	Version   string     `json:"version"`
	Artifacts []Artifact `json:"artifacts"`
}

type Artifact struct {
	Name string `json:"name"`
	Type string `json:"type"`
}

type EndpointList struct {
	Count     int32             `json:"count"`
	Endpoints []EndpointSummary `json:"list"`
}

type EndpointSummary struct {
	Name   string `json:"name"`
	Type   string `json:"type"`
	Method string `json:"method"`
	Url    string `json:"url"`
}

type Endpoint struct {
	Name   string `json:"name"`
	Type   string `json:"type"`
	Method string `json:"method"`
	Url    string `json:"url"`
	Stats  string `json:"stats"`
}

type InboundEndpointList struct {
	Count            int32                    `json:"count"`
	InboundEndpoints []InboundEndpointSummary `json:"list"`
}

type InboundEndpointSummary struct {
	Name string `json:"name"`
	Type string `json:"protocol"`
}

type InboundEndpoint struct {
	Name       string      `json:"name"`
	Type       string      `json:"protocol"`
	Stats      string      `json:"stats"`
	Tracing    string      `json:"tracing"`
	Parameters []Parameter `json:"parameters"`
}

type Parameter struct {
	Name  string `json:"name"`
	Value string `json:"value"`
}

type API struct {
	Name      string     `json:"name"`
	Url       string     `json:"url"`
	Version   string     `json:"version"`
	Stats     string     `json:"stats"`
	Tracing   string     `json:"tracing"`
	Resources []Resource `json:"resources"`
}

type APIList struct {
	Count int32        `json:"count"`
	Apis  []APISummary `json:"list"`
}

type APISummary struct {
	Name string `json:"name"`
	Url  string `json:"url"`
}

type Resource struct {
	Methods []string `json:"methods"`
	Url     string   `json:"url"`
}

type ProxyServiceList struct {
	Count   int32          `json:"count"`
	Proxies []ProxySummary `json:"list"`
}

func (data *ProxyServiceList) GetDataIterator() <-chan []string {
	ch := make(chan []string)

	go func() {
		for _, proxy := range data.Proxies {
			ch <- []string{proxy.Name, proxy.WSDL1_1, proxy.WSDL2_0}
		}
	}()

	return ch
}

func (data *ProxyServiceList) GetCount() int32 {
	return data.Count
}

type ProxySummary struct {
	Name    string `json:"name"`
	WSDL1_1 string `json:"wsdl1_1"`
	WSDL2_0 string `json:"wsdl2_0"`
}

type Proxy struct {
	Name    string `json:"name"`
	WSDL1_1 string `json:"wsdl1_1"`
	WSDL2_0 string `json:"wsdl2_0"`
	Stats   string `json:"stats"`
	Tracing string `json:"tracing"`
}

type Service struct {
	Name        string `json:"name"`
	Description string `json:"description"`
	Type        string `json:"type"`
	Status      string `json:"status"`
	TryItURL    string `json:"tryItUrl"`
}

type SequenceList struct {
	Count     int32             `json:"count"`
	Sequences []SequenceSummary `json:"list"`
}

type SequenceSummary struct {
	Name      string `json:"name"`
	Container string `json:"container"`
	Stats     string `json:"stats"`
	Tracing   string `json:"tracing"`
}

type Sequence struct {
	Name      string   `json:"name"`
	Container string   `json:"container"`
	Stats     string   `json:"stats"`
	Tracing   string   `json:"tracing"`
	Mediators []string `json:"mediators"`
}

type TaskList struct {
	Count int32  `json:"count"`
	Tasks []Task `json:"list"`
}

type Task struct {
	Name            string `json:"name"`
	Type            string `json:"triggerType"`
	TriggerCount    string `json:"triggerCount"`
	TriggerInterval string `json:"triggerInterval"`
	TriggerCron     string `json:"triggerCron"`
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

type DataServicesList struct {
	Count int32                `json:"count"`
	List  []DataServiceSummary `json:"list"`
}

func (data *DataServicesList) GetDataIterator() <-chan []string {
	ch := make(chan []string)

	go func() {
		for _, val := range data.List {
			ch <- []string{val.ServiceName, val.Wsdl11, val.Wsdl20}
		}
	}()

	return ch
}

func (data *DataServicesList) GetCount() int32 {
	return data.Count
}

type IterableStringArray interface {
	GetCount() int32
	GetDataIterator() <-chan []string
}

type DataServiceInfo struct {
	ServiceName        string         `json:"serviceName"`
	ServiceDescription string         `json:"serviceDescription"`
	ServiceGroupName   string         `json:"serviceGroupName"`
	Wsdl11             string         `json:"wsdl1_1"`
	Wsdl20             string         `json:"wsdl2_0"`
	Queries            []QuerySummary `json:"queries"`
}

type DataServiceSummary struct {
	ServiceName string `json:"name"`
	Wsdl11      string `json:"wsdl1_1"`
	Wsdl20      string `json:"wsdl2_0"`
}

type QuerySummary struct {
	Id        string `json:"id"`
	Namespace string `json:"namespace"`
}
