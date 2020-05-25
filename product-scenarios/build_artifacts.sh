#!/bin/bash

# Copyright (c) 2020, WSO2 Inc. (http://wso2.com) All Rights Reserved.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
# http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

HOME=`pwd`
TEST_SCRIPT=prepare_artifacts.sh
INPUT_DIR=${HOME}/micro-integrator/product-scenarios
CAPP_DIR=$1
GIT_BRANCH=$2


function runTestProfile()
{
    mvn clean install -Dmaven.repo.local="${HOME}/m2" -Dinvocation.uuid="$UUID" -Ddata.bucket.location="${INPUT_DIR}" \
    -Dorg.slf4j.simpleLogger.log.org.apache.maven.cli.transfer.Slf4jMavenTransferListener=info -fae -B -f ./pom.xml \
     -P $1

     find -iname '*-synapseConfigCompositeApplication_1.0.0.car' -exec cp {} ${CAPP_DIR} \;
     rm -f ${CAPP_DIR}/1_6_10-synapseConfigCompositeApplication_1.0.0.car
}

echo "working Directory : ${HOME}"
echo "input directory : ${INPUT_DIR}"
echo "capp directory : ${CAPP_DIR}"
echo "git branch : ${GIT_BRANCH}"

#export DATA_BUCKET_LOCATION=${INPUT_DIR}

#=============== Execute Artifact Generation ===============================================

#generate uuid representing the test run
UUID=$(uuidgen)

git clone -b ${GIT_BRANCH} --single-branch https://github.com/wso2/micro-integrator.git
cd ${INPUT_DIR}

runTestProfile profile_artifacts
       
echo "prepare_artifacts.sh execution completed."
