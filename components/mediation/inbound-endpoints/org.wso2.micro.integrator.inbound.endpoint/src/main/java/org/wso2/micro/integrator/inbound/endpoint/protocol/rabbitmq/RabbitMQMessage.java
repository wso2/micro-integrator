/**
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.micro.integrator.inbound.endpoint.protocol.rabbitmq;

import java.util.Map;

/**
 * Class which wraps an RabbitMQ AMQP message which is used in Inbound Endpoint.
 */
public class RabbitMQMessage {
    private String contentType;
    private String contentEncoding;
    private String correlationId;
    private String replyTo;
    private String messageId;
    private String soapAction;
    private String expiration;
    private Map<String, Object> headers;
    private byte body[];
    private long deliveryTag;

    public RabbitMQMessage() {

    }

    /**
     * Get body of the message
     *
     * @return bytes of the message body
     */
    public byte[] getBody() {
        return body;
    }

    /**
     * Get content type of the message
     *
     * @return content type
     */
    public String getContentType() {
        return contentType;
    }

    /**
     * Set content type for the message
     *
     * @param contentType content type to set for the message
     */
    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    /**
     * Get content encoding of the message
     *
     * @return content encoding
     */
    public String getContentEncoding() {
        return contentEncoding;
    }

    /**
     * Set content encoding for the message
     *
     * @param contentEncoding return content encoding
     */
    public void setContentEncoding(String contentEncoding) {
        this.contentEncoding = contentEncoding;
    }

    /**
     * Get correlation id of the message
     *
     * @return correlation id
     */
    public String getCorrelationId() {
        return correlationId;
    }

    /**
     * Set correlation id of the message
     *
     * @param correlationId correlation id to set
     */
    public void setCorrelationId(String correlationId) {
        this.correlationId = correlationId;
    }

    /**
     * get header 'Replyto' of RabbitMQ
     *
     * @return return header value of 'replyTo'
     */
    public String getReplyTo() {
        return replyTo;
    }

    /**
     * set 'ReplyTo' RabbitMQ header
     *
     * @param replyTo value of the header to set
     */
    public void setReplyTo(String replyTo) {
        this.replyTo = replyTo;
    }

    /**
     * Get identifier of the message
     *
     * @return message id (unique)
     */
    public String getMessageId() {
        return messageId;
    }

    /**
     * Set id for the message
     *
     * @param messageId unique id for the message
     */
    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    /**
     * Get soap action message bares
     *
     * @return Soap Action set to the message
     */
    public String getSoapAction() {
        return soapAction;
    }

    /**
     * Set soap action to the message
     *
     * @param soapAction soap action to e set
     */
    public void setSoapAction(String soapAction) {
        this.soapAction = soapAction;
    }

    /**
     * get all headers of the message as a map
     *
     * @return map of headers
     */
    public Map<String, Object> getHeaders() {
        return headers;
    }

    /**
     * set all headers for the message
     *
     * @param headers a map of headers to be set
     */
    public void setHeaders(Map<String, Object> headers) {
        this.headers = headers;
    }

    /**
     * set body of the message
     *
     * @param body bytes representing body
     */
    public void setBody(byte[] body) {
        this.body = body;
    }

    /**
     * set delivery tag of the message assigned by the transport
     *
     * @param deliveryTag delivery tag to set
     */
    public void setDeliveryTag(long deliveryTag) {
        this.deliveryTag = deliveryTag;
    }

    /**
     * get delivery tag of the message set by transport
     *
     * @return delivery tag of the message
     */
    public long getDeliveryTag() {
        return deliveryTag;
    }

    /**
     * get expiration time of the message set by transport
     *
     * @return expiration time of the message
     */
    public String getExpiration() {
        return expiration;
    }

    /**
     * set expiration time of the message assigned by the transport
     *
     * @param expiration to set
     */
    public void setExpiration(String expiration) {
        this.expiration = expiration;
    }
}
