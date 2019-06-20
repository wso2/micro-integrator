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
	"github.com/spf13/cobra"
	"github.com/wso2/micro-integrator/cmd/utils"
)

const showDataServicesCmdLiteral = "dataservices"
const showDataServicesCmdShortDesc = "Show all available data services"
const showDataServicesCmdLongDesc = "Show a summary of all available data services deployed in the Micro Integrator"

// dataServiceListCmd represents the dataServiceList command
var dataServiceListCmd = &cobra.Command{
	Use:   showDataServicesCmdLiteral,
	Short: showDataServicesCmdShortDesc,
	Long:  showDataServicesCmdLongDesc,
	Run: func(cmd *cobra.Command, args []string) {
		utils.Logln(utils.LogPrefixInfo + showDataServicesCmdLiteral + " called")
		executeDataServiceListCmd()
	},
}

func init() {
	showCmd.AddCommand(dataServiceListCmd)
}

func executeDataServiceListCmd() {
	finalURL := utils.RESTAPIBase + utils.PrefixDataServices

	resp, err := utils.UnmarshalData(finalURL, nil, &utils.DataServicesList{})

	if err == nil {
		// print the list of available data services
		list := resp.(*utils.DataServicesList)
		utils.PrintItemList(list, []string{"NAME", "WSDL 1.1", "WSDL 2.0"}, "No dataservices found")
	} else {
		utils.Logln(utils.LogPrefixError+"Getting List of Dataservices", err)
	}
}
