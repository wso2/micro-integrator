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

package org.wso2.carbon.inbound.endpoint.protocol.hl7.util;

public class Axis2HL7Constants {

    public static final String HL7_DEFAULT_FIELD_SEPARATOR = "|";

    public static final String HL7_DEFAULT_ENCODING_CHARS = "^~\\&";

    public static final String HL7_DEFAULT_ACK_CODE_AR = "AR";

    public static final String HL7_DEFAULT_RECEIVING_APPLICATION = " ";

    public static final String HL7_DEFAULT_RECEIVING_FACILITY = " ";

    public static final String HL7_DEFAULT_PROCESSING_ID = "P";

    public static final String HL7_DEFAULT_MESSAGE_CONTROL_ID = "123456789";

    public static final String HL7_MESSAGE_OBJECT = "HL7_MESSAGE_OBJECT";

    public static final String HL7_VALIDATE_MESSAGE = "transport.hl7.ValidateMessage";

    public static final String HL7_VALIDATION_PASSED = "HL7_VALIDATION_PASSED";   // internal to track invalid HL7 messages

    public static final String HL7_PASS_THROUGH_INVALID_MESSAGES = "transport.hl7.PassThroughInvalidMessages"; // pass through invalid HL7 messages

    public static final String HL7_BUILD_RAW_MESSAGE = "transport.hl7.BuildInvalidMessages"; // if message is invalid, build XML including raw invalid HL7 message

    public static final String HL7_RESULT_MODE = "HL7_RESULT_MODE";

    public static final String HL7_RESULT_MODE_NACK = "NACK";

    public static final String HL7_NACK_MESSAGE = "HL7_NACK_MESSAGE";

    public static final String HL7_APPLICATION_ACK = "HL7_APPLICATION_ACK";

    public static final String HL7_CONFORMANCE_PROFILE_PATH = "transport.hl7.ConformanceProfilePath";

    public static final String HL7_NAMESPACE = "http://wso2.org/hl7";

    public static final String HL7_ELEMENT_NAME = "hl7";

    public static final String HL7_MESSAGE_ELEMENT_NAME = "message";

    public static final String HL7_MESSAGE_CHARSET = "HL7_MESSAGE_CHARSET";

    public static final class MessageType {

        public static final String V2X = "V2X";

        public static final String V3X = "V3X";

    }

    public static final class MessageEncoding {

        public static final String ER7 = "ER7";

        public static final String XML = "XML";

    }

}
