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

package org.wso2.mi.registry.migration;

import org.wso2.carbon.registry.properties.stub.utils.xsd.Property;
import org.wso2.mi.registry.migration.utils.resources.ResourceProperty;

import java.util.ArrayList;
import java.util.List;

class RegistryResource {

    private String resourceName;
    private String resourcePath;
    private String parentPath;
    private Property[] properties;
    private boolean export = false;

    String getResourceName() {
        return resourceName;
    }

    void setResourceName(String resourceName) {
        this.resourceName = resourceName;
    }

    String getResourcePath() {
        return resourcePath;
    }

    void setResourcePath(String resourcePath) {
        this.resourcePath = resourcePath;
    }

    String getParentPath() {
        return parentPath;
    }

    void setParentPath(String parentPath) {
        this.parentPath = parentPath;
    }

    List<ResourceProperty> getProperties() {
        List<ResourceProperty> resourcePropertyList = new ArrayList<>();
        if (properties!= null) {
            for (Property prop : properties) {
                resourcePropertyList.add(new ResourceProperty(prop.getKey(), prop.getValue()));
            }
        }
        return resourcePropertyList;
    }

    void setProperties(Property[] properties) {
        this.properties = properties;
    }

    boolean canExport() {
        return export;
    }

    void updateExportStatus(boolean export) {
        this.export = export;
    }

    String getFullQualifiedResourceName() {
        return resourcePath.replaceAll("/", "_");
    }
}
