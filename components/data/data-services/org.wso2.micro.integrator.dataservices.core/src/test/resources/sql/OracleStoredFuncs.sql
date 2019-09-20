CREATE OR REPLACE FUNCTION getAverageCreditLimit
RETURN BINARY_DOUBLE IS

totalCL Customers.creditLimit%TYPE;
average Customers.creditLimit%TYPE;
noOfCustomers NUMBER;

BEGIN
	SELECT SUM(creditLimit) into totalCL from Customers;
	SELECT COUNT(*) into noOfCustomers from Customers;
    average := totalCL/noOfCustomers;
    RETURN average;
END;

/

CREATE OR REPLACE FUNCTION getCustomerPhoneNumber(custNo NUMBER)
RETURN varchar2 IS

phoneNo Customers.phone%TYPE;


BEGIN
	SELECT phone into phoneNo from Customers WHERE customerNumber = custNo;
    RETURN phoneNo;
END;

/