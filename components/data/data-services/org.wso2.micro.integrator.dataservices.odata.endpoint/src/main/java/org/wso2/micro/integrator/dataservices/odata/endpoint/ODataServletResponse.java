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

package org.wso2.micro.integrator.dataservices.odata.endpoint;

import org.apache.axis2.context.MessageContext;
import org.apache.commons.lang.StringUtils;
import org.apache.http.protocol.HTTP;
import org.apache.synapse.SynapseException;
import org.apache.synapse.transport.passthru.PassThroughConstants;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Locale;
import java.util.Map;

public class ODataServletResponse implements HttpServletResponse {

    private final static int OUTPUT_STREAM_BUFFER_SIZE = 4096;
    private MessageContext axis2MessageContext;
    private final ByteArrayOutputStream content = new ByteArrayOutputStream(1024);
    private final ResponseServletOutputStream outputStream = new ResponseServletOutputStream(this.content);
    private long contentLength = 0;
    private String contentType;
    private String characterEncoding = StandardCharsets.UTF_8.name();
    private int bufferSize = 4096;
    private int statusCode;
    private boolean committed;
    private boolean stopStream = false;

    ODataServletResponse(MessageContext messageContext) {
        this.axis2MessageContext =  messageContext;
    }

    /**
     * This method will signal when to start streaming.
     *
     * @return true if the streaming has started. Otherwise, return false.
     */
    public boolean startStream() {
        return this.outputStream.startStream;
    }

    /**
     * This method will signal when to stop streaming.
     *
     * @return true if the streaming has stopped. Otherwise, return false.
     */
    public boolean isComplete() {
        return this.stopStream;
    }

    @Override
    public void addCookie(Cookie cookie) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean containsHeader(String s) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String encodeURL(String s) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String encodeRedirectURL(String s) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String encodeUrl(String s) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String encodeRedirectUrl(String s) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void sendError(int i, String s) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void sendError(int i) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void sendRedirect(String s) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setDateHeader(String s, long l) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void addDateHeader(String s, long l) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setHeader(String headerName, String headerValue) {
        Object o = axis2MessageContext.getProperty(org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS);
        Map headers = (Map) o;
        if (headers != null) {
            headers.put(headerName, headerValue);
        }
    }

    @Override
    public void addHeader(String headerName, String headerValue) {
        Object o = axis2MessageContext.getProperty(org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS);
        Map headers = (Map) o;
        if (headers != null) {
            headers.put(headerName, headerValue);
        }
        if (HTTP.CONTENT_TYPE.equals(headerName)) {
            contentType = headerValue;
        }
        if (HTTP.CONTENT_LEN.equals(headerName)) {
            contentLength = Long.parseLong(headerValue);
        }
    }

    @Override
    public void setIntHeader(String s, int i) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void addIntHeader(String s, int i) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setStatus(int i) {
        axis2MessageContext.setProperty(PassThroughConstants.HTTP_SC, i);
        statusCode = i;
    }

    @Override
    public void setStatus(int i, String s) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getStatus() {
        return statusCode;
    }

    @Override
    public String getHeader(String s) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Collection<String> getHeaders(String s) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Collection<String> getHeaderNames() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getCharacterEncoding() {
        return characterEncoding;
    }

    @Override
    public String getContentType() {
        return contentType;
    }

    @Override
    public ServletOutputStream getOutputStream() throws IOException {
        return this.outputStream;
    }

    private void setCommittedIfBufferSizeExceeded() {
        int bufSize = getBufferSize();
        if (bufSize > 0 && this.content.size() > bufSize) {
            setCommitted(true);
        }
    }

    /**
     * This method flushes the remaining bytes to the output stream.
     */
    public void flushOutputStream() {
        try {
            this.outputStream.flushOutputByteArray();
        } catch (IOException e) {
            throw new SynapseException("Error occurred while trying to close the Servlet response.", e);
        }
    }

    /**
     * This method closes the response.
     */
    public void close() {
        try {
            this.content.close();
            this.outputStream.close();
        } catch (IOException e) {
            throw new SynapseException("Error occurred while trying to close the Servlet response.", e);
        }
    }

    public void setCommitted(boolean committed) {
        this.committed = committed;
    }

    private class ResponseServletOutputStream extends ServletOutputStream {

        public boolean readWithoutLimit = false;
        private OutputStream targetStream;
        private boolean startStream = false;
        private byte[] byteArr = new byte[OUTPUT_STREAM_BUFFER_SIZE];
        private int byteCount = 0;

        ResponseServletOutputStream(OutputStream out) {
            targetStream = out;
        }

        /**
         * This method writes a byte to a byte array. Once the byte array is full the entire array is written to the
         * output stream.
         * This will reduce the latency of the response.
         *
         * @param b Byte value to be written.
         * @throws IOException if any unexpected I/O error occurred while writing to the output stream.
         */
        @Override
        public void write(int b) throws IOException {
            byteArr[byteCount++] = (byte) b;
            if (!startStream) {
                startStream = true;
            }
            if (byteCount >= OUTPUT_STREAM_BUFFER_SIZE) {
                writeOutputStream();
                byteCount = 0;
            }
        }

        /**
         * This method writes a byte array to the output stream.
         *
         * @throws IOException if any unexpected I/O error occurred while writing to the output stream.
         */
        private synchronized void writeOutputStream() throws IOException {
            targetStream.write(byteArr);
            super.flush();
            targetStream.flush();
            setCommittedIfBufferSizeExceeded();
            byteCount = 0;
        }

        /**
         * This method returns the content in the output stream in string format.
         *
         * @throws IOException if any unexpected I/O error occurred while reading the output stream.
         */
        private synchronized String readOutputStream(String characterEncoding) throws IOException {
            String response = StringUtils.EMPTY;
            if (this.readWithoutLimit
                    || ((ByteArrayOutputStream) targetStream).size() > OUTPUT_STREAM_BUFFER_SIZE * 8) {
                response = (characterEncoding != null ? ((ByteArrayOutputStream) targetStream).toString(
                        characterEncoding) : ((ByteArrayOutputStream) targetStream).toString());
                ((ByteArrayOutputStream) targetStream).reset();
            }
            if (this.readWithoutLimit) {
                stopStream = true;
            }
            return response;
        }

        /**
         * This method reads the remaining content in the output stream (content that has not yet read).
         *
         * @throws IOException if any unexpected I/O error occurred while writing to the output stream.
         */
        private synchronized void flushOutputByteArray() throws IOException {
            if (byteCount < OUTPUT_STREAM_BUFFER_SIZE) {
                targetStream.write(byteArr, 0, byteCount);
                super.flush();
                targetStream.flush();
                setCommittedIfBufferSizeExceeded();
                byteCount = 0;
                byteArr = new byte[OUTPUT_STREAM_BUFFER_SIZE];
            }
            readWithoutLimit = true;
        }

        @Override
        public void flush() throws IOException {
            super.flush();
            setCommitted(true);
        }

        @Override
        public void close() throws IOException {
            super.close();
            this.targetStream.close();
        }

    }

    @Override
    public PrintWriter getWriter() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setCharacterEncoding(String s) {
        this.characterEncoding = characterEncoding;
    }

    @Override
    public void setContentLength(int i) {
        contentLength = i;
    }

    public int getContentLength() {
        return (int) this.contentLength;
    }

    /**
     * This method returns the content in the output stream in string format.
     *
     * @return content in the output stream in string format.
     * @throws IOException if any unexpected I/O error occurred while reading the output stream.
     */
    public String getContentAsString() throws IOException {
        if (this.content != null) {
            return outputStream.readOutputStream(this.characterEncoding);
        }
        return StringUtils.EMPTY;
    }

    @Override
    public void setContentType(String s) {
        contentType = s;
    }

    @Override
    public void setBufferSize(int bufferSize) {
        this.bufferSize = bufferSize;
    }

    @Override
    public int getBufferSize() {
        return this.bufferSize;
    }

    @Override
    public void flushBuffer() {
        setCommitted(true);
    }

    @Override
    public void resetBuffer() {
        this.content.reset();
    }

    @Override
    public boolean isCommitted() {
        return this.committed;
    }

    @Override
    public void reset() {
        resetBuffer();
        this.characterEncoding = null;
        this.contentLength = 0;
        this.contentType = null;
    }

    /**
     * This method will set stopStream to true. Forcing the response to be closed.
     */
    public void forceComplete() {
        this.stopStream = true;
    }

    @Override
    public void setLocale(Locale locale) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Locale getLocale() {
        throw new UnsupportedOperationException();
    }
}
