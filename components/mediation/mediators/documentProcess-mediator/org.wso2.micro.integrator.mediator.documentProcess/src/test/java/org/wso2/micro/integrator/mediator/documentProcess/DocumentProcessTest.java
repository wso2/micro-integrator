package org.wso2.micro.integrator.mediator.documentProcess;

import junit.framework.TestCase;
import org.apache.axiom.om.OMElement;
import org.apache.synapse.config.SynapseConfigUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Properties;


public class DocumentProcessTest extends TestCase {
    private static final String mediatorXml = "<documentProcess xmlns=\"http://ws.apache.org/ns/synapse\" api-key=\"sskfnkjoj-xHsdbfkjsP9Tsy2bT3BfihdfbhqwRzOInsOTddljnr\" max-tokens=\"1000\" gpt-model=\"gpt-4-turbo\" ><schema key = \"gov:schema.json\"></schema></documentProcess>";
    public void testMediatorFactory(){
        OMElement mediatorElement = SynapseConfigUtils.stringToOM(mediatorXml);

        DocumentProcessMediatorFactory factory = new DocumentProcessMediatorFactory();
        DocumentProcessMediator mediator = (DocumentProcessMediator) factory.createSpecificMediator(mediatorElement,new Properties());

        assertEquals("Incorrect value for the api key", mediator.getApiKey(), "sskfnkjoj-xHsdbfkjsP9Tsy2bT3BfihdfbhqwRzOInsOTddljnr");
        assertEquals("Incorrect value for the model name", mediator.getGptModel(), "gpt-4-turbo");
        assertEquals("Incorrect value for the registry file path", mediator.getSchemaPath(), "gov:schema.json");
        assertEquals("Incorrect value for the number of tokens prefer", mediator.getMaxTokens(), 1000);
    }

    public void testPdfToImageConversion() throws IOException {
        String sampleBase64Path = "/Users/yasasm/Documents/internProject/micro-integrator/components/mediation/mediators/documentProcess-mediator/org.wso2.micro.integrator.mediator.documentProcess/src/test/resources/sampleBase64Pdf.txt";
        String sampleBase64 = new String(Files.readAllBytes(Paths.get(sampleBase64Path)));
        DocumentProcessMediator mediator = new DocumentProcessMediator();
        List<String> base64_images = mediator.pdfToImage(sampleBase64);
        assertEquals("Incorrect pdf to Image conversion", base64_images.size(),2);
    }

    public void testGptMessagePayloadGenerationForPdf() throws IOException {
        String payloadMessagePath;
        String payloadMessage;
        String gptModel = "gpt-4-turbo";
        InputStream schema = Files.newInputStream(Paths.get("/Users/yasasm/Documents/internProject/micro-integrator/components/mediation/mediators/documentProcess-mediator/org.wso2.micro.integrator.mediator.documentProcess/src/test/resources/schema.xsd"));
        DocumentProcessMediator mediator = new DocumentProcessMediator();
        String sampleBase64Path = "/Users/yasasm/Documents/internProject/micro-integrator/components/mediation/mediators/documentProcess-mediator/org.wso2.micro.integrator.mediator.documentProcess/src/test/resources/sampleBase64Pdf.txt";
        String sampleBase64 = new String(Files.readAllBytes(Paths.get(sampleBase64Path)));
        String fileName = "testing.pdf";
        String payload;

        payloadMessagePath = "/Users/yasasm/Documents/internProject/micro-integrator/components/mediation/mediators/documentProcess-mediator/org.wso2.micro.integrator.mediator.documentProcess/src/test/resources/payloadMessageSamplePdfWithSchema.txt";
        payloadMessage = new String(Files.readAllBytes(Paths.get(payloadMessagePath)));
        payload = mediator.generateGptRequestMessage(schema,fileName, sampleBase64,gptModel,1000);
        assertEquals("Incorrect Payload generated for pdf when schema has provided",payload, payloadMessage);
        payloadMessagePath ="/Users/yasasm/Documents/internProject/micro-integrator/components/mediation/mediators/documentProcess-mediator/org.wso2.micro.integrator.mediator.documentProcess/src/test/resources/payloadMessageSamplePdfWithoutSchema.txt";
        payloadMessage = new String(Files.readAllBytes(Paths.get(payloadMessagePath)));
        payload = mediator.generateGptRequestMessage(null,fileName, sampleBase64,gptModel,1000);
        assertEquals("Incorrect Payload generated for pdf when no schema",payload, payloadMessage);


    }

    public void testGptMessagePayloadGenerationForImage() throws IOException {
        String payloadMessagePath;
        String payloadMessage;
        String gptModel = "gpt-4-turbo";
        InputStream schema = Files.newInputStream(Paths.get("/Users/yasasm/Documents/internProject/micro-integrator/components/mediation/mediators/documentProcess-mediator/org.wso2.micro.integrator.mediator.documentProcess/src/test/resources/schema.xsd"));
        DocumentProcessMediator mediator = new DocumentProcessMediator();
        String sampleBase64Path = "/Users/yasasm/Documents/internProject/micro-integrator/components/mediation/mediators/documentProcess-mediator/org.wso2.micro.integrator.mediator.documentProcess/src/test/resources/sampleBase64Image.txt";
        String sampleBase64 = new String(Files.readAllBytes(Paths.get(sampleBase64Path)));
        String fileName = "lc.png";
        String payload;

        payloadMessagePath = "/Users/yasasm/Documents/internProject/micro-integrator/components/mediation/mediators/documentProcess-mediator/org.wso2.micro.integrator.mediator.documentProcess/src/test/resources/payloadMessageSampleImageWithSchema.txt";
        payloadMessage = new String(Files.readAllBytes(Paths.get(payloadMessagePath)));
        payload = mediator.generateGptRequestMessage(schema,fileName, sampleBase64,gptModel,1000);
        assertEquals("Incorrect Payload generated for Image when schema has provided",payload, payloadMessage);
        payloadMessagePath = "/Users/yasasm/Documents/internProject/micro-integrator/components/mediation/mediators/documentProcess-mediator/org.wso2.micro.integrator.mediator.documentProcess/src/test/resources/payloadMessageSampleImageWithoutSchema.txt";
        payloadMessage = new String(Files.readAllBytes(Paths.get(payloadMessagePath)));
        payload = mediator.generateGptRequestMessage(null,fileName, sampleBase64,gptModel,1000);
        assertEquals("Incorrect Payload generated for Image when no schema",payload, payloadMessage);
    }
}
