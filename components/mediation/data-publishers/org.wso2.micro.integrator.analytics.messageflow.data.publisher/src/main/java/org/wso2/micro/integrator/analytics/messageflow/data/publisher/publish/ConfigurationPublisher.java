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
package org.wso2.micro.integrator.analytics.messageflow.data.publisher.publish;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.aspects.flow.statistics.structuring.StructuringArtifact;
import org.apache.synapse.aspects.flow.statistics.structuring.StructuringElement;
import org.wso2.carbon.databridge.commons.Event;
import org.wso2.carbon.databridge.commons.utils.DataBridgeCommonsUtils;
import org.wso2.micro.integrator.analytics.messageflow.data.publisher.util.MediationDataPublisherConstants;

import java.util.ArrayList;

public class ConfigurationPublisher {
    private static Log log = LogFactory.getLog(ConfigurationPublisher.class);
    private static String streamId = DataBridgeCommonsUtils
            .generateStreamId(MediationDataPublisherConstants.CONFIG_STREAM_NAME,
                              MediationDataPublisherConstants.CONFIG_STREAM_VERSION);
    private static ObjectMapper mapper = new ObjectMapper();

    public static void process(StructuringArtifact structuringArtifact, int tenantId) {
        Object[] metaData = new Object[1];
        Object[] eventData = new Object[3];

        addMetaData(metaData, tenantId);
        addEventData(eventData, structuringArtifact);

        if (log.isDebugEnabled()) {
            log.debug("Before sending to analytic server ------");

            /*
             Logs to print data sending to analytics server. Use log4j.properties to enable this logs
              */
            for (int i = 0; i < eventData.length; i++) {
                log.debug("Section-" + i + " -> " + eventData[i]);
            }
        }

        publishToAgent(eventData, metaData);

        if (log.isDebugEnabled()) {
            log.debug("------ After sending to analytic server");
        }

    }

    private static void addMetaData(Object[] metaDataValueList, int tenantId) {

        /* [1] -> tenantId */
        metaDataValueList[0] = tenantId;
    }

    private static void addEventData(Object[] eventData, StructuringArtifact structuringArtifact) {

        /* [0] -> hashcode */
        eventData[0] = String.valueOf(structuringArtifact.getHashcode());

        /* [1] -> entryName */
        eventData[1] = String.valueOf(structuringArtifact.getName());

        ArrayList<StructuringElement> elementList = structuringArtifact.getList();
        String jsonString = null;
        try {
            jsonString = mapper.writeValueAsString(elementList);
        } catch (JsonProcessingException e) {
            log.error("Error while reading input stream. " + e.getMessage());
        }

        /* [2] -> configData */
        eventData[2] = jsonString;
    }

    private static void publishToAgent(Object[] eventData, Object[] metaData) {
        // Creating Event
        Event event = new Event(streamId, System.currentTimeMillis(), metaData, null, eventData);

        // Has to use try-publish for asynchronous publishing
        DataBridgePublisher.getDataPublisher().publish(event);
    }
}
