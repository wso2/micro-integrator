/*
*  Copyright (c) WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/

package utils

type CarbonApp struct {	
	Name            string 		`xml:"Name"`	
	Version         string 		`xml:"Version"`	
	Artifacts       []Artifact 	`xml:"Artifacts>Artifact"`
}

type Artifact struct {
	Name            string 		`xml:"Name"`
	Type            string 		`xml:"Type"`
}

type Endpoint struct {	
	Name            	string `xml:"Name"`
	ArtifactContainer   string `xml:"Container"`
	Description         string `xml:"Description"`
	EndpointString      string `xml:"EndpointString"`
}

type InboundEndpoint struct {	
	Name            	string `xml:"Name"`
	Class   			string `xml:"Class"`
	Protocol         	string `xml:"Protocol"`
	Sequence       		string `xml:"Sequence"`
	ErrorSequence       string `xml:"ErrorSequence"`
	Parameters        	[]Parameter `xml:"Parameters>Parameter"`	
}

type Parameter struct {
	Name			string `xml:"Name"`
	Value			string `xml:"Value"`
}

type API struct {	
	Name            string `xml:"Name"`
	Context         string `xml:"Context"`
	Resources       []Resource `xml:"Resources>Resource"`
}

type Resource struct {	
	Methods         []string `xml:"Methods>Item"`
	Style			string `xml:"Style"`
	Template        string `xml:"Template"`
	Mapping       	string `xml:"Mapping"`
	InSequence      string `xml:"Inseq"`
	OutSequence     string `xml:"Outseq"`
	FaultSequence   string `xml:"Faultseq"`
}

type ProxyService struct {
	Name			string `xml:"Name"`
	Description     string `xml:"Description"`
	InSequence      string `xml:"InSequence"`
	OutSequence     string `xml:"OutSequence"`
	FaultSequence   string `xml:"FaultSequence"`
	Endpoint		string `xml:"Endpoint"`
	Transports		[]string `xml:"Transports>Value"`
}

type Service struct {
	Name            string `xml:"Name"`
	Description     string `xml:"Description"`
	Type        	string `xml:"Type"`
	Status        	string `xml:"Status"`
	TryItURL		string `xml:"TryItUrl"`
}

type Sequence struct {
	Name            string `xml:"Name"`
	Container		string `xml:"Container"`
	Mediators       []string `xml:"Mediators>Mediator"`
}

type Task struct {
	Name			string 	`xml:"Name"`
	Class			string 	`xml:"Class"`
	Group			string 	`xml:"Group"`
	Type			string 	`xml:"Type"`
	TriggerCount	string 	`xml:"TriggerCount"`
	TriggerInterval	string 	`xml:"TriggerInterval"`
	TriggerCron		string 	`xml:"TriggerCron"`
}

type ServerSummary struct {
	Name			string 	`xml:"Name"`
	Version			string 	`xml:"Version"`
	Location		string 	`xml:"Location"`
}

type ListResponse struct {
	Count int32 	`xml:"Count"`
	List  []string 	`xml:"List>Item"`
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
