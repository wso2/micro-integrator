How to enable data service tests?
=================================
1.pom.xml
- Goto maven-surefire-plugin declaration.
- Change <skip>true</skip> to <skip>false</skip>

- Drop the JDBC driver jar (mysql-connector-java-5.1.5-bin.jar in this case) to src/test/resources/lib folder
- Uncomment & change the <!-- JDBC Driver classes --> section


2.Create MySQL database using supplied script
- MySQL - Run /sql/CreateMySQLTestDB.sql file with root privileges.