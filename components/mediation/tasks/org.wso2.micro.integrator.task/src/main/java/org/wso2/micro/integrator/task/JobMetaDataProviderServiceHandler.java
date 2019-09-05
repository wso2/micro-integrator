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
package org.wso2.micro.integrator.task;


import org.wso2.carbon.task.services.JobMetaDataProviderService;

import java.util.ArrayList;
import java.util.List;


/**
 *  ServiceHandler for the JobMetaDataProviderService 
 */
public class JobMetaDataProviderServiceHandler extends ServiceHanlder {


    public List<String> getJobClasses() {
        final List<String> tobeReturn = new ArrayList<String>();
        final List<Object> services = getServices();
        if (!assertEmpty(services)) {
            for (Object serviceObject : services) {
                if (serviceObject instanceof JobMetaDataProviderService) {
                    tobeReturn.add(((JobMetaDataProviderService) serviceObject).getJobClass());
                }
            }
        }
        return tobeReturn;
    }

    public List<String> getJobGroups() {
        final List<String> tobeReturn = new ArrayList<String>();
        final List<Object> services = getServices();
        if (!assertEmpty(services)) {
            for (Object serviceObject : services) {
                if (serviceObject instanceof JobMetaDataProviderService) {
                    tobeReturn.add(((JobMetaDataProviderService) serviceObject).getJobGroup());
                }
            }
        }
        return tobeReturn;
    }

    public List<String> getTaskManagementServiceImplementers() {
        final List<String> tobeReturn = new ArrayList<String>();
        final List<Object> services = getServices();
        if (!assertEmpty(services)) {
            for (Object serviceObject : services) {
                if (serviceObject instanceof JobMetaDataProviderService) {
                    tobeReturn.add(
                            ((JobMetaDataProviderService) serviceObject).
                                    getTaskManagementServiceImplementer());
                }
            }
        }
        return tobeReturn;
    }

    public String getTaskManagementServiceImplementer(String jobGroupGiven) {
        if (jobGroupGiven == null || "".equals(jobGroupGiven)) {
            return jobGroupGiven;
        }
        final List<Object> services = getServices();
        if (!assertEmpty(services)) {
            for (Object serviceObject : services) {
                if (serviceObject instanceof JobMetaDataProviderService) {
                    JobMetaDataProviderService service = (JobMetaDataProviderService) serviceObject;
                    String jobGroup = service.getJobGroup();
                    if (jobGroupGiven.equals(jobGroup)) {
                        return service.getTaskManagementServiceImplementer();
                    }
                }
            }
        }
        return "";
    }

}
