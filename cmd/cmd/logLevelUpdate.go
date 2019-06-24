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

var logLevel string

// Show API command related usage info
const updateLogLevelCmdLiteral = "update"
const updateLogLevelCmdShortDesc = "Update log level"

const updateLogLevelCmdLongDesc = "Update log level of the Loggers in Micro Integrator\n"

var updateLogLevelCmdUsage = "Usage:\n" +
	"  " + programName + " " + logLevelCmdLiteral + " " + updateLogLevelCmdLiteral + " [logger-name] [log-level]\n\n"

var updateLogLevelCmdExamples = "Example:\n" +
	"  " + programName + " " + logLevelCmdLiteral + " " + updateLogLevelCmdLiteral + " org.apache.coyote DEBUG\n\n"

var updateLogLevelCmdHelpString = updateLogLevelCmdLongDesc + updateLogLevelCmdUsage + updateLogLevelCmdExamples

// apiupdateCmd represents the update api command
var loggerUpdateCmd = &cobra.Command{
	Use:   updateLogLevelCmdLiteral,
	Short: updateLogLevelCmdShortDesc,
	Long:  updateLogLevelCmdLongDesc + updateLogLevelCmdExamples,
	Run: func(cmd *cobra.Command, args []string) {
		handleUpdateLoggerCmdArguments(args)
	},
}

func init() {
	logLevelCmd.AddCommand(loggerUpdateCmd)
	loggerUpdateCmd.SetHelpTemplate(updateLogLevelCmdHelpString)
}

func handleUpdateLoggerCmdArguments(args []string) {
	utils.Logln(utils.LogPrefixInfo + "Update Logger called")
	if len(args) == 2 {
		if args[0] == "help" || args[1] == "help" {
			printUpdateLoggerHelp()
		} else {
			loggerName = args[0]
			logLevel = args[1]
			executeUpdateLoggerCmd(loggerName, logLevel)
		}
	} else {
		fmt.Println(programName, "log-level update requires 2 argument. See the usage below")
		printUpdateLoggerHelp()
	}
}

func printUpdateLoggerHelp() {
	fmt.Print(updateLogLevelCmdHelpString)
}

func executeUpdateLoggerCmd(loggerName, logLevel string) {
	resp := utils.UpdateMILogger(loggerName, logLevel)
	fmt.Println(resp)
}
