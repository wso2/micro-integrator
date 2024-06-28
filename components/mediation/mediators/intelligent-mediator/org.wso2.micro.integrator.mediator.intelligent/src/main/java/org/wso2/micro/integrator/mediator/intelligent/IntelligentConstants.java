/*
 * Copyright (c) 2024, WSO2 LLC. (http://www.wso2.org) All Rights Reserved.
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
 */package org.wso2.micro.integrator.mediator.intelligent;

import org.apache.synapse.config.xml.XMLConfigConstants;
import javax.xml.namespace.QName;

public class IntelligentConstants {
    public static final QName INTELLIGENT_Q = new QName(XMLConfigConstants.SYNAPSE_NAMESPACE,
            IntelligentConstants.INTELLIGENT_LOCAL_NAME);
    public static final String INTELLIGENT_LOCAL_NAME = "intelligent";

    public static final QName PAYLOAD_Q = new QName(XMLConfigConstants.SYNAPSE_NAMESPACE, "payload");

    public static final String KEY_STRING = "key";
    public static final String MODEL_STRING = "model";
    public static final String ENDPOINT_STRING = "endpoint";
    public static final String RETRY_COUNT_STRING = "retry-count";
    public static final String PROMPT_STRING = "prompt";
    public static final String HEADERS_STRING = "headers";

    public static final String MESSAGE_TYPE_STRING = "messageType";
    public static final String CONTENT_TYPE_STRING = "ContentType";
    public static final String JSON_CONTENT_TYPE = "application/json";
}
