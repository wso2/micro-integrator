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

var templateType string
var templateName string

var endpointKey string = "endpoint"
var sequenceKey string = "sequence"

// Show template command related usage info
const showTemplateCmdShortDesc = "Get information about templates"
const showTemplateCmdLongDesc = "Get information about the template specified by command line arguments " +
	"[template-type] and [template-name] If not specified, list all the templates\n"

var showTemplateCmdExamples = "Example:\n" +
	"To get details about a specific template\n" +
	"  " + programName + " " + templateCmdLiteral + " " + utils.ShowCommand + " TemplateType TemplateName\n\n" +
	"To get details about a specific template type\n" +
	"  " + programName + " " + templateCmdLiteral + " " + utils.ShowCommand + " TemplateType\n\n" +
	"To list all the templates\n" +
	"  " + programName + " " + templateCmdLiteral + " " + utils.ShowCommand + "\n\n"

// templateShowCmd represents the show template command
var templateShowCmd = &cobra.Command{
	Use:   utils.ShowCommand,
	Short: showTemplateCmdShortDesc,
	Long:  showTemplateCmdLongDesc + showTemplateCmdExamples,
	Run: func(cmd *cobra.Command, args []string) {
		handleTemplateCmdArguments(args)
	},
}

func init() {
	templateCmd.AddCommand(templateShowCmd)
	templateShowCmd.SetHelpTemplate(showTemplateCmdLongDesc + utils.GetCmdUsage(programName, templateCmdLiteral,
		utils.ShowCommand, "[template-type] [template-name]") + showTemplateCmdExamples +
		utils.GetCmdFlags(templateCmdLiteral))
}

// template argument handling method
func handleTemplateCmdArguments(args []string) {
	utils.Logln(utils.LogPrefixInfo + "Show Template called")
	if len(args) == 0 {
		executeListTemplatesCmd()
	} else if len(args) == 1 {
		if args[0] == utils.HelpCommand {
			printTemplateHelp()
		} else if args[0] == endpointKey || args[0] == sequenceKey {
			templateType = args[0]
			executeGetTemplateByTypeCmd(templateType)
		} else {
			printTemplateHelp()
		}
	} else if len(args) == 2 {
		if args[0] == endpointKey || args[0] == sequenceKey {
			templateType = args[0]
			templateName = args[1]
			executeGetTemplateByNameCmd(templateType, templateName)
		} else {
			printTemplateHelp()
		}
	} else {
		fmt.Println("Too many arguments. See the usage below")
		printTemplateHelp()
	}
}

func printTemplateHelp() {
	fmt.Print(showTemplateCmdLongDesc + utils.GetCmdUsage(programName, templateCmdLiteral, utils.ShowCommand,
		"[template-type] [template-name]") + showTemplateCmdExamples + utils.GetCmdFlags(templateCmdLiteral))
}

func executeListTemplatesCmd() {
	finalUrl := utils.GetRESTAPIBase() + utils.PrefixTemplates
	resp, err := utils.UnmarshalData(finalUrl, nil, nil, &artifactUtils.TemplateList{})

	if err == nil {
		// Printing the list of available Templates
		list := resp.(*artifactUtils.TemplateList)
		printTemplateList(*list)
	} else {
		utils.Logln(utils.LogPrefixError+"Getting List of Templates", err)
	}
}

func executeGetTemplateByTypeCmd(templateType string) {
	finalUrl, params := utils.GetUrlAndParams(utils.PrefixTemplates, "type", templateType)
	resp, err := utils.UnmarshalData(finalUrl, nil, params, &artifactUtils.TemplateListByType{})

	if err == nil {
		// Printing the details of the Templates by type
		list := resp.(*artifactUtils.TemplateListByType)
		printTemplatesByType(*list)
	} else {
		fmt.Println(utils.LogPrefixError+"Getting Information of Template", err)
	}
}

func executeGetTemplateByNameCmd(templateType string, templateName string) {
	finalUrl, params := utils.GetUrlAndParams(utils.PrefixTemplates, "type", templateType)
	params = utils.PutQueryParamsToMap(params, "name", templateName)
	resp, err := utils.UnmarshalData(finalUrl, nil, params, &artifactUtils.TemplateListByName{})

	if err == nil {
		// Printing the details of the Template by name
		list := resp.(*artifactUtils.TemplateListByName)
		printTemplatesByName(*list)
	} else {
		fmt.Println(utils.LogPrefixError+"Getting Information of Template - "+templateName, err)
	}
}

func printTemplateList(templateList artifactUtils.TemplateList) {
	var isTemplateFound bool = false

	allTemplateList := [][]string{}
	if len(templateList.SequenceTemplates) > 0 {
		isTemplateFound = true
		for _, template := range templateList.SequenceTemplates {
			data := []string{template.Name, "Sequence"}
			allTemplateList = append(allTemplateList, data)
		}
	}

	if len(templateList.EndpointTemplates) > 0 {
		isTemplateFound = true
		for _, template := range templateList.EndpointTemplates {
			data := []string{template.Name, "Endpoint"}
			allTemplateList = append(allTemplateList, data)
		}
	}

	if len(allTemplateList) > 0 {
		isTemplateFound = true
		table := utils.GetTableWriter()

		data := []string{utils.Name, utils.Type}
		table.Append(data)

		for _, template := range allTemplateList {
			table.Append(template)
		}
		table.Render()
	}

	if !isTemplateFound {
		fmt.Println("No Templates found")
	}
}

func printTemplatesByType(templateList artifactUtils.TemplateListByType) {
	if templateList.Count > 0 {
		table := utils.GetTableWriter()

		data := []string{utils.Name}
		table.Append(data)

		for _, template := range templateList.Templates {
			data = []string{template.Name}
			table.Append(data)
		}
		table.Render()
	} else {
		fmt.Println("No Template found from the given type")
	}
}

func printTemplatesByName(templateList artifactUtils.TemplateListByName) {
	fmt.Println("Name - " + templateList.Name)
	var parameters string
	for _, params := range templateList.Parameters {
		parameters = parameters + params + ", "
	}
	fmt.Println("Parameters - " + parameters[:len(parameters)-2])
}
