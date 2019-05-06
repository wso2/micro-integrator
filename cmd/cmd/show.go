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
var showCmdShortDesc = "List or show details about carbon app, endpoint, api, inbound endpoint, proxy service, task or sequence"
var showCmdLongDesc = "List or show details about carbon app, endpoint, api, inbound endpoint, proxy service, task or sequence\n"

var showUsageError = dedent.Dedent(`
Usage
  ` + utils.ProjectName + ` ` + showCmdLiteral + ` [command] [argument] [flags]

Available Commands:
  api [apiname]                 Get information about the API specified by argument [apiname]
                                If not specified, list all the apis
  carbonapp [appname]           Get information about the Carbon App specified by argument [appname]
                                If not specified, list all the carbon apps
  endpoint [endpointname]       Get information about the Endpoint specified by argument [endpointname]
                                If not specified, list all the endpoints
  inboundendpoint [inboundname] Get information about the Inbound specified by argument [inboundname]
                                If not specified, list all the inbound endpoints
  proxyservice [proxyname]      Get information about the Proxy specified by argument [proxyname]
                                If not specified, list all the proxies
  sequence [sequencename]       Get information about the Sequence specified by argument [sequencename]
                                If not specified, list all the sequences
  task [taskname]               Get information about the Task specified by argument [taskname]
                                If not specified, list all the tasks
Flags:
  -h, --help   help for show
  
Global Flags:
  -v, --verbose   Enable verbose mode
`)

var showCmdExamples = dedent.Dedent(`
Examples:
To list all the carbon apps
  ` + utils.ProjectName + ` ` + showCmdLiteral + ` ` + showApplicationCmdLiteral + `
To get details about a specific carbon app
  ` + utils.ProjectName + ` ` + showCmdLiteral + ` ` + showApplicationCmdLiteral + ` sampleApp` + `
`)

var showCmdHelp = `
Use "micli show [command] --help" for more information about a command
`

// showCmd represents the show command
var showCmd = &cobra.Command{
  Use:   "show [command]",
  Short: showCmdShortDesc,
  Long:  showCmdLongDesc,
  Run: func(cmd *cobra.Command, args []string) {
    fmt.Println(showCmdLongDesc + showUsageError + showCmdExamples + showCmdHelp)
  },
}

func init() {
  rootCmd.AddCommand(showCmd)
  showCmd.SetHelpTemplate(showCmdLongDesc + showUsageError + showCmdExamples + showCmdHelp)
}
