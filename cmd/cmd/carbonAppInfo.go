/*
* Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
* WSO2 Inc. licenses this file to you under the Apache License,
* Version 2.0 (the "License"); you may not use this file except
* in compliance with the License.
* You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
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
    "github.com/lithammer/dedent"
    "github.com/olekukonko/tablewriter"
    "github.com/spf13/cobra"
    "github.com/wso2/micro-integrator/cmd/utils"
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

    finalUrl := utils.RESTAPIBase + utils.PrefixCarbonApps + "?carbonAppName=" + appname

    resp, err := utils.UnmarshalData(finalUrl, &utils.CarbonApp{})

    if err == nil {
        // Printing the details of the Carbon App
        app := resp.(*utils.CarbonApp)
        printCarbonAppInfo(*app)
    } else {
        utils.Logln(utils.LogPrefixError+"Getting Information of the Carbon App", err)
    }
}

// Print the details of a Carbon app
// Name, Version, and summary about it's artifacts
// @param app : CarbonApp object
func printCarbonAppInfo(app utils.CarbonApp) {
    table := tablewriter.NewWriter(os.Stdout)
    table.SetAlignment(tablewriter.ALIGN_LEFT)

    data := []string{"NAME", "", app.Name}
    table.Append(data)

    data = []string{"VERSION", "", app.Version}
    table.Append(data)

    for id, artifact := range app.Artifacts {

        artifactId := "ARTIFACTS " + strconv.Itoa(id)

        data = []string{artifactId, "NAME", artifact.Name}
        table.Append(data)
        data = []string{artifactId, "TYPE", artifact.Type}
        table.Append(data)
    }

    table.SetBorders(tablewriter.Border{Left: true, Top: true, Right: true, Bottom: false})
    table.SetRowLine(true)
    table.SetAutoMergeCells(true)
    table.Render() // Send output
}
