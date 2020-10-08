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

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

@Root(name="resources")
public class RegistryInfo {

    @Element(name="item", required = false)
    private ResourceItem resourceItem;

    @Element(name="collection", required = false)
    private Collection collection;

    public RegistryInfo(ResourceItem resourceItem) {
        this.resourceItem = resourceItem;
    }

    public RegistryInfo(Collection collection) {
        this.collection = collection;
    }

    public ResourceItem getResourceItem() {
        return resourceItem;
    }

    public void setResourceItem(ResourceItem resourceItem) {
        this.resourceItem = resourceItem;
    }

    public Collection getCollection() {
        return collection;
    }

    public void setCollection(Collection collection) {
        this.collection = collection;
    }
}
