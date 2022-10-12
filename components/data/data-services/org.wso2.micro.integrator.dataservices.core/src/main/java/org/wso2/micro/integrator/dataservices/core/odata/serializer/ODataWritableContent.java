/*
 * Copyright (c) 2022, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.micro.integrator.dataservices.core.odata.serializer;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.channels.Channels;
import java.nio.channels.WritableByteChannel;

import org.apache.olingo.commons.api.data.EntityIterator;
import org.apache.olingo.commons.api.edm.EdmEntityType;
import org.apache.olingo.commons.api.ex.ODataRuntimeException;
import org.apache.olingo.server.api.ODataContent;
import org.apache.olingo.server.api.ODataContentWriteErrorCallback;
import org.apache.olingo.server.api.ODataContentWriteErrorContext;
import org.apache.olingo.server.api.ODataLibraryException;
import org.apache.olingo.server.api.ServiceMetadata;
import org.apache.olingo.server.api.serializer.EntityCollectionSerializerOptions;
import org.apache.olingo.server.api.serializer.SerializerException;
import org.apache.olingo.server.api.serializer.SerializerStreamResult;
import org.apache.olingo.server.core.serializer.SerializerStreamResultImpl;

/**
 * This class includes Stream supporting implementation of the ODataContent and the response content for the
 * OData request.
 */
public class ODataWritableContent implements ODataContent {
    private StreamContent streamContent;

    private ODataWritableContent(StreamContent streamContent) {
        this.streamContent = streamContent;
    }

    public static ODataWritableContentBuilder with(EntityIterator iterator, EdmEntityType entityType,
                                                   ODataSerializer serializer, ServiceMetadata metadata,
                                                   EntityCollectionSerializerOptions options) {
        return new ODataWritableContentBuilder(iterator, entityType, serializer, metadata, options);
    }

    public void write(WritableByteChannel writeChannel) {
        this.streamContent.write(Channels.newOutputStream(writeChannel));
    }

    public void write(OutputStream stream) {
        this.write(Channels.newChannel(stream));
    }

    /**
     * This is the content builder class to support OData streaming.
     */
    public static class ODataWritableContentBuilder {
        private ODataSerializer serializer;
        private EntityIterator entities;
        private ServiceMetadata metadata;
        private EdmEntityType entityType;
        private EntityCollectionSerializerOptions options;

        public ODataWritableContentBuilder(EntityIterator entities, EdmEntityType entityType,
                                           ODataSerializer serializer, ServiceMetadata metadata,
                                           EntityCollectionSerializerOptions options) {
            this.entities = entities;
            this.entityType = entityType;
            this.serializer = serializer;
            this.metadata = metadata;
            this.options = options;
        }

        public ODataContent buildContent() {
            if (this.serializer instanceof ODataJsonSerializer) {
                StreamContent input = new StreamContentForJson(this.entities, this.entityType,
                                                               (ODataJsonSerializer) this.serializer, this.metadata,
                                                               this.options);
                return new ODataWritableContent(input);
            } else if (this.serializer instanceof ODataXmlSerializer) {
                StreamContentForXml input = new StreamContentForXml(this.entities, this.entityType,
                                                                    (ODataXmlSerializer) this.serializer, this.metadata,
                                                                    this.options);
                return new ODataWritableContent(input);
            } else {
                throw new ODataRuntimeException("No suitable serializer found");
            }
        }

        public SerializerStreamResult build() {
            return SerializerStreamResultImpl.with().content(this.buildContent()).build();
        }
    }

    /**
     * This class handles error call backs while streaming.
     */
    public static class WriteErrorContext implements ODataContentWriteErrorContext {
        private ODataLibraryException exception;

        public WriteErrorContext(ODataLibraryException exception) {
            this.exception = exception;
        }

        public Exception getException() {
            return this.exception;
        }

        public ODataLibraryException getODataLibraryException() {
            return this.exception;
        }
    }

    /**
     * This class handles streaming entities in XML format.
     */
    private static class StreamContentForXml extends StreamContent {
        private ODataXmlSerializer xmlSerializer;

        public StreamContentForXml(EntityIterator iterator, EdmEntityType entityType, ODataXmlSerializer xmlSerializer,
                                   ServiceMetadata metadata, EntityCollectionSerializerOptions options) {
            super(iterator, entityType, metadata, options);
            this.xmlSerializer = xmlSerializer;
        }

        protected void writeEntity(EntityIterator iterator, OutputStream outputStream) throws SerializerException {
            try {
                this.xmlSerializer.entityCollectionIntoStream(this.metadata, this.entityType, iterator, this.options,
                                                              outputStream);
                outputStream.flush();
            } catch (IOException e) {
                throw new ODataRuntimeException("Failed entity serialization", e);
            }
        }
    }

    /**
     * This class handles streaming entities in JSON format.
     */
    private static class StreamContentForJson extends StreamContent {
        private ODataJsonSerializer jsonSerializer;

        public StreamContentForJson(EntityIterator iterator, EdmEntityType entityType,
                                    ODataJsonSerializer jsonSerializer, ServiceMetadata metadata,
                                    EntityCollectionSerializerOptions options) {
            super(iterator, entityType, metadata, options);
            this.jsonSerializer = jsonSerializer;
        }

        protected void writeEntity(EntityIterator iterator, OutputStream outputStream) throws SerializerException {
            try {
                this.jsonSerializer.entityCollectionIntoStream(this.metadata, this.entityType, iterator, this.options,
                                                               outputStream);
                outputStream.flush();
            } catch (IOException e) {
                throw new ODataRuntimeException("Failed entity serialization", e);
            }
        }
    }

    /**
     * This class handles streaming entities.
     */
    private abstract static class StreamContent {
        protected EntityIterator iterator;
        protected ServiceMetadata metadata;
        protected EdmEntityType entityType;
        protected EntityCollectionSerializerOptions options;

        public StreamContent(EntityIterator iterator, EdmEntityType entityType, ServiceMetadata metadata,
                             EntityCollectionSerializerOptions options) {
            this.iterator = iterator;
            this.entityType = entityType;
            this.metadata = metadata;
            this.options = options;
        }

        protected abstract void writeEntity(EntityIterator iterator, OutputStream outputStream)
                throws SerializerException;

        public void write(OutputStream out) {
            try {
                this.writeEntity(this.iterator, out);
            } catch (SerializerException e) {
                ODataContentWriteErrorCallback errorCallback = this.options.getODataContentWriteErrorCallback();
                if (errorCallback != null) {
                    WriteErrorContext errorContext = new WriteErrorContext(e);
                    errorCallback.handleError(errorContext, Channels.newChannel(out));
                }
            }
        }
    }
}
