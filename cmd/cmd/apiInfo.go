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
	"os"
    "github.com/olekukonko/tablewriter"
    "strconv"
    "fmt"
)

var apiName string

// Show API command related usage info
const showAPICmdLiteral = "api"
const showAPICmdShortDesc = "Get information about the specified API"

var showAPICmdLongDesc = "Get information about the API specified by the flag --name, -n\n"

var showAPICmdExamples = dedent.Dedent(`
Example:
  ` + utils.ProjectName + ` ` + showCmdLiteral + ` ` + showAPICmdLiteral + ` -n TestAPI
`)

// apiShowCmd represents the show api command
var apiShowCmd = &cobra.Command{
	Use:   showAPICmdLiteral,
	Short: showAPICmdShortDesc,
	Long: showAPICmdLongDesc + showAPICmdExamples,
	Run: func(cmd *cobra.Command, args []string) {
		utils.Logln(utils.LogPrefixInfo+"Show API called")
		executeGetAPICmd(apiName)
	},
}

func init() {
	showCmd.AddCommand(apiShowCmd)

	// Here you will define your flags and configuration settings.

	apiShowCmd.Flags().StringVarP(&apiName, "name", "n", "", "Name of the API")
    apiShowCmd.MarkFlagRequired("name")
}

func executeGetAPICmd(apiname string) {

    api, err := GetAPIInfo(apiname)

    if err == nil {
        // Printing the details of the API
        printAPIInfo(*api)		

    } else {
        fmt.Println(utils.LogPrefixError+"Getting Information of the API", err)
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

// GetAPIInfo
// @param name of the api
// @return API object
// @return error
func GetAPIInfo(name string) (*utils.API, error) {

    finalUrl := utils.RESTAPIBase + utils.PrefixAPIs + "?apiName=" + name

    utils.Logln(utils.LogPrefixInfo+"URL:", finalUrl)

    headers := make(map[string]string)
    // headers[utils.HeaderAuthorization] = utils.HeaderValueAuthPrefixBearer + " " + accessToken

    resp, err := utils.InvokeGETRequest(finalUrl, headers)

    if err != nil {
        utils.HandleErrorAndExit("Unable to connect to "+finalUrl, err)
    }

    utils.Logln(utils.LogPrefixInfo+"Response:", resp.Status())

    if resp.StatusCode() == http.StatusOK {
        apiResponse := &utils.API{}
        unmarshalError := xml.Unmarshal([]byte(resp.Body()), &apiResponse)

        if unmarshalError != nil {
            utils.HandleErrorAndExit(utils.LogPrefixError+"invalid XML response", unmarshalError)
        }

        return apiResponse, nil
    } else {
        return nil, errors.New(resp.Status())
    }

}

// printAPIInfo
// @param app : API object
func printAPIInfo(api utils.API) {
	table := tablewriter.NewWriter(os.Stdout)
	table.SetAlignment(tablewriter.ALIGN_LEFT)

	data := []string{"NAME", "", api.Name}
	table.Append(data)

	data = []string{"CONTEXT", "", api.Context}
    table.Append(data)

    for id, resource := range api.Resources {

        resourceId := "RESOURCES " + strconv.Itoa(id)

        for _, method := range resource.Methods {
            data = []string{resourceId, "METHOD", method}
		    table.Append(data)
        }        
		data = []string{resourceId, "STYLE", resource.Style}
		table.Append(data)
		data = []string{resourceId, "TEMPLATE", resource.Template}
		table.Append(data)
		data = []string{resourceId, "MAPPING", resource.Mapping}
        table.Append(data)

	}

	table.SetBorders(tablewriter.Border{Left: true, Top: true, Right: true, Bottom: false})
	table.SetRowLine(true)
    table.SetAutoMergeCells(true)
	table.Render() // Send output
}