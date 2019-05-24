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
	"github.com/spf13/cobra"
	"github.com/wso2/micro-integrator/cmd/utils"
)

// List Proxy Services command related usage info
const listProxyServicesCmdLiteral = "proxyservices"

// proxyServicesListCmd represents the proxyServices command
var proxyServicesListCmd = &cobra.Command{
	Use:   listProxyServicesCmdLiteral,
	Short: showProxyServiceCmdShortDesc,
	Long:  showProxyServiceCmdLongDesc + showProxyServiceCmdExamples,
	Run: func(cmd *cobra.Command, args []string) {
		// defined in proxyServiceInfo.go
		handleProxyServiceCmdArguments(args)
	},
}

func init() {
	showCmd.AddCommand(proxyServicesListCmd)
	proxyServicesListCmd.SetHelpTemplate(showProxyServiceCmdLongDesc + utils.GetCmdUsage(programName, showCmdLiteral,
		showProxyServiceCmdLiteral, "[proxy-name]") + showProxyServiceCmdExamples + utils.GetCmdFlags("proxyservice(s)"))
}
