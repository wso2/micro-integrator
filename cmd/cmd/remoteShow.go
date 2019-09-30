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

const remoteShowCmdLiteral = "show"
const remoteShowCmdShortDesc = "Show currently available Micro Integrators"
const remoteShowCmdLongDesc = "Show currently available Micro Integrators which can be associated with the CLI for next operations\n"

var remoteShowUsage = dedent.Dedent(`
Usage:
  ` + programName + ` ` + remoteCmdLiteral + ` ` + remoteShowCmdLiteral + `
`)

var remoteShowCmdExamples = dedent.Dedent(`
Example:
  ` + programName + ` ` + remoteCmdLiteral + ` ` + remoteShowCmdLiteral + `
`)

var remoteShowCmdHelpString = remoteShowCmdLongDesc + remoteShowUsage + remoteShowCmdExamples

var remoteShowCmd = &cobra.Command{
	Use:   remoteShowCmdLiteral,
	Short: remoteShowCmdShortDesc,
	Long:  remoteShowCmdLongDesc + remoteShowCmdExamples,
	Run: func(cmd *cobra.Command, args []string) {
		handleRemoteShowCmdArguments(args)
	},
}

func handleRemoteShowCmdArguments(args []string) {
	utils.Logln(utils.LogPrefixInfo + remoteCmdLiteral + " " + remoteShowCmdLiteral + " called")
	if len(args) == 0 {
		executeRemoteShowCmd(args)
	} else {
		fmt.Println("Incorrect number of arguments. See the usage below")
		printRemoteShowHelp()
	}
}

func executeRemoteShowCmd(args []string) {
	if utils.IsFileExist(utils.GetRemoteConfigFilePath()) {
		fmt.Print(utils.GetFileContent(utils.GetRemoteConfigFilePath()))
	} else {
		message := utils.RemoteConfigFileName + ` file does not exist. Please run "` +
			programName + ` ` + remoteCmdLiteral + ` ` + remoteAddCmdLiteral +
			`" to add MI configs.`
		fmt.Println(message)
	}

}

func printRemoteShowHelp() {
	fmt.Print(remoteShowCmdHelpString)
}

func init() {
	remoteCmd.AddCommand(remoteShowCmd)
	remoteShowCmd.SetHelpTemplate(remoteShowCmdHelpString)
}
