/*
 * Copyright (c) 2023, WSO2 LLC. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
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
package org.wso2.micro.integrator.observability.metric.handler;

import org.apache.axis2.AxisFault;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.handlers.AbstractHandler;
import org.wso2.micro.integrator.core.util.MicroIntegratorBaseUtils;
import org.wso2.micro.integrator.observability.util.MetricConstants;
import org.wso2.micro.integrator.observability.util.MetricUtils;

public class DSMetricHandler extends AbstractHandler {

    private MetricReporter metricReporter;
    public DSMetricHandler() {
        metricReporter = MetricUtils.getMetricReporter();
    }

    @Override
    public InvocationResponse invoke(MessageContext messageContext) throws AxisFault {
        if (MicroIntegratorBaseUtils.isDataService(messageContext)) {
            String dataServiceName = messageContext.getAxisService().getName();
            if (messageContext.isProcessingFault()) {
                metricReporter.incrementCount(MetricConstants.DATA_SERVICE_REQUEST_COUNT_ERROR_TOTAL,
                        new String[]{dataServiceName, MetricConstants.DATA_SERVICE});
                metricReporter.observeTime(messageContext.getProperty(MetricConstants.DATA_SERVICE_LATENCY_TIMER));
            } else if (MetricConstants.MESSAGE_DIRECTION_IN.equalsIgnoreCase(
                    messageContext.getAxisMessage().getDirection())) {
                metricReporter.incrementCount(MetricConstants.DATA_SERVICE_REQUEST_COUNT_TOTAL,
                        new String[]{dataServiceName, MetricConstants.DATA_SERVICE});
                messageContext.setProperty(MetricConstants.DATA_SERVICE_LATENCY_TIMER,
                        metricReporter.getTimer(MetricConstants.DATA_SERVICE_LATENCY_SECONDS,
                                new String[]{dataServiceName,
                                        MetricConstants.DATA_SERVICE}));
            } else if (MetricConstants.MESSAGE_DIRECTION_OUT.equalsIgnoreCase(
                    messageContext.getAxisMessage().getDirection())) {
                metricReporter.observeTime(messageContext.getProperty(MetricConstants.DATA_SERVICE_LATENCY_TIMER));
            }
        }
        return InvocationResponse.CONTINUE;
    }
}
