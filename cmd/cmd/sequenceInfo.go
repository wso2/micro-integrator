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
	"github.com/olekukonko/tablewriter"
	"github.com/spf13/cobra"
	"github.com/wso2/micro-integrator/cmd/utils"
	"os"
)

var sequenceName string

// Show Sequence command related usage info
const showSequenceCmdLiteral = "sequence"
const showSequenceCmdShortDesc = "Get information about sequences"

var showSequenceCmdLongDesc = "Get information about the Sequence specified by command line argument [sequence-name] If not specified, list all the sequences\n"

var showSequenceCmdExamples = "Example:\n" +
	"To get details about a specific sequence\n" +
	"  " + programName + " " + showCmdLiteral + " " + showSequenceCmdLiteral + " SampleSequence\n\n" +
	"To list all the sequences\n" +
	"  " + programName + " " + showCmdLiteral + " " + showSequenceCmdLiteral + "\n\n"

// sequenceShowCmd represents the show sequence command
var sequenceShowCmd = &cobra.Command{
	Use:   showSequenceCmdLiteral,
	Short: showSequenceCmdShortDesc,
	Long:  showSequenceCmdLongDesc + showSequenceCmdExamples,
	Run: func(cmd *cobra.Command, args []string) {
		handleSequenceCmdArguments(args)
	},
}

func init() {
	showCmd.AddCommand(sequenceShowCmd)
	sequenceShowCmd.SetHelpTemplate(showSequenceCmdLongDesc + utils.GetCmdUsage(programName, showCmdLiteral,
		showSequenceCmdLiteral, "[sequence-name]") + showSequenceCmdExamples + utils.GetCmdFlags("sequence(s)"))
}

func handleSequenceCmdArguments(args []string) {
	utils.Logln(utils.LogPrefixInfo + "Show sequence called")
	if len(args) == 0 {
		executeListSequencesCmd()
	} else if len(args) == 1 {
		if args[0] == "help" {
			printSequenceHelp()
		} else {
			sequenceName = args[0]
			executeGetSequenceCmd(sequenceName)
		}
	} else {
		fmt.Println("Too many arguments. See the usage below")
		printSequenceHelp()
	}
}

func printSequenceHelp() {
	fmt.Print(showSequenceCmdLongDesc + utils.GetCmdUsage(programName, showCmdLiteral, showSequenceCmdLiteral,
		"[sequence-name]") + showSequenceCmdExamples + utils.GetCmdFlags("sequence(s)"))
}

func executeGetSequenceCmd(sequencename string) {

	finalUrl, params := utils.GetUrlAndParams(utils.PrefixSequences, "sequenceName", sequencename)

	resp, err := utils.UnmarshalData(finalUrl, params, &utils.Sequence{})

	if err == nil {
		// Printing the details of the Sequence
		sequence := resp.(*utils.Sequence)
		printSequenceInfo(*sequence)
	} else {
		fmt.Println(utils.LogPrefixError+"Getting Information of the Sequence", err)
	}
}

// Print the details of a Sequence
// Name, Conatiner and list of mediators
// @param task : Sequence object
func printSequenceInfo(sequence utils.Sequence) {

	fmt.Println("Name - " + sequence.Name)
	fmt.Println("Container - " + sequence.Container)
	fmt.Println("Stats - " + sequence.Stats)
	fmt.Println("Tracing - " + sequence.Tracing)

	var mediatorSring string

	for i, mediator := range sequence.Mediators {
		if i > 0 {
			mediatorSring += " , "
		}
		mediatorSring += mediator
	}
	if mediatorSring != "" {
		fmt.Println("Mediators - " + mediatorSring)
	}
}

func executeListSequencesCmd() {

	finalUrl := utils.RESTAPIBase + utils.PrefixSequences

	resp, err := utils.GetArtifactList(finalUrl, &utils.SequenceList{})

	if err == nil {
		// Printing the list of available Sequences
		list := resp.(*utils.SequenceList)
		printSequenceList(*list)
	} else {
		utils.Logln(utils.LogPrefixError+"Getting List of Sequences", err)
	}
}

func printSequenceList(sequenceList utils.SequenceList) {
	if sequenceList.Count > 0 {
		table := tablewriter.NewWriter(os.Stdout)
		table.SetAlignment(tablewriter.ALIGN_LEFT)

		data := []string{"NAME", "STATS", "TRACING"}
		table.Append(data)

		for _, sequence := range sequenceList.Sequences {
			data = []string{sequence.Name, sequence.Stats, sequence.Tracing}
			table.Append(data)
		}
		table.SetBorder(false)
		table.SetColumnSeparator("  ")
		table.Render()
	} else {
		fmt.Println("No Sequences found")
	}
}
