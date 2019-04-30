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
    // "strconv"
    //"reflect"
)

var apiName string

// Show API command related usage info
const showAPICmdLiteral = "api"
const showAPICmdShortDesc = "Get information about the specified API"

var showAPICmdLongDesc = "Get information about the API specified by command line argument [apiName] or list all the apis\n"

var showAPICmdExamples = dedent.Dedent(`
Example:
To get details about a specific api
  ` + utils.ProjectName + ` ` + showCmdLiteral + ` ` + showAPICmdLiteral + ` TestAPI

To list all the apis
  ` + utils.ProjectName + ` ` + showCmdLiteral + ` ` + showAPICmdLiteral + `
`)

var showAPICmdUsage = dedent.Dedent(
`Usage:
  ` + utils.ProjectName + ` ` + showCmdLiteral + ` ` + showAPICmdLiteral + ` [apiName]
`)

// apiShowCmd represents the show api command
var apiShowCmd = &cobra.Command{
    Use:   showAPICmdLiteral,
    Short: showAPICmdShortDesc,
    Long:  showAPICmdLongDesc + showAPICmdExamples,
    Run: func(cmd *cobra.Command, args []string) {
        utils.Logln(utils.LogPrefixInfo + "Show API called")
        if len(args) == 0 {
            executeListAPIsCmd()
        }else if len(args) == 1{
            if args[0] == "help" {
                fmt.Print(showAPICmdLongDesc + showAPICmdExamples + showAPICmdUsage)
            }else {
                apiName = args[0]
                executeGetAPICmd(apiName)
            }
        }else {
            fmt.Println("Too many arguments. See the usage below")
            fmt.Print(showAPICmdExamples + showAPICmdUsage)
        }
    },
}

func init() {
    showCmd.AddCommand(apiShowCmd)
    apiShowCmd.SetUsageTemplate(showAPICmdUsage)

    // apiShowCmd.Flags().StringVarP(&apiName, "name", "n", "", "Name of the API")
    // apiShowCmd.MarkFlagRequired("name")
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

// Print the details of an API
// Name, Context, Http Method, URL Style
// @param app : API object
func printAPIInfo(api utils.API) {


    
    // s := reflect.ValueOf(&api).Elem()
    // typeOfT := s.Type()

    // for i := 0; i < s.NumField(); i++ {
    //     f := s.Field(i)
    //     fmt.Printf("%s : %v\n",
    //         typeOfT.Field(i).Name, f.Interface())
    // }


    // data := []string{"Name - ", api.Name}
    // table.Append(data)

    // data = []string{"Context - ", api.Context}
    // table.Append(data)

    // data = []string{"Version - ", api.Version}
    // table.Append(data)

    // data = []string{"Stats - ", api.Stats}
    // table.Append(data)

    // data = []string{"Tracing - ", api.Tracing}
    // table.Append(data)

    // data = []string{"Resources : ", ""}
    // table.Append(data)

    fmt.Println("Name - " + api.Name)
    fmt.Println("Context - " + api.Context)
    fmt.Println("Version - " + api.Version)
    fmt.Println("Stats - " + api.Stats)
    fmt.Println("Tracing - " + api.Tracing)
    fmt.Println("Resources : ")

    table := tablewriter.NewWriter(os.Stdout)
    table.SetAlignment(tablewriter.ALIGN_LEFT)

    data := []string{"URL", "METHOD"}
    table.Append(data)

    for _, resource := range api.Resources {

        var methodSring string

        for i, method := range resource.Methods {
            if i > 1 {
                methodSring += "/"    
            }
            methodSring += method
        }

        data = []string{resource.Url, methodSring}
        table.Append(data)
    }

    table.SetBorder(false)
    table.SetColumnSeparator(" ")
    table.SetAutoMergeCells(true)
    table.Render()
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
