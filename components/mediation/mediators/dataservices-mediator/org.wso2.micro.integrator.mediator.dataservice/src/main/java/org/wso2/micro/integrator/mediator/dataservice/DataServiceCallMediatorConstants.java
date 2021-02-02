/*
 *
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  you may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */

package org.wso2.micro.integrator.mediator.dataservice;

public class DataServiceCallMediatorConstants {

    public static final String PAYLOAD_NAME_SPACE_URI = "http://ws.wso2.org/dataservice";
    public static final String PAYLOAD_PREFIX = "axis2ns";
    public static final String TARGET_PROPERTY_TYPE = "property";
    public static final String TARGET_BODY_TYPE = "body";
    public static final String JSON_TYPE = "json";
    public static final String XML_TYPE = "xml";
    public static final String DATA_SERVICE_CALL = "dataServiceCall";
    public static final String SERVICE_NAME = "serviceName";
    public static final String OPERATIONS = "operations";
    public static final String OPERATION = "operation";
    public static final String PARAM = "param";
    public static final String EXPRESSION = "expression";
    public static final String EVALUATOR = "evaluator";
    public static final String VALUE = "value";
    public static final String SOURCE = "source";
    public static final String INLINE_SOURCE = "inline";
    public static final String TARGET = "target";
    public static final String PROPERTY = "property";
    public static final String SOURCE_BODY_TYPE = "body";
    public static final String NAME = "name";
    public static final String TYPE = "type";
    public static final String REQUEST_BOX = "request_box";
    public static final String BATCH_REQ_SUFFIX = "_batch_req";
    public static final String APPLICATION_XML = "application/xml";
    public static final String JSON_OBJECT = "jsonObject";

    public class OperationsType {

        public static final String SINGLE = "single";
        public static final String BATCH = "batch";
        public static final String REQUEST_BOX = "request-box";

    }
}
