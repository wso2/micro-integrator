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

// List Carbon App command related usage info
const listApplicationCmdLiteral = "carbonApps"
const listApplicationCmdShortDesc = "List all the Carbon Applications"

var listApplicationCmdLongDesc = "List all the Carbon Applications\n"

var listApplicationCmdExamples = dedent.Dedent(`
Example:
  ` + utils.ProjectName + ` ` + listCmdLiteral + ` ` + listApplicationCmdLiteral)

// carbonAppsListCmd represents the list carbonApps command
var carbonAppsListCmd = &cobra.Command{
	Use:   listApplicationCmdLiteral,
	Short: listApplicationCmdShortDesc,
	Long:  listApplicationCmdLongDesc + listApplicationCmdExamples,
	Run: func(cmd *cobra.Command, args []string) {
		utils.Logln(utils.LogPrefixInfo + "List Carbon apps called")
		executeListCarbonAppsCmd()
	},
}

func init() {
	listCmd.AddCommand(carbonAppsListCmd)
}

func executeListCarbonAppsCmd() {

	finalUrl := utils.RESTAPIBase + utils.PrefixCarbonApps

	count, carbonApps, err := utils.GetArtifactList(finalUrl)

	if err == nil {
		// Printing the list of available Carbon Apps
		fmt.Println("No. of Carbon Apps:", count)
		if count > 0 {
			utils.PrintList(carbonApps)
		}
	} else {
		utils.Logln(utils.LogPrefixError+"Getting List of Carbon Apps", err)
	}
}
