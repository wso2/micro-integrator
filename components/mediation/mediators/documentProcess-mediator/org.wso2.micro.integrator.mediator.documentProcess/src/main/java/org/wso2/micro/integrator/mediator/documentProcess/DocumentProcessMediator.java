package org.wso2.micro.integrator.mediator.documentProcess;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.commons.io.IOUtils;
import org.apache.synapse.commons.json.JsonUtil;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.apache.synapse.mediators.AbstractMediator;
import org.apache.synapse.MessageContext;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.wso2.micro.integrator.registry.Resource;
import org.wso2.micro.integrator.registry.MicroIntegratorRegistry ;

import javax.imageio.ImageIO;
import javax.xml.namespace.QName;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;

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
    private int maxTokens ;

    /**
     * This is gpt model name that is using
     */
    private String gptModel;

    private InputStream jsonStream = null;

    public boolean mediate(MessageContext synCtx) {

        org.apache.axis2.context.MessageContext msgContext = ((Axis2MessageContext) synCtx).getAxis2MessageContext();
        String filename = msgContext.getEnvelope().getBody().getFirstElement().getFirstElement().getAttributeValue(QName.valueOf("filename"));
        String fileContent = msgContext.getEnvelope().getBody().getFirstElement().getFirstElement().getText();
        this.jsonStream = getSchemaFromRegistry(this.SchemaPath);

        // Constructing the JSON payload
        String payload = null;
        payload = generateGptRequestMessage(jsonStream,filename,fileContent,gptModel,maxTokens);

        // API endpoint
        String endpoint = "https://api.openai.com/v1/chat/completions";

        //URL instance for connection
        URL url;
        try {
            url = new URL(endpoint);
        } catch (MalformedURLException e) {
            throw new RuntimeException(e + "GPT endpoint url is not valid url");
        }

        //Constructing HttpURLConnection
        HttpURLConnection connection;
        try {
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
            throw new RuntimeException(e + "Request time out");
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
            String contentString = jsonObject.getAsJsonArray("choices").get(0).getAsJsonObject().getAsJsonObject("message").get("content").getAsString();

            // Parse the content string into a JsonObject
            JsonObject contentObject = JsonParser.parseString(contentString).getAsJsonObject();

            JsonUtil.getNewJsonPayload(msgContext, String.valueOf(contentObject),true,true);
            msgContext.setProperty("messageType", "application/json");
            msgContext.setProperty("ContentType", "application/json");

        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }

        // Disconnecting the connection
        connection.disconnect();
        System.out.println("visionAI Mediator");
        return true;
    }

    /**
     * @param apiKey String value to be set as apiKey.
     */
    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    /**
     *
     * @param schemaPath String value to be set as SchemaPath
     */
    public void setSchemaPath(String schemaPath) {
        this.SchemaPath = schemaPath;
    }

    /**
     *
     * @param maxTokens String value to be set as maxTokens
     */
    public void setMaxTokens(int maxTokens) {
        this.maxTokens = maxTokens;
    }

    /**
     *
     * @param gptModel String value to be set as gptModel
     */
    public void setGptModel(String gptModel){
        this.gptModel = gptModel;
    }

    /**
     * This method gives apiKey of the chat gpt.
     *
     * @return gpt key
     */
    public String getApiKey() {
        return apiKey;
    }

    /**
     *
     * @return registry path of the json schema file
     */
    public String getSchemaPath() {
        return SchemaPath;
    }

    /**
     *
     * @return name of the gpt model
     */
    public String getGptModel() {
        return gptModel;
    }

    /**
     *
     * @return preferred amount of gpt tokens
     */
    public int getMaxTokens() {
        return  maxTokens;
    }

    public static String generateGptRequestMessage(InputStream schemaStream, String filename, String fileContent, String gptModel , int maxTokens) {
        //Contain converted pdf pages into images in Base 64
        List<String> base64_images = new ArrayList<>();

        //Contain list of gpt messages of image url type for each image of the pdf
        List<String> imageRequestList = new ArrayList<>();
        //Hold total image url messages as a String
        StringBuilder imageRequestPayload;

        // Reading the schema file
        String schemaJson;
        try {
            schemaJson = IOUtils.toString(schemaStream, StandardCharsets.UTF_8).replace("\"","").replace("\t","").replace("\n","");
        } catch (Exception e){
            schemaJson = "";
        }

        System.out.println(schemaJson);

        //Prompt Message when json schema not provided
        final String noSchemaPromptMessage = "Retrieve all form fields and values as JSON (get XML containing fields also not xml tags whole one as one field with actual field name in front it), return as JSON object {fieldName:FieldValue} including all fields . The key names should match the field names exactly and should be case sensitive. Scan the whole document and retrieve all fields.";

        //Prompt Message when json schema not provided
        final String withSchemaPromptMessage = "Retrieve all form fields and  values as json (get XML containing fields also not xml tags whole one as one field with actual field name in front it), return as provided schema (Only values related to schema) ," +schemaJson +" as json object {fieldName:FieldValue} only fields in schema, only return this object is enough, these are should be keys of the json under content, key names should be same as provided, case sensitive (scan whole document and check, get all fields there could be some lengthy fields,xml fields like value).";

        // Getting the base64 string
        if(filename.endsWith("pdf")){
            try {
                base64_images = pdfToImage(fileContent);
            } catch (IOException e) {
                throw new RuntimeException(e + "Error in the content, not a valid pdf in BASE64");
            }

            for(int i = 0; i < Objects.requireNonNull(base64_images).size(); i++){
                String imageRequest = "{\"type\": \"image_url\", \"image_url\": {\"url\": \"data:image/jpeg;base64," + base64_images.get(i) + "\"}}";
                imageRequestList.add(imageRequest);
            }
            imageRequestPayload = new StringBuilder(imageRequestList.get(0));

            for(int i = 1; i < imageRequestList.size(); i++){
                imageRequestPayload.append(",").append(imageRequestList.get(i));
            }

        } else {
            base64_images.add(fileContent);
            String imageRequest = "{\"type\": \"image_url\", \"image_url\": {\"url\": \"data:image/jpeg;base64," + base64_images.get(0) + "\"}}";
            imageRequestPayload = new StringBuilder(imageRequest);
        }

        // Constructing the JSON payload
        String payload;
        if(schemaJson.isEmpty()){
            payload = "{\"model\": \""+gptModel+"\", \"messages\": [{\"role\": \"user\", \"content\": [{\"type\": \"text\", \"text\": \" "+noSchemaPromptMessage+"\"},"+imageRequestPayload+"]}], \"response_format\": {\"type\": \"json_object\"}, \"max_tokens\":"+maxTokens+"}";
        }
        else {
            payload = "{\"model\": \""+gptModel+"\", \"messages\": [{\"role\": \"user\", \"content\": [{\"type\": \"text\", \"text\": \" "+withSchemaPromptMessage+"\"},"+imageRequestPayload+"]}], \"response_format\": {\"type\": \"json_object\"}, \"max_tokens\":"+maxTokens+"}";
        }
        return payload;
    }

    private static  InputStream getSchemaFromRegistry(String schemaPath){
        MicroIntegratorRegistry registry = new MicroIntegratorRegistry();
        Resource resource;
        InputStream jsonStream;
        if(!schemaPath.isEmpty()) {
            resource = registry.getResource(schemaPath);
            try {
                jsonStream = resource.getContentStream();
                return jsonStream;
            } catch (IOException e) {
                throw new RuntimeException(e + "Cannot read resources in registry path");
            }
        }
        return null;
    }

    public static List<String> pdfToImage(String base64Pdf) throws IOException {
        byte[] decodedBytes = Base64.getDecoder().decode(base64Pdf);
        try (InputStream pdfInputStream = new ByteArrayInputStream(decodedBytes)) {
            PDDocument document = PDDocument.load(pdfInputStream);
            PDFRenderer pdfRenderer = new PDFRenderer(document);
            List<BufferedImage> images = new ArrayList<>();
            List<String> encodedImages = new ArrayList<>();

            int numberOfPages = document.getNumberOfPages();
            System.out.println("Total files to be converting -> " + numberOfPages);

            int dpi = 300; // Use less dpi to save more space on the hard disk. For professional usage, you can use more than 300dpi

            for (int i = 0; i < numberOfPages; ++i) {
                BufferedImage bImage = pdfRenderer.renderImageWithDPI(i, dpi, ImageType.RGB);
                images.add(bImage);
            }

            document.close();

            for (BufferedImage image : images) {
                String base64Image = encodeConvertedImage(image);
                encodedImages.add(base64Image);
            }
            return encodedImages;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e + "Error in reading content");
        }
    }

    private static String encodeConvertedImage(BufferedImage image) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ImageIO.write(image, "png", byteArrayOutputStream);
        byte[] bytes = byteArrayOutputStream.toByteArray();
        return Base64.getEncoder().encodeToString(bytes);
    }
}
