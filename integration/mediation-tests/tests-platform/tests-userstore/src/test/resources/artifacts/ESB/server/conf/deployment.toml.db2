[server]
hostname = "localhost"
hot_deployment = "true"

[user_store]
type = "database"
read_only = "false"

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

[management_api_token_handler]
enable = false

[management_api.jwt_token_security_handler]
enable = false

[management_api.authorization_handler]
enable = false

[[datasource]]
id = "WSO2CarbonDB"
url = "jdbc:db2://localhost:50000/testdb"
username = "db2inst1"
password = "Niro"
driver = "com.ibm.db2.jcc.DB2Driver"

[internal_apis.file_user_store]
enable = false
