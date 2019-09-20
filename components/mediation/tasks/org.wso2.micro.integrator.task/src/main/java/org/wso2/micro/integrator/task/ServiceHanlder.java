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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public abstract class ServiceHanlder {

    protected final Log log;
    private final List<Object> servicesTracker = new ArrayList<Object>();

    public ServiceHanlder() {
        log = LogFactory.getLog(this.getClass());
    }

    public List<Object> getServices() {
        return this.servicesTracker;
    }

    public void addService(Object service) {
        this.servicesTracker.add(service);
    }

    public void removeService(Object service) {
        this.servicesTracker.remove(service);
    }

    protected boolean assertEmpty(List<Object> services) {
        if (services.isEmpty()) {
            if (log.isDebugEnabled()) {
                log.debug("Currently There are no services implementations ");
            }
            return true;
        }
        if (log.isDebugEnabled()) {
            log.debug("Number of Service implementations : " + services.size());
        }
        return false;
    }

}
