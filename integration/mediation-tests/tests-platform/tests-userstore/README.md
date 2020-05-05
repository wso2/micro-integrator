### How to run tests ###
1.  Start the sql server locally.

2.  Create a data base with respective <db>_user.sql scripts ( Not needed for mysql as it will be created in tests).

3.  Update the datasource details in following file
    {MI_Repo}/integration/mediation-tests/tests-platform/tests-userstore/src/test/resources/artifacts/ESB/server/conf
    /deployment.toml  

4.  Execute the following command from the root directory.
    ```bash
    mvn clean install -P platform-tests
    ```
