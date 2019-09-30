/*
* Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
* WSO2 Inc. licenses this file to you under the Apache License,
* Version 2.0 (the "License"); you may not use this file except
* in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
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
	"github.com/spf13/cobra"
	"github.com/wso2/micro-integrator/cmd/utils"
	"github.com/wso2/micro-integrator/cmd/utils/artifactUtils"
)

var messageProcessorName string

// Show MessageProcessor command related usage info
const showMessageProcessorCmdShortDesc = "Get information about messageprocessors"
const showMessageProcessorCmdLongDesc = "Get information about the messageprocessor specified by command line argument " +
	"[messageprocessor-name] If not specified, list all the messageprocessors\n"

var showMessageProcessorCmdExamples = "Example:\n" +
	"To get details about a specific messageprocessor\n" +
	"  " + programName + " " + messageProcessorCmdLiteral + " " + utils.ShowCommand + " TestMessageProcessor\n\n" +
	"To list all the messageprocessors\n" +
	"  " + programName + " " + messageProcessorCmdLiteral + " " + utils.ShowCommand + "\n\n"

// messageProcessorShowCmd represents the show endpoint command
var messageProcessorShowCmd = &cobra.Command{
	Use:   utils.ShowCommand,
	Short: showMessageProcessorCmdShortDesc,
	Long:  showMessageProcessorCmdLongDesc + showMessageProcessorCmdExamples,
	Run: func(cmd *cobra.Command, args []string) {
		handleMessageProcessorCmdArguments(args)
	},
}

func init() {
	messageProcessorCmd.AddCommand(messageProcessorShowCmd)
	messageProcessorShowCmd.SetHelpTemplate(showMessageProcessorCmdLongDesc +
		utils.GetCmdUsage(programName, messageProcessorCmdLiteral,
			utils.ShowCommand, "[messageprocessor-name]") + showMessageProcessorCmdExamples +
		utils.GetCmdFlags(messageProcessorCmdLiteral))
}

// messageprocessor argument handling method
func handleMessageProcessorCmdArguments(args []string) {
	utils.Logln(utils.LogPrefixInfo + "Show Message Processors called")
	if len(args) == 0 {
		executeListMessageProcessorCmd()
	} else if len(args) == 1 {
		if args[0] == utils.HelpCommand {
			printMessageProcessorHelp()
		} else {
			messageProcessorName = args[0]
			executeGetMessageProcessorCmd(messageProcessorName)
		}
	} else {
		fmt.Println("Too many arguments. See the usage below")
		printMessageProcessorHelp()
	}
}

func printMessageProcessorHelp() {
	fmt.Print(showMessageProcessorCmdLongDesc + utils.GetCmdUsage(programName, messageProcessorCmdLiteral, utils.ShowCommand,
		"[messageprocessor-name]") + showMessageProcessorCmdExamples + utils.GetCmdFlags(messageProcessorCmdLiteral))
}

func executeGetMessageProcessorCmd(messageProcessorName string) {
	finalUrl, params := utils.GetUrlAndParams(utils.PrefixMessageProcessors, "name", messageProcessorName)
	resp, err := utils.UnmarshalData(finalUrl, nil, params, &artifactUtils.MessageProcessorData{})

	if err == nil {
		// Printing the details of the MessageProcessor
		messageProcessor := resp.(*artifactUtils.MessageProcessorData)
		printMessageProcessor(*messageProcessor)
	} else {
		fmt.Println(utils.LogPrefixError+"Getting Information of Endpoint", err)
	}
}

func printMessageProcessor(messageProcessor artifactUtils.MessageProcessorData) {
	fmt.Println("Name - " + messageProcessor.Name)
	fmt.Println("Type - " + messageProcessor.Type)
	fmt.Println("File Name - " + messageProcessor.FileName)
	fmt.Println("Message Store - " + messageProcessor.Store)
	fmt.Println("Artifact Container - " + messageProcessor.Container)
	fmt.Println("Status - " + messageProcessor.Status)
	fmt.Println("Parameters - " + utils.CreateKeyValuePairs(messageProcessor.Parameters))
}

func executeListMessageProcessorCmd() {
	finalUrl := utils.GetRESTAPIBase() + utils.PrefixMessageProcessors
	resp, err := utils.UnmarshalData(finalUrl, nil, nil, &artifactUtils.MessageProcessorList{})

	if err == nil {
		// Printing the list of available Endpoints
		list := resp.(*artifactUtils.MessageProcessorList)
		utils.PrintItemList(list, []string{utils.Name, utils.Type, utils.Status}, "No Message Processors Found")
	} else {
		utils.Logln(utils.LogPrefixError+"Getting List of Message Processors", err)
	}
}
