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

const remoteUpdateCmdLiteral = "update"
const remoteUpdateCmdShortDesc = "Update Micro Integrator hostname and port"
const remoteUpdateCmdLongDesc = "Update a Micro Integrator which will be associated with the CLI\n"

var remoteUpdateUsage = dedent.Dedent(`
Usage:
  ` + programName + ` ` + remoteCmdLiteral + ` ` + remoteUpdateCmdLiteral + ` [nick-name] [host] [port]` + `
`)

var remoteUpdateCmdExamples = dedent.Dedent(`
Example:
  ` + programName + ` ` + remoteCmdLiteral + ` ` + remoteUpdateCmdLiteral + ` TestServer 192.168.1.16 9164` + `
`)

var remoteUpdateCmdHelpString = remoteUpdateCmdLongDesc + remoteUpdateUsage + remoteUpdateCmdExamples

var remoteUpdateCmd = &cobra.Command{
	Use:   remoteUpdateCmdLiteral,
	Short: remoteUpdateCmdShortDesc,
	Long:  remoteUpdateCmdLongDesc + remoteUpdateCmdExamples,
	Run: func(cmd *cobra.Command, args []string) {
		handleServerUpdateCmdArguments(args)
	},
}

func handleServerUpdateCmdArguments(args []string) {
	utils.Logln(utils.LogPrefixInfo + remoteCmdLiteral + " " + remoteUpdateCmdLiteral + " called")
	expectedArgCount := 3
	if len(args) == expectedArgCount {
		if args[0] == "help" {
			printServerUpdateHelp()
		} else {
			executeServerUpdateCmd(args)
		}
	} else {
		if len(args) < expectedArgCount {
			fmt.Println("Error: Please specify hostname and port")
		} else {
			fmt.Println("Error: Please specify only the hostname and port")
		}
		printServerUpdateHelp()
	}
}

func executeServerUpdateCmd(args []string) {
	var err = utils.RemoteConfigData.UpdateRemote(args[0], args[1], args[2])
	if err != nil {
		utils.HandleErrorAndExit("Error: ", err)
	} else {
		utils.Logln(utils.LogPrefixInfo + "Persisting remote " + args[0])
		utils.RemoteConfigData.Persist(utils.GetServerConfigFilePath())
		fmt.Println("Remote " + args[0] + " updated successfully!")
	}
}

func printServerUpdateHelp() {
	fmt.Print(remoteUpdateCmdHelpString)
}

func init() {
	remoteCmd.AddCommand(remoteUpdateCmd)
	remoteUpdateCmd.SetHelpTemplate(remoteUpdateCmdHelpString)
}
