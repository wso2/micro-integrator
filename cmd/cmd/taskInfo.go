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
)

var taskName string

// Show Task command related usage info
const showTaskCmdLiteral = "task"
const showTaskCmdShortDesc = "Get information about the specified Task"

var showTaskCmdLongDesc = "Get information about the Task specified by the flag --name, -n\n"

var showTaskCmdExamples = dedent.Dedent(`
Example:
  ` + utils.ProjectName + ` ` + showCmdLiteral + ` ` + showTaskCmdLiteral + ` -n TestTask
`)

// taskShowCmd represents the task command
var taskShowCmd = &cobra.Command{
    Use:   showTaskCmdLiteral,
    Short: showTaskCmdShortDesc,
    Long:  showTaskCmdLongDesc + showTaskCmdExamples,
    Run: func(cmd *cobra.Command, args []string) {
        utils.Logln(utils.LogPrefixInfo + "Show task called")
        executeGetTaskCmd(taskName)
    },
}

func init() {
    showCmd.AddCommand(taskShowCmd)

    taskShowCmd.Flags().StringVarP(&taskName, "name", "n", "", "Name of the Task")
    taskShowCmd.MarkFlagRequired("name")
}

func executeGetTaskCmd(taskname string) {

    finalUrl := utils.RESTAPIBase + utils.PrefixTasks + "?taskName=" + taskname

    resp, err := utils.UnmarshalData(finalUrl, &utils.Task{})

    if err == nil {
        // Printing the details of the Task
        task := resp.(*utils.Task)
        printTask(*task)
    } else {
        utils.Logln(utils.LogPrefixError+"Getting Information of the Task", err)
    }
}

// printTaskInfo
// @param task : Task object
func printTask(task utils.Task) {
    table := tablewriter.NewWriter(os.Stdout)
    table.SetAlignment(tablewriter.ALIGN_LEFT)

    data := []string{"NAME", task.Name}
    table.Append(data)

    data = []string{"CLASS", task.Class}
    table.Append(data)

    data = []string{"GROUP", task.Group}
    table.Append(data)

    data = []string{"TYPE", task.Type}
    table.Append(data)

    data = []string{"TRIGGER COUNT", task.TriggerCount}
    table.Append(data)

    data = []string{"TRIGGER INTERVAL", task.TriggerInterval}
    table.Append(data)

    data = []string{"TRIGGER CRON", task.TriggerCron}
    table.Append(data)

    table.SetBorders(tablewriter.Border{Left: true, Top: true, Right: true, Bottom: false})
    table.SetRowLine(true)
    table.Render() // Send output
}
