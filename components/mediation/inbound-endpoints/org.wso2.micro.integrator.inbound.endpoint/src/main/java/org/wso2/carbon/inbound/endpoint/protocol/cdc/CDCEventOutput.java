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

import io.debezium.engine.ChangeEvent;
import org.json.JSONObject;

import static org.wso2.carbon.inbound.endpoint.protocol.cdc.InboundCDCConstants.AFTER;
import static org.wso2.carbon.inbound.endpoint.protocol.cdc.InboundCDCConstants.BEFORE;
import static org.wso2.carbon.inbound.endpoint.protocol.cdc.InboundCDCConstants.DB;
import static org.wso2.carbon.inbound.endpoint.protocol.cdc.InboundCDCConstants.OP;
import static org.wso2.carbon.inbound.endpoint.protocol.cdc.InboundCDCConstants.PAYLOAD;
import static org.wso2.carbon.inbound.endpoint.protocol.cdc.InboundCDCConstants.SOURCE;
import static org.wso2.carbon.inbound.endpoint.protocol.cdc.InboundCDCConstants.TABLE;
import static org.wso2.carbon.inbound.endpoint.protocol.cdc.InboundCDCConstants.TS_MS;

public class CDCEventOutput {

    private JSONObject payload;

    private enum operations {c, r, u, d};

    CDCEventOutput(ChangeEvent event) {
        String valueString = event.value().toString();
        JSONObject value = new JSONObject(valueString);
        this.payload = value.getJSONObject(PAYLOAD);
    }

    public Object getJsonPayloadBeforeEvent() {
        Object beforeObject = null;
        if (payload.has(BEFORE)) {
            beforeObject = payload.get(BEFORE);
        }
        return beforeObject;

    }

    public Object getJsonPayloadAfterEvent() {
        Object afterObject = null;
        if (payload.has(AFTER)) {
            afterObject = payload.get(AFTER);
        }
        return afterObject;
    }

    public Long getTs_ms() {
        if (payload.has(TS_MS)) {
            return payload.getLong(TS_MS);
        }
        return null;
    }

    public String getDatabase() {
        if (getSource() != null) {
            if (getSource().has(DB)) {
                return getSource().getString(DB);
            }
            return null;
        }
        return null;
    }

    public Object getTable() {
        Object tableObject = null;
        if (getSource() != null) {
            if (getSource().has(TABLE)) {
                tableObject = getSource().get(TABLE);
            }
        }
        return tableObject;
    }

    private JSONObject getSource () {
        if (payload.has(SOURCE)) {
            return payload.getJSONObject(SOURCE);
        }
        return null;
    }

    public String getOp() {
        if (payload.has(OP)) {
            return getOpString(payload.getString(OP));
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

    public JSONObject getOutputJsonPayload () {
        if (payload == null) {
            return null;
        }
        JSONObject jsonPayload = new JSONObject();
        jsonPayload.put(OP, getOp());
        jsonPayload.put(BEFORE, getJsonPayloadBeforeEvent());
        jsonPayload.put(AFTER, getJsonPayloadAfterEvent());
        return jsonPayload;
    }
}
