
#!/bin/bash

# Copyright (c) 2019, WSO2 Inc. (http://www.wso2.com). All Rights Reserved.
#
# This software is the property of WSO2 Inc. and its suppliers, if any.
# Dissemination of any information or reproduction of any material contained
# herein is strictly forbidden, unless permitted by WSO2 in accordance with
# the WSO2 Commercial License available at http://wso2.com/licenses.
# For specific language governing the permissions and limitations under this
# license, please see the license as well as any agreement you’ve entered into
# with WSO2 governing the purchase of this software and any associated services.

set -o xtrace

#Setting up the development environment
cd ../../cmd

#download all the dependencies
go mod vendor

#build Micro Integrator CLI
cd ../
make install

#Extract the compressed archive generated
cd ../../cmd/build

#for f in *.tar.gz
#do
#  tar zxvf ../../cmd/build
#done

tar zxvf ../../cmd/build

#start the application
cd ../../cmd/build/wso2mi-cli--f/bin
./mi