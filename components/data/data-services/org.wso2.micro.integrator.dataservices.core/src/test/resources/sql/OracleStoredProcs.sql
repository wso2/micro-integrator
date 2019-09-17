CREATE OR REPLACE PACKAGE Types AS 
  TYPE cursor_type IS REF CURSOR;
END Types; 
/

-------

CREATE OR REPLACE PROCEDURE getCustomerInfo(p_recordset1 OUT  Types.cursor_type) AS

BEGIN 
	OPEN p_recordset1 FOR
	SELECT customerNumber, customerName, contactLastName, phone, city FROM Customers;
	
END;

/

-------

CREATE OR REPLACE PROCEDURE getCustomerInfoWithId(custNo IN NUMBER, p_recordset1 OUT  Types.cursor_type) AS

BEGIN 
	OPEN p_recordset1 FOR
	SELECT customerNumber, customerName, contactLastName, phone, city FROM Customers WHERE customerNumber = custNo;
	
END;
/

-------

CREATE OR REPLACE PROCEDURE getCustomerInfoWithIdLastName(custNo IN NUMBER, custLastName IN VARCHAR2, p_recordset1 OUT  Types.cursor_type) AS

BEGIN 
	OPEN p_recordset1 FOR
	SELECT customerNumber, customerName, contactLastName, phone, city  FROM Customers WHERE customerNumber = custNo and contactLastName = custLastName;
	
END;
/

-------

CREATE OR REPLACE PROCEDURE getCustomerCreditLimitWithId(custNo IN NUMBER, p_recordset1 OUT  Types.cursor_type) AS

BEGIN
	OPEN p_recordset1 FOR
	SELECT *  FROM Customers WHERE customerNumber=custNo;
	
END;
/

-------

CREATE OR REPLACE PROCEDURE getPaymentInfo (p_recordset1 OUT  Types.cursor_type) AS

BEGIN 
	OPEN p_recordset1 FOR
	SELECT customerNumber, checkNumber, paymentDate, amount  FROM Payments WHERE customerNumber is NOT NULL;
	
END;
/

-------

CREATE OR REPLACE PROCEDURE get103CustomerLim(custNo OUT NUMBER, custName OUT VARCHAR2) AS

BEGIN 
	custNo :=103;
	SELECT customerName INTO custName FROM Customers WHERE customerNumber=custNo; 
	
END;
/

------

CREATE OR REPLACE PROCEDURE get103CustomerFull(custNo OUT NUMBER, custName OUT VARCHAR2,contactLastName OUT VARCHAR2,phone OUT VARCHAR2, city OUT VARCHAR2) AS

BEGIN
	custNo :=103;
	SELECT customerName, contactLastName, phone, city INTO custName, contactLastName, phone, city FROM Customers WHERE customerNumber=custNo;
   	
END;
/

------

CREATE OR REPLACE PROCEDURE getCustomerFullWithNumber(custNo IN OUT NUMBER, custName OUT VARCHAR2, contactLastName OUT VARCHAR2,phone OUT VARCHAR2, city OUT VARCHAR2) AS

BEGIN
	
	SELECT customerName, customerNumber, contactLastName, phone, city  INTO custName, custNo, contactLastName, phone, city FROM Customers WHERE customerNumber=custNo;
		
END;
/

------
CREATE OR REPLACE
PROCEDURE getCustomersByNumber(custNo IN INTEGER, my_ref_cursor OUT SYS_REFCURSOR, custName OUT STRING) AS
   BEGIN
   SELECT customerName INTO custName FROM Customers WHERE customerNumber=custNo;
   OPEN my_ref_cursor FOR
	    SELECT contactFirstName,phone
            FROM   Customers
            WHERE  customerNumber > custNo;
   END getCustomersByNumber;
/
------
CREATE OR REPLACE
PROCEDURE addAndGetMemberGroup(groupNum IN NUMBER, name IN VARCHAR2, city IN VARCHAR2,
member IN mem_array_type, groupNameRef OUT SYS_REFCURSOR ) AS
BEGIN
    INSERT into Groups (GroupNum, Name, City, Members)  values (groupNum, name, city, member);
	OPEN groupNameRef FOR
	    SELECT Name
	    FROM Groups
	    WHERE GroupNum = groupNum;
END;
/

------


