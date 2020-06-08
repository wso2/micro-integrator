/**
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
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.inbound.endpoint.protocol.rabbitmq;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.Recoverable;
import com.rabbitmq.client.RecoveryListener;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Logging notifications about completed automatic connection recovery
 */
public class RabbitMQRecoveryListener implements RecoveryListener {

    private static final Log log = LogFactory.getLog(RabbitMQRecoveryListener.class);

    @Override
    public void handleRecovery(Recoverable recoverable) {
        if (recoverable instanceof Connection) {
            String connectionId = ((Connection) recoverable).getId();
            log.info("Connection with id " + connectionId + " was recovered.");
        }

        if (recoverable instanceof Channel) {
            int channelNumber = ((Channel) recoverable).getChannelNumber();
            log.info("Connection to channel number " + channelNumber + " was recovered.");
        }
    }

    @Override
    public void handleRecoveryStarted(Recoverable recoverable) {
        if (recoverable instanceof Connection) {
            String connectionId = ((Connection) recoverable).getId();
            log.info("Connection with id " + connectionId + " started to recover.");
        }

        if (recoverable instanceof Channel) {
            int channelNumber = ((Channel) recoverable).getChannelNumber();
            log.info("Connection to channel number " + channelNumber + " started to recover.");
        }
    }
}
