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

package org.wso2.micro.integrator.business.messaging.hl7.transport;

import org.apache.axis2.transport.OutTransportInfo;
import org.wso2.micro.integrator.business.messaging.hl7.common.HL7ProcessingContext;

/**
 * Out Transport info to keep incoming message attributes
 */
public class HL7TransportOutInfo implements OutTransportInfo {

    private String contentType;
    private String messageControllerID;
    private HL7ProcessingContext processingContext;

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public String getContentType() {
        return contentType;
    }

    public String getMessageControllerID() {
        return messageControllerID;
    }

    public void setMessageControllerID(String messageControllerID) {
        this.messageControllerID = messageControllerID;
    }

    public HL7ProcessingContext getProcessingContext() {
        return processingContext;
    }

    public void setProcessingContext(HL7ProcessingContext processingContext) {
        this.processingContext = processingContext;
    }

}
