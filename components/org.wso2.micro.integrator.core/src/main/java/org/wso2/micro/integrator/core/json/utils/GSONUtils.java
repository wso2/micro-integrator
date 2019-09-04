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

package org.wso2.micro.integrator.core.json.utils;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * Utility class containing utility functions to manipulated GSON objects
 */
public class GSONUtils {

    private static final Log log = LogFactory.getLog(GSONUtils.class);

    /**
     * Function to convert from GSON object model to generic map model.
     *
     * @param jsonElement gson object to transform
     * @return
     */
    public static Map gsonJsonObjectToMap(JsonElement jsonElement) {
        Map<String, Object> gsonMap = new LinkedHashMap<>();

        if (jsonElement.isJsonObject()) {
            JsonObject jsonObject = jsonElement.getAsJsonObject();
            Set<Map.Entry<String, JsonElement>> entrySet = jsonObject.entrySet();

            for (Map.Entry<String, JsonElement> entry : entrySet) {
                if (entry.getValue().isJsonPrimitive()) {
                    JsonPrimitive jsonPrimitive = entry.getValue().getAsJsonPrimitive();

                    if (jsonPrimitive.isString()) {
                        gsonMap.put(entry.getKey(), jsonPrimitive.getAsString());
                    } else if (jsonPrimitive.isBoolean()) {
                        gsonMap.put(entry.getKey(), jsonPrimitive.getAsBoolean());
                    } else if (jsonPrimitive.isNumber()) {
                        gsonMap.put(entry.getKey(), jsonPrimitive.getAsNumber());
                    } else {
                        //unknown type
                        log.warn("Unknown JsonPrimitive type found : " + jsonPrimitive.toString());
                    }
                } else if (entry.getValue().isJsonObject()) {
                    gsonMap.put(entry.getKey(), gsonJsonObjectToMap(entry.getValue()));

                } else if (entry.getValue().isJsonArray()) {
                    gsonMap.put(entry.getKey(), gsonJsonArrayToObjectArray(entry.getValue().getAsJsonArray()));

                } else {
                    // remaining JsonNull type
                    gsonMap.put(entry.getKey(), null);

                }
            }
        } else {
            //Not a JsonObject hence return empty map
            log.error("Provided gson model does not represent json object");
        }

        return gsonMap;
    }

    /**
     * Function to convert GSON array model to generic array model.
     *
     * @param jsonElement gson array to transform
     * @return
     */
    public static Object[] gsonJsonArrayToObjectArray(JsonElement jsonElement) {
        ArrayList<Object> jsonArrayList = new ArrayList<>();

        if (jsonElement.isJsonArray()) {
            JsonArray jsonArray = jsonElement.getAsJsonArray();

            for (int i = 0; i < jsonArray.size(); i++) {
                JsonElement arrayElement = jsonArray.get(i);
                if (arrayElement.isJsonPrimitive()) {
                    JsonPrimitive jsonPrimitive = arrayElement.getAsJsonPrimitive();

                    if (jsonPrimitive.isString()) {
                        jsonArrayList.add(jsonPrimitive.getAsString());
                    } else if (jsonPrimitive.isBoolean()) {
                        jsonArrayList.add(jsonPrimitive.getAsBoolean());
                    } else if (jsonPrimitive.isNumber()) {
                        jsonArrayList.add(jsonPrimitive.getAsNumber());
                    } else {
                        //unknown type
                        log.warn("Unknown JsonPrimitive type found : " + jsonPrimitive.toString());
                    }
                } else if (arrayElement.isJsonObject()) {
                    jsonArrayList.add(gsonJsonObjectToMap(arrayElement));

                } else if (arrayElement.isJsonArray()) {
                    jsonArrayList.add(gsonJsonArrayToObjectArray(arrayElement.getAsJsonArray()));
                } else {
                    // remaining JsonNull
                    jsonArrayList.add(null);
                }
            }
        } else {
            //Not a JsonObject hence return empty array
            log.error("Provided gson model does not represent json array");
        }

        return jsonArrayList.toArray();
    }
}
