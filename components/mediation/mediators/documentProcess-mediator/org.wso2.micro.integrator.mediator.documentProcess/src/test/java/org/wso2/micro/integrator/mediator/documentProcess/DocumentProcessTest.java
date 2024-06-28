/*
 *  Copyright (c) 2024, WSO2 LLC. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 LLC. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied. See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.wso2.micro.integrator.mediator.documentProcess;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.util.UUIDGenerator;
import org.apache.axiom.util.UIDGenerator;
import org.apache.axis2.AxisFault;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.OperationContext;
import org.apache.axis2.context.ServiceContext;
import org.apache.axis2.description.InOutAxisOperation;
import org.apache.synapse.config.SynapseConfiguration;
import org.apache.synapse.core.axis2.Axis2MessageContext;

import junit.framework.TestCase;
import org.apache.axiom.om.OMElement;
import org.apache.synapse.MessageContext;
import org.apache.synapse.config.SynapseConfigUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Properties;

/**
 * Test the functionality of the {@link DocumentProcessMediatorFactory} and the {@link DocumentProcessMediator}
 */
public class DocumentProcessTest extends TestCase {
    private static final String mediatorXml = "<documentProcess xmlns=\"http://ws.apache.org/ns/synapse\" api-key=\"" +
            "sskfnkjoj-xHsdbfkjsP9Tsy2bT3BfihdfbhqwRzOInsOTddljnr\" max-tokens=\"1000\" gpt-model=\"gpt-4-turbo\" " +
            "schema=\"gov:schema.json\"/>";
    private ConfigurationContext configContext;
    private SynapseConfiguration synapseConfig;

    public void testMediatorFactory() {
        OMElement mediatorElement = SynapseConfigUtils.stringToOM(mediatorXml);

        DocumentProcessMediatorFactory factory = new DocumentProcessMediatorFactory();
        DocumentProcessMediator mediator = (DocumentProcessMediator) factory.createSpecificMediator(mediatorElement,
                new Properties());

        assertEquals("Incorrect value for the api key", mediator.getApiKey(),
                "sskfnkjoj-xHsdbfkjsP9Tsy2bT3BfihdfbhqwRzOInsOTddljnr");
        assertEquals("Incorrect value for the model name", mediator.getGptModel(), "gpt-4-turbo");
        assertEquals("Incorrect value for the registry file path", mediator.getSchemaPath(),
                "gov:schema.json");
        assertEquals("Incorrect value for the number of tokens prefer", mediator.getMaximumChatGptTokens(), 1000);
    }

    /**
     * Test pdf to image conversion for pdfToImage()
     * @throws IOException
     */
    public void testPdfToImageConversion() throws IOException {
        String sampleBase64Path = "src/test/resources/sampleBase64Pdf.txt";
        String sampleBase64 = new String(Files.readAllBytes(Paths.get(sampleBase64Path)));
        DocumentProcessMediator mediator = new DocumentProcessMediator();
        List<String> base64_images = mediator.pdfToImage(null, sampleBase64);
        assertEquals("Incorrect pdf to Image conversion", base64_images.size(), 2);
    }

    /**
     * Test whether exception is throwing when false base64 provided of pdfToImage().
     * @throws IOException
     */
    public void testPdfToImageConversionFaultSituation() throws IOException {
        String sampleBase64Path = "src/test/resources/falseBase64Example.txt";
        String sampleBase64 = new String(Files.readAllBytes(Paths.get(sampleBase64Path)));
        MessageContext messageContext = createMessageContext();
        DocumentProcessMediator mediator = new DocumentProcessMediator();
        try {
            List<String> base64_images = mediator.pdfToImage(messageContext, sampleBase64);
            assertEquals("Incorrect Base 64 conversion", false);
        } catch (Exception e) {
            assertEquals("Error while converting pdf to image , incorrect Base64",e.getMessage());
        }
    }

    /**
     * Test GPT Prompt with image payload for pdfs on generateGptRequestMessage()
     * @throws IOException
     */
    public void testGptMessagePayloadGenerationForPdf() throws IOException {
        String payloadMessagePath;
        String payloadMessage;
        String gptModel = "gpt-4-turbo";
        InputStream schema = Files.newInputStream(Paths.get("src/test/resources/schema.xsd"));
        DocumentProcessMediator mediator = new DocumentProcessMediator();
        String sampleBase64Path = "src/test/resources/sampleBase64Pdf.txt";
        String sampleBase64 = new String(Files.readAllBytes(Paths.get(sampleBase64Path)));
        String fileName = "testing.pdf";
        String payload;

        payloadMessagePath = "src/test/resources/payloadMessageSamplePdfWithSchema.txt";
        payloadMessage = new String(Files.readAllBytes(Paths.get(payloadMessagePath)));
        payload = mediator.generateGptRequestMessage(null, schema, fileName, sampleBase64, gptModel,
                1000);
        assertEquals("Incorrect Payload generated for pdf when schema has provided", payload, payloadMessage);
        payloadMessagePath = "src/test/resources/payloadMessageSamplePdfWithoutSchema.txt";
        payloadMessage = new String(Files.readAllBytes(Paths.get(payloadMessagePath)));
        payload = mediator.generateGptRequestMessage(null, null, fileName, sampleBase64,
                gptModel, 1000);
        assertEquals("Incorrect Payload generated for pdf when no schema", payload, payloadMessage);


    }

    /**
     * Test GPT Prompt with image payload for images on generateGptRequestMessage()
     * @throws IOException
     */
    public void testGptMessagePayloadGenerationForImage() throws IOException {
        String payloadMessagePath;
        String payloadMessage;
        String gptModel = "gpt-4-turbo";
        InputStream schema = Files.newInputStream(Paths.get("src/test/resources/schema.xsd"));
        DocumentProcessMediator mediator = new DocumentProcessMediator();
        String sampleBase64Path = "src/test/resources/sampleBase64Image.txt";
        String sampleBase64 = new String(Files.readAllBytes(Paths.get(sampleBase64Path)));
        String fileName = "lc.png";
        String payload;

        payloadMessagePath = "src/test/resources/payloadMessageSampleImageWithSchema.txt";
        payloadMessage = new String(Files.readAllBytes(Paths.get(payloadMessagePath)));
        payload = mediator.generateGptRequestMessage(null, schema, fileName, sampleBase64, gptModel,
                1000);
        assertEquals("Incorrect Payload generated for Image when schema has provided", payload,
                payloadMessage);
        payloadMessagePath = "src/test/resources/payloadMessageSampleImageWithoutSchema.txt";
        payloadMessage = new String(Files.readAllBytes(Paths.get(payloadMessagePath)));
        payload = mediator.generateGptRequestMessage(null, null, fileName, sampleBase64,
                gptModel, 1000);
        assertEquals("Incorrect Payload generated for Image when no schema", payload, payloadMessage);
    }

    /**
     * Create Axis2 Message Context.
     *
     * @return msgCtx created message context.
     * @throws AxisFault when exception happens on message context creation.
     */
    private MessageContext createMessageContext() throws AxisFault {
        MessageContext msgCtx = createSynapseMessageContext();
        org.apache.axis2.context.MessageContext axis2MsgCtx = ((Axis2MessageContext) msgCtx).getAxis2MessageContext();
        axis2MsgCtx.setServerSide(true);
        axis2MsgCtx.setMessageID(UUIDGenerator.getUUID());

        return msgCtx;
    }

    /**
     * Create Synapse Context.
     *
     * @return mc created message context.
     * @throws AxisFault when exception happens on message context creation.
     */
    private MessageContext createSynapseMessageContext() throws AxisFault {
        org.apache.axis2.context.MessageContext axis2MC = new org.apache.axis2.context.MessageContext();
        axis2MC.setConfigurationContext(this.configContext);
        ServiceContext svcCtx = new ServiceContext();
        OperationContext opCtx = new OperationContext(new InOutAxisOperation(), svcCtx);
        axis2MC.setServiceContext(svcCtx);
        axis2MC.setOperationContext(opCtx);
        Axis2MessageContext mc = new Axis2MessageContext(axis2MC, this.synapseConfig, null);
        mc.setMessageID(UIDGenerator.generateURNString());
        mc.setEnvelope(OMAbstractFactory.getSOAP12Factory().createSOAPEnvelope());
        mc.getEnvelope().addChild(OMAbstractFactory.getSOAP12Factory().createSOAPBody());

        return mc;
    }
}
