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

var inboundEndpointName string

// Show InboundEndpoint command related usage info
const showInboundEndpointCmdLiteral = "inboundendpoint"
const showInboundEndpointCmdShortDesc = "Get information about inbound endpoints"

var showInboundEndpointCmdLongDesc = "Get information about the InboundEndpoint specified by command line argument [inbound-name] If not specified, list all the Inbound Endpoints\n"

var showInboundEndpointCmdExamples = "Example:\n" +
	"To get details about a specific inbound endpoint\n" +
	"  " + programName + " " + showCmdLiteral + " " + showInboundEndpointCmdLiteral + " TestInboundEndpoint\n\n" +
	"To list all the inbound endpoints\n" +
	"  " + programName + " " + showCmdLiteral + " " + showInboundEndpointCmdLiteral + "\n\n"

// InboundEndpointShowCmd represents the Show inboundEndpoint command
var inboundEndpointShowCmd = &cobra.Command{
	Use:   showInboundEndpointCmdLiteral,
	Short: showInboundEndpointCmdShortDesc,
	Long:  showInboundEndpointCmdLongDesc + showInboundEndpointCmdExamples,
	Run: func(cmd *cobra.Command, args []string) {
		handleInboundCmdArguments(args)
	},
}

func init() {
	showCmd.AddCommand(inboundEndpointShowCmd)
	inboundEndpointShowCmd.SetHelpTemplate(showInboundEndpointCmdLongDesc + utils.GetCmdUsage(programName, showCmdLiteral,
		showInboundEndpointCmdLiteral, "[inbound-name]") + showInboundEndpointCmdExamples + utils.GetCmdFlags("inboundendpoint(s)"))
}

func handleInboundCmdArguments(args []string) {
	utils.Logln(utils.LogPrefixInfo + "Show InboundEndpoint called")
	if len(args) == 0 {
		executeListInboundEndpointsCmd()
	} else if len(args) == 1 {
		if args[0] == "help" {
			printInboundHelp()
		} else {
			inboundEndpointName = args[0]
			executeGetInboundEndpointCmd(inboundEndpointName)
		}
	} else {
		fmt.Println("Too many arguments. See usage below")
		printInboundHelp()
	}
}

func printInboundHelp() {
	fmt.Print(showInboundEndpointCmdLongDesc + utils.GetCmdUsage(programName, showCmdLiteral, showInboundEndpointCmdLiteral,
		"[inbound-name]") + showInboundEndpointCmdExamples + utils.GetCmdFlags("inboundendpoint(s)"))
}

func executeGetInboundEndpointCmd(inboundEndpointname string) {

	finalUrl, params := utils.GetUrlAndParams(utils.PrefixInboundEndpoints, "inboundEndpointName", inboundEndpointname)

	resp, err := utils.UnmarshalData(finalUrl, params, &utils.InboundEndpoint{})

	if err == nil {
		// Printing the details of the InboundEndpoint
		inboundEndpoint := resp.(*utils.InboundEndpoint)
		printInboundEndpoint(*inboundEndpoint)
	} else {
		fmt.Println(utils.LogPrefixError+"Getting Information of InboundEndpoint", err)
	}
}

// Print the details of an Inbound endpoint
// Name, Protocol and a list of parameters
// @param InboundEndpoint : InboundEndpoint object
func printInboundEndpoint(inbound utils.InboundEndpoint) {

	fmt.Println("Name - " + inbound.Name)
	fmt.Println("Type - " + inbound.Type)
	fmt.Println("Stats - " + inbound.Stats)
	fmt.Println("Tracing - " + inbound.Tracing)
	fmt.Println("Parameters : ")

	table := tablewriter.NewWriter(os.Stdout)
	table.SetAlignment(tablewriter.ALIGN_LEFT)

	data := []string{"NAME", "VALUE"}
	table.Append(data)

	for _, param := range inbound.Parameters {
		data = []string{param.Name, param.Value}
		table.Append(data)
	}
	table.SetBorder(false)
	table.SetColumnSeparator(" ")
	table.SetAutoMergeCells(true)
	table.Render()
}

func executeListInboundEndpointsCmd() {
	finalUrl := utils.RESTAPIBase + utils.PrefixInboundEndpoints

	resp, err := utils.GetArtifactList(finalUrl, &utils.InboundEndpointList{})

	if err == nil {
		// Printing the list of available Inbound endpoints
		list := resp.(*utils.InboundEndpointList)
		printInboundList(*list)
	} else {
		utils.Logln(utils.LogPrefixError+"Getting List of Inbound Endpoints", err)
	}
}

func printInboundList(inboundList utils.InboundEndpointList) {

	if inboundList.Count > 0 {
		table := tablewriter.NewWriter(os.Stdout)
		table.SetAlignment(tablewriter.ALIGN_LEFT)

		data := []string{"NAME", "TYPE"}
		table.Append(data)

		for _, api := range inboundList.InboundEndpoints {
			data = []string{api.Name, api.Type}
			table.Append(data)
		}
		table.SetBorder(false)
		table.SetColumnSeparator("  ")
		table.Render()
	} else {
		fmt.Println("No Inbound Endpoints found")
	}
}
