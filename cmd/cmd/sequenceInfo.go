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

var sequenceName string

// Show Sequence command related usage info
const showSequenceCmdLiteral = "sequence"
const showSequenceCmdShortDesc = "Get information about the specified Sequence"

var showSequenceCmdLongDesc = "Get information about the Sequence specified by the flag --name, -n\n"

var showSequenceCmdExamples = dedent.Dedent(`
Example:
  ` + utils.ProjectName + ` ` + showCmdLiteral + ` ` + showSequenceCmdLiteral + ` -n TestSequence
`)

// sequenceShowCmd represents the show sequence command
var sequenceShowCmd = &cobra.Command{
	Use:   showSequenceCmdLiteral,
	Short: showSequenceCmdShortDesc,
	Long:  showSequenceCmdLongDesc + showSequenceCmdExamples,
	Run: func(cmd *cobra.Command, args []string) {
		utils.Logln(utils.LogPrefixInfo + "Show sequence called")
		executeGetSequenceCmd(sequenceName)
	},
}

func init() {
	showCmd.AddCommand(sequenceShowCmd)

	sequenceShowCmd.Flags().StringVarP(&sequenceName, "name", "n", "", "Name of the Sequence")
	sequenceShowCmd.MarkFlagRequired("name")
}

func executeGetSequenceCmd(sequencename string) {

	finalUrl := utils.RESTAPIBase + utils.PrefixSequences + "?inboundEndpointName=" + sequencename

	resp, err := utils.UnmarshalData(finalUrl, &utils.Sequence{})

	if err == nil {
		// Printing the details of the Sequence
		sequence := resp.(*utils.Sequence)
		printSequenceInfo(*sequence)
	} else {
		utils.Logln(utils.LogPrefixError+"Getting Information of the Sequence", err)
	}
}

// printSequenceInfo
// @param task : Sequence object
func printSequenceInfo(sequence utils.Sequence) {
	table := tablewriter.NewWriter(os.Stdout)
	table.SetAlignment(tablewriter.ALIGN_LEFT)

	data := []string{"NAME", sequence.Name}
	table.Append(data)

	data = []string{"CONTAINER", sequence.Container}
	table.Append(data)

	for _, mediator := range sequence.Mediators {
		data = []string{"MEDIATORS", mediator}
		table.Append(data)
	}

	table.SetBorders(tablewriter.Border{Left: true, Top: true, Right: true, Bottom: false})
	table.SetRowLine(true)
	table.SetAutoMergeCells(true)
	table.Render() // Send output
}
