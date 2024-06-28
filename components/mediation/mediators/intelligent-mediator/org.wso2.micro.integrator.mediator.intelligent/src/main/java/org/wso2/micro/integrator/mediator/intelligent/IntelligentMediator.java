/*
 * Copyright (c) 2024, WSO2 LLC. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
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
package org.wso2.micro.integrator.mediator.intelligent;

import org.apache.axis2.AxisFault;
import org.apache.synapse.commons.json.JsonUtil;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.apache.synapse.mediators.AbstractMediator;
import org.apache.synapse.MessageContext;

import com.azure.ai.openai.OpenAIClient;
import com.azure.ai.openai.OpenAIClientBuilder;
import com.azure.ai.openai.models.*;
import com.azure.core.credential.AzureKeyCredential;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

import org.wso2.micro.integrator.mediator.intelligent.util.RequestSchema;
import org.wso2.micro.integrator.mediator.intelligent.util.ResponseSchema;

import java.util.ArrayList;
import java.util.List;

/**
 * This mediator will ask for a prompt from the user and is able to send the request to OpenAI with a
 * JSON or XML payload within this mediator or in the message context.
 */
public class IntelligentMediator extends AbstractMediator {

    private String openaiKey = "";

    private String openaiModel = "gpt-3.5-turbo";

    private String openaiEndpoint = "https://api.openai.com/v1/chat/completions";

    private String basePrompt = "";

    private String retryPrompt = "";

    private String prompt = "";

    private String headers = "";

    private String payload = "";

    private String requestSchema;

    private String responseSchema;

    private int retryCount = 3;

    private int remainingRetries = 3;

    /**
     * After reading the prompt from the user, this method will initialize the mediator.
     */
    public void initialize() {
        basePrompt = "You are a helpful assistant who do what I tell you. I will provide a request-schema and a " +
                "response-schema. The task to do is available in the request-schema (task prop). The data you have " +
                "to process is available in the request-schema (payload prop). Avoid xmlns attribute if data is " +
                "given in xml format. When you're giving an answer, always use the response-schema. The processed " +
                "data should be in the data prop. This processed data can be json, xml  or text. It depends on the " +
                "task. Set the dataType prop in response to 'json', 'xml' or 'text' depending on the processed " +
                "data. And don't forget to set the status prop to 'success' or 'error' depending on the result of " +
                "the task.";

        retryPrompt = "The response you provided is not in the given format in response-schema. Please provide a " +
                "valid response.";

        RequestSchema request = new RequestSchema(prompt, payload, headers);
        ResponseSchema response = new ResponseSchema();

        Gson gsonBuilder = new GsonBuilder().setPrettyPrinting().create();
        requestSchema = gsonBuilder.toJson(request);
        responseSchema = gsonBuilder.toJson(response);
    }

    /**
     * This method will convert a given string into JSON object.
     *
     * @param json The JSON data in string format.
     * @return The JSON object.
     */
    public JsonObject loadJsonData(String json) {
        try {
            return JsonParser.parseString(json).getAsJsonObject();
        } catch (JsonSyntaxException e) {
            return null;
        }
    }

    /**
     * This method will check whether the given response is in the correct format as per the response-schema.
     *
     * @param response The response from OpenAI.
     * @return Whether the response is in the correct format. (true | false)
     */
    public boolean isInvalidResponse(String response) {
        JsonObject jsonNode = loadJsonData(response);

        if (jsonNode == null || jsonNode.get("data") == null) {
            return true;
        }

        if (jsonNode.get("dataType") != null) {
            String type = jsonNode.get("dataType").getAsString();
            switch (type.toLowerCase()) {
                case "json":
                case "xml":
                case "text":
                    return false;
                default:
                    return true;
            }
        }

        return true;
    }

    /**
     * This method will send the request to OpenAI and get the response. If the response is not in the given format
     * in response-schema, it will resend a request with the given response saying that the given response is not in
     * the correct format.
     *
     * @param client   The OpenAI client.
     * @param response The given response from OpenAI (if any).
     * @return The correct response from OpenAI.
     */
    public List<ChatChoice> getResponses(OpenAIClient client, String response) {
        String userMessage = "request-schema: \n" + requestSchema + "\nresponse-schema: \n" + responseSchema;

        List<ChatRequestMessage> chatMessages = new ArrayList<>();
        chatMessages.add(new ChatRequestSystemMessage(basePrompt));
        chatMessages.add(new ChatRequestUserMessage(userMessage));

        if (!response.isEmpty()) {
            chatMessages.add(new ChatRequestAssistantMessage(response));
            chatMessages.add(new ChatRequestUserMessage(retryPrompt));
        }

        ChatCompletions chatCompletions = client.getChatCompletions(
                openaiModel,
                new ChatCompletionsOptions(chatMessages)
        );
        remainingRetries--;

        return chatCompletions.getChoices();
    }

    public boolean mediate(MessageContext synCtx) {

        // TODO: This mediator only support for INLINE payload at the moment.
        //  Need to add support for other types like the `Enrich Mediator`.
        //  INLINE, PROPERTY, BODY, ENVELOPE, CUSTOM

        org.apache.axis2.context.MessageContext msgContext = ((Axis2MessageContext) synCtx).getAxis2MessageContext();

        try {
            OpenAIClientBuilder builder = new OpenAIClientBuilder()
                    .credential(new AzureKeyCredential(openaiKey))
                    .endpoint(openaiEndpoint);

            OpenAIClient client = builder.buildClient();
            List<ChatChoice> chats = getResponses(client, "");

            boolean noValidResponse = true;
            String response = "";
            while (noValidResponse && remainingRetries > 0) {
                for (ChatChoice chat : chats) {
                    response = chat.getMessage().getContent();
                    if (!isInvalidResponse(response)) {
                        noValidResponse = false;
                        break;
                    }
                }
                if (noValidResponse) {
                    chats = getResponses(client, response);
                }
            }

            if (noValidResponse) {
                JsonUtil.getNewJsonPayload(
                        msgContext,
                        "Failed to get a valid JSON response after " + retryCount + " attempts.",
                        true,
                        true
                );
            } else {
                JsonUtil.getNewJsonPayload(
                        msgContext,
                        String.valueOf(response),
                        true,
                        true
                );
            }
            msgContext.setProperty(IntelligentConstants.MESSAGE_TYPE_STRING, IntelligentConstants.JSON_CONTENT_TYPE);
            msgContext.setProperty(IntelligentConstants.CONTENT_TYPE_STRING, IntelligentConstants.JSON_CONTENT_TYPE);
        } catch (AxisFault e) {
            handleException("JsonUtil error.", e, synCtx);
        }
        return true;
    }

    public void setPrompt(String prompt) {
        this.prompt = prompt;
    }

    public void setHeaders(String headers) {
        this.headers = headers;
    }

    public void setPayload(String payload) {
        this.payload = payload;
    }

    public void setOpenaiKey(String openaiKey) {
        this.openaiKey = openaiKey;
    }

    public void setOpenaiModel(String openaiModel) {
        this.openaiModel = openaiModel;
    }

    public void setOpenaiEndpoint(String openaiEndpoint) {
        this.openaiEndpoint = openaiEndpoint;
    }

    public void setRetryCount(int retryCount) {
        this.retryCount = retryCount;
        this.remainingRetries = retryCount;
    }
}
