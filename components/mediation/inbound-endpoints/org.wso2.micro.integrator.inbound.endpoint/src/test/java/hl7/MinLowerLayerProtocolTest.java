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

package hl7;//package hl7;
//
//import ca.uhn.hl7v2.DefaultHapiContext;
//import ca.uhn.hl7v2.HL7Exception;
//import ca.uhn.hl7v2.HapiContext;
//import ca.uhn.hl7v2.app.Connection;
//import ca.uhn.hl7v2.app.Initiator;
//import ca.uhn.hl7v2.app.SimpleServer;
//import ca.uhn.hl7v2.model.Message;
//import ca.uhn.hl7v2.model.v26.message.ADT_A01;
//import org.apache.synapse.transport.passthru.util.BufferFactory;
//import org.junit.After;
//import org.junit.Before;
//import org.junit.Test;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.wso2.carbon.inbound.endpoint.protocol.hl7.codec.HL7Codec;
//import org.wso2.carbon.inbound.endpoint.protocol.hl7.context.MLLPContext;
//import org.wso2.carbon.inbound.endpoint.protocol.hl7.core.HL7Processor;
//import org.wso2.carbon.inbound.endpoint.protocol.hl7.core.InboundHL7IOReactor;
//import org.wso2.carbon.inbound.endpoint.protocol.hl7.core.MLLProtocolException;
//
//import java.io.IOException;
//import java.nio.ByteBuffer;
//import java.nio.charset.Charset;
//import java.nio.charset.CharsetDecoder;
//
//import static org.junit.Assert.*;
//
//public class MinLowerLayerProtocolTest {
//    private static final Logger log = LoggerFactory.getLogger(MinLowerLayerProtocolTest.class);
//
//
//    private MLLPContext context;
//    private HL7Codec codec;
//    private String message;
//    private byte [] llpEncodedMessage;
//    private ByteBuffer req;
//    private BufferFactory bufferFactory;
//    private CharsetDecoder charsetDecoder = Charset.forName("UTF-8").newDecoder();
//
//    @Before
//    public void config() throws Exception {
//        System.setProperty("ca.uhn.hl7v2.llp.logBytesRead", "FALSE");
//        System.setProperty("ca.uhn.hl7v2.util.status.out", "");
//        context = new MLLPContext(null, charsetDecoder, true, true,null, bufferFactory);
//        codec = context.getCodec();
//        message = "MSH|^~\\&|||||20150403091225.929+0530||ADT^A01^ADT_A01|208601|T|2.6";
//        llpEncodedMessage = (MllpTestConstants.START_BYTE + message + MllpTestConstants.END_BYTE1 + MllpTestConstants.END_BYTE1 + MllpTestConstants.END_BYTE2).getBytes();
//        req = ByteBuffer.allocate(llpEncodedMessage.length);
//        req.put(llpEncodedMessage);
//    }
//
//    @After
//    public void cleanup() throws Exception {
//        context = null;
//        codec = null;
//        message = null;
//        llpEncodedMessage = null;
//    }
//
//
//    /**
//     * Testing constructor
//     */
//    @Test
//    public void testConstructor() {
//        assertNotNull("Codec object present", codec);
//    }
//
//    /**
//     * Testing decode
//     */
//    @Test
//    public void testDecode() throws HL7Exception, IOException, MLLProtocolException {
//        assertNull(context.getHl7Message());
//        codec.decode(req, context);
//        req.flip();
//        assertNotNull("Should have encoded HL7 message", context.getHl7Message());
//    }
//
//    /**
//     * Testing encode
//     */
//    @Test
//    public void testEncode() throws HL7Exception, IOException, MLLProtocolException {
//        assertNull(context.getHl7Message());
//        codec.decode(req, context);
//        req.flip();
//        assertNotNull(context.getHl7Message());
//        ByteBuffer outBuff = ByteBuffer.allocate(1024);
//        codec.encode(outBuff, context);
//
//        byte[] ack = new byte[outBuff.remaining()];
//        outBuff.get(ack);
//        String v = new String( ack, Charset.forName("UTF-8"));
//        log.info("ACK: " + v);
//        assertTrue(v.contains("ACK^A01^ACK"));
//    }
//
//    @Test
//    public void testReceiveWithDelayInBetween() throws Exception {
//
//        int port = 20000;
//
//        InboundHL7IOReactor.start();
//
//        HL7Processor hl7Processor = new HL7Processor(null);
//
//        InboundHL7IOReactor.bind(port, hl7Processor);
//
//        Thread.sleep(2000);
//
//        HapiContext context = new DefaultHapiContext();
//        Connection c = context.newClient("127.0.0.1", port, false);
//        Initiator initiator = c.getInitiator();
//
//        ADT_A01 msg = new ADT_A01();
//        msg.initQuickstart("ADT", "A01", "T");
//        Message resp1 = initiator.sendAndReceive(msg);
//        log.info(resp1.encode());
//        assertNotNull(resp1);
//
//        Thread.sleep(SimpleServer.SO_TIMEOUT + 500);
//
//        msg.initQuickstart("ADT", "A01", "T");
//        Message resp2 = initiator.sendAndReceive(msg);
//        log.info(resp2.encode());
//        assertNotNull(resp2);
//
//        c.close();
//        Thread.sleep(SimpleServer.SO_TIMEOUT + 500);
//
//        InboundHL7IOReactor.stop();
//    }
//
//}
//
//
