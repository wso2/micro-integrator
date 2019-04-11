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
	"errors"
    "mi-management-cli/utils"
    "github.com/spf13/cobra"
    "github.com/renstrom/dedent"
    "net/http"
	"encoding/xml"
)

// Server Summary command related usage info
const serverSummaryCmdLiteral = "summary"
const serverSummaryCmdShortDesc = "Summary of the Server"

var serverSummaryCmdLongDesc = "Summary of the Micro Integrator Runtime"

var serverSummaryCmdExamples = dedent.Dedent(`
Example:
  ` + utils.ProjectName + ` ` + serverCmdLiteral + ` ` + serverSummaryCmdLiteral + `
`)

// summaryCmd represents the summary command
var summaryCmd = &cobra.Command{
	Use:   serverSummaryCmdLiteral,
	Short: serverSummaryCmdShortDesc,
	Long: serverSummaryCmdLongDesc + serverSummaryCmdExamples,
	Run: func(cmd *cobra.Command, args []string) {
		utils.Logln(utils.LogPrefixInfo+"Show server summary called")
		executeGetServerSummaryCmd()
	},
}

func init() {
	serverCmd.AddCommand(summaryCmd)

	// Here you will define your flags and configuration settings.

}

func executeGetServerSummaryCmd() {

    serverData, err := GetServerInfo()

    if err == nil {
        // Printing the details of the Server
        // printTask(*sequence)
        utils.Logln(utils.LogPrefixInfo+"Server Data", *serverData)
    } else {
        utils.Logln(utils.LogPrefixError+"Getting Information of the Sequence", err)
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

// GetServerInfo
// @return Server Data Object
// @return error
func GetServerInfo() (*utils.ServerSummary, error) {

    finalUrl := utils.RESTAPIBase + utils.PrefixServer + "/summary"

    utils.Logln(utils.LogPrefixInfo+"URL:", finalUrl)

    headers := make(map[string]string)
    // headers[utils.HeaderAuthorization] = utils.HeaderValueAuthPrefixBearer + " " + accessToken

    resp, err := utils.InvokeGETRequest(finalUrl, headers)

    if err != nil {
        utils.HandleErrorAndExit("Unable to connect to "+finalUrl, err)
    }

    utils.Logln(utils.LogPrefixInfo+"Response:", resp.Status())

    if resp.StatusCode() == http.StatusOK {
        serverDataResponse := &utils.ServerSummary{}
        unmarshalError := xml.Unmarshal([]byte(resp.Body()), &serverDataResponse)

        if unmarshalError != nil {
            utils.HandleErrorAndExit(utils.LogPrefixError+"invalid XML response", unmarshalError)
        }

        return serverDataResponse, nil
    } else {
        return nil, errors.New(resp.Status())
    }
}