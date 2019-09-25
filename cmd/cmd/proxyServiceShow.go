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
	"github.com/wso2/micro-integrator/cmd/utils/artifactUtils"

	"github.com/spf13/cobra"
	"github.com/wso2/micro-integrator/cmd/utils"
)

var proxyServiceName string

// Show ProxyService command related usage info
const showProxyServiceCmdLiteral = "show"
const showProxyServiceCmdShortDesc = "Get information about proxy services"

const showProxyServiceCmdLongDesc = "Get information about the Proxy Service specified by command line argument [proxy-name] If not specified, list all the proxy services\n"

var showProxyServiceCmdExamples = "Example:\n" +
	"To get details about a specific proxy\n" +
	"  " + programName + " " + proxyServiceCmdLiteral + " " + showProxyServiceCmdLiteral + " SampleProxy\n\n" +
	"To list all the proxies\n" +
	"  " + programName + " " + proxyServiceCmdLiteral + " " + showProxyServiceCmdLiteral + "\n\n"

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
	proxyServiceCmd.AddCommand(proxyServiceShowCmd)
	proxyServiceShowCmd.SetHelpTemplate(showProxyServiceCmdLongDesc + utils.GetCmdUsage(programName, proxyServiceCmdLiteral,
		showProxyServiceCmdLiteral, "[proxy-name]") + showProxyServiceCmdExamples + utils.GetCmdFlags(proxyServiceCmdLiteral))
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
	fmt.Print(showProxyServiceCmdLongDesc + utils.GetCmdUsage(programName, proxyServiceCmdLiteral, showProxyServiceCmdLiteral,
		"[proxy-name]") + showProxyServiceCmdExamples + utils.GetCmdFlags(proxyServiceCmdLiteral))
}

func executeGetProxyServiceCmd(proxyServiceName string) {

	finalUrl, params := utils.GetUrlAndParams(utils.PrefixProxyServices, "proxyServiceName", proxyServiceName)

	resp, err := utils.UnmarshalData(finalUrl, nil, params, &artifactUtils.Proxy{})

	if err == nil {
		// Printing the details of the Proxy Service
		proxyService := resp.(*artifactUtils.Proxy)
		printProxyServiceInfo(*proxyService)
	} else {
		fmt.Println(utils.LogPrefixError+"Getting Information of ProxyService", err)
	}
}

// Print the details of a Proxy service
// Name, Description, Sequences(In, Out and Fault), Endpoint
// @param ProxyService : ProxyService object
func printProxyServiceInfo(proxyService artifactUtils.Proxy) {

	fmt.Println("Name - " + proxyService.Name)
	fmt.Println("WSDL 1.1 - " + proxyService.Wsdl11)
	fmt.Println("WSDL 2.0 - " + proxyService.Wsdl20)
	fmt.Println("Stats - " + proxyService.Stats)
	fmt.Println("Tracing - " + proxyService.Tracing)
}

func executeListProxyServicesCmd() {

	finalUrl := utils.GetRESTAPIBase() + utils.PrefixProxyServices

	resp, err := utils.UnmarshalData(finalUrl, nil, nil, &artifactUtils.ProxyServiceList{})

	if err == nil {
		// Printing the list of available Endpoints
		list := resp.(*artifactUtils.ProxyServiceList)
		utils.PrintItemList(list, []string{utils.Name, utils.Wsdl11, utils.Wsdl20}, "No Proxy Services found")
	} else {
		utils.Logln(utils.LogPrefixError+"Getting List of Proxy Services", err)
	}
}
