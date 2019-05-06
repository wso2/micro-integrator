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
    "github.com/spf13/cobra"
    "github.com/wso2/micro-integrator/cmd/utils"
)

// List Carbon App command related usage info
const listApplicationCmdLiteral = "carbonapps"

// carbonAppsListCmd represents the list carbonApps command
var carbonAppsListCmd = &cobra.Command{
    Use:   listApplicationCmdLiteral,
    Short: showAPICmdShortDesc,
    Long:  showAPICmdLongDesc + showAPICmdExamples,
    Run: func(cmd *cobra.Command, args []string) {
        // defined in carbonAppInfo.go
        handleApplicationCmdArguments(args)
    },
}

func init() {
    showCmd.AddCommand(carbonAppsListCmd)
    carbonAppsListCmd.SetHelpTemplate(showApplicationCmdLongDesc + utils.GetCmdUsage(showCmdLiteral, 
        showApplicationCmdLiteral, "[app-name]") + showApplicationCmdExamples + utils.GetCmdFlags("carbonapp(s)"))
}
