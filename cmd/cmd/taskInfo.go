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
    "mi-management-cli/utils"
    "github.com/spf13/cobra"
    "github.com/renstrom/dedent"
    "net/http"
	"encoding/xml"
	"os"
	"github.com/olekukonko/tablewriter"
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
	Long: showTaskCmdLongDesc + showTaskCmdExamples,
	Run: func(cmd *cobra.Command, args []string) {
		utils.Logln(utils.LogPrefixInfo+"Show task called")
		executeGetTaskCmd(taskName)
	},
}

func init() {
	showCmd.AddCommand(taskShowCmd)

	// Here you will define your flags and configuration settings.

	taskShowCmd.Flags().StringVarP(&taskName, "name", "n", "", "Name of the Task")
    taskShowCmd.MarkFlagRequired("name")
}

func executeGetTaskCmd(taskname string) {

    task, err := GetTaskInfo(taskname)

    if err == nil {
        // Printing the details of the Task
        printTask(*task)
        
    } else {
        utils.Logln(utils.LogPrefixError+"Getting Information of the Task", err)
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

// GetTaskInfo
// @param name of the task
// @return Task Object
// @return error
func GetTaskInfo(name string) (*utils.Task, error) {

    finalUrl := utils.RESTAPIBase + utils.PrefixTasks + "?taskName=" + name

    utils.Logln(utils.LogPrefixInfo+"URL:", finalUrl)

    headers := make(map[string]string)
    // headers[utils.HeaderAuthorization] = utils.HeaderValueAuthPrefixBearer + " " + accessToken

    resp, err := utils.InvokeGETRequest(finalUrl, headers)

    // fmt.Println(resp)

    if err != nil {
        utils.HandleErrorAndExit("Unable to connect to "+finalUrl, err)
    }

    utils.Logln(utils.LogPrefixInfo+"Response:", resp.Status())

    if resp.StatusCode() == http.StatusOK {
        taskResponse := &utils.Task{}
        unmarshalError := xml.Unmarshal([]byte(resp.Body()), &taskResponse)

        if unmarshalError != nil {
            utils.HandleErrorAndExit(utils.LogPrefixError+"invalid XML response", unmarshalError)
        }

        return taskResponse, nil
    } else {
        return nil, errors.New(resp.Status())
    }
}

// printTaskInfo
// @param task : Task object
func printTask(task utils.Task) {
	table := tablewriter.NewWriter(os.Stdout)
	table.SetAlignment(tablewriter.ALIGN_LEFT)

	d_name := []string{"NAME", task.Name}
	table.Append(d_name)

	d_class := []string{"CLASS", task.Class}
	table.Append(d_class)

	d_group := []string{"GROUP", task.Group}
	table.Append(d_group)

	d_type := []string{"TYPE", task.Type}
	table.Append(d_type)

	d_count := []string{"TRIGGER COUNT", task.TriggerCount}
	table.Append(d_count)

	d_interval := []string{"TRIGGER INTERVAL", task.TriggerInterval}
	table.Append(d_interval)

	d_cron := []string{"TRIGGER CRON", task.TriggerCron}
	table.Append(d_cron)

	table.SetBorders(tablewriter.Border{Left: true, Top: true, Right: true, Bottom: false})
	table.SetRowLine(true) 
	table.Render() // Send output
}