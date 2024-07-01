package org.wso2.micro.integrator.initializer.state.monitor;


import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class DatabaseQueryManager {

    public enum DBTypes {
        MYSQL,
        POSTGRESQL,
        MICROSOFTSQLSERVER,
        ORACLE,
        DB2
    }
    private DBTypes dbTypes;

    public enum DBQueries {
        GET_ARTIFACT_STATUS_TABLE_FOR_GIVEN_NODE,
        GET_ARTIFACT_STATUS_TABLE_FOR_ALL_NODES
    }

    private final Map<DatabaseQueryManager.DBQueries, String> mysqlQueryMap =
            Collections.unmodifiableMap(new HashMap<DatabaseQueryManager.DBQueries, String>() {{
                put(DBQueries.GET_ARTIFACT_STATUS_TABLE_FOR_GIVEN_NODE, "SELECT * FROM ARTIFACT_STATUS_TABLE WHERE NODE_ID = ?");
                put(DBQueries.GET_ARTIFACT_STATUS_TABLE_FOR_ALL_NODES, "SELECT * FROM ARTIFACT_STATUS_TABLE WHERE NODE_ID = '__ALL_NODES__'");
            }});

    private final Map<DatabaseQueryManager.DBQueries, String> postgreSQLQueryMap =
            Collections.unmodifiableMap(new HashMap<DatabaseQueryManager.DBQueries, String>() {{
                put(DBQueries.GET_ARTIFACT_STATUS_TABLE_FOR_GIVEN_NODE, "SELECT * FROM ARTIFACT_STATUS_TABLE WHERE NODE_ID = ?");
                put(DBQueries.GET_ARTIFACT_STATUS_TABLE_FOR_ALL_NODES, "SELECT * FROM ARTIFACT_STATUS_TABLE WHERE NODE_ID = '__ALL_NODES__'");
            }});

    private final Map<DatabaseQueryManager.DBQueries, String> oracleQueryMap =
            Collections.unmodifiableMap(new HashMap<DatabaseQueryManager.DBQueries, String>() {{
                put(DBQueries.GET_ARTIFACT_STATUS_TABLE_FOR_GIVEN_NODE, "SELECT * FROM ARTIFACT_STATUS_TABLE WHERE NODE_ID = ?");
                put(DBQueries.GET_ARTIFACT_STATUS_TABLE_FOR_ALL_NODES, "SELECT * FROM ARTIFACT_STATUS_TABLE WHERE NODE_ID = '__ALL_NODES__'");
            }});
    private final Map<DatabaseQueryManager.DBQueries, String> db2QueryMap =
            Collections.unmodifiableMap(new HashMap<DatabaseQueryManager.DBQueries, String>() {{
                put(DBQueries.GET_ARTIFACT_STATUS_TABLE_FOR_GIVEN_NODE, "SELECT * FROM ARTIFACT_STATUS_TABLE WHERE NODE_ID = ?");
                put(DBQueries.GET_ARTIFACT_STATUS_TABLE_FOR_ALL_NODES, "SELECT * FROM ARTIFACT_STATUS_TABLE WHERE NODE_ID = '__ALL_NODES__'");
            }});
    private final Map<DatabaseQueryManager.DBQueries, String> microsoftSQLServerQueryMap =
            Collections.unmodifiableMap(new HashMap<DatabaseQueryManager.DBQueries, String>() {{
                put(DBQueries.GET_ARTIFACT_STATUS_TABLE_FOR_GIVEN_NODE, "SELECT * FROM ARTIFACT_STATUS_TABLE WHERE NODE_ID = ?");
                put(DBQueries.GET_ARTIFACT_STATUS_TABLE_FOR_ALL_NODES, "SELECT * FROM ARTIFACT_STATUS_TABLE WHERE NODE_ID = '__ALL_NODES__'");
            }});

    public DatabaseQueryManager(String databaseType) {
        if ("MySQL".equals(databaseType)) {
            this.dbTypes = DBTypes.MYSQL;
        } else if ("PostgreSQL".equals(databaseType)) {
            this.dbTypes = DBTypes.POSTGRESQL;
        } else if ("Oracle".equals(databaseType)) {
            this.dbTypes = DBTypes.ORACLE;
        } else if (databaseType.toLowerCase(Locale.ENGLISH).contains("DB2".toLowerCase(Locale.ENGLISH))) {
            this.dbTypes = DBTypes.DB2;
        } else {
            this.dbTypes = DBTypes.MICROSOFTSQLSERVER;
        }
    }

    public String getQuery(DBQueries dbQueries) {
        switch (this.dbTypes) {
            case MYSQL: return mysqlQueryMap.get(dbQueries);
            case POSTGRESQL: return postgreSQLQueryMap.get(dbQueries);
            case ORACLE: return oracleQueryMap.get(dbQueries);
            case DB2: return db2QueryMap.get(dbQueries);
            default: return microsoftSQLServerQueryMap.get(dbQueries);
        }
    }
}
