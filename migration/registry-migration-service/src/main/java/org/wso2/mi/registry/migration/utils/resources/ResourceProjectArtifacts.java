/*
Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
* WSO2 Inc. licenses this file to you under the Apache License,
* Version 2.0 (the "License"); you may not use this file except
* in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied. See the License for the
* specific language governing permissions and limitations
* under the License.
*/

package org.wso2.mi.registry.migration.utils.resources;

import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

import java.util.List;

@Root(name = "artifacts")
public class ResourceProjectArtifacts {

    @ElementList(inline = true)
    private List<ResourceProjectArtifact> artifacts;

    public ResourceProjectArtifacts(List<ResourceProjectArtifact> artifacts) {
        this.artifacts = artifacts;
    }

    public List<ResourceProjectArtifact> getArtifacts() {
        return artifacts;
    }

    public void setArtifacts(List<ResourceProjectArtifact> artifacts) {
        this.artifacts = artifacts;
    }
}
