/*
 * Copyright (c) (2017-2022), WSO2 Inc. (http://www.wso2.com).
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
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.micro.integrator.analytics.messageflow.data.publisher.publish.ei;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Output;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.aspects.flow.statistics.publishing.PublishingFlow;
import org.apache.synapse.aspects.flow.statistics.publishing.PublishingPayload;
import org.apache.synapse.aspects.flow.statistics.publishing.PublishingPayloadEvent;
import org.wso2.carbon.databridge.commons.Event;
import org.wso2.carbon.databridge.commons.utils.DataBridgeCommonsUtils;
import org.wso2.micro.integrator.analytics.data.publisher.util.PublisherUtil;
import org.wso2.micro.integrator.analytics.messageflow.data.publisher.publish.DataBridgePublisher;
import org.wso2.micro.integrator.analytics.messageflow.data.publisher.publish.StatisticsPublisher;
import org.wso2.micro.integrator.analytics.messageflow.data.publisher.util.MediationDataPublisherConstants;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.GZIPOutputStream;
import javax.xml.bind.DatatypeConverter;

public class EIStatisticsPublisher implements StatisticsPublisher {
    private static Log log = LogFactory.getLog(EIStatisticsPublisher.class);
    private static EIStatisticsPublisher instance = null;
    private static String streamId = DataBridgeCommonsUtils
            .generateStreamId(MediationDataPublisherConstants.STREAM_NAME,
                              MediationDataPublisherConstants.STREAM_VERSION);
    private static ThreadLocal<Kryo> kryoTL = new ThreadLocal<Kryo>() {
        @Override
        protected Kryo initialValue() {
            Kryo kryo = new Kryo();

            /**
             * When registering classes use for serialization, the numbering order should be preserved and
             * SHOULD follow the same convention from Analytic Server as well. Otherwise deserialization fails.
             */
            kryo.register(HashMap.class, 111);
            kryo.register(ArrayList.class, 222);
            kryo.register(PublishingPayload.class, 333);
            kryo.register(PublishingPayloadEvent.class, 444);

            return kryo;
        }
    };

    public static EIStatisticsPublisher GetInstance() {
        if (instance == null) {
            instance = new EIStatisticsPublisher();
        }
        return instance;
    }

    @Override
    public void process(PublishingFlow publishingFlow, int tenantId) {
        Object[] metaData = new Object[2];
        Object[] eventData = new Object[2];

        addMetaData(metaData, tenantId);
        addEventData(eventData, publishingFlow);

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

        /* [0] -> compressed */
        metaDataValueList[0] = true; // payload-data is in compressed form

        /* [1] -> tenantId */
        metaDataValueList[1] = tenantId;
    }

    private static void addEventData(Object[] eventData, PublishingFlow publishingFlow) {

        /* [0] -> messageId */
        eventData[0] = publishingFlow.getMessageFlowId();

        Map<String, Object> mapping = publishingFlow.getObjectAsMap();
        String host = null;
        String port = null;

        host = PublisherUtil.getHostAddress();

        mapping.put("host", host); // Adding host
        if (port != null) {
            mapping.put("port", port);
        }

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Output output = new Output(out);

        kryoTL.get().writeObject(output, mapping);

        output.flush();

        /* [1] -> flowData */
        eventData[1] = compress(out.toByteArray());

        if (log.isDebugEnabled()) {
            ObjectMapper mapper = new ObjectMapper();
            String jsonString = null;
            try {
                jsonString = mapper.writeValueAsString(mapping);
            } catch (JsonProcessingException e) {
                log.error("Unable to convert", e);
            }
            log.debug("Uncompressed data :");
            log.debug(jsonString);
        }
    }

    private static void publishToAgent(Object[] eventData, Object[] metaData) {
        // Creating Event
        Event event = new Event(streamId, System.currentTimeMillis(), metaData, null, eventData);

        // Has to use try-publish for asynchronous publishing
        DataBridgePublisher.getDataPublisher().publish(event);
    }

    /**
     * Compress the payload
     *
     * @param str
     * @return
     */
    private static String compress(byte[] str) {
        if (str == null || str.length == 0) {
            return null;
        }

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            GZIPOutputStream gzip = new GZIPOutputStream(out);
            gzip.write(str);
            gzip.close();
            return DatatypeConverter.printBase64Binary(out.toByteArray());
        } catch (IOException e) {
            log.error("Unable to compress data", e);
        }

        return null;
    }
}
