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

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.axiom.om.OMElement;
import org.apache.commons.io.IOUtils;
import org.apache.synapse.commons.json.JsonUtil;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.apache.synapse.mediators.AbstractMediator;
import org.apache.synapse.MessageContext;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.wso2.micro.integrator.registry.Resource;
import org.wso2.micro.integrator.registry.MicroIntegratorRegistry;

import javax.imageio.ImageIO;
import javax.xml.namespace.QName;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Objects;
import java.io.ByteArrayOutputStream;
import java.io.ByteArrayInputStream;

/**
 * If a request comes to this class it create request to the gpt and respond with the result. Otherwise it will pass on
 * the request to the next mediator.
 */
public class DocumentProcessMediator extends AbstractMediator {

    /**
     * This the secret key for chatgpt vision
     */
    private String apiKey = "";

    /**
     * This the path for json of pattern that user need
     */
    private String SchemaPath = "";

    /**
     * This is the maximum tokens for GPT API request
     */
    private int maximumChatGptTokens;

    /**
     * This is gpt model name that is using
     */
    private String gptModel;

    private InputStream schemaStream = null;

    /**
     * {@inheritDoc}
     */
    public boolean mediate(MessageContext synCtx) {

        org.apache.axis2.context.MessageContext msgContext = ((Axis2MessageContext) synCtx).getAxis2MessageContext();
        final String filename = getNameFromMessageContext(msgContext);
        final String fileContent = getContentFromMessageContext(msgContext);
        if (filename.isEmpty()) {
            handleException("Cannot find the filename in the payload", synCtx);
        } else if (fileContent.isEmpty()) {
            handleException("Cannot find the content in the payload", synCtx);
        }

        this.schemaStream = getSchemaFromRegistry(synCtx, this.SchemaPath);

        // Constructing the JSON payload
        String payload = null;
        payload = generateGptRequestMessage(synCtx, schemaStream, filename, fileContent, gptModel, maximumChatGptTokens);


        //URL instance for connection
        URL url = null;
        try {
            url = new URL(DocumentProcessConstants.GPT_API_ENDPOINT_STRING);
        } catch (MalformedURLException e) {
            handleException("GPT endpoint url is not valid url", e, synCtx);
        }

        //Constructing HttpURLConnection
        HttpURLConnection connection;
        try {
            assert url != null;
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
        } catch (IOException e) {
            throw new RuntimeException(e + "Connection to the url unsuccessful");
        }

        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestProperty("Authorization", "Bearer " + apiKey);
        connection.setDoOutput(true);

        // Sending the request
        try (OutputStream os = connection.getOutputStream()) {
            byte[] input = payload.getBytes("utf-8");
            os.write(input, 0, input.length);
        } catch (IOException e) {
            handleException("Request time out", e, synCtx);
        }

        // Reading the response
        try (BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream(), "utf-8"))) {
            StringBuilder response = new StringBuilder();
            String responseLine = null;
            while ((responseLine = br.readLine()) != null) {
                response.append(responseLine.trim());
            }
            String jsonString = response.toString();

            // Parsing the JSON response
            // Parse the JSON response string into a JsonObject
            JsonObject jsonObject = JsonParser.parseString(jsonString).getAsJsonObject();

            // Get the content string
            String contentString = jsonObject.getAsJsonArray("choices").get(0).getAsJsonObject().
                    getAsJsonObject("message").get("content").getAsString();

            // Parse the content string into a JsonObject
            JsonObject contentObject = JsonParser.parseString(contentString).getAsJsonObject();

            JsonUtil.getNewJsonPayload(msgContext, String.valueOf(contentObject), true, true);
            msgContext.setProperty("messageType", "application/json");
            msgContext.setProperty("ContentType", "application/json");

        } catch (Exception e) {
            handleException("Error reading the response from API", e, synCtx);
        }

        // Disconnecting the connection
        connection.disconnect();
        return true;
    }

    /**
     * @param apiKey String value to be set as apiKey.
     */
    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    /**
     * @param schemaPath String value to be set as SchemaPath
     */
    public void setSchemaPath(String schemaPath) {
        this.SchemaPath = schemaPath;
    }

    /**
     * @param maximumChatGptTokens String value to be set as maximumChatGptTokens
     */
    public void setMaximumChatGptTokens(int maximumChatGptTokens) {
        this.maximumChatGptTokens = maximumChatGptTokens;
    }

    /**
     * @param gptModel String value to be set as gptModel
     */
    public void setGptModel(String gptModel) {
        this.gptModel = gptModel;
    }

    /**
     * This method gives apiKey of the ChatGPT.
     *
     * @return gpt key
     */
    public String getApiKey() {
        return apiKey;
    }

    /**
     * @return registry path of the json schema file
     */
    public String getSchemaPath() {
        return SchemaPath;
    }

    /**
     * @return name of the gpt model
     */
    public String getGptModel() {
        return gptModel;
    }

    /**
     * @return preferred amount of gpt tokens
     */
    public int getMaximumChatGptTokens() {
        return maximumChatGptTokens;
    }

    /**
     * Generate total prompt message with images that need for payload to send to ChatGPT
     *
     * @param messageContext Message Context
     * @param schemaStream   Schema file as a stream from registry
     * @param filename       Content Name
     * @param fileContent    Content received from request
     * @param gptModel       Model using for task
     * @param maxTokens      Maxium tokens preferred to use for the prompt
     * @return payload as String
     */
    public String generateGptRequestMessage(MessageContext messageContext, InputStream schemaStream, String filename,
                                            String fileContent, String gptModel, int maxTokens) {
        //Contain converted pdf pages into images in Base 64
        List<String> base64_images = new ArrayList<>();

        //Contain list of gpt messages of image url type for each image of the pdf
        List<String> imageRequestList = new ArrayList<>();
        //Hold total image url messages as a String
        StringBuilder imageRequestPayload;

        // Reading the schema file
        String schema = "";
        if (schemaStream != null) {
            try {
                schema = IOUtils.toString(schemaStream, StandardCharsets.UTF_8).replace("\"", "").
                        replace("\t", "").replace("\n", "");
            } catch (IOException e) {
                handleException("Error with the schema content reading", e, messageContext);
            }
        } else {
            schema = "";
        }
        //TODO : check whether document is other than image or pdf
        // Getting the base64 string
        if (filename.endsWith("pdf")) {
            base64_images = pdfToImage(messageContext, fileContent);

            for (int i = 0; i < Objects.requireNonNull(base64_images).size(); i++) {
                String imageRequest = "{\"type\": \"image_url\", \"image_url\": {\"url\": \"data:image/jpeg;base64," +
                        base64_images.get(i) + "\"}}";
                imageRequestList.add(imageRequest);
            }
            imageRequestPayload = new StringBuilder(String.join(",", imageRequestList));

        } else {
            base64_images.add(fileContent);
            String imageRequest = "{\"type\": \"image_url\", \"image_url\": {\"url\": \"data:image/jpeg;base64," +
                    base64_images.get(0) + "\"}}";
            imageRequestPayload = new StringBuilder(imageRequest);
        }

        // Constructing the JSON payload
        String payload;
        if (schema.isEmpty()) {
            payload = "{\"model\": \"" + gptModel + "\", \"messages\": [{\"role\": \"user\", \"content\": " +
                    "[{\"type\": \"text\", \"text\": \" " + DocumentProcessConstants.NO_SCHEMA_PROMPT_STRING + "\"}," +
                    imageRequestPayload + "]}], \"response_format\": {\"type\": \"json_object\"}, \"max_tokens\":" +
                    maxTokens + "}";
        } else {
            payload = "{\"model\": \"" + gptModel + "\", \"messages\": [{\"role\": \"user\", \"content\": " +
                    "[{\"type\": \"text\", \"text\": \" " + DocumentProcessConstants.SCHEMA_PROMPT_STRING_1 +
                    schema + DocumentProcessConstants.SCHEMA_PROMPT_STRING_2 + "\"}," + imageRequestPayload +
                    "]}], \"response_format\": {\"type\": \"json_object\"}, \"max_tokens\":" + maxTokens + "}";
        }
        return payload;
    }

    /**
     * Reading the schema from the registry
     *
     * @param messageContext Message Context
     * @param schemaPath     Registry Path of the Schema
     * @return
     */
    private InputStream getSchemaFromRegistry(MessageContext messageContext, String schemaPath) {
        MicroIntegratorRegistry registry = new MicroIntegratorRegistry();
        Resource resource;
        InputStream schemaStream;
        if (!schemaPath.isEmpty()) {
            resource = registry.getResource(schemaPath);
            try {
                schemaStream = resource.getContentStream();
                return schemaStream;
            } catch (IOException e) {
                handleException("Error while reading schema from registry", e, messageContext);
            }
        }
        return null;
    }

    /**
     * Convert PDF to Images and return as Base64 String List
     *
     * @param messageContext Message Context
     * @param base64Pdf      Content of the pdf in Base64
     * @return
     */
    public List<String> pdfToImage(MessageContext messageContext, String base64Pdf) {
        try (InputStream pdfInputStream = new ByteArrayInputStream(Base64.getDecoder().decode(base64Pdf))) {
            PDDocument document = PDDocument.load(pdfInputStream);
            PDFRenderer pdfRenderer = new PDFRenderer(document);
            List<BufferedImage> images = new ArrayList<>();
            List<String> encodedImages = new ArrayList<>();

            int numberOfPages = document.getNumberOfPages();

            for (int i = 0; i < numberOfPages; ++i) {
                BufferedImage bImage = pdfRenderer.renderImageWithDPI(i, DocumentProcessConstants.dpi, ImageType.RGB);
                images.add(bImage);
            }

            document.close();

            for (BufferedImage image : images) {
                String base64Image = encodeConvertedImage(image);
                encodedImages.add(base64Image);
            }
            return encodedImages;
        } catch (Exception e) {
            handleException("Error while converting pdf to image , incorrect Base64", e, messageContext);
            return null;
        }
    }

    /**
     * Receive converted pdf pages as Buffered Images and return In Base64
     *
     * @param image Buffered Image
     * @return Images as Base64
     * @throws IOException
     */
    private static String encodeConvertedImage(BufferedImage image) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ImageIO.write(image, "png", byteArrayOutputStream);
        byte[] bytes = byteArrayOutputStream.toByteArray();
        return Base64.getEncoder().encodeToString(bytes);
    }

    /**
     * Check whether elements are null or not and, extract Content and return
     *
     * @param messageContext Message Context
     * @return
     */
    private String getContentFromMessageContext(org.apache.axis2.context.MessageContext messageContext) {
        OMElement firstElement = messageContext.getEnvelope().getBody().getFirstElement();
        if (firstElement.getFirstElement() != null) {
            return firstElement.getFirstElement().getText();
        } else {
            return "";
        }
    }

    /**
     * Check whether elements are null or not and, extract Name of the content and return
     *
     * @param messageContext Message Context
     * @return
     */
    private String getNameFromMessageContext(org.apache.axis2.context.MessageContext messageContext) {
        OMElement firstElement = messageContext.getEnvelope().getBody().getFirstElement();
        if (firstElement.getFirstElement() != null) {
            OMElement secondElement = firstElement.getFirstElement();
            if (secondElement.getAttributeValue(QName.valueOf("filename")) != null) {
                return secondElement.getAttributeValue(QName.valueOf("filename"));
            } else {
                return "";
            }
        } else {
            return "";
        }
    }
}
