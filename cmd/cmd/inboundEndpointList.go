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

package cmd

import (
	"fmt"
	"github.com/lithammer/dedent"
	"github.com/spf13/cobra"
	"github.com/wso2/micro-integrator/cmd/utils"
)

// List Inbound Endpoints command related usage info
const listInboundEndpointsCmdLiteral = "inboundEndpoints"
const listInboundEndpointsCmdShortDesc = "List all the Inbound Endpoints"

var listInboundEndpointsCmdLongDesc = "List all the Inbound Endpoints\n"

var listInboundEndpointsCmdExamples = dedent.Dedent(`
Example:
  ` + utils.ProjectName + ` ` + listCmdLiteral + ` ` + listInboundEndpointsCmdLiteral)

// inboundEndpointsListCmd represents the list inboundEndpoints command
var inboundEndpointsListCmd = &cobra.Command{
	Use:   listInboundEndpointsCmdLiteral,
	Short: listInboundEndpointsCmdShortDesc,
	Long:  listInboundEndpointsCmdLongDesc + listInboundEndpointsCmdExamples,
	Run: func(cmd *cobra.Command, args []string) {
		utils.Logln(utils.LogPrefixInfo + "List inbound endpoints called")
		executeListInboundEndpointsCmd()
	},
}

func init() {
	listCmd.AddCommand(inboundEndpointsListCmd)
}

func executeListInboundEndpointsCmd() {

	finalUrl := utils.RESTAPIBase + utils.PrefixInboundEndpoints

	count, inboundEndpoints, err := utils.GetArtifactList(finalUrl)

	if err == nil {
		// Printing the list of available Inbound endpoints
		fmt.Println("No. of Inbound Endpoints:", count)
		if count > 0 {
			utils.PrintList(inboundEndpoints)
		}
	} else {
		utils.Logln(utils.LogPrefixError+"Getting List of Inbound Endpoints", err)
	}
}
