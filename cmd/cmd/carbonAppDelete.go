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

var appToDelete string

// Delete Carbon App command related usage info
const deleteApplicationCmdLiteral = "carbonApp"
const deleteApplicationCmdShortDesc = "Delete a specific Carbon Application"

var deleteApplicationCmdLongDesc = "Delete the Carbon Application specified by the flag --name, -n\n"

var deleteApplicationCmdExamples = dedent.Dedent(`
Example:
  ` + utils.ProjectName + ` ` + deleteCmdLiteral + ` ` + deleteApplicationCmdLiteral + ` -n TestApp
`)

// carbonAppDeleteCmd represents the Delete carbonApp command
var carbonAppDeleteCmd = &cobra.Command{
	Use:   deleteApplicationCmdLiteral,
	Short: deleteApplicationCmdShortDesc,
	Long: deleteApplicationCmdLongDesc + deleteApplicationCmdExamples,
	Run: func(cmd *cobra.Command, args []string) {
		utils.Logln(utils.LogPrefixInfo+"Delete carbon app called")
		executeDeleteCarbonAppCmd(appToDelete)
	},
}

func init() {
	deleteCmd.AddCommand(carbonAppDeleteCmd)

    // Here you will define your flags and configuration settings.

	carbonAppDeleteCmd.Flags().StringVarP(&appToDelete, "name", "n", "", "Name of the Carbon Application to be deleted")
    carbonAppDeleteCmd.MarkFlagRequired("name")
}

func executeDeleteCarbonAppCmd(appname string) {

    err := DeleteCarbonApp(appname)

    // Result after deleting the carbon application
    if err == nil {
        
        fmt.Println("Successfully removed Carbon App")
        
    } else {
        utils.Logln(utils.LogPrefixError+"Deleting Carbon Application", err)
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

// DeleteCarbonApp
// @param name of the carbon app
// @return error
func DeleteCarbonApp(name string) (error) {

    finalUrl := utils.RESTAPIBase + utils.PrefixCarbonApps + "/" + name

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
