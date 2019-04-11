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
    "fmt"
)

var endpointName string

// Show Endpoint command related usage info
const showEndpointCmdLiteral = "endpoint"
const showEndpointCmdShortDesc = "Get information about the specified Endpoint"

var showEndpointCmdLongDesc = "Get information about the Endpoint specified by the flag --name, -n\n"

var showEndpointCmdExamples = dedent.Dedent(`
Example:
  ` + utils.ProjectName + ` ` + showCmdLiteral + ` ` + showEndpointCmdLiteral + ` -n TestEndpoint
`)

// endpointShowCmd represents the show endpoint command
var endpointShowCmd = &cobra.Command{
	Use:   showEndpointCmdLiteral,
	Short: showEndpointCmdShortDesc,
	Long: showEndpointCmdLongDesc + showEndpointCmdExamples,
	Run: func(cmd *cobra.Command, args []string) {
		utils.Logln(utils.LogPrefixInfo+"Show endpoint called")
		executeGetEndpointCmd(endpointName)
	},
}

func init() {
	showCmd.AddCommand(endpointShowCmd)

	// Here you will define your flags and configuration settings.

	endpointShowCmd.Flags().StringVarP(&endpointName, "name", "n", "", "Name of the Endpoint")
    endpointShowCmd.MarkFlagRequired("name")
}

func executeGetEndpointCmd(endpointname string) {

    endpoint, err := GetEndpointInfo(endpointname)

    if err == nil {
        // Printing the details of the Endpoint
		printEndpoint(*endpoint)
	
    } else {
        fmt.Println(utils.LogPrefixError+"Getting Information of Endpoint", err)
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

// GetEndpointInfo
// @param name of the endpoint
// @return Endpoint object
// @return error
func GetEndpointInfo(name string) (*utils.Endpoint, error) {

    finalUrl := utils.RESTAPIBase + utils.PrefixEndpoints + "?endpointName=" + name

    utils.Logln(utils.LogPrefixInfo+"URL:", finalUrl)

    headers := make(map[string]string)
    // headers[utils.HeaderAuthorization] = utils.HeaderValueAuthPrefixBearer + " " + accessToken

    resp, err := utils.InvokeGETRequest(finalUrl, headers)

    if err != nil {
        utils.HandleErrorAndExit("Unable to connect to "+finalUrl, err)
    }

    utils.Logln(utils.LogPrefixInfo+"Response:", resp.Status())

    if resp.StatusCode() == http.StatusOK {
        endpointResponse := &utils.Endpoint{}
        unmarshalError := xml.Unmarshal([]byte(resp.Body()), &endpointResponse)

        if unmarshalError != nil {
            utils.HandleErrorAndExit(utils.LogPrefixError+"invalid XML response", unmarshalError)
        }

        return endpointResponse, nil
    } else {
        return nil, errors.New(resp.Status())
    }

}

// printEndpointInfo
// @param Endpoint : Endpoint object
func printEndpoint(endpoint utils.Endpoint) {

	table := tablewriter.NewWriter(os.Stdout)

	d_name := []string{"NAME", endpoint.Name}
	table.Append(d_name)

	d_desc := []string{"DESCRIPTION", endpoint.Description}
	table.Append(d_desc)

	d_container := []string{"CONTAINER", endpoint.ArtifactContainer}
	table.Append(d_container)

	d_type := []string{"ENDPOINT STRING", endpoint.EndpointString}
	table.Append(d_type)

	// d_status := []string{"STATUS", endpoint.Status}
	// table.Append(d_status)

	// d_uri := []string{"URI", endpoint.URI}
	// table.Append(d_uri)

	table.SetBorders(tablewriter.Border{Left: true, Top: true, Right: true, Bottom: false})
	table.SetRowLine(true) 
	table.Render() // Send output
}