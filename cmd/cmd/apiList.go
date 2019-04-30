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
    "github.com/spf13/cobra"
    "github.com/wso2/micro-integrator/cmd/utils"
    "github.com/olekukonko/tablewriter"
    "os"
)

// List APIs command related usage info
const listAPICmdLiteral = "apis"
const listAPICmdShortDesc = "List all the APIs"

var listAPICmdLongDesc = "List all the APIs\n"

var listAPICmdExamples = dedent.Dedent(`
Example:
  ` + utils.ProjectName + ` ` + listCmdLiteral + ` ` + listAPICmdLiteral)

// apisListCmd represents the list apis command
var apisListCmd = &cobra.Command{
    Use:   listAPICmdLiteral,
    Short: listAPICmdShortDesc,
    Long:  listAPICmdLongDesc + listAPICmdExamples,
    Run: func(cmd *cobra.Command, args []string) {
        utils.Logln(utils.LogPrefixInfo + "List APIs called")
        executeListAPIsCmd()
    },
}

func init() {
    showCmd.AddCommand(apisListCmd)
}

func executeListAPIsCmd() {

    finalUrl := utils.RESTAPIBase + utils.PrefixAPIs

    resp, err := utils.GetArtifactList(finalUrl, &utils.APIList{})

    if err == nil {
        // Printing the list of available APIs
        list := resp.(*utils.APIList)
        printApiList(*list)        
    } else {
        utils.Logln(utils.LogPrefixError+"Getting List of APIs", err)
    }
}

func printApiList(apiList utils.APIList) {

    if apiList.Count > 0 {
        table := tablewriter.NewWriter(os.Stdout)
        table.SetAlignment(tablewriter.ALIGN_LEFT)

        data := []string{"NAME", "CONTEXT"}
        table.Append(data)

        for _, api := range apiList.Apis {
            data = []string{api.Name, api.Context}
            table.Append(data)
        }
        table.SetBorder(false)
        table.SetColumnSeparator("  ")
        table.Render()
    }else {
        fmt.Println("No APIs found")
    }    
}