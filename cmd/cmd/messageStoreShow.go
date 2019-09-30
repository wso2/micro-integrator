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
	"strconv"
)

var messageStoreName string

// Show MessageStore command related usage info
const showMessageStoreCmdShortDesc = "Get information about messagestores"
const showMessageStoreCmdLongDesc = "Get information about the messagestore specified by command line argument " +
	"[messagestore-name] If not specified, list all the messagestores\n"

var showMessageStoreCmdExamples = "Example:\n" +
	"To get details about a specific messagestore\n" +
	"  " + programName + " " + messageStoreCmdLiteral + " " + utils.ShowCommand + " TestMessageStore\n\n" +
	"To list all the messagestore\n" +
	"  " + programName + " " + messageStoreCmdLiteral + " " + utils.ShowCommand + "\n\n"

// messageStoreShowCmd represents the show message store command
var messageStoreShowCmd = &cobra.Command{
	Use:   utils.ShowCommand,
	Short: showMessageStoreCmdShortDesc,
	Long:  showMessageStoreCmdLongDesc + showMessageStoreCmdExamples,
	Run: func(cmd *cobra.Command, args []string) {
		handleMessageStoreCmdArguments(args)
	},
}

func init() {
	messageStoreCmd.AddCommand(messageStoreShowCmd)
	messageStoreShowCmd.SetHelpTemplate(showMessageStoreCmdLongDesc +
		utils.GetCmdUsage(programName, messageStoreCmdLiteral,
			utils.ShowCommand, "[messagestore-name]") + showMessageStoreCmdExamples +
		utils.GetCmdFlags(messageStoreCmdLiteral))
}

// messagestore argument handling method
func handleMessageStoreCmdArguments(args []string) {
	utils.Logln(utils.LogPrefixInfo + "Show Message Stores called")
	if len(args) == 0 {
		executeListMessageStoreCmd()
	} else if len(args) == 1 {
		if args[0] == utils.HelpCommand {
			printMessageStoreHelp()
		} else {
			messageStoreName = args[0]
			executeGetMessageStoreCmd(messageStoreName)
		}
	} else {
		fmt.Println("Too many arguments. See the usage below")
		printMessageStoreHelp()
	}
}

func printMessageStoreHelp() {
	fmt.Print(showMessageStoreCmdLongDesc + utils.GetCmdUsage(programName, messageStoreCmdLiteral, utils.ShowCommand,
		"[messagestore-name]") + showMessageStoreCmdExamples + utils.GetCmdFlags(messageStoreCmdLiteral))
}

func executeGetMessageStoreCmd(messageStoreName string) {
	finalUrl, params := utils.GetUrlAndParams(utils.PrefixMessageStores, "name", messageStoreName)
	resp, err := utils.UnmarshalData(finalUrl, nil, params, &artifactUtils.MessageStoreData{})

	if err == nil {
		// Printing the details of the MessageStore
		messageStore := resp.(*artifactUtils.MessageStoreData)
		printMessageStore(*messageStore)
	} else {
		fmt.Println(utils.LogPrefixError+"Getting Information of Message Store", err)
	}
}

func printMessageStore(messageStore artifactUtils.MessageStoreData) {
	fmt.Println("Name - " + messageStore.Name)
	fmt.Println("File Name - " + messageStore.FileName)
	fmt.Println("Container - " + messageStore.Container)
	fmt.Println("Producer - " + messageStore.Producer)
	fmt.Println("Consumer - " + messageStore.Consumer)
	fmt.Println("Size - " + strconv.Itoa(messageStore.Size))
	fmt.Println("Properties - " + utils.CreateKeyValuePairs(messageStore.Properties))
}

func executeListMessageStoreCmd() {
	finalUrl := utils.GetRESTAPIBase() + utils.PrefixMessageStores
	resp, err := utils.UnmarshalData(finalUrl, nil, nil, &artifactUtils.MessageStoreList{})

	if err == nil {
		// Printing the list of available Message Stores
		list := resp.(*artifactUtils.MessageStoreList)
		utils.PrintItemList(list, []string{utils.Name, utils.Type, utils.Size}, "No Message Stores found")
	} else {
		utils.Logln(utils.LogPrefixError+"Getting List of Message Stores", err)
	}
}
