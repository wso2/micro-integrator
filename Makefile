# --------------------------------------------------------------------
# Copyright (c) 2019, WSO2 Inc. (http://wso2.com) All Rights Reserved.
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
# -----------------------------------------------------------------------

# do not indent (Makefile syntax, not bash)
ifdef MVN_RELEASE_VERSION
# maven release
$(info MVN_RELEASE_VERSION is $(MVN_RELEASE_VERSION))
VERSION=$(MVN_RELEASE_VERSION)
else
# not a maven release
ifdef POM_VERSION
VERSION=$(POM_VERSION)
$(info POM_VERSION is $(POM_VERSION))
endif
endif
# one of these will be set to VERSION in Jenkins Production Environment


.PHONY: install
install:
	mvn clean install
	cd cmd && ./build.sh -t mi.go -v ${VERSION} -f

.PHONY: install-cli
install-cli:
	cd cmd && ./build.sh -t mi.go -v ${VERSION} -f

.PHONY: install-cli-local
install-cli-local:
	$(eval VERSION := $(shell mvn -q -Dexec.executable=echo -Dexec.args='$${project.version}' --non-recursive exec:exec))
	cd cmd && ./build.sh -t mi.go -v ${VERSION} -f

