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
	"github.com/olekukonko/tablewriter"
	"github.com/spf13/cobra"
	"github.com/wso2/micro-integrator/cmd/utils"
	"os"
)

var endpointName string

// Show Endpoint command related usage info
const showEndpointCmdLiteral = "endpoint"
const showEndpointCmdShortDesc = "Get information about endpoints"

var showEndpointCmdLongDesc = "Get information about the endpoint specified by command line argument [endpoint-name] If not specified, list all the endpoints\n"

var showEndpointCmdExamples = "Example:\n" +
	"To get details about a specific endpoint\n" +
	"  " + programName + " " + showCmdLiteral + " " + showEndpointCmdLiteral + " TestEndpoint\n\n" +
	"To list all the endpoints\n" +
	"  " + programName + " " + showCmdLiteral + " " + showEndpointCmdLiteral + "\n\n"

// endpointShowCmd represents the show endpoint command
var endpointShowCmd = &cobra.Command{
	Use:   showEndpointCmdLiteral,
	Short: showEndpointCmdShortDesc,
	Long:  showEndpointCmdLongDesc + showEndpointCmdExamples,
	Run: func(cmd *cobra.Command, args []string) {
		handleEndpointCmdArguments(args)
	},
}

func init() {
	showCmd.AddCommand(endpointShowCmd)
	endpointShowCmd.SetHelpTemplate(showEndpointCmdLongDesc + utils.GetCmdUsage(programName, showCmdLiteral,
		showEndpointCmdLiteral, "[endpoint-name]") + showEndpointCmdExamples + utils.GetCmdFlags("endpoint(s)"))
}

func handleEndpointCmdArguments(args []string) {
	utils.Logln(utils.LogPrefixInfo + "Show Endpoint called")
	if len(args) == 0 {
		executeListEndpointsCmd()
	} else if len(args) == 1 {
		if args[0] == "help" {
			printEndpointHelp()
		} else {
			endpointName = args[0]
			executeGetEndpointCmd(endpointName)
		}
	} else {
		fmt.Println("Too many arguments. See the usage below")
		printEndpointHelp()
	}
}

func printEndpointHelp() {
	fmt.Print(showEndpointCmdLongDesc + utils.GetCmdUsage(programName, showCmdLiteral, showEndpointCmdLiteral,
		"[endpoint-name]") + showEndpointCmdExamples + utils.GetCmdFlags("endpoint(s)"))
}

func executeGetEndpointCmd(endpointname string) {

	finalUrl, params := utils.GetUrlAndParams(utils.PrefixEndpoints, "endpointName", endpointname)

	resp, err := utils.UnmarshalData(finalUrl, params, &utils.Endpoint{})

	if err == nil {
		// Printing the details of the Endpoint
		endpoint := resp.(*utils.Endpoint)
		printEndpoint(*endpoint)
	} else {
		fmt.Println(utils.LogPrefixError+"Getting Information of Endpoint", err)
	}
}

// Print the details of an Endpoint
// Name, Type, Method, Url, Stats
// @param Endpoint : Endpoint object
func printEndpoint(endpoint utils.Endpoint) {

	fmt.Println("Name - " + endpoint.Name)
	fmt.Println("Type - " + endpoint.Type)
	fmt.Println("Method - " + endpoint.Method)
	fmt.Println("Url - " + endpoint.Url)
	fmt.Println("Stats - " + endpoint.Stats)
}

func executeListEndpointsCmd() {

	finalUrl := utils.RESTAPIBase + utils.PrefixEndpoints

	resp, err := utils.GetArtifactList(finalUrl, &utils.EndpointList{})

	if err == nil {
		// Printing the list of available Endpoints
		list := resp.(*utils.EndpointList)
		printEndpointsist(*list)
	} else {
		utils.Logln(utils.LogPrefixError+"Getting List of Endpoints", err)
	}
}

func printEndpointsist(endpointList utils.EndpointList) {

	if endpointList.Count > 0 {
		table := tablewriter.NewWriter(os.Stdout)
		table.SetAlignment(tablewriter.ALIGN_LEFT)

		data := []string{"NAME", "TYPE", "METHOD", "URL"}
		table.Append(data)

		for _, endpoint := range endpointList.Endpoints {
			data = []string{endpoint.Name, endpoint.Type, endpoint.Method, endpoint.Url}
			table.Append(data)
		}
		table.SetBorder(false)
		table.SetColumnSeparator("  ")
		table.Render()
	} else {
		fmt.Println("No Endpoints found")
	}
}
