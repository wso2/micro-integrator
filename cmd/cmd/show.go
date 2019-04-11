/*
*  Copyright (c) WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/

package cmd

import (
	"fmt"
	"mi-management-cli/utils"
	"github.com/renstrom/dedent"
	"github.com/spf13/cobra"
)

var showCmdLiteral = "show"

var showUsageError = dedent.Dedent(`Error: required command(s) and flag(s) not set

Usage
  ` + utils.ProjectName + ` ` + showCmdLiteral + ` [COMMANDS] [flags]

Flags:
  Required:
    --name, -n

Available Commands:
  ` + showApplicationCmdLiteral + `		` + showApplicationCmdShortDesc +`
  ` + showAPICmdLiteral + `			` + showAPICmdShortDesc +`
  ` + showEndpointCmdLiteral + `		` + showEndpointCmdShortDesc +`
  ` + showInboundEndpointCmdLiteral + `	` + showInboundEndpointCmdShortDesc +`
  ` + showProxyServiceCmdLiteral + `		` + showProxyServiceCmdShortDesc +`
  ` + showServiceCmdLiteral + `		` + showServiceCmdShortDesc +`
  ` + listSequenceCmdLiteral + `		` + showApplicationCmdShortDesc +`
  ` + showTaskCmdLiteral + `			` + showTaskCmdShortDesc +`
  `)

var showCmdExamples = dedent.Dedent(`
Examples:
  ` + utils.ProjectName + ` ` + showCmdLiteral + ` ` + showApplicationCmdLiteral + ` -n TestApp` + `
  ` + utils.ProjectName + ` ` + showCmdLiteral + ` ` + showAPICmdLiteral + ` -n TestApi` + `
  ` + utils.ProjectName + ` ` + showCmdLiteral + ` ` + showEndpointCmdLiteral + ` -n TestEndpoint` + `
`)

var showCmdHelp = `
Use "micli show [command] --help" for more information about a command.`

// showCmd represents the show command
var showCmd = &cobra.Command{
	Use:   "show [COMMANDS]",
	Short: "Show details about a carbon app, endpoint, api, service, task or sequence",
	Long: "Show details about a carbon app, endpoint, api, task or sequence",
	Run: func(cmd *cobra.Command, args []string) {
		fmt.Println(showUsageError + showCmdExamples + showCmdHelp)		
	},
}

func init() {
	rootCmd.AddCommand(showCmd)

	// Here you will define your flags and configuration settings.

}
