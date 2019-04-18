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

package cmd

import (
	"fmt"
	"github.com/lithammer/dedent"
	"github.com/spf13/cobra"
	"github.com/wso2/micro-integrator/cmd/utils"
)

// List Endpoints command related usage info
const listEndpointsCmdLiteral = "endpoints"
const listEndpointsCmdShortDesc = "List all the Endpoints"

var listEndpointsCmdLongDesc = "List all the Endpoints\n"

var listEndpointsCmdExamples = dedent.Dedent(`
Example:
  ` + utils.ProjectName + ` ` + listCmdLiteral + ` ` + listEndpointsCmdLiteral)

// endpointsListCmd represents the list endpoints command
var endpointsListCmd = &cobra.Command{
	Use:   listEndpointsCmdLiteral,
	Short: listEndpointsCmdShortDesc,
	Long:  listEndpointsCmdLongDesc + listEndpointsCmdExamples,
	Run: func(cmd *cobra.Command, args []string) {
		utils.Logln(utils.LogPrefixInfo + "List endpoints called")
		executeListEndpointsCmd()
	},
}

func init() {
	listCmd.AddCommand(endpointsListCmd)
}

func executeListEndpointsCmd() {

	finalUrl := utils.RESTAPIBase + utils.PrefixEndpoints

	count, endpoints, err := utils.GetArtifactList(finalUrl)

	if err == nil {
		// Printing the list of available Endpoints
		fmt.Println("No. of Endpoints:", count)
		if count > 0 {
			utils.PrintList(endpoints)
		}
	} else {
		utils.Logln(utils.LogPrefixError+"Getting List of Endpoints", err)
	}
}
