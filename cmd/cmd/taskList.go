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

// List Tasks command related usage info
const listTaskCmdLiteral = "tasks"
const listTaskCmdShortDesc = "List all the Tasks"

var listTaskCmdLongDesc = "List all the Tasks"

var listTaskCmdExamples = dedent.Dedent(`
Example:
  ` + utils.ProjectName + ` ` + listCmdLiteral + ` ` + listTaskCmdLiteral)

// taskListCmd represents the list tasks command
var taskListCmd = &cobra.Command{
    Use:   listTaskCmdLiteral,
    Short: listTaskCmdShortDesc,
    Long:  listTaskCmdLongDesc + listTaskCmdExamples,
    Run: func(cmd *cobra.Command, args []string) {
        utils.Logln(utils.LogPrefixInfo + "List Tasks called")
        executeListTasksCmd()
    },
}

func init() {
    listCmd.AddCommand(taskListCmd)
}

func executeListTasksCmd() {

    finalUrl := utils.RESTAPIBase + utils.PrefixTasks

    count, tasks, err := utils.GetArtifactList(finalUrl)

    if err == nil {
        // Printing the list of available Tasks
        fmt.Println("No. of Tasks:", count)
        if count > 0 {
            utils.PrintList(tasks)
        }
    } else {
        utils.Logln(utils.LogPrefixError+"Getting List of Tasks", err)
    }
}
