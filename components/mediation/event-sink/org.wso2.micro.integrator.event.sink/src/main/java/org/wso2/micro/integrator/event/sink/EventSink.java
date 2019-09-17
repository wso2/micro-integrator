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

import org.apache.axiom.om.OMElement;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.micro.core.util.CryptoException;
import org.wso2.micro.core.util.CryptoUtil;
import org.wso2.carbon.databridge.agent.DataPublisher;
import org.wso2.carbon.databridge.agent.exception.DataEndpointAgentConfigurationException;
import org.wso2.carbon.databridge.agent.exception.DataEndpointAuthenticationException;
import org.wso2.carbon.databridge.agent.exception.DataEndpointConfigurationException;
import org.wso2.carbon.databridge.agent.exception.DataEndpointException;
import org.wso2.carbon.databridge.agent.util.DataEndpointConstants;
import org.wso2.carbon.databridge.commons.exception.TransportException;

import java.nio.charset.Charset;

public class EventSink {
    private static final Log log = LogFactory.getLog(EventSink.class);
    private String name;
    private String receiverUrlSet;
    private String authenticationUrlSet;
    private String username;
    private String password;
    private DataPublisher dataPublisher;

    public EventSink() {
    }

    public EventSink(String name, String username, String password, String receiverUrlSet, String authenticationUrlSet) {
        this.setName(name);
        this.setUsername(username);
        this.setPassword(password);
        this.setReceiverUrlSet(receiverUrlSet);
        this.setAuthenticationUrlSet(authenticationUrlSet);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getReceiverUrlSet() {
        return receiverUrlSet;
    }

    public void setReceiverUrlSet(String urlSet) {
        this.receiverUrlSet = urlSet;
    }

    public String getAuthenticationUrlSet() {
        return authenticationUrlSet;
    }

    public void setAuthenticationUrlSet(String urlSet) {
        this.authenticationUrlSet = urlSet;
    }

    public DataPublisher getDataPublisher() {
        return dataPublisher;
    }

    public void setDataPublisher(DataPublisher loadBalancingDataPublisher) {
        this.dataPublisher = loadBalancingDataPublisher;
    }

    /**
     * Generates an event sink from XML configuration element.
     *
     * @param eventSinkElement XML configuration element of event sink
     * @param name             Name to be set for the created event sink
     * @return Generated event sink
     * @throws EventSinkException
     */
    public static EventSink createEventSink(OMElement eventSinkElement, String name) throws EventSinkException {

        EventSink eventSink = new EventSink();

        OMElement receiverUrl = eventSinkElement.getFirstChildWithName(EventSinkConstants.RECEIVER_URL_Q);
        if (receiverUrl == null || "".equals(receiverUrl.getText())) {
            throw new EventSinkException(
                    EventSinkConstants.RECEIVER_URL_Q.getLocalPart() + " is missing in thrift endpoint config");
        }
        eventSink.setReceiverUrlSet(receiverUrl.getText());

        OMElement authenticatorUrl = eventSinkElement.getFirstChildWithName(EventSinkConstants.AUTHENTICATOR_URL_Q);
        if (authenticatorUrl != null) {
            eventSink.setAuthenticationUrlSet(authenticatorUrl.getText());
        }

        OMElement userName = eventSinkElement.getFirstChildWithName(EventSinkConstants.USERNAME_Q);
        if (userName == null || "".equals(userName.getText())) {
            throw new EventSinkException(
                    EventSinkConstants.USERNAME_Q.getLocalPart() + " is missing in thrift endpoint config");
        }
        eventSink.setUsername(userName.getText());

        OMElement password = eventSinkElement.getFirstChildWithName(EventSinkConstants.PASSWORD_Q);
        if (password == null || "".equals(password.getText())) {
            throw new EventSinkException(
                    EventSinkConstants.PASSWORD_Q.getLocalPart() + " attribute missing in thrift endpoint config");
        }

        try {
            eventSink.setPassword(
                    new String(CryptoUtil.getDefaultCryptoUtil().base64DecodeAndDecrypt(password.getText()),
                            Charset.forName("UTF-8")));
        } catch (CryptoException e) {
            throw new EventSinkException("Failed to decrypt password", e);
        }
        eventSink.setName(name);

        String authenticationUrlSet = null;
        if (eventSink.getAuthenticationUrlSet().trim().length() > 0) {
            authenticationUrlSet = eventSink.getAuthenticationUrlSet();
        }
        try {
            eventSink.setDataPublisher(
                    new DataPublisher(DataEndpointConstants.THRIFT_DATA_AGENT_TYPE, eventSink.getReceiverUrlSet(),
                            authenticationUrlSet, eventSink.getUsername(),
                            eventSink.getPassword()));
        } catch (DataEndpointAgentConfigurationException | DataEndpointException | DataEndpointConfigurationException
                | DataEndpointAuthenticationException | TransportException e) {
            throw new EventSinkException("Error creating DataPublisher for the event sink ", e);
        }
        return eventSink;
    }

    /**
     * Stop DataPublisher before removing the Event Sink.
     *
     * @param eventSink name of the Event Sink which is going to be removed
     */
    public static void stopDataPublisher(EventSink eventSink) throws EventSinkException {
        if (eventSink != null && eventSink.getDataPublisher() != null) {
            try {
                eventSink.getDataPublisher().shutdownWithAgent();
            } catch (DataEndpointException e) {
                throw new EventSinkException("Error shutting down the publisher for the event sink ", e);
            }
        }
    }
}
