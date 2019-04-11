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
)

var inboundEndpointName string

// Show InboundEndpoint command related usage info
const showInboundEndpointCmdLiteral = "inboundEndpoint"
const showInboundEndpointCmdShortDesc = "Get information about the specified Inbound Endpoint"

var showInboundEndpointCmdLongDesc = "Get information about the InboundEndpoint specified by the flag --name, -n\n"

var showInboundEndpointCmdExamples = dedent.Dedent(`
Example:
  ` + utils.ProjectName + ` ` + showCmdLiteral + ` ` + showInboundEndpointCmdLiteral + ` -n TestInboundEndpoint
`)

// InboundEndpointShowCmd represents the Show inboundEndpoint command
var inboundEndpointShowCmd = &cobra.Command{
	Use:   showInboundEndpointCmdLiteral,
	Short: showInboundEndpointCmdShortDesc,
	Long: showInboundEndpointCmdLongDesc + showInboundEndpointCmdExamples,
	Run: func(cmd *cobra.Command, args []string) {
		utils.Logln(utils.LogPrefixInfo+"Show InboundEndpoint called")
		executeGetInboundEndpointCmd(inboundEndpointName)
	},
}

func init() {
	showCmd.AddCommand(inboundEndpointShowCmd)

	// Here you will define your flags and configuration settings.

	inboundEndpointShowCmd.Flags().StringVarP(&inboundEndpointName, "name", "n", "", "Name of the Inbound Endpoint")
    inboundEndpointShowCmd.MarkFlagRequired("name")
}

func executeGetInboundEndpointCmd(inboundEndpointname string) {

    inboundEndpoint, err := GetInboundEndpointInfo(inboundEndpointname)

    if err == nil {
        // Printing the details of the InboundEndpoint
		printInboundEndpoint(*inboundEndpoint)
		
        utils.Logln(utils.LogPrefixInfo+"InboundEndpoint", inboundEndpoint)
    } else {
        utils.Logln(utils.LogPrefixError+"Getting Information of InboundEndpoint", err)
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

// GetInboundEndpointInfo
// @param name of the inbound endpoint
// @return InboundEndpoint object
// @return error
func GetInboundEndpointInfo(name string) (*utils.InboundEndpoint, error) {

    finalUrl := utils.RESTAPIBase + utils.PrefixInboundEndpoints + "?inboundEndpointName=" + name

    utils.Logln(utils.LogPrefixInfo+"URL:", finalUrl)

    headers := make(map[string]string)
    // headers[utils.HeaderAuthorization] = utils.HeaderValueAuthPrefixBearer + " " + accessToken

    resp, err := utils.InvokeGETRequest(finalUrl, headers)

    if err != nil {
        utils.HandleErrorAndExit("Unable to connect to "+finalUrl, err)
    }

    utils.Logln(utils.LogPrefixInfo+"Response:", resp.Status())

    if resp.StatusCode() == http.StatusOK {
        endpointResponse := &utils.InboundEndpoint{}
        unmarshalError := xml.Unmarshal([]byte(resp.Body()), &endpointResponse)

        if unmarshalError != nil {
            utils.HandleErrorAndExit(utils.LogPrefixError+"invalid XML response", unmarshalError)
        }

        return endpointResponse, nil
    } else {
        return nil, errors.New(resp.Status())
    }

}

// printInboundEndpointInfo
// @param InboundEndpoint : InboundEndpoint object
func printInboundEndpoint(endpoint utils.InboundEndpoint) {
	table := tablewriter.NewWriter(os.Stdout)

	row := []string{"NAME", "", endpoint.Name}
	table.Append(row)

	row = []string{"PROTOCOL", "", endpoint.Protocol}
	table.Append(row)
	
	row = []string{"CLASS", "", endpoint.Class}
	table.Append(row)
	
	row = []string{"SEQUENCE", "", endpoint.Sequence}
	table.Append(row)
	
	row = []string{"ERROR SEQUENCE", "", endpoint.ErrorSequence}
    table.Append(row)
    
    for _, param := range endpoint.Parameters {
        row = []string{"PARAMETERS", param.Name, param.Value}
		table.Append(row)
	}

	table.SetBorders(tablewriter.Border{Left: true, Top: true, Right: true, Bottom: false})
	table.SetRowLine(true)
    table.SetAutoMergeCells(true)
	table.Render() // Send output
}