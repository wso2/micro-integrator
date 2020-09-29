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

package org.wso2.micro.integrator.business.messaging.hl7.common.data.conf;

import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.model.Composite;
import ca.uhn.hl7v2.model.GenericPrimitive;
import ca.uhn.hl7v2.model.Message;
import ca.uhn.hl7v2.model.Primitive;
import ca.uhn.hl7v2.model.Segment;
import ca.uhn.hl7v2.model.Structure;
import ca.uhn.hl7v2.model.Type;
import ca.uhn.hl7v2.model.Varies;
import ca.uhn.hl7v2.util.Terser;
import org.apache.axis2.context.MessageContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.micro.integrator.analytics.data.publisher.util.PublisherUtil;
import org.wso2.micro.integrator.business.messaging.hl7.common.HL7Constants;
import org.wso2.micro.integrator.business.messaging.hl7.common.data.MessageData;
import org.wso2.micro.integrator.core.services.CarbonServerConfigurationService;

import java.util.HashMap;
import java.util.Map;

/**
 * This class holds HL7 message extraction operations
 */
public class HL7MessagePublisherConfig {

    private static final Log log = LogFactory.getLog(HL7MessagePublisherConfig.class);

    private static final String NAME = "Name";
    private String serverName = null;

    public HL7MessagePublisherConfig() {
    }

    /**
     * This extracts the incoming HL7 message
     *
     * @param message
     * @return MessageData
     * @throws HL7Exception
     */
    public MessageData createMessage(Message message, MessageContext msgCtx) throws HL7Exception {
        MessageData messageData = new MessageData();
        Map<String, String> extractedValues = createCustomMap(message);
        messageData.setExtractedValues(extractedValues);
        messageData.setPayload(message.encode());
        messageData.setOpName(msgCtx.getAxisOperation().getName().getLocalPart());
        messageData.setServiceName(msgCtx.getAxisService().getName());
        messageData.setMsgDirection(HL7Constants.OUT_DIRECTION);
        messageData.setServerName(getServerName());
        Terser terser = new Terser(message);
        String activityId = terser.get("/MSH-10");

        if (activityId != null) {
            messageData.setActivityId(activityId);
        } else {
            messageData.setActivityId(
                    String.valueOf(System.nanoTime()) + Math.round(Math.random() * HL7Constants.ACTIVITY_ID_GEN));
        }

        messageData.setStatus((String) msgCtx.getProperty(HL7Constants.HL7_DEFAULT_VALIDATION_PASSED));
        messageData.setHost(PublisherUtil.getHostAddress());
        messageData.setTimestamp(System.currentTimeMillis());
        messageData.setType(HL7Constants.TRANSPORT_NAME);
        return messageData;
    }

    /**
     * This creates arbitrary data map which contains extracted values of the HL7 message
     *
     * @param message
     * @return Map
     * @throws HL7Exception
     */
    private Map<String, String> createCustomMap(Message message) throws HL7Exception {
        String[] segments;
        HashMap<String, String> elements = new HashMap<String, String>();
        if (message != null) {
            segments = message.getNames();
        } else {
            return null;
        }
        for (String segmentName : segments) {
            try {
                Structure[] structures = message.getAll(segmentName);
                String keyA = segmentName;
                for (int y = 0; y < structures.length; y++) {
                    if (y > 0) {
                        keyA = segmentName + "[" + y + "]";
                    }
                    Structure structure = structures[y];

                    if (structure instanceof Segment) {
                        Segment segment = (Segment) structure;
                        String[] fieldNames = segment.getNames();

                        for (int i = 1; i < segment.numFields(); i++) {
                            Type[] fields = segment.getField(i);
                            for (int x = 0; x < fields.length; x++) {
                                String value = null;
                                String keyB = keyA + "." + fieldNames[i - 1].replaceAll("\\s+", "");
                                if (x > 0) {
                                    keyB = keyA + "." + fieldNames[i - 1].replaceAll("\\s+", "") + "[" + x + "]";
                                }
                                Type field = fields[x];

                                if (field instanceof Composite) {

                                    evaluateComposite((Composite) field, elements, keyB);

                                } else if (field instanceof Varies) {
                                    value = ((GenericPrimitive) ((Varies) field).getData()).getValue();
                                } else {
                                    value = ((Primitive) field).getValue();
                                }
                                if (value != null) {
                                    elements.put(keyB, value);
                                }
                            }
                        }
                    }
                }
            } catch (HL7Exception e) {
                throw new HL7Exception("Error creating publisher message : " + e.getMessage(), e);
            }
        }
        return elements;
    }

    /**
     * This extracts the Composite
     *
     * @param composite
     * @param elements
     * @param key
     */
    private void evaluateComposite(Composite composite, Map<String, String> elements, String key) {

        Type[] types = composite.getComponents();
        for (int z = 0; z < types.length; z++) {
            Type type = types[z];
            String value = null;
            String keyC = key;
            if (z > 0) {
                keyC = key + "[" + z + "]";
            }
            if (type instanceof Composite) {
                evaluateComposite((Composite) type, elements, keyC);
            } else if (type instanceof Varies) {
                value = ((GenericPrimitive) ((Varies) type).getData()).getValue();
            } else {
                value = ((Primitive) type).getValue();
            }
            if (value != null) {
                elements.put(keyC, value);
            }
        }
    }

    public String getServerName() {
        if (serverName == null) {
            String[] properties = CarbonServerConfigurationService.getInstance().getProperties(NAME);
            if (properties != null && properties.length > 0) {
                serverName = properties[0];
            }
        }

        return serverName;
    }
}
