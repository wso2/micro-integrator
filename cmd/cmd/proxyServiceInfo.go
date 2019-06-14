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

var proxyServiceName string

// Show ProxyService command related usage info
const showProxyServiceCmdLiteral = "proxyservice"
const showProxyServiceCmdShortDesc = "Get information about proxy services"

var showProxyServiceCmdLongDesc = "Get information about the Proxy Service specified by command line argument [proxy-name] If not specified, list all the proxy services\n"

var showProxyServiceCmdExamples = "Example:\n" +
	"To get details about a specific prxoy\n" +
	"  " + programName + " " + showCmdLiteral + " " + showProxyServiceCmdLiteral + " SampleProxy\n\n" +
	"To list all the proxies\n" +
	"  " + programName + " " + showCmdLiteral + " " + showProxyServiceCmdLiteral + "\n\n"

// proxyServiceShowCmd represents the proxyService command
var proxyServiceShowCmd = &cobra.Command{
	Use:   showProxyServiceCmdLiteral,
	Short: showProxyServiceCmdShortDesc,
	Long:  showProxyServiceCmdLongDesc + showProxyServiceCmdExamples,
	Run: func(cmd *cobra.Command, args []string) {
		handleProxyServiceCmdArguments(args)
	},
}

func init() {
	showCmd.AddCommand(proxyServiceShowCmd)
	proxyServiceShowCmd.SetHelpTemplate(showProxyServiceCmdLongDesc + utils.GetCmdUsage(programName, showCmdLiteral,
		showProxyServiceCmdLiteral, "[proxy-name]") + showProxyServiceCmdExamples + utils.GetCmdFlags("proxyservice(s)"))
}

func handleProxyServiceCmdArguments(args []string) {
	utils.Logln(utils.LogPrefixInfo + "Show ProxyService called")
	if len(args) == 0 {
		executeListProxyServicesCmd()
	} else if len(args) == 1 {
		if args[0] == "help" {
			printProxyServiceHelp()
		} else {
			proxyServiceName = args[0]
			executeGetProxyServiceCmd(proxyServiceName)
		}
	} else {
		fmt.Println("Too many arguments. See the usage below")
		printProxyServiceHelp()
	}
}

func printProxyServiceHelp() {
	fmt.Print(showProxyServiceCmdLongDesc + utils.GetCmdUsage(programName, showCmdLiteral, showProxyServiceCmdLiteral,
		"[proxy-name]") + showProxyServiceCmdExamples + utils.GetCmdFlags("proxyservice(s)"))
}

func executeGetProxyServiceCmd(proxyServiceName string) {

	finalUrl, params := utils.GetUrlAndParams(utils.PrefixProxyServices, "proxyServiceName", proxyServiceName)

	resp, err := utils.UnmarshalData(finalUrl, params, &utils.Proxy{})

	if err == nil {
		// Printing the details of the Proxy Service
		proxyService := resp.(*utils.Proxy)
		printProxyServiceInfo(*proxyService)
	} else {
		fmt.Println(utils.LogPrefixError+"Getting Information of ProxyService", err)
	}
}

// Print the details of a Proxy service
// Name, Description, Sequences(In, Out and Fault), Endpoint
// @param ProxyService : ProxyService object
func printProxyServiceInfo(proxyService utils.Proxy) {

	fmt.Println("Name - " + proxyService.Name)
	fmt.Println("WSDL 1.1 - " + proxyService.WSDL1_1)
	fmt.Println("WSDL 2.0 - " + proxyService.WSDL2_0)
	fmt.Println("Stats - " + proxyService.Stats)
	fmt.Println("Tracing - " + proxyService.Tracing)
}

func executeListProxyServicesCmd() {

	finalUrl := utils.GetRESTAPIBase() + utils.PrefixProxyServices

	resp, err := utils.GetArtifactList(finalUrl, &utils.ProxyServiceList{})

	if err == nil {
		// Printing the list of available Endpoints
		list := resp.(*utils.ProxyServiceList)
		printProxyList(*list)
	} else {
		utils.Logln(utils.LogPrefixError+"Getting List of Proxies", err)
	}
}

func printProxyList(proxyList utils.ProxyServiceList) {

	if proxyList.Count > 0 {
		table := tablewriter.NewWriter(os.Stdout)
		table.SetAlignment(tablewriter.ALIGN_LEFT)

		data := []string{"NAME", "WSDL 1.1", "WSDL 2.0"}
		table.Append(data)

		for _, proxy := range proxyList.Proxies {
			data = []string{proxy.Name, proxy.WSDL1_1, proxy.WSDL2_0}
			table.Append(data)
		}
		table.SetBorder(false)
		table.SetColumnSeparator("  ")
		table.Render()
	} else {
		fmt.Println("No proxies found")
	}
}
