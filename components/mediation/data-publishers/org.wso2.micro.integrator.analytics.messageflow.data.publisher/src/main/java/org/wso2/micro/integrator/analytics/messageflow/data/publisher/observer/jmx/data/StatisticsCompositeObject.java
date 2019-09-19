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

package org.wso2.micro.integrator.analytics.messageflow.data.publisher.observer.jmx.data;

import java.beans.ConstructorProperties;

/**
 * Composite data object which is passed to the JMX Agent.
 */
public class StatisticsCompositeObject {

    public static final String STATISTIC_DATA_NOT_FOUND = "Not Found";
    private final String name;

    private long maxProcessingTime = 0;

    private long minProcessingTime = 0;

    private long avgProcessingTime = 0;

    private long numberOfInvocations = 0;

    private long faultCount = 0;

    @ConstructorProperties({ "name", "maxProcessingTime", "minProcessingTime", "avgProcessingTime",
            "numberOfInvocations", "faultCount" })
    public StatisticsCompositeObject(String name, long maxProcessingTime, long minProcessingTime,
                                     long avgProcessingTime, long numberOfInvocations, long faultCount) {
        this.name = name;
        this.faultCount = faultCount;
        this.maxProcessingTime = maxProcessingTime;
        this.minProcessingTime = minProcessingTime;
        this.avgProcessingTime = avgProcessingTime;
        this.numberOfInvocations = numberOfInvocations;
    }

    public StatisticsCompositeObject() {
        name = STATISTIC_DATA_NOT_FOUND;
    }

    public long getFaultCount() {
        return faultCount;
    }

    public String getName() {
        return name;
    }

    public long getMaxProcessingTime() {
        return maxProcessingTime;
    }

    public long getMinProcessingTime() {
        return minProcessingTime;
    }

    public long getAvgProcessingTime() {
        return avgProcessingTime;
    }

    public long getNumberOfInvocations() {
        return numberOfInvocations;
    }

}
