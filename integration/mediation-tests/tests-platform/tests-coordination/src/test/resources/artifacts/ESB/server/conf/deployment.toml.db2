[server]
hostname = "localhost"
hot_deployment = "true"

[user_store]
type = "read_only_ldap"

[keystore.primary]
file_name = "repository/resources/security/wso2carbon.jks"
password = "wso2carbon"
alias = "wso2carbon"
key_password = "wso2carbon"

[truststore]
file_name = "repository/resources/security/client-truststore.jks"
password = "wso2carbon"
alias = "symmetric.key.value"
algorithm = "AES"

[management_api.jwt_token_security_handler]
enable = false

## DB2 v11.5.0.0
[[datasource]]
id = "WSO2_COORDINATION_DB"
url = "jdbc:db2://localhost:50000/testdb"
username = "db2inst1"
password = "Niro"
driver = "com.ibm.db2.jcc.DB2Driver"
