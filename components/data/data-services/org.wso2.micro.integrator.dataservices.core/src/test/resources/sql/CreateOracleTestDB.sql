START DropOracleUser;

CREATE USER datauser IDENTIFIED BY wso2;

GRANT CONNECT,RESOURCE to datauser with admin option;

CONNECT datauser/wso2;
alter session set NLS_DATE_FORMAT='YYYY-MM-DD';

SET ESCAPE ON
SET ESCAPE '#'


START CreateOracleTables;
START Offices;
START Employees;
START Customers;
START Products;
START Orders;
START Payments;
START ProductLines;
START OrderDetails;
START OracleStoredProcs;
START OracleStoredFuncs;



exit;



