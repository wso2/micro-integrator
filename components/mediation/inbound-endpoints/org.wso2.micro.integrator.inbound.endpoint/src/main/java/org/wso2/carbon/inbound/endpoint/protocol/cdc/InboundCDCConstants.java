package org.wso2.carbon.inbound.endpoint.protocol.cdc;

public class InboundCDCConstants {

    /** Inbound Endpoint Parameters **/
    public static final String CDC = "cdc";
    public static final String INBOUND_ENDPOINT_PARAMETER_CDC_PORT = "inbound.cdc.port";

    //debezium
    public static final String DEBEZIUM_NAME = "name";
    public static final String DEBEZIUM_SNAPSHOT_MODE = "snapshot.mode";
    public static final String DEBEZIUM_MAX_THREADS = "snapshot.max.threads";
    public static final String DEBEZIUM_OFFSET_STORAGE = "offset.storage";
    public static final String DEBEZIUM_OFFSET_STORAGE_FILE_FILENAME = "offset.storage.file.filename";
    public static final String DEBEZIUM_OFFSET_FLUSH_INTERVAL_MS = "offset.flush.interval.ms";
    public static final String DEBEZIUM_CONNECTOR_CLASS = "connector.class";
    public static final String DEBEZIUM_DATABASE_HOSTNAME = "database.hostname";
    public static final String DEBEZIUM_DATABASE_PORT = "database.port";
    public static final String DEBEZIUM_DATABASE_USER = "database.user";
    public static final String DEBEZIUM_DATABASE_PASSWORD = "database.password";
    public static final String DEBEZIUM_DATABASE_DBNAME = "database.dbname";
    public static final String DEBEZIUM_TABLES_INCLUDE_LIST = "table.include.list";
    public static final String DEBEZIUM_OPERATIONS_EXCLUDE_LIST = "skipped.operations";
    public static final String DEBEZIUM_DATABASE_SERVER_ID = "database.server.id";
    public static final String DEBEZIUM_DATABASE_ALLOW_PUBLIC_KEY_RETRIEVAL = "database.allowPublicKeyRetrieval";
    public static final String DEBEZIUM_TOPIC_PREFIX = "topic.prefix";

    public static final String DEBEZIUM_VALUE_CONVERTER = "value.converter";
    public static final String DEBEZIUM_KEY_CONVERTER = "key.converter";
    public static final String DEBEZIUM_VALUE_CONVERTER_SCHEMAS_ENABLE = "value.converter.schemas.enable";
    public static final String DEBEZIUM_KEY_CONVERTER_SCHEMAS_ENABLE = "key.converter.schemas.enable";

    public static final String DEBEZIUM_SCHEMA_HISTORY_INTERNAL = "schema.history.internal";
    public static final String DEBEZIUM_SCHEMA_HISTORY_INTERNAL_FILE_FILENAME = "schema.history.internal.file.filename";

    /** Output Properties **/
    public static final String DATABASE_NAME = "database";
    public static final String TABLES ="tables";
    public static final String OPERATIONS ="operations";
    public static final String TS_MS = "ts_ms";

    public static final String BEFORE = "before";
    public static final String AFTER = "after";
    public static final String SOURCE = "source";
    public static final String OP = "op";
    public static final String PAYLOAD = "payload";
    public static final String DB = "db";
    public static final String TABLE = "table";

    public static final String TRUE = "true";
}
