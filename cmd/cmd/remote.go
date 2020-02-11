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

const remoteCmdLiteral = "remote"
const remoteCmdShortDesc = "Add, login to, logout of, remove, update or select Micro Integrator"
const remoteCmdLongDesc = "Add, login to, logout of, remove, update or select Micro Integrator which will be associated with the CLI\n"

var remoteUsage = dedent.Dedent(`
Usage
  ` + programName + ` ` + remoteCmdLiteral + ` [command] [arguments]

Available Commands:
  add [nick-name] [host] [port]            Add a Micro Integrator
  remove [nick-name]                       Remove a Micro Integrator
  update [nick-name] [host] [port]         Update a Micro Integrator
  select [nick-name]                       Select a Micro Integrator on which commands are executed
  show                                     Show available Micro Integrators
  login                                    Login to the selected Micro Integrator
  logout                                   Logout of the current Micro Integrator instance
`)

var remoteCmdExamples = dedent.Dedent(`
Examples:
To add a Micro Integrator
  ` + programName + ` ` + remoteCmdLiteral + ` ` + remoteAddCmdLiteral + ` TestServer 192.168.1.15 9164` + `
To remove a Micro Integrator
  ` + programName + ` ` + remoteCmdLiteral + ` ` + remoteRemoveCmdLiteral + ` TestServer ` + `
To update hostname of a Micro Integrator
  ` + programName + ` ` + remoteCmdLiteral + ` ` + remoteUpdateCmdLiteral + ` TestServer 192.168.1.17 9164 ` + `
To select the current Micro Integrator
  ` + programName + ` ` + remoteCmdLiteral + ` ` + remoteSelectCmdLiteral + ` TestServer ` + `
To show available Micro Integrators
  ` + programName + ` ` + remoteCmdLiteral + ` ` + remoteShowCmdLiteral + `
To login to the current Micro Integrator instance
  ` + programName + ` ` + remoteCmdLiteral + ` ` + loginCmdLiteral + `
To logout of the current Micro Integrator instance
  ` + programName + ` ` + remoteCmdLiteral + ` ` + logoutCmdLiteral + `
`)

var remoteCmdValidArgs = []string{"add", "remove", "update", "select", "show", "login", "logout"}

var remoteCmd = &cobra.Command{
	Use:   "remote [command]",
	Short: remoteCmdShortDesc,
	Long:  remoteCmdLongDesc,
	Run: func(cmd *cobra.Command, args []string) {
		fmt.Print(remoteCmdLongDesc + remoteUsage + utils.GetCmdFlags("remote") + remoteCmdExamples)
	},
	ValidArgs: remoteCmdValidArgs,
}

func init() {
	RootCmd.AddCommand(remoteCmd)
	remoteCmd.SetHelpTemplate(remoteCmdLongDesc + remoteUsage + utils.GetCmdFlags("remote") + remoteCmdExamples)
}
