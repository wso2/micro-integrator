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
)

var loggerName string

// Show Log-level command related usage info
const showLogLevelCmdLiteral = "show"
const showLogLevelCmdShortDesc = "Get information about a Logger"

const showLogLevelCmdLongDesc = "Get information about the Logger specified by command line argument [logger-name]\n"

var showLogLevelCmdUsage = "Usage:\n" +
	"  " + programName + " " + logLevelCmdLiteral + " " + showLogLevelCmdLiteral + " [logger-name]\n\n"

var showLogLevelCmdExamples = "Example:\n" +
	"To get details about a specific logger\n" +
	"  " + programName + " " + logLevelCmdLiteral + " " + showLogLevelCmdLiteral + " org.apache.coyote\n\n"

// loggerShowCmd represents the show logger command
var loggerShowCmd = &cobra.Command{
	Use:   showLogLevelCmdLiteral,
	Short: showLogLevelCmdShortDesc,
	Long:  showLogLevelCmdLongDesc + showLogLevelCmdExamples,
	Run: func(cmd *cobra.Command, args []string) {
		handleShowLoggerCmdArguments(args)
	},
}

func init() {
	logLevelCmd.AddCommand(loggerShowCmd)
	loggerShowCmd.SetHelpTemplate(showLogLevelCmdLongDesc + showLogLevelCmdUsage + showLogLevelCmdExamples +
		utils.GetCmdFlags(logLevelCmdLiteral))
}

func handleShowLoggerCmdArguments(args []string) {
	utils.Logln(utils.LogPrefixInfo + "Show Logger called")
	if len(args) == 1 {
		if args[0] == "help" {
			printLoggerHelp()
		} else {
			loggerName = args[0]
			executeGetLoggerCmd(loggerName)
		}
	} else {
		fmt.Println(programName, "log-level show requires 1 argument. See the usage below")
		printLoggerHelp()
	}
}

func printLoggerHelp() {
	fmt.Print(showLogLevelCmdLongDesc + showLogLevelCmdUsage + showLogLevelCmdExamples + utils.GetCmdFlags(logLevelCmdLiteral))
}

func executeGetLoggerCmd(loggerName string) {

	finalUrl, params := utils.GetUrlAndParams(utils.PrefixLogging, "loggerName", loggerName)

	resp, err := utils.UnmarshalData(finalUrl, nil, params, &utils.Logger{})

	if err == nil {
		// Printing the details of the Logger
		logger := resp.(*utils.Logger)
		printLoggerInfo(*logger)
	} else {
		if resp == nil {
			fmt.Println(utils.LogPrefixError+"Getting Information of the Logger", err)
		} else {
			fmt.Println(utils.LogPrefixError+"Getting Information of the Logger:", resp.(string))
		}
	}
}

// Print the details of a Logger
// Name, Parent and loglevel
// @param logger : Logger object
func printLoggerInfo(logger utils.Logger) {
	fmt.Println("Name - " + logger.Name)
	fmt.Println("LogLevel - " + logger.LogLevel)
	fmt.Println("Parent - " + logger.ParentName)
}
