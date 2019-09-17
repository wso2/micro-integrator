CREATE TABLE Customers(
	customerNumber NUMBER,
	customerName VARCHAR2(50),
	contactLastName VARCHAR2(50),
	contactFirstName VARCHAR2(50),
	phone VARCHAR2(50),
	addressLine1 VARCHAR2(50),
	addressLine2 VARCHAR2(50),
	city VARCHAR2(50),
	state VARCHAR2(50),
	postalCode VARCHAR2(15),
	country VARCHAR2(50),
	salesRepEmployeeNumber NUMBER,
	creditLimit BINARY_DOUBLE
);

ALTER TABLE Customers ADD CONSTRAINT customers_pk PRIMARY KEY (customerNumber);

CREATE TABLE Employees(
	employeeNumber NUMBER,
	lastName VARCHAR2(50),
	firstName VARCHAR2(50),
	extension VARCHAR2(10),
	email VARCHAR2(100),
	officeCode VARCHAR2(10),
	reportsTo NUMBER,
	jobTitle VARCHAR2(50) 
);

ALTER TABLE Employees ADD CONSTRAINT employees_pk PRIMARY KEY (employeeNumber );

CREATE TABLE Offices(
	officeCode VARCHAR2(10),
	city VARCHAR2(50),
	phone VARCHAR2(50),
	addressLine1 VARCHAR2(50),
	addressLine2 VARCHAR2(50),
	state VARCHAR2(50),
	country VARCHAR2(50),
	postalCode VARCHAR2(15),
	territory VARCHAR2(10)
);

ALTER TABLE Offices ADD CONSTRAINT offices_pk PRIMARY KEY (officeCode );

CREATE TABLE Groups (
     GroupNum NUMBER,
     Name VARCHAR2(10),
     City VARCHAR2(20),
     Members mem_array_type
);

ALTER TABLE Groups ADD CONSTRAINT groups_pk PRIMARY KEY (GroupNum);

CREATE TABLE Products(
	productCode VARCHAR2(15),
	productName VARCHAR2(70),
	productLine VARCHAR2(50),
	productScale VARCHAR2(10),
	productVendor VARCHAR2(50),
	productDescription VARCHAR2(4000),
	quantityInStock NUMBER,
	buyPrice BINARY_DOUBLE,
	MSRP BINARY_DOUBLE
);

ALTER TABLE Products ADD CONSTRAINT products_pk PRIMARY KEY (productCode );

CREATE TABLE ProductLines(
	productLine VARCHAR2(50),
	textDescription VARCHAR2(4000),
	htmlDescription VARCHAR2(4000),
	image BLOB
);

ALTER TABLE ProductLines ADD CONSTRAINT productLines_pk PRIMARY KEY (productLine );

CREATE TABLE Orders(
	orderNumber NUMBER,
	orderDate DATE,
	requiredDate DATE,
	shippedDate DATE,
	status VARCHAR2(15),
	comments VARCHAR2(4000),
	customerNumber NUMBER 
);

ALTER TABLE Orders ADD CONSTRAINT orders_pk PRIMARY KEY (orderNumber );
CREATE INDEX orders_cutomer ON Orders( customerNumber );

CREATE TABLE OrderDetails(
	orderNumber NUMBER,
	productCode VARCHAR2(15),
	quantityOrdered NUMBER,
	priceEach BINARY_DOUBLE,
	orderLineNumber NUMBER);
	
ALTER TABLE OrderDetails ADD CONSTRAINT orderDetails_pk PRIMARY KEY (orderNumber, productCode );

CREATE TABLE Payments(
	customerNumber NUMBER,
	checkNumber VARCHAR2(50),
	paymentDate DATE,
	amount BINARY_DOUBLE 
);

ALTER TABLE Payments ADD CONSTRAINT payments_pk PRIMARY KEY (customerNumber, checkNumber );

CREATE TABLE department(
	id NUMBER,
	name VARCHAR2(200)
);

CREATE TABLE BinaryData(
	id NUMBER,
	data BLOB
);


