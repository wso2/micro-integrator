# Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
#
# WSO2 Inc. licenses this file to you under the Apache License,
# Version 2.0 (the "License"); you may not use this file except
# in compliance with the License.
# You may obtain a copy of the License at
#
# http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing,
# software distributed under the License is distributed on an
# "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
# KIND, either express or implied. See the License for the
# specific language governing permissions and limitations
# under the License.

#!/bin/bash

set -o xtrace

echo "Executing Environment Setup script"
platform=$(uname -s)
bitType=$(arch)
BASEDIR=$(dirname "$0")

echo "Platform : $platform"
echo "BitType : $bitType"

#Setting up the CLI environment
#Check if the cli build is available in the location
cd $BASEDIR

#Get the CLI distribution from the latest jenkins build
DISTRIBUTION_NAME=$(eval "curl -s https://wso2.org/jenkins/job/products/job/product-mi-tooling/lastSuccessfulBuild/api/json | python3 -c \"import sys, json; print(json.load(sys.stdin)['artifacts'][0]['fileName'])\"")
echo "name: $DISTRIBUTION_NAME"
cd ../../../target

#Download the latest CLI distribution
echo "downloading https://wso2.org/jenkins/job/products/job/product-mi-tooling/lastSuccessfulBuild/artifact/cmd/build/$DISTRIBUTION_NAME"
wget "https://wso2.org/jenkins/job/products/job/product-mi-tooling/lastSuccessfulBuild/artifact/cmd/build/$DISTRIBUTION_NAME"

#Extract the CLI
mkdir wso2mi-cli
tar -xvzf "$DISTRIBUTION_NAME" --strip=1 -C wso2mi-cli

#start the application
cd wso2mi-cli/bin
echo "ClI setup Complete"
