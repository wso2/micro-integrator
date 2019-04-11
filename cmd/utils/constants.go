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

package utils

import (
	"os"
	"path/filepath"
)

const ProjectName = "micli"

// File Names and Paths
var CurrentDir, _ = os.Getwd()

const ConfigDirName = ".wso2micli"

var HomeDirectory = os.Getenv("HOME")
var ConfigDirPath = filepath.Join(HomeDirectory, ConfigDirName)

var PathSeparator_ = string(os.PathSeparator)

const EnvKeysAllFileName = "env_keys_all.yaml"

var EnvKeysAllFilePath = filepath.Join(ConfigDirPath, EnvKeysAllFileName)

const MainConfigFileName = "main_config.yaml"
const SampleMainConfigFileName = "main_config.yaml.sample"

var MainConfigFilePath = filepath.Join(ConfigDirPath, MainConfigFileName)
var SampleMainConfigFilePath = filepath.Join(ConfigDirPath, SampleMainConfigFileName)

const ExportDirName = "exported"

var ExportDirPath = filepath.Join(ConfigDirPath, ExportDirName)

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

// Server Address
const Host = "http://localhost"
const Port = "9091"
const Context = "admin"

const RESTAPIBase = Host + ":" + Port + "/" + Context + "/"
const PrefixCarbonApps = "applications"
const PrefixAPIs = "apis"
const PrefixServices = "services"
const PrefixProxyServices ="proxy-services"
const PrefixInboundEndpoints ="inbound-endpoints"
const PrefixEndpoints = "endpoints"
const PrefixSequences = "sequences"
const PrefixTasks = "tasks"
const PrefixServer = "server"