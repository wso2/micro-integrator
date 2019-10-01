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

const remoteRemoveCmdLiteral = "remove"
const remoteRemoveCmdShortDesc = "Remove Micro Integrator"
const remoteRemoveCmdLongDesc = "Remove a Micro Integrator which will not be associated with the CLI anymore\n"

var remoteRemoveUsage = dedent.Dedent(`
Usage:
  ` + programName + ` ` + remoteCmdLiteral + ` ` + remoteRemoveCmdLiteral + ` [nick-name]` + `
`)

var remoteRemoveCmdExamples = dedent.Dedent(`
Example:
  ` + programName + ` ` + remoteCmdLiteral + ` ` + remoteRemoveCmdLiteral + ` TestServer` + `
`)

var remoteRemoveCmdHelpString = remoteRemoveCmdLongDesc + remoteRemoveUsage + remoteRemoveCmdExamples

var remoteRemoveCmd = &cobra.Command{
	Use:   remoteRemoveCmdLiteral,
	Short: remoteRemoveCmdShortDesc,
	Long:  remoteRemoveCmdLongDesc + remoteRemoveCmdExamples,
	Run: func(cmd *cobra.Command, args []string) {
		handleServerRemoveCmdArguments(args)
	},
}

func handleServerRemoveCmdArguments(args []string) {
	utils.Logln(utils.LogPrefixInfo + remoteCmdLiteral + " " + remoteRemoveCmdLiteral + " called")
	expectedArgCount := 1
	if len(args) == expectedArgCount {
		if args[0] == "help" {
			printServerRemoveHelp()
		} else {
			executeServerRemoveCmd(args)
		}
	} else {
		if len(args) < expectedArgCount {
			fmt.Println("Error: Please specify the nick-name of Micro Integrator")
		} else {
			fmt.Println("Error: Please specify only the nick-name of Micro Integrator")
		}
		printServerRemoveHelp()
	}
}

func executeServerRemoveCmd(args []string) {
	var result = utils.RemoteConfigData.RemoveRemote(args[0])
	if result != nil {
		utils.HandleErrorAndExit("Error: ", result)
	}
	utils.RemoteConfigData.Persist(utils.GetRemoteConfigFilePath())
}

func printServerRemoveHelp() {
	fmt.Print(remoteRemoveCmdHelpString)
}

func init() {
	remoteCmd.AddCommand(remoteRemoveCmd)
	remoteRemoveCmd.SetHelpTemplate(remoteRemoveCmdHelpString)
}
