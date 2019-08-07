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

INPUT_DIR=$2
OUTPUT_DIR=$4

echo "Running test.sh file"

HOME=`pwd`

TEST_SCRIPT=test.sh

echo "working Directory : ${HOME}"
echo "input directory : ${INPUT_DIR}"
echo "output directory : ${OUTPUT_DIR}"

export input_dir="${INPUT_DIR}"

optspec=":hiom-:"
while getopts "$optspec" optchar; do
    case "${optchar}" in
        -)
            case "${OPTARG}" in
                input-dir)
                    val="${!OPTIND}"; OPTIND=$(( $OPTIND + 1 ))
                    INPUT_DIR=$val
                    ;;
                output-dir)
                    val="${!OPTIND}"; OPTIND=$(( $OPTIND + 1 ))
                    OUTPUT_DIR=$val
                    ;;
                mvn-opts)
                    val="${!OPTIND}"; OPTIND=$(( $OPTIND + 1 ))
                    MAVEN_OPTS=$val
                    ;;
                *)
                    usage
                    if [ "$OPTERR" = 1 ] && [ "${optspec:0:1}" != ":" ]; then
                        echo "Unknown option --${OPTARG}" >&2
                    fi
                    ;;
            esac;;
        h)
            usage
            exit 2
            ;;
        o)
            OUTPUT_DIR=$val
            ;;
        m)
            MVN_OPTS=$val
            ;;
        i)
            INPUT_DIR=$val
            ;;
        *)
            usage
            if [ "$OPTERR" != 1 ] || [ "${optspec:0:1}" = ":" ]; then
                echo "Non-option argument: '-${OPTARG}'" >&2
            fi
            ;;
    esac
done

export DATA_BUCKET_LOCATION=${INPUT_DIR}

git clone https://github.com/wso2/micro-integrator/tree/master
cd mi_docker_tests
mvn clean install

echo "Copying surefire-reports to ${OUTPUT_DIR}"

mkdir -p ${OUTPUT_DIR}/scenarios/mi-sample-tests
find ./* -name "surefire-reports" -exec cp --parents -r {} ${OUTPUT_DIR}/scenarios/mi-sample-tests \;
