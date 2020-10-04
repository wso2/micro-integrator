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
package org.wso2.micro.integrator.business.messaging.hl7.common;

/**
 * HL7 Transport related constants.
 */
public class HL7Constants {

    public static final String HL7_CONTENT_TYPE = "application/edi-hl7";

    public static final String HL7_DEFAULT_FIELD_SEPARATOR = "|";

    public static final String HL7_DEFAULT_ENCODING_CHARS = "^~\\&";

    public static final String HL7_DEFAULT_ACK_CODE_AR = "AR";

    public static final String HL7_DEFAULT_RECEIVING_APPLICATION = " ";

    public static final String HL7_DEFAULT_RECEIVING_FACILITY = " ";

    public static final String HL7_DEFAULT_PROCESSING_ID = "P";

    public static final String HL7_DEFAULT_MESSAGE_CONTROL_ID = "123456789";

    public static final String TRANSPORT_NAME = "hl7";

    public static final String HL7_PORT = "transport.hl7.Port";

    public static final String OUT_DIRECTION = "out";

    public static final int DEFAULT_TIMEOUT = 60000;

    public static final int DEFAULT_SYNAPSE_HL7_PORT = 9792;

    public static final String TIMEOUT_PARAM = "timeout";

    public static final String HL7_RAW_MESSAGE_PROPERTY_NAME = "HL7_RAW_MESSAGE";

    public static final String HL7_MESSAGE_OBJECT = "HL7_MESSAGE_OBJECT";

    public static final int ACTIVITY_ID_GEN = 123456789;

    public static final String HL7_VALIDATE_MESSAGE = "transport.hl7.ValidateMessage";

    public static final String HL7_DEFAULT_VALIDATION_PASSED = "HL7_DEFAULT_VALIDATION_PASSED";

    public static final String HL7_VALIDATION_PASSED = "HL7_VALIDATION_PASSED";
    // internal to track invalid HL7 messages

    public static final String HL7_PASS_THROUGH_INVALID_MESSAGES = "transport.hl7.PassThroughInvalidMessages";
    // pass through invalid HL7 messages

    public static final String HL7_BUILD_RAW_MESSAGE = "transport.hl7.BuildInvalidMessages";
    // if message is invalid, build XML including raw invalid HL7 message

    public static final String HL7_CORE_POOL_SIZE = "transport.hl7.corePoolSize";

    public static final int HL7_DEFAULT_CORE_POOL_SIZE = 10;

    public static final int HL7_DEFAULT_MAX_POOL_SIZE = 20;

    public static final int HL7_DEFAULT_IDLE_THREAD_KEEPALIVE = 10000;

    public static final String HL7_PUBLISHER_STREAM_NAME = "bam_hl7_data_publisher_store";

    public static final String HL7_PUBLISHER_STREAM_VERSION = "1.0.0";

    public static final String HL7_MAX_POOL_SIZE = "transport.hl7.maxPoolSize";

    public static final String HL7_IDLE_THREAD_KEEPALIVE = "transport.hl7.idleThreadKeepAlive";

    public static final String HL7_AUTO_ACKNOWLEDGE = "transport.hl7.AutoAck";

    public static final String HL7_RESULT_MODE = "HL7_RESULT_MODE";

    public static final String HL7_RESULT_MODE_ACK = "ACK";

    public static final String HL7_RESULT_MODE_NACK = "NACK";

    public static final String HL7_NACK_MESSAGE = "HL7_NACK_MESSAGE";

    public static final String HL7_MSA_ERROR_FIELD_VALUE = "AE";

    public static final String HL7_CONFORMANCE_PROFILE_PATH = "transport.hl7.ConformanceProfilePath";

    public static final String HL7_MESSAGE_PREPROCESSOR_CLASS = "transport.hl7.MessagePreprocessorClass";

    public static final String HL7_MESSAGE_PUBLISHER_ENABLE = "transport.hl7.PublisherEnable";

    public static final String HL7_MESSAGE_PUBLISHER_SERVER_URL = "transport.hl7.PublisherServerUrl";

    public static final String HL7_MESSAGE_PUBLISHER_SECURE_SERVER_URL = "transport.hl7.PublisherSecureServerUrl";

    public static final String HL7_MESSAGE_PUBLISHER_SERVER_USERNAME = "transport.hl7.PublisherServerUsername";

    public static final String HL7_MESSAGE_PUBLISHER_SERVER_PASSWORD = "transport.hl7.PublisherServerPassword";

    public static final String HL7_NAMESPACE = "http://wso2.org/hl7";

    public static final String HL7_ELEMENT_NAME = "hl7";

    public static final String HL7_MESSAGE_ELEMENT_NAME = "message";

    public static final String HL7_GENERATE_ACK = "HL7_GENERATE_ACK";

    public static final String HL7_APPLICATION_ACK = "HL7_APPLICATION_ACK";

    public static final String HL7_MESSAGE_CHARSET = "HL7_MESSAGE_CHARSET";

    public static final class MessageType {

        public static final String V2X = "V2X";

        public static final String V3X = "V3X";

    }

    public static final class MessageEncoding {

        public static final String ER7 = "ER7";

        public static final String XML = "XML";

    }

    //HL7 Transport parameters

    public static final String HL7_TRANSPORT_TIMEOUT = "transport.hl7.TimeOut";

}
