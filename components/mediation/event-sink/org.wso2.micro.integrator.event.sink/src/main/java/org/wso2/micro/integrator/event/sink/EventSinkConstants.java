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

package org.wso2.micro.integrator.event.sink;

import javax.xml.namespace.QName;

public class EventSinkConstants {

    public static final QName EVENT_SINK_Q = new QName("eventSink");
    public static final QName RECEIVER_URL_Q = new QName("receiverUrl");
    public static final QName AUTHENTICATOR_URL_Q = new QName("authenticatorUrl");
    public static final QName USERNAME_Q = new QName("username");
    public static final QName PASSWORD_Q = new QName("password");
}
