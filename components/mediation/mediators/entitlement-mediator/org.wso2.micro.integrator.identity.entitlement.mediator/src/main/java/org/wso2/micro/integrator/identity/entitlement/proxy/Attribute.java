/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.micro.integrator.identity.entitlement.proxy;

public class Attribute {

    private String type;
    private String id;
    private String value;
    private String category;
    private String content;

    public Attribute(String category, String id, String type, String value) {
        this.category = category;
        this.id = id;
        this.type = type;
        this.value = value;
    }

    public Attribute(String category, String id, String type, String value, String content) {

        this.category = category;
        this.id = id;
        this.type = type;
        this.value = value;
        this.content = content;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getContent() {

        return content;
    }

    public void setContent(String content) {

        this.content = content;
    }

    @Override
    public int hashCode() {
        int result = 1;
        result = 7 * result + ((type == null) ? 0 : type.hashCode());
        result = 17 * result + ((id == null) ? 0 : id.hashCode());
        result = 37 * result + ((value == null) ? 0 : value.hashCode());
        result = 57 * result + ((category == null) ? 0 : category.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        Attribute attribute = (Attribute) o;

        if (!type.equals(attribute.type))
            return false;
        if (!id.equals(attribute.id))
            return false;
        if (!value.equals(attribute.value))
            return false;
        return category.equals(attribute.category);

    }
}
