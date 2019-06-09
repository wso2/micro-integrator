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

const remoteAddCmdLiteral = "add"
const remoteAddCmdShortDesc = "Add a Micro Integrator"
const remoteAddCmdLongDesc = "Add a Micro Integrator which will be associated with the CLI\n"

var remoteAddUsage = dedent.Dedent(`
Usage:
  ` + programName + ` ` + remoteCmdLiteral + ` ` + remoteAddCmdLiteral + ` [nick-name] [host] [port]` + `
`)

var remoteAddCmdExamples = dedent.Dedent(`
Example:
  ` + programName + ` ` + remoteCmdLiteral + ` ` + remoteAddCmdLiteral + ` TestServer 192.168.1.15 9164` + `
`)

var remoteAddCmdHelpString = remoteAddCmdLongDesc + remoteAddUsage + remoteAddCmdExamples

var remoteAddCmd = &cobra.Command{
	Use:   remoteAddCmdLiteral,
	Short: remoteAddCmdShortDesc,
	Long:  remoteAddCmdLongDesc + remoteAddCmdExamples,
	Run: func(cmd *cobra.Command, args []string) {
		handleServerAddCmdArguments(args)
	},
}

func handleServerAddCmdArguments(args []string) {
	utils.Logln(utils.LogPrefixInfo + remoteCmdLiteral + " " + remoteAddCmdLiteral + " called")
	expectedArgCount := 3
	if len(args) == expectedArgCount {
		if args[0] == "help" {
			printServerAddHelp()
		} else {
			executeServerAddCmd(args)
		}
	} else {
		if len(args) < expectedArgCount {
			fmt.Println("Error: Please specify hostname and port")
		} else {
			fmt.Println("Error: Please specify only the hostname and port")
		}
		printServerAddHelp()
	}
}

func executeServerAddCmd(args []string) {
	var result = utils.RemoteConfigData.AddRemote(args[0], args[1], args[2])
	if result != nil {
		utils.HandleErrorAndExit("Error: ", result)
	}
	utils.RemoteConfigData.Persist(utils.GetServerConfigFilePath())
}

func printServerAddHelp() {
	fmt.Print(remoteAddCmdHelpString)
}

func init() {
	remoteCmd.AddCommand(remoteAddCmd)
	remoteAddCmd.SetHelpTemplate(remoteAddCmdHelpString)
}
