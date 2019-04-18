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
	"bufio"
	"encoding/xml"
	"errors"
	"fmt"
	"golang.org/x/crypto/ssh/terminal"
	"gopkg.in/resty.v1"
	"net/http"
	"os"
	"runtime"
	"strings"
)

// Invoke http-post request using go-resty
func InvokePOSTRequest(url string, headers map[string]string, body string) (*resty.Response, error) {

	resp, err := resty.R().SetHeaders(headers).SetBody(body).Post(url)

	return resp, err
}

// Invoke http-get request using go-resty
func InvokeGETRequest(url string, headers map[string]string) (*resty.Response, error) {

	resp, err := resty.R().SetHeaders(headers).Get(url)

	return resp, err
}

// Invoke http-put request using go-resty
func InvokeUPDATERequest(url string, headers map[string]string, body string) (*resty.Response, error) {

	resp, err := resty.R().SetHeaders(headers).SetBody(body).Put(url)

	return resp, err
}

// Invoke http-delete request using go-resty
func InvokeDELETERequest(url string, headers map[string]string) (*resty.Response, error) {

	resp, err := resty.R().SetHeaders(headers).Delete(url)

	return resp, err
}

func PromptForUsername() string {
	reader := bufio.NewReader(os.Stdin)

	fmt.Print("Enter Username: ")
	username, _ := reader.ReadString('\n')

	return username
}

func PromptForPassword() string {
	fmt.Print("Enter Password: ")
	bytePassword, _ := terminal.ReadPassword(0)
	password := string(bytePassword)
	fmt.Println()
	return password
}

// return a string containing the file name, function name
// and the line number of a specified entry on the call stack
func WhereAmI(depthList ...int) string {
	var depth int
	if depthList == nil {
		depth = 1
	} else {
		depth = depthList[0]
	}
	function, file, line, _ := runtime.Caller(depth)
	return fmt.Sprintf("File: %s Line: %d Function: %s ", chopPath(file), line, runtime.FuncForPC(function).Name())
}

// return the source filename after the last slash
func chopPath(original string) string {
	i := strings.LastIndex(original, "/")
	if i == -1 {
		return original
	} else {
		return original[i+1:]
	}
}

func PrintList(list []string) {
	for _, item := range list {
		fmt.Println(item)
	}
}

// GetArtifactList
// @return count (no. of Artifacts)
// @return array of Artifact names
// @return error
func GetArtifactList(url string) (int32, []string, error) {

	Logln(LogPrefixInfo+"URL:", url)

	headers := make(map[string]string)

	resp, err := InvokeGETRequest(url, headers)

	if err != nil {
		HandleErrorAndExit("Unable to connect to "+url, err)
	}

	Logln(LogPrefixInfo+"Response:", resp.Status())

	if resp.StatusCode() == http.StatusOK {
		apiListResponse := &ListResponse{}
		unmarshalError := xml.Unmarshal([]byte(resp.Body()), &apiListResponse)

		if unmarshalError != nil {
			HandleErrorAndExit(LogPrefixError+"invalid XML response", unmarshalError)
		}
		return apiListResponse.Count, apiListResponse.List, nil
	} else {
		return 0, nil, errors.New(resp.Status())
	}
}

// UnmarshalData
// @param url: url of rest api
// @param model: struct object
// @return struct object
// @return error
func UnmarshalData(url string, model interface{}) (interface{}, error) {

	Logln(LogPrefixInfo+"URL:", url)

	headers := make(map[string]string)

	resp, err := InvokeGETRequest(url, headers)

	if err != nil {
		HandleErrorAndExit("Unable to connect to "+url, err)
	}

	Logln(LogPrefixInfo+"Response:", resp.Status())

	if resp.StatusCode() == http.StatusOK {
		response := model
		unmarshalError := xml.Unmarshal([]byte(resp.Body()), &response)

		if unmarshalError != nil {
			HandleErrorAndExit(LogPrefixError+"invalid XML response", unmarshalError)
		}
		return response, nil
	} else {
		return nil, errors.New(resp.Status())
	}
}
