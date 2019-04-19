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
)

// List Proxy Services command related usage info
const listProxyServicesCmdLiteral = "proxyServices"
const listProxyServicesCmdShortDesc = "List all the Proxy Services"

var listProxyServicesCmdLongDesc = "List all the Proxy Services\n"

var listProxyServicesCmdExamples = dedent.Dedent(`
Example:
  ` + utils.ProjectName + ` ` + listCmdLiteral + ` ` + listProxyServicesCmdLiteral)

// proxyServicesListCmd represents the proxyServices command
var proxyServicesListCmd = &cobra.Command{
    Use:   listProxyServicesCmdLiteral,
    Short: listProxyServicesCmdShortDesc,
    Long:  listProxyServicesCmdLongDesc + listProxyServicesCmdExamples,
    Run: func(cmd *cobra.Command, args []string) {
        utils.Logln(utils.LogPrefixInfo + "List proxy services called")
        executeListProxyServicesCmd()
    },
}

func init() {
    listCmd.AddCommand(proxyServicesListCmd)
}

func executeListProxyServicesCmd() {

    finalUrl := utils.RESTAPIBase + utils.PrefixProxyServices

    count, endpoints, err := utils.GetArtifactList(finalUrl)

    if err == nil {
        // Printing the list of available Proxy Services
        fmt.Println("No. of Proxy Services:", count)
        if count > 0 {
            utils.PrintList(endpoints)
        }
    } else {
        utils.Logln(utils.LogPrefixError+"Getting List of Proxy Services", err)
    }
}
