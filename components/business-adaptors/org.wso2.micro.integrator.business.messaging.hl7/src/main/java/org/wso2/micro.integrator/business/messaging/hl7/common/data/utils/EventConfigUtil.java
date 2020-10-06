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
package org.wso2.micro.integrator.business.messaging.hl7.common.data.utils;

import org.wso2.micro.integrator.business.messaging.hl7.common.data.MessageData;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * This is the utility class for fetching event data
 */
public class EventConfigUtil {

    public static List<Object> getCorrelationData(MessageData message) {
        List<Object> correlationData = new ArrayList<Object>(1);
        correlationData.add(message.getActivityId());
        return correlationData;
    }

    public static List<Object> getMetaData(MessageData message) {
        List<Object> metaData = new ArrayList<Object>(2);
        metaData.add(message.getHost());
        metaData.add(message.getServerName());
        return metaData;
    }

    public static List<Object> getEventData(MessageData message) {
        List<Object> payloadData = new ArrayList<Object>(7);
        payloadData.add(message.getPayload());
        payloadData.add(message.getType());
        payloadData.add(message.getTimestamp());
        payloadData.add(message.getMsgDirection());
        payloadData.add(message.getServiceName());
        payloadData.add(message.getOpName());
        payloadData.add(message.getStatus());
        return payloadData;
    }

    public static Map<String, String> getExtractedDataMap(MessageData message) {
        return message.getExtractedValues();
    }
}
