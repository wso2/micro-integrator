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
	"github.com/lithammer/dedent"
	"github.com/spf13/cobra"
	"github.com/wso2/micro-integrator/cmd/utils"
)

const remoteSelectCmdLiteral = "select"
const remoteSelectCmdShortDesc = "Select Micro Integrator for next operations"
const remoteSelectCmdLongDesc = "Select a Micro Integrator which will be associated with the CLI for next operations\n"

var remoteSelectUsage = dedent.Dedent(`
Usage:
  ` + programName + ` ` + remoteCmdLiteral + ` ` + remoteSelectCmdLiteral + ` [nick-name]` + `
`)

var remoteSelectCmdExamples = dedent.Dedent(`
Example:
  ` + programName + ` ` + remoteCmdLiteral + ` ` + remoteSelectCmdLiteral + ` TestServer` + `
`)

var remoteSelectCmdHelpString = remoteSelectCmdLongDesc + remoteSelectUsage + remoteSelectCmdExamples

var remoteSelectCmd = &cobra.Command{
	Use:   remoteSelectCmdLiteral,
	Short: remoteSelectCmdShortDesc,
	Long:  remoteSelectCmdLongDesc + remoteSelectCmdExamples,
	Run: func(cmd *cobra.Command, args []string) {
		handleServerSelectCmdArguments(args)
	},
}

func handleServerSelectCmdArguments(args []string) {
	utils.Logln(utils.LogPrefixInfo + remoteCmdLiteral + " " + remoteSelectCmdLiteral + " called")
	expectedArgCount := 1
	if len(args) == expectedArgCount {
		if args[0] == "help" {
			printServerSelectHelp()
		} else {
			executeServerSelectCmd(args)
		}
	} else {
		if len(args) < expectedArgCount {
			fmt.Println("Error: Please specify the nick-name of Micro Integrator")
		} else {
			fmt.Println("Error: Please specify only the nick-name of Micro Integrator")
		}
		printServerSelectHelp()
	}
}

func executeServerSelectCmd(args []string) {
	var result = utils.RemoteConfigData.SelectRemote(args[0])
	if result != nil {
		utils.HandleErrorAndExit("Error: ", result)
	}
	fmt.Println("Selected remote: " + args[0])
	utils.RemoteConfigData.Persist(utils.GetServerConfigFilePath())
}

func printServerSelectHelp() {
	fmt.Print(remoteSelectCmdHelpString)
}

func init() {
	remoteCmd.AddCommand(remoteSelectCmd)
	remoteSelectCmd.SetHelpTemplate(remoteSelectCmdHelpString)
}
