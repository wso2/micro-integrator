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

package org.wso2.carbon.inbound.endpoint.protocol.rabbitmq;

/**
 * This class represents RabbitMQ specific exceptions
 */
public class RabbitMQException extends Exception {

    /**
     * Create an exception with given exception message
     *
     * @param msg an exception message
     */
    public RabbitMQException(String msg) {
        super(msg);
    }

    /**
     * Create an exception with given message and wrapping the given exception object
     *
     * @param msg exception message
     * @param e   exception
     */
    public RabbitMQException(String msg, Exception e) {
        super(msg, e);
    }
}