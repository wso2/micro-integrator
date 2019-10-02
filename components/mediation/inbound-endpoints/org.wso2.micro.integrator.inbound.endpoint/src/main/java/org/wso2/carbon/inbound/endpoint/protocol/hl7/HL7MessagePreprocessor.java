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

package org.wso2.carbon.inbound.endpoint.protocol.hl7;

/**
 * This interface represents an implementation of a preprocessor which is used to modify the incoming
 * HL7 message before parsing.
 */
public interface HL7MessagePreprocessor {

    /**
     * Processes the incoming message and returns a possibly modified message.
     *
     * @param message  The incomding message
     * @param type     The type of the message, i.e. v2 or v3, defined at Axis2HL7Constants.MessageType
     * @param encoding The encoding of the message, i.e. ER7 or XML,
     *                 defined at Axis2HL7Constants.MessageEncoding
     * @return
     */
    String process(String message, String type, String encoding);

}
