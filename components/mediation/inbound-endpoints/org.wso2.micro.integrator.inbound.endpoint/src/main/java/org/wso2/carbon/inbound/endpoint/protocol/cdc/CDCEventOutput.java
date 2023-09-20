/*
 * Copyright (c) 2023, WSO2 LLC. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.inbound.endpoint.protocol.cdc;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.debezium.engine.ChangeEvent;

import static org.wso2.carbon.inbound.endpoint.protocol.cdc.InboundCDCConstants.AFTER;
import static org.wso2.carbon.inbound.endpoint.protocol.cdc.InboundCDCConstants.BEFORE;
import static org.wso2.carbon.inbound.endpoint.protocol.cdc.InboundCDCConstants.DB;
import static org.wso2.carbon.inbound.endpoint.protocol.cdc.InboundCDCConstants.OP;
import static org.wso2.carbon.inbound.endpoint.protocol.cdc.InboundCDCConstants.PAYLOAD;
import static org.wso2.carbon.inbound.endpoint.protocol.cdc.InboundCDCConstants.SOURCE;
import static org.wso2.carbon.inbound.endpoint.protocol.cdc.InboundCDCConstants.TABLE;
import static org.wso2.carbon.inbound.endpoint.protocol.cdc.InboundCDCConstants.TS_MS;

public class CDCEventOutput {

    private JsonObject payload;

    private enum operations {c, r, u, d};

    CDCEventOutput(ChangeEvent event) {
        String valueString = event.value().toString();
        JsonObject value = new Gson().fromJson(valueString, JsonObject.class);
        this.payload = value.getAsJsonObject(PAYLOAD);
    }

    public JsonElement getJsonPayloadBeforeEvent() {
        return payload.get(BEFORE);
    }

    public JsonElement getJsonPayloadAfterEvent() {
        return payload.get(AFTER);
    }

    public Long getTs_ms() {
        if (payload.has(TS_MS)) {
            return payload.get(TS_MS).getAsLong();
        }
        return null;
    }

    public String getDatabase() {
        if (getSource() != null && getSource().has(DB)) {
                return getSource().get(DB).getAsString();
        }
        return null;
    }

    public JsonElement getTable() {
        JsonElement tableObject = null;
        if (getSource() != null && getSource().has(TABLE)) {
            tableObject = getSource().get(TABLE);
        }
        return tableObject;
    }

    private JsonObject getSource () {
        if (payload.has(SOURCE)) {
            return payload.getAsJsonObject(SOURCE);
        }
        return null;
    }

    public String getOp() {
        if (payload.has(OP)) {
            return getOpString(payload.get(OP).getAsString());
        }
        return null;
    }

    private String getOpString(String op) {
        if (op != null) {
            switch (operations.valueOf(op)) {
                case c:
                    return "CREATE";
                case r:
                    return "READ";
                case u:
                    return "UPDATE";
                case d:
                    return "DELETE";
            }
        }
        return null;
    }

    public JsonObject getOutputJsonPayload () {
        if (payload == null) {
            return null;
        }
        JsonObject jsonPayload = new JsonObject();
        jsonPayload.addProperty(OP, getOp());
        jsonPayload.add(BEFORE, getJsonPayloadBeforeEvent());
        jsonPayload.add(AFTER, getJsonPayloadAfterEvent());
        return jsonPayload;
    }
}
