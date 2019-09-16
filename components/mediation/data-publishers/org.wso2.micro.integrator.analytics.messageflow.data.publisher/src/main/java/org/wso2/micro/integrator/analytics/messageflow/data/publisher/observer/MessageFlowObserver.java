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

package org.wso2.micro.integrator.analytics.messageflow.data.publisher.observer;

import org.apache.synapse.aspects.flow.statistics.publishing.PublishingFlow;

/**
 * This interface can be used to register consumers into the MediationStatisticsStore
 * and retrieve statistics updates from the data store.
 */
public interface MessageFlowObserver {

    /**
     * Clean up this observer and prepare for shutdown
     */
    public void destroy();

    /**
     * Receive a statistics update from the statistics store/provider. The cumulative records
     * received from this method will be 'null' for the very first update sent by the data
     * store. This is because for the very first update there are no cumulative data in the
     * store. The implementations of this method should not attempt to modify the provided
     * StatisticsRecord instances. If such modifications are necessary the StatisticsRecord
     * objects should be first copied using the copy constructor. Also implementations of the
     * updateStatistics method should finish quickly. Long running tasks should be executed using
     * separate threads to prevent the notifier thread getting blocked.
     *
     * @param snapshot Current latest update containing resource specific data, category specific
     *                 data, cumulative data and error logs
     */
    public void updateStatistics(PublishingFlow snapshot);

}
