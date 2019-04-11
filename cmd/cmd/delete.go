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
	"fmt"
	"github.com/spf13/cobra"
	"github.com/renstrom/dedent"
	"mi-management-cli/utils"
)

var deleteCmdLiteral = "delete"

var deleteUsageError = dedent.Dedent(`Error: required command(s) and flag(s) not set

Usage
  ` + utils.ProjectName + ` ` + deleteCmdLiteral + ` [command] [flags]

Commands:
  ` + deleteApplicationCmdLiteral + ` -n <name>	` + deleteApplicationCmdShortDesc +`
`)

var deleteCmdExamples = dedent.Dedent(`
Example:
  ` + utils.ProjectName + ` ` + deleteCmdLiteral + ` ` + deleteApplicationCmdLiteral + ` -n TestApp`)

// deleteCmd represents the delete command
var deleteCmd = &cobra.Command{
	Use:   "delete [COMMANDS]",
	Short: "Delete a carbon app, endpoint, api, task or sequence",
	Long: "Delete a carbon app, endpoint, api, task or sequence",
	Run: func(cmd *cobra.Command, args []string) {
		fmt.Println(deleteUsageError + deleteCmdExamples)
	},
}

func init() {
	rootCmd.AddCommand(deleteCmd)

	// Here you will define your flags and configuration settings.

}
