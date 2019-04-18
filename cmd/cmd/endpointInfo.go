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
	"github.com/olekukonko/tablewriter"
	"github.com/spf13/cobra"
	"github.com/wso2/micro-integrator/cmd/utils"
	"os"
)

var endpointName string

// Show Endpoint command related usage info
const showEndpointCmdLiteral = "endpoint"
const showEndpointCmdShortDesc = "Get information about the specified Endpoint"

var showEndpointCmdLongDesc = "Get information about the Endpoint specified by the flag --name, -n\n"

var showEndpointCmdExamples = dedent.Dedent(`
Example:
  ` + utils.ProjectName + ` ` + showCmdLiteral + ` ` + showEndpointCmdLiteral + ` -n TestEndpoint
`)

// endpointShowCmd represents the show endpoint command
var endpointShowCmd = &cobra.Command{
	Use:   showEndpointCmdLiteral,
	Short: showEndpointCmdShortDesc,
	Long:  showEndpointCmdLongDesc + showEndpointCmdExamples,
	Run: func(cmd *cobra.Command, args []string) {
		utils.Logln(utils.LogPrefixInfo + "Show endpoint called")
		executeGetEndpointCmd(endpointName)
	},
}

func init() {
	showCmd.AddCommand(endpointShowCmd)

	endpointShowCmd.Flags().StringVarP(&endpointName, "name", "n", "", "Name of the Endpoint")
	endpointShowCmd.MarkFlagRequired("name")
}

func executeGetEndpointCmd(endpointname string) {

	finalUrl := utils.RESTAPIBase + utils.PrefixEndpoints + "?endpointName=" + endpointname

	resp, err := utils.UnmarshalData(finalUrl, &utils.Endpoint{})

	if err == nil {
		// Printing the details of the Endpoint
		endpoint := resp.(*utils.Endpoint)
		printEndpoint(*endpoint)
	} else {
		fmt.Println(utils.LogPrefixError+"Getting Information of Endpoint", err)
	}
}

// printEndpointInfo
// @param Endpoint : Endpoint object
func printEndpoint(endpoint utils.Endpoint) {

	table := tablewriter.NewWriter(os.Stdout)

	d_name := []string{"NAME", endpoint.Name}
	table.Append(d_name)

	d_desc := []string{"DESCRIPTION", endpoint.Description}
	table.Append(d_desc)

	d_container := []string{"CONTAINER", endpoint.ArtifactContainer}
	table.Append(d_container)

	d_type := []string{"ENDPOINT STRING", endpoint.EndpointString}
  table.Append(d_type)
    
	table.SetBorders(tablewriter.Border{Left: true, Top: true, Right: true, Bottom: false})
	table.SetRowLine(true)
	table.Render() // Send output
}
