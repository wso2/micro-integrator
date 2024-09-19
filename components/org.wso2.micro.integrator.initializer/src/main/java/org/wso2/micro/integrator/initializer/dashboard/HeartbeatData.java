/*
 * Copyright (c) 2024, WSO2 LLC. (http://wso2.com).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.micro.integrator.initializer.dashboard;

import static org.wso2.micro.integrator.initializer.dashboard.Constants.PRODUCT_MI;

public class HeartbeatData {

    private final String product;
    private String groupId;
    private String nodeId;
    private long interval;
    private String mgtApiUrl;
    private long[] memoryMetrics;
    private int threadCount;
    private double[] cpuUsage;

    public HeartbeatData(String groupId, String nodeId, long interval, String mgtApiUrl, long[] memoryMetrics,
                         int threadCount, double[] cpuUsage) {
        this.product = PRODUCT_MI;
        this.groupId = groupId;
        this.nodeId = nodeId;
        this.interval = interval;
        this.mgtApiUrl = mgtApiUrl;
        this.memoryMetrics = memoryMetrics;
        this.threadCount = threadCount;
        this.cpuUsage = cpuUsage;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }


    public String getNodeId() {
        return nodeId;
    }

    public void setNodeId(String nodeId) {
        this.nodeId = nodeId;
    }

    public long getInterval() {
        return interval;
    }

    public void setInterval(long interval) {
        this.interval = interval;
    }

    public String getMgtApiUrl() {
        return mgtApiUrl;
    }

    public void setMgtApiUrl(String mgtApiUrl) {
        this.mgtApiUrl = mgtApiUrl;
    }

    public long[] getMemoryMetrics() {
        return memoryMetrics;
    }

    public void setMemoryMetrics(long[] memoryMetrics) {
        this.memoryMetrics = memoryMetrics;
    }

    public int getThreadCount() {
        return threadCount;
    }

    public void setThreadCount(int threadCount) {
        this.threadCount = threadCount;
    }

    public String getProduct() {
        return product;
    }

    public double[] getCpuUsage() {
        return cpuUsage;
    }

    public void setCpuUsage(double[] cpuUsage) {
        this.cpuUsage = cpuUsage;
    }
}
