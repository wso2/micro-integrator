# ---------------------------------------------------------------------------
#  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

# set base Docker image to AdoptOpenJDK Alpine Docker image
FROM adoptopenjdk/openjdk8:jre8u212-b03-alpine
LABEL maintainer="WSO2 Docker Maintainers <dev@wso2.org>"

# docker image build arguments for user/group configurations
ARG USER=wso2carbon
ARG USER_ID=802
ARG USER_GROUP=wso2
ARG USER_GROUP_ID=802
ARG USER_HOME=/home/${USER}
# docker image build arguments for wso2 product installation
ARG MICROESB_NAME=wso2mi
ARG MICROESB_HOME=${USER_HOME}/${MICROESB_NAME}

# create the user and group
RUN \
    addgroup --system -g ${USER_GROUP_ID} ${USER_GROUP} \
    && adduser --system --home ${USER_HOME} -g ${USER_GROUP_ID} -u ${USER_ID} ${USER}

# copy the wso2 micro intergator product distribution to user's home directory
COPY --chown=wso2carbon:wso2 ${MICROESB_NAME}/ ${MICROESB_HOME}

# set the user and work directory
USER ${USER}
WORKDIR ${USER_HOME}

# expose micro-integrator ports
EXPOSE 8290 8253

# initiate container and execute the micro integrator product startup script
ENTRYPOINT ["/home/wso2carbon/wso2mi/bin/micro-integrator.sh"]
