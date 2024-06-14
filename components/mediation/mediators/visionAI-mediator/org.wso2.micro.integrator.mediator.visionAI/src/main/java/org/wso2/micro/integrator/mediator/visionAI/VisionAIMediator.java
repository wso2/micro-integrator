package org.wso2.micro.integrator.mediator.visionAI;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.pdfbox.io.IOUtils;
import org.apache.synapse.mediators.AbstractMediator;
import org.apache.synapse.MessageContext;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.springframework.web.multipart.MultipartFile;
import org.wso2.micro.integrator.registry.Resource;
import org.wso2.micro.integrator.registry.MicroIntegratorRegistry ;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

public class VisionAIMediator extends AbstractMediator {

    /**
     * This the secret key for chatgpt vision
     */
    private String apiKey = "";

    /**
     * This the path for json of pattern that user need
     */
    private String jsonFilePath = "";

    MicroIntegratorRegistry registry = new MicroIntegratorRegistry();

    private Resource resource = null;

    private InputStream jsonStream = null;

    private MultipartFile multipartFile = null;

    public boolean mediate(MessageContext synCtx) {



        this.resource = registry.getResource(jsonFilePath);

        try {
            this.jsonStream = resource.getContentStream();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }


        List<String> base64_images = new ArrayList<>();

        List<String> requests = new ArrayList<>();
        StringBuilder res = new StringBuilder();

        // Reading the schema file
        String schemaJson = null;
        try {
            schemaJson = readSchema(this.jsonStream).replace(" ","").replace("\"","").replace("\t","").replace("\n","");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        System.out.println(schemaJson.replace(" ",""));

        // Getting the base64 string
        if(multipartFile.getContentType().equals("pdf")){
            try {
                base64_images = pdfToImage(multipartFile.getInputStream());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            System.out.println(base64_images.size());
            for(int i = 0; i < Objects.requireNonNull(base64_images).size(); i++){
                String request = "{\"type\": \"image_url\", \"image_url\": {\"url\": \"data:image/jpeg;base64," + base64_images.get(i) + "\"}}";
                requests.add(request);
            }
            res = new StringBuilder(requests.get(0));

            for(int i = 1; i < requests.size(); i++){
                res.append(",").append(requests.get(i));
            }
            System.out.println(res);

            //System.out.println(requests.get(0));
        } else {
            try {
                base64_images.add(encodeImage(multipartFile.getInputStream()));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            String request = "{\"type\": \"image_url\", \"image_url\": {\"url\": \"data:image/jpeg;base64," + base64_images.get(0) + "\"}}";
            res = new StringBuilder(request);
        }

        // API endpoint
        String endpoint = "https://api.openai.com/v1/chat/completions";

        // Constructing the JSON payload
        String payload = "{\"model\": \"gpt-4-turbo\", \"messages\": [{\"role\": \"user\", \"content\": [{\"type\": \"text\", \"text\": \"What’s in this image? Retrieve all form fields and  values as json(get xml values inside fields as a form field), return as provided schema ," +schemaJson +" as json object {fieldName:FieldValue} only fields in schema, only return this object is enough, these are should be keys of the json under content, key names should be same as provided, case sensitive.\"},"+res+"]}], \"response_format\": {\"type\": \"json_object\"}, \"max_tokens\": 300}";
        //System.out.println(payload);
        // Setting up the HTTP connection
        URL url = null;
        try {
            url = new URL(endpoint);
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
        HttpURLConnection connection = null;
        try {
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestProperty("Authorization", "Bearer " + apiKey);
        connection.setDoOutput(true);

        // Sending the request
        try (OutputStream os = connection.getOutputStream()) {
            byte[] input = payload.getBytes("utf-8");
            os.write(input, 0, input.length);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // Reading the response
        try (BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream(), "utf-8"))) {
            StringBuilder response = new StringBuilder();
            String responseLine = null;
            while ((responseLine = br.readLine()) != null) {
                response.append(responseLine.trim());
            }
            String jsonString = response.toString();
            System.out.println(jsonString);

            // Parsing the JSON response
            // Parse the JSON response string into a JsonObject
            JsonObject jsonObject = JsonParser.parseString(jsonString).getAsJsonObject();
            System.out.println(jsonObject);

            // Get the content string
            String contentString = jsonObject.getAsJsonArray("choices").get(0).getAsJsonObject().getAsJsonObject("message").get("content").getAsString();
            System.out.println(contentString);

            // Parse the content string into a JsonObject
            JsonObject contentObject = JsonParser.parseString(contentString).getAsJsonObject();
            System.out.println(contentObject.get("Value").getAsString());
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
     * @param jsonFilePath String value to be set as jsonFilePath
     */
    public void setJsonFilePath(String jsonFilePath) {
        this.jsonFilePath = jsonFilePath;
    }


    private static String encodeImage(InputStream inputStream) throws IOException {
            byte[] bytes = IOUtils.toByteArray(inputStream);
            return Base64.getEncoder().encodeToString(bytes);
    }

    private static String readSchema(InputStream jsonStream) throws IOException {
        StringBuilder schemaJson = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(jsonStream))) {
            String line;
            while ((line = br.readLine()) != null) {
                schemaJson.append(line);
            }
        }
        return schemaJson.toString();
    }

    private static List<String> pdfToImage(InputStream pdfInputStream) throws IOException {
        try {
            PDDocument document = PDDocument.load(pdfInputStream);
            PDFRenderer pdfRenderer = new PDFRenderer(document);
            List<BufferedImage> images = new ArrayList<>();
            List<String> encodedImages = new ArrayList<>();

            int numberOfPages = document.getNumberOfPages();
            System.out.println("Total files to be converting -> " + numberOfPages);

            int dpi = 300; // use less dpi to save more space in hard disk. For professional usage, you can use more than 300dpi

            for (int i = 0; i < numberOfPages; ++i) {
                BufferedImage bImage = pdfRenderer.renderImageWithDPI(i, dpi, ImageType.RGB);
                images.add(bImage);
            }

            document.close();

            for (BufferedImage image : images) {
                String base64Image = encodeConvertedImage(image);
                encodedImages.add(base64Image);
            }

            System.out.println(encodedImages.size());
            return encodedImages;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    private static String encodeConvertedImage(BufferedImage image) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ImageIO.write(image, "png", byteArrayOutputStream);
        byte[] bytes = byteArrayOutputStream.toByteArray();
        return Base64.getEncoder().encodeToString(bytes);
    }
}
