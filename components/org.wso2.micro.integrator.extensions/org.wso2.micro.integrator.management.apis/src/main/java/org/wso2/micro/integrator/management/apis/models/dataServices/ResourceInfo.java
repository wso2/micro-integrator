/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.micro.integrator.management.apis.models.dataServices;

import org.wso2.micro.integrator.dataservices.core.engine.QueryParam;

import java.util.List;

public class ResourceInfo {

    private String resourcePath;
    private String resourceMethod;
    private String resourceQuery;
    private List<QueryParam> queryParams;

    public void setResourcePath(String resourcePath) {

        this.resourcePath = resourcePath;
    }

    public void setResourceMethod(String resourceMethod) {

        this.resourceMethod = resourceMethod;
    }

    public void setResourceQuery(String resourceQuery) {

        this.resourceQuery = resourceQuery;
    }

    public void setQueryParams(List<QueryParam> queryParams) {

        this.queryParams = queryParams;
    }
}
