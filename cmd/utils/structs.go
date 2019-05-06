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

type ServerConfig struct {
    Host string `yaml:"server_address"`
    Port string `yaml:"server_port"`
}

type CarbonAppList struct {
    Count       int32                   `xml:"Count"`
    CarbonApps  []CarbonAppSummary      `xml:"List>Application"`
}

type CarbonAppSummary struct {
    Name      string     `xml:"Name"`
    Version   string     `xml:"Version"`
}

type CarbonApp struct {
    Name      string     `xml:"Name"`
    Version   string     `xml:"Version"`
    Artifacts []Artifact `xml:"Artifacts>Artifact"`
}

type Artifact struct {
    Name string `xml:"Name"`
    Type string `xml:"Type"`
}

type EndpointList struct {
    Count       int32               `xml:"Count"`
    Endpoints  []EndpointSummary    `xml:"List>Endpoint"`
}

type EndpointSummary struct {
    Name      string     `xml:"Name"`
    Type      string     `xml:"Type"`
    Method    string     `xml:"Method"`
    Url       string     `xml:"Url"`
}

type Endpoint struct {
    Name      string     `xml:"Name"`
    Type      string     `xml:"Type"`
    Method    string     `xml:"Method"`
    Url       string     `xml:"Url"`
    Stats     string     `xml:"Stats"`
}

type InboundEndpointList struct {
    Count               int32                       `xml:"Count"`
    InboundEndpoints    []InboundEndpointSummary    `xml:"List>InboundEndpoint"`
}

type InboundEndpointSummary struct {
    Name      string     `xml:"Name"`
    Type      string     `xml:"Protocol"`
}

type InboundEndpoint struct {
    Name          string      `xml:"Name"`
    Type          string      `xml:"Protocol"`
    Stats         string      `xml:"Stats"`
    Tracing       string      `xml:"Tracing"`
    Parameters    []Parameter `xml:"Parameters>Parameter"`
}

type Parameter struct {
    Name  string `xml:"Name"`
    Value string `xml:"Value"`
}

type API struct {
    Name      string     `xml:"Name"`
    Context   string     `xml:"Context"`
    // Host      string     `xml:"Host"`
    // Port      string     `xml:"Port"`
    Version   string     `xml:"Version"`
    Stats     string     `xml:"Stats"`
    Tracing   string     `xml:"Tracing"`
    Resources []Resource `xml:"Resources>Resource"`
}

type APIList struct {
    Count     int32         `xml:"Count"`
    Apis      []APISummary  `xml:"List>API"`
}

type APISummary struct {
    Name      string     `xml:"Name"`
    Context   string     `xml:"Context"`
}

type Resource struct {
    Methods       []string  `xml:"Methods>Item"`
    Url             string  `xml:"Url"`
}

type ProxyServiceList struct {
    Count           int32             `xml:"Count"`
    Proxies         []ProxySummary    `xml:"List>Proxy"`
}

type ProxySummary struct {
    Name      string     `xml:"Name"`
    WSDL1_1   string     `xml:"WSDL1_1"`
    WSDL2_0   string     `xml:"WSDL2_0"`
}

type Proxy struct {
    Name      string     `xml:"Name"`
    WSDL1_1   string     `xml:"WSDL1_1"`
    WSDL2_0   string     `xml:"WSDL2_0"`
    Stats     string     `xml:"Stats"`
    Tracing   string     `xml:"Tracing"`
}

type Service struct {
    Name        string `xml:"Name"`
    Description string `xml:"Description"`
    Type        string `xml:"Type"`
    Status      string `xml:"Status"`
    TryItURL    string `xml:"TryItUrl"`
}

type SequenceList struct {
    Count       int32               `xml:"Count"`
    Sequences  []SequenceSummary    `xml:"List>Sequence"`
}

type SequenceSummary struct {
    Name        string      `xml:"Name"`
    Container   string      `xml:"Container"`
    Stats       string      `xml:"Stats"`
    Tracing     string      `xml:"Tracing"`
}

type Sequence struct {
    Name      string   `xml:"Name"`
    Container string   `xml:"Container"`
    Stats     string   `xml:"Stats"`
    Tracing   string   `xml:"Tracing"`
    Mediators []string `xml:"Mediators>Mediator"`
}

type TaskList struct {
    Count       int32     `xml:"Count"`
    Tasks       []Task    `xml:"List>Task"`
}

type Task struct {
    Name            string `xml:"Name"`
    Type            string `xml:"TriggerType"`
    TriggerCount    string `xml:"TriggerCount"`
    TriggerInterval string `xml:"TriggerInterval"`
    TriggerCron     string `xml:"TriggerCron"`
}

type ServerSummary struct {
    Name     string `xml:"Name"`
    Version  string `xml:"Version"`
    Location string `xml:"Location"`
}

type ListResponse struct {
    Count int32    `xml:"Count"`
    List  []string `xml:"List>Item"`
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
