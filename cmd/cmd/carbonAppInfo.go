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
	"encoding/xml"
	"errors"
	"github.com/lithammer/dedent"
	"github.com/olekukonko/tablewriter"
	"github.com/spf13/cobra"
	"github.com/wso2/micro-integrator/cmd/utils"
	"net/http"
	"os"
	"strconv"
)

var appName string

// Show Carbon App command related usage info
const showApplicationCmdLiteral = "carbonApp"
const showApplicationCmdShortDesc = "Get information about the specified Carbon Application"

var showApplicationCmdLongDesc = "Get information about the Carbon App specified by the flag --name, -n\n"

var showApplicationCmdExamples = dedent.Dedent(`
Example:
  ` + utils.ProjectName + ` ` + showCmdLiteral + ` ` + showApplicationCmdLiteral + ` -n TestApp
`)

// carbonAppShowCmd represents the show carbonApp command
var carbonAppShowCmd = &cobra.Command{
	Use:   showApplicationCmdLiteral,
	Short: showApplicationCmdShortDesc,
	Long:  showApplicationCmdLongDesc + showApplicationCmdExamples,
	Run: func(cmd *cobra.Command, args []string) {
		utils.Logln(utils.LogPrefixInfo + "Show carbon app called")
		executeGetCarbonAppCmd(appName)
	},
}

func init() {
  showCmd.AddCommand(carbonAppShowCmd)
    
	carbonAppShowCmd.Flags().StringVarP(&appName, "name", "n", "", "Name of the Carbon Application")
	carbonAppShowCmd.MarkFlagRequired("name")
}

func executeGetCarbonAppCmd(appname string) {

	app, err := GetCarbonAppInfo(appname)

	if err == nil {
		// Printing the details of the Carbon App
		printCarbonAppInfo(*app)
	} else {
		utils.Logln(utils.LogPrefixError+"Getting Information of the Carbon App", err)
	}
}

// GetCarbonAppInfo
// @param name of the carbon app
// @return carbonApp object
// @return error
func GetCarbonAppInfo(name string) (*utils.CarbonApp, error) {

	finalUrl := utils.RESTAPIBase + utils.PrefixCarbonApps + "?carbonAppName=" + name

	utils.Logln(utils.LogPrefixInfo+"URL:", finalUrl)

	headers := make(map[string]string)

	resp, err := utils.InvokeGETRequest(finalUrl, headers)

	if err != nil {
		utils.HandleErrorAndExit("Unable to connect to "+finalUrl, err)
	}

	utils.Logln(utils.LogPrefixInfo+"Response:", resp.Status())

	if resp.StatusCode() == http.StatusOK {
		carbonAppResponse := &utils.CarbonApp{}
		unmarshalError := xml.Unmarshal([]byte(resp.Body()), &carbonAppResponse)

		if unmarshalError != nil {
			utils.HandleErrorAndExit(utils.LogPrefixError+"invalid XML response", unmarshalError)
		}
		return carbonAppResponse, nil
	} else {
		return nil, errors.New(resp.Status())
	}
}

// printCarbonAppInfo
// @param app : CarbonApp object
func printCarbonAppInfo(app utils.CarbonApp) {
	table := tablewriter.NewWriter(os.Stdout)
	table.SetAlignment(tablewriter.ALIGN_LEFT)

	d_name := []string{"NAME", "", app.Name}
	table.Append(d_name)

	d_version := []string{"VERSION", "", app.Version}
	table.Append(d_version)

	for id, artifact := range app.Artifacts {

		artifactId := "ARTIFACTS " + strconv.Itoa(id)

		data := []string{artifactId, "NAME", artifact.Name}
		table.Append(data)
		data = []string{artifactId, "TYPE", artifact.Type}
		table.Append(data)
	}

	table.SetBorders(tablewriter.Border{Left: true, Top: true, Right: true, Bottom: false})
	table.SetRowLine(true)
	table.SetAutoMergeCells(true)
	table.Render() // Send output
}
