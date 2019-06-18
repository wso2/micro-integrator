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

var showCmdLiteral = "show"
var showCmdShortDesc = dedent.Dedent("List or show details about carbon app, endpoint, api, inbound endpoint, " +
	"proxy service, task, sequence or data-service")
var showCmdLongDesc = dedent.Dedent("List or show details about carbon app, endpoint, api, inbound endpoint, " +
	"proxy service, task, sequence or data-service\n")

var showUsageError = dedent.Dedent(`
Usage
  ` + programName + ` ` + showCmdLiteral + ` [command] [argument] [flags]

Available Commands:
  api [api-name]                   Get information about one or more Apis
  carbonapp [app-name]             Get information about one or more Carbon Apps
  endpoint [endpoint-name]         Get information about one or more Endpoints
  inboundendpoint [inbound-name]   Get information about one or more Inbounds
  proxyservice [proxy-name]        Get information about one or more Proxies
  sequence [sequence-name]         Get information about one or more Sequences
  task [task-name]                 Get information about one or more Task
  dataservice [dataservice-name] Get Information about one or more data-service
`)

var showCmdExamples = dedent.Dedent(`
Examples:
To list all the carbon apps
  ` + programName + ` ` + showCmdLiteral + ` ` + showApplicationCmdLiteral + `
To get details about a specific carbon app
  ` + programName + ` ` + showCmdLiteral + ` ` + showApplicationCmdLiteral + ` sampleApp` + `
`)

var showCmdHelp = `
Use " show [command] --help" for more information about a command
`

var showCmdValidArgs = []string{
	"api",
	"carbonapp",
	"endpoint",
	"inboundendpoint",
	"proxyservice",
	"sequence",
	"task",
	"dataservice",
	"help",
}

// showCmd represents the show command
var showCmd = &cobra.Command{
	Use:   "show [command]",
	Short: showCmdShortDesc,
	Long:  showCmdLongDesc,
	Run: func(cmd *cobra.Command, args []string) {
		fmt.Print(showCmdLongDesc + showUsageError + utils.GetCmdFlags("show") + showCmdExamples + showCmdHelp)
	},
	ValidArgs: showCmdValidArgs,
}

func init() {
	rootCmd.AddCommand(showCmd)
	showCmd.SetHelpTemplate(showCmdLongDesc + showUsageError + utils.GetCmdFlags("show") + showCmdExamples + showCmdHelp)
}
