/*
* Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
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

var localEntryName string

// Show LocalEntry command related usage info
const showLocalEntryCmdShortDesc = "Get information about localentries"
const showLocalEntryCmdLongDesc = "Get information about the localentry specified by command line argument " +
	"[localentry-name] If not specified, list all the localentries\n"

var showLocalEntryCmdExamples = "Example:\n" +
	"To get details about a specific localentry\n" +
	"  " + programName + " " + localEntryCmdLiteral + " " + utils.ShowCommand + " TestLocalEntry\n\n" +
	"To list all the localentries\n" +
	"  " + programName + " " + localEntryCmdLiteral + " " + utils.ShowCommand + "\n\n"

// localEntryShowCmd represents the show localentry command
var localEntryShowCmd = &cobra.Command{
	Use:   utils.ShowCommand,
	Short: showLocalEntryCmdShortDesc,
	Long:  showLocalEntryCmdLongDesc + showLocalEntryCmdExamples,
	Run: func(cmd *cobra.Command, args []string) {
		handleLocalEntryCmdArguments(args)
	},
}

func init() {
	localEntryCmd.AddCommand(localEntryShowCmd)
	localEntryShowCmd.SetHelpTemplate(showLocalEntryCmdLongDesc +
		utils.GetCmdUsage(programName, localEntryCmdLiteral,
			utils.ShowCommand, "[localentry-name]") + showLocalEntryCmdExamples +
		utils.GetCmdFlags(localEntryCmdLiteral))
}

// localentry argument handling method
func handleLocalEntryCmdArguments(args []string) {
	utils.Logln(utils.LogPrefixInfo + "Show Local Entries called")
	if len(args) == 0 {
		executeListLocalEntryCmd()
	} else if len(args) == 1 {
		if args[0] == utils.HelpCommand {
			printLocalEntryHelp()
		} else {
			localEntryName = args[0]
			executeGetLocalEntryCmd(localEntryName)
		}
	} else {
		fmt.Println("Too many arguments. See the usage below")
		printLocalEntryHelp()
	}
}

func printLocalEntryHelp() {
	fmt.Print(showLocalEntryCmdLongDesc + utils.GetCmdUsage(programName, localEntryCmdLiteral, utils.ShowCommand,
		"[localentry-name]") + showLocalEntryCmdExamples + utils.GetCmdFlags(localEntryCmdLiteral))
}

func executeGetLocalEntryCmd(localEntryName string) {
	finalUrl, params := utils.GetUrlAndParams(utils.PrefixLocalEntries, "name", localEntryName)
	resp, err := utils.UnmarshalData(finalUrl, nil, params, &artifactUtils.LocalEntryData{})

	if err == nil {
		// Printing the details of the LocalEntry
		localEntry := resp.(*artifactUtils.LocalEntryData)
		printLocalEntry(*localEntry)
	} else {
		fmt.Println(utils.LogPrefixError+"Getting Information of Local Entry", err)
	}
}

func printLocalEntry(localEntry artifactUtils.LocalEntryData) {
	fmt.Println("Name - " + localEntry.Name)
	fmt.Println("Type - " + localEntry.Type)
	fmt.Println("Value - " + localEntry.Value)
}

func executeListLocalEntryCmd() {
	finalUrl := utils.GetRESTAPIBase() + utils.PrefixLocalEntries
	resp, err := utils.UnmarshalData(finalUrl, nil, nil, &artifactUtils.LocalEntryList{})

	if err == nil {
		// Printing the list of available Local Entries
		list := resp.(*artifactUtils.LocalEntryList)
		utils.PrintItemList(list, []string{utils.Name, utils.Type}, "No Local Entries found")
	} else {
		utils.Logln(utils.LogPrefixError+"Getting List of Message Stores", err)
	}
}
