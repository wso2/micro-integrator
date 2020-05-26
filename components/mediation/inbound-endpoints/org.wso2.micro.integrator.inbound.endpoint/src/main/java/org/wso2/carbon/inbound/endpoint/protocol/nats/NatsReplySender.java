/*
 * Copyright 2020 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wso2.carbon.inbound.endpoint.protocol.nats;

import io.nats.client.Connection;
import org.apache.synapse.MessageContext;
import org.apache.synapse.inbound.InboundResponseSender;

/**
 * Send the Reply from the Respond Mediator using this class (only for core NATS).
 */
public class NatsReplySender implements InboundResponseSender {

    private String replyTo;
    private Connection connection;

    public NatsReplySender(String replyTo, Connection connection) {
        this.replyTo = replyTo;
        this.connection = connection;
    }

    @Override public void sendBack(MessageContext messageContext) {
        connection.publish(replyTo, messageContext.getEnvelope().getBody().toString().getBytes());
    }
}
