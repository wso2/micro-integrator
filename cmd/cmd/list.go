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

var listCmdLiteral = "list"

var listUsageError = dedent.Dedent(`Error: required command(s) not set

Usage
  ` + utils.ProjectName + ` ` + listCmdLiteral + ` [COMMANDS]

Available Commands:
  ` + listApplicationCmdLiteral + `		` + listApplicationCmdShortDesc +`
  ` + listAPICmdLiteral + `			` + listAPICmdShortDesc +`
  ` + listEndpointsCmdLiteral + `		` + listEndpointsCmdShortDesc +`
  ` + listInboundEndpointsCmdLiteral + `	` + listInboundEndpointsCmdShortDesc +`
  ` + listSequenceCmdLiteral + `		` + listSequenceCmdShortDesc +`
  ` + listProxyServicesCmdLiteral + `		` + listProxyServicesCmdShortDesc +`
  ` + listServicesCmdLiteral + `		` + listApplicationCmdShortDesc +`
  ` + listTaskCmdLiteral + `			` + listTaskCmdShortDesc +`
  `)

var listCmdExamples = dedent.Dedent(`
Examples:
  ` + utils.ProjectName + ` ` + listCmdLiteral + ` ` + listApplicationCmdLiteral + `
  ` + utils.ProjectName + ` ` + listCmdLiteral + ` ` + listAPICmdLiteral + `
  ` + utils.ProjectName + ` ` + listCmdLiteral + ` ` + listSequenceCmdLiteral + `
`)

var listCmdHelp = `
Use "micli list [command] --help" for more information about a command.`


// listCmd represents the list command
var listCmd = &cobra.Command{
	Use: "list [COMMANDS]",
	Short: "List all carbon apps, endpoints, apis, tasks or sequences",
	Long: "List all carbon apps, endpoints, apis, tasks or sequences",
	Run: func(cmd *cobra.Command, args []string) {
		fmt.Println(listUsageError + listCmdExamples + listCmdHelp)		
	},
}

func init() {
	rootCmd.AddCommand(listCmd)	
	// Here you will define your flags and configuration settings.
}
