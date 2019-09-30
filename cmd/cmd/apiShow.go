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
	"github.com/spf13/cobra"
	"github.com/wso2/micro-integrator/cmd/utils"
	"github.com/wso2/micro-integrator/cmd/utils/artifactUtils"
)

var apiName string

// Show API command related usage info
const showAPICmdLiteral = "show"
const showAPICmdShortDesc = "Get information about APIs"

const showAPICmdLongDesc = "Get information about the API specified by command line argument [api-name] If not specified, list all the apis\n"

var showAPICmdExamples = "Example:\n" +
	"To get details about a specific api\n" +
	"  " + programName + " " + apiCmdLiteral + " " + showAPICmdLiteral + " TestAPI\n\n" +
	"To list all the apis\n" +
	"  " + programName + " " + apiCmdLiteral + " " + showAPICmdLiteral + "\n\n"

// apiShowCmd represents the show api command
var apiShowCmd = &cobra.Command{
	Use:   showAPICmdLiteral,
	Short: showAPICmdShortDesc,
	Long:  showAPICmdLongDesc + showAPICmdExamples,
	Run: func(cmd *cobra.Command, args []string) {
		handleAPICmdArguments(args)
	},
}

func init() {
	apiCmd.AddCommand(apiShowCmd)
	apiShowCmd.SetHelpTemplate(showAPICmdLongDesc + utils.GetCmdUsage(programName, apiCmdLiteral,
		showAPICmdLiteral, "[api-name]") + showAPICmdExamples + utils.GetCmdFlags(apiCmdLiteral))
}

func handleAPICmdArguments(args []string) {
	utils.Logln(utils.LogPrefixInfo + "Show API called")
	if len(args) == 0 {
		executeListAPIsCmd()
	} else if len(args) == 1 {
		if args[0] == "help" {
			printAPIHelp()
		} else {
			apiName = args[0]
			executeGetAPICmd(apiName)
		}
	} else {
		fmt.Println("Too many arguments. See the usage below")
		printAPIHelp()
	}
}

func printAPIHelp() {
	fmt.Print(showAPICmdLongDesc + utils.GetCmdUsage(programName, apiCmdLiteral, showAPICmdLiteral,
		"[api-name]") + showAPICmdExamples + utils.GetCmdFlags(apiCmdLiteral))
}

func executeGetAPICmd(apiname string) {

	finalUrl, params := utils.GetUrlAndParams(utils.PrefixAPIs, "apiName", apiname)

	resp, err := utils.UnmarshalData(finalUrl, nil, params, &artifactUtils.API{})

	if err == nil {
		// Printing the details of the API
		api := resp.(*artifactUtils.API)
		printAPIInfo(*api)
	} else {
		fmt.Println(utils.LogPrefixError+"Getting Information of the API", err)
	}
}

// Print the details of an API
// Name, Context, Http Method, URL Style
// @param app : API object
func printAPIInfo(api artifactUtils.API) {

	fmt.Println("Name - " + api.Name)
	fmt.Println("Version - " + api.Version)
	fmt.Println("Url - " + api.Url)
	fmt.Println("Stats - " + api.Stats)
	fmt.Println("Tracing - " + api.Tracing)
	fmt.Println("Resources : ")

	table := utils.GetTableWriter()

	data := []string{utils.Url, utils.Method}
	table.Append(data)

	for _, resource := range api.Resources {

		var methodString string

		for i, method := range resource.Methods {
			if i > 0 {
				methodString += "/"
			}
			methodString += method
		}
		data = []string{resource.Url, methodString}
		table.Append(data)
	}
	table.SetAutoMergeCells(true)
	table.Render()
}

func executeListAPIsCmd() {

	finalUrl := utils.GetRESTAPIBase() + utils.PrefixAPIs

	resp, err := utils.UnmarshalData(finalUrl, nil, nil, &artifactUtils.APIList{})

	if err == nil {
		// Printing the list of available APIs
		list := resp.(*artifactUtils.APIList)
		utils.PrintItemList(list, []string{utils.Name, utils.Url}, "No APIs found")
	} else {
		utils.Logln(utils.LogPrefixError+"Getting List of APIs", err)
	}
}
