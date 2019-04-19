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
    "github.com/spf13/cobra"
    "github.com/wso2/micro-integrator/cmd/utils"
)

// Init command related usage info
const initCmdLiteral = "init"
const initCmdShortDesc = "Set Management API configuration"

var initCmdLongDesc = "Set the URL and the port of the Management API with flags --server, -s and --port, -p\n"

var initCmdExamples = dedent.Dedent(`
Example:
  ` + utils.ProjectName + ` ` + initCmdLiteral + ` -s https://localhost -p 9091
`)

var serverName, serverPort string

// initCmd represents the init command
var initCmd = &cobra.Command{
    Use:   initCmdLiteral,
    Short: initCmdShortDesc,
    Long:  initCmdLongDesc + initCmdExamples,
    Run: func(cmd *cobra.Command, args []string) {
        utils.Logln(utils.LogPrefixInfo + "Init called")
        writeConfig()
    },
}

func init() {
    rootCmd.AddCommand(initCmd)

    initCmd.Flags().StringVarP(&serverName, "server", "s", "", "Address of the Server")
    initCmd.MarkFlagRequired("server")
    initCmd.Flags().StringVarP(&serverPort, "port", "p", "", "Port of the Management API")
    initCmd.MarkFlagRequired("port")
}

func writeConfig(){
    serverConfig := utils.ServerConfig{serverName, serverPort}
    utils.WriteServerConfigFile(serverConfig, utils.ServerConfigFilePath)
}
