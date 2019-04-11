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
    "encoding/xml"
    "os"
	"github.com/olekukonko/tablewriter"
)

var serviceName string

// Show service command related usage info
const showServiceCmdLiteral = "service"
const showServiceCmdShortDesc = "Get information about the specified Service"

var showServiceCmdLongDesc = "Get information about the Service specified by the flag --name, -n\n"

var showServiceCmdExamples = dedent.Dedent(`
Example:
  ` + utils.ProjectName + ` ` + showCmdLiteral + ` ` + showServiceCmdLiteral + ` -n TestService
`)

// serviceShowCmd represents the show service command
var serviceShowCmd = &cobra.Command{
	Use:   showServiceCmdLiteral,
	Short: showServiceCmdShortDesc,
	Long: showServiceCmdLongDesc + showServiceCmdExamples,
	Run: func(cmd *cobra.Command, args []string) {
		utils.Logln(utils.LogPrefixInfo+"Show service called")
		executeGetServiceCmd(serviceName)
	},
}

func init() {
	showCmd.AddCommand(serviceShowCmd)

	// Here you will define your flags and configuration settings.

	serviceShowCmd.Flags().StringVarP(&serviceName, "name", "n", "", "Name of the Service")
    serviceShowCmd.MarkFlagRequired("name")
}

func executeGetServiceCmd(servicename string) {

    service, err := GetServiceInfo(servicename)

    if err == nil {
        // Printing the details of the Service
        printService(*service)
        
    } else {
        utils.Logln(utils.LogPrefixError+"Getting Information of Service", err)
        fmt.Println("Something went wrong", err)
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

// GetServiceInfo
// @param name of the service
// @return Service object
// @return error
func GetServiceInfo(name string) (*utils.Service, error) {

    finalUrl := utils.RESTAPIBase + utils.PrefixServices + "/" + name

    utils.Logln(utils.LogPrefixInfo+"URL:", finalUrl)

    headers := make(map[string]string)
    // headers[utils.HeaderAuthorization] = utils.HeaderValueAuthPrefixBearer + " " + accessToken

    resp, err := utils.InvokeGETRequest(finalUrl, headers)

    if err != nil {
        utils.HandleErrorAndExit("Unable to connect to "+finalUrl, err)
    }

    utils.Logln(utils.LogPrefixInfo+"Response:", resp.Status())

    if resp.StatusCode() == http.StatusOK {
        serviceResponse := &utils.Service{}
        unmarshalError := xml.Unmarshal([]byte(resp.Body()), &serviceResponse)

        if unmarshalError != nil {
            utils.HandleErrorAndExit(utils.LogPrefixError+"invalid XML response", unmarshalError)
        }

        return serviceResponse, nil
    } else {
        return nil, errors.New(resp.Status())
    }

}

// printService
// @param Service : Service object
func printService(service utils.Service) {
	table := tablewriter.NewWriter(os.Stdout)

	row := []string{"NAME", service.Name}
	table.Append(row)
	
	row = []string{"DESCRIPTION", service.Description}
	table.Append(row)
	
	row = []string{"TYPE", service.Type}
	table.Append(row)
	
	row = []string{"STATUS", service.Status}
    table.Append(row)
    
    row = []string{"TRY IT URL", service.TryItURL}
    table.Append(row)

    table.SetAlignment(tablewriter.ALIGN_LEFT)   // Set Alignment
	table.SetBorders(tablewriter.Border{Left: true, Top: true, Right: true, Bottom: false})
	table.SetRowLine(true)
    table.SetAutoMergeCells(true)
	table.Render() // Send output
}