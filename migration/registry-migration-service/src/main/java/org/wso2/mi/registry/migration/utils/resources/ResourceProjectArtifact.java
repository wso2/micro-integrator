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

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

@Root(name = "artifact")
public class ResourceProjectArtifact {

    @Element(name = "item", required = false)
    private ResourceItem item;

    @Element(name = "collection", required = false)
    private Collection collection;

    @Attribute
    private String name;

    @Attribute
    private String groupId;

    @Attribute
    private String version;

    @Attribute
    private String type;

    @Attribute
    private String serverRole;

    public ResourceProjectArtifact(String name, String groupId, String version, ResourceItem item) {
        this.name = name;
        this.groupId = groupId + ".resource";
        this.version = version;
        this.type = "registry/resource";
        this.serverRole = "EnterpriseIntegrator";
        this.item = item;
    }

    public ResourceProjectArtifact(String name, String groupId, String version, Collection collection) {
        this.name = name;
        this.groupId = groupId + ".resource";
        this.version = version;
        this.type = "registry/resource";
        this.serverRole = "EnterpriseIntegrator";
        this.collection = collection;
    }

    public ResourceItem getItem() {
        return item;
    }

    public void setItem(ResourceItem item) {
        this.item = item;
    }

    public Collection getCollection() {
        return collection;
    }

    public void setCollection(Collection collection) {
        this.collection = collection;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getServerRole() {
        return serverRole;
    }

    public void setServerRole(String serverRole) {
        this.serverRole = serverRole;
    }
}
