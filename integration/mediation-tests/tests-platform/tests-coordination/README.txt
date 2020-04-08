1) Start a mysql server locally ( No need to create any data base ).
2) Update the datasource details in
<micro-integrator-repo>/integration/mediation-tests/tests-platform/tests-coordination/src/test/resources/artifacts/ESB/server/conf/deployment.toml
file ( you may define any name for database and it will be created in the tests if it doesn't exist or will be
dropped and created if it exists to make sure that we source the updated db script )
3) Execute the command "mvn clean install -P cluster-tests"
