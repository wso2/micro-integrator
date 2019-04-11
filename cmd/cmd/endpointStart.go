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
	"mi-management-cli/utils"
	"github.com/renstrom/dedent"
	"github.com/spf13/cobra"
	"fmt"
	"net/http"
	"encoding/json"
	"errors"
)

var endpointToStart string

// Switch On endpoint command related usage info
const startEndpointCmdLiteral = "endpoint"
const startEndpointCmdShortDesc = "Switch ON a specific Endpoint"

var startEndpointCmdLongDesc = "Switch ON an Endpoint specified by the flag --name, -n\n"

var startEndpointCmdExamples = dedent.Dedent(`
Example:
  ` + utils.ProjectName + ` ` + startCmdLiteral + ` ` + startEndpointCmdLiteral + ` -n TestEndpoint
`)

// startEndpointCmd represents the endpoint command
var startEndpointCmd = &cobra.Command{
	Use:   startEndpointCmdLiteral,
	Short: startEndpointCmdShortDesc,
	Long: startEndpointCmdLongDesc + startEndpointCmdExamples,
	Run: func(cmd *cobra.Command, args []string) {
		utils.Logln(utils.LogPrefixInfo+"Switch ON endpoint called")
		executeSwitchOnEndpointCmd(endpointToStart)
	},
}

func init() {
	startCmd.AddCommand(startEndpointCmd)

	// Here you will define your flags and configuration settings.

	startEndpointCmd.Flags().StringVarP(&endpointToStart, "name", "n", "", "Name of the Endpoint to Switch on")
    startEndpointCmd.MarkFlagRequired("name")
}

func executeSwitchOnEndpointCmd(endpoint string) {

    err := SwitchOnEndpoint(endpoint)

    // Result after switching on the endpoint
    if err == nil {
        
        fmt.Println("Successfully switched on the endpoint:", endpoint)
        
    } else {
        utils.Logln(utils.LogPrefixError+"Switching On Endpoint", err)
    }

    // if flagExportAPICmdToken != "" {
    //  // token provided with --token (-t) flag
    //  if exportAPICmdUsername != "" || exportAPICmdPassword != "" {
    //      // username and/or password provided with -u and/or -p flags
    //      // Error
    //      utils.HandleErrorAndExit("username/password provided with OAuth token.", nil)
    //  } else {
    //      // token only, proceed with token
    //  }
    // } else {
    //  // no token provided with --token (-t) flag
    //  // proceed with username and password
    //  accessToken, apiManagerEndpoint, preCommandErr := utils.ExecutePreCommand(listApisCmdEnvironment, listApisCmdUsername,
    //      listApisCmdPassword, utils.MainConfigFilePath, utils.EnvKeysAllFilePath)

    //  if preCommandErr == nil {
    //      if listApisCmdQuery != "" {
    //          fmt.Println("Search query:", listApisCmdQuery)
    //      }
    //      count, apis, err := GetCarbonAppInfo(listApisCmdQuery, accessToken, apiManagerEndpoint)

    //      if err == nil {
    //          // Printing the list of available APIs
    //          fmt.Println("Environment:", listApisCmdEnvironment)
    //          fmt.Println("No. of APIs:", count)
    //          if count > 0 {
    //              printAPIs(apis)
    //          }
    //      } else {
    //          utils.Logln(utils.LogPrefixError+"Getting List of APIs", err)
    //      }
    //  } else {
    //      utils.HandleErrorAndExit("Error calling '"+listCmdLiteral+" "+apisCmdLiteral+"'", preCommandErr)
    //  }
    // }
}

// SwitchOnEndpoint
// @param name of the endpoint
// @return error
func SwitchOnEndpoint(name string) (error) {

    finalUrl := utils.RESTAPIBase + utils.PrefixEndpoints + "/on"

    utils.Logln(utils.LogPrefixInfo+"URL:", finalUrl)

    headers := make(map[string]string)
	headers[utils.HeaderContentType] = utils.HeaderValueApplicationJSON

	key := "name"
    _map := make(map[string]string)

    _map[key] = name

    body, _ := json.Marshal(_map)

    resp, err := utils.InvokePOSTRequest(finalUrl, headers, string(body))

    if err != nil {
        utils.HandleErrorAndExit("Unable to connect to "+finalUrl, err)
    }

    utils.Logln(utils.LogPrefixInfo+"Response:", resp.Status())

    if resp.StatusCode() == http.StatusOK {
        // return no error

        return nil
    } else {
        return errors.New(resp.Status())
    }

}