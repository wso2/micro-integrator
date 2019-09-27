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
	"encoding/base64"
	"fmt"
	"github.com/lithammer/dedent"
	"github.com/spf13/cobra"
	"github.com/wso2/micro-integrator/cmd/utils"
)

var username string
var password string

const loginCmdLiteral = "login"
const loginCmdShortDesc = "Login to the current Micro Integrator instance (current remote)"

var loginCmdExamples = dedent.Dedent(`
Example: 
	` + utils.ProjectName + ` ` + remoteCmdLiteral + ` ` + loginCmdLiteral + `  # will be prompted for username and password
	` + utils.ProjectName + ` ` + remoteCmdLiteral + ` ` + loginCmdLiteral + ` admin  admin
`)

// loginCmd represents the login command
var loginCmd = &cobra.Command{
	Use:   loginCmdLiteral,
	Short: loginCmdLiteral,
	Long:  dedent.Dedent(loginCmdShortDesc + loginCmdExamples),
	Run: func(cmd *cobra.Command, args []string) {
		utils.Logln(utils.LogPrefixInfo + loginCmdLiteral + " called")
		executeLoginCmd(args)
	},
}

func executeLoginCmd(args []string) {
	if len(args) == 0 {
		if username == "" {
			username = utils.PromptForUsername()
		}

		if password == "" {
			password = utils.PromptForPassword()
		}
	} else if len(args) == 1 {
		username = args[0]
		password = utils.PromptForPassword()
	} else if len(args) == 2 {
		username = args[0]
		password = args[1]
	} else {
		fmt.Println("Too many arguments. See the usage of " + loginCmdLiteral)
		fmt.Println("Execute " + remoteCmdLiteral + " " + loginCmdLiteral + " --help")
	}

	if username != "" && password != "" {
		b64encodedCredentials := base64.StdEncoding.EncodeToString([]byte(username + ":" + password))

		// call the login resource of MI management API
		url := utils.GetRESTAPIBase() + utils.LoginResource
		headers := map[string]string{
			utils.HeaderAuthorization: utils.HeaderValueAuthPrefixBasic + " " + b64encodedCredentials,
		}
		resp, err := utils.UnmarshalData(url, headers, nil, &utils.LoginResponse{})
		if err != nil {
			fmt.Println(utils.LogPrefixError + err.Error())
		} else {
			loginResponse := resp.(*utils.LoginResponse)
			err := utils.RemoteConfigData.UpdateCurrentRemoteToken(loginResponse.AccessToken)
			if err != nil {
				utils.HandleErrorAndExit("Error updating credentials", err)
			} else {
				utils.Logln(utils.LogPrefixInfo + "Persisting auth credentials for current remote")
				fmt.Println("Login successful for remote: " + utils.RemoteConfigData.CurrentServer + "!")
				utils.RemoteConfigData.Persist(utils.GetServerConfigFilePath())
			}
		}
	} else {
		utils.HandleErrorAndExit("Username and Password cannot be blank", nil)
	}
}

func init() {
	remoteCmd.AddCommand(loginCmd)
	loginCmd.SetHelpTemplate(loginCmdShortDesc + loginCmdExamples)
}
