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

getPomVersion(){
    VERSION=$(cat pom.xml | grep "^    <version>.*</version>$" | awk -F'[><]' '{print $3}');
    echo "Version : $VERSION"
}

buildCli(){
    go mod vendor
    sleep 10

    #build Micro Integrator CLI
    echo "Build Micro Integrator CLI"
    cd ../

    #get the version from the pom
    getPomVersion
    cd cmd
    ./build.sh -t mi.go -v ${VERSION} -f
}

#Setting up the CLI environment
#Check if the cli build is available in the location
setup(){

cd $BASEDIR

DIR="../../../../../cmd/build"

if [ -d "$DIR" ]; then
    echo "CLI build exists. Hence skipping the cli environment setup"
    cd ../../../../../cmd/build
    pwd
    ls
    tar -xvzf wso2mi-cli-$VERSION-linux-x64.tar.gz
else
    cd ../../../../../cmd
    go mod vendor
    sleep 10

    #build Micro Integrator CLI
    echo "Build Micro Integrator CLI"
    cd ../

    #get the version from the pom
    getPomVersion
    cd cmd
    ./build.sh -t mi.go -v ${VERSION} -f

    cd build
    pwd
    ls
    tar -xvzf wso2mi-cli-$VERSION-linux-x64.tar.gz

    #start the application
    cd wso2mi-cli-$VERSION/bin
    echo "ClI setup Complete"
fi

}

setup