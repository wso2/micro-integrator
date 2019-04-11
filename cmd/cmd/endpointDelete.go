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
    "fmt"
    "mi-management-cli/utils"
    "github.com/spf13/cobra"
    "github.com/renstrom/dedent"
    "net/http"
)

var endpointToDelete string

// Delete endpoint command related usage info
const deleteEndpointCmdLiteral = "endpoint"
const deleteEndpointCmdShortDesc = "Delete a specific Endpoint"

var deleteEndpointCmdLongDesc = "Delete the Endpoint specified by the flag --name, -n\n"

var deleteEndpointCmdExamples = dedent.Dedent(`
Example:
  ` + utils.ProjectName + ` ` + deleteCmdLiteral + ` ` + deleteEndpointCmdLiteral + ` -n TestEndpoint
`)

// endpointendpointDeleteCmd represents the delete endpoint command
var endpointDeleteCmd = &cobra.Command{
	Use:   deleteEndpointCmdLiteral,
	Short: deleteEndpointCmdShortDesc,
	Long: deleteEndpointCmdLongDesc + deleteEndpointCmdExamples,
	Run: func(cmd *cobra.Command, args []string) {
		utils.Logln(utils.LogPrefixInfo+"Delete endpoint called")
		executeDeleteEndpointCmd(endpointToDelete)
	},
}

func init() {
	deleteCmd.AddCommand(endpointDeleteCmd)

	// Here you will define your flags and configuration settings.
	endpointDeleteCmd.Flags().StringVarP(&endpointToDelete, "name", "n", "", "Name of the Endpoint to be deleted")
    endpointDeleteCmd.MarkFlagRequired("name")	
}

func executeDeleteEndpointCmd(endpointname string) {

    err := DeleteEndpoint(endpointname)

    // Result after deleting the endpoint
    if err == nil {
        
        fmt.Println("Successfully removed Endpoint")
        
    } else {
        utils.Logln(utils.LogPrefixError+"Deleting Endpoint", err)
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

// DeleteEndpoint
// @param name of the endpoint
// @return error
func DeleteEndpoint(name string) (error) {

    finalUrl := utils.RESTAPIBase + utils.PrefixEndpoints + "/" + name

    utils.Logln(utils.LogPrefixInfo+"URL:", finalUrl)

    headers := make(map[string]string)
    // headers[utils.HeaderAuthorization] = utils.HeaderValueAuthPrefixBearer + " " + accessToken

    resp, err := utils.InvokeDELETERequest(finalUrl, headers)

    if err != nil {
        utils.HandleErrorAndExit("Unable to connect to "+finalUrl, err)
    }

    utils.Logln(utils.LogPrefixInfo+"Response:", resp.Status())

    if resp.StatusCode() == http.StatusOK {
        // apiCarbonAppResponse := &utils.CarbonApp{}
        // unmarshalError := json.Unmarshal([]byte(resp.Body()), &apiCarbonAppResponse)

        // if unmarshalError != nil {
        //     utils.HandleErrorAndExit(utils.LogPrefixError+"invalid JSON response", unmarshalError)
        // }

        return nil
    } else {
        return errors.New(resp.Status())
    }

}