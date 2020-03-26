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

import java.io.IOException;
import java.util.concurrent.TimeoutException;

/**
 * The interface which defines behaviour of a core NATS or NATS streaming connection.
 */
public interface NatsMessageListener {
    boolean createConnection() throws IOException, InterruptedException;
    void consumeMessage(String sequenceName) throws InterruptedException, IOException, TimeoutException;
    void closeConnection();
}
