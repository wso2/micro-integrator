/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
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
	"github.com/spf13/cobra"
	"github.com/wso2/micro-integrator/cmd/utils"
	"github.com/wso2/micro-integrator/cmd/utils/artifactUtils"
)

var dataServiceNameInput string

const showDataServiceCmdLiteral = "show"
const showDataServiceCmdShortDesc = "Show infomation about data services"
const showDataServiceCmdLongDesc = "Get information about the Data service specified by command line argument [dataservice-name] If not specified, list all the data services\n"

var showDataServiceCmdExmaples = "Example:\n" +
	"To get details about a specific data-service\n" +
	"  " + programName + " " + dataServicesCmdLiteral + " " + showDataServiceCmdLiteral + " SampleDataService\n\n" +
	"To list all the proxies\n" +
	"  " + programName + " " + dataServicesCmdLiteral + " " + showDataServiceCmdLiteral + "\n\n"

// dataServiceInfoCmd represents the dataServiceInfo command
var dataServiceInfoCmd = &cobra.Command{
	Use:   showDataServiceCmdLiteral,
	Short: showDataServiceCmdShortDesc,
	Long:  showDataServiceCmdLongDesc,
	Run: func(cmd *cobra.Command, args []string) {
		utils.Logln(utils.LogPrefixInfo + showDataServiceCmdLiteral + " called")
		handleDataServiceCmdArguments(args)
	},
}

func init() {
	dataServiceCmd.AddCommand(dataServiceInfoCmd)
}

func handleDataServiceCmdArguments(args []string) {
	if len(args) == 0 {
		executeDataServiceListCmd()
	} else if len(args) == 1 {
		if args[0] == "help" {
			printShowDataServiceHelp()
		} else {
			dataServiceNameInput = args[0]
			executeGetDataServiceCmd(dataServiceNameInput)
		}
	} else {
		fmt.Println("Too many arguments. See the usage below")
		printShowDataServiceHelp()
	}
}

func executeDataServiceListCmd() {
	finalURL := utils.GetRESTAPIBase() + utils.PrefixDataServices

	resp, err := utils.UnmarshalData(finalURL, nil, nil, &artifactUtils.DataServicesList{})

	if err == nil {
		// print the list of available data services
		list := resp.(*artifactUtils.DataServicesList)
		utils.PrintItemList(list, []string{utils.Name, utils.Wsdl11, utils.Wsdl20}, "No dataservices found")
	} else {
		utils.Logln(utils.LogPrefixError+"Getting List of Dataservices", err)
	}
}

func executeGetDataServiceCmd(dataServiceName string) {
	finalUrl, params := utils.GetUrlAndParams(utils.PrefixDataServices, "dataServiceName", dataServiceName)
	resp, err := utils.UnmarshalData(finalUrl, nil, params, &artifactUtils.DataServiceInfo{})

	if err == nil {
		// printing the details of the Data Service
		dataService := resp.(*artifactUtils.DataServiceInfo)
		printDataServiceInfo(*dataService)
	} else {
		fmt.Println("Error: " + err.Error())
		utils.Logln(utils.LogPrefixError + "Error in receiving data-service '" + dataServiceName + "'")
	}
}

func printShowDataServiceHelp() {
	fmt.Println(showProxyServiceCmdLongDesc + utils.GetCmdUsage(programName, dataServicesCmdLiteral, showDataServiceCmdLiteral,
		"[data-service-name]") + showDataServiceCmdExmaples + utils.GetCmdFlags(dataServicesCmdLiteral))
}

func printDataServiceInfo(dataServiceInfo artifactUtils.DataServiceInfo) {
	fmt.Println("Name - " + dataServiceInfo.ServiceName)
	fmt.Println("Group Name - " + dataServiceInfo.ServiceGroupName)
	fmt.Println("Description - " + dataServiceInfo.ServiceDescription)
	fmt.Println("WSDL 1.1 - " + dataServiceInfo.Wsdl11)
	fmt.Println("WSDL 2.0 - " + dataServiceInfo.Wsdl20)
	querySummaries := dataServiceInfo.Queries
	if len(querySummaries) > 0 {
		fmt.Println("Queries - ")
		table := utils.GetTableWriter()

		data := []string{"ID", "NAMESPACE"}
		table.Append(data)

		for _, querySummary := range querySummaries {
			data = []string{querySummary.Id, querySummary.Namespace}
			table.Append(data)
		}
		table.Render()
	} else {
		fmt.Println("No queries found")
	}
}
