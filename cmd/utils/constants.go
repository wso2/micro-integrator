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

package utils

import (
	"os"
	"path/filepath"
)

const ProjectName = "mi"

// File Names and Paths

const ConfigDirName = ".wso2micli"

var HomeDirectory = os.Getenv("HOME")
var ConfigDirPath = filepath.Join(HomeDirectory, ConfigDirName)

var PathSeparator_ = string(os.PathSeparator)

const ServerConfigFileName = "server_config.yaml"
const SampleMainConfigFileName = "main_config.yaml.sample"

const DefaultEnvironmentName = "default"

// Headers and Header Values
const HeaderAuthorization = "Authorization"
const HeaderContentType = "Content-Type"
const HeaderConnection = "Connection"
const HeaderAccept = "Accept"
const HeaderProduces = "Produces"
const HeaderConsumes = "Consumes"
const HeaderContentEncoding = "Content-Encoding"
const HeaderTransferEncoding = "transfer-encoding"
const HeaderValueChunked = "chunked"
const HeaderValueGZIP = "gzip"
const HeaderValueKeepAlive = "keep-alive"
const HeaderValueApplicationZip = "application/zip"
const HeaderValueApplicationJSON = "application/json"
const HeaderValueXWWWFormUrlEncoded = "application/x-www-form-urlencoded"
const HeaderValueAuthPrefixBearer = "Bearer"
const HeaderValueAuthPrefixBasic = "Basic"
const HeaderValueMultiPartFormData = "multipart/form-data"

// Logging Prefixes
const LogPrefixInfo = "[INFO] "
const LogPrefixWarning = "[WARN] "
const LogPrefixError = "[ERROR] "

// Other
const DefaultTokenValidityPeriod = "3600"
const DefaultHttpRequestTimeout = 100000

// DO NOT CHANGE THESE MANUALLY
// Default Server Address
const HTTPProtocol = "http://"
const HTTPSProtocol = "https://"
const DefaultRemoteName = "default"
const DefaultHost = "localhost"
const DefaultPort = "9164"
const Context = "management"

const DefaultRESTAPIBase = HTTPSProtocol + DefaultHost + ":" + DefaultPort + "/" + Context + "/"
const PrefixCarbonApps = "applications"
const PrefixAPIs = "apis"
const PrefixServices = "services"
const PrefixProxyServices = "proxy-services"
const PrefixInboundEndpoints = "inbound-endpoints"
const PrefixEndpoints = "endpoints"
const PrefixMessageProcessors = "message-processors"
const PrefixTemplates = "templates"
const PrefixConnectors = "connectors"
const PrefixMessageStores = "message-stores"
const PrefixLocalEntries = "local-entries"
const PrefixSequences = "sequences"
const PrefixTasks = "tasks"
const PrefixLogging = "logging"
const PrefixServer = "server"
const PrefixDataServices = "data-services"
const ShowCommand = "show"
const HelpCommand = "help"
const LoginResource = "login"
const LogoutResource = "logout"

const Name = "NAME"
const Type = "TYPE"
const Url = "URL"
const Method = "METHOD"
const Status = "STATUS"
const Size = "SIZE"
const Version = "VERSION"
const Package = "PACKAGE"
const Description = "DESCRIPTION"
const Stats = "STATS"
const Tracing = "TRACING"
const Wsdl11 = "WSDL 1.1"
const Wsdl20 = "WSDL 2.0"
const TriggerType = "TRIGGER TYPE"
const Count = "COUNT"
const Interval = "INTERVAL"
const CronExpression = "CRON EXPRESSION"
