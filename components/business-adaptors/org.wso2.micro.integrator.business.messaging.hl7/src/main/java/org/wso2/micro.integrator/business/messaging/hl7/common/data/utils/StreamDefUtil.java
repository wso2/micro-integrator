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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.databridge.commons.Attribute;
import org.wso2.carbon.databridge.commons.AttributeType;
import org.wso2.carbon.databridge.commons.StreamDefinition;
import org.wso2.carbon.databridge.commons.exception.MalformedStreamDefinitionException;
import org.wso2.micro.integrator.business.messaging.hl7.common.HL7Constants;

import java.util.ArrayList;
import java.util.List;

/**
 * This is the util class for Stream definition
 */
public class StreamDefUtil {

    protected static final String CONTENT = "content";
    protected static final String TYPE = "type";
    protected static final String TIMESTAMP = "timestamp";
    protected static final String HOST = "host";
    protected static final String ACTIVITY_ID = "activity_id";
    protected static final String STATUS = "status";
    protected static final String MSG_DIRECTION = "message_direction";
    protected static final String OP_NAME = "operation_name";
    protected static final String SERVICE_NAME = "service_name";
    protected static final String SERVER_NAME = "server_name";

    private static Log log = LogFactory.getLog(StreamDefUtil.class);

    public static StreamDefinition getStreamDefinition() throws MalformedStreamDefinitionException {

        StreamDefinition streamDefinition = null;
        try {
            streamDefinition = new StreamDefinition(HL7Constants.HL7_PUBLISHER_STREAM_NAME,
                                                    HL7Constants.HL7_PUBLISHER_STREAM_VERSION);

            List<Attribute> metaDataAttributeList = getMetaDataDef();
            streamDefinition.setMetaData(metaDataAttributeList);

            List<Attribute> correlationDataAttributeList = getCorrelationDataDef();
            streamDefinition.setCorrelationData(correlationDataAttributeList);

            List<Attribute> payLoadData = getPayLoadDataDef();
            streamDefinition.setPayloadData(payLoadData);
        } catch (MalformedStreamDefinitionException e) {
            throw new MalformedStreamDefinitionException("Unable to create HL7 StreamDefinition : " + e.getMessage(),
                                                         e);
        }

        return streamDefinition;
    }

    private static List<Attribute> getPayLoadDataDef() {

        List<Attribute> payLoadList = new ArrayList<Attribute>(7);
        payLoadList.add(new Attribute(CONTENT, AttributeType.STRING));
        payLoadList.add(new Attribute(TYPE, AttributeType.STRING));
        payLoadList.add(new Attribute(TIMESTAMP, AttributeType.LONG));
        payLoadList.add(new Attribute(MSG_DIRECTION, AttributeType.STRING));
        payLoadList.add(new Attribute(SERVICE_NAME, AttributeType.STRING));
        payLoadList.add(new Attribute(OP_NAME, AttributeType.STRING));
        payLoadList.add(new Attribute(STATUS, AttributeType.STRING));

        return payLoadList;
    }

    private static List<Attribute> getMetaDataDef() {

        List<Attribute> metaDataList = new ArrayList<Attribute>(2);
        metaDataList.add(new Attribute(HOST, AttributeType.STRING));
        metaDataList.add(new Attribute(SERVER_NAME, AttributeType.STRING));
        return metaDataList;
    }

    private static List<Attribute> getCorrelationDataDef() {
        List<Attribute> correlationList = new ArrayList<Attribute>(1);
        correlationList.add(new Attribute(ACTIVITY_ID, AttributeType.STRING));
        return correlationList;
    }
}
