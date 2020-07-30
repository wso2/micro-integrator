/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.micro.integrator.transaction;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.wso2.esb.integration.common.utils.clients.SimpleHttpClient;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Transaction Counter test Utils
 */
public class TransactionCounterUtils {
    private final static JsonParser jsonParser = new JsonParser();
    private final static int POLL_TIMEOUT = 150;

    public static boolean isTransactionCountUpdated(String transactionCountResource, int expectedRequestCount)
            throws InterruptedException, IOException {
        for (int i = 0; i < POLL_TIMEOUT; i+=2) {
            if (expectedRequestCount == getTransactionCount(transactionCountResource)) {
                return true;
            }
            TimeUnit.SECONDS.sleep(2);
        }
        return false;
    }

    public static int getTransactionCount(String transactionCountUrl) throws IOException {
        SimpleHttpClient client = new SimpleHttpClient();
        Map<String, String> header = new HashMap<String, String>();
        header.put("Content-Type", "application/json");
        String response = client.getResponsePayload(client.doGet(transactionCountUrl, header));
        JsonObject transactionResourceResponse = jsonParser.parse(response).getAsJsonObject();
        if (transactionResourceResponse.get("error") != null) {
            return -1;
        }
        return transactionResourceResponse.get("TransactionCount").getAsInt();
    }
}
