# ---------------------------------------------------------------------------
#  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
#
#  WSO2 Inc. licenses this file to you under the Apache License,
#  Version 2.0 (the "License"); you may not use this file except
#  in compliance with the License.
#  You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
#  Unless required by applicable law or agreed to in writing,
#  software distributed under the License is distributed on an
#  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
#  KIND, either express or implied. See the License for the
#  specific language governing permissions and limitations
#  under the License.
# ---------------------------------------------------------------------------

# set base Docker image to AdoptOpenJDK Ubuntu Docker image
FROM adoptopenjdk:11.0.10_9-jdk-hotspot-focal
LABEL maintainer="WSO2 Docker Maintainers <dev@wso2.org>"

# set Docker image build arguments
# build arguments for user/group configurations
ARG USER=wso2carbon
ARG USER_ID=802
ARG USER_GROUP=wso2
ARG USER_GROUP_ID=802
ARG USER_HOME=/home/${USER}
# docker image build arguments for Micro Integrator product installation
ARG MICROESB_NAME=wso2mi

# get the Micro Integrator version which is passed as a build argument and assert to be not empty
ARG MICROESB_VERSION
RUN test -n "$MICROESB_VERSION"

ARG MICROESB_SERVER=${MICROESB_NAME}-${MICROESB_VERSION}
ARG MICROESB_HOME=${USER_HOME}/${MICROESB_SERVER}

# create the user and group
RUN \
    groupadd --system -g ${USER_GROUP_ID} ${USER_GROUP} \
    && useradd --system --home ${USER_HOME} -g ${USER_GROUP_ID} -u ${USER_ID} ${USER}

# copy Micro Intergator product distribution to user's home directory
COPY --chown=wso2carbon:wso2 ${MICROESB_NAME}/ ${MICROESB_HOME}
# copy init script to user home
COPY --chown=wso2carbon:wso2 docker-entrypoint.sh ${USER_HOME}/

# set the user and work directory
USER ${USER}
WORKDIR ${USER_HOME}

# set environment variables
ENV WORKING_DIRECTORY=${USER_HOME} \
    WSO2_SERVER_HOME=${MICROESB_HOME}

# expose Micro Integrator ports
EXPOSE 8290 8253

# initiate container and execute the Micro Integrator product startup script
ENTRYPOINT ["/home/wso2carbon/docker-entrypoint.sh"]
