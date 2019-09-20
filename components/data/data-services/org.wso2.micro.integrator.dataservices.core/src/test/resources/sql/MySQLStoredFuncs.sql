DELIMITER $$

DROP FUNCTION IF EXISTS getAverageCreditLimit $$
CREATE FUNCTION getAverageCreditLimit() RETURNS DOUBLE
BEGIN
    DECLARE totalCL,average DOUBLE;
    DECLARE noOfCustomers INT;
    SELECT SUM(creditLimit) into totalCL from Customers;
    SELECT COUNT(*) into noOfCustomers from Customers;
    SET average = totalCL/noOfCustomers;
    RETURN average;
END $$

DROP FUNCTION IF EXISTS getCustomerPhoneNumber $$
CREATE FUNCTION getCustomerPhoneNumber(custNo INTEGER) RETURNS varchar(50)
BEGIN
    DECLARE phoneNo varchar(50);
    SELECT phone into phoneNo from Customers WHERE customerNumber = custNo;
    RETURN phoneNo;
END $$

DELIMITER ;
