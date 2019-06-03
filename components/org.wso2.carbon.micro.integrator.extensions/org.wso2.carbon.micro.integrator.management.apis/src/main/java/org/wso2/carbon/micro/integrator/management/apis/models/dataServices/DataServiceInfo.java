/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.micro.integrator.management.apis.models.dataServices;

import java.util.ArrayList;
import java.util.List;
import com.google.gson.annotations.SerializedName;

public class DataServiceInfo {
    private String serviceName;
    private String serviceDescription;
    private String serviceGroupName;

    @SerializedName("wsdl1_1")
    private String wsdl11;

    @SerializedName("wsdl2_0")
    private String wsdl20;
    private List<Query> queries;

    public DataServiceInfo(String serviceName, String serviceDescription, String serviceGroupName, String wsdl11,
                           String wsdl20) {
        this.serviceName = serviceName;
        this.serviceDescription = serviceDescription;
        this.serviceGroupName = serviceGroupName;
        this.wsdl11 = wsdl11;
        this.wsdl20 = wsdl20;
        queries = new ArrayList<>();
    }

    public String getServiceName() {
        return serviceName;
    }

    public String getServiceDescription() {
        return serviceDescription;
    }

    public String getServiceGroupName() {
        return serviceGroupName;
    }

    public String getWsdl11() {
        return wsdl11;
    }

    public String getWsdl20() {
        return wsdl20;
    }

    public List<Query> getQueries() {
        return queries;
    }

    public void addQuery(Query query) {
        queries.add(query);
    }
}
