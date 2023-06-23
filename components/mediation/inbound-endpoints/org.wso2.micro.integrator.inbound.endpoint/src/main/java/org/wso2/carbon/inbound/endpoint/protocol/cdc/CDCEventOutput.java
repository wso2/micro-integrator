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

    private Object beforeEvent;
    private Object afterEvent;
    private Long ts_ms;

    private String database;
    private Object table;
    private String op;
    private JSONObject payload;

    private enum operations {c, r, u, d};

    CDCEventOutput (ChangeEvent event) {
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

    public void setJsonPayloadBeforeEvent(Object beforeEvent) {
        this.beforeEvent = beforeEvent;
    }

    public Object getJsonPayloadAfterEvent() {
        Object afterObject = null;
        if (payload.has(AFTER)) {
            afterObject = payload.get(AFTER);
        }
        return afterObject;
    }

    public void setJsonPayloadAfterEvent(Object afterEvent) {
        this.afterEvent = afterEvent;
    }

    public Long getTs_ms() {
        if (payload.has(TS_MS)) {
            return payload.getLong(TS_MS);
        }
        return null;
    }

    public void setTs_ms(Long ts_ms) {
        this.ts_ms = ts_ms;
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

    public void setDatabase(String database) {
        this.database = database;
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

    public void setTable(Object table) {
        this.table = table;
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

    public void setOp(String op) {
        this.op = op;
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
