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
    "fmt"
    "github.com/lithammer/dedent"
    "github.com/olekukonko/tablewriter"
    "github.com/spf13/cobra"
    "github.com/wso2/micro-integrator/cmd/utils"
    "os"
    "strconv"
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
    Long:  showAPICmdLongDesc + showAPICmdExamples,
    Run: func(cmd *cobra.Command, args []string) {
        utils.Logln(utils.LogPrefixInfo + "Show API called")
        executeGetAPICmd(apiName)
    },
}

func init() {
    showCmd.AddCommand(apiShowCmd)

    apiShowCmd.Flags().StringVarP(&apiName, "name", "n", "", "Name of the API")
    apiShowCmd.MarkFlagRequired("name")
}

func executeGetAPICmd(apiname string) {

    finalUrl := utils.RESTAPIBase + utils.PrefixAPIs + "?apiName=" + apiname

    resp, err := utils.UnmarshalData(finalUrl, &utils.API{})

    if err == nil {
        // Printing the details of the API
        api := resp.(*utils.API)
        printAPIInfo(*api)
    } else {
        fmt.Println(utils.LogPrefixError+"Getting Information of the API", err)
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
