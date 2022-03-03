### How to run tests ###

## Primary user store tests

1.  Start the sql server locally.

2.  Update the datasource details in the file
    {MI_Repo}/integration/mediation-tests/tests-platform/tests-userstore/src/test/resources/artifacts/ESB/server/conf
    /deployment.toml

3. Do the following w.r.t to your database

- MySQL , MS SQL , Postgres : Do Nothing, mentioned db will be dropped and created in tests.
- DB2 : Create the database defined in the step2. Tables and sequences will be dropped and
  created if they exist.
- Oracle : Create the user defined in step 2 with necessary permissions. Tables and sequences will be dropped and
  created if they exist.

4 Execute the following command from the root directory.
```bash
mvn clean install -P db-tests
```

## Secondary user store tests (MySql only)

1. Start the MySql server locally.
2. Update the datasource details in the file
    {MI_Repo}/integration/mediation-tests/tests-platform/tests-userstore/src/test/resources/artifacts/ESB/server/conf
    /deployment.toml
3. Update the secondary user store xml file with sql server details
    {MI_Repo}/integration/mediation-tests/tests-platform/tests-userstore/src/test/resources/artifacts/ESB/server/
    repository/deployment/server/userstores/wso2_com.xml
4. Un-comment SecondaryUserManagementTests from testng.xml file.
5. Un-comment `<parameter name="create-secondary" value="true"/>` in automation.xml file.
6. Execute the following command from the root directory.
```bash
mvn clean install -P db-tests
```