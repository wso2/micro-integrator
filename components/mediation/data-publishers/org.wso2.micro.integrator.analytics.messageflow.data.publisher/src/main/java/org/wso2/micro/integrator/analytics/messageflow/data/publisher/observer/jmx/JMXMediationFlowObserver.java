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

package org.wso2.micro.integrator.analytics.messageflow.data.publisher.observer.jmx;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.aspects.flow.statistics.publishing.PublishingEvent;
import org.apache.synapse.aspects.flow.statistics.publishing.PublishingFlow;
import org.apache.synapse.aspects.flow.statistics.util.StatisticsConstants;
import org.apache.synapse.commons.jmx.MBeanRegistrar;
import org.wso2.micro.integrator.analytics.messageflow.data.publisher.observer.MessageFlowObserver;
import org.wso2.micro.integrator.analytics.messageflow.data.publisher.observer.TenantInformation;
import org.wso2.micro.integrator.analytics.messageflow.data.publisher.observer.jmx.data.StatisticCollectionViewMXBean;
import org.wso2.micro.integrator.analytics.messageflow.data.publisher.observer.jmx.data.StatisticsCompositeObject;
import org.wso2.micro.integrator.analytics.messageflow.data.publisher.observer.jmx.data.SummeryStatisticObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Publish statistics data for JMX monitoring.
 */
public class JMXMediationFlowObserver implements StatisticCollectionViewMXBean, MessageFlowObserver, TenantInformation {

    private static final Log log = LogFactory.getLog(JMXMediationFlowObserver.class);

    public static final String MBEAN_CATEGORY = "Mediation Flow Statistic View";

    public static final String MBEAN_ID = "MediationFlowStatisticView_";

    private int tenantId = -1234;

    private final Map<String, SummeryStatisticObject> proxyStatistics = new HashMap<>();

    private final Map<String, SummeryStatisticObject> apiStatistics = new HashMap<>();

    private final Map<String, SummeryStatisticObject> sequenceStatistics = new HashMap<>();

    private final Map<String, SummeryStatisticObject> inboundEndpointStatistics = new HashMap<>();

    private final Map<String, SummeryStatisticObject> endpointStatistics = new HashMap<>();

    public JMXMediationFlowObserver(int tenantId) {
        this.tenantId = tenantId;
        MBeanRegistrar.getInstance().registerMBean(this, MBEAN_CATEGORY, MBEAN_ID + this.tenantId);
    }

    @Override
    public void resetAPIStatistics() {
        apiStatistics.clear();
    }

    @Override
    public void resetProxyStatistics() {
        proxyStatistics.clear();
    }

    @Override
    public void resetSequenceStatistics() {
        sequenceStatistics.clear();
    }

    @Override
    public void resetInboundEndpointStatistics() {
        inboundEndpointStatistics.clear();
    }

    @Override
    public void resetEndpointStatistics() {
        endpointStatistics.clear();
    }

    @Override
    public void resetAllStatistics() {
        resetAPIStatistics();
        resetProxyStatistics();
        resetSequenceStatistics();
        resetInboundEndpointStatistics();
        resetEndpointStatistics();
    }

    @Override
    public StatisticsCompositeObject getProxyServiceJmxStatistics(String proxyName) {
        SummeryStatisticObject statisticsObject = proxyStatistics.get(proxyName);
        if (statisticsObject != null) {
            return statisticsObject.getJmxObject();
        }
        return new StatisticsCompositeObject();
    }

    @Override
    public StatisticsCompositeObject getSequenceJmxStatistics(String sequenceName) {
        SummeryStatisticObject statisticsObject = sequenceStatistics.get(sequenceName);
        if (statisticsObject != null) {
            return statisticsObject.getJmxObject();
        }
        return new StatisticsCompositeObject();
    }

    @Override
    public StatisticsCompositeObject getApiJmxStatistics(String apiName) {
        SummeryStatisticObject statisticsObject = apiStatistics.get(apiName);
        if (statisticsObject != null) {
            return statisticsObject.getJmxObject();
        }
        return new StatisticsCompositeObject();
    }

    @Override
    public StatisticsCompositeObject getInboundEndpointJmxStatistics(String inboundEndpointName) {
        SummeryStatisticObject statisticsObject = inboundEndpointStatistics.get(inboundEndpointName);
        if (statisticsObject != null) {
            return statisticsObject.getJmxObject();
        }
        return new StatisticsCompositeObject();
    }

    @Override
    public StatisticsCompositeObject getEndpointJmxStatistics(String endpointName) {
        SummeryStatisticObject statisticsObject = endpointStatistics.get(endpointName);
        if (statisticsObject != null) {
            return statisticsObject.getJmxObject();
        }
        return new StatisticsCompositeObject();
    }

    @Override
    public void destroy() {
        MBeanRegistrar.getInstance().unRegisterMBean("Mediation Flow Statistic View", "MediationFlowStatisticView");
    }

    @Override
    public void updateStatistics(PublishingFlow snapshot) {
        for (PublishingEvent event : snapshot.getEvents()) {
            String componentType = event.getComponentType();
            //Mediator is the most common component type, therefore checking it first and ignoring will save time
            if (!StatisticsConstants.FLOW_STATISTICS_MEDIATOR.equals(componentType)) {
                if (StatisticsConstants.FLOW_STATISTICS_ENDPOINT.equals(componentType)) {
                    SummeryStatisticObject statisticObject = endpointStatistics.get(event.getComponentName());
                    if (statisticObject == null) {
                        endpointStatistics.put(event.getComponentName(), new SummeryStatisticObject(event));
                    } else {
                        statisticObject.updateStatistics(event);
                    }
                } else if (StatisticsConstants.FLOW_STATISTICS_SEQUENCE.equals(componentType)) {
                    SummeryStatisticObject statisticObject = sequenceStatistics.get(event.getComponentName());
                    if (statisticObject == null) {
                        sequenceStatistics.put(event.getComponentName(), new SummeryStatisticObject(event));
                    } else {
                        statisticObject.updateStatistics(event);
                    }
                } else if (StatisticsConstants.FLOW_STATISTICS_PROXYSERVICE.equals(componentType)) {
                    SummeryStatisticObject statisticObject = proxyStatistics.get(event.getComponentName());
                    if (statisticObject == null) {
                        proxyStatistics.put(event.getComponentName(), new SummeryStatisticObject(event));
                    } else {
                        statisticObject.updateStatistics(event);
                    }
                } else if (StatisticsConstants.FLOW_STATISTICS_API.equals(componentType)) {
                    SummeryStatisticObject statisticObject = apiStatistics.get(event.getComponentName());
                    if (statisticObject == null) {
                        apiStatistics.put(event.getComponentName(), new SummeryStatisticObject(event));
                    } else {
                        statisticObject.updateStatistics(event);
                    }
                } else if (StatisticsConstants.FLOW_STATISTICS_INBOUNDENDPOINT.equals(componentType)) {
                    SummeryStatisticObject statisticObject = inboundEndpointStatistics.get(event.getComponentName());
                    if (statisticObject == null) {
                        inboundEndpointStatistics.put(event.getComponentName(), new SummeryStatisticObject(event));
                    } else {
                        statisticObject.updateStatistics(event);
                    }
                }
            }
        }
    }

    @Override
    public int getTenantId() {
        return tenantId;
    }

    @Override
    public void setTenantId(int i) {
        tenantId = i;
    }
}
