/*
 *  Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.esb.api.apidefinition;

import org.apache.http.HttpResponse;
import org.junit.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.esb.integration.common.utils.ESBIntegrationTest;
import org.wso2.esb.integration.common.utils.clients.SimpleHttpClient;

public class CustomSwaggerWithSpecialCharactersTestCase extends ESBIntegrationTest {

    private final SimpleHttpClient httpClient = new SimpleHttpClient();

    @BeforeClass(alwaysRun = true)
    public void setEnvironment() throws Exception {

        super.init();
    }

    @Test(groups = "wso2.esb", description = "Retrieve custom swagger without special characters in json format")
    public void testCustomSwaggerJSON() throws Exception {

        String restURL = getMainSequenceURL() + "CustomSwagger?swagger.json";

        HttpResponse response = httpClient.doGet(restURL, null);
        String payload = httpClient.getResponsePayload(response);

        String expectedSwaggerJson = "{\n" +
                "  \"swagger\": \"2.0\",\n" +
                "  \"info\": {\n" +
                "    \"version\": \"1.0.0\",\n" +
                "    \"title\": \"TestProject\",\n" +
                "    \"description\": \"Test Project\"\n" +
                "  },\n" +
                "  \"host\": \"localhost:8290\",\n" +
                "  \"basePath\": \"/services\",\n" +
                "  \"schemes\": [\n" +
                "    \"http\"\n" +
                "  ],\n" +
                "  \"tags\": [\n" +
                "    {\n" +
                "      \"name\": \"Test\",\n" +
                "      \"description\": \"Test\"\n" +
                "    }\n" +
                "  ],\n" +
                "  \"paths\": {\n" +
                "    \"/test\": {\n" +
                "      \"post\": {\n" +
                "        \"summary\": \"test\",\n" +
                "        \"tags\": [\n" +
                "          \"Test\"\n" +
                "        ],\n" +
                "        \"consumes\": [\n" +
                "          \"application/json; charset=utf-8\"\n" +
                "        ],\n" +
                "        \"produces\": [\n" +
                "          \"application/json; charset=utf-8\"\n" +
                "        ],\n" +
                "        \"parameters\": [\n" +
                "          {\n" +
                "            \"in\": \"body\",\n" +
                "            \"name\": \"request\",\n" +
                "            \"schema\": {\n" +
                "              \"$ref\": \"#/definitions/TestType\"\n" +
                "            }\n" +
                "          }\n" +
                "        ],\n" +
                "        \"responses\": {\n" +
                "          \"200\": {\n" +
                "            \"description\": \"Request accepted\",\n" +
                "            \"schema\": {\n" +
                "              \"type\": \"object\",\n" +
                "              \"properties\": {\n" +
                "                \"status\": {\n" +
                "                  \"type\": \"string\",\n" +
                "                  \"description\": \"Status\",\n" +
                "                  \"enum\": [\n" +
                "                    \"ACCEPTED\"\n" +
                "                  ],\n" +
                "                  \"example\": \"ACCEPTED\"\n" +
                "                }\n" +
                "              }\n" +
                "            }\n" +
                "          },\n" +
                "          \"400\": {\n" +
                "            \"description\": \"Bad Request\",\n" +
                "            \"schema\": {\n" +
                "              \"$ref\": \"#/definitions/ErrorType\"\n" +
                "            }\n" +
                "          },\n" +
                "          \"500\": {\n" +
                "            \"description\": \"System Error\",\n" +
                "            \"schema\": {\n" +
                "              \"$ref\": \"#/definitions/ErrorType\"\n" +
                "            }\n" +
                "          }\n" +
                "        }\n" +
                "      }\n" +
                "    }\n" +
                "  },\n" +
                "  \"definitions\": {\n" +
                "    \"TestType\": {\n" +
                "      \"type\": \"object\",\n" +
                "      \"required\": [\n" +
                "        \"msgId\"\n" +
                "      ],\n" +
                "      \"properties\": {\n" +
                "        \"msgId\": {\n" +
                "          \"type\": \"string\",\n" +
                "          \"description\": \"UUID\",\n" +
                "          \"example\": \"123456\"\n" +
                "        }\n" +
                "      }\n" +
                "    },\n" +
                "    \"ErrorType\": {\n" +
                "      \"type\": \"object\",\n" +
                "      \"required\": [\n" +
                "        \"errorCode\",\n" +
                "        \"errorMessage\"\n" +
                "      ],\n" +
                "      \"properties\": {\n" +
                "        \"errorCode\": {\n" +
                "          \"type\": \"string\",\n" +
                "          \"description\": \"Error Code\",\n" +
                "          \"example\": \"20015\"\n" +
                "        },\n" +
                "        \"errorMessage\": {\n" +
                "          \"type\": \"string\",\n" +
                "          \"description\": \"Error Text\",\n" +
                "          \"example\": \"Example error text\"\n" +
                "        }\n" +
                "      }\n" +
                "    }\n" +
                "  }\n" +
                "}\n";
        Assert.assertEquals(expectedSwaggerJson, payload);
    }

    @Test(groups = "wso2.esb", description = "Retrieve custom swagger with special characters in json format")
    public void testCustomSwaggerWithSpecialCharactersJSON() throws Exception {

        String restURL = getMainSequenceURL() + "CustomSwaggerRus?swagger.json";

        HttpResponse response = httpClient.doGet(restURL, null);
        String payload = httpClient.getResponsePayload(response);

        String expectedSwaggerJson = "{\n" +
                "  \"swagger\": \"2.0\",\n" +
                "  \"info\": {\n" +
                "    \"version\": \"1.0.0\",\n" +
                "    \"title\": \"Test Project\",\n" +
                "    \"description\": \"Test Project\"\n" +
                "  },\n" +
                "  \"host\": \"localhost:8290\",\n" +
                "  \"basePath\": \"/services\",\n" +
                "  \"schemes\": [\n" +
                "    \"http\"\n" +
                "  ],\n" +
                "  \"tags\": [\n" +
                "    {\n" +
                "      \"name\": \"Test\",\n" +
                "      \"description\": \"Test\"\n" +
                "    }\n" +
                "  ],\n" +
                "  \"paths\": {\n" +
                "    \"/test\": {\n" +
                "      \"post\": {\n" +
                "        \"summary\": \"test\",\n" +
                "        \"tags\": [\n" +
                "          \"Test\"\n" +
                "        ],\n" +
                "        \"consumes\": [\n" +
                "          \"application/json; charset=utf-8\"\n" +
                "        ],\n" +
                "        \"produces\": [\n" +
                "          \"application/json; charset=utf-8\"\n" +
                "        ],\n" +
                "        \"parameters\": [\n" +
                "          {\n" +
                "            \"in\": \"body\",\n" +
                "            \"name\": \"request\",\n" +
                "            \"schema\": {\n" +
                "              \"$ref\": \"#/definitions/TestType\"\n" +
                "            }\n" +
                "          }\n" +
                "        ],\n" +
                "        \"responses\": {\n" +
                "          \"200\": {\n" +
                "            \"description\": \"Запрос принят\",\n" +
                "            \"schema\": {\n" +
                "              \"type\": \"object\",\n" +
                "              \"properties\": {\n" +
                "                \"status\": {\n" +
                "                  \"type\": \"string\",\n" +
                "                  \"description\": \"Статус выполнения запроса\",\n" +
                "                  \"enum\": [\n" +
                "                    \"ACCEPTED\"\n" +
                "                  ],\n" +
                "                  \"example\": \"ACCEPTED\"\n" +
                "                }\n" +
                "              }\n" +
                "            }\n" +
                "          },\n" +
                "          \"400\": {\n" +
                "            \"description\": \"Некорректный запрос\",\n" +
                "            \"schema\": {\n" +
                "              \"$ref\": \"#/definitions/ErrorType\"\n" +
                "            }\n" +
                "          },\n" +
                "          \"500\": {\n" +
                "            \"description\": \"Системная ошибка\",\n" +
                "            \"schema\": {\n" +
                "              \"$ref\": \"#/definitions/ErrorType\"\n" +
                "            }\n" +
                "          }\n" +
                "        }\n" +
                "      }\n" +
                "    }\n" +
                "  },\n" +
                "  \"definitions\": {\n" +
                "    \"TestType\": {\n" +
                "      \"type\": \"object\",\n" +
                "      \"required\": [\n" +
                "        \"msgId\"\n" +
                "      ],\n" +
                "      \"properties\": {\n" +
                "        \"msgId\": {\n" +
                "          \"type\": \"string\",\n" +
                "          \"description\": \"UUID\",\n" +
                "          \"example\": \"123456\"\n" +
                "        }\n" +
                "      }\n" +
                "    },\n" +
                "    \"ErrorType\": {\n" +
                "      \"type\": \"object\",\n" +
                "      \"required\": [\n" +
                "        \"errorCode\",\n" +
                "        \"errorMessage\"\n" +
                "      ],\n" +
                "      \"properties\": {\n" +
                "        \"errorCode\": {\n" +
                "          \"type\": \"string\",\n" +
                "          \"description\": \"Код ошибки\",\n" +
                "          \"example\": \"20015\"\n" +
                "        },\n" +
                "        \"errorMessage\": {\n" +
                "          \"type\": \"string\",\n" +
                "          \"description\": \"Текст ошибки\",\n" +
                "          \"example\": \"Ошибка запроса\"\n" +
                "        }\n" +
                "      }\n" +
                "    }\n" +
                "  }\n" +
                "}\n";
        Assert.assertEquals(expectedSwaggerJson, payload);
    }

    @Test(groups = "wso2.esb", description = "Retrieve custom swagger without special characters in yaml format")
    public void testCustomSwaggerYAML() throws Exception {

        String restURL = getMainSequenceURL() + "CustomSwagger?swagger.yaml";

        HttpResponse response = httpClient.doGet(restURL, null);
        String payload = httpClient.getResponsePayload(response);

        String expectedSwaggerYaml = "swagger: '2.0'\n" +
                "info:\n" +
                "  version: 1.0.0\n" +
                "  title: TestProject\n" +
                "  description: Test Project\n" +
                "host: localhost:8290\n" +
                "basePath: /services\n" +
                "schemes:\n" +
                "- http\n" +
                "tags:\n" +
                "- name: Test\n" +
                "  description: Test\n" +
                "paths:\n" +
                "  /test:\n" +
                "    post:\n" +
                "      summary: test\n" +
                "      tags:\n" +
                "      - Test\n" +
                "      consumes:\n" +
                "      - application/json; charset=utf-8\n" +
                "      produces:\n" +
                "      - application/json; charset=utf-8\n" +
                "      parameters:\n" +
                "      - in: body\n" +
                "        name: request\n" +
                "        schema:\n" +
                "          $ref: '#/definitions/TestType'\n" +
                "      responses:\n" +
                "        '200':\n" +
                "          description: Request accepted\n" +
                "          schema:\n" +
                "            type: object\n" +
                "            properties:\n" +
                "              status:\n" +
                "                type: string\n" +
                "                description: Status\n" +
                "                enum:\n" +
                "                - ACCEPTED\n" +
                "                example: ACCEPTED\n" +
                "        '400':\n" +
                "          description: Bad Request\n" +
                "          schema:\n" +
                "            $ref: '#/definitions/ErrorType'\n" +
                "        '500':\n" +
                "          description: System Error\n" +
                "          schema:\n" +
                "            $ref: '#/definitions/ErrorType'\n" +
                "definitions:\n" +
                "  TestType:\n" +
                "    type: object\n" +
                "    required:\n" +
                "    - msgId\n" +
                "    properties:\n" +
                "      msgId:\n" +
                "        type: string\n" +
                "        description: UUID\n" +
                "        example: '123456'\n" +
                "  ErrorType:\n" +
                "    type: object\n" +
                "    required:\n" +
                "    - errorCode\n" +
                "    - errorMessage\n" +
                "    properties:\n" +
                "      errorCode:\n" +
                "        type: string\n" +
                "        description: Error Code\n" +
                "        example: '20015'\n" +
                "      errorMessage:\n" +
                "        type: string\n" +
                "        description: Error Text\n" +
                "        example: Example error text\n";

        Assert.assertEquals(expectedSwaggerYaml, payload);
    }

    @Test(groups = "wso2.esb", description = "Retrieve custom swagger with special characters in yaml format")
    public void testCustomSwaggerWithSpecialCharactersYAML() throws Exception {

        String restURL = getMainSequenceURL() + "CustomSwaggerRus?swagger.yaml";

        HttpResponse response = httpClient.doGet(restURL, null);
        String payload = httpClient.getResponsePayload(response);

        String expectedSwaggerYaml = "swagger: '2.0'\n" +
                "info:\n" +
                "  version: 1.0.0\n" +
                "  title: Test Project\n" +
                "  description: Test Project\n" +
                "host: localhost:8290\n" +
                "basePath: /services\n" +
                "schemes:\n" +
                "- http\n" +
                "tags:\n" +
                "- name: Test\n" +
                "  description: Test\n" +
                "paths:\n" +
                "  /test:\n" +
                "    post:\n" +
                "      summary: test\n" +
                "      tags:\n" +
                "      - Test\n" +
                "      consumes:\n" +
                "      - application/json; charset=utf-8\n" +
                "      produces:\n" +
                "      - application/json; charset=utf-8\n" +
                "      parameters:\n" +
                "      - in: body\n" +
                "        name: request\n" +
                "        schema:\n" +
                "          $ref: '#/definitions/TestType'\n" +
                "      responses:\n" +
                "        '200':\n" +
                "          description: Запрос принят\n" +
                "          schema:\n" +
                "            type: object\n" +
                "            properties:\n" +
                "              status:\n" +
                "                type: string\n" +
                "                description: Статус выполнения запроса\n" +
                "                enum:\n" +
                "                - ACCEPTED\n" +
                "                example: ACCEPTED\n" +
                "        '400':\n" +
                "          description: Некорректный запрос\n" +
                "          schema:\n" +
                "            $ref: '#/definitions/ErrorType'\n" +
                "        '500':\n" +
                "          description: Системная ошибка\n" +
                "          schema:\n" +
                "            $ref: '#/definitions/ErrorType'\n" +
                "definitions:\n" +
                "  TestType:\n" +
                "    type: object\n" +
                "    required:\n" +
                "    - msgId\n" +
                "    properties:\n" +
                "      msgId:\n" +
                "        type: string\n" +
                "        description: UUID\n" +
                "        example: '123456'\n" +
                "  ErrorType:\n" +
                "    type: object\n" +
                "    required:\n" +
                "    - errorCode\n" +
                "    - errorMessage\n" +
                "    properties:\n" +
                "      errorCode:\n" +
                "        type: string\n" +
                "        description: Код ошибки\n" +
                "        example: '20015'\n" +
                "      errorMessage:\n" +
                "        type: string\n" +
                "        description: Текст ошибки\n" +
                "        example: Ошибка запроса\n";

        Assert.assertEquals(expectedSwaggerYaml, payload);
    }

    @AfterClass(alwaysRun = true)
    public void destroy() throws Exception {

        super.cleanup();
    }

}
