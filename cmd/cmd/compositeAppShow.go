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
	"github.com/wso2/micro-integrator/cmd/utils/artifactUtils"
	"os"
)

var appName string

// Show Carbon App command related usage info
const showApplicationCmdLiteral = "show"
const showApplicationCmdShortDesc = "Get information about Composite Applications"

const showApplicationCmdLongDesc = "Get information about the Composite App specified by command line argument [app-name] If not specified, list all the composite apps\n"

var showApplicationCmdExamples = "Example:\n" +
	"To get details about a composite app\n" +
	"  " + programName + " " + appCmdLiteral + " " + showApplicationCmdLiteral + " SampleApp\n\n" +
	"To list all the composite apps\n" +
	"  " + programName + " " + appCmdLiteral + " " + showApplicationCmdLiteral + "\n\n"

// carbonAppShowCmd represents the show carbonApp command
var carbonAppShowCmd = &cobra.Command{
	Use:   showApplicationCmdLiteral,
	Short: showApplicationCmdShortDesc,
	Long:  showApplicationCmdLongDesc + showApplicationCmdExamples,
	Run: func(cmd *cobra.Command, args []string) {
		handleApplicationCmdArguments(args)
	},
}

func init() {
	compositeAppCmd.AddCommand(carbonAppShowCmd)
	carbonAppShowCmd.SetHelpTemplate(showApplicationCmdLongDesc + utils.GetCmdUsage(programName, appCmdLiteral,
		showApplicationCmdLiteral, "[app-name]") + showApplicationCmdExamples + utils.GetCmdFlags(appCmdLiteral))
}

func handleApplicationCmdArguments(args []string) {
	utils.Logln(utils.LogPrefixInfo + "Show Carbon app called")
	if len(args) == 0 {
		executeListCarbonAppsCmd()
	} else if len(args) == 1 {
		if args[0] == "help" {
			printAppHelp()
		} else {
			appName = args[0]
			executeGetCarbonAppCmd(appName)
		}
	} else {
		fmt.Println("Too many arguments. See the usage below")
		printAppHelp()
	}
}

func printAppHelp() {
	fmt.Print(showApplicationCmdLongDesc + utils.GetCmdUsage(programName, appCmdLiteral, showApplicationCmdLiteral,
		"[app-name]") + showApplicationCmdExamples + utils.GetCmdFlags(appCmdLiteral))
}

func executeGetCarbonAppCmd(appname string) {

	finalUrl, params := utils.GetUrlAndParams(utils.PrefixCarbonApps, "carbonAppName", appname)

	resp, err := utils.UnmarshalData(finalUrl, nil, params, &artifactUtils.CompositeApp{})

	if err == nil {
		// Printing the details of the Carbon App
		app := resp.(*artifactUtils.CompositeApp)
		printCarbonAppInfo(*app)
	} else {
		fmt.Println(utils.LogPrefixError+"Getting Information of the Carbon App", err)
	}
}

// Print the details of a Carbon app
// Name, Version, and summary about it's artifacts
// @param app : CompositeApp object
func printCarbonAppInfo(app artifactUtils.CompositeApp) {

	fmt.Println("Name - " + app.Name)
	fmt.Println("Version - " + app.Version)
	fmt.Println("Artifacts :")

	table := tablewriter.NewWriter(os.Stdout)
	table.SetAlignment(tablewriter.ALIGN_LEFT)

	data := []string{"NAME", "TYPE"}
	table.Append(data)

	for _, artifact := range app.Artifacts {
		data = []string{artifact.Name, artifact.Type}
		table.Append(data)
	}
	table.SetBorder(false)
	table.SetColumnSeparator(" ")
	table.Render() // Send output
}

func executeListCarbonAppsCmd() {

	finalUrl := utils.GetRESTAPIBase() + utils.PrefixCarbonApps

	resp, err := utils.UnmarshalData(finalUrl, nil, nil, &artifactUtils.CompositeAppList{})

	if err == nil {
		// Printing the list of available Carbon apps
		list := resp.(*artifactUtils.CompositeAppList)
		utils.PrintItemList(list, []string{utils.Name, utils.Version}, "No Composite Apps found")
	} else {
		utils.Logln(utils.LogPrefixError+"Getting List of Carbon apps", err)
	}
}
