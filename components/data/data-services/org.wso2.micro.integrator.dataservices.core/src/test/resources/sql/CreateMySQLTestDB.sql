DROP DATABASE IF EXISTS DSTestDB;
create database DSTestDB;
grant all on DSTestDB.* to 'datauser'@'localhost' identified by 'wso2';
grant select on mysql.proc to 'datauser'@'localhost';
grant all on DSTestDB.* to 'datauser'@'%' identified by 'wso2';
grant select on mysql.proc to 'datauser'@'%';

use DSTestDB;

source CreateTables.sql;
source Offices.sql;
source Employees.sql;
source Customers.sql;
source Products.sql;
source Orders.sql;
source Payments.sql;
source ProductLines.sql;
source OrderDetails.sql;
source MySQLStoredProcs.sql;
source MySQLStoredFuncs.sql;

