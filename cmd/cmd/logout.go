/*
* Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
* WSO2 Inc. licenses this file to you under the Apache License,
* Version 2.0 (the "License"); you may not use this file except
* in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
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
	"github.com/wso2/micro-integrator/cmd/utils"
	"net/http"

	"github.com/spf13/cobra"
)

const logoutCmdLiteral = "logout"
const logoutCmdShortDesc = "Logout of the current Micro Integrator instance (current remote)"

var logoutCmdExamples = dedent.Dedent(`
Example:
	` + utils.ProjectName + ` ` + remoteCmdLiteral + ` ` + logoutCmdLiteral + `
`)

// logoutCmd represents the logout command
var logoutCmd = &cobra.Command{
	Use:   logoutCmdLiteral,
	Short: logoutCmdShortDesc,
	Long:  dedent.Dedent(loginCmdShortDesc + logoutCmdExamples),
	Run: func(cmd *cobra.Command, args []string) {
		utils.Logln(utils.LogPrefixInfo + logoutCmdLiteral + " called")
		executeLogoutCmd()
	},
}

func executeLogoutCmd() {
	// call the logout resource of MI management API
	url := utils.GetRESTAPIBase() + utils.LogoutResource
	headers := make(map[string]string)
	headers[utils.HeaderAuthorization] = utils.HeaderValueAuthPrefixBearer + " " +
		utils.RemoteConfigData.Remotes[utils.RemoteConfigData.CurrentServer].AccessToken
	resp, err := utils.InvokeGETRequest(url, headers, nil)
	if err != nil {
		utils.HandleErrorAndExit("Error logging out of the current remote", err)
	} else {
		if resp.StatusCode() == http.StatusOK {
			fmt.Println("Successfully logged out of the current remote: " + utils.RemoteConfigData.CurrentServer)
		} else {
			utils.HandleErrorAndExit("Error logging out of the current remote: "+resp.Status(), nil)
		}
	}
}

func init() {
	remoteCmd.AddCommand(logoutCmd)
	logoutCmd.SetHelpTemplate(logoutCmdShortDesc + logoutCmdExamples)
}
